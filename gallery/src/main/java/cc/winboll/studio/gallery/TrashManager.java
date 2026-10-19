package cc.winboll.studio.gallery;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.File;
import java.util.UUID;

import cc.winboll.studio.libappbase.LogUtils;

public class TrashManager {
    public static final String TAG = "TrashManager";
    private final Context context;
    private final TrashDbHelper dbHelper;
    
    public TrashManager(Context context) {
        LogUtils.d(TAG, "TrashManager created");
        this.context = context;
        this.dbHelper = new TrashDbHelper(context);
    }
    
    public long addToTrash(String imagePath) {
        LogUtils.d(TAG, "addToTrash: " + imagePath);
        File sourceFile = new File(imagePath);
        if (!sourceFile.exists()) {
            return -1;
        }
        
        String uniqueId = UUID.randomUUID().toString();
        String extension = getExtension(imagePath);
        String newFileName = uniqueId + extension;
        String trashPath = TrashDbHelper.getTrashPath();
        File destFile = new File(trashPath, newFileName);
        
        if (sourceFile.renameTo(destFile)) {
            String originalFolder = sourceFile.getParent();
            long result = dbHelper.insert(newFileName, imagePath, originalFolder);
            LogUtils.i(TAG, "Added to trash: " + newFileName);
            return result;
        }
        LogUtils.e(TAG, "Failed to move to trash");
        return -1;
    }
    
    public Cursor getTrashList() {
        return dbHelper.getAll();
    }
    
    public boolean restore(long id, String fileName, String originalPath) {
        LogUtils.i(TAG, "restore: " + fileName + " -> " + originalPath);
        File trashFile = new File(TrashDbHelper.getTrashPath(), fileName);
        LogUtils.d(TAG, "trashFile exists: " + trashFile.exists() + ", path: " + trashFile.getAbsolutePath());
        if (!trashFile.exists()) {
            LogUtils.e(TAG, "trashFile not exists: " + trashFile.getAbsolutePath());
            return false;
        }
        
        File originalFolder = new File(originalPath).getParentFile();
        LogUtils.d(TAG, "originalFolder: " + originalFolder + ", exists: " + (originalFolder != null && originalFolder.exists()));
        if (originalFolder != null && !originalFolder.exists()) {
            boolean created = originalFolder.mkdirs();
            LogUtils.d(TAG, "mkdirs result: " + created + ", path: " + originalFolder.getAbsolutePath());
        }
        
        File originalFile = new File(originalPath);
        String restoreName = originalFile.getName();
        File restoreFile = new File(originalFolder, restoreName);
        LogUtils.d(TAG, "restoreFile: " + restoreFile.getAbsolutePath() + ", exists: " + restoreFile.exists());
        
        boolean renameResult = trashFile.renameTo(restoreFile);
        LogUtils.d(TAG, "renameTo result: " + renameResult);
        
        if (renameResult) {
            dbHelper.delete(id);
            LogUtils.i(TAG, "Restored: " + fileName);
            scanMedia(restoreFile.getAbsolutePath());
            return true;
        }
        
        // Try copy + delete if rename failed
        LogUtils.i(TAG, "renameTo failed, trying copy + delete");
        try {
            java.io.InputStream in = new java.io.FileInputStream(trashFile);
            java.io.OutputStream out = new java.io.FileOutputStream(restoreFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
            boolean deleted = trashFile.delete();
            LogUtils.d(TAG, "copy+delete result: " + deleted);
            if (deleted) {
                dbHelper.delete(id);
                LogUtils.i(TAG, "Restored (copy): " + fileName);
                scanMedia(restoreFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "copy failed: " + e.getMessage());
        }
        
        LogUtils.e(TAG, "Failed to restore: " + fileName);
        return false;
    }
    
    public boolean deletePermanently(long id, String fileName) {
        File trashFile = new File(TrashDbHelper.getTrashPath(), fileName);
        boolean deleted = trashFile.delete();
        if (deleted) {
            dbHelper.delete(id);
        }
        return deleted;
    }
    
    public void clearTrash() {
        Cursor cursor = getTrashList();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    int colIndex = cursor.getColumnIndexOrThrow("_id");
                    if (!cursor.isNull(colIndex)) {
                        String fileName = cursor.getString(cursor.getColumnIndexOrThrow("file_name"));
                        File trashFile = new File(TrashDbHelper.getTrashPath(), fileName);
                        trashFile.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        dbHelper.clear();
    }
    
    private String getExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(lastDot);
        }
        return ".jpg";
    }
    
    private void scanMedia(String filePath) {
        LogUtils.d(TAG, "scanMedia: " + filePath);
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, new android.media.MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                LogUtils.d(TAG, "scanCompleted: " + path + " -> " + uri);
            }
        });
    }
}