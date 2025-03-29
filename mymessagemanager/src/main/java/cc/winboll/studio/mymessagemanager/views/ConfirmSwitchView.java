package cc.winboll.studio.mymessagemanager.views;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2023/07/25 13:37:55
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.dialogs.YesNoAlertDialog;

public class ConfirmSwitchView extends Switch {

    public static final String TAG = "SwitchView";

    Context mContext;

    public ConfirmSwitchView(Context context) {
        super(context);
        initView(context);
    }

    public ConfirmSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ConfirmSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ConfirmSwitchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    void initView(Context context) {
        mContext = context;
        /*TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SMSView, 0, 0);
         colorInbox = a.getColor(R.styleable.SMSView_attrSMSViewInboxColor, 0);
         colorSend = a.getColor(R.styleable.SMSView_attrSMSViewSendColor, 0);
         a.recycle();*/
    }

    @Override
    public void setOnClickListener(final View.OnClickListener l) {
        super.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isChecked = isChecked();
                    StringBuilder sbMessage = new StringBuilder("请确定[");
                    sbMessage.append(isChecked ?"开启": "关闭");
                    sbMessage.append("]操作。");
                    // 在这里添加您的点击事件响应逻辑
                    YesNoAlertDialog.show(mContext, getText().toString(), sbMessage.toString(), new YesNoAlertDialog.OnDialogResultListener(){
                            @Override
                            public void onYes() {
                                LogUtils.d(TAG, "onYes");
                                setChecked(isChecked);
                            }
                            @Override
                            public void onNo() {
                                LogUtils.d(TAG, "onNo");
                                setChecked(!isChecked);
                            }
                        });
                    //Toast.makeText(getContext(), "Switch clicked", Toast.LENGTH_SHORT).show();
                    // 确保调用了父类的onClick()方法
                    l.onClick(v);
                }
            });
    }
}
