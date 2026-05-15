package cc.winboll.studio.libappbase.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.SFTPAuthModel;

/**
 * 文件备份工具类（单例模式）
 * 区分应用Data目录/应用专属外部文件目录双Map管理备份文件路径
 * 核心功能：文件添加/移除 + ZIP打包（分data/sdcard目录） + SFTP分步式上传（登录→传输→登出）
 * 依赖：FTPUtils（单例）、SFTPAuthModel（外部实体类）、Android上下文
 * 兼容：Java7、Android 6.0+，无第三方依赖（ZIP为原生实现），免动态读写权限
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/30 20:18:00
 * @LastEditTime 2026/02/01 02:05:00
 */
public class BackupUtils {
    public static final String TAG = "BackupUtils";
    // ZIP内部分级目录常量（统一维护，便于修改）
    private static final String ZIP_DIR_DATA = "data/";
    private static final String ZIP_DIR_SDCARD = "sdcard/";

    // 单例实例（双重校验锁，volatile保证可见性，线程安全）
    private static volatile BackupUtils sInstance;

    // 双Map分目录管理：key=文件唯一标识，value=对应目录下的相对路径
    private final Map<String, String> mDataDirFileMap; // 基础根目录：应用私有Data目录(/data/data/[包名]/files)
    private final Map<String, String> mSdcardFileMap; // 基础根目录：应用专属外部文件目录(/storage/emulated/0/Android/data/[包名]/files)

    // 全局上下文（持有Application上下文，避免Activity内存泄漏）
    private Context mAppContext;
    // SFTP认证配置（直接引用外部实体类，无内部封装）
    private SFTPAuthModel mFtpAuthModel;
    // SFTP服务器指定上传目录（独立参数传入，标准化后作为成员变量）
    private String mFtpTargetDir;
    // 应用专属外部文件目录（SDCard Map的基础根目录，初始化时赋值，避免重复创建）
    private File mAppExternalFilesDir;

    // 私有构造器：新增双Map入参，空值则使用内部默认初始化，非空则用入参初始化
    private BackupUtils(Context context, SFTPAuthModel ftpAuthModel, String ftpTargetDir,
                        Map<String, String> dataDirFileMap, Map<String, String> sdcardFileMap) {
        this.mAppContext = context.getApplicationContext();
        this.mFtpAuthModel = ftpAuthModel;
        // 初始化SDCard Map的基础根目录：应用专属外部文件目录（/storage/emulated/0/Android/data/[包名]/files）
        this.mAppExternalFilesDir = mAppContext.getExternalFilesDir(null);
        // 标准化SFTP上传目录：空则默认/，非空则补全结尾斜杠
        this.mFtpTargetDir = TextUtils.isEmpty(ftpTargetDir) ? "/" : (ftpTargetDir.endsWith("/") ? ftpTargetDir : ftpTargetDir + "/");

        // 核心修改：入参Map非空且非空集合时，使用入参初始化；否则内部new HashMap()
        this.mDataDirFileMap = (dataDirFileMap != null && !dataDirFileMap.isEmpty()) 
			? new HashMap<String, String>(dataDirFileMap)
			: new HashMap<String, String>();
        this.mSdcardFileMap = (sdcardFileMap != null && !sdcardFileMap.isEmpty())
			? new HashMap<String, String>(sdcardFileMap)
			: new HashMap<String, String>();

        LogUtils.d(TAG, "BackupUtils初始化完成 → SFTP服务器：" + ftpAuthModel.getFtpServer() + ":" + ftpAuthModel.getFtpPort() + " | 上传目录：" + mFtpTargetDir);
        LogUtils.d(TAG, "SDCard Map基础根目录：" + (mAppExternalFilesDir == null ? "获取失败" : mAppExternalFilesDir.getAbsolutePath()));
        LogUtils.d(TAG, "初始化后DataMap大小：" + mDataDirFileMap.size() + " | SdcardMap大小：" + mSdcardFileMap.size());
    }

