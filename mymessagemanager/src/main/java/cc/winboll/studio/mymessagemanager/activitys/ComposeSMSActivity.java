package cc.winboll.studio.mymessagemanager.activitys;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toolbar;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.ComposeSMSActivity;
import cc.winboll.studio.mymessagemanager.beans.PhoneBean;
import cc.winboll.studio.mymessagemanager.utils.PhoneUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposeSMSActivity extends BaseActivity {

    public static String TAG = "ComposeSMSActivity";

    public static String EXTRA_SMSBODY = "sms_body";

    static String MAP_NAME = "NAME";
    static String MAP_PHONE = "PHONE";

    String mszSMSBody;
    String mszScheme;
    String mszPhoneTo;
    EditText metTO;
    EditText metSMSBody;
    SimpleAdapter mSimpleAdapter;
    List<Map<String,Object>> mAdapterData = new ArrayList<>();
    ListView mlvContracts;
    List<PhoneBean> mListPhoneBeanContracts;
    Toolbar mToolbar;
    AOHPCTCSeekBar mAOHPCTCSeekBar;
    RelativeLayout mrlContracts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composesms);
        mszSMSBody = getIntent().getStringExtra(EXTRA_SMSBODY);
        mszScheme = getIntent().getData().getScheme();
        mszPhoneTo = getIntent().getData().getSchemeSpecificPart();
        if (!mszScheme.equals("smsto")) {
            // 其他方式未支持就退出
            finish();
        }
        // 初始化视图
        initView();
        // 设置适配器
        initAdapter();
        // 设置搜索到的匹配位置
        setListViewPrePosition();
    }

    //
    // 初始化视图
    //
    void initView() {
        //Drawable drawableFrame = AppCompatResources.getDrawable(this, R.drawable.bg_frame);

        // 初始化标题栏
        mToolbar = findViewById(R.id.activitycomposesmsASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_composesms));
        setActionBar(mToolbar);

        // 初始化联系人栏目框
        mrlContracts = findViewById(R.id.activitycomposesmsRelativeLayout1);
        //mrlContracts.setBackground(drawableFrame);

        // 初始化联系人列表
        mlvContracts = findViewById(R.id.activitycomposesmsListView1);

        // 初始化联系人输入框
        metTO = findViewById(R.id.activitycomposesmsEditText1);
        metTO.setText(mszPhoneTo);
        metTO.addTextChangedListener(new TextWatcher() {           
                @Override  
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setListViewPrePosition();
                }
                @Override  
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override  
                public void afterTextChanged(Editable s) {
                }
            });

        // 初始化发送拉动控件
        mAOHPCTCSeekBar = findViewById(R.id.viewsmssendpart1AOHPCTCSeekBar1);
        mAOHPCTCSeekBar.setThumb(getDrawable(R.drawable.ic_message));
        mAOHPCTCSeekBar.setThumbOffset(20);
        mAOHPCTCSeekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {
                @Override
                public void onOHPCommit() {
                    // 空号码不发送
                    mszPhoneTo = metTO.getText().toString();
                    if (mszPhoneTo.trim().equals("")) {
                        ToastUtils.show("没有设置接收号码。");
                        return;
                    }
                    // 空消息不发送
                    mszSMSBody = metSMSBody.getText().toString();
                    if (mszSMSBody.equals("")) {
                        ToastUtils.show("没有消息内容可发送。");
                        return;
                    }
                    // 发送消息
                    if (SMSUtil.sendMessageByInterface2(ComposeSMSActivity.this, mszPhoneTo, mszSMSBody)) {
                        ComposeSMSActivity.this.finish();
                    }
                }
			});

        // 初始化提示框
        TextView tvAOHPCTCSeekBarMSG = findViewById(R.id.viewsmssendpart1TextView1);
        tvAOHPCTCSeekBarMSG.setText(R.string.msg_100sendmsg);

        // 初始化发送消息框
        metSMSBody = findViewById(R.id.viewsmssendpart1EditText1);
        //metSMSBody.setBackground(drawableFrame);
        metSMSBody.setText(mszSMSBody);
    }

    //
    // 设置搜索到的匹配位置
    //
    void setListViewPrePosition() {
        int nPrePosition = getContractsDataPrePosition(metTO.getText().toString());
        mlvContracts.setSelected(false);
        mlvContracts.setSelection(nPrePosition);
    }

    //
    // 返回搜索到的匹配位置
    //
    int getContractsDataPrePosition(String szPhone) {
        for (int i = 0; i < mListPhoneBeanContracts.size(); i++) {
            if (mListPhoneBeanContracts.get(i).getTelPhone().compareTo(szPhone) > -1) {
                return i;
            }

        }
        return 0;
    }

    //
    // 初始化适配器
    //
    void initAdapter() {
        // 初始化联系人数据适配器
        mAdapterData = new ArrayList<>();
        // 读取联系人数据
        PhoneUtil phoneUtils = new PhoneUtil(this);
        mListPhoneBeanContracts = phoneUtils.getPhoneList();
        // 映射联系人数据给适配器数据对象
        for (int i = 0;i < mListPhoneBeanContracts.size();i++) {
            Map<String,Object> map =new HashMap<>();
            map.put(MAP_NAME, mListPhoneBeanContracts.get(i).getName());
            map.put(MAP_PHONE, mListPhoneBeanContracts.get(i).getTelPhone());
            mAdapterData.add(map);
        }
        // 绑定适配器与数据
        mSimpleAdapter = new SimpleAdapter(ComposeSMSActivity.this, mAdapterData, R.layout.listview_contracts
                                           , new String[]{MAP_NAME, MAP_PHONE}
                                           , new int[]{R.id.listviewcontractsTextView1, R.id.listviewcontractsTextView2});
        mSimpleAdapter.setDropDownViewResource(R.layout.listview_contracts);
        mlvContracts.setAdapter(mSimpleAdapter);
        mlvContracts.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    metTO.setText(mAdapterData.get(position).get(MAP_PHONE).toString());

                }
            });
    }
}
