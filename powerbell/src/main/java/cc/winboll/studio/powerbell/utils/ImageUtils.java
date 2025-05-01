package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtils {

    public static final String TAG = ImageUtils.class.getSimpleName();

    // 这里我们生成了一个Pic文件夹，在下面放了我们质量压缩后的图片，用于和原图对比
    // 压缩图片使用Bitmap.compress()，这里是质量压缩
    // 参数：Context context ：调用本函数函数引用的资源体系
    //      String szSrcImagePath ：要压缩的源文件路径
    //      String szDstImagePath ：压缩后文件要保存的路径
    //      int nPictureCompress ：图片压缩比例
    public static void bitmapCompress(Context context, String szSrcImagePath, String szDstImagePath, int nPictureCompress) {
        try {
            Bitmap bmpCompressImage;

            //生成新的文件
            File fDstCompressImage = new File(szDstImagePath);

            //裁剪后的图像转成BitMap
            //photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriClipUri));
            bmpCompressImage = BitmapFactory.decodeFile(szSrcImagePath);

            //创建输出流
            OutputStream out = null;

            out = new FileOutputStream(fDstCompressImage.getPath());

            //压缩文件，返回结果，参数分别是压缩的格式，压缩质量的百分比，输出流
            boolean bCompress = bmpCompressImage.compress(Bitmap.CompressFormat.JPEG, nPictureCompress, out);

            // 复制压缩后的文件到源路径
            File fSrcImage = new File(szSrcImagePath);
            FileUtils.copyFileUsingFileChannels(fDstCompressImage, fSrcImage);
            LogUtils.d(TAG, Integer.toString(nPictureCompress) + "%压缩结束。");

        } catch (FileNotFoundException e) {
            LogUtils.d(TAG, "bitmapCompress FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            LogUtils.d(TAG, "bitmapCompress IOException : " + e.getMessage());
        }
    }
}
