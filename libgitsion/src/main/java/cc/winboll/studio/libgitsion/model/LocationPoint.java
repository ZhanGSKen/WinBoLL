package cc.winboll.studio.libgitsion.model;

import java.io.Serializable;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:23
 * 订阅者基准定点坐标
 * 每次推送成功自动更新
 */
public final class LocationPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double latitude;
    private final double longitude;
    private final long recordTime;

    public LocationPoint(double latitude, double longitude, long recordTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordTime = recordTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getRecordTime() {
        return recordTime;
    }
}

