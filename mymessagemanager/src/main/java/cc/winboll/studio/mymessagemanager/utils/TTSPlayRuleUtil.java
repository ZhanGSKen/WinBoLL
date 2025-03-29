package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 14:30:57
 * @Describe TTS 语音播放工具类
 */
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.JsonReader;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.TTSPlayRuleActivity;
import cc.winboll.studio.mymessagemanager.beans.TTSPlayRuleBean;
import cc.winboll.studio.mymessagemanager.beans.TTSPlayRuleBean_V1;
import cc.winboll.studio.mymessagemanager.beans.TTSSpeakTextBean;
import cc.winboll.studio.mymessagemanager.dialogs.YesNoAlertDialog;
import cc.winboll.studio.mymessagemanager.services.TTSPlayService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TTSPlayRuleUtil {
    public static final String TAG = "TTSPlayRuleUtil";

    TTSPlayRuleActivity mTTSPlayRuleActivity;
    static TTSPlayRuleUtil _mTTSPlayRuleUtil;
	Context mContext;
	ArrayList<TTSPlayRuleBean> mConfigData;
    public static String mszConfigFileName = TAG + ".json";
    String mszConfigPath_V1 = null;

    TTSPlayRuleUtil(Context context) {
        mContext = context;
		mszConfigPath_V1 = context.getExternalFilesDir(TAG) + File.separator + mszConfigFileName;
		mConfigData = new ArrayList<TTSPlayRuleBean>();
		mConfigData = loadConfigData();
    }

    public static TTSPlayRuleUtil getInstance(Context context) {
        if (_mTTSPlayRuleUtil == null) {
            _mTTSPlayRuleUtil = new TTSPlayRuleUtil(context);
        }
        return _mTTSPlayRuleUtil;
    }

    public void initTTSPlayRuleActivity(TTSPlayRuleActivity activity) {
        mTTSPlayRuleActivity = activity;
    }

    //
    // 用 TTS 播放语音
    // @szSpeak : 语音内容
    // @nRepeat : 重复次数
    //
    public static void speakText(Context context, String szSpeak, int nRepeat) {
        speakText(context, szSpeak, 0, nRepeat);
    }


    //
    // 用 TTS 播放语音
    // @szSpeak : 语音内容
    // @nTtsPlayDelayTimes : 延迟初始播放时间毫秒数
    // @nRepeat : 重复次数
    //
    public static void speakText(Context context, String szSpeak, int nTtsPlayDelayTimes, int nRepeat) {
        // 初始化语音数据
        ArrayList<TTSSpeakTextBean> ttsData = new ArrayList<TTSSpeakTextBean>();
        ttsData.add(new TTSSpeakTextBean(nTtsPlayDelayTimes, szSpeak));
        for (int i = 0; i < nRepeat - 1; i++) {
            ttsData.add(new TTSSpeakTextBean(3000, szSpeak));
        }
        // 调用TTS语音服务
        Intent intent = new Intent(context, TTSPlayService.class);
        intent.putExtra(TTSPlayService.EXTRA_SPEAKDATA, ttsData);
        context.startService(intent);
    }

	//
    // 短信解析模式播放短信的 TTS 语音
    // @context : 上下文
    // @szSMS : 要解析的短信
    //
    public int speakTTSAnalyzeModeText(String szSMS) {
		return speakTTSAnalyzeModeText(szSMS, 0);
	}

    //
    // 短信解析模式播放短信的 TTS 语音
    // @context : 上下文
    // @szSMS : 要解析的短信
    // @nTtsPlayDelayTimes : 延迟初始播放时间毫秒数
    //
    public int speakTTSAnalyzeModeText(String szSMS, int nTtsPlayDelayTimes) {
        // 初始化语音数据
        ArrayList<TTSSpeakTextBean> ttsData = new ArrayList<TTSSpeakTextBean>();
        int reault = addTTSAnalyzeModeReply(szSMS, ttsData, nTtsPlayDelayTimes);

        // 调用TTS语音服务
        Intent intent = new Intent(mContext, TTSPlayService.class);
        intent.putExtra(TTSPlayService.EXTRA_SPEAKDATA, ttsData);
        mContext.startService(intent);

        return reault;
	}

	public void deleteTTSRuleBean(int position) {
		mConfigData.remove(position);
        saveConfigData();
	}

    //
    // 添加语音回复数据
    // @context : 上下文
    // @szSMS : 提供校验的短信
    // @ttsData : 语音数据规则表
    // @nTtsPlayDelayTimes : 延迟初始播放时间毫秒数
    //
    int addTTSAnalyzeModeReply(String szSMS, ArrayList<TTSSpeakTextBean> ttsData, int nTtsPlayDelayTimes) {
        for (int i = 0; i < mConfigData.size(); i++) {
			if (mConfigData.get(i).isEnable()) {
				String szResult = getRewriteRegExpResult(szSMS, mConfigData.get(i).getPatternText(), mConfigData.get(i).getTtsRuleText());
				if (!szResult.equals("")) {
					ttsData.add(new TTSSpeakTextBean(nTtsPlayDelayTimes, szResult));
					return i;
				}
			}
        }
        return -1;
    }

    //
    // 添加语音回复数据
    // @context : 上下文
    // @szSMS : 提供校验的短信
    // @ttsData : 语音数据规则表
    //
    void addTTSAnalyzeModeReply(String szSMS, ArrayList<TTSSpeakTextBean> ttsData) {
        addTTSAnalyzeModeReply(szSMS, ttsData, 0);
    }

    //
    // 测试语音回复数据
    // @context : 上下文
    // @szSMS : 示例短信
    // @ttsData : 语音数据规则
    //
    public String testTTSAnalyzeModeReply(TTSPlayRuleBean bean) {

        String szResult = getRewriteRegExpResult(bean.getDemoSMSText(), bean.getPatternText(), bean.getTtsRuleText());
		if (szResult.equals("")) {
			szResult += "\nDemoSMSText : " + bean.getDemoSMSText();
			szResult += "\nPatternText : " + bean.getPatternText();
			szResult += "\nTTSRuleText : " + bean.getTtsRuleText();
		} else {
			// 初始化语音数据
			ArrayList<TTSSpeakTextBean> ttsData = new ArrayList<TTSSpeakTextBean>();
			ttsData.add(new TTSSpeakTextBean(0, szResult));

			// 调用TTS语音服务
			Intent intent = new Intent(mContext, TTSPlayService.class);
			intent.putExtra(TTSPlayService.EXTRA_SPEAKDATA, ttsData);
			mContext.startService(intent);
		}
		return szResult;
    }

    //
    // 正则替换函数
    // @szMatchText : 操作字符串
    // @szPattern : 查找模板
    // @szRewrite : 替换模板
    //
    String getRewriteRegExpResult(String szMatchText, String szPattern, String szRewrite) {
        String szReturn = "";
		if (!szPattern.equals("") && !szRewrite.equals("")) {
			try {
				Pattern pattern = Pattern.compile(szPattern, Pattern.MULTILINE);
				Matcher matcher = pattern.matcher(szMatchText);
                LogUtils.d(TAG, "szMatchText : " + szMatchText);
				while (matcher.find()) {
					szReturn += matcher.replaceAll(szRewrite);
				}
			} catch (java.lang.IndexOutOfBoundsException ex) {
				LogUtils.d(TAG, "getRewriteRegExpResult(...) IndexOutOfBoundsException : " + ex.getMessage());
			}
		}

        return szReturn;
    }

	public void addNewTTSRuleBean(TTSPlayRuleBean bean) {
		mConfigData.add(0, bean);
		saveConfigData();
	}

	public void changeBeanPosition(int currentPosition, boolean isUp) {
		if (isUp && currentPosition > 0) {
			//LogUtils.d(TAG, "Up");
			mConfigData.add(currentPosition - 1, mConfigData.get(currentPosition));
			mConfigData.remove(currentPosition + 1);
            saveConfigData();
			return;
		}

		if (!isUp && currentPosition < mConfigData.size() - 1) {
			//LogUtils.d(TAG, "Down");
			mConfigData.add(currentPosition + 2, mConfigData.get(currentPosition));
			mConfigData.remove(currentPosition);
            saveConfigData();
			return;
		}
	}

	public void setBeanEnable(int position, boolean isEnable) {
		mConfigData.get(position).setIsEnable(isEnable);
		saveConfigData();
	}

    /*public String getConfigPath() {
     return mszConfigPath_V1;
     }*/

    //
    // 从指定路径的文件读取数据，添加并保存到现有的数据文件。
    // @szPath : 要读取的文件指定的路径
    // @@ 返回 ：添加的数据条数
    //
    /*public int importConfig(String szPath) {
     ArrayList<TTSPlayRuleBean> listBeanImport = loadDataFromPath(szPath);
     // 添加更新表到现有操作表
     if (mConfigData == null) {
     mConfigData = new ArrayList<TTSPlayRuleBean>();
     }
     ArrayList<TTSPlayRuleBean> configData = loadConfigData();
     configData.addAll(listBeanImport);
     // 保存操作表数据
     saveConfigData(configData);
     return listBeanImport.size();
     }*/

    /*ArrayList<TTSPlayRuleBean> loadDataFromPath(String szPath) {
     File fJson = new File(szPath);
     ArrayList<TTSPlayRuleBean> listTemp = null;
     try {
     listTemp = readJsonStream(new FileInputStream(fJson));
     } catch (IOException e) {
     LogUtils.d(TAG, "IOException : " + e.getMessage());
     }
     if (listTemp == null) {
     listTemp = new ArrayList<TTSPlayRuleBean>();
     }
     return listTemp;
     }*/

    //
    // 加载 TTS 配置数据
    //
    public ArrayList<TTSPlayRuleBean> loadConfigData() {
        TTSPlayRuleBean.loadBeanList(mContext, mConfigData, TTSPlayRuleBean.class);
        return mConfigData;
    }

    //
    // 保存 TTS 配置数据
    //
    public void saveConfigData() {
        // 设定只能在规则编辑窗口改变规则
        if (mTTSPlayRuleActivity == null) {
            LogUtils.i(TAG, "Please edit rules in TTSPlayRuleActivity.");
            return;
        }

        YesNoAlertDialog.show(mTTSPlayRuleActivity, mContext.getString(R.string.text_ttsrule), "是否更新语音规则？", new YesNoAlertDialog.OnDialogResultListener(){
                @Override
                public void onYes() {
                    TTSPlayRuleBean.saveBeanList(mContext, mConfigData, TTSPlayRuleBean.class);
                    Toast.makeText(mTTSPlayRuleActivity, "语音数据已更改。", Toast.LENGTH_SHORT).show();
                    
                    Message message = new Message();
                    message.what = TTSPlayRuleActivity.MSG_RELOAD;
                    mTTSPlayRuleActivity.sendActivityMessage(message);
                }

                @Override
                public void onNo() {
                    Message message = new Message();
                    message.what = TTSPlayRuleActivity.MSG_RELOAD;
                    mTTSPlayRuleActivity.sendActivityMessage(message);
                }
            });
    }

    //
    // 清空 TTS 配置数据
    //
    public void cleanConfig() {
        YesNoAlertDialog.show(mTTSPlayRuleActivity, "确定清理", "您确定清理所有语音规则吗？", new YesNoAlertDialog.OnDialogResultListener(){
                @Override
                public void onYes() {
                    mConfigData.clear();
                    TTSPlayRuleBean.saveBeanList(mContext, mConfigData, TTSPlayRuleBean.class);
                    Toast.makeText(mTTSPlayRuleActivity, "语音数据已更改。", Toast.LENGTH_SHORT).show();
                    
                    Message message = new Message();
                    message.what = TTSPlayRuleActivity.MSG_RELOAD;
                    mTTSPlayRuleActivity.sendActivityMessage(message);
                }
                @Override
                public void onNo() {
                }
            });
	}

    //
    // 重置默认 TTS 配置数据
    //
    public void resetConfig() {
        YesNoAlertDialog.show(mTTSPlayRuleActivity, "确定重置", "您确定重置语音规则为默认设置吗？", new YesNoAlertDialog.OnDialogResultListener(){
                @Override
                public void onYes() {
                    String szAssetsFilePath = "GlobalApplication/TTSPlayRuleBean_List.json";
                    TTSPlayRuleBean beanTemp = new TTSPlayRuleBean();
                    String szConfigFilePath = beanTemp.getBeanListJsonFilePath(mContext);
                    FileUtil.copyAssetsToSD(mContext, szAssetsFilePath, szConfigFilePath);
                    Toast.makeText(mTTSPlayRuleActivity, "语音数据已更改。", Toast.LENGTH_SHORT).show();
                    
                    loadConfigData();
                    Message message = new Message();
                    message.what = TTSPlayRuleActivity.MSG_RELOAD;
                    mTTSPlayRuleActivity.sendActivityMessage(message);
                }
                @Override
                public void onNo() {
                }
            });
	}

    //
    // 读取 Json 文件
    //
    public ArrayList<TTSPlayRuleBean_V1> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        return readJsonArrayList(reader);
    }

    //
    // 读取 Json 文件的每一 Json 项
    //
    public ArrayList<TTSPlayRuleBean_V1> readJsonArrayList(JsonReader reader) throws IOException {
        ArrayList<TTSPlayRuleBean_V1> list = new ArrayList<TTSPlayRuleBean_V1>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readBeanItem(reader));
        }
        reader.endArray();
        return list;
    }

    //
    // 读取 Json 文件的某一 Json 项
    //
    public TTSPlayRuleBean_V1 readBeanItem(JsonReader reader) throws IOException {
        TTSPlayRuleBean_V1 bean = new TTSPlayRuleBean_V1();
        int nReaderCount = 0;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("DemoSMSText")) {
                bean.setDemoSMSText(reader.nextString());
                nReaderCount++;
            } else if (name.equals("PatternText")) {
                bean.setPatternText(reader.nextString());
                nReaderCount++;
            } else if (name.equals("TTSRuleText")) {
                bean.setTtsRuleText(reader.nextString());
                nReaderCount++;
            } else if (name.equals("Enable")) {
                bean.setIsEnable(reader.nextBoolean());
                nReaderCount++;
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return nReaderCount > 0 ? bean : null;
    }

    //
    // 写入 Json 文件的某一 Json 项
    //
    /*public void writeBeanItem(JsonWriter writer, TTSPlayRuleBean bean) throws IOException {
     writer.beginObject();
     writer.name("Name").value(bean.getRuleName());
     writer.name("DemoSMSText").value(bean.getDemoSMSText());
     writer.name("PatternText").value(bean.getPatternText());
     writer.name("TTSRuleText").value(bean.getTtsRuleText());
     writer.name("Enable").value(bean.isEnable());
     writer.endObject();
     }*/

    //
    // 写入 Json 文件
    //
    /*public void writeJsonStream(OutputStream out, ArrayList<TTSPlayRuleBean> beanList) throws IOException {
     JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
     writer.setIndent("  ");
     writeJsonArrayList(writer, beanList);
     writer.close();
     }

     //
     // 记录 Json 文件的某一 Json 项
     //
     public void writeJsonArrayList(JsonWriter writer, ArrayList<TTSPlayRuleBean> beanList) throws IOException {
     writer.beginArray();
     for (TTSPlayRuleBean bean : beanList) {
     writeBeanItem(writer, bean);
     }
     writer.endArray();
     }*/
}
