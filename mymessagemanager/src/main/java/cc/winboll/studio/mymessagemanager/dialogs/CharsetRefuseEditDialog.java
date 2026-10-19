package cc.winboll.studio.mymessagemanager.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/13 17:04
 * @Describe 字符集编辑拒绝对话框
 * 包含标题、300dp×300dp多行编辑框、确定/取消按钮（确定在右，取消在左）
 * 支持预制文本初始化、编辑内容回传
 */
public class CharsetRefuseEditDialog extends Dialog {
	public static final String TAG = "CharsetRefuseEditDialog";
    // 文本回传接口
	public interface OnTextConfirmListener {
		void onTextConfirmed(String editText); // 确定按钮点击时回传编辑后的文本
	}

	private final OnTextConfirmListener mListener; // 外部传入的回传接口
	private final String mPreText; // 预制文本框的内容
	private EditText mEditText; // 多行文本编辑框

	/**
	 * 构造函数
	 * @param context 上下文
	 * @param listener 文本回传接口（外部实现）
	 * @param preText 预制的编辑框初始文本
	 */
	public CharsetRefuseEditDialog(Context context, OnTextConfirmListener listener, String preText) {
		super(context);
		this.mListener = listener;
		this.mPreText = preText;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 禁用对话框默认标题，使用自定义标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 初始化对话框布局
		initView();
	}

	/**
	 * 初始化对话框布局（标题+编辑框+按钮）
	 */
	private void initView() {
		// 根布局：垂直线性布局
		LinearLayout rootLayout = new LinearLayout(getContext());
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		rootLayout.setPadding(20, 20, 20, 20);
		rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
									   ViewGroup.LayoutParams.WRAP_CONTENT,
									   ViewGroup.LayoutParams.WRAP_CONTENT
								   ));

		// 1. 添加标题栏
		TextView titleTv = new TextView(getContext());
		titleTv.setText("拒绝显示字符集编辑");
		titleTv.setTextSize(18);
		titleTv.setGravity(Gravity.CENTER);
		titleTv.setPadding(0, 0, 0, 20); // 标题与编辑框间距
		rootLayout.addView(titleTv);

		// 2. 添加多行编辑框（300dp×300dp）
		mEditText = new EditText(getContext());
		// 转换dp为px（适配不同屏幕密度）
		int dp300 = dp2px(getContext(), 300);
		LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp300, dp300);
		mEditText.setLayoutParams(editParams);
		mEditText.setLines(5); // 多行显示
		mEditText.setHint("请输入内容");
		mEditText.setText(mPreText); // 设置预制文本
		mEditText.setSelection(mPreText.length()); // 光标定位到文本末尾
		rootLayout.addView(mEditText);

		// 3. 添加按钮栏（水平布局，确定在右、取消在左）
		LinearLayout btnLayout = new LinearLayout(getContext());
		btnLayout.setOrientation(LinearLayout.HORIZONTAL);
		btnLayout.setGravity(Gravity.RIGHT); // 整体右对齐，实现确定在右、取消在左
		btnLayout.setLayoutParams(new LinearLayout.LayoutParams(
									  ViewGroup.LayoutParams.MATCH_PARENT,
									  ViewGroup.LayoutParams.WRAP_CONTENT
								  ));
		btnLayout.setPadding(0, 20, 0, 0); // 按钮栏与编辑框间距
		rootLayout.addView(btnLayout);

		// 3.1 取消按钮（左侧）
		TextView cancelBtn = new TextView(getContext());
		cancelBtn.setText("取消");
		cancelBtn.setTextSize(16);
		cancelBtn.setPadding(20, 10, 20, 10);
		cancelBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View view) {
					dismiss();
				}
			}); // 关闭对话框
		btnLayout.addView(cancelBtn);

		// 3.2 确定按钮（右侧）
		TextView confirmBtn = new TextView(getContext());
		confirmBtn.setText("确定");
		confirmBtn.setTextSize(16);
		confirmBtn.setPadding(20, 10, 20, 10);
		confirmBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View view) {
					if (mListener != null) {
						// 回传编辑后的文本
						mListener.onTextConfirmed(mEditText.getText().toString().trim());
					}
					dismiss(); // 关闭对话框
				}
			});
		btnLayout.addView(confirmBtn);

		// 设置对话框内容布局
		setContentView(rootLayout);
	}

	/**
	 * dp转px工具方法
	 * @param context 上下文
	 * @param dpValue dp值
	 * @return 对应的px值
	 */
	private int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}

