package cc.winboll.studio.autoinstaller;

import android.os.FileObserver;
import cc.winboll.studio.libappbase.LogUtils;

public class FileListener extends FileObserver {
	public final static String TAG = "FileListener";

    public EventCallback callback;

    public FileListener(String path) {
		super(path);
		LogUtils.i(TAG, "File Listener to : " + path);
    }
    public void setEventCallback(EventCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onEvent(int event, String path) {
        //LogUtils.d(TAG, "path : "+path);


        //int e = event & FileObserver.ALL_EVENTS;
        int e = event & FileObserver.CLOSE_WRITE;
        //LogUtils.d(TAG, "event->e:"+e);
        switch (e) {
            case FileObserver.ACCESS:
                //LogUtils.d(TAG, "文件操作___" + e + "__1打开文件后读取文件的操作");
                break;
            case FileObserver.MODIFY:
                //LogUtils.d(TAG, "文件操作___" + e + "__2文件被修改");
                break;
            case FileObserver.ATTRIB:
                //LogUtils.d(TAG, "文件操作___" + e + "__4属性变化");
                break;
            case FileObserver.CLOSE_WRITE:
                //LogUtils.d(TAG, "文件操作___" + e + "__8文件写入或编辑后关闭");
				callback.onEvent(path);
                break;
            case FileObserver.CLOSE_NOWRITE:
                //录音时，最后一个有效回调是这个
                //LogUtils.d(TAG, "文件操作___" + e + "__16只读文件被关闭");

                //callback.onEvent(path);


                break;
            case FileObserver.OPEN:
                //LogUtils.d(TAG, "文件操作___" + e + "__32文件被打开");
                break;
            case FileObserver.MOVED_FROM:
                //LogUtils.d(TAG, "文件操作___" + e + "__64移出事件");//试了重命名先MOVED_FROM再MOVED_TO
                break;
            case FileObserver.MOVED_TO:
                //LogUtils.d(TAG, "文件操作___" + e + "__128移入事件");
                break;
            case FileObserver.CREATE:
                //LogUtils.d(TAG, "文件操作___" + e + "__256新建文件");//把文件移动给自己先CREATE在DELETE
                break;
            case FileObserver.DELETE:
                //LogUtils.d(TAG, "文件操作___" + e + "__512有删除文件");//把文件移出去DELETE
                break;
            case FileObserver.DELETE_SELF:
                //LogUtils.d(TAG, "文件操作___" + e + "__1024监听的这个文件夹被删除");
                break;
            case FileObserver.MOVE_SELF:
                //LogUtils.d(TAG, "文件操作___" + e + "__2048监听的这个文件夹被移走");
                break;
            case FileObserver.ALL_EVENTS:
                //LogUtils.d(TAG, "文件操作___" + e + "__4095全部操作");
                break;
        }
    }

	public interface EventCallback {
        void onEvent(String path);
    }

}
