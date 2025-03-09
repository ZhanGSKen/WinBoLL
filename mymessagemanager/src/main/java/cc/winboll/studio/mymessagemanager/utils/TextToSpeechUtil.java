package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/03 10:27:46
 * @Describe TTS语音播放工具类
 */
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.TTSSpeakTextBean;
import cc.winboll.studio.shared.log.LogUtils;
import java.util.ArrayList;

public class TextToSpeechUtil {

    public static final String TAG = "TextToSpeechUtil";

    public static final String UNIQUE_ID = "UNIQUE_ID";

    static TextToSpeechUtil _mTextToSpeechUtil;

    View mView;
    WindowManager mWindowManager;
    TextToSpeech mTextToSpeech;
    Context mContext;
    volatile boolean isExist = false;

    TextToSpeechUtil(Context context) {
        mContext = context;
        // 获取WindowManager
        mWindowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
    }

    public static TextToSpeechUtil getInstance(Context context) {
        if (_mTextToSpeechUtil == null) {
            _mTextToSpeechUtil = new TextToSpeechUtil(context);
        }
        return _mTextToSpeechUtil;
    }

    //
    // 播放 TTS 语音队列
    //
    public void speekTTSList(final ArrayList<TTSSpeakTextBean> listTTSSpeakTextBean) {
        // 重置播放退出标志位
        isExist = false;

        // 开始播放
        if (mTextToSpeech == null) {
            //ToastUtils.show("mTextToSpeech == null");
            // 创建TextToSpeech实例
            mTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i == TextToSpeech.SUCCESS) {
                            speekTTSList(listTTSSpeakTextBean);
                        } else {
                            LogUtils.d(TAG, "TTS init failed : " + Integer.toString(i) + ". The app [https://play.google.com/store/apps/details?id=com.google.android.tts] maybe fix this TTS probrem. ");
                        }
                    }
                });
            mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
        } else {
            if (mTextToSpeech != null && listTTSSpeakTextBean != null && listTTSSpeakTextBean.size() > 0) {
                // 清理过期的悬浮窗
                if (mWindowManager != null && mView != null) {
                    try {
                        mWindowManager.removeView(mView);
                        mView = null;
                    } catch(Exception e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
                
                // 显示悬浮窗
                initWindow();
                
                // 播放 TTS 语音
                //
                //ToastUtils.show("initWindow done.");
                // 设置延迟间隔
                int  nDelay = listTTSSpeakTextBean.get(0).mnDelay;
                try {
                    Thread.sleep(nDelay);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
                //ToastUtils.show("Delay done.");
                for (int speakPosition = 0; speakPosition < listTTSSpeakTextBean.size() && !isExist; speakPosition++) {
                    // 播放语音
                    String szSpeakContent = listTTSSpeakTextBean.get(speakPosition).mszSpeakContent;
                    isExist = (listTTSSpeakTextBean.size() - 2 < speakPosition);
                    //ToastUtils.show("for isExist is : " + Boolean.toString(isExist));
                    if (speakPosition == 0) {
                        mTextToSpeech.speak(szSpeakContent, TextToSpeech.QUEUE_FLUSH, null, UNIQUE_ID);
                    } else {
                        mTextToSpeech.speak(szSpeakContent, TextToSpeech.QUEUE_ADD, null, UNIQUE_ID);
                    }
                    //ToastUtils.show("mTextToSpeech.speak");
                }
            }
        }
    }

    UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            LogUtils.d(TAG, "播放开始");
        }

        @Override
        public void onDone(String utteranceId) {
            LogUtils.d(TAG, "播放结束");
            //ToastUtils.show("isExist is : " + Boolean.toString(isExist));
            // 关闭悬浮窗
            if (isExist && mWindowManager != null && mView != null) {
                LogUtils.d(TAG, "关闭悬浮窗");
                mWindowManager.removeView(mView);
            }
        }

        @Override
        public void onError(String utteranceId) {
            LogUtils.d(TAG, "播放出错");
        }
    };


    //
    // 初始化 TTS 悬浮窗
    //
    private void initWindow() {
        //ToastUtils.show("initWindow");
        // 创建布局参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        //这里需要进行不同的设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //设置透明度
        params.alpha = 0.9f;
        //设置内部视图对齐方式
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        //窗口的右上角角坐标
        params.x = 20;
        params.y = 20;
        //是指定窗口的像素格式为 RGBA_8888。
        //使用 RGBA_8888 像素格式的窗口可以在保持高质量图像的同时实现透明度效果。
        params.format = PixelFormat.RGBA_8888;
        //设置窗口的宽高,这里为自动
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //这段非常重要，是后续是否穿透点击的关键
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  //表示悬浮窗口不需要获取焦点，这样用户点击悬浮窗口以外的区域，就不需要关闭悬浮窗口。
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//表示悬浮窗口不会阻塞事件传递，即用户点击悬浮窗口以外的区域时，事件会传递给后面的窗口处理。
        //这里的引入布局文件的方式，也可以动态添加控件
        mView = View.inflate(mContext, R.layout.view_tts_back, null);
        LinearLayout llMain = mView.findViewById(R.id.viewttsbackLinearLayout1);
        llMain.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    //ToastUtils.show("onClick");
                    isExist = true;
                    if (mTextToSpeech != null) {
                        mTextToSpeech.stop();
                    }
                    if (mWindowManager != null && mView != null) {
                        mWindowManager.removeView(mView);
                        mView = null;
                    }
                }
            });
        mWindowManager.addView(mView, params);
    }
}
