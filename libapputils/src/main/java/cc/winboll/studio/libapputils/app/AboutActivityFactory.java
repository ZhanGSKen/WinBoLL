package cc.winboll.studio.libapputils.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/02/06 08:45:23
 * @Describe 关于活动窗口的介绍窗口工厂
 */
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libapputils.activities.AboutActivity;
import cc.winboll.studio.libapputils.app.AboutActivityFactory;
import cc.winboll.studio.libapputils.bean.APPInfo;

public class AboutActivityFactory {

    public static final String TAG = "AboutActivityFactory";

    public static APPInfo buildDefaultAPPInfo() {
        String szBranchName = "";
        
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName("APP");
        appInfo.setAppIcon(cc.winboll.studio.libapputils.R.drawable.ic_winboll);
        appInfo.setAppDescription("APP Description");
        appInfo.setAppGitName("APP");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(szBranchName);
        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=APP");
        appInfo.setAppAPKName("APP");
        appInfo.setAppAPKFolderName("APP");
        return appInfo;
    }

    public static void showAboutActivity(Context context, APPInfo appInfo) {
        /*String szPN = ((IWinBollActivity)context).getActivityPackageName();
        //String szPN = context.getPackageName();
        String szBranchName = "";
        if (szPN != null) {
            //String szPN = "cc.winboll.studio.apputils.beta";
            String regex = "cc\\.winboll\\.studio\\.([^.]+)\\.*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(szPN);
            if (matcher.find()) {
                szBranchName = matcher.group(1);
            }
        }*/
        //ToastUtils.show(szPN);
        
        Intent intent = new Intent(context, AboutActivity.class);
        intent.putExtra(AboutActivity.EXTRA_APPINFO, (appInfo == null) ? AboutActivityFactory.buildDefaultAPPInfo():appInfo);
        WinBollActivityManager.getInstance(context).startWinBollActivity(context, intent, AboutActivity.class);
    }
}
