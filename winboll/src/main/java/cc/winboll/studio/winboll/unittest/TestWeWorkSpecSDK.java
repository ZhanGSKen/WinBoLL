package cc.winboll.studio.winboll.unittest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.R;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/03 10:52
 * @Describe 企业微信SDK接口测试（基础调试版）
 * 包含：SDK初始化、基础接口调用、日志输出、主线程回调处理
 */
public class TestWeWorkSpecSDK extends AppCompatActivity implements IWinBoLLActivity, View.OnClickListener {

    public static final String TAG = "TestWeWorkSpecSDK";

    // ------------------- 企业微信SDK配置常量（需替换为实际项目参数） -------------------
    // 企业微信 CorpID（从企业微信管理后台获取）
    private static final String CORP_ID = "wwb37c73f34c722852";
    // 应用 AgentID（从企业微信应用管理后台获取）
    private static final String AGENT_ID = "your_agent_id_here";
    // 应用 Secret（从企业微信应用管理后台获取，注意保密）
    private static final String APP_SECRET = "your_app_secret_here";

    // ------------------- Handler消息标识（主线程处理SDK回调） -------------------
    private static final int MSG_SDK_INIT_SUCCESS = 1001;
    private static final int MSG_SDK_INIT_FAILED = 1002;
    private static final int MSG_GET_CORP_INFO_SUCCESS = 1003;
    private static final int MSG_GET_CORP_INFO_FAILED = 1004;

    // ------------------- 控件声明 -------------------
    private Button mBtnInitSDK;
    private Button mBtnGetCorpInfo;
    private Button mBtnCheckAuth;

