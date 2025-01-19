package cc.winboll.studio.powerbell;

import android.content.Context;
import android.view.Gravity;
import cc.winboll.studio.powerbell.receivers.GlobalApplicationReceiver;
import cc.winboll.studio.powerbell.utils.AppCacheUtils;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.shared.app.WinBollApplication;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;

public class GlobalApplication extends WinBollApplication {

    public static final String TAG = "GlobalApplication";

    // 数据配置存储工具
    static AppConfigUtils _mAppConfigUtils;
    static AppCacheUtils _mAppCacheUtils;
    GlobalApplicationReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.toast_custom_view);
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

        // 设置数据配置存储工具
        _mAppConfigUtils = getAppConfigUtils(this);
        _mAppCacheUtils = getAppCacheUtils(this);
        
        mReceiver = new GlobalApplicationReceiver(this);
        mReceiver.registerAction();
    }

    public static AppConfigUtils getAppConfigUtils(Context context) {
        if (_mAppConfigUtils == null) {
            _mAppConfigUtils = AppConfigUtils.getInstance(context);
        }
        return _mAppConfigUtils;
    }

    public static AppCacheUtils getAppCacheUtils(Context context) {
        if (_mAppCacheUtils == null) {
            _mAppCacheUtils = AppCacheUtils.getInstance(context);
        }
        return _mAppCacheUtils;
    }

    public void clearBatteryHistory() {
        _mAppCacheUtils.clearBatteryHistory();
    }
}

