package cc.winboll.studio.libappbase.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;
import cc.winboll.studio.libappbase.utils.NfcRsaAuthTool;

/**
 * @Describe NFC RSA登录认证窗口
 * 核心逻辑：贴近NFC→有密钥显示保存按钮（存应用data区+内存缓存）→无密钥启用初始化按钮（生成私钥写NFC）
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/11 20:34:00
 * @LastEditTime 2026/01/12 16:28:00
 */
public class NfcRsaLoginActivity extends Activity implements View.OnClickListener {
    // 常量定义
    private static final String TAG = "NfcRsaLoginActivity";

    // NFC核心相关属性
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;

    // 视图控件相关属性
    private TextView mTvNfcState;
    private TextView mTvPrivateKey;
    private TextView mTvPublicKey;
    private Button mBtnOptKey; // 复用按钮：有密钥=保存本地，无密钥=初始化密钥

    // 业务相关属性
    private NfcRsaAuthTool mNfcRsaAuthTool;
    private boolean isPreparingInit = false;  // 标记是否准备初始化密钥（替代原写入标记）
    private String mTempPrivateKey;          // 临时存储NFC读取的有效私钥，用于后续保存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_rsa_operate);
        initView();
        initNfcTool();
        initNfcConfig();
        LogUtils.d(TAG, "onCreate: NFC RSA登录窗口初始化完成");
    }

    /**
     * 初始化视图控件，绑定点击事件，默认状态配置
     */
    private void initView() {
        mTvNfcState = findViewById(R.id.tv_nfc_state);
        mTvPrivateKey = findViewById(R.id.tv_private_key);
        mTvPublicKey = findViewById(R.id.tv_public_key);
        mBtnOptKey = findViewById(R.id.btn_create_write_key);

        mBtnOptKey.setOnClickListener(this);
        mBtnOptKey.setEnabled(false);
        mTvNfcState.setText("正在监听NFC卡片，请贴近设备检测密钥...");
        mTvPrivateKey.setText("私钥内容：无");
        mTvPublicKey.setText("公钥内容：无");
        LogUtils.d(TAG, "initView: 视图控件初始化完成，功能按钮默认禁用");
    }

    /**
     * 初始化核心工具类NfcRsaAuthTool，校验NFC基础可用性
     */
    private void initNfcTool() {
        mNfcRsaAuthTool = NfcRsaAuthTool.getInstance(this);
        LogUtils.d(TAG, "initNfcTool: NfcRsaAuthTool单例获取完成");

        if (!mNfcRsaAuthTool.isNfcAvailable()) {
            mTvNfcState.setText("❌ 设备不支持NFC或未开启NFC");
            mBtnOptKey.setEnabled(false);
            Toast.makeText(this, "请先在设置中开启NFC功能", Toast.LENGTH_LONG).show();
            LogUtils.w(TAG, "initNfcTool: NFC不可用，设备不支持或未开启");
        } else {
            LogUtils.d(TAG, "initNfcTool: NFC基础可用性校验通过");
        }
    }

    /**
     * 初始化NFC前台监听配置，页面打开即生效，适配API30
     */
    private void initNfcConfig() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(
			this,
			0,
			new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
			PendingIntent.FLAG_UPDATE_CURRENT
        );
        LogUtils.d(TAG, "initNfcConfig: NFC前台监听配置初始化完成，适配API30");
    }

    /**
     * 核心分发方法：处理NFC相关意图，区分 密钥检测/密钥初始化 逻辑
     * @param intent NFC触发的意图对象
     */
    private void handleNfcIntent(Intent intent) {
        if (mNfcRsaAuthTool == null || !mNfcRsaAuthTool.isNfcAvailable()) {
            LogUtils.w(TAG, "handleNfcIntent: NFC工具类为空或NFC不可用，跳过意图处理");
            return;
        }

        String action = intent.getAction();
        LogUtils.d(TAG, "handleNfcIntent: 收到NFC意图，action=" + action);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
			|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
			|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                LogUtils.d(TAG, "handleNfcIntent: 成功提取NFC Tag对象，当前初始化准备状态=" + isPreparingInit);
                if (isPreparingInit) {
                    createWriteAndValidateKey(tag); // 准备初始化：生成+写NFC
                } else {
                    readAndValidateKey(tag);        // 正常状态：检测NFC密钥
                }
            } else {
                LogUtils.w(TAG, "handleNfcIntent: NFC意图中未提取到有效Tag对象");
            }
        }
    }

    /**
     * 读取NFC中私钥，执行有效性校验，区分场景更新UI（有有效密钥显保存按钮，无则显初始化按钮）
     * @param tag NFC卡片Tag对象
     */
    private void readAndValidateKey(final Tag tag) {
        new Thread(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "readAndValidateKey: 子线程读取NFC私钥，Tag=" + tag);
					final String privateKeyStr = mNfcRsaAuthTool.readPrivateKeyFromNfc(tag);
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (privateKeyStr != null && !privateKeyStr.isEmpty()) {
									// NFC读取到私钥，校验有效性
									boolean priValid = mNfcRsaAuthTool.validatePrivateKey(privateKeyStr);
									String publicKeyStr = mNfcRsaAuthTool.getCachePublicKeyStr();
									boolean pubValid = mNfcRsaAuthTool.validatePublicKey(privateKeyStr, publicKeyStr);

									LogUtils.d(TAG, "readAndValidateKey: 私钥读取完成，有效性=" + priValid + "，公钥提取有效性=" + pubValid);
									if (priValid) {
										mTempPrivateKey = privateKeyStr; // 缓存有效私钥，用于后续保存
										mTvNfcState.setText("✅ NFC检测到有效密钥，点击按钮保存到本地");
										mBtnOptKey.setText("保存密钥到应用本地并缓存");
									} else {
										mTvNfcState.setText("⚠️ NFC私钥无效，点击按钮重新初始化");
										mBtnOptKey.setText("初始化RSA密钥写入NFC");
										mTempPrivateKey = null;
									}
									mTvPrivateKey.setText("私钥内容：\n" + privateKeyStr);
									mTvPublicKey.setText(publicKeyStr != null ? "公钥内容：\n" + publicKeyStr : "公钥内容：提取失败");
									mBtnOptKey.setEnabled(true);
									Toast.makeText(NfcRsaLoginActivity.this, priValid ? "检测到有效密钥" : "密钥无效，请初始化", Toast.LENGTH_SHORT).show();
								} else {
									// NFC无有效私钥，显示初始化按钮
									LogUtils.w(TAG, "readAndValidateKey: NFC中未读取到有效私钥");
									mTvNfcState.setText("❌ NFC无有效RSA私钥，点击按钮初始化");
									mTvPrivateKey.setText("私钥内容：无");
									mTvPublicKey.setText("公钥内容：无");
									mBtnOptKey.setText("初始化RSA密钥写入NFC");
									mBtnOptKey.setEnabled(true);
									mTempPrivateKey = null;
									Toast.makeText(NfcRsaLoginActivity.this, "未检测到有效私钥", Toast.LENGTH_SHORT).show();
								}
								isPreparingInit = false; // 重置初始化标记
								LogUtils.d(TAG, "readAndValidateKey: 私钥检测流程结束，重置初始化准备状态");
							}
						});
				}
			}).start();
    }

    /**
     * 生成RSA私钥、写入NFC、执行密钥有效性校验并更新UI（初始化密钥核心逻辑）
     * @param tag NFC卡片Tag对象
     */
    private void createWriteAndValidateKey(final Tag tag) {
        new Thread(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "createWriteAndValidateKey: 开始创建私钥并写入NFC，Tag=" + tag);
					// 1. 生成RSA私钥
					final String privateKeyStr = mNfcRsaAuthTool.generateRsaPrivateKey();
					if (privateKeyStr == null) {
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mTvNfcState.setText("❌ 私钥生成失败");
									Toast.makeText(NfcRsaLoginActivity.this, "私钥生成失败，请重试", Toast.LENGTH_SHORT).show();
									isPreparingInit = false;
									mBtnOptKey.setEnabled(true);
								}
							});
						LogUtils.e(TAG, "createWriteAndValidateKey: RSA私钥生成失败");
						return;
					}
					LogUtils.d(TAG, "createWriteAndValidateKey: RSA私钥生成成功");

					// 2. 写入NFC卡片
					final boolean writeSuccess = mNfcRsaAuthTool.writePrivateKeyToNfc(tag, privateKeyStr);
					if (!writeSuccess) {
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mTvNfcState.setText("❌ 私钥写入NFC失败");
									Toast.makeText(NfcRsaLoginActivity.this, "私钥写入失败，请重试", Toast.LENGTH_SHORT).show();
									isPreparingInit = false;
									mBtnOptKey.setEnabled(true);
								}
							});
						LogUtils.e(TAG, "createWriteAndValidateKey: 私钥写入NFC失败");
						return;
					}
					LogUtils.d(TAG, "createWriteAndValidateKey: 私钥写入NFC成功");

					// 3. 提取公钥并双重校验有效性
					final String publicKeyStr = mNfcRsaAuthTool.extractPublicKeyFromPrivateKeyStr(privateKeyStr);
					final boolean priValid = mNfcRsaAuthTool.validatePrivateKey(privateKeyStr);
					final boolean pubValid = mNfcRsaAuthTool.validatePublicKey(privateKeyStr, publicKeyStr);

					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								LogUtils.d(TAG, "createWriteAndValidateKey: 密钥校验完成，私钥有效=" + priValid + "，公钥有效=" + pubValid);
								if (priValid && pubValid) {
									mTvNfcState.setText("✅ 密钥初始化成功，已写入NFC");
									mTvPrivateKey.setText("私钥内容：\n" + privateKeyStr);
									mTvPublicKey.setText(publicKeyStr != null ? "公钥内容：\n" + publicKeyStr : "公钥内容：提取失败");
									Toast.makeText(NfcRsaLoginActivity.this, "密钥创建写入成功，可贴近NFC保存本地", Toast.LENGTH_LONG).show();
								} else {
									mTvNfcState.setText("⚠️ 写入成功，但密钥校验失败");
									Toast.makeText(NfcRsaLoginActivity.this, "写入成功但密钥无效，请重新操作", Toast.LENGTH_LONG).show();
								}
								mBtnOptKey.setText("初始化RSA密钥写入NFC");
								mBtnOptKey.setEnabled(true);
								isPreparingInit = false;
								mTempPrivateKey = null;
							}
						});
				}
			}).start();
    }

    /**
     * 保存有效私钥到应用data区，同时缓存到工具类内存属性
     */
    private void saveKeyToLocalAndCache() {
        LogUtils.d(TAG, "saveKeyToLocalAndCache: 开始执行密钥本地保存+内存缓存");
        if (mTempPrivateKey == null || mTempPrivateKey.isEmpty()) {
            Toast.makeText(this, "无有效密钥可保存", Toast.LENGTH_SHORT).show();
            LogUtils.w(TAG, "saveKeyToLocalAndCache: 临时有效私钥为空，保存失败");
            return;
        }
        boolean saveSuccess = mNfcRsaAuthTool.savePrivateKeyToLocal(mTempPrivateKey);
        if (saveSuccess) {
            mTvNfcState.setText("✅ 密钥已保存到应用本地，登录完成");
            mTvPrivateKey.setText("私钥内容：\n" + mNfcRsaAuthTool.getCachePrivateKeyStr());
            mTvPublicKey.setText("公钥内容：\n" + mNfcRsaAuthTool.getCachePublicKeyStr());
            mBtnOptKey.setEnabled(false);
            Toast.makeText(this, "密钥保存成功，已缓存到内存", Toast.LENGTH_LONG).show();
            LogUtils.d(TAG, "saveKeyToLocalAndCache: 密钥本地存储+工具类内存缓存成功");
        } else {
            Toast.makeText(this, "密钥保存到本地失败", Toast.LENGTH_SHORT).show();
            LogUtils.e(TAG, "saveKeyToLocalAndCache: 密钥本地保存失败");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_create_write_key) {
            LogUtils.d(TAG, "onClick: 点击功能按钮，当前按钮文本=" + mBtnOptKey.getText().toString());
            if (!mNfcRsaAuthTool.isNfcAvailable()) {
                Toast.makeText(this, "NFC不可用，无法执行操作", Toast.LENGTH_SHORT).show();
                LogUtils.w(TAG, "onClick: NFC不可用，拒绝按钮操作");
                return;
            }

            if (mBtnOptKey.getText().toString().contains("保存")) {
                // 按钮为保存功能：直接保存本地+缓存
                saveKeyToLocalAndCache();
            } else {
                // 按钮为初始化功能：进入准备状态，等待贴近NFC
                isPreparingInit = true;
                mTvNfcState.setText("请贴近NFC卡片，执行密钥写入...");
                mBtnOptKey.setEnabled(false);
                Toast.makeText(this, "请贴近NFC卡片完成密钥初始化", Toast.LENGTH_SHORT).show();
                LogUtils.d(TAG, "onClick: 已进入密钥初始化准备状态，等待NFC贴近");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null && mNfcRsaAuthTool.isNfcAvailable()) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
            LogUtils.d(TAG, "onResume: NFC前台监听已启用");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
            LogUtils.d(TAG, "onPause: NFC前台监听已禁用");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        LogUtils.d(TAG, "onNewIntent: 收到新NFC意图，分发处理");
        handleNfcIntent(intent);
    }
}

