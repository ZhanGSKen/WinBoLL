package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BatteryInfoBean;
import cc.winboll.studio.powerbell.receivers.ControlCenterServiceReceiver;
import cc.winboll.studio.powerbell.utils.AppCacheUtils;
import cc.winboll.studio.powerbell.utils.StringUtils;
import java.util.ArrayList;

/**
 * 电池记录清理页面，支持滑动清理记录、切换记录显示格式
 * 适配 API30，基于 Java7 开发
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class ClearRecordActivity extends WinBoLLActivity implements IWinBoLLActivity {
    // ======================== 静态常量（按功能分类） =========================
    public static final String TAG = "ClearRecordActivity";
    private static final String TOAST_MSG_CLEAR_SUCCESS = "The APP battery record is cleaned.";

    // ======================== 成员变量（按依赖优先级+功能分类） =========================
    // UI组件
    private Toolbar mToolbar;
    private TextView mtvRecordText;
    private TextView tvAOHPCTCSeekBarMSG;
    private AOHPCTCSeekBar aOHPCTCSeekBar;

    // 应用与配置
    private App mApplication;
    private boolean mIsShowRecordWithEnter = false; // 记录是否带换行显示

    // ======================== 接口实现方法 =========================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ======================== 生命周期方法（按执行顺序排列） =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clearrecord);
        LogUtils.d(TAG, "【onCreate】ClearRecordActivity 初始化开始");

        // 初始化应用实例
        mApplication = (App) getApplication();
        LogUtils.d(TAG, "【onCreate】应用实例初始化完成");

        // 初始化核心逻辑
        initView();
        initSeekBar();
        initRecordText();

        LogUtils.d(TAG, "【onCreate】ClearRecordActivity 初始化完成");
    }

    // ======================== UI初始化方法 =========================
    /**
     * 初始化Toolbar与显示文本组件
     */
    private void initView() {
        // 初始化Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(getTag());
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "【导航栏】点击返回按钮，关闭当前页面");
					finish();
				}
			});

        // 初始化显示文本组件
        tvAOHPCTCSeekBarMSG = (TextView) findViewById(R.id.activityclearrecordTextView1);
        mtvRecordText = (TextView) findViewById(R.id.activityclearrecordTextView2);
        tvAOHPCTCSeekBarMSG.setText(R.string.msg_AOHPCTCSeekBar_ClearRecord);

        LogUtils.d(TAG, "【initView】UI组件初始化完成");
    }

    /**
     * 初始化滑动清理控件，设置回调监听
     */
    private void initSeekBar() {
        aOHPCTCSeekBar = (AOHPCTCSeekBar) findViewById(R.id.activityclearrecordAOHPCTCSeekBar1);
        aOHPCTCSeekBar.setThumb(getDrawable(R.drawable.cursor_pointer));
        aOHPCTCSeekBar.setThumbOffset(0);
        aOHPCTCSeekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {
				@Override
				public void onOHPCommit() {
					LogUtils.d(TAG, "【onOHPCommit】滑动清理触发，开始执行记录清理逻辑");
					// 清理电池历史记录
					mApplication.clearBatteryHistory();
					// 发送广播更新前台通知
					sendBroadcast(new Intent(ControlCenterServiceReceiver.ACTION_UPDATE_FOREGROUND_NOTIFICATION));
					// 刷新记录显示
					initRecordText();
					// 提示清理成功
					ToastUtils.show(TOAST_MSG_CLEAR_SUCCESS);
					LogUtils.d(TAG, "【onOHPCommit】电池记录清理完成，已发送前台通知更新广播");
				}
			});

        LogUtils.d(TAG, "【initSeekBar】滑动清理控件初始化完成，回调监听已绑定");
    }

    // ======================== 业务逻辑方法 =========================
    /**
     * 初始化记录显示文本，根据配置切换带换行/不带换行格式
     */
    void initRecordText() {
        ArrayList<BatteryInfoBean> listBatteryInfo = AppCacheUtils.getInstance(this).getArrayListBatteryInfo();
        String szRecordText;

        // 判空处理：避免空列表导致异常
        if (listBatteryInfo == null || listBatteryInfo.isEmpty()) {
            szRecordText = getString(R.string.msg_no_battery_record);
            LogUtils.d(TAG, "【initRecordText】无电池记录数据，显示空记录提示文本");
        } else {
            // 根据配置切换显示格式
            if (mIsShowRecordWithEnter) {
                szRecordText = StringUtils.formatPCMListStringWithEnter(listBatteryInfo);
                LogUtils.d(TAG, String.format("【initRecordText】使用带换行格式显示记录，记录数量：%d", listBatteryInfo.size()));
            } else {
                szRecordText = StringUtils.formatPCMListString(listBatteryInfo);
                LogUtils.d(TAG, String.format("【initRecordText】使用无换行格式显示记录，记录数量：%d", listBatteryInfo.size()));
            }
        }

        mtvRecordText.setText(szRecordText);
        LogUtils.d(TAG, "【initRecordText】记录显示文本刷新完成");
    }

    // ======================== 事件回调方法 =========================
    /**
     * 切换记录显示格式（带换行/不带换行）
     * @param view 触发事件的Switch控件
     */
    public void onShowRecordWithEnter(View view) {
        Switch swShowRecordWithEnter = (Switch) view;
        mIsShowRecordWithEnter = swShowRecordWithEnter.isChecked();
        LogUtils.d(TAG, String.format("【onShowRecordWithEnter】记录显示格式切换，带换行显示：%b", mIsShowRecordWithEnter));
        // 刷新记录显示
        initRecordText();
    }
}

