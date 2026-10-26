package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChain;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.fragments.WWWBrowserFragment;
import cc.winboll.studio.winboll.utils.CertTrustUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/05 18:48
 * @Describe Settings Activity
 */
public class SettingsActivity extends BaseWinBoLLActivity {

	@Override
	public Activity getActivity() {
		return this;
	}


    public static final String TAG = "SettingsActivity";

    /** 选择证书文件 */
    private static final int REQ_PICK_CERT = 0x1001;
    /** 调起系统证书安装 */
    private static final int REQ_INSTALL_CERT = 0x1002;

	@Override
	public String getTag() {
		return TAG;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

		// 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(TAG);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // 点击导航栏返回按钮，触发 finish()
                }
            });

        // 安装证书：从设备存储选择 CA 根证书，再调系统 KeyChain 安装（不内置证书）
        Button btnInstallCert = findViewById(R.id.btn_install_cert);
        btnInstallCert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCertPicker();
            }
        });

        // WWWBrowser 自定义首页：读取已保存值回显，保存/恢复默认
        final EditText etHomeUrl = (EditText) findViewById(R.id.et_home_url);
        etHomeUrl.setText(getSharedPreferences(WWWBrowserFragment.PREFS_NAME, MODE_PRIVATE)
                .getString(WWWBrowserFragment.KEY_HOME_URL, WWWBrowserFragment.DEFAULT_HOME_URL));

        Button btnSaveHomeUrl = (Button) findViewById(R.id.btn_save_home_url);
        btnSaveHomeUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etHomeUrl.getText().toString().trim();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(SettingsActivity.this, R.string.toast_home_url_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    Toast.makeText(SettingsActivity.this, R.string.toast_home_url_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
                getSharedPreferences(WWWBrowserFragment.PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(WWWBrowserFragment.KEY_HOME_URL, url)
                        .apply();
                Toast.makeText(SettingsActivity.this, R.string.toast_home_url_saved, Toast.LENGTH_SHORT).show();
            }
        });

        Button btnResetHomeUrl = (Button) findViewById(R.id.btn_reset_home_url);
        btnResetHomeUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(WWWBrowserFragment.PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .remove(WWWBrowserFragment.KEY_HOME_URL)
                        .apply();
                etHomeUrl.setText(WWWBrowserFragment.DEFAULT_HOME_URL);
                Toast.makeText(SettingsActivity.this, R.string.toast_home_url_reset, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 打开系统文件选择器，让用户选择 CA 证书文件（.crt/.cer/.pem）。
     * MIME 用 *&#47;* 以保证文件管理器能列出证书文件。
     */
    private void openCertPicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQ_PICK_CERT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.toast_cert_picker_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_CERT) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    byte[] der = CertTrustUtils.readCertificateDer(this, uri);
                    launchSystemInstall(der);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.toast_cert_read_failed, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.toast_cert_canceled, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQ_INSTALL_CERT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.toast_cert_installed, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.toast_cert_install_not_finished, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 调起系统 KeyChain 证书安装界面，EXTRA_CERTIFICATE 必须为 DER 编码。
     */
    private void launchSystemInstall(byte[] der) {
        try {
            Intent intent = KeyChain.createInstallIntent();
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, der);
            intent.putExtra(KeyChain.EXTRA_NAME, CertTrustUtils.WINBOLL_CERT_ALIAS);
            startActivityForResult(intent, REQ_INSTALL_CERT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.toast_cert_install_failed, Toast.LENGTH_LONG).show();
        }
    }

}
