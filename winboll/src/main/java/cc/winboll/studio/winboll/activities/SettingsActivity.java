package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.security.KeyChain;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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

    /** Collapsible panel arrow glyphs: right-pointing closed, down-pointing open. */
    private static final String PANEL_ARROW_COLLAPSED = "\u25B8";
    private static final String PANEL_ARROW_EXPANDED = "\u25BE";

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

        // Model 2 gain slider: 0..+20 dB in 1 dB steps. Persisted in millibels;
        // the native player reads the gain back from the effect and logs the
        // effective value, because devices clamp the request silently.
        final SeekBar sbGain = (SeekBar) findViewById(R.id.sb_gain);
        bindSeekBar(sbGain, R.id.tv_gain_value, MidiBridge.getPlaybackGainMb(this),
                MidiBridge.PLAYBACK_GAIN_MB_MIN, MidiBridge.PLAYBACK_GAIN_MB_MAX,
                MidiBridge.PLAYBACK_GAIN_MB_STEP, SLIDER_DB_PLUS,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setPlaybackGainMb(SettingsActivity.this, value);
                        MidiBridge.reapplyPlaybackSettings();
                        Toast.makeText(SettingsActivity.this,
                                getString(R.string.toast_playback_gain, value / 100),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Model 3 (DynamicsProcessing) sliders: post gain, compressor
        // threshold/ratio and limiter threshold. Timings stay fixed in the
        // player; the values are persisted in millibels / ratio x 10.
        final SeekBar sbDynGain = (SeekBar) findViewById(R.id.sb_dyn_gain);
        bindSeekBar(sbDynGain, R.id.tv_dyn_gain_value, MidiBridge.getDynamicGainMb(this),
                MidiBridge.DYNAMIC_GAIN_MB_MIN, MidiBridge.DYNAMIC_GAIN_MB_MAX,
                MidiBridge.DYNAMIC_GAIN_MB_STEP, SLIDER_DB_PLUS,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setDynamicGainMb(SettingsActivity.this, value);
                        showDynamicSavedToast();
                    }
                });

        final SeekBar sbDynComp = (SeekBar) findViewById(R.id.sb_dyn_comp);
        bindSeekBar(sbDynComp, R.id.tv_dyn_comp_value,
                MidiBridge.getDynamicCompThresholdMb(this),
                MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_MIN, MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_MAX,
                MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_STEP, SLIDER_DB_SIGNED,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setDynamicCompThresholdMb(SettingsActivity.this, value);
                        showDynamicSavedToast();
                    }
                });

        final SeekBar sbDynRatio = (SeekBar) findViewById(R.id.sb_dyn_ratio);
        bindSeekBar(sbDynRatio, R.id.tv_dyn_ratio_value,
                MidiBridge.getDynamicCompRatioX10(this),
                MidiBridge.DYNAMIC_COMP_RATIO_X10_MIN, MidiBridge.DYNAMIC_COMP_RATIO_X10_MAX,
                MidiBridge.DYNAMIC_COMP_RATIO_X10_STEP, SLIDER_RATIO,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setDynamicCompRatioX10(SettingsActivity.this, value);
                        showDynamicSavedToast();
                    }
                });

        final SeekBar sbDynLimit = (SeekBar) findViewById(R.id.sb_dyn_limit);
        bindSeekBar(sbDynLimit, R.id.tv_dyn_limit_value,
                MidiBridge.getDynamicLimiterThresholdMb(this),
                MidiBridge.DYNAMIC_LIMIT_MB_MIN, MidiBridge.DYNAMIC_LIMIT_MB_MAX,
                MidiBridge.DYNAMIC_LIMIT_MB_STEP, SLIDER_DB_SIGNED,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setDynamicLimiterThresholdMb(SettingsActivity.this, value);
                        showDynamicSavedToast();
                    }
                });

        // Per-model reset buttons: model 1 has no parameters, model 2 restores
        // the +10 dB default, model 3 restores +10 / -20 dB @ 2:1 / -3 dB.
        final Button btnResetClassic = (Button) findViewById(R.id.btn_reset_classic);
        final Button btnResetGain = (Button) findViewById(R.id.btn_reset_gain);
        final Button btnResetDynamic = (Button) findViewById(R.id.btn_reset_dynamic);
        if (btnResetClassic != null) {
            btnResetClassic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SettingsActivity.this, R.string.toast_reset_classic,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnResetGain != null) {
            btnResetGain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MidiBridge.setPlaybackGainMb(SettingsActivity.this,
                            MidiBridge.PLAYBACK_GAIN_MB_DEFAULT);
                    applySliderValue(sbGain, (TextView) findViewById(R.id.tv_gain_value),
                            MidiBridge.PLAYBACK_GAIN_MB_DEFAULT, MidiBridge.PLAYBACK_GAIN_MB_MIN,
                            MidiBridge.PLAYBACK_GAIN_MB_STEP, SLIDER_DB_PLUS);
                    MidiBridge.reapplyPlaybackSettings();
                    Toast.makeText(SettingsActivity.this, R.string.toast_reset_gain,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnResetDynamic != null) {
            btnResetDynamic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MidiBridge.setDynamicGainMb(SettingsActivity.this,
                            MidiBridge.DYNAMIC_GAIN_MB_DEFAULT);
                    MidiBridge.setDynamicCompThresholdMb(SettingsActivity.this,
                            MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_DEFAULT);
                    MidiBridge.setDynamicCompRatioX10(SettingsActivity.this,
                            MidiBridge.DYNAMIC_COMP_RATIO_DEFAULT);
                    MidiBridge.setDynamicLimiterThresholdMb(SettingsActivity.this,
                            MidiBridge.DYNAMIC_LIMIT_MB_DEFAULT);
                    applySliderValue(sbDynGain, (TextView) findViewById(R.id.tv_dyn_gain_value),
                            MidiBridge.DYNAMIC_GAIN_MB_DEFAULT, MidiBridge.DYNAMIC_GAIN_MB_MIN,
                            MidiBridge.DYNAMIC_GAIN_MB_STEP, SLIDER_DB_PLUS);
                    applySliderValue(sbDynComp, (TextView) findViewById(R.id.tv_dyn_comp_value),
                            MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_DEFAULT,
                            MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_MIN,
                            MidiBridge.DYNAMIC_COMP_THRESHOLD_MB_STEP, SLIDER_DB_SIGNED);
                    applySliderValue(sbDynRatio, (TextView) findViewById(R.id.tv_dyn_ratio_value),
                            MidiBridge.DYNAMIC_COMP_RATIO_DEFAULT,
                            MidiBridge.DYNAMIC_COMP_RATIO_X10_MIN,
                            MidiBridge.DYNAMIC_COMP_RATIO_X10_STEP, SLIDER_RATIO);
                    applySliderValue(sbDynLimit, (TextView) findViewById(R.id.tv_dyn_limit_value),
                            MidiBridge.DYNAMIC_LIMIT_MB_DEFAULT,
                            MidiBridge.DYNAMIC_LIMIT_MB_MIN, MidiBridge.DYNAMIC_LIMIT_MB_STEP,
                            SLIDER_DB_SIGNED);
                    MidiBridge.reapplyPlaybackSettings();
                    Toast.makeText(SettingsActivity.this, R.string.toast_reset_dynamic,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Hosted playback model selector: model 1 keeps the framework synth
        // output as-is, model 2 adds a LoudnessEnhancer gain, model 3 adds a
        // DynamicsProcessing (compressor + limiter). Persisted at once and
        // re-applied to the running track so the switch is audible immediately.
        RadioGroup rgPlaybackModel = (RadioGroup) findViewById(R.id.rg_hosted_playback_model);
        int playbackModel = MidiBridge.getPlaybackModel(this);
        rgPlaybackModel.check(playbackModel == MidiBridge.PLAYBACK_MODEL_ENHANCED
                ? R.id.rb_model_enhanced
                : playbackModel == MidiBridge.PLAYBACK_MODEL_DYNAMIC
                ? R.id.rb_model_dynamic
                : R.id.rb_model_classic);
        applyModelGroupState(playbackModel == MidiBridge.PLAYBACK_MODEL_ENHANCED, sbGain,
                btnResetGain);
        applyModelGroupState(playbackModel == MidiBridge.PLAYBACK_MODEL_DYNAMIC,
                sbDynGain, sbDynComp, sbDynRatio, sbDynLimit, btnResetDynamic);
        rgPlaybackModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int model = checkedId == R.id.rb_model_enhanced
                        ? MidiBridge.PLAYBACK_MODEL_ENHANCED
                        : checkedId == R.id.rb_model_dynamic
                        ? MidiBridge.PLAYBACK_MODEL_DYNAMIC
                        : MidiBridge.PLAYBACK_MODEL_CLASSIC;
                MidiBridge.setPlaybackModel(SettingsActivity.this, model);
                MidiBridge.reapplyPlaybackSettings();
                applyModelGroupState(model == MidiBridge.PLAYBACK_MODEL_ENHANCED, sbGain,
                        btnResetGain);
                applyModelGroupState(model == MidiBridge.PLAYBACK_MODEL_DYNAMIC,
                        sbDynGain, sbDynComp, sbDynRatio, sbDynLimit, btnResetDynamic);
                int toastRes = model == MidiBridge.PLAYBACK_MODEL_ENHANCED
                        ? R.string.toast_playback_model_enhanced
                        : model == MidiBridge.PLAYBACK_MODEL_DYNAMIC
                        ? R.string.toast_playback_model_dynamic
                        : R.string.toast_playback_model_classic;
                Toast.makeText(SettingsActivity.this, toastRes, Toast.LENGTH_SHORT).show();
            }
        });

        setupReverbAndPitch();
        setupPlaybackPanels();
    }

    /**
     * Reverb and pitch panels. Both stages live on the hosted audio session
     * next to the playback model, and both apply in place: the values are
     * pushed onto the running track at once, without rebuilding the effect.
     */
    private void setupReverbAndPitch() {
        final SeekBar sbReverbPreset = (SeekBar) findViewById(R.id.sb_reverb_preset);
        final SeekBar sbReverbDecay = (SeekBar) findViewById(R.id.sb_reverb_decay);
        final SeekBar sbReverbLevel = (SeekBar) findViewById(R.id.sb_reverb_level);
        final SeekBar sbReverbReflections = (SeekBar) findViewById(R.id.sb_reverb_reflections);
        final Button btnResetReverb = (Button) findViewById(R.id.btn_reset_reverb);
        final TextView tvReverbDecay = (TextView) findViewById(R.id.tv_reverb_decay_value);
        final TextView tvReverbLevel = (TextView) findViewById(R.id.tv_reverb_level_value);
        final TextView tvReverbReflections =
                (TextView) findViewById(R.id.tv_reverb_reflections_value);

        // The preset slider is a shortcut: picking a scene rewrites the three
        // adjustable parameters, which stay the actual source of truth.
        bindLiveSeekBar(sbReverbPreset, R.id.tv_reverb_preset_value,
                MidiBridge.getReverbPreset(this),
                MidiBridge.REVERB_PRESET_MIN, MidiBridge.REVERB_PRESET_MAX,
                MidiBridge.REVERB_PRESET_STEP, SLIDER_PRESET,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        applyReverbPreset(value, true);
                    }
                },
                new LiveSink() {
                    @Override
                    public void apply(int value) {
                        applyReverbPreset(value, false);
                    }
                });

        bindLiveSeekBar(sbReverbDecay, R.id.tv_reverb_decay_value,
                MidiBridge.getReverbDecayMs(this),
                MidiBridge.REVERB_DECAY_MS_MIN, MidiBridge.REVERB_DECAY_MS_MAX,
                MidiBridge.REVERB_DECAY_MS_STEP, SLIDER_MS,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setReverbDecayMs(SettingsActivity.this, value);
                        persistReverbLive();
                    }
                },
                new LiveSink() {
                    @Override
                    public void apply(int value) {
                        MidiBridge.setReverbDecayMs(SettingsActivity.this, value);
                        MidiBridge.reapplyEffectsOnly();
                    }
                });

        bindLiveSeekBar(sbReverbLevel, R.id.tv_reverb_level_value,
                MidiBridge.getReverbLevelMb(this),
                MidiBridge.REVERB_LEVEL_MB_MIN, MidiBridge.REVERB_LEVEL_MB_MAX,
                MidiBridge.REVERB_LEVEL_MB_STEP, SLIDER_DB_SIGNED,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setReverbLevelMb(SettingsActivity.this, value);
                        persistReverbLive();
                    }
                },
                new LiveSink() {
                    @Override
                    public void apply(int value) {
                        MidiBridge.setReverbLevelMb(SettingsActivity.this, value);
                        MidiBridge.reapplyEffectsOnly();
                    }
                });

        bindLiveSeekBar(sbReverbReflections, R.id.tv_reverb_reflections_value,
                MidiBridge.getReverbReflectionsMb(this),
                MidiBridge.REVERB_REFLECTIONS_MB_MIN, MidiBridge.REVERB_REFLECTIONS_MB_MAX,
                MidiBridge.REVERB_REFLECTIONS_MB_STEP, SLIDER_DB_SIGNED,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setReverbReflectionsMb(SettingsActivity.this, value);
                        persistReverbLive();
                    }
                },
                new LiveSink() {
                    @Override
                    public void apply(int value) {
                        MidiBridge.setReverbReflectionsMb(SettingsActivity.this, value);
                        MidiBridge.reapplyEffectsOnly();
                    }
                });

        applyModelGroupState(MidiBridge.getReverbPreset(this) > 0, sbReverbDecay,
                sbReverbLevel, sbReverbReflections);

        if (btnResetReverb != null) {
            btnResetReverb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MidiBridge.setReverbPreset(SettingsActivity.this,
                            MidiBridge.REVERB_PRESET_DEFAULT);
                    MidiBridge.setReverbDecayMs(SettingsActivity.this,
                            MidiBridge.REVERB_DECAY_MS_DEFAULT);
                    MidiBridge.setReverbLevelMb(SettingsActivity.this,
                            MidiBridge.REVERB_LEVEL_MB_DEFAULT);
                    MidiBridge.setReverbReflectionsMb(SettingsActivity.this,
                            MidiBridge.REVERB_REFLECTIONS_MB_DEFAULT);
                    applySliderValue(sbReverbPreset,
                            (TextView) findViewById(R.id.tv_reverb_preset_value),
                            MidiBridge.REVERB_PRESET_DEFAULT, MidiBridge.REVERB_PRESET_MIN,
                            MidiBridge.REVERB_PRESET_STEP, SLIDER_PRESET);
                    applySliderValue(sbReverbDecay, tvReverbDecay,
                            MidiBridge.REVERB_DECAY_MS_DEFAULT, MidiBridge.REVERB_DECAY_MS_MIN,
                            MidiBridge.REVERB_DECAY_MS_STEP, SLIDER_MS);
                    applySliderValue(sbReverbLevel, tvReverbLevel,
                            MidiBridge.REVERB_LEVEL_MB_DEFAULT, MidiBridge.REVERB_LEVEL_MB_MIN,
                            MidiBridge.REVERB_LEVEL_MB_STEP, SLIDER_DB_SIGNED);
                    applySliderValue(sbReverbReflections, tvReverbReflections,
                            MidiBridge.REVERB_REFLECTIONS_MB_DEFAULT,
                            MidiBridge.REVERB_REFLECTIONS_MB_MIN,
                            MidiBridge.REVERB_REFLECTIONS_MB_STEP, SLIDER_DB_SIGNED);
                    applyModelGroupState(false, sbReverbDecay, sbReverbLevel,
                            sbReverbReflections);
                    MidiBridge.reapplyEffectsOnly();
                    Toast.makeText(SettingsActivity.this, R.string.toast_reset_reverb,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        final SeekBar sbPitch = (SeekBar) findViewById(R.id.sb_pitch);
        final Button btnResetPitch = (Button) findViewById(R.id.btn_reset_pitch);
        bindLiveSeekBar(sbPitch, R.id.tv_pitch_value, MidiBridge.getPitchPercent(this),
                MidiBridge.PITCH_PERCENT_MIN, MidiBridge.PITCH_PERCENT_MAX,
                MidiBridge.PITCH_PERCENT_STEP, SLIDER_PERCENT,
                new ParamSink() {
                    @Override
                    public void persist(int value) {
                        MidiBridge.setPitchPercent(SettingsActivity.this, value);
                        MidiBridge.reapplyEffectsOnly();
                        Toast.makeText(SettingsActivity.this, R.string.toast_pitch_param,
                                Toast.LENGTH_SHORT).show();
                    }
                },
                new LiveSink() {
                    @Override
                    public void apply(int value) {
                        MidiBridge.setPitchPercent(SettingsActivity.this, value);
                        MidiBridge.reapplyEffectsOnly();
                    }
                });

        if (btnResetPitch != null) {
            btnResetPitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MidiBridge.setPitchPercent(SettingsActivity.this,
                            MidiBridge.PITCH_PERCENT_DEFAULT);
                    applySliderValue(sbPitch, (TextView) findViewById(R.id.tv_pitch_value),
                            MidiBridge.PITCH_PERCENT_DEFAULT,
                            MidiBridge.PITCH_PERCENT_MIN, MidiBridge.PITCH_PERCENT_STEP,
                            SLIDER_PERCENT);
                    MidiBridge.reapplyEffectsOnly();
                    Toast.makeText(SettingsActivity.this, R.string.toast_reset_pitch,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /** Reverb parameter feedback: apply in place on the running track. */
    private void persistReverbLive() {
        MidiBridge.reapplyEffectsOnly();
        Toast.makeText(SettingsActivity.this, R.string.toast_reverb_param, Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Preset scene write: scene -> the three adjustable parameters -> their
     * sliders -> in-place apply. Used both while dragging (silent) and on
     * release (with the confirming toast).
     */
    private void applyReverbPreset(int value, boolean announce) {
        MidiBridge.setReverbPreset(this, value);
        int[] scene = MidiBridge.reverbPresetValues(value);
        MidiBridge.setReverbDecayMs(this, scene[0]);
        MidiBridge.setReverbLevelMb(this, scene[1]);
        MidiBridge.setReverbReflectionsMb(this, scene[2]);
        SeekBar decayBar = (SeekBar) findViewById(R.id.sb_reverb_decay);
        SeekBar levelBar = (SeekBar) findViewById(R.id.sb_reverb_level);
        SeekBar reflectionsBar = (SeekBar) findViewById(R.id.sb_reverb_reflections);
        applySliderValue(decayBar, (TextView) findViewById(R.id.tv_reverb_decay_value), scene[0],
                MidiBridge.REVERB_DECAY_MS_MIN, MidiBridge.REVERB_DECAY_MS_STEP, SLIDER_MS);
        applySliderValue(levelBar, (TextView) findViewById(R.id.tv_reverb_level_value), scene[1],
                MidiBridge.REVERB_LEVEL_MB_MIN, MidiBridge.REVERB_LEVEL_MB_STEP, SLIDER_DB_SIGNED);
        applySliderValue(reflectionsBar,
                (TextView) findViewById(R.id.tv_reverb_reflections_value), scene[2],
                MidiBridge.REVERB_REFLECTIONS_MB_MIN,
                MidiBridge.REVERB_REFLECTIONS_MB_STEP, SLIDER_DB_SIGNED);
        applyModelGroupState(value > 0, decayBar, levelBar, reflectionsBar);
        MidiBridge.reapplyEffectsOnly();
        if (announce) {
            Toast.makeText(this, R.string.toast_reverb_param, Toast.LENGTH_SHORT).show();
        }
    }

    /** Slider writes its value through this once the user lifts the finger. */
    private interface ParamSink {
        void persist(int value);
    }

    /** Preview sink: pushes a value onto the running track while dragging. */
    private interface LiveSink {
        void apply(int value);
    }

    /** Minimum gap between two live applies so a drag cannot flood the audio HAL. */
    private static final long LIVE_APPLY_INTERVAL_MS = 120L;

    /**
     * Slider that is heard while being dragged: the label follows every step,
     * the live sink is fed at most once per {@link #LIVE_APPLY_INTERVAL_MS}, and
     * the preference (plus the confirming toast) is written on release.
     */
    private void bindLiveSeekBar(final SeekBar bar, int valueViewId, int value, final int min,
            int max, final int step, final int kind, final ParamSink sink,
            final LiveSink live) {
        final TextView valueView = (TextView) findViewById(valueViewId);
        if (bar == null || valueView == null) {
            return;
        }
        bar.setMax((max - min) / step);
        applySliderValue(bar, valueView, value, min, step, kind);
        final long[] lastApply = new long[] {0L};
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int settled = min + progress * step;
                valueView.setText(formatSliderValue(kind, settled));
                if (!fromUser) {
                    return;
                }
                long now = SystemClock.elapsedRealtime();
                if (now - lastApply[0] < LIVE_APPLY_INTERVAL_MS) {
                    return;
                }
                lastApply[0] = now;
                live.apply(settled);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                lastApply[0] = 0L;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sink.persist(min + seekBar.getProgress() * step);
            }
        });
    }

    /** Text formats: model 2 gain is signed-positive, thresholds signed, ratio x10. */
    private static final int SLIDER_DB_PLUS = 0;
    private static final int SLIDER_DB_SIGNED = 1;
    private static final int SLIDER_RATIO = 2;
    /** Reverb preset name from the string array. */
    private static final int SLIDER_PRESET = 3;
    /** Millisecond value (reverb decay time). */
    private static final int SLIDER_MS = 4;
    /** Percentage offset (pitch). */
    private static final int SLIDER_PERCENT = 5;

    /**
     * Wire a preset slider to its value label: the label tracks every progress
     * change, the preference is written once when the finger is lifted.
     */
    private void bindSeekBar(final SeekBar bar, int valueViewId, int value, final int min,
            int max, final int step, final int kind, final ParamSink sink) {
        final TextView valueView = (TextView) findViewById(valueViewId);
        if (bar == null || valueView == null) {
            return;
        }
        bar.setMax((max - min) / step);
        applySliderValue(bar, valueView, value, min, step, kind);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueView.setText(formatSliderValue(kind, min + progress * step));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing to do until the value settles.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int settled = min + seekBar.getProgress() * step;
                sink.persist(settled);
            }
        });
    }

    /** Move a slider (and its value label) to an absolute parameter value. */
    private void applySliderValue(SeekBar bar, TextView valueView, int value, int min, int step,
            int kind) {
        if (bar == null || valueView == null) {
            return;
        }
        bar.setProgress(Math.max(0, Math.min(bar.getMax(), (value - min) / step)));
        valueView.setText(formatSliderValue(kind, value));
    }

    /** Render a slider value for its parameter kind (millibels / ratio / preset / ms / %). */
    private String formatSliderValue(int kind, int value) {
        if (kind == SLIDER_DB_PLUS) {
            return getString(R.string.settings_gain_value_format, value / 100);
        }
        if (kind == SLIDER_DB_SIGNED) {
            return getString(R.string.settings_dynamic_db_value_format, value / 100);
        }
        if (kind == SLIDER_PRESET) {
            String[] names = getResources().getStringArray(R.array.settings_reverb_presets);
            return value >= 0 && value < names.length ? names[value] : String.valueOf(value);
        }
        if (kind == SLIDER_MS) {
            return getString(R.string.settings_ms_value_format, value);
        }
        if (kind == SLIDER_PERCENT) {
            return getString(R.string.settings_percent_value_format, value);
        }
        return getString(R.string.settings_dynamic_ratio_value_format, value / 10, value % 10);
    }

    /** The model parameter panels start collapsed; tapping a header toggles. */
    private void setupPlaybackPanels() {
        setupCollapsiblePanel(R.id.ll_hosted_playback_classic_header,
                R.id.ll_hosted_playback_classic_body, R.id.tv_hosted_playback_classic_arrow);
        setupCollapsiblePanel(R.id.ll_hosted_playback_gain_header,
                R.id.ll_hosted_playback_gain_body, R.id.tv_hosted_playback_gain_arrow);
        setupCollapsiblePanel(R.id.ll_hosted_playback_dynamic_header,
                R.id.ll_hosted_playback_dynamic_body, R.id.tv_hosted_playback_dynamic_arrow);
        setupCollapsiblePanel(R.id.ll_hosted_playback_reverb_header,
                R.id.ll_hosted_playback_reverb_body, R.id.tv_hosted_playback_reverb_arrow);
        setupCollapsiblePanel(R.id.ll_hosted_playback_pitch_header,
                R.id.ll_hosted_playback_pitch_body, R.id.tv_hosted_playback_pitch_arrow);
    }

    private void setupCollapsiblePanel(int headerId, int bodyId, int arrowId) {
        final View header = findViewById(headerId);
        final View body = findViewById(bodyId);
        final TextView arrow = (TextView) findViewById(arrowId);
        if (header == null || body == null || arrow == null) {
            return;
        }
        body.setVisibility(View.GONE);
        arrow.setText(PANEL_ARROW_COLLAPSED);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean expand = body.getVisibility() != View.VISIBLE;
                body.setVisibility(expand ? View.VISIBLE : View.GONE);
                arrow.setText(expand ? PANEL_ARROW_EXPANDED : PANEL_ARROW_COLLAPSED);
            }
        });
    }

    /** Model 3 parameter change feedback (applies to the next prepared track). */
    private void showDynamicSavedToast() {
        // Re-create the model 3 effect on the live track so the new level is
        // audible at once; nothing happens when no hosted track is loaded.
        MidiBridge.reapplyPlaybackSettings();
        Toast.makeText(SettingsActivity.this, R.string.toast_dynamic_param, Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Parameter sliders belong to one model only, so they are greyed out and
     * not draggable unless that model is selected.
     */
    private void applyModelGroupState(boolean enabled, View... controls) {
        for (int i = 0; i < controls.length; i++) {
            View control = controls[i];
            if (control == null) {
                continue;
            }
            control.setEnabled(enabled);
            control.setAlpha(enabled ? 1.0f : 0.5f);
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
