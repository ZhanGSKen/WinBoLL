package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:08:45
 * @Describe WinBoLLService 服务 Binder。
 */
import android.graphics.drawable.Drawable;

public interface IWinBoLLClientServiceBinder {

    public static final String TAG = "IWinBoLLClientServiceBinder";

    public WinBoLLClientService getService();

    public Drawable getCurrentStatusIconDrawable();
}
