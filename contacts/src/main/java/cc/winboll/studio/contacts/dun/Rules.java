package cc.winboll.studio.contacts.dun;

import android.content.Context;
import cc.winboll.studio.contacts.activities.SettingsActivity;
import cc.winboll.studio.contacts.bobulltoon.TomCat;
import cc.winboll.studio.contacts.model.PhoneConnectRuleBean;
import cc.winboll.studio.contacts.model.SettingsBean;
import cc.winboll.studio.contacts.services.LimitedTimeSpecialChannelService;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.utils.ContactUtils;
import cc.winboll.studio.contacts.utils.IntUtils;
import cc.winboll.studio.contacts.utils.RegexPPiUtils;
import cc.winboll.studio.contacts.views.DunTemperatureView;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/21 06:15:10
 * @Describe 云盾防御规则（双重校验锁单例模式）
 */
public class Rules {

    public static final String TAG = "Rules";

    // 单例核心：volatile 保证多线程可见性，禁止指令重排
    private static volatile Rules sInstance;
    // 上下文需使用 ApplicationContext 避免内存泄漏
    private static Context sApplicationContext;

    ArrayList<PhoneConnectRuleBean> _PhoneConnectRuleModelList;
    Context mContext;
    SettingsBean mSettingsModel;
    Timer mDunResumeTimer;

    /**
     * 私有化构造方法，禁止外部 new 实例
     */
    private Rules(Context context) {
        mContext = context.getApplicationContext();
        _PhoneConnectRuleModelList = new ArrayList<PhoneConnectRuleBean>();
        reload();
    }

    /**
     * 获取单例实例（双重校验锁，线程安全）
     * @param context 上下文，建议传入 ApplicationContext
     * @return Rules 唯一实例
     */
    public static Rules getInstance(Context context) {
        // 第一次校验：无锁，提高性能
        if (sInstance == null) {
            // 加锁：保证多线程下仅初始化一次
            synchronized (Rules.class) {
                // 第二次校验：防止多线程并发时重复创建
                if (sInstance == null) {
                    sInstance = new Rules(context);
                }
            }
        }
        return sInstance;
    }

