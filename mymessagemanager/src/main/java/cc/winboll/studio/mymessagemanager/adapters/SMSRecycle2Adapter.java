package cc.winboll.studio.mymessagemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.SMSRecycleBean;
import cc.winboll.studio.mymessagemanager.utils.AddressUtils;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import cc.winboll.studio.mymessagemanager.views.DateAgoTextView;
import cc.winboll.studio.mymessagemanager.views.ProtectModeTextView;
import java.util.ArrayList;

public class SMSRecycle2Adapter extends RecyclerView.Adapter<SMSRecycle2Adapter.ViewHolder> {

    public static final String TAG = "SMSRecycle2Adapter";

    Context mContext;
    ArrayList<SMSRecycleBean> mDataList;
    String mszSMSRecycleListDataPath;
    int mScaleProgress;

    public SMSRecycle2Adapter(Context context, int scaleProgress) {
        mContext = context;
        mScaleProgress = scaleProgress;
        mszSMSRecycleListDataPath = SMSRecycleUtil.getSMSRecycleListDataPath(mContext);
        mDataList = new ArrayList<SMSRecycleBean>();
        mDataList = loadSMSRecycleList();
    }

    public void setScaleProgress(int scaleProgress) {
        mScaleProgress = scaleProgress;
    }

    public ArrayList<SMSRecycleBean> loadSMSRecycleList() {
        ArrayList<SMSRecycleBean> list = new ArrayList<SMSRecycleBean>();
        SMSRecycleBean.loadBeanListFromFile(mszSMSRecycleListDataPath, list, SMSRecycleBean.class);
        SMSRecycleBean.sortSMSByDeleteDateDesc(list, true);
        mDataList.clear();
        mDataList.addAll(list);
        return mDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_smsrecycle2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SMSRecycleBean item = mDataList.get(position);
        holder.mtvAddress.setText(AddressUtils.getFormattedAddress(item.getAddress()));
        holder.mdatvDeleteDate.setDate(item.getDeleteDate());
        holder.mProtectModeTextView.setContentTextWithScale(item.getBody(), mScaleProgress);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mtvAddress;
        DateAgoTextView mdatvDeleteDate;
        ProtectModeTextView mProtectModeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            mtvAddress = itemView.findViewById(R.id.listviewsmsrecycle2TextViewAddress);
            mdatvDeleteDate = itemView.findViewById(R.id.listviewsmsrecycle2DateAgoTextViewDelete);
            mProtectModeTextView = itemView.findViewById(R.id.listviewsmsrecycle2ProtectModeTextView);
        }
    }
}
