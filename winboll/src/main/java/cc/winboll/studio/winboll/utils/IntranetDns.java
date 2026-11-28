package cc.winboll.studio.winboll.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Intranet DNS module: resolves *.studio.winboll with the configured intranet DNS
 * (primary, default 192.168.123.222) and falls back to a public DNS (secondary).
 *
 * Authentication rule: an intranet host name must be answered by the configured
 * intranet DNS. Answers for intranet names coming from the public DNS are reported
 * as untrusted, because a public resolver cannot legitimately own those names.
 *
 * Note: Android does not allow an app to change the system resolver. This module is
 * the app-side resolver used by diagnostics and by future API calls; WebView still
 * follows the system DNS, so the LAN DHCP should hand out 192.168.123.222 as primary.
 *
 * @Author ZhanGSKen&豆包&Big Pickle&MiMo
 * @Date 2026/11/28
 */
public class IntranetDns {

    public static final String TAG = "IntranetDns";

    public static final String PREFS_NAME = "winboll_intranet_dns";
    public static final String KEY_PRIMARY = "dns_primary";
    public static final String KEY_SECONDARY = "dns_secondary";

    /** Default intranet DNS server (LAN side). */
    public static final String DEFAULT_PRIMARY = "192.168.123.222";
    /** Default public DNS server, used only when the intranet one does not answer. */
    public static final String DEFAULT_SECONDARY = "223.5.5.5";

    /** Domain suffix served by the intranet DNS. */
    public static final String INTRANET_SUFFIX = ".studio.winboll";

    private static final int DNS_PORT = 53;
    private static final int UDP_TIMEOUT_MS = 1500;
    private static final int TCP_TIMEOUT_MS = 3000;
    private static final int TYPE_A = 1;
    private static final int CLASS_IN = 1;

    /** Result of one resolution attempt. */
    public static final class Result {
        public String server;
        public List<String> addresses = new ArrayList<String>();
        public long elapsedMs;
        public String error;
        public boolean trusted;

        public boolean isOk() {
            return error == null && addresses.size() > 0;
        }

        public String toDisplayString(String host) {
            StringBuilder sb = new StringBuilder();
            sb.append(host).append(" -> ");
            if (isOk()) {
                for (int i = 0; i < addresses.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(addresses.get(i));
                }
            } else {
                sb.append("解析失败");
                if (error != null) {
                    sb.append(" (").append(error).append(")");
                }
            }
            sb.append("\nDNS: ").append(server).append(" · ").append(elapsedMs).append(" ms");
            sb.append(" · ").append(trusted ? "内网应答（可信）" : "非内网应答（不可信）");
            return sb.toString();
        }
    }

    public static String getPrimary(Context context) {
        return getPrefs(context).getString(KEY_PRIMARY, DEFAULT_PRIMARY);
    }

    public static String getSecondary(Context context) {
        return getPrefs(context).getString(KEY_SECONDARY, DEFAULT_SECONDARY);
    }

