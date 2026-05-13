package cc.winboll.studio.contacts.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/20 20:18:26
 * @Describe 拨号盘窗口（跳转到系统拨号界面）
 */
public class DialerActivity extends AppCompatActivity {

    // ====================== 常量定义区 ======================
    public static final String TAG = "DialerActivity";

    // ====================== UI控件区 ======================
    private EditText phoneNumberEditText;
    private Button dialButton;

    // ====================== 生命周期函数区 ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: 拨号盘页面开始创建");
        setContentView(R.layout.activity_dialer);

        // 初始化UI控件与点击事件
        initViews();
        LogUtils.d(TAG, "onCreate: 拨号盘页面初始化完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 拨号盘页面已销毁");
    }

    // ====================== 控件初始化函数区 ======================
    private void initViews() {
        LogUtils.d(TAG, "initViews: 初始化UI控件");
        // Java7 适配：添加强制类型转换
        phoneNumberEditText = (EditText) findViewById(R.id.phone_number_edit_text);
        dialButton = (Button) findViewById(R.id.dial_button);

        // 设置拨号按钮点击事件
        dialButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String phoneNumber = phoneNumberEditText.getText().toString().trim();
					LogUtils.d(TAG, "initViews: 拨号按钮点击，输入号码=" + phoneNumber);

					// 空号码校验
					if (phoneNumber.isEmpty()) {
						LogUtils.w(TAG, "initViews: 拨号失败，号码为空");
						Toast.makeText(DialerActivity.this, "请输入有效电话号码", Toast.LENGTH_SHORT).show();
						return;
					}

					// 跳转到系统拨号界面
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
					if (intent.resolveActivity(getPackageManager()) != null) {
						startActivity(intent);
						LogUtils.d(TAG, "initViews: 成功跳转到系统拨号界面");
					} else {
						LogUtils.e(TAG, "initViews: 跳转失败，无可用拨号应用");
						Toast.makeText(DialerActivity.this, "未找到可用拨号应用", Toast.LENGTH_SHORT).show();
					}
				}
			});
    }
}

