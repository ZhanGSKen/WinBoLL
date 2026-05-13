package cc.winboll.studio.contacts.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.model.SettingsBean;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/03/02 21:11:03
 * @Describe 云盾防御信息视图控件：展示云盾防御值统计，并支持消息驱动更新
 */
public class DuInfoTextView extends TextView {
    // ====================== 常量定义区 ======================
    public static final String TAG = "DuInfoTextView";
    public static final int MSG_NOTIFY_INFO_UPDATE = 0;

    // ====================== 成员变量区 ======================
    private Context mContext;
    private Handler mHandler;

    // ====================== 构造函数区 ======================
    public DuInfoTextView(Context context) {
        super(context);
        initView(context);
    }

    public DuInfoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DuInfoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public DuInfoTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    // ====================== 初始化方法区 ======================
    private void initView(Context context) {
        LogUtils.d(TAG, "initView: 开始初始化云盾信息控件");
        this.mContext = context;
        initHandler();
        updateInfo();
        LogUtils.d(TAG, "initView: 云盾信息控件初始化完成");
    }

    /**
     * 初始化 Handler，处理信息更新消息
     */
    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_NOTIFY_INFO_UPDATE) {
                    LogUtils.d(TAG, "handleMessage: 收到信息更新消息，开始刷新视图");
                    updateInfo();
                }
            }
        };
    }

    // ====================== 视图更新方法区 ======================
    /**
     * 更新云盾防御信息显示
     */
    private void updateInfo() {
        LogUtils.d(TAG, "updateInfo: 开始更新云盾防御信息");
        // 空值校验，避免上下文为空导致异常
        if (mContext == null) {
            LogUtils.w(TAG, "updateInfo: 上下文为空，跳过信息更新");
            setText("(云盾防御值【--/--】)");
            return;
        }

        try {
            SettingsBean settingsModel = Rules.getInstance(mContext).getSettingsModel();
            // 校验 SettingsBean 非空，防止空指针
            if (settingsModel == null) {
                LogUtils.w(TAG, "updateInfo: SettingsBean 为空，显示默认值");
                setText("(云盾防御值【--/--】)");
                return;
            }

            int currentCount = settingsModel.getDunCurrentCount();
            int totalCount = settingsModel.getDunTotalCount();
            String info = String.format("(云盾防御值【%d/%d】)", currentCount, totalCount);
            setText(info);
            LogUtils.d(TAG, "updateInfo: 云盾防御信息更新完成 | " + info);
        } catch (Exception e) {
            LogUtils.e(TAG, "updateInfo: 信息更新异常", e);
            setText("(云盾防御值【--/--】)");
        }
    }

    /**
     * 对外提供的信息更新通知方法
     */
    public void notifyInfoUpdate() {
        LogUtils.d(TAG, "notifyInfoUpdate: 发送信息更新通知");
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_NOTIFY_INFO_UPDATE));
        } else {
            LogUtils.w(TAG, "notifyInfoUpdate: Handler 未初始化，无法发送更新消息");
        }
    }
}

