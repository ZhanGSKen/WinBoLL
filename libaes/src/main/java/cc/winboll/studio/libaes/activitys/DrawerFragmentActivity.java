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
import android.app.ActivityManager.TaskDescription;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.VoiceInteractor;
import android.app.assist.AssistContent;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.LocusId;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import androidx.core.app.ComponentActivity.ExtraData;
import androidx.core.app.SharedElementCallback;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider.Factory;
import androidx.lifecycle.ViewModelStore;
import androidx.loader.app.LoaderManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.Executor;

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

    // -----------------------------------------------------------------------------------------
    // FragmentActivity  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public void dump(String p0, FileDescriptor p1, PrintWriter p2, String[] p3) {
        super.dump(p0, p1, p2, p3);
    }
    @Override
    public FragmentManager getSupportFragmentManager() {
        return super.getSupportFragmentManager();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public LoaderManager getSupportLoaderManager() {
        return super.getSupportLoaderManager();
    }
    @Override
    protected void onActivityResult(int p0, int p1, Intent p2) {
        super.onActivityResult(p0, p1, p2);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void onAttachFragment(Fragment p0) {
        super.onAttachFragment(p0);
    }
    @Override
    public boolean onCreatePanelMenu(int p0, Menu p1) {
        return super.onCreatePanelMenu(p0, p1);
    }
    @Override
    public View onCreateView(View p0, String p1, Context p2, AttributeSet p3) {
        return super.onCreateView(p0, p1, p2, p3);
    }
    @Override
    public View onCreateView(String p0, Context p1, AttributeSet p2) {
        return super.onCreateView(p0, p1, p2);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    @Override
    public void onMultiWindowModeChanged(boolean p0) {
        super.onMultiWindowModeChanged(p0);
    }
    @Override
    protected void onNewIntent(Intent p0) {
        super.onNewIntent(p0);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onPictureInPictureModeChanged(boolean p0) {
        super.onPictureInPictureModeChanged(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    protected boolean onPrepareOptionsPanel(View p0, Menu p1) {
        return super.onPrepareOptionsPanel(p0, p1);
    }
    @Override
    public boolean onPreparePanel(int p0, View p1, Menu p2) {
        return super.onPreparePanel(p0, p1, p2);
    }
    @Override
    public void onRequestPermissionsResult(int p0, String[] p1, int[] p2) {
        super.onRequestPermissionsResult(p0, p1, p2);
    }
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }
    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
    }
    @Override
    public void setEnterSharedElementCallback(SharedElementCallback p0) {
        super.setEnterSharedElementCallback(p0);
    }
    @Override
    public void setExitSharedElementCallback(SharedElementCallback p0) {
        super.setExitSharedElementCallback(p0);
    }
    @Override
    public void startActivityFromFragment(Fragment p0, Intent p1, int p2) {
        super.startActivityFromFragment(p0, p1, p2);
    }
    @Override
    public void startActivityFromFragment(Fragment p0, Intent p1, int p2, Bundle p3) {
        super.startActivityFromFragment(p0, p1, p2, p3);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startIntentSenderFromFragment(Fragment p0, IntentSender p1, int p2, Intent p3, int p4, int p5, int p6, Bundle p7) throws android.content.IntentSender.SendIntentException {
        super.startIntentSenderFromFragment(p0, p1, p2, p3, p4, p5, p6, p7);
    }
    @Override
    public void supportFinishAfterTransition() {
        super.supportFinishAfterTransition();
    }
    @Override
    public void supportPostponeEnterTransition() {
        super.supportPostponeEnterTransition();
    }
    @Override
    public void supportStartPostponedEnterTransition() {
        super.supportStartPostponedEnterTransition();
    }

    // -----------------------------------------------------------------------------------------
    // ComponentActivity (androidx.activity)  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public Factory getDefaultViewModelProviderFactory() {
        return super.getDefaultViewModelProviderFactory();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public Object getLastCustomNonConfigurationInstance() {
        return super.getLastCustomNonConfigurationInstance();
    }
    @Override
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }
    @Override
    public ViewModelStore getViewModelStore() {
        return super.getViewModelStore();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return super.onRetainCustomNonConfigurationInstance();
    }
    @Override
    protected void onSaveInstanceState(Bundle p0) {
        super.onSaveInstanceState(p0);
    }
    @Override
    public Context peekAvailableContext() {
        return super.peekAvailableContext();
    }
    @Override
    public void reportFullyDrawn() {
        super.reportFullyDrawn();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startActivityForResult(Intent p0, int p1) {
        super.startActivityForResult(p0, p1);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startActivityForResult(Intent p0, int p1, Bundle p2) {
        super.startActivityForResult(p0, p1, p2);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startIntentSenderForResult(IntentSender p0, int p1, Intent p2, int p3, int p4, int p5) throws android.content.IntentSender.SendIntentException {
        super.startIntentSenderForResult(p0, p1, p2, p3, p4, p5);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startIntentSenderForResult(IntentSender p0, int p1, Intent p2, int p3, int p4, int p5, Bundle p6) throws android.content.IntentSender.SendIntentException {
        super.startIntentSenderForResult(p0, p1, p2, p3, p4, p5, p6);
    }

    // -----------------------------------------------------------------------------------------
    // ComponentActivity (androidx.core.app)  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent p0) {
        return super.dispatchKeyShortcutEvent(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public <T extends androidx.core.app.ComponentActivity.ExtraData> T getExtraData(Class<T> p0) {
        return super.getExtraData(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void putExtraData(ExtraData p0) {
        super.putExtraData(p0);
    }
    @Override
    public boolean superDispatchKeyEvent(KeyEvent p0) {
        return super.superDispatchKeyEvent(p0);
    }

    // -----------------------------------------------------------------------------------------
    // android.app.Activity  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public void closeContextMenu() {
        super.closeContextMenu();
    }
    @Override
    public PendingIntent createPendingResult(int p0, Intent p1, int p2) {
        return super.createPendingResult(p0, p1, p2);
    }
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent p0) {
        return super.dispatchGenericMotionEvent(p0);
    }
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent p0) {
        return super.dispatchPopulateAccessibilityEvent(p0);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent p0) {
        return super.dispatchTouchEvent(p0);
    }
    @Override
    public boolean dispatchTrackballEvent(MotionEvent p0) {
        return super.dispatchTrackballEvent(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void enterPictureInPictureMode() {
        super.enterPictureInPictureMode();
    }
    @Override
    public boolean enterPictureInPictureMode(PictureInPictureParams p0) {
        return super.enterPictureInPictureMode(p0);
    }
    @Override
    public void finish() {
        super.finish();
    }
    @Override
    public void finishActivity(int p0) {
        super.finishActivity(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void finishActivityFromChild(Activity p0, int p1) {
        super.finishActivityFromChild(p0, p1);
    }
    @Override
    public void finishAffinity() {
        super.finishAffinity();
    }
    @Override
    public void finishAfterTransition() {
        super.finishAfterTransition();
    }
    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void finishFromChild(Activity p0) {
        super.finishFromChild(p0);
    }
    @Override
    public android.app.ActionBar getActionBar() {
        return super.getActionBar();
    }
    @Override
    public ComponentName getCallingActivity() {
        return super.getCallingActivity();
    }
    @Override
    public String getCallingPackage() {
        return super.getCallingPackage();
    }
    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations();
    }
    @Override
    public ComponentName getComponentName() {
        return super.getComponentName();
    }
    @Override
    public Scene getContentScene() {
        return super.getContentScene();
    }
    @Override
    public TransitionManager getContentTransitionManager() {
        return super.getContentTransitionManager();
    }
    @Override
    public View getCurrentFocus() {
        return super.getCurrentFocus();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public android.app.FragmentManager getFragmentManager() {
        return super.getFragmentManager();
    }
    @Override
    public Intent getIntent() {
        return super.getIntent();
    }
    @Override
    public Object getLastNonConfigurationInstance() {
        return super.getLastNonConfigurationInstance();
    }
    @Override
    public LayoutInflater getLayoutInflater() {
        return super.getLayoutInflater();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public android.app.LoaderManager getLoaderManager() {
        return super.getLoaderManager();
    }
    @Override
    public String getLocalClassName() {
        return super.getLocalClassName();
    }
    @Override
    public int getMaxNumPictureInPictureActions() {
        return super.getMaxNumPictureInPictureActions();
    }
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent();
    }
    @Override
    public SharedPreferences getPreferences(int p0) {
        return super.getPreferences(p0);
    }
    @Override
    public Uri getReferrer() {
        return super.getReferrer();
    }
    @Override
    public int getRequestedOrientation() {
        return super.getRequestedOrientation();
    }
    @Override
    public Object getSystemService(String p0) {
        return super.getSystemService(p0);
    }
    @Override
    public int getTaskId() {
        return super.getTaskId();
    }
    @Override
    public VoiceInteractor getVoiceInteractor() {
        return super.getVoiceInteractor();
    }
    @Override
    public Window getWindow() {
        return super.getWindow();
    }
    @Override
    public WindowManager getWindowManager() {
        return super.getWindowManager();
    }
    @Override
    public boolean hasWindowFocus() {
        return super.hasWindowFocus();
    }
    @Override
    public boolean isActivityTransitionRunning() {
        return super.isActivityTransitionRunning();
    }
    @Override
    public boolean isChangingConfigurations() {
        return super.isChangingConfigurations();
    }
    @Override
    public boolean isDestroyed() {
        return super.isDestroyed();
    }
    @Override
    public boolean isFinishing() {
        return super.isFinishing();
    }
    @Override
    public boolean isImmersive() {
        return super.isImmersive();
    }
    @Override
    public boolean isInMultiWindowMode() {
        return super.isInMultiWindowMode();
    }
    @Override
    public boolean isInPictureInPictureMode() {
        return super.isInPictureInPictureMode();
    }
    @Override
    public boolean isLocalVoiceInteractionSupported() {
        return super.isLocalVoiceInteractionSupported();
    }
    @Override
    public boolean isTaskRoot() {
        return super.isTaskRoot();
    }
    @Override
    public boolean isVoiceInteraction() {
        return super.isVoiceInteraction();
    }
    @Override
    public boolean isVoiceInteractionRoot() {
        return super.isVoiceInteractionRoot();
    }
    @Override
    public boolean moveTaskToBack(boolean p0) {
        return super.moveTaskToBack(p0);
    }
    @Override
    public boolean navigateUpTo(Intent p0) {
        return super.navigateUpTo(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public boolean navigateUpToFromChild(Activity p0, Intent p1) {
        return super.navigateUpToFromChild(p0, p1);
    }
    @Override
    public void onActionModeFinished(android.view.ActionMode p0) {
        super.onActionModeFinished(p0);
    }
    @Override
    public void onActionModeStarted(android.view.ActionMode p0) {
        super.onActionModeStarted(p0);
    }
    @Override
    public void onActivityReenter(int p0, Intent p1) {
        super.onActivityReenter(p0, p1);
    }
    @Override
    protected void onApplyThemeResource(Theme p0, int p1, boolean p2) {
        super.onApplyThemeResource(p0, p1, p2);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void onAttachFragment(android.app.Fragment p0) {
        super.onAttachFragment(p0);
    }
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    @Override
    protected void onChildTitleChanged(Activity p0, CharSequence p1) {
        super.onChildTitleChanged(p0, p1);
    }
    @Override
    public boolean onContextItemSelected(MenuItem p0) {
        return super.onContextItemSelected(p0);
    }
    @Override
    public void onContextMenuClosed(Menu p0) {
        super.onContextMenuClosed(p0);
    }
    @Override
    public void onCreate(Bundle p0, PersistableBundle p1) {
        super.onCreate(p0, p1);
    }
    @Override
    public void onCreateContextMenu(ContextMenu p0, View p1, ContextMenuInfo p2) {
        super.onCreateContextMenu(p0, p1, p2);
    }
    @Override
    public CharSequence onCreateDescription() {
        return super.onCreateDescription();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int p0) {
        return super.onCreateDialog(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int p0, Bundle p1) {
        return super.onCreateDialog(p0, p1);
    }
    @Override
    public void onCreateNavigateUpTaskStack(android.app.TaskStackBuilder p0) {
        super.onCreateNavigateUpTaskStack(p0);
    }
    @Override
    public View onCreatePanelView(int p0) {
        return super.onCreatePanelView(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateThumbnail(Bitmap p0, Canvas p1) {
        return super.onCreateThumbnail(p0, p1);
    }
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
    }
    @Override
    public boolean onGenericMotionEvent(MotionEvent p0) {
        return super.onGenericMotionEvent(p0);
    }
    @Override
    public void onGetDirectActions(android.os.CancellationSignal p0, java.util.function.Consumer<java.util.List<android.app.DirectAction>> p1) {
        super.onGetDirectActions(p0, p1);
    }
    @Override
    public boolean onKeyLongPress(int p0, KeyEvent p1) {
        return super.onKeyLongPress(p0, p1);
    }
    @Override
    public boolean onKeyMultiple(int p0, int p1, KeyEvent p2) {
        return super.onKeyMultiple(p0, p1, p2);
    }
    @Override
    public boolean onKeyShortcut(int p0, KeyEvent p1) {
        return super.onKeyShortcut(p0, p1);
    }
    @Override
    public boolean onKeyUp(int p0, KeyEvent p1) {
        return super.onKeyUp(p0, p1);
    }
    @Override
    public void onLocalVoiceInteractionStarted() {
        super.onLocalVoiceInteractionStarted();
    }
    @Override
    public void onLocalVoiceInteractionStopped() {
        super.onLocalVoiceInteractionStopped();
    }
    @Override
    public void onMultiWindowModeChanged(boolean p0, Configuration p1) {
        super.onMultiWindowModeChanged(p0, p1);
    }
    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public boolean onNavigateUpFromChild(Activity p0) {
        return super.onNavigateUpFromChild(p0);
    }
    @Override
    public void onOptionsMenuClosed(Menu p0) {
        super.onOptionsMenuClosed(p0);
    }
    @Override
    public void onPerformDirectAction(String p0, Bundle p1, android.os.CancellationSignal p2, java.util.function.Consumer<Bundle> p3) {
        super.onPerformDirectAction(p0, p1, p2, p3);
    }
    @Override
    public void onPictureInPictureModeChanged(boolean p0, Configuration p1) {
        super.onPictureInPictureModeChanged(p0, p1);
    }
    @Override
    public boolean onPictureInPictureRequested() {
        return super.onPictureInPictureRequested();
    }
    @Override
    public void onPostCreate(Bundle p0, PersistableBundle p1) {
        super.onPostCreate(p0, p1);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int p0, Dialog p1) {
        super.onPrepareDialog(p0, p1);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int p0, Dialog p1, Bundle p2) {
        super.onPrepareDialog(p0, p1, p2);
    }
    @Override
    public void onPrepareNavigateUpTaskStack(android.app.TaskStackBuilder p0) {
        super.onPrepareNavigateUpTaskStack(p0);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu p0) {
        return super.onPrepareOptionsMenu(p0);
    }
    @Override
    public void onProvideAssistContent(AssistContent p0) {
        super.onProvideAssistContent(p0);
    }
    @Override
    public void onProvideAssistData(Bundle p0) {
        super.onProvideAssistData(p0);
    }
    @Override
    public void onProvideKeyboardShortcuts(java.util.List<android.view.KeyboardShortcutGroup> p0, Menu p1, int p2) {
        super.onProvideKeyboardShortcuts(p0, p1, p2);
    }
    @Override
    public Uri onProvideReferrer() {
        return super.onProvideReferrer();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    @Override
    protected void onRestoreInstanceState(Bundle p0) {
        super.onRestoreInstanceState(p0);
    }
    @Override
    public void onRestoreInstanceState(Bundle p0, PersistableBundle p1) {
        super.onRestoreInstanceState(p0, p1);
    }
    @Override
    public void onSaveInstanceState(Bundle p0, PersistableBundle p1) {
        super.onSaveInstanceState(p0, p1);
    }
    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }
    @Override
    public boolean onSearchRequested(SearchEvent p0) {
        return super.onSearchRequested(p0);
    }
    @Override
    public void onTopResumedActivityChanged(boolean p0) {
        super.onTopResumedActivityChanged(p0);
    }
    @Override
    public boolean onTouchEvent(MotionEvent p0) {
        return super.onTouchEvent(p0);
    }
    @Override
    public boolean onTrackballEvent(MotionEvent p0) {
        return super.onTrackballEvent(p0);
    }
    @Override
    public void onTrimMemory(int p0) {
        super.onTrimMemory(p0);
    }
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }
    @Override
    public void onWindowAttributesChanged(LayoutParams p0) {
        super.onWindowAttributesChanged(p0);
    }
    @Override
    public void onWindowFocusChanged(boolean p0) {
        super.onWindowFocusChanged(p0);
    }
    @Override
    public android.view.ActionMode onWindowStartingActionMode(Callback p0) {
        return super.onWindowStartingActionMode(p0);
    }
    @Override
    public android.view.ActionMode onWindowStartingActionMode(Callback p0, int p1) {
        return super.onWindowStartingActionMode(p0, p1);
    }
    @Override
    public void openContextMenu(View p0) {
        super.openContextMenu(p0);
    }
    @Override
    public void overridePendingTransition(int p0, int p1) {
        super.overridePendingTransition(p0, p1);
    }
    @Override
    public void postponeEnterTransition() {
        super.postponeEnterTransition();
    }
    @Override
    public void recreate() {
        super.recreate();
    }
    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks p0) {
        super.registerActivityLifecycleCallbacks(p0);
    }
    @Override
    public void registerForContextMenu(View p0) {
        super.registerForContextMenu(p0);
    }
    @Override
    public boolean releaseInstance() {
        return super.releaseInstance();
    }
    @Override
    public DragAndDropPermissions requestDragAndDropPermissions(DragEvent p0) {
        return super.requestDragAndDropPermissions(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public boolean requestVisibleBehind(boolean p0) {
        return super.requestVisibleBehind(p0);
    }
    @Override
    public void setActionBar(android.widget.Toolbar p0) {
        super.setActionBar(p0);
    }
    @Override
    public void setContentTransitionManager(TransitionManager p0) {
        super.setContentTransitionManager(p0);
    }
    @Override
    public void setEnterSharedElementCallback(android.app.SharedElementCallback p0) {
        super.setEnterSharedElementCallback(p0);
    }
    @Override
    public void setExitSharedElementCallback(android.app.SharedElementCallback p0) {
        super.setExitSharedElementCallback(p0);
    }
    @Override
    public void setFinishOnTouchOutside(boolean p0) {
        super.setFinishOnTouchOutside(p0);
    }
    @Override
    public void setImmersive(boolean p0) {
        super.setImmersive(p0);
    }
    @Override
    public void setInheritShowWhenLocked(boolean p0) {
        super.setInheritShowWhenLocked(p0);
    }
    @Override
    public void setIntent(Intent p0) {
        super.setIntent(p0);
    }
    @Override
    public void setLocusContext(LocusId p0, Bundle p1) {
        super.setLocusContext(p0, p1);
    }
    @Override
    public void setPictureInPictureParams(PictureInPictureParams p0) {
        super.setPictureInPictureParams(p0);
    }
    @Override
    public void setRequestedOrientation(int p0) {
        super.setRequestedOrientation(p0);
    }
    @Override
    public void setShowWhenLocked(boolean p0) {
        super.setShowWhenLocked(p0);
    }
    @Override
    public void setTaskDescription(TaskDescription p0) {
        super.setTaskDescription(p0);
    }
    @Override
    public void setTitle(int p0) {
        super.setTitle(p0);
    }
    @Override
    public void setTitle(CharSequence p0) {
        super.setTitle(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setTitleColor(int p0) {
        super.setTitleColor(p0);
    }
    @Override
    public boolean setTranslucent(boolean p0) {
        return super.setTranslucent(p0);
    }
    @Override
    public void setTurnScreenOn(boolean p0) {
        super.setTurnScreenOn(p0);
    }
    @Override
    public void setVisible(boolean p0) {
        super.setVisible(p0);
    }
    @Override
    public void setVrModeEnabled(boolean p0, ComponentName p1) throws android.content.pm.PackageManager.NameNotFoundException {
        super.setVrModeEnabled(p0, p1);
    }
    @Override
    public boolean shouldShowRequestPermissionRationale(String p0) {
        return super.shouldShowRequestPermissionRationale(p0);
    }
    @Override
    public boolean shouldUpRecreateTask(Intent p0) {
        return super.shouldUpRecreateTask(p0);
    }
    @Override
    public boolean showAssist(Bundle p0) {
        return super.showAssist(p0);
    }
    @Override
    public void showLockTaskEscapeMessage() {
        super.showLockTaskEscapeMessage();
    }
    @Override
    public android.view.ActionMode startActionMode(Callback p0) {
        return super.startActionMode(p0);
    }
    @Override
    public android.view.ActionMode startActionMode(Callback p0, int p1) {
        return super.startActionMode(p0, p1);
    }
    @Override
    public void startActivities(Intent[] p0) {
        super.startActivities(p0);
    }
    @Override
    public void startActivities(Intent[] p0, Bundle p1) {
        super.startActivities(p0, p1);
    }
    @Override
    public void startActivity(Intent p0) {
        super.startActivity(p0);
    }
    @Override
    public void startActivity(Intent p0, Bundle p1) {
        super.startActivity(p0, p1);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startActivityFromChild(Activity p0, Intent p1, int p2) {
        super.startActivityFromChild(p0, p1, p2);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startActivityFromChild(Activity p0, Intent p1, int p2, Bundle p3) {
        super.startActivityFromChild(p0, p1, p2, p3);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startActivityFromFragment(android.app.Fragment p0, Intent p1, int p2) {
        super.startActivityFromFragment(p0, p1, p2);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startActivityFromFragment(android.app.Fragment p0, Intent p1, int p2, Bundle p3) {
        super.startActivityFromFragment(p0, p1, p2, p3);
    }
    @Override
    public boolean startActivityIfNeeded(Intent p0, int p1) {
        return super.startActivityIfNeeded(p0, p1);
    }
    @Override
    public boolean startActivityIfNeeded(Intent p0, int p1, Bundle p2) {
        return super.startActivityIfNeeded(p0, p1, p2);
    }
    @Override
    public void startIntentSender(IntentSender p0, Intent p1, int p2, int p3, int p4) throws android.content.IntentSender.SendIntentException {
        super.startIntentSender(p0, p1, p2, p3, p4);
    }
    @Override
    public void startIntentSender(IntentSender p0, Intent p1, int p2, int p3, int p4, Bundle p5) throws android.content.IntentSender.SendIntentException {
        super.startIntentSender(p0, p1, p2, p3, p4, p5);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startIntentSenderFromChild(Activity p0, IntentSender p1, int p2, Intent p3, int p4, int p5, int p6) throws android.content.IntentSender.SendIntentException {
        super.startIntentSenderFromChild(p0, p1, p2, p3, p4, p5, p6);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startIntentSenderFromChild(Activity p0, IntentSender p1, int p2, Intent p3, int p4, int p5, int p6, Bundle p7) throws android.content.IntentSender.SendIntentException {
        super.startIntentSenderFromChild(p0, p1, p2, p3, p4, p5, p6, p7);
    }
    @Override
    public void startLocalVoiceInteraction(Bundle p0) {
        super.startLocalVoiceInteraction(p0);
    }
    @Override
    public void startLockTask() {
        super.startLockTask();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void startManagingCursor(Cursor p0) {
        super.startManagingCursor(p0);
    }
    @Override
    public boolean startNextMatchingActivity(Intent p0) {
        return super.startNextMatchingActivity(p0);
    }
    @Override
    public boolean startNextMatchingActivity(Intent p0, Bundle p1) {
        return super.startNextMatchingActivity(p0, p1);
    }
    @Override
    public void startPostponedEnterTransition() {
        super.startPostponedEnterTransition();
    }
    @Override
    public void startSearch(String p0, boolean p1, Bundle p2, boolean p3) {
        super.startSearch(p0, p1, p2, p3);
    }
    @Override
    public void stopLocalVoiceInteraction() {
        super.stopLocalVoiceInteraction();
    }
    @Override
    public void stopLockTask() {
        super.stopLockTask();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void stopManagingCursor(Cursor p0) {
        super.stopManagingCursor(p0);
    }
    @Override
    public void takeKeyEvents(boolean p0) {
        super.takeKeyEvents(p0);
    }
    @Override
    public void triggerSearch(String p0, Bundle p1) {
        super.triggerSearch(p0, p1);
    }
    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks p0) {
        super.unregisterActivityLifecycleCallbacks(p0);
    }
    @Override
    public void unregisterForContextMenu(View p0) {
        super.unregisterForContextMenu(p0);
    }

    // -----------------------------------------------------------------------------------------
    // ContextThemeWrapper  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public void applyOverrideConfiguration(Configuration p0) {
        super.applyOverrideConfiguration(p0);
    }
    @Override
    public AssetManager getAssets() {
        return super.getAssets();
    }
    @Override
    public Theme getTheme() {
        return super.getTheme();
    }
    @Override
    public void setTheme(Theme p0) {
        super.setTheme(p0);
    }

    // -----------------------------------------------------------------------------------------
    // ContextWrapper  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public boolean bindIsolatedService(Intent p0, int p1, String p2, Executor p3, ServiceConnection p4) {
        return super.bindIsolatedService(p0, p1, p2, p3, p4);
    }
    @Override
    public boolean bindService(Intent p0, int p1, Executor p2, ServiceConnection p3) {
        return super.bindService(p0, p1, p2, p3);
    }
    @Override
    public boolean bindService(Intent p0, ServiceConnection p1, int p2) {
        return super.bindService(p0, p1, p2);
    }
    @Override
    public boolean bindServiceAsUser(Intent p0, ServiceConnection p1, int p2, UserHandle p3) {
        return super.bindServiceAsUser(p0, p1, p2, p3);
    }
    @Override
    public int checkCallingOrSelfPermission(String p0) {
        return super.checkCallingOrSelfPermission(p0);
    }
    @Override
    public int checkCallingOrSelfUriPermission(Uri p0, int p1) {
        return super.checkCallingOrSelfUriPermission(p0, p1);
    }
    @Override
    public int checkCallingPermission(String p0) {
        return super.checkCallingPermission(p0);
    }
    @Override
    public int checkCallingUriPermission(Uri p0, int p1) {
        return super.checkCallingUriPermission(p0, p1);
    }
    @Override
    public int checkPermission(String p0, int p1, int p2) {
        return super.checkPermission(p0, p1, p2);
    }
    @Override
    public int checkSelfPermission(String p0) {
        return super.checkSelfPermission(p0);
    }
    @Override
    public int checkUriPermission(Uri p0, int p1, int p2, int p3) {
        return super.checkUriPermission(p0, p1, p2, p3);
    }
    @Override
    public int checkUriPermission(Uri p0, String p1, String p2, int p3, int p4, int p5) {
        return super.checkUriPermission(p0, p1, p2, p3, p4, p5);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void clearWallpaper() throws java.io.IOException {
        super.clearWallpaper();
    }
    @Override
    public Context createAttributionContext(String p0) {
        return super.createAttributionContext(p0);
    }
    @Override
    public Context createConfigurationContext(Configuration p0) {
        return super.createConfigurationContext(p0);
    }
    @Override
    public Context createContextForSplit(String p0) throws android.content.pm.PackageManager.NameNotFoundException {
        return super.createContextForSplit(p0);
    }
    @Override
    public Context createDeviceProtectedStorageContext() {
        return super.createDeviceProtectedStorageContext();
    }
    @Override
    public Context createDisplayContext(Display p0) {
        return super.createDisplayContext(p0);
    }
    @Override
    public Context createPackageContext(String p0, int p1) throws android.content.pm.PackageManager.NameNotFoundException {
        return super.createPackageContext(p0, p1);
    }
    @Override
    public Context createWindowContext(int p0, Bundle p1) {
        return super.createWindowContext(p0, p1);
    }
    @Override
    public String[] databaseList() {
        return super.databaseList();
    }
    @Override
    public boolean deleteDatabase(String p0) {
        return super.deleteDatabase(p0);
    }
    @Override
    public boolean deleteFile(String p0) {
        return super.deleteFile(p0);
    }
    @Override
    public boolean deleteSharedPreferences(String p0) {
        return super.deleteSharedPreferences(p0);
    }
    @Override
    public void enforceCallingOrSelfPermission(String p0, String p1) {
        super.enforceCallingOrSelfPermission(p0, p1);
    }
    @Override
    public void enforceCallingOrSelfUriPermission(Uri p0, int p1, String p2) {
        super.enforceCallingOrSelfUriPermission(p0, p1, p2);
    }
    @Override
    public void enforceCallingPermission(String p0, String p1) {
        super.enforceCallingPermission(p0, p1);
    }
    @Override
    public void enforceCallingUriPermission(Uri p0, int p1, String p2) {
        super.enforceCallingUriPermission(p0, p1, p2);
    }
    @Override
    public void enforcePermission(String p0, int p1, int p2, String p3) {
        super.enforcePermission(p0, p1, p2, p3);
    }
    @Override
    public void enforceUriPermission(Uri p0, int p1, int p2, int p3, String p4) {
        super.enforceUriPermission(p0, p1, p2, p3, p4);
    }
    @Override
    public void enforceUriPermission(Uri p0, String p1, String p2, int p3, int p4, int p5, String p6) {
        super.enforceUriPermission(p0, p1, p2, p3, p4, p5, p6);
    }
    @Override
    public String[] fileList() {
        return super.fileList();
    }
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
    @Override
    public ApplicationInfo getApplicationInfo() {
        return super.getApplicationInfo();
    }
    @Override
    public String getAttributionTag() {
        return super.getAttributionTag();
    }
    @Override
    public Context getBaseContext() {
        return super.getBaseContext();
    }
    @Override
    public File getCacheDir() {
        return super.getCacheDir();
    }
    @Override
    public ClassLoader getClassLoader() {
        return super.getClassLoader();
    }
    @Override
    public File getCodeCacheDir() {
        return super.getCodeCacheDir();
    }
    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }
    @Override
    public File getDataDir() {
        return super.getDataDir();
    }
    @Override
    public File getDatabasePath(String p0) {
        return super.getDatabasePath(p0);
    }
    @Override
    public File getDir(String p0, int p1) {
        return super.getDir(p0, p1);
    }
    @Override
    public Display getDisplay() {
        return super.getDisplay();
    }
    @Override
    public File getExternalCacheDir() {
        return super.getExternalCacheDir();
    }
    @Override
    public File[] getExternalCacheDirs() {
        return super.getExternalCacheDirs();
    }
    @Override
    public File getExternalFilesDir(String p0) {
        return super.getExternalFilesDir(p0);
    }
    @Override
    public File[] getExternalFilesDirs(String p0) {
        return super.getExternalFilesDirs(p0);
    }
    @Override
    public File[] getExternalMediaDirs() {
        return super.getExternalMediaDirs();
    }
    @Override
    public File getFileStreamPath(String p0) {
        return super.getFileStreamPath(p0);
    }
    @Override
    public File getFilesDir() {
        return super.getFilesDir();
    }
    @Override
    public Executor getMainExecutor() {
        return super.getMainExecutor();
    }
    @Override
    public Looper getMainLooper() {
        return super.getMainLooper();
    }
    @Override
    public File getNoBackupFilesDir() {
        return super.getNoBackupFilesDir();
    }
    @Override
    public File getObbDir() {
        return super.getObbDir();
    }
    @Override
    public File[] getObbDirs() {
        return super.getObbDirs();
    }
    @Override
    public String getOpPackageName() {
        return super.getOpPackageName();
    }
    @Override
    public String getPackageCodePath() {
        return super.getPackageCodePath();
    }
    @Override
    public PackageManager getPackageManager() {
        return super.getPackageManager();
    }
    @Override
    public String getPackageName() {
        return super.getPackageName();
    }
    @Override
    public String getPackageResourcePath() {
        return super.getPackageResourcePath();
    }
    @Override
    public SharedPreferences getSharedPreferences(String p0, int p1) {
        return super.getSharedPreferences(p0, p1);
    }
    @Override
    public String getSystemServiceName(Class<?> p0) {
        return super.getSystemServiceName(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public Drawable getWallpaper() {
        return super.getWallpaper();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return super.getWallpaperDesiredMinimumHeight();
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return super.getWallpaperDesiredMinimumWidth();
    }
    @Override
    public void grantUriPermission(String p0, Uri p1, int p2) {
        super.grantUriPermission(p0, p1, p2);
    }
    @Override
    public boolean isDeviceProtectedStorage() {
        return super.isDeviceProtectedStorage();
    }
    @Override
    public boolean isRestricted() {
        return super.isRestricted();
    }
    @Override
    public boolean moveDatabaseFrom(Context p0, String p1) {
        return super.moveDatabaseFrom(p0, p1);
    }
    @Override
    public boolean moveSharedPreferencesFrom(Context p0, String p1) {
        return super.moveSharedPreferencesFrom(p0, p1);
    }
    @Override
    public FileInputStream openFileInput(String p0) throws java.io.FileNotFoundException {
        return super.openFileInput(p0);
    }
    @Override
    public FileOutputStream openFileOutput(String p0, int p1) throws java.io.FileNotFoundException {
        return super.openFileOutput(p0, p1);
    }
    @Override
    public SQLiteDatabase openOrCreateDatabase(String p0, int p1, CursorFactory p2) {
        return super.openOrCreateDatabase(p0, p1, p2);
    }
    @Override
    public SQLiteDatabase openOrCreateDatabase(String p0, int p1, CursorFactory p2, DatabaseErrorHandler p3) {
        return super.openOrCreateDatabase(p0, p1, p2, p3);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public Drawable peekWallpaper() {
        return super.peekWallpaper();
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver p0, IntentFilter p1) {
        return super.registerReceiver(p0, p1);
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver p0, IntentFilter p1, int p2) {
        return super.registerReceiver(p0, p1, p2);
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver p0, IntentFilter p1, String p2, Handler p3) {
        return super.registerReceiver(p0, p1, p2, p3);
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver p0, IntentFilter p1, String p2, Handler p3, int p4) {
        return super.registerReceiver(p0, p1, p2, p3, p4);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void removeStickyBroadcast(Intent p0) {
        super.removeStickyBroadcast(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void removeStickyBroadcastAsUser(Intent p0, UserHandle p1) {
        super.removeStickyBroadcastAsUser(p0, p1);
    }
    @Override
    public void revokeUriPermission(Uri p0, int p1) {
        super.revokeUriPermission(p0, p1);
    }
    @Override
    public void revokeUriPermission(String p0, Uri p1, int p2) {
        super.revokeUriPermission(p0, p1, p2);
    }
    @Override
    public void sendBroadcast(Intent p0) {
        super.sendBroadcast(p0);
    }
    @Override
    public void sendBroadcast(Intent p0, String p1) {
        super.sendBroadcast(p0, p1);
    }
    @Override
    public void sendBroadcastAsUser(Intent p0, UserHandle p1) {
        super.sendBroadcastAsUser(p0, p1);
    }
    @Override
    public void sendBroadcastAsUser(Intent p0, UserHandle p1, String p2) {
        super.sendBroadcastAsUser(p0, p1, p2);
    }
    @Override
    public void sendOrderedBroadcast(Intent p0, int p1, String p2, String p3, BroadcastReceiver p4, Handler p5, String p6, Bundle p7, Bundle p8) {
        super.sendOrderedBroadcast(p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }
    @Override
    public void sendOrderedBroadcast(Intent p0, String p1) {
        super.sendOrderedBroadcast(p0, p1);
    }
    @Override
    public void sendOrderedBroadcast(Intent p0, String p1, BroadcastReceiver p2, Handler p3, int p4, String p5, Bundle p6) {
        super.sendOrderedBroadcast(p0, p1, p2, p3, p4, p5, p6);
    }
    @Override
    public void sendOrderedBroadcast(Intent p0, String p1, String p2, BroadcastReceiver p3, Handler p4, int p5, String p6, Bundle p7) {
        super.sendOrderedBroadcast(p0, p1, p2, p3, p4, p5, p6, p7);
    }
    @Override
    public void sendOrderedBroadcastAsUser(Intent p0, UserHandle p1, String p2, BroadcastReceiver p3, Handler p4, int p5, String p6, Bundle p7) {
        super.sendOrderedBroadcastAsUser(p0, p1, p2, p3, p4, p5, p6, p7);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void sendStickyBroadcast(Intent p0) {
        super.sendStickyBroadcast(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void sendStickyBroadcastAsUser(Intent p0, UserHandle p1) {
        super.sendStickyBroadcastAsUser(p0, p1);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void sendStickyOrderedBroadcast(Intent p0, BroadcastReceiver p1, Handler p2, int p3, String p4, Bundle p5) {
        super.sendStickyOrderedBroadcast(p0, p1, p2, p3, p4, p5);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent p0, UserHandle p1, BroadcastReceiver p2, Handler p3, int p4, String p5, Bundle p6) {
        super.sendStickyOrderedBroadcastAsUser(p0, p1, p2, p3, p4, p5, p6);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setWallpaper(Bitmap p0) throws java.io.IOException {
        super.setWallpaper(p0);
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public void setWallpaper(InputStream p0) throws java.io.IOException {
        super.setWallpaper(p0);
    }
    @Override
    public ComponentName startForegroundService(Intent p0) {
        return super.startForegroundService(p0);
    }
    @Override
    public boolean startInstrumentation(ComponentName p0, String p1, Bundle p2) {
        return super.startInstrumentation(p0, p1, p2);
    }
    @Override
    public ComponentName startService(Intent p0) {
        return super.startService(p0);
    }
    @Override
    public boolean stopService(Intent p0) {
        return super.stopService(p0);
    }
    @Override
    public void unbindService(ServiceConnection p0) {
        super.unbindService(p0);
    }
    @Override
    public void unregisterReceiver(BroadcastReceiver p0) {
        super.unregisterReceiver(p0);
    }
    @Override
    public void updateServiceGroup(ServiceConnection p0, int p1, int p2) {
        super.updateServiceGroup(p0, p1, p2);
    }

    // -----------------------------------------------------------------------------------------
    // Context  (public/protected forwards)
    // -----------------------------------------------------------------------------------------
    @Override
    public void registerComponentCallbacks(ComponentCallbacks p0) {
        super.registerComponentCallbacks(p0);
    }
    @Override
    public void sendBroadcastWithMultiplePermissions(Intent p0, String[] p1) {
        super.sendBroadcastWithMultiplePermissions(p0, p1);
    }
    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks p0) {
        super.unregisterComponentCallbacks(p0);
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
