package cc.winboll.studio.positions.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/04 13:38:13
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.models.PostionModel;
import cc.winboll.studio.positions.utils.PostionUtils;
import cc.winboll.studio.positions.views.LeftScrollView;
import java.util.ArrayList;

public class PostionModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "PostionModelAdapter";

    private static final int VIEW_TYPE_SIMPLE = 0;
    private static final int VIEW_TYPE_EDIT = 1;

    private Context context;
    private ArrayList<PostionModel> mPostionList;

    public PostionModelAdapter(Context context, ArrayList<PostionModel> postionList) {
        this.context = context;
        this.mPostionList = postionList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_SIMPLE) {
            View view = inflater.inflate(R.layout.view_position_simple, parent, false);
            return new SimpleViewHolder(parent, view);
        } else {
            View view = inflater.inflate(R.layout.view_position, parent, false);
            return new EditViewHolder(parent, view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final PostionModel model = mPostionList.get(position);
        if (holder instanceof SimpleViewHolder) {
            final SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            String szView = model.getComments().trim().equals("") ?"[NULL]": model.getComments();
            simpleViewHolder.tvComments.setText(szView);
            simpleViewHolder.scrollView.setOnActionListener(new LeftScrollView.OnActionListener(){

                    @Override
                    public void onUp() {
                        ArrayList<PostionModel> list = mPostionList;
                        if (position > 0) {
                            ToastUtils.show("onUp");
                            simpleViewHolder.scrollView.smoothScrollTo(0, 0);
//                            PhoneConnectRuleModel newBean = new PhoneConnectRuleModel();
//                            newBean.setRuleText(list.get(position).getRuleText());
//                            newBean.setIsAllowConnection(list.get(position).isAllowConnection());
//                            newBean.setIsEnable(list.get(position).isEnable());
//                            newBean.setIsSimpleView(list.get(position).isSimpleView());
                            list.add(position - 1, list.get(position));
                            list.remove(position + 1);
                            PostionUtils.getInstance(context).savePostionModelList();
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onDown() {
                        ArrayList<PostionModel> list = mPostionList;
                        if (position < list.size() - 1) {
                            ToastUtils.show("onDown");
                            simpleViewHolder.scrollView.smoothScrollTo(0, 0);
//                            PhoneConnectRuleModel newBean = new PhoneConnectRuleModel();
//                            newBean.setRuleText(list.get(position).getRuleText());
//                            newBean.setIsAllowConnection(list.get(position).isAllowConnection());
//                            newBean.setIsEnable(list.get(position).isEnable());
//                            newBean.setIsSimpleView(list.get(position).isSimpleView());
                            list.add(position + 2, list.get(position));
                            list.remove(position);
                            PostionUtils.getInstance(context).savePostionModelList();
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onEdit() {
                        simpleViewHolder.scrollView.smoothScrollTo(0, 0);
                        model.setIsSimpleView(false);
                        notifyDataSetChanged();
                        //notifyItemChanged(position);
                    }

                    @Override
                    public void onDelete() {
                        YesNoAlertDialog.show(simpleViewHolder.scrollView.getContext(), "删除确认", "是否删除该通话规则？", new YesNoAlertDialog.OnDialogResultListener(){

                                @Override
                                public void onYes() {
                                    simpleViewHolder.scrollView.smoothScrollTo(0, 0);
                                    model.setIsSimpleView(true);
                                    ArrayList<PostionModel> list = mPostionList;
                                    list.remove(position);
                                    PostionUtils.getInstance(context).savePostionModelList();
                                    notifyDataSetChanged();
                                    //notifyItemChanged(position);
                                }

                                @Override
                                public void onNo() {
                                }
                            });

                    }
                });
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
            editViewHolder.etComments.setText(model.getComments());
            editViewHolder.swEnable.setChecked(model.isEnable());
            editViewHolder.btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.setComments(editViewHolder.etComments.getText().toString());
                        model.setIsEnable(editViewHolder.swEnable.isChecked());
                        model.setIsSimpleView(true);
                        PostionUtils.getInstance(context).savePostionModelList();
                        notifyItemChanged(position);
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return mPostionList.size();
    }

    @Override
    public int getItemViewType(int position) {
        PostionModel model = mPostionList.get(position);
        // 这里可以根据模型的状态来决定视图类型，简单起见，假设点击按钮后进入编辑视图
        return model.isSimpleView() ? VIEW_TYPE_SIMPLE : VIEW_TYPE_EDIT;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private final LeftScrollView scrollView;
        private final TextView tvComments;


        public SimpleViewHolder(@NonNull ViewGroup parent, @NonNull View itemView) {
            super(itemView);
            //LinearLayout linearLayout = itemView.findViewById(R.id.linearLayout);
            scrollView = itemView.findViewById(R.id.scrollView);
            //tvRuleText = itemView.findViewById(R.id.ruletext_tv);
            tvComments = new TextView(itemView.getContext());
            
            //tvComments.setBackgroundColor(Color.GRAY);
            //LogUtils.d(TAG, String.format("linearLayout.getMeasuredWidth() %d", linearLayout.getMeasuredWidth()));
            LogUtils.d(TAG, String.format("parent.getMeasuredWidth() %d", parent.getMeasuredWidth()));
            scrollView.setContentWidth(parent.getMeasuredWidth());
            //scrollView.setContentWidth(600);
            scrollView.addContentLayout(tvComments);
        }

    }

    static class EditViewHolder extends RecyclerView.ViewHolder {
        EditText etComments;
        Switch swEnable;
        Button btnMoveCarema;
        Button btnConfirm;

        public EditViewHolder(@NonNull ViewGroup parent, @NonNull View itemView) {
            super(itemView);
            etComments = itemView.findViewById(R.id.comments_et);
            swEnable = itemView.findViewById(R.id.enable_sw);
            btnMoveCarema = itemView.findViewById(R.id.movecarema_btn);
            btnConfirm = itemView.findViewById(R.id.confirm_btn);
        }
    }
}