    /**
     * 单例初始化方法（必须先调用，否则getInstance()会抛异常）
     * 新增双Map入参，支持外部初始化待备份文件列表
     * @param context 上下文（推荐传Application，避免内存泄漏）
     * @param ftpAuthModel 外部SFTP认证实体类（含服务器/账号/端口等）
     * @param ftpTargetDir SFTP服务器指定上传目录（如/backup，自动补全斜杠）
     * @param dataDirFileMap 外部传入的Data目录文件Map，null/空则内部默认初始化
     * @param sdcardFileMap 外部传入的SDCard目录文件Map，null/空则内部默认初始化
     * @return BackupUtils单例实例
     */
    public static BackupUtils getInstance(Context context, SFTPAuthModel ftpAuthModel, String ftpTargetDir,
                                          Map<String, String> dataDirFileMap, Map<String, String> sdcardFileMap) {
        if (sInstance == null) {
            synchronized (BackupUtils.class) {
                if (sInstance == null) {
                    // 前置强校验：避免空参数导致后续空指针
                    if (context == null) {
                        throw new IllegalArgumentException("初始化失败：Context 不能为空");
                    }
                    if (ftpAuthModel == null || TextUtils.isEmpty(ftpAuthModel.getFtpServer())) {
                        throw new IllegalArgumentException("初始化失败：SFTPAuthModel/ftpServer 不能为空");
                    }
                    // 透传新增的双Map入参至构造器
                    sInstance = new BackupUtils(context, ftpAuthModel, ftpTargetDir, dataDirFileMap, sdcardFileMap);
                }
            }
        }
        return sInstance;
    }

    /**
     * 重载默认初始化方法：兼容原有调用逻辑，无需传入Map，内部默认初始化
     * 避免修改后影响原有代码调用
     */
    public static BackupUtils getInstance(Context context, SFTPAuthModel ftpAuthModel, String ftpTargetDir) {
        return getInstance(context, ftpAuthModel, ftpTargetDir, null, null);
    }

