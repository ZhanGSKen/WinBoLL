package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.BuildConfig;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.models.UserInfoModel;
import cc.winboll.studio.winboll.utils.RSAUtils;
import cc.winboll.studio.winboll.utils.YunUtils;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/04 13:29
 * @Describe 用户登录框
 */
public class LogonActivity extends Activity implements IWinBoLLActivity {

    public static final String TAG = "LogonActivity";

    public static final String DEBUG_HOST = "http://10.8.0.250:456";
    public static final String YUN_HOST = "https://yun.winboll.cc";
    
    
    String mHost = "";
    RadioButton mrbYunHost;
    RadioButton mrbDebugHost;
    LogView mLogView;
    
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
        setContentView(R.layout.activity_logon);
        mLogView = findViewById(R.id.logview);
        mLogView.start();

        mHost = BuildConfig.DEBUG ? DEBUG_HOST: YUN_HOST;
        if (BuildConfig.DEBUG) {
            mrbYunHost = findViewById(R.id.rb_yunhost);
            mrbDebugHost = findViewById(R.id.rb_debughost);
            mrbYunHost.setChecked(!BuildConfig.DEBUG);
            mrbDebugHost.setChecked(BuildConfig.DEBUG);
        } else {
            findViewById(R.id.ll_hostbar).setVisibility(View.GONE);
        }
    }
    
    public void onSwitchHost(View view) {
        if (view.getId() == R.id.rb_yunhost) {
            mrbDebugHost.setChecked(false);
            mHost = YUN_HOST;
        } else if (view.getId() == R.id.rb_debughost) {
            mrbYunHost.setChecked(false);
            mHost = DEBUG_HOST;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }

    public void onTestLogin(View view) {
        LogUtils.d(TAG, "onTestLogin");
        final YunUtils yunUtils = YunUtils.getInstance(this);
        
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setUsername("jian");
        userInfoModel.setPassword("kkiio");
        userInfoModel.setToken("aaa111");
        yunUtils.login(mHost, userInfoModel);
    }

    public void onTestRSA(View view) {
        LogUtils.d(TAG, "onTestRSA");
        RSAUtils utils = RSAUtils.getInstance(this);
        
        try {
            // 测试 1：首次生成密钥对
            LogUtils.d(TAG, "==== 首次生成密钥对 ====");
            if (utils.keysExist()) {
                LogUtils.d(TAG, "密钥对已生成");
            } else {
                utils.generateAndSaveKeys();
                LogUtils.d(TAG, "密钥对生成成功。");
            }

            // 测试 2：获取密钥对（自动读取已生成的文件）
            KeyPair keyPair = utils.getOrGenerateKeys();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 打印密钥信息
            LogUtils.d(TAG, "\n==== 密钥信息 ====");
            LogUtils.d(TAG, "公钥算法：" + publicKey.getAlgorithm());
            LogUtils.d(TAG, "公钥编码长度：" + publicKey.getEncoded().length + "字节");
            LogUtils.d(TAG, "私钥算法：" + privateKey.getAlgorithm());
            LogUtils.d(TAG, "私钥编码长度：" + privateKey.getEncoded().length + "字节");

            // 测试 3：重复调用时检查是否复用文件
            LogUtils.d(TAG, "\n==== 二次调用 ====");
            KeyPair reusedPair = utils.getOrGenerateKeys();
            LogUtils.d(TAG, "是否为同一公钥：" + (publicKey.equals(reusedPair.getPublic()))); // true（单例引用）
            LogUtils.d(TAG, "操作完成");

            String testMessage = "Hello, RSA Encryption!";

            // 1. 获取或生成密钥对
            PublicKey publicKeyReused = reusedPair.getPublic();
            PrivateKey privateKeyReused = reusedPair.getPrivate();

            // 2. 公钥加密
            byte[] encryptedData = utils.encryptWithPublicKey(testMessage, publicKeyReused);
            LogUtils.d(TAG, "加密后数据（字节长度）：" + encryptedData.length);

            // 3. 私钥解密
            String decryptedMessage = utils.decryptWithPrivateKey(encryptedData, privateKeyReused);
            LogUtils.d(TAG, "解密结果: " + decryptedMessage);
            
            // 4. 验证解密是否成功
            if (testMessage.equals(decryptedMessage)) {
                LogUtils.d(TAG, "加密解密测试通过！");
            } else {
                LogUtils.d(TAG, "测试失败：内容不一致");
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }
    

}
