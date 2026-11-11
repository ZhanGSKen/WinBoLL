package cc.winboll.studio.libaes.activitys;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/06/13 18:58:54
 * @Describe 可以加入Fragment的有抽屉的活动窗口抽象类
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;
import androidx.core.view.GravityCompat;
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
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import com.baoyz.widget.PullRefreshLayout;
import java.util.ArrayList;

public abstract class DrawerFragmentActivity extends AppCompatActivity implements IWinBoLLActivity, AdapterView.OnItemClickListener {

    public static final String TAG = "DrawerFragmentActivity";

    static final String SHAREDPREFERENCES_NAME = "SHAREDPREFERENCES_NAME";
    static final String DRAWER_THEME_TYPE = "DRAWER_THEME_TYPE";

    // Fragments are hosted in this container (see activity_drawerfragment.xml)
    private static final int FRAGMENT_CONTAINER_ID = R.id.activitydrawerfragmentFrameLayout1;

    ActivityType mActivityType;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    DrawerLayout mDrawerLayout;
    PullRefreshLayout mPullRefreshLayout;
    ADrawerMenuListView mADrawerMenuListView;
    @SuppressWarnings("rawtypes")
    DrawerMenuDataAdapter mDrawerMenuDataAdapter;
    // Last known drawer open state, maintained by ActionBarDrawerToggle callbacks
    boolean mIsDrawerOpened = false;

    protected Toolbar mToolbar;
    public enum ActivityType { Main, Secondary }
    protected volatile AESThemeBean.ThemeType mThemeType;
    protected ArrayList<DrawerMenuBean> malDrawerMenuItem;

    abstract protected ActivityType initActivityType();

    // =========================================================================================
    // AppCompatActivity public/protected overrides
    // -----------------------------------------------------------------------------------------
    // DrawerFragmentActivity is the single extension point for drawer based activities, so every
    // public/protected member declared by AppCompatActivity (appcompat 1.3.0) is overridden here
    // and forwarded to super. Subclasses may extend any of them in one place instead of touching
    // the AndroidX parent class. Methods without business logic stay pure forwards on purpose.
    // =========================================================================================

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNightModeChanged(int mode) {
        super.onNightModeChanged(mode);
    }

