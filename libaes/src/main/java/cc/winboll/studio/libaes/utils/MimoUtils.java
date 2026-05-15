package cc.winboll.studio.libaes.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/18 15:23
 * @Describe 米盟 MimoUtils
 */
public final class MimoUtils {
	public static final String TAG = "Utils";

	public static int dpToPx(Context context, float dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (int) (dp * displayMetrics.density + 0.5f);
	}

	public static int pxToDp(Context context, float px) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (int) (px / displayMetrics.density + 0.5f);
	}

	public static int pxToSp(Context context, float pxValue) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (int) (pxValue / displayMetrics.scaledDensity + 0.5f);
	}

	public static int spToPx(Context context, float spValue) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (int) (spValue * displayMetrics.scaledDensity + 0.5f);
	}
}
