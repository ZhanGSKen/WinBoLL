package cc.winboll.studio.autonfc.nfc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Binder;
import android.os.IBinder;

import cc.winboll.studio.autonfc.MainActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

public class AutoNFCService extends Service {

    public static final String TAG = "AutoNFCService";

    // ================= 已修改：更新为 Beta 包名 =================
    public static final String ACTION_BUILD = "cc.winboll.studio.winboll.termux.NfcTermuxBridgeActivity.ACTION_BUILD";
    public static final String ACTION_BUILD_VIEW = "cc.winboll.studio.winboll.termux.NfcTermuxBridgeActivity.ACTION_BUILD_VIEW";

    private final IBinder mBinder = new LocalBinder();
    private String mNfcData;
    private MainActivity mActivity; // 持有 Activity 引用，用于回调

    // ========================= 生命周期 =========================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate() -> 服务创建");
        // 移除：startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy() -> 服务已停止");
        mActivity = null; // 释放引用
    }

    // ========================= 服务绑定 =========================
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind() -> 服务被绑定");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d(TAG, "onUnbind() -> 服务解绑");
        // 移除：stopForeground(true);
        stopSelf();
        return super.onUnbind(intent);
    }

    // ========================= 对外暴露方法 =========================
    /**
     * 绑定 Activity，用于回调显示对话框
     */
    public void attachActivity(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * 处理 NFC 意图
     */
    public void handleNfcIntent(Intent intent) {
        LogUtils.d(TAG, "handleNfcIntent() -> 开始处理");

        if (intent == null) {
            LogUtils.e(TAG, "handleNfcIntent() -> 参数 intent 为空");
            return;
        }

        String action = intent.getAction();
        LogUtils.d(TAG, "handleNfcIntent() -> Action = " + action);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
			|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
			|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            LogUtils.d(TAG, "handleNfcIntent() -> 匹配 NFC 动作");

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag == null) {
                LogUtils.e(TAG, "handleNfcIntent() -> Tag 为空");
                return;
            }

            LogUtils.d(TAG, "handleNfcIntent() -> Tag ID = " + bytesToHexString(tag.getId()));
            LogUtils.d(TAG, "handleNfcIntent() -> Tech List = " + Arrays.toString(tag.getTechList()));

            parseNdefData(tag);
        }
    }

    // ========================= 内部业务 =========================
    private void parseNdefData(Tag tag) {
        LogUtils.d(TAG, "parseNdefData() -> 开始解析");

        if (tag == null) return;

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            LogUtils.e(TAG, "parseNdefData() -> 不支持 NDEF 格式");
            return;
        }

        try {
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();

            if (msg == null || msg.getRecords() == null || msg.getRecords().length == 0) {
                LogUtils.w(TAG, "parseNdefData() -> 卡片无数据");
                return;
            }

            NdefRecord record = msg.getRecords()[0];
            byte[] payload = record.getPayload();

            int langLen = payload[0] & 0x3F;
            int start = 1 + langLen;

            if (start < payload.length) {
                mNfcData = new String(payload, start, payload.length - start, Charset.forName("UTF-8"));
                LogUtils.d(TAG, "parseNdefData() -> 读卡成功: " + mNfcData);

                // 关键：回调给 Activity 弹框，此时 Activity 一定是存活状态
                if (mActivity != null) {
                    mActivity.showNfcActionDialog(mNfcData);
                }
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "parseNdefData() -> 读取失败", e);
        } finally {
            try {
                ndef.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }

    /**
     * 执行 Termux 命令
     */
    public void executeTermuxCommand(String action, String nfcData) {
        LogUtils.d(TAG, "executeTermuxCommand() -> 开始执行");

        if (nfcData == null || nfcData.isEmpty()) {
            ToastUtils.show("数据错误");
            return;
        }

        try {
            LogUtils.d(TAG, "executeTermuxCommand() -> 发送指令: " + nfcData);

            Intent bridgeIntent = new Intent(action);

            // ================= 已修改：使用 Beta 包名 =================
            bridgeIntent.setClassName(
                "cc.winboll.studio.winboll.beta",
                "cc.winboll.studio.winboll.termux.NfcTermuxBridgeActivity"
            );

            bridgeIntent.putExtra(Intent.EXTRA_TEXT, nfcData);
            bridgeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(bridgeIntent);
            ToastUtils.show("指令已发送");
        } catch (Exception e) {
            LogUtils.e(TAG, "executeTermuxCommand() -> 发送失败", e);
            ToastUtils.show("发送失败");
        }
    }

    // ========================= 工具方法 =========================
    private String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // ========================= Binder =========================
    public class LocalBinder extends Binder {
        public AutoNFCService getService() {
            return AutoNFCService.this;
        }
    }
}