    public void reload() {
        LogUtils.d(TAG, "reload()");
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
        int ss = IntUtils.getIntInRange(mSettingsModel.getDunResumeSecondCount() * 1000, SettingsBean.MIN_INTRANGE, SettingsBean.MAX_INTRANGE);
        mDunResumeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (mSettingsModel.getDunCurrentCount() != mSettingsModel.getDunTotalCount()) {
						LogUtils.d(TAG, String.format("当前防御值为%d，最大防御值为%d", mSettingsModel.getDunCurrentCount(), mSettingsModel.getDunTotalCount()));
						int newDunCount = mSettingsModel.getDunCurrentCount() + mSettingsModel.getDunResumeCount();
						// 设置盾值在[0，DunTotalCount]之内其他值一律重置为 DunTotalCount。
						newDunCount = (newDunCount > mSettingsModel.getDunTotalCount()) ?mSettingsModel.getDunTotalCount(): newDunCount;
						mSettingsModel.setDunCurrentCount(newDunCount);
						LogUtils.d(TAG, String.format("设置防御值为%d", newDunCount));
						saveDun();
						// 一键更新所有 DunTemperatureView 实例的盾值
						DunTemperatureView.updateDunValue(mSettingsModel.getDunTotalCount(), mSettingsModel.getDunCurrentCount());

						SettingsActivity.notifyDunInfoUpdate();
					}
				}
			}, 1000, ss);
    }

    public void loadRules() {
        _PhoneConnectRuleModelList.clear();
        PhoneConnectRuleBean.loadBeanList(mContext, _PhoneConnectRuleModelList, PhoneConnectRuleBean.class);
    }

    public void saveRules() {
        LogUtils.d(TAG, String.format("saveRules()"));
        PhoneConnectRuleBean.saveBeanList(mContext, _PhoneConnectRuleModelList, PhoneConnectRuleBean.class);
    }

    public void resetDefaultBoBullToonURL() {
        mSettingsModel.setBoBullToon_URL(TomCat.getInstance(mContext).getDefaultBobulltoonUrl());
        saveDun();
    }

    public void setBoBullToonURL(String szUrl) {
        mSettingsModel.setBoBullToon_URL(szUrl);
        saveDun();
    }

    public String getBoBullToonURL() {
        return mSettingsModel.getBoBullToon_URL();
    }

    public void loadDun() {
        mSettingsModel = SettingsBean.loadBean(mContext, SettingsBean.class);
        if (mSettingsModel == null) {
            mSettingsModel = new SettingsBean();
            SettingsBean.saveBean(mContext, mSettingsModel);
        }
    }

    public void saveDun() {
        LogUtils.d(TAG, String.format("saveDun()"));
        SettingsBean.saveBean(mContext, mSettingsModel);
    }

	public boolean isAllowed(String phoneNumber) {
		return isAllowed(phoneNumber, false);
	}

    public boolean isAllowed(String phoneNumber, boolean isTest) {
        // 没有启用云盾，默认允许接通任何电话
        if (!mSettingsModel.isEnableDun()) {
            LogUtils.d(TAG, String.format("没有启用云盾，默认允许接通任何电话。isAllowed(...) return true"));
            return true;
        }

        // 云盾防御体系
        boolean isDefend = false; // 盾牌是否生效
        boolean isConnect = true; // 防御结果是否连接

        // 进行盾牌层数预计缩减计算
        int nDunCurrentCount = mSettingsModel.getDunCurrentCount() - 1;
        LogUtils.d(TAG, String.format("nDunCurrentCount : %d", nDunCurrentCount));

        // 如果盾值小于1，则解除防御
        if (!isDefend && nDunCurrentCount < 1) {
            // 盾层为1以下，防御解除
            LogUtils.d(TAG, "盾层为1以下，防御解除");
            isDefend = true;
            isConnect = true;
            LogUtils.d(TAG, String.format("isDefend == %s\nisConnect == %s", isDefend, isConnect));
        }

        // 正则运算预防针
        if (!isDefend && !RegexPPiUtils.isPPiOK(phoneNumber)) {
            LogUtils.d(TAG, "正则运算预防针生效。");
            isDefend = true;
            isConnect = false;
            LogUtils.d(TAG, String.format("isDefend == %s\nisConnect == %s", isDefend, isConnect));
        }
		
        // 限时特殊通道打开时返回连接
        if (!isDefend && LimitedTimeSpecialChannelService.isServiceRunning()) {
            LogUtils.d(TAG, String.format("PhoneNumber %s\n and Limited Time Special Channel Service Is Running.", phoneNumber));
            isDefend = true;
            isConnect = true;
            LogUtils.d(TAG, String.format("isDefend == %s\nisConnect == %s", isDefend, isConnect));
        }

        // 检验拨不通号码群
        if (!isDefend && MainService.isPhoneInBoBullToon(phoneNumber)) {
            LogUtils.d(TAG, String.format("PhoneNumber %s\n Is In BoBullToon", phoneNumber));
            isDefend = true;
            isConnect = false;
            LogUtils.d(TAG, String.format("isDefend == %s\nisConnect == %s", isDefend, isConnect));
        }

        // 查询通讯录是否有该联系人
        boolean isPhoneInContacts = ContactUtils.getInstance(mContext).isPhoneInContacts(mContext, phoneNumber);
        if (!isDefend) {
            if (isPhoneInContacts) {
                LogUtils.d(TAG, String.format("Phone %s is in contacts.", phoneNumber));
                isDefend = true;
                isConnect = true;
                LogUtils.d(TAG, String.format("isDefend == %s\nisConnect == %s", isDefend, isConnect));
            } else {
                LogUtils.d(TAG, String.format("Phone %s is not in contacts.", phoneNumber));
            }
        }

        // 正则匹配规则名单校验
        if (!isDefend) {
            for (int i = 0; i < _PhoneConnectRuleModelList.size(); i++) {
                if (_PhoneConnectRuleModelList.get(i).isEnable()) {
                    String regex = _PhoneConnectRuleModelList.get(i).getRuleText();
                    if (Pattern.matches(regex, phoneNumber)) {
                        LogUtils.d(TAG, String.format("Phone Number [%s] is matched by rule : %s", phoneNumber, _PhoneConnectRuleModelList.get(i)));
                        isDefend = true;
                        isConnect = _PhoneConnectRuleModelList.get(i).isAllowConnection();
                        LogUtils.d(TAG, String.format("isDefend == %s\nisConnect == %s", isDefend, isConnect));
                        break;
                    }
                }
            }
        }

		// 如果不是规则测试时，就执行云盾防御机能。
		if (isTest == false) {
			if (isConnect) {
				// 如果防御结果为连接，则恢复防御盾牌最大值层数
				mSettingsModel.setDunCurrentCount(mSettingsModel.getDunTotalCount());
				LogUtils.d(TAG, String.format("防御结果为连接，恢复防御盾牌最大值层数 %d", mSettingsModel.getDunTotalCount()));
				saveDun();
				SettingsActivity.notifyDunInfoUpdate();
			} else if (isDefend) {
				// 如果触发了以上某个防御模块，减少防御盾牌层数
				int newDunCount = nDunCurrentCount;
				LogUtils.d(TAG, String.format("新的防御层数预计为 %d", newDunCount));

				// 保证盾值在[1，DunTotalCount]之内其他值一律重置为 DunTotalCount。
				if (newDunCount > 0 && newDunCount < mSettingsModel.getDunTotalCount()) {
					mSettingsModel.setDunCurrentCount(newDunCount);
					LogUtils.d(TAG, String.format("设置防御层数为 %d", newDunCount));
				} else {
					mSettingsModel.setDunCurrentCount(mSettingsModel.getDunTotalCount());
					LogUtils.d(TAG, String.format("盾值不在[0，%d]区间，恢复防御最大值%d", mSettingsModel.getDunTotalCount(), mSettingsModel.getDunTotalCount()));
				}

				saveDun();
				SettingsActivity.notifyDunInfoUpdate();
			}

			// 一键更新所有 DunTemperatureView 实例的盾值
			DunTemperatureView.updateDunValue(mSettingsModel.getDunTotalCount(), mSettingsModel.getDunCurrentCount());
		}

		// 返回校验结果
		LogUtils.d(TAG, String.format("返回校验结果 isConnect == %s", isConnect));
		return isConnect;
	}

    public void add(String szPhoneConnectRule, boolean isAllowConnection, boolean isEnable) {
        _PhoneConnectRuleModelList.add(new PhoneConnectRuleBean(szPhoneConnectRule, isAllowConnection, isEnable));
    }

    public ArrayList<PhoneConnectRuleBean> getPhoneBlacRuleBeanList() {
        return _PhoneConnectRuleModelList;
    }

    public SettingsBean getSettingsModel() {
        return mSettingsModel;
    }

    /**
     * 可选：释放单例资源（如退出应用时调用）
     */
    public static void releaseInstance() {
        if (sInstance != null) {
            sInstance.mDunResumeTimer.cancel();
            sInstance._PhoneConnectRuleModelList.clear();
            sInstance.mSettingsModel = null;
            sInstance.mContext = null;
            sInstance = null;
        }
    }
}

