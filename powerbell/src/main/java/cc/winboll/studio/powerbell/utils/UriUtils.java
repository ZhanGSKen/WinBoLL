package cc.winboll.studio.powerbell.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Uri 工具类（Java7兼容，适配API29-30+小米机型，FileProvider安全适配）
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/06/28
 */
public class UriUtils {
    // ====================== 常量定义（顶部统一管理）======================
    public static final String TAG = "UriUtils";
    // FileProvider 授权后缀（与Manifest配置保持一致）
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";
    // 应用公共图片目录（API29+ 适配，替代废弃API）
    private static final String APP_PUBLIC_PIC_DIR = "PowerBell/";
    // MIME类型与文件后缀映射表（覆盖常见格式，小米机型精准匹配）
    private static final Map<String, String> MIME_SUFFIX_MAP = new HashMap<String, String>() {{
			// 图片格式（重点，含透明格式）
			put("image/png", "png");
			put("image/jpeg", "jpg");
			put("image/jpg", "jpg");
			put("image/gif", "gif");
			put("image/bmp", "bmp");
			put("image/webp", "webp");
			// 音视频格式
			put("video/mp4", "mp4");
			put("video/avi", "avi");
			put("video/mkv", "mkv");
			put("audio/mp3", "mp3");
			put("audio/wav", "wav");
			// 文档格式
			put("application/pdf", "pdf");
			put("application/msword", "doc");
			put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
			put("application/vnd.ms-excel", "xls");
			put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
		}};

    // ====================== 新增核心方法：Uri 转文件后缀 ======================
    /**
     * 【静态公共方法】根据 Uri 获取文件真实后缀（优先MIME类型匹配，适配所有Uri场景+小米机型）
     * @param context 上下文（非空，用于获取ContentResolver）
     * @param uri 待解析 Uri（支持 content:// / file:// 双Scheme）
     * @return 小写文件后缀（如 png/jpg/mp4，无匹配返回空字符串）
     */
    public static String getSuffixFromUri(Context context, Uri uri) {
        LogUtils.d(TAG, "=== getSuffixFromUri 调用 start，Uri：" + (uri != null ? uri.toString() : "null") + " ===");
        // 1. 基础参数校验
        if (context == null) {
            LogUtils.e(TAG, "getSuffixFromUri：Context 为空，获取失败");
            return "";
        }
        if (uri == null) {
            LogUtils.e(TAG, "getSuffixFromUri：Uri 为空，获取失败");
            return "";
        }

        String suffix = "";
        String scheme = uri.getScheme();
        // 2. 按 Uri Scheme 分类处理（优先精准匹配，再降级截取）
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            // 场景1：content:// Uri（优先通过MIME类型获取，最精准）
            suffix = getSuffixFromContentUri(context, uri);
            LogUtils.d(TAG, "getSuffixFromUri：content:// Uri，MIME匹配后缀：" + suffix);
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            // 场景2：file:// Uri（直接解析文件名截取后缀）
            String filePath = new File(uri.getPath()).getAbsolutePath();
            suffix = getSuffixFromFilePath(filePath);
            LogUtils.d(TAG, "getSuffixFromUri：file:// Uri，路径截取后缀：" + suffix);
        } else {
            // 场景3：未知Scheme（尝试解析Uri路径截取，兜底）
            String uriPath = uri.getPath();
            suffix = uriPath != null ? getSuffixFromFilePath(uriPath) : "";
            LogUtils.w(TAG, "getSuffixFromUri：未知Scheme=" + scheme + "，兜底截取后缀：" + suffix);
        }

