package cc.winboll.studio.winboll.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.models.TermuxButtonModel;

/**
 * 自定义Termux功能按钮控件
 * 绑定TermuxButtonModel实体数据，拦截点击事件做确认弹窗逻辑判断
 * isCommitted为true直接执行点击事件，为false弹出确认对话框二次确认
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026/04/30 10:57:00
 * @EditTime 2026/04/30 13:52:15
 */
public class TermuxButton extends Button {

    public static final String TAG = "TermuxButton";

    /** 绑定按钮对应数据实体 */
    private TermuxButtonModel buttonModel;
    /** 保存外部设置的原始点击监听 */
    private OnClickListener originClickListener;

    //==================== 构造方法 ====================
    /**
     * 代码动态创建控件构造
     * @param context 上下文
     */
    public TermuxButton(Context context) {
        super(context);
        LogUtils.d(TAG, "TermuxButton 无参构造执行，上下文：" + context);
        initView(null, null);
    }

    /**
     * XML布局引用控件基础构造
     * @param context 上下文
     * @param attrs XML属性集
     */
    public TermuxButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtils.d(TAG, "TermuxButton XML构造执行");
        initView(attrs, null);
    }

    /**
     * XML布局带自定义属性构造
     * @param context 上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TermuxButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtils.d(TAG, "TermuxButton 带样式属性构造执行");
        initView(attrs, null);
    }

    /**
     * 高版本Android完整全参构造
     * @param context 上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     * @param defStyleRes 默认样式资源
     */
    public TermuxButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LogUtils.d(TAG, "TermuxButton 全参构造执行");
        initView(attrs, null);
    }

    /**
     * 直接传入Model初始化控件构造
     * @param context 上下文
     * @param model 按钮数据实体
     */
    public TermuxButton(Context context, TermuxButtonModel model) {
        super(context);
        LogUtils.d(TAG, "TermuxButton Model入参构造执行");
        initView(null, model);
    }

    //==================== 核心初始化 ====================
    /**
     * 控件统一初始化方法
     * @param attrs XML属性集合
     * @param model 绑定数据实体
     */
    private void initView(AttributeSet attrs, TermuxButtonModel model) {
        this.buttonModel = model;

        // 按钮基础默认配置
        setClickable(true);
        setFocusable(true);

        // 解析XML布局自定义属性
        if (attrs != null) {
            parseXmlCustomAttr(attrs);
        }

        // 同步Model内按钮名称到控件展示文本
        refreshButtonText();
        // 绑定自定义拦截点击事件
        setCustomClickEvent();
    }

    /**
     * 解析XML布局属性，读取原生android:text与自定义属性赋值到Model
     * @param attrs XML属性集
     */
    private void parseXmlCustomAttr(AttributeSet attrs) {
        if (buttonModel == null) {
            buttonModel = new TermuxButtonModel();
            LogUtils.d(TAG, "自动初始化空的TermuxButtonModel实体");
        }

        // 读取原生android:text作为按钮名称
        String androidText = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
        // 读取自定义扩展属性
        String exeCommand = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "exeCommand");
        String workDir = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "workDir");
        String isCommittedStr = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "isCommitted");
        String commitTitle = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "commitTitle");
        String commitInfo = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "commitInfo");

        // 属性赋值绑定
        if (androidText != null) {
            buttonModel.setButtonName(androidText);
        }
        if (exeCommand != null) {
            buttonModel.setExeCommand(exeCommand);
        }
        if (workDir != null) {
            buttonModel.setWorkDir(workDir);
        }
        if (isCommittedStr != null) {
            buttonModel.setCommitted(Boolean.parseBoolean(isCommittedStr));
        }
        if (commitTitle != null) {
            buttonModel.setCommitTitle(commitTitle);
        }
        if (commitInfo != null) {
            buttonModel.setCommitInfo(commitInfo);
        }

        LogUtils.d(TAG, "XML属性解析完成，按钮名称：" + androidText);
    }

    /**
     * 同步Model中buttonName，更新按钮展示文字
     */
    private void refreshButtonText() {
        if (buttonModel != null) {
            setText(buttonModel.getButtonName());
        }
    }

    //==================== 点击事件相关 ====================
    /**
     * 重写点击监听设置，保存外部原始点击事件
     * @param l 外部传入点击监听
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        this.originClickListener = l;
        LogUtils.d(TAG, "保存外部原始按钮点击监听");
    }

    /**
     * 自定义拦截按钮点击逻辑
     * isCommitted=true 直接执行原始点击事件
     * isCommitted=false 弹出确认二次弹窗
     */
    private void setCustomClickEvent() {
        super.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (buttonModel == null) {
						LogUtils.d(TAG, "无绑定Model，直接执行原始点击事件");
						if (originClickListener != null) {
							originClickListener.onClick(view);
						}
						return;
					}

					boolean commitState = buttonModel.isCommitted();
					LogUtils.d(TAG, "按钮点击触发，isCommitted状态：" + commitState);
					if (commitState) {
						// 无需确认，直接执行原有点击任务
						if (originClickListener != null) {
							originClickListener.onClick(view);
						}
					} else {
						// 需要二次确认，弹出提示对话框
						showCommitDialog();
					}
				}
			});
    }

    /**
     * 弹出操作确认对话框
     * 标题：commitTitle  内容：commitInfo
     * 取消：关闭弹窗无操作  确定：执行原始点击事件
     */
    private void showCommitDialog() {
        Context context = getContext();
        String dialogTitle = buttonModel.getCommitTitle();
        String dialogMsg = buttonModel.getCommitInfo();

        // 空值默认兜底处理
        if (dialogTitle == null || "".equals(dialogTitle)) {
            dialogTitle = "温馨提示";
        }
        if (dialogMsg == null || "".equals(dialogMsg)) {
            dialogMsg = "确定要执行该操作吗？";
        }

        LogUtils.d(TAG, "弹出确认对话框，标题：" + dialogTitle);
        new AlertDialog.Builder(context)
			.setTitle(dialogTitle)
			.setMessage(dialogMsg)
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					LogUtils.d(TAG, "对话框点击取消，终止操作");
				}
			})
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					LogUtils.d(TAG, "对话框点击确定，继续执行操作");
					if (originClickListener != null) {
						originClickListener.onClick(TermuxButton.this);
					}
				}
			})
			.setCancelable(false)
			.show();
    }

    //==================== Getter & Setter ====================
    public TermuxButtonModel getButtonModel() {
        return buttonModel;
    }

    /**
     * 设置绑定按钮数据实体，自动刷新按钮展示文字
     * @param buttonModel 数据实体类
     */
    public void setButtonModel(TermuxButtonModel buttonModel) {
        this.buttonModel = buttonModel;
        LogUtils.d(TAG, "外部设置ButtonModel，自动刷新按钮文本");
        refreshButtonText();
    }

}

