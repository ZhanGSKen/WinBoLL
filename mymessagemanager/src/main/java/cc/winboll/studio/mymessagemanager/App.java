package cc.winboll.studio.mymessagemanager;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2023/07/24 01:46:59
 * @Describe 全局应用类
 */
import android.view.Gravity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.mymessagemanager.R;
import com.hjq.toast.ToastUtils;
import java.io.File;

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

        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        ToastUtils.setView(R.layout.toast_custom_view);
        //ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

        //LogUtils.d(TAG, "BuildConfig.DEBUG " + Boolean.toString(BuildConfig.DEBUG));

        _mszAppExternalFilesDir = getExternalFilesDir(TAG).toString();
        _mszConfigUtilPath = _mszAppExternalFilesDir + File.separator + _mszConfigUtilFileName;
        _mszSMSReceiveRuleUtilPath = _mszAppExternalFilesDir + File.separator + _mszSMSReceiveRuleUtilFileName;
    }
}
