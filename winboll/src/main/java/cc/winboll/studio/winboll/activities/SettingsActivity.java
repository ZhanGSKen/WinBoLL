package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChain;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.fragments.WWWBrowserFragment;
import cc.winboll.studio.winboll.media.MidiBridge;
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

        // MIDI hosted playback switch: persist immediately; turning it off
        // while a hosted session is active hands playback back to the web
        // player at once (no takeover is accepted while disabled).
        Switch swHostedPlayback = (Switch) findViewById(R.id.switch_hosted_playback);
        swHostedPlayback.setChecked(MidiBridge.isHostingEnabled(this));
        swHostedPlayback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MidiBridge.setHostingEnabled(SettingsActivity.this, isChecked);
                if (!isChecked) {
                    MidiBridge.cancelHostingForSetting();
                }
                Toast.makeText(SettingsActivity.this,
                        isChecked ? R.string.toast_hosted_playback_enabled
                                : R.string.toast_hosted_playback_disabled,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Model 2 gain presets (+6/+10/+14 dB). Persisted in millibels; the
        // native player reads it back from the effect and logs the effective
        // value, because devices clamp the request silently.
        final RadioGroup rgPlaybackGain = (RadioGroup) findViewById(R.id.rg_hosted_playback_gain);
        int gainMb = MidiBridge.getPlaybackGainMb(this);
        rgPlaybackGain.check(gainMb == MidiBridge.GAIN_PRESET_6_DB ? R.id.rb_gain_6
                : gainMb == MidiBridge.GAIN_PRESET_14_DB ? R.id.rb_gain_14
                : R.id.rb_gain_10);
        rgPlaybackGain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int gain = checkedId == R.id.rb_gain_6 ? MidiBridge.GAIN_PRESET_6_DB
                        : checkedId == R.id.rb_gain_14 ? MidiBridge.GAIN_PRESET_14_DB
                        : MidiBridge.GAIN_PRESET_10_DB;
                MidiBridge.setPlaybackGainMb(SettingsActivity.this, gain);
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.toast_playback_gain, gain / 100),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Model 3 (DynamicsProcessing) presets: post gain, compressor
        // threshold/ratio and limiter threshold. Timings stay fixed in the
        // player; the values are persisted in millibels / ratio x 10.
        final RadioGroup rgDynGain = (RadioGroup) findViewById(R.id.rg_dynamic_gain);
        final RadioGroup rgDynComp = (RadioGroup) findViewById(R.id.rg_dynamic_comp);
        final RadioGroup rgDynRatio = (RadioGroup) findViewById(R.id.rg_dynamic_ratio);
        final RadioGroup rgDynLimit = (RadioGroup) findViewById(R.id.rg_dynamic_limit);

        int dynGainMb = MidiBridge.getDynamicGainMb(this);
        rgDynGain.check(dynGainMb == MidiBridge.DYNAMIC_GAIN_6_DB ? R.id.rb_dyn_gain_6
                : dynGainMb == MidiBridge.DYNAMIC_GAIN_14_DB ? R.id.rb_dyn_gain_14
                : R.id.rb_dyn_gain_10);
        rgDynGain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int gain = checkedId == R.id.rb_dyn_gain_6 ? MidiBridge.DYNAMIC_GAIN_6_DB
                        : checkedId == R.id.rb_dyn_gain_14 ? MidiBridge.DYNAMIC_GAIN_14_DB
                        : MidiBridge.DYNAMIC_GAIN_10_DB;
                MidiBridge.setDynamicGainMb(SettingsActivity.this, gain);
                showDynamicSavedToast();
            }
        });

        int dynCompMb = MidiBridge.getDynamicCompThresholdMb(this);
        rgDynComp.check(dynCompMb == MidiBridge.DYNAMIC_COMP_THRESHOLD_10_DB ? R.id.rb_dyn_comp_10
                : dynCompMb == MidiBridge.DYNAMIC_COMP_THRESHOLD_30_DB ? R.id.rb_dyn_comp_30
                : R.id.rb_dyn_comp_20);
        rgDynComp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int threshold = checkedId == R.id.rb_dyn_comp_10
                        ? MidiBridge.DYNAMIC_COMP_THRESHOLD_10_DB
                        : checkedId == R.id.rb_dyn_comp_30
                        ? MidiBridge.DYNAMIC_COMP_THRESHOLD_30_DB
                        : MidiBridge.DYNAMIC_COMP_THRESHOLD_20_DB;
                MidiBridge.setDynamicCompThresholdMb(SettingsActivity.this, threshold);
                showDynamicSavedToast();
            }
        });

        int dynRatio = MidiBridge.getDynamicCompRatioX10(this);
        rgDynRatio.check(dynRatio == MidiBridge.DYNAMIC_COMP_RATIO_15 ? R.id.rb_dyn_ratio_15
                : dynRatio == MidiBridge.DYNAMIC_COMP_RATIO_30 ? R.id.rb_dyn_ratio_30
                : R.id.rb_dyn_ratio_20);
        rgDynRatio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int ratio = checkedId == R.id.rb_dyn_ratio_15 ? MidiBridge.DYNAMIC_COMP_RATIO_15
                        : checkedId == R.id.rb_dyn_ratio_30 ? MidiBridge.DYNAMIC_COMP_RATIO_30
                        : MidiBridge.DYNAMIC_COMP_RATIO_20;
                MidiBridge.setDynamicCompRatioX10(SettingsActivity.this, ratio);
                showDynamicSavedToast();
            }
        });

        int dynLimitMb = MidiBridge.getDynamicLimiterThresholdMb(this);
        rgDynLimit.check(dynLimitMb == MidiBridge.DYNAMIC_LIMIT_1_DB ? R.id.rb_dyn_limit_1
                : dynLimitMb == MidiBridge.DYNAMIC_LIMIT_6_DB ? R.id.rb_dyn_limit_6
                : R.id.rb_dyn_limit_3);
        rgDynLimit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int limit = checkedId == R.id.rb_dyn_limit_1 ? MidiBridge.DYNAMIC_LIMIT_1_DB
                        : checkedId == R.id.rb_dyn_limit_6 ? MidiBridge.DYNAMIC_LIMIT_6_DB
                        : MidiBridge.DYNAMIC_LIMIT_3_DB;
                MidiBridge.setDynamicLimiterThresholdMb(SettingsActivity.this, limit);
                showDynamicSavedToast();
            }
        });

        // Hosted playback model selector: model 1 keeps the framework synth
        // output as-is, model 2 adds a LoudnessEnhancer gain, model 3 adds a
        // DynamicsProcessing (compressor + limiter). Persisted at once; applied
        // when the next track is prepared (current playback is left untouched).
        RadioGroup rgPlaybackModel = (RadioGroup) findViewById(R.id.rg_hosted_playback_model);
        int playbackModel = MidiBridge.getPlaybackModel(this);
        rgPlaybackModel.check(playbackModel == MidiBridge.PLAYBACK_MODEL_ENHANCED
                ? R.id.rb_model_enhanced
                : playbackModel == MidiBridge.PLAYBACK_MODEL_DYNAMIC
                ? R.id.rb_model_dynamic
                : R.id.rb_model_classic);
        applyModelGroupState(playbackModel == MidiBridge.PLAYBACK_MODEL_ENHANCED, rgPlaybackGain);
        applyModelGroupState(playbackModel == MidiBridge.PLAYBACK_MODEL_DYNAMIC,
                rgDynGain, rgDynComp, rgDynRatio, rgDynLimit);
        rgPlaybackModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int model = checkedId == R.id.rb_model_enhanced
                        ? MidiBridge.PLAYBACK_MODEL_ENHANCED
                        : checkedId == R.id.rb_model_dynamic
                        ? MidiBridge.PLAYBACK_MODEL_DYNAMIC
                        : MidiBridge.PLAYBACK_MODEL_CLASSIC;
                MidiBridge.setPlaybackModel(SettingsActivity.this, model);
                applyModelGroupState(model == MidiBridge.PLAYBACK_MODEL_ENHANCED, rgPlaybackGain);
                applyModelGroupState(model == MidiBridge.PLAYBACK_MODEL_DYNAMIC,
                        rgDynGain, rgDynComp, rgDynRatio, rgDynLimit);
                int toastRes = model == MidiBridge.PLAYBACK_MODEL_ENHANCED
                        ? R.string.toast_playback_model_enhanced
                        : model == MidiBridge.PLAYBACK_MODEL_DYNAMIC
                        ? R.string.toast_playback_model_dynamic
                        : R.string.toast_playback_model_classic;
                Toast.makeText(SettingsActivity.this, toastRes, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Model 3 parameter change feedback (applies to the next prepared track). */
    private void showDynamicSavedToast() {
        Toast.makeText(SettingsActivity.this, R.string.toast_dynamic_param, Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Parameter groups belong to one model only, so they are greyed out and
     * not clickable unless that model is selected.
     */
    private void applyModelGroupState(boolean enabled, RadioGroup... groups) {
        for (int g = 0; g < groups.length; g++) {
            RadioGroup group = groups[g];
            if (group == null) {
                continue;
            }
            group.setEnabled(enabled);
            group.setAlpha(enabled ? 1.0f : 0.5f);
            for (int i = 0; i < group.getChildCount(); i++) {
                group.getChildAt(i).setEnabled(enabled);
            }
        }
    }

    /**
     * 打开系统文件选择器，让用户选择 CA 证书文件（.crt/.cer/.pem）。
     * MIME 用 *&#47;* 以保证文件管理器能列出证书文件。
     */
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
