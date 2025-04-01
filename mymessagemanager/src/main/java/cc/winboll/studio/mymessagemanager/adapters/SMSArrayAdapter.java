package cc.winboll.studio.mymessagemanager.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.TTSPlayRuleActivity;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.mymessagemanager.dialogs.YesNoAlertDialog;
import cc.winboll.studio.mymessagemanager.utils.SMSReceiveRuleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.TTSPlayRuleUtil;
import cc.winboll.studio.mymessagemanager.views.DateAgoTextView;
import cc.winboll.studio.mymessagemanager.views.SMSView;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;
import cc.winboll.studio.mymessagemanager.beans.SMSAcceptRuleBean;
import cc.winboll.studio.mymessagemanager.utils.NotificationHelper;

public class SMSArrayAdapter extends BaseAdapter {

    public static String TAG = "SMSArrayAdapter";

    Context mContext;
    String mszPhone;
    ArrayList<SMSBean> mData;

    public SMSArrayAdapter(Context context, String szPhone) {
        mContext = context;
        mszPhone = szPhone;
        mData = new ArrayList<SMSBean>();
        mData = loadSMSList(context, szPhone);
    }

    ArrayList<SMSBean> loadSMSList(Context context, String szPhone) {
        ArrayList<SMSBean> data = SMSUtil.getSMSListByPhone(context, szPhone);
        SMSBean.sortSMSByDateDesc(data, false);
        mData.clear();
        mData.addAll(data);
        return mData;
    }

    public void cancelMessageNotification() {
        for (SMSBean bean : mData) {
            NotificationHelper notificationHelper = new NotificationHelper(mContext);
            notificationHelper.cancelNotification(bean.getId());
        }
    }

    void deleteSMSById(final int position) {
        YesNoAlertDialog.show(mContext,
                              "短信删除提示",
                              "请确认删除动作！"
                              , (new YesNoAlertDialog.OnDialogResultListener(){

                                  @Override
                                  public void onYes() {
                                      SMSRecycleUtil.addSMSRecycleItem(mContext, (SMSBean)getItem(position));
                                      SMSUtil.deleteSMSById(mContext, ((SMSBean)getItem(position)).getId());
                                      mData.remove(position);
                                      notifyDataSetChanged();
                                      Toast.makeText(mContext, "SMS delete.", Toast.LENGTH_SHORT).show();
                                  }

                                  @Override
                                  public void onNo() {

                                  }
                              }));
    }

    public void reLoadSMSList(Context context, String szPhone) {
        mData = loadSMSList(context, szPhone);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int p1) {
        return mData.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_sms, parent, false);
            viewHolder.mSMSView = convertView.findViewById(R.id.listviewsmsSMSView1);

            viewHolder.mllMain = convertView.findViewById(R.id.listviewsmspart1LinearLayout1);
            viewHolder.mllContent = convertView.findViewById(R.id.listviewsmspart1LinearLayout2);
            viewHolder.mvMenu = convertView.findViewById(R.id.listviewsmspart1View1);
            viewHolder.mtvBody = (TextView) convertView
                .findViewById(R.id.listviewsmspart1TextView1);
            viewHolder.mdatvDate = convertView.findViewById(R.id.listviewsmspart1DateAgoTextView1);

            viewHolder.mvLeft = convertView.findViewById(R.id.listviewsmsView1);
			viewHolder.mvRight = convertView.findViewById(R.id.listviewsmsView2);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final SMSBean item = (SMSBean) getItem(position);
        if (item != null) {
            if (item.getType() == SMSBean.Type.INBOX) {
				viewHolder.mvLeft.setVisibility(View.GONE);
				viewHolder.mvRight.setVisibility(View.VISIBLE);

				viewHolder.mSMSView.setSMSType(SMSView.SMSType.INBOX);
				viewHolder.mllMain.setGravity(Gravity.LEFT);
			} else {
				viewHolder.mvLeft.setVisibility(View.VISIBLE);
				viewHolder.mvRight.setVisibility(View.GONE);
				viewHolder.mSMSView.setSMSType(SMSView.SMSType.SEND);
            }

            //Drawable drawableFrame = AppCompatResources.getDrawable(mContext, R.drawable.bg_frame);
            //viewHolder.mllContent.setBackground(drawableFrame);

            viewHolder.mtvBody.setText(item.getBody());
            viewHolder.mdatvDate.setDate(item.getDate());
            //viewHolder.mtvType.setText(" [" + item.getType().name() + "] ");

            viewHolder.mSMSView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View p1) {
                        // 弹出复制菜单
                        PopupMenu menu = new PopupMenu(mContext, viewHolder.mvMenu);
                        //加载菜单资源
                        menu.getMenuInflater().inflate(R.menu.toolbar_item_sms, menu.getMenu());
                        //设置点击事件的响应
                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    int nItemId = menuItem.getItemId();
                                    if (nItemId == R.id.copy) {
                                        // Gets a handle to the clipboard service.
                                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                        // Creates a new text clip to put on the clipboard
                                        ClipData clip = ClipData.newPlainText("simple text", item.getBody());
                                        // Set the clipboard's primary clip.
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(mContext, "Copy to clipboard.", Toast.LENGTH_SHORT).show();
                                    } else if (nItemId == R.id.delete) {
                                        deleteSMSById(position);
                                    } else if (nItemId == R.id.addttsrule) {
                                        Intent intent = new Intent(mContext, TTSPlayRuleActivity.class);
                                        intent.putExtra(TTSPlayRuleActivity.EXTRA_TTSDEMOTEXT, viewHolder.mtvBody.getText().toString());
                                        mContext.startActivity(intent);
                                    } else if (nItemId == R.id.testtts) {
                                        //Toast.makeText(mContext, "Testing TTS.", Toast.LENGTH_SHORT).show();
                                        TTSPlayRuleUtil ttsPlayRuleUtil = TTSPlayRuleUtil.getInstance(mContext);
                                        ttsPlayRuleUtil.speakTTSAnalyzeModeText(viewHolder.mtvBody.getText().toString());
                                    } else if (nItemId == R.id.testreceivetule) {
                                        //Toast.makeText(mContext, "Testing Receive Rule.", Toast.LENGTH_SHORT).show();
                                        SMSReceiveRuleUtil smsReceiveRuleUtil = SMSReceiveRuleUtil.getInstance(mContext, true);
                                        SMSReceiveRuleUtil.MatchResult matchResult = smsReceiveRuleUtil.getReceiveRuleMatchResult(mContext, viewHolder.mtvBody.getText().toString());
                                        if (matchResult.matchPositionInRules == SMSReceiveRuleUtil.VALID_MATCHRESULT_POSITION
                                            || matchResult.matchRuleType == SMSAcceptRuleBean.RuleType.REGEXPPIUTILS_ISPPIOK_FALSE) {
                                            //ToastUtils.show("Test");
                                            ToastUtils.show("Not Receive Rule is Matched.\nResult is : " + matchResult.matchRuleType);
                                        } else {
                                            ToastUtils.show("MatchResult : " + matchResult.matchRuleType + "\nReceiveRule Match Position : " + Integer.toString(matchResult.matchPositionInRules + 1));
                                        }
                                    }

                                    return true;
                                }
                            });
                        //一定要调用show()来显示弹出式菜单
                        menu.show();

                        return true;
                    }
                });
        }

        return convertView;
    }

    class ViewHolder {
        SMSView mSMSView;
        LinearLayout mllMain;
        LinearLayout mllContent;
        TextView mtvBody;
        View mvMenu;
        DateAgoTextView mdatvDate;
        View mvLeft;
		View mvRight;
    }
}
