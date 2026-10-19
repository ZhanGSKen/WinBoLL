package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.ThoughtfulServiceBean;
import java.lang.reflect.Field;

/**
 * 应用设置窗口，提供应用配置项的统一入口
 * 适配 API30，基于 Java7 开发
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025年11月27日14时26分00秒
 * @LastEditTime 2026-02-28
 * @Describe 应用设置窗口（主开关联动子开关启用/禁用，主开关关闭则子开关禁用并取消勾选）
 */
public class SettingsActivity extends WinBoLLActivity implements IWinBoLLActivity {
    // ======================== 静态常量区 =========================
    public static final String TAG = "SettingsActivity";
    private static final int REQUEST_READ_MEDIA_IMAGES = 1001;

    // ======================== 成员属性区 =========================
    private Toolbar mToolbar;
    private CheckBox cbUsePowerTts;      // 用电TTS（主开关）
    private CheckBox cbChargeTts;        // 充电TTS（主开关）
    private CheckBox cbUseageTtsBattary; // 用电TTS带电量提醒（子开关）
    private CheckBox cbChargeTtsBattary; // 充电TTS带电量提醒（子开关）
    private CheckBox cbTtsWhenNotifyBattery; // 👉 新增：通知电量时同时播放TTS

    // ======================== 接口实现区 =========================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ======================== 生命周期区 =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LogUtils.d(TAG, "onCreate: 应用设置页面初始化开始");

        initToolbar();
        initTtsCheckBoxes();
        initTtsCheckBoxStatus();

