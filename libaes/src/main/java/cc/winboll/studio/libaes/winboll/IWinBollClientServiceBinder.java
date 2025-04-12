package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:08:45
 * @Describe WinBollService 服务 Binder。
 */
import android.graphics.drawable.Drawable;

public interface IWinBollClientServiceBinder {

    public static final String TAG = "IWinBollClientServiceBinder";

    public WinBollClientService getService();

    public Drawable getCurrentStatusIconDrawable();
}
