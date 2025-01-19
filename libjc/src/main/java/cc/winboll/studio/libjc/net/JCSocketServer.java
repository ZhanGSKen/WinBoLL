package cc.winboll.studio.libjc.net;

import cc.winboll.studio.libjc.JCMainThread;
import cc.winboll.studio.libjc.Main;
import cc.winboll.studio.libjc.util.ConsoleUtils;
import cc.winboll.studio.libjc.util.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class JCSocketServer {

	public final static String TAG = "JCSocketServer";

	volatile static JCSocketServer _JCSocketServer;
	ServerSocket serverSocket;
	Socket clientSocket;
	Scanner in_socket;
	PrintWriter out_socket;
	public volatile boolean isServerMessageReady;
	public volatile String szServerMessage;
    ServerMessageSendThread mServerMessageSendThread;
    //SyncMesaageThread mSyncMesaageThread;
    ConsoleMessageSendThread mConsoleMessageSendThread;


	JCSocketServer() {
		ConsoleUtils.Debug.printFuncInfo(TAG);
        //Log.d(TAG, "JCSocketClient()");
        isServerMessageReady = false;
        szServerMessage = "";
	}

	public static JCSocketServer getInstance() {
		if (_JCSocketServer == null) {
			_JCSocketServer = new JCSocketServer();
		}
		return _JCSocketServer;
	}

    public static void main(String[] args) {
        System.out.println("JCSocketServer main :");
		JCSocketServer.getInstance().start();
	}



	void start() {
        System.out.println("JCSocketServer start :");
        // 连接服务器
        try {
			// 创建一个ServerSocket，监听端口8888
            serverSocket = new ServerSocket(8888);
            System.out.println("服务器已启动，等待客户端连接...");
            // 等待客户端连接
            clientSocket = serverSocket.accept();
            LogUtils.d(TAG, "客户端已连接");
            System.out.println("客户端已连接");
            out_socket = new PrintWriter(clientSocket.getOutputStream(), true);
            in_socket = new Scanner(clientSocket.getInputStream());
			

            mServerMessageSendThread = new ServerMessageSendThread();
            mServerMessageSendThread.start();
//            mSyncMesaageThread = new SyncMesaageThread();
//            mSyncMesaageThread.start();
            if (Main.getRunningMode() == Main.JAR_RUNNING_MODE.CONSOLE
				|| Main.getRunningMode() == Main.JAR_RUNNING_MODE.JCNDK) {
                mConsoleMessageSendThread = new ConsoleMessageSendThread();
                mConsoleMessageSendThread.start();
            }
            System.out.println("JCSocketServer start() end.");

        } catch (IOException e) {
            System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
            LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
        }

	}

	public void sendMessage(String message) {
        //Log.d(TAG, String.format("sendMessage : %s", message));
		szServerMessage = message;
		isServerMessageReady = true;
	}

//    public synchronized void syncServerMessage() {
//        //Log.d(TAG, "syncServerMessage()");
//        //System.out.println("syncServerMessage()");
////        if (isServerMessageReady) {
////            isServerMessageReady = false;
////            //System.out.println(String.format("JCSocketServer : %s", szServerMessage));
////            //Log.d(TAG, String.format("Send %s", szClientMessage));
////            out.println(szServerMessage);
////            szServerMessage = "";
////        }
//	}

//    public synchronized void syncSystemInMessage() {
//        //Log.d(TAG, "syncSystemInMessage()");
//        //System.out.println("syncSystemInMessage");
//
//        if (in_socket.hasNextLine()) {
//            //Log.d(TAG, "Reciving System Input Message...");
//            String response = in_socket.nextLine();
//            System.out.println("Recived ：" + response);
//            
//            // 发送一个消息回去给客户端
//            out_socket.println(String.format("%s : Recived [%s]", TAG, response));
//        }
//	}

	class ServerMessageSendThread extends Thread {

		@Override
		public void run() {
			super.run();// 获取输入输出流
            try {
                // 向客户端发送消息
                while (true) {
					
//					if(userInput.hasNextLine()) {
//						String szServerMessage = userInput.nextLine();
//						System.out.println(String.format("Send [%s] to client.", szServerMessage));
//						out_socket.println(szServerMessage);
//					}
                    if (isServerMessageReady) {
                        isServerMessageReady = false;
                        //System.out.println(String.format("JCSocketServer : %s", szServerMessage));
                        //Log.d(TAG, String.format("Send %s", szClientMessage));
						System.out.println(String.format("%s : %s", TAG, szServerMessage));
                        out_socket.println(szServerMessage);
                        szServerMessage = "";
                    }
                    //syncServerMessage();
//                    try {
//                        syncServerMessage();
//                        Thread.sleep(500);
//                    } catch (Exception e) {
//                        System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
//                        Log.e(TAG, e, Thread.currentThread().getStackTrace());
//                    }
                }
            } catch (Exception e) {
                System.err.println(String.format("%s ServerMessageSendThread : %s", TAG, e.getMessage()));
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
            // 获取输入输出流
            try {
                System.out.println("ConsoleMessageSendThread run()");
				
                // 读取服务器响应
                while (true) {
                    if (in_socket.hasNextLine()) {
                        //Log.d(TAG, "Reciving System Input Message...");
                        String response = in_socket.nextLine();
                        System.out.println(String.format("%s ：%s", JCSocketClient.TAG, response));

                        // 发送一个消息回去给客户端
                        //out_socket.println(String.format("%s : Recived [%s]", TAG, response));
                    }
                    //syncSystemInMessage();
//                    try {
//                        //syncSystemInMessage();
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        Log.e(TAG, e, Thread.currentThread().getStackTrace());
//                    }
                }
			} catch (Exception e) {
                System.err.println(String.format("%s ConsoleMessageSendThread : %s", TAG, e.getMessage()));
				LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
			}
        }
	}
}
