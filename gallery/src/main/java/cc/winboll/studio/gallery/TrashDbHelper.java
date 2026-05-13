package cc.winboll.studio.gallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import java.io.File;

import cc.winboll.studio.libappbase.LogUtils;

public class TrashDbHelper extends SQLiteOpenHelper {
    public static final String TAG = "TrashDbHelper";
    private static final String DB_NAME = "trash.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "trash_items";
    private static final String COL_ID = "_id";
    private static final String COL_FILE_NAME = "file_name";
    private static final String COL_ORIGINAL_PATH = "original_path";
    private static final String COL_ORIGINAL_FOLDER = "original_folder";
    private static final String COL_DELETE_TIME = "delete_time";
    
    public TrashDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_FILE_NAME + " TEXT, " +
            COL_ORIGINAL_PATH + " TEXT, " +
            COL_ORIGINAL_FOLDER + " TEXT, " +
            COL_DELETE_TIME + " INTEGER)");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.i(TAG, "onUpgrade: " + oldVersion + " -> " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    
    public long insert(String fileName, String originalPath, String originalFolder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FILE_NAME, fileName);
        values.put(COL_ORIGINAL_PATH, originalPath);
        values.put(COL_ORIGINAL_FOLDER, originalFolder);
        values.put(COL_DELETE_TIME, System.currentTimeMillis());
        return db.insert(TABLE_NAME, null, values);
    }
    
    public Cursor getAll() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COL_DELETE_TIME + " DESC");
    }
    
    public int delete(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public void clear() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
    
    public static String getTrashPath() {
        File trashDir = new File(Environment.getExternalStorageDirectory(), ".Trash");
        if (!trashDir.exists()) {
            trashDir.mkdirs();
        }
        return trashDir.getAbsolutePath();
    }
}