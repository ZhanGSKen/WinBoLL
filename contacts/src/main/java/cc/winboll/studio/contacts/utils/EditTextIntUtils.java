package cc.winboll.studio.contacts.utils;
import android.widget.EditText;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/13 00:59:13
 * @Describe Int类型数字输入框工具集
 */
public class EditTextIntUtils {

    public static final String TAG = "EditTextIntUtils";

    public static int getIntFromEditText(EditText editText) {
        try {
            String sz = editText.getText().toString().trim();
            return Integer.parseInt(sz);
        } catch (NumberFormatException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            return 0;
        }
    }

}
