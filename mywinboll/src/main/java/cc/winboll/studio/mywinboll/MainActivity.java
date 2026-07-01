package cc.winboll.studio.mywinboll;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.DevelopUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLogView = findViewById(R.id.logview);

        ToastUtils.show("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AESThemeUtil.inflateCompatThemeMenu(this, menu);
        if (GlobalApplication.isDebugging()) {
            DevelopUtils.inflateMenu(this, menu);
        }
        getMenuInflater().inflate(cc.winboll.studio.libaes.R.menu.toolbar_drawerbase, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AESThemeUtil.onWinBoLLThemeItemSelected(this, item)) {
            recreate();
        }
        if (DevelopUtils.onDevelopItemSelected(this, item)) {
            LogUtils.d(TAG, String.format("onOptionsItemSelected item.getItemId() %d ", item.getItemId()));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
