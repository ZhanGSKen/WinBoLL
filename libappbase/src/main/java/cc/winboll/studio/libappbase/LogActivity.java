package cc.winboll.studio.libappbase;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 20:29
 * @Describe 应用日志展示 Activity
 * 用于单独启动窗口展示应用运行日志，依赖 LogView 控件实现日志加载与显示
 */
public class LogActivity extends Activity {

    /** 日志标签，用于当前 Activity 的日志输出标识 */
    public static final String TAG = "LogActivity";

    /** 日志展示控件（用于加载和显示应用日志） */
    private LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if(!GlobalApplication.isDebugging()) {
			ToastUtils.show("非调试状态日志功能不可用");
			finish();
		}
		
        // 设置布局文件（包含 LogView 控件）
        setContentView(R.layout.activity_log);

        // 绑定布局中的 LogView 控件
        mLogView = findViewById(R.id.logview);
        // 启动 LogView 日志加载（如实时刷新日志内容）
        mLogView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复 Activity 时重新启动 LogView（确保日志持续更新）
        mLogView.start();
    }

    /**
     * 启动日志 Activity 的静态方法（外部调用入口）
     * 配置 Intent 标志，以多任务/分屏模式启动，避免与主应用任务栈冲突
     * @param context 上下文（Activity/Fragment），用于启动 Activity
     */
    public static void startLogActivity(Context context) {
        startLogActivity(context, true);
    }

    /**
     * 启动日志 Activity 的静态方法重载（外部调用入口）
     * @param context 上下文（Activity/Fragment），用于启动 Activity
     * @param newTask 是否在新窗口中启动
     */
    public static void startLogActivity(Context context, boolean newTask) {
        Intent intent = new Intent(context, LogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            context.startActivity(intent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            Rect bounds = new Rect();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity.getWindow().getDecorView().getDisplay().getRectSize(bounds);
                bounds.set(0, bounds.height() / 2, bounds.width(), bounds.height());
            }
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchBounds(bounds);
            context.startActivity(intent, options.toBundle());
        } else {
            context.startActivity(intent);
        }
    }
}

