package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2023/07/01 05:59:25
 * @Describe 短信接收过滤规则工具类
 */
import android.content.Context;
import android.util.JsonReader;
import cc.winboll.studio.mymessagemanager.beans.SMSAcceptRuleBean;
import cc.winboll.studio.mymessagemanager.beans.SMSAcceptRuleBean_V1;
import cc.winboll.studio.shared.log.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.nio.channels.AcceptPendingException;

public class SMSReceiveRuleUtil {

    public static final String TAG = "SMSReceiveRuleUtil";

    public static final int VALID_MATCHRESULT_POSITION = -1;


    // 单例模式的实例变量
    static SMSReceiveRuleUtil _mInstance;
    // 类实例运行的上下文环境
    Context mContext;
    // 当前实例的配置操作数据
    ArrayList<SMSAcceptRuleBean> mDataList;

    //
    // ReceiveRule 规则数组匹配结果数据类
    //
    public class MatchResult {
        // 当前匹配规则的运算结果
        public SMSAcceptRuleBean.RuleType matchRuleType;
        // 在规则数组中匹配的位置
        public int matchPositionInRules;
        
        MatchResult() {
            matchRuleType = SMSAcceptRuleBean.RuleType.ACCEPT;
            // 在规则数组中匹配的位置
            matchPositionInRules = VALID_MATCHRESULT_POSITION;
        }
    }

    //
    // 私有的隐藏的实例构造函数
    //
    private SMSReceiveRuleUtil(Context context) {
        // 保存当前类实例的运行环境
        mContext = context;
        //mszConfigPath = context.getExternalFilesDir(TAG) + File.separator + mszConfigFileName;
        // 从存储设备加载应用数据
        mDataList = new ArrayList<SMSAcceptRuleBean>();
        loadConfigData();
    }

    //
    // 为了解决各个窗口数据同步问题，
    // 设置数据重加载开关
    // @isReload : 每次取数据实例都从存储设备读取数据
    //
    public static SMSReceiveRuleUtil getInstance(Context context, boolean isReload) {
        if (_mInstance == null) {
            _mInstance = new SMSReceiveRuleUtil(context);
        } else if (isReload) {
            _mInstance = new SMSReceiveRuleUtil(context);
        }

        return _mInstance;
    }

    //
    // 从存储设备读取数据，添加默认配置，并保存到存储设备。
    //
    public void resetConfig() {
        String szAssetsFilePath = "GlobalApplication/SMSAcceptRuleBean_List.json";
        SMSAcceptRuleBean beanTemp = new SMSAcceptRuleBean();
        String szConfigFilePath = beanTemp.getBeanListJsonFilePath(mContext);
        /*File fConfigFilePath = new File(szConfigFilePath);
         if(fConfigFilePath.exists()) {
         fConfigFilePath.delete();
         }*/
        FileUtil.copyAssetsToSD(mContext, szAssetsFilePath, szConfigFilePath);
        loadConfigData();
    }

    public void cleanConfig() {
        mDataList.clear();
        saveConfigData();
    }

    //
    // Rule数据排序
    // @isDesc : 是否降序排列
    //
    public static <T extends SMSAcceptRuleBean> void sortListByRuleData(List<T> list, boolean isDesc) {
        Collections.sort(list, new SortListByRuleData(isDesc));
    }

    //
    // Rule数据排序比较类定义
    //
    static class SortListByRuleData implements Comparator<SMSAcceptRuleBean> {
        private boolean mIsDesc = true;
        // isDesc 是否降序排列
        public SortListByRuleData(boolean isDesc) {
            mIsDesc = isDesc;
        }
        Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
        @Override
        public int compare(SMSAcceptRuleBean o1, SMSAcceptRuleBean o2) {
            int b0_1 = cmp.compare(o1.getRuleData(), o2.getRuleData());
            if (mIsDesc) {
                return b0_1 > 0 ? -1 : 1;
            } else {
                return b0_1 > 0 ? 1 : -1;
            }
        }
    }

    //
    // 添加Rule数据现
    // @szRule : Rule数据内容
    // @isEnable ： Rule数据启用开关项
    // @@ 返回 ：Rule数据添加结果
    //
    public boolean addRule(SMSAcceptRuleBean bean) {
        loadConfigData();
        mDataList.add(bean);
        saveConfigData();
        return true;
    }

    //
    // 校验短信是否在规则表里
    //
    public boolean checkIsSMSAcceptInRule(Context context, String szSMS) {
        /*ArrayList<SMSAcceptRuleBean> configData = loadConfigData();
         for (int i = 0; i < configData.size(); i++) {
         SMSAcceptRuleBean bean = configData.get(i);
         if (bean.isEnable()) {
         String regex = bean.getRuleData();
         if (szSMS.matches(regex)) {
         if (bean.getRuleType() == SMSAcceptRuleBean.RuleType.REFUSE) {
         return false;
         } else if (bean.getRuleType() == SMSAcceptRuleBean.RuleType.ACCEPT) {
         return true;
         }
         }
         }

         }*/
        MatchResult matchResult = getReceiveRuleMatchResult(context, szSMS);
        return matchResult.matchRuleType == SMSAcceptRuleBean.RuleType.ACCEPT;
    }

