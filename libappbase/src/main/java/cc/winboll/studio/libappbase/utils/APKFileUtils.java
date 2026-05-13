package cc.winboll.studio.libappbase.utils;

import cc.winboll.studio.libappbase.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * APK文件工具类（单例）- 生产级签名+哈希双校验版（修复Too short异常）
 * 1. 稳定解析CERT.RSA原始字节，与客户端Signature.toByteArray()1:1对齐，解决X509解析异常
 * 2. 支持SHA256文件哈希字节级唯一校验，签名+哈希双重验证
 * 3. 入参包含：项目名/版本名/APK名/客户端签名/客户端哈希，适配生产级版本管理
 * 4. APK路径规范：apks_root/项目名/debug/tag/APK文件（支持调试/正式环境）
 * @Author ZhanGSKen<zhangsken@qq.com>
 */
public class APKFileUtils {
    // 单例实例
    private static volatile APKFileUtils sInstance;
    // 配置项
    private static final String CONFIG_SECTION = "APP";
    private static final String KEY_APKS_FOLDER = "apks_folder_path";
    // 算法常量（与客户端严格对齐）
    private static final String SIGN_ALGORITHM = "SHA1";    // 签名摘要算法
    private static final String HASH_ALGORITHM = "SHA-256"; // 文件哈希算法
    // 签名文件（兼容大小写，适配所有打包工具）
    private static final String CERT_RSA_UPPER = "META-INF/CERT.RSA";
    private static final String CERT_RSA_LOWER = "META-INF/cert.rsa";
    // APK根目录
    private String apksRootPath;

    private APKFileUtils() {}

    /**
     * 初始化工具类（需在应用启动时调用）
     */
    public static void init() {
        if (sInstance == null) {
            synchronized (APKFileUtils.class) {
                if (sInstance == null) {
                    sInstance = new APKFileUtils();
                    //sInstance.loadConfig();
                }
            }
        }
    }

    /**
     * 获取单例实例
     */
    public static APKFileUtils getInstance() {
        if (sInstance == null) {
            LogUtils.e("APKFileUtils", "请先调用init()初始化工具类");
            throw new IllegalStateException("APKFileUtils未初始化，请先调用init()");
        }
        return sInstance;
    }

    /**
     * 加载配置文件中的APK根目录
     */
//    private void loadConfig() {
//        try {
//            apksRootPath = IniConfigUtils.getConfigValue(CONFIG_SECTION, KEY_APKS_FOLDER, "").trim();
//            if (apksRootPath.isEmpty()) {
//                LogUtils.e("APKFileUtils", "配置项apks_folder_path为空，初始化失败");
//                return;
//            }
//            File rootDir = new File(apksRootPath);
//            if (!rootDir.exists() && !rootDir.mkdirs()) {
//                LogUtils.e("APKFileUtils", "APK根目录创建失败：" + apksRootPath);
//                apksRootPath = "";
//                return;
//            }
//            LogUtils.i("APKFileUtils", "APK根目录加载成功：" + apksRootPath);
//        } catch (Exception e) {
//            LogUtils.e("APKFileUtils", "加载APK根目录配置失败", e);
//            apksRootPath = "";
//        }
//    }

    /**
     * 对外暴露核心校验方法：签名 + SHA256文件哈希 双校验
     * 入参包含：项目名/版本名/APK文件名/客户端签名Base64/客户端文件哈希
     * APK路径规范：apksRootPath/项目名/版本名/APK文件
     * @param projectName 项目名（非空）
     * @param versionName 版本名（非空，如15.11.11）
     * @param apkFileName APK文件名（非空，需以.apk结尾）
     * @param clientSignBase64 客户端传入的签名Base64（非空）
     * @param clientFileHash 客户端传入的APK文件SHA256哈希（小写/大写均可，非空）
     * @return 校验通过返回true，否则false
     */
    public static boolean checkAPK(String projectName, String versionName, String apkFileName,
                                   String clientSignBase64, String clientFileHash) {
        return getInstance().doCheckAPK(projectName, versionName, apkFileName, clientSignBase64, clientFileHash);
    }

