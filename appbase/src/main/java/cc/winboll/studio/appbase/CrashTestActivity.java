package cc.winboll.studio.appbase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;

public class CrashTestActivity extends Activity {

    public static final String TAG = "CrashTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_test);
        LogUtils.d(TAG, "CrashTestActivity onCreate()");
    }

    public void onBack(View view) {
        finish();
    }

    public void onTestCrash(View view) {
        LogUtils.d(TAG, "onTestCrash()");
        ToastUtils.show("测试布局崩溃...");
    }
}
