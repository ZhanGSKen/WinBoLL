package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 14:36:18
 * @Describe 日志视图类，继承 RelativeLayout 类。
 */
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LogView extends RelativeLayout {

    public static final String TAG = "LogView";

    public volatile boolean mIsHandling;
    public volatile boolean mIsAddNewLog;

    Context mContext;
    ScrollView mScrollView;
    TextView mTextView;
    CheckBox mSelectableCheckBox;
    CheckBox mSelectAllTAGCheckBox;
    TAGListAdapter mTAGListAdapter;
    LogViewThread mLogViewThread;
    LogViewHandler mLogViewHandler;
    Spinner mLogLevelSpinner;
    ArrayAdapter<CharSequence> mLogLevelSpinnerAdapter;
    // 标签列表
    RecyclerView recyclerView;

    public LogView(Context context) {
        super(context);
        initView(context);
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public LogView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public void start() {
        mLogViewThread = new LogViewThread(LogView.this);
        mLogViewThread.start();
        // 显示日志
        showAndScrollLogView();
    }

    public void scrollLogUp() {
        mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    // 日志显示结束
                    mLogViewHandler.setIsHandling(false);
                    // 检查是否添加了新日志
                    if (mLogViewHandler.isAddNewLog()) {
                        // 有新日志添加，先更改新日志标志
                        mLogViewHandler.setIsAddNewLog(false);
                        // 再次发送显示日志的显示
                        Message message = mLogViewHandler.obtainMessage(LogViewHandler.MSG_LOGVIEW_UPDATE);
                        mLogViewHandler.sendMessage(message);
                    }
                }
            });
    }

    void initView(Context context) {
        mContext = context;
        mLogViewHandler = new LogViewHandler();
        // 加载视图布局
        addView(inflate(mContext, cc.winboll.studio.libappbase.R.layout.view_log, null));
        // 初始化日志子控件视图
        //
        mScrollView = findViewById(cc.winboll.studio.libappbase.R.id.viewlogScrollViewLog);
        mTextView = findViewById(cc.winboll.studio.libappbase.R.id.viewlogTextViewLog);
        // 获取Log Level spinner实例
        mLogLevelSpinner = findViewById(cc.winboll.studio.libappbase.R.id.viewlogSpinner1);

        (findViewById(cc.winboll.studio.libappbase.R.id.viewlogButtonClean)).setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    LogUtils.cleanLog();
                    LogUtils.d(TAG, "Log is cleaned.");
                }
            });
        (findViewById(cc.winboll.studio.libappbase.R.id.viewlogButtonCopy)).setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(mContext.getPackageName(), LogUtils.loadLog()));
                    LogUtils.d(TAG, "Log is copied.");
                }
            });
        mSelectableCheckBox = findViewById(cc.winboll.studio.libappbase.R.id.viewlogCheckBoxSelectable);
        mSelectableCheckBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (mSelectableCheckBox.isChecked()) {
                        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                    } else {
                        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                    }
                }
            });

        // 设置日志级别列表
        ArrayList<String> adapterItems = new ArrayList<>();
        for (LogUtils.LOG_LEVEL e : LogUtils.LOG_LEVEL.values()) {
            adapterItems.add(e.name());
        }
        // 假设你有一个字符串数组作为选项列表
        //String[] options = {"Option 1", "Option 2", "Option 3"};
        // 创建一个ArrayAdapter来绑定数据到spinner
        mLogLevelSpinnerAdapter = ArrayAdapter.createFromResource(
            context, cc.winboll.studio.libappbase.R.array.enum_loglevel_array, android.R.layout.simple_spinner_item);
        mLogLevelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置适配器并将它应用到spinner上
        mLogLevelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 设置下拉视图样式
        mLogLevelSpinner.setAdapter(mLogLevelSpinnerAdapter);
        // 为Spinner添加监听器
        mLogLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //String selectedOption = mLogLevelSpinnerAdapter.getItem(position);
                    // 处理选中的选项...
                    LogUtils.setLogLevel(LogUtils.LOG_LEVEL.values()[position]);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // 如果没有选择，则执行此操作...
                }
            });
        // 获取默认值的索引
        int defaultValueIndex = LogUtils.getLogLevel().ordinal();

        if (defaultValueIndex != -1) {
            // 如果找到了默认值，设置默认选项
            mLogLevelSpinner.setSelection(defaultValueIndex);
        }

        // 加载标签列表
        Map<String, Boolean> mapTAGList = LogUtils.getMapTAGList();
        boolean isAllSelect = true;
        for (Map.Entry<String, Boolean> entry : mapTAGList.entrySet()) {
            if (entry.getValue() == false) {
                isAllSelect = false;
                break;
            }
        }
        CheckBox cbALLTAG = findViewById(cc.winboll.studio.libappbase.R.id.viewlogCheckBox1);
        cbALLTAG.setChecked(isAllSelect);

        // 加载标签表
        recyclerView = findViewById(cc.winboll.studio.libappbase.R.id.viewlogRecyclerView1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mTAGListAdapter = new TAGListAdapter(mapTAGList);
        recyclerView.setAdapter(mTAGListAdapter);

        // 可以添加点击监听器来处理勾选框状态变化后的逻辑，比如获取当前勾选情况等
        mTAGListAdapter.notifyDataSetChanged();

        mSelectAllTAGCheckBox = findViewById(cc.winboll.studio.libappbase.R.id.viewlogCheckBox1);
        mSelectAllTAGCheckBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    LogUtils.setALlTAGListEnable(mSelectAllTAGCheckBox.isChecked());
                    //LogUtils.setALlTAGListEnable(false);
                    //mTAGListAdapter.notifyDataSetChanged();
                    mTAGListAdapter.reload();
                    //ToastUtils.show(String.format("onClick\nmSelectAllTAGCheckBox.isChecked() : %s", mSelectAllTAGCheckBox.isChecked()));
                }
            });


        // 设置滚动时不聚焦日志
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    public void updateLogView() {
        if (mLogViewHandler.isHandling() == true) {
            // 正在处理日志显示，
            // 就先设置一个新日志标志位
            // 以便日志显示完后，再次显示新日志内容
            mLogViewHandler.setIsAddNewLog(true);
        } else {
            //LogUtils.d(TAG, "LogListener showLog(String path)");
            Message message = mLogViewHandler.obtainMessage(LogViewHandler.MSG_LOGVIEW_UPDATE);
            mLogViewHandler.sendMessage(message);
            mLogViewHandler.setIsAddNewLog(false);
        }
    }

    void showAndScrollLogView() {
        mTextView.setText(LogUtils.loadLog());
        scrollLogUp();
    }

    class LogViewHandler extends Handler {

        final static int MSG_LOGVIEW_UPDATE = 0;
        volatile boolean isHandling;
        volatile boolean isAddNewLog;

        public LogViewHandler() {
            setIsHandling(false);
            setIsAddNewLog(false);
        }

        public void setIsHandling(boolean isHandling) {
            this.isHandling = isHandling;
        }

        public boolean isHandling() {
            return isHandling;
        }

        public void setIsAddNewLog(boolean isAddNewLog) {
            this.isAddNewLog = isAddNewLog;
        }

        public boolean isAddNewLog() {
            return isAddNewLog;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOGVIEW_UPDATE:{
                        if (isHandling() == false) {
                            setIsHandling(true);
                            showAndScrollLogView();
                        }
                        break;
                    }
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public class TAGItemModel {
        private String tag;
        private boolean isChecked;

        public TAGItemModel(String tag, boolean isChecked) {
            this.tag = tag;
            this.isChecked = isChecked;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }


    public class TAGListAdapter extends RecyclerView.Adapter<TAGListAdapter.ViewHolder> {

        private Map<String, Boolean> mapOrigin;
        private List<TAGItemModel> itemList;

        public TAGListAdapter(Map<String, Boolean> map) {
            mapOrigin = map;
            loadMap(mapOrigin);
        }

        void loadMap(Map<String, Boolean> map) {
            itemList = new ArrayList<TAGItemModel>();
            for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                itemList.add(new TAGItemModel(entry.getKey(), entry.getValue()));
            }
            // 添加排序功能，按照tag进行升序排序
            Collections.sort(itemList, new SortMapEntryByKeyString(true));
            //Collections.sort(itemList, new SortMapEntryByKeyString(false));
        }

        public void reload() {
            loadMap(mapOrigin);
            super.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_logtag, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final TAGItemModel item = itemList.get(position);
            holder.tvText.setText(item.getTag());
            holder.cbChecked.setChecked(item.isChecked());
            holder.cbChecked.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        LogUtils.setTAGListEnable(item.getTag(), ((CheckBox)v).isChecked());
                    }
                });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvText;
            CheckBox cbChecked;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvText = itemView.findViewById(R.id.viewlogtagTextView1);
                cbChecked = itemView.findViewById(R.id.viewlogtagCheckBox1);
            }
        }
    }

    class SortMapEntryByKeyString implements Comparator<TAGItemModel> {
        private boolean mIsDesc = true;
        // isDesc 是否降序排列
        public SortMapEntryByKeyString(boolean isDesc) {
            mIsDesc = isDesc;
        }
        Collator cmp = Collator.getInstance(java.util.Locale.CHINA);
        @Override
        public int compare(TAGItemModel o1, TAGItemModel o2) {
            if (mIsDesc) {
                return o1.getTag().compareTo(o2.getTag());
            } else {
                return o2.getTag().compareTo(o1.getTag());
            }
        }
    }
}
