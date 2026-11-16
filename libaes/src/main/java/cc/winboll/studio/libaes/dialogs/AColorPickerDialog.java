package cc.winboll.studio.libaes.dialogs;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import cc.winboll.studio.libaes.views.AColorChangedListener;
import cc.winboll.studio.libaes.views.AColorPickerLayout;

/**
 * Color picker dialog built on AlertDialog.
 * Buttons: 确定 (apply), 确定并复制 (apply and copy #AARRGGBB), 取消 (cancel).
 */
public class AColorPickerDialog implements DialogInterface.OnClickListener {

    public static final String TAG = "AColorPickerDialog";

    private final AlertDialog.Builder builder;
    private final AColorPickerLayout picker;
    private AlertDialog dialog;
    private Context c;
    private AColorChangedListener listener;

    public AColorPickerDialog(Context context, int color) {
        c = context.getApplicationContext();
        listener = null;
        builder = new AlertDialog.Builder(context);
        picker = new AColorPickerLayout(context);
        picker.setColor(color);
        init();
    }

    private void init() {
        builder.setView(picker);
        builder.setPositiveButton("确定", this);
        builder.setNeutralButton("确定并复制", this);
        builder.setNegativeButton("取消", this);
    }

    public AColorPickerDialog setTitle(CharSequence title) {
        builder.setTitle(title);
        return this;
    }

    public AColorPickerDialog setTitle(int titleId) {
        return setTitle(c.getText(titleId));
    }

    public void show() {
        dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
        c = null;
        dialog = null;
    }

    public AColorPickerDialog setOnColorChangedListener(AColorChangedListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE || which == DialogInterface.BUTTON_NEUTRAL) {
            int color = picker.getColor();
            if (which == DialogInterface.BUTTON_NEUTRAL) {
                ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("color", String.format("#%08X", color)));
                Toast.makeText(c, "复制成功", Toast.LENGTH_SHORT).show();
            }
            if (listener != null) {
                listener.onColorChanged(color);
            }
        }
        dismiss();
    }
}