        // 3. 最终结果处理（统一小写，去空）
        suffix = suffix != null ? suffix.trim().toLowerCase() : "";
        LogUtils.d(TAG, "=== getSuffixFromUri 调用 end，最终后缀：" + suffix + " ===");
        return suffix;
    }

    // ====================== 公有核心方法（对外提供能力，按功能排序）======================
    /**
     * Uri 转真实文件路径（核心方法，适配 content:// / file:// 双 Scheme）
     * @param context 上下文（非空）
     * @param uri 待转换 Uri（非空）
     * @return 真实文件绝对路径（转换失败返回 null）
     */
    public static String getFilePathFromUri(Context context, Uri uri) {
        LogUtils.d(TAG, "=== getFilePathFromUri 调用 start ===");
        if (context == null) {
            LogUtils.e(TAG, "getFilePathFromUri：Context 为空，转换失败");
            return null;
        }
        if (uri == null) {
            LogUtils.e(TAG, "getFilePathFromUri：Uri 为空，转换失败");
            return null;
        }

        String scheme = uri.getScheme();
        String filePath = null;
        // 按 Uri Scheme 分类处理
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            LogUtils.d(TAG, "getFilePathFromUri：Scheme=content，执行ContentUri转换");
            filePath = getFilePathFromContentUri(context, uri);
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            LogUtils.d(TAG, "getFilePathFromUri：Scheme=file，直接转换路径");
            filePath = new File(uri.getPath()).getAbsolutePath();
        } else {
            LogUtils.w(TAG, "getFilePathFromUri：未知Scheme=" + scheme + "，转换失败");
        }

        LogUtils.d(TAG, "=== getFilePathFromUri 调用 end，结果：" + filePath + " ===");
        return filePath;
    }

    /**
     * 文件路径转 Uri（核心方法，适配 Android7.0+ FileProvider，API29-30兼容）
     * @param context 上下文（非空）
     * @param filePath 真实文件路径（非空）
     * @return 安全 Uri（转换失败返回 null）
     */
    public static Uri getUriForFile(Context context, String filePath) {
        LogUtils.d(TAG, "=== getUriForFile（路径版）调用 start ===");
        // 1. 基础参数校验
        if (context == null) {
            LogUtils.e(TAG, "getUriForFile：Context 为空，转换失败");
            return null;
        }
        if (filePath == null || filePath.isEmpty()) {
            LogUtils.e(TAG, "getUriForFile：文件路径为空，转换失败");
            return null;
        }

        // 2. File 对象初始化与校验
        File file = new File(filePath);
        LogUtils.d(TAG, "getUriForFile：文件路径=" + file.getAbsolutePath() + "，是否存在=" + file.exists());
        if (!file.exists() || file.isDirectory()) {
            LogUtils.e(TAG, "getUriForFile：文件不存在或为目录，转换失败");
            return null;
        }

        // 3. 合法路径校验（适配小米机型，避免FileProvider配置外路径）
        if (!isPathInValidDir(context, file)) {
            LogUtils.w(TAG, "getUriForFile：路径不在安全配置目录内，小米机型可能出现权限异常");
        }

        // 4. 调用重载方法生成 Uri
        Uri uri = getUriForFile(context, file);
        LogUtils.d(TAG, "=== getUriForFile（路径版）调用 end，结果：" + (uri != null ? uri.toString() : "null") + " ===");
        return uri;
    }

    /**
     * File 对象转 Uri（重载方法，直接接收File，内部安全适配）
     * @param context 上下文（非空）
     * @param file 待转换 File 对象（非空）
     * @return 安全 Uri（转换失败返回 null）
     */
    public static Uri getUriForFile(Context context, File file) {
        LogUtils.d(TAG, "=== getUriForFile（File版）调用 start ===");
        // 1. 基础参数校验
        if (context == null) {
            LogUtils.e(TAG, "getUriForFile：Context 为空，转换失败");
            return null;
        }
        if (file == null) {
            LogUtils.e(TAG, "getUriForFile：File 对象为空，转换失败");
            return null;
        }
        LogUtils.d(TAG, "getUriForFile：文件路径=" + file.getAbsolutePath() + "，是否存在=" + file.exists());
        if (!file.exists() || file.isDirectory()) {
            LogUtils.e(TAG, "getUriForFile：文件不存在或为目录，转换失败");
            return null;
        }

        // 2. 按系统版本生成 Uri（API24+ 强制 FileProvider，适配小米机型）
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LogUtils.d(TAG, "getUriForFile：Android7.0+，使用FileProvider生成Uri");
            String authority = context.getPackageName() + FILE_PROVIDER_SUFFIX;
            LogUtils.d(TAG, "getUriForFile：FileProvider Authority=" + authority);
            try {
                uri = FileProvider.getUriForFile(context, authority, file);
                LogUtils.d(TAG, "getUriForFile：Content Uri生成成功=" + uri.toString());
            } catch (IllegalArgumentException e) {
                LogUtils.e(TAG, "getUriForFile：FileProvider生成失败（小米机型常见原因：路径未配置/Authority不匹配）", e);
            }
        } else {
            LogUtils.d(TAG, "getUriForFile：Android7.0以下，使用Uri.fromFile生成");
            uri = Uri.fromFile(file);
            LogUtils.d(TAG, "getUriForFile：File Uri生成成功=" + uri.toString());
        }

        LogUtils.d(TAG, "=== getUriForFile（File版）调用 end ===");
        return uri;
    }

    // ====================== 私有辅助方法（内部逻辑封装，不对外暴露）======================
    /**
     * ContentUri 转真实路径（适配 content:// 格式，处理小米机型特殊Uri）
     * @param context 上下文
     * @param uri ContentUri（如：content://media/external/file/xxx）
     * @return 真实文件路径（失败返回 null）
     */
    private static String getFilePathFromContentUri(Context context, Uri uri) {
        LogUtils.d(TAG, "getFilePathFromContentUri：Uri=" + uri.toString());
        String filePath = null;
        Cursor cursor = null;
        // Java7 语法：try-catch-finally 手动关闭Cursor，避免内存泄漏
        try {
            // 查询字段：优先 DATA 字段，失败则通过文件名+流拷贝获取
            String[] queryColumns = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = context.getContentResolver().query(uri, queryColumns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // 优先读取 DATA 字段（直接获取路径）
                int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (dataIndex != -1) {
                    filePath = cursor.getString(dataIndex);
                    LogUtils.d(TAG, "getFilePathFromContentUri：从DATA字段获取路径=" + filePath);
                } else {
                    // DATA 字段为空，通过流拷贝到私有目录获取路径（小米机型特殊场景适配）
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(nameIndex);
                    LogUtils.d(TAG, "getFilePathFromContentUri：DATA字段为空，通过流拷贝获取，文件名=" + fileName);
                    filePath = getPathFromInputStreamUri(context, uri, fileName);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "getFilePathFromContentUri：查询失败", e);
        } finally {
            // 强制关闭Cursor，避免资源泄漏（Java7 必须手动处理）
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, "getFilePathFromContentUri：关闭Cursor失败", e);
                }
            }
        }
        return filePath;
    }

    /**
     * 流拷贝获取路径（适配无 DATA 字段的 ContentUri，小米机型特殊Uri兼容）
     * 将目标文件拷贝到应用私有缓存目录，返回拷贝后的路径
     * @param context 上下文
     * @param uri ContentUri
     * @param fileName 文件名
     * @return 拷贝后的文件路径（失败返回 null）
     */
    private static String getPathFromInputStreamUri(Context context, Uri uri, String fileName) {
        LogUtils.d(TAG, "getPathFromInputStreamUri：开始流拷贝，文件名=" + fileName);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File targetFile = null;
        try {
            // 1. 打开输入流（读取Uri对应文件）
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                LogUtils.e(TAG, "getPathFromInputStreamUri：输入流打开失败");
                return null;
            }

            // 2. 创建目标文件（应用私有缓存目录，无权限限制）
            targetFile = new File(context.getExternalCacheDir(), fileName);
            // 若文件已存在，先删除（避免覆盖导致格式异常）
            if (targetFile.exists()) {
                boolean deleteSuccess = targetFile.delete();
                LogUtils.d(TAG, "getPathFromInputStreamUri：删除已存在文件，结果=" + deleteSuccess);
            }

            // 3. 流拷贝（Java7 手动处理流，避免 try-with-resources）
            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8 * 1024]; // 8KB 缓冲区，平衡效率与内存
            int readLength;
            while ((readLength = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readLength);
            }
            outputStream.flush();
            LogUtils.d(TAG, "getPathFromInputStreamUri：流拷贝成功，路径=" + targetFile.getAbsolutePath());
        } catch (Exception e) {
            LogUtils.e(TAG, "getPathFromInputStreamUri：流拷贝失败", e);
            // 拷贝失败，删除临时文件
            if (targetFile != null && targetFile.exists()) {
                targetFile.delete();
            }
            targetFile = null;
        } finally {
            // 强制关闭流，避免资源泄漏（Java7 必须手动关闭）
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                LogUtils.e(TAG, "getPathFromInputStreamUri：关闭输出流失败", e);
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LogUtils.e(TAG, "getPathFromInputStreamUri：关闭输入流失败", e);
            }
        }
        return targetFile != null ? targetFile.getAbsolutePath() : null;
    }

    /**
     * 校验路径是否在安全目录内（适配API29-30+小米机型，避免FileProvider权限异常）
     * 仅允许：应用私有目录、缓存目录、应用专属公共目录
     * @param context 上下文
     * @param file 待校验文件
     * @return true=安全路径，false=非安全路径
     */
    private static boolean isPathInValidDir(Context context, File file) {
        String absolutePath = file.getAbsolutePath();
        // 1. 应用外部私有目录（API29+ 推荐，无权限限制）
        String externalPrivateDir = context.getExternalFilesDir(null) != null 
			? context.getExternalFilesDir(null).getAbsolutePath() 
			: "";
        // 2. 应用内部私有目录（无权限限制）
        String internalPrivateDir = context.getFilesDir().getAbsolutePath();
        // 3. 应用缓存目录（无权限限制）
        String cacheDir = context.getCacheDir().getAbsolutePath();
        // 4. 应用专属公共目录（API29+ 适配，替代废弃的 getExternalStoragePublicDirectory）
        String appPublicDir = Environment.getExternalStorageDirectory().getAbsolutePath() 
			+ File.separator + Environment.DIRECTORY_PICTURES 
			+ File.separator + APP_PUBLIC_PIC_DIR;

        // 校验路径是否在安全目录内（小米机型必须严格校验，否则FileProvider会抛异常）
        boolean isInValidDir = absolutePath.startsWith(externalPrivateDir)
			|| absolutePath.startsWith(internalPrivateDir)
			|| absolutePath.startsWith(cacheDir)
			|| absolutePath.startsWith(appPublicDir);

        LogUtils.d(TAG, "isPathInValidDir：外部私有目录=" + externalPrivateDir 
				   + "，公共目录=" + appPublicDir 
				   + "，校验结果=" + isInValidDir);
        return isInValidDir;
    }

    /**
     * 流拷贝创建临时文件（内部辅助，封装拷贝逻辑）
     * @param context 上下文
     * @param inputStream 输入流
     * @param fileName 文件名
     * @return 临时文件（失败返回 null）
     * @throws IOException 流操作异常
     */
    private static File createTemporalFileFrom(Context context, InputStream inputStream, String fileName) throws IOException {
        File targetFile = null;
        if (inputStream != null) {
            byte[] buffer = new byte[8 * 1024];
            int readLength;
            targetFile = new File(context.getExternalCacheDir(), fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            OutputStream outputStream = new FileOutputStream(targetFile);
            while ((readLength = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readLength);
            }
            outputStream.flush();
            outputStream.close();
        }
        return targetFile;
    }

    /**
     * 辅助：ContentUri 通过 MIME 类型获取后缀（精准匹配，不受文件名伪造影响）
     * @param context 上下文
     * @param uri ContentUri
     * @return 匹配的后缀（无匹配返回空字符串）
     */
    private static String getSuffixFromContentUri(Context context, Uri uri) {
        String mime = null;
        try {
            // 通过 ContentResolver 获取 Uri 对应的 MIME 类型（系统级匹配，最精准）
            mime = context.getContentResolver().getType(uri);
            LogUtils.d(TAG, "getSuffixFromContentUri：获取MIME类型=" + mime);
            if (mime == null || mime.isEmpty()) {
                // MIME 为空，尝试解析文件名兜底
                String fileName = getFileNameFromContentUri(context, uri);
                return getSuffixFromFilePath(fileName);
            }
            // MIME 类型匹配后缀（优先完全匹配，再模糊匹配）
            if (MIME_SUFFIX_MAP.containsKey(mime)) {
                return MIME_SUFFIX_MAP.get(mime);
            }
            // 模糊匹配（如 image/* 匹配通用图片后缀，默认png）
            if (mime.startsWith("image/")) {
                return "png";
            } else if (mime.startsWith("video/")) {
                return "mp4";
            } else if (mime.startsWith("audio/")) {
                return "mp3";
            } else if (mime.startsWith("application/")) {
                return "pdf";
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "getSuffixFromContentUri：MIME解析失败，mime=" + mime, e);
        }
        // 所有方式失败，解析Uri路径兜底
        return getSuffixFromFilePath(uri.getPath());
    }

    /**
     * 辅助：从 ContentUri 获取文件名（MIME 解析失败时兜底）
     * @param context 上下文
     * @param uri ContentUri
     * @return 文件名（失败返回空字符串）
     */
    private static String getFileNameFromContentUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] queryColumns = {MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = context.getContentResolver().query(uri, queryColumns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "getFileNameFromContentUri：查询失败", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, "getFileNameFromContentUri：关闭Cursor失败", e);
                }
            }
        }
        return "";
    }

    /**
     * 辅助：从文件路径/文件名截取后缀（兜底方案，处理各种路径格式）
     * @param path 文件路径/文件名
     * @return 截取的后缀（无后缀返回空字符串）
     */
    private static String getSuffixFromFilePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        // 处理路径中的分隔符（兼容 Windows/Android 路径格式）
        path = path.replace("\\", "/");
        // 取最后一个 "/" 后的文件名（避免路径包含 "." 导致误判）
        int lastSepIndex = path.lastIndexOf("/");
        if (lastSepIndex != -1 && lastSepIndex < path.length() - 1) {
            path = path.substring(lastSepIndex + 1);
        }
        // 截取最后一个 "." 后的后缀（过滤无后缀/点开头/点结尾场景）
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == 0 || lastDotIndex == path.length() - 1) {
            return "";
        }
        // 过滤后缀中的非法字符（仅保留字母/数字，避免特殊字符干扰）
        String suffix = path.substring(lastDotIndex + 1).replaceAll("[^a-zA-Z0-9]", "");
        // 限制后缀长度（1-5位，避免超长伪造后缀）
        return suffix.length() >= 1 && suffix.length() <= 5 ? suffix : "";
    }
}

