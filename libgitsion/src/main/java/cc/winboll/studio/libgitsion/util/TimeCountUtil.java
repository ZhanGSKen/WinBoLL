package cc.winboll.studio.libgitsion.util;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:26
 */

import android.os.Handler;
import android.os.Message;

public final class TimeCountUtil {

    private final Handler mHandler;
    private long totalTime;
    private boolean isRunning;
    public static final int COUNT_FINISH = 1001;

    public TimeCountUtil(final OnCountListener listener) {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == COUNT_FINISH){
                    isRunning = false;
                    if(listener != null){
                        listener.onTimeOut();
                    }
                }
            }
        };
    }

    public void start(long time){
        if(isRunning){
            return;
        }
        totalTime = time;
        isRunning = true;
        mHandler.sendEmptyMessageDelayed(COUNT_FINISH,totalTime);
    }

    public void cancel(){
        mHandler.removeMessages(COUNT_FINISH);
        isRunning = false;
    }

    public interface OnCountListener{
        void onTimeOut();
    }
}

