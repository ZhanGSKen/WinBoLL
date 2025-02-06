package cc.winboll.studio.libaes.utils;

/**
 * @Author ZhanGSKen@QQ.COM
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
import cc.winboll.studio.libaes.beans.AESThemeBean;

public class AESThemeUtil {

    public static final String TAG = "AESThemeUtil";

    static final String SHAREDPREFERENCES_NAME = "SHAREDPREFERENCES_NAME";
    static final String DRAWER_THEME_TYPE = "DRAWER_THEME_TYPE";

    protected volatile AESThemeBean.ThemeType mThemeType;

    public static <T extends Context> int getThemeTypeID(T context) {
        AESThemeBean bean = AESThemeBean.loadBean(context, AESThemeBean.class);
        return bean == null ? AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.AES): bean.getCurrentThemeTypeID();
    }
    
    public static <T extends Context> void saveThemeStyleID(T context, int nThemeTypeID) {
        AESThemeBean bean = new AESThemeBean(nThemeTypeID);
        AESThemeBean.saveBean(context, bean);
    }

    public static <T extends Activity> void applyAppTheme(T activity) {
        activity.setTheme(getThemeTypeID(activity));
    }

    public static <T extends AppCompatActivity> void applyAppCompatTheme(T activity) {
        activity.setTheme(getThemeTypeID(activity));
    }

    /*public static <T extends WinBollActivity> void applyWinBollTheme(T activity) {
        activity.setTheme(getThemeTypeID(activity.getApplicationContext()));
    }*/

    public static <T extends Activity> void applyAppTheme(Activity activity, AESThemeBean.ThemeType themeType) {
        activity.setTheme(AESThemeBean.getThemeStyleID(themeType));
    }

    public static <T extends AppCompatActivity> void applyAppCompatTheme(Activity activity, AESThemeBean.ThemeType themeType) {
        activity.setTheme(AESThemeBean.getThemeStyleID(themeType));
    }

    /*public static <T extends WinBollActivity> void applyWinBollTheme(Activity activity, AESThemeBean.ThemeType themeType) {
        activity.setTheme(AESThemeBean.getThemeStyleID(themeType));
    }*/

    public static <T extends Activity> void inflateMenu(T activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.toolbar_apptheme, menu);
    }

    public static <T extends AppCompatActivity> void inflateCompatMenu(T activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.toolbar_apptheme, menu);
    }

    /*public static <T extends WinBollActivity> void inflateWinBollMenu(T activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.toolbar_apptheme, menu);
    }*/

    public static <T extends Activity> boolean onAppThemeItemSelected(T activity, MenuItem item) {
        int nThemeStyleID;
        if (R.id.item_depththeme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEPTH);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_skytheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.SKY);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_goldentheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.GOLDEN);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_memortheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.MEMOR);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_taotheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.TAO);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_defaulttheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.AES);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        }

        return false;
    }

    public static <T extends AppCompatActivity> boolean onAppCompatThemeItemSelected(T activity, MenuItem item) {
        int nThemeStyleID;
        if (R.id.item_depththeme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEPTH);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_skytheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.SKY);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_goldentheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.GOLDEN);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_memortheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.MEMOR);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_taotheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.TAO);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        } else if (R.id.item_defaulttheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.AES);
            saveThemeStyleID(activity, nThemeStyleID);
            return true;
        }

        return false;
    }

    public static <T extends AppCompatActivity> boolean onWinBollThemeItemSelected(T activity, MenuItem item) {
        int nThemeStyleID;
        if (R.id.item_depththeme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEPTH);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_skytheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.SKY);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_goldentheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.GOLDEN);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_memortheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.MEMOR);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_taotheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.TAO);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_defaulttheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.AES);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        }

        return false;
    }
    
    public static <T extends DrawerFragmentActivity> boolean onWinBollThemeItemSelected(T activity, MenuItem item) {
        int nThemeStyleID;
        if (R.id.item_depththeme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEPTH);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_skytheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.SKY);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_goldentheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.GOLDEN);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_memortheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.MEMOR);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_taotheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.TAO);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        } else if (R.id.item_defaulttheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.AES);
            saveThemeStyleID(activity.getApplicationContext(), nThemeStyleID);
            return true;
        }

        return false;
    }
}
