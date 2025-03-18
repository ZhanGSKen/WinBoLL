package cc.winboll.studio.libaes.activitys;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/13 18:58:54
 * @Describe 可以加入Fragment的有抽屉的活动窗口抽象类
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cc.winboll.studio.libaes.DrawerMenuDataAdapter;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.beans.AESThemeBean;
import cc.winboll.studio.libaes.beans.DrawerMenuBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.views.ADrawerMenuListView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import com.baoyz.widget.PullRefreshLayout;
import java.util.ArrayList;

public abstract class DrawerFragmentActivity extends AppCompatActivity implements IWinBollActivity,AdapterView.OnItemClickListener {

    public static final String TAG = "DrawerFragmentActivity";

    static final String SHAREDPREFERENCES_NAME = "SHAREDPREFERENCES_NAME";
    static final String DRAWER_THEME_TYPE = "DRAWER_THEME_TYPE";

    //protected Context mContext;
    ActivityType mActivityType;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    DrawerLayout mDrawerLayout;
    PullRefreshLayout mPullRefreshLayout;
    ADrawerMenuListView mADrawerMenuListView;
    DrawerMenuDataAdapter mDrawerMenuDataAdapter;
    boolean mIsDrawerOpened = false;
    boolean mIsDrawerOpening = false;
    boolean mIsDrawerClosing = false;

