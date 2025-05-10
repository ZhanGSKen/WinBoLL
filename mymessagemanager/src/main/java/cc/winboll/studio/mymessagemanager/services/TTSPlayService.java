package cc.winboll.studio.mymessagemanager.services;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe TTS 语音播放服务组件类
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.mymessagemanager.beans.TTSSpeakTextBean;
import cc.winboll.studio.mymessagemanager.utils.TextToSpeechUtil;
import java.util.ArrayList;

public class TTSPlayService extends Service {

    public static final String TAG = "TTSService";

    public static final String EXTRA_SPEAKDATA = "EXTRA_SPEAKDATA";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ArrayList<TTSSpeakTextBean> listTTSSpeakTextBean = (ArrayList<TTSSpeakTextBean>)intent.getSerializableExtra(EXTRA_SPEAKDATA);
            if (listTTSSpeakTextBean != null) {
                
                TextToSpeechUtil.getInstance(this).speekTTSList(listTTSSpeakTextBean);
                
                //Toast.makeText(getApplication(), "onStartCommand", Toast.LENGTH_SHORT).show();
                //TTSThread ttsThread = new TTSThread(TTSService.this, listTTSSpeakTextBean);
                //ttsThread.start();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
