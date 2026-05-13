package cc.winboll.studio.powerbell.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.models.TTSSpeakTextBean;
import cc.winboll.studio.powerbell.utils.TextToSpeechUtils;
import java.util.ArrayList;

/**
 * TTS 语音播放后台服务组件
 * 适配：Java7 语法规范 | Android API30 系统版本
 * 功能：后台承载TTS语音播放，解耦页面生命周期，避免页面销毁中断播放
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/29 19:12
 */
public class TTSPlayService extends Service {

    // ====================================== 常量区 - 静态全局常量 置顶排序 ======================================
    public static final String TAG = "TTSPlayService";
    public static final String EXTRA_SPEAKDATA = "EXTRA_SPEAKDATA";

    // ====================================== 对外公开静态快捷调用方法【新增核心】======================================
    /**
     * 公开静态方法：一键启动TTS播放服务，播放指定文本内容
     * @param context 上下文对象
     * @param speakText 需要播放的语音文本内容
     */
    public static void startPlayTTS(Context context, String speakText) {
        LogUtils.d(TAG, "【startPlayTTS】静态快捷调用方法 | 入参Context=" + context + " | 播放文本=" + speakText);
        if (context != null && speakText != null && !speakText.isEmpty()) {
            // 初始化播放数据集合
            ArrayList<TTSSpeakTextBean> ttsBeanList = new ArrayList<>();
            // 添加播放文本，延迟时间为0：无延迟立即播放
            ttsBeanList.add(new TTSSpeakTextBean(0, speakText));
            LogUtils.d(TAG, "【startPlayTTS】封装播放数据完成，创建启动服务意图");

            // 创建意图并封装序列化参数
            Intent intent = new Intent(context, TTSPlayService.class);
            intent.putExtra(EXTRA_SPEAKDATA, ttsBeanList);

            // 启动当前服务
            context.startService(intent);
            LogUtils.d(TAG, "【startPlayTTS】已调用startService，TTS播放服务启动成功");
        } else {
            LogUtils.d(TAG, "【startPlayTTS】上下文为空 或 播放文本为空/空字符串，跳过启动服务");
        }
    }

    // ====================================== 生命周期方法 - 绑定服务 (无绑定逻辑) ======================================
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "【onBind】服务绑定方法调用，入参Intent：" + intent);
        return null;
    }

    // ====================================== 生命周期方法 - 启动服务【核心方法】 ======================================
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "【onStartCommand】服务启动方法调用 | 入参Intent：" + intent + " | flags：" + flags + " | startId：" + startId);
        // 解析播放数据并执行播放
        if (intent != null) {
            LogUtils.d(TAG, "【onStartCommand】Intent不为空，开始解析序列化播放数据");
            ArrayList<TTSSpeakTextBean> listTTSSpeakTextBean = (ArrayList<TTSSpeakTextBean>) intent.getSerializableExtra(EXTRA_SPEAKDATA);
            if (listTTSSpeakTextBean != null && listTTSSpeakTextBean.size() > 0) {
                LogUtils.d(TAG, "【onStartCommand】解析播放数据成功，队列长度：" + listTTSSpeakTextBean.size() + "，调用TTS播放工具类");
                TextToSpeechUtils.getInstance(this).speekTTSList(listTTSSpeakTextBean);
            } else {
                LogUtils.d(TAG, "【onStartCommand】播放数据为空/长度0，跳过语音播放逻辑");
            }
        } else {
            LogUtils.d(TAG, "【onStartCommand】Intent为空，无播放数据可解析");
        }
        // 返回默认值，保持原服务启动策略不变
        int result = super.onStartCommand(intent, flags, startId);
        LogUtils.d(TAG, "【onStartCommand】方法执行完成，返回值：" + result);
        return result;
    }

}

