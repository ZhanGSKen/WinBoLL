package cc.winboll.studio.libappbase.utils;
import android.content.Context;
import android.widget.Toast;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/12 12:02:31
 */
public class ToastUtils {

    public static final String TAG = "ToastUtils";

    volatile static ToastUtils _ToastUtils;
    Context mContext;

    ToastUtils() {
    }

    synchronized static ToastUtils getInstance() {
        if (_ToastUtils == null) {
            _ToastUtils = new ToastUtils();
        }
        return _ToastUtils;
    }

    public static void init(Context context) {
        getInstance().mContext = context;
    }

    public static void show(String message) {
        Toast.makeText(getInstance().mContext, message, Toast.LENGTH_SHORT).show();
    }
}
