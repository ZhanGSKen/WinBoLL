package cc.winboll.studio.shared.service;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 23:40:05
 * @Describe WinBollService 服务 Binder。
 */
import android.graphics.drawable.Drawable;

public interface IWinBollClientServiceBinder {
    
    public static final String TAG = "IWinBollClientServiceBinder";
    
    public WinBollClientService getService();
    
    public Drawable getCurrentStatusIconDrawable();
}
