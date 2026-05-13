package cc.winboll.studio.autonfc;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.autonfc.nfc.ActionDialog;
import cc.winboll.studio.autonfc.nfc.AutoNFCService;
import cc.winboll.studio.autonfc.nfc.NFCInterfaceActivity;
import cc.winboll.studio.libappbase.LogActivity;
import cc.winboll.studio.libappbase.LogUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private AutoNFCService mService;
    private boolean mBound = false;

    // 服务连接
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AutoNFCService.LocalBinder binder = (AutoNFCService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            LogUtils.d(TAG, "onServiceConnected: 服务已绑定");

            // 关键：把 Activity 传给 Service，用于回调
            mService.attachActivity(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mService = null;
            LogUtils.d(TAG, "onServiceDisconnected: 服务已断开");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 初始化 NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);

        LogUtils.d(TAG, "onCreate() -> NFC 监听已绑定到 MainActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 绑定服务
        Intent intent = new Intent(this, AutoNFCService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        LogUtils.d(TAG, "onStart: 绑定服务");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 解绑服务
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            LogUtils.d(TAG, "onStop: 解绑服务");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume() -> 开启 NFC 前台分发");
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause() -> 关闭 NFC 前台分发");
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    // NFC 卡片靠近唯一入口
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.d(TAG, "onNewIntent() -> 检测到 NFC 卡片");

        // 把 NFC 事件交给 Service 处理
        if (mBound && mService != null) {
            mService.handleNfcIntent(intent);
        } else {
            LogUtils.e(TAG, "服务未绑定，无法处理 NFC");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_log) {
            LogActivity.startLogActivity(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onNFCInterfaceActivity(View view) {
        startActivity(new Intent(this, NFCInterfaceActivity.class));
    }

    // ========================= 【新增】关键方法：由 Service 回调来弹出对话框 =========================
    /**
     * Service 解析完 NFC 数据后，回调此方法在 Activity 中弹出对话框
     */
    public void showNfcActionDialog(final String nfcData) {
        LogUtils.d(TAG, "showNfcActionDialog() ->  Activity 存活，安全弹出对话框");

        // Activity 正在运行，直接弹框，绝对不会报 BadTokenException
        final ActionDialog dialog = new ActionDialog(this);
        dialog.setNfcData(nfcData);
        dialog.setButtonClickListener(new ActionDialog.OnButtonClickListener() {
				@Override
				public void onBuildClick() {
					LogUtils.d(TAG, "点击 Build");
					if (mService != null) {
						mService.executeTermuxCommand(AutoNFCService.ACTION_BUILD, nfcData);
					}
					dialog.dismiss();
				}

				@Override
				public void onViewClick() {
					LogUtils.d(TAG, "点击 View");
					if (mService != null) {
						mService.executeTermuxCommand(AutoNFCService.ACTION_BUILD_VIEW, nfcData);
					}
					dialog.dismiss();
				}

				@Override
				public void onCancelClick() {
					dialog.dismiss();
				}
			});

        dialog.show();
    }
}

