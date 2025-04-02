package cc.winboll.studio.autoinstaller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextClock;
import androidx.core.content.FileProvider;
import cc.winboll.studio.autoinstaller.MainActivity;
import cc.winboll.studio.autoinstaller.models.APKModel;
import cc.winboll.studio.autoinstaller.models.AppConfigs;
import cc.winboll.studio.autoinstaller.services.MainService;
import cc.winboll.studio.autoinstaller.utils.NotificationUtil;
import cc.winboll.studio.autoinstaller.views.ListViewForScrollView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import com.hjq.toast.ToastUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private static final int INSTALL_PERMISSION_CODE = 1;
    
    ArrayList<APKModel> _APKModelList = new ArrayList<APKModel>();
    LogView mLogView;
    TextClock mTextClock;
    EditText mEditText;
    Switch mSwitch;
    //sharedPreferences shPrefs;

    SimpleAdapter mSimpleAdapter;
    static String MAP_NAME = "NAME";
    List<Map<String,Object>> mAdapterData = new ArrayList<>();
    ListViewForScrollView mListViewForScrollView;
    String szAPKFileName;
	String szAPKFilePath;

    public static final String ACTION_NEW_INSTALLTASK = MainActivity.class.getName() + ".ACTION_NEW_INSTALLTASK";
    public static final String EXTRA_INSTALLED_PACKAGENAME = "EXTRA_INSTALLED_PACKAGENAME";
    public static final String EXTRA_INSTALLED_APKFILEPATH = "EXTRA_INSTALLED_APKFILEPATH";
    public static int REQUEST_CODE_ON_INSTALL_COMPLETED = 0;

    String mszInstalledPackageName = "";
    String mszInstalledAPKFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		// Remove this line if you don't want AndroidIDE to show this app's logs
		//LogSender.startLogging(this);

        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        initView();

        if (getIntent().getAction().equals(ACTION_NEW_INSTALLTASK)) {
            mszInstalledPackageName = getIntent().getStringExtra(EXTRA_INSTALLED_PACKAGENAME);
            mszInstalledAPKFilePath = getIntent().getStringExtra(EXTRA_INSTALLED_APKFILEPATH);
            installAPK();
        }

        //LogUtils.d(TAG, "Hello, World!");
	}

    private void initView() {
        // 设置调试日志显示
        mLogView = findViewById(R.id.logview);
        mLogView.start();

        AppConfigs appConfigs = AppConfigs.loadAppConfigs(this);
        if (appConfigs == null) {
            appConfigs = new AppConfigs(); 
            AppConfigs.saveAppConfigs(this, appConfigs);
        }

        if (appConfigs.getSetupMode() == AppConfigs.SetupMode.WATCHOUTPUTINSTALLER) {
            ((RadioButton)findViewById(R.id.activitymainRadioButton1)).setChecked(true);
        } else if (appConfigs.getSetupMode() == AppConfigs.SetupMode.NEWAPKINFONEWAPKINFO) {
            ((RadioButton)findViewById(R.id.activitymainRadioButton2)).setChecked(true);
        } 

        NotificationUtil nu = new NotificationUtil();
        nu.createServiceNotificationChannel(MainActivity.this);


        //Define shared preferences class
        //shPrefs = new sharedPreferences();

        mTextClock = findViewById(R.id.activitymainTextClock1);
        mTextClock.setTimeZone("Asia/Shanghai");
        mTextClock.setFormat12Hour("yyyy/MM/dd HH:mm:ss");

        mEditText = findViewById(R.id.activitymainEditText1);
        //Add text from EditText to Shared Preferences
        //mEditText.setText(shPrefs.getFilePath(MainActivity.this));
        mEditText.setText(appConfigs.getWatchingFilePath());

        mListViewForScrollView = findViewById(R.id.activitymainListViewForScrollView1);
        /*Map<String,Object> map1 =new HashMap<>();
         map1.put(MAP_NAME, "test1");
         mAdapterData.add(map1);
         Map<String,Object> map2 =new HashMap<>();
         map2.put(MAP_NAME, "test2");
         mAdapterData.add(map2);*/
        // 绑定适配器与数据
        mSimpleAdapter = new SimpleAdapter(MainActivity.this, mAdapterData, R.layout.installitem
                                           , new String[]{MAP_NAME}
                                           , new int[]{R.id.installitemTextView1});
        mSimpleAdapter.setDropDownViewResource(R.layout.installitem);
        mListViewForScrollView.setAdapter(mSimpleAdapter);

        mSwitch = findViewById(R.id.activitymainSwitch1);
        //if (shPrefs.getIsLockFilePath(MainActivity.this)) {
        if (appConfigs.isEnableService()) {
            mSwitch.setChecked(true);
            mEditText.setEnabled(false);
            mEditText.setBackgroundColor(getColor(R.color.colorLock));
            szAPKFilePath = mEditText.getText().toString();
            startWatchingFile();

        }

    }

    String getLastApkPackageName() {
        APKModel.loadBeanList(this, _APKModelList, APKModel.class);
        if (_APKModelList.size() > 0) {
            return _APKModelList.get(_APKModelList.size() - 1).getApkPackageName();
        }
        return "";
    }

    public void onOpenAPP(View view) {
        String szInstalledPackageName = getLastApkPackageName();
        if (szInstalledPackageName.trim().equals("")) {
            ToastUtils.show("Installed APP package name is null.");
            return;
        }

        Intent intent = getPackageManager().getLaunchIntentForPackage(mszInstalledPackageName);
        if (intent != null) {
            //ToastUtils.show("startActivity");
            startActivity(intent);
        } else {
            // 若没能获取到启动意图，可进行相应提示等操作，比如跳转到应用商店让用户下载该应用（示例）
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse("market://details?id=" + mszInstalledPackageName));
            startActivity(marketIntent);
        }
    }

    public void onInstallAPK(View view) {
        installAPK();
    }

    void installAPK() {
        if (mszInstalledAPKFilePath.trim().equals("")) {
            ToastUtils.show("Installed APK file path is null.");
            return;
        }
        File file = new File(mszInstalledAPKFilePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            LogUtils.d(TAG, "Uri is : " + apkUri.toString());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivityForResult(intent, REQUEST_CODE_ON_INSTALL_COMPLETED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ON_INSTALL_COMPLETED) {
            // 根据resultCode等判断具体情况，这里简单示例显示一个普通吐司
            //Toast.makeText(this, "[ " + mszInstalledPackageName + " ]安装窗口已关闭啦", Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    public void onLockPath(View view) {
        AppConfigs appConfigs = AppConfigs.loadAppConfigs(this);

        Switch sw = (Switch)view;
        if (sw.isChecked()) {
            String szFilePath = mEditText.getText().toString();

            // 设置空路径时退出
            //
            if (szFilePath.trim().equals("")) {
                sw.setChecked(false);
                ToastUtils.show("监控路径为空。");
                return;
            }

            // 设置监控功能
            //
            File f = new File(szFilePath);
            if (f != null && f.getParentFile() != null && f.getParentFile().exists()) {

                //shPrefs.saveFilePath(MainActivity.this, mEditText.getText().toString());
                appConfigs.setWatchingFilePath(mEditText.getText().toString());
                //shPrefs.saveIsLockFilePath(MainActivity.this, true);
                appConfigs.setIsEnableService(true);
                mEditText.setEnabled(false);
                mEditText.setBackgroundColor(getColor(R.color.colorLock));
                szAPKFilePath = mEditText.getText().toString();
                startWatchingFile();
            } /*else {
             if (mEditText.getText().toString().trim().equals("")) {
             //shPrefs.saveFilePath(MainActivity.this, "");
             appConfigs.setWatchingFilePath("");

             //shPrefs.saveIsLockFilePath(MainActivity.this, true);
             appConfigs.setIsEnableService(true);

             mEditText.setEnabled(false);
             mEditText.setBackgroundColor(getColor(R.color.colorLock));
             szAPKFilePath = "";
             } else {
             sw.setChecked(false);
             }
             }*/
        } else {
            //shPrefs.saveIsLockFilePath(MainActivity.this, false);
            appConfigs.setIsEnableService(false);

            mEditText.setEnabled(true);
            mEditText.setBackgroundColor(Color.TRANSPARENT);
            stopWatchingFile();

        }
        AppConfigs.saveAppConfigs(this, appConfigs);
    }

    void stopWatchingFile() {
        Intent intentService = new Intent(this, MainService.class);
        stopService(intentService);


    }

    void startWatchingFile() {
        LogUtils.d(TAG, "startWatchingFile()");
        Intent intentService = new Intent(MainActivity.this, MainService.class);
        //intentService.putExtra(MainService.EXTRA_APKFILEPATH, szAPKFilePath);
        startService(intentService);
    }

    /*
     *Using class allows you to read data from Shared Preferences from other Activities
     */
    /*static class sharedPreferences {

     //
     //Save file path
     //
     public void saveFilePath(Activity activity, String _filePath) {
     SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
     SharedPreferences.Editor editor = sharedPref.edit();
     editor.putString("_filePath", _filePath);
     editor.apply();
     }

     //
     //Read file path.
     //
     public String  getFilePath(Activity activity) {
     SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
     return sharedPref.getString("_filePath", "");
     }

     //
     //Set file path lock status.
     //
     public void saveIsLockFilePath(Activity activity, boolean isLockFilePath) {
     SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
     SharedPreferences.Editor editor = sharedPref.edit();
     editor.putBoolean("isLockFilePath", isLockFilePath);
     editor.apply();
     }

     //
     //Read file path lock status.
     //
     public boolean getIsLockFilePath(Activity activity) {
     SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
     return sharedPref.getBoolean("isLockFilePath", false);
     }
     }*/

    public void onChangeSetupMode(View view) {
        AppConfigs appConfigs = AppConfigs.loadAppConfigs(this);

        if (view.getId() == R.id.activitymainRadioButton1) {
            appConfigs.setSetupMode(AppConfigs.SetupMode.WATCHOUTPUTINSTALLER);
            ((RadioButton)findViewById(R.id.activitymainRadioButton2)).setChecked(false);
        } else if (view.getId() == R.id.activitymainRadioButton2) {
            appConfigs.setSetupMode(AppConfigs.SetupMode.NEWAPKINFONEWAPKINFO);
            ((RadioButton)findViewById(R.id.activitymainRadioButton1)).setChecked(false);
        }
        AppConfigs.saveAppConfigs(this, appConfigs);
    }
}