    //
    // 校验短信是否在规则表里
    //
    public MatchResult getReceiveRuleMatchResult(Context context, String szSMS) {
        
        MatchResult matchResult = new MatchResult();
        matchResult.matchRuleType = !RegexPPiUtils.isPPiOK(szSMS)? SMSAcceptRuleBean.RuleType.REGEXPPIUTILS_ISPPIOK_FALSE : matchResult.matchRuleType;
        //LogUtils.d(TAG, "RegexPPiUtils.isPPiOK(szSMS) " + Boolean.toString(RegexPPiUtils.isPPiOK(szSMS)));
        
        ArrayList<SMSAcceptRuleBean> configData = loadConfigData();
        for (int i = 0; i < configData.size(); i++) {
            SMSAcceptRuleBean bean = configData.get(i);
            if (bean.isEnable()) {
                String regex = bean.getRuleData();
                if (szSMS.matches(regex)) {
                    matchResult.matchRuleType = bean.getRuleType();
                    matchResult.matchPositionInRules = i;
                    LogUtils.d(TAG, "matchPositionInRules " + Integer.toString(i));
                    return matchResult;
                }
            }

        }
        return matchResult;
    }

    //
    // 加载应用配置数据
    //
    public ArrayList<SMSAcceptRuleBean> loadConfigData() {
        ArrayList<SMSAcceptRuleBean> list = new ArrayList<SMSAcceptRuleBean>();
        SMSAcceptRuleBean.loadBeanList(mContext, list, SMSAcceptRuleBean.class);
        for (int i = 0; i < list.size(); i++) {
            LogUtils.d(TAG, "loadConfigData isEnable : " + Boolean.toString(list.get(i).isEnable()));
        }
        mDataList.clear();
        mDataList.addAll(list);
        return mDataList;
    }

    /*ArrayList<SMSAcceptRuleBean_V1> loadDataFromPath(String szPath) {
     File fJson = new File(szPath);
     ArrayList<SMSAcceptRuleBean_V1> listTemp = null;
     try {
     listTemp = readJsonStream(new FileInputStream(fJson));
     } catch (IOException e) {
     LogUtils.d(TAG, "IOException : " + e.getMessage());
     }
     if (listTemp == null) {
     listTemp = new ArrayList<SMSAcceptRuleBean_V1>();
     }
     return listTemp;
     }*/

    //
    // 读取 Json 文件
    //
    public ArrayList<SMSAcceptRuleBean_V1> readJsonStream_V1(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        return readJsonArrayList_V1(reader);
    }

    //
    // 读取 Json 文件的每一 Json 项
    //
    public ArrayList<SMSAcceptRuleBean_V1> readJsonArrayList_V1(JsonReader reader) throws IOException {
        ArrayList<SMSAcceptRuleBean_V1> list = new ArrayList<SMSAcceptRuleBean_V1>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readBeanItem_V1(reader));
        }
        reader.endArray();
        return list;
    }

    //
    // 读取 Json 文件的某一 Json 项
    //
    public SMSAcceptRuleBean_V1 readBeanItem_V1(JsonReader reader) throws IOException {
        SMSAcceptRuleBean_V1 bean = new SMSAcceptRuleBean_V1();
        int nReaderCount = 0;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("userID")) {
                bean.setUserID(reader.nextString());
                nReaderCount++;
            } else if (name.equals("ruleData")) {
                bean.setRuleData(reader.nextString());
                nReaderCount++;
            } else if (name.equals("enable")) {
                bean.setEnable(reader.nextBoolean());
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
    /*public void writeBeanItem(JsonWriter writer, SMSAcceptRuleBean_V1 bean) throws IOException {
     writer.beginObject();
     writer.name("userID").value(bean.getUserID());
     writer.name("ruleData").value(bean.getRuleData());
     writer.name("enable").value(bean.isEnable());
     writer.endObject();
     }*/

    //
    // 保存应用配置数据
    //
    public void saveConfigData() {
        SMSAcceptRuleBean.saveBeanList(mContext, mDataList, SMSAcceptRuleBean.class);
    }

    /*//
     // 写入 Json 文件
     //
     public void writeJsonStream(OutputStream out, ArrayList<SMSAcceptRuleBean_V1> beanList) throws IOException {
     JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
     writer.setIndent("  ");
     writeJsonArrayList(writer, beanList);
     writer.close();
     }

     //
     // 记录 Json 文件的某一 Json 项
     //
     public void writeJsonArrayList(JsonWriter writer, ArrayList<SMSAcceptRuleBean_V1> beanList) throws IOException {
     writer.beginArray();
     for (SMSAcceptRuleBean_V1 bean : beanList) {
     writeBeanItem(writer, bean);
     }
     writer.endArray();
     }*/
}
