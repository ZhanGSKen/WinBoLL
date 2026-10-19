package cc.winboll.studio.powerbell.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.dialogs.BackgroundPicturePreviewDialog;
import cc.winboll.studio.powerbell.dialogs.ColorPaletteDialog;
import cc.winboll.studio.powerbell.dialogs.NetworkBackgroundDialog;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import cc.winboll.studio.powerbell.utils.BitmapCacheUtils;
import cc.winboll.studio.powerbell.utils.FileUtils;
import cc.winboll.studio.powerbell.utils.ImageCropUtils;
import cc.winboll.studio.powerbell.utils.ImageUtils;
import cc.winboll.studio.powerbell.utils.UriUtils;
import cc.winboll.studio.powerbell.views.BackgroundView;
import java.io.File;

/**
 * 背景设置页面（支持图片选择、拍照、裁剪、像素拾取、调色板等功能）
 * 核心：基于强制缓存策略，支持预览与设置提交分离，保留操作状态
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class BackgroundSettingsActivity extends WinBoLLActivity {
    // ====================== 常量定义（按功能分类排序）======================
    public static final String TAG = "BackgroundSettingsActivity";

    // 系统版本常量
    private static final int SDK_VERSION_TIRAMISU = 33;

    // 请求码（按功能分组，从小到大排序）
    public static final int REQUEST_SELECT_PICTURE = 0;
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_CROP_IMAGE = 2;
    private static final int REQUEST_PIXELPICKER = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1004;

    // Bitmap解析常量
    private static final int BITMAP_MAX_SIZE = 2048;
    private static final int BITMAP_MAX_SAMPLE_SIZE = 16;

    // ====================== 成员变量（按依赖优先级+功能分类）======================
    // 工具类实例
    private BackgroundSourceUtils mBgSourceUtils;
    private BitmapCacheUtils mBitmapCache;

    // 视图组件
    private Toolbar mToolbar;
    private BackgroundView mBackgroundView;

    // 状态标记（volatile保证多线程可见性）
    private volatile boolean isCommitSettings = false;
    private volatile boolean isPreviewBackgroundChanged = false;

    // ====================== 生命周期方法（按执行顺序排列）======================
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
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate() 开始初始化");
        setContentView(R.layout.activity_background_settings);

        // 初始化核心组件
        initCoreComponents();
        // 初始化Toolbar与点击事件
        initToolbar();
        initClickListeners();
        LogUtils.d(TAG, "onCreate() 视图与事件绑定完成");

        // 处理分享意图或初始化预览
        handleIntentOrPreview();
        // 初始化预览环境并刷新
        initPreviewEnvironment();

        LogUtils.d(TAG, "onCreate() 初始化完成");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LogUtils.d(TAG, "onPostCreate() 执行双重刷新预览");

        // 监听视图布局完成事件
        mBackgroundView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					// 移除监听，避免重复回调
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						mBackgroundView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						mBackgroundView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}

					// 此时已获取真实宽高
					int width = mBackgroundView.getWidth();
					int height = mBackgroundView.getHeight();
					LogUtils.d(TAG, String.format("onPostCreate() 获取视图尺寸 | width=%d | height=%d", width, height));
					if (width > 0 && height > 0) {
						AppConfigUtils appConfigUtils = AppConfigUtils.getInstance(BackgroundSettingsActivity.this);
						appConfigUtils.loadAppConfig();
						appConfigUtils.mAppConfigBean.setDefaultFrameWidth(width);
						appConfigUtils.mAppConfigBean.setDefaultFrameHeight(height);
						appConfigUtils.saveAppConfig();
						LogUtils.d(TAG, "onPostCreate() 保存默认相框尺寸成功");
						doubleRefreshPreview();
					}
				}
			});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d(TAG, String.format("onActivityResult() | requestCode=%d | resultCode=%d | data=%s",
									  requestCode, resultCode, data != null ? data.toString() : "null"));

        try {
            if (resultCode != RESULT_OK) {
                LogUtils.d(TAG, String.format("onActivityResult() 操作取消 | requestCode=%d", requestCode));
                handleOperationCancelOrFail();
                return;
            }
            handleActivityResult(requestCode, data);
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("onActivityResult() 异常 | requestCode=%d | 异常信息=%s",
										  requestCode, e.getMessage()));
            ToastUtils.show("操作失败");
        }
    }

    @Override
    public void finish() {
        LogUtils.d(TAG, String.format("finish() | isCommitSettings=%b | isPreviewBackgroundChanged=%b",
									  isCommitSettings, isPreviewBackgroundChanged));
        if (isCommitSettings) {
            super.finish();
        } else {
            handleFinishConfirmation();
        }
    }

    // ====================== 权限回调方法（单独分类）======================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.d(TAG, String.format("onRequestPermissionsResult() | requestCode=%d | 权限数量=%d | 结果数量=%d",
									  requestCode, permissions.length, grantResults.length));
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            handleCameraPermissionResult(grantResults);
        }
    }

    // ====================== 界面初始化方法（Toolbar + 点击事件）======================
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

    private void initClickListeners() {
        LogUtils.d(TAG, "initClickListeners() 开始绑定按钮点击事件");
        // 绑定所有按钮点击事件
        bindClickListener(R.id.activitybackgroundsettingsAButton1, onOriginNullClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton2, onReceivedPictureClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton3, onTakePhotoClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton4, onSelectPictureClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton5, onNetworkBackgroundDialog);
        bindClickListener(R.id.activitybackgroundsettingsAButton6, onCropPictureClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton7, onCropFreePictureClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton8, onPixelPickerClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton9, onColorPaletteClickListener);
        bindClickListener(R.id.activitybackgroundsettingsAButton10, onCleanPixelClickListener);
        LogUtils.d(TAG, "initClickListeners() 按钮点击事件绑定完成");
    }

    // 通用按钮绑定工具方法
    private void bindClickListener(int resId, View.OnClickListener listener) {
        LogUtils.d(TAG, String.format("bindClickListener() | resId=%d", resId));
        View view = findViewById(resId);
        if (view != null) {
            view.setOnClickListener(listener);
            LogUtils.d(TAG, String.format("bindClickListener() | resId=%d 绑定成功", resId));
        } else {
            LogUtils.e(TAG, String.format("bindClickListener() | 未找到视图：%d", resId));
        }
    }

    // ====================== 按钮点击事件（按功能分类）======================
    private View.OnClickListener onOriginNullClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onOriginNullClickListener() | 取消背景图片");
            BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
            if (previewBean == null) {
                LogUtils.e(TAG, "onOriginNullClickListener() | 预览Bean为空");
                return;
            }
            previewBean.setIsUseBackgroundFile(false);
            mBgSourceUtils.saveSettings();
            doubleRefreshPreview();
            isPreviewBackgroundChanged = true;
        }
    };

    private View.OnClickListener onSelectPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onSelectPictureClickListener() | 选择图片");
            launchImageSelector();
        }
    };

    private View.OnClickListener onNetworkBackgroundDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onNetworkBackgroundDialog() | 打开网络背景对话框");
            NetworkBackgroundDialog networkBackgroundDialog = new NetworkBackgroundDialog(BackgroundSettingsActivity.this, new NetworkBackgroundDialog.OnDialogClickListener() {
					@Override
					public void onConfirm(String szConfirmFilePath) {
						LogUtils.d(TAG, String.format("网络背景确认 onConfirm() | 文件路径=%s", szConfirmFilePath));
						// 拷贝文件到预览数据并启动裁剪
						if (putUriFileToPreviewSource(new File(szConfirmFilePath))) {
							startImageCrop(false);
						}
					}

					@Override
					public void onCancel() {
						LogUtils.d(TAG, "网络背景取消 onCancel()");
					}
				});
            networkBackgroundDialog.show();
        }
    };

    private View.OnClickListener onCropPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onCropPictureClickListener() | 固定比例裁剪");
            startImageCrop(false);
        }
    };

    private View.OnClickListener onCropFreePictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onCropFreePictureClickListener() | 自由裁剪");
            startImageCrop(true);
        }
    };

    private View.OnClickListener onTakePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onTakePhotoClickListener() | 拍照");
            // 动态申请相机权限
            if (ContextCompat.checkSelfPermission(BackgroundSettingsActivity.this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
                LogUtils.d(TAG, "拍照准备 | 相机权限未授予，发起申请");
                ActivityCompat.requestPermissions(
					BackgroundSettingsActivity.this,
					new String[]{Manifest.permission.CAMERA},
					REQUEST_CAMERA_PERMISSION);
                return;
            }
            handleTakePhoto();
        }
    };

    private View.OnClickListener onReceivedPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onReceivedPictureClickListener() | 恢复收到的图片");
            BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
            if (previewBean == null) {
                LogUtils.e(TAG, "onReceivedPictureClickListener() | 预览Bean为空");
                return;
            }
            previewBean.setIsUseBackgroundFile(true);
            mBgSourceUtils.saveSettings();
            doubleRefreshPreview();
            isPreviewBackgroundChanged = true;
        }
    };

    private View.OnClickListener onPixelPickerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onPixelPickerClickListener() | 像素拾取");
            BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
            if (previewBean == null) {
                LogUtils.e(TAG, "onPixelPickerClickListener() | 预览Bean为空");
                ToastUtils.show("无有效图片可拾取像素");
                return;
            }
            String targetImagePath = previewBean.getBackgroundFilePath();
            File targetFile = new File(targetImagePath);
            if (targetFile == null || !targetFile.exists() || targetFile.length() <= 0) {
                ToastUtils.show("无有效图片可拾取像素");
                LogUtils.e(TAG, String.format("像素拾取失败 | 文件无效：%s", targetImagePath));
                return;
            }
            Intent intent = new Intent(getApplicationContext(), PixelPickerActivity.class);
            intent.putExtra("imagePath", targetImagePath);
            startActivityForResult(intent, REQUEST_PIXELPICKER);
            LogUtils.d(TAG, String.format("像素拾取启动 | 路径：%s", targetImagePath));
        }
    };

    private View.OnClickListener onCleanPixelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onCleanPixelClickListener() | 清空像素颜色");
            BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
            if (previewBean == null) {
                LogUtils.e(TAG, "onCleanPixelClickListener() | 预览Bean为空");
                return;
            }
            int oldColor = previewBean.getPixelColor();
            previewBean.setPixelColor(ImageUtils.getColorAccent(BackgroundSettingsActivity.this));
            mBgSourceUtils.saveSettings();
            doubleRefreshPreview();
            isPreviewBackgroundChanged = true;
            ToastUtils.show("像素颜色已清空");
            LogUtils.d(TAG, String.format("像素清空 | 旧颜色：#%08X", oldColor));
        }
    };

    private View.OnClickListener onColorPaletteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onColorPaletteClickListener() | 调色板按钮");
            final BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
            if (previewBean == null) {
                LogUtils.e(TAG, "onColorPaletteClickListener() | 预览Bean为空");
                return;
            }
            int initialColor = previewBean.getPixelColor();
            LogUtils.d(TAG, String.format("调色板 | 初始颜色：#%08X", initialColor));
            ColorPaletteDialog dialog = new ColorPaletteDialog(BackgroundSettingsActivity.this, initialColor, new ColorPaletteDialog.OnColorSelectedListener() {
					@Override
					public void onColorSelected(int color) {
						previewBean.setPixelColor(color);
						mBgSourceUtils.saveSettings();
						doubleRefreshPreview();
						isPreviewBackgroundChanged = true;
						LogUtils.d(TAG, String.format("颜色选择 | 选中颜色：#%08X", color));
					}
				});
            dialog.show();
            LogUtils.d(TAG, "调色板 | 对话框已显示");
        }
    };

    // ====================== 工具方法（通用工具 + 视图工具）======================
    /**
     * 生成 FileProvider Uri，适配 Android 7.0+
     * @param file 目标文件
     * @return 适配后的Uri，失败返回null
     */
    public Uri getFileProviderUri(File file) {
        LogUtils.d(TAG, String.format("getFileProviderUri() | 文件路径：%s", (file != null ? file.getAbsolutePath() : "null")));
        if (file == null) {
            LogUtils.e(TAG, "getFileProviderUri() | 文件为空");
            return null;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String FILE_PROVIDER_AUTHORITY = getPackageName() + ".fileprovider";
                return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
            } else {
                return Uri.fromFile(file);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("getFileProviderUri() | 生成Uri失败：%s", e.getMessage()));
            return null;
        }
    }

    /**
     * 校验 Bitmap 是否有效（未被回收且不为空）
     * @param bitmap 目标Bitmap
     * @return 有效返回true，否则false
     */
    private boolean isBitmapValid(Bitmap bitmap) {
        boolean isValid = bitmap != null && !bitmap.isRecycled();
        LogUtils.d(TAG, String.format("isBitmapValid() | Bitmap有效性校验：%b", isValid));
        return isValid;
    }

    /**
     * 双重刷新预览，确保背景加载最新数据
     */
    private void doubleRefreshPreview() {
        LogUtils.d(TAG, "doubleRefreshPreview() 开始双重刷新预览");
        if (mBgSourceUtils == null || mBackgroundView == null || isFinishing()) {
            LogUtils.w(TAG, "双重刷新 跳过：对象为空或Activity已结束");
            return;
        }

        // 第一重刷新
        try {
            mBgSourceUtils.loadSettings();
            BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
            mBackgroundView.loadByBackgroundBean(previewBean, true);
            LogUtils.d(TAG, "双重刷新 第一重完成");
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("双重刷新 第一重异常：%s", e.getMessage()));
            return;
        }

        // 第二重刷新（延迟执行）
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mBackgroundView != null && !isFinishing() && mBgSourceUtils != null) {
						try {
							mBgSourceUtils.loadSettings();
							BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
							mBackgroundView.loadByBackgroundBean(previewBean, true);
							LogUtils.d(TAG, "双重刷新 第二重完成");
						} catch (Exception e) {
							LogUtils.e(TAG, String.format("双重刷新 第二重异常：%s", e.getMessage()));
						}
					}
				}
			}, 200);
    }

    // ====================== 业务逻辑方法（按功能分类）======================
    /**
     * 初始化核心组件（工具类+视图）
     */
    private void initCoreComponents() {
        LogUtils.d(TAG, "initCoreComponents() 开始初始化");
        // 初始化视图
        mBackgroundView = findViewById(R.id.background_view);
        if (mBackgroundView == null) {
            LogUtils.e(TAG, "initCoreComponents() | BackgroundView未找到");
        }
        // 初始化工具类
        mBgSourceUtils = BackgroundSourceUtils.getInstance(this);
        mBgSourceUtils.loadSettings();
        mBitmapCache = BitmapCacheUtils.getInstance();
        LogUtils.d(TAG, "initCoreComponents() 视图与工具类加载完成");
    }

    /**
     * 处理意图或初始化预览
     */
    private void handleIntentOrPreview() {
        LogUtils.d(TAG, "handleIntentOrPreview() 开始处理");
        if (handleShareIntent()) {
            ToastUtils.show("已接收分享图片");
            LogUtils.d(TAG, "handleIntentOrPreview() | 处理分享意图成功");
        } else {
            mBgSourceUtils.setCurrentSourceToPreview();
            LogUtils.d(TAG, "handleIntentOrPreview() | 加载当前背景配置");
        }
    }

    /**
     * 初始化预览环境
     */
    private void initPreviewEnvironment() {
        LogUtils.d(TAG, "initPreviewEnvironment() 开始初始化");
        BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
        mBgSourceUtils.createAndUpdatePreviewEnvironmentForCropping(previewBean);
        doubleRefreshPreview();
        LogUtils.d(TAG, "initPreviewEnvironment() 初始化完成");
    }

    /**
     * 处理分享意图
     * @return 处理成功返回true，否则false
     */
    private boolean handleShareIntent() {
        LogUtils.d(TAG, "handleShareIntent() 开始处理");
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            LogUtils.d(TAG, String.format("分享处理 | action：%s，type：%s", action, type));
            if (Intent.ACTION_SEND.equals(action) && type != null && isImageType(type)) {
                showSharePreviewDialog();
                return true;
            }
        }
        return false;
    }

    /**
     * 显示分享图片预览对话框
     */
    private void showSharePreviewDialog() {
        LogUtils.d(TAG, "showSharePreviewDialog() 开始显示");
        BackgroundPicturePreviewDialog dlg = new BackgroundPicturePreviewDialog(this, new BackgroundPicturePreviewDialog.IOnRecivedPictureListener() {
				@Override
				public void onAcceptRecivedPicture(Uri uriRecivedPicture) {
					LogUtils.d(TAG, String.format("分享确认 | Uri：%s", uriRecivedPicture.toString()));
					if (putUriFileToPreviewSource(uriRecivedPicture)) {
						startImageCrop(false);
					}
				}
			});
        dlg.show();
        LogUtils.d(TAG, "分享处理 | 显示图片预览对话框");
    }

    /**
     * 判断是否为图片类型
     * @param mimeType MIME类型
     * @return 是图片返回true，否则false
     */
    private boolean isImageType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String lowerMimeType = mimeType.toLowerCase();
        LogUtils.d(TAG, String.format("isImageType() | mimeType: %s, lowerMimeType: %s", mimeType, lowerMimeType));
        return lowerMimeType.startsWith("image/");
    }

    /**
     * 启动图片选择器
     */
    private void launchImageSelector() {
        LogUtils.d(TAG, "launchImageSelector() 启动图片选择器");
        Intent[] intents = createImageSelectorIntents();
        Intent validIntent = findValidIntent(intents);

        if (validIntent != null) {
            launchImageChooser(validIntent);
        } else {
            showNoGalleryDialog();
        }
    }

    /**
     * 创建图片选择器意图数组
     * @return 意图数组
     */
    private Intent[] createImageSelectorIntents() {
        LogUtils.d(TAG, "createImageSelectorIntents() 开始创建");
        Intent[] intents = new Intent[3];
        // ACTION_GET_CONTENT
        Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getContentIntent.setType("image/*");
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
        getContentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intents[0] = getContentIntent;

        // ACTION_PICK
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        pickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intents[1] = pickIntent;

        // ACTION_OPEN_DOCUMENT（API19+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent openDocIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openDocIntent.setType("image/*");
            openDocIntent.addCategory(Intent.CATEGORY_OPENABLE);
            openDocIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intents[2] = openDocIntent;
        }
        LogUtils.d(TAG, "createImageSelectorIntents() 意图数组创建完成");
        return intents;
    }

    /**
     * 查找有效的意图
     * @param intents 意图数组
     * @return 有效意图，无则返回null
     */
    private Intent findValidIntent(Intent[] intents) {
        LogUtils.d(TAG, "findValidIntent() 开始查找");
        for (Intent intent : intents) {
            if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
                LogUtils.d(TAG, "findValidIntent() | 找到有效意图");
                return intent;
            }
        }
        LogUtils.d(TAG, "findValidIntent() | 无有效意图");
        return null;
    }

    /**
     * 启动图片选择器
     * @param validIntent 有效意图
     */
    private void launchImageChooser(Intent validIntent) {
        LogUtils.d(TAG, "launchImageChooser() 启动选择器");
        Intent chooser = Intent.createChooser(validIntent, "选择图片");
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(chooser, REQUEST_SELECT_PICTURE);
        LogUtils.d(TAG, "launchImageChooser() | 启动图片选择");
    }

    /**
     * 显示无相册应用提示对话框
     */
    private void showNoGalleryDialog() {
        LogUtils.d(TAG, "showNoGalleryDialog() | 无相册应用");
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ToastUtils.show("未找到相册应用，请安装后重试");
					new AlertDialog.Builder(BackgroundSettingsActivity.this)
                        .setTitle("无图片选择应用")
                        .setMessage("需要安装相册应用才能选择图片")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchGalleryMarket();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
				}
			});
    }

    /**
     * 启动应用商店下载相册
     */
    private void launchGalleryMarket() {
        LogUtils.d(TAG, "launchGalleryMarket() 启动应用商店");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
        marketIntent.setData(Uri.parse("market://details?id=com.android.gallery3d"));
        if (marketIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(marketIntent);
            LogUtils.d(TAG, "launchGalleryMarket() | 启动成功");
        } else {
            ToastUtils.show("无法打开应用商店");
            LogUtils.e(TAG, "launchGalleryMarket() | 启动失败");
        }
    }

    /**
     * 处理操作取消或失败
     */
    private void handleOperationCancelOrFail() {
        LogUtils.d(TAG, "handleOperationCancelOrFail() 操作取消或失败");
        mBgSourceUtils.setCurrentSourceToPreview();
        ToastUtils.show("操作取消或失败");
        doubleRefreshPreview();
    }

    /**
     * 处理拍照逻辑（权限通过后执行）
     */
    void handleTakePhoto() {
        LogUtils.d(TAG, "handleTakePhoto() 开始处理拍照");
        BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
        if (previewBean == null) {
            LogUtils.e(TAG, "handleTakePhoto() | 预览Bean为空");
            ToastUtils.show("拍照文件创建失败");
            return;
        }

        File takePhotoFile = new File(previewBean.getBackgroundFilePath());
        if (!takePhotoFile.exists()) {
            ToastUtils.show("拍照文件创建失败");
            LogUtils.e(TAG, String.format("handleTakePhoto() | 文件不存在：%s", takePhotoFile.getAbsolutePath()));
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri photoUri = getFileProviderUri(takePhotoFile);
            if (photoUri == null) {
                throw new Exception("生成FileProvider Uri失败");
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            LogUtils.d(TAG, String.format("handleTakePhoto() | Uri：%s", photoUri.toString()));
        } catch (Exception e) {
            String errMsg = "拍照启动异常：" + e.getMessage();
            ToastUtils.show(errMsg.substring(0, 20));
            LogUtils.e(TAG, String.format("handleTakePhoto() | %s", e.getMessage()));
        }
    }

    /**
     * 处理ActivityResult分发
     * @param requestCode 请求码
     * @param data 回调数据
     */
    private void handleActivityResult(int requestCode, Intent data) {
        LogUtils.d(TAG, String.format("handleActivityResult() | 处理请求码：%d", requestCode));
        switch (requestCode) {
            case REQUEST_SELECT_PICTURE:
                handleSelectPictureResult(data);
                break;
            case REQUEST_TAKE_PHOTO:
                handleTakePhotoResult(data);
                break;
            case REQUEST_CROP_IMAGE:
                handleCropImageResult(data);
                break;
            case REQUEST_PIXELPICKER:
                handlePixelPickerResult();
                break;
            default:
                LogUtils.d(TAG, String.format("handleActivityResult() | 未知requestCode：%d", requestCode));
                break;
        }
    }

    /**
     * 处理拍照结果
     * @param data 回调数据
     */
    private void handleTakePhotoResult(Intent data) {
        LogUtils.d(TAG, "handleTakePhotoResult() 处理拍照结果");
        BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
        if (previewBean == null) {
            LogUtils.e(TAG, "handleTakePhotoResult() | 预览Bean为空");
            return;
        }

        previewBean.setIsUseBackgroundFile(true);
        previewBean.setIsUseBackgroundScaledCompressFile(false);
        mBgSourceUtils.saveSettings();
        doubleRefreshPreview();

        startImageCrop(false);
        LogUtils.d(TAG, "handleTakePhotoResult() | 已启动裁剪");
    }

    /**
     * 处理选图结果
     * @param data 回调数据
     */
    private void handleSelectPictureResult(Intent data) {
        LogUtils.d(TAG, "handleSelectPictureResult() 处理选图结果");
        Uri selectedImage = data.getData();
        if (selectedImage == null) {
            ToastUtils.show("图片Uri为空");
            LogUtils.e(TAG, "handleSelectPictureResult() | Uri为空");
            return;
        }
        LogUtils.d(TAG, String.format("handleSelectPictureResult() | 系统返回Uri : %s", selectedImage.toString()));

        // 申请持久化权限（API33+）
        if (Build.VERSION.SDK_INT >= SDK_VERSION_TIRAMISU) {
            getContentResolver().takePersistableUriPermission(
				selectedImage,
				Intent.FLAG_GRANT_READ_URI_PERMISSION);
            LogUtils.d(TAG, "handleSelectPictureResult() | 已添加持久化权限");
        }

        // 同步文件并启动裁剪
        if (putUriFileToPreviewSource(selectedImage)) {
            LogUtils.d(TAG, "handleSelectPictureResult() | 路径绑定完成");
            startImageCrop(false);
        } else {
            ToastUtils.show("图片同步失败");
            LogUtils.e(TAG, "handleSelectPictureResult() | 文件复制失败");
        }
    }

    /**
     * 将 Uri 文件同步到预览 Bean
     * @param srcUriFile 源Uri
     * @return 同步成功返回true，否则false
     */
    private boolean putUriFileToPreviewSource(Uri srcUriFile) {
        LogUtils.d(TAG, String.format("putUriFileToPreviewSource() | 源Uri：%s", srcUriFile.toString()));
        String filePath = UriUtils.getFilePathFromUri(this, srcUriFile);
        if (TextUtils.isEmpty(filePath)) {
            LogUtils.e(TAG, "putUriFileToPreviewSource() | Uri解析路径为空");
            return false;
        }
        File srcFile = new File(filePath);
        return putUriFileToPreviewSource(srcFile);
    }

    /**
     * 将 File 同步到预览 Bean
     * @param srcFile 源文件
     * @return 同步成功返回true，否则false
     */
    private boolean putUriFileToPreviewSource(File srcFile) {
        LogUtils.d(TAG, String.format("putUriFileToPreviewSource() | 源文件：%s", srcFile.getAbsolutePath()));
        mBgSourceUtils.loadSettings();
        BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
        File dstFile = new File(previewBean.getBackgroundFilePath());
        LogUtils.d(TAG, String.format("putUriFileToPreviewSource() | 目标文件：%s", dstFile.getAbsolutePath()));
        if (FileUtils.copyFile(srcFile, dstFile)) {
            LogUtils.d(TAG, "putUriFileToPreviewSource() | 文件拷贝成功");
            return true;
        }
        LogUtils.d(TAG, "putUriFileToPreviewSource() | 文件无法拷贝");
        return false;
    }

    /**
     * 处理裁剪结果
     * @param data 回调数据
     */
    private void handleCropImageResult(Intent data) {
        LogUtils.d(TAG, "handleCropImageResult() 处理裁剪结果");
        BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
        if (previewBean == null) {
            LogUtils.e(TAG, "handleCropImageResult() | 预览Bean为空");
            handleOperationCancelOrFail();
            return;
        }

        File cropTempFile = new File(previewBean.getBackgroundScaledCompressFilePath());
        boolean isFileExist = cropTempFile.exists();
        boolean isFileReadable = isFileExist ? cropTempFile.canRead() : false;
        long fileSize = isFileExist ? cropTempFile.length() : 0;
        boolean isCropSuccess = isFileExist && isFileReadable && fileSize > 100;

        if (isCropSuccess) {
            handleCropSuccess(previewBean, fileSize);
        } else {
            handleCropFailure(isFileExist, isFileReadable, fileSize);
        }
    }

    /**
     * 处理裁剪成功
     * @param previewBean 预览Bean
     * @param fileSize 文件大小
     */
    private void handleCropSuccess(BackgroundBean previewBean, long fileSize) {
        LogUtils.d(TAG, String.format("handleCropSuccess() | 裁剪成功，文件大小：%d", fileSize));
        isPreviewBackgroundChanged = true;
        previewBean.setIsUseBackgroundFile(true);
        previewBean.setIsUseBackgroundScaledCompressFile(true);
        mBgSourceUtils.saveSettings();
        doubleRefreshPreview();
    }

    /**
     * 处理裁剪失败
     * @param isFileExist 文件是否存在
     * @param isFileReadable 文件是否可读
     * @param fileSize 文件大小
     */
    private void handleCropFailure(boolean isFileExist, boolean isFileReadable, long fileSize) {
        LogUtils.e(TAG, String.format("handleCropFailure() | 裁剪失败，文件状态：存在=%b，可读=%b，大小=%d",
									  isFileExist, isFileReadable, fileSize));
        handleOperationCancelOrFail();
    }

    /**
     * 处理像素拾取结果
     */
    private void handlePixelPickerResult() {
        LogUtils.d(TAG, "handlePixelPickerResult() 处理像素拾取结果");
        doubleRefreshPreview();
        isPreviewBackgroundChanged = true;
    }

    /**
     * 处理相机权限申请结果
     * @param grantResults 权限结果数组
     */
    private void handleCameraPermissionResult(int[] grantResults) {
        LogUtils.d(TAG, "handleCameraPermissionResult() 处理相机权限结果");
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogUtils.d(TAG, "handleCameraPermissionResult() | 相机权限授予成功");
            handleTakePhoto();
        } else {
            LogUtils.d(TAG, "handleCameraPermissionResult() | 相机权限授予失败");
            ToastUtils.show("相机权限被拒绝，无法拍照");
            // 引导用户到设置页面开启权限（用户选择不再询问时）
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                launchAppSettings();
            }
        }
    }

    /**
     * 启动应用设置页面
     */
    private void launchAppSettings() {
        LogUtils.d(TAG, "launchAppSettings() 启动应用设置页面");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        ToastUtils.show("请在设置中开启相机权限");
    }

    /**
     * 处理Finish确认对话框
     */
    private void handleFinishConfirmation() {
        LogUtils.d(TAG, "handleFinishConfirmation() 处理Finish确认");
        if (isPreviewBackgroundChanged) {
            YesNoAlertDialog.show(this, "背景更换问题", "是否确定背景图片设置？", new YesNoAlertDialog.OnDialogResultListener() {
					@Override
					public void onYes() {
						mBgSourceUtils.commitPreviewSourceToCurrent();
						isCommitSettings = true;
						finish();
						Intent mainIntent = new Intent(BackgroundSettingsActivity.this, MainActivity.class);
						mainIntent.putExtra(MainActivity.EXTRA_ISRELOAD_BACKGROUNDVIEW, true);
						startActivity(mainIntent);
						LogUtils.d(TAG, "handleFinishConfirmation() | 确认设置，启动MainActivity并刷新背景");
					}

					@Override
					public void onNo() {
						isCommitSettings = true;
						finish();
						LogUtils.d(TAG, "handleFinishConfirmation() | 取消设置，关闭页面");
					}
				});
        } else {
            isCommitSettings = true;
            finish();
        }
    }

    /**
     * 启动图片裁剪
     * @param isFreeCrop 是否自由裁剪
     */
    private void startImageCrop(boolean isFreeCrop) {
        LogUtils.d(TAG, String.format("startImageCrop() | 是否自由裁剪：%b", isFreeCrop));
        BackgroundBean previewBean = mBgSourceUtils.getPreviewBackgroundBean();
        if (previewBean == null) {
            LogUtils.e(TAG, "startImageCrop() | 预览Bean为空");
            ToastUtils.show("裁剪失败：无有效图片");
            return;
        }
        int width = isFreeCrop ? 0 : mBackgroundView.getWidth();
        int height = isFreeCrop ? 0 : mBackgroundView.getHeight();
        ImageCropUtils.startImageCrop(BackgroundSettingsActivity.this,
									  previewBean,
									  width,
									  height,
									  isFreeCrop,
									  REQUEST_CROP_IMAGE);
        LogUtils.d(TAG, String.format("startImageCrop() | 目标尺寸：%dx%d", width, height));
    }
}

