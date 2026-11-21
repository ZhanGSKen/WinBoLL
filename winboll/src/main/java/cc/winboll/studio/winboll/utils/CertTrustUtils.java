package cc.winboll.studio.winboll.utils;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2026/10/26
 * @Describe 证书信任工具：检测 WinBoLL 根 CA 是否已安装（系统/用户信任库），以及从存储读取证书。
 * 说明：本类不内置任何证书或私钥，仅保存根证书的 SHA-256 指纹（公开标识）用于比对。
 */
public class CertTrustUtils {

    public static final String TAG = "CertTrustUtils";

    /** WinBoLL 根 CA 的默认别名 */
    public static final String WINBOLL_CERT_ALIAS = "WinBoLL";

    /**
     * WinBoLL 根 CA 的 SHA-256 指纹（仅为公开的证书标识，用于判断该证书是否已被信任）。
     * Subject CN=WinBoLL，自签。
     */
    public static final String WINBOLL_ROOT_SHA256 =
            "65:F8:12:F2:2C:A4:AC:72:08:D0:6F:8D:7B:CE:92:27:E3:88:4B:B7:71:A1:F6:68:22:86:79:A0:EA:21:57:9B";

    /**
     * 检测 WinBoLL 根 CA 是否在当前应用的受信任 CA 列表中（系统 CA + 用户 CA，
     * 受 network_security_config 中 trust-anchors 配置影响）。
     *
     * @return true 表示已安装且被信任
     */
    public static boolean isWinBoLLRootTrusted() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            TrustManager[] managers = tmf.getTrustManagers();
            if (managers != null) {
                for (int i = 0; i < managers.length; i++) {
                    TrustManager tm = managers[i];
                    if (tm instanceof X509TrustManager) {
                        X509Certificate[] issuers = ((X509TrustManager) tm).getAcceptedIssuers();
                        if (issuers != null) {
                            for (int j = 0; j < issuers.length; j++) {
                                String fp = sha256Fingerprint(issuers[j].getEncoded());
                                if (WINBOLL_ROOT_SHA256.equalsIgnoreCase(fp)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 计算 DER 编码证书的 SHA-256 指纹，格式为冒号分隔的大写十六进制。
     */
    public static String sha256Fingerprint(byte[] der) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(der);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                if (i > 0) {
                    sb.append(':');
                }
                sb.append(String.format("%02X", digest[i] & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从存储的 content Uri 读取证书文件，兼容 PEM 与 DER 编码，统一返回 DER 编码字节，
     * 供 {@link android.security.KeyChain#createInstallIntent()} 的 EXTRA_CERTIFICATE 使用。
     *
     * @param context 上下文
     * @param uri     用户通过文件选择器选中的证书文件
     * @return DER 编码的 X.509 证书字节
     */
    public static byte[] readCertificateDer(Context context, Uri uri) throws Exception {
        InputStream is = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                throw new Exception("cannot open certificate uri: " + uri);
            }
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                bos.write(buffer, 0, n);
            }
            byte[] raw = bos.toByteArray();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(raw));
            return cert.getEncoded();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
