package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 04:13:51
 * @Describe 简单链接 SOS 体系的服务
 */
import android.content.Intent;

public interface ISOSService {
    
    public static final String TAG = "ISOSService";
    public static final String EXTRA_ENABLE = "EXTRA_ENABLE";
    
    public Intent getISOSServiceIntentWhichAskForHelp();
    public boolean isEnable();
}
