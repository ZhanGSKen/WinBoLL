package cc.winboll.studio.contacts.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/20 17:15:46
 * @Describe 拨号窗口
 */
public class CallActivity extends AppCompatActivity {

    // ====================== 常量定义区 ======================
    public static final String TAG = "CallActivity";
    private static final int REQUEST_CALL_PHONE = 1;

    // ====================== UI控件区 ======================
    private EditText phoneNumberEditText;
    private TextView callStatusTextView;
    private Button dialButton;

    // ====================== 业务成员区 ======================
    private TelephonyManager telephonyManager;
    private MyPhoneStateListener phoneStateListener;

    // ====================== 生命周期函数区 ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: 拨号页面开始创建");
        setContentView(R.layout.activity_call);

        // 初始化控件
        initViews();
        // 初始化电话状态监听
        initPhoneStateListener();
        LogUtils.d(TAG, "onCreate: 拨号页面初始化完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 拨号页面开始销毁");
        // 取消电话状态监听，避免内存泄漏
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            LogUtils.d(TAG, "onDestroy: 电话状态监听已取消");
        }
        LogUtils.d(TAG, "onDestroy: 拨号页面销毁完成");
    }

    // ====================== 权限回调函数区 ======================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.d(TAG, "onRequestPermissionsResult: 权限请求回调，requestCode=" + requestCode);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtils.d(TAG, "onRequestPermissionsResult: 拨打电话权限授予成功");
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                dialPhoneNumber(phoneNumber);
            } else {
                LogUtils.w(TAG, "onRequestPermissionsResult: 拨打电话权限被拒绝");
                Toast.makeText(this, "未授予拨打电话权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ====================== 控件初始化函数区 ======================
    private void initViews() {
        LogUtils.d(TAG, "initViews: 初始化UI控件");
        // Java7 适配：添加强制类型转换
        phoneNumberEditText = (EditText) findViewById(R.id.phone_number);
        dialButton = (Button) findViewById(R.id.dial_button);
        callStatusTextView = (TextView) findViewById(R.id.call_status);

        // 设置拨号按钮点击事件
        dialButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String phoneNumber = phoneNumberEditText.getText().toString().trim();
					LogUtils.d(TAG, "initViews: 拨号按钮点击，号码=" + phoneNumber);
					if (phoneNumber.isEmpty()) {
						Toast.makeText(CallActivity.this, "请输入电话号码", Toast.LENGTH_SHORT).show();
						return;
					}

					// 权限检查
					if (ContextCompat.checkSelfPermission(CallActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
						LogUtils.w(TAG, "initViews: 拨打电话权限未授予，发起权限申请");
						ActivityCompat.requestPermissions(CallActivity.this,
														  new String[]{Manifest.permission.CALL_PHONE},
														  REQUEST_CALL_PHONE);
					} else {
						dialPhoneNumber(phoneNumber);
					}
				}
			});
    }

    // ====================== 电话状态监听初始化函数区 ======================
    private void initPhoneStateListener() {
        LogUtils.d(TAG, "initPhoneStateListener: 初始化电话状态监听");
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    // ====================== 核心业务函数区 ======================
    private void dialPhoneNumber(String phoneNumber) {
        LogUtils.d(TAG, "dialPhoneNumber: 发起拨号，号码=" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.e(TAG, "dialPhoneNumber: 拨打电话权限缺失，拨号失败");
            return;
        }
        startActivity(intent);
    }

    // ====================== 内部电话状态监听类 ======================
    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    callStatusTextView.setText("电话已挂断");
                    LogUtils.d(TAG, "MyPhoneStateListener: 通话状态-挂断");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    callStatusTextView.setText("正在通话中");
                    LogUtils.d(TAG, "MyPhoneStateListener: 通话状态-通话中");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    callStatusTextView.setText("来电: " + incomingNumber);
                    LogUtils.d(TAG, "MyPhoneStateListener: 通话状态-来电，号码=" + incomingNumber);
                    break;
            }
        }
    }
}

