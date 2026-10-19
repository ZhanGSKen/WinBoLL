package cc.winboll.studio.libappbase.utils;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Base64;
import cc.winboll.studio.libappbase.LogUtils;

import java.io.ByteArrayOutputStream;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/**
 * @Describe NFC RSA认证工具类，单例模式
 * 核心功能：RSA密钥生成、NFC密钥读写、本地data区密钥存储、密钥有效性校验、内存密钥缓存
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/11 21:00:00
 * @LastEditTime 2026/01/12 17:46:00
 */
public class NfcRsaAuthTool {
    // 常量配置（集中管理，简洁无冗余）
    private static final String TAG                  = "NfcRsaAuthTool";
    private static final String RSA_ALGORITHM        = "RSA";
    private static final int RSA_KEY_SIZE            = 2048;
    private static final String NFC_KEY_TAG          = "RSA_AUTH_PRIV_";
    private static final String CHARSET              = "UTF-8";
    private static final String LOCAL_KEY_FILE_NAME  = "rsa_auth_private.key";
    private static final String RSA_TEST_DATA        = "NFC_RSA_AUTH_VALID";

    // 单例实例（线程安全双重校验锁核心）
    private static volatile NfcRsaAuthTool sInstance;

    // 核心属性（按用途排序，注释清晰）
    private Context mContext;
    private NfcAdapter mNfcAdapter;
    private String mCachePrivateKeyStr;  // 内存缓存Base64私钥字符串
    private String mCachePublicKeyStr;   // 内存缓存Base64公钥字符串

    // 私有构造器（禁止外部实例化，绑定全局上下文）
    private NfcRsaAuthTool(Context context) {
        this.mContext = context.getApplicationContext();
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        LogUtils.d(TAG, "构造初始化完成，已绑定全局上下文");
    }

