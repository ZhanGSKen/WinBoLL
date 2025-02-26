package cc.winboll.studio.contacts;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.activities.SettingsActivity;
import cc.winboll.studio.contacts.adapters.MyPagerAdapter;
import cc.winboll.studio.contacts.beans.MainServiceBean;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.YesNoAlertDialog;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

final public class MainActivity extends AppCompatActivity implements IWinBollActivity, ViewPager.OnPageChangeListener, View.OnClickListener {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;

    public static final String ACTION_SOS = "cc.winboll.studio.libappbase.WinBoll.ACTION_SOS";

    static MainActivity _MainActivity;
    LogView mLogView;
    Toolbar mToolbar;
    CheckBox cbMainService;
    MainServiceBean mMainServiceBean;
    ViewPager viewPager;
    private List<View> views; //用来存放放进ViewPager里面的布局
    //实例化存储imageView（导航原点）的集合
    ImageView[] imageViews;
    //MyPagerAdapter adapter;//适配器
    MyPagerAdapter pagerAdapter;
    LinearLayout linearLayout;//下标所在在LinearLayout布局里
    int currentPoint = 0;//当前被选中中页面的下标

    private TelephonyManager telephonyManager;
    private MyPhoneStateListener phoneStateListener;

    private static final int DIALER_REQUEST_CODE = 1;

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public APPInfo getAppInfo() {
//        String szBranchName = "contacts";
//
//        APPInfo appInfo = AboutActivityFactory.buildDefaultAPPInfo();
//        appInfo.setAppName("Contacts");
//        appInfo.setAppIcon(cc.winboll.studio.libapputils.R.drawable.ic_winboll);
//        appInfo.setAppDescription("Contacts Description");
//        appInfo.setAppGitName("APP");
//        appInfo.setAppGitOwner("Studio");
//        appInfo.setAppGitAPPBranch(szBranchName);
//        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
//        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=Contacts");
//        appInfo.setAppAPKName("Contacts");
//        appInfo.setAppAPKFolderName("Contacts");
//        return appInfo;
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 接收并处理 Intent 数据，函数 Intent 处理接收就直接返回
        //if (prosessIntents(getIntent())) return;
        // 以下正常创建主窗口
        super.onCreate(savedInstanceState);
        _MainActivity = this;
        setContentView(R.layout.activity_main);

        // 初始化工具栏
        mToolbar = findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(mToolbar);
        if (isEnableDisplayHomeAsUp()) {
            // 显示后退按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setSubtitle(getTag());

        initData();
        initView();
        //initPoint();//调用初始化导航原点的方法
        viewPager.addOnPageChangeListener(this);//滑动事件

        ViewPager viewPager = findViewById(R.id.activitymainViewPager1);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.activitymainTabLayout1);
        tabLayout.setupWithViewPager(viewPager);

//        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
//        if (mMainServiceBean == null) {
//            mMainServiceBean = new MainServiceBean();
//        }
//        cbMainService = findViewById(R.id.activitymainCheckBox1);
//        cbMainService.setChecked(mMainServiceBean.isEnable());
//        cbMainService.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View view) {
//                    if (cbMainService.isChecked()) {
//                        MainService.startMainService(MainActivity.this);
//                    } else {
//                        MainService.stopMainService(MainActivity.this);
//                    }
//                }
//            });

        MainServiceBean mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean == null) {
            mMainServiceBean = new MainServiceBean();
            MainServiceBean.saveBean(this, mMainServiceBean);
        }
        if (mMainServiceBean.isEnable()) {
            MainService.startMainService(this);
        }

        // 初始化TelephonyManager和PhoneStateListener
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public static void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(_MainActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        _MainActivity.startActivity(intent);
    }

    //初始化view，即显示的图片
    void initView() {
        viewPager = findViewById(R.id.activitymainViewPager1);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        //adapter = new MyPagerAdapter(views);
        //viewPager = findViewById(R.id.activitymainViewPager1);
        //viewPager.setAdapter(adapter);
        //linearLayout = findViewById(R.id.activitymainLinearLayout1);
        //initPoint();//初始化页面下方的点
        viewPager.setOnPageChangeListener(this);

    }

    //初始化所要显示的布局
    void initData() {
        ViewPager viewPager = findViewById(R.id.activitymainViewPager1);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view1 = inflater.inflate(R.layout.fragment_call_log, viewPager, false);
        View view2 = inflater.inflate(R.layout.fragment_contacts, viewPager, false);
        View view3 = inflater.inflate(R.layout.fragment_log, viewPager, false);

        views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
    }

//    void initPoint() {
//        imageViews = new ImageView[5];//实例化5个图片
//        for (int i = 0; i < linearLayout.getChildCount(); i++) {
//            imageViews[i] = (ImageView) linearLayout.getChildAt(i);
//            imageViews[i].setImageResource(R.drawable.ic_launcher);
//            imageViews[i].setOnClickListener(this);//点击导航点，即可跳转
//            imageViews[i].setTag(i);//重复利用实例化的对象
//        }
//        currentPoint = 0;//默认第一个坐标
//        imageViews[currentPoint].setImageResource(R.drawable.ic_launcher);
//    }

