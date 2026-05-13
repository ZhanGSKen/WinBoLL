package cc.winboll.studio.contacts.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/20 12:58:15
 * @Describe 应用日志区域视图（支持懒加载，仅切换到当前页才启动日志）
 */
public class LogFragment extends Fragment {

    // ====================== 常量定义区 ======================
    public static final String TAG = "LogFragment";
    private static final String ARG_PAGE = "ARG_PAGE";

    // ====================== 页面参数区 ======================
    private int mPage;

    // ====================== UI控件区 ======================
    private LogView mLogView;

    // ====================== 懒加载标记区 ======================
    private boolean isViewInitialized = false; // 视图控件绑定完成标记
    private boolean isLazyInitCompleted = false; // 懒加载总流程完成标记
    private boolean isLogViewStarted = false; // LogView启动状态标记

    // ====================== 实例化函数区 ======================
    public static LogFragment newInstance(int page) {
        LogUtils.d(TAG, "newInstance: 创建日志Fragment实例，页码=" + page);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        LogFragment fragment = new LogFragment();
        fragment.setArguments(args);
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
        LogUtils.d(TAG, "onCreate: Fragment创建完成");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView: 加载Fragment布局");
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        // Java7 适配：添加强制类型转换，仅初始化LogView控件（不启动）
        mLogView = (LogView) view.findViewById(R.id.logview);
        LogUtils.d(TAG, "onCreateView: LogView控件初始化完成（未启动）");
        // 标记视图控件绑定完成
        isViewInitialized = true;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume: Fragment进入前台");
        // 已完成懒加载 → 仅重启LogView（切回页面时恢复日志显示）
        if (isLazyInitCompleted && mLogView != null && !isLogViewStarted) {
            mLogView.start();
            isLogViewStarted = true;
            LogUtils.d(TAG, "onResume: LogView已重启，恢复日志显示");
        }
        // 未完成懒加载 → 不操作（等待MainActivity调用initData触发）
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: Fragment开始销毁");
        if (mLogView != null) {
            // 若LogView有停止方法，必须调用（避免后台持续占用资源，根据实际API调整）
            // mLogView.stop(); // 关键：释放LogView资源，防止内存泄漏
            LogUtils.d(TAG, "onDestroy: LogView资源已释放");
        }
        // 重置所有标记，避免重建时状态异常
        mLogView = null;
        isViewInitialized = false;
        isLazyInitCompleted = false;
        isLogViewStarted = false;
        LogUtils.d(TAG, "onDestroy: Fragment销毁完成");
    }

    // ====================== 懒加载核心方法（供MainActivity调用） ======================
    public void initData() {
        // 双重防护：避免重复初始化（标记+视图就绪+控件非空）
        if (isLazyInitCompleted || !isViewInitialized || mLogView == null || getContext() == null) {
            LogUtils.d(TAG, "initData: 懒加载已完成/视图未就绪，跳过");
            return;
        }
        LogUtils.d(TAG, "initData: 开始懒加载初始化，启动LogView");
        // 核心：启动LogView（原onCreateView中的start逻辑迁移至此）
        mLogView.start();
        isLogViewStarted = true;
        // 标记懒加载总流程完成（仅执行一次）
        isLazyInitCompleted = true;
        LogUtils.d(TAG, "initData: 懒加载初始化完成，LogView正常启动");
    }
}

