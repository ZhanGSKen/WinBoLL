package cc.winboll.studio.winboll.theme;

import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.winboll.R;

public class WinBoLLThemeBean {

    public static final String TAG = "WinBoLLThemeBean";

    public static int getDefaultThemeStyleID() {
        return R.style.MyAppTheme;
    }

    public static int getThemeStyleID(AESThemeBean.ThemeType themeType) {
        int themeStyleID = getDefaultThemeStyleID();
        if (AESThemeBean.ThemeType.DEPTH == themeType) {
            themeStyleID = R.style.MyDepthAppTheme;
        } else if (AESThemeBean.ThemeType.SKY == themeType) {
            themeStyleID = R.style.MySkyAppTheme;
        } else if (AESThemeBean.ThemeType.GOLDEN == themeType) {
            themeStyleID = R.style.MyGoldenAppTheme;
        } else if (AESThemeBean.ThemeType.BEARING == themeType) {
            themeStyleID = R.style.MyBearingAppTheme;
        } else if (AESThemeBean.ThemeType.MEMOR == themeType) {
            themeStyleID = R.style.MyMemorAppTheme;
        } else if (AESThemeBean.ThemeType.TAO == themeType) {
            themeStyleID = R.style.MyTaoAppTheme;
        }
        return themeStyleID;
    }

    public static AESThemeBean.ThemeType getThemeStyleType(int nThemeStyleID) {
        AESThemeBean.ThemeType themeStyle = AESThemeBean.ThemeType.AES;
        if (R.style.MyDepthAppTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.DEPTH;
        } else if (R.style.MySkyAppTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.SKY;
        } else if (R.style.MyGoldenAppTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.GOLDEN;
        } else if (R.style.MyBearingAppTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.BEARING;
        } else if (R.style.MyMemorAppTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.MEMOR;
        } else if (R.style.MyTaoAppTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.TAO;
        }
        return themeStyle;
    }
}
