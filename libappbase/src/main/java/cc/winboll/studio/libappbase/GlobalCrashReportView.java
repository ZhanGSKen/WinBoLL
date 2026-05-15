package cc.winboll.studio.libappbase;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;
import cc.winboll.studio.libappbase.R;
import android.content.res.Resources;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 20:21
 * @Describe 全局崩溃报告视图控件
 * 用于展示应用崩溃信息，包含顶部工具栏和崩溃日志文本区域，支持自定义配色
 */
public class GlobalCrashReportView extends LinearLayout {

	// 日志标签
	public static final String TAG = "GlobalCrashReportView";

	// 上下文对象
	private Context mContext;
	// 顶部工具栏（标题栏）
	private Toolbar mToolbar;
	// 标题文字颜色
	private int mTitleColor;
	// 标题栏背景颜色
	private int mTitleBackgroundColor;
	// 日志文本颜色
	private int mTextColor;
	// 日志区域背景颜色
	private int mTextBackgroundColor;
	// 崩溃日志显示文本控件
	private TextView mTvReport;

	/**
	 * 构造方法：仅上下文
	 * @param context 上下文
	 */
	public GlobalCrashReportView(Context context) {
		super(context);
		mContext = context;
		// 初始化默认配置（无自定义属性）
		initDefaultConfig();
	}

