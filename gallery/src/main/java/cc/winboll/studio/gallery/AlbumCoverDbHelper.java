package cc.winboll.studio.gallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cc.winboll.studio.libappbase.LogUtils;

public class AlbumCoverDbHelper extends SQLiteOpenHelper {
    public static final String TAG = "AlbumCoverDbHelper";
    private static final String DB_NAME = "album_cover.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "album_covers";
    private static final String COLUMN_ALBUM_PATH = "album_path";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_CROP_PATH = "crop_path";
    
    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" 
        + COLUMN_ALBUM_PATH + " TEXT PRIMARY KEY, "
        + COLUMN_IMAGE_PATH + " TEXT, "
        + COLUMN_CROP_PATH + " TEXT)";
    
    private static AlbumCoverDbHelper dbHelper;
    
    public static AlbumCoverDbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new AlbumCoverDbHelper(context.getApplicationContext());
        }
        return dbHelper;
    }
    
    public AlbumCoverDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_CROP_PATH + " TEXT");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
            }
        }
    }
    
    public void setCoverWithCrop(String albumPath, String imagePath, String cropPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_PATH, albumPath);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_CROP_PATH, cropPath);
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        LogUtils.d(TAG, "setCoverWithCrop: album=" + albumPath + ", image=" + imagePath + ", crop=" + cropPath);
    }
    
    public void setCover(String albumPath, String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_PATH, albumPath);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        LogUtils.d(TAG, "setCover: album=" + albumPath + ", image=" + imagePath);
    }
    
    public String getCover(String albumPath) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_CROP_PATH, COLUMN_IMAGE_PATH}, 
            COLUMN_ALBUM_PATH + " = ?", new String[]{albumPath}, null, null, null);
        String coverPath = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                coverPath = cursor.getString(0);
                if (coverPath == null) {
                    coverPath = cursor.getString(1);
                }
            }
            cursor.close();
        }
        return coverPath;
    }
    
    public String getCropPath(String albumPath) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_CROP_PATH}, 
            COLUMN_ALBUM_PATH + " = ?", new String[]{albumPath}, null, null, null);
        String cropPath = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cropPath = cursor.getString(0);
            }
            cursor.close();
        }
        return cropPath;
    }
    
    public String getOriginalImagePath(String albumPath) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_IMAGE_PATH}, 
            COLUMN_ALBUM_PATH + " = ?", new String[]{albumPath}, null, null, null);
        String imagePath = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                imagePath = cursor.getString(0);
            }
            cursor.close();
        }
        return imagePath;
    }
    
    public void clearCover(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(COLUMN_CROP_PATH);
        db.update(TABLE_NAME, values, COLUMN_ALBUM_PATH + " = ?", new String[]{albumPath});
        LogUtils.d(TAG, "clearCover: " + albumPath);
    }
    
    public void deleteCover(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ALBUM_PATH + " = ?", new String[]{albumPath});
        LogUtils.d(TAG, "deleteCover: " + albumPath);
    }
}