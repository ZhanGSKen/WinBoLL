package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/30 14:32
 * @Describe 联系人查询与短信发送窗口
 */
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.PhoneBean;
import cc.winboll.studio.mymessagemanager.utils.PhoneUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;

public class ComposeSMSActivity extends WinBoLLActivity implements IWinBoLLActivity {

    public static String TAG = "ComposeSMSActivity";
    public static String EXTRA_SMSBODY = "sms_body";
    private static final String MAP_NAME = "NAME";
    private static final String MAP_PHONE = "PHONE";

    private String mszSMSBody;
    private String mszScheme;
    private String mszPhoneTo;
    private TextView mtvTOName;
    private EditText metTONameSearch;
    private EditText metTO;
    private EditText metSMSBody;
    private SimpleAdapter mSimpleAdapter;
    private List<Map<String, Object>> mAdapterData = new ArrayList<Map<String, Object>>();
    private ListView mlvContracts;
    private List<PhoneBean> mListPhoneBeanContracts;
    private Toolbar mToolbar;
    private AOHPCTCSeekBar mAOHPCTCSeekBar;
    private RelativeLayout mrlContracts;

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
        LogUtils.d(TAG, "onCreate");
        setContentView(R.layout.activity_composesms);

        // 初始化Intent数据（增加空判断，避免NullPointerException）
        Intent intent = getIntent();
        if (intent != null) {
            mszSMSBody = intent.getStringExtra(EXTRA_SMSBODY);
            if (intent.getData() != null) {
                mszScheme = intent.getData().getScheme();
                mszPhoneTo = intent.getData().getSchemeSpecificPart();
            }
        }

        // 校验启动方式，非smsto则退出
        if (mszScheme == null || !"smsto".equals(mszScheme)) {
            ToastUtils.show("不支持的启动方式");
            finish();
            return;
        }

