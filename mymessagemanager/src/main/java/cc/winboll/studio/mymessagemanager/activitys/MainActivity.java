package cc.winboll.studio.mymessagemanager.activitys;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.mymessagemanager.BuildConfig;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.MainActivity;
import cc.winboll.studio.mymessagemanager.adapters.PhoneArrayAdapter;
import cc.winboll.studio.mymessagemanager.services.MainService;
import cc.winboll.studio.mymessagemanager.unittest.UnitTestActivity;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.AppGoToSettingsUtil;
import cc.winboll.studio.mymessagemanager.utils.NotificationUtil;
import cc.winboll.studio.mymessagemanager.utils.PermissionUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.ViewUtil;
import cc.winboll.studio.mymessagemanager.views.ConfirmSwitchView;
import cc.winboll.studio.mymessagemanager.views.PhoneListViewForScrollView;
import com.baoyz.widget.PullRefreshLayout;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    public final static String TAG = "MainActivity";

    public static final int ACTIVITY_RESULT_APP_SETTINGS = -1;

    public final static int MSG_RELOADSMS = 0;

    public static final int PERMISSION_SETTING_FOR_RESULT = 0;
    public static final int MY_PERMISSIONS_REQUEST = 0;

    static MainActivity _mMainActivity;
    LogView mLogView;
    AppConfigUtil mAppConfigUtil;
    ConfirmSwitchView msvEnableService;
    ConfirmSwitchView msvOnlyReceiveContacts;
    ConfirmSwitchView msvEnableTTS;
    ConfirmSwitchView msvEnableTTSRuleMode;
    PhoneListViewForScrollView mListViewPhone;
    Toolbar mToolbar;
    PhoneArrayAdapter mPhoneArrayAdapter;
	AppGoToSettingsUtil mAppGoToSettingsUtil;
    String[] mPermissionList = {Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, 
        Manifest.permission.READ_SMS};
    ArrayList<String> listPerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _mMainActivity = MainActivity.this;

        mAppConfigUtil = AppConfigUtil.getInstance(this);
		initView();

        // 调用调试检查函数
        onOnceAndroidStory(null);
    }

    //
    // 这是一个测试函数，
    // 用于调试读取 string.xml string-array使用。
    //
    public void onOnceAndroidStory(View view) {
        if (BuildConfig.DEBUG) {
            // 获取strings.xml文件中的tab_names数组
            String[] tab_names = getResources().getStringArray(R.array.strings_OnceAndroidStory);
            // 这里R.array.tab_names是你在XML文件中定义的数组资源ID
            // 例如，在strings.xml中可能这样定义：
            /*/            <!-- strings.xml -->
             <resources>
             <string-array name="tab_names">
             <item>Tab 1</item>
             <item>Tab 2</item>
             <item>Tab 3</item>
             </string-array>
             </resources>
             */
            // 现在你可以遍历这个数组来访问每个元素
            for (int i = 0; i < tab_names.length; i++) {
                // 创建Random实例并传入任意非负种子（这里是1）
                java.util.Random r = new java.util.Random(1);
                // 调用nextInt(6)，范围是0到5（包括0和5），加1后得到1到5
                int randomNum = r.nextInt(6) + 1;
                System.out.println("Random number between 1 and 5: " + randomNum);
                LogUtils.d("OnceAndroidStory", tab_names[i]);
            }
        }
    }

    void scrollScrollView() {
        ScrollView sv = findViewById(R.id.activitymainScrollView1);
        ViewUtil.scrollScrollView(sv);
    }

    void genTestData() {
        for (int i = 0; i < 2; i++) {
            SMSUtil.saveReceiveSms(this, "13172887736", "调试阶段生成的短信" + Integer.toString(i), "0", -1, "inbox");
        }
    }

    //
    // 初始化视图控件
    //
    void initView() {
        // 设置调试日志
        mLogView = findViewById(R.id.logview);
        mLogView.start();

        // 设置消息处理函数
        setOnActivityMessageReceived(mIOnActivityMessageReceived);

        // 设置标题栏
        mToolbar = findViewById(R.id.activitymainASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_main));
        setSupportActionBar(mToolbar);

        // 创建通知频道
        NotificationUtil nu = new NotificationUtil();
        nu.createServiceNotificationChannel(MainActivity.this);
        nu.createSMSNotificationChannel(MainActivity.this);

        boolean isEnableService = mAppConfigUtil.mAppConfigBean.isEnableService();
        msvEnableService = findViewById(R.id.activitymainSwitchView1);
        msvEnableService.setChecked(isEnableService);
        msvEnableService.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isEnable = ((ConfirmSwitchView)v).isChecked();
                    mAppConfigUtil.reLoadConfig();
					mAppConfigUtil.mAppConfigBean.setIsEnableService(isEnable);
                    mAppConfigUtil.saveConfig();
					initService(isEnable);
				}
			});

        boolean isOnlyReceiveContacts = mAppConfigUtil.mAppConfigBean.isEnableOnlyReceiveContacts();
        msvOnlyReceiveContacts = findViewById(R.id.activitymainSwitchView2);
        msvOnlyReceiveContacts.setChecked(isOnlyReceiveContacts);
		msvOnlyReceiveContacts.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isEnable = ((ConfirmSwitchView)v).isChecked();
                    mAppConfigUtil.reLoadConfig();
					mAppConfigUtil.mAppConfigBean.setIsEnableOnlyReceiveContacts(isEnable);
                    mAppConfigUtil.saveConfig();
				}
			});

        boolean isEnableTTS = mAppConfigUtil.mAppConfigBean.isEnableTTS();
        msvEnableTTS = findViewById(R.id.activitymainSwitchView3);
        msvEnableTTS.setChecked(isEnableTTS);
		msvEnableTTS.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isEnable = ((ConfirmSwitchView)v).isChecked();
                    mAppConfigUtil.reLoadConfig();
					mAppConfigUtil.mAppConfigBean.setIsEnableTTS(isEnable);
                    mAppConfigUtil.saveConfig();
				}
			});

        boolean isEnableTTSRuleMode = mAppConfigUtil.mAppConfigBean.isEnableTTSRuleMode();
        msvEnableTTSRuleMode = findViewById(R.id.activitymainSwitchView4);
        msvEnableTTSRuleMode.setChecked(isEnableTTSRuleMode);
		msvEnableTTSRuleMode.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isEnable = ((ConfirmSwitchView)v).isChecked();
                    mAppConfigUtil.reLoadConfig();
					mAppConfigUtil.mAppConfigBean.setIsEnableTTSRuleMode(isEnable);
                    mAppConfigUtil.saveConfig();
				}
			});

        initService(isEnableService);

		// 短信发送窗口按钮
		Button btnSendSMS = findViewById(R.id.activitymainButton1);
		btnSendSMS.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Uri uri = Uri.parse("smsto:");
					Intent it = new Intent(Intent.ACTION_SENDTO, uri);
					it.putExtra("sms_body", "");
					startActivity(it);
				}
			});

		mListViewPhone = (PhoneListViewForScrollView) findViewById(R.id.activitymainListView1);
        //准备数据
        mPhoneArrayAdapter = new PhoneArrayAdapter(MainActivity.this);

        final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.activitymainPullRefreshLayout1);
		//将适配器加载到控件中
        mListViewPhone.setAdapter(mPhoneArrayAdapter);

        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // start refresh
                    reloadSMS();
                    layout.setRefreshing(false);
                }
            });
    }

    void initService(boolean isEnableService) {
        if (isEnableService) {
            Intent service = new Intent(this, MainService.class);
            startService(service);
        } else {
            Intent service = new Intent(this, MainService.class);
            stopService(service);
        }
    }

    //
    // 定义应用内消息处理函数
    //
    IOnActivityMessageReceived mIOnActivityMessageReceived = new IOnActivityMessageReceived(){

        @Override
        public void onActivityMessageReceived(Message msg) {
            switch (msg.arg1) {
                case MSG_RELOADSMS : {
                        LogUtils.d(TAG, "MSG_RELOADSMS");
                        if (PermissionUtil.checkAppPermission(MainActivity.this)) {
                            mPhoneArrayAdapter.loadData();
                            mPhoneArrayAdapter.notifyDataSetChanged();
                        } else {
                            LogUtils.i(TAG, "遇到应用权限问题，请打开应用设置检查一下应用权限。");
                        }
                        break;
                    }
            }
        }
    };

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadSMS();
        mLogView.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_main, menu);

        /*ThemeUtil.BaseTheme baseTheme = ThemeUtil.getTheme(mAppConfigUtil.mAppConfigBean.getAppThemeID());
        if (baseTheme == ThemeUtil.BaseTheme.DEFAULT) {
            menu.findItem(R.id.app_defaulttheme).setChecked(true);
        } else if (baseTheme == ThemeUtil.BaseTheme.SKY) {
            menu.findItem(R.id.app_skytheme).setChecked(true);
        } else if (baseTheme == ThemeUtil.BaseTheme.GOLDEN) {
            menu.findItem(R.id.app_goldentheme).setChecked(true);
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    public static void reloadSMS() {
        if (_mMainActivity != null) {
            Message msg = new Message();
            msg.arg1 = MSG_RELOADSMS;
            _mMainActivity.sendActivityMessage(msg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int nItemId = item.getItemId();
        if (nItemId == R.id.app_ttsrule) {
			Intent i = new Intent(MainActivity.this, TTSPlayRuleActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else if (nItemId == R.id.app_smsrule) {
			Intent i = new Intent(MainActivity.this, SMSReceiveRuleActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else if (nItemId == R.id.app_appsettings) {
            Intent i = new Intent(MainActivity.this, AppSettingsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
		} else if (nItemId ==  R.id.app_unittest) {
            Intent i = new Intent(MainActivity.this, UnitTestActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
        } else if (nItemId ==  R.id.app_crashtest) {
            for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
                getString(i);
            }
        } else if (nItemId ==  R.id.app_about) {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else if (nItemId ==  R.id.app_smsrecycle) {
            Intent i = new Intent(MainActivity.this, SMSRecycleActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        
        return super.onOptionsItemSelected(item);
    }

}
