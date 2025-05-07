package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/07 11:03
 * @Describe TimeStampUtil
 */
import android.content.Context;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeStampUtil {

    public static final String TAG = "TimeStampUtil";

    volatile static TimeStampUtil _TimeStampUtil;

    Context mContext;
    long mTimeStamp;

    TimeStampUtil(Context context) {
        mContext = context;
    }

    public synchronized static TimeStampUtil getInstance(Context context) {
        if (_TimeStampUtil == null) {
            _TimeStampUtil = new TimeStampUtil(context);
        }
        return _TimeStampUtil;
    }

    public void genTimeStamp() {
        mTimeStamp = System.currentTimeMillis();
    }

    public String getTimeStampShowString() {
        long currentMillis = mTimeStamp;
        Instant instant = Instant.ofEpochMilli(currentMillis);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        String szTimeStampFormatString = AppConfigsUtil.getInstance(mContext).getAppConfigsModel().getTimeStampFormatString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
        String formattedDateTime = ldt.format(formatter);
        return formattedDateTime;
    }

    public String getTimeStampCopyString() {
        long currentMillis = mTimeStamp;
        Instant instant = Instant.ofEpochMilli(currentMillis);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        String szTimeStampFormatString = AppConfigsUtil.getInstance(mContext).getAppConfigsModel().getTimeStampCopyFormatString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
        String formattedDateTime = ldt.format(formatter);
        return formattedDateTime;
    }
}
