package cc.winboll.studio.libaes.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenUtil {

    // 分辨率宽度和高度计量类
    //
    public static class ScreenSize {
        int widthPixels;
        int heightPixels;

        public ScreenSize(int widthPixels, int heightPixels) {
            this.widthPixels = widthPixels;
            this.heightPixels = heightPixels;
        }

        public void setWidthPixels(int widthPixels) {
            this.widthPixels = widthPixels;
        }

        public int getWidthPixels() {
            return widthPixels;
        }

        public void setHeightPixels(int heightPixels) {
            this.heightPixels = heightPixels;
        }

        public int getHeightPixels() {
            return heightPixels;
        }
    }

    // 获取屏幕分辨率宽度和高度
    //
    public static ScreenSize getScreenSize(Context mContext) {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return new ScreenSize(dm.widthPixels, dm.heightPixels);
    }

    // 获取屏幕宽度
    //
    public static int getScreenWidth(Context mContext) {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    // 获取屏幕高度
    //
    public static int getScreenHeight(Context mContext) {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

}
