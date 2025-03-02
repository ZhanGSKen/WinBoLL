package cc.winboll.studio.contacts.bobulltoon;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 13:47:48
 * @Describe 汤姆猫管家 ：使用 BoBullToon 项目，对通讯地址进行筛选判断的好朋友。
 */
import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TomCat {

    public static final String TAG = "TomCat";

    List<String> listPhoneBoBullToon = new ArrayList<String>();

    static volatile TomCat _TomCat;
    Context mContext;
    TomCat(Context context) {
        mContext = context;
    }

    public static synchronized TomCat getInstance(Context context) {
        if (_TomCat == null) {
            _TomCat = new TomCat(context);
        }
        return _TomCat;
    }

    void downloadAndExtractZip(String zipUrl, String destinationFolder) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(zipUrl)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 下载 ZIP 文件到临时位置
            File tempZipFile = File.createTempFile("temp", ".zip");
            try (InputStream inputStream = response.body().byteStream();
            FileOutputStream outputStream = new FileOutputStream(tempZipFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            // 解压 ZIP 文件到指定文件夹
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(tempZipFile.toPath()))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    Path targetFilePath = Paths.get(destinationFolder, zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(targetFilePath);
                    } else {
                        Files.createDirectories(targetFilePath.getParent());
                        try (FileOutputStream fos = new FileOutputStream(targetFilePath.toFile())) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zipInputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zipInputStream.closeEntry();
                }
            }

            // 删除临时 ZIP 文件
            tempZipFile.delete();
        }
    }

    public boolean downloadBoBullToon() {
        String zipUrl = "https://gitea.winboll.cc//Studio/BoBullToon/archive/main.zip"; // 替换为实际的 ZIP 文件 URL
        String destinationFolder = getWorkingFolder().getPath(); // 替换为实际的目标文件夹路径
        try {
            downloadAndExtractZip(zipUrl, destinationFolder);
            LogUtils.d(TAG, "ZIP 文件下载并解压成功。");
            return true;
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return false;
    }

    File getWorkingFolder() {
        return mContext.getExternalFilesDir(TAG);
    }

    public boolean loadPhoneBoBullToon() {
        listPhoneBoBullToon.clear();
        File fBoBullToon = new File(getWorkingFolder(), "bobulltoon");
        if (fBoBullToon.exists()) {
            LogUtils.d(TAG, String.format("getWorkingFolder() %s", getWorkingFolder()));
            for (File userFolder : fBoBullToon.listFiles()) {
                if (userFolder.isDirectory()) {
                    for (File recordFile : userFolder.listFiles()) {
                        listPhoneBoBullToon.add(recordFile.getName());
                    }
                }
            }

            for (int i = 0; i < listPhoneBoBullToon.size(); i++) {
                LogUtils.d(TAG, String.format("listPhoneBoBullToon add : %s", listPhoneBoBullToon.get(i)));
            }
            return true;
        } else {
            LogUtils.d(TAG, "fBoBullToon not exists。");
        }
        return false;
    }

    public boolean isPhoneBoBullToon(String phone) {
        for (int i = 0; i < listPhoneBoBullToon.size(); i++) {
            LogUtils.d(TAG, String.format("isPhoneBoBullToon(...) get(i) phone : %s", listPhoneBoBullToon.get(i)));
            if (listPhoneBoBullToon.get(i).equals(phone)) {
                return true;
            }
        }
        return false;
    }
}
