package cc.winboll.studio.mymessagemanager.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.TTSSpeakTextBean;
import cc.winboll.studio.mymessagemanager.views.DraggableView;
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
        mWindowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
    }

    public static TextToSpeechUtil getInstance(Context context) {
        if (_mTextToSpeechUtil == null) {
            _mTextToSpeechUtil = new TextToSpeechUtil(context);
        }
        return _mTextToSpeechUtil;
    }

    public void speekTTSList(final ArrayList<TTSSpeakTextBean> listTTSSpeakTextBean) {
        isExist = false;

        if (mTextToSpeech == null) {
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
                if (mWindowManager != null && mView != null) {
                    try {
                        mWindowManager.removeView(mView);
                        mView = null;
                    } catch (Exception e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }

                initWindow(); // 已同步尺寸和位置

                int nDelay = listTTSSpeakTextBean.get(0).mnDelay;
                try {
                    Thread.sleep(nDelay);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }

                for (int speakPosition = 0; speakPosition < listTTSSpeakTextBean.size() && !isExist; speakPosition++) {
                    String szSpeakContent = listTTSSpeakTextBean.get(speakPosition).mszSpeakContent;
                    isExist = (listTTSSpeakTextBean.size() - 2 < speakPosition);
                    if (speakPosition == 0) {
                        mTextToSpeech.speak(szSpeakContent, TextToSpeech.QUEUE_FLUSH, null, UNIQUE_ID);
                    } else {
                        mTextToSpeech.speak(szSpeakContent, TextToSpeech.QUEUE_ADD, null, UNIQUE_ID);
                    }
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

    private void initWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // 窗口类型适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 基础配置
        params.alpha = 0.9f;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
			| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.gravity = Gravity.LEFT | Gravity.TOP; // 与保存的左上角坐标匹配

        // 核心修改1：同步DraggableView保存的尺寸（宽高完全一致）
        int[] savedSize = DraggableView.getLastViewSize(mContext);
        params.width = savedSize[0]; // 同步宽度
        params.height = savedSize[1]; // 同步高度

        // 核心修改2：同步DraggableView保存的位置
        int[] savedPosition = DraggableView.getLastPosition(mContext);
        params.x = savedPosition[0]; // 同步X坐标
        params.y = savedPosition[1]; // 同步Y坐标

        // 加载布局（view_tts_back.xml与DraggableView一致，确保样式统一）
        mView = View.inflate(mContext, R.layout.view_tts_back, null);
        LinearLayout llMain = mView.findViewById(R.id.viewttsbackLinearLayout1);
        llMain.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
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

