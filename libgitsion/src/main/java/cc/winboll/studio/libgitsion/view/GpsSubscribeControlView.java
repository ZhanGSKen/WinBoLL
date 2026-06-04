package cc.winboll.studio.libgitsion.view;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import cc.winboll.studio.libgitsion.R;
import cc.winboll.studio.libgitsion.manager.GpsSubscribeManager;
import cc.winboll.studio.libgitsion.manager.SubscribeLocationManager;
import cc.winboll.studio.libgitsion.model.GpsSubscribeConst;
import cc.winboll.studio.libgitsion.model.GpsSubscribeMsg;
import cc.winboll.studio.libgitsion.model.LocationPoint;

import java.util.UUID;

public final class GpsSubscribeControlView extends LinearLayout {

    //常量抽取
    private static final long REFRESH_INTERVAL = 600;

    private RadioGroup rgSubscribeMode;
    private RadioButton rbModeAll;
    private RadioButton rbModeStep;
    private EditText etStepMeter;
    private Switch switchSubscribe;
    private TextView tvSubscribeSid;
    private TextView tvSubscribeRecord;

    private String currentSubscribeSid;
    //一对一专属绑定的接收服务
    private Class<?> mBindReceiverServiceClazz;

    //final管理器 构造器初始化
    private final GpsSubscribeManager mSubscribeManager;
    private final SubscribeLocationManager mLocationManager;

    private final Handler mRefreshHandler = new Handler(Looper.getMainLooper());


    public GpsSubscribeControlView(Context context) {
        super(context);
        mSubscribeManager = GpsSubscribeManager.getInstance();
        mLocationManager = SubscribeLocationManager.getInstance();
        initView(context);
    }

    public GpsSubscribeControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSubscribeManager = GpsSubscribeManager.getInstance();
        mLocationManager = SubscribeLocationManager.getInstance();
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_gps_subscribe_control, this, true);

        rgSubscribeMode = findViewById(R.id.rg_subscribe_mode);
        rbModeAll = findViewById(R.id.rb_mode_all);
        rbModeStep = findViewById(R.id.rb_mode_step);
        etStepMeter = findViewById(R.id.et_step_meter);
        switchSubscribe = findViewById(R.id.switch_subscribe);
        tvSubscribeSid = findViewById(R.id.tv_subscribe_sid);
        tvSubscribeRecord = findViewById(R.id.tv_subscribe_record);

        initDefaultConfig();
        initModeSwitch();
        initSubscribeSwitch();
        startAutoRefreshRecord();
    }

    private void initDefaultConfig() {
        currentSubscribeSid = UUID.randomUUID().toString().substring(0, 16);
        tvSubscribeSid.setText("订阅SID：" + currentSubscribeSid);
        rbModeAll.setChecked(true);
        etStepMeter.setText("10");
    }

    /**
     * 外部绑定当前视图专属的接收服务Class
     */
    public void bindReceiverService(Class<?> serviceClazz){
        this.mBindReceiverServiceClazz = serviceClazz;
    }

    private void initModeSwitch() {
        rgSubscribeMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					etStepMeter.setVisibility(checkedId == R.id.rb_mode_step ? VISIBLE : GONE);
				}
			});
    }

    private void initSubscribeSwitch() {
        switchSubscribe.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						startSubscribe();
					} else {
						stopSubscribe();
					}
				}
			});
    }

    private void startSubscribe() {
        int subMode = GpsSubscribeConst.SUB_TYPE_ALL;
        float stepVal = 10f;

        if (rbModeStep.isChecked()) {
            subMode = GpsSubscribeConst.SUB_TYPE_STEP_DISTANCE;
            try {
                stepVal = Float.parseFloat(etStepMeter.getText().toString().trim());
            } catch (Exception ignored) {}
        }

        GpsSubscribeMsg subscribeMsg = new GpsSubscribeMsg(
			getContext().getPackageName(),
			subMode,
			stepVal,
			GpsSubscribeConst.SUBSCRIBE_TYPE_LOCATION,
			1000,
			1f,
			true,
			currentSubscribeSid
        );

        mSubscribeManager.addSubscribe(subscribeMsg);
        mLocationManager.putSubscribeConfig(currentSubscribeSid, subscribeMsg);
        mLocationManager.clearPushCount(currentSubscribeSid);

        //开启订阅自动启动专属接收服务（携带SID）
        if(mBindReceiverServiceClazz != null){
            Intent startServiceIntent = new Intent(getContext(), mBindReceiverServiceClazz);
            startServiceIntent.putExtra(GpsSubscribeConst.EXTRA_SUBSCRIBE_SID, currentSubscribeSid);
            getContext().startService(startServiceIntent);
        }
    }

    private void stopSubscribe() {
        mSubscribeManager.removeSubscribe(currentSubscribeSid);
        mLocationManager.removeSubscribe(currentSubscribeSid);
        tvSubscribeRecord.setText("状态：未订阅");

        //关闭订阅 同步停止专属接收服务
        if(mBindReceiverServiceClazz != null){
            Intent stopServiceIntent = new Intent(getContext(), mBindReceiverServiceClazz);
            getContext().stopService(stopServiceIntent);
        }
    }

    private void startAutoRefreshRecord() {
        mRefreshHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					refreshRecordInfo();
					mRefreshHandler.postDelayed(this, REFRESH_INTERVAL);
				}
			}, REFRESH_INTERVAL);
    }

    private void refreshRecordInfo() {
        if (!switchSubscribe.isChecked()) {
            tvSubscribeRecord.setText("状态：空闲未订阅");
            return;
        }

        GpsSubscribeMsg config = mLocationManager.getSubscribeConfig(currentSubscribeSid);
        LocationPoint lastPoint = mLocationManager.getLastPoint(currentSubscribeSid);

        if (config == null) {
            tvSubscribeRecord.setText("状态：已订阅｜等待管理器加载");
            return;
        }

        String modeText = config.getSubscribeMode() == GpsSubscribeConst.SUB_TYPE_ALL
			? "全量订阅" : "步长订阅";

        int realPushCount = mLocationManager.getPushCount(currentSubscribeSid);

        StringBuilder record = new StringBuilder();
        record.append("【订阅实时数据表】\n");
        record.append("订阅模式：").append(modeText).append("\n");
        record.append("步长阈值：").append(config.getStepDistanceM()).append(" 米\n");

        if(lastPoint != null){
            record.append("基准定点：").append(lastPoint.getLatitude()).append(" , ").append(lastPoint.getLongitude()).append("\n");
        }else{
            record.append("基准定点：等待首次定位建立\n");
        }

        record.append("真实推送次数：").append(realPushCount).append(" 次");

        tvSubscribeRecord.setText(record);
    }

    public String getCurrentSid() {
        return currentSubscribeSid;
    }

    public boolean isSubscribeOpen() {
        return switchSubscribe.isChecked();
    }

    /**
     * 视图销毁：强制停止订阅 + 停止服务 + 清空刷新任务
     */
    @Override
    protected void onDetachedFromWindow() {
        if(switchSubscribe.isChecked()){
            switchSubscribe.setChecked(false);
        }
        mRefreshHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}

