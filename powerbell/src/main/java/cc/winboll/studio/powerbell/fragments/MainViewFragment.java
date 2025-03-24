package cc.winboll.studio.powerbell.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.activities.BackgroundPictureActivity;
import cc.winboll.studio.powerbell.beans.BackgroundPictureBean;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BackgroundPictureUtils;
import cc.winboll.studio.powerbell.utils.ServiceUtils;
import cc.winboll.studio.powerbell.views.BatteryDrawable;
import cc.winboll.studio.powerbell.views.VerticalSeekBar;
import java.io.File;

public class MainViewFragment extends Fragment {

    public static final String TAG = MainViewFragment.class.getSimpleName();

    public static final int MSG_RELOAD_APPCONFIG = 0;
    public static final int MSG_CURRENTVALUEBATTERY = 1;

    static MainViewFragment _mMainViewFragment;
    AppConfigUtils mAppConfigUtils;
    View mView;
    Drawable mDrawableFrame;
    LinearLayout mllLeftSeekBar;
    LinearLayout mllRightSeekBar;
    CheckBox mcbIsEnableChargeReminder;
    CheckBox mcbIsEnableUsegeReminder;
    Switch mswIsEnableService;
    TextView mtvTips;

    // 背景布局
    //LinearLayout mLinearLayoutloadBackground;

    // 现在电量图示
    BatteryDrawable mCurrentValueBatteryDrawable;
    // 现在充电提醒电量图示
    BatteryDrawable mChargeReminderValueBatteryDrawable;
    // 现在耗电提醒电量图示
    BatteryDrawable mUsegeReminderValueBatteryDrawable;

    ImageView mCurrentValueBatteryImageView;
    ImageView mChargeReminderValueBatteryImageView;
    ImageView mUsegeReminderValueBatteryImageView;

    VerticalSeekBar mChargeReminderSeekBar;
    ChargeReminderSeekBarChangeListener mChargeReminderSeekBarChangeListener;
    TextView mtvChargeReminderValue;


