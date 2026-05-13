package cc.winboll.studio.positions.utils;
import android.content.Context;
import cc.winboll.studio.positions.models.AppConfigsModel;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/10/01 12:15
 * @Describe AppConfigsUtils
 */
public class AppConfigsUtil {

    public static final String TAG = "AppConfigsUtils";

    // 1. 私有静态成员变量（volatile 防止指令重排，确保实例初始化完全）
    private static volatile AppConfigsUtil sInstance;
	Context mContext;
	AppConfigsModel mAppConfigsModel;

    // 2. 私有构造方法（禁止外部 new 实例）
    private AppConfigsUtil(Context context) {
        // 可选：防止通过反射创建实例（增强单例安全性）
        if (sInstance != null) {
            throw new RuntimeException("禁止通过反射创建单例实例");
        }
		mContext = context;
		loadConfigs();
    }

    // 3. 公开静态方法（双重校验锁，获取唯一实例）
    public static AppConfigsUtil getInstance(Context context) {
        // 第一次校验：无锁，快速判断实例是否存在（提升性能）
        if (sInstance == null) {
            // 加锁：确保同一时间只有一个线程进入初始化逻辑
            synchronized (AppConfigsUtil.class) {
                // 第二次校验：防止多线程并发时重复创建实例（线程安全）
                if (sInstance == null) {
                    sInstance = new AppConfigsUtil(context);
                }
            }
        }
        return sInstance;
    }

	public void loadConfigs() {
		mAppConfigsModel = AppConfigsModel.loadBean(mContext, AppConfigsModel.class);
	}

	public void saveConfigs() {
		AppConfigsModel.saveBean(mContext, mAppConfigsModel);
	}

	public boolean isEnableMainService(boolean isReloadConfigs) {
		if (isReloadConfigs) {
			loadConfigs();
		}

		return (mAppConfigsModel == null) ?false: mAppConfigsModel.isEnableMainService();
	}

	public void setIsEnableMainService(boolean isEnableMainService) {
		if(mAppConfigsModel == null) {
			mAppConfigsModel = new AppConfigsModel();
		}
		mAppConfigsModel.setIsEnableMainService(isEnableMainService);
		saveConfigs();
	}
}

