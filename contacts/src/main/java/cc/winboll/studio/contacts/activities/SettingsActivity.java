package cc.winboll.studio.contacts.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.adapters.PhoneConnectRuleAdapter;
import cc.winboll.studio.contacts.bobulltoon.TomCat;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.model.MainServiceBean;
import cc.winboll.studio.contacts.model.PhoneConnectRuleBean;
import cc.winboll.studio.contacts.model.RingTongBean;
import cc.winboll.studio.contacts.model.SettingsBean;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.views.DuInfoTextView;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/21 05:37:42
 * @Describe Contacts 设置页面（完全适配 API 30 + Java 7 语法）
 * 核心优化：1. 移除高版本API依赖 2. Java7规范写法 3. 强化内存泄漏防护 4. 版本判断硬编码 5. LogUtils统一日志管理
 */
public class SettingsActivity extends WinBollActivity implements IWinBoLLActivity {

    // ====================== 常量定义区（置顶，统一管理） ======================
    public static final String TAG = "SettingsActivity";
    // API版本硬编码（替代Build.VERSION_CODES，适配Java7）
    private static final int ANDROID_6_API = 23;

    // ====================== 静态成员属性区 ======================
    private static DuInfoTextView sDuInfoTextView; // 规范命名：静态属性加s前缀

    // ====================== 数据业务属性区 ======================
    private int mStreamMaxVolume; // 铃音最大音量
    private int mStreamVolume;    // 当前铃音音量
    private List<PhoneConnectRuleBean> mRuleList; // 通话规则列表
    private PhoneConnectRuleAdapter mRuleAdapter; // 规则列表适配器

    // ====================== UI控件属性区（统一归类，规范命名） ======================
    private Toolbar mToolbar;                  // 顶部工具栏
    private Switch mSwMainService;             // 主服务开关
    private SeekBar mSbVolume;                 // 音量调节条
    private TextView mTvVolume;                // 音量显示文本
    private Switch mSwEnableDun;               // 云盾功能开关
    private EditText mEtDunTotalCount;         // 云盾总次数输入框
    private EditText mEtDunResumeSecondCount;  // 云盾恢复秒数输入框
    private EditText mEtDunResumeCount;        // 云盾恢复次数输入框
    private RecyclerView mRvRuleList;          // 规则列表RecyclerView
    private EditText mEtBoBullToonUrl;         // BoBullToon地址输入框
    private EditText mEtSearchPhone;           // 号码查询输入框

    // ====================== 接口实现区（IWinBoLLActivity规范实现） ======================
    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ====================== 生命周期函数区（按执行顺序排列） ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: 设置页面启动");
        setContentView(R.layout.activity_settings);

        // 初始化核心流程（按优先级执行）
        initToolbar();          // 工具栏初始化（优先）
        initMainServiceSwitch();// 主服务开关初始化
        initVolumeControl();    // 音量控制初始化
        initRuleRecyclerView(); // 规则列表初始化
        initDunSettings();      // 云盾设置初始化
        initBoBullToonViews();  // BoBullToon功能初始化

