package cc.winboll.studio.libappbase.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libappbase.dialogs.DebugHostDialog;
import cc.winboll.studio.libappbase.models.APPInfo;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026-01-11 12:23:00
 * @LastEditTime 2026-01-24 20:50:00
 * @Describe AboutView 原生实现关于页面，无第三方依赖，适配API30；抽象通用功能控件（邮件/网页跳转），支持调试工具入口动态显隐，集成应用正版校验、调试地址配置弹窗
 */
public class AboutView extends LinearLayout {
    // ===================================== 全局常量 =====================================
    public static final String TAG = "AboutView";
    public static final int MSG_APPUPDATE_CHECKED = 0;

    // 固定链接/邮件常量
    private static final String WINBOLL_OFFICIAL_HOME = "https://www.winboll.cc";
    private static final String EMAIL_TITLE = "联系WinBoLLStudio";
    private static final String EMAIL_ADDRESS = "studio@winboll.cc";
    private static final String EMAIL_TYPE = "message/rfc822";

    // 布局尺寸常量（dp）
    private static final int PADDING_LARGE = 32;
    private static final int PADDING_MID = 16;
    private static final int PADDING_SMALL = 8;
    private static final int ICON_SIZE = 48;
    private static final int ITEM_ICON_SIZE = 24;

    // 服务器默认地址常量
    private static final String SERVER_DEBUG_HOST = "https://yun-preivew.winboll.cc";
    private static final String SERVER_RELEASE_HOST = "https://yun.winboll.cc";

    // ===================================== 核心成员属性 =====================================
    // 上下文与业务实体
    private Context mContext;
    private APPInfo mAPPInfo;
    private OnRequestDevUserInfoAutofillListener mOnRequestDevUserInfoAutofillListener;

    // 应用基础信息
    private String mszAppName = "";
    private String mszAppVersionName = "";
    private String mszAppDescription = "";
    private String mszHomePage = "";
    private String mszGitea = "";
    private String mszAppGitName = "";
    private String mszAppAPKName = "";
    private String mszAppAPKFolderName = "";
    private String mszCurrentAppPackageName = "";
    private String mszReleaseAPKName = "";
    private volatile String mszNewestAppPackageName = "";
    private String mszWinBoLLServerHost = "";
    private int mnAppIcon = 0;
    private boolean mIsAddDebugTools = false;

    // 调试视图
    private EditText metDevUserName;
    private EditText metDevUserPassword;

    // ===================================== 页面视图控件 =====================================
    private DebugSwitchInfoImageView ivAppIcon;
    private TextView tvAppNameVersion;
    private TextView tvAppDesc;
    private LinearLayout llFunctionContainer;
	private ImageButton ibSebugStepOver;
    private ImageButton ibDebugUnlock;
    private ImageButton ibWinBoLLHostDialog;

    // ===================================== 构造方法（按参数从少到多排序） =====================================
    public AboutView(Context context) {
        super(context);
        LogUtils.d(TAG, "AboutView(Context)：代码创建视图，执行默认初始化");
        this.mContext = context;
        initDefaultParams();
        initViewFromXml();
    }

//    public AboutView(Context context, APPInfo appInfo) {
//        super(context);
//        LogUtils.d(TAG, "AboutView(Context,APPInfo)：传入应用信息，appName=" + (appInfo == null ? "null" : appInfo.getAppName()));
//        this.mContext = context;
//        this.mAPPInfo = appInfo;
//        initViewFromXml();
//        initAll();
//    }

