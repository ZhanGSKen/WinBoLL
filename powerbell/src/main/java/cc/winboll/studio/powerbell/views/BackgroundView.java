package cc.winboll.studio.powerbell.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.ImageUtils;
import java.io.File;

/**
 * 基于Java7的BackgroundView（LinearLayout+ImageView，保持原图比例居中平铺）
 * 核心：ImageView保持原图比例，在LinearLayout中居中平铺，无拉伸、无裁剪、无压缩
 * 改进：强制保持缓存策略，无论内存是否紧张，不自动清理任何缓存，保留图片原始品质
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class BackgroundView extends RelativeLayout {
    // ====================================== 静态常量区（首屏可见，统一管理） ======================================
    public static final String TAG = "BackgroundView";
    // Bitmap 配置常量（原始品质）
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int BITMAP_SAMPLE_SIZE = 1; // 不缩放采样率

    // ====================================== 成员变量区（按功能分类：上下文→视图→缓存→图片属性） ======================================
    // 上下文
    private Context mContext;
    // 视图组件
    private LinearLayout mLlContainer;    // 主容器LinearLayout
    private ImageView mIvBackground;      // 图片显示控件
    // 缓存相关
    private String mCurrentCachedPath = "";// 当前缓存图片路径
    // 图片属性
    private float mImageAspectRatio = 1.0f;// 原图宽高比（宽/高）
    private int mBgColor = 0xFFFFFFFF;    // 当前图片背景色

    // ====================================== 构造器（Java7兼容，按参数重载顺序排列） ======================================
    public BackgroundView(Context context) {
        super(context);
        LogUtils.d(TAG, String.format("【构造器1】启动 | context=%s", context.getClass().getSimpleName()));
        this.mContext = context;
        initView();
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtils.d(TAG, String.format("【构造器2】启动 | context=%s", context.getClass().getSimpleName()));
        this.mContext = context;
        initView();
    }

    public BackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtils.d(TAG, String.format("【构造器3】启动 | context=%s", context.getClass().getSimpleName()));
        this.mContext = context;
        initView();
    }

    // ====================================== 初始化方法（按执行顺序：主视图→子容器→图片控件→默认背景） ======================================
    private void initView() {
        LogUtils.d(TAG, "【initView】启动");
        // 1. 配置当前控件：全屏+透明
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // 2. 初始化主容器LinearLayout
        initLinearLayout();
        // 3. 初始化ImageView
        initImageView();
        // 4. 初始设置透明背景
        setDefaultEmptyBackground();
        LogUtils.d(TAG, "【initView】完成");
    }

    private void initLinearLayout() {
        LogUtils.d(TAG, "【initLinearLayout】启动");
        mLlContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.MATCH_PARENT
        );
        mLlContainer.setLayoutParams(llParams);
        mLlContainer.setOrientation(LinearLayout.VERTICAL);
        mLlContainer.setGravity(android.view.Gravity.CENTER);
        this.addView(mLlContainer);
        LogUtils.d(TAG, "【initLinearLayout】完成");
    }

    private void initImageView() {
        LogUtils.d(TAG, "【initImageView】启动");
        mIvBackground = new ImageView(mContext);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mIvBackground.setLayoutParams(ivParams);
        mIvBackground.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mLlContainer.addView(mIvBackground);
        LogUtils.d(TAG, "【initImageView】完成");
    }

    // ====================================== 对外公开方法（按功能分类：Bean加载→图片加载） ======================================
    public void loadByBackgroundBean(BackgroundBean bean) {
        loadByBackgroundBean(bean, false);
    }

    public void loadByBackgroundBean(BackgroundBean bean, boolean isRefresh) {
        LogUtils.d(TAG, String.format("【loadByBackgroundBean】启动 | isRefresh=%b | bean=%s", isRefresh, bean));
        // 参数校验
        if (bean == null) {
            LogUtils.e(TAG, "【loadByBackgroundBean】异常：BackgroundBean为空");
            setDefaultEmptyBackground();
            return;
        }

        // 设置图片背景色
        mBgColor = bean.getPixelColor();
        LogUtils.d(TAG, String.format("【loadByBackgroundBean】背景色设置为 0x%08X", mBgColor));

        // 判断是否使用背景文件
        if (!bean.isUseBackgroundFile()) {
            LogUtils.d(TAG, "【loadByBackgroundBean】不使用背景文件，设置透明背景");
            setDefaultEmptyBackground();
            return;
        }

        // 获取目标路径
        String targetPath = bean.isUseBackgroundScaledCompressFile()
			? bean.getBackgroundScaledCompressFilePath()
			: bean.getBackgroundFilePath();
        LogUtils.d(TAG, String.format("【loadByBackgroundBean】目标路径=%s | 使用压缩文件=%b",
									  targetPath, bean.isUseBackgroundScaledCompressFile()));

        // 校验文件是否存在
        File targetFile = new File(targetPath);
        if (!targetFile.exists() || !targetFile.isFile()) {
            LogUtils.e(TAG, String.format("【loadByBackgroundBean】异常：图片文件不存在 | path=%s", targetPath));
            setDefaultEmptyBackground();
            return;
        }

        loadImage(mBgColor, targetPath, isRefresh);
    }

    public void loadImage(int bgColor, String imagePath, boolean isRefresh) {
        LogUtils.d(TAG, String.format("【loadImage】启动 | bgColor=0x%08X | imagePath=%s | isRefresh=%b",
									  bgColor, imagePath, isRefresh));
        // 隐藏ImageView防止闪烁
        mIvBackground.setVisibility(View.GONE);

        // 刷新逻辑：重新解码原始品质图片并更新缓存
        if (isRefresh) {
            LogUtils.d(TAG, "【loadImage】执行刷新逻辑：重新解码原始品质图片");
            File imageFile = new File(imagePath);
            Bitmap newBitmap = decodeOriginalBitmap(imageFile);
            LogUtils.d(TAG, String.format("【loadImage】原始图片解码完成 | newBitmap=%s",
										  newBitmap != null ? newBitmap.getWidth() + "x" + newBitmap.getHeight() : "null"));

            // 合成纯色背景图片（使用配置文件中默认相框尺寸）
            Bitmap combinedBitmap = ImageUtils.drawBitmapOnSolidBackground(
				bgColor,
				App.sAppConfigUtils.mAppConfigBean.getDefaultFrameWidth(),
				App.sAppConfigUtils.mAppConfigBean.getDefaultFrameHeight(),
				newBitmap
            );

            if (combinedBitmap == null) {
                LogUtils.e(TAG, "【loadImage】纯色背景合成失败，使用原始Bitmap");
                combinedBitmap = newBitmap;
            } else {
                LogUtils.d(TAG, String.format("【loadImage】纯色背景合成成功 | combinedBitmap=%dx%d",
											  combinedBitmap.getWidth(), combinedBitmap.getHeight()));
                // 回收原始Bitmap（避免重复缓存）
                if (newBitmap != null && !newBitmap.isRecycled() && newBitmap != combinedBitmap) {
                    newBitmap.recycle();
                    LogUtils.d(TAG, "【loadImage】原始Bitmap已回收");
                }
            }

            // 更新缓存
            if (combinedBitmap != null) {
                App.sBitmapCacheUtils.cacheBitmap(imagePath, combinedBitmap);
                App.sBitmapCacheUtils.increaseRefCount(imagePath);
                mCurrentCachedPath = imagePath;
                LogUtils.d(TAG, String.format("【loadImage】刷新缓存成功 | path=%s", imagePath));
            } else {
                LogUtils.e(TAG, String.format("【loadImage】刷新解码失败 | path=%s", imagePath));
            }
        }

        // 加载缓存图片
        Bitmap cachedBitmap = App.sBitmapCacheUtils.getCachedBitmap(imagePath);
        LogUtils.d(TAG, String.format("【loadImage】加载缓存图片 | cachedBitmap=%s",
									  cachedBitmap != null ? cachedBitmap.getWidth() + "x" + cachedBitmap.getHeight() : "null"));
        mIvBackground.setImageBitmap(cachedBitmap);
        mIvBackground.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mIvBackground.setVisibility(View.VISIBLE);
        LogUtils.d(TAG, "【loadImage】完成");
    }

    // ====================================== 内部工具方法（按功能分类：Bitmap校验→比例计算→解码→背景设置） ======================================
    /**
     * 工具方法：判断Bitmap是否有效（非空且未被回收）
     */
    private boolean isBitmapValid(Bitmap bitmap) {
        boolean valid = bitmap != null && !bitmap.isRecycled();
        if (!valid) {
            LogUtils.w(TAG, "【isBitmapValid】无效：Bitmap为空或已回收");
        }
        return valid;
    }

    /**
     * 计算图片宽高比
     */
    private boolean calculateImageAspectRatio(File file) {
        LogUtils.d(TAG, String.format("【calculateImageAspectRatio】启动 | file=%s", file.getAbsolutePath()));
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            // 尺寸校验
            int width = options.outWidth;
            int height = options.outHeight;
            if (width <= 0 || height <= 0) {
                LogUtils.e(TAG, String.format("【calculateImageAspectRatio】无效尺寸 | width=%d | height=%d", width, height));
                return false;
            }

            // 计算比例
            mImageAspectRatio = (float) width / height;
            LogUtils.d(TAG, String.format("【calculateImageAspectRatio】完成 | 比例=%.2f", mImageAspectRatio));
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("【calculateImageAspectRatio】失败：%s", e.getMessage()));
            return false;
        }
    }

    /**
     * 移除压缩逻辑：解码原始品质图片（无缩放、无色彩损失）
     */
    private Bitmap decodeOriginalBitmap(File file) {
        LogUtils.d(TAG, String.format("【decodeOriginalBitmap】启动 | file=%s", file.getAbsolutePath()));
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 核心配置：原始品质
            options.inSampleSize = BITMAP_SAMPLE_SIZE;
            options.inPreferredConfig = BITMAP_CONFIG;
            options.inPurgeable = false;
            options.inInputShareable = false;
            options.inDither = true;
            options.inScaled = false;

            // 解码图片
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (bitmap != null) {
                LogUtils.d(TAG, String.format("【decodeOriginalBitmap】成功 | width=%d | height=%d", bitmap.getWidth(), bitmap.getHeight()));
            } else {
                LogUtils.e(TAG, "【decodeOriginalBitmap】失败：返回null");
            }
            return bitmap;
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("【decodeOriginalBitmap】异常：%s", e.getMessage()));
            return null;
        }
    }

    /**
     * 设置默认透明背景，仅减少引用计数，不删除缓存
     */
    private void setDefaultEmptyBackground() {
        LogUtils.d(TAG, "【setDefaultEmptyBackground】启动");
        // 清空ImageView
        mIvBackground.setImageDrawable(null);
        mImageAspectRatio = 1.0f;

        // 减少引用计数，不删除缓存
        if (!TextUtils.isEmpty(mCurrentCachedPath)) {
            LogUtils.d(TAG, String.format("【setDefaultEmptyBackground】减少引用计数 | path=%s", mCurrentCachedPath));
            App.sBitmapCacheUtils.decreaseRefCount(mCurrentCachedPath);
            mCurrentCachedPath = "";
        }
        LogUtils.d(TAG, "【setDefaultEmptyBackground】完成");
    }

    // ====================================== 重写生命周期方法（按执行顺序：绘制→尺寸变化→窗口分离） ======================================
    /**
     * 重写：绘制前强制校验Bitmap有效性，防止已回收Bitmap崩溃
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = mIvBackground.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (!isBitmapValid(bitmap)) {
                LogUtils.e(TAG, "【onDraw】检测到已回收Bitmap，清空绘制");
                mIvBackground.setImageDrawable(null);
                return;
            }
        }
        super.onDraw(canvas);
    }

    /**
     * 重写：恢复尺寸调整逻辑，确保View尺寸变化时正确显示（无压缩）
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtils.d(TAG, String.format("【onSizeChanged】尺寸变化 | newW=%d | newH=%d | oldW=%d | oldH=%d", w, h, oldw, oldh));
    }

    /**
     * 重写：View从窗口移除时仅减少引用计数，不删除全局缓存（强制保持策略）
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtils.d(TAG, "【onDetachedFromWindow】启动");
        // 清空ImageView的Drawable，释放本地引用
        mIvBackground.setImageDrawable(null);

        // 减少引用计数，不删除全局缓存
        if (!TextUtils.isEmpty(mCurrentCachedPath)) {
            LogUtils.d(TAG, String.format("【onDetachedFromWindow】减少引用计数 | path=%s", mCurrentCachedPath));
            App.sBitmapCacheUtils.decreaseRefCount(mCurrentCachedPath);
            mCurrentCachedPath = "";
        }
        LogUtils.d(TAG, "【onDetachedFromWindow】完成");
    }
}

