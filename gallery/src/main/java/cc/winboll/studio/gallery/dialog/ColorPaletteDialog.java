package cc.winboll.studio.gallery.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cc.winboll.studio.gallery.R;

/**
 * 颜色表对话框
 * 继承于普通对话框类，使用视图文件
 */
public class ColorPaletteDialog extends Dialog {

    public ColorPaletteDialog(@NonNull Context context) {
        super(context, R.style.ColorPaletteDialog);
    }

    public ColorPaletteDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_palette);
        
        TextView titleText = findViewById(R.id.title_text);
        
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
    }

    public void setTitle(String title) {
        
    }

    public interface OnColorItemClick {
        void onColorClick(int color);
    }
}
