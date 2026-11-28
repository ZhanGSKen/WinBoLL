package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2026/11/28 12:30
 * @Describe Entry page for requesting to join the official WinBoLL enterprise on WeCom.
 *
 * Notes that must stay true:
 * 1) The app never hardcodes the WeCom invitation link. It only opens the fixed short
 *    link JOIN_URL, and the server keeps that short link pointing at the current
 *    invitation (the long-lived one, or a refreshed one when it stops working).
 * 2) Joining is voluntary and only used to receive official notifications. The app sends
 *    no attendance record and assigns no work, so joining does not create an employment
 *    relationship.
 * 3) Whether the account has already joined cannot be detected from outside WeCom (no
 *    SDK, no server account service yet), so this page reports only what it can prove
 *    locally: WeCom install state and the last attempt made from this device.
 * 4) The member quota is delivered by the server. This build shows the verified
 *    enterprise cap instead of inventing a live number.
 */
public class JoinEnterpriseActivity extends BaseWinBoLLActivity {

    public static final String TAG = "JoinEnterpriseActivity";

    /** Fixed short link; the server redirects it to the current WeCom invitation. */
    public static final String JOIN_URL = "https://develop.studio.winboll/join";

    /** WeCom package, used only to detect whether the client is installed. */
    private static final String WECOM_PACKAGE = "com.tencent.wework";
    private static final String WECOM_DOWNLOAD_URL = "https://work.weixin.qq.com/#indexDownload";
    private static final String WECOM_MARKET_URL = "market://details?id=" + WECOM_PACKAGE;

    /** Local record of the last attempt, so the user can tell whether this device tried. */
    private static final String PREF_NAME = "join_enterprise";
    private static final String KEY_LAST_OPEN_MS = "last_open_ms";
    private static final String KEY_OPEN_COUNT = "open_count";

    private TextView mTvStatus;
    private Button mBtnJoin;

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

        mTvStatus = (TextView) findViewById(R.id.tv_join_status);

        // Quota line: enterprise cap is a verified fact, live quota is not available yet.
        TextView tvQuota = (TextView) findViewById(R.id.tv_join_quota);
        if (tvQuota != null) {
            tvQuota.setText(getString(R.string.join_enterprise_quota_value));
        }

        mBtnJoin = (Button) findViewById(R.id.btn_join_enterprise);
        if (mBtnJoin != null) {
            mBtnJoin.setOnClickListener(new View.OnClickListener() {
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

        refreshLocalStatus();
    }

    /** Refresh the two facts this page can prove: WeCom installed, last attempt on this device. */
    private void refreshLocalStatus() {
        boolean installed = isWeComInstalled();

        if (mBtnJoin != null) {
            mBtnJoin.setText(installed
                    ? R.string.join_enterprise_open_btn
                    : R.string.join_enterprise_install_btn);
        }

        if (mTvStatus == null) {
            return;
        }

        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastOpenMs = sp.getLong(KEY_LAST_OPEN_MS, 0L);
        int openCount = sp.getInt(KEY_OPEN_COUNT, 0);

        StringBuilder sb = new StringBuilder();
        sb.append(getString(installed
                ? R.string.join_enterprise_status_wecom_on
                : R.string.join_enterprise_status_wecom_off));
        sb.append('\n');
        if (lastOpenMs > 0L) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sb.append(getString(R.string.join_enterprise_status_last_open,
                    fmt.format(new Date(lastOpenMs)), openCount));
        } else {
            sb.append(getString(R.string.join_enterprise_status_never_open));
        }
        sb.append('\n');
        sb.append(getString(R.string.join_enterprise_status_joined_unknown));

        mTvStatus.setText(sb.toString());
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

    /** Remember that the invitation page was opened, so the page can report it later. */
    private void recordJoinAttempt() {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int count = sp.getInt(KEY_OPEN_COUNT, 0) + 1;
        sp.edit()
                .putLong(KEY_LAST_OPEN_MS, System.currentTimeMillis())
                .putInt(KEY_OPEN_COUNT, count)
                .apply();
    }

    /**
     * Open the invitation through the short link. WeCom is preferred when installed;
     * otherwise the user is sent to the store, then to the official download page.
     */
    private void openJoinPage() {
        recordJoinAttempt();

        final Uri uri = Uri.parse(JOIN_URL);
        if (isWeComInstalled()) {
            Intent wecomIntent = new Intent(Intent.ACTION_VIEW, uri);
            wecomIntent.setPackage(WECOM_PACKAGE);
            try {
                startActivity(wecomIntent);
                refreshLocalStatus();
                return;
            } catch (ActivityNotFoundException e) {
                LogUtils.d(TAG, "企业微信未响应 VIEW，回退到通用浏览器");
            }
            Intent plainIntent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(plainIntent);
                refreshLocalStatus();
                return;
            } catch (ActivityNotFoundException e) {
                LogUtils.e(TAG, "没有任何应用能打开加入链接");
                Toast.makeText(this, R.string.toast_join_enterprise_failed,
                        Toast.LENGTH_LONG).show();
                refreshLocalStatus();
                return;
            }
        }

        // Not installed: the invitation cannot be consumed by a browser session, so guide
        // the user to the official download page first. market:// comes second because on
        // devices without Play Store it lands on a chooser instead of an install page.
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WECOM_DOWNLOAD_URL)));
            Toast.makeText(this, R.string.toast_join_enterprise_not_installed,
                    Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WECOM_MARKET_URL)));
                Toast.makeText(this, R.string.toast_join_enterprise_not_installed,
                        Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.toast_join_enterprise_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
        refreshLocalStatus();
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
