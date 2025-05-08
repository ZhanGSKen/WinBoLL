package cc.winboll.studio.powerbell.utils;

import cc.winboll.studio.powerbell.beans.BatteryInfoBean;
import java.util.ArrayList;

public class StringUtils {
    public static final String TAG = StringUtils.class.getSimpleName();

    // 电量改变使用分钟数列表
    // List of power-changing usage minutes
    //
    public static String formatPCMListString(ArrayList<BatteryInfoBean> arrayListBatteryInfo) {
        /* 调试数据
         Time t1 = new Time();
         //t.set(int second, int minute, int hour, int monthDay, int month, int year) {}
         t1.set(4, 8, 0, 27, 4, 2022);
         long ntime1 = t1.toMillis(true);
         Time t2 = new Time();
         //t.set(int second, int minute, int hour, int monthDay, int month, int year) {}
         t2.set(9, 12, 3, 29, 4, 2022);
         long ntime2 = t2.toMillis(true);
         LogUtils.d(TAG, "ntime1 is " + Long.toString(ntime1));
         LogUtils.d(TAG, "ntime2 is " + Long.toString(ntime2));
         LogUtils.d(TAG, "getTimespanDifference(ntime1, ntime2) is " + getTimespanDifference(ntime1, ntime2));
         */

        /*String sz = "";
         for (int i = 0; i < lnTime.size() - 1; i++) {
         sz += getTimespanDifference(lnTime.get(i), lnTime.get(i + 1));
         }
         return sz;*/

        String sz = "";
        for (int i = 0; i < arrayListBatteryInfo.size() - 1; i++) {
            //LogUtils.d(TAG, "arrayListBatteryInfo.get(i).getBattetyValue() is "+ Integer.toString(arrayListBatteryInfo.get(i).getBattetyValue()));
            sz = arrayListBatteryInfo.get(i).getBattetyValue() + "% " + getTimespanDifference(arrayListBatteryInfo.get(i).getTimeStamp(), arrayListBatteryInfo.get(i + 1).getTimeStamp()) + " " + sz;
        }
        return sz;
    }

    // 获取时间之间的时间跨度字符串。
    // Get timespan string between times.
    // 返回值： {(几天/)(几小时/)(几分钟/)(几秒钟)}
    // 返回值： {(几小时/)(几分钟/)(几秒钟)}
    // 返回值： {(几分钟/)(几秒钟)}
    // 返回值： {(几秒钟)}
    // (注：start == end 时) 返回值： {0}
    public static String getTimespanDifference(long start, long end) {
        String szReturn = "{";
        long between = end - start;
        //LogUtils.d(TAG, "between is " + Long.toString(between));
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        /* 调试数据
         day = 0;
         hour = 2;
         min = 0;
         s = 7;
         */

        //long ms = (between - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000
        //- min * 60 * 1000 - s * 1000);

        szReturn += day > 0 ? String.format(java.util.Locale.getDefault(), "%d☀", day) : "";
        szReturn += hour > 0 || day > 0 ? String.format(java.util.Locale.getDefault(), "%d★", hour) : "";
        szReturn += min > 0 || hour > 0 || day > 0 ? String.format(java.util.Locale.getDefault(), "%d✰", min) : "";
        szReturn += min > 0 || hour > 0 || day > 0 ? String.format(java.util.Locale.getDefault(), "%d}", s) : "☆}";

        //String strmin = String.format("%02d", min);
        //String strs = String.format("%02d", s);
        //String strms = String.format("%03d",ms);
        //String timeDifference = day + "天" + hour + "小时" + strmin + "分" + strs + "秒" + strms + "毫秒";
        //String timeDifference = hour + getString(R.string.activity_main_msg_hour)
        //    + strmin + getString(R.string.activity_main_msg_minute)
        //    + strs + getString(R.string.activity_main_msg_second);
        //return timeDifference;

        return szReturn;
    }

    // 调试函数： 调试formatPCMListString(ArrayList<Long> lnTime)
    //
    /*public static String formatPCMListString_test() {
     // 调试数据
     ArrayList<Long> listTime = new ArrayList<Long>();
     Time t1 = new Time();
     //t.set(int second, int minute, int hour, int monthDay, int month, int year) {}
     t1.set(0, 8, 0, 27, 4, 2022);
     long ntime1 = t1.toMillis(true);
     listTime.add(ntime1);
     for (int i = 0; i < 5; i++) {
     Time t2 = new Time();
     //t.set(int second, int minute, int hour, int monthDay, int month, int year) {}
     t2.set(4, 8 + i + 1, 0, 27, 4, 2022);
     long ntime2 = t2.toMillis(true);
     listTime.add(ntime2);
     }

     return formatPCMListString(listTime);
     //LogUtils.d(TAG,  StringUtils.formatPCMListString(listTime));

     }*/
}
