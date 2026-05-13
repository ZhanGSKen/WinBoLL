package cc.winboll.studio.powerbell.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BatteryData;
import java.util.ArrayList;
import java.util.List;

/**
 * 电池报告数据适配器，用于RecyclerView展示电池电量、充放电时间数据
 * 适配 API30，基于 Java7 开发
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/03/22 14:38:55
 * @Describe 电池报告数据适配器
 */
public class BatteryAdapter extends RecyclerView.Adapter<BatteryAdapter.ViewHolder> {
    // ======================== 静态常量 =========================
    public static final String TAG = "BatteryAdapter";
    private static final String FORMAT_BATTERY_LEVEL = "%d%%"; // 电量显示格式
    private static final String PREFIX_DISCHARGE_TIME = "使用时间: "; // 放电时间前缀
    private static final String PREFIX_CHARGE_TIME = "充电时间: "; // 充电时间前缀

    // ======================== 成员变量 =========================
    private List<BatteryData> dataList = new ArrayList<>(); // 电池数据列表

    // ======================== 构造方法 =========================
    public BatteryAdapter() {
        LogUtils.d(TAG, "【BatteryAdapter】适配器初始化，初始数据列表为空");
    }

    // ======================== 数据操作方法 =========================
    /**
     * 更新适配器数据并刷新列表
     * @param newData 新的电池数据列表
     */
    public void updateData(List<BatteryData> newData) {
        LogUtils.d(TAG, "【updateData】开始更新数据，新数据列表是否为空：" + (newData == null));
        // 判空处理，避免空指针
        if (newData != null) {
            dataList = newData;
            LogUtils.d(TAG, "【updateData】数据更新完成，当前数据量：" + dataList.size());
        } else {
            dataList.clear();
            LogUtils.w(TAG, "【updateData】新数据列表为空，已清空本地数据");
        }
        notifyDataSetChanged();
        LogUtils.d(TAG, "【updateData】已通知列表刷新");
    }

    // ======================== RecyclerView 重写方法 =========================
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtils.d(TAG, "【onCreateViewHolder】创建ViewHolder，父容器：" + parent.getContext().getClass().getSimpleName());
        View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.item_battery_report, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        LogUtils.d(TAG, "【onCreateViewHolder】ViewHolder创建完成");
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LogUtils.d(TAG, "【onBindViewHolder】绑定ViewHolder，位置：" + position);
        // 判空与越界校验
        if (dataList == null || dataList.isEmpty() || position >= dataList.size()) {
            LogUtils.w(TAG, "【onBindViewHolder】数据异常，无法绑定视图，位置：" + position);
            return;
        }

        BatteryData item = dataList.get(position);
        // 绑定数据到视图
        holder.tvLevel.setText(String.format(FORMAT_BATTERY_LEVEL, item.getCurrentLevel()));
        holder.tvDischargeTime.setText(PREFIX_DISCHARGE_TIME + item.getDischargeTime());
        holder.tvChargeTime.setText(PREFIX_CHARGE_TIME + item.getChargeTime());

        LogUtils.d(TAG, "【onBindViewHolder】视图绑定完成，位置：" + position + "，电量：" + item.getCurrentLevel() + "%");
    }

    @Override
    public int getItemCount() {
        int count = dataList.size();
        LogUtils.d(TAG, "【getItemCount】获取条目数量：" + count);
        return count;
    }

    // ======================== ViewHolder 内部类 =========================
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel; // 电量显示
        TextView tvDischargeTime; // 放电时间显示
        TextView tvChargeTime; // 充电时间显示

        ViewHolder(View itemView) {
            super(itemView);
            // 初始化视图控件
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvDischargeTime = itemView.findViewById(R.id.tvDischargeTime);
            tvChargeTime = itemView.findViewById(R.id.tvChargeTime);
            LogUtils.d(TAG, "【ViewHolder】控件初始化完成");
        }
    }
}

