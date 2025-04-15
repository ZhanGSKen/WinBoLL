package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.activities.BackgroundPictureActivity;
import cc.winboll.studio.powerbell.beans.BackgroundPictureBean;
import cc.winboll.studio.powerbell.dialogs.BackgroundPicturePreviewDialog;
import cc.winboll.studio.powerbell.utils.BackgroundPictureUtils;
import cc.winboll.studio.powerbell.utils.FileUtils;
import cc.winboll.studio.powerbell.utils.UriUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BackgroundPictureActivity extends Activity
implements BackgroundPicturePreviewDialog.IOnRecivedPictureListener {

    public static final String TAG = "BackgroundPictureActivity";

    public BackgroundPictureUtils mBackgroundPictureUtils;

    // 图片选择请求
    public static final int REQUEST_SELECT_PICTURE = 0;
    // 照相选择请求
    public static final int REQUEST_TAKE_PHOTO = 1;
    // 图片裁剪选择请求
    public static final int REQUEST_CROP_IMAGE = 2;

    AToolbar mAToolbar;
    // 所有图片存储的文件夹
    File mfBackgroundDir;
    // 拍照与剪裁的文件夹
    File mfPictureDir;
    // 拍照文件类
    File mfTakePhoto;
    // 接收到的图片文件类
    public File mfRecivedPicture;
    // 剪裁文件类
    File mfTempCropPicture;
    // 剪裁接收后的文件的文件名
    public static String _mszRecivedCropPicture = "RecivedCrop.jpg";
    File mfRecivedCropPicture;
    static String _mszCommonFileType = "jpeg";
    // 背景图片的压缩比
    int mnPictureCompress = 100;
    static String _RecivedPictureFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backgroundpicture);
        initEnv();

        mBackgroundPictureUtils = BackgroundPictureUtils.getInstance(this);
        mfBackgroundDir = new File(mBackgroundPictureUtils.getBackgroundDir());
        if (!mfBackgroundDir.exists()) {
            mfBackgroundDir.mkdirs();
        }
        //mfPictureDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_projectname));
        mfPictureDir = new File(App.getTempDirPath());
        if (!mfPictureDir.exists()) {
            mfPictureDir.mkdirs();
        }
        mfTakePhoto = new File(mfPictureDir, "TakePhoto.jpg");
        mfTempCropPicture = new File(mfPictureDir, "TempCrop.jpg");
        mfRecivedPicture = getRecivedPictureFile(this);
        mfRecivedCropPicture = new File(mfBackgroundDir, _mszRecivedCropPicture);

        // 初始化工具栏
        mAToolbar = (AToolbar) findViewById(R.id.toolbar);
        setActionBar(mAToolbar);
        //mAToolbar.setTitle(getTitle() + "-" + getString(R.string.subtitle_activity_backgroundpicture));
        mAToolbar.setSubtitle(R.string.subtitle_activity_backgroundpicture);
        //mAToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        //mAToolbar.setSubtitleTextAppearance(this, R.style.Toolbar_SubTitleText);
        //mAToolbar.setBackgroundColor(getColor(R.color.colorPrimary));
        setActionBar(mAToolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mAToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        //给按钮设置点击事件
        findViewById(R.id.activitybackgroundpictureAButton5).setOnClickListener(onOriginNullClickListener);
        findViewById(R.id.activitybackgroundpictureAButton4).setOnClickListener(onReceivedPictureClickListener);
        findViewById(R.id.activitybackgroundpictureAButton1).setOnClickListener(onTakePhotoClickListener);
        findViewById(R.id.activitybackgroundpictureAButton2).setOnClickListener(onSelectPictureClickListener);
        findViewById(R.id.activitybackgroundpictureAButton3).setOnClickListener(onCropPictureClickListener);
        findViewById(R.id.activitybackgroundpictureAButton6).setOnClickListener(onCropFreePictureClickListener);

        updatePreviewBackground();



        // 判断并且处理应用分享到的文件
        //
        //ToastUtils.show("Activity Opened.");

        // 预备接收参数
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //LogUtils.d(TAG, "action : " + action);
        //LogUtils.d(TAG, "type : " + type);

        // 判断是否进入图片分享状态
        if (Intent.ACTION_SEND.equals(action)
            && type != null
            && ("image/*".equals(type) || "image/jpeg".equals(type) || "image/jpg".equals(type) || "image/png".equals(type) || "image/webp".equals(type))) {
            // 预览图片
            BackgroundPicturePreviewDialog dlg= new BackgroundPicturePreviewDialog(this);
            dlg.show();
        }
    }
    
    void initEnv() {
        _RecivedPictureFileName = "Recived.data";
    }

    public static String getBackgroundFileName() {
        return _mszRecivedCropPicture;
    }

    @Override
    public void onAcceptRecivedPicture(String szPreRecivedPictureName) {
        //ToastUtils.show("onAcceptRecivedPicture");
        BackgroundPictureUtils utils = BackgroundPictureUtils.getInstance(this);
        utils.getBackgroundPictureBean().setIsUseBackgroundFile(true);
        utils.saveData();
        File fPreRecivedPictureName = new File(utils.getBackgroundDir(), szPreRecivedPictureName);
        FileUtils.copyFile(fPreRecivedPictureName, mfRecivedPicture);
        // 加载背景
        startCropImageActivity(false);
    }
    
    //
    // 更新预览背景
    //
    public void updatePreviewBackground() {
        LogUtils.d(TAG, "updatePreviewBackground");
        ImageView ivPreviewBackground = findViewById(R.id.activitybackgroundpictureImageView1);
        BackgroundPictureUtils utils = BackgroundPictureUtils.getInstance(this);
        utils.loadBackgroundPictureBean();
        boolean isUseBackgroundFile = utils.getBackgroundPictureBean().isUseBackgroundFile();
        if (isUseBackgroundFile && mfRecivedCropPicture.exists()) {
            try {
                String szBackgroundFilePath = utils.getBackgroundDir() + getBackgroundFileName();
                Drawable drawableBackground = FileUtils.getImageDrawable(szBackgroundFilePath);
                drawableBackground.setAlpha(120);
                ivPreviewBackground.setImageDrawable(drawableBackground);
                ToastUtils.show("Use acceptRecived background.");
            } catch (IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
        } else {
            ToastUtils.show(" No background.");
            Drawable drawableBackground = getDrawable(R.drawable.blank10x10);
            drawableBackground.setAlpha(120);
            ivPreviewBackground.setImageDrawable(drawableBackground);
        }
    }

    private View.OnClickListener onOriginNullClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 选择原始空白背景
            BackgroundPictureUtils utils = BackgroundPictureUtils.getInstance(BackgroundPictureActivity.this);
            BackgroundPictureBean bean = utils.getBackgroundPictureBean();
            bean.setIsUseBackgroundFile(false);
            utils.saveData();
            updatePreviewBackground();
        }
    };

    private View.OnClickListener onSelectPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 导入外部图片
            Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_SELECT_PICTURE);
        }
    };

    private View.OnClickListener onCropPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File fCheck = new File(mfBackgroundDir, getBackgroundFileName());
            if (fCheck.exists()) {
                startCropImageActivity(false);
            } else {
                ToastUtils.show("There is not any picture to crop.");
            }
        }
    };

    private View.OnClickListener onCropFreePictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File fCheck = new File(mfBackgroundDir, getBackgroundFileName());
            if (fCheck.exists()) {
                startCropImageActivity(true);
            } else {
                ToastUtils.show("There is not any picture to crop.");
            }
        }
    };

    private View.OnClickListener onTakePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.d(TAG, "onTakePhotoClickListener");
            LogUtils.d(TAG, "mfTakePhoto : " + mfTakePhoto.getPath());
            if (mfTakePhoto.exists()) {
                mfTakePhoto.delete();
            }
            try {
                mfTakePhoto.createNewFile();
            } catch (IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    };

    private View.OnClickListener onReceivedPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 选择接收到的背景图片
            BackgroundPictureUtils utils = BackgroundPictureUtils.getInstance(BackgroundPictureActivity.this);
			utils.getBackgroundPictureBean().setIsUseBackgroundFile(true);
            utils.saveData();
            updatePreviewBackground();
        }
    };

    void compressQualityToRecivedPicture(Bitmap bitmap) {
        // 设置输出流
        OutputStream outStream = null;
        try {
            // 创建输出流对象，准备写入压缩后的图片文件
            mfRecivedPicture = getRecivedPictureFile(this);
            // 创建新的接收文件
            if (!mfRecivedPicture.exists()) {
                mfRecivedPicture.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(mfRecivedPicture);

            // 获取输出流对象
            outStream = new BufferedOutputStream(fos);

            // 使用默认的质量参数压缩图片
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream); // 70% 的质量

            // 关闭输出流以完成文件操作
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public void startCropImageActivity(boolean isCropFree) {
        LogUtils.d(TAG, "startCropImageActivity");
        BackgroundPictureBean bean = mBackgroundPictureUtils.loadBackgroundPictureBean();
        mfRecivedPicture = getRecivedPictureFile(this);
        Uri uri = UriUtil.getUriForFile(this, mfRecivedPicture);
        LogUtils.d(TAG, "uri : " + uri.toString());
        if (mfTempCropPicture.exists()) {
            mfTempCropPicture.delete();
        }
        try {
            mfTempCropPicture.createNewFile();
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        // 使用正确的文件路径构建 Uri
        Uri cropOutPutUri = Uri.fromFile(mfTempCropPicture);
        LogUtils.d(TAG, "mfTempCropPicture : " + mfTempCropPicture.getPath());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/" + _mszCommonFileType);
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("noFaceDetection", true);
        if (!isCropFree) {
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", bean.getBackgroundWidth());
            intent.putExtra("aspectY", bean.getBackgroundHeight());
        }
        // outputX outputY 是裁剪图片宽高
        //intent.putExtra("outputX", 100);
        //intent.putExtra("outputY", 100);
        //return-data =false 意味着裁剪成功后不能在onActivityResult 的intent 中获得图片
        //intent.putExtra("return-data", false);
        intent.putExtra("return-data", true);
        //裁剪后的图片输出至  cropOutPutUri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropOutPutUri);
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CROP_IMAGE);
    }

    // 启动裁剪窗口，裁剪操作文件为 uirImage
    //
    void sharePicture() {
        Uri uri = UriUtil.getUriForFile(this, mfRecivedPicture);
        Intent shareIntent = new Intent();    
        shareIntent.setAction(Intent.ACTION_SEND);    
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);    
        shareIntent.setType("image/" + _mszCommonFileType);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    public static File getRecivedPictureFile(Context context) {
        BackgroundPictureUtils utils = BackgroundPictureUtils.getInstance(context);
        utils.loadBackgroundPictureBean();
        return new File(utils.getBackgroundDir(), _RecivedPictureFileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PICTURE) {
            // 处理选择后图片
            if (resultCode == RESULT_OK) {
                try {
                    Uri selectedImage = data.getData(); 
                    LogUtils.d(TAG, "Uri is : " + selectedImage.toString());
                    File fSrcImage = new File(UriUtil.getFilePathFromUri(this, selectedImage));
                    mfRecivedPicture = getRecivedPictureFile(this);

                    FileUtils.copyFile(fSrcImage, mfRecivedPicture);
                    // 启动剪裁文件窗口
                    startCropImageActivity(false);
                } catch (Exception e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                LogUtils.d(TAG, "REQUEST_TAKE_PHOTO");
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                compressQualityToRecivedPicture(imageBitmap);
                startCropImageActivity(false);
            }
        } else if (requestCode == REQUEST_CROP_IMAGE) {
            if (resultCode == RESULT_OK) {
                LogUtils.d(TAG, "CROP_IMAGE_REQUEST_CODE");
                FileUtils.copyFile(mfTempCropPicture, mfRecivedCropPicture);
                mfTempCropPicture.delete();
                mBackgroundPictureUtils.getBackgroundPictureBean().setIsUseBackgroundFile(true);
                updatePreviewBackground();
            }

        } else {
            String sz = "Unsolved requestCode = " + Integer.toString(requestCode);
            Toast.makeText(getApplication(), sz, Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, sz);
        }
    }
}
