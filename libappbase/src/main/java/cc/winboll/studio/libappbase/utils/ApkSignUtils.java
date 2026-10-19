package cc.winboll.studio.libappbase.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import cc.winboll.studio.libappbase.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026-01-24 10:00:00
 * @LastEditTime 2026-01-24 22:00:00
 * @Describe 客户端签名工具类：与服务端APKFileUtils签名/哈希校验逻辑严格对齐，纯Java7实现；兼容MT重签名（遍历META-INF所有RSA文件），增加PackageManager兜底方案
 */
public class ApkSignUtils {
    // ===================================== 全局常量定义 =====================================
    private static final String TAG = "ApkSignUtils";
    // 加密算法常量
    private static final String ALGORITHM_SHA1 = "SHA1";
    private static final String ALGORITHM_SHA256 = "SHA-256";
    // 缓冲区大小常量（按业务场景区分）
    private static final int BUFFER_4K = 4096;
    private static final int BUFFER_8K = 8192;
    // 签名文件目录与后缀
    private static final String META_INF_DIR = "META-INF/";
    private static final String RSA_SUFFIX_UPPER = ".RSA";
    private static final String RSA_SUFFIX_LOWER = ".rsa";

    // ===================================== 对外核心方法 =====================================
    /**
     * 获取与服务端对齐的签名Base64串（兼容MT重签名）
     * 优先逻辑：遍历APK内META-INF所有.RSA文件 → 读取第一个有效文件原始字节 → SHA1摘要 → Base64.NO_WRAP
     * 兜底逻辑：PackageManager获取系统解析的签名 → SHA1摘要 → Base64.NO_WRAP
     * @param context 上下文，用于获取当前应用APK路径/包信息
     * @return 签名Base64字符串，任意步骤失败返回null
     */
    public static String getApkSignAlignedWithServer(Context context) {
        LogUtils.d(TAG, "getApkSignAlignedWithServer: 方法调用，开始执行服务端对齐签名计算（兼容MT重签名）");
        // 入参空值快速校验
        if (context == null) {
            LogUtils.w(TAG, "getApkSignAlignedWithServer: 入参context为null，直接返回null");
            return null;
        }

        // 方案1：优先读取APK内META-INF目录下所有RSA文件（兼容MT重签名任意命名）
        String signBase64 = getSignFromApkRsaFile(context);
        if (signBase64 != null) {
            LogUtils.d(TAG, "getApkSignAlignedWithServer: 方案1成功（APK内读取RSA文件），返回签名Base64");
            return signBase64;
        }

        // 方案2：兜底 - PackageManager获取系统解析的应用签名（避免APK文件读取失败）
        signBase64 = getSignFromPackageManager(context);
        if (signBase64 != null) {
            LogUtils.d(TAG, "getApkSignAlignedWithServer: 方案2成功（PackageManager兜底），返回签名Base64");
            return signBase64;
        }

        // 所有方案失败
        LogUtils.e(TAG, "getApkSignAlignedWithServer: 所有签名获取方案均失败");
        return null;
    }

