package cc.winboll.studio.mygallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.LinkedHashMap;

public class PinnedAlbumDbHelper extends SQLiteOpenHelper {
    public static final String TAG = "PinnedAlbumDbHelper";
    private static final String DB_NAME = "pinned_album.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "pinned_albums";
    private static final String COLUMN_PATH = "album_path";
    private static final String COLUMN_SORT_ORDER = "sort_order";
    
    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" 
        + COLUMN_PATH + " TEXT PRIMARY KEY, "
        + COLUMN_SORT_ORDER + " INTEGER DEFAULT 0)";
    
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
        if (oldVersion < 2) {
            // 创建新表
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + "_new");
            db.execSQL("CREATE TABLE " + TABLE_NAME + "_new ("
                + COLUMN_PATH + " TEXT PRIMARY KEY, "
                + COLUMN_SORT_ORDER + " INTEGER DEFAULT 0)");
            
            // 迁移旧数据，为现有记录分配排序号（按原始顺序）
            Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH}, null, null, null, null, null);
            int sortOrder = 1;
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                ContentValues values = new ContentValues();
                values.put(COLUMN_PATH, path);
                values.put(COLUMN_SORT_ORDER, sortOrder++);
                db.insert(TABLE_NAME + "_new", null, values);
            }
            cursor.close();
            
            // 删除旧表，重命名新表
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("ALTER TABLE " + TABLE_NAME + "_new RENAME TO " + TABLE_NAME);
        }
    }
    
    public void pinAlbum(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 将所有现有置顶数据的排序号加1
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_SORT_ORDER + " = " + COLUMN_SORT_ORDER + " + 1");
            
            // 插入新数据，排序号为1
            ContentValues insertValues = new ContentValues();
            insertValues.put(COLUMN_PATH, albumPath);
            insertValues.put(COLUMN_SORT_ORDER, 1);
            db.insertWithOnConflict(TABLE_NAME, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);
            
            db.setTransactionSuccessful();
            
            // 查询所有置顶相册的排序号并输出调试信息
            Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH, COLUMN_SORT_ORDER}, 
                null, null, null, null, COLUMN_SORT_ORDER + " ASC");
            StringBuilder sb = new StringBuilder("All pinned albums after pin:\n");
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                int sortOrder = cursor.getInt(1);
                sb.append("  [").append(sortOrder).append("] ").append(path).append("\n");
            }
            cursor.close();
            LogUtils.d(TAG, sb.toString());
        } finally {
            db.endTransaction();
        }
    }
    
    public void unpinAlbum(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 获取要取消置顶的相册的排序号
            Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_SORT_ORDER}, 
                COLUMN_PATH + " = ?", new String[]{albumPath}, null, null, null);
            if (cursor.moveToFirst()) {
                int sortNumber = cursor.getInt(0);
                cursor.close();
                
                // 将排序号大于该排序号的记录减1
                db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_SORT_ORDER + " = " + COLUMN_SORT_ORDER + " - 1 WHERE " + COLUMN_SORT_ORDER + " > ?", new Object[]{sortNumber});
                
                // 删除该相册的置顶记录
                db.delete(TABLE_NAME, COLUMN_PATH + " = ?", new String[]{albumPath});
                
                LogUtils.d(TAG, "unpinAlbum: " + albumPath + ", sort number was: " + sortNumber);
            } else {
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    public void repinAlbumToFront(String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 获取要再次置顶的相册的排序号
            Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_SORT_ORDER}, 
                COLUMN_PATH + " = ?", new String[]{albumPath}, null, null, null);
            if (cursor.moveToFirst()) {
                int sortNumber = cursor.getInt(0);
                cursor.close();
                
                if (sortNumber > 1) {
                    // 将排序号小于该记录的记录全部加1（整体后移）
                    db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_SORT_ORDER + " = " + COLUMN_SORT_ORDER + " + 1 WHERE " + COLUMN_SORT_ORDER + " < ?", new Object[]{sortNumber});
                    
                    // 该相册置顶到排序号1
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_SORT_ORDER, 1);
                    db.update(TABLE_NAME, values, COLUMN_PATH + " = ?", new String[]{albumPath});
                    
                    LogUtils.d(TAG, "repinAlbumToFront: " + albumPath + ", sort number was: " + sortNumber);
                }
                
                // 按当前顺序将排序号重排为连续的1..n
                normalizeSortOrders(db);
                
                // 查询所有置顶相册的排序号并输出调试信息
                Cursor debugCursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH, COLUMN_SORT_ORDER}, 
                    null, null, null, null, COLUMN_SORT_ORDER + " ASC");
                StringBuilder sb = new StringBuilder("All pinned albums after repin:\n");
                while (debugCursor.moveToNext()) {
                    sb.append("  [").append(debugCursor.getInt(1)).append("] ").append(debugCursor.getString(0)).append("\n");
                }
                debugCursor.close();
                LogUtils.d(TAG, sb.toString());
            } else {
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    // 按排序号升序将所有记录重排为连续的1..n，修正可能出现的序号空洞或重复
    private void normalizeSortOrders(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH}, null, null, null, null, 
            COLUMN_SORT_ORDER + " ASC");
        int sortOrder = 1;
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_SORT_ORDER + " = ? WHERE " + COLUMN_PATH + " = ?", 
                new Object[]{sortOrder++, path});
        }
        cursor.close();
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
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH}, null, null, null, null, 
            COLUMN_SORT_ORDER + " ASC");
        String[] paths = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            paths[i++] = cursor.getString(0);
        }
        cursor.close();
        return paths;
    }
    
    public LinkedHashMap<String, Integer> getPinnedPathsWithSortOrder() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PATH, COLUMN_SORT_ORDER}, 
            null, null, null, null, COLUMN_SORT_ORDER + " ASC");
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            int sortOrder = cursor.getInt(1);
            map.put(path, sortOrder);
        }
        cursor.close();
        return map;
    }
}
