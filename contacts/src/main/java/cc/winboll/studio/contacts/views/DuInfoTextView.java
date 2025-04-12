package cc.winboll.studio.contacts.views;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 21:11:03
 * @Describe 云盾防御信息
 */
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import cc.winboll.studio.contacts.beans.SettingsModel;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.libappbase.LogUtils;

public class DuInfoTextView extends TextView {
    
    public static final String TAG = "DuInfoTextView";
    
    public static final int MSG_NOTIFY_INFO_UPDATE = 0;
    
    Context mContext;
    
    public DuInfoTextView(android.content.Context context) {
        super(context);
    }

    public DuInfoTextView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DuInfoTextView(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DuInfoTextView(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    void initView(android.content.Context context) {
        mContext = context;
        updateInfo();
    }
    
    void updateInfo() {
        LogUtils.d(TAG, "updateInfo()");
        SettingsModel settingsModel = Rules.getInstance(mContext).getSettingsModel();
        String info = String.format("(云盾防御值【%d/%d】)", settingsModel.getDunCurrentCount(), settingsModel.getDunTotalCount());
        setText(info);
    }
    
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_NOTIFY_INFO_UPDATE) {
                updateInfo();
            }
        }
        
    };
    
    public void notifyInfoUpdate() {
        LogUtils.d(TAG, "notifyInfoUpdate()");
        mHandler.sendMessage(mHandler.obtainMessage(MSG_NOTIFY_INFO_UPDATE));
    }
}
