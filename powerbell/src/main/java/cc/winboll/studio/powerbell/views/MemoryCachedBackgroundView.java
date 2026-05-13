package cc.winboll.studio.powerbell.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.AttributeSet;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;

/**
 * 单实例缓存版背景视图控件（基于Java7）- 强制缓存版
 * 核心：通过静态属性保存当前缓存路径和实例，支持强制重载图片
 * 新增：SP持久化最后加载路径、获取最后加载实例功能
 * 强制缓存策略：无论内存是否紧张，不自动清理任何缓存实例和路径记录
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class MemoryCachedBackgroundView extends BackgroundView {
    // ====================================== 静态常量区（TAG + SP相关常量） ======================================
    public static final String TAG = "MemoryCachedBackgroundView";
    // SP相关常量（持久化最后加载路径）
    private static final String SP_NAME = "MemoryCachedBackgroundView_SP";
    private static final String KEY_LAST_LOAD_IMAGE_PATH = "last_load_image_path";

    // ====================================== 静态属性区（强制缓存核心：保存实例、路径、实例计数） ======================================
    // 静态属性：保存当前缓存的路径和实例（强制保持，不自动销毁）
    private static String sCachedImagePath;
    private static MemoryCachedBackgroundView sCachedView;
    // 新增：记录所有创建过的实例数量（用于强制缓存监控）
    private static int sInstanceCount = 0;

    // ====================================== 构造器（继承并兼容父类，私有构造防止外部实例化） ======================================
    private MemoryCachedBackgroundView(Context context) {
        super(context);
        sInstanceCount++;
        LogUtils.d(TAG, String.format("构造器1启动 | 创建实例，当前实例总数=%d", sInstanceCount));
    }

    private MemoryCachedBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sInstanceCount++;
        LogUtils.d(TAG, String.format("构造器2启动 | 创建实例，当前实例总数=%d", sInstanceCount));
    }

    private MemoryCachedBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sInstanceCount++;
        LogUtils.d(TAG, String.format("构造器3启动 | 创建实例，当前实例总数=%d", sInstanceCount));
    }

    // ====================================== 核心静态方法：获取/创建缓存实例（强制缓存版） ======================================
    /**
     * 从缓存获取或创建MemoryCachedBackgroundView实例（强制保持旧实例）
     * @param context 上下文
     * @param imagePath 图片绝对路径（作为缓存标识）
     * @param isReload 是否强制重新加载图片（路径匹配时仍刷新）
     * @return 缓存/新创建的MemoryCachedBackgroundView实例
     */
    public static MemoryCachedBackgroundView getInstance(Context context, BackgroundBean bean,  boolean isReload) {
        LogUtils.d(TAG, String.format("getInstance 调用 | BackgroundBean=%s | 是否重载=%b", bean.toString(), isReload));
		//App.notifyMessage(TAG, String.format("getInstance 调用 | BackgroundBean=%s | 是否重载=%b", bean.toString(), isReload));
        sCachedView = new MemoryCachedBackgroundView(context);
		sCachedView.loadByBackgroundBean(bean, isReload);
		saveLastLoadImagePath(context, getBackgroundBeanImagePath(bean));
        LogUtils.d(TAG, String.format("getInstance: 已更新当前缓存实例，旧实例路径=%s（强制保持）", getBackgroundBeanImagePath(bean)));
		//App.notifyMessage(TAG, String.format("getInstance: 已更新当前缓存实例，旧实例路径=%s（强制保持）", getBackgroundBeanImagePath(bean)));
        return sCachedView;
    }

	static String getBackgroundBeanImagePath(BackgroundBean bean) {
		if (bean.isUseBackgroundFile()) {
			if (bean.isUseBackgroundScaledCompressFile()) {
				return bean.getBackgroundScaledCompressFilePath();
			}
			return bean.getBackgroundFilePath();
		}
		return "";
	}

    // ====================================== 新增功能：获取最后加载的实例（强制缓存版） ======================================
    /**
     * 获取最后一次loadImage的路径对应的实例（强制保持所有实例）
     * 无实例则创建并加载图片，同时更新静态缓存
     * @param context 上下文
     * @return 最后加载路径对应的实例
     */
    public static MemoryCachedBackgroundView getLastInstance(Context context) {
        LogUtils.d(TAG, "getLastInstance 调用");
		//App.notifyMessage(TAG, "getLastInstance 调用");
        // 1. 从SP获取最后加载的路径（强制保持，不自动删除）
        sCachedImagePath = getLastLoadImagePath(context);
		String lastPath = getBackgroundBeanImagePath(App.sBackgroundSourceUtils.getCurrentBackgroundBean());
		//App.notifyMessage(TAG, String.format("sCachedImagePath : %s", sCachedImagePath));
        //App.notifyMessage(TAG, String.format("lastPath : %s", lastPath));
        if (lastPath.equals(sCachedImagePath) && sCachedView != null) {
            LogUtils.d(TAG, String.format("getLastInstance: 使用最后路径缓存实例 | 路径=%s", lastPath));
			//App.notifyMessage(TAG, String.format("getLastInstance: 使用最后路径缓存实例 | 路径=%s", lastPath));
            return sCachedView;
        }
		//App.notifyMessage(TAG, "getLastInstance 返回 null");
        return null;
    }

    // ====================================== 工具方法：SP持久化最后加载路径（强制保持版） ======================================
    /**
     * 保存最后一次loadImage的路径到SP（强制保持，不自动删除）
     * @param context 上下文
     * @param imagePath 图片路径
     */
    private static void saveLastLoadImagePath(Context context, String imagePath) {
        if (TextUtils.isEmpty(imagePath) || context == null) {
            LogUtils.w(TAG, "saveLastLoadImagePath: 路径或上下文为空，跳过保存");
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_LAST_LOAD_IMAGE_PATH, imagePath).apply();
        LogUtils.d(TAG, String.format("saveLastLoadImagePath: 已保存最后路径（强制保持） | 路径=%s", imagePath));
    }

    /**
     * 从SP获取最后一次loadImage的路径（强制保持，不自动删除）
     * @param context 上下文
     * @return 最后加载的图片路径，空则返回null
     */
    public static String getLastLoadImagePath(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "getLastLoadImagePath: 上下文为空，返回null");
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String lastPath = sp.getString(KEY_LAST_LOAD_IMAGE_PATH, null);
        LogUtils.d(TAG, String.format("getLastLoadImagePath: 获取最后路径（强制保持） | 路径=%s", lastPath));
        return lastPath;
    }

    // ====================================== 工具方法：缓存管理（强制缓存版 - 仅日志，不清理） ======================================
    /**
     * 清除当前缓存实例和路径（强制缓存策略：仅日志，不实际清理）
     */
    public static void clearCache() {
        LogUtils.w(TAG, String.format("clearCache 调用（强制缓存策略：不实际清理缓存） | 当前缓存路径=%s", sCachedImagePath));
        LogUtils.d(TAG, "clearCache: 强制缓存策略生效，未清除任何实例和路径");
    }

    /**
     * 清除指定路径的缓存（强制缓存策略：仅日志，不实际清理）
     * @param imagePath 图片路径
     */
    public static void removeCache(String imagePath) {
        LogUtils.w(TAG, String.format("removeCache 调用（强制缓存策略：不实际清理缓存） | 图片路径=%s", imagePath));
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "removeCache: 图片路径为空，清除失败");
            return;
        }
        LogUtils.d(TAG, "removeCache: 强制缓存策略生效，未清除任何实例和路径");
    }

    /**
     * 清除所有缓存（强制缓存策略：仅日志，不实际清理）
     */
    public static void clearAllCache() {
        LogUtils.w(TAG, "clearAllCache 调用（强制缓存策略：不实际清理缓存）");
        LogUtils.d(TAG, "clearAllCache: 强制缓存策略生效，未清除任何实例、路径和SP记录");
    }

    /**
     * 判断是否存在缓存实例
     * @return 存在返回true，否则返回false
     */
    public static boolean hasCache() {
        boolean hasCache = sCachedView != null && !TextUtils.isEmpty(sCachedImagePath);
        LogUtils.d(TAG, String.format("hasCache 调用 | 缓存存在状态=%b", hasCache));
        return hasCache;
    }

    /**
     * 清除SP中最后加载的路径记录（强制缓存策略：仅日志，不实际清理）
     * @param context 上下文
     */
    public static void clearLastLoadImagePath(Context context) {
        LogUtils.w(TAG, "clearLastLoadImagePath 调用（强制缓存策略：不实际清理SP记录）");
        LogUtils.d(TAG, "clearLastLoadImagePath: 强制缓存策略生效，未清除SP中最后路径记录");
    }

    // ====================================== 辅助方法：从缓存获取上下文 ======================================
    /**
     * 从缓存实例中获取上下文（用于无外部上下文时的SP操作）
     * @return 上下文实例，无则返回null
     */
