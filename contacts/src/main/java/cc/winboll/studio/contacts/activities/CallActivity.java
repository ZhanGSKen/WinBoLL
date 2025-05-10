package cc.winboll.studio.contacts.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/20 17:15:46
 * @Describe 拨号窗口
 */
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
import cc.winboll.studio.contacts.MainActivity;
import cc.winboll.studio.contacts.R;

public class CallActivity extends AppCompatActivity {
    public static final String TAG = "CallActivity";

    private static final int REQUEST_CALL_PHONE = 1;
    private EditText phoneNumberEditText;
    private TextView callStatusTextView;
    private TelephonyManager telephonyManager;
    private MyPhoneStateListener phoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_call);

        phoneNumberEditText = findViewById(R.id.phone_number);
        Button dialButton = findViewById(R.id.dial_button);
        callStatusTextView = findViewById(R.id.call_status);

        dialButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = phoneNumberEditText.getText().toString().trim();
                    if (!phoneNumber.isEmpty()) {
                        if (ContextCompat.checkSelfPermission(CallActivity.this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CallActivity.this,
                                                              new String[]{Manifest.permission.CALL_PHONE},
                                                              REQUEST_CALL_PHONE);
                        } else {
                            dialPhoneNumber(phoneNumber);
                        }
                    } else {
                        Toast.makeText(CallActivity.this, "请输入电话号码", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        // 初始化TelephonyManager和PhoneStateListener
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                dialPhoneNumber(phoneNumber);
            } else {
                Toast.makeText(this, "未授予拨打电话权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    callStatusTextView.setText("电话已挂断");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    callStatusTextView.setText("正在通话中");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    callStatusTextView.setText("来电: " + incomingNumber);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消监听
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}

