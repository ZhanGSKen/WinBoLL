package cc.winboll.studio.positions.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 03:02:17
 * @Describe LocationFileStorage
 */
import android.content.Context;
import android.location.Location;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.positions.beans.LocationJson;

public class LocationFileStorage {
    public static final String TAG = "LocationFileStorage";

    private static final String FILE_NAME = "locations.json";

    public static void saveToFile(Context context, ArrayList<LocationJson> locations) {
        try {
            LocationJson.saveBeanList(context, locations, LocationJson.class);
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public static ArrayList<LocationJson> loadFromFile(Context context) {
        ArrayList<LocationJson> result = new ArrayList<LocationJson>();
        try {
            LocationJson.loadBeanList(context, result, LocationJson.class);
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return result;
    }
}
