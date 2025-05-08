package cc.winboll.studio.jcndk.services;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/09 11:19:28
 * @Describe JC 主服务类
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.libjc.Main;
import cc.winboll.studio.libjc.net.JCSocketServer;

public class JCMainService extends Service {

    public static final String TAG = "JCService";

    JCMainServiceThread mJCMainServiceThread;
    
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        mJCMainServiceThread = new JCMainServiceThread();
        mJCMainServiceThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    class JCMainServiceThread extends Thread {

        @Override
        public void run() {
            super.run();
            System.out.println("JCMainServiceThread run()");
            JCSocketServer.main(null);
			System.out.println("JCMainServiceThread exit.");
        }
        
    }
}
