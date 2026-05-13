package cc.winboll.studio.mymessagemanager.activitys;

import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.mymessagemanager.R;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/02/11 03:45
 * @Describe TTS悬浮窗设置类（使用可拖动自定义控件）
 */
public class TTSFloatSettingsActivity extends Activity {

    public static final String TAG = "TTSFloatSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 直接加载包含自定义拖动控件的布局
        setContentView(R.layout.activity_ttsfloatsettings);
    }
}

