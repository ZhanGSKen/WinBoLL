package cc.winboll.studio.contacts.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.activities.UnitTestActivity;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.services.LimitedTimeSpecialChannelService;
import cc.winboll.studio.contacts.utils.IntUtils;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/02 16:07:04
 * @Describe 规则单元测试页面
 */
public class UnitTestActivity extends WinBollActivity implements IWinBoLLActivity {

    // ====================== 常量定义区 ======================
    public static final String TAG = "UnitTestActivity";

    // ====================== UI控件区 ======================
    private LogView logView;
    private EditText etPhone;

    // ====================== 接口实现区 ======================
    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }
	
    // ====================== 生命周期函数区 ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: 单元测试页面开始创建");
        setContentView(R.layout.activity_unittest);

        // 初始化控件
        initViews();
        LogUtils.d(TAG, "onCreate: 单元测试页面初始化完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 单元测试页面开始销毁");
        if (logView != null) {
            // 若LogView有停止方法，建议调用避免资源泄漏
            // logView.stop();
            LogUtils.d(TAG, "onDestroy: LogView资源已处理");
        }
        LogUtils.d(TAG, "onDestroy: 单元测试页面销毁完成");
    }

    // ====================== 控件初始化函数区 ======================
    private void initViews() {
        LogUtils.d(TAG, "initViews: 初始化UI控件");
        // Java7 适配：添加强制类型转换
        logView = (LogView) findViewById(R.id.logview);
        etPhone = (EditText) findViewById(R.id.phone_et);

        // 启动日志视图
        logView.start();
        LogUtils.d(TAG, "initViews: LogView已启动");
    }

    // ====================== 点击事件测试函数区 ======================
    /**
     * 测试单个号码匹配规则
     */
    public void onTestPhone(View view) {
        LogUtils.d(TAG, "onTestPhone: 开始测试单个号码规则匹配");
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            LogUtils.w(TAG, "onTestPhone: 测试号码为空，跳过匹配");
            return;
        }

        Rules rules = Rules.getInstance(this);
        boolean isAllowed = rules.isAllowed(phone);
        LogUtils.d(TAG, String.format("onTestPhone: 测试号码: %s | 匹配结果: %s", phone, isAllowed));
    }

    /**
     * 批量测试预设号码规则匹配
     */
    public void onTestMain(View view) {
        LogUtils.d(TAG, "onTestMain: 开始批量测试号码规则匹配");
        // 测试IntUtils工具类方法
        LogUtils.d(TAG, "onTestMain: 执行 IntUtils.unittest_getIntInRange() 测试");
        IntUtils.unittest_getIntInRange();

        // 初始化规则实例
        Rules rules = Rules.getInstance(this);
        // 无规则时添加测试规则集
        initTestRulesIfEmpty(rules);

        // 预设测试号码列表
        String[] testPhones = {
			"16769764848", "16856582777", "17519703124",
			"0205658955", "0108965253", "+8616769764848",
			"4005816769764848", "95566"
        };

        // 遍历测试号码并输出结果
        for (String phone : testPhones) {
            boolean isAllowed = rules.isAllowed(phone);
            LogUtils.d(TAG, String.format("onTestMain: 测试号码: %s | 匹配结果: %s", phone, isAllowed));
        }
        LogUtils.d(TAG, "onTestMain: 批量号码规则测试完成");
		
		new Thread(new Runnable(){
				@Override
				public void run() {
					LimitedTimeSpecialChannelService.unitTest(UnitTestActivity.this);
				}
			}).start(); 
    }

    // ====================== 私有工具函数区 ======================
    /**
     * 规则集为空时初始化测试规则
     */
    private void initTestRulesIfEmpty(Rules rules) {
        if (rules.getPhoneBlacRuleBeanList().size() == 0) {
            LogUtils.d(TAG, "initTestRulesIfEmpty: 当前无规则，添加测试规则集");
            // 规则1：中国手机号允许
            rules.add("^1[3-9]\\d{9}$", true, true);
            // 规则2：0660区号号码允许
            rules.add("^0660\\d+$", true, true);
            // 规则3：020区号号码允许
            rules.add("^020\\d+$", true, true);
            // 规则4：默认拒接所有号码
            rules.add(".*", false, true);

            // 保存规则到本地
            rules.saveRules();
            LogUtils.d(TAG, "initTestRulesIfEmpty: 测试规则集已保存");
        } else {
            LogUtils.d(TAG, "initTestRulesIfEmpty: 当前已有规则，跳过初始化");
        }
    }
}

