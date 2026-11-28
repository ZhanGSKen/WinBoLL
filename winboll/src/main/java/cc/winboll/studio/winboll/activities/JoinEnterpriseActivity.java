package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import cc.winboll.studio.winboll.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2026/11/28 12:30
 * @Describe Entry page for requesting to join the official WinBoLL enterprise on WeCom.
 *
 * Notes that must stay true:
 * 1) The WeCom invitation link is regenerated every day and expires after 7 days, so it
 *    must never be hardcoded into the APK. The app only opens the fixed short link
 *    JOIN_URL, and the server keeps that short link pointing at the current invitation.
 * 2) Joining is voluntary and only used to receive official notifications. The app sends
 *    no attendance record and assigns no work, so joining does not create an employment
 *    relationship.
 * 3) The member quota is delivered by the server. This build shows a placeholder until
 *    the account service is wired up, because a wrong hardcoded number is worse than none.
 */
public class JoinEnterpriseActivity extends BaseWinBoLLActivity {

    public static final String TAG = "JoinEnterpriseActivity";

    /** Fixed short link; the server redirects it to the current WeCom invitation. */
    public static final String JOIN_URL = "https://winboll.cc/join";

    /** WeCom package, used only to detect whether the client is installed. */
    private static final String WECOM_PACKAGE = "com.tencent.wework";
    private static final String WECOM_MARKET_URL = "market://details?id=" + WECOM_PACKAGE;
    private static final String WECOM_HOME_URL = "https://work.weixin.qq.com/";

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_enterprise);

        // Toolbar: same pattern as SettingsActivity, back arrow finishes this page.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(R.string.join_enterprise_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Quota line: placeholder until the account service publishes the real numbers.
        TextView tvQuota = (TextView) findViewById(R.id.tv_join_quota);
        if (tvQuota != null) {
            tvQuota.setText(getString(R.string.join_enterprise_quota_value));
        }

        Button btnJoin = (Button) findViewById(R.id.btn_join_enterprise);
        if (btnJoin != null) {
            btnJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openJoinPage();
                }
            });
        }

        Button btnExitGuide = (Button) findViewById(R.id.btn_join_exit_guide);
        if (btnExitGuide != null) {
            btnExitGuide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showExitGuide();
                }
            });
        }
    }

    /** Whether the WeCom client is installed. Any failure counts as "not installed". */
    private boolean isWeComInstalled() {
        try {
            getPackageManager().getPackageInfo(WECOM_PACKAGE, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Open the invitation through the short link. WeCom is preferred when installed;
     * otherwise the user is sent to the store, then to the official download page.
     */
    private void openJoinPage() {
        Uri uri = Uri.parse(JOIN_URL);
        if (isWeComInstalled()) {
            Intent wecomIntent = new Intent(Intent.ACTION_VIEW, uri);
            wecomIntent.setPackage(WECOM_PACKAGE);
            try {
                startActivity(wecomIntent);
                return;
            } catch (ActivityNotFoundException e) {
                // Some builds expose no VIEW handler; fall back to a plain VIEW.
            }
            Intent plainIntent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(plainIntent);
                return;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.toast_join_enterprise_failed,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Not installed: the invitation cannot be consumed by a browser session,
        // so guide the user to install WeCom first.
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WECOM_MARKET_URL)));
            Toast.makeText(this, R.string.toast_join_enterprise_not_installed,
                    Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WECOM_HOME_URL)));
                Toast.makeText(this, R.string.toast_join_enterprise_not_installed,
                        Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.toast_join_enterprise_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Leaving is always done inside WeCom; this page only explains the path. */
    private void showExitGuide() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.join_enterprise_exit_dialog_title);
        builder.setMessage(R.string.join_enterprise_exit_dialog_msg);
        builder.setPositiveButton(R.string.join_enterprise_exit_dialog_ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
