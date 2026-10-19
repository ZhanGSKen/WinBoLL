package cc.winboll.studio.autonfc.nfc;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cc.winboll.studio.autonfc.R;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * 自定义对话框类，用于与用户交互，展示 NFC 相关操作选项
 * 兼容 Java 7 语法
 *
 * @author 豆包&ZhanGSKen
 * @create 2025-08-15
 * @lastModify 2026-03-17
 */
public class ActionDialog extends Dialog {

    private static final String TAG = "ActionDialog";
    private String mNfcData;
    private OnButtonClickListener mClickListener;

    /**
     * 构造函数
     */
    public ActionDialog(Context context) {
        super(context);
        initDialog();
    }

    /**
     * 设置 NFC 数据
     */
    public void setNfcData(String nfcData) {
        this.mNfcData = nfcData;
        LogUtils.d(TAG, "setNfcData() -> " + nfcData);
    }

    /**
     * 设置点击监听
     */
    public void setButtonClickListener(OnButtonClickListener listener) {
        this.mClickListener = listener;
    }

    /**
     * 初始化布局
     */
    private void initDialog() {
        setTitle("请选择操作");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        addButtons(layout);

        setContentView(layout);
    }

    /**
     * 添加按钮
     */
    private void addButtons(LinearLayout layout) {
        // Build 按钮
        Button btnBuild = createButton("Build", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "点击 Build");
					if (mClickListener != null) {
						mClickListener.onBuildClick();
					}
				}
			});
        layout.addView(btnBuild);

        // View 按钮
        Button btnView = createButton("View", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "点击 View");
					if (mClickListener != null) {
						mClickListener.onViewClick();
					}
				}
			});
        layout.addView(btnView);

        // 取消按钮
        Button btnCancel = createButton("Cancel", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "点击 Cancel");
					dismiss();
				}
			});
        layout.addView(btnCancel);
    }

    /**
     * 创建按钮
     */
    private Button createButton(String text, View.OnClickListener listener) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setPadding(10, 10, 10, 10);
        button.setOnClickListener(listener);
        return button;
    }

    /**
     * 回调接口
     */
    public interface OnButtonClickListener {
        void onBuildClick();
        void onViewClick();
        void onCancelClick();
    }
}

