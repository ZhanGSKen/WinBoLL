package cc.winboll.studio.libappbase.utils;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.SFTPAuthModel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;

/**
 * SFTP/FTP工具类（单例模式）- Java7兼容 · 适配FTPAuthModel实体类
 * 底层严格基于JSch 0.1.54原生ChannelSftp+SftpException接口实现，替换原commons-net FTP
 * 核心功能：登录/登出、文件上传/下载、文件夹列举、文件/文件夹存在性判断
 * 依赖：com.jcraft:jsch:0.1.54
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/30 19:04
 */
public class FTPUtils {
    // 单例实例（双重校验锁 volatile 保证可见性，Java7兼容）
    private static volatile FTPUtils sInstance;
    // JSch核心对象：Session（连接会话）、ChannelSftp（SFTP通道）
    private JSch mJSch;
    private Session mSession;
    private ChannelSftp mSftpChannel;
    // 日志TAG
    public static final String TAG = "FTPUtils";
    // SFTP默认端口（FTPAuthModel未设置时使用）
    private static final int DEFAULT_SFTP_PORT = 22;
    // 连接超时时间 5s（Java7原生Socket超时）
    private static final int CONNECT_TIMEOUT = 5000;

    // 私有构造器：禁止外部实例化
    private FTPUtils() {
        initSftpClient();
    }