    VerticalSeekBar mUsegeReminderSeekBar;
    UsegeReminderSeekBarChangeListener mUsegeReminderSeekBarChangeListener;
    TextView mtvUsegeReminderValue;
    CheckBox mcbUsegeReminderValue;
    TextView mtvCurrentValue;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mainview, container, false);
        _mMainViewFragment = MainViewFragment.this;
        mAppConfigUtils = App.getAppConfigUtils(getActivity());
        
        // 获取指定ID的View实例
        final View mainImageView = mView.findViewById(R.id.fragmentmainviewImageView1);

        // 注册OnGlobalLayoutListener
        mainImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // 获取宽度和高度
                    int width = mainImageView.getMeasuredWidth();
                    int height = mainImageView.getMeasuredHeight();

                    BackgroundPictureUtils utils = BackgroundPictureUtils.getInstance(getActivity());
                    BackgroundPictureBean bean = utils.loadBackgroundPictureBean();
                    bean.setBackgroundWidth(width);
                    bean.setBackgroundHeight(height);
                    utils.saveData();
                    // 移除监听器以避免内存泄漏
                    mainImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

        mDrawableFrame = getActivity().getDrawable(R.drawable.bg_frame);
        mllLeftSeekBar = (LinearLayout) mView.findViewById(R.id.fragmentmainviewLinearLayout1);
        mllRightSeekBar = (LinearLayout) mView.findViewById(R.id.fragmentmainviewLinearLayout2);

        // 初始化充电电量提醒设置控件
        mtvChargeReminderValue = (TextView) mView.findViewById(R.id.fragmentandroidviewTextView2);
        mChargeReminderSeekBar = (VerticalSeekBar) mView.findViewById(R.id.fragmentandroidviewVerticalSeekBar1);
        mcbIsEnableChargeReminder = mView.findViewById(R.id.fragmentmainviewCheckBox1);

        // 初始化耗电电量提醒设置控件
        mtvUsegeReminderValue = (TextView) mView.findViewById(R.id.fragmentandroidviewTextView3);
        mUsegeReminderSeekBar = (VerticalSeekBar) mView.findViewById(R.id.fragmentandroidviewVerticalSeekBar2);
        mcbIsEnableUsegeReminder = mView.findViewById(R.id.fragmentmainviewCheckBox2);

        // 初始化现在电量显示控件
        mtvCurrentValue = (TextView) mView.findViewById(R.id.fragmentandroidviewTextView4);

        // 初始化服务总开关
        mswIsEnableService = (Switch) mView.findViewById(R.id.fragmentandroidviewSwitch1);
        mtvTips = mView.findViewById(R.id.fragmentandroidviewTextView1);

        // 设置视图显示数据
        setViewData();
        // 设置视图控件响应
        setViewListener();

        // 注册一个广播接收
        //mMainActivityReceiver = new MainActivityReceiver(this);
        //mMainActivityReceiver.registerAction();

        // 启动的时候检查一下服务
        if (mAppConfigUtils.getIsEnableService()
            && ServiceUtils.isServiceAlive(getActivity(), ControlCenterService.class.getName()) == false) {
            // 如果配置了服务启动，服务没有启动
            // 就启动服务
            Intent intent = new Intent(getActivity(), ControlCenterService.class);
            getActivity().startForegroundService(intent);
        }

        return mView;
    }

    void setViewData() {
        int nChargeReminderValue = mAppConfigUtils.getChargeReminderValue();
        int nUsegeReminderValue = mAppConfigUtils.getUsegeReminderValue();
        int nCurrentValue = mAppConfigUtils.getCurrentValue();

        mllLeftSeekBar.setBackground(mDrawableFrame);
        mllRightSeekBar.setBackground(mDrawableFrame);

        // 初始化电量图
        mCurrentValueBatteryDrawable = new BatteryDrawable(getActivity().getColor(R.color.colorCurrent));
        mCurrentValueBatteryDrawable.setValue(mAppConfigUtils.getCurrentValue());
        mCurrentValueBatteryImageView = mView.findViewById(R.id.fragmentandroidviewImageView1);
        mCurrentValueBatteryImageView.setImageDrawable(mCurrentValueBatteryDrawable);

        // 初始化充电电量提醒图
        mChargeReminderValueBatteryDrawable = new BatteryDrawable(getActivity().getColor(R.color.colorCharge));
        mChargeReminderValueBatteryDrawable.setValue(nChargeReminderValue);
        mChargeReminderValueBatteryImageView = mView.findViewById(R.id.fragmentandroidviewImageView3);
        mChargeReminderValueBatteryImageView.setImageDrawable(mChargeReminderValueBatteryDrawable);

        // 初始化耗电电量提醒图
        mUsegeReminderValueBatteryDrawable = new BatteryDrawable(getActivity().getColor(R.color.colorUsege));
        mUsegeReminderValueBatteryDrawable.setValue(nUsegeReminderValue);
        mUsegeReminderValueBatteryImageView = mView.findViewById(R.id.fragmentandroidviewImageView2);
        mUsegeReminderValueBatteryImageView.setImageDrawable(mUsegeReminderValueBatteryDrawable);

        // 初始化充电电量提醒设置控件
        mtvChargeReminderValue.setTextColor(getActivity().getColor(R.color.colorCharge));
        //LogUtils.d(TAG, "Color.YELLOW is " + Integer.toString(mApplication.getColor(R.color.colorCharge)));
        mtvChargeReminderValue.setText(Integer.toString(nChargeReminderValue) + "%");
        mChargeReminderSeekBar.setProgress(nChargeReminderValue);
        mcbIsEnableChargeReminder.setChecked(mAppConfigUtils.getIsEnableChargeReminder());

        // 初始化耗电电量提醒设置控件
        mtvUsegeReminderValue.setTextColor(getActivity().getColor(R.color.colorUsege));
        mtvUsegeReminderValue.setText(Integer.toString(nUsegeReminderValue) + "%");
        mUsegeReminderSeekBar.setProgress(nUsegeReminderValue);
        mcbIsEnableUsegeReminder.setChecked(mAppConfigUtils.getIsEnableUsegeReminder());

        // 初始化现在电量显示控件
        mtvCurrentValue.setTextColor(getActivity().getColor(R.color.colorCurrent));
        mtvCurrentValue.setText(Integer.toString(nCurrentValue) + "%");

        // 初始化服务总开关
        mswIsEnableService.setChecked(mAppConfigUtils.getIsEnableService());
        if (mAppConfigUtils.getIsEnableService()) {
            //LogUtils.d(TAG, "mApplication.getIsEnableService() " + Boolean.toString(mAppConfigUtils.getIsEnableService()));
            ControlCenterService.startControlCenterService(getActivity());
        } else {
            //LogUtils.d(TAG, "mApplication.getIsEnableService() " + Boolean.toString(mAppConfigUtils.getIsEnableService()));
            ControlCenterService.stopControlCenterService(getActivity());
        }
        mswIsEnableService.setText(getString(R.string.txt_aboveswitch));
        mtvTips.setText(getString(R.string.txt_aboveswitchtips));

    }

    void setViewListener() {
        // 初始化充电电量提醒设置控件
        mChargeReminderSeekBarChangeListener = new ChargeReminderSeekBarChangeListener();
        mChargeReminderSeekBar.setOnSeekBarChangeListener(mChargeReminderSeekBarChangeListener);
        mcbIsEnableChargeReminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.d(TAG, "setIsEnableChargeReminder");
                    mAppConfigUtils.setIsEnableChargeReminder(mcbIsEnableChargeReminder.isChecked());
                    //ControlCenterService.updateIsEnableChargeReminder(mcbIsEnableChargeReminder.isChecked());
                }
            });


        // 初始化耗电电量提醒设置控件
        mUsegeReminderSeekBarChangeListener = new UsegeReminderSeekBarChangeListener();
        mUsegeReminderSeekBar.setOnSeekBarChangeListener(mUsegeReminderSeekBarChangeListener);
        mcbIsEnableUsegeReminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.d(TAG, "setIsEnableUsegeReminder");
                    mAppConfigUtils.setIsEnableUsegeReminder(mcbIsEnableUsegeReminder.isChecked());
                    //ControlCenterService.updateIsEnableUsegeReminder(mcbIsEnableUsegeReminder.isChecked());
                }
            });

        // 初始化服务总开关
        mswIsEnableService.setOnClickListener(new CompoundButton.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mAppConfigUtils.setIsEnableService(getActivity(), mswIsEnableService.isChecked());
                }
            });
    }

    void setCurrentValueBattery(int value) {
        //LogUtils.d(TAG, "setCurrentValueBattery");
        mtvCurrentValue.setText(Integer.toString(value) + "%");
        mCurrentValueBatteryDrawable.setValue(value);
        mCurrentValueBatteryDrawable.invalidateSelf();
    }

    class ChargeReminderSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //LogUtils.d(TAG, "call onProgressChanged");
            int nChargeReminderValue = progress;
            mtvChargeReminderValue.setText(Integer.toString(nChargeReminderValue) + "%");
            mChargeReminderValueBatteryDrawable.setValue(nChargeReminderValue);
            mChargeReminderValueBatteryDrawable.invalidateSelf();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //LogUtils.d(TAG, "call onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //LogUtils.d(TAG, "call onStopTrackingTouch");
            //取得当前进度条的刻度
            int nChargeReminderValue = ((VerticalSeekBar)seekBar)._mnProgress;

            mAppConfigUtils.setChargeReminderValue(nChargeReminderValue);
            mtvChargeReminderValue.setText(Integer.toString(nChargeReminderValue) + "%");
            //ControlCenterService.updateChargeReminderValue(nChargeReminderValue);
        }
    }

    class UsegeReminderSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //LogUtils.d(TAG, "call onProgressChanged");
            int nUsegeReminderValue = progress;
            mtvUsegeReminderValue.setText(Integer.toString(nUsegeReminderValue) + "%");
            mUsegeReminderValueBatteryDrawable.setValue(nUsegeReminderValue);
            mUsegeReminderValueBatteryDrawable.invalidateSelf();

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //LogUtils.d(TAG, "call onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //LogUtils.d(TAG, "call onStopTrackingTouch");
            //取得当前进度条的刻度
            int nUsegeReminderValue = ((VerticalSeekBar)seekBar)._mnProgress;
            LogUtils.d(TAG, "nUsegeReminderValue is " + Integer.toString(nUsegeReminderValue));
            //LogUtils.d(TAG, "mPowerReminder is " + mApplication);
            mAppConfigUtils.setUsegeReminderValue(nUsegeReminderValue);
            //LogUtils.d(TAG, "opopopopopopopop");
            mtvUsegeReminderValue.setText(Integer.toString(nUsegeReminderValue) + "%");
            //ControlCenterService.updateUsegeReminderValue(nUsegeReminderValue);
        }
    }

    public void loadBackground() {
        BackgroundPictureBean bean = BackgroundPictureUtils.getInstance(getActivity()).getBackgroundPictureBean();
        ImageView imageView = mView.findViewById(R.id.fragmentmainviewImageView1);
        String szBackgroundFilePath = BackgroundPictureUtils.getInstance(getActivity()).getBackgroundDir() + BackgroundPictureActivity.getBackgroundFileName();
        File fBackgroundFilePath = new File(szBackgroundFilePath);
        LogUtils.d(TAG, "szBackgroundFilePath : " + szBackgroundFilePath);
        if (bean.isUseBackgroundFile() && fBackgroundFilePath.exists()) {
            Drawable drawableBackground = Drawable.createFromPath(szBackgroundFilePath);
            drawableBackground.setAlpha(120);
            imageView.setImageDrawable(drawableBackground);
        } else {
            Drawable drawableBackground = getActivity().getDrawable(R.drawable.blank10x10);
            drawableBackground.setAlpha(120);
            imageView.setImageDrawable(drawableBackground);
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RELOAD_APPCONFIG : {
                        setViewData();
                        break;
                    }
                case MSG_CURRENTVALUEBATTERY : {
                        setCurrentValueBattery(msg.arg1);
                        break;
                    }
            }
            super.handleMessage(msg);
        }

    };

    public static void relaodAppConfigs() {
        if (_mMainViewFragment != null) {
            Handler handler = _mMainViewFragment.mHandler;
            handler.sendMessage(handler.obtainMessage(MSG_RELOAD_APPCONFIG));
        }
    }

    public static void sendMsgCurrentValueBattery(int value) {
        if (_mMainViewFragment != null) {
            Handler handler = _mMainViewFragment.mHandler;
            Message msg = handler.obtainMessage(MSG_CURRENTVALUEBATTERY);
            msg.arg1 = value;
            handler.sendMessage(msg);
        }
    }
}
    
    
    

