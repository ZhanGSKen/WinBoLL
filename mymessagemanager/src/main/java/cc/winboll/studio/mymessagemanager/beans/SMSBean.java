package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/30 10:57:14
 * @Describe 短信信息类
 参考资料：
 https://blog.csdn.net/freeking101/article/details/121575985

 获取短信只需要得到 ContentResolver 就行了，它的 URI 主要有：
 content://sms/          所有短信
 content://sms/inbox     收件箱
 content://sms/sent      已发送
 content://sms/draft     草稿
 content://sms/outbox    发件箱
 content://sms/failed    发送失败
 content://sms/queued    待发送列表
 SMS 数据库中的字段如下：
 _id        一个自增字段，从1开始
 thread_id  序号，同一发信人的id相同
 address    发件人手机号码
 person     联系人列表里的序号，陌生人为null
 date       发件日期
 protocol   协议，分为： 0 SMS_RPOTO, 1 MMS_PROTO
 read       是否阅读 0未读， 1已读
 status     状态 -1接收，0 complete, 64 pending, 128 failed
 type       ALL = 0;INBOX = 1;SENT = 2;DRAFT = 3;OUTBOX = 4;FAILED = 5; QUEUED = 6;
 body       短信内容
 service_center 短信服务中心号码编号。如+8613800755500
 subject        短信的主题
 reply_path_present TP-Reply-Path
 locked
 */
