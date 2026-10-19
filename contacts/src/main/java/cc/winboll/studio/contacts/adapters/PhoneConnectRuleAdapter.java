package cc.winboll.studio.contacts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.model.PhoneConnectRuleBean;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.views.LeftScrollView;
import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/02 17:27:41
 * @Describe 通话规则列表适配器，支持简单查看/编辑两种视图切换
 */
public class PhoneConnectRuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ====================== 常量定义区 ======================
    public static final String TAG = "PhoneConnectRuleAdapter";
    private static final int VIEW_TYPE_SIMPLE = 0;
    private static final int VIEW_TYPE_EDIT = 1;
    private static final String NULL_RULE_TEXT = "[NULL]";

    // ====================== 成员变量区 ======================
    private Context mContext;
    private List<PhoneConnectRuleBean> mRuleList;

    // ====================== 构造函数区 ======================
    public PhoneConnectRuleAdapter(Context context, List<PhoneConnectRuleBean> ruleList) {
        LogUtils.d(TAG, "PhoneConnectRuleAdapter: 初始化适配器，规则数量=" + ruleList.size());
        this.mContext = context;
        this.mRuleList = ruleList;
    }

    // ====================== RecyclerView 重写方法区 ======================
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == VIEW_TYPE_SIMPLE) {
            LogUtils.d(TAG, "onCreateViewHolder: 创建简单视图ViewHolder");
            View view = inflater.inflate(R.layout.view_phone_connect_rule_simple, parent, false);
            return new SimpleViewHolder(parent, view);
        } else {
            LogUtils.d(TAG, "onCreateViewHolder: 创建编辑视图ViewHolder");
            View view = inflater.inflate(R.layout.view_phone_connect_rule, parent, false);
            return new EditViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final PhoneConnectRuleBean model = mRuleList.get(position);
        LogUtils.d(TAG, "onBindViewHolder: 绑定规则数据，position=" + position + "，视图类型=" + getItemViewType(position));

        if (holder instanceof SimpleViewHolder) {
            bindSimpleViewHolder((SimpleViewHolder) holder, model, position);
        } else if (holder instanceof EditViewHolder) {
            bindEditViewHolder((EditViewHolder) holder, model, position);
        }
    }

    @Override
    public int getItemCount() {
        return mRuleList == null ? 0 : mRuleList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mRuleList.get(position).isSimpleView() ? VIEW_TYPE_SIMPLE : VIEW_TYPE_EDIT;
    }

    // ====================== 私有视图绑定方法区 ======================
    /**
     * 绑定简单视图数据
     */
    private void bindSimpleViewHolder(final SimpleViewHolder holder, final PhoneConnectRuleBean model, final int position) {
        // 绑定规则文本，空值显示[NULL]
        String ruleText = model.getRuleText().trim().isEmpty() ? NULL_RULE_TEXT : model.getRuleText().trim();
        holder.tvRuleText.setText(ruleText);
        // 设置复选框状态并禁用编辑
        holder.checkBoxAllow.setChecked(model.isAllowConnection());
        holder.checkBoxAllow.setEnabled(false);
        holder.checkBoxEnable.setChecked(model.isEnable());
        holder.checkBoxEnable.setEnabled(false);

        // 设置左滑操作监听
        holder.scrollView.setOnActionListener(new LeftScrollView.OnActionListener() {
				@Override
				public void onUp() {
					LogUtils.d(TAG, "onUp: 规则上移，position=" + position);
					moveRuleUp(position);
					holder.scrollView.smoothScrollTo(0, 0);
				}

				@Override
				public void onDown() {
					LogUtils.d(TAG, "onDown: 规则下移，position=" + position);
					moveRuleDown(position);
					holder.scrollView.smoothScrollTo(0, 0);
				}

				@Override
				public void onEdit() {
					LogUtils.d(TAG, "onEdit: 切换到编辑视图，position=" + position);
					model.setIsSimpleView(false);
					notifyItemChanged(position);
					holder.scrollView.smoothScrollTo(0, 0);
				}

				@Override
				public void onDelete() {
					LogUtils.d(TAG, "onDelete: 触发规则删除确认，position=" + position);
					showDeleteConfirmDialog(holder.scrollView.getContext(), model, position);
				}
			});
    }

    /**
     * 绑定编辑视图数据
     */
    private void bindEditViewHolder(final EditViewHolder holder, final PhoneConnectRuleBean model, final int position) {
        // 绑定规则文本到输入框
        holder.editText.setText(model.getRuleText());
        // 绑定复选框状态
        holder.checkBoxAllow.setChecked(model.isAllowConnection());
        holder.checkBoxEnable.setChecked(model.isEnable());

        // 确认按钮点击事件
        holder.buttonConfirm.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String newRuleText = holder.editText.getText().toString().trim();
					model.setRuleText(newRuleText);
					model.setIsAllowConnection(holder.checkBoxAllow.isChecked());
					model.setIsEnable(holder.checkBoxEnable.isChecked());
					model.setIsSimpleView(true);

					// 保存规则并刷新视图
					Rules.getInstance(mContext).saveRules();
					notifyItemChanged(position);
					Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
					LogUtils.d(TAG, "bindEditViewHolder: 规则保存成功，position=" + position + "，规则内容=" + newRuleText);
				}
			});
    }

    // ====================== 私有业务工具方法区 ======================
    /**
     * 规则上移
     */
    private void moveRuleUp(int position) {
        if (position <= 0) {
            ToastUtils.show("已到顶部，无法上移");
            return;
        }
        ArrayList<PhoneConnectRuleBean> ruleList = Rules.getInstance(mContext).getPhoneBlacRuleBeanList();
        swapRulePosition(ruleList, position, position - 1);
    }

    /**
     * 规则下移
     */
    private void moveRuleDown(int position) {
        ArrayList<PhoneConnectRuleBean> ruleList = Rules.getInstance(mContext).getPhoneBlacRuleBeanList();
        if (position >= ruleList.size() - 1) {
            ToastUtils.show("已到底部，无法下移");
            return;
        }
        swapRulePosition(ruleList, position, position + 1);
    }

    /**
     * 交换规则位置
     */
    private void swapRulePosition(ArrayList<PhoneConnectRuleBean> list, int fromPos, int toPos) {
        PhoneConnectRuleBean temp = list.get(fromPos);
        list.set(fromPos, list.get(toPos));
        list.set(toPos, temp);
        Rules.getInstance(mContext).saveRules();
        notifyDataSetChanged();
        LogUtils.d(TAG, "swapRulePosition: 规则位置交换完成，from=" + fromPos + "，to=" + toPos);
    }

    /**
     * 显示删除确认弹窗
     */
    private void showDeleteConfirmDialog(Context dialogContext, final PhoneConnectRuleBean model, final int position) {
        YesNoAlertDialog.show(dialogContext, "删除确认", "是否删除该通话规则？", new YesNoAlertDialog.OnDialogResultListener() {
				@Override
				public void onYes() {
					ArrayList<PhoneConnectRuleBean> ruleList = Rules.getInstance(mContext).getPhoneBlacRuleBeanList();
					ruleList.remove(position);
					Rules.getInstance(mContext).saveRules();
					notifyDataSetChanged();
					LogUtils.d(TAG, "showDeleteConfirmDialog: 规则删除成功，position=" + position);
				}

				@Override
				public void onNo() {
					LogUtils.d(TAG, "showDeleteConfirmDialog: 用户取消删除规则，position=" + position);
				}
			});
    }

    // ====================== ViewHolder 内部类区 ======================
    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        LeftScrollView scrollView;
        TextView tvRuleText;
        CheckBox checkBoxAllow;
        CheckBox checkBoxEnable;

        public SimpleViewHolder(@NonNull ViewGroup parent, @NonNull View itemView) {
            super(itemView);
            scrollView = (LeftScrollView) itemView.findViewById(R.id.scrollView);
            // 初始化简单视图内容布局
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            View viewContent = inflater.inflate(R.layout.view_phone_connect_rule_simple_content, parent, false);
            tvRuleText = (TextView) viewContent.findViewById(R.id.ruletext_tv);
            checkBoxAllow = (CheckBox) viewContent.findViewById(R.id.checkbox_allow);
            checkBoxEnable = (CheckBox) viewContent.findViewById(R.id.checkbox_enable);
            // 设置内容宽度并添加到滚动视图
            scrollView.setContentWidth(parent.getWidth());
            scrollView.addContentLayout(viewContent);
        }
    }

    static class EditViewHolder extends RecyclerView.ViewHolder {
        EditText editText;
        CheckBox checkBoxAllow;
        CheckBox checkBoxEnable;
        Button buttonConfirm;

        public EditViewHolder(@NonNull View itemView) {
            super(itemView);
            // Java7 适配：添加强制类型转换
            editText = (EditText) itemView.findViewById(R.id.edit_text);
            checkBoxAllow = (CheckBox) itemView.findViewById(R.id.checkbox_allow);
            checkBoxEnable = (CheckBox) itemView.findViewById(R.id.checkbox_enable);
            buttonConfirm = (Button) itemView.findViewById(R.id.button_confirm);
        }
    }
}

