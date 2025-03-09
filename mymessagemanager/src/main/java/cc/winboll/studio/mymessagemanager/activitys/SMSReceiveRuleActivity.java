package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 12:50:52
 * @Describe 短信匹配过滤规则设置窗口
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.mymessagemanager.GlobalApplication;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.SMSReceiveRuleActivity;
import cc.winboll.studio.mymessagemanager.adapters.SMSAcceptRuleArrayAdapter;
import cc.winboll.studio.mymessagemanager.beans.SMSAcceptRuleBean;
import cc.winboll.studio.mymessagemanager.utils.FileUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSReceiveRuleUtil;
import com.baoyz.widget.PullRefreshLayout;

public class SMSReceiveRuleActivity extends BaseActivity {

    public static final String TAG = "SMSReceiveRuleActivity";

    Context mContext;
    RecyclerView mRecyclerView;
    Toolbar mToolbar;
    RadioButton mrbAccept;
    RadioButton mrbRefuse;
    CheckBox mcbEnable;
    SMSAcceptRuleBean mSMSAcceptRuleBeanAdd;
    SMSAcceptRuleArrayAdapter mSMSAcceptRuleArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsacceptrulesetting);
        mContext = SMSReceiveRuleActivity.this;
        initSMSAcceptRuleBeanAdd();
        // 初始化视图
        initView();
    }

    //
    // 初始化视图
    //
    public void initView() {
        // 初始化标题栏
        mToolbar = findViewById(R.id.activitysmsacceptrulesettingASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.text_smsrule));
        setSupportActionBar(mToolbar);

        mrbAccept = findViewById(R.id.activitysmsacceptrulesettingRadioButton1);
        mrbRefuse = findViewById(R.id.activitysmsacceptrulesettingRadioButton2);
        mcbEnable = findViewById(R.id.activitysmsacceptrulesettingCheckBox1);
        if (mSMSAcceptRuleBeanAdd.getRuleType() == SMSAcceptRuleBean.RuleType.ACCEPT) {
            mrbAccept.setChecked(true);
            mrbRefuse.setChecked(false);
        }
        if (mSMSAcceptRuleBeanAdd.getRuleType() == SMSAcceptRuleBean.RuleType.REFUSE) {
            mrbAccept.setChecked(false);
            mrbRefuse.setChecked(true);
        }
        mcbEnable.setChecked(mSMSAcceptRuleBeanAdd.isEnable());

		Button btnAddSMSAcceptRule = findViewById(R.id.activitysmsacceptrulesettingButton1);
		btnAddSMSAcceptRule.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText et = findViewById(R.id.activitysmsacceptrulesettingEditText1);
					String szRule = et.getText().toString().trim();
					if (szRule.equals("")) {
						Toast.makeText(getApplication(), "空字符串规则不能添加", Toast.LENGTH_SHORT).show();
					} else {
                        mSMSAcceptRuleBeanAdd.setRuleData(et.getText().toString());
                        mSMSAcceptRuleBeanAdd.setIsEnable(mcbEnable.isChecked());
                        mSMSAcceptRuleBeanAdd.setRuleType(mrbRefuse.isChecked() ?SMSAcceptRuleBean.RuleType.REFUSE: SMSAcceptRuleBean.RuleType.ACCEPT);
						mSMSAcceptRuleArrayAdapter.addSMSAcceptRule(mSMSAcceptRuleBeanAdd);
                        initSMSAcceptRuleBeanAdd();
						et.setText("");
						Toast.makeText(getApplication(), "已添加规则 : " + szRule, Toast.LENGTH_SHORT).show();
					}
				}
            });

        // 绑定控件
        mRecyclerView = findViewById(R.id.activitysmsacceptrulesettingRecyclerView1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mSMSAcceptRuleArrayAdapter = new SMSAcceptRuleArrayAdapter(this);
        mRecyclerView.setAdapter(mSMSAcceptRuleArrayAdapter);

        final PullRefreshLayout pullRefreshLayout = findViewById(R.id.activitysmsacceptrulesettingPullRefreshLayout1);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    pullRefreshLayout.setRefreshing(false);
                    mSMSAcceptRuleArrayAdapter.loadConfigData();
                    mSMSAcceptRuleArrayAdapter.notifyDataSetChanged();
                }
            });
    }

    void initSMSAcceptRuleBeanAdd() {
        mSMSAcceptRuleBeanAdd = new SMSAcceptRuleBean(GlobalApplication.USER_ID, "", true, SMSAcceptRuleBean.RuleType.REFUSE, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_rule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int nItemId = item.getItemId();
        if (nItemId == R.id.item_rule_share) {
            //SMSReceiveRuleUtil smsAcceptRuleConfig = SMSReceiveRuleUtil.getInstance(this, false);
            SMSAcceptRuleBean beanTemp = new SMSAcceptRuleBean();
            String szConfigPath = beanTemp.getBeanListJsonFilePath(mContext);
            FileUtil.shareJSONFile(SMSReceiveRuleActivity.this, szConfigPath);
        } else if (nItemId == R.id.item_rule_reset) {
            showResetConfigDialog();
        } else if (nItemId == R.id.item_rule_clean) {
            showCleanConfigDialog();
        } 
        return true;
    }

    //
    // 短信匹配过滤规则数据重置对话框
    //
    void showResetConfigDialog() {
        Dialog alertDialog = new AlertDialog.Builder(this).
            setTitle("确定重置？").
            setMessage("您确定重置短信接收规则为默认设置吗？").
            setIcon(R.drawable.ic_launcher).
            setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    SMSReceiveRuleUtil smsAcceptRuleConfig = SMSReceiveRuleUtil.getInstance(getApplicationContext(), false);
                    smsAcceptRuleConfig.resetConfig();
                    mSMSAcceptRuleArrayAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplication(), "Rules Reset", Toast.LENGTH_SHORT).show();
                }
            }).
            setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            }).
            /*setNeutralButton("查看详情", new DialogInterface.OnClickListener() {

             @Override
             public void onClick(DialogInterface dialog, int which) {
             // TODO Auto-generated method stub
             }
             }).*/
            create();
        alertDialog.show();
    }

    //
    // 短信匹配过滤规则数据清空对话框
    //
    void showCleanConfigDialog() {
        Dialog alertDialog = new AlertDialog.Builder(this).
            setTitle("确定清理").
            setMessage("您确定清理所有短信接收规则吗？").
            setIcon(R.drawable.ic_launcher).
            setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    SMSReceiveRuleUtil smsAcceptRuleConfig = SMSReceiveRuleUtil.getInstance(getApplicationContext(), false);
                    smsAcceptRuleConfig.cleanConfig();
                    mSMSAcceptRuleArrayAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplication(), "Rules Cleaned.", Toast.LENGTH_SHORT).show();
                }
            }).
            setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            }).
            /*setNeutralButton("查看详情", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
             // TODO Auto-generated method stub
             }
             }).*/
            create();
        alertDialog.show();
    }

    public void onAcceptRuleType(View view) {
        mrbRefuse.setChecked(false);
    }

    public void onRefuseRuleType(View view) {
        mrbAccept.setChecked(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSMSAcceptRuleArrayAdapter.loadConfigData();
        mSMSAcceptRuleArrayAdapter.notifyDataSetChanged();
    }
}
