package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 16:56:18
 * @Describe 短信回收站
 */
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libappbase.dialogs.YesNoAlertDialog;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.SMSRecycleActivity;
import cc.winboll.studio.mymessagemanager.adapters.SMSRecycleAdapter;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import com.baoyz.widget.PullRefreshLayout;
import java.io.File;

public class SMSRecycleActivity extends BaseActivity {

    public static final String TAG = "SMSRecycleActivity";

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    SMSRecycleAdapter mSMSRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsrecycle);
        // 初始化标题栏
        mToolbar = findViewById(R.id.activitysmsrecycleASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_about));
        setSupportActionBar(mToolbar);
        initView();
    }

    void initView() {
        // 绑定控件
        mRecyclerView = findViewById(R.id.activitysmsrecycleRecyclerView1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mSMSRecycleAdapter = new SMSRecycleAdapter(this);
        mRecyclerView.setAdapter(mSMSRecycleAdapter);

        final PullRefreshLayout pullRefreshLayout = findViewById(R.id.activitysmsrecyclePullRefreshLayout1);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mSMSRecycleAdapter.loadSMSRecycleList();
                    mSMSRecycleAdapter.notifyDataSetChanged();
                    pullRefreshLayout.setRefreshing(false);
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
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
            File file = new File(SMSRecycleUtil.getSMSRecycleListDataPath(SMSRecycleActivity.this));
            file.delete();
            mSMSRecycleAdapter.loadSMSRecycleList();
            mSMSRecycleAdapter.notifyDataSetChanged();
        }
    };
}
