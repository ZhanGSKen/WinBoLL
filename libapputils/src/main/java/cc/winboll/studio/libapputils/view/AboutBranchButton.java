package cc.winboll.studio.libapputils.view;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/02/06 09:02:28
 * @Describe 应用分支介绍按钮
 */
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import cc.winboll.studio.libapputils.activities.AboutActivity;
import cc.winboll.studio.libapputils.app.AboutActivityFactory;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;
import com.hjq.toast.ToastUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AboutBranchButton extends Button {
    
    public static final String TAG = "AboutBranchButton";
    
    public AboutBranchButton(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    AboutActivityFactory.showAboutActivity(context, ((IWinBollActivity)context).getAppInfo());
                }
            });
    }
}