import android.content.ContentValues;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.shared.app.BaseBean;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SMSBean extends BaseBean {

    //public enum Type { ALL(8), INBOX(0), SENT, DRAFT, OUTBOX, FAILED, QUEUED, TRASH }
    public enum Type { ALL(0), INBOX(1), SENT(2), DRAFT(3), OUTBOX(4), FAILED(5), QUEUED(6), TRASH(7);
        static String[] _mlistName = { "所有短信", "接收", "发送", "草稿", "发件箱", "发送失败", "待发送列表", "回收站" };
        private int value = 0;
        private Type(int value) {    //必须是private的，否则编译错误
            this.value = value;
        }
    }

    public enum ReadStatus { UNREAD, READ }

    transient private static String _ContentValuesName_address = "address";
    transient private static String _ContentValuesName_body = "body";
    transient private static String _ContentValuesName_read = "read";
    transient private static String _ContentValuesName_date = "date";

    // 短信标识
    protected int id;
    // 发件人手机号码
    protected String mszAddress;
    // 短信内容
    protected String mszBody;
    // 发件日期
    protected long mnDate;
    // 短息归类
    protected Type mType;
    // 是否阅读
    protected ReadStatus mReadStatus;
    // 联系人列表里的序号，陌生人为null
    protected int mnPerson;
    
    public SMSBean() {
        this.id = -1;
        this.mszAddress = "";
        this.mszBody = "";
        this.mnDate = 0;
        this.mType = Type.INBOX;
        this.mReadStatus = ReadStatus.UNREAD;
        this.mnPerson = 0;
    }
    
    public SMSBean(int id, String mszAddress, String mszBody, long mnDate, Type mType, ReadStatus mReadStatus, int mnPerson) {
        this.id = id;
        this.mszAddress = mszAddress;
        this.mszBody = mszBody;
        this.mnDate = mnDate;
        this.mType = mType;
        this.mReadStatus = mReadStatus;
        this.mnPerson = mnPerson;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setAddress(String szAddress) {
        this.mszAddress = szAddress;
    }

    public String getAddress() {
        return mszAddress;
    }

    public void setBody(String szBody) {
        this.mszBody = szBody;
    }

    public String getBody() {
        return mszBody;
    }

    public void setDate(long date) {
        this.mnDate = date;
    }

    public long getDate() {
        return mnDate;
    }

    public void setType(Type type) {
        this.mType = type;
    }

    public Type getType() {
        return mType;
    }

    public void setReadStatus(ReadStatus readStatus) {
        this.mReadStatus = readStatus;
    }

    public ReadStatus getReadStatus() {
        return mReadStatus;
    }

    public void setPerson(int person) {
        this.mnPerson = person;
    }

    public int getPerson() {
        return mnPerson;
    }

    @Override
    public String getName() {
        return SMSBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        SMSBean bean = this;
        jsonWriter.name("id").value(bean.getId());
        jsonWriter.name("mszAddress").value(bean.getAddress());
        jsonWriter.name("mszBody").value(bean.getBody());
        jsonWriter.name("mnDate").value(bean.getDate());
        jsonWriter.name("mType").value(bean.getType().ordinal());
        jsonWriter.name("mReadStatus").value(bean.getReadStatus().ordinal());
        jsonWriter.name("mnPerson").value(bean.getPerson());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if(super.initObjectsFromJsonReader(jsonReader, name)) { return true; }
        else{
            if (name.equals("id")) {
                setId(jsonReader.nextInt());
            } else if (name.equals("mszAddress")) {
                setAddress(jsonReader.nextString());
            } else if (name.equals("mszBody")) {
                setBody(jsonReader.nextString());
            } else if (name.equals("mnDate")) {
                setDate(jsonReader.nextLong());
            } else if (name.equals("mType")) {
                setType(Type.values()[jsonReader.nextInt()]);
            } else if (name.equals("mReadStatus")) {
                setReadStatus(ReadStatus.values()[jsonReader.nextInt()]);
            } else if (name.equals("mnPerson")) {
                setPerson(jsonReader.nextInt());
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if(!initObjectsFromJsonReader(jsonReader, name)) {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return this;
    }
    
    public static ContentValues createOldSendedSMSContentValues(SMSBean smsBean) {
        ContentValues result = new ContentValues();
        result.put(_ContentValuesName_address, smsBean.mszAddress);
        result.put(_ContentValuesName_body, smsBean.mszBody);
        result.put(_ContentValuesName_read, smsBean.mReadStatus.toString()); //"0" for have not read sms and "1" for have read sms
        result.put(_ContentValuesName_date, Long.toString(smsBean.getDate()));

        return result;
    }

    public static ContentValues createSendedSMSContentValues(SMSBean smsBean) {
        ContentValues result = new ContentValues();
        result.put(_ContentValuesName_address, smsBean.mszAddress);
        result.put(_ContentValuesName_body, smsBean.mszBody);
        result.put(_ContentValuesName_read, smsBean.mReadStatus.toString()); //"0" for have not read sms and "1" for have read sms
        result.put(_ContentValuesName_date, Long.toString(System.currentTimeMillis()));

        return result;
    }

    public static String getTypeName(Type type) {
        return Type._mlistName[type.ordinal()];
    }

    @Override
    public String toString() {
        String szResult = "\n";
        szResult += "mszAddress is (" + mszAddress + ")\n";
        szResult += "mszBody is (" + mszBody + ")\n";
        szResult += "mnDate is (" + Long.toString(mnDate) + ")\n";
        szResult += "mType is (" + mType.name() + ")\n";
        if (mReadStatus != null) {
            szResult += "mReadStatus is (" + mReadStatus.name() + ")\n";
        }
        szResult += "mnPerson is (" + Integer.toString(mnPerson) + ")\n";


        return szResult;
    }

    public static void sortSMSByDateDesc(ArrayList<SMSBean> list, boolean isDesc) {
        Collections.sort(list, new SortSMSByDateDesc(isDesc));

    }

    private static class SortSMSByDateDesc implements Comparator<SMSBean> {
        private boolean mIsDesc = true;
        // isDesc 是否降序排列
        public SortSMSByDateDesc(boolean isDesc) {
            mIsDesc = isDesc;
        }
        Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
        @Override
        public int compare(SMSBean o1, SMSBean o2) {
            boolean b0_1 = (o1.getDate() < o2.getDate());
            if (mIsDesc) {
                return b0_1 ?1: -1;
            } else {
                return b0_1 ?-1: 1;
            }
        }
    }
}
