package cc.winboll.studio.mymessagemanager.adapters;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/20 12:27:34
 * @Describe 短信过滤规则数据适配器
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.SMSAcceptRuleBean;
import cc.winboll.studio.mymessagemanager.utils.SMSReceiveRuleUtil;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;

public class SMSAcceptRuleArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "SMSAcceptRuleArrayAdapter";

    Context mContext;
    ArrayList<SMSAcceptRuleBean> mDataList;
    SMSReceiveRuleUtil mSMSReceiveRuleUtil;

    public SMSAcceptRuleArrayAdapter(Context context) {
        mContext = context;
        mSMSReceiveRuleUtil = SMSReceiveRuleUtil.getInstance(mContext, true);
        loadConfigData();
    }

    public void addSMSAcceptRule(SMSAcceptRuleBean bean) {
        mSMSReceiveRuleUtil.addRule(bean);
        notifyDataSetChanged();
    }

    public void loadConfigData() {
        mDataList = mSMSReceiveRuleUtil.loadConfigData();
        for (int i = 0; i < mDataList.size(); i++) {
            mDataList.get(i).setIsSimpleView(true);
            //LogUtils.d(TAG, "loadConfigData isEnable : " + Boolean.toString(mDataList.get(i).isEnable()));
        }
    }

    void deleteItem(int position) {
        mDataList.remove(position);
        mSMSReceiveRuleUtil.saveConfigData();
        notifyDataSetChanged();
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_smsacceptrule_simple, parent, false);
            return new SimpleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_smsacceptrule, parent, false);
            return new ComplexViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final SMSAcceptRuleBean item = mDataList.get(position);
        if (holder.getItemViewType() == 0) {
            final SimpleViewHolder viewHolder = (SimpleViewHolder) holder;
            viewHolder.mtvContent.setText(item.getRuleData());
            viewHolder.mcbEnable.setChecked(item.isEnable());
            viewHolder.mcbEnable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        item.setIsEnable(viewHolder.mcbEnable.isChecked());
                        item.setIsSimpleView(true);
                        mSMSReceiveRuleUtil.saveConfigData();
                        notifyDataSetChanged();
                    }
                });
            viewHolder.mtvRuleType.setText(item.getRuleType().toString());
            viewHolder.mbtnEdit.setOnClickListener(new View.OnClickListener(){
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
            if (item != null) {
                //Drawable drawableFrame = AppCompatResources.getDrawable(mContext, R.drawable.bg_frame);
                viewHolder.metContent.setText(item.getRuleData());
                if (item.getRuleType() == SMSAcceptRuleBean.RuleType.ACCEPT) {
                    viewHolder.mrbAccept.setChecked(true);
                    viewHolder.mrbRefuse.setChecked(false);
                }
                if (item.getRuleType() == SMSAcceptRuleBean.RuleType.REFUSE) {
                    viewHolder.mrbAccept.setChecked(false);
                    viewHolder.mrbRefuse.setChecked(true);
                }
                viewHolder.mrbAccept.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            viewHolder.mrbRefuse.setChecked(false);
                            item.setRuleType(SMSAcceptRuleBean.RuleType.ACCEPT);
                            mSMSReceiveRuleUtil.saveConfigData();
                            notifyDataSetChanged();
                        }
                    });
                viewHolder.mrbRefuse.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            viewHolder.mrbAccept.setChecked(false);
                            item.setRuleType(SMSAcceptRuleBean.RuleType.REFUSE);
                            mSMSReceiveRuleUtil.saveConfigData();
                            notifyDataSetChanged();
                        }
                    });
                viewHolder.mbtnUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View p1) {
                            if (position > 0) {
                                mDataList.add(position-1, mDataList.get(position));
                                mDataList.remove(position+1);
                                mSMSReceiveRuleUtil.saveConfigData();
                                notifyDataSetChanged();
                            }
                        }
                    });
                viewHolder.mbtnDown.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View p1) {
                            if (position < mDataList.size() - 1) {
                                //ToastUtils.show("mbtnDown");
                                ToastUtils.show("position " + Integer.toString(position));
                                mDataList.add(position+2, mDataList.get(position));
                                mDataList.remove(position);
                                mSMSReceiveRuleUtil.saveConfigData();
                                notifyDataSetChanged();
                            }
                        }
                    });
                viewHolder.mbtnOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View p1) {
                            item.setRuleData(viewHolder.metContent.getText().toString());
                            item.setRuleType(viewHolder.mrbAccept.isChecked() ?SMSAcceptRuleBean.RuleType.ACCEPT: SMSAcceptRuleBean.RuleType.REFUSE);
                            item.setIsEnable(viewHolder.mcbEnable.isChecked());
                            item.setIsSimpleView(true);
                            mSMSReceiveRuleUtil.saveConfigData();
                            notifyDataSetChanged();
                        }
                    });
                viewHolder.mbtnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View p1) {
                            deleteItem(position);
                        }
                    });
                viewHolder.mcbEnable.setChecked(item.isEnable());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public long getItemId(int posttion) {
        return 0;
    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView mtvContent;
        CheckBox mcbEnable;
        TextView mtvRuleType;
        Button mbtnEdit;

        SimpleViewHolder(View itemView) {
            super(itemView);
            mtvContent = itemView.findViewById(R.id.listviewsmsacceptrulesimpleTextView1);
            mcbEnable = itemView.findViewById(R.id.listviewsmsacceptrulesimpleCheckBox1);
            mtvRuleType = itemView.findViewById(R.id.listviewsmsacceptrulesimpleTextView2);
            mbtnEdit = itemView.findViewById(R.id.listviewsmsacceptrulesimpleButton1);
        }
    }

    private static class ComplexViewHolder extends RecyclerView.ViewHolder {
        EditText metContent;
        RadioButton mrbAccept;
        RadioButton mrbRefuse;
        CheckBox mcbEnable;
        Button mbtnUp;
        Button mbtnDown;
        Button mbtnOK;
        Button mbtnDelete;

        ComplexViewHolder(View itemView) {
            super(itemView);
            metContent = itemView.findViewById(R.id.listviewsmsacceptruleEditText1);
            mrbAccept = itemView.findViewById(R.id.listviewsmsacceptruleRadioButton1);
            mrbRefuse = itemView.findViewById(R.id.listviewsmsacceptruleRadioButton2);
            mcbEnable = itemView.findViewById(R.id.listviewsmsacceptruleCheckBox1);
            mbtnUp = itemView.findViewById(R.id.listviewsmsacceptruleButton3);
            mbtnDown = itemView.findViewById(R.id.listviewsmsacceptruleButton4);
            mbtnOK = itemView.findViewById(R.id.listviewsmsacceptruleButton1);
            mbtnDelete = itemView.findViewById(R.id.listviewsmsacceptruleButton2);
        }
    }
}
