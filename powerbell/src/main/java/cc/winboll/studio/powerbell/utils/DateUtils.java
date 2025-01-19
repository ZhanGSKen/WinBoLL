package cc.winboll.studio.powerbell.utils;

import java.text.SimpleDateFormat;

public class DateUtils {
    
    // 获取当前时间的格式化字符串
    public static String getDateNowString() {
        // 日期类转化成字符串类的工具
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("YYYYMMdd_HHmmssmmm", java.util.Locale.getDefault());
        // 读取当前时间
        long nTimeNow = System.currentTimeMillis();
        return mSimpleDateFormat.format(nTimeNow);
    }
    
}
