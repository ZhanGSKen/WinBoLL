package cc.winboll.studio.contacts.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 13:09:32
 * @Describe CallLogAdapter
 */
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.beans.CallLogModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {
    public static final String TAG = "CallLogAdapter";

    private List<CallLogModel> callLogList;

    public CallLogAdapter(List<CallLogModel> callLogList) {
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
        CallLogModel callLog = callLogList.get(position);
        holder.phoneNumber.setText(callLog.getPhoneNumber());
        holder.callStatus.setText(callLog.getCallStatus());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.callDate.setText(dateFormat.format(callLog.getCallDate()));
    }

    @Override
    public int getItemCount() {
        return callLogList.size();
    }

    public class CallLogViewHolder extends RecyclerView.ViewHolder {
        TextView phoneNumber, callStatus, callDate;

        public CallLogViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneNumber = itemView.findViewById(R.id.phone_number);
            callStatus = itemView.findViewById(R.id.call_status);
            callDate = itemView.findViewById(R.id.call_date);
        }
    }
}

