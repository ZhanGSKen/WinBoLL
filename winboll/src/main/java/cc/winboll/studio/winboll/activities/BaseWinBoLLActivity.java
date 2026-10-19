package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.winboll.theme.WinBoLLThemeUtil;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/13 16:33
 * BaseWinBollActivity 【继承AppCompatActivity，保留核心能力，不额外暴露方法】
 * 继承链路：BaseWinBoLLActivity → AppCompatActivity → FragmentActivity，AppCompat能力天然继承可用
 */
public abstract class BaseWinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {
    public static final String TAG = "BaseWinBoLLActivity";

    protected volatile AESThemeBean.ThemeType mThemeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeType = getThemeType();
        setThemeStyle();
        super.onCreate(savedInstanceState);
        WinBoLLActivityManager.getInstance().add(this);
        //ToastUtils.show(getTag() + ": onCreate");
    }

    AESThemeBean.ThemeType getThemeType() {
        return WinBoLLThemeUtil.getThemeStyleType(WinBoLLThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    void setThemeStyle() {
        setTheme(WinBoLLThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    @Override
    protected void onDestroy() {
        WinBoLLActivityManager.getInstance().registeRemove(this);
        super.onDestroy();
    }

    // 子类必须实现getTag()，确保唯一标识
    @Override
    public abstract String getTag();

    public abstract Activity  getActivity();
}

