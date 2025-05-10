package cc.winboll.studio.mymessagemanager.dialogs;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/30 09:53:26
 * @Describe 用户确定与否选择框
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class YesNoAlertDialog {

    public static final String TAG = "YesNoAlertDialog";

    public static void show(Context context, String szTitle, String szMessage, final OnDialogResultListener listener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
            context);

        // set title
        alertDialogBuilder.setTitle(szTitle);

        // set dialog message
        alertDialogBuilder
            .setMessage(szMessage)
            .setCancelable(true)
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog) {
                    listener.onNo();
                }
            })
            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity
                    listener.onYes();
                }
            })
            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                }
            });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public interface OnDialogResultListener {
        abstract void onYes();
        abstract void onNo();
    }
}