//    private static Context getContextFromCache() {
//        Context context = sCachedView != null ? sCachedView.getContext() : null;
//        LogUtils.d(TAG, String.format("getContextFromCache 调用 | 从缓存获取上下文=%s", context));
//        return context;
//    }

    // ====================================== 重写父类方法：增强日志+SP持久化（强制保持版） ======================================
    @Override
    public void loadByBackgroundBean(BackgroundBean bean) {
        LogUtils.d(TAG, String.format("loadByBackgroundBean 调用 | BackgroundBean=%s", (bean == null ? "null" : bean.toString())));
        super.loadByBackgroundBean(bean);
    }

    @Override
    public void loadByBackgroundBean(BackgroundBean bean, boolean isRefresh) {
        LogUtils.d(TAG, String.format("loadByBackgroundBean 调用 | BackgroundBean=%s | 是否刷新=%b", (bean == null ? "null" : bean.toString()), isRefresh));
        super.loadByBackgroundBean(bean, isRefresh);
    }

    // ====================================== 新增：强制缓存监控方法 ======================================
    /**
     * 获取当前所有创建过的实例总数（用于监控强制缓存状态）
     * @return 实例总数
     */
    public static int getInstanceCount() {
        LogUtils.d(TAG, String.format("getInstanceCount 调用 | 当前实例总数=%d", sInstanceCount));
        return sInstanceCount;
    }
}

