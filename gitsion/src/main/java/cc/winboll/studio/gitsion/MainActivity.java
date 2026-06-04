package cc.winboll.studio.gitsion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.gitsion.R;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogActivity;
import cc.winboll.studio.libappbase.LogUtils;

import cc.winboll.studio.libappbase.ToastUtils;

/**
 * WinBoLL Studio
 * GPSRelaySentinel 主控制页面
 * Java7 | API26~30
 * 新增：模拟模式勾选控制 + 按钮互斥可用状态
 */
public final class MainActivity extends AppCompatActivity {

    //原有控件
    private Toolbar mToolbar;

    private Switch mSwitchService;

    //新增
    private CheckBox mCheckBoxSimMode;
    private Button btnSendLastGps;
    private Spinner spinDirection;
    private EditText etSimDistance;
    private TextView tvTargetPreview;
    private Button btnSimSend;

    //全局模式标识 供给MainService判断
    public static boolean IS_GPS_SIM_MODE = false;

    //最后真实GPS坐标
    public static double lastLat = 30.5928;
    public static double lastLng = 114.3055;

    //全局模拟坐标 供给MainService使用
    public static double simLat = 30.5928;
    public static double simLng = 114.3055;

    //方位对应角度(正北0° 顺时针)
    private double currentAngle = 0.0D;

    //权限请求常量
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initToolbar();
        initSwitchEvent();
        initSimPanelEvent();
        initSimModeCheck();