        LogUtils.d(TAG, "onCreate: 设置页面初始化完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 设置页面销毁");
        // 内存泄漏防护：清空所有引用（静态+成员+UI）
        sDuInfoTextView = null;
        mRuleList = null;
        mRuleAdapter = null;
        mToolbar = null;
        mSwMainService = null;
        mSbVolume = null;
        mTvVolume = null;
        mSwEnableDun = null;
        mEtDunTotalCount = null;
        mEtDunResumeSecondCount = null;
        mEtDunResumeCount = null;
        mRvRuleList = null;
        mEtBoBullToonUrl = null;
        mEtSearchPhone = null;
        LogUtils.d(TAG, "onDestroy: 设置页面资源清理完成");
    }

    // ====================== 初始化函数区（按功能模块归类） ======================
    /**
     * 初始化顶部工具栏（后退按钮+标题）
     */
    private void initToolbar() {
        LogUtils.d(TAG, "initToolbar: 初始化工具栏");
        mToolbar = (Toolbar) findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(mToolbar);

        // 显示后退按钮（空指针防护）
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle(TAG);
        }

        // 后退按钮点击事件（Java7匿名内部类）
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "initToolbar: 点击后退按钮，关闭页面");
					finish();
				}
			});
    }

    /**
     * 初始化主服务开关（联动MainService启停）
     */
    private void initMainServiceSwitch() {
        LogUtils.d(TAG, "initMainServiceSwitch: 初始化主服务开关");
        mSwMainService = (Switch) findViewById(R.id.sw_mainservice);
        MainServiceBean serviceBean = MainServiceBean.loadBean(this, MainServiceBean.class);

        // 加载开关状态（空指针防护）
        boolean isServiceEnable = serviceBean != null && serviceBean.isEnable();
        mSwMainService.setChecked(isServiceEnable);
        LogUtils.d(TAG, "initMainServiceSwitch: 主服务当前状态：" + (isServiceEnable ? "启用" : "禁用"));

        // 开关点击事件
        mSwMainService.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isChecked = mSwMainService.isChecked();
					LogUtils.d(TAG, "initMainServiceSwitch: 主服务开关切换：" + (isChecked ? "启用" : "禁用"));
					if (isChecked) {
						MainService.startMainServiceAndSaveStatus(SettingsActivity.this);
					} else {
						MainService.stopMainServiceAndSaveStatus(SettingsActivity.this);
					}
				}
			});
    }

    /**
     * 初始化音量控制（SeekBar+音量显示+配置保存）
     */
    private void initVolumeControl() {
        LogUtils.d(TAG, "initVolumeControl: 初始化音量控制");
        mSbVolume = (SeekBar) findViewById(R.id.bellvolume);
        mTvVolume = (TextView) findViewById(R.id.tv_volume);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // 空指针防护：AudioManager获取失败直接返回
        if (audioManager == null) {
            LogUtils.e(TAG, "initVolumeControl: AudioManager获取失败，音量控制初始化失败");
            return;
        }

        // 初始化音量参数
        mStreamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        mStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        mSbVolume.setMax(mStreamMaxVolume);
        mSbVolume.setProgress(mStreamVolume);
        updateVolumeDisplay(); // 更新音量文本显示

        // 音量调节监听
        mSbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser) {
						LogUtils.d(TAG, "initVolumeControl: 音量调节至：" + progress + "/" + mStreamMaxVolume);
						// 实时更新系统音量+保存配置
						audioManager.setStreamVolume(AudioManager.STREAM_RING, progress, 0);
						RingTongBean ringBean = RingTongBean.loadBean(SettingsActivity.this, RingTongBean.class);
						if (ringBean == null) {
							ringBean = new RingTongBean();
						}
						ringBean.setStreamVolume(progress);
						RingTongBean.saveBean(SettingsActivity.this, ringBean);
						mStreamVolume = progress;
						updateVolumeDisplay();
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});
    }

    /**
     * 初始化通话规则列表（加载黑白名单规则）
     */
    private void initRuleRecyclerView() {
        LogUtils.d(TAG, "initRuleRecyclerView: 初始化规则列表");
        mRvRuleList = (RecyclerView) findViewById(R.id.recycler_view);
        mRvRuleList.setLayoutManager(new LinearLayoutManager(this));

        // 加载规则数据
        Rules rules = Rules.getInstance(this);
        if (rules == null) {
            LogUtils.e(TAG, "initRuleRecyclerView: Rules实例获取失败，列表初始化失败");
            return;
        }
        mRuleList = rules.getPhoneBlacRuleBeanList();
        mRuleAdapter = new PhoneConnectRuleAdapter(this, mRuleList);
        mRvRuleList.setAdapter(mRuleAdapter);
        LogUtils.d(TAG, "initRuleRecyclerView: 规则列表加载完成，共" + mRuleList.size() + "条规则");
    }

    /**
     * 初始化云盾设置（参数加载+开关联动）
     */
    private void initDunSettings() {
        LogUtils.d(TAG, "initDunSettings: 初始化云盾设置");
        sDuInfoTextView = (DuInfoTextView) findViewById(R.id.tv_DunInfo);
        mSwEnableDun = (Switch) findViewById(R.id.sw_IsEnableDun);
        mEtDunTotalCount = (EditText) findViewById(R.id.et_DunTotalCount);
        mEtDunResumeSecondCount = (EditText) findViewById(R.id.et_DunResumeSecondCount);
        mEtDunResumeCount = (EditText) findViewById(R.id.et_DunResumeCount);

        // 加载云盾配置
        Rules rules = Rules.getInstance(this);
        if (rules == null) {
            LogUtils.e(TAG, "initDunSettings: Rules实例获取失败，云盾初始化失败");
            return;
        }
        SettingsBean dunSettings = rules.getSettingsModel();
        if (dunSettings == null) {
            LogUtils.e(TAG, "initDunSettings: 云盾配置获取失败");
            return;
        }

        // 填充配置参数
        mEtDunTotalCount.setText(String.valueOf(dunSettings.getDunTotalCount()));
        mEtDunResumeSecondCount.setText(String.valueOf(dunSettings.getDunResumeSecondCount()));
        mEtDunResumeCount.setText(String.valueOf(dunSettings.getDunResumeCount()));
        mSwEnableDun.setChecked(dunSettings.isEnableDun());

        // 开关联动：启用云盾时禁用参数编辑
        boolean isDunEnable = dunSettings.isEnableDun();
        mEtDunTotalCount.setEnabled(!isDunEnable);
        mEtDunResumeSecondCount.setEnabled(!isDunEnable);
        mEtDunResumeCount.setEnabled(!isDunEnable);
        LogUtils.d(TAG, "initDunSettings: 云盾当前状态：" + (isDunEnable ? "启用" : "禁用"));
    }

    /**
     * 初始化BoBullToon功能（地址配置+号码查询）
     */
    private void initBoBullToonViews() {
        LogUtils.d(TAG, "initBoBullToonViews: 初始化BoBullToon功能");
        mEtBoBullToonUrl = (EditText) findViewById(R.id.bobulltoonurl_et);
        mEtSearchPhone = (EditText) findViewById(R.id.activitysettingsEditText1);

        // 加载保存的地址
        Rules rules = Rules.getInstance(this);
        if (rules != null) {
            mEtBoBullToonUrl.setText(rules.getBoBullToonURL());
            LogUtils.d(TAG, "initBoBullToonViews: 加载BoBullToon地址完成");
        } else {
            LogUtils.e(TAG, "initBoBullToonViews: Rules实例获取失败，地址加载失败");
        }
    }

    // ====================== 点击事件回调区（按功能模块归类） ======================
    /**
     * 云盾开关点击事件（联动参数编辑权限+配置保存）
     */
    public void onSW_IsEnableDun(View view) {
        boolean isChecked = mSwEnableDun.isChecked();
        LogUtils.d(TAG, "onSW_IsEnableDun: 云盾开关切换：" + (isChecked ? "启用" : "禁用"));

        // 联动参数编辑权限
        mEtDunTotalCount.setEnabled(!isChecked);
        mEtDunResumeSecondCount.setEnabled(!isChecked);
        mEtDunResumeCount.setEnabled(!isChecked);

        // 保存配置
        Rules rules = Rules.getInstance(this);
        if (rules == null) {
            LogUtils.e(TAG, "onSW_IsEnableDun: Rules实例获取失败，配置保存失败");
            mSwEnableDun.setChecked(false);
            return;
        }
        SettingsBean dunSettings = rules.getSettingsModel();
        if (dunSettings == null) {
            LogUtils.e(TAG, "onSW_IsEnableDun: 云盾配置获取失败，保存失败");
            mSwEnableDun.setChecked(false);
            return;
        }

        // 启用云盾时校验参数合法性
        if (isChecked) {
            try {
                String totalCountStr = mEtDunTotalCount.getText().toString().trim();
                String resumeSecStr = mEtDunResumeSecondCount.getText().toString().trim();
                String resumeCountStr = mEtDunResumeCount.getText().toString().trim();

                // 空参数校验
                if (totalCountStr.isEmpty() || resumeSecStr.isEmpty() || resumeCountStr.isEmpty()) {
                    throw new NumberFormatException("参数不能为空");
                }

                // 转换参数并保存
                int totalCount = Integer.parseInt(totalCountStr);
                int resumeSec = Integer.parseInt(resumeSecStr);
                int resumeCount = Integer.parseInt(resumeCountStr);
                dunSettings.setDunTotalCount(totalCount);
                dunSettings.setDunResumeSecondCount(resumeSec);
                dunSettings.setDunResumeCount(resumeCount);
                LogUtils.d(TAG, "onSW_IsEnableDun: 云盾参数保存完成，总次数：" + totalCount + "，恢复秒数：" + resumeSec);

                // 提示信息
                String toastMsg = totalCount == 1 ? "电话骚扰防御力几乎为0" : "连拨" + totalCount + "次后接通电话";
                ToastUtils.show(toastMsg);
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "onSW_IsEnableDun: 云盾参数格式错误", e);
                ToastUtils.show("参数格式错误，请输入整数");
                mSwEnableDun.setChecked(false);
                return;
            }
        }

        // 保存开关状态并刷新配置
        dunSettings.setIsEnableDun(isChecked);
        rules.saveDun();
        rules.reload();
        LogUtils.d(TAG, "onSW_IsEnableDun: 云盾配置保存完成");
    }

    /**
     * 添加新通话规则（黑白名单）
     */
    public void onAddNewConnectionRule(View view) {
        LogUtils.d(TAG, "onAddNewConnectionRule: 添加新通话规则");
        Rules rules = Rules.getInstance(this);
        if (rules == null) {
            LogUtils.e(TAG, "onAddNewConnectionRule: Rules实例获取失败，添加失败");
            return;
        }
        mRuleList.add(new PhoneConnectRuleBean());
        rules.saveRules();
        mRuleAdapter.notifyDataSetChanged();
        LogUtils.d(TAG, "onAddNewConnectionRule: 规则添加完成，当前共" + mRuleList.size() + "条规则");
    }

    /**
     * 跳转默认电话应用设置
     */
    public void onDefaultPhone(View view) {
        LogUtils.d(TAG, "onDefaultPhone: 跳转默认电话应用设置");
        startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
    }

    /**
     * 悬浮窗权限检查与请求
     */
    public void onCanDrawOverlays(View view) {
        LogUtils.d(TAG, "onCanDrawOverlays: 检查悬浮窗权限");
        // API6.0+校验权限
        if (Build.VERSION.SDK_INT >= ANDROID_6_API && !Settings.canDrawOverlays(this)) {
            LogUtils.d(TAG, "onCanDrawOverlays: 未开启悬浮窗权限，发起请求");
            showDrawOverlayRequestDialog();
        } else {
            ToastUtils.show("悬浮窗权限已开启");
        }
    }

    /**
     * 清理BoBullToon本地数据
     */
    public void onCleanBoBullToonData(View view) {
        LogUtils.d(TAG, "onCleanBoBullToonData: 清理BoBullToon数据");
        TomCat tomCat = TomCat.getInstance(this);
        if (tomCat != null) {
            tomCat.cleanBoBullToon();
            ToastUtils.show("BoBullToon数据已清理");
            LogUtils.d(TAG, "onCleanBoBullToonData: 数据清理完成");
        } else {
            LogUtils.e(TAG, "onCleanBoBullToonData: TomCat实例获取失败，清理失败");
        }
    }

    /**
     * 重置BoBullToon默认地址
     */
    public void onResetBoBullToonURL(View view) {
        LogUtils.d(TAG, "onResetBoBullToonURL: 重置BoBullToon地址");
        Rules rules = Rules.getInstance(this);
        if (rules == null) {
            LogUtils.e(TAG, "onResetBoBullToonURL: Rules实例获取失败，重置失败");
            return;
        }
        rules.resetDefaultBoBullToonURL();
        mEtBoBullToonUrl.setText(rules.getBoBullToonURL());
        ToastUtils.show("BoBullToon地址已重置为默认");
        LogUtils.d(TAG, "onResetBoBullToonURL: 地址重置完成");
    }

    /**
     * 下载BoBullToon数据（子线程执行，避免阻塞UI）
     */
    public void onDownloadBoBullToon(View view) {
        LogUtils.d(TAG, "onDownloadBoBullToon: 开始下载BoBullToon数据");
        Rules rules = Rules.getInstance(this);
        if (rules == null) {
            LogUtils.e(TAG, "onDownloadBoBullToon: Rules实例获取失败，下载失败");
            return;
        }

        // 校验并更新地址
        String inputUrl = mEtBoBullToonUrl.getText().toString().trim();
        String savedUrl = rules.getBoBullToonURL();
        if (!inputUrl.equals(savedUrl)) {
            rules.setBoBullToonURL(inputUrl);
            LogUtils.d(TAG, "onDownloadBoBullToon: BoBullToon地址更新为：" + inputUrl);
        }

        // 子线程下载（Java7匿名内部类）
        final TomCat tomCat = TomCat.getInstance(this);
        new Thread(new Runnable() {
				@Override
				public void run() {
					boolean downloadSuccess = tomCat != null && tomCat.downloadBoBullToon();
					if (downloadSuccess) {
						LogUtils.d(TAG, "onDownloadBoBullToon: 数据下载成功");
						// 主线程更新UI
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ToastUtils.show("BoBullToon下载成功");
								}
							});
						// 重启主服务+刷新配置
						MainService.restartMainService(SettingsActivity.this);
						Rules.getInstance(SettingsActivity.this).reload();
					} else {
						LogUtils.e(TAG, "onDownloadBoBullToon: 数据下载失败");
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ToastUtils.show("BoBullToon下载失败");
								}
							});
					}
				}
			}).start();
    }

    /**
     * 查询号码是否为BoBullToon号码
     */
    public void onSearchBoBullToonPhone(View view) {
        LogUtils.d(TAG, "onSearchBoBullToonPhone: 执行号码查询");
        String phone = mEtSearchPhone.getText().toString().trim();
        // 空号码校验
        if (phone.isEmpty()) {
            LogUtils.w(TAG, "onSearchBoBullToonPhone: 查询号码为空，取消查询");
            ToastUtils.show("请输入查询号码");
            return;
        }

        // 执行查询
        TomCat tomCat = TomCat.getInstance(this);
        if (tomCat == null || !tomCat.loadPhoneBoBullToon()) {
            LogUtils.w(TAG, "onSearchBoBullToonPhone: BoBullToon数据未加载，查询失败");
            ToastUtils.show("请先下载BoBullToon数据");
            return;
        }

        boolean isBoBullToon = tomCat.isPhoneBoBullToon(phone);
        String resultMsg = isBoBullToon ? "是BoBullToon号码" : "非BoBullToon号码";
        ToastUtils.show(resultMsg);
        LogUtils.d(TAG, "onSearchBoBullToonPhone: 号码" + phone + "查询结果：" + resultMsg);
    }

    /**
     * 跳转单元测试页面
     */
    public void onUnitTest(View view) {
        LogUtils.d(TAG, "onUnitTest: 跳转单元测试页面");
        startActivity(new Intent(this, UnitTestActivity.class));
    }

    /**
     * 跳转关于页面
     */
    public void onAbout(View view) {
        LogUtils.d(TAG, "onAbout: 跳转关于页面");
        WinBoLLActivityManager.getInstance().startWinBoLLActivity(this, AboutActivity.class);
    }

    /**
     * 跳转日志查看页面
     */
    public void onLogView(View view) {
        LogUtils.d(TAG, "onLogView: 跳转日志页面");
        WinBoLLActivityManager.getInstance().startLogActivity(this);
    }

    // ====================== 工具方法区（通用功能+权限相关） ======================
    /**
     * 更新音量显示文本（当前音量/最大音量）
     */
    private void updateVolumeDisplay() {
        mTvVolume.setText(mStreamVolume + "/" + mStreamMaxVolume);
    }

    /**
     * 显示悬浮窗权限请求对话框
     */
    private void showDrawOverlayRequestDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
			.setTitle("权限请求")
			.setMessage("为保证通话监听功能正常，需开启悬浮窗权限")
			.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					jumpToDrawOverlaySettings();
				}
			})
			.setNegativeButton("稍后", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create();

        // 解决对话框焦点问题
        if (dialog.getWindow() != null) {
            dialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        dialog.show();
    }

    /**
     * 跳转悬浮窗权限设置页面（反射适配低版本）
     */
    private void jumpToDrawOverlaySettings() {
        LogUtils.d(TAG, "jumpToDrawOverlaySettings: 跳转悬浮窗权限设置");
        try {
            // 反射获取设置页面Action（避免高版本API依赖）
            Class<?> settingsClazz = Settings.class;
            Field actionField = settingsClazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
            String action = (String) actionField.get(null);

            // 跳转当前应用权限设置页
            Intent intent = new Intent(action);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            LogUtils.e(TAG, "jumpToDrawOverlaySettings: 跳转权限设置失败", e);
            Toast.makeText(this, "请手动在设置中开启悬浮窗权限", Toast.LENGTH_LONG).show();
        }
    }

    // ====================== 静态通知方法区（云盾信息更新） ======================
    /**
     * 通知云盾信息刷新（外部调用）
     */
    public static void notifyDunInfoUpdate() {
        if (sDuInfoTextView != null) {
            LogUtils.d(TAG, "notifyDunInfoUpdate: 刷新云盾信息显示");
            sDuInfoTextView.notifyInfoUpdate();
        } else {
            LogUtils.w(TAG, "notifyDunInfoUpdate: 云盾信息控件未初始化，刷新失败");
        }
    }
}

