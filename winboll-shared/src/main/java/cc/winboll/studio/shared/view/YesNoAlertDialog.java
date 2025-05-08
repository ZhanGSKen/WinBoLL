package cc.winboll.studio.shared.view;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 14:46:25
 * @Describe 询问用户确定与否的选择框
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
