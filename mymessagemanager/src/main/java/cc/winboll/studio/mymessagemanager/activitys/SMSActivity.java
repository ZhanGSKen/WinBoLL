package cc.winboll.studio.mymessagemanager.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.adapters.SMSArrayAdapter;
import cc.winboll.studio.mymessagemanager.utils.AddressUtils;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.ViewUtil;
import cc.winboll.studio.mymessagemanager.views.BottomPositionFixedScrollView;
import cc.winboll.studio.mymessagemanager.views.SMSListViewForScrollView;
import android.app.Activity;

public class SMSActivity extends WinBoLLActivity implements IWinBoLLActivity {
    public static String TAG = "SMSActivity";
    public static final String ACTION_NOTIFY_SMS_CHANGED = "cc.winboll.studio.mymessagemanager.activitys.SMSActivity.ACTION_NOTIFY_SMS_CHANGED";
    public static final String EXTRA_PHONE = "Phone";
    final static int MSG_SET_FOCUS = 0;

    SMSListViewForScrollView mlvSMS;
    Toolbar mToolbar;
    String mszPhoneTo;
    SMSArrayAdapter mSMSArrayAdapter;
    BottomPositionFixedScrollView mScrollView1;
    EditText metSMSBody;
    SMSActivityBroadcastReceiver mSMSActivityBroadcastReceiver;
    Handler mSetFocusHandler;
    private boolean isImeVisible = false;

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public String getTag() {
		return TAG;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        initView();
        scrollScrollView();
        setupImeStatusListener();

        // 新增：监听窗口加载完成，触发mScrollView1滚动到底部
        setupScrollToBottomAfterWindowLoaded();
    }

    // 新增：窗口加载完成后让mScrollView1滚动到底部
    private void setupScrollToBottomAfterWindowLoaded() {
        final View rootView = findViewById(android.R.id.content);
        // 监听根布局绘制完成（窗口加载完成的标志）
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					// 滚动到底部
					mScrollView1.post(new Runnable() {
							@Override
							public void run() {
								mScrollView1.fullScroll(ScrollView.FOCUS_DOWN);
							}
						});

					// 移除监听，避免重复触发
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			});
    }

    private void setupImeStatusListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					int rootViewHeight = rootView.getHeight();
					int screenHeight = getResources().getDisplayMetrics().heightPixels;
					int imeThreshold = dp2px(200);

					boolean currentImeVisible = (screenHeight - rootViewHeight) > imeThreshold;

					if (currentImeVisible != isImeVisible) {
						isImeVisible = currentImeVisible;
						setupScrollView1Height();
						if (!isImeVisible) {
							metSMSBody.clearFocus();
						}
					}

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
					setupImeStatusListener();
				}
			});
    }

    private int dp2px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    /*static class MyHandler extends Handler {
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
	 theActivity.setupScrollView1Height();
	 break;
	 default:
	 break;
	 }
	 super.handleMessage(msg);
	 }
	 }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSMSActivityBroadcastReceiver);
    }

    void initView() {
        mszPhoneTo = getIntent().getStringExtra(EXTRA_PHONE);
        if (mszPhoneTo == null || mszPhoneTo.trim().equals("")) {
            finish();
        }

        mToolbar = (Toolbar) findViewById(R.id.activitysmsASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_smsinphone) + " < Phone : " + AddressUtils.getFormattedAddress(mszPhoneTo) + " >");
        setActionBar(mToolbar);

        mScrollView1 = (BottomPositionFixedScrollView) findViewById(R.id.activitysmsScrollView1);

        metSMSBody = (EditText) findViewById(R.id.viewsmssendpart1EditText1);
        metSMSBody.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setupScrollView1Height();
				}
			});
        metSMSBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					setupScrollView1Height();
				}
			});

        final AOHPCTCSeekBar aOHPCTCSeekBar = (AOHPCTCSeekBar) findViewById(R.id.viewsmssendpart1AOHPCTCSeekBar1);
        aOHPCTCSeekBar.setThumb(getDrawable(R.drawable.ic_message));
        aOHPCTCSeekBar.setThumbOffset(20);
        aOHPCTCSeekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {
				@Override
				public void onOHPCommit() {
					sendSMS();
				}
			});

        TextView tvAOHPCTCSeekBarMSG = (TextView) findViewById(R.id.viewsmssendpart1TextView1);
        tvAOHPCTCSeekBarMSG.setText(R.string.msg_100sendmsg);

        mlvSMS = (SMSListViewForScrollView) findViewById(R.id.activitysmsSMSListViewForScrollView1);
        mSMSArrayAdapter = new SMSArrayAdapter(SMSActivity.this, mszPhoneTo);
        mlvSMS.setAdapter(mSMSArrayAdapter);

        mlvSMS.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
						mSMSArrayAdapter.cancelMessageNotification();
					}
				}
			});

        mSMSActivityBroadcastReceiver = new SMSActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_NOTIFY_SMS_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mSMSActivityBroadcastReceiver, intentFilter);
    }

    private void setupScrollView1Height() {
        mScrollView1.postDelayed(new Runnable() {
				@Override
				public void run() {
					final ScrollView scrollView2 = (ScrollView) findViewById(R.id.activitysmsScrollView2);
					final BottomPositionFixedScrollView scrollView1 = (BottomPositionFixedScrollView) findViewById(R.id.activitysmsScrollView1);
					final View includeView = findViewById(R.id.activitysmsinclude1);

					scrollView2.post(new Runnable() {
							@Override
							public void run() {
								int scrollView2Height = scrollView2.getHeight();
								int includeHeight = includeView.getHeight();
								int targetHeight = Math.max(scrollView2Height - includeHeight, 0);

								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollView1.getLayoutParams();
								params.height = targetHeight;
								scrollView1.setLayoutParams(params);
							}
						});
				}
			}, 100);
    }

    public void updateSMSView() {
        mSMSArrayAdapter.reLoadSMSList(SMSActivity.this, mszPhoneTo);
        mSMSArrayAdapter.notifyDataSetChanged();
    }

    void scrollScrollView() {
        ViewUtil.scrollScrollView(mScrollView1);
    }

    void sendSMS() {
        String szSMSBody = metSMSBody.getText().toString();
        if (szSMSBody.equals("")) {
            Toast.makeText(getApplication(), "没有消息内容可发送。", Toast.LENGTH_SHORT).show();
            return;
        }

        if (SMSUtil.sendMessageByInterface2(this, mszPhoneTo, szSMSBody)) {
            metSMSBody.setText("");
            metSMSBody.clearFocus();
            new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						updateSMSView();
						ViewUtil.scrollScrollView(mScrollView1);
					}
				}, 1000);
        }
    }

    class SMSActivityBroadcastReceiver extends BroadcastReceiver {
        public SMSActivityBroadcastReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_NOTIFY_SMS_CHANGED.equals(intent.getAction())) {
                updateSMSView();
                ViewUtil.scrollScrollView(mScrollView1);
            } else {
                throw new IllegalStateException("Unexpected value: " + intent.getAction());
            }
        }
    }
}

