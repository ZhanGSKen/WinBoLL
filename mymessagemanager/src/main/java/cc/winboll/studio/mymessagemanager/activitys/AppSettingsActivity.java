package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/12 20:03:42
 * @Describe 应用设置窗口
 */
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.PermissionUtil;

public class AppSettingsActivity extends BaseActivity {

    public static final String TAG = "AppSettingsActivity";

    AppConfigUtil mAppConfigUtil;
    AToolbar mAToolbar;
    AOHPCTCSeekBar mAOHPCTCSeekBar;
    EditText metTTSPlayDelayTimes;
    EditText metPhoneMergePrefix;
    Switch mswMergePrefixPhone;
    Switch mswSMSRecycleProtectMode;
    EditText metProtectModerRefuseChars;
    EditText metProtectModerReplaceChars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appsettings);

        // 初始化属性
        mAppConfigUtil = AppConfigUtil.getInstance(this);
        int nTtsPlayDelayTimes = mAppConfigUtil.mAppConfigBean.getTtsPlayDelayTimes();
        metTTSPlayDelayTimes = findViewById(R.id.activityappsettingsEditText1);
        metTTSPlayDelayTimes.setText(Integer.toString(nTtsPlayDelayTimes / 1000));

        // 初始化标题栏
        mAToolbar = findViewById(R.id.activityappsettingsAToolbar1);
        mAToolbar.setSubtitle(getString(R.string.activity_name_appsettings));
        setActionBar(mAToolbar);

        metPhoneMergePrefix = findViewById(R.id.activityappsettingsEditText2);
        metPhoneMergePrefix.setText(mAppConfigUtil.mAppConfigBean.getCountryCode());

        mswMergePrefixPhone = findViewById(R.id.activityappsettingsSwitch1);
        mswMergePrefixPhone.setChecked(mAppConfigUtil.mAppConfigBean.isMergeCountryCodePrefix());

        mswSMSRecycleProtectMode = findViewById(R.id.activityappsettingsSwitch3);
        mswSMSRecycleProtectMode.setChecked(mAppConfigUtil.mAppConfigBean.isSMSRecycleProtectMode());
        
        metProtectModerRefuseChars = findViewById(R.id.activityappsettingsEditText3);
        metProtectModerRefuseChars.setText(mAppConfigUtil.mAppConfigBean.getProtectModerRefuseChars());
        
        metProtectModerReplaceChars = findViewById(R.id.activityappsettingsEditText4);
        metProtectModerReplaceChars.setText(mAppConfigUtil.mAppConfigBean.getProtectModerReplaceChars());
        
        mAOHPCTCSeekBar = findViewById(R.id.activityappsettingsAOHPCTCSeekBar1);
        mAOHPCTCSeekBar.setThumb(getDrawable(R.drawable.cursor_pointer));
        mAOHPCTCSeekBar.setThumbOffset(0);
        mAOHPCTCSeekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener(){

                @Override
                public void onOHPCommit() {
                    mAppConfigUtil.reLoadConfig();
                    mAppConfigUtil.mAppConfigBean.setIsSMSRecycleProtectMode(mswSMSRecycleProtectMode.isChecked());
                    mAppConfigUtil.mAppConfigBean.setProtectModerRefuseChars(metProtectModerRefuseChars.getText().toString());
                    mAppConfigUtil.mAppConfigBean.setProtectModerReplaceChars(metProtectModerReplaceChars.getText().toString());
                    mAppConfigUtil.mAppConfigBean.setCountryCode(metPhoneMergePrefix.getText().toString());
                    mAppConfigUtil.mAppConfigBean.setIsMergeCountryCodePrefix(mswMergePrefixPhone.isChecked());
                    int nTtsPlayDelayTimes = 1000 * Integer.parseInt(metTTSPlayDelayTimes.getText().toString());
                    mAppConfigUtil.mAppConfigBean.setTtsPlayDelayTimes(nTtsPlayDelayTimes);
                    mAppConfigUtil.saveConfig();
                    Toast.makeText(getApplication(), "App config data is saved.", Toast.LENGTH_SHORT).show();
                    //LogUtils.d(TAG, "TTS Play Delay Times is setting to : " + Integer.toString(mAppConfigData.getTtsPlayDelayTimes()));Toast.makeText(getApplication(), "onOHPCommit", Toast.LENGTH_SHORT).show();
                }
            });
    };

    public void onOpenSystemDefaultAppSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
        startActivity(intent);
    }

    public void onCheckAndGetAppPermission(View view) {
        //LogUtils.d(TAG, "onCheckAndGetAppPermission");
        if (PermissionUtil.checkAndGetAppPermission(this)) {
            Toast.makeText(getApplication(), "应用已获得所需权限。", Toast.LENGTH_SHORT).show();
        }
    }
}
