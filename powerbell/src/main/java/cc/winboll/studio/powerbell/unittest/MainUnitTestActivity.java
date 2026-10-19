package cc.winboll.studio.powerbell.unittest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.utils.FileUtils;
import cc.winboll.studio.powerbell.utils.ImageCropUtils;
import cc.winboll.studio.powerbell.utils.ImageUtils;
import cc.winboll.studio.powerbell.views.BackgroundView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 单元测试页面
 * 功能：测试背景图加载、图片裁剪、双重刷新预览等功能
 * 适配：Java7 | API30 | 私有目录文件操作 | 无Uri冲突
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Describe 单元测试页：验证图片处理与背景预览相关逻辑
 */
public class MainUnitTestActivity extends AppCompatActivity {
    // ====================== 静态常量区（置顶归类，消除魔法值） ======================
    public static final String TAG = "MainUnitTestActivity";
    public static final int REQUEST_CROP_IMAGE = 0;
    private static final String ASSETS_TEST_IMAGE_PATH = "unittest/unittest-miku.png";
    private static final long FILE_MIN_SIZE = 100L;
    private static final long DOUBLE_REFRESH_DELAY = 200L;

    // ====================== 成员变量区（按功能分层，移除所有Uri相关） ======================
    private BackgroundView mBackgroundView;
    private String mAppPrivateDirPath;
    private File mPrivateTestImageFile; // 仅用File，不用Uri
    private File mPrivateCropImageFile;
    private BackgroundBean mPreviewBackgroundBean;

    // ====================== 生命周期方法（按执行顺序：onCreate→onActivityResult） ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "=== 页面 onCreate 启动 ===");

        initBaseParams();
        initViewAndEvent();
        copyAssetsTestImageToPrivateDir();
        initBackgroundBean();
        doubleRefreshPreview();

