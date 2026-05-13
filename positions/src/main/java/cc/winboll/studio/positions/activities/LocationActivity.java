package cc.winboll.studio.positions.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.positions.App;
import cc.winboll.studio.positions.MainActivity;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.adapters.PositionAdapter;
import cc.winboll.studio.positions.models.PositionModel;
import cc.winboll.studio.positions.services.IdleGpsService;
import cc.winboll.studio.positions.services.MainService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025/09/29 18:22:00
 * @EditTime 2026/05/03 16:36:54
 * @Describe 位置列表页面，适配MainService GPS接口、规范服务交互、完善生命周期资源释放，新增应用空转状态副标题提示
 */
public class LocationActivity extends WinBoLLActivity implements IWinBoLLActivity {
    // 常量
    public static final String TAG = "LocationActivity";

    // 成员属性有序排版
    private Toolbar mToolbar;
    private RecyclerView mRvPosition;
    private PositionAdapter mPositionAdapter;

    private MainService mMainService;
    private final AtomicBoolean isServiceBound = new AtomicBoolean(false);
    private final AtomicBoolean isAdapterInited = new AtomicBoolean(false);

    private MainService.GpsUpdateListener mGpsUpdateListener;
    private PositionModel mCurrentGpsPos;
    private final ArrayList<PositionModel> mLocalPosCache = new ArrayList<PositionModel>();

    // 服务连接回调
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            LogUtils.d(TAG, "onServiceConnected invoke");
            if (!(service instanceof MainService.LocalBinder)) {
                LogUtils.e(TAG, "服务绑定失败：Binder类型不匹配");
                isServiceBound.set(false);
                return;
            }

