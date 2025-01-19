package cc.winboll.studio.libjc.net;

import cc.winboll.studio.libjc.JCMainThread;
import cc.winboll.studio.libjc.util.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import cc.winboll.studio.libjc.Main;

public class JCSocketClient {

	public final static String TAG = "JCSocketClient";

    public final static String DEFAULT_SERVER = "127.0.0.1";

	volatile static JCSocketClient _JCSocketClient;
    String mszServer = "";
	Socket socket;
	Scanner in_socket;
	PrintWriter out_socket;
	Scanner userInput;
	public volatile boolean isMessage2Server;
	public volatile String szMessage2Server;
    ClientMessageSendThread mClientMessageSendThread;
    //SyncMesaageThread mSyncMesaageThread;
    ConsoleMessageSendThread mConsoleMessageSendThread;

	JCSocketClient() {
        LogUtils.d(TAG, "JCSocketClient()");
        isMessage2Server = false;
        szMessage2Server = "";
	}

	public static JCSocketClient getInstance() {
		if (_JCSocketClient == null) {
			_JCSocketClient = new JCSocketClient();
		}
		return _JCSocketClient;
	}

    public static void main(String[] args) {
        System.out.println("JCSocketClient main :");
        
        JCSocketClient jcSocketClient = JCSocketClient.getInstance();
        if (args != null && args.length == 1 && !args.equals("")) {
            jcSocketClient.mszServer = args[0];
        } else {
            jcSocketClient.mszServer = DEFAULT_SERVER;
        }
		jcSocketClient.start();
	}
    
    public void start(String server) {
        mszServer = server;
        start();
    }

    void start() {
        System.out.println("JCSocketClient start()");
        // 连接服务器
        try {
            System.out.println(String.format("Connect to %s", mszServer));
            socket = new Socket(mszServer, 8888);
            System.out.println("已连接到服务器");
            // 获取输出流
            out_socket = new PrintWriter(socket.getOutputStream(), true);
            // 获取输入流
            in_socket = new Scanner(socket.getInputStream());
            //userInput = new Scanner(System.in);

            mClientMessageSendThread = new ClientMessageSendThread();
            mClientMessageSendThread.start();
//            mSyncMesaageThread = new SyncMesaageThread();
//            mSyncMesaageThread.start();
            if (Main.getRunningMode() == Main.JAR_RUNNING_MODE.CONSOLE
                || Main.getRunningMode() == Main.JAR_RUNNING_MODE.JC) {
                mConsoleMessageSendThread = new ConsoleMessageSendThread();
                mConsoleMessageSendThread.start();
            }
            // 开始发送消息
            //sendMessage(String.format("%s OK", TAG));

        } catch (IOException e) {
            System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
            LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
        }

	}

	public void sendMessage(String message) {
        //Log.d(TAG, String.format("sendMessage : %s", message));
		szMessage2Server = message;
		isMessage2Server = true;
	}

//    public synchronized void syncClientMessage() {
//        //Log.d(TAG, "syncClientMessage()");
//        //System.out.println("syncClientMessage()");
//        if (isClientMessageReady) {
//            isClientMessageReady = false;
//            //System.out.println(String.format("Client : %s", szClientMessage));
//            //Log.d(TAG, String.format("Send %s", szClientMessage));
//            //out_socket.println(szClientMessage);
//            out_socket.println(szClientMessage);
//            System.out.println(String.format("out_socket->%s", szClientMessage));
//            szClientMessage = "";
//        }
//	}

    public synchronized void scan_in_socket() {
        //Log.d(TAG, "scan_in_socket()");
        //System.out.println("scan_in_socket");

        if (in_socket.hasNextLine()) {
            //Log.d(TAG, "Reciving System Input Message...");
            String response = in_socket.nextLine();
            System.out.println(String.format("%s ：%s", JCSocketServer.TAG, response));
        }
	}

	class ClientMessageSendThread extends Thread {

		@Override
		public void run() {
			super.run();
            try {
                // 向服务器发送消息
                while (true) {
                    if (isMessage2Server) {
                        isMessage2Server = false;
                        //System.out.println(String.format("Client : %s", szClientMessage));
                        //Log.d(TAG, String.format("Send %s", szClientMessage));
                        //out_socket.println(szClientMessage);
                        out_socket.println(szMessage2Server);
                        System.out.println(String.format("%s : %s", TAG, szMessage2Server));
                        szMessage2Server = "";
                    }
//                    syncClientMessage();
//                    try {
//                        //syncClientMessage();
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        Log.e(TAG, e, Thread.currentThread().getStackTrace());
//                    }
                }
            } catch (Exception e) {
                System.err.println(String.format("%s ClientMessageSendThread : %s", TAG, e.getMessage()));
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
		}

	}

//    class SyncMesaageThread extends Thread {
//
//        @Override
//        public void run() {
//            super.run();
//            while (true) {
//                sendMessage(String.format("%s OK", TAG));
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
//                    Log.e(TAG, e, Thread.currentThread().getStackTrace());
//                }
//            }
//        }
//    }

    class ConsoleMessageSendThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                System.out.println("ConsoleMessageSendThread run()");

                // 读取服务器响应
                while (true) {
                    scan_in_socket();
//                    try {
//                        syncSystemInMessage();
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        Log.e(TAG, e, Thread.currentThread().getStackTrace());
//                    }
                }
            } catch (Exception e) {
                System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
	}
}
