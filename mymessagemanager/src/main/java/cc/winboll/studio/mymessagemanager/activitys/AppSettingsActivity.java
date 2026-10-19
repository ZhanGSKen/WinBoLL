package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/05/12 20:03:42
 * @Describe 应用设置窗口
 */
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.dialogs.CharsetRefuseEditDialog;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.PermissionUtil;

public class AppSettingsActivity extends WinBoLLActivity implements IWinBoLLActivity {

    public static final String TAG = "AppSettingsActivity";

	// 讯飞语记官网下载页链接
	private static final String XUNFEI_YUJI_DOWNLOAD_URL = "https://iflynote.com/h/share-download-app.html";

    AppConfigUtil mAppConfigUtil;
    AToolbar mAToolbar;
    AOHPCTCSeekBar mAOHPCTCSeekBar;
    EditText metTTSPlayDelayTimes;
    EditText metPhoneMergePrefix;
    Switch mswMergePrefixPhone;
    Switch mswSMSRecycleProtectMode;
    //EditText metProtectModerRefuseChars;
    EditText metProtectModerReplaceChars;
	String mszProtectModerRefuseChars = "";
    RadioGroup mRadioGroupRecycleBin;

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public String getTag() {
		return TAG;
	}

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

        //metProtectModerRefuseChars = findViewById(R.id.activityappsettingsEditText3);
        //metProtectModerRefuseChars.setText(mAppConfigUtil.mAppConfigBean.getProtectModerRefuseChars());
		mszProtectModerRefuseChars = mAppConfigUtil.mAppConfigBean.getProtectModerRefuseChars();

        metProtectModerReplaceChars = findViewById(R.id.activityappsettingsEditText4);
        metProtectModerReplaceChars.setText(mAppConfigUtil.mAppConfigBean.getProtectModerReplaceChars());

        mRadioGroupRecycleBin = findViewById(R.id.activityappsettingsRadioGroup1);
        if (mAppConfigUtil.mAppConfigBean.getRecycleBinClass().equals("SMSRecycle2Activity")) {
            mRadioGroupRecycleBin.check(R.id.activityappsettingsRadioButton2);
        } else {
            mRadioGroupRecycleBin.check(R.id.activityappsettingsRadioButton1);
        }

        mAOHPCTCSeekBar = findViewById(R.id.activityappsettingsAOHPCTCSeekBar1);
        mAOHPCTCSeekBar.setThumb(getDrawable(R.drawable.cursor_pointer));
        mAOHPCTCSeekBar.setThumbOffset(0);
        mAOHPCTCSeekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener(){

                @Override
                public void onOHPCommit() {
                    mAppConfigUtil.reLoadConfig();
                    mAppConfigUtil.mAppConfigBean.setIsSMSRecycleProtectMode(mswSMSRecycleProtectMode.isChecked());
                    if (mRadioGroupRecycleBin.getCheckedRadioButtonId() == R.id.activityappsettingsRadioButton2) {
                        mAppConfigUtil.mAppConfigBean.setRecycleBinClass("SMSRecycle2Activity");
                    } else {
                        mAppConfigUtil.mAppConfigBean.setRecycleBinClass("SMSRecycleActivity");
                    }
                    //mAppConfigUtil.mAppConfigBean.setProtectModerRefuseChars(metProtectModerRefuseChars.getText().toString());
                    mAppConfigUtil.mAppConfigBean.setProtectModerRefuseChars(mszProtectModerRefuseChars);
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

    public void onAddTTSSupport(View view) {
		try {
			// 1. 创建Intent，Action为“打开网页”
			Intent intent = new Intent(Intent.ACTION_VIEW);
			// 2. 设置要跳转的URL
			intent.setData(Uri.parse(XUNFEI_YUJI_DOWNLOAD_URL));
			// 3. 确保Intent可被解析（避免无浏览器时崩溃）
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent); // 跳转至浏览器打开下载页
			} else {
				// 无浏览器时的提示
				Toast.makeText(this, "未找到浏览器应用，请安装后重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "无法打开下载页面，请稍后再试", Toast.LENGTH_SHORT).show();
		}
    }

	public void onCharsetRefuseEditDialog(View view) {
		CharsetRefuseEditDialog dlg = new CharsetRefuseEditDialog(this, new CharsetRefuseEditDialog.OnTextConfirmListener(){
				@Override
				public void onTextConfirmed(String editText) {
					//ToastUtils.show(editText);
					mszProtectModerRefuseChars = editText;
				}
			}, mszProtectModerRefuseChars);
		dlg.show();
	}

	public void onTTSFloatSettingsActivity(View view) {
		Intent intent = new Intent(this, TTSFloatSettingsActivity.class);
		startActivity(intent);
	}
}
