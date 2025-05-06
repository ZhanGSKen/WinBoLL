package cc.winboll.studio.timestamp.receivers;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 11:35
 * @Describe ButtonClickReceiver
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.utils.AppConfigsUtil;
import cc.winboll.studio.timestamp.utils.ClipboardUtil;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ButtonClickReceiver extends BroadcastReceiver {

    public static final String TAG = "ButtonClickReceiver";

    public static final String BUTTON_COPYTIMESTAMP_ACTION = "cc.winboll.studio.timestamp.receivers.ButtonClickReceiver.BUTTON_COPYTIMESTAMP_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(TAG, "onReceive");
        if (intent.getAction().equals(BUTTON_COPYTIMESTAMP_ACTION)) {
            // 在这里编写按钮点击后要执行的代码
            long currentMillis = System.currentTimeMillis();
            Instant instant = Instant.ofEpochMilli(currentMillis);
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            String szTimeStampFormatString = AppConfigsUtil.getInstance(context).getAppConfigsModel().getTimeStampCopyFormatString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
            String formattedDateTime = ldt.format(formatter);

            ClipboardUtil.copyTextToClipboard(context, formattedDateTime);

            // 比如显示一个Toast
            Toast.makeText(context, formattedDateTime + " 已复制", Toast.LENGTH_SHORT).show();
        }
    }

}
