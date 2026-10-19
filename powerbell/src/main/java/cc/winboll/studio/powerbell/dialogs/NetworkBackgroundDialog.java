package cc.winboll.studio.powerbell.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import cc.winboll.studio.powerbell.utils.ImageDownloader;
import cc.winboll.studio.powerbell.views.BackgroundView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/19 20:11
 * @Describe 网络背景使用提示对话框
 * 继承 AndroidX AlertDialog，绑定自定义布局 dialog_networkbackground.xml
 * 适配 API30，基于 Java7 开发，支持网络图片下载、预览与回调
 */
public class NetworkBackgroundDialog extends AlertDialog {
    // ====================== 静态常量（首屏可见，统一管理） ======================
    public static final String TAG = "NetworkBackgroundDialog";
    private static final int MSG_IMAGE_LOAD_SUCCESS = 1001; // 图片加载成功消息标识
    private static final int MSG_IMAGE_LOAD_FAILED = 1002; // 图片加载失败消息标识

    // ====================== 回调接口（紧跟常量，逻辑关联） ======================
    /**
     * 按钮点击回调接口（Java7 接口实现）
     */
    public interface OnDialogClickListener {
        void onConfirm(String szConfirmFilePath); // 确认按钮点击，返回图片路径
        void onCancel();                          // 取消按钮点击
    }

    // ====================== 成员变量（按优先级排序：核心数据→控件引用） ======================
    // 核心数据
    private OnDialogClickListener listener;       // 按钮点击回调
    private Context mContext;                     // 上下文对象
    private Handler mUiHandler;                   // 主线程 Handler，用于接收子线程消息更新 UI
    private String mPreviewFilePath;              // 预览图片文件路径
    private String mPreviewFileUrl;               // 预览图片网络 URL
    private String mDownloadSavedPath;            // 下载图片保存路径
    // 控件引用
    private TextView tvTitle;                     // 对话框标题
    private TextView tvContent;                   // 对话框内容
    private Button btnCancel;                     // 取消按钮
    private Button btnConfirm;                    // 确认按钮
    private Button btnPreview;                    // 预览按钮
    private EditText etURL;                       // URL 输入框
    private BackgroundView mBackgroundView;       // 背景预览视图

    // ====================== 构造方法（Java7 显式构造，按参数重载排序） ======================
    /**
     * 基础构造（仅传入 Context）
     * @param context 上下文
     */
    public NetworkBackgroundDialog(@NonNull Context context) {
        super(context);
        LogUtils.d(TAG, "NetworkBackgroundDialog: 基础构造初始化");
        initHandler();
        initView();
        setDismissListener();
    }

    /**
     * 带回调的构造（便于外部处理点击事件）
     * @param context 上下文
     * @param listener 按钮点击回调
     */
    public NetworkBackgroundDialog(@NonNull Context context, OnDialogClickListener listener) {
        super(context);
        this.listener = listener;
        LogUtils.d(TAG, "NetworkBackgroundDialog: 带回调构造初始化");
        initHandler();
        initView();
        setDismissListener();
    }

