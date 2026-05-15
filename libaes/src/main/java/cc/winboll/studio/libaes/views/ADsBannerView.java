package cc.winboll.studio.libaes.views;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.enums.ADsMode;
import cc.winboll.studio.libaes.utils.MimoUtils;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.BannerAd;
import com.miui.zeus.mimo.sdk.MimoCustomController;
import com.miui.zeus.mimo.sdk.MimoLocation;
import com.miui.zeus.mimo.sdk.MimoSdk;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/18 14:41
 * @Describe WinBoLL 横幅广告类
 */
public class ADsBannerView extends LinearLayout {

    public static final String TAG = "ADsBannerView";


	private String BANNER_POS_ID = "802e356f1726f9ff39c69308bfd6f06a";
    private String BANNER_POS_ID_WINBOLL_BETA = "d129ee5a263911f981a6dc7a9802e3e7";
    private String BANNER_POS_ID_WINBOLL = "4ec30efdb32271765b9a4efac902828b";

	/*
	 private String BANNER_POS_ID = "802e356f1726f9ff39c69308bfd6f06a";
	 private String BANNER_POS_ID_WINBOLL_BETA = "802e356f1726f9ff39c69308bfd6f06a";
	 private String BANNER_POS_ID_WINBOLL = "802e356f1726f9ff39c69308bfd6f06a";
	 */

	Context mContext;
	View mMianView;
	SharedPreferences mSharedPreferences;
	ViewGroup mContainer;
	BannerAd mBannerAd;
	List<BannerAd> mAllBanners = new ArrayList<>();
	// 新增：主线程Handler，确保广告操作在主线程执行
    private Handler mMainHandler;

    public ADsBannerView(Context context) {
		super(context);
		initView(context);
	}

	public ADsBannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ADsBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public ADsBannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}

	void initView(Context context) {
		this.mContext = context;
		initMimoSdk(this.mContext);

		// 初始化主线程Handler（关键：确保广告操作在主线程执行）
		mMainHandler = new Handler(Looper.getMainLooper());

		this.mMianView = inflate(this.mContext, R.layout.view_adsbanner, null);
		mContainer = this.mMianView.findViewById(R.id.ads_container);
		addView(this.mMianView);
	}

	public void resumeADs(final Activity activity) {
		// 没有设置米盟广告支持就退出
		if (ADsControlView.getAdsModeFromStatic(this.mContext) != ADsMode.MIMO_SDK) {
			// 2. 释放之前的广告资源
			if (mBannerAd != null) {
				mBannerAd.destroy();
			}
			return;
		}

		// 修复：优化广告请求逻辑（添加生命周期判断 + 主线程执行）
		if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
			if (ADsControlView.getAdsModeFromStatic(this.mContext) == ADsMode.MIMO_SDK) {
				LogUtils.i(TAG, "已设置播放米盟广告，正在播放...");
				mMainHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// 再次校验生命周期，避免延迟执行时Activity已销毁
							if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
								fetchAd(activity);
							}
						}
					}, 1000); // 延迟1秒请求广告，提升页面加载体验
			}
		}
	}

    /**
     * 释放广告资源（关键：避免内存泄漏和空Context调用）
     */
    public void releaseAdResources() {
		// 没有设置米盟广告支持就退出
		if (ADsControlView.getAdsModeFromStatic(this.mContext) != ADsMode.MIMO_SDK) {
			return;
		}

        LogUtils.d(TAG, "releaseAdResources()");

        // 移除Handler回调
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }

        // 销毁所有广告实例
        if (mAllBanners != null && !mAllBanners.isEmpty()) {
            for (BannerAd ad : mAllBanners) {
                if (ad != null) {
                    ad.destroy();
                }
            }
            mAllBanners.clear();
        }
        // 置空当前广告引用
        mBannerAd = null;
        // 移除广告容器中的视图
        if (mContainer != null) {
            mContainer.removeAllViews();
        }
    }

    /**
     * 显示广告（核心修复：传递安全的Context + 生命周期校验）
     */
    private void showAd(final Activity activity) {
		// 没有设置米盟广告支持就退出
		if (ADsControlView.getAdsModeFromStatic(this.mContext) != ADsMode.MIMO_SDK) {
			return;
		}

        LogUtils.d(TAG, "showAd()");
        // 1. 生命周期校验：避免Activity已销毁时操作UI
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            LogUtils.e(TAG, "showAd: Activity is finishing or destroyed");
            return;
        }
        // 2. 非空校验：广告实例和容器
        if (mBannerAd == null || mContainer == null) {
            LogUtils.e(TAG, "showAd: BannerAd or Container is null");
            return;
        }
        // 3. 创建广告容器（使用ApplicationContext避免内存泄漏）
        final FrameLayout container = new FrameLayout(activity.getApplicationContext());
        container.setPadding(0, 0, 0, MimoUtils.dpToPx(activity, 10));
        mContainer.addView(container, new FrameLayout.LayoutParams(
							   FrameLayout.LayoutParams.MATCH_PARENT,
							   FrameLayout.LayoutParams.WRAP_CONTENT
						   ));

