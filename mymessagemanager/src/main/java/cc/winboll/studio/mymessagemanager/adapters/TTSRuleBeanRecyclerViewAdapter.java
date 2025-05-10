package cc.winboll.studio.mymessagemanager.adapters;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/18 16:08:20
 * @Describe TTSRuleBean RecyclerView Adapter
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.TTSPlayRuleActivity;
import cc.winboll.studio.mymessagemanager.beans.TTSPlayRuleBean;
import cc.winboll.studio.mymessagemanager.utils.TTSPlayRuleUtil;
import cc.winboll.studio.mymessagemanager.views.TTSRuleView;
import java.util.ArrayList;

public class TTSRuleBeanRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "TTSRuleBeanRecyclerViewAdapter";

    Context mContext;
    ArrayList<TTSPlayRuleBean> mDataList;
    OnTTSRuleChangeListener mOnTTSRuleChangeListener;
    TTSPlayRuleUtil mTTSPlayRuleUtil;

    public TTSRuleBeanRecyclerViewAdapter(TTSPlayRuleActivity ttsPlayRuleActivity, OnTTSRuleChangeListener onTTSRuleChangeListener) {
        mContext = ttsPlayRuleActivity;
        mOnTTSRuleChangeListener = onTTSRuleChangeListener;

        mTTSPlayRuleUtil = TTSPlayRuleUtil.getInstance(ttsPlayRuleActivity);
        mTTSPlayRuleUtil.initTTSPlayRuleActivity(ttsPlayRuleActivity);
		mDataList = mTTSPlayRuleUtil.loadConfigData();
    }

    public void addNewTTSRuleBean(TTSPlayRuleBean bean) {
        mTTSPlayRuleUtil.addNewTTSRuleBean(bean);
        //notifyDataSetChanged();
	}

    public void saveConfigData() {
        mTTSPlayRuleUtil.saveConfigData();
        //notifyDataSetChanged();
	}

    public void reloadConfigData() {
        mDataList = mTTSPlayRuleUtil.loadConfigData();
        notifyDataSetChanged();
    }

    public interface OnTTSRuleChangeListener {
        abstract void onTTSRuleChange(TTSPlayRuleBean bean);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_ttsplayrule_simple, parent, false);
            return new SimpleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_ttsplayrule, parent, false);
            return new ComplexViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final TTSPlayRuleBean item = mDataList.get(position);
        if (holder.getItemViewType() == 0) {
            SimpleViewHolder viewHolder = (SimpleViewHolder) holder;
            viewHolder.mSortNumber.setText(Integer.toString(position + 1));
            viewHolder.mtvDemoSMSText.setText(item.getDemoSMSText());
        } else {
            final ComplexViewHolder viewHolder = (ComplexViewHolder) holder;
            viewHolder.mSortNumber.setText(Integer.toString(position + 1));
            viewHolder.mtvDemoSMSText.setText(item.getDemoSMSText());
            viewHolder.mTTSRuleView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mOnTTSRuleChangeListener.onTTSRuleChange(item);
                    }
                });
            viewHolder.mbtnUp.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();
                        mTTSPlayRuleUtil.changeBeanPosition(position, true);
                        //notifyDataSetChanged();
                    }
                });
            viewHolder.mbtnDown.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();
                        mTTSPlayRuleUtil.changeBeanPosition(position, false);
                        //notifyDataSetChanged();
                    }
                });
            viewHolder.mchbEnable.setChecked(item.isEnable());
            viewHolder.mchbEnable.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mTTSPlayRuleUtil.setBeanEnable(position, ((CheckBox)v).isChecked());
                        //notifyDataSetChanged();
                    }
                });
            viewHolder.mTTSRuleView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View p1) {
                        // 弹出复制菜单
                        PopupMenu menu = new PopupMenu(mContext, viewHolder.mSortNumber);
                        //加载菜单资源
                        menu.getMenuInflater().inflate(R.menu.toolbar_ttsrule, menu.getMenu());
                        //设置点击事件的响应
                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    int nItemId = menuItem.getItemId();
                                    if (nItemId == R.id.deletettsrule) {
                                        mTTSPlayRuleUtil.deleteTTSRuleBean(position);
                                        //notifyDataSetChanged();
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
        TextView mSortNumber;
        TextView mtvDemoSMSText;


        SimpleViewHolder(View itemView) {
            super(itemView);
            mSortNumber = itemView.findViewById(R.id.itemttsplayrulesimpleTextView2);
            mtvDemoSMSText = itemView.findViewById(R.id.itemttsplayrulesimpleTextView1);

        }
    }

    private static class ComplexViewHolder extends RecyclerView.ViewHolder {
        TextView mSortNumber;
        TTSRuleView mTTSRuleView;
        LinearLayout mllMain;
        TextView mtvDemoSMSText;
        Button mbtnUp;
        Button mbtnDown;
        CheckBox mchbEnable;

        ComplexViewHolder(View itemView) {
            super(itemView);
            mSortNumber = itemView.findViewById(R.id.itemttsplayruleTextView2);
            mTTSRuleView = itemView.findViewById(R.id.listviewttsplayruleTTSRuleView1);
            mllMain = itemView.findViewById(R.id.itemttsplayruleLinearLayout1);
            mtvDemoSMSText = itemView.findViewById(R.id.itemttsplayruleTextView1);
            mbtnUp = itemView.findViewById(R.id.itemttsplayruleButton1);
            mbtnDown = itemView.findViewById(R.id.itemttsplayruleButton2);
            mchbEnable = itemView.findViewById(R.id.itemttsplayruleCheckBox1);

        }
    }
}
