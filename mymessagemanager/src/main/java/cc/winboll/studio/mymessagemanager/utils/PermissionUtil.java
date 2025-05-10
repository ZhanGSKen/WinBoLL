package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/06/01 13:02:30
 * @Describe 应用权限申请工具类
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.BaseActivity;
import cc.winboll.studio.shared.log.LogUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.util.List;

public class PermissionUtil {

    public static final String TAG = "PermissionUtil";

    public static boolean checkAppPermission(Context context) {
        if (!XXPermissions.isGranted(context, Permission.READ_CONTACTS)) {
            LogUtils.i(TAG, "Permission.READ_CONTACTS error.");
            return false;
        }
        if (!XXPermissions.isGranted(context, Permission.Group.STORAGE)) {
            LogUtils.i(TAG, "Permission.Group.STORAGE error.");
            return false;
        }
        if (!XXPermissions.isGranted(context, Permission.READ_SMS)) {
            LogUtils.i(TAG, "Permission.READ_SMS error.");
            return false;
        }
        if (!XXPermissions.isGranted(context, Permission.RECEIVE_SMS)) {
            LogUtils.i(TAG, "Permission.RECEIVE_SMS error.");
            return false;
        }

        return true;
    }

    public static boolean checkAndGetAppPermission(Context context) {
        if (!XXPermissions.isGranted(context, Permission.READ_CONTACTS)) {
            getAppPermission(context, Permission.READ_CONTACTS);
            return false;
        }
        if (!XXPermissions.isGranted(context, Permission.Group.STORAGE)) {
            getAppPermissionsList(context, Permission.Group.STORAGE);
            return false;
        }
        if (!XXPermissions.isGranted(context, Permission.READ_SMS)) {
            getAppPermission(context, Permission.READ_SMS);
            return false;
        }
        if (!XXPermissions.isGranted(context, Permission.RECEIVE_SMS)) {
            getAppPermission(context, Permission.RECEIVE_SMS);
            return false;
        }
        return true;
    }

    //
    // 申请多个权限
    //
    static void getAppPermissionsList(final Context context, final String[] szPermissionList) {
        XXPermissions.with(context)
            // 申请多个权限
            .permission(szPermissionList)
            // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean allGranted) {
                    StringBuilder sb = new StringBuilder();
                    for (String sz : permissions) {
                        sb.append(sz);
                    }
                    if (!allGranted) {

                        showPermissionDialog(context, sb.toString());
                        LogUtils.d(TAG, "获取部分权限成功，但部分权限未正常授予");
                        return;
                    }
                    checkAndGetAppPermission(context);
                    LogUtils.d(TAG, "获取权限成功：" + sb.toString());
                }

                @Override
                public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                    StringBuilder sb = new StringBuilder();
                    for (String sz : permissions) {
                        sb.append(sz);
                    }
                    if (doNotAskAgain) {
                        LogUtils.d(TAG, "被永久拒绝授权，请手动授予应用权限：" + sb.toString());
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        //XXPermissions.startPermissionActivity(context, permissions);
                        showPermissionDialog(context, sb.toString());

                    } else {
                        LogUtils.d(TAG, "获取应用权限失败：" + sb.toString());
                    }
                }
            });
    }

    //
    // 申请单个权限
    //
    static void getAppPermission(final Context context, final String szPermission) {
        XXPermissions.with(context)
            // 申请单个权限
            .permission(szPermission)
            // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean allGranted) {
                    StringBuilder sb = new StringBuilder();
                    for (String sz : permissions) {
                        sb.append(sz);
                    }
                    if (!allGranted) {
                        showPermissionDialog(context, sb.toString());
                        LogUtils.d(TAG, "获取部分权限成功，但部分权限未正常授予");
                        return;
                    }
                    checkAndGetAppPermission(context);
                    LogUtils.d(TAG, "获取权限成功：" + sb.toString());
                }

                @Override
                public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                    StringBuilder sb = new StringBuilder();
                    for (String sz : permissions) {
                        sb.append(sz);
                    }
                    if (doNotAskAgain) {
                        LogUtils.d(TAG, "被永久拒绝授权，请手动授予应用权限");
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        //XXPermissions.startPermissionActivity(context, permissions);
                        showPermissionDialog(context, sb.toString());

                    } else {
                        LogUtils.d(TAG, "获取应用权限失败：" + sb.toString());
                    }
                }
            });
    }


    //
    // 打开应用属性设置页
    //
    static void openSettingIntent(Context context) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    static void showPermissionDialog(final Context context, String szPermissionMessage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle(context.getString(R.string.app_permission_require_info));

        // set dialog message
        alertDialogBuilder
            .setMessage(szPermissionMessage)
            .setCancelable(false)
            .setPositiveButton("Open Permission Dialog", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity
                    //MainActivity.this.finish();
                    AppGoToSettingsUtil appGoToSettingsUtil = new AppGoToSettingsUtil();
                    appGoToSettingsUtil.GoToSetting((BaseActivity)context);
                }
            })
            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                }
            });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
	}
}
