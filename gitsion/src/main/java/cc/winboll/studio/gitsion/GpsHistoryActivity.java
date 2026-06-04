package cc.winboll.studio.gitsion;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cc.winboll.studio.gitsion.db.GpsHistoryDatabaseHelper;
import cc.winboll.studio.gitsion.model.GpsHistoryRecord;

public class GpsHistoryActivity extends Activity {

    private ListView mListView;
    private TextView mEmptyHint;
    private TextView mTvSystemCount;
    private TextView mTvSimCount;
    private GpsHistoryAdapter mAdapter;
    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_history);

        mListView = findViewById(R.id.lv_gps_history);
        mEmptyHint = findViewById(R.id.tv_empty_hint);
        mTvSystemCount = findViewById(R.id.tv_system_count);
        mTvSimCount = findViewById(R.id.tv_sim_count);

        List<GpsHistoryRecord> records = GpsHistoryManager.getInstance().getRecords();
        mAdapter = new GpsHistoryAdapter(records);
        mListView.setAdapter(mAdapter);

        updateEmptyHint(records.isEmpty());
        updateCounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GpsHistoryManager.getInstance().addListener(mRefreshRunnable);
        refreshData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GpsHistoryManager.getInstance().removeListener(mRefreshRunnable);
    }

    private void refreshData() {
        List<GpsHistoryRecord> records = GpsHistoryManager.getInstance().getRecords();
        mAdapter.setData(records);
        mAdapter.notifyDataSetChanged();
        updateEmptyHint(records.isEmpty());
        updateCounts();
    }

    private void updateCounts() {
        mTvSystemCount.setText(String.valueOf(GpsHistoryManager.getInstance().getSystemCount()));
        mTvSimCount.setText(String.valueOf(GpsHistoryManager.getInstance().getSimCount()));
    }

    private void updateEmptyHint(boolean empty) {
        mEmptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
        mListView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private static class GpsHistoryAdapter extends BaseAdapter {

        private List<GpsHistoryRecord> mData;
        private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        GpsHistoryAdapter(List<GpsHistoryRecord> data) {
            mData = data;
        }

        void setData(List<GpsHistoryRecord> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.list_item_gps_history, null);
            }

            GpsHistoryRecord item = mData.get(position);

            LinearLayout root = (LinearLayout) convertView.findViewById(R.id.layout_record_root);
            TextView tvType = (TextView) convertView.findViewById(R.id.tv_record_type);
            TextView tvTime = (TextView) convertView.findViewById(R.id.tv_record_time);
            TextView tvCoord = (TextView) convertView.findViewById(R.id.tv_record_coord);

            String timeStr = mTimeFormat.format(new Date(item.getLocationTime()));
            String coordStr = String.format(Locale.getDefault(),
                "纬度: %.6f  经度: %.6f  SID: %s",
                item.getLatitude(), item.getLongitude(), item.getSid());

            tvTime.setText(timeStr);
            tvCoord.setText(coordStr);

            if (item.isSim()) {
                root.setBackgroundColor(Color.parseColor("#2a3a2a"));
                tvType.setText("[模拟数据]");
                tvType.setTextColor(Color.parseColor("#ffb74d"));
            } else {
                root.setBackgroundColor(Color.parseColor("#1c1c1c"));
                tvType.setText("[系统数据]");
                tvType.setTextColor(Color.parseColor("#4fc3f7"));
            }

            return convertView;
        }
    }
}