    /**
     * 获取单例实例（双重校验锁，线程安全，Java7兼容）
     * @return FTPUtils 单例
     */
    public static FTPUtils getInstance() {
        if (sInstance == null) {
            synchronized (FTPUtils.class) {
                if (sInstance == null) {
                    sInstance = new FTPUtils();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化SFTP客户端（JSch），创建核心原生对象
     */
    private void initSftpClient() {
        if (mJSch == null) {
            mJSch = new JSch();
            LogUtils.d(TAG, "SFTP客户端（JSch）初始化完成");
        }
        // 重置会话和通道，避免连接残留
        mSession = null;
        mSftpChannel = null;
    }

    /**
     * 【推荐】SFTP登录（基于FTPAuthModel实体类，完全兼容原有参数）
     * @param ftpAuthModel 登录配置实体类（不能为空，端口默认22，编码默认UTF-8）
     * @return 登录成功返回true，失败false
     */
    public boolean login(SFTPAuthModel ftpAuthModel) {
        // 1. 实体类非空校验
        if (ftpAuthModel == null) {
            LogUtils.e(TAG, "SFTP登录失败：FTPAuthModel实体类为null");
            return false;
        }
        // 2. 核心参数校验（服务器地址不能为空）
        if (isParamEmpty(ftpAuthModel.getFtpServer())) {
            LogUtils.e(TAG, "SFTP登录失败：服务器地址（ftpServer）不能为空");
            return false;
        }
        // 3. 若已连接，先断开
        if (isConnected()) {
            logout();
        }
        // 4. 重新初始化客户端
        initSftpClient();

        try {
            // 获取服务器地址、端口（默认22）、账号、密码
            String host = ftpAuthModel.getFtpServer();
            int port = ftpAuthModel.getFtpPort() <= 0 ? DEFAULT_SFTP_PORT : ftpAuthModel.getFtpPort();
            String username = ftpAuthModel.getFtpUsername();
            String password = ftpAuthModel.getFtpPassword();

            // SFTP不支持匿名登录，账号密码不能为空（原生接口无匿名登录能力）
            if (isParamEmpty(username) || isParamEmpty(password)) {
                LogUtils.e(TAG, "SFTP登录失败：SFTP不支持匿名登录，请配置有效账号密码");
                return false;
            }

            // 1. 创建JSch会话（原生接口）
            mSession = mJSch.getSession(username, host, port);
            mSession.setPassword(password);

            // 2. 设置会话属性（跳过SSH密钥校验，适配大部分服务器）
            Properties sessionProps = new Properties();
            sessionProps.put("StrictHostKeyChecking", "no");
            sessionProps.put("PreferredAuthentications", "password");
            mSession.setConfig(sessionProps);

            // 3. 设置会话连接超时（原生接口，底层Socket超时）
            mSession.setTimeout(CONNECT_TIMEOUT);

            // 4. 建立会话连接（原生接口）
            mSession.connect();
            LogUtils.d(TAG, "SFTP会话连接成功：" + host + ":" + port);

            // 5. 打开SFTP通道（类型：sftp，原生接口强转）
            mSftpChannel = (ChannelSftp) mSession.openChannel("sftp");
            mSftpChannel.connect();

            // 6. 设置文件名编码（解决中文乱码，ChannelSftp原生接口）
            String charset = isParamEmpty(ftpAuthModel.getFtpCharset()) ? "UTF-8" : ftpAuthModel.getFtpCharset();
            mSftpChannel.setFilenameEncoding(charset);
            LogUtils.d(TAG, "SFTP文件名编码设置成功：" + charset);

            LogUtils.i(TAG, "SFTP登录成功，服务器：" + host + ":" + port + "，用户名：" + username);
            return true;

        } catch (JSchException e) {
            LogUtils.e(TAG, "SFTP登录JSch异常：" + e.getMessage(), e);
            logout();
            return false;
        } catch (SftpException e) {
            // 匹配SftpException原生属性和方法
            LogUtils.e(TAG, "SFTP通道初始化异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
            logout();
            return false;
        }
    }

    /**
     * 【已废弃】原FTP多参数登录方法，适配JSch后保留，推荐使用login(FTPAuthModel)
     * @deprecated 请使用基于FTPAuthModel的登录方法
     */
    @Deprecated
    public boolean login(String host, int port, String username, String password) {
        SFTPAuthModel ftpAuthModel = new SFTPAuthModel();
        ftpAuthModel.setFtpServer(host);
        ftpAuthModel.setFtpPort(port <= 0 ? DEFAULT_SFTP_PORT : port);
        ftpAuthModel.setFtpUsername(username);
        ftpAuthModel.setFtpPassword(password);
        return login(ftpAuthModel);
    }

    /**
     * SFTP登出并断开连接，释放所有资源（严格调用原生disconnect接口）
     * @return 登出成功返回true，失败false
     */
    public boolean logout() {
        boolean isSuccess = true;
        // 关闭SFTP通道（原生接口disconnect，非空判断即可）
        if (mSftpChannel != null) {
            try {
                mSftpChannel.disconnect();
                LogUtils.d(TAG, "SFTP通道已断开");
            } catch (Exception e) {
                LogUtils.e(TAG, "关闭SFTP通道异常：" + e.getMessage(), e);
                isSuccess = false;
            }
        }
        // 关闭JSch会话（原生接口disconnect，非空判断即可）
        if (mSession != null) {
            try {
                mSession.disconnect();
                LogUtils.d(TAG, "SFTP会话已断开");
            } catch (Exception e) {
                LogUtils.e(TAG, "关闭SFTP会话异常：" + e.getMessage(), e);
                isSuccess = false;
            }
        }
        // 重置客户端，避免资源残留
        initSftpClient();
        if (isSuccess) {
            LogUtils.i(TAG, "SFTP登出成功");
        } else {
            LogUtils.w(TAG, "SFTP登出失败：部分资源未正常释放");
        }
        return isSuccess;
    }

    /**
     * 强制断开连接（兜底资源释放），同logout方法
     */
    public void disconnect() {
        logout();
    }

    /**
     * 判断SFTP是否已连接（会话+通道均调用原生isConnected接口）
     * @return 已连接返回true，否则false
     */
    public boolean isConnected() {
        return mSession != null && mSession.isConnected()
			&& mSftpChannel != null && mSftpChannel.isConnected();
    }

    /**
     * 上传文件到SFTP指定路径（覆盖式上传，调用ChannelSftp原生put接口，OVERWRITE模式）
     * @param localFilePath 本地文件绝对路径（如/sdcard/test.apk）
     * @param remoteFilePath SFTP服务器目标路径（如/ftp/apk/test.apk，需包含文件名）
     * @return 上传成功返回true，失败false
     */
    public boolean uploadFile(String localFilePath, String remoteFilePath) {
        // 前置校验
        if (!isConnected()) {
            LogUtils.e(TAG, "文件上传失败：SFTP未连接服务器");
            return false;
        }
        if (isParamEmpty(localFilePath) || isParamEmpty(remoteFilePath)) {
            LogUtils.e(TAG, "文件上传失败：本地/远程路径不能为空");
            return false;
        }
        File localFile = new File(localFilePath);
        if (!localFile.exists() || !localFile.isFile()) {
            LogUtils.e(TAG, "文件上传失败：本地文件不存在/非文件，路径：" + localFilePath);
            return false;
        }

        InputStream fis = null;
        try {
            // 自动创建远程多级目录（基于原生mkdir/stat接口）
            createRemoteDir(remoteFilePath);
            // 读取本地文件，上传到SFTP（原生put接口，OVERWRITE覆盖模式）
            fis = new FileInputStream(localFile);
            mSftpChannel.put(fis, remoteFilePath, ChannelSftp.OVERWRITE);
            LogUtils.i(TAG, "文件上传成功：本地" + localFilePath + " → 远程" + remoteFilePath);
            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "文件上传IO异常：" + e.getMessage(), e);
            return false;
        } catch (SftpException e) {
            // 严格匹配SftpException原生属性：id、getMessage()、toString()
            LogUtils.e(TAG, "文件上传SFTP异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
            return false;
        } finally {
            // 关闭流资源，避免内存泄漏
            closeStream(fis, null);
        }
    }

    /**
     * 从SFTP下载文件到本地指定路径（覆盖式下载，调用ChannelSftp原生get接口）
     * @param remoteFilePath SFTP服务器文件路径（如/ftp/apk/test.apk）
     * @param localFilePath 本地目标路径（如/sdcard/test.apk，需包含文件名）
     * @return 下载成功返回true，失败false
     */
    public boolean downloadFile(String remoteFilePath, String localFilePath) {
        // 前置校验
        if (!isConnected()) {
            LogUtils.e(TAG, "文件下载失败：SFTP未连接服务器");
            return false;
        }
        if (isParamEmpty(remoteFilePath) || isParamEmpty(localFilePath)) {
            LogUtils.e(TAG, "文件下载失败：远程/本地路径不能为空");
            return false;
        }
        // 校验远程文件是否存在（基于ChannelSftp原生stat接口）
        if (!isFileExists(remoteFilePath)) {
            LogUtils.e(TAG, "文件下载失败：远程文件不存在，路径：" + remoteFilePath);
            return false;
        }

        OutputStream fos = null;
        try {
            // 创建本地多级目录
            File localFile = new File(localFilePath);
            File parentDir = localFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                LogUtils.e(TAG, "文件下载失败：创建本地目录失败，路径：" + parentDir.getAbsolutePath());
                return false;
            }
            // 从SFTP读取文件，写入本地（原生get接口）
            fos = new FileOutputStream(localFile);
            mSftpChannel.get(remoteFilePath, fos);
            LogUtils.i(TAG, "文件下载成功：远程" + remoteFilePath + " → 本地" + localFilePath);
            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "文件下载IO异常：" + e.getMessage(), e);
            // 删除未下载完成的本地文件
            new File(localFilePath).delete();
            return false;
        } catch (SftpException e) {
            // 严格匹配SftpException原生属性：id、getMessage()、toString()
            LogUtils.e(TAG, "文件下载SFTP异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
            // 删除未下载完成的本地文件
            new File(localFilePath).delete();
            return false;
        } finally {
            // 关闭流资源，避免内存泄漏
            closeStream(null, fos);
        }
    }

    /**
     * 列举SFTP指定文件夹下的所有文件/文件夹（返回ChannelSftp原生Vector，过滤.和..）
     * @param remoteDir SFTP服务器目录路径（如/ftp/apk/，结尾带/或不带均可）
     * @return 成功返回原生Vector<ChannelSftp.LsEntry>，失败返回空Vector
     */
    @SuppressWarnings("rawtypes")
    public Vector listDir(String remoteDir) {
        Vector fileList = new Vector();
        // 前置校验
        if (!isConnected()) {
            LogUtils.e(TAG, "列举目录失败：SFTP未连接服务器");
            return fileList;
        }
        if (isParamEmpty(remoteDir)) {
            LogUtils.e(TAG, "列举目录失败：远程目录路径不能为空");
            return fileList;
        }
        // 校验目录是否存在（基于ChannelSftp原生stat接口）
        if (!isDirExists(remoteDir)) {
            LogUtils.e(TAG, "列举目录失败：远程目录不存在，路径：" + remoteDir);
            return fileList;
        }

        try {
            // 列举目录下所有文件/文件夹（调用ChannelSftp原生ls接口，返回原生Vector）
            Vector vector = mSftpChannel.ls(remoteDir);
            if (vector != null && vector.size() > 0) {
                for (Object obj : vector) {
                    // 过滤.和..上级目录，仅保留有效文件/目录
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                    String fileName = entry.getFilename();
                    if (!".".equals(fileName) && !"..".equals(fileName)) {
                        fileList.add(obj);
                    }
                }
            }
            LogUtils.i(TAG, "列举目录成功：" + remoteDir + "，共" + fileList.size() + "个文件/文件夹");
        } catch (SftpException e) {
            // 严格匹配SftpException原生属性：id、getMessage()、toString()
            LogUtils.e(TAG, "列举目录SFTP异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
        }
        return fileList;
    }

    /**
     * 判断SFTP服务器上**文件**是否存在（基于ChannelSftp原生stat接口，匹配SftpException原生异常）
     * @param remoteFilePath SFTP服务器文件路径（如/ftp/apk/test.apk）
     * @return 存在且为文件返回true，否则false
     */
    public boolean isFileExists(String remoteFilePath) {
        // 前置校验
        if (!isConnected()) {
            LogUtils.e(TAG, "判断文件存在性失败：SFTP未连接服务器");
            return false;
        }
        if (isParamEmpty(remoteFilePath)) {
            LogUtils.e(TAG, "判断文件存在性失败：远程文件路径不能为空");
            return false;
        }

        try {
            // 调用ChannelSftp原生stat接口获取属性，不存在会抛出SSH_FX_NO_SUCH_FILE异常
            SftpATTRS attrs = mSftpChannel.stat(remoteFilePath);
            // 原生isReg()判断是否为文件
            return attrs.isReg();
        } catch (SftpException e) {
            // 仅匹配原生异常码SSH_FX_NO_SUCH_FILE(2)：文件/目录不存在，不记错误日志
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                LogUtils.e(TAG, "判断文件存在性SFTP异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
            }
            return false;
        }
    }

    /**
     * 判断SFTP服务器上**文件夹**是否存在（基于ChannelSftp原生stat接口，匹配SftpException原生异常）
     * @param remoteDir SFTP服务器目录路径（如/ftp/apk/，结尾带/或不带均可）
     * @return 存在且为目录返回true，否则false
     */
    public boolean isDirExists(String remoteDir) {
        // 前置校验
        if (!isConnected()) {
            LogUtils.e(TAG, "判断目录存在性失败：SFTP未连接服务器");
            return false;
        }
        if (isParamEmpty(remoteDir)) {
            LogUtils.e(TAG, "判断目录存在性失败：远程目录路径不能为空");
            return false;
        }

        try {
            // 调用ChannelSftp原生stat接口获取属性，不存在会抛出SSH_FX_NO_SUCH_FILE异常
            SftpATTRS attrs = mSftpChannel.stat(remoteDir);
            // 原生isDir()判断是否为目录
            return attrs.isDir();
        } catch (SftpException e) {
            // 仅匹配原生异常码SSH_FX_NO_SUCH_FILE(2)：文件/目录不存在，不记错误日志
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                LogUtils.e(TAG, "判断目录存在性SFTP异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
            }
            return false;
        }
    }

    // ===================================== 内部工具方法（仅调用原生接口） =====================================
    /**
     * 递归创建SFTP远程多级目录（基于ChannelSftp原生mkdir/stat接口，不存在则创建）
     * @param remoteFilePath SFTP远程文件路径/目录路径
     */
    private void createRemoteDir(String remoteFilePath) {
        if (!isConnected()) {
            LogUtils.e(TAG, "创建远程目录失败：SFTP未连接服务器");
            return;
        }
        try {
            // 提取目录路径（文件路径→目录路径，目录路径直接使用）
            String remoteDir = remoteFilePath.lastIndexOf("/") > 0
				? remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/"))
				: remoteFilePath;
            // 按/分割多级目录，递归创建（避免多级目录不存在）
            String[] dirs = remoteDir.split("/");
            StringBuilder currentDir = new StringBuilder();
            for (String dir : dirs) {
                if (isParamEmpty(dir)) {
                    continue;
                }
                currentDir.append("/").append(dir);
                String dirPath = currentDir.toString();
                // 目录不存在则调用ChannelSftp原生mkdir创建
                if (!isDirExists(dirPath)) {
                    mSftpChannel.mkdir(dirPath);
                    LogUtils.d(TAG, "创建SFTP远程目录成功：" + dirPath);
                }
            }
        } catch (SftpException e) {
            // 严格匹配SftpException原生属性：id、getMessage()、toString()
            LogUtils.e(TAG, "创建远程目录SFTP异常：id=" + e.id + "，msg=" + e.getMessage() + "，detail=" + e.toString());
        }
    }

    /**
     * 关闭流资源（通用工具方法，Java7原生IO，避免内存泄漏）
     * @param is 输入流（可为null）
     * @param os 输出流（可为null）
     */
    private void closeStream(InputStream is, OutputStream os) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LogUtils.e(TAG, "关闭输入流异常：" + e.getMessage(), e);
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LogUtils.e(TAG, "关闭输出流异常：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 判断参数是否为空（null/空字符串/全空格，Java7原生字符串操作）
     * @param param 待判断参数
     * @return 为空返回true，否则false
     */
    private boolean isParamEmpty(String param) {
        return param == null || param.trim().isEmpty();
    }
}

