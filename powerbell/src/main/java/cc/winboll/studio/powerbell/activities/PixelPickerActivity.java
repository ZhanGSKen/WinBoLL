package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 像素拾取页面，支持加载图片并拾取指定位置像素颜色，同步至背景配置
 * 适配 API30，基于 Java7 开发
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/22 14:15
 */
public class PixelPickerActivity extends WinBoLLActivity implements IWinBoLLActivity {
    // ======================== 静态常量 =========================
    public static final String TAG = "PixelPickerActivity";
    public static final String EXTRA_IMAGE_PATH = "imagePath"; // 图片路径传递键
    // 提示文本常量
    private static final String MSG_IMAGE_LOADED = "图片已加载，点击获取像素值";
    private static final String MSG_NO_IMAGE_PATH = "未找到图片路径";
    private static final String MSG_IMAGE_LOAD_FAILED = "图片加载失败";
    private static final String MSG_FILE_NOT_EXIST = "图片文件不存在";
    private static final String MSG_FILE_NOT_FOUND = "图片文件未找到";
    private static final String MSG_PIXEL_OUT_OF_RANGE = "像素坐标超出范围";
    private static final String MSG_TOUCH_OUT_OF_IMAGE = "点击位置超出图片显示范围";
    private static final String MSG_PIXEL_CALC_FAILED = "计算像素位置失败";
    private static final String MSG_PIXEL_RECORDED = "已记录像素值";

    // ======================== 成员变量 =========================
    // UI组件
    private Toolbar mToolbar;
    private ImageView imageView;
    private TextView infoText;
    private ViewGroup imageContainer;
    private RelativeLayout mainLayout;
    // 图片与像素数据
    private Bitmap originalBitmap; // 原始图片Bitmap（用于像素拾取）

    // ======================== 接口实现方法 =========================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ======================== 生命周期方法 =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pixelpicker);
        LogUtils.d(TAG, "【onCreate】PixelPickerActivity 初始化开始");

        // 初始化UI组件
        initView();
        // 初始化工具栏
        initToolbar();
        // 加载传递的图片
        loadImageFromIntent();
        // 绑定图片触摸事件
        bindImageTouchListener();

