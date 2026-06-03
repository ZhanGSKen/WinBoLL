package cc.winboll.studio.libgitsion.model;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:24
 */
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class GpsSubscribeMsg implements Parcelable {

    private final String subscribePackage;
    private final int subscribeMode;
    private final float stepDistanceM;

    private final int subscribeType;
    private final long updateInterval;
    private final float minDistance;
    private final boolean backgroundPush;
    private final String subscribeUniqueId;

    public GpsSubscribeMsg(String subscribePackage,
                           int subscribeMode,
                           float stepDistanceM,
                           int subscribeType,
                           long updateInterval,
                           float minDistance,
                           boolean backgroundPush,
                           String subscribeUniqueId) {
        this.subscribePackage = subscribePackage;
        this.subscribeMode = subscribeMode;
        this.stepDistanceM = stepDistanceM;
        this.subscribeType = subscribeType;
        this.updateInterval = updateInterval;
        this.minDistance = minDistance;
        this.backgroundPush = backgroundPush;
        this.subscribeUniqueId = subscribeUniqueId;
    }

    public String getSubscribePackage() {
        return subscribePackage;
    }

    public int getSubscribeMode() {
        return subscribeMode;
    }

    public float getStepDistanceM() {
        return stepDistanceM;
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public boolean isBackgroundPush() {
        return backgroundPush;
    }

    public String getSubscribeUniqueId() {
        return subscribeUniqueId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subscribePackage);
        dest.writeInt(subscribeMode);
        dest.writeFloat(stepDistanceM);
        dest.writeInt(subscribeType);
        dest.writeLong(updateInterval);
        dest.writeFloat(minDistance);
        dest.writeByte((byte) (backgroundPush ? 1 : 0));
        dest.writeString(subscribeUniqueId);
    }

    public static final Creator<GpsSubscribeMsg> CREATOR = new Creator<GpsSubscribeMsg>() {
        @Override
        public GpsSubscribeMsg createFromParcel(Parcel in) {
            return new GpsSubscribeMsg(
				in.readString(),
				in.readInt(),
				in.readFloat(),
				in.readInt(),
				in.readLong(),
				in.readFloat(),
				in.readByte() == 1,
				in.readString()
            );
        }

        @Override
        public GpsSubscribeMsg[] newArray(int size) {
            return new GpsSubscribeMsg[size];
        }
    };

    public Bundle convertToBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("pkg", subscribePackage);
        bundle.putInt("subMode",subscribeMode);
        bundle.putFloat("stepM",stepDistanceM);
        bundle.putInt("type", subscribeType);
        bundle.putLong("interval", updateInterval);
        bundle.putFloat("distance", minDistance);
        bundle.putBoolean("bgPush", backgroundPush);
        bundle.putString("sid", subscribeUniqueId);
        return bundle;
    }

    public static GpsSubscribeMsg createByBundle(Bundle bundle) {
        return new GpsSubscribeMsg(
			bundle.getString("pkg"),
			bundle.getInt("subMode"),
			bundle.getFloat("stepM"),
			bundle.getInt("type"),
			bundle.getLong("interval"),
			bundle.getFloat("distance"),
			bundle.getBoolean("bgPush"),
			bundle.getString("sid")
        );
    }
}

