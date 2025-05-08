package cc.winboll.studio.libjc.util;

import cc.winboll.studio.libjc.Main;

public class ConsoleUtils {

	public static String TAG = "ConsoleUtils";

	public static class Debug {


		public static void printFuncInfo(String tag) {
			if (Main._DEBUG) {
				String methodName = "";
				int lineNumber = 0;
				try {
					// 获取当前正在执行的栈帧
					StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
					// 一般取第2个元素（索引为1）对应的就是当前调用的方法，可根据实际情况调整
					methodName = stackTrace[3].getMethodName();
					lineNumber = stackTrace[3].getLineNumber();
					System.out.println(String.format("[ Class=>%s ] [ Line=>(%d) ] [ Func=>%s ]: ", tag, lineNumber, methodName));
				} catch (Exception e) {
					System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
				}
			}
		}

        public static void printObjectInfo(String objName, Object obj) {
            if (Main._DEBUG) {
                System.out.println(String.format("[ %s : %s ]", objName, obj.toString()));
            }
		}
	}

}
