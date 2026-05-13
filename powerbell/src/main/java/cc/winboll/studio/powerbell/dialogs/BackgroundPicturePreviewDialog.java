package cc.winboll.studio.powerbell.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.activities.BackgroundSettingsActivity;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import cc.winboll.studio.powerbell.utils.UriUtils;
import cc.winboll.studio.powerbell.views.BackgroundView;

/**
 * 背景图片的接收分享文件后的预览对话框
 * 适配 API30，基于 Java7 开发，支持分享图片的Uri解析、预览与确认选择
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/04/25 16:27:53
 * @Describe 背景图片的接收分享文件后的预览对话框
 */
public class BackgroundPicturePreviewDialog extends Dialog {
    // ======================== 静态常量 =========================
    public static final String TAG = "BackgroundPicturePreviewDialog";
    private static final String TOAST_MSG_EMPTY_FILE = "接收到的文件为空。"; // 空文件提示文本

    // ======================== 成员变量 =========================
    private Context mContext; // 上下文对象
    private IOnRecivedPictureListener mIOnRecivedPictureListener; // 图片接收监听
    private Uri mUriRecivedPicture; // 接收的图片Uri
    // 控件对象
    private BackgroundView mBackgroundView; // 背景预览视图
    private Button dialogbackgroundpicturepreviewButton1; // 取消按钮
    private Button dialogbackgroundpicturepreviewButton2; // 确认按钮

    // ======================== 接口定义 =========================
    /**
     * 图片接收监听接口，用于通知确认选择的图片Uri
     */
    public interface IOnRecivedPictureListener {
        void onAcceptRecivedPicture(Uri uriRecivedPicture);
    }

    // ======================== 构造方法 =========================
    public BackgroundPicturePreviewDialog(Context context, IOnRecivedPictureListener iOnRecivedPictureListener) {
        super(context);
        LogUtils.d(TAG, "【BackgroundPicturePreviewDialog】对话框初始化开始");
        // 初始化成员变量
        mContext = context;
        mIOnRecivedPictureListener = iOnRecivedPictureListener;

        // 设置布局与控件
        setContentView(R.layout.dialog_backgroundpicturepreview);
        initViews();
        bindButtonClickEvents();

        // 预览接收的图片
        previewRecivedPicture();
        LogUtils.d(TAG, "【BackgroundPicturePreviewDialog】对话框初始化完成");
    }

    // ======================== 视图初始化方法 =========================
    /**
     * 初始化对话框内所有控件
     */
    private void initViews() {
        mBackgroundView = findViewById(R.id.backgroundview);
        dialogbackgroundpicturepreviewButton1 = findViewById(R.id.dialogbackgroundpicturepreviewButton1);
        dialogbackgroundpicturepreviewButton2 = findViewById(R.id.dialogbackgroundpicturepreviewButton2);
        LogUtils.d(TAG, "【initViews】对话框控件初始化完成");
    }

    // ======================== 事件绑定方法 =========================
    /**
     * 绑定按钮点击事件
     */
    private void bindButtonClickEvents() {
        // 取消按钮：跳转到主页面并关闭对话框
        dialogbackgroundpicturepreviewButton1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LogUtils.d(TAG, "【onClick】点击取消按钮，跳转到主页面");
					Intent intent = new Intent(mContext, MainActivity.class);
					mContext.startActivity(intent);
					dismiss();
					LogUtils.d(TAG, "【onClick】对话框已关闭");
				}
			});

        // 确认按钮：通知监听并关闭对话框
        dialogbackgroundpicturepreviewButton2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "【onClick】点击确认按钮，通知接收图片");
					if (mIOnRecivedPictureListener != null && mUriRecivedPicture != null) {
						mIOnRecivedPictureListener.onAcceptRecivedPicture(mUriRecivedPicture);
						LogUtils.d(TAG, "【onClick】已通知监听，图片Uri：" + mUriRecivedPicture);
					} else {
						LogUtils.w(TAG, "【onClick】监听为空或图片Uri无效，无法通知");
					}
					dismiss();
					LogUtils.d(TAG, "【onClick】对话框已关闭");
				}
			});
        LogUtils.d(TAG, "【bindButtonClickEvents】按钮点击事件绑定完成");
    }

    // ======================== 业务逻辑方法 =========================
    /**
     * 预览接收的分享图片
     */
    private void previewRecivedPicture() {
        LogUtils.d(TAG, "【previewRecivedPicture】开始预览接收的图片");
        // 校验上下文类型
        if (!(mContext instanceof BackgroundSettingsActivity)) {
            LogUtils.e(TAG, "【previewRecivedPicture】上下文不是BackgroundSettingsActivity，无法获取图片Uri");
            Toast.makeText(mContext, TOAST_MSG_EMPTY_FILE, Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        BackgroundSettingsActivity activity = (BackgroundSettingsActivity) mContext;
        // 从Intent中获取图片Uri（优先getData，其次EXTRA_STREAM）
        mUriRecivedPicture = activity.getIntent().getData();
        if (mUriRecivedPicture == null) {
            mUriRecivedPicture = activity.getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            LogUtils.d(TAG, "【previewRecivedPicture】从EXTRA_STREAM获取Uri：" + mUriRecivedPicture);
        } else {
            LogUtils.d(TAG, "【previewRecivedPicture】从getData获取Uri：" + mUriRecivedPicture);
        }

        // 解析Uri为文件路径
        String szSrcImage = UriUtils.getFilePathFromUri(mContext, mUriRecivedPicture);
		//App.notifyMessage(TAG, "szSrcImage : " + szSrcImage);
        if (TextUtils.isEmpty(szSrcImage)) {
            LogUtils.w(TAG, "【previewRecivedPicture】解析的文件路径为空");
            Toast.makeText(mContext, TOAST_MSG_EMPTY_FILE, Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // 加载图片到预览视图
		int nCurrentPixelColor = BackgroundSourceUtils.getInstance(mContext).getCurrentBackgroundBean().getPixelColor();
        mBackgroundView.loadImage(nCurrentPixelColor, szSrcImage, true);
        LogUtils.d(TAG, "【previewRecivedPicture】图片预览完成，文件路径：" + szSrcImage);
    }
}

