package cc.winboll.studio.libjc;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/06 11:14:19
 * @Describe 测试类A
 */
public class TestClassA {
    
    public static final String TAG = "TestClassA";
    
    //
    // 当前类入口
    // 
    // @args[0] 为 "UnitTest" 时就进行单元测试输出
    //
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("UnitTest")) {
            //System.out.println("main : ");
            System.out.println("args[0] " + args[0]);
            //System.out.println("args[1]" + args[1]);
            System.out.println(hello(args[0]));
        } else {
            System.out.println(hello());
        }
    }
    
    public static String hello() {
        return String.format("Hello, World! %s works well.", TAG);
    }

    public static String hello(String name) {
        return String.format("Hello, %s! %s works well.", name, TAG);
    }
}
