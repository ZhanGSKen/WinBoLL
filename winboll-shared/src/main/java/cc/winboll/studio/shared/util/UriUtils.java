package cc.winboll.studio.shared.util;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Describe Uri 资源管理工具类
 */
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UriUtils {

    public static final String TAG = "UriUtil";

    //
    // 获取真实路径
    //
    // @param context
    //
    public static String getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        switch (uri.getScheme()) {
            case ContentResolver.SCHEME_CONTENT:
                //Android7.0之后的uri content:// URI
                return getFilePathFromContentUri(context, uri);
            case ContentResolver.SCHEME_FILE:
            default:
                //Android7.0之前的uri file://
                return new File(uri.getPath()).getAbsolutePath();
        }
    }

    //
    // 从uri获取path
    //
    // @param uri content://media/external/file/109009
    //            <p>
    //            FileProvider适配
    //            content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/
    //            content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/
    //
    private static String getFilePathFromContentUri(Context context, Uri uri) {
        if (null == uri) return null;
        String data = null;

        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (index > -1) {
                    data = cursor.getString(index);
                } else {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(nameIndex);
                    data = getPathFromInputStreamUri(context, uri, fileName);
                }
            }
            cursor.close();
        }
        return data;
    }

    //
    // 用流拷贝文件一份到自己APP私有目录下
    //
    // @param context
    // @param uri
    // @param fileName
    //
    private static String getPathFromInputStreamUri(Context context, Uri uri, String fileName) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File file = createTemporalFileFrom(context, inputStream, fileName);
                filePath = file.getPath();

            } catch (Exception e) {
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
        }

        return filePath;
    }

    private static File createTemporalFileFrom(Context context, InputStream inputStream, String fileName)
    throws IOException {
        File targetFile = null;
        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];
            //自己定义拷贝文件路径
            targetFile = new File(context.getExternalCacheDir(), fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return targetFile;
    }
}
