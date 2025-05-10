package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/28 20:22:12
 * @Describe TTS 语音播放文本内容类
 */
import java.io.Serializable;

public class TTSSpeakTextBean implements Serializable {

    transient public static final String TAG = "TTSSpeakTextBean";

    // 延迟播放
    public int mnDelay = 0;
    // 语音播放内容
    public String mszSpeakContent = "";

    public TTSSpeakTextBean(int nDelay, String szSpeakContent) {
        this.mnDelay = nDelay;
        this.mszSpeakContent = szSpeakContent;
    }

}