    /**
     * 获取单例实例（线程安全，双重校验锁，适配多线程场景）
     * @param context 上下文对象
     * @return 单例工具类实例
     */
    public static NfcRsaAuthTool getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NfcRsaAuthTool.class) {
                if (sInstance == null) {
                    sInstance = new NfcRsaAuthTool(context);
                    LogUtils.d(TAG, "首次创建单例实例成功");
                }
            }
        }
        LogUtils.d(TAG, "获取单例实例成功");
        return sInstance;
    }

    // ==================== 核心功能1：生成RSA私钥（返回Base64编码字符串，便于存储） ====================
    public String generateRsaPrivateKey() {
        LogUtils.d(TAG, "开始生成RSA私钥，密钥长度：" + RSA_KEY_SIZE);
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGen.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            String privateKeyStr = Base64.encodeToString(privateKey.getEncoded(), Base64.NO_WRAP);
            LogUtils.d(TAG, "RSA私钥生成成功，已完成Base64编码");
            return privateKeyStr;
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, "RSA私钥生成失败，无对应算法支持", e);
            return null;
        }
    }

    // ==================== 核心功能2：NFC密钥读写（适配NDEF标签，兼容已格式化/未格式化场景） ====================
    /**
     * 写入Base64私钥到NFC标签，带专属标识防数据混淆
     * @param tag NFC标签对象
     * @param privateKeyStr Base64编码私钥字符串
     * @return 写入成功返回true，失败返回false
     */
    public boolean writePrivateKeyToNfc(Tag tag, String privateKeyStr) {
        LogUtils.d(TAG, "写入NFC私钥，入参校验：Tag=" + tag + "，私钥非空=" + (privateKeyStr != null && !privateKeyStr.isEmpty()));
        if (tag == null || privateKeyStr == null || privateKeyStr.isEmpty() || mNfcAdapter == null) {
            LogUtils.w(TAG, "入参无效，写入失败（Tag/NFC适配器为空或私钥为空）");
            return false;
        }
        try {
            byte[] writeData = (NFC_KEY_TAG + privateKeyStr).getBytes(CHARSET);
            boolean result = writeNfcData(tag, writeData);
            LogUtils.d(TAG, "NFC私钥写入结果：" + result);
            return result;
        } catch (Exception e) {
            LogUtils.e(TAG, "NFC私钥写入异常", e);
            return false;
        }
    }

    /**
     * 从NFC标签读取私钥，自动校验标识并缓存到内存
     * @param tag NFC标签对象
     * @return 有效私钥返回Base64字符串，无效返回null
     */
    public String readPrivateKeyFromNfc(Tag tag) {
        LogUtils.d(TAG, "读取NFC私钥，Tag对象：" + tag);
        if (tag == null || mNfcAdapter == null) {
            LogUtils.w(TAG, "Tag或NFC适配器为空，读取失败");
            return null;
        }
        try {
            byte[] nfcData = readNfcData(tag);
            if (nfcData == null || nfcData.length == 0) {
                LogUtils.w(TAG, "NFC标签无有效存储数据");
                return null;
            }
            String allDataStr = new String(nfcData, CHARSET);
            if (!allDataStr.startsWith(NFC_KEY_TAG)) {
                LogUtils.w(TAG, "NFC数据无专属标识，判定为无效私钥数据");
                return null;
            }
            String privateKeyStr = allDataStr.substring(NFC_KEY_TAG.length());
            if (!privateKeyStr.isEmpty()) {
                mCachePrivateKeyStr = privateKeyStr;
                extractPublicKeyFromPrivateKeyStr(privateKeyStr);
                LogUtils.d(TAG, "NFC私钥读取成功，已缓存私钥并提取公钥");
            }
            return privateKeyStr;
        } catch (Exception e) {
            LogUtils.e(TAG, "NFC私钥读取异常", e);
            return null;
        }
    }

    // ==================== 核心功能3：本地data区密钥存储（仅应用可访问，安全存储） ====================
    /**
     * 私钥存储到应用内部data区，同步缓存到内存
     * @param privateKeyStr Base64编码私钥字符串
     * @return 存储成功返回true，失败返回false
     */
    public boolean savePrivateKeyToLocal(String privateKeyStr) {
        LogUtils.d(TAG, "本地存储私钥，私钥非空校验：" + (privateKeyStr != null && !privateKeyStr.isEmpty()));
        if (privateKeyStr == null || privateKeyStr.isEmpty()) {
            LogUtils.w(TAG, "待存储私钥为空，存储失败");
            return false;
        }
        File keyFile = new File(mContext.getFilesDir(), LOCAL_KEY_FILE_NAME);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(keyFile);
            fos.write(privateKeyStr.getBytes(CHARSET));
            fos.flush();
            mCachePrivateKeyStr = privateKeyStr;
            extractPublicKeyFromPrivateKeyStr(privateKeyStr);
            LogUtils.d(TAG, "私钥本地存储成功，存储路径：" + keyFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "私钥本地存储失败", e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtils.w(TAG, "关闭存储输出流异常", e);
                }
            }
        }
    }

    /**
     * 从本地data区读取私钥，同步缓存到内存
     * @return 本地私钥返回Base64字符串，无文件返回null
     */
    public String getLocalPrivateKey() {
        LogUtils.d(TAG, "开始读取本地存储私钥");
        File keyFile = new File(mContext.getFilesDir(), LOCAL_KEY_FILE_NAME);
        if (!keyFile.exists()) {
            LogUtils.w(TAG, "本地私钥文件不存在");
            return null;
        }
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(keyFile);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            String privateKeyStr = new String(bos.toByteArray(), CHARSET);
            mCachePrivateKeyStr = privateKeyStr;
            extractPublicKeyFromPrivateKeyStr(privateKeyStr);
            LogUtils.d(TAG, "本地私钥读取成功，已同步缓存");
            return privateKeyStr;
        } catch (Exception e) {
            LogUtils.e(TAG, "本地私钥读取失败", e);
            return null;
        } finally {
            try {
                if (fis != null) fis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                LogUtils.w(TAG, "关闭读取流异常", e);
            }
        }
    }

    // ==================== 核心功能4：私钥提取公钥（自动缓存，全局可用） ====================
    public String extractPublicKeyFromPrivateKeyStr(String privateKeyStr) {
        LogUtils.d(TAG, "从私钥提取公钥，私钥非空校验：" + (privateKeyStr != null && !privateKeyStr.isEmpty()));
        if (privateKeyStr == null || privateKeyStr.isEmpty()) {
            LogUtils.w(TAG, "待提取私钥为空，提取失败");
            return null;
        }
        try {
            byte[] priKeyBytes = Base64.decode(privateKeyStr, Base64.NO_WRAP);
            PKCS8EncodedKeySpec priSpec = new PKCS8EncodedKeySpec(priKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(priSpec);

            RSAPrivateCrtKeySpec privateCrtSpec = (RSAPrivateCrtKeySpec) keyFactory.getKeySpec(privateKey, RSAPrivateCrtKeySpec.class);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privateCrtSpec.getModulus(), privateCrtSpec.getPublicExponent());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            String publicKeyStr = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
            mCachePublicKeyStr = publicKeyStr;
            LogUtils.d(TAG, "公钥提取成功，已缓存");
            return publicKeyStr;
        } catch (Exception e) {
            LogUtils.e(TAG, "公钥提取失败", e);
            return null;
        }
    }

    // ==================== 核心功能5：密钥有效性校验（私钥自校验，公钥交叉校验） ====================
    /**
     * 私钥有效性校验：自加密自解密测试明文
     * @param privateKeyStr Base64编码私钥字符串
     * @return 有效返回true，无效返回false
     */
    public boolean validatePrivateKey(String privateKeyStr) {
        LogUtils.d(TAG, "开始校验私钥有效性");
        if (privateKeyStr == null || privateKeyStr.isEmpty()) {
            LogUtils.w(TAG, "待校验私钥为空，直接判定无效");
            return false;
        }
        try {
            byte[] priBytes = Base64.decode(privateKeyStr, Base64.NO_WRAP);
            PKCS8EncodedKeySpec priSpec = new PKCS8EncodedKeySpec(priBytes);
            PrivateKey privateKey = KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(priSpec);

            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptData = cipher.doFinal(RSA_TEST_DATA.getBytes(CHARSET));
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptData = cipher.doFinal(encryptData);

            boolean valid = RSA_TEST_DATA.equals(new String(decryptData, CHARSET));
            LogUtils.d(TAG, "私钥有效性校验结果：" + valid);
            return valid;
        } catch (Exception e) {
            LogUtils.e(TAG, "私钥校验异常，判定无效", e);
            return false;
        }
    }

    /**
     * 公钥有效性校验：私钥加密+公钥解密测试明文
     * @param privateKeyStr 基准Base64私钥字符串
     * @param publicKeyStr  待校验Base64公钥字符串
     * @return 有效返回true，无效返回false
     */
    public boolean validatePublicKey(String privateKeyStr, String publicKeyStr) {
        LogUtils.d(TAG, "开始校验公钥有效性，私钥非空=" + (privateKeyStr != null && !privateKeyStr.isEmpty()) + "，公钥非空=" + (publicKeyStr != null && !publicKeyStr.isEmpty()));
        if (privateKeyStr == null || publicKeyStr == null || privateKeyStr.isEmpty() || publicKeyStr.isEmpty()) {
            LogUtils.w(TAG, "私钥或公钥为空，直接判定无效");
            return false;
        }
        try {
            PrivateKey privateKey = getPrivateKeyFromStr(privateKeyStr);
            PublicKey publicKey = getPublicKeyFromStr(publicKeyStr);
            if (privateKey == null || publicKey == null) {
                LogUtils.w(TAG, "私钥或公钥转对象失败，判定无效");
                return false;
            }

            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptData = cipher.doFinal(RSA_TEST_DATA.getBytes(CHARSET));

            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptData = cipher.doFinal(encryptData);

            boolean valid = RSA_TEST_DATA.equals(new String(decryptData, CHARSET));
            LogUtils.d(TAG, "公钥有效性校验结果：" + valid);
            return valid;
        } catch (Exception e) {
            LogUtils.e(TAG, "公钥校验异常，判定无效", e);
            return false;
        }
    }

    // ==================== 内部辅助方法（密钥字符串转对象，仅工具类内部调用） ====================
    /**
     * 内部辅助：Base64私钥字符串转PrivateKey对象
     * @param privateKeyStr Base64编码私钥字符串
     * @return 转换成功返回对象，失败返回null
     */
    private PrivateKey getPrivateKeyFromStr(String privateKeyStr) {
        LogUtils.d(TAG, "私钥字符串转PrivateKey对象");
        if (privateKeyStr == null || privateKeyStr.isEmpty()) {
            LogUtils.w(TAG, "私钥字符串为空，转换失败");
            return null;
        }
        try {
            byte[] priBytes = Base64.decode(privateKeyStr, Base64.NO_WRAP);
            PKCS8EncodedKeySpec priSpec = new PKCS8EncodedKeySpec(priBytes);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(priSpec);
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, "设备不支持RSA算法，私钥转换失败", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "私钥格式无效，转换失败", e);
        }
        return null;
    }

    /**
     * 内部辅助：Base64公钥字符串转PublicKey对象
     * @param publicKeyStr Base64编码公钥字符串
     * @return 转换成功返回对象，失败返回null
     */
    private PublicKey getPublicKeyFromStr(String publicKeyStr) {
        LogUtils.d(TAG, "公钥字符串转PublicKey对象");
        if (publicKeyStr == null || publicKeyStr.isEmpty()) {
            LogUtils.w(TAG, "公钥字符串为空，转换失败");
            return null;
        }
        try {
            byte[] pubBytes = Base64.decode(publicKeyStr, Base64.NO_WRAP);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(pubSpec);
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, "设备不支持RSA算法，公钥转换失败", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "公钥格式无效，转换失败", e);
        }
        return null;
    }

    // ==================== 内部NFC读写辅助方法（底层交互，对外隐藏） ====================
    /**
     * 内部辅助：读取NFC标签原始字节数据
     */
    private byte[] readNfcData(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                byte[] data = ndef.getNdefMessage().getRecords()[0].getPayload();
                ndef.close();
                return data;
            } catch (Exception e) {
                LogUtils.e(TAG, "NDEF格式NFC读取异常", e);
                try { ndef.close(); } catch (IOException ex) { LogUtils.w(TAG, "关闭Ndef连接异常", ex); }
            }
        }
        LogUtils.w(TAG, "NFC标签非NDEF格式，无有效数据");
        return null;
    }

    /**
     * 内部辅助：写入字节数据到NFC标签，兼容两种标签状态
     */
    private boolean writeNfcData(Tag tag, byte[] data) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) return writeToNdef(ndef, data);

        NdefFormatable formatable = NdefFormatable.get(tag);
        if (formatable != null) return writeToFormatable(formatable, data);

        LogUtils.w(TAG, "NFC标签不支持NDEF格式，写入失败");
        return false;
    }

    /**
     * 内部辅助：写入数据到已格式化NDEF标签
     */
    private boolean writeToNdef(Ndef ndef, byte[] data) {
        try {
            ndef.connect();
            ndef.writeNdefMessage(createNdefMessage(data));
            ndef.close();
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "写入已格式化NFC异常", e);
            try { ndef.close(); } catch (IOException ex) { LogUtils.w(TAG, "关闭Ndef连接异常", ex); }
            return false;
        }
    }

    /**
     * 内部辅助：格式化标签并写入数据
     */
    private boolean writeToFormatable(NdefFormatable formatable, byte[] data) {
        try {
            formatable.connect();
            formatable.format(createNdefMessage(data));
            formatable.close();
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "格式化NFC并写入异常", e);
            try { formatable.close(); } catch (IOException ex) { LogUtils.w(TAG, "关闭NdefFormatable连接异常", ex); }
            return false;
        }
    }

    /**
     * 内部辅助：创建标准NDEF消息，适配NFC传输规范
     */
    private android.nfc.NdefMessage createNdefMessage(byte[] payload) {
        android.nfc.NdefRecord record = new android.nfc.NdefRecord(
			android.nfc.NdefRecord.TNF_MIME_MEDIA,
			"application/octet-stream".getBytes(),
			new byte[0],
			payload
        );
        return new android.nfc.NdefMessage(new android.nfc.NdefRecord[]{record});
    }

    // ==================== 对外公共访问方法（获取缓存/状态，简洁易用） ====================
    public String getCachePrivateKeyStr() {
        return mCachePrivateKeyStr;
    }

    public String getCachePublicKeyStr() {
        return mCachePublicKeyStr;
    }

    /**
     * 校验NFC功能是否可用（硬件支持+已开启）
     * @return 可用返回true，不可用返回false
     */
    public boolean isNfcAvailable() {
        boolean available = mNfcAdapter != null && mNfcAdapter.isEnabled();
        LogUtils.d(TAG, "NFC当前可用性：" + available);
        return available;
    }

    /**
     * 清空内存中密钥缓存（如退出登录场景使用）
     */
    public void clearCache() {
        mCachePrivateKeyStr = null;
        mCachePublicKeyStr = null;
        LogUtils.d(TAG, "内存密钥缓存已清空");
    }
}