        ToastUtils.show("单元测试页面启动完成");
        LogUtils.d(TAG, "=== 页面 onCreate 初始化结束 ===");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d(TAG, String.format("=== onActivityResult 回调 | requestCode=%d | resultCode=%d ===", requestCode, resultCode));
        if (requestCode == REQUEST_CROP_IMAGE) {
            handleCropResult(resultCode);
        }
    }

    // ====================== 初始化相关方法（基础参数→视图→背景Bean） ======================
    /**
     * 初始化基础参数：私有目录、测试文件
     */
    private void initBaseParams() {
        LogUtils.d(TAG, "initBaseParams：初始化基础参数");
        // 初始化私有目录（无需权限，无UID冲突）
        mAppPrivateDirPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/PowerBellTest/";
        File privateDir = new File(mAppPrivateDirPath);
        if (!privateDir.exists()) {
            boolean isDirCreated = privateDir.mkdirs();
            LogUtils.d(TAG, String.format("initBaseParams：创建私有目录 | 路径=%s | 结果=%b", mAppPrivateDirPath, isDirCreated));
        }

        // 初始化测试文件与裁剪文件（无Uri）
        File refFile = new File(ASSETS_TEST_IMAGE_PATH);
        String uniqueTestName = FileUtils.createUniqueFileName(refFile) + ".png";
        String uniqueCropName = uniqueTestName.replace(".png", "_crop.png");
        mPrivateTestImageFile = new File(mAppPrivateDirPath, uniqueTestName);
        mPrivateCropImageFile = new File(mAppPrivateDirPath, uniqueCropName);

        LogUtils.d(TAG, String.format("initBaseParams：测试图路径=%s", mPrivateTestImageFile.getAbsolutePath()));
        LogUtils.d(TAG, String.format("initBaseParams：裁剪图路径=%s", mPrivateCropImageFile.getAbsolutePath()));
    }

    /**
     * 初始化布局与控件事件
     */
    private void initViewAndEvent() {
        LogUtils.d(TAG, "initViewAndEvent：初始化布局与控件事件");
        setContentView(R.layout.activity_mainunittest);
        mBackgroundView = (BackgroundView) findViewById(R.id.backgroundview);

        // 跳转主页面按钮
        Button btnMain = (Button) findViewById(R.id.btn_main_activity);
        btnMain.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "initViewAndEvent：点击按钮→跳转主页面");
					startActivity(new Intent(MainUnitTestActivity.this, MainActivity.class));
				}
			});

        // 裁剪按钮（直接用File路径启动，无Uri）
        Button btnCrop = (Button) findViewById(R.id.btn_test_cropimage);
        btnCrop.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "initViewAndEvent：点击按钮→启动裁剪（File路径版）");
					ToastUtils.show("准备启动图片裁剪");

					if (isFileValid(mPrivateTestImageFile)) {
						startCropTestByFile();
					} else {
						ToastUtils.show("测试图片未准备好，重新拷贝");
						copyAssetsTestImageToPrivateDir();
					}
				}
			});
    }

    /**
     * 初始化背景Bean
     */
    private void initBackgroundBean() {
        LogUtils.d(TAG, "initBackgroundBean：初始化背景Bean");
        mPreviewBackgroundBean = new BackgroundBean();
		mPreviewBackgroundBean.setPixelColor(ImageUtils.getColorAccent(this));
        mPreviewBackgroundBean.setBackgroundFileName(mPrivateTestImageFile.getName());
        mPreviewBackgroundBean.setBackgroundFilePath(mPrivateTestImageFile.getAbsolutePath());
        mPreviewBackgroundBean.setBackgroundScaledCompressFileName(mPrivateCropImageFile.getName());
        mPreviewBackgroundBean.setBackgroundScaledCompressFilePath(mPrivateCropImageFile.getAbsolutePath());
        mPreviewBackgroundBean.setIsUseBackgroundFile(true);
        LogUtils.d(TAG, "initBackgroundBean：背景Bean初始化完成");
    }

    // ====================== 核心业务方法（文件拷贝→裁剪→结果处理→预览刷新） ======================
    /**
     * 从assets拷贝图片到私有目录
     */
    private void copyAssetsTestImageToPrivateDir() {
        LogUtils.d(TAG, "copyAssetsTestImageToPrivateDir：开始拷贝assets图片到私有目录");
        if (isFileValid(mPrivateTestImageFile)) {
            LogUtils.d(TAG, "copyAssetsTestImageToPrivateDir：图片已存在，无需拷贝");
            return;
        }

        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(ASSETS_TEST_IMAGE_PATH);
            FileUtils.copyStreamToFile(inputStream, mPrivateTestImageFile);
            LogUtils.d(TAG, String.format("copyAssetsTestImageToPrivateDir：图片拷贝成功 | 大小=%d字节", mPrivateTestImageFile.length()));
        } catch (IOException e) {
            LogUtils.e(TAG, String.format("copyAssetsTestImageToPrivateDir：图片拷贝失败 | %s", e.getMessage()), e);
            ToastUtils.show("图片准备失败");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, String.format("copyAssetsTestImageToPrivateDir：关闭流失败 | %s", e.getMessage()));
                }
            }
        }
    }

    /**
     * 直接用File启动裁剪（关键：调用ImageCropUtils的File重载方法）
     */
    private void startCropTestByFile() {
        LogUtils.d(TAG, String.format("startCropTestByFile：启动裁剪 | 原图=%s", mPrivateTestImageFile.getAbsolutePath()));

        // 确保输出目录存在
        File cropParent = mPrivateCropImageFile.getParentFile();
        if (!cropParent.exists()) {
            boolean isDirCreated = cropParent.mkdirs();
            LogUtils.d(TAG, String.format("startCropTestByFile：创建裁剪目录 | 路径=%s | 结果=%b", cropParent.getAbsolutePath(), isDirCreated));
        }

        // 调用ImageCropUtils的File参数方法（核心：绕开Uri）
        ImageCropUtils.startImageCrop(
			this,
			mPrivateTestImageFile, // 原图File
			mPrivateCropImageFile, // 输出File
			0,
			0,
			true,
			REQUEST_CROP_IMAGE
        );

        LogUtils.d(TAG, String.format("startCropTestByFile：裁剪请求已发送 | 输出路径=%s", mPrivateCropImageFile.getAbsolutePath()));
        ToastUtils.show("已启动图片裁剪");
    }

    /**
     * 处理裁剪结果（直接校验输出File）
     * @param resultCode 裁剪结果码
     */
    private void handleCropResult(int resultCode) {
//        LogUtils.d(TAG, String.format("handleCropResult：裁剪回调处理 | resultCode=%d", resultCode));
//        if (resultCode == RESULT_OK) {
//            if (isFileValid(mPrivateCropImageFile)) {
//                mBackgroundView.loadImage(mPrivateCropImageFile.getAbsolutePath());
//                LogUtils.d(TAG, String.format("handleCropResult：裁剪成功 | 加载裁剪图=%s", mPrivateCropImageFile.getAbsolutePath()));
//                ToastUtils.show("裁剪成功");
//                mPreviewBackgroundBean.setIsUseBackgroundScaledCompressFile(true);
//                doubleRefreshPreview();
//            } else {
//                LogUtils.e(TAG, "handleCropResult：裁剪成功但输出文件无效");
//                ToastUtils.show("裁剪失败：输出文件无效");
//            }
//        } else if (resultCode == RESULT_CANCELED) {
//            LogUtils.d(TAG, "handleCropResult：裁剪取消");
//            ToastUtils.show("裁剪已取消");
//        } else {
//            LogUtils.e(TAG, String.format("handleCropResult：裁剪失败 | resultCode异常=%d", resultCode));
//            ToastUtils.show("裁剪失败");
//        }
    }

    /**
     * 双重刷新预览，确保背景加载最新数据
     */
    private void doubleRefreshPreview() {
        LogUtils.d(TAG, "doubleRefreshPreview：执行双重刷新预览");
        // 第一重刷新
        try {
            mBackgroundView.loadByBackgroundBean(mPreviewBackgroundBean, true);
            mBackgroundView.setBackgroundColor(mPreviewBackgroundBean.getPixelColor());
            LogUtils.d(TAG, "doubleRefreshPreview：【双重刷新】第一重完成");
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("doubleRefreshPreview：【双重刷新】第一重异常 | %s", e.getMessage()));
            return;
        }

        // 第二重刷新（延迟执行）
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mBackgroundView != null && !isFinishing()) {
						try {
							mBackgroundView.loadByBackgroundBean(mPreviewBackgroundBean, true);
							mBackgroundView.setBackgroundColor(mPreviewBackgroundBean.getPixelColor());
							LogUtils.d(TAG, "doubleRefreshPreview：【双重刷新】第二重完成");
						} catch (Exception e) {
							LogUtils.e(TAG, String.format("doubleRefreshPreview：【双重刷新】第二重异常 | %s", e.getMessage()));
						}
					}
				}
			}, DOUBLE_REFRESH_DELAY);
    }

    // ====================== 工具辅助方法（文件校验） ======================
    /**
     * 校验文件是否有效（存在且大小达标）
     * @param file 待校验文件
     * @return true=有效 false=无效
     */
    private boolean isFileValid(File file) {
        boolean isValid = file != null && file.exists() && file.length() > FILE_MIN_SIZE;
        LogUtils.d(TAG, String.format("isFileValid：文件校验 | 路径=%s | 结果=%b", file != null ? file.getAbsolutePath() : "null", isValid));
        return isValid;
    }
}