    public AboutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtils.d(TAG, "AboutView(Context,AttributeSet)：XML布局引用，执行默认初始化");
        this.mContext = context;
        initDefaultParams();
        initViewFromXml();
    }

    public AboutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtils.d(TAG, "AboutView(Context,AttributeSet,int)：XML布局+样式配置，defStyleAttr=" + defStyleAttr);
        this.mContext = context;
        initDefaultParams();
        initViewFromXml();
    }

    // ===================================== 对外公开方法 =====================================
    /**
     * 一站式初始化所有关于页逻辑，包含参数、应用信息、页面视图全流程
     */
    public void initAll() {
        LogUtils.d(TAG, "initAll()：开始一站式初始化，APPInfo是否为空=" + (mAPPInfo == null));
        if (mAPPInfo == null) {
            LogUtils.w(TAG, "initAll()：初始化终止，APPInfo为null");
            return;
        }
        initDefaultParams();
        initAPPBaseInfo();
        initAPPVersionInfo();
        initServerConfig();
        initAPPLinkInfo();
        initReleaseAPKInfo();
        initAboutPageView();
        LogUtils.d(TAG, "initAll()：所有初始化流程执行完成");
    }

    /**
     * 重置应用信息并重新初始化页面，支持动态更新关于页内容
     * @param appInfo 新的应用信息实体
     */
