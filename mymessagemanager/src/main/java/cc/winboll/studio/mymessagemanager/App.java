package cc.winboll.studio.mymessagemanager;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2023/07/24 01:46:59
 * @Describe 全局应用类
 */
import android.view.Gravity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.mymessagemanager.R;
import java.io.File;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;

public class App extends GlobalApplication {

    public static final String TAG = "GlobalApplication";

    static String _mszAppExternalFilesDir;
    static String _mszConfigUtilFileName = "ConfigUtil.json";
    static String _mszConfigUtilPath;
    static String _mszSMSReceiveRuleUtilFileName = "SMSReceiveRuleUtil.json";
    static String _mszSMSReceiveRuleUtilPath;

    public static final int USER_ID = -1;
    Long mszVersionName = 1L;
    Long mszDataVersionName = 1L;


    @Override
    public void onCreate() {
        super.onCreate();
		setIsDebugging(BuildConfig.DEBUG);
		//setIsDebugging(false);

		// 初始化窗口管理类
		WinBoLLActivityManager.init(this);

        // 初始化 Toast 框架
        ToastUtils.init(this);

        _mszAppExternalFilesDir = getExternalFilesDir(TAG).toString();
        _mszConfigUtilPath = _mszAppExternalFilesDir + File.separator + _mszConfigUtilFileName;
        _mszSMSReceiveRuleUtilPath = _mszAppExternalFilesDir + File.separator + _mszSMSReceiveRuleUtilFileName;
    }

	@Override
	public void onTerminate() {
		super.onTerminate();
		ToastUtils.release();
	}
}
