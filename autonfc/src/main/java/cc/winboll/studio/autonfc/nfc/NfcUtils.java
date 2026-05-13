package cc.winboll.studio.autonfc.nfc;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/03/16 14:26
 */
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import cc.winboll.studio.autonfc.models.NfcTermuxCmd;
import cc.winboll.studio.libappbase.LogUtils;
import java.nio.charset.Charset;
import java.util.Locale;

public class NfcUtils {

    public static final String TAG = "NfcUtils";
    private static Gson sGson = new Gson();

    // -------------------------------------------------------------------------
    // 读取 NFC 标签并解析为 NfcTermuxCmd
    // -------------------------------------------------------------------------
    public static NfcTermuxCmd readTag(Tag tag) throws Exception {
        if (tag == null) {
            LogUtils.e(TAG, "readTag: tag is null");
            return null;
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            LogUtils.e(TAG, "readTag: 不支持 NDEF");
            return null;
        }

        try {
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();
            if (msg == null) return null;

            NdefRecord[] records = msg.getRecords();
            if (records == null || records.length == 0) return null;

            byte[] payload = records[0].getPayload();
            int status = payload[0] & 0xFF;
            int langLen = status & 0x3F;
            int start = 1 + langLen;

            if (start >= payload.length) return null;

            String json = new String(payload, start, payload.length - start, Charset.forName("UTF-8"));
            LogUtils.d(TAG, "readTag: 提取 JSON -> " + json);

            return sGson.fromJson(json, NfcTermuxCmd.class);
        } finally {
            if (ndef != null && ndef.isConnected()) {
                ndef.close();
            }
        }
    }

    // -------------------------------------------------------------------------
    // 写入 NfcTermuxCmd 到 NFC 标签
    // -------------------------------------------------------------------------
    public static void writeTag(Tag tag, NfcTermuxCmd cmd) throws Exception {
        if (tag == null) throw new Exception("tag is null");

        String json = sGson.toJson(cmd);
        writeJson(tag, json);
    }

    // -------------------------------------------------------------------------
    // 写入原始 JSON 字符串到 NFC
    // -------------------------------------------------------------------------
    public static void writeJson(Tag tag, String json) throws Exception {
        if (tag == null) throw new Exception("tag is null");

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) throw new Exception("标签不支持 NDEF");

        try {
            ndef.connect();
            int maxSize = ndef.getMaxSize();
            int realSize = json.getBytes(Charset.forName("UTF-8")).length;

            if (realSize > maxSize) {
                throw new Exception("数据过大 (" + realSize + ") > 容量 (" + maxSize + ")");
            }

            NdefRecord record = createTextRecord(json, true);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{record});

            ndef.writeNdefMessage(msg);
            LogUtils.d(TAG, "writeJson: 写入成功");
        } finally {
            if (ndef != null && ndef.isConnected()) {
                ndef.close();
            }
        }
    }

    // -------------------------------------------------------------------------
    // 创建 NFC 文本记录
    // -------------------------------------------------------------------------
    public static NdefRecord createTextRecord(String text, boolean isUtf8) {
        byte[] langBytes = "en".getBytes(Charset.forName("US-ASCII"));
        byte[] textBytes = text.getBytes(Charset.forName(isUtf8 ? "UTF-8" : "UTF-16"));

        int status = isUtf8 ? 0 : 0x80;
        status |= langBytes.length & 0x3F;

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    // -------------------------------------------------------------------------
    // 辅助：JSON -> NfcTermuxCmd
    // -------------------------------------------------------------------------
    public static NfcTermuxCmd jsonToCmd(String json) throws JsonSyntaxException {
        return sGson.fromJson(json, NfcTermuxCmd.class);
    }

    // -------------------------------------------------------------------------
    // 辅助：NfcTermuxCmd -> JSON
    // -------------------------------------------------------------------------
    public static String cmdToJson(NfcTermuxCmd cmd) {
        return sGson.toJson(cmd);
    }
}

