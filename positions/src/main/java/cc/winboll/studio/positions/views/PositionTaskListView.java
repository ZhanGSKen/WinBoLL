package cc.winboll.studio.positions.views;

/**
 * @Describe 位置任务列表视图（适配MainService唯一数据源+同步任务状态+支持简单/编辑模式）
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025-09-30 08:09:00
 * @EditTime 2026-03-31 19:00:00
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.models.PositionTaskModel;
import cc.winboll.studio.positions.services.MainService;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PositionTaskListView extends LinearLayout {

    // 视图模式常量
    public static final int VIEW_MODE_SIMPLE = 1;
    public static final int VIEW_MODE_EDIT = 2;

    private static final String TAG = "PositionTaskListView";

    // 核心成员变量
    private String mBindPositionId;
    private MainService mMainService;
    private int mCurrentViewMode;
    private TaskListAdapter mTaskAdapter;
    private RecyclerView mRvTasks;

    // 任务修改回调接口
    public interface OnTaskUpdatedListener {
        void onTaskUpdated(String positionId, ArrayList<PositionTaskModel> updatedTasks);
    }

    private OnTaskUpdatedListener mOnTaskUpdatedListener;

    // ---------------------- 构造函数 ----------------------
    public PositionTaskListView(Context context) {
        super(context);
        LogUtils.d(TAG, "PositionTaskListView 构造函数 1 参数: context=" + context);
        initView(context);
    }

    public PositionTaskListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LogUtils.d(TAG, "PositionTaskListView 构造函数 2 参数: context=" + context + ", attrs=" + attrs);
        initView(context);
    }

    public PositionTaskListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtils.d(TAG, "PositionTaskListView 构造函数 3 参数: context=" + context + ", attrs=" + attrs + ", defStyleAttr=" + defStyleAttr);
        initView(context);
    }

    // ---------------------- 初始化视图 ----------------------
    private void initView(Context context) {
        LogUtils.d(TAG, "initView 开始执行 参数: context=" + context);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_position_task_list, this, true);

        mRvTasks = (RecyclerView) findViewById(R.id.rv_position_tasks);
        mRvTasks.setLayoutManager(new LinearLayoutManager(context));

        mTaskAdapter = new TaskListAdapter(new ArrayList<PositionTaskModel>());
        mRvTasks.setAdapter(mTaskAdapter);

        mCurrentViewMode = VIEW_MODE_SIMPLE;
        LogUtils.d(TAG, "initView 执行完成，视图初始化完毕");
    }

    // ---------------------- 对外API ----------------------
    public void init(MainService mainService, String positionId) {
        LogUtils.d(TAG, "init 开始执行 参数: mainService=" + mainService + ", positionId=" + positionId);
        if (mainService == null) {
            LogUtils.e(TAG, "init 失败：MainService 为空");
            showToast("任务列表初始化失败：服务未就绪");
            return;
        }
        if (positionId == null || positionId.trim().isEmpty()) {
            LogUtils.e(TAG, "init 失败：positionId 为空");
            showToast("任务列表初始化失败：未关联位置");
            return;
        }

        mMainService = mainService;
        mBindPositionId = positionId;
        LogUtils.d(TAG, "init 绑定成功：positionId=" + positionId);

        syncTasksFromMainService();
        LogUtils.d(TAG, "init 执行完成");
    }

    public void syncTasksFromMainService() {
        LogUtils.d(TAG, "syncTasksFromMainService 开始执行");
        if (mMainService == null || mBindPositionId == null || mBindPositionId.trim().isEmpty()) {
            LogUtils.w(TAG, "syncTasksFromMainService 失败：服务或位置ID无效");
            return;
        }

        try {
            ArrayList<PositionTaskModel> allServiceTasks = mMainService.getAllTasks();
            LogUtils.d(TAG, "syncTasksFromMainService 从服务获取任务总数: " + (allServiceTasks == null ? 0 : allServiceTasks.size()));

            ArrayList<PositionTaskModel> currentPosTasks = new ArrayList<>();
            if (allServiceTasks != null && !allServiceTasks.isEmpty()) {
                for (PositionTaskModel task : allServiceTasks) {
                    if (isTaskMatchedWithPosition(task)) {
                        currentPosTasks.add(task);
                    }
                }
            }
            LogUtils.d(TAG, "syncTasksFromMainService 筛选后当前位置任务数: " + currentPosTasks.size());

            mTaskAdapter.updateData(currentPosTasks);
            LogUtils.d(TAG, "syncTasksFromMainService 执行完成，Adapter已刷新");

        } catch (Exception e) {
            LogUtils.e(TAG, "syncTasksFromMainService 异常: " + e.getMessage(), e);
            showToast("任务同步失败，请重试");
        }
    }

    public void setViewStatus(int viewMode) {
        LogUtils.d(TAG, "setViewStatus 参数: viewMode=" + viewMode);
        if (viewMode != VIEW_MODE_SIMPLE && viewMode != VIEW_MODE_EDIT) {
            LogUtils.w(TAG, "setViewStatus 无效模式");
            return;
        }
        mCurrentViewMode = viewMode;
        mTaskAdapter.notifyDataSetChanged();
        LogUtils.d(TAG, "setViewStatus 切换完成: " + (viewMode == VIEW_MODE_SIMPLE ? "简单模式" : "编辑模式"));
    }

    public void setOnTaskUpdatedListener(OnTaskUpdatedListener listener) {
        LogUtils.d(TAG, "setOnTaskUpdatedListener 参数: listener=" + listener);
        mOnTaskUpdatedListener = listener;
    }

    public ArrayList<PositionTaskModel> getCurrentPosTasks() {
        LogUtils.d(TAG, "getCurrentPosTasks 被调用");
        return new ArrayList<>(mTaskAdapter.getAdapterData());
    }

    public void clearData() {
        LogUtils.d(TAG, "clearData 开始执行");
        mTaskAdapter.updateData(new ArrayList<PositionTaskModel>());
        mMainService = null;
        mBindPositionId = null;
        mCurrentViewMode = VIEW_MODE_SIMPLE;
        LogUtils.d(TAG, "clearData 执行完成，数据已清空");
    }

    public void triggerTaskSync() {
        LogUtils.d(TAG, "triggerTaskSync 主动触发同步");
        syncTasksFromMainService();
        if (mOnTaskUpdatedListener != null && mBindPositionId != null) {
            mOnTaskUpdatedListener.onTaskUpdated(mBindPositionId, getCurrentPosTasks());
        }
    }

    // ---------------------- 内部工具 ----------------------
    private boolean isTaskMatchedWithPosition(PositionTaskModel task) {
        if (task == null || mBindPositionId == null) {
            return false;
        }
        return mBindPositionId.equals(task.getPositionId());
    }

    private void showToast(String content) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }

    private String genSelectedTimeText(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    // ---------------------- 外部新增任务 ----------------------
    public void addNewTask(PositionTaskModel newTask) {
        LogUtils.d(TAG, "addNewTask 参数: newTask=" + newTask);
        if (mMainService == null) {
            LogUtils.e(TAG, "addNewTask 失败：MainService为空");
            showToast("新增任务失败：服务未就绪");
            return;
        }
        if (newTask == null) {
            LogUtils.e(TAG, "addNewTask 失败：任务为空");
            showToast("新增任务失败：任务数据为空");
            return;
        }
        if (mBindPositionId == null || mBindPositionId.trim().isEmpty()) {
            LogUtils.e(TAG, "addNewTask 失败：未绑定位置");
            showToast("新增任务失败：未关联位置");
            return;
        }

        try {
            newTask.setPositionId(mBindPositionId);
            mMainService.addTask(newTask);
            LogUtils.d(TAG, "addNewTask 服务添加成功 taskId=" + newTask.getTaskId());

            syncTasksFromMainService();
            if (mOnTaskUpdatedListener != null) {
                mOnTaskUpdatedListener.onTaskUpdated(mBindPositionId, getCurrentPosTasks());
            }
            showToast("新增任务成功");

        } catch (Exception e) {
            LogUtils.e(TAG, "addNewTask 异常: " + e.getMessage(), e);
            showToast("新增失败，请重试");
            syncTasksFromMainService();
        }
    }

    // ---------------------- Adapter ----------------------
    private class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

        private List<PositionTaskModel> mAdapterData;

        public TaskListAdapter(List<PositionTaskModel> data) {
            LogUtils.d(TAG, "TaskListAdapter 构造 参数 data.size=" + data.size());
            mAdapterData = new ArrayList<>(data);
        }

        public void updateData(List<PositionTaskModel> newData) {
            LogUtils.d(TAG, "updateData 参数 newData.size=" + (newData == null ? 0 : newData.size()));
            if (newData == null) {
                mAdapterData.clear();
            } else {
                mAdapterData = new ArrayList<>(newData);
            }
            notifyDataSetChanged();
        }

        public List<PositionTaskModel> getAdapterData() {
            return new ArrayList<>(mAdapterData);
        }

        @Override
        public int getItemCount() {
            return mAdapterData.isEmpty() ? 1 : mAdapterData.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mAdapterData.isEmpty()) {
                return 0;
            } else {
                return mCurrentViewMode;
            }
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            if (viewType == 0) {
                View emptyView = inflater.inflate(R.layout.item_task_empty, parent, false);
                return new EmptyViewHolder(emptyView);
            } else if (viewType == VIEW_MODE_SIMPLE) {
                View simpleView = inflater.inflate(R.layout.item_position_task_simple, parent, false);
                return new SimpleTaskViewHolder(simpleView);
            } else {
                View editView = inflater.inflate(R.layout.item_task_content, parent, false);
                return new TaskContentViewHolder(editView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            if (holder instanceof EmptyViewHolder) {
                EmptyViewHolder emptyHolder = (EmptyViewHolder) holder;
                TextView tv = emptyHolder.itemView.findViewById(R.id.tv_task_empty_tip);
                tv.setText(mCurrentViewMode == VIEW_MODE_EDIT
						   ? "暂无任务，点击\"添加新任务\"创建"
						   : "暂无启用的任务");
                return;
            }

            if (position >= mAdapterData.size()) {
                LogUtils.w(TAG, "onBindViewHolder 越界 position=" + position);
                return;
            }

            final PositionTaskModel task = mAdapterData.get(position);
            if (task == null) {
                LogUtils.w(TAG, "onBindViewHolder task为空 position=" + position);
                return;
            }

            if (holder instanceof SimpleTaskViewHolder) {
                SimpleTaskViewHolder h = (SimpleTaskViewHolder) holder;
                String desc = task.getTaskDescription() == null ? "未设置描述" : task.getTaskDescription();
                h.tvSimpleTaskDesc.setText(String.format("任务：%s", desc));
                h.tvStartTime.setText(genSelectedTimeText(task.getStartTime()));
                String cond = task.isGreaterThan() ? "大于(◎)" : "小于(•)";
                h.tvSimpleDistanceCond.setText(String.format("条件：距离 %s %d 米", cond, task.getDiscussDistance()));
                h.tvSimpleIsEnable.setText(task.isEnable() ? "状态：已启用" : "状态：已禁用");
                h.tvSimpleIsEnable.setTextColor(task.isEnable()
												? getContext().getResources().getColor(R.color.colorEnableGreen)
												: getContext().getResources().getColor(R.color.colorGrayText));
                h.vBingoDot.setVisibility(task.isBingo() ? View.VISIBLE : View.GONE);

            } else if (holder instanceof TaskContentViewHolder) {
                TaskContentViewHolder h = (TaskContentViewHolder) holder;
                bindEditModeTask(h, task, position);
            }
        }

        private void bindEditModeTask(final TaskContentViewHolder holder, final PositionTaskModel task, final int position) {
            LogUtils.d(TAG, "bindEditModeTask position=" + position + " taskId=" + task.getTaskId());

            String desc = task.getTaskDescription() == null ? "未设置描述" : task.getTaskDescription();
            holder.tvTaskDesc.setText(String.format("任务：%s", desc));
            String cond = task.isGreaterThan() ? "大于(◎)" : "小于(•)";
            holder.tvTaskDistance.setText(String.format("条件：%s %d 米", cond, task.getDiscussDistance()));
            holder.tvStartTime.setText(genSelectedTimeText(task.getStartTime()));

            holder.cbTaskEnable.setOnCheckedChangeListener(null);
            holder.cbTaskEnable.setChecked(task.isEnable());
            holder.cbTaskEnable.setEnabled(mCurrentViewMode == VIEW_MODE_EDIT);

            holder.btnEditTask.setVisibility(View.VISIBLE);
            holder.btnDeleteTask.setVisibility(View.VISIBLE);

            holder.btnDeleteTask.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						YesNoAlertDialog.show(getContext(), "删除任务提示", "是否删除此项位置任务？", new YesNoAlertDialog.OnDialogResultListener(){
								@Override
								public void onNo() {
								}

								@Override
								public void onYes() {
									LogUtils.d(TAG, "删除按钮点击 position=" + position + " taskId=" + task.getTaskId());
									if (mMainService == null) {
										showToast("删除失败：服务未就绪");
										LogUtils.e(TAG, "删除失败：MainService为空");
										return;
									}
									try {
										mMainService.deleteTask(task.getTaskId());
										LogUtils.d(TAG, "服务删除任务成功 taskId=" + task.getTaskId());
										notifyItemRemoved(position);
										notifyItemRangeChanged(position, mAdapterData.size());

										if (mOnTaskUpdatedListener != null && mBindPositionId != null) {
											mOnTaskUpdatedListener.onTaskUpdated(mBindPositionId, new ArrayList<>(mAdapterData));
										}
										showToast("任务已删除");

									} catch (Exception e) {
										LogUtils.e(TAG, "删除异常: " + e.getMessage(), e);
										showToast("删除失败，请重试");
										syncTasksFromMainService();
									}
								}

							});
					}
				});

            holder.btnEditTask.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d(TAG, "编辑按钮点击 position=" + position + " taskId=" + task.getTaskId());
						if (mMainService == null) {
							showToast("编辑失败：服务未就绪");
							LogUtils.e(TAG, "编辑失败：MainService为空");
							return;
						}
						showEditTaskDialog(task, position);
					}
				});

            holder.cbTaskEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
						LogUtils.d(TAG, "开关状态变更 taskId=" + task.getTaskId() + " isChecked=" + isChecked);
						if (mMainService == null) {
							showToast("状态修改失败：服务未就绪");
							buttonView.setChecked(!isChecked);
							return;
						}
						try {
							task.setIsEnable(isChecked);
							mMainService.updateTaskStatus(task);
							mRvTasks.post(new Runnable() {
									@Override
									public void run() {
										notifyItemChanged(position);
										if (mOnTaskUpdatedListener != null && mBindPositionId != null) {
											mOnTaskUpdatedListener.onTaskUpdated(mBindPositionId, new ArrayList<>(mAdapterData));
										}
									}
								});
						} catch (Exception e) {
							LogUtils.e(TAG, "开关变更异常: " + e.getMessage(), e);
							buttonView.setChecked(!isChecked);
							task.setIsEnable(!isChecked);
							syncTasksFromMainService();
						}
					}
				});
        }

        private void showEditTaskDialog(final PositionTaskModel task, final int position) {
            LogUtils.d(TAG, "showEditTaskDialog position=" + position + " taskId=" + task.getTaskId());
            final Context context = getContext();
            if (context == null) {
                LogUtils.w(TAG, "showEditTaskDialog 上下文为空");
                return;
            }

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null);
            final EditText etEditDesc = dialogView.findViewById(R.id.et_edit_task_desc);
            final RadioGroup rgDistanceCondition = dialogView.findViewById(R.id.rg_distance_condition);
            final EditText etEditDistance = dialogView.findViewById(R.id.et_edit_distance);
            Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
            Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);
            final Button btnSelectTime = dialogView.findViewById(R.id.btn_select_time);
            final TextView tvSelectedTime = dialogView.findViewById(R.id.tv_selected_time);

            tvSelectedTime.setText(genSelectedTimeText(task.getStartTime()));

            Calendar initCal = Calendar.getInstance();
            initCal.setTimeInMillis(task.getStartTime());
            int y = initCal.get(Calendar.YEAR);
            int M = initCal.get(Calendar.MONTH) + 1;
            int d = initCal.get(Calendar.DAY_OF_MONTH);
            int h = initCal.get(Calendar.HOUR_OF_DAY);
            int m = initCal.get(Calendar.MINUTE);

            final DateTimePickerPopup dateTimePopup = new DateTimePickerPopup.Builder(context)
				.setDateTimeRange(2020, 2030, 1, 12, 1, 31, 0, 23, 0, 59)
				.setDefaultDateTime(y, M, d, h, m)
				.setOnDateTimeSelectedListener(new DateTimePickerPopup.OnDateTimeSelectedListener() {
					@Override
					public void onDateTimeSelected(int year, int month, int day, int hour, int minute) {
						Calendar cal = Calendar.getInstance();
						cal.set(year, month - 1, day, hour, minute, 0);
						cal.set(Calendar.MILLISECOND, 0);
						long time = cal.getTimeInMillis();
						tvSelectedTime.setText(genSelectedTimeText(time));
						task.setStartTime(time);
					}

					@Override
					public void onCancel() {}
				})
				.build();

            btnSelectTime.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dateTimePopup.showAsDropDown(btnSelectTime);
					}
				});

            etEditDesc.setText(task.getTaskDescription() == null ? "" : task.getTaskDescription());
            etEditDesc.setSelection(etEditDesc.getText().length());

            if (task.isGreaterThan()) {
                rgDistanceCondition.check(R.id.rb_greater_than);
            } else {
                rgDistanceCondition.check(R.id.rb_less_than);
            }
            etEditDistance.setText(String.valueOf(task.getDiscussDistance()));

            final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
				.setView(dialogView)
				.setCancelable(false)
				.create();
            dialog.show();

            final OnDateSetListener listener = new OnDateSetListener() {
                @Override
                public void onDateSet(TimePickerDialog timePickerDialog, long p) {}
            };

            btnCancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

            btnSave.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String newDesc = etEditDesc.getText().toString().trim();
						String distStr = etEditDistance.getText().toString().trim();

						if (distStr.isEmpty()) {
							showToast("请输入有效距离");
							etEditDistance.requestFocus();
							return;
						}

						int newDist;
						try {
							newDist = Integer.parseInt(distStr);
							if (newDist < 1) {
								showToast("距离不能小于1米");
								etEditDistance.requestFocus();
								return;
							}
						} catch (NumberFormatException e) {
							showToast("距离请输入数字");
							etEditDistance.requestFocus();
							return;
						}

						task.setTaskDescription(newDesc);
						task.setDiscussDistance(newDist);
						boolean isGreater = rgDistanceCondition.getCheckedRadioButtonId() == R.id.rb_greater_than;
						task.setIsGreaterThan(isGreater);
						task.setPositionId(mBindPositionId);

						try {
							mMainService.updateTask(task);
							LogUtils.d(TAG, "保存修改成功 taskId=" + task.getTaskId());
							mAdapterData.set(position, task);
							mRvTasks.post(new Runnable() {
									@Override
									public void run() {
										notifyItemChanged(position);
										if (mOnTaskUpdatedListener != null && mBindPositionId != null) {
											mOnTaskUpdatedListener.onTaskUpdated(mBindPositionId, new ArrayList<>(mAdapterData));
										}
									}
								});
							dialog.dismiss();
							showToast("任务已更新");
						} catch (Exception e) {
							LogUtils.e(TAG, "保存异常: " + e.getMessage(), e);
							showToast("保存失败，请重试");
							syncTasksFromMainService();
						}
					}
				});
        }

        // ---------------------- ViewHolder ----------------------
        public abstract class TaskViewHolder extends RecyclerView.ViewHolder {
            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        public class EmptyViewHolder extends TaskViewHolder {
            public EmptyViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        public class SimpleTaskViewHolder extends TaskViewHolder {
            TextView tvSimpleTaskDesc;
            TextView tvSimpleDistanceCond;
            TextView tvStartTime;
            TextView tvSimpleIsEnable;
            View vBingoDot;

            public SimpleTaskViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSimpleTaskDesc = itemView.findViewById(R.id.tv_simple_task_desc);
                tvSimpleDistanceCond = itemView.findViewById(R.id.tv_simple_distance_cond);
                tvStartTime = itemView.findViewById(R.id.tv_starttime);
                tvSimpleIsEnable = itemView.findViewById(R.id.tv_simple_is_enable);
                vBingoDot = itemView.findViewById(R.id.v_bingo_dot);
            }
        }

        public class TaskContentViewHolder extends TaskViewHolder {
            TextView tvTaskDesc;
            TextView tvTaskDistance;
            TextView tvStartTime;
            CompoundButton cbTaskEnable;
            Button btnEditTask;
            Button btnDeleteTask;

            public TaskContentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTaskDesc = itemView.findViewById(R.id.tv_task_desc);
                tvTaskDistance = itemView.findViewById(R.id.tv_task_distance);
                tvStartTime = itemView.findViewById(R.id.tv_starttime);
                cbTaskEnable = itemView.findViewById(R.id.cb_task_enable);
                btnEditTask = itemView.findViewById(R.id.btn_edit_task);
                btnDeleteTask = itemView.findViewById(R.id.btn_delete_task);
            }
        }
    }
}

