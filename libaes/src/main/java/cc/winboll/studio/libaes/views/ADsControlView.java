package cc.winboll.studio.libaes.views;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.enums.ADsMode;
import cc.winboll.studio.libaes.enums.PrivacyAgreeStatus;
import cc.winboll.studio.libaes.utils.WebUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import com.miui.zeus.mimo.sdk.MimoCustomController;
import com.miui.zeus.mimo.sdk.MimoLocation;
import com.miui.zeus.mimo.sdk.MimoSdk;
import java.lang.reflect.Field;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/26 17:51
 * @LastEditTime 2026/01/08 11:00:00 HKT
 * @Describe 广告模式控制控件（Java 7 兼容，云宝物语模式）
 * 核心修改：将PopupMenu锚点绑定到view_popmenu_anchor_point控件，菜单精准显示在锚点位置
 */
public class ADsControlView extends LinearLayout {
    public static final String TAG = "ADsControlView";

    // SP存储配置
    private static final String SP_NAME = "ads_control_config";
    private static final String KEY_SELECTED_MODE = "selected_ads_mode";
    ADsMode mADsMode;
    private static final String PRIVACY_VALUE = "privacy_value";
    PrivacyAgreeStatus mPrivacyAgreeStatus;

    // Handler消息标识
    private static final int MSG_UPDATE_MODE = 1001;

    // 控件引用
    private RadioGroup rgADsMode;
    private RadioButton rbStandalone;
    private RadioButton rbMimoSDK;
    private RadioButton rbStoreQrcode;
    private RelativeLayout rlWinbollStore;
    private ImageView ivWinbollStoreQrcode;
    // 新增：锚点控件引用
    private TextView viewPopmenuAnchorPoint;

    // 外部监听、SP实例、Handler实例
    private OnAdsModeSelectedListener listener;
    private SharedPreferences sharedPreferences;
    private InternalHandler mHandler;
    private Context mContext;

    // 静态列表：存储所有已创建的控件实例
    private static final java.util.List<ADsControlView> sControlViews = new java.util.ArrayList<ADsControlView>();

    // 常量定义
    private static final String WECHAT_STORE_URL = "https://store.weixin.qq.com/shop/b/XhrPkZgoeHo4zug";
    private static final int MENU_ITEM_OPEN_STORE = 1001;

    // 构造方法（Java 7 兼容）
    public ADsControlView(Context context) {
        super(context);
        initView(context);
    }

    public ADsControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @SuppressWarnings("deprecation")
    public ADsControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setPrivacyAgreeStatus(PrivacyAgreeStatus privacyAgreeStatus) {
        this.mPrivacyAgreeStatus = privacyAgreeStatus;
        sharedPreferences.edit().putString(PRIVACY_VALUE, this.mPrivacyAgreeStatus.name()).apply();
    }

    public PrivacyAgreeStatus getPrivacyAgreeStatus() {
        String privacyAgreeStatusStr = sharedPreferences.getString(PRIVACY_VALUE, PrivacyAgreeStatus.UN_SIGNED.name());
        PrivacyAgreeStatus privacyAgreeStatus = PrivacyAgreeStatus.fromString(privacyAgreeStatusStr);
        return privacyAgreeStatus;
    }

    public void setADsMode(ADsMode mADsMode) {
        this.mADsMode = mADsMode;
        sharedPreferences.edit().putString(KEY_SELECTED_MODE, this.mADsMode.name()).apply();
        updateStoreQrcodeLayoutVisibility(mADsMode);
    }

    public ADsMode getADsMode() {
        String savedModeStr = sharedPreferences.getString(KEY_SELECTED_MODE, ADsMode.STANDALONE.name());
        mADsMode = ADsMode.fromValue(savedModeStr);
        return mADsMode;
    }

    /**
     * 初始化视图、SP、Handler
     */
    private void initView(final Context context) {
        this.mContext = context;

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_adscontrol, this, true);

        // 初始化SP
        sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

        // 绑定控件
        rgADsMode = (RadioGroup) findViewById(R.id.rg_ads_mode);
        rbStandalone = (RadioButton) findViewById(R.id.rb_standalone);
        rbMimoSDK = (RadioButton) findViewById(R.id.rb_mimo_sdk);
        rbStoreQrcode = (RadioButton) findViewById(R.id.rb_store_qrcode);
        rlWinbollStore = (RelativeLayout) findViewById(R.id.rl_winboll_store);
        ivWinbollStoreQrcode = (ImageView) findViewById(R.id.iv_winboll_store);
        // 绑定锚点控件
        viewPopmenuAnchorPoint = (TextView) findViewById(R.id.view_popmenu_anchor_point);