    /**
     * 获取单例实例（需先调用带参getInstance初始化）
     * @return BackupUtils单例实例
     */
    public static BackupUtils getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("BackupUtils未初始化，请先调用getInstance(Context, SFTPAuthModel, String[, Map, Map])");
        }
        return sInstance;
    }

    // ====================================== 以下原有方法均未修改 ======================================
    public void addDataDirFile(String key, String relativePath) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(relativePath)) {
            mDataDirFileMap.put(key, relativePath);
            LogUtils.d(TAG, "添加Data目录文件：" + key + " → " + relativePath);
        }
    }

    public void removeDataDirFile(String key) {
        if (!TextUtils.isEmpty(key) && mDataDirFileMap.containsKey(key)) {
            mDataDirFileMap.remove(key);
            LogUtils.d(TAG, "移除Data目录文件：" + key);
        }
    }

    public String getDataDirFile(String key) {
        return mDataDirFileMap.get(key);
    }

    public Map<String, String> getAllDataDirFiles() {
        return new HashMap<>(mDataDirFileMap);
    }

    public void clearDataDirFiles() {
        mDataDirFileMap.clear();
        LogUtils.d(TAG, "清空Data目录所有备份文件");
    }

    public void addSdcardFile(String key, String relativePath) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(relativePath) && mAppExternalFilesDir != null) {
            mSdcardFileMap.put(key, relativePath);
            LogUtils.d(TAG, "添加外部文件目录文件：" + key + " → " + relativePath);
        }
    }

    public void removeSdcardFile(String key) {
        if (!TextUtils.isEmpty(key) && mSdcardFileMap.containsKey(key)) {
            mSdcardFileMap.remove(key);
            LogUtils.d(TAG, "移除外部文件目录文件：" + key);
        }
    }

    public String getSdcardFile(String key) {
        return mSdcardFileMap.get(key);
    }

    public Map<String, String> getAllSdcardFiles() {
        return new HashMap<>(mSdcardFileMap);
    }

    public void clearSdcardFiles() {
        mSdcardFileMap.clear();
        LogUtils.d(TAG, "清空外部文件目录所有备份文件");
    }

    public boolean packAndUploadByFtp() {
        if (mDataDirFileMap.isEmpty() && mSdcardFileMap.isEmpty()) {
            LogUtils.e(TAG, "SFTP上传失败：无待备份文件（DataDir+外部文件目录均为空）");
            return false;
        }
        if (mAppExternalFilesDir == null) {
            LogUtils.e(TAG, "SFTP上传失败：应用专属外部文件目录获取失败，无法访问文件");
            return false;
        }

        String zipFileName = UUID.randomUUID().toString().replace("-", "")
			+ "-" + System.currentTimeMillis() + ".zip";
        File tempZipFile = new File(mAppContext.getExternalCacheDir(), zipFileName);
        String remoteFtpFilePath = mFtpTargetDir + zipFileName;

        FTPUtils ftpUtils = FTPUtils.getInstance();
        boolean isUploadSuccess = false;

        try {
            LogUtils.d(TAG, "开始SFTP登录：" + mFtpAuthModel.getFtpServer() + ":" + mFtpAuthModel.getFtpPort());
            boolean isFtpLogin = ftpUtils.login(mFtpAuthModel);
            if (!isFtpLogin) {
                LogUtils.e(TAG, "SFTP上传失败：SFTP登录失败（账号/密码/服务器/端口错误）");
                return false;
            }
            LogUtils.i(TAG, "SFTP登录成功，准备打包文件：" + zipFileName);

            LogUtils.d(TAG, "开始本地ZIP打包（分data/sdcard目录），临时文件路径：" + tempZipFile.getAbsolutePath());
            boolean isPackSuccess = packFilesToZip(tempZipFile);
            if (!isPackSuccess || !tempZipFile.exists() || tempZipFile.length() == 0) {
                LogUtils.e(TAG, "SFTP上传失败：ZIP打包失败（文件不存在/空文件）");
                return false;
            }
            LogUtils.i(TAG, "ZIP打包成功，文件大小：" + tempZipFile.length() / 1024 + "KB");

            LogUtils.d(TAG, "开始SFTP上传：本地→SFTP" + remoteFtpFilePath);
            isUploadSuccess = ftpUtils.uploadFile(tempZipFile.getAbsolutePath(), remoteFtpFilePath);
            if (isUploadSuccess) {
                LogUtils.i(TAG, "SFTP上传全流程成功：" + remoteFtpFilePath);
            } else {
                LogUtils.e(TAG, "SFTP上传失败：文件传输到服务器失败（响应码异常/权限不足）");
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "SFTP上传异常：" + e.getMessage(), e);
            isUploadSuccess = false;
        } finally {
            if (ftpUtils.isConnected()) {
                ftpUtils.logout();
            }
            ftpUtils.disconnect();
            if (tempZipFile.exists()) {
                boolean isDelete = tempZipFile.delete();
                LogUtils.d(TAG, "本地临时ZIP文件删除：" + (isDelete ? "成功" : "失败"));
            }
            System.gc();
        }

        return isUploadSuccess;
    }

    private boolean packFilesToZip(File zipFile) {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile), Charset.forName("UTF-8"));
            zos.setLevel(ZipOutputStream.DEFLATED);

            if (!mDataDirFileMap.isEmpty()) {
                packDirFilesToZip(zos, mDataDirFileMap, mAppContext.getFilesDir(), ZIP_DIR_DATA);
                LogUtils.d(TAG, "Data目录文件已打包到ZIP→" + ZIP_DIR_DATA + "子目录");
            }
            if (!mSdcardFileMap.isEmpty() && mAppExternalFilesDir != null) {
                packDirFilesToZip(zos, mSdcardFileMap, mAppExternalFilesDir, ZIP_DIR_SDCARD);
                LogUtils.d(TAG, "应用专属外部文件目录文件已打包到ZIP→" + ZIP_DIR_SDCARD + "子目录");
            }

            zos.flush();
            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "ZIP打包IO异常：" + e.getMessage(), e);
            return false;
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "关闭ZIP流异常：" + e.getMessage(), e);
                }
            }
        }
    }

    private void packDirFilesToZip(ZipOutputStream zos, Map<String, String> fileMap, File baseDir, String zipSubDir) {
        for (Map.Entry<String, String> entry : fileMap.entrySet()) {
            String relativePath = entry.getValue();
            if (TextUtils.isEmpty(relativePath)) {
                continue;
            }
            File localFile = new File(baseDir, relativePath);
            if (!localFile.exists() || !localFile.isFile()) {
                LogUtils.w(TAG, "跳过无效文件：" + localFile.getAbsolutePath());
                continue;
            }
            String zipInnerPath = zipSubDir + relativePath;
            try {
                addSingleFileToZip(zos, localFile, zipInnerPath);
            } catch (IOException e) {
                LogUtils.e(TAG, "打包单个文件失败：" + zipInnerPath, e);
            }
        }
    }

    private void addSingleFileToZip(ZipOutputStream zos, File localFile, String zipInnerPath) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipInnerPath);
        zos.putNextEntry(zipEntry);
        FileInputStream fis = new FileInputStream(localFile);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, len);
        }
        fis.close();
        zos.closeEntry();
    }
}

