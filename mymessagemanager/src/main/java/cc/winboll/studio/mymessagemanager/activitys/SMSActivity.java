package cc.winboll.studio.mymessagemanager.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.SMSActivity;
import cc.winboll.studio.mymessagemanager.adapters.SMSArrayAdapter;
import cc.winboll.studio.mymessagemanager.utils.AddressUtils;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.ViewUtil;
import cc.winboll.studio.mymessagemanager.views.SMSListViewForScrollView;
import java.lang.ref.WeakReference;

public class SMSActivity extends BaseActivity {
    public static String TAG = "SMSActivity";

    public static final String ACTION_NOTIFY_SMS_CHANGED = "cc.winboll.studio.mymessagemanager.activitys.SMSActivity.ACTION_NOTIFY_SMS_CHANGED";

    public static final String EXTRA_PHONE = "Phone";
    final static int MSG_SET_FOCUS = 0;

    SMSListViewForScrollView mlvSMS;
    Toolbar mToolbar;
    String mszPhoneTo;
    SMSArrayAdapter mSMSArrayAdapter;
    ScrollView mScrollView;
    EditText metSMSBody;
    SMSActivityBroadcastReceiver mSMSActivityBroadcastReceiver;
    Handler mSetFocusHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        initView();
		mSetFocusHandler = new MyHandler(SMSActivity.this);
        scrollScrollView();

        // 每隔一定时间设置输入框获得焦点
        //
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {}
                    Message message = mSetFocusHandler.obtainMessage(MSG_SET_FOCUS);
                    mSetFocusHandler.sendMessage(message);
                }
            }}.start();
    }

    //
    // 设置输入框获得焦点的类
	//
	static class MyHandler extends Handler {
		WeakReference<SMSActivity> mActivity;  
		MyHandler(SMSActivity activity) {  
			mActivity = new WeakReference<SMSActivity>(activity);  
		}
        public void handleMessage(Message msg) {
			SMSActivity theActivity = mActivity.get();
            switch (msg.what) {
                case MSG_SET_FOCUS:
                    theActivity.metSMSBody.setFocusable(true);
                    theActivity.metSMSBody.requestFocus();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSMSActivityBroadcastReceiver);
    }

    void initView() {
        // 发送端空号码退出
        mszPhoneTo = getIntent().getStringExtra(EXTRA_PHONE);
        if (mszPhoneTo == null || mszPhoneTo.trim().equals("")) {
            finish();
        }

        // 初始化标题栏
        mToolbar = findViewById(R.id.activitysmsASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_smsinphone) + " < Phone : " + AddressUtils.getFormattedAddress(mszPhoneTo) + " >");
        setActionBar(mToolbar);

        // 初始化滚动窗口
        mScrollView = findViewById(R.id.activitysmsinphoneScrollView1);

        // 初始化发送消息框
        //Drawable drawableFrame = AppCompatResources.getDrawable(this, R.drawable.bg_frame);
        metSMSBody = findViewById(R.id.viewsmssendpart1EditText1);
        //metSMSBody.setBackground(drawableFrame);

        // 初始化发送拉动控件
        final AOHPCTCSeekBar aOHPCTCSeekBar = findViewById(R.id.viewsmssendpart1AOHPCTCSeekBar1);
        aOHPCTCSeekBar.setThumb(getDrawable(R.drawable.ic_message));
        aOHPCTCSeekBar.setThumbOffset(20);
        aOHPCTCSeekBar.setOnOHPCListener(
            new AOHPCTCSeekBar.OnOHPCListener(){
                @Override
                public void onOHPCommit() {
                    //Toast.makeText(getApplication(), "Send", Toast.LENGTH_SHORT).show();
                    sendSMS();
                }
            });

        // 初始化提示框
        TextView tvAOHPCTCSeekBarMSG = findViewById(R.id.viewsmssendpart1TextView1);
        tvAOHPCTCSeekBarMSG.setText(R.string.msg_100sendmsg);

        mlvSMS = (SMSListViewForScrollView) findViewById(R.id.activitysmsinphoneListView1);

        // 准备数据
        mSMSArrayAdapter = new SMSArrayAdapter(SMSActivity.this, mszPhoneTo);
        mlvSMS.setAdapter(mSMSArrayAdapter);

        // 设置短信列表滚动到底部就取消已发送的通知消息
        //
        mlvSMS.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                        // 滑动到了底部
                        mSMSArrayAdapter.cancelMessageNotification();
                    }
                }
            });

        mSMSActivityBroadcastReceiver = new SMSActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFY_SMS_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mSMSActivityBroadcastReceiver, intentFilter);

        /*SMSView mSMSView = findViewById(R.id.viewsmssendSMSView1);
        mSMSView.setSMSType(SMSView.SMSType.SEND);*/
    }

    //
    // 更新信息列表
    //
    public void updateSMSView() {
        mSMSArrayAdapter.reLoadSMSList(SMSActivity.this, mszPhoneTo);
        mSMSArrayAdapter.notifyDataSetChanged();
    }

    //
    // 滚动消息文本框
    //
    void scrollScrollView() {

        ViewUtil.scrollScrollView(mScrollView);

    }

    //
    // 发送短信
    //
    void sendSMS() {
        // 空消息不发送
        String szSMSBody = metSMSBody.getText().toString();
        if (szSMSBody.equals("")) {
            Toast.makeText(getApplication(), "没有消息内容可发送。", Toast.LENGTH_SHORT).show();
            return;
        }

        // 发送短信
        if (SMSUtil.sendMessageByInterface2(this, mszPhoneTo, szSMSBody)) {
            metSMSBody.setText("");
            new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        updateSMSView();
                        ViewUtil.scrollScrollView(mScrollView);
                    }
                }, 1000);
        }
    }

    class SMSActivityBroadcastReceiver extends BroadcastReceiver {

        public SMSActivityBroadcastReceiver() {
            //LogUtils.d(TAG, "SMSActivityBroadcastReceiver()");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_NOTIFY_SMS_CHANGED :
                    //Toast.makeText(context, "ACTION_NOTIFY_SMS_CHANGED", Toast.LENGTH_SHORT).show();
                    updateSMSView();
                    ViewUtil.scrollScrollView(mScrollView);
                    //LogUtils.d(TAG, "ACTION_NOTIFY_SMS_CHANGED");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + intent.getAction());
            }

        }

    }

}