    /**
     * 核心校验实现：严格按「哈希先验，签名后验」顺序，哈希不匹配直接返回
     */
    private boolean doCheckAPK(String projectName, String versionName, String apkFileName,
                               String clientSignBase64, String clientFileHash) {
        // 1. 基础入参非空校验
        if (isParamEmpty(projectName) || isParamEmpty(versionName) || isParamEmpty(apkFileName)
            || isParamEmpty(clientSignBase64) || isParamEmpty(clientFileHash)) {
            LogUtils.w("APKFileUtils", "基础参数不能为空：projectName/versionName/apkFileName/clientSignBase64/clientFileHash");
            return false;
        }
        // 2. APK文件名格式校验
        if (!apkFileName.endsWith(".apk")) {
            LogUtils.w("APKFileUtils", "APK文件名格式错误，需以.apk结尾：" + apkFileName);
            return false;
        }
        // 3. APK根目录校验
        if (isParamEmpty(apksRootPath)) {
            LogUtils.w("APKFileUtils", "APK根目录未配置，无法进行校验");
            return false;
        }
        // 4. 拼接标准APK路径：根目录/项目名/debug/项目名_版本名.apk（调试环境，可切换tag）
        String apkFullPath = String.format("%s/%s/debug/%s_%s.apk",
                                           apksRootPath,
                                           projectName,
                                           projectName,
                                           versionName);
        //正式环境路径（注释保留，切换时解开即可）
//        String apkFullPath = String.format("%s/%s/tag/%s_%s.apk",
//                                           apksRootPath,
//                                           projectName,
//                                           projectName,
//                                           versionName);
        LogUtils.d("APKFileUtils", String.format("apkFullPath : %s", apkFullPath));
        File apkFile = new File(apkFullPath);
        // 5. APK文件存在性校验
        if (!apkFile.exists() || !apkFile.isFile()) {
            LogUtils.w("APKFileUtils", "APK文件不存在或非文件类型：" + apkFullPath);
            return false;
        }

        try {
            // ===== 第一步：SHA256文件哈希校验（字节级唯一，优先级最高）=====
            String serverFileHash = getAPKFileHash(apkFile);
            if (isParamEmpty(serverFileHash)) {
                LogUtils.w("APKFileUtils", "解析服务端APK文件哈希失败：" + apkFileName);
                return false;
            }
            boolean isHashMatch = serverFileHash.equalsIgnoreCase(clientFileHash.trim());
            LogUtils.d("APKFileUtils", "【哈希对比】服务端SHA256：" + serverFileHash);
            LogUtils.d("APKFileUtils", "【哈希对比】客户端SHA256：" + clientFileHash.trim());
            if (!isHashMatch) {
                LogUtils.i("APKFileUtils", "【哈希对比结果】❌ 不匹配（字节级文件不一致）");
                return false;
            }
            LogUtils.i("APKFileUtils", "【哈希对比结果】✅ 匹配（字节级文件完全一致）");

            // ===== 第二步：签名校验（直接读取CERT.RSA原始字节，与客户端严格对齐）=====
            String serverSignBase64 = getAPKSign(apkFile);
            if (isParamEmpty(serverSignBase64)) {
                LogUtils.w("APKFileUtils", "解析服务端APK签名失败：" + apkFileName);
                return false;
            }
            boolean isSignMatch = serverSignBase64.equals(clientSignBase64.trim());
            LogUtils.d("APKFileUtils", "【签名对比】服务端Base64：" + serverSignBase64);
            LogUtils.d("APKFileUtils", "【签名对比】客户端Base64：" + clientSignBase64.trim());
            if (!isSignMatch) {
                LogUtils.i("APKFileUtils", "【签名对比结果】❌ 不匹配（签名不一致）");
                return false;
            }
            LogUtils.i("APKFileUtils", "【签名对比结果】✅ 匹配（签名完全一致）");

            // 所有校验通过
            LogUtils.i("APKFileUtils", "APK双校验全部通过：项目名=" + projectName + "，版本名=" + versionName + "，文件名=" + apkFileName);
            return true;
        } catch (Exception e) {
            LogUtils.e("APKFileUtils", "APK双校验异常", e);
            return false;
        }
    }