        // 初始化Handler
        mHandler = new InternalHandler(Looper.getMainLooper());

        // 核心修改：初始化图片的点击和长按事件（锚点改为view_popmenu_anchor_point）
        initImageViewClickAndLongClick();

        // 注册控件实例
        registerControlView(this);

        // 从SP读取初始模式
        setSelectedMode(getADsMode());

        // 单选组选择事件监听
        rgADsMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (checkedId == R.id.rb_standalone) {
						setADsMode(ADsMode.STANDALONE);
						if (listener != null) listener.onModeSelected(ADsMode.STANDALONE);
					} else if (checkedId == R.id.rb_mimo_sdk) {
						handlePrivacyLogic((Activity) context, PrivacyAgreeStatus.UN_SIGNED, new OnPrivacyChangeListener() {
								@Override
								public void onAgreePrivacy() {
									setADsMode(ADsMode.MIMO_SDK);
									if (listener != null) listener.onModeSelected(ADsMode.MIMO_SDK);
								}

								@Override
								public void onDisagreePrivacy() {
									setADsMode(ADsMode.STANDALONE);
									setSelectedMode(ADsMode.STANDALONE);
									if (listener != null) listener.onModeSelected(ADsMode.STANDALONE);
								}
							});
					} else if (checkedId == R.id.rb_store_qrcode) {
						setADsMode(ADsMode.STORE_QRCODE);
						if (listener != null) listener.onModeSelected(ADsMode.STORE_QRCODE);
					}
				}
			});
    }

    /**
     * 初始化图片的点击和长按事件
     * 核心：将PopupMenu锚点绑定到view_popmenu_anchor_point控件
     */
    private void initImageViewClickAndLongClick() {
        if (ivWinbollStoreQrcode == null || viewPopmenuAnchorPoint == null) {
            LogUtils.e(TAG, "initImageViewClickAndLongClick: 控件引用为空");
            return;
        }

        // 1. 点击事件：简化为提示信息
        ivWinbollStoreQrcode.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ToastUtils.show("长按图片可打开微信小店");
					LogUtils.d(TAG, "图片点击：提示用户长按打开微信小店");
				}
			});

        // 2. 长按事件：锚点改为view_popmenu_anchor_point
        ivWinbollStoreQrcode.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					// 计算锚点控件的屏幕坐标（用于菜单位置微调）
					int[] anchorLocation = new int[2];
					viewPopmenuAnchorPoint.getLocationOnScreen(anchorLocation);
					final int anchorX = anchorLocation[0];
					final int anchorY = anchorLocation[1];

					// 创建PopupMenu，锚点绑定到view_popmenu_anchor_point
					PopupMenu popupMenu = new PopupMenu(mContext, viewPopmenuAnchorPoint);
					// 设置菜单重力：相对锚点居中显示
					popupMenu.setGravity(Gravity.CENTER);

					Menu menu = popupMenu.getMenu();
					menu.add(Menu.NONE, MENU_ITEM_OPEN_STORE, Menu.NONE, "打开微信小店");

					// 设置菜单点击事件
					popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								if (item.getItemId() == MENU_ITEM_OPEN_STORE) {
									WebUtils.openUrlInBrowser(mContext, WECHAT_STORE_URL);
									return true;
								}
								return false;
							}
						});

					try {
						// 反射获取PopupWindow，微调菜单位置（可选）
						Field popupField = PopupMenu.class.getDeclaredField("mPopup");
						popupField.setAccessible(true);
						Object popupObject = popupField.get(popupMenu);
						if (popupObject instanceof PopupWindow) {
							final PopupWindow popupWindow = (PopupWindow) popupObject;
							popupWindow.setAnimationStyle(0); // 关闭默认动画

							// 延迟微调菜单位置（确保布局测量完成）
							new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
									@Override
									public void run() {
										int menuX = anchorX + viewPopmenuAnchorPoint.getWidth() / 2 - popupWindow.getWidth() / 2;
										int menuY = anchorY + viewPopmenuAnchorPoint.getHeight() / 2 - popupWindow.getHeight() / 2;
										if (!popupWindow.isShowing()) {
											popupWindow.showAtLocation(viewPopmenuAnchorPoint, Gravity.NO_GRAVITY, menuX, menuY);
										}
									}
								}, 30);
						}
					} catch (NoSuchFieldException | IllegalAccessException e) {
						LogUtils.e(TAG, "反射获取PopupWindow失败", e);
					}

					// 显示菜单
					popupMenu.show();
					LogUtils.d(TAG, "长按图片，菜单锚点为view_popmenu_anchor_point");
					return true;
				}
			});

        // 设置控件可交互标识
        ivWinbollStoreQrcode.setClickable(true);
        ivWinbollStoreQrcode.setFocusable(true);
        ivWinbollStoreQrcode.setLongClickable(true);
        viewPopmenuAnchorPoint.setClickable(false); // 锚点控件不可点击
        viewPopmenuAnchorPoint.setLongClickable(false);
    }

    /**
     * 从ImageView中提取Bitmap（保留方法，无实际调用）
     */
    private Bitmap getBitmapFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    /**
     * 压缩Bitmap（备用方法，无实际调用）
     */
    private Bitmap compressBitmapBySize(Bitmap src, int maxWidth, int maxHeight) {
        if (src == null) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    /**
     * 计算Bitmap采样率（备用方法，无实际调用）
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 从ImageView反射获取资源ID（备用方法，无实际调用）
     */
    private int getResIdFromImageView(ImageView imageView) {
        try {
            Field field = ImageView.class.getDeclaredField("mSrcResource");
            field.setAccessible(true);
            return field.getInt(imageView);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LogUtils.e(TAG, "getResIdFromImageView: 反射失败", e);
            return 0;
        }
    }

    /**
     * 更新二维码布局显示状态
     */
    private void updateStoreQrcodeLayoutVisibility(ADsMode mode) {
        if (rlWinbollStore == null) return;
        rlWinbollStore.setVisibility(mode == ADsMode.STORE_QRCODE ? View.VISIBLE : View.GONE);
    }

    /**
     * 清理SP中的隐私协议状态
     */
    public static void cleanPrivacyStatus(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "cleanPrivacyStatus: Context is null");
            return;
        }
        SharedPreferences sp = getPrivacySharedPreferences(context);
        sp.edit().remove(PRIVACY_VALUE).apply();
        LogUtils.i(TAG, "隐私协议状态清理成功");
        ToastUtils.show("隐私协议状态已清理");
    }

    /**
     * 获取隐私协议SP实例
     */
    private static SharedPreferences getPrivacySharedPreferences(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            return appContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return context.getSharedPreferences(PRIVACY_VALUE, Context.MODE_PRIVATE);
    }

    /**
     * 处理隐私协议逻辑
     */
    private static void handlePrivacyLogic(final Activity activity, PrivacyAgreeStatus privacyAgreeStatus, final OnPrivacyChangeListener onPrivacyChangeListener) {
        if (privacyAgreeStatus == PrivacyAgreeStatus.REJECTED) {
            Toast.makeText(activity.getApplicationContext(), "已拒绝隐私协议，广告已处于不可用状态", Toast.LENGTH_SHORT).show();
            return;
        } else if (privacyAgreeStatus == PrivacyAgreeStatus.AGREED) {
            initMimoSdkStatic(activity.getApplicationContext());
            return;
        } else {
            AlertDialog dialog = createPrivacyDialog(activity, onPrivacyChangeListener);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                WindowManager m = activity.getWindowManager();
                Display d = m.getDefaultDisplay();
                WindowManager.LayoutParams p = window.getAttributes();
                p.width = d.getWidth();
                window.setAttributes(p);
            }
            dialog.show();
        }
    }

    /**
     * 初始化米盟SDK
     */
    private static void initMimoSdkStatic(Context appContext) {
        if (appContext == null) return;
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
						LogUtils.d(TAG, "米盟SDK初始化成功");
					}

					@Override
					public void fail(int code, String msg) {
						LogUtils.e(TAG, "米盟SDK初始化失败：" + code + ", " + msg);
					}
				});
            MimoSdk.setDebugOn(true);
        } catch (Exception e) {
            LogUtils.e(TAG, "米盟SDK初始化异常", e);
        }
    }

    /**
     * 静态方法：更新SP中的模式
     */
    public static void updateAdsModeByStatic(Context context, ADsMode mode) {
        if (context == null || mode == null) return;
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SELECTED_MODE, mode.name()).apply();
        InternalHandler.sendUpdateModeMessage(mode);
    }

    /**
     * 静态方法：读取SP中的模式
     */
    public static ADsMode getAdsModeFromStatic(Context context) {
        if (context == null) return ADsMode.STANDALONE;
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String savedModeStr = sp.getString(KEY_SELECTED_MODE, ADsMode.STANDALONE.name());
        return ADsMode.fromValue(savedModeStr);
    }

    /**
     * 注册控件实例
     */
    private static void registerControlView(ADsControlView view) {
        synchronized (sControlViews) {
            if (!sControlViews.contains(view)) {
                sControlViews.add(view);
            }
        }
    }

    /**
     * 移除控件实例
     */
    private static void unregisterControlView(ADsControlView view) {
        synchronized (sControlViews) {
            sControlViews.remove(view);
        }
    }

    /**
     * 设置选中模式
     */
    private void setSelectedMode(final ADsMode mode) {
        final ADsMode mode2 = (mode == null) ? ADsMode.STANDALONE : mode;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mode2 == ADsMode.STANDALONE) {
                rbStandalone.setChecked(true);
            } else if (mode2 == ADsMode.MIMO_SDK) {
                rbMimoSDK.setChecked(true);
            } else if (mode2 == ADsMode.STORE_QRCODE) {
                rbStoreQrcode.setChecked(true);
            }
            updateStoreQrcodeLayoutVisibility(mode2);
        } else {
            mHandler.post(new Runnable() {
					@Override
					public void run() {
						setSelectedMode(mode2);
					}
				});
        }
    }

    /**
     * 获取选中模式
     */
    public ADsMode getSelectedMode() {
        int checkedId = rgADsMode.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_mimo_sdk) {
            return ADsMode.MIMO_SDK;
        } else if (checkedId == R.id.rb_store_qrcode) {
            return ADsMode.STORE_QRCODE;
        } else {
            return ADsMode.STANDALONE;
        }
    }

    /**
     * 设置外部监听
     */
    public void setOnAdsModeSelectedListener(OnAdsModeSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * 内部Handler类
     */
    private static class InternalHandler extends Handler {
        static volatile InternalHandler _InternalHandler;

        public InternalHandler(Looper looper) {
            super(looper);
            _InternalHandler = this;
        }

        public static void sendUpdateModeMessage(ADsMode mode) {
            if (mode == null || _InternalHandler == null) return;
            Message msg = _InternalHandler.obtainMessage();
            msg.what = MSG_UPDATE_MODE;
            msg.obj = mode;
            _InternalHandler.sendMessage(msg);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_MODE) {
                ADsMode mode = (ADsMode) msg.obj;
                if (mode == null) return;
                synchronized (sControlViews) {
                    for (ADsControlView view : sControlViews) {
                        if (view != null && view.isShown() && view.isAttachedToWindow()) {
                            view.setSelectedMode(mode);
                            view.updateStoreQrcodeLayoutVisibility(mode);
                        }
                    }
                }
            }
        }
    }

    /**
     * 生命周期：控件销毁
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        unregisterControlView(this);
    }

    /**
     * 外部监听接口
     */
    public interface OnAdsModeSelectedListener {
        void onModeSelected(ADsMode selectedMode);
    }

    /**
     * 隐私协议监听接口
     */
    public interface OnPrivacyChangeListener {
        void onAgreePrivacy();
        void onDisagreePrivacy();
    }

    /**
     * 创建隐私协议对话框
     */
    private static AlertDialog createPrivacyDialog(final Activity activity, final OnPrivacyChangeListener onPrivacyChangeListener) {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_privacy_agreement, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView).setCancelable(false);
        final AlertDialog dialog = builder.create();

        final TextView tvPrivacyUrl = (TextView) dialogView.findViewById(R.id.tv_privacy_url);
        Button btnAgree = (Button) dialogView.findViewById(R.id.btn_agree);
        Button btnDisagree = (Button) dialogView.findViewById(R.id.btn_disagree);

        tvPrivacyUrl.setText(Html.fromHtml("<u>" + tvPrivacyUrl.getText().toString() + "</u>"));
        tvPrivacyUrl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = tvPrivacyUrl.getText().toString().trim();
					ToastUtils.show("隐私协议链接：" + url);
				}
			});
        tvPrivacyUrl.setClickable(true);
        tvPrivacyUrl.setFocusable(true);

        btnAgree.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onPrivacyChangeListener != null) {
						onPrivacyChangeListener.onAgreePrivacy();
					}
					dialog.dismiss();
				}
			});

        btnDisagree.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onPrivacyChangeListener != null) {
						onPrivacyChangeListener.onDisagreePrivacy();
					}
					dialog.dismiss();
				}
			});

        return dialog;
    }
}

