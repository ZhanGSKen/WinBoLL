package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/31 01:31:17
 * @Describe 应用活动窗口基类
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.beans.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;

public class WinBollActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "WinBollActivity";

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
