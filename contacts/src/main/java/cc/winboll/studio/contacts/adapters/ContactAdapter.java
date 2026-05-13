package cc.winboll.studio.contacts.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.model.ContactModel;
import cc.winboll.studio.contacts.utils.ContactUtils;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/26 13:35:44
 * @Describe 联系人列表适配器
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    // ====================== 常量定义区 ======================
    public static final String TAG = "ContactAdapter";
    // 移除未使用的 REQUEST_CALL_PHONE 常量，精简冗余代码

    // ====================== 成员变量区 ======================
    private Context mContext;
    private List<ContactModel> contactList;

    // ====================== 构造函数区 ======================
    public ContactAdapter(Context context, List<ContactModel> contactList) {
        LogUtils.d(TAG, "ContactAdapter: 初始化适配器，联系人数量=" + contactList.size());
        this.mContext = context;
        this.contactList = contactList;
    }

    // ====================== RecyclerView 重写方法区 ======================
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LogUtils.d(TAG, "onCreateViewHolder: 创建联系人列表项ViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        LogUtils.d(TAG, "onBindViewHolder: 绑定联系人列表项数据，position=" + position);
        final ContactModel contact = contactList.get(position);

        // 绑定联系人名称与号码
        holder.contactName.setText(contact.getName());
        holder.contactNumber.setText(contact.getNumber());

        // 长按联系人条目弹出操作菜单
        holder.llPhoneNumberMain.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showContactPopupMenu(holder.llPhoneNumberMain, contact);
					return true;
				}
			});

        // 初始化滑动拨号SeekBar
        initDialSeekBar(holder.dialAOHPCTCSeekBar, contact);
    }

    @Override
    public int getItemCount() {
        // 增加空指针判断，避免空列表崩溃
        return contactList == null ? 0 : contactList.size();
    }

    // ====================== 私有工具方法区 ======================
    /**
     * 显示联系人操作弹窗菜单
     */
    private void showContactPopupMenu(View anchorView, final ContactModel contact) {
        LogUtils.d(TAG, "showContactPopupMenu: 弹出联系人操作菜单");
        PopupMenu menu = new PopupMenu(mContext, anchorView);
        menu.getMenuInflater().inflate(R.menu.toolbar_contact_phonenumber, menu.getMenu());

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem menuItem) {
					int itemId = menuItem.getItemId();
					if (itemId == R.id.item_contact_phonenumber_copy) {
						// 复制联系人号码到剪贴板
						ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("contact_phone", contact.getNumber());
						clipboard.setPrimaryClip(clip);
						Toast.makeText(mContext, "Copy to clipboard.", Toast.LENGTH_SHORT).show();
						LogUtils.d(TAG, "showContactPopupMenu: 联系人号码" + contact.getNumber() + "已复制到剪贴板");
					} else if (itemId == R.id.item_calllog_phonenumber_edit_contact) {
						// 跳转到编辑联系人页面
						Long contactId = ContactUtils.getContactIdByPhone(mContext, contact.getNumber());
						ContactUtils.jumpToEditContact(mContext, contact.getNumber(), contactId);
						LogUtils.d(TAG, "showContactPopupMenu: 跳转编辑联系人页面，号码=" + contact.getNumber() + "，ID=" + contactId);
					}
					return true;
				}
			});
        menu.show();
    }

    /**
     * 初始化滑动拨号SeekBar
     */
    private void initDialSeekBar(AOHPCTCSeekBar seekBar, final ContactModel contact) {
        LogUtils.d(TAG, "initDialSeekBar: 初始化滑动拨号控件");
        seekBar.setThumb(seekBar.getContext().getDrawable(R.drawable.ic_call));
        seekBar.setBlurRightDP(80);
        seekBar.setThumbOffset(0);

        seekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {
				@Override
				public void onOHPCommit() {
					String phoneNumber = contact.getNumber().replaceAll("\\s", "");
					LogUtils.d(TAG, "initDialSeekBar: 滑动拨号触发，号码=" + phoneNumber);
					ToastUtils.show(phoneNumber);

					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			});
    }

    // ====================== ViewHolder 内部类 ======================
    public class ContactViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llPhoneNumberMain;
        TextView contactName;
        TextView contactNumber;
        AOHPCTCSeekBar dialAOHPCTCSeekBar;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            // Java7 适配：添加强制类型转换
            llPhoneNumberMain = (LinearLayout) itemView.findViewById(R.id.itemcontactLinearLayout1);
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            contactNumber = (TextView) itemView.findViewById(R.id.contact_number);
            dialAOHPCTCSeekBar = (AOHPCTCSeekBar) itemView.findViewById(R.id.aohpctcseekbar_dial);
        }
    }
}

