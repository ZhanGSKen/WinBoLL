package cc.winboll.studio.contacts.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.model.CallLogModel;
import cc.winboll.studio.contacts.utils.ContactUtils;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import cc.winboll.studio.contacts.dun.Rules;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/26 13:09:32
 * @Describe 通话记录列表适配器
 */
public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {

    // ====================== 常量定义区 ======================
    public static final String TAG = "CallLogAdapter";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // ====================== 成员变量区 ======================
    private Context mContext;
    private List<CallLogModel> callLogList;
    private ContactUtils mContactUtils;

    // ====================== 构造函数区 ======================
    public CallLogAdapter(Context context, List<CallLogModel> callLogList) {
        LogUtils.d(TAG, "CallLogAdapter: 初始化适配器，数据量=" + callLogList.size());
        this.mContext = context;
        this.callLogList = callLogList;
        this.mContactUtils = ContactUtils.getInstance(mContext);
    }

    // ====================== 公共方法区 ======================
    /**
     * 重新加载联系人数据
     */
    public void relaodContacts() {
        LogUtils.d(TAG, "relaodContacts: 开始重新加载联系人数据");
        this.mContactUtils.reloadContacts();
        notifyDataSetChanged();
        LogUtils.d(TAG, "relaodContacts: 联系人数据加载完成，列表已刷新");
    }

    // ====================== RecyclerView 重写方法区 ======================
    @NonNull
    @Override
    public CallLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LogUtils.d(TAG, "onCreateViewHolder: 创建列表项ViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogViewHolder holder, int position) {
        LogUtils.d(TAG, "onBindViewHolder: 绑定列表项数据，position=" + position);
        final CallLogModel callLog = callLogList.get(position);

        // 绑定通话号码与联系人名称
        String contactName = mContactUtils.getContactName(callLog.getPhoneNumber());
        String phoneText = callLog.getPhoneNumber() + "☎" + (contactName == null ? "" : contactName);
        holder.phoneNumber.setText(phoneText);

        // 号码长按弹出菜单事件
        holder.phoneNumber.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View p1) {
					showPhonePopupMenu(holder.phoneNumber, callLog);
					return true;
				}
			});

        // 绑定通话状态与时间
        holder.callStatus.setText(callLog.getCallStatus());
        holder.callDate.setText(DATE_FORMAT.format(callLog.getCallDate()));

        // 初始化滑动拨号SeekBar
        initDialSeekBar(holder.dialAOHPCTCSeekBar, callLog);
    }

    @Override
    public int getItemCount() {
        return callLogList == null ? 0 : callLogList.size();
    }

    // ====================== 私有工具方法区 ======================
    /**
     * 显示号码操作弹窗菜单
     */
    private void showPhonePopupMenu(View anchorView, final CallLogModel callLog) {
        LogUtils.d(TAG, "showPhonePopupMenu: 弹出号码操作菜单");
        PopupMenu menu = new PopupMenu(mContext, anchorView);
        menu.getMenuInflater().inflate(R.menu.toolbar_calllog_phonenumber, menu.getMenu());

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem menuItem) {
					int itemId = menuItem.getItemId();
					if (itemId == R.id.item_calllog_phonenumber_copy) {
						// 复制号码到剪贴板
						ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("call_log_phone", callLog.getPhoneNumber());
						clipboard.setPrimaryClip(clip);
						Toast.makeText(mContext, "Copy to clipboard.", Toast.LENGTH_SHORT).show();
						LogUtils.d(TAG, "showPhonePopupMenu: 号码" + callLog.getPhoneNumber() + "已复制到剪贴板");
					} else if (itemId == R.id.item_calllog_phonenumber_yundun_test) {
						// 跳转到添加联系人页面
						//if (Rules.getInstance(mContext).isAllowed(callLog.getPhoneNumber(), false)) {
						if (Rules.getInstance(mContext).isAllowed(callLog.getPhoneNumber(), true)) {
							ToastUtils.show("(✔)" + callLog.getPhoneNumber() + " Is Allowed By YunDun.");
						} else {
							ToastUtils.show("(✘)YunDun Defense The Phone " + callLog.getPhoneNumber() + "");
						}
					} else if (itemId == R.id.item_calllog_phonenumber_add_contact) {
						// 跳转到添加联系人页面
						ContactUtils.jumpToAddContact(mContext, callLog.getPhoneNumber());
						LogUtils.d(TAG, "showPhonePopupMenu: 跳转添加联系人页面，号码=" + callLog.getPhoneNumber());
					}
					return true;
				}
			});
        menu.show();
    }

    /**
     * 初始化滑动拨号SeekBar
     */
    private void initDialSeekBar(AOHPCTCSeekBar seekBar, final CallLogModel callLog) {
        LogUtils.d(TAG, "initDialSeekBar: 初始化滑动拨号控件");
        seekBar.setThumb(seekBar.getContext().getDrawable(R.drawable.ic_call));
        seekBar.setBlurRightDP(80);
        seekBar.setThumbOffset(0);

        seekBar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {
				@Override
				public void onOHPCommit() {
					String phoneNumber = callLog.getPhoneNumber().replaceAll("\\s", "");
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
    public class CallLogViewHolder extends RecyclerView.ViewHolder {
        TextView phoneNumber;
        TextView callStatus;
        TextView callDate;
        AOHPCTCSeekBar dialAOHPCTCSeekBar;

        public CallLogViewHolder(@NonNull View itemView) {
            super(itemView);
            // Java7 适配：添加强制类型转换
            phoneNumber = (TextView) itemView.findViewById(R.id.phone_number);
            callStatus = (TextView) itemView.findViewById(R.id.call_status);
            callDate = (TextView) itemView.findViewById(R.id.call_date);
            dialAOHPCTCSeekBar = (AOHPCTCSeekBar) itemView.findViewById(R.id.aohpctcseekbar_dial);
        }
    }
}

