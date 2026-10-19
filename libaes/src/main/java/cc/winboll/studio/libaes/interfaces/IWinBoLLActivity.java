package cc.winboll.studio.libaes.interfaces;

import android.app.Activity;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/05/10 09:34
 * @Describe WinBoll 窗口操作接口（规范定义，职责单一）
 */
public interface IWinBoLLActivity {
    String TAG = "IWinBoLLActivity";
    String ACTION_BIND = IWinBoLLActivity.class.getName() + ".ACTION_BIND";

    /**
     * 获取当前Activity实例
     */
    Activity getActivity();

    /**
     * 获取Activity唯一标识（建议使用类名+UUID或固定唯一字符串）
     */
    String getTag();
}

