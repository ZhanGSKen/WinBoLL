package cc.winboll.studio.libappbase.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/15 13:51
 * @Describe 纯原生 LogTag 专属 Spinner（无 androidx 依赖）
 * 核心特性：1. 继承原生 Spinner，适配全 Android 版本；2. dimen 统一配置所有尺寸；3. 文字大小支持 dp 单位；4. 简化外部初始化
 */
public class LogTagSpinner extends Spinner {
	public static final String TAG = "LogTagSpinner";

	Context mContext;
	// 尺寸缓存（dimen 解析后转 px，避免重复计算）
	private int mSpinnerWidth;       // 控件自身框度（px）
	private int mSpinnerHeight;       // 控件自身高度（px）
	private int mItemWidth;          // 下拉项单个高度（px）
	private int mItemHeight;          // 下拉项单个高度（px）
	private float mTextSizePx;        // 文字大小（px，dp 转译后）
	private int mTextPadding;         // 文字左右内边距（px）

	// 内置适配器（外部无需关心内部实现）
	private ArrayAdapter<String> mLogTagAdapter;


	// -------------------------- 构造方法（原生 Spinner 必重写 3 个）--------------------------
	public LogTagSpinner(Context context) {
		super(context);
		initCoreLogic(context);
	}

	public LogTagSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCoreLogic(context);
	}

	public LogTagSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initCoreLogic(context);
	}

	// -------------------------- 核心初始化（解析资源 + 配置样式 + 初始化适配器）--------------------------
	private void initCoreLogic(Context context) {
		this.mContext = context;
		// 1. 读取 dimen 资源（dp 转 px，核心适配）
		parseDimenResources();
		// 2. 设置 Spinner 自身基础样式（高度、背景）
		configSelfStyle();
		// 3. 初始化适配器（统一选中项+下拉项样式）
		initCustomAdapter();
	}


	/**
	 * 步骤 1：解析 dimen 资源，所有 dp 单位转为 px（跨设备视觉一致）
	 */
	private void parseDimenResources() {
		Context context = this.mContext;
		// 控件自身宽度（dp → px）
		mSpinnerWidth = context.getResources().getDimensionPixelSize(R.dimen.log_spinner_width);
		// 控件自身高度（dp → px）
		mSpinnerHeight = context.getResources().getDimensionPixelSize(R.dimen.log_spinner_height);
		// 下拉项宽度（dp → px）
		mItemWidth = context.getResources().getDimensionPixelSize(R.dimen.log_spinner_item_width);
		// 下拉项高度（dp → px）
		mItemHeight = context.getResources().getDimensionPixelSize(R.dimen.log_spinner_item_height);
		// 文字大小（dp → px，核心：用 getDimensionPixelSize 确保 dp 精准转译）
		mTextSizePx = context.getResources().getDimensionPixelSize(R.dimen.log_spinner_text_size);
		// 文字内边距（dp → px）
		mTextPadding = context.getResources().getDimensionPixelSize(R.dimen.log_spinner_text_padding);

		LogUtils.d("LogTagSpinner", "dimen 解析完成：高度=" + mSpinnerHeight + "px，文字大小=" + mTextSizePx + "px");
	}


	/**
	 * 步骤 2：配置 Spinner 自身样式（覆盖布局属性，统一控制）
	 */
	private void configSelfStyle() {
		// 动态设置控件高度（布局中 layout_height 设 wrap_content 即可，这里统一控制）
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if (layoutParams != null) {
			layoutParams.width = mSpinnerWidth;
			layoutParams.height = mSpinnerHeight;
		} else {
			// 代码创建控件时，手动初始化布局参数
			layoutParams = new ViewGroup.LayoutParams(
				mSpinnerWidth,
				mSpinnerHeight
			);
		}
		setLayoutParams(layoutParams);

		// 统一背景色（外部可通过 setBackground 手动覆盖）
		setBackgroundColor(this.mContext.getColor(R.color.btn_gray_normal));
	}


	/**
	 * 步骤 3：初始化自定义适配器，统一选中项+下拉项样式
	 */
	private void initCustomAdapter() {
		// 用原生系统布局（避免自定义布局，减少依赖），后续重写样式
		mLogTagAdapter = new ArrayAdapter<String>(this.mContext, android.R.layout.simple_spinner_item) {
			// 重写：控制「已选中项」的样式（文字大小、高度、内边距）
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView itemTv = (TextView) super.getView(position, convertView, parent);
				setItemUniformStyle(itemTv);
				return itemTv;
			}

			// 重写：控制「下拉列表项」的样式（必须重写，否则下拉项样式不生效）
			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				TextView itemTv = (TextView) super.getDropDownView(position, convertView, parent);
				setItemUniformStyle(itemTv);
				return itemTv;
			}
		};

		// 绑定下拉项布局（原生系统布局，确保低版本兼容）
		mLogTagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 设置适配器到 Spinner
		setAdapter(mLogTagAdapter);
	}


	/**
	 * 通用方法：统一设置列表项（选中项/下拉项）的样式
	 */
	private void setItemUniformStyle(TextView itemTv) {
		if (itemTv == null) return;

		// 1. 文字大小（核心：按 px 赋值，dp 转译后无二次换算，精准适配）
		itemTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizePx);
		// 2. 列表项高度（固定高度，避免文字多少导致高度不一致）
		itemTv.setWidth(mItemWidth);
		itemTv.setHeight(mItemHeight);
		// 3. 内边距（左右留白，优化排版，避免文字贴边）
		itemTv.setPadding(mTextPadding, 0, mTextPadding, 0);
		// 4. 文字对齐（垂直居中+靠左，符合常规 UI 设计）
		//itemTv.setGravity(View.GRAVITY_CENTER_VERTICAL | View.GRAVITY_START);
