package cc.winboll.studio.libaes.winboll;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/03 19:14
 */
public class WinBoLLServiceStatusView extends LinearLayout {
    
    public static final String TAG = "WinBoLLServiceStatusView";
    
    public WinBoLLServiceStatusView(Context context) {
        super(context);
    }

    public WinBoLLServiceStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WinBoLLServiceStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WinBoLLServiceStatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    
    void setServerHost(String szWinBoLLServerHost) {
        
    }
    
    void setAuthInfo(String szDevUserName, String szDevUserPassword) {
        
    }
}
