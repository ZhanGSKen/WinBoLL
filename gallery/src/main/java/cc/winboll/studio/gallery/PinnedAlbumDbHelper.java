package cc.winboll.studio.gallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cc.winboll.studio.libappbase.LogUtils;

public class PinnedAlbumDbHelper extends SQLiteOpenHelper {
    public static final String TAG = "PinnedAlbumDbHelper";
    private static final String DB_NAME = "pinned_album.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "pinned_albums";
    private static final String COLUMN_PATH = "album_path";
    
    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" 
        + COLUMN_PATH + " TEXT PRIMARY KEY)";
    
    private static PinnedAlbumDbHelper dbHelper;
    
    public static PinnedAlbumDbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new PinnedAlbumDbHelper(context.getApplicationContext());
        }
        return dbHelper;
    }
    
    public PinnedAlbumDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    
    public void pinAlbum(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATH, albumPath);
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        LogUtils.d(TAG, "pinAlbum: " + albumPath);
    }
    
    public void unpinAlbum(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PATH + " = ?", new String[]{albumPath});
        LogUtils.d(TAG, "unpinAlbum: " + albumPath);
    }
    
    public boolean isPinned(String albumPath) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PATH + " = ?", 
            new String[]{albumPath}, null, null, null);
        boolean pinned = cursor.getCount() > 0;
        cursor.close();
        return pinned;
    }
    
    public String[] getPinnedPaths() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH}, null, null, null, null, null);
        String[] paths = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            paths[i++] = cursor.getString(0);
        }
        cursor.close();
        return paths;
    }
}