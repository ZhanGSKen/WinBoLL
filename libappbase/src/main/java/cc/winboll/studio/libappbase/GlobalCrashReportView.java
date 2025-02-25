package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/11 20:18:30
 * @Describe 应用崩溃报告视图
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.R;

public class GlobalCrashReportView extends LinearLayout {

    public static final String TAG = "GlobalCrashReportView";

    Context mContext;
    Toolbar mToolbar;
    int colorTittle;
    int colorTittleBackground;
    int colorText;
    int colorTextBackground;
    TextView mtvReport;

    public GlobalCrashReportView(Context context) {
        super(context);
        mContext = context;
        //initView();
    }

    public GlobalCrashReportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(attrs);
    }

    public GlobalCrashReportView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //initView();
    }

    public GlobalCrashReportView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        //initView();
    }

    public void setColorTittle(int colorTittle) {
        this.colorTittle = colorTittle;
    }

    public int getColorTittle() {
        return colorTittle;
    }

    public void setColorTittleBackground(int colorTittleBackground) {
        this.colorTittleBackground = colorTittleBackground;
    }

    public int getColorTittleBackground() {
        return colorTittleBackground;
    }

    public void setColorText(int colorText) {
        this.colorText = colorText;
    }

    public int getColorText() {
        return colorText;
    }

    public void setColorTextBackground(int colorTextBackground) {
        this.colorTextBackground = colorTextBackground;
    }

    public int getColorTextBackground() {
        return colorTextBackground;
    }

    void initView(AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.GlobalCrashActivity, R.attr.themeGlobalCrashActivity, 0);
        this.colorTittle = a.getColor(R.styleable.GlobalCrashActivity_colorTittle, Color.WHITE);
        this.colorTittleBackground = a.getColor(R.styleable.GlobalCrashActivity_colorTittleBackgound, Color.BLACK);
        this.colorText = a.getColor(R.styleable.GlobalCrashActivity_colorText, Color.BLACK);
        this.colorTextBackground = a.getColor(R.styleable.GlobalCrashActivity_colorTextBackgound, Color.WHITE);
        // 返回一个绑定资源结束的信号给资源
        a.recycle();

        /*this.colorTittle = Color.WHITE;
        this.colorTittleBackground = Color.BLACK;
        this.colorText = Color.BLACK;
        this.colorTextBackground = Color.WHITE;
        */
        
        inflate(mContext, R.layout.view_globalcrashreport, this);

        LinearLayout llMain = findViewById(R.id.viewglobalcrashreportLinearLayout1);
        llMain.setBackgroundColor(this.colorTextBackground);
        mToolbar = findViewById(R.id.viewglobalcrashreportToolbar1);
        mToolbar.setBackgroundColor(this.colorTittleBackground);
        mToolbar.setTitleTextColor(this.colorTittle);
        mToolbar.setSubtitleTextColor(this.colorTittle);
        mtvReport = findViewById(R.id.viewglobalcrashreportTextView1);
        mtvReport.setTextColor(this.colorText);
        mtvReport.setBackgroundColor(this.colorTextBackground);
    }

    public void setReport(String report) {
        mtvReport.setText(report);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    //
    // 更新菜单文字风格
    //
    public void updateMenuStyle() {
        // 设置菜单文本颜色
        Menu menu = mToolbar.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(item.getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(this.colorTittle), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }
    }
}
