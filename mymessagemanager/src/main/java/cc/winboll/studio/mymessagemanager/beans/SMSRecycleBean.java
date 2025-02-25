package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/20 01:51:44
 * @Describe 回收站短信存储类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.shared.app.BaseBean;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SMSRecycleBean extends SMSBean {

    public static final String TAG = "SMSRecycleBean";

    // 短信删除日期
    long deleteDate;
    // 当前是否是简单视图
    boolean isSimpleView;

    public void setDeleteDate(long deleteDate) {
        this.deleteDate = deleteDate;
    }

    public long getDeleteDate() {
        return deleteDate;
    }

    public void setIsSimpleView(boolean isSimpleView) {
        this.isSimpleView = isSimpleView;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    public SMSRecycleBean() {

    }

    public SMSRecycleBean(SMSBean smsBean, long deleteDate) {
        super.id = smsBean.getId();
        super.mszAddress = smsBean.getAddress();
        super.mszBody = smsBean.getBody();
        super.mnDate = smsBean.getDate();
        super.mType = smsBean.getType();
        super.mReadStatus = smsBean.getReadStatus();
        super.mnPerson = smsBean.getPerson();
        this.deleteDate = deleteDate;
        this.isSimpleView = true;
    }

    @Override
    public String getName() {
        return SMSRecycleBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        SMSRecycleBean bean = this;
        jsonWriter.name("deleteDate").value(bean.getDeleteDate());
        jsonWriter.name("isSimpleView").value(bean.isSimpleView());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        /*SMSRecycleBean bean = new SMSRecycleBean((SMSBean)super.readBeanFromJsonReader(jsonReader), 0);
         // 只有在读取完成后，才能获取整个JSON字符串
         String completeJson = jsonReader.toString();
         JsonReader newJsonReader = new JsonReader(new StringReader(completeJson));
         newJsonReader.setLenient(true);
         LogUtils.d(TAG, completeJson);*/
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (!initObjectsFromJsonReader(jsonReader, name)) {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return this;
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if(super.initObjectsFromJsonReader(jsonReader, name)) { return true; }
        else{
            if (name.equals("deleteDate")) {
                setDeleteDate(jsonReader.nextLong());
            } else if (name.equals("isSimpleView")) {
                setIsSimpleView(jsonReader.nextBoolean());
            } else {
                return false;
            }
        }
        return true;
    }

    public static void sortSMSByDeleteDateDesc(ArrayList<SMSRecycleBean> list, boolean isDesc) {
        Collections.sort(list, new SortSMSByDeleteDateDesc(isDesc));
    }

    private static class SortSMSByDeleteDateDesc implements Comparator<SMSRecycleBean> {
        private boolean mIsDesc = true;
        // isDesc 是否降序排列
        public SortSMSByDeleteDateDesc(boolean isDesc) {
            mIsDesc = isDesc;
        }
        Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
        @Override
        public int compare(SMSRecycleBean o1, SMSRecycleBean o2) {
            boolean b0_1 = (o1.getDeleteDate() < o2.getDeleteDate());
            if (mIsDesc) {
                return b0_1 ?1: -1;
            } else {
                return b0_1 ?-1: 1;
            }
        }
    }
}
