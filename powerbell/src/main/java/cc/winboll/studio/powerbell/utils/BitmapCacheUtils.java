package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/11 01:57
 * @Describe 单例 Bitmap 缓存工具类（Java 7 兼容）- 极致强制缓存版（无图片压缩）
 * 功能：内存缓存 Bitmap，支持路径关联缓存、全局获取、缓存清空、SP 持久化最后缓存路径、构造时预加载
 * 特点：1. 单例模式  2. 硬引用唯一缓存（极致强制保持，任何情况不自动回收）  3. 路径-Bitmap 映射  4. 线程安全
 *       5. SP 持久化最后缓存路径  6. 构造时预加载  7. 引用计数防误回收  8. 无图片压缩，保留原始品质
 * 核心策略：无论内存如何紧张，强制保持已缓存的Bitmap，保留图片原始品质，永不自动清理
 */
public class BitmapCacheUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "BitmapCacheUtils";

    // SP 相关常量
    private static final String SP_NAME = "BitmapCacheSP";
    private static final String SP_KEY_LAST_CACHE_PATH = "last_cache_image_path";

    // Bitmap 解码常量
    private static final int BITMAP_SAMPLE_SIZE_ORIGINAL = 1; // 无压缩采样率
    private static final Bitmap.Config BITMAP_CONFIG_DEFAULT = Bitmap.Config.ARGB_8888; // 全彩品质配置

    // ================================== 成员变量（按功能分类，volatile 保证多线程可见性）=================================
    // 单例实例
    private static volatile BitmapCacheUtils sInstance;
    // 路径-Bitmap 硬引用缓存（极致强制保持，永不自动回收）
    private final Map<String, Bitmap> mHardCacheMap;
    // 路径-引用计数 映射（仅统计，不影响缓存生命周期）
    private final Map<String, Integer> mRefCountMap;
    // SP 实例（用于持久化最后缓存路径）
    private final SharedPreferences mSp;

    // ================================== 单例方法（双重校验锁，线程安全）=================================
    /**
     * 私有构造器（单例模式）
     */
    private BitmapCacheUtils() {
        LogUtils.d(TAG, "【BitmapCacheUtils】单例构造开始");
		//App.notifyMessage(TAG, "【BitmapCacheUtils】单例构造开始");
        // 使用 ConcurrentHashMap 保证线程安全，避免手动同步
        mHardCacheMap = new ConcurrentHashMap<>();
        mRefCountMap = new ConcurrentHashMap<>();
        // 初始化 SP（使用 App 全局上下文，避免内存泄漏）
        mSp = App.getInstance().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        // 构造时自动预加载 SP 中保存的最后一次缓存路径的图片
        preloadLastCachedBitmap();
        // 注册内存状态监听（仅记录日志，不清理缓存）
        registerMemoryStatusListener();
        LogUtils.d(TAG, "【BitmapCacheUtils】单例构造完成，极致强制缓存策略已启用");
    }

    /**
     * 获取单例实例（双重校验锁，线程安全）
     */
    public static BitmapCacheUtils getInstance() {
        if (sInstance == null) {
            synchronized (BitmapCacheUtils.class) {
                if (sInstance == null) {
                    sInstance = new BitmapCacheUtils();
                }
            }
        }
        return sInstance;
    }

    // ================================== 对外监控接口（App 类调用专用）=================================
    /**
     * 获取当前缓存的 Bitmap 数量
     * @return 缓存的 Bitmap 数量
     */
    public int getCacheCount() {
        int count = mHardCacheMap.size();
        LogUtils.d(TAG, "【getCacheCount】当前缓存 Bitmap 数量 - " + count);
        return count;
    }

    /**
     * 获取当前缓存的所有图片路径集合
     * @return 路径集合
     */
    public Set<String> getCachedPaths() {
        Set<String> paths = mHardCacheMap.keySet();
        LogUtils.d(TAG, "【getCachedPaths】当前缓存路径数量 - " + paths.size());
        return paths;
    }

    /**
     * 估算当前缓存的总内存占用（单位：字节）
     * @return 总内存占用
     */
    public long getTotalCacheSize() {
        long totalSize = 0;
        for (Bitmap bitmap : mHardCacheMap.values()) {
            if (isBitmapValid(bitmap)) {
                if (Build.VERSION.SDK_INT >= 12) {
                    totalSize += bitmap.getByteCount();
                } else {
                    totalSize += bitmap.getRowBytes() * bitmap.getHeight();
                }
            }
        }
        LogUtils.d(TAG, "【getTotalCacheSize】当前缓存总内存占用 - " + totalSize + " 字节");
        return totalSize;
    }

    // ================================== 对外核心接口：缓存操作（无压缩）=================================
    /**
     * 直接缓存已解码的 Bitmap（适配 BackgroundView 改进需求）
     * @param imagePath 图片绝对路径
     * @param bitmap 已解码的有效 Bitmap
     * @return 缓存后的 Bitmap / null（参数无效）
     */
    public Bitmap cacheBitmap(String imagePath, Bitmap bitmap) {
        LogUtils.d(TAG, "【cacheBitmap】调用开始（直接缓存已解码 Bitmap）| 路径=" + imagePath);
        // 入参非空校验
        if (TextUtils.isEmpty(imagePath) || !isBitmapValid(bitmap)) {
            LogUtils.e(TAG, "【cacheBitmap】入参异常：路径为空或 Bitmap 无效");
            return null;
        }

        // 极致强制：直接存入硬引用缓存，覆盖旧值（若存在）
        mHardCacheMap.put(imagePath, bitmap);
        // 初始化引用计数为1（若不存在）
        mRefCountMap.putIfAbsent(imagePath, 1);
        // 持久化当前路径到 SP
        saveLastCachePathToSp(imagePath);
        LogUtils.d(TAG, "【cacheBitmap】调用成功（直接缓存已解码 Bitmap）| 路径=" + imagePath);
        return bitmap;
    }

    /**
     * 根据图片路径缓存 Bitmap 到内存，并持久化路径到 SP
     * @param imagePath 图片绝对路径
     * @return 缓存成功的 Bitmap / null（路径无效/文件不存在/解码失败）
     */
    public Bitmap cacheBitmap(String imagePath) {
        LogUtils.d(TAG, "【cacheBitmap】调用开始（路径缓存）| 路径=" + imagePath);
        // 入参非空校验
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【cacheBitmap】入参异常：图片路径为空");
            return null;
        }

        // 文件有效性校验
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile() || imageFile.length() <= 0) {
            LogUtils.e(TAG, "【cacheBitmap】文件无效：不存在/非文件/空文件 | 路径=" + imagePath);
            return null;
        }

        // 已缓存则直接返回，避免重复加载
        Bitmap hardCacheBitmap = mHardCacheMap.get(imagePath);
        if (isBitmapValid(hardCacheBitmap)) {
            LogUtils.d(TAG, "【cacheBitmap】硬引用缓存命中，引用计数+1 | 路径=" + imagePath);
            // 引用计数+1
            increaseRefCount(imagePath);
            // 持久化当前路径到 SP
            saveLastCachePathToSp(imagePath);
            LogUtils.d(TAG, "【cacheBitmap】调用成功（缓存命中）| 路径=" + imagePath);
            return hardCacheBitmap;
        }

        // 无压缩解码 Bitmap（保留原始品质）
        Bitmap bitmap = decodeOriginalBitmap(imagePath);
        if (bitmap != null) {
            // 极致强制：存入硬引用缓存，永不自动回收
            mHardCacheMap.put(imagePath, bitmap);
            // 初始化引用计数为1
            mRefCountMap.put(imagePath, 1);
            // 持久化当前路径到 SP
            saveLastCachePathToSp(imagePath);
            LogUtils.d(TAG, "【cacheBitmap】调用成功（新缓存）| 路径=" + imagePath);
        } else {
            LogUtils.e(TAG, "【cacheBitmap】调用失败：图片解码失败 | 路径=" + imagePath);
        }
        return bitmap;
    }

    /**
     * 根据路径获取缓存的 Bitmap
     * @param imagePath 图片绝对路径
     * @return 缓存的有效 Bitmap / null（未缓存/已回收）
     */
    public Bitmap getCachedBitmap(String imagePath) {
        LogUtils.d(TAG, "【getCachedBitmap】调用开始 | 路径=" + imagePath);
        // 入参非空校验
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【getCachedBitmap】入参异常：图片路径为空");
            return null;
        }

        // 仅从硬引用缓存获取，无任何 fallback
        Bitmap hardCacheBitmap = mHardCacheMap.get(imagePath);
        if (isBitmapValid(hardCacheBitmap)) {
            LogUtils.d(TAG, "【getCachedBitmap】调用成功（缓存命中）| 路径=" + imagePath);
            return hardCacheBitmap;
        }

        // 缓存未命中或 Bitmap 已失效（极致强制策略下，理论上不会出现已回收情况）
        LogUtils.w(TAG, "【getCachedBitmap】调用失败：缓存未命中或 Bitmap 已失效 | 路径=" + imagePath);
        return null;
    }

    // ================================== 对外接口：引用计数管理（仅统计，不影响缓存）=================================
    /**
     * 增加指定路径 Bitmap 的引用计数
     * @param imagePath 图片绝对路径
     */
    public void increaseRefCount(String imagePath) {
        LogUtils.d(TAG, "【increaseRefCount】调用开始 | 路径=" + imagePath);
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【increaseRefCount】入参异常：图片路径为空");
            return;
        }
        synchronized (mRefCountMap) {
            Integer count = mRefCountMap.get(imagePath);
            if (count == null) {
                mRefCountMap.put(imagePath, 1);
            } else {
                mRefCountMap.put(imagePath, count + 1);
            }
            int newCount = mRefCountMap.get(imagePath);
            LogUtils.d(TAG, "【increaseRefCount】调用成功 | 路径=" + imagePath + " | 引用计数=" + newCount);
        }
    }

    /**
     * 减少指定路径 Bitmap 的引用计数，计数为0时仅标记不回收（极致强制缓存策略）
     * @param imagePath 图片绝对路径
     */
    public void decreaseRefCount(String imagePath) {
        LogUtils.d(TAG, "【decreaseRefCount】调用开始 | 路径=" + imagePath);
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【decreaseRefCount】入参异常：图片路径为空");
            return;
        }
        synchronized (mRefCountMap) {
            Integer count = mRefCountMap.get(imagePath);
            if (count == null || count <= 0) {
                LogUtils.w(TAG, "【decreaseRefCount】引用计数无效：路径=" + imagePath);
                return;
            }

            int newCount = count - 1;
            if (newCount <= 0) {
                // 极致强制缓存策略：引用计数为0时仅移除计数，绝对不回收 Bitmap
                mRefCountMap.remove(imagePath);
                LogUtils.d(TAG, "【decreaseRefCount】调用成功 | 路径=" + imagePath + " | 引用计数为0，极致强制保持 Bitmap");
            } else {
                mRefCountMap.put(imagePath, newCount);
                LogUtils.d(TAG, "【decreaseRefCount】调用成功 | 路径=" + imagePath + " | 引用计数=" + newCount);
            }
        }
    }

    // ================================== 对外接口：缓存清理（仅手动调用，永不自动执行）=================================
    /**
     * 清空所有 Bitmap 缓存（仅手动调用时执行，任何情况不自动执行）
     */
    public void clearAllCache() {
        LogUtils.w(TAG, "【clearAllCache】调用开始（极致强制缓存策略下，需谨慎使用）");

        // 清空硬引用缓存并回收 Bitmap
        for (Bitmap bitmap : mHardCacheMap.values()) {
            if (isBitmapValid(bitmap)) {
                bitmap.recycle();
            }
        }
        mHardCacheMap.clear();

        // 清空引用计数
        mRefCountMap.clear();

        // 清空 SP 中保存的最后缓存路径
        clearLastCachePathInSp();

        LogUtils.d(TAG, "【clearAllCache】调用成功：所有 Bitmap 缓存已清空");
    }

    /**
     * 移除指定路径的 Bitmap 缓存（仅手动调用时执行，任何情况不自动执行）
     * @param imagePath 图片绝对路径
     */
    public void removeCachedBitmap(String imagePath) {
        LogUtils.d(TAG, "【removeCachedBitmap】调用开始 | 路径=" + imagePath);
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【removeCachedBitmap】入参异常：图片路径为空");
            return;
        }

        synchronized (mRefCountMap) {
            // 手动移除时才回收 Bitmap
            Bitmap hardBitmap = mHardCacheMap.remove(imagePath);
            if (isBitmapValid(hardBitmap)) {
                hardBitmap.recycle();
                LogUtils.d(TAG, "【removeCachedBitmap】手动回收硬引用缓存 | 路径=" + imagePath);
            }
            mRefCountMap.remove(imagePath);

            // 若移除的是最后缓存的路径，清空 SP
            String lastPath = getLastCachePathFromSp();
            if (imagePath.equals(lastPath)) {
                clearLastCachePathInSp();
                LogUtils.d(TAG, "【removeCachedBitmap】移除最后缓存路径，已清空 SP");
            }
        }
        LogUtils.d(TAG, "【removeCachedBitmap】调用成功 | 路径=" + imagePath);
    }

    // ================================== 内部工具方法（无压缩解码 + Bitmap 有效性判断）=================================
    /**
     * 无压缩解码 Bitmap（保留原始品质）
     * @param imagePath 图片绝对路径
     * @return 解码后的 Bitmap / null（文件无效/解码失败）
     */
    private Bitmap decodeOriginalBitmap(String imagePath) {
        LogUtils.d(TAG, "【decodeOriginalBitmap】调用开始 | 路径=" + imagePath);
        // 前置校验：确保文件有效
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile() || imageFile.length() <= 0) {
            LogUtils.e(TAG, "【decodeOriginalBitmap】文件无效，跳过解码 | 路径=" + imagePath);
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        // 仅获取尺寸用于日志记录，不参与解码逻辑
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // 校验尺寸是否有效
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            LogUtils.e(TAG, "【decodeOriginalBitmap】图片尺寸无效 | 路径=" + imagePath);
            return null;
        }

        LogUtils.d(TAG, "【decodeOriginalBitmap】图片原始尺寸 | 宽=" + options.outWidth + " | 高=" + options.outHeight);

        // 无压缩解码配置
        options.inJustDecodeBounds = false;
        options.inSampleSize = BITMAP_SAMPLE_SIZE_ORIGINAL; // 不缩放，采样率为1
        options.inPreferredConfig = BITMAP_CONFIG_DEFAULT; // 保留全彩品质
        options.inPurgeable = false; // 关闭可清除标志，极致强制保持内存
        options.inInputShareable = false;
        options.inDither = true; // 开启抖动，保证色彩还原
        options.inScaled = false; // 关闭自动缩放，保留原始尺寸

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            LogUtils.d(TAG, "【decodeOriginalBitmap】解码" + (bitmap != null ? "成功" : "失败") + " | 路径=" + imagePath);
            return bitmap;
        } catch (OutOfMemoryError e) {
            LogUtils.e(TAG, "【decodeOriginalBitmap】OOM 异常（无压缩，图片尺寸过大）| 路径=" + imagePath);
            // 极致强制缓存策略：OOM 时仅放弃当前解码，绝对不清理已缓存的 Bitmap
            return null;
        } catch (Exception e) {
            LogUtils.e(TAG, "【decodeOriginalBitmap】解码异常 | 路径=" + imagePath, e);
            return null;
        }
    }

    /**
     * 判断 Bitmap 是否有效（非空且未被回收）
     */
    private boolean isBitmapValid(Bitmap bitmap) {
        boolean isValid = bitmap != null && !bitmap.isRecycled();
        if (!isValid) {
            LogUtils.w(TAG, "【isBitmapValid】Bitmap 无效：空或已回收");
        }
        return isValid;
    }

    // ================================== 内部工具方法：SP 持久化相关 ==================================
    /**
     * 从 SP 中获取最后一次缓存的图片路径
     * @return 最后缓存的路径 / null（未保存）
     */
    private String getLastCachePathFromSp() {
        String path = mSp.getString(SP_KEY_LAST_CACHE_PATH, null);
        LogUtils.d(TAG, "【getLastCachePathFromSp】获取最后缓存路径 | 路径=" + path);
        return path;
    }

    /**
     * 将当前缓存路径持久化到 SP
     * @param imagePath 图片绝对路径
     */
    private void saveLastCachePathToSp(String imagePath) {
        LogUtils.d(TAG, "【saveLastCachePathToSp】调用开始 | 路径=" + imagePath);
        if (TextUtils.isEmpty(imagePath)) {
            LogUtils.e(TAG, "【saveLastCachePathToSp】入参异常：图片路径为空");
            return;
        }
        mSp.edit().putString(SP_KEY_LAST_CACHE_PATH, imagePath).commit(); // Java 7 兼容，使用 commit 而非 apply
        LogUtils.d(TAG, "【saveLastCachePathToSp】调用成功 | 路径=" + imagePath);
    }

    /**
     * 清空 SP 中保存的最后缓存路径
     */
    private void clearLastCachePathInSp() {
        mSp.edit().remove(SP_KEY_LAST_CACHE_PATH).commit();
        LogUtils.d(TAG, "【clearLastCachePathInSp】调用成功：SP 中最后缓存路径已清空");
    }

    // ================================== 内部工具方法：预加载相关 ==================================
    /**
     * 构造时预加载 SP 中保存的最后一次缓存路径的图片
     */
    private void preloadLastCachedBitmap() {
        LogUtils.d(TAG, "【preloadLastCachedBitmap】调用开始");
        String lastPath = getLastCachePathFromSp();
        if (TextUtils.isEmpty(lastPath)) {
            LogUtils.d(TAG, "【preloadLastCachedBitmap】SP 中无保存的缓存路径，跳过预加载");
            return;
        }
        // 调用 cacheBitmap 预加载（内部已做文件校验和缓存判断）
        Bitmap bitmap = cacheBitmap(lastPath);
        if (bitmap != null) {
            LogUtils.d(TAG, "【preloadLastCachedBitmap】预加载成功 | 路径=" + lastPath);
        } else {
            LogUtils.w(TAG, "【preloadLastCachedBitmap】预加载失败，清空无效路径 | 路径=" + lastPath);
            // 预加载失败，清空 SP 中无效路径
            clearLastCachePathInSp();
        }
    }

    // ================================== 内部工具方法：内存状态监听（仅记录日志）=================================
    /**
     * 注册内存状态监听（仅记录日志，不清理缓存，极致强制缓存策略）
     */
    private void registerMemoryStatusListener() {
        LogUtils.d(TAG, "【registerMemoryStatusListener】调用开始");
        if (Build.VERSION.SDK_INT >= 14) {
            App.getInstance().registerComponentCallbacks(new MemoryStatusCallback());
            LogUtils.d(TAG, "【registerMemoryStatusListener】内存状态监听已注册（仅记录日志，不清理缓存）");
        } else {
            LogUtils.w(TAG, "【registerMemoryStatusListener】API 版本低于14，不支持内存状态监听");
        }
    }

    /**
     * 记录当前缓存状态（用于内存紧张时的调试）
     */
    private void logCurrentCacheStatus() {
        LogUtils.d(TAG, "【logCurrentCacheStatus】缓存数量 - " + getCacheCount() + "，总内存占用 - " + getTotalCacheSize() + " 字节");
        LogUtils.d(TAG, "【logCurrentCacheStatus】缓存路径 - " + getCachedPaths().toString());
    }

    // ================================== 内部类：内存状态回调（仅记录日志）=================================
    /**
     * 内存状态回调（仅记录日志，不清理缓存，极致强制缓存策略）
     */
    private class MemoryStatusCallback implements android.content.ComponentCallbacks2 {
        @Override
        public void onTrimMemory(int level) {
            // 极致强制缓存策略：内存紧张时仅记录日志，不清理任何缓存
            LogUtils.w(TAG, "【onTrimMemory】内存紧张级别 - " + level + "，极致强制保持所有 Bitmap 缓存（无压缩）");
            // 记录当前缓存状态
            logCurrentCacheStatus();
        }

        @Override
        public void onLowMemory() {
            // 极致强制缓存策略：低内存时仅记录日志，不清理任何缓存
            LogUtils.w(TAG, "【onLowMemory】系统低内存，极致强制保持所有 Bitmap 缓存（无压缩）");
            // 记录当前缓存状态
            logCurrentCacheStatus();
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            // 配置变化时无需处理
        }
    }
}

