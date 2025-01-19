package cc.winboll.studio.libapputils.log;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.app.WinBollActivity;
import cc.winboll.studio.libapputils.ads.ADsView;
import cc.winboll.studio.libapputils.app.WinBollApplication;
import android.view.View;
import android.app.Activity;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 15:07:58
 * @Describe WinBoll 应用日志窗口
 */
public class LogActivity extends Activity {

    public static final String TAG = "LogActivity";

    LogView mLogView;
    //ADsView mADsView;

//    @Override
//    protected boolean isEnableDisplayHomeAsUp() {
//        return false;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mLogView = findViewById(R.id.logview);
        //mADsView = findViewById(R.id.adsview);
        //mADsView.loadUrl("https://www.winboll.cc");
        //mLogView.setVisibility(WinBollApplication.isDebug()?View.GONE:View.VISIBLE);
        //mADsView.setVisibility(WinBollApplication.isDebug()?View.GONE:View.VISIBLE);
        
        if(WinBollApplication.isDebug()) { mLogView.start(); }
    }

    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume");
        super.onResume();
        mLogView.start();
    }

//    @Override
//    protected boolean isAddWinBollToolBar() {
//        return false;
//    }
//
//    @Override
//    protected Toolbar initToolBar() {
//        LogUtils.d(TAG, "initToolBar");
//        return null;
//    }
//
//    @Override
//    public String getTag() {
//        LogUtils.d(TAG, "getTag");
//        return TAG;
//    }
}
