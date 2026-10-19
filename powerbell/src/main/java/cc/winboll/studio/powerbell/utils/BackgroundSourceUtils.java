package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import androidx.core.content.FileProvider;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.BuildConfig;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/18 12:07:20
 * @Describe 背景图片工具集（精简版：复用FileUtils，聚焦业务逻辑）
 */
public class BackgroundSourceUtils {

    // ====================== 常量定义（按功能分类置顶）======================
    public static final String TAG = "BackgroundSourceUtils";
    // FileProvider 授权常量
    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";
    // 目录名称常量
    private static final String CROP_CACHE_DIR_NAME = "cache";
    private static final String SOURCE_DIR_NAME = "BackgroundSource";
    private static final String COMPRESS_DIR_NAME = "BackgroundCompress";
    private static final String MODEL_DIR_NAME = "ModelDir";
    // 文件名称常量
    private static final String CURRENT_BEAN_FILE_NAME = "currentBackgroundBean.json";
    private static final String PREVIEW_BEAN_FILE_NAME = "previewBackgroundBean.json";
    private static final String BLANK_ASSET_PATH = "images/blank100x100.png";
    // 图片操作基础目录
    private static final String PICTURE_BASE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "PowerBell";
    // 压缩常量
    private static final int BITMAP_COMPRESS_QUALITY = 80;
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    // ====================== 成员变量（按依赖优先级+功能分类）======================
    // 单例实例
    private static volatile BackgroundSourceUtils sInstance;
    // 上下文（应用级，避免内存泄漏）
    private Context mContext;
    // 配置文件对象
    private File currentBackgroundBeanFile;
    private File previewBackgroundBeanFile;
    // Bean实例
    private BackgroundBean currentBackgroundBean;
    private BackgroundBean previewBackgroundBean;
    // 目录对象
    private File fPictureBaseDir;
    private File fCropCacheDir;
    private File fBackgroundSourceDir;
    private File fBackgroundCompressDir;
    private File fUtilsDir;
    private File fModelDir;
    // 裁剪文件对象
    private File mCropSourceFile;
    private File mCropResultFile;

    // ====================== 单例方法（双重校验锁）======================
    private BackgroundSourceUtils(Context context) {
        if (sInstance != null) {
            throw new RuntimeException("BackgroundSourceUtils 是单例类，禁止重复创建！");
        }
        this.mContext = context.getApplicationContext();
        LogUtils.d(TAG, "【单例初始化】开始初始化必要资源");
        initNecessaryDirs();
        initAllFiles();
        loadSettings();
        LogUtils.d(TAG, "【单例初始化】资源初始化完成");
    }

