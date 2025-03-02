package cc.winboll.studio.contacts.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 16:07:04
 */
public class UnitTestActivity extends Activity {

    public static final String TAG = "UnitTestActivity";

    LogView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unittest);
        logView = findViewById(R.id.logview);
        logView.start();
    }

    public void onTestMain(View view) {
        Rules rules = Rules.getInstance(this);

        // 如果没有规则就添加测试规则
        if (rules.getPhoneBlacRuleBeanList().size() == 0) {
            // 手机号码允许
            // 中国手机号码正则表达式，以1开头，第二位可以是3、4、5、6、7、8、9，后面跟9位数字
            String regex = "^1[3-9]\\d{9}$";
            rules.add(regex, true, true);

            // 指定区号号码允许
            regex = "^0660\\d+$";
            rules.add(regex, true, true);

            // 指定区号号码允许
            regex = "^020\\d+$";
            rules.add(regex, true, true);

            // 添加默认拒接规则
            regex = ".*";
            rules.add(regex, false, true);

            // 保存规则到文件
            rules.saveRules();
        }

        // 开始测试数据
        String phone = "16769764848";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "16856582777";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "17519703124";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "0205658955";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "0108965253";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "+8616769764848";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "4005816769764848";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

        phone = "95566";
        LogUtils.d(TAG, String.format("Test phone : %s\n%s", phone, rules.isAllowed(phone)));

    }
}