    //OnPageChangeListener接口要实现的三个方法
    /*    onPageScrollStateChanged(int state)
     此方法是在状态改变的时候调用，其中state这个参数有三种状态：
     SCROLL_STATE_DRAGGING（1）表示用户手指“按在屏幕上并且开始拖动”的状态
     （手指按下但是还没有拖动的时候还不是这个状态，只有按下并且手指开始拖动后log才打出。）
     SCROLL_STATE_IDLE（0）滑动动画做完的状态。
     SCROLL_STATE_SETTLING（2）在“手指离开屏幕”的状态。*/
    @Override
    public void onPageScrollStateChanged(int state) {

    }
    /*    onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
     当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。其中三个参数的含义分别为：

     position :当前页面，即你点击滑动的页面（从A滑B，则是A页面的position。
     positionOffset:当前页面偏移的百分比
     positionOffsetPixels:当前页面偏移的像素位置*/
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    /*    onPageSelected(int position)
     此方法是页面滑动完后得到调用，position是你当前选中的页面的Position（位置编号）
     (从A滑动到B，就是B的position)*/
    public void onPageSelected(int position) {

//        ImageView preView = imageViews[currentPoint];
//        preView.setImageResource(R.drawable.ic_launcher);
//        ImageView currView = imageViews[position];
//        currView.setImageResource(R.drawable.ic_launcher);
//        currentPoint = position;
    }

    //小圆点点击事件
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        //通过getTag(),可以判断是哪个控件
//        int i = (Integer) v.getTag();
//        viewPager.setCurrentItem(i);//直接跳转到某一个页面的情况
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //setSubTitle("");
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    LogUtils.d(TAG, "电话已挂断");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    LogUtils.d(TAG, "正在通话中");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    LogUtils.d(TAG, "来电: " + incomingNumber);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy() SOS");
    }



    //
    // 处理传入的 Intent 数据
    //
//    boolean prosessIntents(Intent intent) {
//        if (intent == null 
//            || intent.getAction() == null
//            || intent.getAction().equals(""))
//            return false;
//
//        if (intent.getAction().equals(StringToQrCodeView.ACTION_UNITTEST_QRCODE)) {
//            try {
//                WinBollActivity clazzActivity = UnitTestActivity.class.newInstance();
//                String tag = clazzActivity.getTag();
//                LogUtils.d(TAG, "String tag = clazzActivity.getTag(); tag " + tag);
//                Intent subIntent = new Intent(this, UnitTestActivity.class);
//                subIntent.setAction(intent.getAction());
//                File file = new File(getCacheDir(), UUID.randomUUID().toString());
//                //取出文件uri
//                Uri uri = intent.getData();
//                if (uri == null) {
//                    uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
//                }
//                //获取文件真实地址
//                String szSrcPath = UriUtils.getFileFromUri(getApplication(), uri);
//                if (TextUtils.isEmpty(szSrcPath)) {
//                    return false;
//                }
//
//                Files.copy(Paths.get(szSrcPath), Paths.get(file.getPath()));
//                //startWinBollActivity(subIntent, tag);
//                WinBollActivityManager.getInstance(this).startWinBollActivity(this, subIntent, UnitTestActivity.class);
//            } catch (IllegalAccessException | InstantiationException | IOException e) {
//                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//                // 函数处理异常返回失败
//                return false;
//            }
//        } else {
//            LogUtils.d(TAG, "prosessIntents|" + intent.getAction() + "|yet");
//            return false;
//        }
//        return true;
//    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activitymainToolbar1);
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    void exit() {
        YesNoAlertDialog.OnDialogResultListener listener = new YesNoAlertDialog.OnDialogResultListener(){

            @Override
            public void onYes() {
                WinBollActivityManager.getInstance(getApplicationContext()).finishAll();
            }

            @Override
            public void onNo() {
            }
        };
        YesNoAlertDialog.show(this, "[ " + getString(R.string.app_name) + " ]", "Exit(Yes/No).\nIs close all activity?", listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //WinBollActivityManager.getInstance(this).startWinBollActivity(this, CallActivity.class);
        }
//        } else 
//        if (item.getItemId() == R.id.item_exit) {
//            exit();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Android M 及以上检查是否是系统默认电话应用
     */
    public boolean isDefaultPhoneCallApp() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager manger = (TelecomManager) getSystemService(TELECOM_SERVICE);
            if (manger != null && manger.getDefaultDialerPackage() != null) {
                return manger.getDefaultDialerPackage().equals(getPackageName());
            }
        }
        return false;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
            Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (resultCode) {
//            case REQUEST_HOME_ACTIVITY : {
//                    LogUtils.d(TAG, "REQUEST_HOME_ACTIVITY");
//                    break;
//                }
//            case REQUEST_ABOUT_ACTIVITY : {
//                    LogUtils.d(TAG, "REQUEST_ABOUT_ACTIVITY");
//                    break;
//                }
//            default : {
//                    super.onActivityResult(requestCode, resultCode, data);
//                }
//        }
        if (requestCode == DIALER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, getString(R.string.app_name) + " 已成为默认电话应用",
                               Toast.LENGTH_SHORT).show();
            }
        }
    }
}