    /**
     * 稳定解析APK签名：直接读取CERT.RSA原始字节，SHA1+Base64（与客户端1:1对齐）
     * 解决X509证书解析的Too short异常，兼容所有APK（普通/加固/自定义打包）
     * @param apkFile APK文件
     * @return 签名Base64字符串，失败返回null
     */
    private String getAPKSign(File apkFile) {
        JarFile jarFile = null;
        InputStream certIs = null;
        try {
            jarFile = new JarFile(apkFile);
            // 先找大写CERT.RSA，找不到再找小写，兼容所有打包工具
            JarEntry certEntry = jarFile.getJarEntry(CERT_RSA_UPPER);
            if (certEntry == null) {
                certEntry = jarFile.getJarEntry(CERT_RSA_LOWER);
                if (certEntry == null) {
                    LogUtils.w("APKFileUtils", "APK中未找到签名文件：META-INF/CERT.RSA/cert.rsa");
                    return null;
                }
            }
            // 核心：直接读取CERT.RSA的原始字节流（不做证书解析，适配PKCS7签名块）
            certIs = jarFile.getInputStream(certEntry);
            byte[] sigRawBytes = readStreamToBytes(certIs);
            if (sigRawBytes == null || sigRawBytes.length == 0) {
                LogUtils.w("APKFileUtils", "读取CERT.RSA原始字节为空");
                return null;
            }
            // 与客户端完全一致的处理流程：SHA1摘要 → Base64编码（去换行）
            MessageDigest md = MessageDigest.getInstance(SIGN_ALGORITHM);
            byte[] signDigest = md.digest(sigRawBytes);
            String signBase64 = Base64.getEncoder().encodeToString(signDigest)
				.replaceAll("\\r", "").replaceAll("\\n", "");

            LogUtils.d("APKFileUtils", "APK签名解析成功(Base64)：" + signBase64);
            return signBase64;
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e("APKFileUtils", "解析签名失败：" + SIGN_ALGORITHM + "算法不存在", e);
            return null;
        } catch (Exception e) {
            LogUtils.e("APKFileUtils", "解析APK签名异常", e);
            return null;
        } finally {
            // 强制关闭流资源，避免内存泄漏
            try {
                if (certIs != null) certIs.close();
                if (jarFile != null) jarFile.close();
            } catch (IOException e) {
                LogUtils.e("APKFileUtils", "关闭签名文件流失败", e);
            }
        }
    }

    /**
     * 解析APK文件的SHA256哈希（字节级唯一，任何字节修改都会改变）
     * @param apkFile APK文件
     * @return 小写64位SHA256哈希字符串，失败返回null
     */
    private String getAPKFileHash(File apkFile) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            fis = new FileInputStream(apkFile);
            byte[] buffer = new byte[8192]; // 8K缓冲区，提升大APK读取效率
            int len;
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            // 哈希字节转小写16进制字符串（64位，官方标准格式）
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String fileHash = sb.toString();
            LogUtils.d("APKFileUtils", "APK文件SHA256哈希解析成功：" + fileHash);
            return fileHash;
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e("APKFileUtils", "获取文件哈希失败：" + HASH_ALGORITHM + "算法不存在", e);
            return null;
        } catch (Exception e) {
            LogUtils.e("APKFileUtils", "解析APK文件哈希异常", e);
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtils.e("APKFileUtils", "关闭APK文件流失败", e);
                }
            }
        }
    }

    /**
     * 流转字节数组工具方法：稳定读取任意输入流，无截断/空指针问题
     */
    private byte[] readStreamToBytes(InputStream is) throws IOException {
        if (is == null) {
            LogUtils.w("APKFileUtils", "readStreamToBytes: 输入流为null");
            return new byte[0];
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        byte[] result = bos.toByteArray();
        // 按顺序关闭流
        is.close();
        bos.close();
        return result;
    }

    /**
     * 工具方法：判断参数是否为空（null/空字符串/全空格）
     */
    private boolean isParamEmpty(String param) {
        return param == null || param.trim().isEmpty();
    }
}