// 5. 文字颜色（使用主题属性 ?attr/toolbarTextColor）
		TypedArray ta = mContext.obtainStyledAttributes(new int[] { R.attr.toolbarTextColor });
		int toolbarTextColor = ta.getColor(0, mContext.getResources().getColor(R.color.white));
		ta.recycle();
		itemTv.setTextColor(toolbarTextColor);
		itemTv.setBackgroundColor(this.mContext.getResources().getColor(R.color.btn_gray_normal));
		// 6. 文字溢出处理（最多 2 行，超出省略，避免长标签换行过多）
		itemTv.setSingleLine(false);
		itemTv.setMaxLines(2);
		itemTv.setEllipsize(TextUtils.TruncateAt.END);
	}


	// -------------------------- 外部调用 API（极简用法，无需关心内部逻辑）--------------------------
	/**
	 * 填充日志标签数据（外部核心调用，一行代码搞定）
	 * @param logTagArray 日志标签数组（如：{"TAG_MAIN", "TAG_NET", "TAG_DB"}）
	 */
	public void setLogTagData(String[] logTagArray) {
		if (mLogTagAdapter == null || logTagArray == null || logTagArray.length == 0) {
			LogUtils.w("LogTagSpinner", "填充数据失败：适配器为空或数据无效");
			return;
		}
		// 清空旧数据，添加新数据，刷新适配器
		mLogTagAdapter.clear();
		mLogTagAdapter.addAll(logTagArray);
		mLogTagAdapter.notifyDataSetChanged();
	}

	/**
	 * 设置默认选中的标签（索引从 0 开始）
	 * @param defaultIndex 默认选中索引（需在 setLogTagData 之后调用）
	 */
	public void setDefaultSelectedTag(int defaultIndex) {
		if (mLogTagAdapter == null) return;
		// 索引合法性校验，避免数组越界
		if (defaultIndex >= 0 && defaultIndex < mLogTagAdapter.getCount()) {
			setSelection(defaultIndex);
			LogUtils.d("LogTagSpinner", "默认选中标签：" + mLogTagAdapter.getItem(defaultIndex));
		} else {
			LogUtils.w("LogTagSpinner", "默认选中索引无效：" + defaultIndex);
		}
	}

	/**
	 * 获取当前选中的日志标签（外部业务逻辑调用）
	 * @return 当前选中的标签文字（无选中时返回空字符串）
	 */
	public String getCurrentSelectedTag() {
		Object selectedItem = getSelectedItem();
		return selectedItem != null ? selectedItem.toString() : "";
	}


	// -------------------------- 优化扩展（可选，提升稳定性）--------------------------
	/**
	 * 视图附着到窗口时，确保默认有数据（避免空指针）
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mLogTagAdapter != null && mLogTagAdapter.getCount() == 0) {
			setLogTagData(new String[]{"默认标签"});
		}
	}

	/**
	 * 外部扩展：动态修改文字大小（dp 单位）
	 * @param textSizeDp 目标文字大小（dp）
	 */
	public void updateTextSize(int textSizeDp) {
		mTextSizePx = this.mContext.getResources().getDimensionPixelSize(textSizeDp);
		// 刷新所有列表项样式
		if (mLogTagAdapter != null) {
			mLogTagAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 外部扩展：动态修改文字颜色
	 * @param textColorRes 文字颜色资源 ID（如 R.color.red）
	 */
	public void updateTextColor(int textColorRes) {
		int textColor = this.mContext.getResources().getColor(textColorRes);
		// 刷新选中项颜色
		TextView selectedTv = (TextView) getSelectedView();
		if (selectedTv != null) {
			selectedTv.setTextColor(textColor);
		}
		// 刷新下拉项颜色
		if (mLogTagAdapter != null) {
			mLogTagAdapter.notifyDataSetChanged();
		}
	}
}

