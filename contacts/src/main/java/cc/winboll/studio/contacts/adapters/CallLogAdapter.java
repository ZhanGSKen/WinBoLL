package cc.winboll.studio.contacts.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 13:09:32
 * @Describe CallLogAdapter
 */
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.beans.CallLogModel;
import cc.winboll.studio.contacts.utils.ContactUtils;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import com.hjq.toast.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {
    public static final String TAG = "CallLogAdapter";

    private List<CallLogModel> callLogList;
    ContactUtils mContactUtils;
    Context mContext;

    public CallLogAdapter(Context context, List<CallLogModel> callLogList) {
        mContext = context;
        this.mContactUtils = ContactUtils.getInstance(mContext);
        this.callLogList = callLogList;
    }

    @NonNull
    @Override
    public CallLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogViewHolder holder, int position) {
        final CallLogModel callLog = callLogList.get(position);
        holder.phoneNumber.setText(callLog.getPhoneNumber() + " ☎ " + mContactUtils.getContactsName(callLog.getPhoneNumber()));
        holder.callStatus.setText(callLog.getCallStatus());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.callDate.setText(dateFormat.format(callLog.getCallDate()));

        // 初始化拉动后拨号控件 
        holder.dialAOHPCTCSeekBar.setThumb(holder.itemView.getContext().getDrawable(R.drawable.ic_call));
        holder.dialAOHPCTCSeekBar.setThumbOffset(20);
        holder.dialAOHPCTCSeekBar.setOnOHPCListener(
            new AOHPCTCSeekBar.OnOHPCListener(){
                @Override
                public void onOHPCommit() {
                    String phoneNumber = callLog.getPhoneNumber().replaceAll("\\s", "");
                    ToastUtils.show(phoneNumber);
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
                    // 添加 FLAG_ACTIVITY_NEW_TASK 标志
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.itemView.getContext().startActivity(intent);
                }
            });
    }

    @Override
    public int getItemCount() {
        return callLogList.size();
    }

    public class CallLogViewHolder extends RecyclerView.ViewHolder {
        TextView phoneNumber, callStatus, callDate;
        Button dialButton;
        AOHPCTCSeekBar dialAOHPCTCSeekBar;

        public CallLogViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneNumber = itemView.findViewById(R.id.phone_number);
            callStatus = itemView.findViewById(R.id.call_status);
            callDate = itemView.findViewById(R.id.call_date);
            dialAOHPCTCSeekBar = itemView.findViewById(R.id.aohpctcseekbar_dial);
        }
    }

}

