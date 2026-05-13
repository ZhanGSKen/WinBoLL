package cc.winboll.studio.mymessagemanager.enums;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.StyleRes;
import cc.winboll.studio.mymessagemanager.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/09 14:15
 * @Describe 主题风格枚举类 - 含SharedPreferences存取方法（主题持久化）
 */
public enum ThemeStyleEnum {
	// 主题枚举项（与原逻辑完全对应）
	DEPTH_THEME(R.id.item_depththeme, R.style.MyDepthAESTheme),
	SKY_THEME(R.id.item_skytheme, R.style.MySkyAESTheme),
	GOLDEN_THEME(R.id.item_goldentheme, R.style.MyGoldenAESTheme),
	MEMOR_THEME(R.id.item_memortheme, R.style.MyMemorAESTheme),
	TAO_THEME(R.id.item_taotheme, R.style.MyTaoAESTheme),
	DEFAULT_THEME(R.id.item_defaulttheme, R.style.MyAppTheme);

	// ---------------------- 基础字段（原逻辑保留） ----------------------
	private final int menuId;
	@StyleRes
	private final int styleId;

	ThemeStyleEnum(int menuId, @StyleRes int styleId) {
		this.menuId = menuId;
		this.styleId = styleId;
	}

	public int getMenuId() {
		return menuId;
	}

	@StyleRes
	public int getStyleId() {
		return styleId;
	}

	// ---------------------- SharedPreferences 配置（新增） ----------------------
	// SP文件名：主题配置（建议与枚举类关联，便于查找）
	private static final String SP_THEME_NAME = "sp_theme_config";
	// SP存储键：当前选中的主题Style ID
	private static final String KEY_CURRENT_THEME_STYLE_ID = "current_theme_style_id";

	// ---------------------- 核心方法：保存主题到SP（新增） ----------------------
	/**
	 * 保存当前选中的主题Style ID到SharedPreferences
	 * @param context 上下文（Activity/Application均可）
	 * @param theme 要保存的主题枚举项（如ThemeStyleEnum.DEFAULT_THEME）
	 */
	public static void saveThemeToSP(Context context, ThemeStyleEnum theme) {
		if (context == null || theme == null) return;
		// 获取SP实例（私有模式，仅当前App可访问）
		SharedPreferences sp = context.getSharedPreferences(SP_THEME_NAME, Context.MODE_PRIVATE);
		// 存入主题对应的Style ID
		sp.edit().putInt(KEY_CURRENT_THEME_STYLE_ID, theme.getStyleId()).apply();
	}

	// ---------------------- 核心方法：从SP读取主题（新增） ----------------------
	/**
	 * 从SharedPreferences读取保存的主题，无存储时返回默认主题
	 * @param context 上下文
	 * @return 保存的主题枚举项（默认返回DEFAULT_THEME）
	 */
	public static ThemeStyleEnum getThemeFromSP(Context context) {
		if (context == null) return DEFAULT_THEME;
		// 读取SP中保存的Style ID
		SharedPreferences sp = context.getSharedPreferences(SP_THEME_NAME, Context.MODE_PRIVATE);
		int savedStyleId = sp.getInt(KEY_CURRENT_THEME_STYLE_ID, DEFAULT_THEME.getStyleId());

		// 根据保存的Style ID匹配对应的枚举项
		for (ThemeStyleEnum theme : values()) {
			if (theme.getStyleId() == savedStyleId) {
				return theme;
			}
		}
		// 无匹配时返回默认主题（防止异常）
		return DEFAULT_THEME;
	}

	// ---------------------- 辅助方法：根据菜单ID获取主题（原逻辑保留并优化） ----------------------
	public static ThemeStyleEnum getThemeByMenuId(int menuId) {
		for (ThemeStyleEnum theme : values()) {
			if (theme.getMenuId() == menuId) {
				return theme;
			}
		}
		return null;
	}
}

