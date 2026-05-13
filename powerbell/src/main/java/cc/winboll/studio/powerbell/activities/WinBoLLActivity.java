package cc.winboll.studio.powerbell.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.BuildConfig;
import cc.winboll.studio.powerbell.R;

/** 
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/19 20:35
 * @Describe 应用窗口基类，提供主题设置、Activity 管理、工具栏配置、全屏切换、版本标签显示等通用功能
 * 适配 API30，基于 Java7 开发，所有子类需继承此类实现统一窗口行为
 */
public abstract class WinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {
    // ======================== 静态常量 =========================
    public static final String TAG = "WinBoLLActivity";
    private static final String VERSION_TAG_TEXT = "MIMO SDK V%s"; // 版本标签文本格式
    private static final float VERSION_TAG_TEXT_SIZE = 10f; // 版本标签字体大小（sp）

    // ======================== 成员变量 =========================
    protected volatile AESThemeBean.ThemeType mThemeType; // 当前主题类型
    protected TextView mTagView; // 版本标签显示控件

    // ======================== 接口实现 & 抽象方法 =========================
    @Override
    public abstract Activity getActivity();

    @Override
    public abstract String getTag();

    // ======================== 生命周期方法 =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, String.format("【%s-onCreate】窗口基类初始化开始", getTag()));
        // 初始化主题
        mThemeType = getThemeType();
        setThemeStyle();
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, String.format("【%s-onCreate】窗口基类初始化完成，当前主题：%s", getTag(), mThemeType));
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d(TAG, String.format("【%s-onStart】添加版本标签到页面", getTag()));
        // 添加版本标签
        addVersionNameToContentView();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 注册到Activity管理器
        WinBoLLActivityManager.getInstance().add(this);
        LogUtils.d(TAG, String.format("【%s-onPostCreate】已注册到Activity管理器", getTag()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 从Activity管理器移除
        WinBoLLActivityManager.getInstance().registeRemove(this);
        LogUtils.d(TAG, String.format("【%s-onDestroy】已从Activity管理器移除", getTag()));
    }

    // ======================== 主题相关方法 =========================
    /**
     * 获取当前主题类型
     * @return 主题类型枚举
     */
    AESThemeBean.ThemeType getThemeType() {
        int themeId = AESThemeUtil.getThemeTypeID(getApplicationContext());
        AESThemeBean.ThemeType themeType = AESThemeBean.getThemeStyleType(themeId);
        LogUtils.d(TAG, String.format("【%s-getThemeType】获取主题类型，ID：%d，类型：%s", getTag(), themeId, themeType));
        return themeType;
    }

    /**
     * 设置主题样式
     */
    void setThemeStyle() {
        int themeId = AESThemeUtil.getThemeTypeID(getApplicationContext());
        setTheme(themeId);
        LogUtils.d(TAG, String.format("【%s-setThemeStyle】应用主题样式，ID：%d", getTag(), themeId));
    }

    // ======================== UI 配置方法 =========================
    /**
     * 添加版本标签到页面底部
     */
    protected void addVersionNameToContentView() {
        if (!isTagViewVisible()) {
            LogUtils.d(TAG, String.format("【%s-addVersionNameToContentView】版本标签不可见，跳过添加", getTag()));
            return;
        }

        if (mTagView == null) {
            mTagView = new TextView(this);
            // 配置版本标签样式
            mTagView.setTextColor(Color.GRAY);
            mTagView.setTextSize(TypedValue.COMPLEX_UNIT_SP, VERSION_TAG_TEXT_SIZE);
            mTagView.setText(String.format(VERSION_TAG_TEXT, BuildConfig.VERSION_NAME));
            // 配置布局参数
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            // 添加到根布局
            FrameLayout frameLayout = findViewById(android.R.id.content);
            if (frameLayout != null) {
                frameLayout.addView(mTagView, params);
                LogUtils.d(TAG, String.format("【%s-addVersionNameToContentView】版本标签添加完成，版本：%s", getTag(), BuildConfig.VERSION_NAME));
            } else {
                LogUtils.w(TAG, String.format("【%s-addVersionNameToContentView】根布局为空，无法添加版本标签", getTag()));
            }
        }
    }

    /**
     * 配置工具栏，显示返回按钮
     */
    public void setupToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                LogUtils.d(TAG, String.format("【%s-setupToolbar】工具栏配置完成，已显示返回按钮", getTag()));
            } else {
                LogUtils.w(TAG, String.format("【%s-setupToolbar】ActionBar为空，无法显示返回按钮", getTag()));
            }
        } else {
            LogUtils.w(TAG, String.format("【%s-setupToolbar】未找到工具栏控件（ID：toolbar）", getTag()));
        }
    }

    /**
     * 版本标签是否可见
     * @return 默认为true，子类可重写修改
     */
    protected boolean isTagViewVisible() {
        return true;
    }

    // ======================== 菜单 & 返回键处理 =========================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LogUtils.d(TAG, String.format("【%s-onOptionsItemSelected】点击返回菜单", getTag()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogUtils.d(TAG, String.format("【%s-onBackPressed】触发返回键", getTag()));
    }

    // ======================== 工具方法 =========================
    /**
     * 切换至全屏模式，隐藏状态栏与导航栏
     * @param activity 目标Activity
     */
    public void changeFullScreen(Activity activity) {
        if (activity == null) {
            LogUtils.w(TAG, String.format("【%s-changeFullScreen】目标Activity为空，无法切换全屏", getTag()));
            return;
        }

        Window window = activity.getWindow();
        if (window == null) {
            LogUtils.w(TAG, String.format("【%s-changeFullScreen】窗口为空，无法切换全屏", getTag()));
            return;
        }

        View decorView = window.getDecorView();
        if (decorView == null) {
            LogUtils.w(TAG, String.format("【%s-changeFullScreen】DecorView为空，无法切换全屏", getTag()));
            return;
        }

        // 配置全屏标志位
        int flag = decorView.getSystemUiVisibility();
        flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(flag);
        // 配置窗口标志位
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LogUtils.d(TAG, String.format("【%s-changeFullScreen】已切换至全屏模式", getTag()));
    }
}

