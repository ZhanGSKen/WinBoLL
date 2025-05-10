package cc.winboll.studio.libaes.views;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/16 01:41:22
 * @Describe AButton
 */
import android.content.Context;
import android.util.AttributeSet;
import cc.winboll.studio.libaes.R;

public class AButton extends android.widget.Button {

    public static final String TAG = "AButton";

    public AButton(Context context) {
        super(context);
    }

    public AButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(context.getDrawable(R.drawable.btn_style));
    }

    public AButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
