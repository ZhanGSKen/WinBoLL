package cc.winboll.studio.powerbell.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/18 12:07:20
 * @Describe 背景图片工具集
 */
import android.content.Context;
import cc.winboll.studio.powerbell.beans.BackgroundPictureBean;
import java.io.File;

public class BackgroundPictureUtils {

    public static final String TAG = "BackgroundPictureUtils";

    static BackgroundPictureUtils _mBackgroundPictureUtils;
    Context mContext;
    BackgroundPictureBean mBackgroundPictureBean;
    // 背景图片目录
    String mszBackgroundDir;

    BackgroundPictureUtils(Context context) {
        mContext = context;
        String szExternalFilesDir = mContext.getExternalFilesDir(TAG) + File.separator;
        setBackgroundDir(szExternalFilesDir + "Background" + File.separator);
        loadBackgroundPictureBean();
    }

    public static BackgroundPictureUtils getInstance(Context context) {
        if (_mBackgroundPictureUtils == null) {
            _mBackgroundPictureUtils = new BackgroundPictureUtils(context);
        }
        return _mBackgroundPictureUtils;
    }

    //
    // 加载应用背景图片配置数据
    //
    public BackgroundPictureBean loadBackgroundPictureBean() {
        mBackgroundPictureBean = BackgroundPictureBean.loadBean(mContext, BackgroundPictureBean.class);
        if (mBackgroundPictureBean == null) {
            mBackgroundPictureBean = new BackgroundPictureBean();
            BackgroundPictureBean.saveBean(mContext, mBackgroundPictureBean);
        }
        return mBackgroundPictureBean;
    }


    void setBackgroundDir(String mszBackgroundDir) {
        this.mszBackgroundDir = mszBackgroundDir;
    }

    public String getBackgroundDir() {
        return mszBackgroundDir;
    }

    public BackgroundPictureBean getBackgroundPictureBean() {
        return mBackgroundPictureBean;
    }

    public void saveData() {
        BackgroundPictureBean.saveBean(mContext, mBackgroundPictureBean);
    }
}