        LogUtils.d(TAG, "onCreate: 应用设置页面初始化完成");
    }

    // ======================== UI初始化区 =========================
    /**
     * 初始化顶部工具栏
     */
    private void initToolbar() {
        LogUtils.d(TAG, "initToolbar: 工具栏初始化开始");
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(getTag());
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "initToolbar-navigationOnClick: 点击导航返回按钮");
					finish();
				}
			});
        LogUtils.d(TAG, "initToolbar: 工具栏初始化完成");
    }

    /**
     * 绑定TTS相关复选框控件
     */
    private void initTtsCheckBoxes() {
        LogUtils.d(TAG, "initTtsCheckBoxes: TTS复选框绑定开始");
        cbUsePowerTts = findViewById(R.id.activitysettingsCheckBox1);
        cbChargeTts = findViewById(R.id.activitysettingsCheckBox2);
        cbUseageTtsBattary = findViewById(R.id.activitysettingsCheckBox3);
        cbChargeTtsBattary = findViewById(R.id.activitysettingsCheckBox4);
        cbTtsWhenNotifyBattery = findViewById(R.id.activitysettingsCheckBox5); // 👉 新增绑定
        LogUtils.d(TAG, "initTtsCheckBoxes: TTS复选框绑定完成");
    }

    /**
     * 初始化TTS复选框初始状态
     */
    private void initTtsCheckBoxStatus() {
        LogUtils.d(TAG, "initTtsCheckBoxStatus: TTS复选框状态初始化开始");
        ThoughtfulServiceBean bean = ThoughtfulServiceBean.loadBean(this, ThoughtfulServiceBean.class);
        if (bean == null) {
            LogUtils.d(TAG, "initTtsCheckBoxStatus: 未读取到配置Bean，创建新实例");
            bean = new ThoughtfulServiceBean();
        }

        boolean useMainOpen = bean.isEnableUsePowerTts();
        boolean chargeMainOpen = bean.isEnableChargeTts();
        cbUsePowerTts.setChecked(useMainOpen);
        cbChargeTts.setChecked(chargeMainOpen);
        cbUseageTtsBattary.setChecked(bean.isEnableUseageTtsWithBattary());
        cbChargeTtsBattary.setChecked(bean.isEnableChargeTtsWithBattary());
        cbTtsWhenNotifyBattery.setChecked(bean.isEnableTtsWhenNotifyBattery()); // 👉 新增赋值
        cbUseageTtsBattary.setEnabled(useMainOpen);
        cbChargeTtsBattary.setEnabled(chargeMainOpen);

        LogUtils.d(TAG, "initTtsCheckBoxStatus: 主开关状态-用电TTS：" + useMainOpen + " 充电TTS：" + chargeMainOpen);
        LogUtils.d(TAG, "initTtsCheckBoxStatus: TTS复选框状态初始化完成");
    }

    // ======================== 事件响应区 =========================
    /**
     * 悬浮窗权限检查入口
     */
    public void onCheckTTSDrawOverlaysPermission(View view) {
        LogUtils.d(TAG, "onCheckTTSDrawOverlaysPermission: 触发悬浮窗权限检查");
        canDrawOverlays();
    }

    /**
     * 用电TTS主开关点击事件
     */
    public void onEnableUsePowerTts(View view) {
        boolean isChecked = cbUsePowerTts.isChecked();
        LogUtils.d(TAG, "onEnableUsePowerTts: 用电TTS主开关点击，切换后状态=" + isChecked);
        // 主开关联动子开关
        cbUseageTtsBattary.setEnabled(isChecked);
        // 保存配置
        ThoughtfulServiceBean bean = getThoughtfulServiceBean();
        bean.setIsEnableUsePowerTts(isChecked);
        ThoughtfulServiceBean.saveBean(this, bean);
        LogUtils.d(TAG, "onEnableUsePowerTts: 用电TTS状态保存完成");
    }

    /**
     * 充电TTS主开关点击事件
     */
    public void onEnableChargeTts(View view) {
        boolean isChecked = cbChargeTts.isChecked();
        LogUtils.d(TAG, "onEnableChargeTts: 充电TTS主开关点击，切换后状态=" + isChecked);
        // 主开关联动子开关
        cbChargeTtsBattary.setEnabled(isChecked);
        // 保存配置
        ThoughtfulServiceBean bean = getThoughtfulServiceBean();
        bean.setIsEnableChargeTts(isChecked);
        ThoughtfulServiceBean.saveBean(this, bean);
        LogUtils.d(TAG, "onEnableChargeTts: 充电TTS状态保存完成");
    }

    /**
     * 用电TTS带电量提醒子开关点击事件
     */
    public void onEnableUseageTtsWithBattary(View view) {
        boolean isChecked = cbUseageTtsBattary.isChecked();
        LogUtils.d(TAG, "onEnableUseageTtsWithBattary: 用电TTS电量提醒开关点击，切换后状态=" + isChecked);
        ThoughtfulServiceBean bean = getThoughtfulServiceBean();
        bean.setIsEnableUseageTtsWithBattary(isChecked);
        ThoughtfulServiceBean.saveBean(this, bean);
        LogUtils.d(TAG, "onEnableUseageTtsWithBattary: 用电TTS电量提醒状态保存完成");
    }

    /**
     * 充电TTS带电量提醒子开关点击事件
     */
    public void onEnableChargeTtsWithBattary(View view) {
        boolean isChecked = cbChargeTtsBattary.isChecked();
        LogUtils.d(TAG, "onEnableChargeTtsWithBattary: 充电TTS电量提醒开关点击，切换后状态=" + isChecked);
        ThoughtfulServiceBean bean = getThoughtfulServiceBean();
        bean.setIsEnableChargeTtsWithBattary(isChecked);
        ThoughtfulServiceBean.saveBean(this, bean);
        LogUtils.d(TAG, "onEnableChargeTtsWithBattary: 充电TTS电量提醒状态保存完成");
    }

    /**
     * 👉 新增：允许通知电量消息时同时播放TTS语音
     */
    public void onEnableTtsWhenNotifyBattery(View view) {
        boolean isChecked = cbTtsWhenNotifyBattery.isChecked();
        LogUtils.d(TAG, "onEnableTtsWhenNotifyBattery: 通知电量时播TTS开关状态=" + isChecked);
        ThoughtfulServiceBean bean = getThoughtfulServiceBean();
        bean.setIsEnableTtsWhenNotifyBattery(isChecked);
        ThoughtfulServiceBean.saveBean(this, bean);
        LogUtils.d(TAG, "onEnableTtsWhenNotifyBattery: 保存完成");
    }

    // ======================== 工具方法区 =========================
    /**
     * 获取配置Bean实例，避免重复代码
     */
    private ThoughtfulServiceBean getThoughtfulServiceBean() {
        LogUtils.d(TAG, "getThoughtfulServiceBean: 获取配置Bean");
        ThoughtfulServiceBean bean = ThoughtfulServiceBean.loadBean(this, ThoughtfulServiceBean.class);
        if (bean == null) {
            LogUtils.d(TAG, "getThoughtfulServiceBean: 配置Bean为空，创建新实例");
            bean = new ThoughtfulServiceBean();
        }
        return bean;
    }

    /**
     * 悬浮窗权限检查与请求
     */
    void canDrawOverlays() {
        LogUtils.d(TAG, "canDrawOverlays: 悬浮窗权限检查开始");
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            LogUtils.d(TAG, "canDrawOverlays: 未开启悬浮窗权限，发起请求");
            showDrawOverlayRequestDialog();
        } else {
            LogUtils.d(TAG, "canDrawOverlays: 悬浮窗权限已开启");
            ToastUtils.show("悬浮窗权限已开启");
        }
    }

    /**
     * 显示悬浮窗权限请求对话框
     */
    private void showDrawOverlayRequestDialog() {
        LogUtils.d(TAG, "showDrawOverlayRequestDialog: 显示悬浮窗权限请求弹窗");
        AlertDialog dialog = new AlertDialog.Builder(this)
			.setTitle("权限请求")
			.setMessage("为保证通话监听功能正常，需开启悬浮窗权限")
			.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					LogUtils.d(TAG, "showDrawOverlayRequestDialog-去设置: 点击跳转权限页面");
					dialog.dismiss();
					jumpToDrawOverlaySettings();
				}
			})
			.setNegativeButton("稍后", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					LogUtils.d(TAG, "showDrawOverlayRequestDialog-稍后: 点击取消请求");
					dialog.dismiss();
				}
			})
			.create();

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
        LogUtils.d(TAG, "jumpToDrawOverlaySettings: 跳转悬浮窗权限设置页面");
        try {
            Class<?> settingsClazz = Settings.class;
            Field actionField = settingsClazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
            String action = (String) actionField.get(null);

            Intent intent = new Intent(action);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            LogUtils.d(TAG, "jumpToDrawOverlaySettings: 跳转权限页面意图已发送");
        } catch (Exception e) {
            LogUtils.e(TAG, "jumpToDrawOverlaySettings: 跳转权限设置失败", e);
            Toast.makeText(this, "请手动在设置中开启悬浮窗权限", Toast.LENGTH_LONG).show();
        }
    }
}

