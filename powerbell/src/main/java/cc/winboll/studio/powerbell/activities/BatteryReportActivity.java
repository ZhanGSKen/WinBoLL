package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电池报告页面，统计应用24小时运行时长与电池消耗情况
 * 支持应用搜索、累计耗电计算、电池广播监听，适配 API30
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class BatteryReportActivity extends WinBoLLActivity implements IWinBoLLActivity {
    // ======================== 静态常量（按功能分类） =========================
    public static final String TAG = "BatteryReportActivity";
    private static final long ONE_DAY_MS = 24 * 3600 * 1000;   // 24小时毫秒数
    private static final long ONE_MINUTE_MS = 60 * 1000;       // 1分钟毫秒数

    // ======================== 成员变量（按依赖优先级+功能分类） =========================
    // UI组件
    private Toolbar mToolbar;
    private RecyclerView rvBatteryReport;
    private EditText etSearch;

    // 数据与适配器
    private BatteryReportAdapter adapter;
    private List<AppBatteryModel> dataList = new ArrayList<>();
    private List<AppBatteryModel> filteredList = new ArrayList<>();

    // 电池相关
    private BroadcastReceiver batteryReceiver;
    private int batteryCapacity = 5400;                        // 电池容量（mAh）
    private float lastBatteryPercent = 100.0f;                 // 上次电池百分比
    private long lastCheckTime = System.currentTimeMillis();   // 上次检查时间戳

    // 缓存相关
    private Map<String, Long> appRunTimeCache = new HashMap<>();
    private Map<String, String> packageToAppNameCache = new HashMap<>();
    private PackageManager mPackageManager;

    // ======================== 接口实现方法 =========================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ======================== 生命周期方法（按执行顺序排列） =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_report);
        LogUtils.d(TAG, "【onCreate】BatteryReportActivity 初始化开始");

        // 初始化UI组件
        initView();
        // 初始化PackageManager
        mPackageManager = getPackageManager();
        LogUtils.d(TAG, "【onCreate】基础组件初始化完成");

        // 权限检查（Java7 传统条件判断）
        if (!hasUsageStatsPermission(this)) {
            Toast.makeText(this, "请进入设置-应用-权限-特殊访问权限-使用情况访问权限，开启本应用的权限", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            LogUtils.w(TAG, "【onCreate】缺少使用情况访问权限，引导用户开启");
            return;
        }

        // 初始化数据流程：加载应用→缓存名称→获取运行时长→计算初始累计耗电
        loadAllAppPackage();
        preCacheAllAppNames();
        appRunTimeCache = getAppRunTime();
        updateAppRunTimeToModel();
        calculateInitial24hTotalConsumption();
        filteredList.addAll(dataList);
        LogUtils.d(TAG, "【onCreate】数据初始化完成，原始数据量：" + dataList.size());

        // 初始化适配器
        adapter = new BatteryReportAdapter(this, filteredList, mPackageManager, packageToAppNameCache);
        rvBatteryReport.setAdapter(adapter);
        LogUtils.d(TAG, "【onCreate】适配器初始化完成，过滤后数据量：" + filteredList.size());

        // 绑定搜索监听 + 注册电池广播
        bindSearchListener();
        registerBatteryReceiver();

        LogUtils.d(TAG, "【onCreate】BatteryReportActivity 初始化完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Java7 显式非空判断
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            LogUtils.d(TAG, "【onDestroy】电池广播已注销");
        }
        LogUtils.d(TAG, "【onDestroy】BatteryReportActivity 销毁完成");
    }

    // ======================== UI初始化方法 =========================
    private void initView() {
        // 初始化Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(getTag());
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "【导航栏】点击返回");
					finish();
				}
			});

        // 初始化RecyclerView与搜索框
        etSearch = (EditText) findViewById(R.id.et_search);
        rvBatteryReport = (RecyclerView) findViewById(R.id.rv_battery_report);
        rvBatteryReport.setLayoutManager(new LinearLayoutManager(this));
        LogUtils.d(TAG, "【initView】UI组件初始化完成");
    }

    // ======================== 搜索监听绑定方法 =========================
    private void bindSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String keyword = s.toString().trim();
					LogUtils.d(TAG, "【bindSearchListener】搜索关键词变化：" + keyword);
					filterAppsByPackageAndName(keyword);
				}

				@Override
				public void afterTextChanged(Editable s) {}
			});
        LogUtils.d(TAG, "【bindSearchListener】搜索监听绑定完成");
    }

    // ======================== 电池广播注册方法 =========================
    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra("level", 100);
                int scale = intent.getIntExtra("scale", 100);
                float currentPercent = (float) level / scale * 100;
                LogUtils.d(TAG, "【电池广播】电池百分比变化：" + lastBatteryPercent + " -> " + currentPercent);

                if (currentPercent < lastBatteryPercent) {
                    float dropPercent = lastBatteryPercent - currentPercent;
                    long duration = System.currentTimeMillis() - lastCheckTime;
                    LogUtils.d(TAG, "【电池广播】电池消耗：" + dropPercent + "%，时长：" + formatRunTime(duration));

                    // 更新运行时长并计算耗电
                    appRunTimeCache = getAppRunTime();
                    updateAppRunTimeToModel();
                    calculateSingleConsumptionAndAccumulate(dropPercent, appRunTimeCache);
                }

                // 刷新记录
                lastBatteryPercent = currentPercent;
                lastCheckTime = System.currentTimeMillis();
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        LogUtils.d(TAG, "【registerBatteryReceiver】电池广播注册完成");
    }

    // ======================== 权限检查方法 =========================
    /**
     * 检查是否拥有使用情况访问权限
     * @param context 上下文
     * @return 拥有权限返回true，否则返回false
     */
    private boolean hasUsageStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.w(TAG, "【hasUsageStatsPermission】系统版本低于LOLLIPOP，不支持使用情况访问权限");
            return false;
        }

        android.app.usage.UsageStatsManager manager =
			(android.app.usage.UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) {
            LogUtils.e(TAG, "【hasUsageStatsPermission】获取UsageStatsManager失败");
            return false;
        }

        long endTime = System.currentTimeMillis();
        long startTime = endTime - ONE_MINUTE_MS;
        List<android.app.usage.UsageStats> statsList = manager.queryUsageStats(
			android.app.usage.UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        boolean hasPermission = statsList != null && !statsList.isEmpty();
        LogUtils.d(TAG, "【hasUsageStatsPermission】使用情况访问权限检查结果：" + hasPermission);
        return hasPermission;
    }

    // ======================== 数据加载与缓存方法 =========================
    /**
     * 加载所有应用包名，初始化数据模型
     */
    private void loadAllAppPackage() {
        List<ApplicationInfo> appList = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        dataList.clear();
        LogUtils.d(TAG, "【loadAllAppPackage】开始加载应用包名列表，共找到" + appList.size() + "个应用");

        for (ApplicationInfo appInfo : appList) {
            String packageName = appInfo.packageName;
            dataList.add(new AppBatteryModel(packageName, 0.0f, 0.0f, 0));
        }
        LogUtils.d(TAG, "【loadAllAppPackage】应用包名列表加载完成，共添加" + dataList.size() + "个包名");
    }

    /**
     * 预缓存所有应用名称，减少PackageManager重复调用
     */
    private void preCacheAllAppNames() {
        packageToAppNameCache.clear();
        LogUtils.d(TAG, "【preCacheAllAppNames】开始预缓存包名-应用名称映射");

        for (AppBatteryModel model : dataList) {
            String packageName = model.getPackageName();
            String appName = getAppNameByPackage(packageName);
            packageToAppNameCache.put(packageName, appName);
        }
        LogUtils.d(TAG, "【preCacheAllAppNames】预缓存完成，共缓存" + packageToAppNameCache.size() + "个应用名称");
    }

    /**
     * 通过包名获取应用名称，带异常处理
     * @param packageName 应用包名
     * @return 应用名称，获取失败返回包名
     */
    private String getAppNameByPackage(String packageName) {
        LogUtils.v(TAG, "【getAppNameByPackage】查询包名：" + packageName);
        try {
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, 0);
            return mPackageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "【getAppNameByPackage】包名" + packageName + "对应的应用未找到：" + e.getMessage());
            return packageName;
        } catch (Exception e) {
            LogUtils.e(TAG, "【getAppNameByPackage】查询应用名称失败（包名：" + packageName + "）：" + e.getMessage());
            return packageName;
        }
    }

    /**
     * 更新运行时长到数据模型
     */
    private void updateAppRunTimeToModel() {
        int updateCount = 0;
        for (AppBatteryModel model : dataList) {
            String packageName = model.getPackageName();
            Long runTime = appRunTimeCache.containsKey(packageName) ? appRunTimeCache.get(packageName) : 0L;
            model.setRunTime(runTime);
            if (runTime > 0) {
                updateCount++;
            }
        }
        LogUtils.d(TAG, "【updateAppRunTimeToModel】更新完成，数据量：" + dataList.size() + "，更新运行时长应用数：" + updateCount);
    }

    /**
     * 获取应用24小时运行时长
     * @return 应用包名-运行时长（ms）映射
     */
    private Map<String, Long> getAppRunTime() {
        Map<String, Long> runTimeMap = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                android.app.usage.UsageStatsManager manager =
					(android.app.usage.UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long startTime = endTime - ONE_DAY_MS; // 近24小时
                List<android.app.usage.UsageStats> statsList = manager.queryUsageStats(
					android.app.usage.UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

                for (android.app.usage.UsageStats stats : statsList) {
                    long runTimeMs = stats.getTotalTimeInForeground();
                    String packageName = stats.getPackageName();
                    runTimeMap.put(packageName, runTimeMs);
                    LogUtils.v(TAG, "【getAppRunTime】包名" + packageName + "24小时运行时长：" + formatRunTime(runTimeMs));
                    if (packageName.equals("aidepro.top")) {
                        LogUtils.d(TAG, "【getAppRunTime】特殊查询包名" + packageName + "有结果");
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "【getAppRunTime】获取应用运行时长失败：" + e.getMessage());
            }
        }
        LogUtils.d(TAG, "【getAppRunTime】应用运行时长列表数量：" + runTimeMap.size());
        return runTimeMap;
    }

    // ======================== 核心计算方法 =========================
    /**
     * 初始化时计算24小时累计耗电（赋值给totalConsumption）
     * 逻辑：基于24小时运行时长占比，分配当前电池容量的理论24小时消耗
     */
    private void calculateInitial24hTotalConsumption() {
        long total24hRunTime = 0;
        // 1. 计算24小时内所有应用总运行时长
        for (Map.Entry<String, Long> entry : appRunTimeCache.entrySet()) {
            total24hRunTime += entry.getValue();
        }
        LogUtils.d(TAG, "【calculateInitial24hTotalConsumption】24小时内所有应用总运行时长：" + formatRunTime(total24hRunTime));

        // 2. 按运行时长占比分配24小时累计耗电
        for (AppBatteryModel model : dataList) {
            String packageName = model.getPackageName();
            Long app24hRunTime = appRunTimeCache.getOrDefault(packageName, 0L);

            float ratio = (total24hRunTime > 0) ? (float) app24hRunTime / total24hRunTime : 0;
            float initialTotalConsumption = batteryCapacity * ratio;
            model.setTotalConsumption(initialTotalConsumption);
            LogUtils.v(TAG, "【calculateInitial24hTotalConsumption】应用包" + packageName + "24小时累计耗电初始化：" + initialTotalConsumption + " mAh");
        }
        LogUtils.d(TAG, "【calculateInitial24hTotalConsumption】24小时累计耗电初始化完成");
    }

    /**
     * 计算单次耗电（赋值给consumption）+ 累加至累计耗电
     * @param dropPercent 电池下降百分比
     * @param runTimeMap  应用运行时长映射
     */
    private void calculateSingleConsumptionAndAccumulate(float dropPercent, Map<String, Long> runTimeMap) {
        LogUtils.d(TAG, "【calculateSingleConsumptionAndAccumulate】开始计算，电池下降百分比：" + dropPercent);
        long totalSingleRunTime = 0;
        // 1. 计算本次电池下降期间的总运行时长
        for (Map.Entry<String, Long> entry : runTimeMap.entrySet()) {
            totalSingleRunTime += entry.getValue();
        }
        LogUtils.d(TAG, "【calculateSingleConsumptionAndAccumulate】本次电池下降总运行时长：" + formatRunTime(totalSingleRunTime));

        // 2. 遍历计算每个应用的单次耗电并累加
        for (AppBatteryModel model : dataList) {
            String packageName = model.getPackageName();
            Long appSingleRunTime = runTimeMap.getOrDefault(packageName, 0L);

            float ratio = (totalSingleRunTime > 0) ? (float) appSingleRunTime / totalSingleRunTime : 0;
            float singleConsumption = batteryCapacity * dropPercent / 100 * ratio;
            model.setConsumption(singleConsumption);

            // 累加至累计耗电
            float newTotalConsumption = model.getTotalConsumption() + singleConsumption;
            model.setTotalConsumption(newTotalConsumption);
            model.setRunTime(appSingleRunTime);

            LogUtils.v(TAG, String.format("【calculateSingleConsumptionAndAccumulate】应用包%s：单次耗电%.1f mAh，累计耗电%.1f mAh",
										  packageName, singleConsumption, newTotalConsumption));
        }

        // 3. 按累计耗电降序排序
        Collections.sort(dataList, new Comparator<AppBatteryModel>() {
				@Override
				public int compare(AppBatteryModel m1, AppBatteryModel m2) {
					return Float.compare(m2.getTotalConsumption(), m1.getTotalConsumption());
				}
			});

        // 4. 重新过滤并刷新列表
        filterAppsByPackageAndName(etSearch.getText().toString().trim());
        LogUtils.d(TAG, "【calculateSingleConsumptionAndAccumulate】单次耗电计算与累加完成，列表已刷新");
    }

    /**
     * 双维度过滤（包名+应用名）
     * @param keyword 搜索关键词
     */
    private void filterAppsByPackageAndName(String keyword) {
        filteredList.clear();
        if (keyword == null || keyword.isEmpty()) {
            filteredList.addAll(dataList);
            LogUtils.d(TAG, "【filterAppsByPackageAndName】搜索关键词为空，显示全部应用，数量：" + filteredList.size());
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (AppBatteryModel model : dataList) {
                String packageName = model.getPackageName();
                String packageNameLower = packageName.toLowerCase();
                String appName = packageToAppNameCache.get(packageName);
                String appNameLower = appName.toLowerCase();

                boolean isMatched = packageNameLower.contains(lowerKeyword) || appNameLower.contains(lowerKeyword);
                if (isMatched) {
                    filteredList.add(model);
                }
            }
            LogUtils.d(TAG, "【filterAppsByPackageAndName】搜索关键词：" + keyword + "，匹配应用数量：" + filteredList.size());
        }
        adapter.notifyDataSetChanged();
    }

    // ======================== 工具方法 =========================
    /**
     * 格式化运行时长
     * @param runTimeMs 运行时长（ms）
     * @return 格式化后的运行时长字符串
     */
    private String formatRunTime(long runTimeMs) {
        if (runTimeMs <= 0) {
            return "0秒";
        }
        long seconds = runTimeMs / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours > 0) {
            return String.format("%d时%d分%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    // ======================== 内部类：数据模型 =========================
    /**
     * 应用电池数据模型
     * - consumption：单次耗电（两次电池广播间的消耗）
     * - totalConsumption：累计耗电（24小时初始化值+后续单次累加）
     * - runTime：运行时长（ms）
     */
    public static class AppBatteryModel {
        private String packageName;    // 应用包名（核心标识）
        private float consumption;     // 单次耗电（mAh）
        private float totalConsumption;// 累计耗电（mAh）
        private long runTime;          // 运行时长（ms）

        // Java7 显式构造
        public AppBatteryModel(String packageName, float consumption, float totalConsumption, long runTime) {
            this.packageName = packageName;
            this.consumption = consumption;
            this.totalConsumption = totalConsumption;
            this.runTime = runTime;
        }

        // Getter/Setter
        public String getPackageName() {
            return packageName;
        }

        public float getConsumption() {
            return consumption;
        }

        public void setConsumption(float consumption) {
            this.consumption = consumption;
        }

        public float getTotalConsumption() {
            return totalConsumption;
        }

        public void setTotalConsumption(float totalConsumption) {
            this.totalConsumption = totalConsumption;
        }

        public long getRunTime() {
            return runTime;
        }

        public void setRunTime(long runTime) {
            this.runTime = runTime;
        }
    }

    // ======================== 内部类：RecyclerView适配器 =========================
    /**
     * 电池报告列表适配器，显示应用名称、累计耗电、运行时长
     */
    public static class BatteryReportAdapter extends RecyclerView.Adapter<BatteryReportAdapter.ViewHolder> {
        private Context mContext;
        private List<AppBatteryModel> mDataList;
        private PackageManager mPm;
        private Map<String, String> mPackageToNameCache;

        // Java7 显式构造
        public BatteryReportAdapter(Context context, List<AppBatteryModel> dataList,
                                    PackageManager pm, Map<String, String> packageToNameCache) {
            this.mContext = context;
            this.mDataList = dataList;
            this.mPm = pm;
            this.mPackageToNameCache = packageToNameCache;
            LogUtils.d(TAG, "【BatteryReportAdapter】适配器构造完成，数据量：" + dataList.size());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext)
				.inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Java7 显式非空判断
            if (mDataList == null || mDataList.isEmpty() || position >= mDataList.size()) {
                holder.tvAppName.setText("未知应用");
                holder.tvConsumption.setText("累计耗电：0.0 mAh | 运行时长：0秒");
                LogUtils.w(TAG, "【onBindViewHolder】数据异常，位置：" + position);
                return;
            }

            AppBatteryModel model = mDataList.get(position);
            String packageName = model.getPackageName();
            String appName = "";

            // 优先从缓存获取应用名
            if (mPackageToNameCache != null && mPackageToNameCache.containsKey(packageName)) {
                appName = mPackageToNameCache.get(packageName);
            } else {
                // 缓存无数据时兜底查询
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(packageName, 0);
                    appName = mPm.getApplicationLabel(appInfo).toString();
                    if (mPackageToNameCache != null) {
                        mPackageToNameCache.put(packageName, appName);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    appName = packageName;
                    LogUtils.e("BatteryReportAdapter", "【onBindViewHolder】包名" + packageName + "对应的应用未找到：" + e.getMessage());
                } catch (Exception e) {
                    appName = packageName;
                    LogUtils.e("BatteryReportAdapter", "【onBindViewHolder】查询应用名称失败（包名：" + packageName + "）：" + e.getMessage());
                }
            }

            // 显示逻辑：应用名称 + 累计耗电 + 运行时长
            holder.tvAppName.setText(appName);
            String runTimeStr = ((BatteryReportActivity) mContext).formatRunTime(model.getRunTime());
            String totalConsumptionText = String.format("累计耗电：%.1f mAh | 运行时长：%s",
														model.getTotalConsumption(), runTimeStr);
            holder.tvConsumption.setText(totalConsumptionText);

            // 显示优化
            holder.tvAppName.setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.tvConsumption.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
            holder.tvAppName.setTextSize(16);
            holder.tvConsumption.setTextSize(14);
        }

        @Override
        public int getItemCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        /**
         * ViewHolder：绑定系统布局控件
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAppName;     // 应用名称
            TextView tvConsumption; // 累计耗电 + 运行时长

            public ViewHolder(View itemView) {
                super(itemView);
                tvAppName = (TextView) itemView.findViewById(android.R.id.text1);
                tvConsumption = (TextView) itemView.findViewById(android.R.id.text2);
            }
        }
    }
}

