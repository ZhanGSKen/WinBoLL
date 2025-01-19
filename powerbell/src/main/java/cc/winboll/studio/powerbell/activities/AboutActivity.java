package cc.winboll.studio.powerbell.activities;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/12 13:33:59
 * @Describe AboutActivity
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.powerbell.R;

public class AboutActivity extends Activity {

    public static final String TAG = "AboutActivity";

    AToolbar mAToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 初始化工具栏
        mAToolbar = (AToolbar) findViewById(R.id.toolbar);
        setActionBar(mAToolbar);
        //mAToolbar.setTitle(getTitle() + "-" + getString(R.string.subtitle_activity_backgroundpicture));
        mAToolbar.setSubtitle(R.string.subtitle_activity_about);
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
    }
}