    // ====================== 生命周期相关方法（对话框消失监听、Handler 初始化） ======================
    /**
     * 初始化主线程 Handler，用于接收子线程消息并更新 UI
     */
    private void initHandler() {
        mUiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 对话框已消失时，不再处理 UI 消息
                if (!isShowing()) {
                    LogUtils.d(TAG, "handleMessage: 对话框已消失，忽略消息");
                    return;
                }
                switch (msg.what) {
                    case MSG_IMAGE_LOAD_SUCCESS:
                        // 图片加载成功，获取文件路径并设置背景
                        mDownloadSavedPath = (String) msg.obj;
                        LogUtils.d(TAG, String.format("handleMessage: 图片加载成功，保存路径：%s", mDownloadSavedPath));
						int nCurrentPixelColor = BackgroundSourceUtils.getInstance(mContext).getCurrentBackgroundBean().getPixelColor();
						
                        mBackgroundView.loadImage(nCurrentPixelColor, mDownloadSavedPath, true);
                        break;
                    case MSG_IMAGE_LOAD_FAILED:
                        // 图片加载失败，设置默认背景
                        LogUtils.e(TAG, "handleMessage: 图片加载失败");
                        mBackgroundView.setBackgroundResource(R.drawable.ic_launcher);
                        ToastUtils.show("图片预览失败，请检查链接");
                        break;
                    default:
                        break;
                }
            }
        };
        LogUtils.d(TAG, "initHandler: 主线程 Handler 初始化完成");
    }

    /**
     * 设置对话框消失监听：移除 Handler 消息，避免内存泄漏
     */
    private void setDismissListener() {
        this.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					// 对话框消失时，移除所有未处理的消息和回调
					if (mUiHandler != null) {
						mUiHandler.removeCallbacksAndMessages(null);
						LogUtils.d(TAG, "onDismiss: Handler 消息已清理");
					}
					LogUtils.d(TAG, "onDismiss: 对话框已消失");
				}
			});
        LogUtils.d(TAG, "setDismissListener: 对话框消失监听已设置");
    }

    // ====================== 初始化方法（布局、控件、点击事件） ======================
    /**
     * 初始化布局和控件
     */
    private void initView() {
        mContext = this.getContext();
        // 加载自定义布局
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_networkbackground, null);
        // 设置对话框内容视图
        setView(dialogView);

        // 绑定控件
        tvTitle = (TextView) dialogView.findViewById(R.id.tv_dialog_title);
        tvContent = (TextView) dialogView.findViewById(R.id.tv_dialog_content);
        btnCancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        btnConfirm = (Button) dialogView.findViewById(R.id.btn_confirm);
        btnPreview = (Button) dialogView.findViewById(R.id.btn_preview);
        etURL = (EditText) dialogView.findViewById(R.id.et_url);
        mBackgroundView = (BackgroundView) dialogView.findViewById(R.id.bv_background_preview);

        // 控件非空校验
        if (tvTitle == null || tvContent == null || btnCancel == null || btnConfirm == null || btnPreview == null
			|| etURL == null || mBackgroundView == null) {
            LogUtils.e(TAG, "initView: 控件绑定失败，请检查布局ID是否正确");
            dismiss();
            return;
        }

        // 加载初始图片
        mBackgroundView.setBackgroundResource(R.drawable.blank100x100);
        // 设置按钮点击事件
        setButtonClickListeners();

        LogUtils.d(TAG, "initView: 布局和控件初始化完成");
    }

    /**
     * 设置按钮点击监听
     */
    private void setButtonClickListeners() {
        // 取消按钮：关闭对话框 + 回调外部
        btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "onClick: 取消按钮点击");
					BackgroundSourceUtils utils = BackgroundSourceUtils.getInstance(mContext);
					utils.setCurrentSourceToPreview();

					dismiss(); // 关闭对话框
					if (listener != null) {
						listener.onCancel();
						LogUtils.d(TAG, "onClick: 取消回调已执行");
					}
				}
			});

        // 确认按钮：关闭对话框 + 回调外部
        btnConfirm.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "onClick: 确认按钮点击");
					dismiss(); // 关闭对话框
					if (TextUtils.isEmpty(mDownloadSavedPath)) {
						ToastUtils.show("未下载图片。");
						LogUtils.w(TAG, "onClick: 确认失败，未下载图片");
						return;
					}
					if (listener != null) {
						listener.onConfirm(mDownloadSavedPath);
						LogUtils.d(TAG, String.format("onClick: 确认回调已执行，图片路径：%s", mDownloadSavedPath));
					}
				}
			});

        // 图片预览按钮：预览输入框地址图片
        btnPreview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "onClick: 预览按钮点击");
					downloadImageToAlbumAndPreview();
				}
			});

        LogUtils.d(TAG, "setButtonClickListeners: 按钮点击监听已设置");
    }

    // ====================== 业务逻辑方法（图片下载、预览） ======================
    /**
     * 下载网络图片并预览
     */
    void downloadImageToAlbumAndPreview() {
        mPreviewFileUrl = etURL.getText().toString().trim();
        if (TextUtils.isEmpty(mPreviewFileUrl)) {
            ToastUtils.show("请输入图片URL");
            LogUtils.w(TAG, "downloadImageToAlbumAndPreview: 图片URL为空");
            return;
        }

        LogUtils.d(TAG, String.format("downloadImageToAlbumAndPreview: 开始下载图片，URL：%s", mPreviewFileUrl));
        ImageDownloader.getInstance(mContext).downloadImage(mPreviewFileUrl, new ImageDownloader.DownloadCallback() {
				@Override
				public void onSuccess(String savePath) {
					LogUtils.d(TAG, String.format("onSuccess: 图片下载成功，保存路径：%s", savePath));
					// 发送消息到主线程，携带图片路径
					Message successMsg = mUiHandler.obtainMessage(MSG_IMAGE_LOAD_SUCCESS, savePath);
					mUiHandler.sendMessage(successMsg);
				}

				@Override
				public void onFailure(String errorMsg) {
					LogUtils.e(TAG, String.format("onFailure: 图片下载失败，错误信息：%s", errorMsg));
					ToastUtils.show("下载失败：" + errorMsg);
					// 发送图片加载失败消息
					Message failMsg = mUiHandler.obtainMessage(MSG_IMAGE_LOAD_FAILED);
					mUiHandler.sendMessage(failMsg);
				}
			});
    }

    /**
     * 根据文件路径设置 BackgroundView 背景（主线程调用）
     * @param previewFilePath 图片文件路径
     */
    private void previewBackground(String previewFilePath) {
        if (TextUtils.isEmpty(previewFilePath)) {
            LogUtils.w(TAG, "previewBackground: 预览文件路径为空");
            return;
        }

        FileInputStream fis = null;
        try {
            File imageFile = new File(previewFilePath);
            if (!imageFile.exists()) {
                ToastUtils.show("图片文件不存在：" + previewFilePath);
                LogUtils.e(TAG, String.format("previewBackground: 图片文件不存在，路径：%s", previewFilePath));
                mBackgroundView.setBackgroundResource(R.drawable.ic_launcher);
                return;
            }

            // 预览背景
            mPreviewFilePath = previewFilePath;
            BackgroundSourceUtils utils = BackgroundSourceUtils.getInstance(mContext);
            utils.saveFileToPreviewBean(new File(mPreviewFilePath), mPreviewFileUrl);
            mBackgroundView.loadByBackgroundBean(utils.getPreviewBackgroundBean());

            LogUtils.d(TAG, String.format("previewBackground: 图片预览成功，路径：%s", previewFilePath));
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("previewBackground: 图片预览失败，错误信息：%s", e.getMessage()), e);
            mBackgroundView.setBackgroundResource(R.drawable.ic_launcher);
        } finally {
            // Java7 手动关闭流，避免资源泄漏
            if (fis != null) {
                try {
                    fis.close();
                    LogUtils.d(TAG, "previewBackground: 文件输入流已关闭");
                } catch (IOException e) {
                    LogUtils.e(TAG, String.format("previewBackground: 关闭文件输入流失败，错误信息：%s", e.getMessage()), e);
                }
            }
        }
    }

    // ====================== 对外提供方法（灵活适配不同场景） ======================
    /**
     * 对外提供方法：修改对话框标题
     * @param title 标题文本
     */
    public void setTitle(String title) {
        if (tvTitle != null && !TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
            LogUtils.d(TAG, String.format("setTitle: 对话框标题已修改为：%s", title));
        }
    }

    /**
     * 对外提供方法：修改对话框内容
     * @param content 内容文本
     */
    public void setContent(String content) {
        if (tvContent != null && !TextUtils.isEmpty(content)) {
            tvContent.setText(content);
            LogUtils.d(TAG, String.format("setContent: 对话框内容已修改为：%s", content));
        }
    }

    /**
     * 对外提供方法：设置按钮点击回调（替代带参构造）
     * @param listener 按钮点击回调
     */
    public void setOnDialogClickListener(OnDialogClickListener listener) {
        this.listener = listener;
        LogUtils.d(TAG, "setOnDialogClickListener: 按钮点击回调已设置");
    }
}

