package cc.winboll.studio.powerbell.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/22 14:20:15
 */
import android.app.Activity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.adapters.BatteryAdapter;
import cc.winboll.studio.powerbell.beans.BatteryData;
import java.util.Arrays;
import java.util.List;

public class BatteryReporterActivity extends Activity {
    public static final String TAG = "BatteryReporterActivity";

    private RecyclerView rvBatteryReport;
    private BatteryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_reporter);

        rvBatteryReport = findViewById(R.id.rvBatteryReport);
        setupRecyclerView();
        loadSampleData();
    }

    private void setupRecyclerView() {
        adapter = new BatteryAdapter();
        rvBatteryReport.setLayoutManager(new LinearLayoutManager(this));
        rvBatteryReport.setAdapter(adapter);
        rvBatteryReport.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadSampleData() {
        List<BatteryData> dataList = Arrays.asList(
            new BatteryData(95, "01:23:45", "00:05:12"),
            new BatteryData(80, "02:15:30", "00:10:00"),
            new BatteryData(65, "03:45:15", "00:15:30"),
            new BatteryData(50, "05:00:00", "00:20:45")
        );
        adapter.updateData(dataList);
    }
}

