package cc.winboll.studio.contacts.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 05:37:42
 */
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.adapters.PhoneConnectRuleAdapter;
import cc.winboll.studio.contacts.beans.MainServiceBean;
import cc.winboll.studio.contacts.beans.PhoneConnectRuleModel;
import cc.winboll.studio.contacts.beans.RingTongBean;
import cc.winboll.studio.contacts.beans.SettingsModel;
import cc.winboll.studio.contacts.bobulltoon.TomCat;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.views.DuInfoTextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;
import com.hjq.toast.ToastUtils;
import java.lang.reflect.Field;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "SettingsActivity";

    Toolbar mToolbar;
    Switch swSilent;
    SeekBar msbVolume;
    TextView mtvVolume;
    int mnStreamMaxVolume;
    int mnStreamVolume;
    Switch mswMainService;
    static DuInfoTextView _DuInfoTextView;

    // 云盾防御层数量
    EditText etDunTotalCount;
    // 防御层恢复时间间隔(秒钟)
    EditText etDunResumeSecondCount;
    // 每次恢复防御层数
    EditText etDunResumeCount;
    // 是否启用云盾
    Switch swIsEnableDun;

    private RecyclerView recyclerView;
    private PhoneConnectRuleAdapter adapter;
    private List<PhoneConnectRuleModel> ruleList;

    @Override
    public APPInfo getAppInfo() {
        return null;
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activitymainToolbar1);
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化工具栏
        mToolbar = findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(mToolbar);
        if (isEnableDisplayHomeAsUp()) {
            // 显示后退按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setSubtitle(getTag());

        mswMainService = findViewById(R.id.sw_mainservice);
        MainServiceBean mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        mswMainService.setChecked(mMainServiceBean.isEnable());
        mswMainService.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    LogUtils.d(TAG, "mswMainService onClick");
                    // TODO: Implement this method
                    if (mswMainService.isChecked()) {
                        //ToastUtils.show("Is Checked");
                        MainService.startMainServiceAndSaveStatus(SettingsActivity.this);
                    } else {
                        //ToastUtils.show("Not Checked");
                        MainService.stopMainServiceAndSaveStatus(SettingsActivity.this);
                    }
                }
            });

        msbVolume = findViewById(R.id.bellvolume);
        mtvVolume = findViewById(R.id.tv_volume);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // 设置SeekBar的最大值为系统铃声音量的最大刻度
        mnStreamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        msbVolume.setMax(mnStreamMaxVolume);
        // 获取当前铃声音量并设置为SeekBar的初始进度
        mnStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        msbVolume.setProgress(mnStreamVolume);

        updateStreamVolumeTextView();

        msbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // 设置铃声音量
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, progress, 0);
                        RingTongBean bean = RingTongBean.loadBean(SettingsActivity.this, RingTongBean.class);
                        if (bean == null) {
                            bean = new RingTongBean();
                        }
                        bean.setStreamVolume(progress);
                        RingTongBean.saveBean(SettingsActivity.this, bean);
                        mnStreamVolume = progress;
                        updateStreamVolumeTextView();
                        //Toast.makeText(SettingsActivity.this, "音量设置为: " + progress, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // 当开始拖动SeekBar时的操作
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // 当停止拖动SeekBar时的操作
                }
            });


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ruleList = Rules.getInstance(this).getPhoneBlacRuleBeanList();

        adapter = new PhoneConnectRuleAdapter(this, ruleList);
        recyclerView.setAdapter(adapter);

        // 设置参数云盾
        _DuInfoTextView = findViewById(R.id.tv_DunInfo);
        etDunTotalCount = findViewById(R.id.et_DunTotalCount);
        etDunResumeSecondCount = findViewById(R.id.et_DunResumeSecondCount);
        etDunResumeCount = findViewById(R.id.et_DunResumeCount);
        swIsEnableDun = findViewById(R.id.sw_IsEnableDun);
        SettingsModel settingsModel = Rules.getInstance(this).getSettingsModel();

        etDunTotalCount.setText(Integer.toString(settingsModel.getDunTotalCount()));
        etDunResumeSecondCount.setText(Integer.toString(settingsModel.getDunResumeSecondCount()));
        etDunResumeCount.setText(Integer.toString(settingsModel.getDunResumeCount()));
        swIsEnableDun.setChecked(settingsModel.isEnableDun());

        boolean isEnableDun = settingsModel.isEnableDun();
        etDunTotalCount.setEnabled(!isEnableDun);
        etDunResumeSecondCount.setEnabled(!isEnableDun);
        etDunResumeCount.setEnabled(!isEnableDun);

    }

    public static void notifyDunInfoUpdate() {
        if (_DuInfoTextView != null) {
            _DuInfoTextView.notifyInfoUpdate();
        }
    }

    public void onSW_IsEnableDun(View view) {
        LogUtils.d(TAG, "onSW_IsEnableDun");
        boolean isEnableDun = swIsEnableDun.isChecked();
        etDunTotalCount.setEnabled(!isEnableDun);
        etDunResumeSecondCount.setEnabled(!isEnableDun);
        etDunResumeCount.setEnabled(!isEnableDun);

        SettingsModel settingsModel = Rules.getInstance(this).getSettingsModel();
        if (isEnableDun) {
            settingsModel.setDunTotalCount(Integer.parseInt(etDunTotalCount.getText().toString()));
            settingsModel.setDunResumeSecondCount(Integer.parseInt(etDunResumeSecondCount.getText().toString()));
            settingsModel.setDunResumeCount(Integer.parseInt(etDunResumeCount.getText().toString()));
        }
        settingsModel.setIsEnableDun(isEnableDun);
        Rules.getInstance(this).saveDun();
        Rules.getInstance(this).reload();
    }

    void updateStreamVolumeTextView() {
        mtvVolume.setText(String.format("%d/%d", mnStreamVolume, mnStreamMaxVolume));
    }

    public void onUnitTest(View view) {
        Intent intent = new Intent(this, UnitTestActivity.class);
        startActivity(intent);
    }
    
    public void onAddNewConnectionRule(View view) {
        Rules.getInstance(this).getPhoneBlacRuleBeanList().add(new PhoneConnectRuleModel());
        Rules.getInstance(this).saveRules();
        adapter.notifyDataSetChanged();
    }

    public void onDefaultPhone(View view) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
        startActivity(intent);
    }

    public void onCanDrawOverlays(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !Settings.canDrawOverlays(this)) {
            // 请求 悬浮框 权限
            askForDrawOverlay();
        } else {
            ToastUtils.show("悬浮窗已开启");
        }
    }

    public void onDownloadBoBullToon(View view) {
        final TomCat tomCat = TomCat.getInstance(this);
        new Thread(new Runnable() {
                @Override
                public void run() {
                    if (tomCat.downloadBoBullToon()) {
                        ToastUtils.show("BoBullToon downlaod OK!");
                        MainService.restartMainService(SettingsActivity.this);
                        Rules.getInstance(SettingsActivity.this).reload();
                    }
                }
            }).start();
    }
    
    

    public void onSearchBoBullToonPhone(View view) {
        TomCat tomCat = TomCat.getInstance(this);
        EditText etPhone = findViewById(R.id.activitysettingsEditText1);
        String phone = etPhone.getText().toString().trim();
        if (tomCat.loadPhoneBoBullToon()) {
            if (tomCat.isPhoneBoBullToon(phone)) {
                ToastUtils.show("It is a BoBullToon Phone!");
            } else {
                ToastUtils.show("Not in BoBullToon.");
            }
        } else {
            ToastUtils.show("没有下载 BoBullToon。");
        }

    }

    private void askForDrawOverlay() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle("允许显示悬浮框")
            .setMessage("为了使电话监听服务正常工作，请允许这项权限")
            .setPositiveButton("去设置", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openDrawOverlaySettings();
                    dialog.dismiss();
                }
            })
            .setNegativeButton("稍后再说", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create();

        //noinspection ConstantConditions
        alertDialog.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
    }

    /**
     * 跳转悬浮窗管理设置界面
     */
    private void openDrawOverlaySettings() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M 以上引导用户去系统设置中打开允许悬浮窗
            // 使用反射是为了用尽可能少的代码保证在大部分机型上都可用
            try {
                Context context = this;
                Class clazz = Settings.class;
                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
                Intent intent = new Intent(field.get(null).toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "请在悬浮窗管理中打开权限", Toast.LENGTH_LONG).show();
            }
        }
    }
}
