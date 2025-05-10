package cc.winboll.studio.shared.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.R;
import cc.winboll.studio.shared.app.WinBollActivity;
import cc.winboll.studio.shared.app.WinBollActivityManager;
import cc.winboll.studio.shared.log.LogUtils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/14 13:20:33
 * @Describe AboutFragment Test
 */
final public class AboutActivity extends WinBollActivity {

    public static final String TAG = "AboutActivity";


    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        /*AboutView aboutView = findViewById(R.id.activityaboutAboutView1);
         aboutView.setOnRequestDevUserInfoAutofillListener(new AboutView.OnRequestDevUserInfoAutofillListener(){

         @Override
         public void requestAutofill(EditText etDevUserName, EditText etDevUserPassword) {
         AutofillManager autofillManager = (AutofillManager) getSystemService(AutofillManager.class);
         if (autofillManager!= null) {
         //ToastUtils.show("0");
         autofillManager.requestAutofill(findViewById(R.id.usernameEditText));
         autofillManager.requestAutofill(findViewById(R.id.passwordEditText));
         }
         }
         });*/

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSubTitle(TAG);
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activityaboutToolbar1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_winboll_shared_about, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_help) {
            WinBollActivityManager.getInstance(this).startWinBollActivity(this, HelpActivity.class);
        }
//        else if (item.getItemId() == android.R.id.home) {
//            WinBollActivityManager.getInstance(this).finish(this);
//        }
        return super.onOptionsItemSelected(item);
    }
}