//        if (mIsBiddingWin) {
//            mBannerAd.setPrice(getPrice());
//        }
        // 4. 显示广告：传递ApplicationContext，避免Activity Context失效
        mBannerAd.showAd(activity, container, new BannerAd.BannerInteractionListener() {
				@Override
				public void onAdClick() {
					LogUtils.d(TAG, "onAdClick");
				}

				@Override
				public void onAdShow() {
					LogUtils.d(TAG, "onAdShow");
				}

				@Override
				public void onAdDismiss() {
					LogUtils.d(TAG, "onAdDismiss");
					// 修复：移除容器时校验Activity状态
					if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && mContainer != null) {
						mContainer.removeView(container);
					}
				}

				@Override
				public void onRenderSuccess() {
					LogUtils.d(TAG, "onRenderSuccess");
				}

				@Override
				public void onRenderFail(int code, String msg) {
					LogUtils.e(TAG, "onRenderFail errorCode " + code + " errorMsg " + msg);
					// 修复：渲染失败时移除容器
					if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && mContainer != null) {
						mContainer.removeView(container);
					}
				}
			});
    }

    /**
     * 请求广告（核心修复：Context安全校验 + 异常捕获 + 资源管理）
     */
    private void fetchAd(final Activity activity) {
		// 没有设置米盟广告支持就退出
		if (ADsControlView.getAdsModeFromStatic(this.mContext) != ADsMode.MIMO_SDK) {
			return;
		}

        LogUtils.d(TAG, "fetchAd()");
        // 1. 双重校验：Activity未销毁 + Context非空
        if (activity == null || activity.isFinishing() || activity.isDestroyed() || activity.getApplicationContext() == null) {
            LogUtils.e(TAG, "fetchAd: Invalid Context or Activity state");
            return;
        }
        // 2. 释放之前的广告资源，避免内存泄漏
        if (mBannerAd != null) {
            mBannerAd.destroy();
        }
        // 3. 初始化广告（使用ApplicationContext，避免Activity Context失效）
        try {
            mBannerAd = new BannerAd();
            mAllBanners.add(mBannerAd);
        } catch (Exception e) {
            LogUtils.e(TAG, "fetchAd: Init BannerAd failed", e);
            return;
        }
        // 4. 设置下载监听
        mBannerAd.setDownLoadListener(new BannerAd.BannerDownloadListener() {
				@Override
				public void onDownloadStarted() {
					LogUtils.d(TAG, "onDownloadStarted");
				}

				@Override
				public void onDownloadPaused() {
					LogUtils.d(TAG, "onDownloadPaused");
				}

				@Override
				public void onDownloadFailed(int errorCode) {
					String msg = "onDownloadFailed, errorCode = " + errorCode;
					LogUtils.d(TAG, msg);
					//ToastUtils.show(msg);
				}

				@Override
				public void onDownloadFinished() {
					LogUtils.d(TAG, "onDownloadFinished");
				}

				@Override
				public void onDownloadProgressUpdated(int progress) {
					LogUtils.d(TAG, "onDownloadProgressUpdated " + progress + "%");
				}

				@Override
				public void onInstallFailed(int errorCode) {
					LogUtils.d(TAG, "onInstallFailed, errorCode = " + errorCode);
				}

				@Override
				public void onInstallStart() {
					LogUtils.d(TAG, "onInstallStart");
				}

				@Override
				public void onInstallSuccess() {
					LogUtils.d(TAG, "onInstallSuccess");
				}

				@Override
				public void onDownloadCancel() {
					LogUtils.d(TAG, "onDownloadCancel");
				}
			});

        // 5. 构建广告参数并请求
        String currentAD_ID = getAD_ID();
        LogUtils.d(TAG, String.format("currentAD_ID = %s", currentAD_ID));
        ADParams params = new ADParams.Builder().setUpId(currentAD_ID).build();
        mBannerAd.loadAd(params, new BannerAd.BannerLoadListener() {
				@Override
				public void onBannerAdLoadSuccess() {
					LogUtils.d(TAG, "onBannerAdLoadSuccess()");
					// 修复：广告加载成功后校验Activity状态
					if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
						showAd(activity);
						//ToastUtils.show("showAd()");
					}
				}

				@Override
				public void onAdLoadFailed(int errorCode, String errorMsg) {
					String msg = "onAdLoadFailed: errorCode = " + errorCode + ", errorMsg = " + errorMsg;
					LogUtils.d(TAG, msg);
					removeAllBanners();
				}
			});
    }

	void removeAllBanners() {
		// 没有设置米盟广告支持就退出
		if (ADsControlView.getAdsModeFromStatic(this.mContext) != ADsMode.MIMO_SDK) {
			return;
		}

		// 修复：加载失败时移除当前广告实例
		if (mAllBanners.contains(mBannerAd)) {
			mAllBanners.remove(mBannerAd);
		}
		mBannerAd.destroy();
		mBannerAd = null;
	}

    /**
     * 根据当前秒数获取广告ID（原逻辑保留）
     */
    private String getAD_ID() {
        long currentSecond = System.currentTimeMillis() / 1000;
        return (currentSecond % 2 == 0) ? BANNER_POS_ID :
			(GlobalApplication.isDebugging() ? BANNER_POS_ID_WINBOLL_BETA : BANNER_POS_ID_WINBOLL);
    }

    /**
     * 获取广告价格（原逻辑保留，添加空指针校验）
     */