        ToastUtils.show("onCreate");
    }

    /**
     * 全部控件绑定
     */
    private void initView() {
        //原有
        mToolbar = findViewById(R.id.toolbar);

        mSwitchService = findViewById(R.id.switch_service);

        //新增
        mCheckBoxSimMode = findViewById(R.id.checkbox_sim_mode);
        btnSendLastGps = findViewById(R.id.btn_send_last_gps);
        spinDirection = findViewById(R.id.spin_direction);
        etSimDistance = findViewById(R.id.et_sim_distance);
        tvTargetPreview = findViewById(R.id.tv_target_point_preview);
        btnSimSend = findViewById(R.id.btn_sim_send_gps);

		//方位下拉 全局灰色文字
		ArrayAdapter<CharSequence> dirAdapter = ArrayAdapter.createFromResource(
			this,
			R.array.direction_list,
			R.layout.spinner_item_gray
		);
		dirAdapter.setDropDownViewResource(R.layout.spinner_item_gray);
		spinDirection.setAdapter(dirAdapter);

        //初始化开关状态
        mSwitchService.setChecked(hasLocationPermission());
        refreshButtonEnableStatus();
        refreshTargetPreview();
    }

    //模拟勾选框监听
    private void initSimModeCheck() {
        mCheckBoxSimMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					IS_GPS_SIM_MODE = isChecked;
					refreshButtonEnableStatus();
					if (isChecked) {
						ToastUtils.show("已进入GPS模拟模式");
					} else {
						ToastUtils.show("退出模拟模式，使用真实GPS");
					}
				}
			});
    }

    //刷新按钮互斥可用状态
    private void refreshButtonEnableStatus() {
        if (IS_GPS_SIM_MODE) {
            //模拟模式：真实按钮禁用、模拟按钮可用
            btnSendLastGps.setEnabled(false);
            btnSimSend.setEnabled(true);
        } else {
            //正常模式：真实可用、模拟禁用
            btnSendLastGps.setEnabled(true);
            btnSimSend.setEnabled(false);
        }
    }

    /**
     * 初始化标题栏
     */
    private void initToolbar() {
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (GlobalApplication.isDebugging()) {
            menu.setGroupVisible(R.id.group_debug, true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_gps_history) {
            startActivity(new Intent(this, GpsHistoryActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_app_log) {
            LogActivity.startLogActivity(this, false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * GPS服务开关监听
     */
    private void initSwitchEvent() {
        mSwitchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						if (hasLocationPermission()) {
							startGpsService();
						} else {
							requestLocationPermission();
							mSwitchService.setChecked(false);
						}
					} else {
						stopGpsService();
					}
				}
			});
    }

    /**
     * 模拟发送面板 全部事件初始化
     */
    private void initSimPanelEvent() {
        //1.原按钮：发送最后一条真实GPS
        btnSendLastGps.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sendLastRealGpsBroadcast();
				}
			});

        //2.方位下拉选择 -> 切换角度并刷新预览
        spinDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					currentAngle = getDirectionAngle(position);
					refreshTargetPreview();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

        //3.距离输入变化自动预览
        etSimDistance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						refreshTargetPreview();
					}
				}
			});

        //4.模拟发送按钮：计算偏移并赋值全局模拟坐标
        btnSimSend.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveSimGpsData();
					GpsHistoryManager.getInstance().addSimRecord(new cc.winboll.studio.libgitsion.model.GpsSubscribeResult(
							"SIM",
							cc.winboll.studio.libgitsion.model.GpsSubscribeConst.RESULT_SUCCESS,
							"模拟GPS定位",
							cc.winboll.studio.libgitsion.model.GpsSubscribeConst.GPS_STATE_LOCATED,
							0,
							System.currentTimeMillis(),
							simLat,
							simLng,
							System.currentTimeMillis()
						));
					ToastUtils.show("已设置当前模拟GPS坐标");
				}
			});
    }

    /**
     * 保存模拟坐标到全局静态变量 供给MainService使用
     */
    private void saveSimGpsData() {
        String disText = etSimDistance.getText().toString().trim();
        double distance = 10D;
        try {
            distance = Double.parseDouble(disText);
        } catch (Exception e) {
            ToastUtils.show("请输入合法距离");
            return;
        }
        double[] target = calculateOffsetLatLng(lastLat, lastLng, distance, currentAngle);
        simLat = target[0];
        simLng = target[1];
        refreshTargetPreview();
    }

    /**
     * 根据下拉position获取对应方位角度
     */
    private double getDirectionAngle(int pos) {
        switch (pos) {
            case 0: return 0.0D;    //正北
            case 1: return 180.0D;  //正南
            case 2: return 90.0D;   //正东
            case 3: return 270.0D;  //正西
            case 4: return 45.0D;   //东北
            case 5: return 315.0D;  //西北
            case 6: return 135.0D;  //东南
            case 7: return 225.0D;  //西南
            default:return 0.0D;
        }
    }

    /**
     * 根据基准坐标+距离+角度 计算偏移经纬度
     */
    private double[] calculateOffsetLatLng(double lat, double lng, double distanceMeter, double angle) {
        double radAngle = Math.toRadians(angle);
        double radLat = Math.toRadians(lat);

        double meterPerLat = 111320D;
        double meterPerLng = Math.cos(radLat) * 111320D;

        double offsetLat = (distanceMeter * Math.cos(radAngle)) / meterPerLat;
        double offsetLng = (distanceMeter * Math.sin(radAngle)) / meterPerLng;

        return new double[]{lat + offsetLat , lng + offsetLng};
    }

    /**
     * 刷新目标坐标预览
     */
    private void refreshTargetPreview() {
        String disText = etSimDistance.getText().toString().trim();
        double distance = 10D;
        try {
            distance = Double.parseDouble(disText);
        } catch (Exception e) {}

        double[] target = calculateOffsetLatLng(lastLat, lastLng, distance, currentAngle);
        String info = "目标模拟坐标："
			+ String.format("%.6f", target[0])
			+ " , "
			+ String.format("%.6f", target[1]);
        tvTargetPreview.setText(info);
    }

    /**
     * 发送【最后真实GPS】广播
     */
    private void sendLastRealGpsBroadcast() {
        Intent broadcast = new Intent("GPS_DATA_BROADCAST");
        broadcast.putExtra("isSim", false);
        broadcast.putExtra("lat", lastLat);
        broadcast.putExtra("lng", lastLng);
        sendBroadcast(broadcast);
        LogUtils.d("GPS_SEND", "发送真实GPS -> lat:" + lastLat + " lng:" + lastLng);
    }

    //—————— 原有权限 & 服务启停 完全原样保留 ——————

    private boolean hasLocationPermission() {
        boolean basicPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
			|| checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (basicPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return basicPermission;
    }

    private void requestLocationPermission() {
        String[] permissionArray;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissionArray = new String[]{
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION,
				android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            permissionArray = new String[]{
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        requestPermissions(permissionArray, REQUEST_LOCATION_PERMISSION);
    }

    private void startGpsService() {
        Intent serviceIntent = new Intent(MainActivity.this, MainService.class);
        startForegroundService(serviceIntent);
        ToastUtils.show("GPS Service started");
        LogUtils.d(MainService.TAG, "GPS Service started from MainActivity");
    }

    private void stopGpsService() {
        getSharedPreferences(MainService.PREF_NAME, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(MainService.KEY_SERVICE_ENABLED, false)
			.apply();

        Intent serviceIntent = new Intent(MainActivity.this, MainService.class);
        stopService(serviceIntent);
        ToastUtils.show("GPS Service stopped");
        LogUtils.d(MainService.TAG, "GPS Service stopped from MainActivity");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mSwitchService.setChecked(true);
                startGpsService();
            } else {
                ToastUtils.show("需要位置权限才能使用GPS服务");
                mSwitchService.setChecked(false);
            }
        }
    }


}

