package cc.winboll.studio.libjc;

import cc.winboll.studio.libjc.Main;
import cc.winboll.studio.libjc.cmd.ListJarClassHasMain;
import cc.winboll.studio.libjc.net.JCSocketServer;
import java.util.Scanner;

public class Main {

    public final static String TAG = "Main";

    /**
     * @Author ZhanGSKen@QQ.COM
     * @Date 2025/01/09 17:03:50
     * @Describe 当前源码实例的运行模式。
     */
    public final static int JAR_RUNNING_MODE_UNKNOWN = 0;
    public final static int JAR_RUNNING_MODE_CONSOLE = 1;
    public final static int JAR_RUNNING_MODE_CONSOLE_DEBUG = 2;
    public final static int JAR_RUNNING_MODE_JCNDK = 3;
    public final static int JAR_RUNNING_MODE_JCNDK_DEBUG = 4;
    public final static int JAR_RUNNING_MODE_JC = 5;
    public final static int JAR_RUNNING_MODE_JC_DEBUG = 6;
    public enum JAR_RUNNING_MODE {
        UNKNOWN(JAR_RUNNING_MODE_UNKNOWN),
        CONSOLE(JAR_RUNNING_MODE_CONSOLE),
        CONSOLE_DEBUG(JAR_RUNNING_MODE_CONSOLE_DEBUG),
        JCNDK(JAR_RUNNING_MODE_JCNDK),
        JCNDK_DEBUG(JAR_RUNNING_MODE_JCNDK_DEBUG),
        JC(JAR_RUNNING_MODE_JC),
        JC_DEBUG(JAR_RUNNING_MODE_JC_DEBUG);
        static String[] _mlistName = {
            "未知模式",
            "控制台",
            "控制台调试",
            "JCNDK",
            "JCNDK_DEBUG",
            "JC",
            "JC_DEBUG"};
        private int value = 0;
        private JAR_RUNNING_MODE(int value) {    //必须是private的，否则编译错误
            this.value = value;
        }
    }

    public static volatile boolean _DEBUG = true; //调试标志
    static volatile JAR_RUNNING_MODE _JAR_RUNNING_MODE = JAR_RUNNING_MODE.UNKNOWN;

    static UserInputThread _UserInputThread;
    static JCMainThread _JCMainThread;

	static JCSocketServer _SocketServer;

	public static void setDebug(boolean isDebug) {
		_DEBUG = isDebug;
	}

	public static boolean isDebug() {
		return _DEBUG;
	}

    public static void setRunningMode(JAR_RUNNING_MODE mode) {
        _JAR_RUNNING_MODE = mode;
        switch (_JAR_RUNNING_MODE.ordinal()) {
            case JAR_RUNNING_MODE_CONSOLE_DEBUG: 
            case JAR_RUNNING_MODE_JCNDK_DEBUG:
            case JAR_RUNNING_MODE_JC_DEBUG: {
                    setDebug(true);
                    break;
                }
            default : {
                    setDebug(false);
                    break;
                }
        }
    }

    public static JAR_RUNNING_MODE getRunningMode() {
        return _JAR_RUNNING_MODE;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
		System.out.println(String.format("%s Start", TAG));

        Scanner scanner = new Scanner(System.in);
        System.out.println("是否进入调试状态？(请输入yes或其他内容)");
        String input = scanner.nextLine();
        if ("yes".equalsIgnoreCase(input)) {
            //System.out.println("OK");
            Main.setRunningMode(Main.JAR_RUNNING_MODE.CONSOLE_DEBUG);
        } else {
            Main.setRunningMode(Main.JAR_RUNNING_MODE.CONSOLE);
        }
        scanner.close();

        //Main.setRunningMode(Main.JAR_RUNNING_MODE.CONSOLE);
        //Main.setRunningMode(Main.JAR_RUNNING_MODE.CONSOLE_DEBUG);

        try {
            if (args.length > 0 && args[0].equals("UnitTest")) {
                System.out.println(TestClassA.hello());
                System.out.println(TestClassB.hello());
            } else {
                _JCMainThread = JCMainThread.getInstance(Main.class.getName());
                _JCMainThread.start();

                _SocketServer = JCSocketServer.getInstance();
                _SocketServer.main(null);

                _UserInputThread = new UserInputThread();
                _UserInputThread.start();

                ListJarClassHasMain.main(null);
            }
        } catch (Exception e) {
            System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
        }
    }

    static class UserInputThread extends Thread {

        @Override
        public void run() {
            super.run();
            Scanner scanner;
            scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                //System.out.println(String.format("User Input : %s", input));

				_SocketServer.sendMessage(input);
            }
        }
    }
}
