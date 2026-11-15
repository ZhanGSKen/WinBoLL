package cc.winboll.studio.libaes.utils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/11/29 22:52:09
 * @Describe AES 主题工具集
 */
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import java.util.ArrayList;

public class AESThemeUtil {

    public static final String TAG = "AESThemeUtil";
    private static final String SHAREDPREFERENCES_NAME = "SHAREDPREFERENCES_NAME";
    private static final String DRAWER_THEME_TYPE = "DRAWER_THEME_TYPE";

    // 私有静态集合，外部不可直接修改
    private static ArrayList<Integer> themeStyleIDList = new ArrayList<>();

    // 移除无用实例成员 mThemeType，工具类不保留实例字段

    /**
     * 初始化主题样式ID集合
     */
    public static void init(ArrayList<Integer> themeStyleIDList) {

		if (themeStyleIDList == null) {
			themeStyleIDList = new ArrayList<Integer>();
			AESThemeBean.fillThemeStyleIDList(themeStyleIDList);
		}
		AESThemeUtil.themeStyleIDList.clear();
		AESThemeUtil.themeStyleIDList.addAll(themeStyleIDList);
    }

    /**
     * 获取当前主题样式ID
     */
    public static int getThemeTypeID(Context context) {
		AESThemeBean bean = AESThemeBean.loadBean(context, AESThemeBean.class);
        return bean == null ? getThemeStyleID(AESThemeBean.ThemeType.AES) : bean.getCurrentThemeTypeID();
    }

    /**
     * 保存主题样式ID
     */
    public static void saveThemeStyleID(Context context, int themeTypeID) {
        AESThemeBean bean = new AESThemeBean(themeTypeID);
        AESThemeBean.saveBean(context, bean);
    }

    // ====================== 应用主题 - 规范重载 ======================
    /**
     * 应用当前持久化主题（通用 Activity）
     */
    public static void applyTheme(Activity activity) {
        activity.setTheme(getThemeTypeID(activity));
    }

    /**
     * 应用指定主题（通用 Activity）
     */
    public static void applyTheme(Activity activity, AESThemeBean.ThemeType themeType) {
        activity.setTheme(getThemeStyleID(themeType));
    }

    /**
     * 应用当前持久化主题（AppCompat 兼容 Activity）
     */
    public static void applyAppCompatTheme(AppCompatActivity activity) {
        activity.setTheme(getThemeTypeID(activity));
    }

    /**
     * 应用指定主题（AppCompat 兼容 Activity）
     */
    public static void applyAppCompatTheme(AppCompatActivity activity, AESThemeBean.ThemeType themeType) {
        activity.setTheme(getThemeStyleID(themeType));
    }

    // ====================== 加载菜单 ======================
    /**
     * 加载主题菜单（通用 Activity）
     */
    public static void inflateThemeMenu(Activity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.toolbar_apptheme, menu);
    }

    /**
     * 加载主题菜单（AppCompat Activity）
     */
    public static void inflateCompatThemeMenu(AppCompatActivity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.toolbar_apptheme, menu);
    }

    // ====================== 菜单点击统一核心逻辑（消除重复代码） ======================
    /**
     * 主题菜单项点击统一处理
     * @param context 上下文（用于持久化）
     * @param item 点击的菜单项
     * @return 是否消费点击事件
     */
    public static boolean handleThemeMenuClick(Context context, MenuItem item) {
        int themeStyleId;
        int itemId = item.getItemId();
        if (R.id.item_depththeme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.DEPTH);
        } else if (R.id.item_skytheme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.SKY);
        } else if (R.id.item_goldentheme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.GOLDEN);
        } else if (R.id.item_bearingtheme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.BEARING);
        } else if (R.id.item_memortheme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.MEMOR);
        } else if (R.id.item_taotheme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.TAO);
        } else if (R.id.item_defaulttheme == itemId) {
            themeStyleId = getThemeStyleID(AESThemeBean.ThemeType.AES);
        } else {
            return false;
        }
        saveThemeStyleID(context, themeStyleId);
        return true;
    }

    // 对外暴露不同 Activity 类型的入口，内部调用统一核心方法
    public static boolean onThemeItemSelected(Activity activity, MenuItem item) {
        return handleThemeMenuClick(activity, item);
    }

    public static boolean onAppCompatThemeItemSelected(AppCompatActivity activity, MenuItem item) {
        return handleThemeMenuClick(activity, item);
    }

    public static boolean onWinBoLLThemeItemSelected(AppCompatActivity activity, MenuItem item) {
        // 使用 Application 上下文保存，避免 Activity 泄漏
        return handleThemeMenuClick(activity.getApplicationContext(), item);
    }

    public static boolean onWinBoLLThemeItemSelected(DrawerFragmentActivity activity, MenuItem item) {
        return handleThemeMenuClick(activity.getApplicationContext(), item);
    }

    // ====================== 主题类型转换工具 ======================
    /**
     * 根据枚举获取对应样式ID
     */
    public static int getThemeStyleID(AESThemeBean.ThemeType themeType) {
        return themeStyleIDList.get(themeType.ordinal());
    }

    /**
     * 根据样式ID反向获取主题枚举
     */
    public static AESThemeBean.ThemeType getThemeStyleType(int themeStyleID) {
        for (int i = 0; i < themeStyleIDList.size(); i++) {
            if (themeStyleIDList.get(i) == themeStyleID) {
                return AESThemeBean.ThemeType.values()[i];
            }
        }
        return AESThemeBean.ThemeType.values()[0];
    }
}

