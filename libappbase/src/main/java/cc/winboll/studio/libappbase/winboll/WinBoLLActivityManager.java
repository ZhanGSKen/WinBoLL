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

public class WinBoLLActivityManager {

    public static final String TAG = "WinBoLLActivityManager";
    public static final String EXTRA_TAG = "EXTRA_TAG";

    public static enum WinBoLLUI_TYPE {
        Aplication, // 退出应用后，保持最近任务栏任务记录主窗口
        Service // 退出应用后，清理所有最近任务栏任务记录窗口
        };

    // 应用类型标志
    static volatile WinBoLLUI_TYPE _mWinBoLLUI_TYPE = WinBoLLUI_TYPE.Service;

    GlobalApplication mGlobalApplication;
    static volatile WinBoLLActivityManager _Instance;
    static volatile Map<String, IWinBoLLActivity> _mapIWinBoLLList;
    volatile IWinBoLLActivity mFirstIWinBoLLActivity;

    WinBoLLActivityManager(GlobalApplication application) {
        mGlobalApplication = application;
        _mapIWinBoLLList = new HashMap<String, IWinBoLLActivity>();
    }

    public static synchronized WinBoLLActivityManager getInstance(GlobalApplication application) {
        LogUtils.d(TAG, "getInstance");
        if (_Instance == null) {
            LogUtils.d(TAG, "_Instance == null");
            _Instance = new WinBoLLActivityManager(application);
        }
        return _Instance;
    }

    //
    // 设置 WinBoLL 应用 UI 类型
    //
    public synchronized static void setWinBoLLUI_TYPE(WinBoLLUI_TYPE mWinBoLLUI_TYPE) {
        _mWinBoLLUI_TYPE = mWinBoLLUI_TYPE;
    }

    //
    // 获取 WinBoLL 应用 UI 类型
    //
    public synchronized static WinBoLLUI_TYPE getWinBoLLUI_TYPE() {
        return _mWinBoLLUI_TYPE;
    }

    //
    // 把Activity添加到管理中
    //
    public <T extends IWinBoLLActivity> void add(T iWinBoLL) {
        String tag = ((IWinBoLLActivity)iWinBoLL).getTag();
        LogUtils.d(TAG, String.format("add(T iWinBoLL) tag is %s", tag));
        if (isActive(tag)) {
            LogUtils.d(TAG, String.format("isActive(tag) is true, tag : %s.", tag));
        } else {
            // 设置起始活动窗口，以便最后退出时提问
            if (mFirstIWinBoLLActivity == null && _mapIWinBoLLList.size() == 0) {
                LogUtils.d(TAG, "Set firstIWinBoLLActivity, iWinBoLL.getTag() is %s" + iWinBoLL.getTag());
                mFirstIWinBoLLActivity = iWinBoLL;
            }

            // 添加到活动窗口列表
            _mapIWinBoLLList.put(iWinBoLL.getTag(), iWinBoLL);
            LogUtils.d(TAG, String.format("Add activity : %s\n_mapActivityList.size() : %d", iWinBoLL.getTag(), _mapIWinBoLLList.size()));
        }
    }


