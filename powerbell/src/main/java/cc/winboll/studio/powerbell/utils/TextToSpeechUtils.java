package cc.winboll.studio.powerbell.utils;

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
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.TTSSpeakTextBean;
import java.util.ArrayList;

/**
 * TTS语音播放工具类 (单例实现)
 * 适配：Java7 语法规范 | Android API36 系统版本【修复崩溃】
 * 功能：队列播放语音文本 + 播放悬浮窗展示 + 点击悬浮窗停止播放/关闭悬浮窗
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/29 19:03
 */
public class TextToSpeechUtils {

    // ====================================== 常量区 - 静态全局常量 (置顶) ======================================
    public static final String TAG = "TextToSpeechUtils";
    public static final String UNIQUE_ID = "UNIQUE_ID";

    // ====================================== 单例实例 - 静态私有 (饿汉式优化) ======================================
    private static volatile TextToSpeechUtils sTextToSpeechUtils;

    // ====================================== 成员属性区 - 私有成员变量 (按功能归类 有序排列) ======================================
    private Context mContext;
    private WindowManager mWindowManager;
    private TextToSpeech mTextToSpeech;
    private View mView;
    private volatile boolean isExist = false;
    private UtteranceProgressListener mUtteranceProgressListener;

    // ====================================== 构造方法 - 私有私有化 (单例模式) ======================================
    private TextToSpeechUtils(Context context) {
        LogUtils.d(TAG, "【构造方法】初始化TextToSpeechUtil实例");
        this.mContext = context.getApplicationContext();
        this.mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        this.initUtteranceProgressListener();
        LogUtils.d(TAG, "【构造方法】初始化完成，获取WindowManager实例："+mWindowManager);
    }

    // ====================================== 对外暴露方法 - 单例获取入口 (线程安全) ======================================
    public static synchronized TextToSpeechUtils getInstance(Context context) {
        LogUtils.d(TAG, "【getInstance】获取单例实例，入参Context：" + context);
        if (sTextToSpeechUtils == null) {
            LogUtils.d(TAG, "【getInstance】实例为空，创建新的TextToSpeechUtil对象");
            sTextToSpeechUtils = new TextToSpeechUtils(context);
        }
        return sTextToSpeechUtils;
    }

