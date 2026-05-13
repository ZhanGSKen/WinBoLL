package cc.winboll.studio.powerbell.models;

import cc.winboll.studio.libappbase.LogUtils;
import java.io.Serializable;

/**
 * TTS 语音播放文本内容实体类
 * 适配：Java7 语法规范 | Android API30 系统版本
 * 特性：实现序列化接口，支持跨页面/进程传递，属性默认值初始化
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/29 19:13
 */
public class TTSSpeakTextBean implements Serializable {

    // ====================================== 常量区 - 置顶排序 ======================================
    /** 日志TAG 瞬态修饰，不参与序列化，减少序列化体积 */
    transient public static final String TAG = "TTSSpeakTextBean";

    // ====================================== 成员属性区 - 业务属性排序 ======================================
    /** 延迟播放时长 单位：毫秒，默认值0：无延迟播放 */
    public int mnDelay = 0;
    /** TTS语音播放文本内容，默认值空字符串：防止空指针 */
    public String mszSpeakContent = "";

    // ====================================== 构造方法区 - 无参+有参 完整实现 ======================================
    /**
     * 无参构造方法
     * Java7序列化规范必备 + 兼容反射实例化场景
     */
    public TTSSpeakTextBean() {
        LogUtils.d(TAG, "【无参构造】TTSSpeakTextBean 实例化，使用默认值 | 延迟:" + mnDelay + " | 文本:" + mszSpeakContent);
    }

    /**
     * 有参构造方法【主构造】
     * @param nDelay 延迟播放时长(ms)
     * @param szSpeakContent 语音播放文本内容
     */
    public TTSSpeakTextBean(int nDelay, String szSpeakContent) {
        LogUtils.d(TAG, "【有参构造】TTSSpeakTextBean 实例化，入参 | 延迟:" + nDelay + " | 文本:" + szSpeakContent);
        this.mnDelay = nDelay;
        this.mszSpeakContent = szSpeakContent;
        LogUtils.d(TAG, "【有参构造】赋值完成 | 最终延迟:" + this.mnDelay + " | 最终文本:" + this.mszSpeakContent);
    }

}

