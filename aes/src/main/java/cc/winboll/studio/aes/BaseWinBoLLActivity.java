package cc.winboll.studio.aes;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/13 16:35
 * @Describe BaseWinBollActivity 【继承AppCompatActivity，保留核心能力，不额外暴露方法】
 * 继承链路：BaseWinBoLLActivity → AppCompatActivity → FragmentActivity，AppCompat能力天然继承可用
 */
public abstract class BaseWinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {
    public static final String TAG = "BaseWinBoLLActivity";

    protected volatile AESThemeBean.ThemeType mThemeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeType = AESThemeBean.getThemeStyleType(AESThemeUtil.getThemeTypeID(getApplicationContext()));
        setTheme(AESThemeUtil.getThemeTypeID(getApplicationContext()));
        super.onCreate(savedInstanceState);
        WinBoLLActivityManager.getInstance().add(this);
    }

    @Override
    protected void onDestroy() {
        WinBoLLActivityManager.getInstance().registeRemove(this);
        super.onDestroy();
    }

    // 子类必须实现getTag()，确保唯一标识
    @Override
    public abstract String getTag();

    @Override
    public Activity getActivity() {
        return this;
    }
}

