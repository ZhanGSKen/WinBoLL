package cc.winboll.studio.autonfc.nfc;

import java.util.HashMap;
import java.util.Map;

public class NfcStateMonitor {
    private static Map<String, OnNfcStateListener> sListenerMap = new HashMap<>();
    private static boolean sIsRunning = false;

    public static void startMonitor() {
        if (sIsRunning) return;
        sListenerMap = new HashMap<>();
        sIsRunning = true;
    }

    public static void stopMonitor() {
        if (!sIsRunning) return;
        sIsRunning = false;
        if (sListenerMap != null) {
            sListenerMap.clear();
            sListenerMap = null;
        }
    }

    // 你原来的方法名：registerListener
    public static void registerListener(String key, OnNfcStateListener listener) {
        if (!sIsRunning || listener == null) return;
        sListenerMap.put(key, listener);
    }

    public static void unregisterListener(String key) {
        if (!sIsRunning || key == null) return;
        sListenerMap.remove(key);
    }

    public static void notifyNfcConnected() {
        if (!sIsRunning) return;
        for (OnNfcStateListener l : sListenerMap.values()) {
            l.onNfcConnected();
        }
    }

    public static void notifyNfcDisconnected() {
        if (!sIsRunning) return;
        for (OnNfcStateListener l : sListenerMap.values()) {
            l.onNfcDisconnected();
        }
    }

    public static void notifyReadSuccess(String data) {
        if (!sIsRunning) return;
        for (OnNfcStateListener l : sListenerMap.values()) {
            l.onNfcReadSuccess(data);
        }
    }

    public static void notifyReadFail(String error) {
        if (!sIsRunning) return;
        for (OnNfcStateListener l : sListenerMap.values()) {
            l.onNfcReadFail(error);
        }
    }

    public static void notifyWriteSuccess() {
        if (!sIsRunning) return;
        for (OnNfcStateListener l : sListenerMap.values()) {
            l.onNfcWriteSuccess();
        }
    }

    public static void notifyWriteFail(String error) {
        if (!sIsRunning) return;
        for (OnNfcStateListener l : sListenerMap.values()) {
            l.onNfcWriteFail(error);
        }
    }
}

