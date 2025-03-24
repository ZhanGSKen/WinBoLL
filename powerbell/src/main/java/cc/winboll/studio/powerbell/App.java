package cc.winboll.studio.powerbell;

import android.content.Context;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.powerbell.receivers.GlobalApplicationReceiver;
import cc.winboll.studio.powerbell.utils.AppCacheUtils;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.libapputils.bean.DebugBean;

public class App extends GlobalApplication {

    public static final String TAG = "GlobalApplication";

    // 数据配置存储工具
    static AppConfigUtils _mAppConfigUtils;
    static AppCacheUtils _mAppCacheUtils;
    GlobalApplicationReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置应用调试标志
        DebugBean debugBean = DebugBean.loadBean(this, DebugBean.class);
        if (debugBean == null) {
            //ToastUtils.show("debugBean == null");
            setIsDebuging(this, false);
        } else {
            //ToastUtils.show("saveDebugStatus(" + String.valueOf(debugBean.isDebuging()) + ")");
            setIsDebuging(this, debugBean.isDebuging());
        }
        
        // 初始化 Toast 框架
        //ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.toast_custom_view);
        //ToastUtils.setStyle(new WhiteToastStyle());
        //ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

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

