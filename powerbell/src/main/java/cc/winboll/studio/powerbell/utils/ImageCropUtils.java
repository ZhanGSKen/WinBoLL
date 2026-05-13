package cc.winboll.studio.powerbell.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.util.regex.Pattern;

/**
 * 图片裁剪工具类（集成 uCrop 2.2.8 终极兼容版，强制输出 PNG 格式，全程保留透明通道，支持 Uri/File/BackgroundBean 多传参）
 * 适配：Java 7 + Android API 30
 * 核心策略：强制 PNG 输出，保留透明通道，统一裁剪配置
 */
public class ImageCropUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "ImageCropUtils";
    // FileProvider 授权（与 AndroidManifest 配置一致）
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";
    // 强制输出格式：固定为 PNG（保留透明通道）
    private static final String FORCE_OUTPUT_SUFFIX = "png";
    private static final Bitmap.CompressFormat FORCE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    // 图片后缀正则（用于强制替换）
    private static final Pattern IMAGE_SUFFIX_PATTERN = Pattern.compile("\\.(jpg|jpeg|png|bmp|gif)$", Pattern.CASE_INSENSITIVE);

    // ================================== 核心裁剪方法（重载：Uri/File/BackgroundBean）=================================
    /**
     * 【Uri 传参版】启动 uCrop 裁剪 - 强制输出 PNG，保留透明通道
     * @param activity 上下文
     * @param inputUri 输入图片 Uri（本应用 FileProvider Uri，非空）
     * @param outputUri 输出图片 Uri（本应用 FileProvider Uri，非空）
     * @param aspectX 固定比例 X（自由裁剪传 0）
     * @param aspectY 固定比例 Y（自由裁剪传 0）
     * @param isFreeCrop 是否自由裁剪
     * @param requestCode 裁剪请求码
     */
    public static void startImageCrop(Activity activity,
                                      Uri inputUri,
                                      Uri outputUri,
                                      int aspectX,
                                      int aspectY,
                                      boolean isFreeCrop,
                                      int requestCode) {
        LogUtils.d(TAG, "【startImageCrop】调用开始（Uri 版）| 请求码=" + requestCode);
        // 1. 输入参数校验
        if (activity == null || activity.isFinishing()) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：Activity 无效或已销毁");
            return;
        }
        if (inputUri == null || outputUri == null) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：输入/输出 Uri 为空");
            showToast(activity, "图片 Uri 无效，无法裁剪");
            return;
        }
        if (!isValidUri(activity, inputUri)) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：输入 Uri 无效 " + inputUri);
            showToast(activity, "原图 Uri 无效，无法裁剪");
            return;
        }

        // 2. 核心：强制修正输出为 PNG（忽略原图格式，统一转 PNG）
        File outputFile = uriToFile(activity, outputUri);
        if (outputFile == null) {
            LogUtils.e(TAG, "【startImageCrop】转换异常：输出 Uri 转 File 失败 " + outputUri);
            showToast(activity, "裁剪输出路径无效");
            return;
        }
        outputFile = correctFileSuffix(outputFile, FORCE_OUTPUT_SUFFIX); // 强制 .png 后缀
        outputUri = getFileProviderUri(activity, outputFile); // 重新生成 PNG 对应的 Uri
        LogUtils.d(TAG, "【startImageCrop】格式修正：强制输出 PNG " + outputFile.getAbsolutePath());

        // 3. 初始化 uCrop + 强制 PNG 配置（保留透明核心）
        UCrop uCrop = UCrop.of(inputUri, outputUri);
        //uCrop.withAspectRatio(aspectX, aspectY);
        UCrop.Options options = initCropOptions(activity, isFreeCrop, aspectX, aspectY);

        // 4. 启动裁剪
        uCrop.withOptions(options);
        uCrop.start(activity, requestCode);
        LogUtils.d(TAG, "【startImageCrop】启动成功（Uri 版）| 输出路径=" + outputFile.getAbsolutePath());
    }

    /**
     * 【File 传参版】启动 uCrop 裁剪 - 强制输出 PNG，保留透明通道
     * @param activity 上下文
     * @param inputFile 输入图片文件（任意格式）
     * @param outputFile 输出图片文件（最终强制转为 PNG）
     * @param aspectX 固定比例 X（自由裁剪传 0）
     * @param aspectY 固定比例 Y（自由裁剪传 0）
     * @param isFreeCrop 是否自由裁剪
     * @param requestCode 裁剪请求码
     */
    public static void startImageCrop(Activity activity,
                                      File inputFile,
                                      File outputFile,
                                      int aspectX,
                                      int aspectY,
                                      boolean isFreeCrop,
                                      int requestCode) {
        LogUtils.d(TAG, "【startImageCrop】调用开始（File 版）| 请求码=" + requestCode);
        // 1. 输入参数校验
        if (activity == null || activity.isFinishing()) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：Activity 无效或已销毁");
            return;
        }
        if (inputFile == null || !inputFile.exists() || inputFile.length() <= 100) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：输入图片文件无效 " + (inputFile != null ? inputFile.getAbsolutePath() : "null"));
            showToast(activity, "无有效图片可裁剪");
            return;
        }
        if (outputFile == null) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：输出文件路径为空");
            showToast(activity, "裁剪输出路径无效");
            return;
        }

        // 2. 核心：强制修正输出为 PNG（忽略原图格式）
        Uri inputUri = getFileProviderUri(activity, inputFile);
        outputFile = correctFileSuffix(outputFile, FORCE_OUTPUT_SUFFIX); // 强制 .png 后缀
        Uri outputUri = getFileProviderUri(activity, outputFile);
        LogUtils.d(TAG, "【startImageCrop】格式修正：强制输出 PNG " + outputFile.getAbsolutePath());

        // 3. 初始化 uCrop + 强制 PNG 配置
        UCrop uCrop = UCrop.of(inputUri, outputUri);
        //uCrop.withAspectRatio(aspectX, aspectY);
        UCrop.Options options = initCropOptions(activity, isFreeCrop, aspectX, aspectY);

        // 4. 启动裁剪
        uCrop.withOptions(options);
        uCrop.start(activity, requestCode);
        LogUtils.d(TAG, "【startImageCrop】启动成功（File 版）| 输出路径=" + outputFile.getAbsolutePath());
    }

    /**
     * 【BackgroundBean 传参版】启动 uCrop 裁剪 - 强制输出 PNG，保留透明通道
     * @param activity 上下文
     * @param cropBean 背景图片 Bean
     * @param aspectX 固定比例 X
     * @param aspectY 固定比例 Y
     * @param isFreeCrop 是否自由裁剪
     * @param requestCode 裁剪请求码
     */
    public static void startImageCrop(Activity activity,
                                      BackgroundBean cropBean,
                                      int aspectX,
                                      int aspectY,
                                      boolean isFreeCrop,
                                      int requestCode) {
        LogUtils.d(TAG, "【startImageCrop】调用开始（BackgroundBean 版）| 请求码=" + requestCode);
        if (cropBean == null) {
            LogUtils.e(TAG, "【startImageCrop】参数异常：BackgroundBean 为空");
            showToast(activity, "裁剪参数无效");
            return;
        }
        File inputFile = new File(cropBean.getBackgroundFilePath());
        File outputFile = new File(cropBean.getBackgroundScaledCompressFilePath());
        startImageCrop(activity, inputFile, outputFile, aspectX, aspectY, isFreeCrop, requestCode);
        LogUtils.d(TAG, "【startImageCrop】启动成功（BackgroundBean 版）| 输入路径=" + inputFile.getAbsolutePath());
    }

    // ================================== 裁剪结果处理（优化日志，增强容错）=================================
    /**
     * 处理裁剪结果
     * @param requestCode 当前请求码
     * @param resultCode 结果码
     * @param data 结果数据
     * @param cropRequestCode 裁剪请求码
     * @return 裁剪成功返回输出路径，失败返回 null
     */
    public static String handleCropResult(int requestCode, int resultCode, Intent data, int cropRequestCode) {
        LogUtils.d(TAG, "【handleCropResult】调用开始 | 请求码=" + requestCode + " | 裁剪请求码=" + cropRequestCode);
        if (requestCode != cropRequestCode) {
            LogUtils.d(TAG, "【handleCropResult】请求码不匹配，忽略结果");
            return null;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri outputUri = UCrop.getOutput(data);
            if (outputUri != null) {
                String outputPath = uriToPath(outputUri);
                LogUtils.d(TAG, "【handleCropResult】裁剪成功 | 输出路径=" + outputPath);
                return outputPath;
            } else {
                LogUtils.e(TAG, "【handleCropResult】裁剪失败：输出 Uri 为空");
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable error = UCrop.getError(data);
            LogUtils.e(TAG, "【handleCropResult】裁剪异常：" + (error != null ? error.getMessage() : "未知错误。"));
        } else {
            LogUtils.d(TAG, "【handleCropResult】裁剪取消：用户手动取消");
        }
        return null;
    }

    // ================================== 私有辅助方法（参数校验 + 格式转换 + 配置初始化）=================================
    /**
     * 校验 Uri 有效性（确保是图片类型）
     */
    private static boolean isValidUri(Activity activity, Uri uri) {
        try {
            String type = activity.getContentResolver().getType(uri);
            boolean isValid = type != null && type.startsWith("image/");
            LogUtils.d(TAG, "【isValidUri】Uri 校验结果 | " + uri + " | 有效=" + isValid);
            return isValid;
        } catch (Exception e) {
            LogUtils.e(TAG, "【isValidUri】Uri 校验失败 " + uri, e);
            return false;
        }
    }

    /**
     * Uri 转 File（适配 FileProvider Uri 和普通 Uri）
     */
    private static File uriToFile(Activity activity, Uri uri) {
        if (uri == null) {
            LogUtils.e(TAG, "【uriToFile】参数异常：Uri 为空");
            return null;
        }
        try {
            if (uri.getScheme().equals("file")) {
                File file = new File(uri.getPath());
                LogUtils.d(TAG, "【uriToFile】转换成功（普通 Uri）| " + uri + " → " + file.getAbsolutePath());
                return file;
            }
            String filePath = uri.getPath();
            if (filePath == null) {
                LogUtils.e(TAG, "【uriToFile】转换失败：Uri 路径为空 " + uri);
                return null;
            }
            // 适配 FileProvider 路径
            if (filePath.contains("/external_files/")) {
                filePath = filePath.replace("/external_files/", activity.getExternalFilesDir("").getAbsolutePath() + "/");
            } else if (filePath.contains("/cache/")) {
                filePath = filePath.replace("/cache/", activity.getCacheDir().getAbsolutePath() + "/");
            }
            File file = new File(filePath);
            LogUtils.d(TAG, "【uriToFile】转换成功（FileProvider Uri）| " + uri + " → " + file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            LogUtils.e(TAG, "【uriToFile】转换失败 " + uri, e);
            return null;
        }
    }

    /**
     * Uri 提取文件路径
     */
    private static String uriToPath(Uri uri) {
        if (uri == null) {
            LogUtils.e(TAG, "【uriToPath】参数异常：Uri 为空");
            return null;
        }
        try {
            if (uri.getScheme().equals("file")) {
                String path = uri.getPath();
                LogUtils.d(TAG, "【uriToPath】提取成功（普通 Uri）| " + uri + " → " + path);
                return path;
            }
            String path = uri.getPath();
            if (path == null) {
                LogUtils.e(TAG, "【uriToPath】提取失败：Uri 路径为空 " + uri);
                return null;
            }
            // 适配多种 FileProvider 前缀
            String[] prefixes = {"/external/", "/external_files/", "/cache/", "/files/"};
            for (String prefix : prefixes) {
                if (path.contains(prefix)) {
                    path = path.substring(path.indexOf(prefix) + prefix.length());
                    String externalRoot = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                    path = externalRoot + "/" + path;
                    LogUtils.d(TAG, "【uriToPath】提取成功（FileProvider Uri）| " + uri + " → " + path);
                    return path;
                }
            }
            LogUtils.d(TAG, "【uriToPath】提取成功（默认路径）| " + uri + " → " + path);
            return path;
        } catch (Exception e) {
            LogUtils.e(TAG, "【uriToPath】提取失败 " + uri, e);
            return null;
        }
    }

    /**
     * 统一初始化裁剪配置（强制 PNG 专属配置，保留透明核心）
     */
    private static UCrop.Options initCropOptions(Activity activity, boolean isFreeCrop, int aspectX, int aspectY) {
        LogUtils.d(TAG, "【initCropOptions】初始化裁剪配置 | 自由裁剪=" + isFreeCrop);
        UCrop.Options options = new UCrop.Options();

        // 裁剪模式配置（自由裁剪/固定比例）
        options.setFreeStyleCropEnabled(isFreeCrop);
		options.withAspectRatio(aspectX, aspectY);
		
        // 核心：强制 PNG 保留透明（固定配置，无需判断原图格式）
        options.setCompressionFormat(FORCE_COMPRESS_FORMAT); // 强制 PNG 压缩
        options.setCompressionQuality(100); // PNG 100% 质量，不损失透明
        options.setDimmedLayerColor(activity.getResources().getColor(android.R.color.transparent)); // 遮罩透明（关键）
        options.setCropFrameColor(activity.getResources().getColor(R.color.colorPrimary)); // 裁剪框主题色
        options.setCropGridColor(activity.getResources().getColor(R.color.colorAccent)); // 网格线主题色

        // 通用 UI 配置（保持原有风格）
		
        //options.setHideBottomControls(true); // 隐藏底部控制栏
        options.setToolbarTitle("图片裁剪");
        options.setToolbarColor(activity.getResources().getColor(R.color.colorPrimary));
        options.setToolbarWidgetColor(activity.getResources().getColor(android.R.color.white));
        options.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));

        LogUtils.d(TAG, "【initCropOptions】配置完成：强制 PNG 输出，保留透明通道");
        return options;
    }

    /**
     * 修正文件后缀（强制转为指定后缀，覆盖原有任何图片后缀）
     */
    private static File correctFileSuffix(File originFile, String targetSuffix) {
        String originName = originFile.getName();
        // 强制替换所有图片后缀为 targetSuffix
        String newName = IMAGE_SUFFIX_PATTERN.matcher(originName).replaceAll("") + "." + targetSuffix;
        File newFile = new File(originFile.getParent(), newName);
        LogUtils.d(TAG, "【correctFileSuffix】后缀修正 | " + originFile.getName() + " → " + newFile.getName());
        return newFile;
    }

    /**
     * 生成 FileProvider Uri（适配 Android 7.0+）
     */
    private static Uri getFileProviderUri(Activity activity, File file) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authority = activity.getPackageName() + FILE_PROVIDER_SUFFIX;
                Uri uri = FileProvider.getUriForFile(activity, authority, file);
                LogUtils.d(TAG, "【getFileProviderUri】生成成功（Android 7.0+）| " + file.getAbsolutePath() + " → " + uri);
                return uri;
            } else {
                Uri uri = Uri.fromFile(file);
                LogUtils.d(TAG, "【getFileProviderUri】生成成功（Android 7.0-）| " + file.getAbsolutePath() + " → " + uri);
                return uri;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "【getFileProviderUri】生成失败 " + file.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * 显示 Toast（避免崩溃）
     */
    private static void showToast(Activity activity, String msg) {
        if (activity != null && !activity.isFinishing()) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, "【showToast】显示提示：" + msg);
        } else {
            LogUtils.e(TAG, "【showToast】无法显示提示：Activity 无效");
        }
    }

    // ================================== 公有辅助方法（供外部调用）=================================
    /**
     * 公有方法：生成 FileProvider Uri
     */
    public static Uri getFileProviderUriPublic(Activity activity, File file) {
        return getFileProviderUri(activity, file);
    }

    /**
     * 公有方法：Uri 转 File
     */
    public static File getFileFromUriPublic(Activity activity, Uri uri) {
        return uriToFile(activity, uri);
    }

    /**
     * 公有方法：Uri 提取路径
     */
    public static String getPathFromUriPublic(Uri uri) {
        return uriToPath(uri);
    }
}

