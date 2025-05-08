package cc.winboll.studio.powerbell.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/22 14:38:55
 * @Describe 电池报告数据适配器
 */
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.adapters.BatteryAdapter;
import cc.winboll.studio.powerbell.beans.BatteryData;
import java.util.ArrayList;
import java.util.List;

public class BatteryAdapter extends RecyclerView.Adapter<BatteryAdapter.ViewHolder> {
    public static final String TAG = "BatteryAdapter";
    private List<BatteryData> dataList = new ArrayList<>();

    public void updateData(List<BatteryData> newData) {
        dataList = newData;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_battery_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BatteryData item = dataList.get(position);
        holder.tvLevel.setText(String.format("%d%%", item.getCurrentLevel()));
        holder.tvDischargeTime.setText("使用时间: " + item.getDischargeTime());
        holder.tvChargeTime.setText("充电时间: " + item.getChargeTime());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel;
        TextView tvDischargeTime;
        TextView tvChargeTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvDischargeTime = itemView.findViewById(R.id.tvDischargeTime);
            tvChargeTime = itemView.findViewById(R.id.tvChargeTime);
        }
    }
}

