package cc.winboll.studio.gallery.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class CustomApplicationBackground {
    Drawable mDrawable;

    public CustomApplicationBackground(Drawable drawable) {
        mDrawable = drawable;
    }

    public CustomApplicationBackground(int color) {
        mDrawable = new ColorDrawable(color);
    }

    public CustomApplicationBackground(Context context, int resId) {
        mDrawable = context.getDrawable(resId);
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
