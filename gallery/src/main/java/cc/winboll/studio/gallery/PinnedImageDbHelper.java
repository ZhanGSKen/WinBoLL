package cc.winboll.studio.gallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cc.winboll.studio.libappbase.LogUtils;

public class PinnedImageDbHelper extends SQLiteOpenHelper {
    public static final String TAG = "PinnedImageDbHelper";
    private static final String DB_NAME = "pinned_image.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "pinned_images";
    private static final String COLUMN_PATH = "image_path";
    
    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" 
        + COLUMN_PATH + " TEXT PRIMARY KEY)";
    
    private static PinnedImageDbHelper dbHelper;
    
    public static PinnedImageDbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new PinnedImageDbHelper(context.getApplicationContext());
        }
        return dbHelper;
    }
    
    public PinnedImageDbHelper(Context context) {
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
    
    public void pinImage(String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATH, imagePath);
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        LogUtils.d(TAG, "pinImage: " + imagePath);
    }
    
    public void unpinImage(String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PATH + " = ?", new String[]{imagePath});
        LogUtils.d(TAG, "unpinImage: " + imagePath);
    }
    
    public boolean isPinned(String imagePath) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PATH + " = ?", 
            new String[]{imagePath}, null, null, null);
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