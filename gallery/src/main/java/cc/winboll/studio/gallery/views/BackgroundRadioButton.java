package cc.winboll.studio.gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class BackgroundRadioButton extends RadioButton {

    CustomApplicationBackground mCustomApplicationBackground;

    public BackgroundRadioButton(Context context) {
        super(context);
    }

    public BackgroundRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setupCustomApplicationBackground(Context context, int resId) {
        mCustomApplicationBackground = new CustomApplicationBackground(context, resId);
    }

    public void setCustomApplicationBackground(CustomApplicationBackground customApplicationBackground) {
        mCustomApplicationBackground = customApplicationBackground;
    }

    public CustomApplicationBackground getCustomApplicationBackground() {
        return mCustomApplicationBackground;
    }
}
