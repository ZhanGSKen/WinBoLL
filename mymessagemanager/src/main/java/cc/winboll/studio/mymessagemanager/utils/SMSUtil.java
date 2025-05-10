package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 应用短信管理工具类
 */
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.shared.log.LogUtils;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;

public class SMSUtil {

    public static String TAG = "SMSUtil";
    public static String SENT_SMS_ACTION = SMSUtil.class.getName() + ".SENT_SMS_ACTION";
    public static String DELIVERED_SMS_ACTION = SMSUtil.class.getName() + ".SENT_SMS_ACTION";

    final static String SMS_URI_ALL = "content://sms/"; // 所有短信
    final static String SMS_URI_INBOX = "content://sms/inbox"; // 收件箱
    final static String SMS_URI_SEND = "content://sms/sent"; // 已发送
    final static String SMS_URI_DRAFT = "content://sms/draft"; // 草稿
    final static String SMS_URI_OUTBOX = "content://sms/outbox"; // 发件箱
    final static String SMS_URI_FAILED = "content://sms/failed"; // 发送失败
    final static String SMS_URI_QUEUED = "content://sms/queued"; // 待发送列表

    //
    // 获得短信内容
    //
    public static String getSmsBody(Intent intent) {

        String tempString = "";
        Bundle bundle = intent.getExtras();
        Object messages[] = (Object[]) bundle.get("pdus");
        SmsMessage[] smsMessage = new SmsMessage[messages.length];
        for (int n = 0; n < messages.length; n++) {
            smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            // 短信有可能因为使用了回车而导致分为多条，所以要加起来接受
            tempString += smsMessage[n].getDisplayMessageBody();
        }
        return tempString;

    }

    public static int deleteSMSById(Context context, int nDeleteId) {
        //LogUtils.d(TAG, "nDeleteId is " + Integer.toString(nDeleteId));
        int nResult = 0;
        //int nTotal = 0;
        //Uri uri = Uri.parse(SMS_URI_ALL);
        //String[] projection = new String[] { "_id", "address", "person",
        //    "body", "date", "type", };
        /*Cursor cursor = context.getContentResolver().query(uri, projection,
         "_id = ? ",
         new String[] {Integer.toString(nDeleteId)},
         "");*/
        nResult = context.getContentResolver().delete(Uri.parse("content://sms/" + Integer.toString(nDeleteId)), null, null);
        /*if (cursor.moveToFirst()) {
         do {
         //nTotal++;
         //int nSMSId = cursor.getInt(0);
         //String szType = (SMSBean.Type.values()[cursor.getInt(5)]).toString();
         //LogUtils.d(TAG, "nSMSId is : " + Integer.toString(nSMSId));
         //LogUtils.d(TAG, "szType is : " + szType);
         //if (nSMSId == nDeleteId) {
         //LogUtils.d(TAG, "nSMSId == nDeleteId");
         //int threadId = cursor.getInt(cursor.getColumnIndex("_id"));
         //nResult = context.getContentResolver().delete(Uri.parse("content://sms/" + nSMSId), null, null);
         //LogUtils.d(TAG, "getContentResolver delete");
         //}
         } while (cursor.moveToNext());
         }*/

        /*if (!cursor.isClosed()) {
         cursor.close();
         cursor = null;
         }*/

        //LogUtils.d(TAG, "nTotal is : " + Integer.toString(nTotal));
        //LogUtils.d(TAG, "nResult is : " + Integer.toString(nResult));
        return nResult;
    }

