package cc.winboll.studio.libaes.views;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/03/24 15:08:52
 * @Describe WinBoLL应用介绍视图
 */
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libaes.models.APPInfo;
import cc.winboll.studio.libaes.utils.AppVersionUtils;
import cc.winboll.studio.libaes.utils.PrefUtils;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.io.IOException;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutView extends LinearLayout {

    public static final String TAG = "AboutView";

    public static final int MSG_APPUPDATE_CHECKED = 0;

    static Context _mContext;
    APPInfo mAPPInfo;

    //WinBoLLServiceStatusView mWinBoLLServiceStatusView;
    OnRequestDevUserInfoAutofillListener mOnRequestDevUserInfoAutofillListener;
    String mszAppName = "";
    String mszAppAPKFolderName = "";
    String mszAppAPKName = "";
    String mszAppGitName = "";
    String mszAppVersionName = "";
    String mszCurrentAppPackageName = "";
    boolean mIsAddDebugTools;
    volatile String mszNewestAppPackageName = "";
    String mszAppDescription = "";
    String mszHomePage = "";
    String mszGitea = "";
    int mnAppIcon = 0;
    String mszWinBoLLServerHost;
    String mszReleaseAPKName;
    EditText metDevUserName;
    EditText metDevUserPassword;

    public AboutView(Context context, APPInfo appInfo) {
        super(context);
        _mContext = context;

        setAPPInfo(appInfo);
        initView(context);
    }

    public AboutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _mContext = context;

        initView(context, attrs);
    }

    public void setAPPInfo(APPInfo appInfo) {
        mAPPInfo = appInfo;
    }

    APPInfo createAppInfo(Context context, AttributeSet attrs) {
        APPInfo appInfo = new APPInfo();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AboutView);
        appInfo.setAppName(typedArray.getString(R.styleable.AboutView_app_name));
        appInfo.setAppAPKFolderName(typedArray.getString(R.styleable.AboutView_app_apkfoldername));
        appInfo.setAppAPKName(typedArray.getString(R.styleable.AboutView_app_apkname));
        appInfo.setAppGitName(typedArray.getString(R.styleable.AboutView_app_gitname));
        appInfo.setAppGitOwner(typedArray.getString(R.styleable.AboutView_app_gitowner));
        appInfo.setAppGitAPPBranch(typedArray.getString(R.styleable.AboutView_app_gitappbranch));
        appInfo.setAppGitAPPSubProjectFolder(typedArray.getString(R.styleable.AboutView_app_gitappsubprojectfolder));
        appInfo.setAppDescription(typedArray.getString(R.styleable.AboutView_appdescription));
        appInfo.setAppIcon(typedArray.getResourceId(R.styleable.AboutView_appicon, R.drawable.ic_winboll));
        appInfo.setIsAddDebugTools(typedArray.getBoolean(R.styleable.AboutView_is_adddebugtools, false));
        // 返回一个绑定资源结束的信号给资源
        typedArray.recycle();
        return appInfo;
    }

    void initView(Context context) {
        mszAppName = mAPPInfo.getAppName();
        mszAppAPKFolderName = mAPPInfo.getAppAPKFolderName();
        mszAppAPKName = mAPPInfo.getAppAPKName();
        mszAppGitName = mAPPInfo.getAppGitName();
        mszAppDescription = mAPPInfo.getAppDescription();
        mnAppIcon = mAPPInfo.getAppIcon();

        mszWinBoLLServerHost = GlobalApplication.isDebugging() ?  "https://yun-preivew.winboll.cc": "https://yun.winboll.cc";

        try {
            mszAppVersionName = _mContext.getPackageManager().getPackageInfo(_mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        mszCurrentAppPackageName = mszAppAPKName + "_" + mszAppVersionName + ".apk";
        mszHomePage = mAPPInfo.getAppHomePage();
        //mszHomePage = mszWinBoLLServerHost + "/studio/details.php?app=" + mszAppAPKFolderName;
        if (mAPPInfo.getAppGitAPPBranch().equals("")) {
            mszGitea = "https://gitea.winboll.cc/" + mAPPInfo.getAppGitOwner() + "/" + mszAppGitName;
        } else {
            mszGitea = "https://gitea.winboll.cc/" + mAPPInfo.getAppGitOwner() + "/" + mszAppGitName + "/src/branch/" + mAPPInfo.getAppGitAPPBranch() + "/" + mAPPInfo.getAppGitAPPSubProjectFolder();
        }
		
		addView(createAboutPage());

        // 初始化标题栏
        //setSubtitle(getContext().getString(R.string.text_about));

//        LinearLayout llMain = findViewById(R.id.viewaboutLinearLayout1);
//        llMain.addView(createAboutPage());

        // 就读取正式版应用包版本号，设置 Release 应用包文件名
        String szReleaseAppVersionName = "";
        try {
            //LogUtils.d(TAG, String.format("mContext.getPackageName() %s", mContext.getPackageName()));
            String szSubBetaSuffix = subBetaSuffix(_mContext.getPackageName());
            //LogUtils.d(TAG, String.format("szSubBetaSuffix : %s", szSubBetaSuffix));
            szReleaseAppVersionName = _mContext.getPackageManager().getPackageInfo(szSubBetaSuffix, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        mszReleaseAPKName = mszAppAPKName + "_" + szReleaseAppVersionName + ".apk";

    }

    void initView(Context context, AttributeSet attrs) {
        mAPPInfo = createAppInfo(context, attrs);
        initView(context);
    }

    public static String subBetaSuffix(String input) {
        if (input.endsWith(".beta")) {
            return input.substring(0, input.length() - ".beta".length());
        }
        return input;
    }

    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_APPUPDATE_CHECKED : {
                        /*//检查当前应用包文件名是否是测试版，如果是就忽略检查
                         if(mszCurrentAppPackageName.matches(".*_\\d+\\.\\d+\\.\\d+-beta.*\\.apk")) {
                         ToastUtils.show("APP is the beta Version. Version check ignore.");
                         return;
                         }*/

//                        if (!AppVersionUtils.isHasNewStageReleaseVersion(mszReleaseAPKName, mszNewestAppPackageName)) {
//                            ToastUtils.delayedShow("Current app is the newest.", 5000);
//                        } 
                        if (!AppVersionUtils.isHasNewVersion2(mszCurrentAppPackageName, mszNewestAppPackageName)) {
                            ToastUtils.show("Current app is the newest.");
                        } else {
                            String szMsg = "Current app is :\n[ " + mszCurrentAppPackageName
                                + " ]\nThe last app is :\n[ " + mszNewestAppPackageName
                                + " ]\nIs download the last app?";
                            YesNoAlertDialog.show(_mContext, "Application Update Prompt", szMsg, mIsDownlaodUpdateListener);
                        }
                        break;
                    }
            }
        }
    };

    protected View createAboutPage() {
        // 定义 GitWeb 按钮
        //
        Element elementGitWeb = new Element(_mContext.getString(R.string.gitea_home), R.drawable.ic_winboll);
        elementGitWeb.setOnClickListener(mGitWebOnClickListener);
        // 定义检查更新按钮
        //
        /*Element elementAppUpdate = new Element(_mContext.getString(R.string.app_update), R.drawable.ic_winboll);
        elementAppUpdate.setOnClickListener(mAppUpdateOnClickListener);
		*/

        String szAppInfo = "";
        try {
            szAppInfo = mszAppName + " "
                + _mContext.getPackageManager().getPackageInfo(_mContext.getPackageName(), 0).versionName
                + "\n" + mszAppDescription;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        AboutPage aboutPage = new AboutPage(_mContext);
        aboutPage.setDescription(szAppInfo)
            //.isRTL(false)
            //.setCustomFont(String) // or Typeface
            .setImage(mnAppIcon)
            //.addItem(versionElement)
            //.addItem(adsElement)
            //.addGroup("Connect with us")
            .addEmail("WinBoLLStudio<studio@winboll.cc>")
            .addWebsite(mszHomePage)
            .addItem(elementGitWeb);
            //.addItem(elementAppUpdate);
        //.addFacebook("the.medy")
        //.addTwitter("medyo80")
        //.addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
        //.addPlayStore("com.ideashower.readitlater.pro")
        //.addGitHub("medyo")
        //.addInstagram("medyo80")
        //.create();

        /*if (mAPPInfo.isAddDebugTools()) {
            // 定义应用调试按钮
            //
            Element elementAppMode;
            if (GlobalApplication.isDebugging()) {
                elementAppMode = new Element(_mContext.getString(R.string.app_normal), R.drawable.ic_winboll);
                elementAppMode.setOnClickListener(mAppNormalOnClickListener);
            } else {
                elementAppMode = new Element(_mContext.getString(R.string.app_debug), R.drawable.ic_winboll);
                elementAppMode.setOnClickListener(mAppDebugOnClickListener);
            }
            aboutPage.addItem(elementAppMode);
        }*/

        return aboutPage.create();
    }

    View.OnClickListener mAppDebugOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //ToastUtils.show("mAppDebugOnClickListener");
            setApp2DebugMode(_mContext);
        }
    };

    View.OnClickListener mAppNormalOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //ToastUtils.show("mAppNormalOnClickListener");
            setApp2NormalMode(_mContext);
        }
    };

    public static void setApp2DebugMode(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            //intent.setAction(cc.winboll.studio.libapputils.intent.action.DEBUGVIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            GlobalApplication.setIsDebugging(true);
            GlobalApplication.saveDebugStatus((GlobalApplication)_mContext.getApplicationContext());

            WinBoLLActivityManager.getInstance().finishAll();
            context.startActivity(intent);
        } 
    }

    public static void setApp2NormalMode(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            GlobalApplication.setIsDebugging(false);
            GlobalApplication.saveDebugStatus((GlobalApplication)_mContext.getApplicationContext());

            WinBoLLActivityManager.getInstance().finishAll();
            context.startActivity(intent);
        } 
    }

    View.OnClickListener mGitWebOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mszGitea));
            _mContext.startActivity(browserIntent);
        }
    };

    View.OnClickListener mAppUpdateOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            ToastUtils.show("Start app update checking.");
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String szUrl = mszWinBoLLServerHost + "/studio/details.php?app=" + mszAppAPKFolderName;
                        // 构建包含认证信息的请求
                        String credential = "";
                        if (GlobalApplication.isDebugging()) {
                            credential = Credentials.basic(metDevUserName.getText().toString(), metDevUserPassword.getText().toString());
                            PrefUtils.saveString(_mContext, "metDevUserName", metDevUserName.getText().toString());
                            PrefUtils.saveString(_mContext, "metDevUserPassword", metDevUserPassword.getText().toString());
                        } else {
                            String username = "WinBoLL";
                            String password = "WinBoLLPowerByZhanGSKen";
                            credential = Credentials.basic(username, password);
                        }

                        Request request = new Request.Builder()
                            .url(szUrl)
                            .header("Accept", "text/plain") // 设置正确的Content-Type头
                            .header("Authorization", credential)
                            .build();
                        OkHttpClient client = new OkHttpClient();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    // 处理网络请求失败
                                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        LogUtils.d(TAG, "Unexpected code " + response, Thread.currentThread().getStackTrace());
                                        return;
                                    }

                                    try {
                                        // 读取响应体作为字符串，注意这里可能需要解码
                                        String text = response.body().string();
                                        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(text);
                                        LogUtils.v(TAG, doc.text());

                                        // 使用id选择器找到具有特定id的元素
                                        org.jsoup.nodes.Element elementWithId = doc.select("#LastRelease").first(); // 获取第一个匹配的元素

                                        // 提取并打印元素的文本内容
                                        mszNewestAppPackageName = elementWithId.text();
                                        //ToastUtils.delayedShow(text + "\n" + mszNewestAppPackageName, 5000);

                                        mHandler.sendMessage(mHandler.obtainMessage(MSG_APPUPDATE_CHECKED));
                                    } catch (Exception e) {
                                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                                    }
                                }
                            });
                    }
                }).start();
        }
    };

    YesNoAlertDialog.OnDialogResultListener mIsDownlaodUpdateListener = new YesNoAlertDialog.OnDialogResultListener() {
        @Override
        public void onYes() {
            String szUrl = mszWinBoLLServerHost + "/studio/download.php?appname=" + mszAppAPKFolderName + "&apkname=" + mszNewestAppPackageName;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(szUrl));
            _mContext.startActivity(browserIntent);
        }

        @Override
        public void onNo() {
        }
    };

    public interface OnRequestDevUserInfoAutofillListener {
        void requestAutofill(EditText etDevUserName, EditText etDevUserPassword);
    }

    public void setOnRequestDevUserInfoAutofillListener(OnRequestDevUserInfoAutofillListener l) {
        mOnRequestDevUserInfoAutofillListener = l;
    }
}
