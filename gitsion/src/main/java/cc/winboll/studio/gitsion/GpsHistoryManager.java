package cc.winboll.studio.gitsion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cc.winboll.studio.gitsion.db.GpsHistoryDatabaseHelper;
import cc.winboll.studio.gitsion.model.GpsHistoryRecord;
import cc.winboll.studio.libgitsion.model.GpsSubscribeResult;

public final class GpsHistoryManager {

    private static final int MAX_RECORDS = 2000;
    private static final int TRIM_COUNT = 1000;
    private static final GpsHistoryManager sInstance = new GpsHistoryManager();
    private final List<Runnable> mListeners = new ArrayList<Runnable>();
    private GpsHistoryDatabaseHelper mDbHelper;

    private GpsHistoryManager() {}

    public static GpsHistoryManager getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mDbHelper = new GpsHistoryDatabaseHelper(context.getApplicationContext());
    }

    public void addRecord(GpsSubscribeResult result, int type) {
        if (mDbHelper == null) return;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GpsHistoryDatabaseHelper.COL_LATITUDE, result.getLatitude());
        cv.put(GpsHistoryDatabaseHelper.COL_LONGITUDE, result.getLongitude());
        cv.put(GpsHistoryDatabaseHelper.COL_LOCATION_TIME, result.getLocationTime());
        cv.put(GpsHistoryDatabaseHelper.COL_RECORD_TIME, System.currentTimeMillis());
        cv.put(GpsHistoryDatabaseHelper.COL_TYPE, type);
        cv.put(GpsHistoryDatabaseHelper.COL_SID, result.getSubscribeUniqueId());
        cv.put(GpsHistoryDatabaseHelper.COL_DESC, result.getResultDesc());
        db.insert(GpsHistoryDatabaseHelper.TABLE_NAME, null, cv);

        trimIfNeeded(db);
        notifyListeners();
    }

    public void addSystemRecord(GpsSubscribeResult result) {
        addRecord(result, GpsHistoryDatabaseHelper.TYPE_SYSTEM);
    }

    public void addSimRecord(GpsSubscribeResult result) {
        addRecord(result, GpsHistoryDatabaseHelper.TYPE_SIM);
    }

    public List<GpsHistoryRecord> getRecords() {
        List<GpsHistoryRecord> list = new ArrayList<GpsHistoryRecord>();
        if (mDbHelper == null) return list;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(GpsHistoryDatabaseHelper.TABLE_NAME, null, null, null,
            null, null, GpsHistoryDatabaseHelper.COL_ID + " DESC", null);
        while (c.moveToNext()) {
            list.add(parseCursor(c));
        }
        c.close();
        return list;
    }

    public int getSystemCount() {
        return countByType(GpsHistoryDatabaseHelper.TYPE_SYSTEM);
    }

    public int getSimCount() {
        return countByType(GpsHistoryDatabaseHelper.TYPE_SIM);
    }

    public void clear() {
        if (mDbHelper == null) return;
        mDbHelper.getWritableDatabase().delete(GpsHistoryDatabaseHelper.TABLE_NAME, null, null);
        notifyListeners();
    }

    public void addListener(Runnable listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    private void notifyListeners() {
        synchronized (mListeners) {
            for (Runnable r : mListeners) {
                r.run();
            }
        }
    }

    private int countByType(int type) {
        if (mDbHelper == null) return 0;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT COUNT(*) FROM " + GpsHistoryDatabaseHelper.TABLE_NAME
            + " WHERE " + GpsHistoryDatabaseHelper.COL_TYPE + "=?",
            new String[]{String.valueOf(type)});
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    private void trimIfNeeded(SQLiteDatabase db) {
        Cursor c = db.rawQuery(
            "SELECT COUNT(*) FROM " + GpsHistoryDatabaseHelper.TABLE_NAME, null);
        int total = 0;
        if (c.moveToFirst()) {
            total = c.getInt(0);
        }
        c.close();
        if (total > MAX_RECORDS) {
            db.execSQL("DELETE FROM " + GpsHistoryDatabaseHelper.TABLE_NAME
                + " WHERE " + GpsHistoryDatabaseHelper.COL_ID + " IN ("
                + " SELECT " + GpsHistoryDatabaseHelper.COL_ID
                + " FROM " + GpsHistoryDatabaseHelper.TABLE_NAME
                + " ORDER BY " + GpsHistoryDatabaseHelper.COL_ID + " ASC"
                + " LIMIT " + TRIM_COUNT + ")");
        }
    }

    private static GpsHistoryRecord parseCursor(Cursor c) {
        return new GpsHistoryRecord(
            c.getLong(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_ID)),
            c.getDouble(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_LATITUDE)),
            c.getDouble(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_LONGITUDE)),
            c.getLong(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_LOCATION_TIME)),
            c.getLong(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_RECORD_TIME)),
            c.getInt(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_TYPE)),
            c.getString(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_SID)),
            c.getString(c.getColumnIndexOrThrow(GpsHistoryDatabaseHelper.COL_DESC))
        );
    }
}
