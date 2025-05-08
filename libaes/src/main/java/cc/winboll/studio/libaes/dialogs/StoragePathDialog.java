package cc.winboll.studio.libaes.dialogs;

import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.utils.ScreenUtil;

public class StoragePathDialog extends Dialog {

    public static final String TAG = "StoragePathDialog";
    View.OnClickListener mOnOKClickListener;

    public StoragePathDialog(android.content.Context context) {
        super(context);

    }

    public StoragePathDialog(android.content.Context context, int themeResId) {
        super(context, (themeResId == 0) ? cc.winboll.studio.libaes.R.style.NormalDialogStyle: themeResId);
        // 加载默认布局
        View view = View.inflate(context, R.layout.dialog_storagepath, null);
        setContentView(view);
        // 添加按键点击监听
        view.findViewById(R.id.dialogstoragepathButton1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnOKClickListener != null) {
                        mOnOKClickListener.onClick(view);
                    }
                }
            });
        // 使得点击对话框外部不消失对话框
        setCanceledOnTouchOutside(false);
        // 设置对话框大小
        ScreenUtil.ScreenSize ss = ScreenUtil.getScreenSize(context);
        view.setMinimumHeight((int) (ss.getHeightPixels() * 0.23f));
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ss.getWidthPixels() * 0.75f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);

    }

    protected StoragePathDialog(android.content.Context context, boolean cancelable, android.content.DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setOnOKClickListener(View.OnClickListener listener) {
        mOnOKClickListener = listener;
    }

}
