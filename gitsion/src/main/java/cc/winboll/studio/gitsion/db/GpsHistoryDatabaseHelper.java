package cc.winboll.studio.gitsion.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class GpsHistoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "gps_history.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "gps_history";
    public static final String COL_ID = "_id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_LOCATION_TIME = "location_time";
    public static final String COL_RECORD_TIME = "record_time";
    public static final String COL_TYPE = "type";
    public static final String COL_SID = "sid";
    public static final String COL_DESC = "description";

    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_SIM = 1;

    private static final String CREATE_TABLE =
        "CREATE TABLE " + TABLE_NAME + " ("
        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_LATITUDE + " REAL NOT NULL, "
        + COL_LONGITUDE + " REAL NOT NULL, "
        + COL_LOCATION_TIME + " INTEGER NOT NULL, "
        + COL_RECORD_TIME + " INTEGER NOT NULL, "
        + COL_TYPE + " INTEGER NOT NULL DEFAULT " + TYPE_SYSTEM + ", "
        + COL_SID + " TEXT, "
        + COL_DESC + " TEXT"
        + ")";

    public GpsHistoryDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
