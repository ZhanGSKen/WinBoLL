package cc.winboll.studio.mymessagemanager.activitys;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.R;

abstract public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    IOnActivityMessageReceived mIOnActivityMessageReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //AppConfigUtil configUtil = AppConfigUtil.getInstance(this);
        //setTheme(configUtil.mAppConfigBean.getAppThemeID());
        LogUtils.d(TAG, "AESThemeUtil.getThemeTypeID(this) is : " + Integer.toString(AESThemeUtil.getThemeTypeID(this)));
        setTheme(AESThemeUtil.getThemeTypeID(this));
        super.onCreate(savedInstanceState);
    }

    public void sendActivityMessage(Message msg) {
        mHandler.sendMessage(msg);
    }

    protected void setOnActivityMessageReceived(IOnActivityMessageReceived iOnActivityMessageReceived) {
        mIOnActivityMessageReceived = iOnActivityMessageReceived;
    }

    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mIOnActivityMessageReceived != null) {
                mIOnActivityMessageReceived.onActivityMessageReceived(msg);
            }
        }
    };

    protected interface IOnActivityMessageReceived {
        void onActivityMessageReceived(Message msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AESThemeUtil.inflateMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (AESThemeUtil.onAppCompatThemeItemSelected(this, item)) {
         ToastUtils.show("onAppCompatThemeItemSelected");
         recreate();
         }*/

        /*int nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEFAULT);
        if (R.id.item_depththeme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEPTH);
            AESThemeUtil.saveThemeStyleID(this, nThemeStyleID);
            recreate();
        } else if (R.id.item_skytheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.SKY);
            AESThemeUtil.saveThemeStyleID(this, nThemeStyleID);
            recreate();
        } else if (R.id.item_goldentheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.GOLDEN);
            AESThemeUtil.saveThemeStyleID(this, nThemeStyleID);
            recreate();
        } else if (R.id.item_taotheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.TAO);
            AESThemeUtil.saveThemeStyleID(this, nThemeStyleID);
            recreate();
        } else if (R.id.item_defaulttheme == item.getItemId()) {
            nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEFAULT);
            AESThemeUtil.saveThemeStyleID(this, nThemeStyleID);
            recreate();
        }*/
        
        //int nThemeStyleID = AESThemeBean.getThemeStyleID(AESThemeBean.ThemeType.DEFAULT);
        if (R.id.item_depththeme == item.getItemId()) {
            AESThemeUtil.saveThemeStyleID(this, R.style.MyDepthAESTheme);
            recreate();
        } else if (R.id.item_skytheme == item.getItemId()) {
            AESThemeUtil.saveThemeStyleID(this, R.style.MySkyAESTheme);
            recreate();
        } else if (R.id.item_goldentheme == item.getItemId()) {
            AESThemeUtil.saveThemeStyleID(this, R.style.MyGoldenAESTheme);
            recreate();
        } else if (R.id.item_memortheme == item.getItemId()) {
            AESThemeUtil.saveThemeStyleID(this, R.style.MyMemorAESTheme);
            recreate();
        } else if (R.id.item_taotheme == item.getItemId()) {
            AESThemeUtil.saveThemeStyleID(this, R.style.MyTaoAESTheme);
            recreate();
        } else if (R.id.item_defaulttheme == item.getItemId()) {
            AESThemeUtil.saveThemeStyleID(this, R.style.MyAppTheme);
            recreate();
        }
        //ToastUtils.show("nThemeStyleID " + Integer.toString(nThemeStyleID));

        return super.onOptionsItemSelected(item);
    }
}
