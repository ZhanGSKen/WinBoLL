package cc.winboll.studio.libappbase.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import cc.winboll.studio.libappbase.LogUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/20 19:50
 * @Describe 获取应用签名指纹（SHA1+Base64，直接复制用）
 */
public class SignGetUtils {
    private static final String TAG = "SignGetUtils";

    /**
     * 一键获取当前应用签名指纹（直接调用，看日志复制结果）
     */
    public static void getCurrentAppSign(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "context不能为空");
            return;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = pkgInfo.signatures;
            if (signatures == null || signatures.length == 0) {
                LogUtils.e(TAG, "未获取到应用签名");
                return;
            }
            // 和APPUtils校验格式完全一致（SHA1+Base64 NO_WRAP）
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(signatures[0].toByteArray());
            String signBase64 = Base64.encodeToString(md.digest(), Base64.NO_WRAP);

            // 关键日志：复制【】里的内容到APPUtils的TARGET_SIGN_FINGERPRINT
            LogUtils.d(TAG, "当前应用包名：" + context.getPackageName());
            LogUtils.d(TAG, "当前应用签名指纹（直接复制）：【" + signBase64 + "】");
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "获取签名失败：包名不存在", e);
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, "获取签名失败：不支持SHA1", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "获取签名失败", e);
        }
    }
	
	// 新增：直接返回签名字符串，供对话框调用
//	public static String getSignStr(Context context) {
//		if (context == null) return null;
//		try {
//			PackageManager pm = context.getPackageManager();
//			PackageInfo pkgInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
//			Signature[] signatures = pkgInfo.signatures;
//			if (signatures == null || signatures.length == 0) return null;
//
//			MessageDigest md = MessageDigest.getInstance("SHA1");
//			md.update(signatures[0].toByteArray());
//			return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
//		} catch (Exception e) {
//			LogUtils.e(TAG, "获取签名字符串失败", e);
//			return null;
//		}
//	}
}