    @Override
    public ActionBar getSupportActionBar() {
        return super.getSupportActionBar();
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return super.getMenuInflater();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    @Override
    public <T extends View> T findViewById(int id) {
        return super.findViewById(id);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
    }

    @Override
    public boolean supportRequestWindowFeature(int featureId) {
        return super.supportRequestWindowFeature(featureId);
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        super.supportInvalidateOptionsMenu();
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        super.onSupportActionModeFinished(mode);
    }

    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return super.onWindowStartingSupportActionMode(callback);
    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        return super.startSupportActionMode(callback);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setSupportProgressBarVisibility(boolean visible) {
        super.setSupportProgressBarVisibility(visible);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        super.setSupportProgressBarIndeterminateVisibility(visible);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setSupportProgressBarIndeterminate(boolean indeterminate) {
        super.setSupportProgressBarIndeterminate(indeterminate);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setSupportProgress(int progress) {
        super.setSupportProgress(progress);
    }

    @Override
    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onCreateSupportNavigateUpTaskStack(builder);
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onPrepareSupportNavigateUpTaskStack(builder);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    @Override
    public boolean supportShouldUpRecreateTask(Intent targetIntent) {
        return super.supportShouldUpRecreateTask(targetIntent);
    }

    @Override
    public void supportNavigateUpTo(Intent upIntent) {
        super.supportNavigateUpTo(upIntent);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
    }

    @Override
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return super.getDrawerToggleDelegate();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }

    @Override
    public AppCompatDelegate getDelegate() {
        return super.getDelegate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public Resources getResources() {
        return super.getResources();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void openOptionsMenu() {
        super.openOptionsMenu();
    }

    @Override
    public void closeOptionsMenu() {
        super.closeOptionsMenu();
    }

    // =========================================================================================
    // Business overrides
    // =========================================================================================

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
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        if (mPullRefreshLayout != null) {
            mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (malDrawerMenuItem != null) {
                        reinitDrawerMenuItemList(malDrawerMenuItem);
                    }
                    notifyDrawerMenuDataChanged();
                    mPullRefreshLayout.setRefreshing(false);
                }
            });
        }

        malDrawerMenuItem = new ArrayList<DrawerMenuBean>();

        mDrawerMenuDataAdapter = new DrawerMenuDataAdapter<DrawerMenuBean>(malDrawerMenuItem, R.layout.listview_drawermenu) {
            @Override
            public void bindView(ViewHolder holder, DrawerMenuBean obj) {
                holder.setImageResource(R.id.listviewdrawermenuImageView1, obj.getIconId());
                holder.setText(R.id.listviewdrawermenuTextView1, obj.getIconName());
            }
        };
        if (mADrawerMenuListView != null) {
            mADrawerMenuListView.setAdapter(mDrawerMenuDataAdapter);
            mADrawerMenuListView.setOnItemClickListener(this);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (mDrawerLayout != null) {
            // ActionBarDrawerToggle owns the hamburger click and the arrow animation,
            // so no custom navigation click listener is needed (it would disable the animation).
            mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.lib_name, R.string.lib_name) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    mIsDrawerOpened = true;
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    mIsDrawerOpened = false;
                }
            };
            mActionBarDrawerToggle.syncState();
            mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        }

        initDrawerMenuItemList(malDrawerMenuItem);
    }

    void initSecondaryRootView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // ===================================== Fragment 管理 =====================================

    /**
     * Remove the given fragment from the container.
     * @return the fragment count reported after commit (informational only)
     */
    public <T extends Fragment> int removeFragment(T fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragment != null && fragment.isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
        return fragmentManager.getFragments().size() - 1;
    }

    /**
     * Add the given fragment into the container. Does nothing when already added.
     * @return the fragment count reported after commit (informational only)
     */
    public <T extends Fragment> int addFragment(T fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragment != null && !fragment.isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(FRAGMENT_CONTAINER_ID, fragment);
            fragmentTransaction.commit();
        }
        return fragmentManager.getFragments().size() - 1;
    }

    /**
     * Show the given fragment and hide all others.
     * The fragment must have been added first (see {@link #addFragment(Fragment)} or
     * {@link #switchFragment(Fragment)}); a pending add is not added again here.
     */
    public <T extends Fragment> void showFragment(T fragment) {
        if (fragment == null) {
            LogUtils.w(TAG, "showFragment: null fragment ignored.");
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<Fragment>(fragmentManager.getFragments());
        boolean found = false;

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (Fragment item : fragments) {
            if (item.equals(fragment)) {
                fragmentTransaction.show(item);
                found = true;
            } else {
                fragmentTransaction.hide(item);
            }
        }
        fragmentTransaction.commit();
        if (found) {
            LogUtils.d(TAG, "showFragment: " + fragment.getClass().getSimpleName());
        } else {
            LogUtils.w(TAG, "showFragment: " + fragment.getClass().getSimpleName()
                    + " is not attached yet, call addFragment() first.");
        }
    }

    /**
     * Show the fragment at the given position inside the FragmentManager list and hide others.
     */
    public void showFragment(int nPosition) {
        ArrayList<Fragment> fragments = new ArrayList<Fragment>(getSupportFragmentManager().getFragments());
        if (nPosition < 0 || nPosition >= fragments.size()) {
            LogUtils.w(TAG, "showFragment: position " + nPosition + " out of range, size=" + fragments.size());
            return;
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            if (i == nPosition) {
                fragmentTransaction.show(fragments.get(i));
            } else {
                fragmentTransaction.hide(fragments.get(i));
            }
        }
        fragmentTransaction.commit();
    }

    /**
     * Convenience one-shot API: attach the fragment if needed and show it in a single
     * transaction, then notify {@link #onFragmentShown(Fragment)}.
     * Use this instead of calling addFragment() followed by showFragment().
     */
    protected <T extends Fragment> void switchFragment(T fragment) {
        if (fragment == null) {
            LogUtils.w(TAG, "switchFragment: null fragment ignored.");
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<Fragment>(fragmentManager.getFragments());
        boolean attached = false;

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (Fragment item : fragments) {
            if (item.equals(fragment)) {
                fragmentTransaction.show(item);
                attached = true;
            } else {
                fragmentTransaction.hide(item);
            }
        }
        if (!attached) {
            // Not attached yet: add and show it inside the same transaction (no double add)
            fragmentTransaction.add(FRAGMENT_CONTAINER_ID, fragment);
        }
        fragmentTransaction.commit();
        onFragmentShown(fragment);
    }

    /**
     * Hook invoked after {@link #switchFragment(Fragment)} has shown a fragment.
     * Subclasses may override to update the toolbar title/subtitle.
     */
    protected void onFragmentShown(Fragment fragment) {
    }

    // ===================================== 抽屉菜单 =====================================

    /** @return true when the drawer is currently open (Main type only). */
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    /** Open the drawer when available (Main type only). */
    public void openDrawer() {
        if (mDrawerLayout != null && mPullRefreshLayout != null) {
            mDrawerLayout.openDrawer(mPullRefreshLayout);
        }
    }

    /** Close the drawer when available (Main type only). */
    public void closeDrawer() {
        if (mDrawerLayout != null && mPullRefreshLayout != null) {
            mDrawerLayout.closeDrawer(mPullRefreshLayout);
        }
    }

    protected void initDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {

    }

    protected void reinitDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {

    }

    public void notifyDrawerMenuDataChanged() {
        if (mDrawerMenuDataAdapter != null) {
            mDrawerMenuDataAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        closeDrawer();
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
}