    public static void saveServers(Context context, String primary, String secondary) {
        getPrefs(context).edit()
                .putString(KEY_PRIMARY, primary)
                .putString(KEY_SECONDARY, secondary)
                .apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** True when the host name belongs to the intranet suffix. */
    public static boolean isIntranetHost(String host) {
        return host != null && host.toLowerCase(Locale.US).endsWith(INTRANET_SUFFIX);
    }

    /**
     * Resolve the host with the intranet DNS first, then the public DNS.
     * Never throws; errors are reported inside the result.
     */
    public static Result resolve(Context context, String host) {
        String primary = getPrimary(context);
        String secondary = getSecondary(context);

        Result first = queryOnce(primary, host);
        first.trusted = isIntranetHost(host) && isIntranetServer(primary);
        if (first.isOk()) {
            return first;
        }

        Result second = queryOnce(secondary, host);
        second.trusted = isIntranetHost(host) && isIntranetServer(secondary);
        if (second.isOk()) {
            return second;
        }

        // Both failed: keep the intranet attempt as the reported failure.
        first.error = first.error == null ? "no answer" : first.error;
        return first;
    }

    private static boolean isIntranetServer(String server) {
        if (server == null) {
            return false;
        }
        String[] parts = server.trim().split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[1]);
            if (a == 10) {
                return true;
            }
            if (a == 172 && b >= 16 && b <= 31) {
                return true;
            }
            return a == 192 && b == 168;
        } catch (Exception e) {
            return false;
        }
    }

    private static Result queryOnce(String server, String host) {
        Result result = new Result();
        result.server = server;
        long started = System.currentTimeMillis();
        try {
            byte[] query = buildQuery(host);
            // UDP first (cheap); some firewalls drop UDP/53, so fall back to TCP/53.
            Result udp = queryUdp(server, host, query);
            if (udp.isOk()) {
                result.addresses = udp.addresses;
            } else {
                Result tcp = queryTcp(server, host, query);
                if (tcp.isOk()) {
                    result.addresses = tcp.addresses;
                } else {
                    result.error = tcp.error != null ? tcp.error : udp.error;
                }
            }
        } catch (Exception e) {
            result.error = e.getClass().getSimpleName();
        } finally {
            result.elapsedMs = System.currentTimeMillis() - started;
        }
        return result;
    }

    private static Result queryUdp(String server, String host, byte[] query) {
        Result result = new Result();
        result.server = server;
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(server.trim());
            socket = new DatagramSocket();
            socket.setSoTimeout(UDP_TIMEOUT_MS);
            DatagramPacket out = new DatagramPacket(query, query.length, address, DNS_PORT);
            socket.send(out);

            byte[] buffer = new byte[512];
            DatagramPacket in = new DatagramPacket(buffer, buffer.length);
            socket.receive(in);
            result.addresses = parseAnswers(buffer, in.getLength());
            if (result.addresses.isEmpty()) {
                result.error = "no A record";
            }
        } catch (Exception e) {
            result.error = e.getClass().getSimpleName();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    private static Result queryTcp(String server, String host, byte[] query) {
        Result result = new Result();
        result.server = server;
        Socket socket = null;
        try {
            InetAddress address = InetAddress.getByName(server.trim());
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, DNS_PORT), TCP_TIMEOUT_MS);
            socket.setSoTimeout(TCP_TIMEOUT_MS);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeShort(query.length);
            out.write(query);
            out.flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            int length = in.readUnsignedShort();
            byte[] response = new byte[length];
            in.readFully(response);
            result.addresses = parseAnswers(response, length);
            if (result.addresses.isEmpty()) {
                result.error = "no A record";
            }
        } catch (Exception e) {
            result.error = e.getClass().getSimpleName();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    private static byte[] buildQuery(String host) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Header: id=0x5742, flags=RD, qdcount=1, ancount=0, nscount=0, arcount=0
        bos.write(new byte[]{0x57, 0x42, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        String[] labels = host.toLowerCase(Locale.US).split("\\.");
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            if (label.length() == 0) {
                continue;
            }
            bos.write(label.length());
            bos.write(label.getBytes("US-ASCII"));
        }
        bos.write(0x00);
        bos.write(new byte[]{(byte) TYPE_A, 0x00, (byte) CLASS_IN, 0x00});
        return bos.toByteArray();
    }

    private static List<String> parseAnswers(byte[] data, int length) {
        List<String> list = new ArrayList<String>();
        try {
            int qdCount = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
            int anCount = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
            int offset = 12;
            for (int i = 0; i < qdCount; i++) {
                offset = skipName(data, offset);
                offset += 4; // qtype + qclass
            }
            for (int i = 0; i < anCount; i++) {
                offset = skipName(data, offset);
                if (offset + 10 > length) {
                    break;
                }
                int type = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
                int rdLength = ((data[offset + 8] & 0xFF) << 8) | (data[offset + 9] & 0xFF);
                offset += 10;
                if (type == TYPE_A && rdLength == 4 && offset + 4 <= length) {
                    String ip = (data[offset] & 0xFF) + "."
                            + (data[offset + 1] & 0xFF) + "."
                            + (data[offset + 2] & 0xFF) + "."
                            + (data[offset + 3] & 0xFF);
                    if (!list.contains(ip)) {
                        list.add(ip);
                    }
                }
                offset += rdLength;
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private static int skipName(byte[] data, int offset) {
        int guard = 0;
        while (offset < data.length && guard < 256) {
            int len = data[offset] & 0xFF;
            if (len == 0) {
                return offset + 1;
            }
            if ((len & 0xC0) == 0xC0) {
                return offset + 2;
            }
            offset += 1 + len;
            guard++;
        }
        return offset;
    }

    /** Format helper used by the settings page. */
    public static String describeServers(Context context) {
        String primary = getPrimary(context);
        String secondary = getSecondary(context);
        if (TextUtils.isEmpty(secondary)) {
            return primary;
        }
        return primary + " / " + secondary;
    }
}

