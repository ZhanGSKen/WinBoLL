package cc.winboll.studio.contacts.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.adapters.ContactAdapter;
import cc.winboll.studio.contacts.model.ContactModel;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/30 14:32
 * @Describe 联系人区域视图（支持懒加载，仅切换到当前页才加载数据）
 */
public class ContactsFragment extends Fragment {

    // ====================== 常量定义区 ======================
    public static final String TAG = "ContactsFragment";
    private static final String ARG_PAGE = "ARG_PAGE";
    private static final int REQUEST_READ_CONTACTS = 1;
    private static final long DEBOUNCE_DELAY = 300; // 搜索防抖延迟

    // ====================== 静态缓存区 ======================
    // 全局复用联系人数据，减少重复查询
    private static List<ContactModel> sCachedOriginalList = new ArrayList<ContactModel>();
    private static List<ContactModel> sCachedFilteredList = new ArrayList<ContactModel>();

    // ====================== 页面参数区 ======================
    private int mPage;
    private boolean isViewInitialized = false; // 视图初始化标记（控件绑定完成）
    private boolean isDataLoaded = false;     // 数据加载标记（数据+功能初始化完成）
    private boolean isLazyInitCompleted = false; // 懒加载总标记（供MainActivity判断）

    // ====================== UI控件区 ======================
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private EditText searchEditText;
    private Button btnDial;

    // ====================== 数据容器区 ======================
    private List<ContactModel> contactList = new ArrayList<ContactModel>();
    private List<ContactModel> originalContactList = new ArrayList<ContactModel>();

