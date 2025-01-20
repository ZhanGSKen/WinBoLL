package cc.winboll.studio.libapputils.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 16:09:15
 * @Describe 应用活动窗口管理器
 * 参考 ：
 * android 类似微信小程序多任务窗口 及 设置 TaskDescription 修改 icon 和 label
 * https://blog.csdn.net/qq_29364417/article/details/109379915?app_version=6.4.2&code=app_1562916241&csdn_share_tail=%7B%22type%22%3A%22blog%22%2C%22rType%22%3A%22article%22%2C%22rId%22%3A%22109379915%22%2C%22source%22%3A%22weixin_38986226%22%7D&uLinkId=usr1mkqgl919blen&utm_source=app
 */
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.TaskStackBuilder;
import cc.winboll.studio.libapputils.log.LogUtils;
import com.hjq.toast.ToastUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WinBollActivityManager {

    public static final String TAG = "WinBollActivityManager";

    Context mContext;
    static WinBollActivityManager _mWinBollActivityManager;
    static Map<String, WinBollActivity> _mapActivityList;
    //static ArrayList<WinBollActivity> _mWinBollActivityList;

    public WinBollActivityManager(Context context) {
        mContext = context;
        LogUtils.d(TAG, "WinBollActivityManager()");
        _mapActivityList = new HashMap<String, WinBollActivity>();
        //_mWinBollActivityList = new ArrayList<WinBollActivity>();
    }

    public static synchronized WinBollActivityManager getInstance(Context context) {
        LogUtils.d(TAG, "getInstance");
        if (_mWinBollActivityManager == null) {
            LogUtils.d(TAG, "_mWinBollActivityManager == null");
            _mWinBollActivityManager = new WinBollActivityManager(context);
        }
        return _mWinBollActivityManager;
    }

    /**
     * 把Activity添加到管理中
     */
    public <T extends WinBollActivity> void add(T activity) {
        /*for (int i = 0; i < _mWinBollActivityList.size(); i++) {
         LogUtils.d(TAG, String.format("add for i %d\nget(i).getTag() %s", i, _mWinBollActivityList.get(i).getTag()));
         if (_mWinBollActivityList.get(i).getTag().equals(activity.getTag())) {
         _mWinBollActivityList.add(i, activity);
         _mWinBollActivityList.remove(i);
         LogUtils.d(TAG, String.format("Replace activity : %s\nSize %d", activity.getTag(), _mWinBollActivityList.size()));
         return;
         }
         }*/
        if (isActive(activity.getTag())) {
            LogUtils.d(TAG, String.format("add(...) %s is active.", activity.getTag()));
        } else {
            _mapActivityList.put(activity.getTag(), activity);
            LogUtils.d(TAG, String.format("Add activity : %s\n_mapActivityList.size() : %d", activity.getTag(), _mapActivityList.size()));
        }
    }


    //
    // activity: 为 null 时，
    // intent.putExtra 函数 "tag" 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
    public <T extends WinBollActivity> void startWinBollActivity(Context context, Class<T> clazz) {
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

    public <T extends WinBollActivity> void startWinBollActivity(Context context, Intent intent, Class<T> clazz) {
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
        printAvtivityListInfo();
        WinBollActivity activity = getWinBollActivity(tag);
        if (activity != null) {
            LogUtils.d(TAG, "isActive(...) activity != null tag " + tag);
            //ToastUtils.show("activity != null tag " + tag);
            //判断是否为 BaseActivity,如果已经销毁，则移除
            if (activity.isFinishing() || activity.isDestroyed()) {
                _mapActivityList.remove(activity.getTag());
                //_mWinBollActivityList.remove(activity);
                LogUtils.d(TAG, String.format("isActive(...) remove activity.\ntag : %s", tag));
                return false;
            } else {
                LogUtils.d(TAG, String.format("isActive(...) activity is exist.\ntag : %s", tag));
                return true;
            }
        } else {
            LogUtils.d(TAG, String.format("isActive(...) activity == null\ntag : %s", tag));
            return false;
        }
    }

    static WinBollActivity getWinBollActivity(String tag) {
        return _mapActivityList.get(tag);
    }

    /**
     * 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
     */
    public <T extends WinBollActivity> void resumeActivity(Context context, String tag) {
        LogUtils.d(TAG, "resumeActivty");
        T activity = (T)getWinBollActivity(tag);
        LogUtils.d(TAG, "activity " + activity.getTag());
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            resumeActivity(context, activity);
        }
    }

    /**
     * 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
     */
    public <T extends WinBollActivity> void resumeActivity(Context context, T activity) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        //返回启动它的根任务（home 或者 MainActivity）
        Intent intent = new Intent(context, activity.getClass());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.startActivities();
        //moveTaskToFront(YourTaskId, 0);
        LogUtils.d(TAG, "am.moveTaskToFront");
        //ToastUtils.show("resumeActivity am.moveTaskToFront");
        am.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
    }


    /**
     * 结束所有 Activity
     */
    public void finishAll() {
        try {
            for (String key : _mapActivityList.keySet()) {
                //System.out.println("Key: " + key + ", Value: " + _mapActivityList.get(key));
                WinBollActivity activity = _mapActivityList.get(key);
                //ToastUtils.show("finishAll() activity");
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    //ToastUtils.show("activity != null ...");
                    if (WinBollApplication.getWinBollUI_TYPE() == WinBollApplication.WinBollUI_TYPE.Service) {
                        // 结束窗口和最近任务栏, 建议前台服务类应用使用，可以方便用户再次调用 UI 操作。
                        activity.finishAndRemoveTask();
                        //ToastUtils.show("finishAll() activity.finishAndRemoveTask();");
                    } else if (WinBollApplication.getWinBollUI_TYPE() == WinBollApplication.WinBollUI_TYPE.Aplication) {
                        // 结束窗口保留最近任务栏，建议前台服务类应用使用，可以保持应用的系统自觉性。
                        activity.finish();
                        //ToastUtils.show("finishAll() activity.finish();");
                    } else {
                        LogUtils.d(TAG, "WinBollApplication.WinBollUI_TYPE error.");
                        //ToastUtils.show("WinBollApplication.WinBollUI_TYPE error.");
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
    public <T extends WinBollActivity> void finish(T activity) {
        try {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                //根据tag 移除 MyActivity
                //String tag= activity.getTag();
                //_mWinBollActivityList.remove(tag);
                //ToastUtils.show("remove");
                //ToastUtils.show("_mWinBollActivityArrayMap.size() " + Integer.toString(_mWinBollActivityArrayMap.size()));

                // 窗口回调规则：
                // [] 当前窗口位置 >> 调度出的窗口位置
                // ★：[0] 1 2 3 4 >> 1
                // ★：0 1 [2] 3 4 >> 1
                // ★：0 1 2 [3] 4 >> 2
                // ★：0 1 2 3 [4] >> 3
                // ★：[0] >> 直接关闭当前窗口
                LogUtils.d(TAG, "finish no yet.");
                WinBollActivity preActivity = getPreActivity(activity);
                activity.finish();
                if (preActivity != null) {
                    resumeActivity(mContext, preActivity);
                }

//                for (int i = 0; i < _mWinBollActivityList.size(); i++) {
//                    if (_mWinBollActivityList.get(i).getTag().equals(activity.getTag())) {
//                        //ToastUtils.show(String.format("equals i : %d\nTag : %s\nSize : %d", i, activity.getTag(), _mWinBollActivityList.size()));
//                        if (i == 0) {
//                            finishAll();
//                            //ToastUtils.show("finish finishAll");
//                            return;
//                        }
//                        if (i > 0) {
//                            activity.finish();
//                            resumeActivity(mContext, _mWinBollActivityList.get(i - 1));
//                            return;
//                        }
//                    }
//                }
            }

        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    WinBollActivity getPreActivity(WinBollActivity activity) {
        try {
            boolean bingo = false;
            WinBollActivity preActivity = null;
            for (Map.Entry<String, WinBollActivity> entity : _mapActivityList.entrySet()) {
                if (entity.getKey().equals(activity.getTag())) {
                    bingo = true;
                    LogUtils.d(TAG, "bingo");
                    break;
                }
                preActivity = entity.getValue();
            }

            if (bingo) {
                return preActivity;
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }

        return null;
    }

    public <T extends WinBollActivity> boolean registeRemove(T activity) {
//        for (int i = 0; i < _mWinBollActivityList.size(); i++) {
//            if (registeRemove(activity, i)) {
//                return true;
//            }
//        }
        WinBollActivity activityTest = _mapActivityList.get(activity.getTag());
        if (activityTest != null) {
            _mapActivityList.remove(activity.getTag());
            return true;
        }
        return false;
    }

//    public <T extends WinBollActivity> boolean registeRemove(T activity, int position) {
//        if (_mWinBollActivityList.get(position) == activity) {
//            _mWinBollActivityList.remove(position);
//            //ToastUtils.show(String.format("registeRemove remove.\nTag %s\nposition %d", activity.getTag(), position));
//            return true;
//        }
//        return false;
//    }

    public static void printAvtivityListInfo() {
        //LogUtils.d(TAG, "printAvtivityListInfo");
        if (!_mapActivityList.isEmpty()) {
            StringBuilder sb = new StringBuilder("Map entries : " + Integer.toString(_mapActivityList.size()));
            Iterator<Map.Entry<String, WinBollActivity>> iterator = _mapActivityList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, WinBollActivity> entry = iterator.next();
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
