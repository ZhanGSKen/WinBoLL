package cc.winboll.studio.positions.adapters;

/**
 * @Describe 位置数据适配器（修复视图复用资源加载，支持滚动后重新绑定数据，Java 7语法适配）
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025-09-29 20:25:00
 * @EditTime 2026-03-31 23:14:55
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.models.PositionModel;
import cc.winboll.studio.positions.models.PositionTaskModel;
import cc.winboll.studio.positions.services.MainService;
import cc.winboll.studio.positions.views.PositionTaskListView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class PositionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
implements MainService.TaskUpdateListener {

    public static final String TAG = "PositionAdapter";

    // 视图类型常量
    private static final int VIEW_TYPE_SIMPLE = 0;
    private static final int VIEW_TYPE_EDIT = 1;

    // 默认配置常量
    private static final String DEFAULT_MEMO = "无备注";
    private static final String DEFAULT_TASK_DESC = "新任务";
    private static final int DEFAULT_TASK_DISTANCE = 50;
    private static final String DISTANCE_FORMAT = "实时距离：%.1f 米";
    private static final String DISTANCE_DISABLED = "实时距离：未启用";
    private static final String DISTANCE_ERROR = "实时距离：计算失败";

    // 核心成员变量
    private final Context mContext;
    private final ArrayList<PositionModel> mCachedPositionList;
    private final WeakReference<MainService> mMainServiceRef;
    private final ConcurrentHashMap<String, PositionTaskListView> mSimpleTaskViewMap;
    private final ConcurrentHashMap<String, PositionTaskListView> mEditTaskViewMap;
    private final ConcurrentHashMap<String, TextView> mPosDistanceViewMap;

    // 回调接口
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnSavePositionClickListener {
        void onSavePositionClick(int position, PositionModel updatedPos);
    }

    private OnDeleteClickListener mOnDeleteListener;
    private OnSavePositionClickListener mOnSavePosListener;

    // =========================================================================
    // 构造函数
    // =========================================================================
    public PositionAdapter(Context context, ArrayList<PositionModel> cachedPositionList, MainService mainService) {
        LogUtils.d(TAG, "PositionAdapter 构造函数开始，context=" + context
				   + "，cachedPositionList=" + (cachedPositionList != null ? cachedPositionList.size() : 0)
				   + "，mainService=" + mainService);

        this.mContext = context;
        this.mCachedPositionList = (cachedPositionList != null) ? cachedPositionList : new ArrayList<PositionModel>();
        this.mMainServiceRef = new WeakReference<MainService>(mainService);
        this.mSimpleTaskViewMap = new ConcurrentHashMap<String, PositionTaskListView>();
        this.mEditTaskViewMap = new ConcurrentHashMap<String, PositionTaskListView>();
        this.mPosDistanceViewMap = new ConcurrentHashMap<String, TextView>();

        if (mainService != null) {
            mainService.registerTaskUpdateListener(this);
            LogUtils.d(TAG, "已注册 MainService 任务监听");
        } else {
            LogUtils.w(TAG, "构造函数：MainService 为空，无法初始化任务视图");
        }

        LogUtils.d(TAG, "PositionAdapter 初始化完成，位置数量=" + mCachedPositionList.size());
    }

    // =========================================================================
    // RecyclerView 核心方法
    // =========================================================================
    @Override
    public int getItemViewType(int position) {
        PositionModel posModel = getPositionByIndex(position);
        return (posModel != null && posModel.isSimpleView()) ? VIEW_TYPE_SIMPLE : VIEW_TYPE_EDIT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtils.d(TAG, "onCreateViewHolder viewType=" + viewType);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == VIEW_TYPE_SIMPLE) {
            View simpleView = inflater.inflate(R.layout.item_position_simple, parent, false);
            return new SimpleViewHolder(simpleView);
        } else {
            View editView = inflater.inflate(R.layout.item_position_edit, parent, false);
            return new EditViewHolder(editView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LogUtils.d(TAG, "onBindViewHolder position=" + position);
        PositionModel posModel = getPositionByIndex(position);
        if (posModel == null) {
            LogUtils.w(TAG, "onBindViewHolder：位置模型为空，索引=" + position);
            return;
        }
        final String posId = posModel.getPositionId();
        MainService mainService = mMainServiceRef.get();

        if (holder instanceof SimpleViewHolder) {
            LogUtils.d(TAG, "绑定 SimpleViewHolder，posId=" + posId);
            SimpleViewHolder simpleHolder = (SimpleViewHolder) holder;
            bindSimplePositionData(simpleHolder, posModel);
            initAndBindSimpleTaskView(simpleHolder.ptlvSimpleTasks, posId, mainService);
            mSimpleTaskViewMap.put(posId, simpleHolder.ptlvSimpleTasks);
            mPosDistanceViewMap.put(posId, simpleHolder.tvSimpleDistance);

        } else if (holder instanceof EditViewHolder) {
            LogUtils.d(TAG, "绑定 EditViewHolder，posId=" + posId);
            EditViewHolder editHolder = (EditViewHolder) holder;
            bindEditPositionData(editHolder, posModel, position);
            initAndBindEditTaskView(editHolder.ptlvEditTasks, posId, mainService, editHolder.btnAddTask);
            mEditTaskViewMap.put(posId, editHolder.ptlvEditTasks);
            mPosDistanceViewMap.put(posId, editHolder.tvEditDistance);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        PositionModel posModel = getPositionByIndex(holder.getAdapterPosition());
        if (posModel == null || TextUtils.isEmpty(posModel.getPositionId())) {
            return;
        }
        String posId = posModel.getPositionId();

        if (mPosDistanceViewMap.containsKey(posId)) {
            TextView distanceView = mPosDistanceViewMap.get(posId);
            if (distanceView == null || !distanceView.isAttachedToWindow()) {
                mPosDistanceViewMap.remove(posId);
                LogUtils.d(TAG, "视图脱离：移除无效距离控件缓存 posId=" + posId);
            }
        }

        if (holder instanceof SimpleViewHolder && mSimpleTaskViewMap.containsKey(posId)) {
            PositionTaskListView taskView = mSimpleTaskViewMap.get(posId);
            if (taskView == null || !taskView.isAttachedToWindow()) {
                mSimpleTaskViewMap.remove(posId);
                LogUtils.d(TAG, "视图脱离：移除无效简单任务视图缓存 posId=" + posId);
            }
        }

        if (holder instanceof EditViewHolder && mEditTaskViewMap.containsKey(posId)) {
            PositionTaskListView taskView = mEditTaskViewMap.get(posId);
            if (taskView == null || !taskView.isAttachedToWindow()) {
                mEditTaskViewMap.remove(posId);
                LogUtils.d(TAG, "视图脱离：移除无效编辑任务视图缓存 posId=" + posId);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCachedPositionList.size();
    }

    // =========================================================================
    // 位置数据绑定
    // =========================================================================
    private void bindSimplePositionData(SimpleViewHolder holder, final PositionModel posModel) {
        LogUtils.d(TAG, "bindSimplePositionData posId=" + posModel.getPositionId());
        holder.tvSimpleLon.setText(String.format("经度：%.6f", posModel.getLongitude()));
        holder.tvSimpleLat.setText(String.format("纬度：%.6f", posModel.getLatitude()));

        String memo = posModel.getMemo();
        holder.tvSimpleMemo.setText("备注：" + (TextUtils.isEmpty(memo) ? DEFAULT_MEMO : memo));
        updateDistanceDisplay(holder.tvSimpleDistance, posModel);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					posModel.setIsSimpleView(false);
					notifyItemChanged(getPositionIndexById(posModel.getPositionId()));
					LogUtils.d(TAG, "简单视图点击：切换编辑模式 posId=" + posModel.getPositionId());
				}
			});
    }

    private void bindEditPositionData(final EditViewHolder holder, final PositionModel posModel, final int position) {
        LogUtils.d(TAG, "bindEditPositionData posId=" + posModel.getPositionId() + " position=" + position);
        final String posId = posModel.getPositionId();

        holder.tvEditLon.setText(String.format("经度：%.6f", posModel.getLongitude()));
        holder.tvEditLat.setText(String.format("纬度：%.6f", posModel.getLatitude()));

        String memo = posModel.getMemo();
        if (!TextUtils.isEmpty(memo)) {
            holder.etEditMemo.setText(memo);
            holder.etEditMemo.setSelection(memo.length());
        } else {
            holder.etEditMemo.setText("");
        }

        updateDistanceDisplay(holder.tvEditDistance, posModel);

        holder.rgDistanceSwitch.check(posModel.isEnableRealPositionDistance()
									  ? R.id.rb_distance_enable
									  : R.id.rb_distance_disable);

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					posModel.setIsSimpleView(true);
					notifyItemChanged(position);
					hideSoftKeyboard(v);
					LogUtils.d(TAG, "取消编辑：切换简单视图 posId=" + posId);
				}
			});

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnDeleteListener != null) {
						mOnDeleteListener.onDeleteClick(position);
					}
					hideSoftKeyboard(v);
					LogUtils.d(TAG, "触发删除位置 posId=" + posId);
				}
			});

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String newMemo = holder.etEditMemo.getText().toString().trim();
					boolean isDistanceEnable = (holder.rgDistanceSwitch.getCheckedRadioButtonId() == R.id.rb_distance_enable);

					PositionModel updatedPos = new PositionModel();
					updatedPos.setPositionId(posId);
					updatedPos.setLongitude(posModel.getLongitude());
					updatedPos.setLatitude(posModel.getLatitude());
					updatedPos.setMemo(newMemo);
					updatedPos.setIsEnableRealPositionDistance(isDistanceEnable);
					updatedPos.setIsSimpleView(true);

					if (mOnSavePosListener != null) {
						mOnSavePosListener.onSavePositionClick(position, updatedPos);
					}

					posModel.setMemo(newMemo);
					posModel.setIsEnableRealPositionDistance(isDistanceEnable);
					posModel.setIsSimpleView(true);
					notifyItemChanged(position);
					hideSoftKeyboard(v);

					LogUtils.d(TAG, "保存位置 posId=" + posId + " 新备注=" + newMemo + " 距离启用=" + isDistanceEnable);
				}
			});
    }

    // =========================================================================
    // PositionTaskListView 集成
    // =========================================================================
    private void initAndBindSimpleTaskView(PositionTaskListView taskView, String posId, MainService mainService) {
        LogUtils.d(TAG, "initAndBindSimpleTaskView posId=" + posId);
        if (taskView == null || TextUtils.isEmpty(posId) || mainService == null) {
            LogUtils.w(TAG, "初始化简单任务视图失败：参数无效");
            return;
        }
        taskView.init(mainService, posId);
        taskView.setViewStatus(PositionTaskListView.VIEW_MODE_SIMPLE);
        taskView.syncTasksFromMainService();

        taskView.setOnTaskUpdatedListener(new PositionTaskListView.OnTaskUpdatedListener() {
				@Override
				public void onTaskUpdated(String positionId, ArrayList updatedTasks) {
					LogUtils.d(TAG, "简单模式任务更新 posId=" + positionId + " 任务数=" + updatedTasks.size());
				}
			});
    }

    private void initAndBindEditTaskView(final PositionTaskListView taskView, final String posId,
                                         MainService mainService, Button btnAddTask) {
        LogUtils.d(TAG, "initAndBindEditTaskView posId=" + posId);
        if (taskView == null || TextUtils.isEmpty(posId) || mainService == null || btnAddTask == null) {
            LogUtils.w(TAG, "初始化编辑任务视图失败：参数无效");
            return;
        }
        taskView.init(mainService, posId);
        taskView.setViewStatus(PositionTaskListView.VIEW_MODE_EDIT);
        taskView.syncTasksFromMainService();

        btnAddTask.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PositionTaskModel newTask = new PositionTaskModel();
					newTask.setTaskId(PositionTaskModel.genTaskId());
					newTask.setPositionId(posId);
					newTask.setTaskDescription(DEFAULT_TASK_DESC);
					newTask.setDiscussDistance(DEFAULT_TASK_DISTANCE);
					newTask.setIsEnable(true);
					newTask.setIsBingo(false);

					taskView.addNewTask(newTask);
					hideSoftKeyboard(v);
					LogUtils.d(TAG, "新增任务 posId=" + posId + " taskId=" + newTask.getTaskId());
				}
			});

        taskView.setOnTaskUpdatedListener(new PositionTaskListView.OnTaskUpdatedListener() {
				@Override
				public void onTaskUpdated(String positionId, ArrayList updatedTasks) {
					LogUtils.d(TAG, "编辑模式任务更新 posId=" + positionId + " 任务数=" + updatedTasks.size());
				}
			});
    }

    // =========================================================================
    // 工具方法
    // =========================================================================
    private void updateDistanceDisplay(TextView distanceView, PositionModel posModel) {
        if (distanceView == null || posModel == null) {
            LogUtils.w(TAG, "updateDistanceDisplay：参数为空");
            return;
        }
        if (!posModel.isEnableRealPositionDistance()) {
            distanceView.setText(DISTANCE_DISABLED);
            distanceView.setTextColor(mContext.getResources().getColor(R.color.gray));
            return;
        }
        double distance = posModel.getRealPositionDistance();
        if (distance < 0) {
            distanceView.setText(DISTANCE_ERROR);
            distanceView.setTextColor(mContext.getResources().getColor(R.color.red));
            return;
        }
        distanceView.setText(String.format(DISTANCE_FORMAT, distance));
        if (distance <= 100) {
            distanceView.setTextColor(mContext.getResources().getColor(R.color.green));
        } else if (distance <= 500) {
            distanceView.setTextColor(mContext.getResources().getColor(R.color.yellow));
        } else {
            distanceView.setTextColor(mContext.getResources().getColor(R.color.red));
        }
    }

    private PositionModel getPositionByIndex(int index) {
        if (mCachedPositionList == null || index < 0 || index >= mCachedPositionList.size()) {
            LogUtils.w(TAG, "getPositionByIndex：无效索引 index=" + index);
            return null;
        }
        return mCachedPositionList.get(index);
    }

    private int getPositionIndexById(String positionId) {
        if (TextUtils.isEmpty(positionId) || mCachedPositionList == null || mCachedPositionList.isEmpty()) {
            LogUtils.w(TAG, "getPositionIndexById：参数无效");
            return -1;
        }
        for (int i = 0; i < mCachedPositionList.size(); i++) {
            PositionModel pos = mCachedPositionList.get(i);
            if (positionId.equals(pos.getPositionId())) {
                return i;
            }
        }
        LogUtils.w(TAG, "未找到位置ID=" + positionId);
        return -1;
    }

    public void updateSinglePositionDistance(String positionId) {
        LogUtils.d(TAG, "updateSinglePositionDistance posId=" + positionId);
        if (TextUtils.isEmpty(positionId) || mPosDistanceViewMap.isEmpty()) {
            return;
        }
        MainService mainService = getMainServiceWithRetry(2);
        if (mainService == null) {
            LogUtils.e(TAG, "无法获取 MainService");
            return;
        }
        PositionModel latestPos = null;
        try {
            ArrayList servicePosList = mainService.getPositionList();
            if (servicePosList != null && !servicePosList.isEmpty()) {
                Iterator iter = servicePosList.iterator();
                while (iter.hasNext()) {
                    PositionModel pos = (PositionModel) iter.next();
                    if (positionId.equals(pos.getPositionId())) {
                        latestPos = pos;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "获取位置数据异常", e);
            return;
        }
        if (latestPos == null) {
            LogUtils.w(TAG, "未找到位置 posId=" + positionId);
            return;
        }
        final TextView distanceView = mPosDistanceViewMap.get(positionId);
        if (distanceView != null && distanceView.isAttachedToWindow()) {
            final PositionModel finalLatestPos = latestPos;
            distanceView.post(new Runnable() {
					@Override
					public void run() {
						updateDistanceDisplay(distanceView, finalLatestPos);
					}
				});
        } else {
            mPosDistanceViewMap.remove(positionId);
        }
    }

    public void updateAllPositionData(ArrayList<PositionModel> newPosList) {
        LogUtils.d(TAG, "updateAllPositionData 新数据数量=" + (newPosList != null ? newPosList.size() : 0));
        if (newPosList == null) {
            return;
        }
        ArrayList<PositionModel> validPosList = new ArrayList<PositionModel>();
        for (PositionModel pos : newPosList) {
            if (TextUtils.isEmpty(pos.getPositionId())
				|| pos.getLongitude() < -180 || pos.getLongitude() > 180
				|| pos.getLatitude() < -90 || pos.getLatitude() > 90) {
                continue;
            }
            validPosList.add(pos);
        }
        ConcurrentHashMap<String, PositionModel> uniquePosMap = new ConcurrentHashMap<String, PositionModel>();
        for (PositionModel pos : validPosList) {
            uniquePosMap.put(pos.getPositionId(), pos);
        }
        ArrayList uniquePosList = new ArrayList(uniquePosMap.values());

        mCachedPositionList.clear();
        mCachedPositionList.addAll(uniquePosList);
        mPosDistanceViewMap.clear();
        mSimpleTaskViewMap.clear();
        mEditTaskViewMap.clear();
        notifyDataSetChanged();

        LogUtils.d(TAG, "全量更新完成，有效数量=" + uniquePosList.size());
    }

    private void hideSoftKeyboard(View view) {
        if (mContext == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private MainService getMainServiceWithRetry(int retryCount) {
        MainService mainService = mMainServiceRef.get();
        if (mainService != null) {
            return mainService;
        }
        for (int i = 0; i < retryCount; i++) {
            try {
                Thread.sleep(100);
                mainService = mMainServiceRef.get();
                if (mainService != null) {
                    LogUtils.d(TAG, "重试获取 MainService 成功，第" + (i + 1) + "次");
                    return mainService;
                }
            } catch (InterruptedException e) {
                LogUtils.e(TAG, "重试被中断", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
        LogUtils.e(TAG, "重试" + retryCount + "次仍未获取到 MainService");
        return null;
    }

    // =========================================================================
    // 任务更新监听
    // =========================================================================
    @Override
    public void onTaskUpdated() {
        LogUtils.d(TAG, "onTaskUpdated：收到服务任务更新");
        if (!mSimpleTaskViewMap.isEmpty()) {
            Iterator<ConcurrentHashMap.Entry<String, PositionTaskListView>> iter = mSimpleTaskViewMap.entrySet().iterator();
            while (iter.hasNext()) {
                ConcurrentHashMap.Entry<String, PositionTaskListView> entry = iter.next();
                PositionTaskListView taskView = entry.getValue();
                if (taskView != null && taskView.isAttachedToWindow()) {
                    taskView.syncTasksFromMainService();
                } else {
                    iter.remove();
                }
            }
        }
        if (!mEditTaskViewMap.isEmpty()) {
            Iterator<ConcurrentHashMap.Entry<String, PositionTaskListView>> iter = mEditTaskViewMap.entrySet().iterator();
            while (iter.hasNext()) {
                ConcurrentHashMap.Entry<String, PositionTaskListView> entry = iter.next();
                PositionTaskListView taskView = entry.getValue();
                if (taskView != null && taskView.isAttachedToWindow()) {
                    taskView.syncTasksFromMainService();
                } else {
                    iter.remove();
                }
            }
        }
    }

    // =========================================================================
    // 外部回调设置
    // =========================================================================
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        LogUtils.d(TAG, "setOnDeleteClickListener listener=" + listener);
        this.mOnDeleteListener = listener;
    }

    public void setOnSavePositionClickListener(OnSavePositionClickListener listener) {
        LogUtils.d(TAG, "setOnSavePositionClickListener listener=" + listener);
        this.mOnSavePosListener = listener;
    }

    // =========================================================================
    // 资源释放
    // =========================================================================
    public void release() {
        LogUtils.d(TAG, "release：开始释放 Adapter 资源");
        MainService mainService = mMainServiceRef.get();
        if (mainService != null) {
            mainService.unregisterTaskUpdateListener(this);
            LogUtils.d(TAG, "已反注册任务监听");
        }

        if (!mSimpleTaskViewMap.isEmpty()) {
            Iterator<ConcurrentHashMap.Entry<String, PositionTaskListView>> iter = mSimpleTaskViewMap.entrySet().iterator();
            while (iter.hasNext()) {
                PositionTaskListView taskView = iter.next().getValue();
                if (taskView != null) {
                    taskView.clearData();
                    taskView.setOnTaskUpdatedListener(null);
                }
                iter.remove();
            }
        }

        if (!mEditTaskViewMap.isEmpty()) {
            Iterator<ConcurrentHashMap.Entry<String, PositionTaskListView>> iter = mEditTaskViewMap.entrySet().iterator();
            while (iter.hasNext()) {
                PositionTaskListView taskView = iter.next().getValue();
                if (taskView != null) {
                    taskView.clearData();
                    taskView.setOnTaskUpdatedListener(null);
                }
                iter.remove();
            }
        }

        mPosDistanceViewMap.clear();
        if (mCachedPositionList != null) {
            mCachedPositionList.clear();
        }
        mOnDeleteListener = null;
        mOnSavePosListener = null;

        LogUtils.d(TAG, "release：资源释放完成");
    }

    // =========================================================================
    // ViewHolder
    // =========================================================================
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSimpleLon;
        TextView tvSimpleLat;
        TextView tvSimpleMemo;
        TextView tvSimpleDistance;
        PositionTaskListView ptlvSimpleTasks;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            tvSimpleLon = (TextView) itemView.findViewById(R.id.tv_simple_longitude);
            tvSimpleLat = (TextView) itemView.findViewById(R.id.tv_simple_latitude);
            tvSimpleMemo = (TextView) itemView.findViewById(R.id.tv_simple_memo);
            tvSimpleDistance = (TextView) itemView.findViewById(R.id.tv_simple_distance);
            ptlvSimpleTasks = (PositionTaskListView) itemView.findViewById(R.id.ptlv_simple_tasks);
        }
    }

    public static class EditViewHolder extends RecyclerView.ViewHolder {
        TextView tvEditLon;
        TextView tvEditLat;
        EditText etEditMemo;
        TextView tvEditDistance;
        RadioGroup rgDistanceSwitch;
        Button btnCancel;
        Button btnDelete;
        Button btnSave;
        Button btnAddTask;
        TextView tvTaskCount;
        PositionTaskListView ptlvEditTasks;

        public EditViewHolder(View itemView) {
            super(itemView);
            tvEditLon = (TextView) itemView.findViewById(R.id.tv_edit_longitude);
            tvEditLat = (TextView) itemView.findViewById(R.id.tv_edit_latitude);
            etEditMemo = (EditText) itemView.findViewById(R.id.et_edit_memo);
            tvEditDistance = (TextView) itemView.findViewById(R.id.tv_edit_distance);
            rgDistanceSwitch = (RadioGroup) itemView.findViewById(R.id.rg_distance_switch);
            btnCancel = (Button) itemView.findViewById(R.id.btn_edit_cancel);
            btnDelete = (Button) itemView.findViewById(R.id.btn_edit_delete);
            btnSave = (Button) itemView.findViewById(R.id.btn_edit_save);
            btnAddTask = (Button) itemView.findViewById(R.id.btn_add_task);
            tvTaskCount = (TextView) itemView.findViewById(R.id.tv_task_count);
            ptlvEditTasks = (PositionTaskListView) itemView.findViewById(R.id.ptlv_edit_tasks);
        }
    }

    // =========================================================================
    // 方法说明类（保留不动）
    // =========================================================================
    public static class PositionTaskListViewRequiredMethods {
    }
}