    //
    // activity: 为 null 时，
    // intent.putExtra 函数 EXTRA_TAG 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
    public <T extends IWinBoLLActivity> void startWinBoLLActivity(Context context, Class<T> clazz) {
        try {
            // 如果窗口已存在就重启窗口
            String tag = ((IWinBoLLActivity)clazz.newInstance()).getTag();
            LogUtils.d(TAG, String.format("startWinBoLLActivity(Context context, Class<T> clazz) tag is %s", tag));
            if (isActive(tag)) {
                resumeActivity(context, tag);
                return;
            }
            //ToastUtils.show("startWinBoLLActivity(Context context, Class<T> clazz)");

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

    public <T extends IWinBoLLActivity> void startWinBoLLActivity(Context context, Intent intent, Class<T> clazz) {
        try {
            // 如果窗口已存在就重启窗口
            String tag = ((IWinBoLLActivity)clazz.newInstance()).getTag();
            LogUtils.d(TAG, String.format("startWinBoLLActivity(Context context, Intent intent, Class<T> clazz) tag is %s", tag));
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

    public <T extends IWinBoLLActivity> void startLogActivity(Context context) {
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

    public boolean isFirstIWinBoLLActivity(IWinBoLLActivity iWinBoLLActivity) {
        return mFirstIWinBoLLActivity != null && mFirstIWinBoLLActivity == iWinBoLLActivity;
    }

    //
    // 判断 tag绑定的 MyActivity是否存在
    //
    public boolean isActive(String tag) {
        LogUtils.d(TAG, String.format("isActive(String tag) tag is %s", tag));
        //printIWinBoLLListInfo();
        IWinBoLLActivity iWinBoLL = getIWinBoLL(tag);
        if (iWinBoLL != null) {
            //LogUtils.d(TAG, "isActive(...) activity != null tag " + tag);
            //ToastUtils.show("activity != null tag " + tag);
            //判断是否为 BaseActivity,如果已经销毁，则移除
            if (iWinBoLL.getActivity().isFinishing() || iWinBoLL.getActivity().isDestroyed()) {
                _mapIWinBoLLList.remove(iWinBoLL.getTag());
                //_mWinBoLLActivityList.remove(activity);
                LogUtils.d(TAG, String.format("isActive(...) remove activity.\ntag : %s", tag));
                return false;
            } else {
                LogUtils.d(TAG, String.format("isActive(...) activity is exist.\ntag : %s", tag));
                return true;
            }
        } else {
            LogUtils.d(TAG, String.format("isActive(...) iWinBoLL is null tag by %s", tag));
            return false;
        }
    }

    static IWinBoLLActivity getIWinBoLL(String tag) {
        LogUtils.d(TAG, String.format("getIWinBoLL(String tag) %s", tag));
        return _mapIWinBoLLList.get(tag);
    }

    //
    // 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
    //
    public <T extends IWinBoLLActivity> void resumeActivity(Context context, String tag) {
        LogUtils.d(TAG, "resumeActivity(Context context, String tag)");
        T iWinBoLL = (T)getIWinBoLL(tag);
        LogUtils.d(TAG, String.format("iWinBoLL.getTag() %s", iWinBoLL.getTag()));
        //LogUtils.d(TAG, "activity " + activity.getTag());
        if (iWinBoLL != null && !iWinBoLL.getActivity().isFinishing() && !iWinBoLL.getActivity().isDestroyed()) {
            resumeActivity(context, iWinBoLL);
        }
    }

    //
    // 找到tag 绑定的 BaseActivity ，通过 getTaskId() 移动到前台
    //
    public <T extends IWinBoLLActivity> void resumeActivity(Context context, T iWinBoLL) {
        LogUtils.d(TAG, "resumeActivity(Context context, T iWinBoLL)");
        ActivityManager am = (ActivityManager) mGlobalApplication.getSystemService(Context.ACTIVITY_SERVICE);
        //返回启动它的根任务（home 或者 MainActivity）
        Intent intent = new Intent(mGlobalApplication, iWinBoLL.getClass());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mGlobalApplication);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.startActivities();
        //moveTaskToFront(YourTaskId, 0);
        //ToastUtils.show("resumeActivity am.moveTaskToFront");
        LogUtils.d(TAG, String.format("iWinBoLL.getActivity().getTaskId() %d", iWinBoLL.getActivity().getTaskId()));
        am.moveTaskToFront(iWinBoLL.getActivity().getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
        LogUtils.d(TAG, "am.moveTaskToFront");
    }


    //
    // 结束所有 Activity
    //
    public void finishAll() {
        try {
            for (String key : _mapIWinBoLLList.keySet()) {
                //System.out.println("Key: " + key + ", Value: " + _mapActivityList.get(key));
                IWinBoLLActivity iWinBoLL = _mapIWinBoLLList.get(key);
                //ToastUtils.show("finishAll() activity");
                if (iWinBoLL != null && !iWinBoLL.getActivity().isFinishing() && !iWinBoLL.getActivity().isDestroyed()) {
                    //ToastUtils.show("activity != null ...");
                    if (getWinBoLLUI_TYPE() == WinBoLLUI_TYPE.Service) {
                        // 结束窗口和最近任务栏, 建议前台服务类应用使用，可以方便用户再次调用 UI 操作。
                        iWinBoLL.getActivity().finishAndRemoveTask();
                        //ToastUtils.show("finishAll() activity.finishAndRemoveTask();");
                    } else if (getWinBoLLUI_TYPE() == WinBoLLUI_TYPE.Aplication) {
                        // 结束窗口保留最近任务栏，建议前台服务类应用使用，可以保持应用的系统自觉性。
                        iWinBoLL.getActivity().finish();
                        //ToastUtils.show("finishAll() activity.finish();");
                    } else {
                        LogUtils.d(TAG, "WinBoLLApplication.WinBoLLUI_TYPE error.");
                        //ToastUtils.show("WinBoLLApplication.WinBoLLUI_TYPE error.");
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
    public <T extends IWinBoLLActivity> void finish(T iWinBoLL) {
        try {
            if (iWinBoLL != null && !iWinBoLL.getActivity().isFinishing() && !iWinBoLL.getActivity().isDestroyed()) {
                //根据tag 移除 MyActivity
                //String tag= activity.getTag();
                //_mWinBoLLActivityList.remove(tag);
                //ToastUtils.show("remove");
                //ToastUtils.show("_mWinBoLLActivityArrayMap.size() " + Integer.toString(_mWinBoLLActivityArrayMap.size()));

                // 窗口回调规则：
                // [] 当前窗口位置 >> 调度出的窗口位置
                // ★：[0] 1 2 3 4 >> 1
                // ★：0 1 [2] 3 4 >> 1
                // ★：0 1 2 [3] 4 >> 2
                // ★：0 1 2 3 [4] >> 3
                // ★：[0] >> 直接关闭当前窗口
                //LogUtils.d(TAG, "finish no yet.");
                IWinBoLLActivity preIWinBoLL = getPreIWinBoLL(iWinBoLL);
                iWinBoLL.getActivity().finish();
                if (preIWinBoLL != null) {
                    resumeActivity(mGlobalApplication, preIWinBoLL);
                }
            }

        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    //
    // 获取窗口队列中的前一个窗口
    //
    IWinBoLLActivity getPreIWinBoLL(IWinBoLLActivity iWinBoLL) {
        try {
            boolean bingo = false;
            IWinBoLLActivity preIWinBoLL = null;
            for (Map.Entry<String, IWinBoLLActivity> entity : _mapIWinBoLLList.entrySet()) {
                if (entity.getKey().equals(iWinBoLL.getTag())) {
                    bingo = true;
                    //LogUtils.d(TAG, "bingo");
                    break;
                }
                preIWinBoLL = entity.getValue();
            }

            if (bingo) {
                return preIWinBoLL;
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }

        return null;
    }

    //
    // 从管理列表中移除管理项
    //
    public <T extends IWinBoLLActivity> boolean registeRemove(T activity) {
        IWinBoLLActivity iWinBoLLTest = _mapIWinBoLLList.get(activity.getTag());
        if (iWinBoLLTest != null) {
            _mapIWinBoLLList.remove(activity.getTag());
            return true;
        }
        return false;
    }

    //
    // 打印管理列表项列表里的信息
    //
    public static void printIWinBoLLListInfo() {
        //LogUtils.d(TAG, "printAvtivityListInfo");
        if (!_mapIWinBoLLList.isEmpty()) {
            StringBuilder sb = new StringBuilder("Map entries : " + Integer.toString(_mapIWinBoLLList.size()));
            Iterator<Map.Entry<String, IWinBoLLActivity>> iterator = _mapIWinBoLLList.entrySet().iterator();
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
