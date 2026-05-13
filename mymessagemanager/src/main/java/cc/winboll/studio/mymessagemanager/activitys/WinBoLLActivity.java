package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/31 01:31:17
 * @Describe 应用活动窗口基类
 */
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.mymessagemanager.enums.ThemeStyleEnum;

public class WinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {

    public static final String TAG = "WinBoLLActivity";

	IOnActivityMessageReceived mIOnActivityMessageReceived;

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		// 1. 优先读取SP中保存的主题（必须在setContentView前调用！）
		ThemeStyleEnum savedTheme = ThemeStyleEnum.getThemeFromSP(this);
		// 2. 设置主题
		setTheme(savedTheme.getStyleId());
        super.onCreate(savedInstanceState);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selectedMenuId = item.getItemId();
		// 1. 根据菜单ID获取对应的主题枚举
		ThemeStyleEnum selectedTheme = ThemeStyleEnum.getThemeByMenuId(selectedMenuId);

		if (selectedTheme != null) {
			// 2. 调用枚举自带方法保存主题到SP（替代AESThemeUtil）
			ThemeStyleEnum.saveThemeToSP(this, selectedTheme);
			recreate(); // 重建Activity生效主题
		} else if (selectedMenuId == android.R.id.home) {
			finish();
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	protected interface IOnActivityMessageReceived {
        void onActivityMessageReceived(Message msg);
    }

	public void sendActivityMessage(Message msg) {
        mHandler.sendMessage(msg);
    }

    protected void setOnActivityMessageReceived(IOnActivityMessageReceived iOnActivityMessageReceived) {
        mIOnActivityMessageReceived = iOnActivityMessageReceived;
    }

    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mIOnActivityMessageReceived != null) {
                mIOnActivityMessageReceived.onActivityMessageReceived(msg);
            }
        }
    };
}
