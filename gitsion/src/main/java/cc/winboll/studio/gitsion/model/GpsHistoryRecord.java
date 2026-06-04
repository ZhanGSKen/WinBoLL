package cc.winboll.studio.gitsion.model;

import cc.winboll.studio.gitsion.db.GpsHistoryDatabaseHelper;

public final class GpsHistoryRecord {

    private final long id;
    private final double latitude;
    private final double longitude;
    private final long locationTime;
    private final long recordTime;
    private final int type;
    private final String sid;
    private final String description;

    public GpsHistoryRecord(long id, double latitude, double longitude,
                            long locationTime, long recordTime,
                            int type, String sid, String description) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationTime = locationTime;
        this.recordTime = recordTime;
        this.type = type;
        this.sid = sid;
        this.description = description;
    }

    public long getId() {
        return id;
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

    public long getRecordTime() {
        return recordTime;
    }

    public int getType() {
        return type;
    }

    public boolean isSim() {
        return type == GpsHistoryDatabaseHelper.TYPE_SIM;
    }

    public String getSid() {
        return sid;
    }

    public String getDescription() {
        return description;
    }
}