    /**
     * 获取当前运行APK的SHA256哈希值（兼容重签名APK）
     * 逻辑：读取APK完整文件字节流 → SHA256摘要 → 转小写64位16进制字符串，服务端同款校验逻辑
     * @param context 上下文，用于获取当前应用APK的真实安装路径
     * @return SHA256小写16进制字符串，任意步骤失败返回null
     */
    public static String getApkSHA256Hash(Context context) {
        LogUtils.d(TAG, "getApkSHA256Hash: 方法调用，开始执行APK文件SHA256哈希计算");
        // 入参空值快速校验
        if (context == null) {
            LogUtils.w(TAG, "getApkSHA256Hash: 入参context为null，直接返回null");
            return null;
        }

        JarFile jarFile = null;
        FileInputStream fis = null;
        try {
            // 1. 获取当前应用APK真实路径
            ApplicationInfo appInfo = context.getApplicationContext().getApplicationInfo();
            String apkPath = appInfo.sourceDir;
            LogUtils.d(TAG, "getApkSHA256Hash: 成功获取APK路径，path=" + apkPath);
            if (apkPath == null || apkPath.trim().isEmpty()) {
                LogUtils.e(TAG, "getApkSHA256Hash: 获取到的APK路径为空，无法读取文件计算哈希");
                return null;
            }

            // 2. 读取APK文件并计算SHA256哈希（完善流关闭）
            File apkFile = new File(apkPath);
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA256);
            fis = new FileInputStream(apkFile);
            byte[] buffer = new byte[BUFFER_8K];
            int readLen;
            while ((readLen = fis.read(buffer)) != -1) {
                md.update(buffer, 0, readLen);
            }
            LogUtils.d(TAG, "getApkSHA256Hash: APK文件读取完成，开始转换哈希结果");

            // 3. 哈希字节数组转小写64位16进制字符串
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String sha256Hash = sb.toString();
            LogUtils.d(TAG, "getApkSHA256Hash: APK SHA256哈希计算完成，成功返回结果");
            return sha256Hash;

        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, "getApkSHA256Hash: 获取SHA-256算法实例失败", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getApkSHA256Hash: 计算APK SHA256哈希发生未知异常", e);
        } finally {
            // 强制关闭流，避免重签名APK解析的流泄漏
            try {
                if (fis != null) fis.close();
                if (jarFile != null) jarFile.close();
            } catch (IOException e) {
                LogUtils.e(TAG, "getApkSHA256Hash: 关闭流资源异常", e);
            }
        }
        return null;
    }

    // ===================================== 内部核心工具方法（兼容重签名） =====================================
    /**
     * 方案1：遍历APK内META-INF所有.RSA/.rsa文件，读取第一个有效文件计算签名
     * @param context 上下文
     * @return 签名Base64，失败返回null
     */
    private static String getSignFromApkRsaFile(Context context) {
        JarFile jarFile = null;
        InputStream is = null;
        try {
            // 获取APK路径
            ApplicationInfo appInfo = context.getApplicationContext().getApplicationInfo();
            String apkPath = appInfo.sourceDir;
            if (apkPath == null || apkPath.trim().isEmpty()) {
                LogUtils.w(TAG, "getSignFromApkRsaFile: APK路径为空，跳过该方案");
                return null;
            }

            // 打开APK的JarFile
            jarFile = new JarFile(apkPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            JarEntry targetRsaEntry = null;

            // 遍历所有条目，找到META-INF下第一个.RSA/.rsa文件
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                // 过滤：META-INF目录下 + 以.RSA/.rsa结尾 + 非目录
                if (entryName.startsWith(META_INF_DIR) && !entry.isDirectory()
					&& (entryName.endsWith(RSA_SUFFIX_UPPER) || entryName.endsWith(RSA_SUFFIX_LOWER))) {
                    targetRsaEntry = entry;
                    LogUtils.d(TAG, "getSignFromApkRsaFile: 找到有效签名文件，name=" + entryName);
                    break; // 取第一个有效RSA文件即可
                }
            }

            // 未找到任何RSA文件
            if (targetRsaEntry == null) {
                LogUtils.w(TAG, "getSignFromApkRsaFile: 未在META-INF找到任何.RSA/.rsa签名文件");
                return null;
            }

            // 读取RSA文件原始字节
            is = jarFile.getInputStream(targetRsaEntry);
            byte[] certRawBytes = readStreamToBytes(is);
            if (certRawBytes == null || certRawBytes.length == 0) {
                LogUtils.w(TAG, "getSignFromApkRsaFile: 读取RSA文件字节为空");
                return null;
            }

            // 计算SHA1+Base64
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA1);
            byte[] signDigest = md.digest(certRawBytes);
            return Base64.encodeToString(signDigest, Base64.NO_WRAP);

        } catch (Exception e) {
            LogUtils.e(TAG, "getSignFromApkRsaFile: 从APK内读取RSA文件失败", e);
            return null;
        } finally {
            // 强制关闭所有流
            try {
                if (is != null) is.close();
                if (jarFile != null) jarFile.close();
            } catch (IOException e) {
                LogUtils.e(TAG, "getSignFromApkRsaFile: 关闭流资源异常", e);
            }
        }
    }

    /**
     * 方案2：兜底 - 通过PackageManager获取系统解析的应用签名
     * 避免APK文件读取失败（如权限、解析问题），兼容所有重签名场景
     * @param context 上下文
     * @return 签名Base64，失败返回null
     */
    private static String getSignFromPackageManager(Context context) {
        try {
            // 获取当前应用包信息（包含签名）
            PackageInfo packageInfo = context.getPackageManager()
				.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                LogUtils.w(TAG, "getSignFromPackageManager: 未获取到应用签名信息");
                return null;
            }

            // 取第一个签名（重签名后一般只有一个签名）
            Signature signature = packageInfo.signatures[0];
            byte[] signBytes = signature.toByteArray();

            // 计算SHA1+Base64，与服务端逻辑对齐
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA1);
            byte[] signDigest = md.digest(signBytes);
            return Base64.encodeToString(signDigest, Base64.NO_WRAP);

        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "getSignFromPackageManager: 包名未找到，无法获取签名", e);
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, "getSignFromPackageManager: 获取SHA1算法实例失败", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getSignFromPackageManager: PackageManager获取签名失败", e);
        }
        return null;
    }

    /**
     * 输入流转字节数组，通用工具方法（完善try-finally）
     * 4K缓冲区，适配小文件读取（如RSA签名文件），保证流资源正常关闭
     * @param is 待读取的输入流
     * @return 转换后的字节数组，流为null/读取失败返回空字节数组
     * @throws IOException 流读取相关异常向上抛出
     */
    private static byte[] readStreamToBytes(InputStream is) throws IOException {
        if (is == null) {
            LogUtils.w(TAG, "readStreamToBytes: 入参输入流为null，返回空字节数组");
            return new byte[0];
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_4K];
        int readLen;
        try {
            while ((readLen = is.read(buffer)) != -1) {
                bos.write(buffer, 0, readLen);
            }
            return bos.toByteArray();
        } finally {
            // 强制关闭所有流
            is.close();
            bos.close();
        }
    }
}

