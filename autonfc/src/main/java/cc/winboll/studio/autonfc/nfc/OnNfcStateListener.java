package cc.winboll.studio.autonfc.nfc;

public interface OnNfcStateListener {
    void onNfcConnected(); // 无参数！
    void onNfcDisconnected();
    void onNfcReadSuccess(String data);
    void onNfcReadFail(String error);
    void onNfcWriteSuccess();
    void onNfcWriteFail(String error);
}

