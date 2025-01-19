package cc.winboll.studio.powerbell.dialogs;
import cc.winboll.studio.powerbell.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.activities.BackgroundPictureActivity;
import cc.winboll.studio.powerbell.utils.BackgroundPictureUtils;
import cc.winboll.studio.powerbell.utils.FileUtils;
import cc.winboll.studio.powerbell.utils.UriUtil;
import cc.winboll.studio.shared.log.LogUtils;
import java.io.File;
import java.io.IOException;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/04/25 16:27:53
 * @Describe 背景图片的接收分享文件后的预览对话框
 */
public class BackgroundPicturePreviewDialog extends Dialog {

    public static final String TAG = "BackgroundPicturePreviewDialog";

    Context mContext;
    BackgroundPictureUtils mBackgroundPictureUtils;
    Button dialogbackgroundpicturepreviewButton1;
    Button dialogbackgroundpicturepreviewButton2;
    String mszPreReceivedFileName;

    public BackgroundPicturePreviewDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_backgroundpicturepreview);
        mContext = context;
        mBackgroundPictureUtils = ((BackgroundPictureActivity)context).mBackgroundPictureUtils;
        
        ImageView imageView = findViewById(R.id.dialogbackgroundpicturepreviewImageView1);
        copyAndViewRecivePicture(imageView);

        dialogbackgroundpicturepreviewButton1 = findViewById(R.id.dialogbackgroundpicturepreviewButton1);
        dialogbackgroundpicturepreviewButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 不使用分享到的图片
                    // 跳转到主窗口
                    Intent i = new Intent(mContext, MainActivity.class);
                    mContext.startActivity(i);
                }
            });

        dialogbackgroundpicturepreviewButton2 = findViewById(R.id.dialogbackgroundpicturepreviewButton2);
        dialogbackgroundpicturepreviewButton2.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    // 使用分享到的图片
                    //
                    //LogUtils.d(TAG, "mszReceivedFileName : " + mszReceivedFileName);
                    ((IOnRecivedPictureListener)mContext).onAcceptRecivedPicture(mszPreReceivedFileName);
                    // 关闭对话框
                    dismiss();
                }
            });
    }

    void copyAndViewRecivePicture(ImageView imageView) {
        //AppConfigUtils appConfigUtils = AppConfigUtils.getInstance((GlobalApplication)mContext.getApplicationContext());
        BackgroundPictureActivity activity = ((BackgroundPictureActivity)mContext);

        //取出文件uri
        Uri uri = activity.getIntent().getData();
        if (uri == null) {
            uri = activity.getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        }
        //获取文件真实地址
        String szSrcImage = UriUtil.getFilePathFromUri(mContext, uri);
        if (TextUtils.isEmpty(szSrcImage)) {
            Toast.makeText(mContext, "接收到的文件为空。", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        File fSrcImage = new File(szSrcImage);
        mszPreReceivedFileName = "PreReceived.data";
        //mszPreReceivedFileName = DateUtils.getDateNowString() + "-" + fSrcImage.getName();
        File mfPreReceivedPhoto = new File(activity.mBackgroundPictureUtils.getBackgroundDir(), mszPreReceivedFileName);
        // 复制源图片到剪裁文件
        try {
            FileUtils.copyFileUsingFileChannels(fSrcImage, mfPreReceivedPhoto);
            LogUtils.d(TAG, "copyFileUsingFileChannels");
            Drawable drawable = Drawable.createFromPath(mfPreReceivedPhoto.getPath());
            imageView.setBackground(drawable);
            LogUtils.d(TAG, "mszPreReceivedFileName : " + mszPreReceivedFileName);
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }


    //
    // 创建图片背景图片目录
    //
    boolean createBackgroundFolder2(String szBackgroundFolder) {
        // 文件路径参数为空值或无效值时返回false.
        if (szBackgroundFolder == null | szBackgroundFolder.equals("")) {
            return false;
        }

        LogUtils.d(TAG, "Background Folder Is : " + szBackgroundFolder);
        File f = new File(szBackgroundFolder);
        if (f.exists()) {
            if (f.isDirectory()) {
                return true;
            } else {
                // 工作路径不是一个目录
                LogUtils.d(TAG, "createImageWorkFolder() error : szImageCacheFolder isDirectory return false. -->" + szBackgroundFolder);
                return false;
            }
        } else {
            return f.mkdirs();
        }
    }

    public interface IOnRecivedPictureListener {
        void onAcceptRecivedPicture(String szBackgroundFileName);
    }

}
