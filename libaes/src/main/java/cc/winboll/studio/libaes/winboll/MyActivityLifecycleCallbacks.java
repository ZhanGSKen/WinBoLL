package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 08:24:52
 */
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ToastUtils; 

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks { 

    public static final String TAG = "MyActivityLifecycleCallbacks";

    public String mInfo = "";

    public MyActivityLifecycleCallbacks() {
    }

    void createActivityeInfo(Activity activity) {
        StringBuilder sb = new StringBuilder();
        Intent receivedIntent = activity.getIntent();
        sb.append("\nCallingActivity : \n");
        if (activity.getCallingActivity() != null) {
            sb.append(activity.getCallingActivity().getPackageName());
        }
        sb.append("\nReceived Intent Package : \n");
        sb.append(receivedIntent.getPackage());

        Bundle extras = receivedIntent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                sb.append("\nIntentInfo");
                sb.append("\n键: ");
                sb.append(key);
                sb.append(", 值: ");
                sb.append(extras.get(key));
                //Log.d("IntentInfo", "键: " + key + ", 值: " + extras.get(key));
            }
        }
        mInfo = sb.toString();
        //Log.d("IntentInfo", "发送Intent的应用包名: " + senderPackage);
    }

    public void showActivityeInfo() {
        ToastUtils.show("ActivityeInfo : " + mInfo);
        LogUtils.d(TAG, "ActivityeInfo : " + mInfo);
    }

    @Override 
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { 
        // 在这里可以做一些初始化相关的操作，例如记录Activity的创建时间等 
        //System.out.println(activity.getLocalClassName() + " was created"); 
        LogUtils.d(TAG, activity.getLocalClassName() + " was created");
        createActivityeInfo(activity);
    } 

    @Override 
    public void onActivityStarted(Activity activity) { 
        //System.out.println(activity.getLocalClassName() + " was started");
        LogUtils.d(TAG, activity.getLocalClassName() + " was started");
        //createActivityeInfo(activity);
    } 

    @Override 
    public void onActivityResumed(Activity activity) { 
        //System.out.println(activity.getLocalClassName() + " was resumed");
        LogUtils.d(TAG, activity.getLocalClassName() + " was resumed");
        //createActivityeInfo(activity);
    } 

    @Override 
    public void onActivityPaused(Activity activity) { 
        //System.out.println(activity.getLocalClassName() + " was paused");
        LogUtils.d(TAG, activity.getLocalClassName() + " was paused");
    } 

    @Override 
    public void onActivityStopped(Activity activity) { 
        //System.out.println(activity.getLocalClassName() + " was stopped");
        LogUtils.d(TAG, activity.getLocalClassName() + " was stopped");
    } 

    @Override 
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { 
        // 可以在这里添加保存状态的自定义逻辑 
    } 

    @Override 
    public void onActivityDestroyed(Activity activity) { 
        //System.out.println(activity.getLocalClassName() + " was destroyed");
        LogUtils.d(TAG, activity.getLocalClassName() + " was destroyed");
    } 
}
