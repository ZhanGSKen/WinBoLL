package cc.winboll.studio.gallery;

import android.net.Uri;
import cc.winboll.studio.libappbase.LogUtils;

public class Album {
    public static final String TAG = "Album";
    private String name;
    private String path;
    private Uri coverUri;
    private int imageCount;

    public Album(String name, String path, Uri coverUri, int imageCount) {
        LogUtils.d(TAG, "Album created: " + name);
        this.name = name;
        this.path = path;
        this.coverUri = coverUri;
        this.imageCount = imageCount;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public Uri getCoverUri() { return coverUri; }
    public int getImageCount() { return imageCount; }
}