    public static BackgroundSourceUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BackgroundSourceUtils.class) {
                if (sInstance == null) {
                    sInstance = new BackgroundSourceUtils(context);
                }
            }
        }
        return sInstance;
    }

    // ====================== 生命周期方法（初始化→加载→保存）======================
    /**
     * 统一初始化所有必要目录
     */
    private void initNecessaryDirs() {
        LogUtils.d(TAG, "【目录初始化】开始创建所有必要目录");
        initPictureDirs();
        initJsonDirs();
        LogUtils.d(TAG, "【目录初始化】所有必要目录创建完成");
    }

    /**
     * 初始化图片操作目录
     */
    private void initPictureDirs() {
        fPictureBaseDir = new File(PICTURE_BASE_DIR);
        fBackgroundSourceDir = new File(fPictureBaseDir, SOURCE_DIR_NAME);
        fCropCacheDir = new File(fPictureBaseDir, CROP_CACHE_DIR_NAME);
        fBackgroundCompressDir = new File(fPictureBaseDir, COMPRESS_DIR_NAME);

        createDirWithPermission(fPictureBaseDir, "图片基础目录");
        createDirWithPermission(fBackgroundSourceDir, "图片存储目录");
        createDirWithPermission(fCropCacheDir, "裁剪缓存目录");
        createDirWithPermission(fBackgroundCompressDir, "压缩图存储目录");

        validatePictureDirs();
    }

    /**
     * 初始化JSON配置目录
     */
    private void initJsonDirs() {
        fUtilsDir = mContext.getExternalFilesDir(TAG);
        if (fUtilsDir == null) {
            LogUtils.e(TAG, "应用外置存储不可用，切换到应用内部缓存目录");
            fUtilsDir = mContext.getDataDir();
        }
        fModelDir = new File(fUtilsDir, MODEL_DIR_NAME);
        createDirWithPermission(fModelDir, "JSON配置目录");

        currentBackgroundBeanFile = new File(fModelDir, CURRENT_BEAN_FILE_NAME);
        previewBackgroundBeanFile = new File(fModelDir, PREVIEW_BEAN_FILE_NAME);
        LogUtils.d(TAG, "【配置文件初始化】当前Bean文件：" + currentBackgroundBeanFile.getAbsolutePath());
        LogUtils.d(TAG, "【配置文件初始化】预览Bean文件：" + previewBackgroundBeanFile.getAbsolutePath());
    }

    /**
     * 初始化所有文件
     */
    private void initAllFiles() {
        clearCropTempFiles();
        LogUtils.d(TAG, "【文件初始化】裁剪临时文件已清理");
    }

    /**
     * 加载背景配置
     */
    public void loadSettings() {
        LogUtils.d(TAG, "【配置加载】开始加载背景配置");
        // 加载当前Bean
        currentBackgroundBean = BackgroundBean.loadBeanFromFile(currentBackgroundBeanFile.getAbsolutePath(), BackgroundBean.class);
        if (currentBackgroundBean == null) {
            currentBackgroundBean = new BackgroundBean();
			currentBackgroundBean.setPixelColor(ImageUtils.getColorAccent(mContext));
            BackgroundBean.saveBeanToFile(currentBackgroundBeanFile.getAbsolutePath(), currentBackgroundBean);
            LogUtils.d(TAG, "【配置加载】正式背景Bean不存在，已创建新实例");
        }
        // 加载预览Bean
        previewBackgroundBean = BackgroundBean.loadBeanFromFile(previewBackgroundBeanFile.getAbsolutePath(), BackgroundBean.class);
        if (previewBackgroundBean == null) {
            previewBackgroundBean = new BackgroundBean();
			previewBackgroundBean.setPixelColor(ImageUtils.getColorAccent(mContext));
            BackgroundBean.saveBeanToFile(previewBackgroundBeanFile.getAbsolutePath(), previewBackgroundBean);
            LogUtils.d(TAG, "【配置加载】预览背景Bean不存在，已创建新实例");
        }
        LogUtils.d(TAG, "【配置加载】背景配置加载完成");
    }

    /**
     * 保存配置
     */
    public void saveSettings() {
        LogUtils.d(TAG, "【配置保存】开始保存背景配置");
        if (currentBackgroundBean == null || previewBackgroundBean == null) {
            LogUtils.e(TAG, "【配置保存】失败：current/preview Bean存在空值");
            return;
        }
        BackgroundBean.saveBeanToFile(currentBackgroundBeanFile.getAbsolutePath(), currentBackgroundBean);
        BackgroundBean.saveBeanToFile(previewBackgroundBeanFile.getAbsolutePath(), previewBackgroundBean);
        LogUtils.d(TAG, "【配置保存】两份背景配置保存成功");
    }

    // ====================== 工具方法（目录操作→文件操作→Uri转换→图片处理）======================
    /**
     * 创建目录并校验
     */
    private void createDirWithPermission(File dir, String dirDesc) {
        if (dir == null) {
            LogUtils.e(TAG, dirDesc + "创建失败：目录对象为null");
            return;
        }
        if (!dir.exists()) {
            LogUtils.d(TAG, dirDesc + "不存在，开始创建：" + dir.getAbsolutePath());
            dir.mkdirs();
        }
        if (!dir.exists()) {
            LogUtils.e(TAG, dirDesc + "创建失败：mkdirs返回false");
        }
    }

    /**
     * 校验图片目录是否就绪
     */
    private void validatePictureDirs() {
        boolean allReady = fPictureBaseDir.exists() && fBackgroundSourceDir.exists()
			&& fCropCacheDir.exists() && fBackgroundCompressDir.exists();
        if (allReady) {
            LogUtils.d(TAG, "【目录校验】所有图片目录均已就绪");
        } else {
            LogUtils.e(TAG, "【目录校验】部分图片目录未就绪，可能影响后续功能");
        }
    }

    /**
     * 清理单个旧文件
     */
    private void clearOldFile(File file, String fileDesc) {
        if (file == null) {
            return;
        }
        if (file.exists()) {
            boolean isDeleted = file.delete();
            LogUtils.d(TAG, fileDesc + (isDeleted ? "已删除" : "删除失败") + "：" + file.getAbsolutePath());
        }
    }

    /**
     * 生成新的裁剪文件名称
     */
    String genNewCropFileName() {
        String fileName = UUID.randomUUID().toString() + System.currentTimeMillis();
        LogUtils.d(TAG, "【文件命名】生成新裁剪文件名：" + fileName);
        return fileName;
    }

    /**
     * 将File转为ContentUri
     */
    public Uri getFileProviderUri(File file) {
        LogUtils.d(TAG, "【Uri转换】开始生成FileProvider Uri，文件路径：" + (file != null ? file.getAbsolutePath() : "null"));
        if (file == null || !file.exists()) {
            LogUtils.e(TAG, "【Uri转换】失败：文件为空或不存在");
            return null;
        }
        try {
            Uri contentUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentUri = FileProvider.getUriForFile(mContext, FILE_PROVIDER_AUTHORITY, file);
                LogUtils.d(TAG, "【Uri转换】7.0+ 生成ContentUri：" + contentUri.toString());
            } else {
                contentUri = Uri.fromFile(file);
                LogUtils.d(TAG, "【Uri转换】7.0以下 生成FileUri：" + contentUri.toString());
            }
            return contentUri;
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "【Uri转换】失败：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查背景是否为空并创建空白背景Bean
     */
    public boolean checkEmptyBackgroundAndCreateBlankBackgroundBean(BackgroundBean checkBackgroundBean) {
        LogUtils.d(TAG, "【空白背景检查】开始检查背景Bean");
        if (checkBackgroundBean == null) {
            LogUtils.e(TAG, "【空白背景检查】失败：检查Bean为空");
            return false;
        }
        File fCheckBackgroundFile = new File(checkBackgroundBean.getBackgroundFilePath());
        if (fCheckBackgroundFile.exists()) {
            LogUtils.d(TAG, "【空白背景检查】背景Bean文件存在，无需创建空白背景");
            return false;
        }
        LogUtils.d(TAG, "【空白背景检查】背景Bean文件不存在，开始创建空白背景");
        return createBlankBackgroundBean(checkBackgroundBean.getPixelColor());
    }

    /**
     * 获取目录类型描述
     */
    public String getDirTypeDesc(File dir) {
        if (dir == null) {
            return "未知目录（null）";
        }
        String dirPath = dir.getAbsolutePath();
        String publicPicturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String externalFilesPath = mContext.getExternalFilesDir(null) != null ? mContext.getExternalFilesDir(null).getAbsolutePath() : "";
        String cachePath = mContext.getCacheDir().getAbsolutePath();

        if (!TextUtils.isEmpty(publicPicturePath)) {
            if (dirPath.contains(publicPicturePath + File.separator + "PowerBell" + File.separator + COMPRESS_DIR_NAME)) {
                return "系统公共图片目录（/Pictures/PowerBell/BackgroundCompress，压缩图统一存储目录）";
            } else if (dirPath.contains(publicPicturePath + File.separator + "PowerBell")) {
                return "系统公共图片目录（/Pictures/PowerBell，图片存储/裁剪目录）";
            }
        } else if (!TextUtils.isEmpty(externalFilesPath) && dirPath.contains(externalFilesPath)) {
            return "应用私有外部目录（getExternalFilesDir()，JSON配置目录）";
        } else if (dirPath.contains(cachePath)) {
            return "应用内部缓存目录（getCacheDir()，兜底目录）";
        } else {
            return "外部存储目录（非应用私有，权限受限）";
        }
        return "未知目录";
    }

    /**
     * 获取图片旋转角度
     */
    public int getImageRotateAngle(String imagePath) {
        LogUtils.d(TAG, "【图片旋转角度】开始获取图片旋转角度，路径：" + imagePath);
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【图片旋转角度】失败：图片路径为空");
            return 0;
        }
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile() || imageFile.length() <= 0) {
            LogUtils.e(TAG, "【图片旋转角度】失败：图片文件无效：" + imagePath);
            return 0;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imageFile);
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    LogUtils.d(TAG, "【图片旋转角度】90度");
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    LogUtils.d(TAG, "【图片旋转角度】180度");
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    LogUtils.d(TAG, "【图片旋转角度】270度");
                    return 270;
                default:
                    LogUtils.d(TAG, "【图片旋转角度】0度（正常）");
                    return 0;
            }
        } catch (IOException e) {
            LogUtils.w(TAG, "【图片旋转角度】读取EXIF异常：" + e.getMessage());
            return 0;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "【图片旋转角度】流关闭失败：" + e.getMessage());
                }
            }
        }
    }

    // ====================== 核心业务方法（按功能分类）======================
    /**
     * 创建空白背景Bean
     */
    public boolean createBlankBackgroundBean(int nBackgroundPixelColor) {
        LogUtils.d(TAG, "【空白背景创建】开始创建空白背景，像素颜色：" + String.format("#%08X", nBackgroundPixelColor));
        String newCropFileName = genNewCropFileName();
        String fileSuffix = "png";
        mCropSourceFile = new File(fCropCacheDir, newCropFileName + "." + fileSuffix);
        mCropResultFile = new File(fCropCacheDir, "SelectCompress_" + newCropFileName + "." + fileSuffix);

        // 复制空白图片资源
        AssetsCopyUtils.copyAssetsFileToDir(mContext, BLANK_ASSET_PATH, mCropSourceFile.getAbsolutePath());
        LogUtils.d(TAG, "【空白背景创建】空白图片已复制到：" + mCropSourceFile.getAbsolutePath());

        // 创建结果文件
        try {
            mCropResultFile.createNewFile();
            LogUtils.d(TAG, "【空白背景创建】结果文件已创建：" + mCropResultFile.getAbsolutePath());
        } catch (IOException e) {
            LogUtils.e(TAG, "【空白背景创建】结果文件创建失败：" + e.getMessage());
            return false;
        }

        // 更新预览Bean
        loadSettings();
        previewBackgroundBean.setPixelColor(nBackgroundPixelColor);
        previewBackgroundBean.setIsUseBackgroundFile(true);
        previewBackgroundBean.setIsUseBackgroundScaledCompressFile(false);
        previewBackgroundBean.setBackgroundFileName(mCropSourceFile.getName());
        previewBackgroundBean.setBackgroundFilePath(mCropSourceFile.getAbsolutePath());
        previewBackgroundBean.setBackgroundScaledCompressFileName(mCropResultFile.getName());
        previewBackgroundBean.setBackgroundScaledCompressFilePath(mCropResultFile.getAbsolutePath());
        saveSettings();

        LogUtils.d(TAG, "【空白背景创建】空白背景创建成功并更新配置");
        return true;
    }

    /**
     * 创建并更新预览剪裁环境
     */
    public boolean createAndUpdatePreviewEnvironmentForCropping(BackgroundBean oldPreviewBackgroundBean) {
        LogUtils.d(TAG, "【预览剪裁环境】开始初始化预览剪裁环境");
        if (oldPreviewBackgroundBean == null) {
            LogUtils.e(TAG, "【预览剪裁环境】失败：旧预览Bean为空");
            return false;
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            clearCropTempFiles();
            // 检查并创建空白背景
            if (checkEmptyBackgroundAndCreateBlankBackgroundBean(oldPreviewBackgroundBean)) {
                LogUtils.d(TAG, "【预览剪裁环境】空白背景创建成功，直接返回");
                return true;
            }

            // 获取Uri和文件后缀
            Uri uri = UriUtils.getUriForFile(mContext, oldPreviewBackgroundBean.getBackgroundFilePath());
            LogUtils.d(TAG, "【预览剪裁环境】原Uri：" + uri);
            String fileSuffix = UriUtils.getSuffixFromUri(mContext, uri);
            LogUtils.d(TAG, "【预览剪裁环境】文件后缀：" + fileSuffix);

            // 初始化裁剪文件
            String newCropFileName = genNewCropFileName();
            mCropSourceFile = new File(fCropCacheDir, newCropFileName + "." + fileSuffix);
            mCropResultFile = new File(fCropCacheDir, "SelectCompress_" + newCropFileName + ".png");
            LogUtils.d(TAG, "【预览剪裁环境】裁剪数据源：" + mCropSourceFile.getAbsolutePath());
            LogUtils.d(TAG, "【预览剪裁环境】裁剪结果文件：" + mCropResultFile.getAbsolutePath());

            // 复制压缩文件
            if (FileUtils.isFileExists(oldPreviewBackgroundBean.getBackgroundScaledCompressFilePath())) {
                FileUtils.copyFile(new File(oldPreviewBackgroundBean.getBackgroundScaledCompressFilePath()), mCropResultFile);
                LogUtils.d(TAG, "【预览剪裁环境】已复制旧压缩文件");
            } else {
                mCropResultFile.createNewFile();
                LogUtils.d(TAG, "【预览剪裁环境】旧压缩文件不存在，已创建新文件");
            }

            // 复制源文件
            if (FileUtils.isFileExists(oldPreviewBackgroundBean.getBackgroundFilePath())) {
                FileUtils.copyFile(new File(oldPreviewBackgroundBean.getBackgroundFilePath()), mCropSourceFile);
                LogUtils.d(TAG, "【预览剪裁环境】已复制旧源文件");
            } else {
                mCropSourceFile.createNewFile();
                is = mContext.getContentResolver().openInputStream(uri);
                if (is == null) {
                    LogUtils.e(TAG, "【预览剪裁环境】ContentResolver打开Uri失败：" + uri.toString());
                    return false;
                }
                fos = new FileOutputStream(mCropSourceFile);
                byte[] buffer = new byte[1024 * 8];
                int readLen;
                while ((readLen = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, readLen);
                }
                fos.flush();
                try {
                    fos.getFD().sync();
                } catch (IOException e) {
                    LogUtils.w(TAG, "【预览剪裁环境】文件同步到磁盘失败，flush兜底：" + e.getMessage());
                    fos.flush();
                }
                LogUtils.d(TAG, "【预览剪裁环境】已从Uri读取并写入源文件");
            }

            // 更新预览Bean
            loadSettings();
            previewBackgroundBean.setBackgroundFileName(mCropSourceFile.getName());
            previewBackgroundBean.setBackgroundFilePath(mCropSourceFile.getAbsolutePath());
            previewBackgroundBean.setBackgroundScaledCompressFileName(mCropResultFile.getName());
            previewBackgroundBean.setBackgroundScaledCompressFilePath(mCropResultFile.getAbsolutePath());
            saveSettings();

            LogUtils.d(TAG, "【预览剪裁环境】预览剪裁环境初始化成功");
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "【预览剪裁环境】初始化异常：" + e.getMessage(), e);
            clearCropTempFiles();
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "【预览剪裁环境】输入流关闭失败：" + e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "【预览剪裁环境】输出流关闭失败：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 保存裁剪结果图到预览Bean
     */
    public BackgroundBean saveFileToPreviewBean(File sourceFile, String fileInfo) {
        LogUtils.d(TAG, "【裁剪结果保存】开始保存裁剪结果到预览Bean，源文件路径：" + (sourceFile != null ? sourceFile.getAbsolutePath() : "null"));
        if (sourceFile == null || !sourceFile.exists() || sourceFile.length() <= 0) {
            LogUtils.e(TAG, "【裁剪结果保存】失败：源文件无效");
            return previewBackgroundBean;
        }

        // 检查是否为原图目录
        String originalImageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        if (sourceFile.getAbsolutePath().contains(originalImageDir)) {
            LogUtils.w(TAG, "【裁剪结果保存】禁止复制原图，跳过保存");
            return previewBackgroundBean;
        }

        // 确保目录存在
        if (!fBackgroundSourceDir.exists() && !fBackgroundSourceDir.mkdirs()) {
            LogUtils.e(TAG, "【裁剪结果保存】失败：BackgroundSource目录创建失败");
            return previewBackgroundBean;
        }

        // 生成唯一文件名并复制
        String uniqueFileName = "bg_" + System.currentTimeMillis() + "_" + sourceFile.getName();
        File targetFile = new File(fBackgroundSourceDir, uniqueFileName);
        if (FileUtils.copyFile(sourceFile, targetFile)) {
            LogUtils.d(TAG, "【裁剪结果保存】裁剪结果图保存成功：" + targetFile.getAbsolutePath());
            // 更新预览Bean
            previewBackgroundBean.setBackgroundFileName(uniqueFileName);
            previewBackgroundBean.setBackgroundFilePath(targetFile.getAbsolutePath());
            previewBackgroundBean.setBackgroundFileInfo(fileInfo);
            previewBackgroundBean.setIsUseBackgroundFile(true);
            saveSettings();
        } else {
            LogUtils.e(TAG, "【裁剪结果保存】失败：裁剪结果图复制失败");
        }
        return previewBackgroundBean;
    }

    /**
     * 提交预览背景到正式背景
     */
    public void commitPreviewSourceToCurrent() {
        LogUtils.d(TAG, "【背景提交】开始深拷贝预览Bean到正式Bean");
        // 深拷贝Bean属性
        currentBackgroundBean = new BackgroundBean();
		currentBackgroundBean.setPixelColor(ImageUtils.getColorAccent(mContext));
        copyBackgroundBeanProperties(previewBackgroundBean, currentBackgroundBean);

        // 复制文件
        String previewFileName = previewBackgroundBean.getBackgroundFileName();
        String previewCropFileName = previewBackgroundBean.getBackgroundScaledCompressFileName();
        File previewFile = new File(previewBackgroundBean.getBackgroundFilePath());
        File previewCropFile = new File(previewBackgroundBean.getBackgroundScaledCompressFilePath());
        File currentFile = new File(fBackgroundSourceDir, previewFileName);
        File currentCropFile = new File(fBackgroundCompressDir, previewCropFileName);
        FileUtils.copyFile(previewFile, currentFile);
        FileUtils.copyFile(previewCropFile, currentCropFile);

        // 更新文件路径
        currentBackgroundBean.setBackgroundFilePath(currentFile.getAbsolutePath());
        currentBackgroundBean.setBackgroundScaledCompressFilePath(currentCropFile.getAbsolutePath());

        saveSettings();
        LogUtils.d(TAG, "【背景提交】预览背景提交到正式背景成功，两份实例完全独立");
        ToastUtils.show("背景图片应用成功");
    }

    /**
     * 将正式背景同步到预览背景
     */
    public void setCurrentSourceToPreview() {
        LogUtils.d(TAG, "【背景同步】开始深拷贝正式Bean到预览Bean");
        // 深拷贝Bean属性
        previewBackgroundBean = new BackgroundBean();
		previewBackgroundBean.setPixelColor(ImageUtils.getColorAccent(mContext));
        copyBackgroundBeanProperties(currentBackgroundBean, previewBackgroundBean);

        saveSettings();
        LogUtils.d(TAG, "【背景同步】正式背景同步到预览背景成功");
    }

    /**
     * 清理裁剪临时文件
     */
    void clearCropTempFiles() {
        LogUtils.d(TAG, "【裁剪文件清理】开始清理裁剪临时文件");
        File[] files = fCropCacheDir.listFiles();
        if (files == null) {
            LogUtils.d(TAG, "【裁剪文件清理】裁剪缓存目录为空，无需清理");
            return;
        }
        for (File file : files) {
            clearOldFile(file, "旧裁剪缓存文件");
        }
        mCropSourceFile = null;
        mCropResultFile = null;
        LogUtils.d(TAG, "【裁剪文件清理】裁剪临时文件清理完成");
    }

    /**
     * 复制文件
     */
    public boolean copyFile(File source, File target) {
        LogUtils.d(TAG, "【文件复制】开始复制文件，源文件：" + (source != null ? source.getAbsolutePath() : "null") + " 目标：" + (target != null ? target.getAbsolutePath() : "null"));
        if (source == null || TextUtils.isEmpty(source.getPath()) || (source.exists() && source.length() <= 0)) {
            if (target == null) {
                LogUtils.e(TAG, "【文件复制】失败：目标对象为null");
                return false;
            }
            File targetDir = target.isFile() ? target.getParentFile() : target;
            createDirWithPermission(targetDir, "空源文件场景-目录创建");
            LogUtils.d(TAG, "【文件复制】空源文件场景，目录创建完成");
            return true;
        }
        boolean isSuccess = FileUtils.copyFile(source, target);
        LogUtils.d(TAG, "【文件复制】" + (isSuccess ? "成功" : "失败"));
        return isSuccess;
    }

    /**
     * 迁移旧压缩图路径到新目录
     */
    private void migrateCompressPathToNewDir(BackgroundBean bean, boolean isCurrentBean) {
        LogUtils.d(TAG, "【路径迁移】开始迁移" + (isCurrentBean ? "正式" : "预览") + "Bean压缩路径");
        if (bean == null) {
            LogUtils.e(TAG, "【路径迁移】失败：Bean为空");
            return;
        }
        String oldCompressPath = bean.getBackgroundScaledCompressFilePath();
        String beanType = isCurrentBean ? "正式Bean" : "预览Bean";

        if (TextUtils.isEmpty(oldCompressPath) || oldCompressPath.contains(fBackgroundCompressDir.getAbsolutePath())) {
            LogUtils.d(TAG, "【路径迁移】" + beanType + "无需迁移：旧路径为空或已在目标目录");
            return;
        }

        File oldCompressFile = new File(oldCompressPath);
        if (!oldCompressFile.exists() || !oldCompressFile.isFile() || oldCompressFile.length() <= 0) {
            LogUtils.w(TAG, "【路径迁移】" + beanType + "旧压缩文件无效，无需迁移：" + oldCompressPath);
            String compressFileName = bean.getBackgroundScaledCompressFileName();
            if (!TextUtils.isEmpty(compressFileName)) {
                File newCompressFile = new File(fBackgroundCompressDir, compressFileName);
                bean.setBackgroundScaledCompressFilePath(newCompressFile.getAbsolutePath());
                saveSettings();
                LogUtils.d(TAG, "【路径迁移】" + beanType + "压缩路径已重置到目标目录");
            }
            return;
        }

        String compressFileName = bean.getBackgroundScaledCompressFileName();
        if (TextUtils.isEmpty(compressFileName)) {
            compressFileName = "ScaledCompress_" + System.currentTimeMillis() + ".jpg";
        }
        File newCompressFile = new File(fBackgroundCompressDir, compressFileName);

        boolean copySuccess = FileUtils.copyFile(oldCompressFile, newCompressFile);
        if (copySuccess) {
            bean.setBackgroundScaledCompressFilePath(newCompressFile.getAbsolutePath());
            saveSettings();
            clearOldFile(oldCompressFile, beanType + "旧压缩文件（迁移后清理）");
            LogUtils.d(TAG, "【路径迁移】" + beanType + "压缩路径迁移成功：" + oldCompressPath + " → " + newCompressFile.getAbsolutePath());
        } else {
            LogUtils.e(TAG, "【路径迁移】" + beanType + "压缩文件复制失败，迁移终止");
        }
    }

    /**
     * 压缩图片并保存（默认路径）
     */
    public void compressQualityToRecivedPicture(Bitmap bitmap) {
        LogUtils.d(TAG, "【图片压缩】使用默认路径压缩图片");
        String defaultCompressPath = getPreviewBackgroundScaledCompressFilePath();
        compressQualityToRecivedPicture(bitmap, defaultCompressPath);
    }

    /**
     * 压缩图片并保存（指定路径）
     */
    public void compressQualityToRecivedPicture(Bitmap bitmap, String targetCompressPath) {
        LogUtils.d(TAG, "【图片压缩】指定路径压缩图片，目标路径：" + targetCompressPath);
        if (bitmap == null || bitmap.isRecycled()) {
            ToastUtils.show("压缩失败：图片为空");
            LogUtils.e(TAG, "【图片压缩】失败：Bitmap为空或已回收");
            return;
        }
        if (TextUtils.isEmpty(targetCompressPath)) {
            ToastUtils.show("压缩失败：目标路径为空");
            LogUtils.e(TAG, "【图片压缩】失败：目标路径为空");
            return;
        }

        OutputStream outStream = null;
        FileOutputStream fos = null;
        try {
            LogUtils.d(TAG, "【图片压缩】Bitmap原始大小：" + bitmap.getByteCount() / 1024 + "KB");
            File targetCompressFile = new File(targetCompressPath);
            if (targetCompressFile.exists()) {
                targetCompressFile.delete();
                LogUtils.d(TAG, "【图片压缩】已删除旧压缩文件");
            }
            targetCompressFile.createNewFile();

            fos = new FileOutputStream(targetCompressFile);
            outStream = new BufferedOutputStream(fos);
            boolean compressSuccess = bitmap.compress(COMPRESS_FORMAT, BITMAP_COMPRESS_QUALITY, outStream);
            outStream.flush();
            try {
                fos.getFD().sync();
                LogUtils.d(TAG, "【图片压缩】图片已强制同步到磁盘");
            } catch (IOException e) {
                LogUtils.w(TAG, "【图片压缩】sync失败，flush兜底：" + e.getMessage());
                outStream.flush();
            }

            LogUtils.d(TAG, "【图片压缩】" + (compressSuccess ? "成功" : "失败") + "，大小：" + targetCompressFile.length() / 1024 + "KB");
            ToastUtils.show(compressSuccess ? "图片压缩成功" : "图片压缩失败");
        } catch (IOException e) {
            LogUtils.e(TAG, "【图片压缩】IO异常：" + e.getMessage(), e);
            ToastUtils.show("图片压缩失败");
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "【图片压缩】BufferedOutputStream关闭失败：" + e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "【图片压缩】FileOutputStream关闭失败：" + e.getMessage());
                }
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                LogUtils.d(TAG, "【图片压缩】Bitmap已回收");
            }
        }
    }

    // ====================== 辅助方法（属性拷贝）======================
    /**
     * 拷贝BackgroundBean属性（深拷贝）
     */
    private void copyBackgroundBeanProperties(BackgroundBean source, BackgroundBean target) {
        target.setBackgroundFileName(source.getBackgroundFileName());
        target.setBackgroundFilePath(source.getBackgroundFilePath());
        target.setBackgroundFileInfo(source.getBackgroundFileInfo());
        target.setIsUseBackgroundFile(source.isUseBackgroundFile());
        target.setBackgroundScaledCompressFileName(source.getBackgroundScaledCompressFileName());
        target.setBackgroundScaledCompressFilePath(source.getBackgroundScaledCompressFilePath());
        target.setIsUseBackgroundScaledCompressFile(source.isUseBackgroundScaledCompressFile());
        target.setBackgroundWidth(source.getBackgroundWidth());
        target.setBackgroundHeight(source.getBackgroundHeight());
        target.setPixelColor(source.getPixelColor());
    }

    // ====================== 对外提供的getter方法 ======================
    public BackgroundBean getCurrentBackgroundBean() {
        return currentBackgroundBean;
    }

    public BackgroundBean getPreviewBackgroundBean() {
        return previewBackgroundBean;
    }

    public String getPreviewBackgroundScaledCompressFilePath() {
        String compressFileName = previewBackgroundBean.getBackgroundScaledCompressFileName();
        if (TextUtils.isEmpty(compressFileName)) {
            LogUtils.e(TAG, "【路径获取】预览压缩背景文件名为空");
            return "";
        }
        File file = new File(fBackgroundCompressDir, compressFileName);
        return file.getAbsolutePath();
    }

    public String getCurrentBackgroundScaledCompressFilePath() {
        String compressFileName = currentBackgroundBean.getBackgroundScaledCompressFileName();
        if (TextUtils.isEmpty(compressFileName)) {
            LogUtils.e(TAG, "【路径获取】正式压缩背景文件名为空");
            return "";
        }
        File file = new File(fBackgroundCompressDir, compressFileName);
        return file.getAbsolutePath();
    }

    public String getBackgroundSourceDirPath() {
        return fBackgroundSourceDir.getAbsolutePath();
    }

    public String getBackgroundCompressDirPath() {
        return fBackgroundCompressDir.getAbsolutePath();
    }

    public String getCropCacheDir() {
        return fCropCacheDir.getAbsolutePath();
    }

    public String getFileProviderAuthority() {
        return FILE_PROVIDER_AUTHORITY;
    }
}