        LogUtils.d(TAG, "【onCreate】PixelPickerActivity 初始化完成");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "【onResume】PixelPickerActivity 恢复显示");
        // 同步背景颜色
        setBackgroundColor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 回收Bitmap资源，避免内存泄漏
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            originalBitmap = null;
            LogUtils.d(TAG, "【onDestroy】原始图片Bitmap资源已回收");
        }
        LogUtils.d(TAG, "【onDestroy】PixelPickerActivity 销毁完成");
    }

    // ======================== UI初始化方法 =========================
    /**
     * 初始化所有UI组件
     */
    private void initView() {
        imageView = findViewById(R.id.imageView);
        infoText = findViewById(R.id.infoText);
        imageContainer = findViewById(R.id.imageContainer);
        mainLayout = findViewById(R.id.activitypixelpickerRelativeLayout1);

        LogUtils.d(TAG, "【initView】UI组件初始化完成");
    }

    /**
     * 初始化工具栏，设置导航与标题
     */
    private void initToolbar() {
		LogUtils.d(TAG, "initToolbar() 开始初始化");
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar == null) {
            LogUtils.e(TAG, "initToolbar() | Toolbar未找到");
            return;
        }
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(getTag());
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "导航栏 点击返回按钮");
					finish();
				}
			});
        LogUtils.d(TAG, "initToolbar() 配置完成");
    }

    // ======================== 业务逻辑方法 =========================
    /**
     * 从Intent中获取图片路径并加载图片
     */
    private void loadImageFromIntent() {
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        LogUtils.d(TAG, "【loadImageFromIntent】获取到图片路径：" + imagePath);

        if (imagePath != null) {
            loadImage(imagePath);
        } else {
            infoText.setText(MSG_NO_IMAGE_PATH);
            LogUtils.w(TAG, "【loadImageFromIntent】未获取到图片路径");
        }
    }

    /**
     * 加载指定路径的图片
     * @param imagePath 图片文件路径
     */
    private void loadImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                // 解码图片（加载原图）
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                originalBitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);

                if (originalBitmap != null) {
                    imageView.setImageBitmap(originalBitmap);
                    infoText.setText(MSG_IMAGE_LOADED);
                    LogUtils.d(TAG, "【loadImage】图片加载成功，尺寸：" + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
                } else {
                    infoText.setText(MSG_IMAGE_LOAD_FAILED);
                    LogUtils.e(TAG, "【loadImage】图片解码失败");
                }
            } else {
                infoText.setText(MSG_FILE_NOT_EXIST);
                LogUtils.w(TAG, "【loadImage】图片文件不存在：" + imagePath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            infoText.setText(MSG_FILE_NOT_FOUND);
            LogUtils.e(TAG, "【loadImage】图片文件未找到：" + e.getMessage());
        }
    }

    /**
     * 显示像素颜色信息对话框
     * @param pixelColor 拾取的像素颜色（ARGB）
     * @param x 像素X坐标
     * @param y 像素Y坐标
     */
    private void showPixelDialog(final int pixelColor, int x, int y) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pixel);
        dialog.setCancelable(true);

        // 设置颜色预览与信息展示
        TextView colorView = dialog.findViewById(R.id.pixelColorView);
        TextView infoTextView = dialog.findViewById(R.id.colorInfoText);
        colorView.setBackgroundColor(pixelColor);

        String colorInfo = String.format(
			"RGB: (%d, %d, %d)\n" +
			"ARGB: #%08X\n" +
			"实际像素位置: (%d, %d)",
			Color.red(pixelColor),
			Color.green(pixelColor),
			Color.blue(pixelColor),
			pixelColor,
			x, y);
        infoTextView.setText(colorInfo);
        LogUtils.d(TAG, "【showPixelDialog】显示像素信息：" + colorInfo);

        // 确定按钮点击事件
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					// 保存像素颜色到背景配置
					savePixelColor(pixelColor);
					Toast.makeText(PixelPickerActivity.this, MSG_PIXEL_RECORDED, Toast.LENGTH_SHORT).show();
					// 同步背景颜色
					setBackgroundColor();
				}
			});

        dialog.show();
        LogUtils.d(TAG, "【showPixelDialog】像素对话框已显示");
    }

    /**
     * 保存拾取的像素颜色到背景配置
     * @param pixelColor 拾取的像素颜色（ARGB）
     */
    private void savePixelColor(int pixelColor) {
        BackgroundSourceUtils utils = BackgroundSourceUtils.getInstance(this);
        BackgroundBean bean = utils.getPreviewBackgroundBean();
        bean.setPixelColor(pixelColor);
        utils.saveSettings();
        LogUtils.d(TAG, "【savePixelColor】像素颜色已保存：#" + Integer.toHexString(pixelColor));
    }

    /**
     * 同步背景颜色为拾取的像素颜色
     */
    void setBackgroundColor() {
        BackgroundSourceUtils utils = BackgroundSourceUtils.getInstance(this);
        BackgroundBean bean = utils.getPreviewBackgroundBean();
        int pixelColor = bean.getPixelColor();
        mainLayout.setBackgroundColor(pixelColor);
        LogUtils.d(TAG, "【setBackgroundColor】背景颜色已同步：#" + Integer.toHexString(pixelColor));
    }

    // ======================== 事件回调方法 =========================
    /**
     * 绑定图片容器的触摸事件，处理像素拾取逻辑
     */
    private void bindImageTouchListener() {
        imageContainer.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN && originalBitmap != null) {
						float touchX = event.getX();
						float touchY = event.getY();
						LogUtils.v(TAG, "【onTouch】触摸坐标：(" + touchX + ", " + touchY + ")");

						try {
							// 获取图片在窗口中的位置与尺寸
							int[] imageLocation = new int[2];
							imageView.getLocationInWindow(imageLocation);
							int imageWidth = imageView.getWidth();
							int imageHeight = imageView.getHeight();
							LogUtils.v(TAG, "【onTouch】图片显示尺寸：" + imageWidth + "x" + imageHeight + "，位置：(" + imageLocation[0] + ", " + imageLocation[1] + ")");

							// 计算缩放比例
							float scaleX = (float) originalBitmap.getWidth() / imageWidth;
							float scaleY = (float) originalBitmap.getHeight() / imageHeight;
							LogUtils.v(TAG, "【onTouch】图片缩放比例：X=" + scaleX + "，Y=" + scaleY);

							// 调整触摸坐标到图片显示区域坐标系
							float adjustedX = touchX - imageLocation[0];
							float adjustedY = touchY - imageLocation[1];
							LogUtils.v(TAG, "【onTouch】调整后触摸坐标：(" + adjustedX + ", " + adjustedY + ")");

							// 检查是否在图片显示范围内
							if (adjustedX >= 0 && adjustedX <= imageWidth && adjustedY >= 0 && adjustedY <= imageHeight) {
								// 计算原始图片的像素坐标
								int pixelX = (int) (adjustedX * scaleX);
								int pixelY = (int) (adjustedY * scaleY);
								LogUtils.v(TAG, "【onTouch】计算后像素坐标：(" + pixelX + ", " + pixelY + ")");

								// 检查像素坐标是否在原始图片范围内
								if (pixelX >= 0 && pixelX < originalBitmap.getWidth() && pixelY >= 0 && pixelY < originalBitmap.getHeight()) {
									int pixelColor = originalBitmap.getPixel(pixelX, pixelY);
									showPixelDialog(pixelColor, pixelX, pixelY);
								} else {
									Toast.makeText(PixelPickerActivity.this, MSG_PIXEL_OUT_OF_RANGE, Toast.LENGTH_SHORT).show();
									LogUtils.w(TAG, "【onTouch】像素坐标超出原始图片范围");
								}
							} else {
								Toast.makeText(PixelPickerActivity.this, MSG_TOUCH_OUT_OF_IMAGE, Toast.LENGTH_SHORT).show();
								LogUtils.w(TAG, "【onTouch】触摸位置超出图片显示范围");
							}
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(PixelPickerActivity.this, MSG_PIXEL_CALC_FAILED, Toast.LENGTH_SHORT).show();
							LogUtils.e(TAG, "【onTouch】计算像素位置失败：" + e.getMessage());
						}
					}
					return true;
				}
			});
        LogUtils.d(TAG, "【bindImageTouchListener】图片触摸事件已绑定");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LogUtils.d(TAG, "【onOptionsItemSelected】点击返回菜单");
            Intent intent = new Intent(this, BackgroundSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
        LogUtils.d(TAG, "【onBackPressed】返回键触发，页面关闭");
    }
}

