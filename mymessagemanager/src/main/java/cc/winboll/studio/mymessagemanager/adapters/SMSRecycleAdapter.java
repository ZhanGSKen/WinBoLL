package cc.winboll.studio.mymessagemanager.adapters;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 17:07:34
 * @Describe 短信回收站短信数据适配器
 */
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.TTSPlayRuleActivity;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.mymessagemanager.beans.SMSRecycleBean;
import cc.winboll.studio.mymessagemanager.dialogs.YesNoAlertDialog;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.TTSPlayRuleUtil;
import cc.winboll.studio.mymessagemanager.utils.UserVisionSystemProtectModeUtil;
import cc.winboll.studio.mymessagemanager.views.DateAgoTextView;
import cc.winboll.studio.mymessagemanager.views.SMSView;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;

public class SMSRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "SMSRecycleAdapter";

    Context mContext;
    ArrayList<SMSRecycleBean> mDataList;
    String mszSMSRecycleListDataPath;
    AppConfigUtil mAppConfigUtil;

    public SMSRecycleAdapter(Context context) {
        mContext = context;
        mAppConfigUtil = AppConfigUtil.getInstance(mContext);
        mszSMSRecycleListDataPath = SMSRecycleUtil.getSMSRecycleListDataPath(mContext);
        mDataList = new ArrayList<SMSRecycleBean>();
        mDataList = loadSMSRecycleList();
    }

    public ArrayList<SMSRecycleBean> loadSMSRecycleList() {
        ArrayList<SMSRecycleBean> list = new ArrayList<SMSRecycleBean>();
        SMSRecycleBean.loadBeanListFromFile(mszSMSRecycleListDataPath, list, SMSRecycleBean.class);
        SMSRecycleBean.sortSMSByDeleteDateDesc(list, true);
        mDataList.clear();
        mDataList.addAll(list);
        for (int i = 0; i < mDataList.size(); i++) {
            mDataList.get(i).setIsSimpleView(true);
        }
        //ToastUtils.show("mDataList.size() : " + Integer.toString(mDataList.size()));
        return mDataList;
    }

    public void saveSMSRecycleList() {
        SMSBean.saveBeanListToFile(mszSMSRecycleListDataPath, mDataList);
    }

    void restoreSMSRecycleItem(final int position) {
        YesNoAlertDialog.show(mContext,
                              "短信恢复提示",
                              "是否恢复该短信！"
                              , (new YesNoAlertDialog.OnDialogResultListener(){

                                  @Override
                                  public void onYes() {
                                      SMSBean item = mDataList.get(position);
                                      long nResultId = 0;
                                      //LogUtils.d(TAG, "item.getType() : " + item.getType());
                                      if (item.getType() == SMSBean.Type.INBOX) {
                                          nResultId = SMSUtil.saveReceiveSms(mContext, item.getAddress(), item.getBody(),
                                                                             (item.getReadStatus() == SMSBean.ReadStatus.READ) ?"1": "0",
                                                                             item.getDate(), "inbox");
                                      } else if (item.getType() == SMSBean.Type.SENT) {
                                          nResultId = SMSUtil.saveOldSendedSMS(mContext, item);
                                      }
                                      if (nResultId == 0) {
                                          ToastUtils.show("SMS Restored Failed!\nPlease confirm that the application has the SMS management authority.");
                                      } else {
                                          mDataList.remove(position);
                                          SMSBean.saveBeanListToFile(mszSMSRecycleListDataPath, mDataList);
                                          notifyDataSetChanged();
                                          ToastUtils.show("SMS Restored. ID : " + Long.toString(nResultId));
                                      }
                                  }

                                  @Override
                                  public void onNo() {

                                  }
                              }));
    }

    void deleteSMSRecycleItem(final int position) {
        YesNoAlertDialog.show(mContext,
                              "短信删除提示",
                              "请确认删除动作！"
                              , (new YesNoAlertDialog.OnDialogResultListener(){

                                  @Override
                                  public void onYes() {
                                      mDataList.remove(position);
                                      SMSBean.saveBeanListToFile(mszSMSRecycleListDataPath, mDataList);
                                      notifyDataSetChanged();
                                      Toast.makeText(mContext, "SMS delete.", Toast.LENGTH_SHORT).show();
                                  }

                                  @Override
                                  public void onNo() {

                                  }
                              }));
    }

    public void reLoadSMSList(Context context, String szPhone) {
        mDataList = loadSMSRecycleList();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataList.get(position).isSimpleView()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_smsrecycle_simple, parent, false);
            return new SimpleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_smsrecycle, parent, false);
            return new ComplexViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final SMSRecycleBean item = mDataList.get(position);
        if (holder.getItemViewType() == 0) {
            SimpleViewHolder viewHolder = (SimpleViewHolder) holder;
            viewHolder.mtvAddress.setText(item.getAddress());
            viewHolder.mbtnViewBody.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < mDataList.size(); i++) {
                            mDataList.get(i).setIsSimpleView(true);
                        }
                        item.setIsSimpleView(false);
                        notifyDataSetChanged();
                        //ToastUtils.show("setIsSimpleView");
                    }
                });
        } else {
            final ComplexViewHolder viewHolder = (ComplexViewHolder) holder;
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
            viewHolder.mtvAddress.setText(item.getAddress());
            viewHolder.mdatvDeleteDate.setDate(item.getDeleteDate());
            viewHolder.mdatvDate.setDate(item.getDate());
            if(mAppConfigUtil.mAppConfigBean.isSMSRecycleProtectMode()) {
                viewHolder.mtvBody.setText("ProtectMode : " + UserVisionSystemProtectModeUtil.PreviewShuffleSMS(item.getBody(), mAppConfigUtil.mAppConfigBean.getProtectModerRefuseChars(), mAppConfigUtil.mAppConfigBean.getProtectModerReplaceChars()));
            } else {
                viewHolder.mtvBody.setText(item.getBody());
            }
            /*viewHolder.mTagsAdapter = new TagsAdapter(mContext, item);
             RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
             viewHolder.mTagsRecyclerView.setLayoutManager(layoutManager);
             viewHolder.mTagsRecyclerView.setAdapter(viewHolder.mTagsAdapter);
             // 这个设置可以解决嵌套listvew的内部listview拉动问题。
             viewHolder.mTagsRecyclerView.setParentScrollView(viewHolder.mScrollView);*/
            viewHolder.mllMain.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View p1) {
                        // 弹出复制菜单
                        PopupMenu menu = new PopupMenu(mContext, viewHolder.mvMenu);
                        //加载菜单资源
                        menu.getMenuInflater().inflate(R.menu.toolbar_item_smsrecycle, menu.getMenu());
                        menu.getMenuInflater().inflate(R.menu.toolbar_item_sms, menu.getMenu());
                        //设置点击事件的响应
                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    int nItemId = menuItem.getItemId();
                                    if (nItemId == R.id.item_restoresms) {
                                        restoreSMSRecycleItem(position);
                                    } else if (nItemId == R.id.copy) {
                                        // Gets a handle to the clipboard service.
                                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                        // Creates a new text clip to put on the clipboard
                                        ClipData clip = ClipData.newPlainText("simple text", item.getBody());
                                        // Set the clipboard's primary clip.
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(mContext, "Copy to clipboard.", Toast.LENGTH_SHORT).show();
                                    } else if (nItemId == R.id.delete) {
                                        deleteSMSRecycleItem(position);
                                        /*loadSMSRecycleList();
                                         mDataList.remove(item);
                                         saveSMSRecycleList();*/
                                        notifyDataSetChanged();
                                    } else if (nItemId == R.id.addttsrule) {
                                        Intent intent = new Intent(mContext, TTSPlayRuleActivity.class);
                                        intent.putExtra(TTSPlayRuleActivity.EXTRA_TTSDEMOTEXT, viewHolder.mtvBody.getText().toString());
                                        mContext.startActivity(intent);
                                    } else if (nItemId == R.id.testtts) {
                                        //Toast.makeText(mContext, "Testing TTS.", Toast.LENGTH_SHORT).show();
                                        TTSPlayRuleUtil ttsPlayRuleUtil = TTSPlayRuleUtil.getInstance(mContext);
                                        ttsPlayRuleUtil.speakTTSAnalyzeModeText(viewHolder.mtvBody.getText().toString());
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
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView mtvAddress;
        Button mbtnViewBody;

        SimpleViewHolder(View itemView) {
            super(itemView);
            mtvAddress = itemView.findViewById(R.id.listviewsmsrecyclesimpleTextView1);
            mbtnViewBody = itemView.findViewById(R.id.listviewsmsrecyclesimpleButton1);
        }
    }

    private static class ComplexViewHolder extends RecyclerView.ViewHolder {
        TextView mtvAddress;
        DateAgoTextView mdatvDeleteDate;
        SMSView mSMSView;
        LinearLayout mllMain;
        LinearLayout mllContent;
        TextView mtvBody;
        View mvMenu;
        DateAgoTextView mdatvDate;
        View mvLeft;
		View mvRight;

        ComplexViewHolder(View itemView) {
            super(itemView);
            mtvAddress = itemView.findViewById(R.id.listviewsmsrecycleTextView1);
            mdatvDeleteDate = itemView.findViewById(R.id.listviewsmsrecycleDateAgoTextView1);
            mSMSView = itemView.findViewById(R.id.listviewsmsrecycleSMSView1);
            mllMain = itemView.findViewById(R.id.listviewsmspart1LinearLayout1);
            mllContent = itemView.findViewById(R.id.listviewsmspart1LinearLayout2);
            mvMenu = itemView.findViewById(R.id.listviewsmsrecycleView1);
            mtvBody = itemView.findViewById(R.id.listviewsmspart1TextView1);
            mdatvDate = itemView.findViewById(R.id.listviewsmspart1DateAgoTextView1);
            mvLeft = itemView.findViewById(R.id.listviewsmsrecycleView1);
            mvRight = itemView.findViewById(R.id.listviewsmsrecycleView2);
        }
    }
}
