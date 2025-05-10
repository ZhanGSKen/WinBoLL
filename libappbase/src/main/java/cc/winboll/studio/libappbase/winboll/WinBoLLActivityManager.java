package cc.winboll.studio.libappbase.winboll;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/05/10 10:02
 * @Describe 应用活动窗口管理器
 * 参考 ：
 * android 类似微信小程序多任务窗口 及 设置 TaskDescription 修改 icon 和 label
 * https://blog.csdn.net/qq_29364417/article/details/109379915?app_version=6.4.2&code=app_1562916241&csdn_share_tail=%7B%22type%22%3A%22blog%22%2C%22rType%22%3A%22article%22%2C%22rId%22%3A%22109379915%22%2C%22source%22%3A%22weixin_38986226%22%7D&uLinkId=usr1mkqgl919blen&utm_source=app
 */
import android.app.Activity;
import android.app.ActivityManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WinBoLLActivityManager {

    public static final String TAG = "IWinBoLLActivityManager";


    public enum WinBoLLUI_TYPE { Aplication, Service }

    Context mContext;
    volatile static WinBoLLActivityManager _mIWinBoLLActivityManager;
    Map<String, IWinBoLLActivity> mActivityListMap;

    volatile static WinBoLLUI_TYPE _WinBoLLUI_TYPE = WinBoLLUI_TYPE.Service;
    public static void setWinBoLLUI_TYPE(WinBoLLUI_TYPE winBoLLUI_TYPE) {
        _WinBoLLUI_TYPE = winBoLLUI_TYPE;
    }

    public static WinBoLLUI_TYPE getWinBoLLUI_TYPE() {
        return _WinBoLLUI_TYPE;
    }
    WinBoLLActivityManager(Context context) {
        mContext = context;
        mActivityListMap = new HashMap<String, IWinBoLLActivity>();
    }

    public static synchronized WinBoLLActivityManager getInstance(Context context) {
        if (_mIWinBoLLActivityManager == null) {
            _mIWinBoLLActivityManager = new WinBoLLActivityManager(context);
        }
        return _mIWinBoLLActivityManager;
    }

    /**
     * 把Activity添加到管理中
     */
    public <T extends IWinBoLLActivity> void add(T activity) {
        if (isActive(activity.getTag())) {
            LogUtils.d(TAG, String.format("add(...) %s is active.", activity.getTag()));
        } else {
            mActivityListMap.put(activity.getTag(), activity);
            LogUtils.d(TAG, String.format("Add activity : %s\n_mapActivityList.size() : %d", activity.getTag(), mActivityListMap.size()));
        }
    }

    //
    // activity: 为 null 时，
    // intent.putExtra 函数 "tag" 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
    public <T extends IWinBoLLActivity> void startWinBoLLActivity(Context context, Class<T> clazz) {
        try {
            // 如果窗口已存在就重启窗口
            String tag = clazz.newInstance().getTag();
            if (isActive(tag)) {
                resumeActivity(context, tag);
                return;
            }

            // 新建一个任务窗口
            Intent intent = new Intent(context, clazz);
            //打开多任务窗口 flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("tag", tag);
            mContext.startActivity(intent);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public <T extends IWinBoLLActivity> void startWinBoLLActivity(Context context, Intent intent, Class<T> clazz) {
        try {
            // 如果窗口已存在就重启窗口
            String tag = clazz.newInstance().getTag();
            if (isActive(tag)) {
                resumeActivity(context, tag);
                return;
            }

            // 新建一个任务窗口
            //Intent intent = new Intent(context, clazz);
            //打开多任务窗口 flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("tag", tag);
            mContext.startActivity(intent);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    /**
     * 判断 tag绑定的 MyActivity是否存在
     */
    public boolean isActive(String tag) {
        //printAvtivityListInfo();
        IWinBoLLActivity iWinBoLLActivity = getIWinBoLLActivity(tag);
        if (iWinBoLLActivity != null) {
            Activity activity = iWinBoLLActivity.getActivity();
            if (activity != null) {
                LogUtils.d(TAG, "isActive(...) activity != null tag " + tag);
                //ToastUtils.show("activity != null tag " + tag);
                //判断是否为 BaseActivity,如果已经销毁，则移除
                if (activity.isFinishing() || activity.isDestroyed()) {
                    mActivityListMap.remove(iWinBoLLActivity.getTag());
                    //_mIWinBoLLActivityList.remove(activity);
                    LogUtils.d(TAG, String.format("isActive(...) remove activity.\ntag : %s", tag));
                    return false;
                } else {
                    LogUtils.d(TAG, String.format("isActive(...) activity is exist.\ntag : %s", tag));
                    return true;
                }
            }
        }
        return false;

    }

    IWinBoLLActivity getIWinBoLLActivity(String tag) {
        return mActivityListMap.get(tag);
    }

    /**
     * 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
     */
    public <T extends IWinBoLLActivity> void resumeActivity(Context context, String tag) {
        LogUtils.d(TAG, "resumeActivty");
        T iWinBoLLActivity = (T)getIWinBoLLActivity(tag);
        LogUtils.d(TAG, "activity " + iWinBoLLActivity.getTag());
        if (iWinBoLLActivity != null && iWinBoLLActivity.getActivity() != null && !iWinBoLLActivity.getActivity().isFinishing() && !iWinBoLLActivity.getActivity().isDestroyed()) {
            resumeActivity(context, iWinBoLLActivity);
        }
    }

    /**
     * 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
     */
    public <T extends IWinBoLLActivity> void resumeActivity(Context context, T activity) {
        ActivityManager am = (ActivityManager) activity.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        //返回启动它的根任务（home 或者 MainActivity）
        Intent intent = new Intent(context, activity.getClass());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.startActivities();
        //moveTaskToFront(YourTaskId, 0);
        LogUtils.d(TAG, "am.moveTaskToFront");
        //ToastUtils.show("resumeActivity am.moveTaskToFront");
        am.moveTaskToFront(activity.getActivity().getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
    }


    /**
     * 结束所有 Activity
     */
    public void finishAll() {
        try {
            //ToastUtils.show(String.format("finishAll() size : %d", _mIWinBoLLActivityList.size()));
            for (int i = mActivityListMap.size() - 1; i > -1; i--) {
                IWinBoLLActivity iWinBoLLActivity = mActivityListMap.get(i);
                ToastUtils.show("finishAll() activity");
                if (iWinBoLLActivity != null && iWinBoLLActivity.getActivity() != null && !iWinBoLLActivity.getActivity().isFinishing() && !iWinBoLLActivity.getActivity().isDestroyed()) {
                    //ToastUtils.show("activity != null ...");
                    if (GlobalApplication.getWinBoLLActivityManager().getWinBoLLUI_TYPE() == WinBoLLUI_TYPE.Service) {
                        // 结束窗口和最近任务栏, 建议前台服务类应用使用，可以方便用户再次调用 UI 操作。
                        iWinBoLLActivity.getActivity().finishAndRemoveTask();
                        //ToastUtils.show("finishAll() activity.finishAndRemoveTask();");
                    } else if (GlobalApplication.getWinBoLLActivityManager().getWinBoLLUI_TYPE() == WinBoLLUI_TYPE.Aplication) {
                        // 结束窗口保留最近任务栏，建议前台服务类应用使用，可以保持应用的系统自觉性。
                        iWinBoLLActivity.getActivity().finish();
                        //ToastUtils.show("finishAll() activity.finish();");
                    } else {
                        ToastUtils.show("WinBollApplication.WinBollUI_TYPE error.");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    /**
     * 结束指定Activity
     */
    public <T extends IWinBoLLActivity> void finish(T iWinBoLLActivity) {
        try {
            if (iWinBoLLActivity != null && iWinBoLLActivity.getActivity() != null && !iWinBoLLActivity.getActivity().isFinishing() && !iWinBoLLActivity.getActivity().isDestroyed()) {
                //根据tag 移除 MyActivity
                //String tag= activity.getTag();
                //_mIWinBoLLActivityList.remove(tag);
                //ToastUtils.show("remove");
                //ToastUtils.show("_mIWinBoLLActivityArrayMap.size() " + Integer.toString(_mIWinBoLLActivityArrayMap.size()));

                // 窗口回调规则：
                // [] 当前窗口位置 >> 调度出的窗口位置
                // ★：[0] 1 2 3 4 >> 1
                // ★：0 1 [2] 3 4 >> 1
                // ★：0 1 2 [3] 4 >> 2
                // ★：0 1 2 3 [4] >> 3
                // ★：[0] >> 直接关闭当前窗口
                IWinBoLLActivity preActivity = getPreActivity(iWinBoLLActivity);
                iWinBoLLActivity.getActivity().finish();
                if (preActivity != null) {
                    resumeActivity(mContext, preActivity);
                }
            }

        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    IWinBoLLActivity getPreActivity(IWinBoLLActivity iWinBoLLActivity) {
        try {
            boolean bingo = false;
            IWinBoLLActivity preIWinBoLLActivity = null;
            for (Map.Entry<String, IWinBoLLActivity> entity : mActivityListMap.entrySet()) {
                if (entity.getKey().equals(iWinBoLLActivity.getTag())) {
                    bingo = true;
                    LogUtils.d(TAG, "bingo");
                    break;
                }
                preIWinBoLLActivity = entity.getValue();
            }

            if (bingo) {
                return preIWinBoLLActivity;
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }

        return null;
    }

    public <T extends IWinBoLLActivity> boolean registeRemove(T iWinBoLLActivity) {
        IWinBoLLActivity iWinBoLLActivityTest = mActivityListMap.get(iWinBoLLActivity.getTag());
        if (iWinBoLLActivityTest != null) {
            mActivityListMap.remove(iWinBoLLActivity.getTag());
            return true;
        }
        return false;
    }

    public void printAvtivityListInfo() {
        if (!mActivityListMap.isEmpty()) {
            StringBuilder sb = new StringBuilder("Map entries : " + Integer.toString(mActivityListMap.size()));
            Iterator<Map.Entry<String, IWinBoLLActivity>> iterator = mActivityListMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IWinBoLLActivity> entry = iterator.next();
                sb.append("\nKey: " + entry.getKey() + ", \nValue: " + entry.getValue().getTag());
                //ToastUtils.show("\nKey: " + entry.getKey() + ", Value: " + entry.getValue().getTag());
            }
            sb.append("\nMap entries end.");
            LogUtils.d(TAG, sb.toString());
        } else {
            LogUtils.d(TAG, "The map is empty.");
        }
    }
}

