package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.beans.BatteryInfoBean;
import cc.winboll.studio.powerbell.receivers.ControlCenterServiceReceiver;
import cc.winboll.studio.powerbell.utils.AppCacheUtils;
import cc.winboll.studio.powerbell.utils.StringUtils;
import java.util.ArrayList;

public class ClearRecordActivity extends Activity {

    public static final String TAG = "ClearRecordActivity";

    AToolbar mAToolbar;
    TextView mtvRecordText;
    App mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clearrecord);
        mApplication = (App) getApplication();

        // 初始化工具栏
        mAToolbar = (AToolbar) findViewById(R.id.toolbar);
        setActionBar(mAToolbar);
        //mAToolbar.setTitle(getTitle() + " - " + getString(R.string.subtitle_activity_clearrecord));
        mAToolbar.setSubtitle(R.string.subtitle_activity_clearrecord);
        mAToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        mAToolbar.setSubtitleTextAppearance(this, R.style.Toolbar_SubTitleText);
        //mAToolbar.setBackgroundColor(getColor(R.color.colorPrimary));
        setActionBar(mAToolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mAToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            
        // 设置滑动清理控件
        //
        // 初始化发送拉动控件
        final AOHPCTCSeekBar aOHPCTCSeekBar = findViewById(R.id.activityclearrecordAOHPCTCSeekBar1);
        aOHPCTCSeekBar.setThumb(getDrawable(R.drawable.cursor_pointer));
        aOHPCTCSeekBar.setThumbOffset(0);
        aOHPCTCSeekBar.setOnOHPCListener(
            new AOHPCTCSeekBar.OnOHPCListener(){

                @Override
                public void onOHPCommit() {
                    mApplication.clearBatteryHistory();
                    sendBroadcast(new Intent(ControlCenterServiceReceiver.ACTION_UPDATE_SERVICENOTIFICATION));
                    initRecordText();
                    String szMSG = "The APP battery record is cleaned.";
                    LogUtils.d(TAG, szMSG);
                    ToastUtils.show(szMSG);
                }


            });

        // 初始化提示框
        TextView tvAOHPCTCSeekBarMSG = findViewById(R.id.activityclearrecordTextView1);
        tvAOHPCTCSeekBarMSG.setText(R.string.msg_AOHPCTCSeekBar_ClearRecord);
        mtvRecordText = findViewById(R.id.activityclearrecordTextView2);
        initRecordText();
    }

    void initRecordText() {
        ArrayList<BatteryInfoBean> listBatteryInfo = AppCacheUtils.getInstance(this).getArrayListBatteryInfo();
        String szRecordText = StringUtils.formatPCMListString(listBatteryInfo);
        mtvRecordText.setText(szRecordText);
    }
}
