package cc.winboll.studio.libgitsion.model;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:25
 */

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class GpsSubscribeResult implements Parcelable {

    private final String subscribeUniqueId;
    private final int resultCode;
    private final String resultDesc;
    private final int gpsRunningState;
    private final long realEffectiveInterval;
    private final long currentTimeStamp;
    private final double latitude;
    private final double longitude;
    private final long locationTime;

    public GpsSubscribeResult(String subscribeUniqueId,
                              int resultCode,
                              String resultDesc,
                              int gpsRunningState,
                              long realEffectiveInterval,
                              long currentTimeStamp,
                              double latitude,
                              double longitude,
                              long locationTime) {
        this.subscribeUniqueId = subscribeUniqueId;
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
        this.gpsRunningState = gpsRunningState;
        this.realEffectiveInterval = realEffectiveInterval;
        this.currentTimeStamp = currentTimeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationTime = locationTime;
    }

    public String getSubscribeUniqueId() {
        return subscribeUniqueId;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public int getGpsRunningState() {
        return gpsRunningState;
    }

    public long getRealEffectiveInterval() {
        return realEffectiveInterval;
    }

    public long getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getLocationTime() {
        return locationTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subscribeUniqueId);
        dest.writeInt(resultCode);
        dest.writeString(resultDesc);
        dest.writeInt(gpsRunningState);
        dest.writeLong(realEffectiveInterval);
        dest.writeLong(currentTimeStamp);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(locationTime);
    }

    public static final Creator<GpsSubscribeResult> CREATOR = new Creator<GpsSubscribeResult>() {
        @Override
        public GpsSubscribeResult createFromParcel(Parcel in) {
            return new GpsSubscribeResult(
				in.readString(),
				in.readInt(),
				in.readString(),
				in.readInt(),
				in.readLong(),
				in.readLong(),
				in.readDouble(),
				in.readDouble(),
				in.readLong()
            );
        }

        @Override
        public GpsSubscribeResult[] newArray(int size) {
            return new GpsSubscribeResult[size];
        }
    };

    public Bundle convertToBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("sid", subscribeUniqueId);
        bundle.putInt("code", resultCode);
        bundle.putString("desc", resultDesc);
        bundle.putInt("gpsState", gpsRunningState);
        bundle.putLong("realInterval", realEffectiveInterval);
        bundle.putLong("time", currentTimeStamp);
        bundle.putDouble("lat", latitude);
        bundle.putDouble("lng", longitude);
        bundle.putLong("locTime", locationTime);
        return bundle;
    }

    public static GpsSubscribeResult createByBundle(Bundle bundle) {
        return new GpsSubscribeResult(
			bundle.getString("sid"),
			bundle.getInt("code"),
			bundle.getString("desc"),
			bundle.getInt("gpsState"),
			bundle.getLong("realInterval"),
			bundle.getLong("time"),
			bundle.getDouble("lat"),
			bundle.getDouble("lng"),
			bundle.getLong("locTime")
        );
    }
}

