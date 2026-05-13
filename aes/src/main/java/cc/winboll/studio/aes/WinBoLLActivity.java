package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/29 00:11
 * @Describe WinBoLL 窗口基础类
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;

public class WinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {

    public static final String TAG = "WinBoLLActivity";

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, String.format("onResume %s", getTag()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.item_log) {
            WinBoLLActivityManager.getInstance().startLogActivity(this);
            return true;
        } else if (item.getItemId() == R.id.item_home) {
			startActivity(new Intent(this, MainActivity.class));
            return true;
        }*/
        // 在switch语句中处理每个ID，并在处理完后返回true，未处理的情况返回false。
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        WinBoLLActivityManager.getInstance().add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WinBoLLActivityManager.getInstance().finish(this);
    }
}