    public static ArrayList<SMSBean> getAllSMSList(Context context) {
        ArrayList<SMSBean> returnList = new ArrayList<SMSBean>();
        try {
            String szReplaceString = AppConfigUtil.getInstance(context).getPhoneReplaceString();

            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[] { "_id", "address", "person",
                "body", "date", "type", };
            Cursor cur = context.getContentResolver().query(uri, projection,
                                                            "",
                                                            null,
                                                            "");
            // 获取短信中最新的未读短信
            // Cursor cur = getContentResolver().query(uri, projection,
            // "read = ?", new String[]{"0"}, "date desc");
            if (cur.moveToFirst()) {
                do {
                    SMSBean smsBean = new SMSBean();
                    smsBean.setId(cur.getInt(0));
                    smsBean.setAddress(cur.getString(1).replaceAll(szReplaceString, ""));
                    smsBean.setPerson(cur.getInt(2));
                    smsBean.setBody(cur.getString(3));
                    smsBean.setDate(cur.getLong(4));
                    smsBean.setType(SMSBean.Type.values()[cur.getInt(5)]);
                    returnList.add(smsBean);
                } while (cur.moveToNext());

                // 按时间降序排列
                SMSBean.sortSMSByDateDesc(returnList, true);

                // 去除重复 mszAddress 的数据;
                for (int i = returnList.size() - 1; i > -1; i--) {
                    String szCheckAddress =returnList.get(i).getAddress();
					if (szCheckAddress != null) {
						for (int j = i - 1; j > -1; j--) {
							if ((returnList.get(j).getAddress() != null)
								&& (szCheckAddress.equals(returnList.get(j).getAddress()))) {
								returnList.remove(i);
								break;
							}
						}
					}

                }

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            }
        } catch (SQLiteException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return returnList;
    }

    public static ArrayList<SMSBean> getSMSListByPhone(Context context, String szPhone) {
        ArrayList<SMSBean> returnList = new ArrayList<SMSBean>();
        try {


            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[] { "_id", "address", "person",
                "body", "date", "type", };
            Cursor cursor;
            AppConfigUtil configUtil = AppConfigUtil.getInstance(context);
            String szPhoneCountryCodePrefix = "\\s";
            if (configUtil.mAppConfigBean.isMergeCountryCodePrefix()) {
                szPhoneCountryCodePrefix = "+" + configUtil.mAppConfigBean.getCountryCode() + szPhone;
                cursor = context.getContentResolver().query(uri, projection,
                                                            "address = ? or address = ? ",
                                                            new String[] {szPhone, szPhoneCountryCodePrefix},
                                                            "");
            } else {
                cursor = context.getContentResolver().query(uri, projection,
                                                            "address = ? ",
                                                            new String[] {szPhone},
                                                            "");
            }
            // 获取短信中最新的未读短信
            // Cursor cur = getContentResolver().query(uri, projection,
            // "read = ?", new String[]{"0"}, "date desc");
            if (cursor.moveToFirst()) {
                do {
                    /*int nSMSId = cursor.getInt(0);
                     String szType = (SMSBean.Type.values()[cursor.getInt(5)]).toString();
                     LogUtils.d(TAG, "nSMSId is : " + Integer.toString(nSMSId));
                     LogUtils.d(TAG, "szType is : " + szType);
                     */

                    SMSBean smsBean = new SMSBean();
                    smsBean.setId(cursor.getInt(0));
                    smsBean.setAddress(cursor.getString(1));
                    smsBean.setPerson(cursor.getInt(2));
                    smsBean.setBody(cursor.getString(3));
                    smsBean.setDate(cursor.getLong(4));
                    smsBean.setType(SMSBean.Type.values()[cursor.getInt(5)]);
                    /*long nDateDefault = Date.parse("2022/01/01");
                     if (smsBean.mnDate < nDateDefault) {
                     LogUtils.d(TAG, "smsBean >>> " + smsBean.toString());
                     smsBean.mnDate = nDateDefault;
                     }*/

                    returnList.add(smsBean);
                } while (cursor.moveToNext());

                if (!cursor.isClosed()) {
                    cursor.close();
                    cursor = null;
                }
            }
        } catch (SQLiteException e) {
            LogUtils.d("SQLiteException : ", e.getMessage());
        }
        return returnList;
    }


    //
    // 获得短信地址
    //
    public static String getSmsAddress(Intent intent) {

        Bundle bundle = intent.getExtras();
        Object messages[] = (Object[]) bundle.get("pdus");
        return SmsMessage.createFromPdu((byte[]) messages[0])
            .getDisplayOriginatingAddress();
    }

    public static int saveReceiveSms(Context context, String phoneNumber, String message, String readState, long time, String folderName) {
        int nResultId = -1;
        try {
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("body", message);
            values.put("read", readState); //"0" for have not read sms and "1" for have read sms
            values.put("date", Long.toString(time));
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Uri uri = Telephony.Sms.Sent.CONTENT_URI;
			if (folderName.equals("inbox")) {
				uri = Telephony.Sms.Inbox.CONTENT_URI;
			}
		    // 插入数据
            Uri uriReturn = context.getContentResolver().insert(uri, values);
            // 读取插入记录的 id
            nResultId = (int)ContentUris.parseId(uriReturn);
            /*} else {
			 // folderName  could be inbox or sent
			 context.getContentResolver().insert(Uri.parse("content://sms/" + folderName), values);
			 }*/
            //ToastUtils.show("saveReceiveSms done.");
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return nResultId;
    }

    public static long saveSendedSMS(Context context, String phoneNumber, String message) {
        SMSBean smsBean = new SMSBean();
        smsBean.setAddress(phoneNumber);
        smsBean.setBody(message);
        smsBean.setReadStatus(SMSBean.ReadStatus.UNREAD);
        smsBean.setDate(System.currentTimeMillis());

        return saveSendedSMS(context, smsBean);
    }


    public static long saveOldSendedSMS(Context context, SMSBean smsBean) {
        long nResultId = 0;
        try {
            ContentValues values = SMSBean.createOldSendedSMSContentValues(smsBean);
            LogUtils.d(TAG, "ContentValues created.");
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Uri uri = Telephony.Sms.Sent.CONTENT_URI;
            Uri uriReturn = context.getContentResolver().insert(uri, values);
            LogUtils.d(TAG, "inserted");
            // 读取插入记录的 id
            nResultId = ContentUris.parseId(uriReturn);
            /*} else {
             // folderName  could be inbox or sent
             context.getContentResolver().insert(Uri.parse(SMS_URI_SEND), values);
             }*/
            LogUtils.d(TAG, "nResultId : " + Long.toString(nResultId));
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return nResultId;
    }

    public static long saveSendedSMS(Context context, SMSBean smsBean) {
        long nResultId = 0;
        try {
            ContentValues values = SMSBean.createSendedSMSContentValues(smsBean);

            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Uri uri = Telephony.Sms.Sent.CONTENT_URI;
			Uri uriReturn = context.getContentResolver().insert(uri, values);
            // 读取插入记录的 id
            nResultId = ContentUris.parseId(uriReturn);
            /*} else {
			 // folderName  could be inbox or sent
			 context.getContentResolver().insert(Uri.parse(SMS_URI_SEND), values);
			 }*/
            //LogUtils.d(TAG, "nResultId : " + Integer.toString(nResultId));
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return nResultId;
    }

    // 发送短文本短信
    // phoneNumber：接收号码
    // message：发送内容
    //
    /*public static boolean sendSMS(Context context, String phoneNumber, String message) {
     //Getting intent and PendingIntent instance
     PendingIntent pi = PendingIntent.
     getBroadcast(context, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
     //Get the SmsManager instance and call the sendTextMessage method to send message
     SmsManager sms=SmsManager.getDefault();
     sms.sendTextMessage(phoneNumber, null, message, pi, null);
     if (SMSUtil.saveSendedSMS(context, phoneNumber, message) != 0) {
     Toast.makeText(context, "TO : <" + phoneNumber + "> Sended.", Toast.LENGTH_SHORT).show();
     return true;
     }
     return false;
     }*/

    //
    // 发送长文本短信，第一种方法发送短信，是将短信分割成多条短信，分别发给接收方。
    // phoneNumber：接收号码
    // message：发送内容
    //
    /*public static boolean sendMessageByInterface(Context context, String phoneNumber, String message) {
     SmsManager smsManager = SmsManager.getDefault();
     //String message = "这是一条很长的短信，需要分割";
     //int maxLength = SmsManager.getMaxMessageLength(); // 获取最大长度
     int maxLength = 70;
     //int remainingChars = maxLength;
     List<String> messages = new ArrayList<>();
     for (int i = 0; i < message.length(); i += maxLength) {
     if (i + maxLength >= message.length()) { // 如果到达结尾
     messages.add(message.substring(i));
     break;
     } else {
     messages.add(message.substring(i, i + maxLength));
     }
     }

     for (String msg : messages) {
     smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
     }
     ToastUtils.show("TO : <" + phoneNumber + "> Sended.");
     if (SMSUtil.saveSendedSMS(context, phoneNumber, message) != 0) {
     return true;
     }
     return false;
     }*/

    //
    // 发送长文本短信，第二种方法是将短信内容一次性发给接收方。在接收方的短信列表中，显示的是一条短信，但是实际上还是按多条短信计费。
    // phoneNumber：接收号码
    // message：发送内容
    //
    public static boolean sendMessageByInterface2(Context context, String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();

        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 0);

        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, deliverIntent, 0);

        if (message.length() > 70) {  
            ArrayList<String> msgs = sms.divideMessage(message);  
            ArrayList<PendingIntent> sentIntents =  new ArrayList<PendingIntent>();
            for (int i = 0;i < msgs.size();i++) {  
                sentIntents.add(sentPI);  
            }  
            sms.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, null);
            LogUtils.d(TAG, "Long SMS TO : <" + phoneNumber + "> Sended.");
        } else {  
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
            LogUtils.d(TAG, "SMS TO : <" + phoneNumber + "> Sended.");
        }
        if (SMSUtil.saveSendedSMS(context, phoneNumber, message) != 0) {
            ToastUtils.show("SMS TO : <" + phoneNumber + "> Sended.");
            return true;
        }
        return false;
	}
}
