package cc.winboll.studio.positions.services;

import android.os.Handler;
import android.os.Looper;

import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.positions.handlers.AppIdleRunningModeHandler;
import cc.winboll.studio.positions.models.PositionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 空转GPS模拟服务
 * 在应用空转模式下，模拟系统GPS服务向客户端发送坐标数据
 */
public class IdleGpsService {

    private static final long MOCK_INTERVAL_MS = 5000; // 模拟坐标更新间隔（5秒）
    private static final long BEARING_INTERVAL_MS = 1000; // 角动量递增间隔（1秒）
    private static final double EARTH_RADIUS_M = 6371000; // 地球平均半径（米）
    private static final double ANCHOR_LAT = 39.9042; // 固定锚点纬度（北京）
    private static final double ANCHOR_LON = 116.4074; // 固定锚点经度（北京）
    private static final double CIRCLE_RADIUS_M = 50; // 移动轨迹半径（米）

    private static IdleGpsService instance;
    private final List<MainService.GpsUpdateListener> listeners = new ArrayList<>();
    private final Handler handler;
    private final Runnable updateRunnable;
    private final Runnable bearingRunnable;
    private final PositionModel mockPosition;
    private boolean isRunning;
    private int currentBearing = 1; // 当前角动量度数（1-360）

    private IdleGpsService() {
        handler = new Handler(Looper.getMainLooper());
        mockPosition = new PositionModel();
        mockPosition.setPositionId("mock_idle_pos");
        mockPosition.setMemo("空转模拟坐标");

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    calculatePosition();
                    notifyListeners(mockPosition);
                    AppIdleRunningModeHandler.sendIdleLog("模拟GPS数据更新 -> 纬度:" + mockPosition.getLatitude() + ", 经度:" + mockPosition.getLongitude() + ", 角度:" + currentBearing + "°");
                    handler.postDelayed(this, MOCK_INTERVAL_MS);
                }
            }
        };

        bearingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    currentBearing++;
                    if (currentBearing > 360) {
                        currentBearing = 1;
                    }
                    handler.postDelayed(this, BEARING_INTERVAL_MS);
                }
            }
        };
    }

    private void calculatePosition() {
        double bearingRad = Math.toRadians(currentBearing);
        double angularDistance = CIRCLE_RADIUS_M / EARTH_RADIUS_M;

        double anchorLatRad = Math.toRadians(ANCHOR_LAT);
        double anchorLonRad = Math.toRadians(ANCHOR_LON);

        double newLatRad = Math.asin(
            Math.sin(anchorLatRad) * Math.cos(angularDistance) +
            Math.cos(anchorLatRad) * Math.sin(angularDistance) * Math.cos(bearingRad)
        );

        double newLonRad = anchorLonRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(anchorLatRad),
            Math.cos(angularDistance) - Math.sin(anchorLatRad) * Math.sin(newLatRad)
        );

        mockPosition.setLatitude(Math.toDegrees(newLatRad));
        mockPosition.setLongitude(Math.toDegrees(newLonRad));
    }

    public static IdleGpsService getInstance() {
        if (instance == null) {
            instance = new IdleGpsService();
        }
        return instance;
    }

    /**
     * 启动空转模拟服务
     */
    public void start() {
        if (isRunning) return;
        isRunning = true;
        currentBearing = 1;
        AppIdleRunningModeHandler.sendIdleLog("空转GPS服务已启动");
        notifyStatusChange("空转GPS服务已启动");
        ToastUtils.show("空转GPS服务已启动");
        handler.post(updateRunnable);
        handler.post(bearingRunnable);
    }

    /**
     * 停止空转模拟服务
     */
    public void stop() {
        if (!isRunning) return;
        isRunning = false;
        handler.removeCallbacks(updateRunnable);
        handler.removeCallbacks(bearingRunnable);
        notifyStatusChange("空转GPS服务已停止");
        AppIdleRunningModeHandler.sendIdleLog("空转GPS服务已停止");
    }

    /**
     * 注册GPS监听
     */
    public void registerGpsUpdateListener(MainService.GpsUpdateListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
        if (!isRunning) {
            start();
        }
    }

    /**
     * 注销GPS监听
     */
    public void unregisterGpsUpdateListener(MainService.GpsUpdateListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
        if (listeners.isEmpty() && isRunning) {
            stopMockUpdate();
        }
    }

    private void startMockUpdate() {
        if (isRunning) return;
        isRunning = true;
        currentBearing = 1;
        notifyStatusChange("空转GPS服务已启动");
        handler.post(updateRunnable);
        handler.post(bearingRunnable);
    }

    private void stopMockUpdate() {
        isRunning = false;
        handler.removeCallbacks(updateRunnable);
        handler.removeCallbacks(bearingRunnable);
        notifyStatusChange("空转GPS服务已停止");
    }

    private void notifyListeners(PositionModel pos) {
        synchronized (listeners) {
            for (MainService.GpsUpdateListener listener : listeners) {
                listener.onGpsPositionUpdated(pos);
            }
        }
    }

    private void notifyStatusChange(String status) {
        synchronized (listeners) {
            for (MainService.GpsUpdateListener listener : listeners) {
                listener.onGpsStatusChanged(status);
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
