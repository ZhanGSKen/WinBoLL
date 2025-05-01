package cc.winboll.studio.appbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 15:34:16
 * @Describe 应用活动窗口基类
 */
import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import cc.winboll.studio.appbase.App;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;
import cc.winboll.studio.libappbase.winboll.WinBoLLActivityManager;

public class WinBoLLActivityBase extends Activity implements IWinBoLLActivity {

    public static final String TAG = "WinBoLLActivityBase";

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    WinBoLLActivityManager getWinBoLLActivityManager() {
        return WinBoLLActivityManager.getInstance(GlobalApplication.getInstance());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWinBoLLActivityManager().add(this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == cc.winboll.studio.appbase.R.id.item_log) {
            GlobalApplication.getWinBoLLActivityManager().startLogActivity(this);
            return true;
        } else if(item.getItemId() == cc.winboll.studio.appbase.R.id.item_minimal) {
            //moveTaskToBack(true);
            exit();
        }
        // 在switch语句中处理每个ID，并在处理完后返回true，未处理的情况返回false。
        return super.onOptionsItemSelected(item);
    }

    void exit() {
        YesNoAlertDialog.show(this, "Exit " + getString(R.string.app_name), "Close all activity and exit?", new YesNoAlertDialog.OnDialogResultListener(){

                @Override
                public void onYes() {
                    App.getWinBoLLActivityManager().finishAll();
                }

                @Override
                public void onNo() {
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWinBoLLActivityManager().registeRemove(this);
    }
}