    // ====================================== 核心对外业务方法 - 播放TTS语音队列 【主入口】 ======================================
    public void speekTTSList(final ArrayList<TTSSpeakTextBean> listTTSSpeakTextBean) {
        LogUtils.d(TAG, "【speekTTSList】播放语音队列调用，入参队列长度：" + (listTTSSpeakTextBean == null ? 0 : listTTSSpeakTextBean.size()));
        // 重置播放退出标志位
        isExist = false;
        LogUtils.d(TAG, "【speekTTSList】重置播放退出标志位 isExist = " + isExist);

        // TTS实例为空 → 初始化TTS后重放
        if (mTextToSpeech == null) {
            LogUtils.d(TAG, "【speekTTSList】TextToSpeech实例为空，开始初始化TTS");
            mTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
					@Override
					public void onInit(int initStatus) {
						LogUtils.d(TAG, "【onInit】TTS初始化回调，初始化状态码：" + initStatus);
						if (initStatus == TextToSpeech.SUCCESS) {
							LogUtils.d(TAG, "【onInit】TTS初始化成功，重新调用语音播放方法");
							speekTTSList(listTTSSpeakTextBean);
						} else {
							LogUtils.d(TAG, "【onInit】TTS init failed : " + initStatus + ". The app [https://play.google.com/store/apps/details?id=com.google.android.tts] maybe fix this TTS probrem. ");
						}
					}
				});
            mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
            LogUtils.d(TAG, "【speekTTSList】已为TTS绑定播放进度监听器");
        } else {
            // TTS实例就绪 → 执行播放逻辑
            if (listTTSSpeakTextBean != null && listTTSSpeakTextBean.size() > 0) {
                LogUtils.d(TAG, "【speekTTSList】TTS实例就绪，语音队列数据有效，开始播放逻辑处理");
                // 清理过期的悬浮窗 - 防止内存泄漏/重复添加
                clearFloatWindow();

                // ========== 修复1：添加悬浮窗权限检查，有权限才初始化悬浮窗，无权限则只播语音不崩溃 ==========
                if (checkOverlayPermission()) {
                    initWindow();
                    LogUtils.d(TAG, "【speekTTSList】悬浮窗初始化并显示完成");
                } else {
                    LogUtils.d(TAG, "【speekTTSList】悬浮窗权限未授予，跳过悬浮窗显示，仅播放语音");
                }

                // 获取第一条语音的延迟时间并休眠
                int nDelay = listTTSSpeakTextBean.get(0).mnDelay;
                LogUtils.d(TAG, "【speekTTSList】获取播放延迟时间：" + nDelay + "ms，开始休眠等待");
                try {
                    Thread.sleep(nDelay);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, "【speekTTSList】休眠等待被中断", e);
                }
                LogUtils.d(TAG, "【speekTTSList】休眠等待完成，开始循环播放语音队列");

                // 循环播放语音队列
                for (int speakPosition = 0; speakPosition < listTTSSpeakTextBean.size() && !isExist; speakPosition++) {
                    String szSpeakContent = listTTSSpeakTextBean.get(speakPosition).mszSpeakContent;
                    isExist = (listTTSSpeakTextBean.size() - 2 < speakPosition);
                    LogUtils.d(TAG, "【speekTTSList】播放索引：" + speakPosition + " | 播放文本：" + szSpeakContent + " | 当前退出标记位：" + isExist);

                    // 第一条语音清空队列播放，后续语音追加播放
                    if (speakPosition == 0) {
                        mTextToSpeech.speak(szSpeakContent, TextToSpeech.QUEUE_FLUSH, null, UNIQUE_ID);
                        LogUtils.d(TAG, "【speekTTSList】执行清空队列播放 → QUEUE_FLUSH");
                    } else {
                        mTextToSpeech.speak(szSpeakContent, TextToSpeech.QUEUE_ADD, null, UNIQUE_ID);
                        LogUtils.d(TAG, "【speekTTSList】执行追加队列播放 → QUEUE_ADD");
                    }
                }
                LogUtils.d(TAG, "【speekTTSList】语音队列循环播放逻辑执行完毕");
            } else {
                LogUtils.d(TAG, "【speekTTSList】语音队列为空/长度0，跳过播放逻辑");
            }
        }
    }

    // ====================================== 私有工具方法 - 初始化播放监听器 ======================================
    private void initUtteranceProgressListener() {
        LogUtils.d(TAG, "【initUtteranceProgressListener】初始化TTS播放进度监听器");
        mUtteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                LogUtils.d(TAG, "【onStart】TTS语音播放开始，唯一标识ID：" + utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                LogUtils.d(TAG, "【onDone】TTS语音播放结束，唯一标识ID：" + utteranceId + " | 退出标志位：" + isExist);
                // 播放完成 关闭悬浮窗
                if (isExist && mWindowManager != null && mView != null) {
                    LogUtils.d(TAG, "【onDone】满足关闭条件，执行悬浮窗移除操作");
                    clearFloatWindow();
                }
            }

            @Override
            public void onError(String utteranceId) {
                LogUtils.d(TAG, "【onError】TTS语音播放出错，唯一标识ID：" + utteranceId);
            }
        };
    }

    // ====================================== 私有核心方法 - 初始化并添加悬浮窗 【核心修复 根治崩溃】 ======================================
    private void initWindow() {
        LogUtils.d(TAG, "【initWindow】开始初始化播放悬浮窗");
        // 创建Window布局参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        // ========== 修复2 重中之重：Android 12(API31)+ 彻底废弃TYPE_PHONE，统一用TYPE_APPLICATION_OVERLAY 适配API36 ==========
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            LogUtils.d(TAG, "【initWindow】系统版本>=API26，悬浮窗类型：TYPE_APPLICATION_OVERLAY");
        } else {
            // 仅低版本用TYPE_PHONE，高版本不再走这里
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            LogUtils.d(TAG, "【initWindow】系统版本<API26，悬浮窗类型：TYPE_PHONE");
        }
        // 悬浮窗样式配置
        params.alpha = 0.9f;
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        params.x = 20;
        params.y = 20;
        params.format = PixelFormat.RGBA_8888;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 核心Flag：无焦点+不阻塞触摸事件穿透
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        // 加载悬浮窗布局
        mView = View.inflate(mContext, R.layout.view_tts_back, null);
        LinearLayout llMain = mView.findViewById(R.id.viewttsbackLinearLayout1);
        llMain.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View view) {
					LogUtils.d(TAG, "【onClick】悬浮窗被点击，执行停止播放+关闭悬浮窗");
					isExist = true;
					if (mTextToSpeech != null) {
						mTextToSpeech.stop();
						LogUtils.d(TAG, "【onClick】已调用TTS.stop()停止播放");
					}
					clearFloatWindow();
				}
			});
        // ========== 修复3：添加异常捕获+重复添加判断，防止addView时报错导致整个TTS播放崩溃 ==========
        try {
            if (mWindowManager != null && mView != null && mView.getWindowToken() == null) {
                mWindowManager.addView(mView, params);
                LogUtils.d(TAG, "【initWindow】悬浮窗添加到Window成功，布局加载完成");
            }
        } catch (Exception e) {
            LogUtils.d(TAG, "【initWindow】悬浮窗添加异常(权限/系统限制)，不影响语音播放：", e);
            mView = null;
        }
    }

    // ====================================== 私有工具方法 - 清理悬浮窗 (通用封装) 【优化修复】 ======================================
    private void clearFloatWindow() {
        LogUtils.d(TAG, "【clearFloatWindow】执行悬浮窗清理操作");
        if (mWindowManager != null && mView != null) {
            try {
                mWindowManager.removeView(mView);
                LogUtils.d(TAG, "【clearFloatWindow】悬浮窗移除成功");
            } catch (Exception e) {
                LogUtils.d(TAG, "【clearFloatWindow】悬浮窗移除异常", e);
            } finally {
                mView = null;
            }
        } else {
            LogUtils.d(TAG, "【clearFloatWindow】无需清理，WindowManager或View为空");
        }
    }

    // ====================================== ✅ 新增：悬浮窗权限检查【必须】Android6.0+ 强制校验 防止崩溃 ✅ ======================================
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasPermission = android.provider.Settings.canDrawOverlays(mContext);
            LogUtils.d(TAG, "【checkOverlayPermission】Android6.0+ 悬浮窗权限校验结果：" + hasPermission);
            return hasPermission;
        } else {
            // 低版本默认有权限
            return true;
        }
    }

    // ====================================== ✅ 新增：释放资源方法【根治内存泄漏】建议在Service/Activity销毁时调用 ✅ ======================================
    public void release() {
        LogUtils.d(TAG, "【release】释放TTS资源和悬浮窗");
        clearFloatWindow();
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
        sTextToSpeechUtils = null;
    }
}

