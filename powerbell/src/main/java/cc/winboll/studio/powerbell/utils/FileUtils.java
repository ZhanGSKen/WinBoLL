package cc.winboll.studio.powerbell.utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import cc.winboll.studio.shared.log.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具类
 */

public class FileUtils {

    public static final String TAG = "FileUtils";

    //
    // 读取文件内容，作为字符串返回
    //
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } 

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        } 

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流  
        FileInputStream fis = new FileInputStream(filePath);  
        // 创建一个长度为10240的Buffer
        byte[] bbuf = new byte[10240];  
        // 用于保存实际读取的字节数  
        int hasRead = 0;  
        while ((hasRead = fis.read(bbuf)) > 0) {  
            sb.append(new String(bbuf, 0, hasRead));  
        }  
        fis.close();  
        return sb.toString();
    }

    //
    // 根据文件路径读取byte[] 数组
    //
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            BufferedInputStream in = null;

            try {
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                byte[] var7 = bos.toByteArray();
                return var7;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }

                bos.close();
            }
        }
    }

    //
    // 文件复制函数
    //
    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    /**
     * 将文件生成位图
     * @param path
     * @return
     * @throws IOException
     */
    public static BitmapDrawable getImageDrawable(String path)
    throws IOException {
        //打开文件
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int BUFFER_SIZE = 1000;
        byte[] bt = new byte[BUFFER_SIZE];

        //得到文件的输入流
        InputStream in = new FileInputStream(file);

        //将文件读出到输出流中
        int readLength = in.read(bt);
        while (readLength != -1) {
            outStream.write(bt, 0, readLength);
            readLength = in.read(bt);
        }

        //转换成byte 后 再格式化成位图
        byte[] data = outStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// 生成位图
        BitmapDrawable bd = new BitmapDrawable(bitmap);

        return bd;
    }
    
    public static boolean copyFile(File oldFile, File newFile) {
        //String oldPath = "path/to/original/file.txt";
        //String newPath = "path/to/new-location/for/file.txt";

        //File oldFile = new java.io.File(oldPath);
        //File newFile = new java.io.File(newPath);
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }

        if (!oldFile.exists()) {
            //System.out.println("The original file does not exist.");
            LogUtils.d(TAG, "The original file does not exist.");
        } else {
            try {
                // 源文件路径
                Path sourcePath = Paths.get(oldFile.getPath());
                // 目标文件路径
                Path destPath = Paths.get(newFile.getPath());
                if(newFile.exists()) {
                    newFile.delete();
                }
                Files.copy(sourcePath, destPath);
                LogUtils.d(TAG, "File copy successfully.");
                //System.out.println("File moved successfully.");
                return true;
            } catch (Exception e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                //System.err.println("An error occurred while moving the file: " + e.getMessage());
            }
        }
        return false;
    }

}