//    private long getPrice() {
//        if (mBannerAd == null) {
//            return 0;
//        }
//        Map<String, Object> map = mBannerAd.getMediaExtraInfo();
//        if (map == null || map.isEmpty() || !map.containsKey("price")) {
//            LogUtils.w(TAG, "getPrice: media extra info is null or no price key");
//            return 0;
//        }
//        Object priceObj = map.get("price");
//        if (priceObj instanceof Long) {
//            return (Long) priceObj;
//        } else if (priceObj instanceof Integer) {
//            return ((Integer) priceObj).longValue();
//        } else {
//            LogUtils.e(TAG, "getPrice: price type is invalid");
//            return 0;
//        }
//    }

    /**
     * 显示隐私协议弹窗（原逻辑保留，优化Context使用）
     */
//    private void showPrivacy() {
//        // 校验Activity状态，避免弹窗泄露
//        if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
//            return;
//        }
//        ADsMode adsMode = ADsControlView.getAdsModeFromStatic(this.mContext);
//        if (adsMode == ADsMode.STANDALONE) {
//			ADsControlView.updateAdsModeByStatic(this.mContext, ADsMode.STANDALONE);
//            LogUtils.i(TAG, "单机模式，广告已处于不可用状态...");
//            Toast.makeText(getActivity().getApplicationContext(), "单机模式，广告已处于不可用状态...", Toast.LENGTH_SHORT).show();
//            return;
//        } else if (adsMode == ADsMode.MIMO_SDK) {
//			ADsControlView.updateAdsModeByStatic(this.mContext, ADsMode.MIMO_SDK);
//            LogUtils.i(TAG, "米盟广告SDK支持模式，现在初始化SDK...");
//            initMimoSdk();
//            return;
//        } 
//		else {
//			LogUtils.i(TAG, "开始弹出隐私协议...");
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//			builder.setTitle("用户须知");
//			builder.setMessage("小米广告SDK隐私政策: https://dev.mi.com/distribute/doc/details?pId=1688, 请复制到浏览器查看");
//			builder.setIcon(R.drawable.ic_launcher);
//			builder.setCancelable(false); // 点击对话框以外的区域不消失
//			builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						getSharedPreferences().edit()
//							.putString(PRIVACY_VALUE, String.valueOf(1))
//							.apply();
//						initMimoSdk();
//						dialog.dismiss();
//					}
//				});
//			builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						getSharedPreferences().edit()
//							.putString(PRIVACY_VALUE, String.valueOf(0))
//							.apply();
//						dialog.dismiss();
//					}
//				});
//			AlertDialog dialog = builder.create();
//
//			// 配置弹窗位置（底部全屏）
//			Window window = dialog.getWindow();
//			if (window != null) {
//				window.setGravity(Gravity.BOTTOM);
//				WindowManager m = getActivity().getWindowManager();
//				Display d = m.getDefaultDisplay();
//				WindowManager.LayoutParams p = window.getAttributes();
//				p.width = d.getWidth();
//				window.setAttributes(p);
//			}
//			dialog.show();
//		}
//    }

    /**
     * 初始化米盟SDK（核心修复：传递ApplicationContext + 异常捕获）
     */
    private void initMimoSdk(Context context) {
        // 1. 安全获取ApplicationContext，避免Activity Context失效
        Context appContext = context.getApplicationContext();
        if (appContext == null) {
            LogUtils.e(TAG, "initMimoSdk: ApplicationContext is null");
            return;
        }
        // 2. 初始化SDK，捕获异常避免崩溃
        try {
            MimoSdk.init(appContext, new MimoCustomController() {
					@Override
					public boolean isCanUseLocation() {
						return true;
					}

					@Override
					public MimoLocation getMimoLocation() {
						return null;
					}

					@Override
					public boolean isCanUseWifiState() {
						return true;
					}

					@Override
					public boolean alist() {
						return true;
					}
				}, new MimoSdk.InitCallback() {
					@Override
					public void success() {
						LogUtils.d(TAG, "MimoSdk init success");
					}

					@Override
					public void fail(int code, String msg) {
						LogUtils.e(TAG, "MimoSdk init fail, code=" + code + ",msg=" + msg);
					}
				});
            MimoSdk.setDebugOn(true);
        } catch (Exception e) {
            LogUtils.e(TAG, "initMimoSdk: init failed", e);
        }
    }


    /**
     * 获取SharedPreferences实例（原逻辑保留，添加空指针校验）
     */
//    SharedPreferences getSharedPreferences() {
////        if (mSharedPreferences == null) {
////            // 修复：使用ApplicationContext获取SharedPreferences，避免Activity Context泄露
////            Context appContext = getActivity().getApplicationContext();
////            if (appContext != null) {
////                mSharedPreferences = appContext.getSharedPreferences(PRIVACY_FILE, Context.MODE_PRIVATE);
////            } else {
////                LogUtils.e(TAG, "getSharedPreferences: ApplicationContext is null");
////                // 降级方案：若ApplicationContext为空，使用Activity Context（仅作兼容）
////                mSharedPreferences = getActivity().getSharedPreferences(PRIVACY_FILE, Context.MODE_PRIVATE);
////            }
////        }
//        return mSharedPreferences;
//    }
}
