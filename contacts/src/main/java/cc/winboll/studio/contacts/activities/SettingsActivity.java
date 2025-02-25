package cc.winboll.studio.contacts.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 05:37:42
 */
import android.app.NotificationManager;
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
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.beans.RingTongBean;
import cc.winboll.studio.libappbase.IWinBollActivity;
import cc.winboll.studio.libappbase.bean.APPInfo;
import com.hjq.toast.ToastUtils;
import java.lang.reflect.Field;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "SettingsActivity";

    Toolbar mToolbar;
    Switch swSilent;
    SeekBar msbVolume;
    TextView mtvVolume;
    int mnStreamMaxVolume;
    int mnStreamVolume;


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
    }
    
    void updateStreamVolumeTextView() {
        mtvVolume.setText(String.format("%d/%d", mnStreamVolume, mnStreamMaxVolume));
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
