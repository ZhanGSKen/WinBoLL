package cc.winboll.studio.winboll.theme;

import android.content.Context;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;

public class WinBoLLThemeUtil {

    public static final String TAG = "WinBoLLThemeUtil";

    public static int getThemeTypeID(Context context) {
        AESThemeBean bean = AESThemeBean.loadBean(context, AESThemeBean.class);
        int themeTypeID;
        if (bean == null) {
            themeTypeID = WinBoLLThemeBean.getDefaultThemeStyleID();
        } else {
            int aesStyleID = bean.getCurrentThemeTypeID();
            AESThemeBean.ThemeType themeType = WinBoLLThemeBean.getThemeStyleType(aesStyleID);
            themeTypeID = WinBoLLThemeBean.getThemeStyleID(themeType);
        }
        return themeTypeID;
    }

    public static void saveThemeStyleID(Context context, int nThemeTypeID) {
        AESThemeBean bean = new AESThemeBean(nThemeTypeID);
        AESThemeBean.saveBean(context, bean);
    }

    public static AESThemeBean.ThemeType getThemeStyleType(int nThemeStyleID) {
        return WinBoLLThemeBean.getThemeStyleType(nThemeStyleID);
    }

    public static int getThemeStyleID(AESThemeBean.ThemeType themeType) {
        return WinBoLLThemeBean.getThemeStyleID(themeType);
    }
}
