package cc.winboll.studio.libapputils.git;
import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/09 12:30:59
 * @Describe 应用文件接口基础类
 */
public interface IAPPFiles {

    public static final String TAG = "IAPPFiles";
    public static final String UUID_LOGUTILS_JSON = "cef58919-88a6-47c4-9e10-44d8c810328e";
    public static final String UUID_WINBOLLCLIENTSERVICEBEAN_JSON = "d7816eda-9724-4a86-b9d5-43eeee7be76d";

    public static final String APPRoot = "";

    HashFile getFile(String szAPPFilesURI);

    class HashFile {
        static Map<String, String> _mapFiles = new HashMap<String, String>();
        
        volatile static HashFile _mHashFile;
        File mFile;
        static String _mFilesRoot = "";

        HashFile(Context context) {
            _mapFiles.put(UUID_WINBOLLCLIENTSERVICEBEAN_JSON, "/BaseBean/cc.winboll.studio.shared.service.WinBoLLClientServiceBean.json");
            _mapFiles.put(UUID_LOGUTILS_JSON, "/LogUtils/LogUtils.json");
        }

        synchronized static HashFile getInstance(Context context, String uuid) throws IOException {
            if (_mHashFile == null) {
                _mHashFile = new HashFile(context);
            }
            
            try {
                _mHashFile.mFile = new File(context.getExternalFilesDir("") + _mapFiles.get(uuid));
            } catch (Exception e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            } finally {
                if (_mHashFile.mFile == null) {
                    LogUtils.d(TAG, "_mHashFile.mFile == null");
                }
            }
            return _mHashFile;
        }

        static File getFileByUUID(Context context, String uuid) throws IOException {
            return new File(getInstance(context, uuid)._mapFiles.get(uuid));
        }
    }
    
    //class HashFileManager
}
