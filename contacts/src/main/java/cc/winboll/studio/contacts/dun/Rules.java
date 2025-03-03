package cc.winboll.studio.contacts.dun;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 06:15:10
 * @Describe 云盾防御规则
 */
import android.content.Context;
import android.media.AudioManager;
import cc.winboll.studio.contacts.activities.SettingsActivity;
import cc.winboll.studio.contacts.beans.PhoneConnectRuleModel;
import cc.winboll.studio.contacts.beans.RingTongBean;
import cc.winboll.studio.contacts.beans.SettingsModel;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.utils.RegexPPiUtils;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class Rules {

    public static final String TAG = "Rules";

    ArrayList<PhoneConnectRuleModel> _PhoneConnectRuleModelList;
    static volatile Rules _Rules;
    Context mContext;
    SettingsModel mSettingsModel;
    Timer mDunResumeTimer;

    Rules(Context context) {
        mContext = context;
        _PhoneConnectRuleModelList = new ArrayList<PhoneConnectRuleModel>();
        reload();
    }

    public static synchronized Rules getInstance(Context context) {
        if (_Rules == null) {
            _Rules = new Rules(context);
        }
        return _Rules;
    }

    public void reload() {
        loadRules();
        loadDun();
        setDunResumTimer();
    }

    public void setDunResumTimer() {
        if (mDunResumeTimer != null) {
            mDunResumeTimer.cancel();
        }

        // 盾牌恢复定时器
        mDunResumeTimer = new Timer();
        mDunResumeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mSettingsModel.getDunCurrentCount() != mSettingsModel.getDunTotalCount()) {
                        int newDunCount = mSettingsModel.getDunCurrentCount() + mSettingsModel.getDunResumeCount();
                        // 保证盾值在[0，DunTotalCount]之内其他值一律重置为 DunTotalCount。
                        newDunCount = (newDunCount > mSettingsModel.getDunTotalCount()) ?mSettingsModel.getDunTotalCount(): newDunCount;
                        mSettingsModel.setDunCurrentCount(newDunCount);
                        saveDun();
                        SettingsActivity.notifyDunInfoUpdate();
                    }
                }
            }, 1000, mSettingsModel.getDunResumeSecondCount() * 1000);
    }

    public void loadRules() {
        _PhoneConnectRuleModelList.clear();
        PhoneConnectRuleModel.loadBeanList(mContext, _PhoneConnectRuleModelList, PhoneConnectRuleModel.class);
    }

    public void saveRules() {
        PhoneConnectRuleModel.saveBeanList(mContext, _PhoneConnectRuleModelList, PhoneConnectRuleModel.class);
    }

    public void loadDun() {
        mSettingsModel = SettingsModel.loadBean(mContext, SettingsModel.class);
        if (mSettingsModel == null) {
            mSettingsModel = new SettingsModel();
            SettingsModel.saveBean(mContext, mSettingsModel);
        }
    }

    public void saveDun() {
        LogUtils.d(TAG, String.format("saveDun() isEnableDun : %s", mSettingsModel.isEnableDun()));
        SettingsModel.saveBean(mContext, mSettingsModel);
    }

    public boolean isAllowed(String phoneNumber) {
        // 没有启用云盾，默认允许接通任何电话
        if (!mSettingsModel.isEnableDun()) {
            LogUtils.d(TAG, "没有启用云盾，默认允许接通任何电话");
            return true;
        }

        //
        // 以下是云盾防御体系
        boolean isDefend = false; // 盾牌是否生效
        boolean isConnect = true; // 防御结果是否连接

        // 如果盾值小于1，则解除防御
        if (!isDefend && mSettingsModel.getDunCurrentCount() < 1) {
            // 盾层为1以下，防御解除
            LogUtils.d(TAG, "盾层为1以下，防御解除");
            isDefend = true;
            isConnect = true;
        }

        // 正则运算预防针
        if (!isDefend && !RegexPPiUtils.isPPiOK(phoneNumber)) {
            LogUtils.d(TAG, "RegexPPiUtils.isPPiOK return false.");
            isDefend = true;
            isConnect = false;
        }

        // 检验拨不通号码群
        if (!isDefend && MainService.isPhoneInBoBullToon(phoneNumber)) {
            LogUtils.d(TAG, String.format("PhoneNumber %s\n Is In BoBullToon", phoneNumber));
            isDefend = true;
            isConnect = false;
        }

        // 正则匹配规则名单校验
        if (!isDefend) {
            for (int i = 0; i < _PhoneConnectRuleModelList.size(); i++) {
                if (_PhoneConnectRuleModelList.get(i).isEnable()) {
                    String regex = _PhoneConnectRuleModelList.get(i).getRuleText();
                    if (Pattern.matches(regex, phoneNumber)) {
                        LogUtils.d(TAG, String.format("phoneNumber :%s \nisAllowConnection %s By Rule : %s", phoneNumber, _PhoneConnectRuleModelList.get(i).isAllowConnection(), _PhoneConnectRuleModelList.get(i)));
                        isDefend = true;
                        isConnect = _PhoneConnectRuleModelList.get(i).isAllowConnection();
                        break;
                    }
                }
            }
        }

        if (isConnect) {
            // 如果防御结果为连接，则恢复防御盾牌最大值层数
            mSettingsModel.setDunCurrentCount(mSettingsModel.getDunTotalCount());
            saveDun();
            SettingsActivity.notifyDunInfoUpdate();
        } else if (isDefend) {
            // 如果触发了以上某个防御模块，
            // 就减少防御盾牌层数。
            // 每校验一次规则，云盾防御层数减1
            // 当云盾防御层数为0时，再次进行以下程序段则恢复满值防御。
            int newDunCount = mSettingsModel.getDunCurrentCount() - 1;

            // 保证盾值在[0，DunTotalCount]之内其他值一律重置为 DunTotalCount。
            if (newDunCount < 0 || newDunCount > mSettingsModel.getDunTotalCount()) {
                mSettingsModel.setDunCurrentCount(mSettingsModel.getDunTotalCount());
            } else {
                mSettingsModel.setDunCurrentCount(newDunCount);
            }

            saveDun();
            SettingsActivity.notifyDunInfoUpdate();
        }

        // 返回校验结果
        return isConnect;
    }

    public void add(String szPhoneConnectRule, boolean isAllowConnection, boolean isEnable) {
        _PhoneConnectRuleModelList.add(new PhoneConnectRuleModel(szPhoneConnectRule, isAllowConnection, isEnable));
    }

    public ArrayList<PhoneConnectRuleModel> getPhoneBlacRuleBeanList() {
        return _PhoneConnectRuleModelList;
    }

    public SettingsModel getSettingsModel() {
        return mSettingsModel;
    }
}
