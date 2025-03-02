package cc.winboll.studio.contacts.dun;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 06:15:10
 * @Describe 云盾防御规则
 */
import android.content.Context;
import cc.winboll.studio.contacts.beans.PhoneConnectRuleModel;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.utils.RegexPPiUtils;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Rules {

    public static final String TAG = "Rules";

    ArrayList<PhoneConnectRuleModel> _PhoneConnectRuleModelList;
    static volatile Rules _Rules;
    Context mContext;

    Rules(Context context) {
        mContext = context;
        _PhoneConnectRuleModelList = new ArrayList<PhoneConnectRuleModel>();
        loadRules();
    }

    public static synchronized Rules getInstance(Context context) {
        if (_Rules == null) {
            _Rules = new Rules(context);
        }
        return _Rules;
    }

    public void loadRules() {
        _PhoneConnectRuleModelList.clear();
        PhoneConnectRuleModel.loadBeanList(mContext, _PhoneConnectRuleModelList, PhoneConnectRuleModel.class);
    }

    public void saveRules() {
        PhoneConnectRuleModel.saveBeanList(mContext, _PhoneConnectRuleModelList, PhoneConnectRuleModel.class);
    }

    public boolean isAllowed(String phoneNumber) {
        // 正则运算预防针
        if (!RegexPPiUtils.isPPiOK(phoneNumber)) {
            LogUtils.d(TAG, "RegexPPiUtils.isPPiOK return false.");
            return false;
        }
        // 检验拨不通号码群
        if (MainService.isPhoneInBoBullToon(phoneNumber)) {
            LogUtils.d(TAG, String.format("PhoneNumber %s\n Is In BoBullToon", phoneNumber));
            return false;
        }

        // 正则匹配规则名单校验
        for (int i = 0; i < _PhoneConnectRuleModelList.size(); i++) {
            if (_PhoneConnectRuleModelList.get(i).isEnable()) {
                String regex = _PhoneConnectRuleModelList.get(i).getRuleText();
                if (Pattern.matches(regex, phoneNumber)) {
                    LogUtils.d(TAG, String.format("phoneNumber :%s \nisAllowConnection %s By Rule : %s", phoneNumber, _PhoneConnectRuleModelList.get(i).isAllowConnection(), _PhoneConnectRuleModelList.get(i)));
                    return _PhoneConnectRuleModelList.get(i).isAllowConnection();
                }
            }
        }

        // 其他默认接收
        return true;
    }

    public void add(String szPhoneConnectRule, boolean isAllowConnection, boolean isEnable) {
        _PhoneConnectRuleModelList.add(new PhoneConnectRuleModel(szPhoneConnectRule, isAllowConnection, isEnable));
    }

    public ArrayList<PhoneConnectRuleModel> getPhoneBlacRuleBeanList() {
        return _PhoneConnectRuleModelList;
    }
}
