package cc.winboll.studio.libappbase.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 08:25:43
 * @Describe 应用活动窗口管理器
 * 参考 ：
 * android 类似微信小程序多任务窗口 及 设置 TaskDescription 修改 icon 和 label
 * https://blog.csdn.net/qq_29364417/article/details/109379915?app_version=6.4.2&code=app_1562916241&csdn_share_tail=%7B%22type%22%3A%22blog%22%2C%22rType%22%3A%22article%22%2C%22rId%22%3A%22109379915%22%2C%22source%22%3A%22weixin_38986226%22%7D&uLinkId=usr1mkqgl919blen&utm_source=app
 */
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

public class WinBollActivityManager {

    public static final String TAG = "WinBollActivityManager";
    public static final String EXTRA_TAG = "EXTRA_TAG";

    public static enum WinBollUI_TYPE {
        Aplication, // 退出应用后，保持最近任务栏任务记录主窗口
        Service // 退出应用后，清理所有最近任务栏任务记录窗口
        };

    // 应用类型标志
    static volatile WinBollUI_TYPE _mWinBollUI_TYPE = WinBollUI_TYPE.Service;

    GlobalApplication mGlobalApplication;
    static volatile WinBollActivityManager _Instance;
    static volatile Map<String, IWinBollActivity> _mapIWinBollList;
    volatile IWinBollActivity mFirstIWinBollActivity;

    WinBollActivityManager(GlobalApplication application) {
        mGlobalApplication = application;
        _mapIWinBollList = new HashMap<String, IWinBollActivity>();
    }

    public static synchronized WinBollActivityManager getInstance(GlobalApplication application) {
        LogUtils.d(TAG, "getInstance");
        if (_Instance == null) {
            LogUtils.d(TAG, "_Instance == null");
            _Instance = new WinBollActivityManager(application);
        }
        return _Instance;
    }

    //
    // 设置 WinBoll 应用 UI 类型
    //
    public synchronized static void setWinBollUI_TYPE(WinBollUI_TYPE mWinBollUI_TYPE) {
        _mWinBollUI_TYPE = mWinBollUI_TYPE;
    }

    //
    // 获取 WinBoll 应用 UI 类型
    //
    public synchronized static WinBollUI_TYPE getWinBollUI_TYPE() {
        return _mWinBollUI_TYPE;
    }

    //
    // 把Activity添加到管理中
    //
    public <T extends IWinBollActivity> void add(T iWinBoll) {
        String tag = ((IWinBollActivity)iWinBoll).getTag();
        LogUtils.d(TAG, String.format("add(T iWinBoll) tag is %s", tag));
        if (isActive(tag)) {
            LogUtils.d(TAG, String.format("isActive(tag) is true, tag : %s.", tag));
        } else {
            // 设置起始活动窗口，以便最后退出时提问
            if (mFirstIWinBollActivity == null && _mapIWinBollList.size() == 0) {
                LogUtils.d(TAG, "Set firstIWinBollActivity, iWinBoll.getTag() is %s" + iWinBoll.getTag());
                mFirstIWinBollActivity = iWinBoll;
            }

            // 添加到活动窗口列表
            _mapIWinBollList.put(iWinBoll.getTag(), iWinBoll);
            LogUtils.d(TAG, String.format("Add activity : %s\n_mapActivityList.size() : %d", iWinBoll.getTag(), _mapIWinBollList.size()));
        }
    }