        initView();
        initAdapter(null); // 初始加载所有联系人
        setListViewPrePositionByPhone();
    }

    private void initView() {
        // 初始化标题栏
        mToolbar = (Toolbar) findViewById(R.id.activitycomposesmsASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_composesms));
        setActionBar(mToolbar);

        // 初始化联系人姓名显示和搜索栏
        mtvTOName = (TextView) findViewById(R.id.activitycomposesmsTextView2);
        mrlContracts = (RelativeLayout) findViewById(R.id.activitycomposesmsRelativeLayout1);
        metTONameSearch = (EditText) findViewById(R.id.activitycomposesmsEditText2);

        // 姓名搜索框文本变化监听
        metTONameSearch.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					metTO.setText(""); // 清空号码输入框，避免冲突
					String input = s == null ? "" : s.toString().trim();
					if (input.isEmpty()) {
						initAdapter(null); // 空搜索时显示所有联系人
					} else {
						setListViewPrePositionByName(); // 按姓名搜索
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// 无操作
				}

				@Override
				public void afterTextChanged(Editable s) {
					// 无操作
				}
			});

        // 初始化联系人列表（关键：设置单选模式，确保选中状态生效）
        mlvContracts = (ListView) findViewById(R.id.activitycomposesmsListView1);
        mlvContracts.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // 开启单选，与布局中一致

        // 初始化号码输入框（核心：优化文本变化监听逻辑）
        metTO = (EditText) findViewById(R.id.activitycomposesmsEditText1);
        if (mszPhoneTo != null) {
            metTO.setText(mszPhoneTo);
        }
        metTO.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					mtvTOName.setText(""); // 清空姓名显示
					String inputPhone = s == null ? "" : s.toString().trim();

					if (inputPhone.isEmpty()) {
						// 输入为空时，显示所有联系人
						initAdapter(null);
					} else {
						// 输入非空时，按号码搜索并更新列表（无结果则清空）
						filterListByPhone(inputPhone);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// 无操作
				}

				@Override
				public void afterTextChanged(Editable s) {
					// 无操作
				}
			});

        // 初始化发送控件
        mAOHPCTCSeekBar = (AOHPCTCSeekBar) findViewById(R.id.viewsmssendpart1AOHPCTCSeekBar1);
        Drawable thumbDrawable = getResources().getDrawable(R.drawable.ic_message); // Java 7兼容写法
        mAOHPCTCSeekBar.setThumb(thumbDrawable);
        mAOHPCTCSeekBar.setThumbOffset(20);
        mAOHPCTCSeekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {
				@Override
				public void onOHPCommit() {
					sendSMS();
				}
			});

        // 初始化短信内容输入框
        TextView tvAOHPCTCSeekBarMSG = (TextView) findViewById(R.id.viewsmssendpart1TextView1);
        tvAOHPCTCSeekBarMSG.setText(R.string.msg_100sendmsg);
        metSMSBody = (EditText) findViewById(R.id.viewsmssendpart1EditText1);
        if (mszSMSBody != null) {
            metSMSBody.setText(mszSMSBody);
        }
    }

    // 核心优化：根据输入号码筛选列表（无结果则显示空列表，优化选中逻辑）
    private void filterListByPhone(String inputPhone) {
        PhoneUtil phoneUtil = new PhoneUtil(this);
        List<PhoneBean> allContacts = phoneUtil.getPhoneList();
        List<PhoneBean> matchedContacts = new ArrayList<PhoneBean>();

        // 遍历所有联系人，匹配包含输入号码的联系人
        for (PhoneBean contact : allContacts) {
            if (contact.getTelPhone().contains(inputPhone)
                || phoneUtil.isTheSamePhoneNumber(contact.getTelPhone(), inputPhone)) {
                matchedContacts.add(contact);
            }
        }

        LogUtils.d(TAG, "号码搜索：输入'" + inputPhone + "', 匹配" + matchedContacts.size() + "个结果");

        // 用筛选结果更新列表（无结果则传入空列表）
        initAdapter(matchedContacts.isEmpty() ? new ArrayList<PhoneBean>() : matchedContacts);

        // 定位并选中匹配项（如果有）
        if (!matchedContacts.isEmpty()) {
            boolean isFound = false;
            for (int i = 0; i < matchedContacts.size(); i++) {
                PhoneBean item = matchedContacts.get(i);
                // 精确匹配号码（兼容区域码格式）
                if (phoneUtil.isTheSamePhoneNumber(item.getTelPhone(), inputPhone)) {
                    mtvTOName.setText(item.getName());
                    // 关键：先滚动到目标位置，再设置选中状态
                    mlvContracts.setSelection(i);
                    // 主动设置选中（确保样式生效，兼容部分系统）
                    mlvContracts.setItemChecked(i, true);
                    LogUtils.d(TAG, String.format("%s 匹配 %s，选中位置：%d", inputPhone, item.getTelPhone(), i));
                    isFound = true;
                    break;
                }
            }
            // 若未精确匹配，选中第一个结果
            /*if (!isFound) {
                mlvContracts.setSelection(0);
                mlvContracts.setItemChecked(0, true);
                mtvTOName.setText(matchedContacts.get(0).getName());
            }*/
        } else {
            mtvTOName.setText(""); // 无结果时清空姓名显示
        }
    }

    // 根据姓名搜索联系人
    private void setListViewPrePositionByName() {
        String searchName = metTONameSearch.getText().toString().trim();
        PhoneUtil phoneUtil = new PhoneUtil(this);
        List<PhoneBean> matchedContacts = phoneUtil.getPhonesByName(searchName);
        initAdapter(matchedContacts);
        if (!matchedContacts.isEmpty()) {
            // 选中第一个结果并设置样式
            mlvContracts.setSelection(0);
            mlvContracts.setItemChecked(0, true);
        }
    }

    // 初始定位号码对应的联系人
    private void setListViewPrePositionByPhone() {
        String inputPhone = metTO.getText().toString().trim();
        if (inputPhone.isEmpty()) {
            return;
        }
        filterListByPhone(inputPhone); // 复用筛选逻辑
    }

    // 获取号码匹配的位置（兼容旧逻辑）
    private int getContractsDataPrePositionByPhone(String szPhone) {
        if (mListPhoneBeanContracts == null || mListPhoneBeanContracts.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < mListPhoneBeanContracts.size(); i++) {
            PhoneBean bean = mListPhoneBeanContracts.get(i);
            if (bean.getTelPhone().compareTo(szPhone) >= 0) {
                return i;
            }
        }
        return 0;
    }

    // 获取姓名匹配的位置（兼容旧逻辑）
    private int getContractsDataPrePositionByName(String szName) {
        if (mListPhoneBeanContracts == null || mListPhoneBeanContracts.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < mListPhoneBeanContracts.size(); i++) {
            if (mListPhoneBeanContracts.get(i).getName().startsWith(szName)) {
                return i;
            }
        }
        return 0;
    }

	// 初始化或更新列表适配器
    private void initAdapter(List<PhoneBean> initData) {
        mAdapterData.clear(); // 清空旧数据
        final PhoneUtil phoneUtil = new PhoneUtil(this);

        // 确定数据源：传入的筛选数据或所有联系人
        if (initData != null) {
            mListPhoneBeanContracts = initData;
        } else {
            mListPhoneBeanContracts = phoneUtil.getPhoneList();
        }

        // 转换数据为SimpleAdapter所需格式
        if (mListPhoneBeanContracts != null) {
            for (PhoneBean bean : mListPhoneBeanContracts) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(MAP_NAME, bean.getName());
                map.put(MAP_PHONE, bean.getTelPhone());
                mAdapterData.add(map);
            }
        }

        // 初始化或更新适配器
        if (mSimpleAdapter == null) {
            mSimpleAdapter = new SimpleAdapter(
                ComposeSMSActivity.this,
                mAdapterData,
                R.layout.listview_contracts,
                new String[]{MAP_NAME, MAP_PHONE},
                new int[]{R.id.listviewcontractsTextView1, R.id.listviewcontractsTextView2}
            );
            mSimpleAdapter.setDropDownViewResource(R.layout.listview_contracts);
            mlvContracts.setAdapter(mSimpleAdapter);

            // 列表项点击事件：点击时主动设置选中状态，确保样式突显
            mlvContracts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (position < mAdapterData.size()) {
							// 1. 主动设置当前项为选中状态
							mlvContracts.setItemChecked(position, true);
							// 2. 更新号码输入框和姓名显示
							String phone = mAdapterData.get(position).get(MAP_PHONE).toString();
							metTO.setText(phone);
							mtvTOName.setText(phoneUtil.getNameByPhone(phone));
							// 3. 滚动到点击位置（确保可见）
							mlvContracts.setSelection(position);
						}
					}
				});

            // 列表项选中状态变化监听（可选，增强选中反馈）
            mlvContracts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// 未选中时无操作
					}
				});
        } else {
            // 数据更新时，先取消所有旧选中状态，再通知适配器刷新
            mlvContracts.clearChoices();
            mSimpleAdapter.notifyDataSetChanged();
        }
    }

    // 发送短信逻辑
    private void sendSMS() {
        String phoneTo = metTO.getText().toString().trim();
        if (phoneTo.isEmpty()) {
            ToastUtils.show("没有设置接收号码。");
            return;
        }
        String smsBody = metSMSBody.getText().toString().trim();
        if (smsBody.isEmpty()) {
            ToastUtils.show("没有消息内容可发送。");
            return;
        }
        if (SMSUtil.sendMessageByInterface2(ComposeSMSActivity.this, phoneTo, smsBody)) {
            finish();
        }
    }
}

