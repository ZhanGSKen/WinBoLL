package cc.winboll.studio.contacts.fragments;

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
import cc.winboll.studio.contacts.model.CallLogModel;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/20 12:57:00
 * @Describe 通话记录区域视图（支持懒加载，仅切换到当前页才加载数据）
 */
public class CallLogFragment extends Fragment {

    // ====================== 常量定义区 ======================
    public static final String TAG = "CallLogFragment";
    public static final int MSG_UPDATE = 1;
    private static final String ARG_PAGE = "ARG_PAGE";
    private static final int REQUEST_READ_CALL_LOG = 1;

    // ====================== 静态成员区 ======================
    static volatile CallLogFragment _CallLogFragment;

    // ====================== 页面参数区 ======================
    private int mPage;

    // ====================== UI控件与适配器区 ======================
    private RecyclerView recyclerView;
    private CallLogAdapter callLogAdapter;
    private List<CallLogModel> callLogList = new ArrayList<CallLogModel>();

    // ====================== 业务逻辑成员区 ======================
    private Handler mHandler;
    // 懒加载标记：记录当前Fragment是否已初始化数据（避免重复加载）
    private boolean isDataInited = false;

    // ====================== 单例与实例化函数区 ======================
    CallLogFragment() {
        super();
    }

    public static CallLogFragment newInstance(int page) {
        LogUtils.d(TAG, "newInstance: 创建通话记录Fragment实例，页码=" + page);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        CallLogFragment fragment = new CallLogFragment();
        fragment.setArguments(args);
        _CallLogFragment = fragment;
        return fragment;
    }

    // ====================== 生命周期函数区 ======================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: Fragment创建开始");
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
            LogUtils.d(TAG, "onCreate: 读取页面参数，mPage=" + mPage);
        }
        // Java7 兼容：移除Lambda，使用匿名内部类初始化Handler
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_UPDATE) {
                    LogUtils.d(TAG, "handleMessage: 收到更新消息，开始读取通话记录");
                    readCallLog();
                }
            }
        };
        LogUtils.d(TAG, "onCreate: Fragment创建完成");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView: 加载Fragment布局");
        return inflater.inflate(R.layout.fragment_call_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.d(TAG, "onViewCreated: 视图创建完成，仅初始化控件（不加载数据）");
        // 初始化RecyclerView（仅绑定控件、设置布局管理器，不设置数据/发起请求）
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 初始化适配器（传入空列表，后续懒加载时更新数据）
        callLogAdapter = new CallLogAdapter(getContext(), callLogList);
        recyclerView.setAdapter(callLogAdapter);
        LogUtils.d(TAG, "onViewCreated: RecyclerView控件初始化完成（未加载数据）");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume: Fragment进入前台");
        // 已初始化过数据 → 仅刷新（避免重复初始化，优化性能）
        if (isDataInited && callLogAdapter != null) {
            LogUtils.d(TAG, "onResume: 数据已初始化，仅刷新列表");
            callLogAdapter.relaodContacts();
            readCallLog(); // 刷新最新通话记录
            LogUtils.d(TAG, "onResume: 通话记录数据刷新完成");
        }
        // 未初始化 → 不操作（等待MainActivity调用initData触发初始化）
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: Fragment开始销毁");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            LogUtils.d(TAG, "onDestroy: Handler消息已清空");
        }
        // 释放资源，避免内存泄漏
        if (callLogList != null) {
            callLogList.clear();
            callLogList = null;
        }
        callLogAdapter = null;
        recyclerView = null;
        _CallLogFragment = null;
        isDataInited = false;
        LogUtils.d(TAG, "onDestroy: Fragment销毁完成");
    }

    // ====================== 权限回调函数区 ======================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.d(TAG, "onRequestPermissionsResult: 权限请求回调，requestCode=" + requestCode);
        if (requestCode == REQUEST_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtils.d(TAG, "onRequestPermissionsResult: 通话记录权限授予成功，开始加载数据");
                mHandler.sendEmptyMessage(MSG_UPDATE);
            } else {
                LogUtils.e(TAG, "onRequestPermissionsResult: 通话记录权限被拒绝，无法加载数据");
            }
        }
    }

    // ====================== 懒加载核心方法（供MainActivity调用） ======================
    public void initData() {
        // 避免重复初始化（双重防护：标记+判断）
        if (isDataInited || getContext() == null) {
            LogUtils.d(TAG, "initData: 数据已初始化/上下文为空，跳过");
            return;
        }
        LogUtils.d(TAG, "initData: 开始懒加载初始化通话记录数据");
        // 权限检查与数据加载（原onViewCreated中的核心逻辑迁移至此）
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.w(TAG, "initData: 读取通话记录权限未授予，发起权限申请");
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_READ_CALL_LOG);
        } else {
            LogUtils.d(TAG, "initData: 权限已授予，发送更新消息加载数据");
            mHandler.sendEmptyMessage(MSG_UPDATE);
        }
        // 标记为已初始化（后续仅刷新，不重复初始化）
        isDataInited = true;
        LogUtils.d(TAG, "initData: 懒加载初始化流程完成");
    }

    // ====================== 业务核心函数区 ======================
    private void readCallLog() {
        LogUtils.d(TAG, "readCallLog: 开始读取系统通话记录");
        // 避免空指针（懒加载场景下，控件可能未初始化完成）
        if (callLogList == null || callLogAdapter == null || getContext() == null) {
            LogUtils.w(TAG, "readCallLog: 控件/列表为空，跳过读取");
            return;
        }
        callLogList.clear();
        Cursor cursor = null;
        try {
            cursor = requireContext().getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            );
            if (cursor != null) {
                LogUtils.d(TAG, "readCallLog: 成功获取通话记录游标，数据条数=" + cursor.getCount());
                while (cursor.moveToNext()) {
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int callType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    long callDateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    Date callDate = new Date(callDateLong);
                    String callStatus = getCallStatus(callType);

                    callLogList.add(new CallLogModel(phoneNumber, callStatus, callDate));
                }
                callLogAdapter.notifyDataSetChanged();
                LogUtils.d(TAG, "readCallLog: 通话记录数据解析完成，共" + callLogList.size() + "条");
            } else {
                LogUtils.w(TAG, "readCallLog: 通话记录游标为空");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "readCallLog: 读取通话记录异常", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                LogUtils.d(TAG, "readCallLog: 游标已关闭");
            }
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

    // ====================== 外部调用函数区 ======================
    public void triggerUpdate() {
        LogUtils.d(TAG, "triggerUpdate: 外部触发通话记录更新");
        if (isDataInited) { // 已初始化才触发更新（避免未加载时调用）
            mHandler.sendEmptyMessage(MSG_UPDATE);
        }
    }

    public static void updateCallLogFragment() {
        if (_CallLogFragment != null) {
            LogUtils.d(TAG, "updateCallLogFragment: 静态方法触发Fragment更新");
            _CallLogFragment.triggerUpdate();
        } else {
            LogUtils.w(TAG, "updateCallLogFragment: Fragment实例为空，无法更新");
        }
    }
}