    //
    // activity: 为 null 时，
    // intent.putExtra 函数 EXTRA_TAG 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
    public <T extends IWinBollActivity> void startWinBollActivity(Context context, Class<T> clazz) {
        try {
            // 如果窗口已存在就重启窗口
            String tag = ((IWinBollActivity)clazz.newInstance()).getTag();
            LogUtils.d(TAG, String.format("startWinBollActivity(Context context, Class<T> clazz) tag is %s", tag));
            if (isActive(tag)) {
                resumeActivity(context, tag);
                return;
            }
            ToastUtils.show("startWinBollActivity(Context context, Class<T> clazz)");

            // 新建一个任务窗口
            Intent intent = new Intent(context, clazz);
            //打开多任务窗口 flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_TAG, tag);
            context.startActivity(intent);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public <T extends IWinBollActivity> void startWinBollActivity(Context context, Intent intent, Class<T> clazz) {
        try {
            // 如果窗口已存在就重启窗口
            String tag = ((IWinBollActivity)clazz.newInstance()).getTag();
            LogUtils.d(TAG, String.format("startWinBollActivity(Context context, Intent intent, Class<T> clazz) tag is %s", tag));
            if (isActive(tag)) {
                resumeActivity(context, tag);
                return;
            }

            // 新建一个任务窗口
            //Intent intent = new Intent(context, clazz);
            //打开多任务窗口 flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_TAG, tag);
            context.startActivity(intent);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public <T extends IWinBollActivity> void startLogActivity(Context context) {
        // 如果窗口已存在就重启窗口
        String tag = LogActivity.TAG;
        if (isActive(tag)) {
            resumeActivity(context, tag);
            return;
        }

        // 新建一个任务窗口
        Intent intent = new Intent(context, LogActivity.class);
        //打开多任务窗口 flags
        // Define the bounds.
//        Rect bounds = new Rect(0, 0, 800, 200);
//        // Set the bounds as an activity option.
//        ActivityOptions options = ActivityOptions.makeBasic();
//        options.setLaunchBounds(bounds);
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        intent.putExtra(EXTRA_TAG, tag);

        //context.startActivity(intent, options.toBundle());
        context.startActivity(intent);
    }

    public boolean isFirstIWinBollActivity(IWinBollActivity iWinBollActivity) {
        return mFirstIWinBollActivity != null && mFirstIWinBollActivity == iWinBollActivity;
    }

    //
    // 判断 tag绑定的 MyActivity是否存在
    //
    public boolean isActive(String tag) {
        LogUtils.d(TAG, String.format("isActive(String tag) tag is %s", tag));
        //printIWinBollListInfo();
        IWinBollActivity iWinBoll = getIWinBoll(tag);
        if (iWinBoll != null) {
            //LogUtils.d(TAG, "isActive(...) activity != null tag " + tag);
            //ToastUtils.show("activity != null tag " + tag);
            //判断是否为 BaseActivity,如果已经销毁，则移除
            if (iWinBoll.getActivity().isFinishing() || iWinBoll.getActivity().isDestroyed()) {
                _mapIWinBollList.remove(iWinBoll.getTag());
                //_mWinBollActivityList.remove(activity);
                LogUtils.d(TAG, String.format("isActive(...) remove activity.\ntag : %s", tag));
                return false;
            } else {
                LogUtils.d(TAG, String.format("isActive(...) activity is exist.\ntag : %s", tag));
                return true;
            }
        } else {
            LogUtils.d(TAG, String.format("isActive(...) iWinBoll is null tag by %s", tag));
            return false;
        }
    }

    static IWinBollActivity getIWinBoll(String tag) {
        LogUtils.d(TAG, String.format("getIWinBoll(String tag) %s", tag));
        return _mapIWinBollList.get(tag);
    }

    //
    // 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
    //
    public <T extends IWinBollActivity> void resumeActivity(Context context, String tag) {
        LogUtils.d(TAG, "resumeActivity(Context context, String tag)");
        T iWinBoll = (T)getIWinBoll(tag);
        LogUtils.d(TAG, String.format("iWinBoll.getTag() %s", iWinBoll.getTag()));
        //LogUtils.d(TAG, "activity " + activity.getTag());
        if (iWinBoll != null && !iWinBoll.getActivity().isFinishing() && !iWinBoll.getActivity().isDestroyed()) {
            resumeActivity(context, iWinBoll);
        }
    }

    //
    // 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
    //
    public <T extends IWinBollActivity> void resumeActivity(Context context, T iWinBoll) {
        LogUtils.d(TAG, "resumeActivity(Context context, T iWinBoll)");
        ActivityManager am = (ActivityManager) mGlobalApplication.getSystemService(Context.ACTIVITY_SERVICE);
        //返回启动它的根任务（home 或者 MainActivity）
        Intent intent = new Intent(mGlobalApplication, iWinBoll.getClass());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mGlobalApplication);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.startActivities();
        //moveTaskToFront(YourTaskId, 0);
        //ToastUtils.show("resumeActivity am.moveTaskToFront");
        LogUtils.d(TAG, String.format("iWinBoll.getActivity().getTaskId() %d", iWinBoll.getActivity().getTaskId()));
        am.moveTaskToFront(iWinBoll.getActivity().getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
        LogUtils.d(TAG, "am.moveTaskToFront");
    }


    //
    // 结束所有 Activity
    //
    public void finishAll() {
        try {
            for (String key : _mapIWinBollList.keySet()) {
                //System.out.println("Key: " + key + ", Value: " + _mapActivityList.get(key));
                IWinBollActivity iWinBoll = _mapIWinBollList.get(key);
                //ToastUtils.show("finishAll() activity");
                if (iWinBoll != null && !iWinBoll.getActivity().isFinishing() && !iWinBoll.getActivity().isDestroyed()) {
                    //ToastUtils.show("activity != null ...");
                    if (getWinBollUI_TYPE() == WinBollUI_TYPE.Service) {
                        // 结束窗口和最近任务栏, 建议前台服务类应用使用，可以方便用户再次调用 UI 操作。
                        iWinBoll.getActivity().finishAndRemoveTask();
                        //ToastUtils.show("finishAll() activity.finishAndRemoveTask();");
                    } else if (getWinBollUI_TYPE() == WinBollUI_TYPE.Aplication) {
                        // 结束窗口保留最近任务栏，建议前台服务类应用使用，可以保持应用的系统自觉性。
                        iWinBoll.getActivity().finish();
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

    //
    // 结束指定Activity
    //
    public <T extends IWinBollActivity> void finish(T iWinBoll) {
        try {
            if (iWinBoll != null && !iWinBoll.getActivity().isFinishing() && !iWinBoll.getActivity().isDestroyed()) {
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
                //LogUtils.d(TAG, "finish no yet.");
                IWinBollActivity preIWinBoll = getPreIWinBoll(iWinBoll);
                iWinBoll.getActivity().finish();
                if (preIWinBoll != null) {
                    resumeActivity(mGlobalApplication, preIWinBoll);
                }
            }

        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    //
    // 获取窗口队列中的前一个窗口
    //
    IWinBollActivity getPreIWinBoll(IWinBollActivity iWinBoll) {
        try {
            boolean bingo = false;
            IWinBollActivity preIWinBoll = null;
            for (Map.Entry<String, IWinBollActivity> entity : _mapIWinBollList.entrySet()) {
                if (entity.getKey().equals(iWinBoll.getTag())) {
                    bingo = true;
                    //LogUtils.d(TAG, "bingo");
                    break;
                }
                preIWinBoll = entity.getValue();
            }

            if (bingo) {
                return preIWinBoll;
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }

        return null;
    }

    //
    // 从管理列表中移除管理项
    //
    public <T extends IWinBollActivity> boolean registeRemove(T activity) {
        IWinBollActivity iWinBollTest = _mapIWinBollList.get(activity.getTag());
        if (iWinBollTest != null) {
            _mapIWinBollList.remove(activity.getTag());
            return true;
        }
        return false;
    }

    //
    // 打印管理列表项列表里的信息
    //
    public static void printIWinBollListInfo() {
        //LogUtils.d(TAG, "printAvtivityListInfo");
        if (!_mapIWinBollList.isEmpty()) {
            StringBuilder sb = new StringBuilder("Map entries : " + Integer.toString(_mapIWinBollList.size()));
            Iterator<Map.Entry<String, IWinBollActivity>> iterator = _mapIWinBollList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IWinBollActivity> entry = iterator.next();
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
