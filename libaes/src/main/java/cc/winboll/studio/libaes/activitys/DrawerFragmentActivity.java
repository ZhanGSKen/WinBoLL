package cc.winboll.studio.libaes.activitys;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/06/13 18:58:54
 * @Describe 可以加入Fragment的有抽屉的活动窗口抽象类
 */
import android.app.Activity;
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
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import cc.winboll.studio.libaes.DrawerMenuDataAdapter;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.models.DrawerMenuBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.DevelopUtils;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libaes.views.ADrawerMenuListView;
import cc.winboll.studio.libaes.views.ADsBannerView;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import com.baoyz.widget.PullRefreshLayout;

import java.util.ArrayList;

public abstract class DrawerFragmentActivity extends AppCompatActivity implements IWinBoLLActivity, AdapterView.OnItemClickListener {

    public static final String TAG = "DrawerFragmentActivity";

    static final String SHAREDPREFERENCES_NAME = "SHAREDPREFERENCES_NAME";
    static final String DRAWER_THEME_TYPE = "DRAWER_THEME_TYPE";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 替换：使用工具类统一应用主题
        AESThemeUtil.applyAppCompatTheme(this);
        mThemeType = AESThemeBean.getThemeStyleType(AESThemeUtil.getThemeTypeID(getApplicationContext()));
        super.onCreate(savedInstanceState);
        WinBoLLActivityManager.getInstance().add(this);
        mActivityType = initActivityType();
        initRootView();
        LogUtils.d(TAG, "onCreate end.");
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onDestroy() {
        WinBoLLActivityManager.getInstance().registeRemove(this);
        super.onDestroy();
        // 修复：释放广告资源，避免内存泄漏
        ADsBannerView adsBannerView = findViewById(R.id.adsbanner);
        if (adsBannerView != null) {
            adsBannerView.releaseAdResources();
        }
    }

    @Override
    public MenuInflater getMenuInflater() {
        return super.getMenuInflater();
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 替换为 DrawerFragmentActivity 专属点击处理方法
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

    @Override
    protected void onResume() {
        super.onResume();

        ADsBannerView adsBannerView = findViewById(R.id.adsbanner);
        if (adsBannerView != null) {
            adsBannerView.resumeADs(DrawerFragmentActivity.this);
        }
    }

    void initRootView() {
        setContentView(R.layout.activity_drawerfragment);

        mToolbar = findViewById(R.id.activitydrawerfragmentASupportToolbar1);
        setSupportActionBar(mToolbar);

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
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mIsDrawerOpened = true;
                mIsDrawerOpening = false;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mIsDrawerOpened = false;
                mIsDrawerClosing = false;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

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
            // 替换为兼容版菜单加载方法
            AESThemeUtil.inflateCompatThemeMenu(this, menu);
            // 调试工具菜单
            if (GlobalApplication.isDebugging()) {
                DevelopUtils.inflateMenu(this, menu);
            }
            // 应用信息菜单
            getMenuInflater().inflate(R.menu.toolbar_drawerbase, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int who, int targetFragment, Intent requestCode) {
        super.onActivityResult(who, targetFragment, requestCode);
    }
}