    // ------------------- 主线程Handler（处理SDK异步回调） -------------------
    private Handler mWeWorkHandler;


    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_weworkspecsdk);

        // 初始化控件
        initViews();
        // 绑定点击事件
        initEvents();
        // 初始化Handler（主线程处理回调，更新UI）
        initHandler();
        // 初始化SDK（可选：启动时自动初始化，或点击按钮初始化）
        // initWeWorkSDK();
    }

    /**
     * 初始化控件（Java 7 显式绑定）
     */
    private void initViews() {
        mBtnInitSDK = (Button) findViewById(R.id.btn_init_sdk);
        mBtnGetCorpInfo = (Button) findViewById(R.id.btn_get_corp_info);
        mBtnCheckAuth = (Button) findViewById(R.id.btn_check_auth);
    }

    /**
     * 绑定点击事件（Java 7 匿名内部类）
     */
    private void initEvents() {
        mBtnInitSDK.setOnClickListener(this);
        mBtnGetCorpInfo.setOnClickListener(this);
        mBtnCheckAuth.setOnClickListener(this);
    }

    /**
     * 初始化主线程Handler（处理SDK异步回调，安全更新UI）
     */
    private void initHandler() {
        mWeWorkHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SDK_INIT_SUCCESS:
                        showToast("企业微信SDK初始化成功");
                        LogUtils.d(TAG, "SDK初始化成功");
                        break;
                    case MSG_SDK_INIT_FAILED:
                        String initError = (String) msg.obj;
                        showToast("SDK初始化失败：" + initError);
                        LogUtils.e(TAG, "SDK初始化失败：" + initError);
                        break;
                    case MSG_GET_CORP_INFO_SUCCESS:
                        String corpInfo = (String) msg.obj;
                        showToast("获取企业信息成功");
                        LogUtils.d(TAG, "企业信息：" + corpInfo);
                        break;
                    case MSG_GET_CORP_INFO_FAILED:
                        String corpError = (String) msg.obj;
                        showToast("获取企业信息失败：" + corpError);
                        LogUtils.e(TAG, "获取企业信息失败：" + corpError);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    // ------------------- 企业微信SDK核心接口调用 -------------------

    /**
     * 初始化企业微信SDK（异步操作，通过Handler回调结果）
     */
    private void initWeWorkSDK() {
        showToast("开始初始化企业微信SDK...");
        // 模拟SDK异步初始化（实际项目中替换为企业微信SDK的真实初始化接口）
        new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// 真实SDK初始化逻辑示例：
						// WeWorkSDK.init(TestWeWorkSpecSDK.this, CORP_ID, AGENT_ID, new WeWorkSDKCallback() {
						//     @Override
						//     public void onSuccess() {
						//         mWeWorkHandler.sendEmptyMessage(MSG_SDK_INIT_SUCCESS);
						//     }
						//
						//     @Override
						//     public void onFailure(String errorMsg) {
						//         Message msg = Message.obtain();
						//         msg.what = MSG_SDK_INIT_FAILED;
						//         msg.obj = errorMsg;
						//         mWeWorkHandler.sendMessage(msg);
						//     }
						// });

						// 调试模拟：休眠1秒，模拟异步初始化
						Thread.sleep(1000);
						// 模拟初始化成功（如需测试失败，替换为发送MSG_SDK_INIT_FAILED）
						mWeWorkHandler.sendEmptyMessage(MSG_SDK_INIT_SUCCESS);
						// 模拟初始化失败
						// Message msg = Message.obtain();
						// msg.what = MSG_SDK_INIT_FAILED;
						// msg.obj = "CorpID或AgentID错误";
						// mWeWorkHandler.sendMessage(msg);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Message msg = Message.obtain();
						msg.what = MSG_SDK_INIT_FAILED;
						msg.obj = "线程中断：" + e.getMessage();
						mWeWorkHandler.sendMessage(msg);
					}
				}
			}).start();
    }

    /**
     * 获取企业基本信息（异步操作，需先初始化SDK）
     */
    private void getCorpInfo() {
        if (!isSDKInitialized()) {
            showToast("请先初始化SDK");
            return;
        }
        showToast("开始获取企业信息...");
        // 模拟SDK异步获取企业信息（实际项目中替换为真实接口）
        new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// 真实SDK接口示例：
						// WeWorkSDK.getCorpInfo(APP_SECRET, new CorpInfoCallback() {
						//     @Override
						//     public void onSuccess(CorpInfo info) {
						//         Message msg = Message.obtain();
						//         msg.what = MSG_GET_CORP_INFO_SUCCESS;
						//         msg.obj = "企业名称：" + info.getCorpName() + "，企业ID：" + info.getCorpId();
						//         mWeWorkHandler.sendMessage(msg);
						//     }
						//
						//     @Override
						//     public void onFailure(String errorMsg) {
						//         Message msg = Message.obtain();
						//         msg.what = MSG_GET_CORP_INFO_FAILED;
						//         msg.obj = errorMsg;
						//         mWeWorkHandler.sendMessage(msg);
						//     }
						// });

						// 调试模拟：休眠1秒，模拟异步获取
						Thread.sleep(1000);
						// 模拟获取成功
						Message successMsg = Message.obtain();
						successMsg.what = MSG_GET_CORP_INFO_SUCCESS;
						successMsg.obj = "企业名称：WinBoLL Studio，企业ID：" + CORP_ID;
						mWeWorkHandler.sendMessage(successMsg);
						// 模拟获取失败
						// Message failMsg = Message.obtain();
						// failMsg.what = MSG_GET_CORP_INFO_FAILED;
						// failMsg.obj = "AppSecret错误或权限不足";
						// mWeWorkHandler.sendMessage(failMsg);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Message msg = Message.obtain();
						msg.what = MSG_GET_CORP_INFO_FAILED;
						msg.obj = "线程中断：" + e.getMessage();
						mWeWorkHandler.sendMessage(msg);
					}
				}
			}).start();
    }

    /**
     * 检查当前用户是否已授权（同步操作，示例）
     */
    private void checkAuthStatus() {
        if (!isSDKInitialized()) {
            showToast("请先初始化SDK");
            return;
        }
        // 真实SDK接口示例：
        // boolean isAuthorized = WeWorkSDK.isAuthorized();
        // 调试模拟：默认返回true
        boolean isAuthorized = true;

        if (isAuthorized) {
            showToast("用户已授权");
            LogUtils.d(TAG, "当前用户已授权企业微信应用");
        } else {
            showToast("用户未授权，请先授权");
            LogUtils.d(TAG, "当前用户未授权企业微信应用");
            // 真实项目中可调用授权接口：
            // WeWorkSDK.requestAuth(TestWeWorkSpecSDK.this, new AuthCallback() {
            //     @Override
            //     public void onSuccess(String code) {
            //         showToast("授权成功，code：" + code);
            //     }
            //
            //     @Override
            //     public void onFailure(String errorMsg) {
            //         showToast("授权失败：" + errorMsg);
            //     }
            // });
        }
    }

    // ------------------- 工具方法 -------------------

    /**
     * 检查SDK是否已初始化（模拟方法，实际项目中替换为SDK的真实状态检查）
     */
    private boolean isSDKInitialized() {
        // 真实SDK可通过静态方法检查状态：
        // return WeWorkSDK.isInitialized();
        // 调试模拟：假设Handler不为空即表示已初始化
        return mWeWorkHandler != null;
    }

    /**
     * 显示Toast提示（Java 7 简化封装）
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ------------------- 点击事件处理 -------------------

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_init_sdk) {
            initWeWorkSDK();
        } else if (id == R.id.btn_get_corp_info) {
            getCorpInfo();
        } else if (id == R.id.btn_check_auth) {
            checkAuthStatus();
        }
    }

    // ------------------- 生命周期管理 -------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放Handler资源，避免内存泄漏
        if (mWeWorkHandler != null) {
            mWeWorkHandler.removeCallbacksAndMessages(null);
            mWeWorkHandler = null;
        }
        // 真实SDK需调用销毁方法：
        // WeWorkSDK.destroy();
    }
}

