package cc.winboll.studio.contacts.dun;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 06:15:10
 * @Describe 云盾防御规则
 */
import cc.winboll.studio.contacts.beans.PhoneBlackRuleBean;
import java.util.ArrayList;
import java.util.regex.Pattern;
import android.content.Context;

public class Rules {

    public static final String TAG = "Rules";

    ArrayList<PhoneBlackRuleBean> _PhoneBlacRuleBeanList;
    static volatile Rules _Rules;
    Context mContext;

    Rules(Context context) {
        mContext = context;
        _PhoneBlacRuleBeanList = new ArrayList<PhoneBlackRuleBean>();
        PhoneBlackRuleBean.loadBeanList(mContext, _PhoneBlacRuleBeanList, PhoneBlackRuleBean.class);

    }
    public static synchronized Rules getInstance(Context context) {
        if (_Rules == null) {
            _Rules = new Rules(context);
        }
        return _Rules;
    }

    public boolean isAllowed(String phoneNumber) {
        // 黑名单拒接
        for (int i = 0; i < _PhoneBlacRuleBeanList.size(); i++) {
            if (_PhoneBlacRuleBeanList.get(i).isEnable()) {
                String regex = _PhoneBlacRuleBeanList.get(i).getRuleText();
                if (Pattern.matches(regex, phoneNumber)) {
                    return false;
                }
            }
        }

        // 手机号码允许
        // 中国手机号码正则表达式，以1开头，第二位可以是3、4、5、6、7、8、9，后面跟9位数字
        String regex = "^1[3-9]\\d{9}$";
        if (Pattern.matches(regex, phoneNumber)) {
            return true;
        }
        
        // 指定区号号码允许
        regex = "^0660\\d+$";
        if (Pattern.matches(regex, phoneNumber)) {
            return true;
        }
        
        // 指定区号号码允许
        regex = "^020\\d+$";
        if (Pattern.matches(regex, phoneNumber)) {
            return true;
        }

        // 其他拒接
        return false;
    }

    public void add(String phoneRuleBlack, boolean isEnable) {
        _PhoneBlacRuleBeanList.add(new PhoneBlackRuleBean(phoneRuleBlack, isEnable));
        PhoneBlackRuleBean.saveBeanList(mContext, _PhoneBlacRuleBeanList, PhoneBlackRuleBean.class);
    }

    public ArrayList<PhoneBlackRuleBean> getPhoneBlacRuleBeanList() {
        return _PhoneBlacRuleBeanList;
    }
}
