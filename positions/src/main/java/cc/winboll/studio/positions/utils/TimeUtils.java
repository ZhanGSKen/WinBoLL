package cc.winboll.studio.positions.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/21 16:02:56
 * @Describe 时间工具集
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    
    public static final String TAG = "TimeUtils";
    
    public static String getCurrentTimeString() {
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化日期时间
        String formattedDateTime = now.format(formatter);
        //System.out.println(formattedDateTime);
        return formattedDateTime;
    }
    
}
