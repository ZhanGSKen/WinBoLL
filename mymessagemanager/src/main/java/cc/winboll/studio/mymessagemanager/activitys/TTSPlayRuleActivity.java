package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 12:50:52
 * @Describe TTS 语音播放规则规则设置窗口
 */
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.adapters.TTSRuleBeanRecyclerViewAdapter;
import cc.winboll.studio.mymessagemanager.beans.TTSPlayRuleBean;
import cc.winboll.studio.mymessagemanager.utils.FileUtil;
import cc.winboll.studio.mymessagemanager.utils.TTSPlayRuleUtil;

public class TTSPlayRuleActivity extends BaseActivity {

    public static final String TAG = "TTSPlayRuleActivity";

    public static final int MSG_RELOAD = 0;

	public static final String EXTRA_TTSDEMOTEXT = "EXTRA_TTSDEMOTEXT";

	Toolbar mToolbar;
    TTSRuleBeanRecyclerViewAdapter mTTSRuleBeanRecyclerViewAdapter;
	TTSPlayRuleUtil mTTSPlayRuleUtil;
	TTSPlayRuleBean mTTSRuleBeanCurrent;
    RecyclerView mRecyclerView;
    EditText metCurrentDemoSMSText;
    EditText metPatternText;
    EditText metCurrentTTSRuleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ttsplayrule);

		mTTSPlayRuleUtil = TTSPlayRuleUtil.getInstance(TTSPlayRuleActivity.this);

        initView();

        // 设置窗口消息处理
        setOnActivityMessageReceived(new IOnActivityMessageReceived(){
                @Override
                public void onActivityMessageReceived(Message msg) {
                    switch (msg.what) {
                        case MSG_RELOAD : {
                                //Toast.makeText(getApplication(), "MSG_RELOAD", Toast.LENGTH_SHORT).show();
                                mTTSRuleBeanRecyclerViewAdapter.reloadConfigData();
                                break;
                            }
                    }
                }
            });
	}

    void initView() {
        // 初始化标题栏
        mToolbar = findViewById(R.id.activityttsplayruleASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.text_ttsrule));
        setSupportActionBar(mToolbar);

        metCurrentDemoSMSText = findViewById(R.id.activityttsplayruleEditText1);
        metPatternText = findViewById(R.id.activityttsplayruleEditText2);
        metCurrentTTSRuleText = findViewById(R.id.activityttsplayruleEditText3);

		Button btnTestTTSRule = findViewById(R.id.activityttsplayruleButton1);
		btnTestTTSRule.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TTSPlayRuleBean ttsRuleBean = new TTSPlayRuleBean();
                    ttsRuleBean.setDemoSMSText(metCurrentDemoSMSText.getText().toString());
                    ttsRuleBean.setPatternText(metPatternText.getText().toString());
                    ttsRuleBean.setTtsRuleText(metCurrentTTSRuleText.getText().toString());

                    String sz = mTTSPlayRuleUtil.testTTSAnalyzeModeReply(ttsRuleBean);
                    Toast.makeText(getApplication(), sz, Toast.LENGTH_SHORT).show();
				}
			});

		Button btnAcceptTTSRule = findViewById(R.id.activityttsplayruleButton2);
		btnAcceptTTSRule.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mTTSRuleBeanCurrent != null) {
						mTTSRuleBeanCurrent.setDemoSMSText(metCurrentDemoSMSText.getText().toString());
                        mTTSRuleBeanCurrent.setPatternText(metPatternText.getText().toString());
                        mTTSRuleBeanCurrent.setTtsRuleText(metCurrentTTSRuleText.getText().toString());
                        mTTSRuleBeanRecyclerViewAdapter.saveConfigData();
					} else {
                        if (!metCurrentDemoSMSText.getText().toString().equals("")) {
                            mTTSRuleBeanCurrent = new TTSPlayRuleBean();
                            mTTSRuleBeanCurrent.setDemoSMSText(metCurrentDemoSMSText.getText().toString());
                            mTTSRuleBeanCurrent.setPatternText(metPatternText.getText().toString());
                            mTTSRuleBeanCurrent.setTtsRuleText(metCurrentTTSRuleText.getText().toString());
                            mTTSRuleBeanRecyclerViewAdapter.addNewTTSRuleBean(mTTSRuleBeanCurrent);
                            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                            layoutManager.scrollToPositionWithOffset(0, 0);
                        }
                    }
				}
			});

		// 绑定控件
        mRecyclerView = findViewById(R.id.activityttsplayruleRecyclerView1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mTTSRuleBeanRecyclerViewAdapter = new TTSRuleBeanRecyclerViewAdapter(TTSPlayRuleActivity.this, mOnTTSRuleChangeListener);
        mRecyclerView.setAdapter(mTTSRuleBeanRecyclerViewAdapter);

		// 处理传入的窗口启动参数
        //
        String szNewDemoText = getIntent().getStringExtra(EXTRA_TTSDEMOTEXT);
        metCurrentDemoSMSText.setText(szNewDemoText);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_rule, menu);
        return true;
    }

    public void onScrollToDemoSMSTextMatchingRule(View view) {
        int rowIndex = mTTSPlayRuleUtil.speakTTSAnalyzeModeText(metCurrentDemoSMSText.getText().toString());

        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(rowIndex, 0);
        Toast.makeText(getApplication(), "当前文本匹配的规则序号为 " + Integer.toString(rowIndex + 1), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int nItemId = item.getItemId();
        if (nItemId == R.id.item_rule_share) {
            TTSPlayRuleBean bean = new TTSPlayRuleBean();
            FileUtil.shareJSONFile(this, bean.getBeanListJsonFilePath(TTSPlayRuleActivity.this));
        } else if (nItemId == R.id.item_rule_reset) {
            showResetConfigDialog();
        } else if (nItemId == R.id.item_rule_clean) {
            showCleanConfigDialog();
        } 

        return true;
    }

    //
    // 规则数据重置对话框
    //
    void showResetConfigDialog() {
        mTTSPlayRuleUtil.resetConfig();
    }

    //
    // 规则数据重置对话框
    //
    void showCleanConfigDialog() {
        mTTSPlayRuleUtil.cleanConfig();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //
    // 规则项选择事件监听类
    //
	TTSRuleBeanRecyclerViewAdapter.OnTTSRuleChangeListener mOnTTSRuleChangeListener = new TTSRuleBeanRecyclerViewAdapter.OnTTSRuleChangeListener() {
		@Override
		public void onTTSRuleChange(TTSPlayRuleBean bean) {
			metCurrentDemoSMSText.setText(bean.getDemoSMSText());
			metPatternText.setText(bean.getPatternText());
			metCurrentTTSRuleText.setText(bean.getTtsRuleText());
			mTTSRuleBeanCurrent = bean;
		}
	};
}
