package cc.winboll.studio.positions;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.activities.AbsActivity;
import cc.winboll.studio.positions.activities.JointPositioningActivity;
import cc.winboll.studio.positions.activities.SettingsActivity;
import cc.winboll.studio.positions.fragments.LogFragment;
import cc.winboll.studio.positions.fragments.PositionsFragment;
import cc.winboll.studio.positions.fragments.TXMSFragment;
import cc.winboll.studio.positions.fragments.TasksFragment;
import cc.winboll.studio.positions.models.MainServiceBean;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import cc.winboll.studio.positions.listeners.OnTXRTLocationListener;
import android.location.Location;

final public class MainActivity extends AbsActivity implements IWinBollActivity, ViewPager.OnPageChangeListener, View.OnClickListener {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;

    public static final String ACTION_SOS = "cc.winboll.studio.libappbase.WinBoll.ACTION_SOS";

    static MainActivity _MainActivity;
    
    // 创建Fragment列表和标题列表
    List<Fragment> fragmentList = new ArrayList<>();
    List<String> tabTitleList = new ArrayList<>();

    TXMSFragment mTXMSFragment;
    PositionsFragment mPositionsFragment;
    TasksFragment mTasksFragment;
    LogFragment mLogFragment;
    LogView mLogView;
    Toolbar mToolbar;
    CheckBox cbMainService;
    MainServiceBean mMainServiceBean;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<View> views; //用来存放放进ViewPager里面的布局
    //实例化存储imageView（导航原点）的集合
    ImageView[] imageViews;
    //MyPagerAdapter adapter;//适配器
    MyPagerAdapter pagerAdapter;
    LinearLayout linearLayout;//下标所在在LinearLayout布局里
    int currentPoint = 0;//当前被选中中页面的下标

    private static final int DIALER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _MainActivity = this;
        
        // 初始化工具栏
//        mToolbar = findViewById(R.id.toolbar);
//        setActionBar(mToolbar);
////        if (isEnableDisplayHomeAsUp()) {
////            // 显示后退按钮
////            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////        }
//        getActionBar().setTitle("");

        // 初始化地图视图
        // 创建Fragment实例
        mTXMSFragment = new TXMSFragment();
        // 获取FragmentTransaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // 将Fragment添加到FrameLayout容器中
        transaction.add(R.id.frameLayout, mTXMSFragment);
        transaction.commit();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        mPositionsFragment = new PositionsFragment();
        fragmentList.add(mPositionsFragment);
        tabTitleList.add("位置");

        mTasksFragment = new TasksFragment();
        fragmentList.add(mTasksFragment);
        tabTitleList.add("任务");


        mLogFragment = new LogFragment();
        fragmentList.add(mLogFragment);
        tabTitleList.add("日志");

        

        // 设置ViewPager的适配器
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // 关联TabLayout和ViewPager
        tabLayout.setupWithViewPager(viewPager);
        
        mTXMSFragment.setOnTXRTLocationListener(new OnTXRTLocationListener(){
                @Override
                public void onTXRTLocation(Location location) {
                    mPositionsFragment.showLocationTX(location);
                }
            });
    }
    
    public void startTXRTLocation(){
        mTXMSFragment.startRTLocation();
    }

    // ViewPager的适配器
    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private static final int PAGE_COUNT = 3;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            // 根据position返回不同的Fragment
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            // 返回页面总数
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitleList.get(position);
        }
    }

    //初始化view，即显示的图片
//    void initViewPager() {
//        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
//        mViewPager.setAdapter(pagerAdapter);
//        //adapter = new MyPagerAdapter(views);
//        //viewPager = findViewById(R.id.activitymainViewPager1);
//        //viewPager.setAdapter(adapter);
//        //linearLayout = findViewById(R.id.activitymainLinearLayout1);
//        //initPoint();//初始化页面下方的点
//        mViewPager.setOnPageChangeListener(this);
//    }

    //初始化所要显示的布局
//    void initLayoutData() {
//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        View view1 = inflater.inflate(R.layout.fragment_gms, mViewPager, false);
//        View view2 = inflater.inflate(R.layout.fragment_contacts, mViewPager, false);
//        View view3 = inflater.inflate(R.layout.fragment_log, mViewPager, false);
//
//        views = new ArrayList<>();
//        views.add(view1);
//        views.add(view2);
//        views.add(view3);
//    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy() SOS");
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onBackPressed() {
        //exit();
    }

//    void exit() {
//        YesNoAlertDialog.OnDialogResultListener listener = new YesNoAlertDialog.OnDialogResultListener(){
//
//            @Override
//            public void onYes() {
//                WinBollActivityManager.getInstance(getApplicationContext()).finishAll();
//            }
//
//            @Override
//            public void onNo() {
//            }
//        };
//        YesNoAlertDialog.show(this, "[ " + getString(R.string.app_name) + " ]", "Exit(Yes/No).\nIs close all activity?", listener);
//    }

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
        } else if (item.getItemId() == R.id.item_joint_positioning) {
            Intent intent = new Intent(this, JointPositioningActivity.class);
            startActivity(intent);
            //WinBollActivityManager.getInstance(this).startWinBollActivity(this, CallActivity.class);
        } else if (item.getItemId() == R.id.item_demomain) {
//            Intent intent = new Intent(this, com.tencent.map.vector.demo.DemoMainActivity.class);
//            startActivity(intent);
            //WinBollActivityManager.getInstance(this).startWinBollActivity(this, CallActivity.class);
        } else if (item.getItemId() == R.id.item_positionnow) {
            mTXMSFragment.sendRealTimePositioningMessage();
        } else if (item.getItemId() == R.id.item_addnewposition) {
            ToastUtils.show("item_addnewpositon");
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
