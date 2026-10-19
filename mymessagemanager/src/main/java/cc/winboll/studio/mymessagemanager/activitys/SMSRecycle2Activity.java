package cc.winboll.studio.mymessagemanager.activitys;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.adapters.SMSRecycle2Adapter;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import cc.winboll.studio.mymessagemanager.views.ProtectModeTextView;
import com.baoyz.widget.PullRefreshLayout;
import java.io.File;

public class SMSRecycle2Activity extends WinBoLLActivity implements IWinBoLLActivity {

    public static final String TAG = "SMSRecycle2Activity";
    private static final String SP_NAME = "smsrecycle2_config";
    private static final String KEY_SCALE = "recycle2_scale";

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    SMSRecycle2Adapter mSMSRecycle2Adapter;
    ProtectModeTextView mSampleProtectModeTextView;
    SharedPreferences mSP;

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
        setContentView(R.layout.activity_smsrecycle2);
        mToolbar = findViewById(R.id.activitysmsrecycle2ASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_about));
        setSupportActionBar(mToolbar);

        mSP = getSharedPreferences(SP_NAME, MODE_PRIVATE);

        mSampleProtectModeTextView = findViewById(R.id.activitysmsrecycle2SampleProtectModeTextView);
        mSampleProtectModeTextView.setContentTextWithScale(
            "调节本短信下方刻度滑条，可预览文本打乱效果；同时该进度条数值将作为回收站短信全局默认初始值。\n"
            + "刻度0 = 保持原文不打乱；\n"
            + "刻度数值越小，字符分组越细碎，文本打乱混乱程度越大；\n"
            + "刻度数值越大，字符连串分组越长，文本打乱混乱程度越小。",
            mSP.getInt(KEY_SCALE, 0));

        mSampleProtectModeTextView.setOnScaleChangedListener(new ProtectModeTextView.OnScaleChangedListener() {
                @Override
                public void onScaleChanged(int progress) {
                    mSP.edit().putInt(KEY_SCALE, progress).apply();
                    mSMSRecycle2Adapter.setScaleProgress(progress);
                    mSMSRecycle2Adapter.notifyDataSetChanged();
                }
            });

        initView();
    }

    void initView() {
        mRecyclerView = findViewById(R.id.activitysmsrecycle2RecyclerView1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mSMSRecycle2Adapter = new SMSRecycle2Adapter(this, mSP.getInt(KEY_SCALE, 0));
        mRecyclerView.setAdapter(mSMSRecycle2Adapter);

        final PullRefreshLayout pullRefreshLayout = findViewById(R.id.activitysmsrecycle2PullRefreshLayout1);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mSMSRecycle2Adapter.loadSMSRecycleList();
                    mSMSRecycle2Adapter.notifyDataSetChanged();
                    pullRefreshLayout.setRefreshing(false);
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_smsrecycle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int nItemId = item.getItemId();
        if (nItemId == R.id.item_cleansmsrecycle) {
            YesNoAlertDialog.show(this, "回收站清空确认", "是否清空回收站", mDeleteListener);
        }
        return true;
    }

    YesNoAlertDialog.OnDialogResultListener mDeleteListener = new YesNoAlertDialog.OnDialogResultListener() {

        @Override
        public void onNo() {
        }

        @Override
        public void onYes() {
            File file = new File(SMSRecycleUtil.getSMSRecycleListDataPath(SMSRecycle2Activity.this));
            file.delete();
            mSMSRecycle2Adapter.loadSMSRecycleList();
            mSMSRecycle2Adapter.notifyDataSetChanged();
        }
    };
}