//    public void setAPPInfoAndInit(APPInfo appInfo) {
//        LogUtils.d(TAG, "setAPPInfoAndInit()：重置应用信息，appName=" + (appInfo == null ? "null" : appInfo.getAppName()));
//        this.mAPPInfo = appInfo;
//        if (llFunctionContainer != null) llFunctionContainer.removeAllViews();
//        initAll();
//        LogUtils.d(TAG, "setAPPInfoAndInit()：应用信息重置+页面重构完成");
//    }

    /**
     * 设置应用信息，兼容旧调用逻辑，设置后自动重构页面
     * @param appInfo 应用核心信息实体
     */
    public void setAPPInfo(APPInfo appInfo) {
        LogUtils.d(TAG, "setAPPInfo()：设置应用信息，appName=" + (appInfo == null ? "null" : appInfo.getAppName()));
        this.mAPPInfo = appInfo;
        if (llFunctionContainer != null) llFunctionContainer.removeAllViews();
        initAll();
    }

    /**
     * 设置调试信息自动填充监听，供调试场景回调使用
     * @param l 监听回调接口实现
     */
    public void setOnRequestDevUserInfoAutofillListener(OnRequestDevUserInfoAutofillListener l) {
        LogUtils.d(TAG, "setOnRequestDevUserInfoAutofillListener()：设置调试信息填充监听完成");
        this.mOnRequestDevUserInfoAutofillListener = l;
    }

    // ===================================== 内部初始化方法 =====================================
    /**
     * 初始化默认兜底参数，防止空指针，为后续初始化做基础铺垫
     */
    private void initDefaultParams() {
        LogUtils.d(TAG, "initDefaultParams()：开始初始化默认参数");
        mszWinBoLLServerHost = GlobalApplication.isDebugging() ? SERVER_DEBUG_HOST : SERVER_RELEASE_HOST;
        mnAppIcon = (mnAppIcon == 0) ? R.drawable.ic_winboll : mnAppIcon;
        mIsAddDebugTools = false;
        LogUtils.d(TAG, "initDefaultParams()：默认参数初始化完成，服务器地址=" + mszWinBoLLServerHost + "，应用图标ID=" + mnAppIcon);
    }

    /**
     * 加载XML布局并绑定所有视图控件，初始化按钮点击事件
     */
    private void initViewFromXml() {
		LogUtils.d(TAG, "initViewFromXml()：开始加载布局并绑定控件");
		View.inflate(mContext, R.layout.layout_about_view, this);
		// 基础控件绑定
		ivAppIcon = findViewById(R.id.iv_app_icon);
		tvAppNameVersion = findViewById(R.id.tv_app_name_version);
		tvAppDesc = findViewById(R.id.tv_app_desc);
		llFunctionContainer = findViewById(R.id.ll_function_container);
		// 功能按钮绑定
		ibSebugStepOver = findViewById(R.id.ib_debug_step_over);
		ibDebugUnlock = findViewById(R.id.ib_debug_unlock);
		ibWinBoLLHostDialog = findViewById(R.id.ib_winbollhostdialog);

		// 调试按钮统一只在调试模式显示
		ibWinBoLLHostDialog.setVisibility(GlobalApplication.isDebugging() ? View.VISIBLE : View.GONE);
		//ibDebugUnlock.setVisibility(GlobalApplication.isDebugging() ? View.VISIBLE : View.GONE);
		ibSebugStepOver.setVisibility(GlobalApplication.isDebugging() ? View.VISIBLE : View.GONE);

		// 绑定按钮点击事件
		setBtnClickListener();
		LogUtils.d(TAG, "initViewFromXml()：布局加载+控件绑定+事件初始化完成");
	}

    /**
     * 从APPInfo实体读取应用基础核心配置，赋值到本地属性
     */
    private void initAPPBaseInfo() {
        LogUtils.d(TAG, "initAPPBaseInfo()：开始读取APPInfo基础配置");
        if (mAPPInfo == null) {
            LogUtils.w(TAG, "initAPPBaseInfo()：跳过执行，APPInfo为null");
            return;
        }
        mszAppName = mAPPInfo.getAppName() == null ? "" : mAPPInfo.getAppName();
        mszAppAPKFolderName = mAPPInfo.getAppAPKFolderName() == null ? "" : mAPPInfo.getAppAPKFolderName();
        mszAppAPKName = mAPPInfo.getAppAPKName() == null ? "" : mAPPInfo.getAppAPKName();
        mszAppGitName = mAPPInfo.getAppGitName() == null ? "" : mAPPInfo.getAppGitName();
        mszAppDescription = mAPPInfo.getAppDescription() == null ? "" : mAPPInfo.getAppDescription();
        mnAppIcon = (mAPPInfo.getAppIcon() != 0) ? mAPPInfo.getAppIcon() : mnAppIcon;
        mIsAddDebugTools = mAPPInfo.isAddDebugTools();
        LogUtils.d(TAG, "initAPPBaseInfo()：基础配置读取完成，应用名=" + mszAppName + "，调试开关=" + mIsAddDebugTools);
    }

    /**
     * 从包管理中获取当前应用版本号，初始化版本相关信息
     */
    private void initAPPVersionInfo() {
        LogUtils.d(TAG, "initAPPVersionInfo()：开始初始化应用版本信息");
        try {
            mszAppVersionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "initAPPVersionInfo()：获取版本号失败，默认赋值unknown", e);
            mszAppVersionName = "unknown";
        }
        mszCurrentAppPackageName = String.format("%s_%s.apk", mszAppVersionName, mszAppVersionName);
        LogUtils.d(TAG, "initAPPVersionInfo()：版本信息初始化完成，版本号=" + mszAppVersionName + "，当前APK名=" + mszCurrentAppPackageName);
    }

    /**
     * 初始化服务器相关配置，预留扩展接口
     */
    private void initServerConfig() {
        LogUtils.d(TAG, "initServerConfig()：服务器配置初始化，预留扩展接口");
    }

    /**
     * 初始化应用相关链接（主页+Git源码地址），根据分支配置动态拼接Git地址
     */
    private void initAPPLinkInfo() {
        LogUtils.d(TAG, "initAPPLinkInfo()：开始初始化应用链接信息");
        if (mAPPInfo == null) {
            LogUtils.w(TAG, "initAPPLinkInfo()：跳过执行，APPInfo为null");
            return;
        }
        mszHomePage = mAPPInfo.getAppHomePage() == null ? "" : mAPPInfo.getAppHomePage();
        // 拼接Git地址，兼容无分支配置场景
        if (mAPPInfo.getAppGitAPPBranch() == null || mAPPInfo.getAppGitAPPBranch().trim().isEmpty()) {
            mszGitea = String.format("https://gitea.winboll.cc/%s/%s", mAPPInfo.getAppGitOwner(), mszAppGitName);
        } else {
            mszGitea = String.format("https://gitea.winboll.cc/%s/%s/src/branch/%s/%s",
									 mAPPInfo.getAppGitOwner(), mszAppGitName,
									 mAPPInfo.getAppGitAPPBranch(), mAPPInfo.getAppGitAPPSubProjectFolder());
        }
        LogUtils.d(TAG, "initAPPLinkInfo()：链接信息初始化完成，应用主页=" + mszHomePage + "，Git地址=" + mszGitea);
    }

    /**
     * 初始化正式版APK信息，去除beta后缀适配正式包命名规范
     */
    private void initReleaseAPKInfo() {
        LogUtils.d(TAG, "initReleaseAPKInfo()：开始初始化正式版APK信息");
        String szReleaseAppVersionName = "unknown";
        try {
            String szSubBetaSuffix = subBetaSuffix(mContext.getPackageName());
            szReleaseAppVersionName = mContext.getPackageManager().getPackageInfo(szSubBetaSuffix, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "initReleaseAPKInfo()：获取正式版版本号失败", e);
        }
        mszReleaseAPKName = String.format("%s_%s.apk", mszAppAPKName, szReleaseAppVersionName);
        LogUtils.d(TAG, "initReleaseAPKInfo()：正式版APK信息初始化完成，APK名=" + mszReleaseAPKName);
    }

    /**
     * 核心视图组装：赋值基础信息到控件，添加通用功能项到容器
     */
    private void initAboutPageView() {
        LogUtils.d(TAG, "initAboutPageView()：开始组装关于页视图");
        // 赋值基础信息
        ivAppIcon.setImageResource(mnAppIcon);
        tvAppNameVersion.setText(String.format("%s %s", mszAppName, mszAppVersionName));
        if (mszAppDescription.isEmpty()) {
            tvAppDesc.setVisibility(GONE);
        } else {
            tvAppDesc.setVisibility(VISIBLE);
            tvAppDesc.setText(mszAppDescription);
        }
        // 添加通用功能项
        addFunctionView(new WebJumpFunctionItemView(mContext, "WinBoLL 主页", WINBOLL_OFFICIAL_HOME, R.drawable.ic_winboll));
        addFunctionView(new EmailFunctionItemView(mContext, "联系邮箱", "WinBoLLStudio<studio@winboll.cc>", R.drawable.ic_winboll));
        if (!mszHomePage.isEmpty()) {
            addFunctionView(new WebJumpFunctionItemView(mContext, "应用APK下载地址", mszHomePage, R.drawable.ic_winboll));
        }
        if (!mszGitea.isEmpty()) {
            addFunctionView(new WebJumpFunctionItemView(mContext, "应用Git源码地址", mszGitea, R.drawable.ic_winboll));
        }
        LogUtils.d(TAG, "initAboutPageView()：视图组装完成，功能项加载完毕");
    }

    // ===================================== 调试解锁弹窗 =====================================
    private void showDebugUnlockDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        dialog.setTitle("应用调试解锁");
        dialog.setCanceledOnTouchOutside(true);

        final EditText etToken = new EditText(mContext);
        etToken.setHint("请输入调试Token");
        dialog.setView(etToken);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "调试解锁", (DialogInterface.OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "关闭", (DialogInterface.OnClickListener) null);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputToken = etToken.getText().toString().trim();
                        String savedToken = DebugSwitchInfoImageView.getDebugToken();
                        if (savedToken != null && savedToken.equals(inputToken)) {
                            GlobalApplication.setIsDebugging(true);
                            GlobalApplication.saveDebugStatus(GlobalApplication.getInstance());
                            Toast.makeText(mContext, "调试解锁成功，重启应用后生效", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "调试Token不匹配", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    // ===================================== 内部工具/事件方法 =====================================
    /**
     * 绑定功能按钮点击事件，处理正版校验、调试地址配置弹窗唤起
     */
    private void setBtnClickListener() {
        LogUtils.d(TAG, "setBtnClickListener()：开始绑定功能按钮点击事件");
        // 取消调试状态按钮
        ibSebugStepOver.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "ibSebugStepOver onClick：取消调试状态按钮已点击");
					GlobalApplication.setIsDebugging(false);
					GlobalApplication.saveDebugStatus(GlobalApplication.getInstance());
					ToastUtils.show("已取消调试状态，重启应用可生效。");
				}
			});
        
        // 调试地址配置弹窗
        ibWinBoLLHostDialog.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "ibWinBoLLHostDialog onClick：唤起调试地址配置弹窗");
					new DebugHostDialog(mContext).show();
				}
			});

        // 应用调试解锁按钮
        ibDebugUnlock.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "ibDebugUnlock onClick：弹出调试解锁对话框");
					showDebugUnlockDialog();
				}
			});
        LogUtils.d(TAG, "setBtnClickListener()：功能按钮点击事件绑定完成");
    }

    /**
     * 添加功能项视图到容器，统一设置间距
     * @param view 功能项视图
     */
    private void addFunctionView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = 0;
        llFunctionContainer.addView(view, params);
    }

    /**
     * dp转px工具方法，适配不同屏幕密度，保证布局一致性
     * @param dpValue dp单位尺寸
     * @return 转换后的px单位尺寸
     */
    private int dp2px(int dpValue) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * 去除包名beta后缀，适配正式版包名规范，静态方法支持外部调用
     * @param input 原始包名
     * @return 去除beta后缀后的正式包名
     */
    public static String subBetaSuffix(String input) {
        LogUtils.d(TAG, "subBetaSuffix()：执行包名beta后缀去除，原始包名=" + input);
        if (input != null && input.endsWith(".beta")) {
            String result = input.substring(0, input.length() - ".beta".length());
            LogUtils.d(TAG, "subBetaSuffix()：处理成功，正式包名=" + result);
            return result;
        }
        LogUtils.d(TAG, "subBetaSuffix()：无需处理，包名不含beta后缀");
        return input == null ? "" : input;
    }

    // ===================================== 内部抽象通用功能项基类 =====================================
    /**
     * 通用功能项基类，统一样式、布局、视图构建，减少冗余代码
     */
    private abstract class BaseFunctionItemView extends LinearLayout implements OnClickListener {
        protected Context mItemContext;
        protected String mTitle;
        protected String mContent;
        protected int mIconRes;

        public BaseFunctionItemView(Context context, String title, String content, int iconRes) {
            super(context);
            this.mItemContext = context;
            this.mTitle = title;
            this.mContent = content;
            this.mIconRes = iconRes;
            initItemLayout();
            initItemViews();
            setOnClickListener(this);
        }

        /**
         * 统一初始化功能项布局属性
         */
        private void initItemLayout() {
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp2px(PADDING_MID), dp2px(PADDING_SMALL), dp2px(PADDING_MID), dp2px(PADDING_SMALL));
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setClickable(true);
            setBackground(create_item_background());
        }

        /**
         * 创建带1像素边框的背景drawable
         */
        private android.graphics.drawable.Drawable create_item_background() {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setStroke(1, mItemContext.getResources().getColor(R.color.gray_300));
            drawable.setCornerRadius(4);
            boolean isNightMode = (mItemContext.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            drawable.setColor(isNightMode ? mItemContext.getResources().getColor(R.color.gray_800) : mItemContext.getResources().getColor(android.R.color.white));
            return drawable;
        }

        /**
         * 统一构建功能项视图（左侧图标+右侧标题/内容）
         */
        private void initItemViews() {
            // 左侧图标
            if (mIconRes != 0) {
                ImageView ivIcon = new ImageView(mItemContext);
                LayoutParams iconParams = new LayoutParams(dp2px(ITEM_ICON_SIZE), dp2px(ITEM_ICON_SIZE));
                iconParams.rightMargin = dp2px(PADDING_SMALL);
                ivIcon.setLayoutParams(iconParams);
                ivIcon.setImageResource(mIconRes);
                addView(ivIcon);
            }
            // 右侧文本容器
            LinearLayout llText = new LinearLayout(mItemContext);
            llText.setOrientation(VERTICAL);
            llText.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
            addView(llText);
            // 标题
            TextView tvTitle = new TextView(mItemContext);
            tvTitle.setText(mTitle);
            tvTitle.setTextSize(16);
            boolean isNightMode = (mItemContext.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            tvTitle.setTextColor(isNightMode ? mItemContext.getResources().getColor(R.color.gray_500) : mItemContext.getResources().getColor(R.color.gray_900));
            llText.addView(tvTitle);
            // 内容
            TextView tvContent = new TextView(mItemContext);
            tvContent.setText(mContent);
            tvContent.setTextSize(14);
            tvContent.setTextColor(getContentTextColor());
            tvContent.setPadding(0, dp2px(PADDING_SMALL), 0, 0);
            llText.addView(tvContent);
        }

        /**
         * 子类抽象方法：指定内容文本颜色
         * @return 颜色值
         */
        protected abstract int getContentTextColor();
    }

    // ===================================== 内部邮件功能项子类 =====================================
    /**
     * 邮件类功能控件，实现专属邮件唤起逻辑，双方案兼容（纯邮件客户端/通用邮件应用）
     */
    private class EmailFunctionItemView extends BaseFunctionItemView {
        public EmailFunctionItemView(Context context, String title, String content, int iconRes) {
            super(context, title, content, iconRes);
        }

        @Override
        protected int getContentTextColor() {
            return mItemContext.getResources().getColor(R.color.blue_normal);
        }

        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "EmailFunctionItemView onClick：触发邮件唤起逻辑");
            // 方案1：纯邮件客户端唤起
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + EMAIL_ADDRESS));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_TITLE);
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (emailIntent.resolveActivity(mItemContext.getPackageManager()) != null) {
                mItemContext.startActivity(emailIntent);
                LogUtils.d(TAG, "EmailFunctionItemView：纯邮件客户端唤起成功");
                return;
            }
            // 方案2：通用邮件应用兜底
            Intent fallbackIntent = new Intent(Intent.ACTION_SEND);
            fallbackIntent.setType(EMAIL_TYPE);
            fallbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_ADDRESS});
            fallbackIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_TITLE);
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (fallbackIntent.resolveActivity(mItemContext.getPackageManager()) != null) {
                mItemContext.startActivity(fallbackIntent);
                LogUtils.d(TAG, "EmailFunctionItemView：通用邮件应用唤起成功");
            } else {
                ToastUtils.show("未找到可发送邮件的应用");
                LogUtils.w(TAG, "EmailFunctionItemView：邮件唤起失败，无可用邮件应用");
            }
        }
    }

    // ===================================== 内部网页跳转功能项子类 =====================================
    /**
     * 网页跳转类功能控件，实现专属网页唤起逻辑，包含空地址校验、异常捕获
     */
    private class WebJumpFunctionItemView extends BaseFunctionItemView {
        public WebJumpFunctionItemView(Context context, String title, String content, int iconRes) {
            super(context, title, content, iconRes);
        }

        @Override
        protected int getContentTextColor() {
            return mItemContext.getResources().getColor(R.color.blue_normal);
        }

        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "WebJumpFunctionItemView onClick：触发网页跳转，地址=" + mContent);
            if (mContent.isEmpty()) {
                ToastUtils.show("跳转地址为空");
                LogUtils.w(TAG, "WebJumpFunctionItemView：网页跳转失败，地址为空");
                return;
            }
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContent));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mItemContext.startActivity(browserIntent);
                LogUtils.d(TAG, "WebJumpFunctionItemView：网页跳转成功");
            } catch (Exception e) {
                LogUtils.e(TAG, "WebJumpFunctionItemView：网页跳转失败", e);
                ToastUtils.show("链接无法打开");
            }
        }
    }

    // ===================================== 内部回调接口 =====================================
    /**
     * 调试信息自动填充回调接口
     */
    public interface OnRequestDevUserInfoAutofillListener {
        void requestAutofill(EditText etDevUserName, EditText etDevUserPassword);
    }
}

