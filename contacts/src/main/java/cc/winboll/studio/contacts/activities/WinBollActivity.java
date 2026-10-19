package cc.winboll.studio.contacts.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/31 15:16:45
 * @Describe 应用窗口基类，统一处理主题设置与导航返回
 */
public class WinBollActivity extends AppCompatActivity implements IWinBoLLActivity {

    // ====================== 常量定义区 ======================
    public static final String TAG = "WinBollActivity";

    // ====================== 成员变量区 ======================
    protected volatile AESThemeBean.ThemeType mThemeType;

    // ====================== 接口实现区 ======================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ====================== 生命周期函数区 ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //LogUtils.d(TAG, "onCreate: 基类页面开始创建");
        // 优先设置主题，再执行父类初始化
//        mThemeType = getThemeType();
//        setThemeStyle();
        super.onCreate(savedInstanceState);
        //LogUtils.d(TAG, "onCreate: 基类主题设置完成，当前主题类型=" + mThemeType);
    }

    // ====================== 主题相关函数区 ======================
    /**
     * 获取当前应用主题类型
     */
    AESThemeBean.ThemeType getThemeType() {
        LogUtils.d(TAG, "getThemeType: 获取应用主题类型");
        // 注释的SharedPreferences逻辑保留，便于后续扩展
        /*SharedPreferences sharedPreferences = getSharedPreferences(
         SHAREDPREFERENCES_NAME, MODE_PRIVATE);
         return AESThemeBean.ThemeType.values()[((sharedPreferences.getInt(DRAWER_THEME_TYPE, AESThemeBean.ThemeType.DEFAULT.ordinal())))];
         */
        return AESThemeBean.getThemeStyleType(AESThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    /**
     * 应用当前主题样式
     */
    void setThemeStyle() {
        LogUtils.d(TAG, "setThemeStyle: 开始设置应用主题");
        // 替换原注释逻辑，使用AESThemeUtil获取的主题ID
        setTheme(AESThemeUtil.getThemeTypeID(getApplicationContext()));
        LogUtils.d(TAG, "setThemeStyle: 主题设置完成");
    }

    // ====================== 菜单与导航函数区 ======================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.d(TAG, "onOptionsItemSelected: 菜单选项点击，itemId=" + item.getItemId());
        // 处理导航栏返回按钮点击事件
//        if (item.getItemId() == android.R.id.home) {
//            LogUtils.d(TAG, "onOptionsItemSelected: 点击导航返回按钮，关闭当前页面");
//            finish();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}

