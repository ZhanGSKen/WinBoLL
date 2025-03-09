package cc.winboll.studio.contacts.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 13:35:44
 * @Describe ContactAdapter
 */
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.beans.ContactModel;
import com.hjq.toast.ToastUtils;
import java.util.List;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public static final String TAG = "ContactAdapter";

    private static final int REQUEST_CALL_PHONE = 1;

    private List<ContactModel> contactList;

    public ContactAdapter(List<ContactModel> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        final ContactModel contact = contactList.get(position);
        holder.contactName.setText(contact.getName());
        holder.contactNumber.setText(contact.getNumber());

        // 初始化拉动后拨号控件 
        holder.dialAOHPCTCSeekBar.setThumb(holder.itemView.getContext().getDrawable(R.drawable.ic_call));
        holder.dialAOHPCTCSeekBar.setBlurRightDP(80);
        holder.dialAOHPCTCSeekBar.setOnOHPCListener(
            new AOHPCTCSeekBar.OnOHPCListener(){
                @Override
                public void onOHPCommit() {
                    String phoneNumber = contact.getNumber().replaceAll("\\s", "");
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
        return contactList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        TextView contactNumber;
        AOHPCTCSeekBar dialAOHPCTCSeekBar;
        
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            contactNumber = itemView.findViewById(R.id.contact_number);
            dialAOHPCTCSeekBar = itemView.findViewById(R.id.aohpctcseekbar_dial);
        }
    }
}