            try {
                final MainService.LocalBinder binder = (MainService.LocalBinder) service;
                mMainService = binder.getService();
                isServiceBound.set(true);
                LogUtils.d(TAG, "MainService bind success");

                syncDataFromMainService();
                registerGpsListener();
                initPositionAdapter();
            } catch (final Exception e) {
                LogUtils.e(TAG, "服务绑定异常：" + e.getMessage());
                isServiceBound.set(false);
                mMainService = null;
                showToast("服务初始化失败，无法加载数据");
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            LogUtils.w(TAG, "MainService 服务断开");
            mMainService = null;
            isServiceBound.set(false);
            isAdapterInited.set(false);
        }
    };

    // 接口方法实现
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * 刷新空转状态副标题
     */
    private void refreshIdleStatusTitle() {
        LogUtils.d(TAG, "refreshIdleStatusTitle invoke");
        if (mToolbar == null) {
            LogUtils.w(TAG, "Toolbar为空，跳过刷新");
            return;
        }
        final boolean idleStatus = App.isAppIdleRunning();
        if (idleStatus) {
            mToolbar.setSubtitle("当前状态：应用正在空转运行");
            LogUtils.d(TAG, "检测应用空转状态为 : " + idleStatus);
        } else {
            mToolbar.setSubtitle(getTag());
        }
    }

    /**
     * 初始化页面控件
     */
    private void initView() {
        LogUtils.d(TAG, "initView invoke");
        mRvPosition = findViewById(R.id.rv_position_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvPosition.setLayoutManager(layoutManager);
        mLocalPosCache.clear();
    }

    /**
     * 绑定后台服务
     */
    private void bindMainService() {
        LogUtils.d(TAG, "bindMainService invoke");
        if (isServiceBound.get()) {
            LogUtils.w(TAG, "服务已绑定，无需重复执行");
            return;
        }
        final Intent serviceIntent = new Intent(this, MainService.class);
        final boolean bindSuccess = bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if (!bindSuccess) {
            LogUtils.e(TAG, "发起服务绑定请求失败");
            showToast("服务绑定失败，无法加载位置数据");
        }
    }

    /**
     * 同步服务数据至本地缓存
     */
    private void syncDataFromMainService() {
        LogUtils.d(TAG, "syncDataFromMainService invoke");
        if (!isServiceBound.get() || mMainService == null) {
            LogUtils.w(TAG, "服务未就绪，使用本地缓存");
            return;
        }
        try {
            final ArrayList<PositionModel> servicePosList = mMainService.getPositionList();
            synchronized (mLocalPosCache) {
                mLocalPosCache.clear();
                if (servicePosList != null && !servicePosList.isEmpty()) {
                    mLocalPosCache.addAll(servicePosList);
                }
            }
            LogUtils.d(TAG, "同步完成，缓存数量 : " + mLocalPosCache.size());
        } catch (final Exception e) {
            LogUtils.e(TAG, "数据同步异常 : " + e.getMessage());
        }
    }

    /**
     * 初始化列表适配器
     */
    private void initPositionAdapter() {
        LogUtils.d(TAG, "initPositionAdapter invoke");
        if (isAdapterInited.get() || !isServiceBound.get()
			|| mMainService == null || mRvPosition == null) {
            LogUtils.w(TAG, "适配器初始化条件不满足，跳过");
            return;
        }

        try {
            mPositionAdapter = new PositionAdapter(this, mLocalPosCache, mMainService);
            mPositionAdapter.setOnDeleteClickListener(new PositionAdapter.OnDeleteClickListener() {
					@Override
					public void onDeleteClick(final int position) {
						LogUtils.d(TAG, "onDeleteClick position = " + position);
						YesNoAlertDialog.show(LocationActivity.this, "删除位置提示",
                            "是否删除此项锚点位置？", new YesNoAlertDialog.OnDialogResultListener() {
                                @Override
                                public void onNo() {

                                }

                                @Override
                                public void onYes() {
                                    if (position < 0 || position >= mLocalPosCache.size()) {
                                        LogUtils.w(TAG, "删除索引参数非法");
                                        return;
                                    }
                                    final PositionModel deletePos = mLocalPosCache.get(position);
                                    if (deletePos != null && !deletePos.getPositionId().isEmpty()) {
                                        mMainService.removePosition(deletePos.getPositionId());
                                        synchronized (mLocalPosCache) {
                                            mLocalPosCache.remove(position);
                                        }
                                        mPositionAdapter.notifyItemRemoved(position);
                                        showToast("删除位置成功：" + deletePos.getMemo());
                                    }
                                }
                            });
					}
				});

            mPositionAdapter.setOnSavePositionClickListener(new PositionAdapter.OnSavePositionClickListener() {
					@Override
					public void onSavePositionClick(final int position, final PositionModel updatedPos) {
						LogUtils.d(TAG, "onSavePositionClick position = " + position);
						if (position < 0 || position >= mLocalPosCache.size() || updatedPos == null) {
							LogUtils.w(TAG, "保存参数非法");
							showToast("服务未就绪，保存失败");
							return;
						}
						mMainService.updatePosition(updatedPos);
						synchronized (mLocalPosCache) {
							mLocalPosCache.set(position, updatedPos);
						}
						mPositionAdapter.notifyItemChanged(position);
						showToast("保存位置成功：" + updatedPos.getMemo());
					}
				});

            mRvPosition.setAdapter(mPositionAdapter);
            isAdapterInited.set(true);
            LogUtils.d(TAG, "适配器初始化完成");
        } catch (final Exception e) {
            LogUtils.e(TAG, "适配器初始化异常 : " + e.getMessage());
            isAdapterInited.set(false);
            mPositionAdapter = null;
            showToast("位置列表初始化失败，请重试");
        }
    }

    /**
     * 通用Toast弹窗
     */
    private void showToast(final String content) {
        if (isFinishing() || isDestroyed()) {
            LogUtils.w(TAG, "页面已销毁，取消Toast");
            return;
        }
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 新增位置按钮事件
     */
    public void addNewPosition(final View view) {
        LogUtils.d(TAG, "addNewPosition invoke");
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        if (!isServiceBound.get() || mMainService == null) {
            LogUtils.w(TAG, "服务未绑定，无法新增");
            showToast("服务未就绪，无法新增位置");
            return;
        }

        final PositionModel newPos = new PositionModel();
        newPos.setPositionId(PositionModel.genPositionId());
        if (mCurrentGpsPos != null) {
            newPos.setLongitude(mCurrentGpsPos.getLongitude());
            newPos.setLatitude(mCurrentGpsPos.getLatitude());
            newPos.setMemo("当前GPS位置（可编辑）");
        } else {
            newPos.setLongitude(116.404267);
            newPos.setLatitude(39.915119);
            newPos.setMemo("默认位置（可编辑备注）");
        }
        newPos.setIsSimpleView(true);
        newPos.setIsEnableRealPositionDistance(true);

        mMainService.addPosition(newPos);
        synchronized (mLocalPosCache) {
            mLocalPosCache.add(newPos);
        }
        LogUtils.d(TAG, "新增位置成功，ID : " + newPos.getPositionId());

        if (isAdapterInited.get() && mPositionAdapter != null) {
            mPositionAdapter.notifyItemInserted(mLocalPosCache.size() - 1);
        }
        showToast("新增位置成功（已启用GPS距离计算）");
    }

    /**
     * 初始化GPS监听实例
     */
    private void initGpsUpdateListener() {
        LogUtils.d(TAG, "initGpsUpdateListener invoke");
        mGpsUpdateListener = new MainService.GpsUpdateListener() {
            @Override
            public void onGpsPositionUpdated(final PositionModel currentGpsPos) {
                if (currentGpsPos == null || isFinishing() || isDestroyed()) {
                    return;
                }
                mCurrentGpsPos = currentGpsPos;
                LogUtils.d(TAG, "GPS更新 -> 纬度:" + currentGpsPos.getLatitude() + " 经度:" + currentGpsPos.getLongitude());

                final TextView tvLat = (TextView) findViewById(R.id.tv_latitude);
                final TextView tvLon = (TextView) findViewById(R.id.tv_longitude);
                final TextView tvTime = (TextView) findViewById(R.id.tv_timenow);

                tvLat.setText(String.format("当前纬度：%f", currentGpsPos.getLatitude()));
                tvLon.setText(String.format("当前经度：%f", currentGpsPos.getLongitude()));

                final long currentTime = System.currentTimeMillis();
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                final String timeStr = sdf.format(new Date(currentTime));
                tvTime.setText("现在时间：" + timeStr);
            }

            @Override
            public void onGpsStatusChanged(final String status) {
                if (status == null || isFinishing() || isDestroyed()) {
                    return;
                }
                LogUtils.d(TAG, "GPS状态变更 : " + status);
                if (status.contains("未开启") || status.contains("权限") || status.contains("失败")) {
                    ToastUtils.show("GPS提示：" + status);
                }
            }
        };
    }

    /**
     * 刷新GPS监听源（根据空转状态自动切换）
     */
    private void refreshGpsListener() {
        LogUtils.d(TAG, "refreshGpsListener invoke");
        // 统一注销旧监听（防止重复注册）
        IdleGpsService.getInstance().unregisterGpsUpdateListener(mGpsUpdateListener);
        if (mMainService != null) {
            try { mMainService.unregisterGpsUpdateListener(mGpsUpdateListener); } catch (Exception e) {}
        }
        // 重新注册正确的监听
        registerGpsListener();
    }

    /**
     * 注册GPS监听
     */
    private void registerGpsListener() {
        LogUtils.d(TAG, "registerGpsListener invoke");
        if (isFinishing() || isDestroyed() || mGpsUpdateListener == null) {
            return;
        }
        try {
            if (App.isAppIdleRunning()) {
                IdleGpsService.getInstance().registerGpsUpdateListener(mGpsUpdateListener);
                LogUtils.d(TAG, "空转GPS监听注册成功");
            } else {
                if (mMainService != null) {
                    mMainService.registerGpsUpdateListener(mGpsUpdateListener);
                    LogUtils.d(TAG, "系统GPS监听注册成功");
                }
            }
        } catch (final Exception e) {
            LogUtils.e(TAG, "GPS注册异常 : " + e.getMessage());
        }
    }

    /**
     * 反注册GPS监听
     */
    private void unregisterGpsListener() {
        LogUtils.d(TAG, "unregisterGpsListener invoke");
        if (mGpsUpdateListener == null) {
            return;
        }
        try {
            // 尝试从空转服务注销
            IdleGpsService.getInstance().unregisterGpsUpdateListener(mGpsUpdateListener);
        } catch (Exception e) {
            // ignore
        }
        try {
            // 尝试从主服务注销
            if (mMainService != null) {
                mMainService.unregisterGpsUpdateListener(mGpsUpdateListener);
            }
        } catch (final Exception e) {
            LogUtils.e(TAG, "GPS反注册异常 : " + e.getMessage());
        }
    }

    // 生命周期方法
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate invoke");
        setContentView(R.layout.activity_location);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					LogUtils.d(TAG, "点击返回导航按钮");
					final Intent intent = new Intent(LocationActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			});

        refreshIdleStatusTitle();
        initView();
        initGpsUpdateListener();
        bindMainService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume invoke");
        refreshIdleStatusTitle();
        refreshGpsListener(); // 根据当前空转状态刷新GPS源

        if (isServiceBound.get() && mMainService != null && !isAdapterInited.get()) {
            syncDataFromMainService();
            initPositionAdapter();
        } else if (isServiceBound.get() && mMainService != null && mPositionAdapter != null) {
            syncDataFromMainService();
            mPositionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause invoke");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy invoke，开始释放资源");

        unregisterGpsListener();

        if (mPositionAdapter != null) {
            mPositionAdapter.release();
            mPositionAdapter = null;
            LogUtils.d(TAG, "Adapter资源释放完成");
        }

        if (isServiceBound.get()) {
            try {
                unbindService(mServiceConnection);
                LogUtils.d(TAG, "服务解绑完成");
            } catch (final IllegalArgumentException e) {
                LogUtils.e(TAG, "解绑异常：服务已提前解绑");
            }
            isServiceBound.set(false);
            mMainService = null;
        }

        synchronized (mLocalPosCache) {
            mLocalPosCache.clear();
        }
        mCurrentGpsPos = null;
        mGpsUpdateListener = null;
        isAdapterInited.set(false);
        LogUtils.d(TAG, "全部资源释放完毕");
    }
}

