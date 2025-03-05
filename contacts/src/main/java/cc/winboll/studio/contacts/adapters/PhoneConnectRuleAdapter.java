package cc.winboll.studio.contacts.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 17:27:41
 * @Describe PhoneConnectRuleAdapter
 */
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
import cc.winboll.studio.contacts.beans.PhoneConnectRuleModel;
import cc.winboll.studio.contacts.dun.Rules;
import java.util.ArrayList;
import java.util.List;
import android.widget.LinearLayout;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import cc.winboll.studio.contacts.views.LeftScrollView;

public class PhoneConnectRuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "PhoneConnectRuleAdapter";

    private static final int VIEW_TYPE_SIMPLE = 0;
    private static final int VIEW_TYPE_EDIT = 1;

    private Context context;
    private List<PhoneConnectRuleModel> ruleList;

    public PhoneConnectRuleAdapter(Context context, List<PhoneConnectRuleModel> ruleList) {
        this.context = context;
        this.ruleList = ruleList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_SIMPLE) {
            View view = inflater.inflate(R.layout.view_phone_connect_rule_simple, parent, false);
            return new SimpleViewHolder(parent, view);
        } else {
            View view = inflater.inflate(R.layout.view_phone_connect_rule, parent, false);
            return new EditViewHolder(parent, view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final PhoneConnectRuleModel model = ruleList.get(position);
        if (holder instanceof SimpleViewHolder) {
            final SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            simpleViewHolder.tvRuleText.setText(model.getRuleText());
//            simpleViewHolder.setOnActionListener(new SimpleViewHolder.OnActionListener(){
//
//                    @Override
//                    public void onEdit(int position) {
//                        model.setIsSimpleView(false);
//                        notifyItemChanged(position);
//                    }
//                    @Override
//                    public void onDelete(int position) {
//                        model.setIsSimpleView(false);
//                        ArrayList<PhoneConnectRuleModel> list = Rules.getInstance(context).getPhoneBlacRuleBeanList();
//                        list.remove(position);
//                        Rules.getInstance(context).saveRules();
//                        notifyItemChanged(position);
//                    }
//                });
//            simpleViewHolder.editButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        model.setIsSimpleView(false);
//                        notifyItemChanged(position);
//                    }
//                });
//            simpleViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        model.setIsSimpleView(false);
//                        ArrayList<PhoneConnectRuleModel> list = Rules.getInstance(context).getPhoneBlacRuleBeanList();
//                        list.remove(position);
//                        Rules.getInstance(context).saveRules();
//                        notifyItemChanged(position);
//                    }
//                });
//            // 触摸事件处理
//            simpleViewHolder.contentLayout.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        switch (event.getAction()) {
//                            case MotionEvent.ACTION_DOWN:
//                                simpleViewHolder.startX = event.getX();
//                                simpleViewHolder.isSwiping = true;
//                                break;
//                            case MotionEvent.ACTION_MOVE:
//                                if (simpleViewHolder.isSwiping) {
//                                    float deltaX = simpleViewHolder.startX - event.getX();
//                                    if (deltaX > 0) { // 左滑
//                                        float translationX = Math.max(-simpleViewHolder.actionLayout.getWidth(), -deltaX);
//                                        simpleViewHolder.contentLayout.setTranslationX(translationX);
//                                        simpleViewHolder.actionLayout.setVisibility(View.VISIBLE);
//                                    }
//                                }
//                                break;
//                            case MotionEvent.ACTION_UP:
//                                simpleViewHolder.isSwiping = false;
//                                if (simpleViewHolder.contentLayout.getTranslationX() < -simpleViewHolder.actionLayout.getWidth() / 2) {
//                                    // 保持按钮显示
//                                    simpleViewHolder.contentLayout.setTranslationX(-actionLayout.getWidth());
//                                } else {
//                                    // 恢复原状
//                                    simpleViewHolder.contentLayout.animate().translationX(0).setDuration(200).start();
//                                    simpleViewHolder.actionLayout.setVisibility(View.INVISIBLE);
//                                }
//                                break;
//                        }
//                        return true;
//                    }
//                });
        } else if (holder instanceof EditViewHolder) {
            final EditViewHolder editViewHolder = (EditViewHolder) holder;
            editViewHolder.editText.setText(model.getRuleText());
            editViewHolder.checkBoxAllow.setChecked(model.isAllowConnection());
            editViewHolder.checkBoxEnable.setChecked(model.isEnable());
            editViewHolder.buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.setRuleText(editViewHolder.editText.getText().toString());
                        model.setIsAllowConnection(editViewHolder.checkBoxAllow.isChecked());
                        model.setIsEnable(editViewHolder.checkBoxEnable.isChecked());
                        model.setIsSimpleView(true);
                        Rules.getInstance(context).saveRules();
                        notifyItemChanged(position);
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return ruleList.size();
    }

    @Override
    public int getItemViewType(int position) {
        PhoneConnectRuleModel model = ruleList.get(position);
        // 这里可以根据模型的状态来决定视图类型，简单起见，假设点击按钮后进入编辑视图
        return model.isSimpleView() ? VIEW_TYPE_SIMPLE : VIEW_TYPE_EDIT;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private final LeftScrollView scrollView;
        private final TextView tvRuleText;


        public SimpleViewHolder(@NonNull ViewGroup parent, @NonNull View itemView) {
            super(itemView);
            scrollView = itemView.findViewById(R.id.scrollView);
            //tvRuleText = itemView.findViewById(R.id.ruletext_tv);
            tvRuleText = new TextView(itemView.getContext());
            scrollView.setContentWidth(parent.getWidth());
            //scrollView.setContentWidth(600);
            scrollView.addContentLayout(tvRuleText);
        }
        
    }

    static class EditViewHolder extends RecyclerView.ViewHolder {
        EditText editText;
        CheckBox checkBoxAllow;
        CheckBox checkBoxEnable;
        Button buttonConfirm;

        public EditViewHolder(@NonNull ViewGroup parent, @NonNull View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.edit_text);
            checkBoxAllow = itemView.findViewById(R.id.checkbox_allow);
            checkBoxEnable = itemView.findViewById(R.id.checkbox_enable);
            buttonConfirm = itemView.findViewById(R.id.button_confirm);
        }
    }
}