    // ====================== 异步工具区 ======================
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ====================== 实例化函数区 ======================
    public static ContactsFragment newInstance(int page) {
        LogUtils.d(TAG, "newInstance: 创建联系人Fragment实例，页码=" + page);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ContactsFragment fragment = new ContactsFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView: 加载Fragment布局");
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.d(TAG, "onViewCreated: 开始初始化UI控件（仅绑定，不加载数据/功能）");
        // 初始化RecyclerView（仅绑定控件、设适配器，隐藏列表）
        recyclerView = (RecyclerView) view.findViewById(R.id.contacts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactAdapter = new ContactAdapter(getActivity(), contactList);
        recyclerView.setAdapter(contactAdapter);
        recyclerView.setVisibility(View.GONE);

        // 绑定搜索框和拨号按钮（仅赋值，不显示、不绑定事件）
        searchEditText = (EditText) view.findViewById(R.id.search_edit_text);
        btnDial = (Button) view.findViewById(R.id.btn_dial);
        searchEditText.setVisibility(View.GONE);
        btnDial.setVisibility(View.GONE);

        // 标记视图控件绑定完成
        isViewInitialized = true;
        LogUtils.d(TAG, "onViewCreated: UI控件初始化完成（未加载数据/功能）");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume: Fragment进入前台");
        // 已完成懒加载 → 仅恢复缓存数据（切回页面时刷新）
        if (isLazyInitCompleted && isDataLoaded) {
            LogUtils.d(TAG, "onResume: 懒加载已完成，恢复缓存数据");
            contactList.clear();
            contactList.addAll(sCachedFilteredList);
            contactAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
        }
        // 未完成懒加载 → 不操作（等待MainActivity调用initData触发）
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: Fragment开始销毁");
        executor.shutdown(); // 关闭线程池
        mainHandler.removeCallbacksAndMessages(null); // 清空Handler任务
        // 释放本地数据引用（保留静态缓存，全局复用）
        if (contactList != null) {
            contactList.clear();
            contactList = null;
        }
        if (originalContactList != null) {
            originalContactList.clear();
            originalContactList = null;
        }
        // 重置标记
        isViewInitialized = false;
        isDataLoaded = false;
        isLazyInitCompleted = false;
        LogUtils.d(TAG, "onDestroy: 异步工具+本地资源已释放");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.d(TAG, "onHiddenChanged: Fragment隐藏状态变更，hidden=" + hidden);
        // 已完成懒加载+显示状态 → 恢复缓存数据（兼容Tab切换场景）
        if (!hidden && isLazyInitCompleted && isDataLoaded) {
            contactList.clear();
            contactList.addAll(sCachedFilteredList);
            contactAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            LogUtils.d(TAG, "onHiddenChanged: 恢复缓存数据，列表已显示");
        }
    }

    // ====================== 权限相关函数区 ======================
    private void checkContactPermission() {
        LogUtils.d(TAG, "checkContactPermission: 检查联系人读取权限");
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.w(TAG, "checkContactPermission: 权限未授予，发起申请");
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            LogUtils.d(TAG, "checkContactPermission: 权限已授予，开始加载数据");
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.d(TAG, "onRequestPermissionsResult: 权限回调触发，requestCode=" + requestCode);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtils.d(TAG, "onRequestPermissionsResult: 联系人权限授予成功");
                loadContacts();
            } else {
                LogUtils.e(TAG, "onRequestPermissionsResult: 联系人权限被拒绝");
                ToastUtils.show("请授予联系人权限以查看联系人列表");
                recyclerView.setVisibility(View.VISIBLE);
                // 权限拒绝也标记懒加载完成（避免重复触发）
                isLazyInitCompleted = true;
            }
        }
    }

    // ====================== 懒加载核心方法（供MainActivity调用） ======================
    public void initData() {
        // 双重防护：避免重复初始化（标记+视图就绪判断）
        if (isLazyInitCompleted || !isViewInitialized || getContext() == null) {
            LogUtils.d(TAG, "initData: 懒加载已完成/视图未就绪，跳过");
            return;
        }
        LogUtils.d(TAG, "initData: 开始懒加载初始化（功能+数据）");
        // 1. 初始化搜索、拨号功能（原onResume首次进入逻辑迁移至此）
        initSearchAndDial();
        // 2. 检查权限+加载数据（原onResume首次进入逻辑迁移至此）
        checkContactPermission();
        // 标记懒加载总流程完成（无论权限是否授予，仅执行一次）
        isLazyInitCompleted = true;
        LogUtils.d(TAG, "initData: 懒加载初始化流程启动完成");
    }

    // ====================== UI功能初始化区 ======================
    private void initSearchAndDial() {
        LogUtils.d(TAG, "initSearchAndDial: 初始化搜索和拨号功能");
        // 显示控件
        searchEditText.setVisibility(View.VISIBLE);
        btnDial.setVisibility(View.VISIBLE);

        // 搜索防抖监听
        searchEditText.addTextChangedListener(new DebounceTextWatcher(DEBOUNCE_DELAY) {
				@Override
				public void onDebounceTextChanged(String query) {
					filterContacts(query);
				}
			});

        // 拨号按钮点击事件
        btnDial.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String phoneNumber = searchEditText.getText().toString().replaceAll("\\s", "");
					if (phoneNumber.isEmpty()) {
						ToastUtils.show("请输入号码");
						return;
					}
					LogUtils.d(TAG, "initSearchAndDial: 发起拨号，号码=" + phoneNumber);
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			});
        LogUtils.d(TAG, "initSearchAndDial: 功能初始化完成");
    }

    // ====================== 数据加载与处理区 ======================
    private void loadContacts() {
        // 优先使用缓存数据（保留原有缓存逻辑，提升性能）
        if (!sCachedOriginalList.isEmpty() && !sCachedFilteredList.isEmpty()) {
            LogUtils.d(TAG, "loadContacts: 存在缓存数据，直接复用");
            originalContactList.clear();
            originalContactList.addAll(sCachedOriginalList);
            contactList.clear();
            contactList.addAll(sCachedFilteredList);
            contactAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            isDataLoaded = true;
            return;
        }

        // 无缓存时异步加载（保留原有异步逻辑，避免主线程阻塞）
        if (!isDataLoaded) {
            LogUtils.d(TAG, "loadContacts: 无缓存，异步读取联系人数据");
            recyclerView.setVisibility(View.GONE);
            executor.execute(new Runnable() {
					@Override
					public void run() {
						final List<ContactModel> tempList = readContactsInBackground();
						// 主线程更新UI和缓存
						mainHandler.post(new Runnable() {
								@Override
								public void run() {
									sCachedOriginalList.clear();
									sCachedOriginalList.addAll(tempList);
									sCachedFilteredList.clear();
									sCachedFilteredList.addAll(tempList);

									originalContactList.clear();
									originalContactList.addAll(sCachedOriginalList);
									contactList.clear();
									contactList.addAll(sCachedFilteredList);

									contactAdapter.notifyDataSetChanged();
									recyclerView.setVisibility(View.VISIBLE);
									isDataLoaded = true;

									LogUtils.d(TAG, "loadContacts: 联系人数据加载完成，共" + contactList.size() + "条");
								}
							});
					}
				});
        }
    }

    private List<ContactModel> readContactsInBackground() {
        LogUtils.d(TAG, "readContactsInBackground: 子线程读取联系人");
        List<ContactModel> tempList = new ArrayList<ContactModel>();
        Cursor cursor = null;
        try {
            cursor = requireContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                do {
                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex).replaceAll("\\s", "");
                    tempList.add(new ContactModel(name, number));
                } while (cursor.moveToNext());
                LogUtils.d(TAG, "readContactsInBackground: 成功读取" + tempList.size() + "条联系人数据");
            } else {
                LogUtils.w(TAG, "readContactsInBackground: 未读取到联系人数据");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "readContactsInBackground: 读取联系人异常", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                LogUtils.d(TAG, "readContactsInBackground: 游标已关闭");
            }
        }
        return tempList;
    }

    private void filterContacts(String query) {
        LogUtils.d(TAG, "filterContacts: 搜索过滤，关键词=" + query);
        contactList.clear();
        sCachedFilteredList.clear();
        if (query.isEmpty()) {
            contactList.addAll(originalContactList);
            sCachedFilteredList.addAll(originalContactList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ContactModel contact : originalContactList) {
                boolean matchName = contact.getName().toLowerCase().contains(lowerQuery);
                boolean matchPinyin = contact.getPinyin().toLowerCase().contains(lowerQuery);
                boolean matchFirstLetter = contact.getPinyinFirstLetter().toLowerCase().contains(lowerQuery);
                boolean matchNumber = contact.getNumber().contains(lowerQuery);
                if (matchName || matchPinyin || matchFirstLetter || matchNumber) {
                    contactList.add(contact);
                }
            }
            sCachedFilteredList.addAll(contactList);
        }
        contactAdapter.notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);
        LogUtils.d(TAG, "filterContacts: 过滤完成，显示" + contactList.size() + "条数据");
    }

    // ====================== 内部防抖监听类 ======================
    public abstract static class DebounceTextWatcher implements TextWatcher {
        private final long debounceDelay;
        private Handler handler = new Handler(Looper.getMainLooper());
        private Runnable pendingRunnable;

        public DebounceTextWatcher(long debounceDelay) {
            this.debounceDelay = debounceDelay;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            if (pendingRunnable != null) {
                handler.removeCallbacks(pendingRunnable);
            }
            pendingRunnable = new Runnable() {
                @Override
                public void run() {
                    onDebounceTextChanged(s.toString());
                }
            };
            handler.postDelayed(pendingRunnable, debounceDelay);
        }

        @Override
        public void afterTextChanged(Editable s) {}

        public abstract void onDebounceTextChanged(String query);
    }
}

