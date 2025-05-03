package cc.winboll.studio.aes;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.beans.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;
import android.view.MenuItem;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/30 00:34:02
 * @Describe WinBoLL 活动窗口通用基类
 */
public class WinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {

    public static final String TAG = "WinBoLLActivity";

    protected volatile AESThemeBean.ThemeType mThemeType;

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeType = getThemeType();
        setThemeStyle();
        super.onCreate(savedInstanceState);
    }

    AESThemeBean.ThemeType getThemeType() {
        /*SharedPreferences sharedPreferences = getSharedPreferences(
         SHAREDPREFERENCES_NAME, MODE_PRIVATE);
         return AESThemeBean.ThemeType.values()[((sharedPreferences.getInt(DRAWER_THEME_TYPE, AESThemeBean.ThemeType.DEFAULT.ordinal())))];
         */
        return AESThemeBean.getThemeStyleType(AESThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    void setThemeStyle() {
        //setTheme(AESThemeBean.getThemeStyle(getThemeType()));
        setTheme(AESThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