    protected Toolbar mToolbar;
    public enum ActivityType { Main, Secondary }
    protected volatile AESThemeBean.ThemeType mThemeType;
    protected ArrayList<DrawerMenuBean> malDrawerMenuItem;
    abstract protected ActivityType initActivityType();
    //abstract protected View initContentView(LayoutInflater inflater, ViewGroup rootView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //mContext = this;
        mThemeType = getThemeType();
        setThemeStyle();
        super.onCreate(savedInstanceState);
        mActivityType = initActivityType();
        initRootView();
        LogUtils.d(TAG, "onCreate end.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*@Override
    public Intent getIntent() {
        // TODO: Implement this method
        return super.getIntent();
    }

    public Context getContext() {
        return this.mContext;
    }*/

    @Override
    public MenuInflater getMenuInflater() {
        // TODO: Implement this method
        return super.getMenuInflater();
    }

    /*public void setSubtitle(CharSequence context) {
        // TODO: Implement this method
        getSupportActionBar().setSubtitle(context);
    }*/

    @Override
    public void recreate() {
        super.recreate();
    }

    /*@Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }*/

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    /*@Override
    public FragmentManager getSupportFragmentManager() {
        return super.getSupportFragmentManager();
    }

    public void setSubtitle(int resId) {
        // TODO: Implement this method
        getSupportActionBar().setSubtitle(resId);
    }

    public void setTitle(CharSequence context) {
        // TODO: Implement this method
        getSupportActionBar().setTitle(context);
    }

    public void setTitle(int resId) {
        // TODO: Implement this method
        getSupportActionBar().setTitle(resId);
    }*/

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public Context getApplicationContext() {
        // TODO: Implement this method
        return super.getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    void setThemeStyle() {
        //setTheme(AESThemeBean.getThemeStyle(getThemeType()));
        setTheme(AESThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    boolean checkThemeStyleChange() {
        return mThemeType != getThemeType();
    }

    AESThemeBean.ThemeType getThemeType() {
        /*SharedPreferences sharedPreferences = getSharedPreferences(
         SHAREDPREFERENCES_NAME, MODE_PRIVATE);
         return AESThemeBean.ThemeType.values()[((sharedPreferences.getInt(DRAWER_THEME_TYPE, AESThemeBean.ThemeType.DEFAULT.ordinal())))];
         */
        return AESThemeBean.getThemeStyleType(AESThemeUtil.getThemeTypeID(getApplicationContext()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AESThemeUtil.onAppThemeItemSelected(this, item)) {
            recreate();
        } else if (R.id.item_testappcrash == item.getItemId()) {
            for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
                getString(i);
            }
        } else if (R.id.item_about == item.getItemId()) {
            LogUtils.d(TAG, "onAbout");
        } else if (android.R.id.home == item.getItemId()) {
            LogUtils.d(TAG, "android.R.id.home");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkThemeStyleChange()) {
            recreate();
        }
    }

    void initRootView() {
        setContentView(R.layout.activity_drawerfragment);

        mToolbar = findViewById(R.id.activitydrawerfragmentASupportToolbar1);
        setActionBar(mToolbar);

        if (mActivityType == ActivityType.Main) {
            initMainRootView();
        } else if (mActivityType == ActivityType.Secondary) {
            initSecondaryRootView();
        }
    }

    void initMainRootView() {
        mDrawerLayout = findViewById(R.id.activitydrawerfragmentDrawerLayout1);
        mADrawerMenuListView = findViewById(R.id.activitydrawerfragmentDrawerMenuListView1);
        mPullRefreshLayout = findViewById(R.id.activitydrawerfragmentPullRefreshLayout1);

        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //LogUtils.d(TAG, "onRefresh");
                    reinitDrawerMenuItemList(malDrawerMenuItem);
                    mDrawerMenuDataAdapter.notifyDataSetChanged();
                    mPullRefreshLayout.setRefreshing(false);
                }
            });

        malDrawerMenuItem = new ArrayList<DrawerMenuBean>();

        mDrawerMenuDataAdapter = new DrawerMenuDataAdapter<DrawerMenuBean>(malDrawerMenuItem, R.layout.listview_drawermenu) {
            @Override
            public void bindView(ViewHolder holder, DrawerMenuBean obj) {
                holder.setImageResource(R.id.listviewdrawermenuImageView1, obj.getIconId());
                holder.setText(R.id.listviewdrawermenuTextView1, obj.getIconName());
            }
        };
        mADrawerMenuListView.setAdapter(mDrawerMenuDataAdapter);
        mADrawerMenuListView.setOnItemClickListener(this);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.lib_name, R.string.lib_name) {
            @Override
            public void onDrawerOpened(View drawerView) {//完全打开时触发
                super.onDrawerOpened(drawerView);
                mIsDrawerOpened = true;
                mIsDrawerOpening = false;
                //Toast.makeText(MainActivity.this,"onDrawerOpened",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerClosed(View drawerView) {//完全关闭时触发
                super.onDrawerClosed(drawerView);
                mIsDrawerOpened = false;
                mIsDrawerClosing = false;
                //Toast.makeText(MainActivity.this,"onDrawerClosed",Toast.LENGTH_SHORT).show();
            }

            /** 
             * 当抽屉被滑动的时候调用此方法 
             * slideOffset表示 滑动的幅度（0-1） 
             */  
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            /** 
             * 当抽屉滑动状态改变的时候被调用 
             * 状态值是STATE_IDLE（闲置--0）, STATE_DRAGGING（拖拽的--1）, STATE_SETTLING（固定--2）中之一。 
             *具体状态可以慢慢调试
             */  
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };

        //设置显示旋转菜单
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //通过下面这句实现toolbar和Drawer的联动：如果没有这行代码，箭头是不会随着侧滑菜单的开关而变换的（或者没有箭头），
        // 可以尝试一下，不影响正常侧滑
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        //去掉侧滑的默认图标（动画箭头图标），也可以选择不去，
        //不去的话把这一行注释掉或者改成true，然后把toolbar.setNavigationIcon注释掉就行了
        //mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsDrawerOpened || mIsDrawerOpening) {
                        mIsDrawerClosing = true;
                        mIsDrawerOpening = false;
                        mDrawerLayout.closeDrawer(mPullRefreshLayout);
                        return;
                    } 
                    if (!mIsDrawerOpened || mIsDrawerClosing) {
                        mIsDrawerOpening = true;
                        mIsDrawerClosing = false;
                        mDrawerLayout.openDrawer(mPullRefreshLayout);
                        return;
                    }
                }
            });

        initDrawerMenuItemList(malDrawerMenuItem);
    }

    void initSecondaryRootView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //LogUtils.d(TAG, "onClick " + Integer.toString(v.getId()));
                    finish();
                }
            });
    }

    public <T extends Fragment> int removeFragment(T fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
        return fragmentManager.getFragments().size() - 1;
    }

    public <T extends Fragment> int addFragment(T fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.activitydrawerfragmentFrameLayout1, fragment);
        fragmentTransaction.commit();
        return fragmentManager.getFragments().size() - 1;
    }

    public <T extends Fragment> void showFragment(T fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
            if (fragmentManager.getFragments().get(i).equals(fragment)) {
                fragmentTransaction.show(fragmentManager.getFragments().get(i));
            } else {
                fragmentTransaction.hide(fragmentManager.getFragments().get(i));
            }
        }
        fragmentTransaction.commit();
    }

    public void showFragment(int nPosition) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
            if (i == nPosition) {
                fragmentTransaction.show(fragmentManager.getFragments().get(i));
            } else {
                fragmentTransaction.hide(fragmentManager.getFragments().get(i));
            }
        }
        fragmentTransaction.commit();
    }

    protected void initDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {

    }

    protected void reinitDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {

    }

    public void notifyDrawerMenuDataChanged() {
        mDrawerMenuDataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDrawerLayout.closeDrawer(mPullRefreshLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mActivityType == ActivityType.Main) {
            AESThemeUtil.inflateMenu(this, menu);
            getMenuInflater().inflate(R.menu.toolbar_drawerbase, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int who, int targetFragment, Intent requestCode) {
        super.onActivityResult(who, targetFragment, requestCode);
    }
}
