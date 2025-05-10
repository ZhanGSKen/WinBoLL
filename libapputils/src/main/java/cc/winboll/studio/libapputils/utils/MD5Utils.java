package cc.winboll.studio.libapputils.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/19 15:26:58
 */
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    public static String encrypt(String input) {
        try {
            // 获取MD5实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 对输入的字符串进行摘要计算，得到字节数组
            byte[] messageDigest = md.digest(input.getBytes());
            // 将字节数组转换为十六进制的字符串表示形式
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String str = "要加密的字符串";
        String encryptedStr = encrypt(str);
        System.out.println(encryptedStr);
    }
}

