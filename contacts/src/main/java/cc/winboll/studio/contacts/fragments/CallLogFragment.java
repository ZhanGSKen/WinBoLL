package cc.winboll.studio.contacts.fragments;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/20 12:57:00
 * @Describe 拨号
 */
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.adapters.CallLogAdapter;
import cc.winboll.studio.contacts.beans.CallLogModel;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallLogFragment extends Fragment {

    public static final String TAG = "CallFragment";

    static volatile CallLogFragment _CallLogFragment;

    public static final int MSG_UPDATE = 1;  // 添加消息常量

    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private static final int REQUEST_READ_CALL_LOG = 1;
    private RecyclerView recyclerView;
    private CallLogAdapter callLogAdapter;
    private List<CallLogModel> callLogList = new ArrayList<>();

    // 添加Handler
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_UPDATE) {
                readCallLog();  // 接收到消息时更新通话记录
            }
        }
    };

    CallLogFragment() {
        super();
    }

    public static CallLogFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        CallLogFragment fragment = new CallLogFragment();
        fragment.setArguments(args);
        _CallLogFragment = fragment;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_log, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        callLogAdapter = new CallLogAdapter(getContext(), callLogList);
        recyclerView.setAdapter(callLogAdapter);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_READ_CALL_LOG);
        } else {
            mHandler.sendEmptyMessage(MSG_UPDATE);  // 通过Handler触发更新
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mHandler.sendEmptyMessage(MSG_UPDATE);  // 通过Handler触发更新
            }
        }
    }

    private void readCallLog() {
        callLogList.clear();  // 清空原有数据
        Cursor cursor = requireContext().getContentResolver().query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                int callType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                long callDateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                Date callDate = new Date(callDateLong);

                String callStatus = getCallStatus(callType);

                callLogList.add(new CallLogModel(phoneNumber, callStatus, callDate));
            }
            cursor.close();
            callLogAdapter.notifyDataSetChanged();
        }
    }

    private String getCallStatus(int callType) {
        switch (callType) {
            case CallLog.Calls.OUTGOING_TYPE:
                return "Outgoing";
            case CallLog.Calls.INCOMING_TYPE:
                return "Incoming";
            case CallLog.Calls.MISSED_TYPE:
                return "Missed";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);  // 清理Handler防止内存泄漏
    }

    public void triggerUpdate() {
        mHandler.sendEmptyMessage(MSG_UPDATE);
    }

    public static void updateCallLogFragment() {
        if (_CallLogFragment != null) {
            _CallLogFragment.triggerUpdate();
        }
    }
}
