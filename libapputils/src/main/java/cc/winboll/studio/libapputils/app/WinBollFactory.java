package cc.winboll.studio.libapputils.app;


/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/02/04 10:19:34
 * @Describe WinBoll 工厂
 */
public class WinBollFactory {

    public static final String TAG = "WinBollFactory";

    public static IWinBollActivity buildWinBollActivity(IWinBoll iWinBoll) {
        return new IWinBollActivity(iWinBoll);
    }
}
