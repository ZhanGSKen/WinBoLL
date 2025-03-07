package cc.winboll.studio.mymessagemanager.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.SMSAcceptRuleBean;
import cc.winboll.studio.mymessagemanager.beans.TTSPlayRuleBean;
import cc.winboll.studio.mymessagemanager.dialogs.YesNoAlertDialog;
import cc.winboll.studio.mymessagemanager.utils.UriUtil;
import java.util.ArrayList;

public class SharedJSONReceiveActivity extends BaseActivity {

    public static final String TAG = "SharedJSONReceive";

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharedjsonreceive);

        StringBuilder sb = new StringBuilder();
        // 接收分享数据
        Intent intent = getIntent();
        String action = intent.getAction();//action
        String type = intent.getType();//类型
        //LogUtils.d(TAG, "action is " + action);
        //LogUtils.d(TAG, "type is " + type);
        if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action))
            && type != null && (("application/json".equals(type)) || ("text/x-json".equals(type)))) {

            //取出文件uri
            Uri uri = intent.getData();
            if (uri == null) {
                uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
            //获取文件真实地址
            String szSrcJSON = UriUtil.getFileFromUri(getApplication(), uri);
            if (TextUtils.isEmpty(szSrcJSON)) {
                return;
            }

            String szCheck = TTSPlayRuleBean.checkIsTheSameBeanListAndFile(szSrcJSON, TTSPlayRuleBean.class);
            if (szCheck.equals("")) {
                importTTSPlayRuleBean(szSrcJSON);
            } else {
                sb.append("\n语音规则数据检测结果\n");
                sb.append(szCheck);
            }
            //LogUtils.d(TAG, "szCheck is " + szCheck);

            szCheck = SMSAcceptRuleBean.checkIsTheSameBeanListAndFile(szSrcJSON, SMSAcceptRuleBean.class);
            if (szCheck.equals("")) {
                importSMSAcceptRuleBean(szSrcJSON);
            } else {
                sb.append("\n短信接收规则数据检测结果\n");
                sb.append(szCheck);
            }
            //LogUtils.d(TAG, "szCheck is " + szCheck);
        } else {
            sb.append("Not supported action.");
        }

        mToolbar = findViewById(R.id.activitysharedjsonreceiveASupportToolbar1);
        mToolbar.setSubtitle(getString(R.string.activity_name_sharedjsonreceive));
        setActionBar(mToolbar);

        TextView tvMessage = findViewById(R.id.activitysharedjsonreceiveTextView1);
        tvMessage.setText(sb.toString());
    }

    void importSMSAcceptRuleBean(final String szSrcJSON) {
        ArrayList<SMSAcceptRuleBean> beanList = new ArrayList<SMSAcceptRuleBean>();
        boolean bCheck = SMSAcceptRuleBean.loadBeanListFromFile(szSrcJSON, beanList, SMSAcceptRuleBean.class);
        if (bCheck && beanList.size() > 0) {
            YesNoAlertDialog.show(SharedJSONReceiveActivity.this,
                                  "短信接收规则共享提示",
                                  "已收到短信接收规则" + Integer.toString(beanList.size()) + "个，\n是否导入应用？"
                                  , (new YesNoAlertDialog.OnDialogResultListener(){

                                      @Override
                                      public void onYes() {
                                          ArrayList<SMSAcceptRuleBean> beanListShare = new ArrayList<SMSAcceptRuleBean>();
                                          SMSAcceptRuleBean.loadBeanListFromFile(szSrcJSON, beanListShare, SMSAcceptRuleBean.class);
                                          ArrayList<SMSAcceptRuleBean> beanListApp = new ArrayList<SMSAcceptRuleBean>();
                                          SMSAcceptRuleBean.loadBeanList(SharedJSONReceiveActivity.this, beanListApp, SMSAcceptRuleBean.class);
                                          beanListApp.addAll(0, beanListShare);
                                          SMSAcceptRuleBean.saveBeanList(SharedJSONReceiveActivity.this, beanListApp, SMSAcceptRuleBean.class);
                                          Toast.makeText(getApplication(), "已导入" + Integer.toString(beanListShare.size()) + "个数据。", Toast.LENGTH_SHORT).show();
                                          finish();
                                          Intent intent = new Intent(SharedJSONReceiveActivity.this, SMSReceiveRuleActivity.class);
                                          startActivity(intent);
                                      }

                                      @Override
                                      public void onNo() {
                                          finish();
                                      }
                                  }));
        }
    }

    void importTTSPlayRuleBean(final String szSrcJSON) {
        ArrayList<TTSPlayRuleBean> beanList = new ArrayList<TTSPlayRuleBean>();
        boolean bCheck = TTSPlayRuleBean.loadBeanListFromFile(szSrcJSON, beanList, TTSPlayRuleBean.class);
        if (bCheck && beanList.size() > 0) {
            YesNoAlertDialog.show(SharedJSONReceiveActivity.this,
                                  "语音规则共享提示",
                                  "已收到语音规则" + Integer.toString(beanList.size()) + "个，\n是否导入应用？"
                                  , (new YesNoAlertDialog.OnDialogResultListener(){

                                      @Override
                                      public void onYes() {
                                          ArrayList<TTSPlayRuleBean> beanListShare = new ArrayList<TTSPlayRuleBean>();
                                          TTSPlayRuleBean.loadBeanListFromFile(szSrcJSON, beanListShare, TTSPlayRuleBean.class);
                                          ArrayList<TTSPlayRuleBean> beanListApp = new ArrayList<TTSPlayRuleBean>();
                                          TTSPlayRuleBean.loadBeanList(SharedJSONReceiveActivity.this, beanListApp, TTSPlayRuleBean.class);
                                          beanListApp.addAll(0, beanListShare);
                                          TTSPlayRuleBean.saveBeanList(SharedJSONReceiveActivity.this, beanListApp, TTSPlayRuleBean.class);
                                          Toast.makeText(getApplication(), "已导入" + Integer.toString(beanListShare.size()) + "个数据。", Toast.LENGTH_SHORT).show();
                                          finish();
                                          Intent intent = new Intent(SharedJSONReceiveActivity.this, TTSPlayRuleActivity.class);
                                          startActivity(intent);
                                      }

                                      @Override
                                      public void onNo() {
                                          finish();
                                      }
                                  }));
        }
    }
}