	/**
	 * 构造方法：上下文 + 自定义属性
	 * @param context 上下文
	 * @param attrs 自定义属性集合
	 */
	public GlobalCrashReportView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// 初始化视图（解析自定义属性）
		initView(attrs);
	}

	/**
	 * 构造方法：上下文 + 自定义属性 + 样式属性
	 * @param context 上下文
	 * @param attrs 自定义属性集合
	 * @param defStyleAttr 样式属性
	 */
	public GlobalCrashReportView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		// 初始化视图（解析自定义属性）
		initView(attrs);
	}

	/**
	 * 构造方法：上下文 + 自定义属性 + 样式属性 + 样式资源
	 * @param context 上下文
	 * @param attrs 自定义属性集合
	 * @param defStyleAttr 样式属性
	 * @param defStyleRes 样式资源
	 */
	public GlobalCrashReportView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mContext = context;
		// 初始化视图（解析自定义属性）
		initView(attrs);
	}

	/**
	 * 设置标题文字颜色
	 * @param titleColor 颜色值（如 Color.WHITE 或 #FFFFFF）
	 */
	public void setTitleColor(int titleColor) {
		this.mTitleColor = titleColor;
		// 实时更新工具栏标题颜色
		if (mToolbar != null) {
			mToolbar.setTitleTextColor(titleColor);
			mToolbar.setSubtitleTextColor(titleColor);
		}
	}

	/**
	 * 获取标题文字颜色
	 * @return 标题文字颜色值
	 */
	public int getTitleColor() {
		return mTitleColor;
	}

	/**
	 * 设置标题栏背景颜色
	 * @param titleBackgroundColor 颜色值（如 Color.BLACK 或 #000000）
	 */
	public void setTitleBackgroundColor(int titleBackgroundColor) {
		this.mTitleBackgroundColor = titleBackgroundColor;
		// 实时更新工具栏背景颜色
		if (mToolbar != null) {
			mToolbar.setBackgroundColor(titleBackgroundColor);
		}
	}

	/**
	 * 获取标题栏背景颜色
	 * @return 标题栏背景颜色值
	 */
	public int getTitleBackgroundColor() {
		return mTitleBackgroundColor;
	}

	/**
	 * 设置日志文本颜色
	 * @param textColor 颜色值（如 Color.BLACK 或 #000000）
	 */
	public void setTextColor(int textColor) {
		this.mTextColor = textColor;
		// 实时更新日志文本颜色
		if (mTvReport != null) {
			mTvReport.setTextColor(textColor);
		}
	}

	/**
	 * 获取日志文本颜色
	 * @return 日志文本颜色值
	 */
	public int getTextColor() {
		return mTextColor;
	}

	/**
	 * 设置日志区域背景颜色
	 * @param textBackgroundColor 颜色值（如 Color.WHITE 或 #FFFFFF）
	 */
	public void setTextBackgroundColor(int textBackgroundColor) {
		this.mTextBackgroundColor = textBackgroundColor;
		// 实时更新日志区域和主布局背景颜色
		if (mTvReport != null) {
			mTvReport.setBackgroundColor(textBackgroundColor);
		}
		setBackgroundColor(textBackgroundColor);
	}

	/**
	 * 获取日志区域背景颜色
	 * @return 日志区域背景颜色值
	 */
	public int getTextBackgroundColor() {
		return mTextBackgroundColor;
	}

	/**
	 * 初始化默认配置（无自定义属性时使用）
	 */
	private void initDefaultConfig() {
		// 设置默认配色（使用 debugTextColor 属性）
		Resources.Theme theme = mContext.getTheme();
		mTitleColor = theme.getResources().getColor(android.R.color.holo_green_dark);
		mTitleBackgroundColor = Color.GRAY;
		mTextColor = obtainDebugTextColor(theme);
		mTextBackgroundColor = Color.WHITE;
		// 加载布局
		inflateView();
		// 初始化控件样式
		initWidgetStyle();
	}

	private int obtainDebugTextColor(Resources.Theme theme) {
		int[] attrs = new int[] { cc.winboll.studio.libappbase.R.attr.themeDebug };
		TypedArray themeTypedArray = theme.obtainStyledAttributes(attrs);
		int themeResId = themeTypedArray.getResourceId(0, 0);
		themeTypedArray.recycle();
		if (themeResId != 0) {
			int[] debugAttrs = new int[] { cc.winboll.studio.libappbase.R.attr.debugTextColor };
			TypedArray debugTypedArray = theme.obtainStyledAttributes(themeResId, debugAttrs);
			int color = debugTypedArray.getColor(0, Color.GRAY);
			debugTypedArray.recycle();
			return color;
		}
		return Color.GRAY;
	}

	/**
	 * 初始化视图（解析自定义属性 + 加载布局 + 设置样式）
	 * @param attrs 自定义属性集合
	 */
	private void initView(AttributeSet attrs) {
		// 解析自定义属性（关联 attrs.xml 中的 GlobalCrashActivity 样式）
		TypedArray typedArray = mContext.obtainStyledAttributes(
			attrs,
			R.styleable.GlobalCrashActivity,
			R.attr.themeDebug,
			0
		);

		// 读取自定义属性值（无设置时使用默认值）
		mTitleColor = typedArray.getColor(
			R.styleable.GlobalCrashActivity_colorTittle,
			Color.BLACK
		);
		mTitleBackgroundColor = typedArray.getColor(
			R.styleable.GlobalCrashActivity_colorTittleBackgound, // 注：原拼写错误（Backgound→Background），保持与 attrs.xml 一致
			Color.BLACK
		);
		mTextColor = obtainDebugTextColor(mContext.getTheme());
		mTextBackgroundColor = typedArray.getColor(
			R.styleable.GlobalCrashActivity_colorTextBackgound, // 注：原拼写错误，保持与 attrs.xml 一致
			Color.WHITE
		);

		// 回收 TypedArray，避免内存泄漏
		typedArray.recycle();

		// 加载布局文件
		inflateView();
		// 初始化控件样式
		initWidgetStyle();
	}

	/**
	 * 加载布局文件
	 */
	private void inflateView() {
		// 加载自定义布局（R.layout.view_globalcrashreport）
		inflate(mContext, R.layout.view_globalcrashreport, this);
		// 绑定控件
		mToolbar = findViewById(R.id.viewglobalcrashreportToolbar1);
		mTvReport = findViewById(R.id.viewglobalcrashreportTextView1);
	}

	/**
	 * 初始化控件样式（设置配色和基础属性）
	 */
	private void initWidgetStyle() {
		// 配置工具栏样式
		if (mToolbar != null) {
			mToolbar.setTitleTextColor(mTitleColor);
			mToolbar.setSubtitleTextColor(mTitleColor);
		}

		// 配置日志文本控件样式
		if (mTvReport != null) {
			mTvReport.setTextColor(mTextColor);
			mTvReport.setSingleLine(false);
			mTvReport.setHorizontallyScrolling(false);
		}
	}

	/**
	 * 设置崩溃报告内容到文本控件
	 * @param report 崩溃日志字符串（通常包含异常信息、调用栈等）
	 */
	public void setReport(String report) {
		if (mTvReport != null) {
			mTvReport.setText(report);
		}
	}

	/**
	 * 获取顶部工具栏对象（用于外部设置标题、添加菜单等）
	 * @return Toolbar 实例
	 */
	public Toolbar getToolbar() {
		return mToolbar;
	}

	/**
	 * 更新工具栏菜单文字颜色（与标题颜色保持一致）
	 * 需在菜单加载完成后调用（如 Toolbar 加载菜单后）
	 */
	public void updateMenuStyle() {
		if (mToolbar == null) return;

		// 获取工具栏菜单
		Menu menu = mToolbar.getMenu();
		if (menu == null || menu.size() == 0) return;

		// 遍历所有菜单项，设置文字颜色
		for (int i = 0; i < menu.size(); i++) {
			MenuItem menuItem = menu.getItem(i);
			String title = menuItem.getTitle().toString();
			// 使用 SpannableString 设置文字颜色
			SpannableString spanString = new SpannableString(title);
			spanString.setSpan(
				new ForegroundColorSpan(mTitleColor),
				0,
				spanString.length(),
				0 //  Spannable.SPAN_INCLUSIVE_EXCLUSIVE（默认值，包含起始位置，不包含结束位置）
			);
			menuItem.setTitle(spanString);
		}
	}
}

