package cc.winboll.studio.winboll.utils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/04 13:36
 * @Describe RSA加密工具
 */
import android.content.Context;
import android.util.Base64;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import javax.crypto.Cipher;

public class RSAUtils {
    private static final String TAG = "RSAUtils";
    private static final int KEY_SIZE = 2048;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";
    private static final String CIPHER_ALGORITHM = KEY_ALGORITHM + "/ECB/PKCS1Padding"; // 保留原加密方式

    private final String keyPath;
    private static volatile RSAUtils INSTANCE;

    /**
     * 构造方法：初始化密钥存储路径（内部存储）
     */
    private RSAUtils(Context context) {
        keyPath = context.getFilesDir() + File.separator + "keys" + File.separator; // 修正路径格式
    }

    /**
     * 获取单例实例
     */
    public static synchronized RSAUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new RSAUtils(context);
        }
        return INSTANCE;
    }

    /**
     * 检查密钥文件是否存在
     */
    public boolean keysExist() {
        File publicKeyFile = new File(keyPath + PUBLIC_KEY_FILE);
        File privateKeyFile = new File(keyPath + PRIVATE_KEY_FILE);
        return publicKeyFile.exists() && privateKeyFile.exists();
    }

    /**
     * 生成密钥对并保存到文件
     */
    public void generateAndSaveKeys() throws Exception {
        LogUtils.d(TAG, "开始生成 RSA 密钥对（2048位）");
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        generator.initialize(KEY_SIZE);
        KeyPair keyPair = generator.generateKeyPair();

        saveKey(PUBLIC_KEY_FILE, keyPair.getPublic().getEncoded());
        saveKey(PRIVATE_KEY_FILE, keyPair.getPrivate().getEncoded());
        LogUtils.d(TAG, "密钥对生成并保存成功");
    }

    /**
     * 获取或生成密钥对（线程安全）
     */
    public KeyPair getOrGenerateKeys() throws Exception {
        if (!keysExist()) {
            synchronized (RSAUtils.class) { // 双重检查锁，避免多线程重复生成
                if (!keysExist()) {
                    generateAndSaveKeys();
                }
            }
        }
        return readKeysFromFile();
    }

    /**
     * 从文件读取密钥对
     */
    private KeyPair readKeysFromFile() throws Exception {
        LogUtils.d(TAG, "读取密钥对文件");
        try {
            byte[] publicKeyBytes = readFileToBytes(keyPath + PUBLIC_KEY_FILE);
            byte[] privateKeyBytes = readFileToBytes(keyPath + PRIVATE_KEY_FILE);

            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);

            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = factory.generatePublic(publicSpec);
            PrivateKey privateKey = factory.generatePrivate(privateSpec);

            return new KeyPair(publicKey, privateKey);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LogUtils.e(TAG, "密钥文件读取失败：" + e.getMessage());
            throw new Exception("密钥文件损坏或格式错误", e);
        }
    }

    /**
     * 保存密钥到文件（通用方法）
     */
    private void saveKey(String fileName, byte[] keyBytes) throws IOException {
        Objects.requireNonNull(keyBytes, "密钥字节数据不可为空");
        File dir = new File(keyPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("创建密钥目录失败：" + keyPath);
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(keyPath + fileName);
            fos.write(keyBytes);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "关闭文件流失败：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 读取文件为字节数组（Java 7 兼容）
     */
    private byte[] readFileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new IOException("文件不存在或为目录：" + filePath);
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            int bytesRead = fis.read(data);
            if (bytesRead != data.length) {
                throw new IOException("文件读取不完整");
            }
            return data;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "关闭文件流失败：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 公钥加密（带参数校验）
     */
    public byte[] encryptWithPublicKey(String plainText, PublicKey publicKey) throws Exception {
        Objects.requireNonNull(plainText, "明文不可为空");
        Objects.requireNonNull(publicKey, "公钥不可为空");

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // 检查数据长度是否超过 RSA 限制（2048位密钥最大明文为 214字节，PKCS1Padding）
        int maxPlainTextSize = cipher.getBlockSize() - 11; // PKCS1Padding 固定填充长度
        if (plainText.getBytes("UTF-8").length > maxPlainTextSize) {
            throw new IllegalArgumentException("明文过长，最大支持 " + maxPlainTextSize + " 字节");
        }

        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    /**
     * 私钥解密（带参数校验）
     */
    public String decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Objects.requireNonNull(encryptedData, "密文不可为空");
        Objects.requireNonNull(privateKey, "私钥不可为空");

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes, "UTF-8");
    }
    /**
     * 将 HTTP 传输的 Base64 字符串还原为加密字节数组（Java 7 兼容）
     * @param httpString Base64 字符串（非 null）
     * @return 加密字节数组
     * @throws IllegalArgumentException 解码失败时抛出
     */
    public byte[] httpStringToEncryptBytes(String httpString) {
        Objects.requireNonNull(httpString, "HTTP 字符串不可为空");

        // 计算缺失的填充符数量（Java 7 不支持 repeat()，手动拼接）
        int pad = httpString.length() % 4;
        StringBuilder paddedString = new StringBuilder(httpString);
        if (pad != 0) {
            for (int i = 0; i < pad; i++) {
                paddedString.append('='); // 补全 '='
            }
        }

        // 使用 Base64 解码（Android 原生 Base64 类兼容 Java 7）
        return Base64.decode(paddedString.toString(), Base64.URL_SAFE);
    }
}

