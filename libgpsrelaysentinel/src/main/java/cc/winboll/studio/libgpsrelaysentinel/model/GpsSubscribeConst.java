package cc.winboll.studio.libgpsrelaysentinel.model;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:22
 * WinBoLL Studio
 * Java7 | API26-30
 */
public final class GpsSubscribeConst {
	
	// 新增：GPS定位推送广播
	public static final String ACTION_GPS_LOCATION = "cc.winboll.studio.ACTION_GPS_LOCATION";

    //订阅运行模式
    public static final int SUB_TYPE_ALL = 1;
    public static final int SUB_TYPE_STEP_DISTANCE = 2;

    //原始数据订阅类型
    public static final int SUBSCRIBE_TYPE_LOCATION = 1;
    public static final int SUBSCRIBE_TYPE_SATELLITE = 2;
    public static final int SUBSCRIBE_TYPE_NMEA = 3;

    //订阅返回码
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_PERMISSION_DENY = 1;
    public static final int RESULT_PARAM_ERROR = 2;
    public static final int RESULT_GPS_NOT_AVAILABLE = 3;
    public static final int RESULT_SYSTEM_LIMIT = 4;

    //GPS设备状态
    public static final int GPS_STATE_CLOSE = 0;
    public static final int GPS_STATE_SCANNING = 1;
    public static final int GPS_STATE_LOCATED = 2;
    public static final int GPS_STATE_SIGNAL_WEAK = 3;

    //广播Action
    public static final String ACTION_SUBSCRIBE_REQUEST = "cc.winboll.studio.GPS_SUBSCRIBE_REQUEST";
    public static final String ACTION_SUBSCRIBE_CALLBACK = "cc.winboll.studio.GPS_SUBSCRIBE_CALLBACK";

    //超时毫秒
    public static final long SUBSCRIBE_TIME_OUT = 5000;

    //地球半径 距离计算常量
    public static final double EARTH_RADIUS = 6378137.0;
}

