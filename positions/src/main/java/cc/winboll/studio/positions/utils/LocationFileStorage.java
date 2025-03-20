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
import cc.winboll.studio.positions.models.PostionModel;
import cc.winboll.studio.positions.App;
import java.io.File;

public class LocationFileStorage {
    public static final String TAG = "LocationFileStorage";

    static final String FILE_NAME = "locations.json";

    public static void saveToFile(Context context, ArrayList<PostionModel> locations) {
        try {
            PostionModel.saveBeanListToFile(getDataPath(), locations);
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public static ArrayList<PostionModel> loadFromFile(Context context) {
        ArrayList<PostionModel> result = new ArrayList<PostionModel>();
        try {
            PostionModel.loadBeanListFromFile(getDataPath(), result, PostionModel.class);
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return result;
    }
    
    static String getDataPath() {
        return App.szDataFolder + File.separator + FILE_NAME;
    }
}
