package cc.winboll.studio.libappbase.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libappbase.utils.APPUtils;
import cc.winboll.studio.libappbase.utils.ApkSignUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026-01-20 21:20:00
 * @LastEditTime 2026-01-24 18:45:00
 * @Describe 签名显示+正版校验对话框：展示应用签名字节位信息，调用网络接口完成正版合法性校验，实时返回校验结果
 */
public class APPValidationDialog extends Dialog {
    // ===================================== 全局常量 =====================================
    public static final String TAG = "AppValidationDialog";
    // 签名字节位分组大小
    private static final int BIT_GROUP_SIZE = 16;

    // ===================================== 控件与上下文属性 =====================================
    private Context mContext;
    private EditText etSignFingerprint;
    private TextView tvAuthResult;

    // ===================================== 业务入参属性 =====================================
    private String appName;
    private String versionName;
    private String clientSign;
    private String clientHash;

    // ===================================== 构造方法 =====================================
    public APPValidationDialog(Context context, String appName, String versionName) {
        super(context, R.style.DialogStyle);
        this.mContext = context;
        this.appName = appName;
        this.versionName = versionName;
        LogUtils.d(TAG, "AppValidationDialog: 构造方法初始化，入参-> projectName=" + appName + ", versionName=" + versionName);
    }

    // ===================================== 生命周期方法 =====================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: 对话框创建，开始初始化布局与业务逻辑");
        setContentView(R.layout.dialog_sign_get);
        setCancelable(true);
        // 初始化应用签名与哈希
        initSignAndHash();
        // 初始化页面控件
        initView();
        // 执行签名展示与正版校验
        doSignShowAndAuthCheck();
        LogUtils.d(TAG, "onCreate: 对话框初始化流程执行完成");
    }

    // ===================================== 页面与数据初始化方法 =====================================
    /**
     * 初始化页面控件，绑定视图并设置基础属性
     */
    private void initView() {
        LogUtils.d(TAG, "initView: 开始初始化页面控件");
        etSignFingerprint = findViewById(R.id.et_sign_fingerprint);
        tvAuthResult = findViewById(R.id.tv_auth_result);
        // 签名显示框设为只读，方便用户复制
        etSignFingerprint.setEnabled(false);
        // 填充签名字节位信息
        etSignFingerprint.setText(convertSignToBitArrayWithWrap(clientSign));
        LogUtils.d(TAG, "initView: 控件初始化完成，已填充签名字节位信息");
    }

    /**
     * 初始化应用签名与SHA256哈希，调用工具类获取与服务端对齐的参数
     */
    private void initSignAndHash() {
        LogUtils.d(TAG, "initSignAndHash: 开始获取应用签名与SHA256哈希");
        this.clientSign = ApkSignUtils.getApkSignAlignedWithServer(mContext);
        this.clientHash = ApkSignUtils.getApkSHA256Hash(mContext);
        LogUtils.d(TAG, "initSignAndHash: 签名与哈希获取完成-> clientSign=" + clientSign + ", clientHash=" + clientHash);
    }

    // ===================================== 核心业务方法 =====================================
    /**
     * 核心业务：展示签名字节位信息，发起网络正版校验请求
     */
    private void doSignShowAndAuthCheck() {
        LogUtils.d(TAG, "doSignShowAndAuthCheck: 开始执行应用正版合法性校验");
        // 校验签名与哈希非空，避免空参请求
        if (clientSign == null || clientHash == null) {
            String errorMsg = "应用签名或哈希获取失败，无法执行正版校验";
            LogUtils.e(TAG, "doSignShowAndAuthCheck: " + errorMsg);
            tvAuthResult.setTextColor(Color.RED);
            tvAuthResult.setText(errorMsg);
            ToastUtils.show(errorMsg);
            return;
        }
        // 调用网络校验接口
        new APPUtils().checkAPKValidation(
			mContext,
			appName,
			versionName,
			clientSign,
			clientHash,
			new APPUtils.CheckResultCallback() {
				@Override
				public void onResult(boolean isValid, String message) {
					LogUtils.d(TAG, "checkAPKValidation: 校验结果返回-> isValid=" + isValid + ", message=" + message);
					handleAuthResult(isValid, message);
				}
			}
        );
    }

    /**
     * 处理正版校验结果，更新UI并提示用户
     * @param isValid 校验是否通过
     * @param message 服务端返回提示信息
     */
    private void handleAuthResult(boolean isValid, String message) {
        String showMessage;
        if (isValid) {
            showMessage = "< 这是正版的 WinBoLL 应用，请放心使用。 >";
            tvAuthResult.setTextColor(Color.BLUE);
            LogUtils.d(TAG, "handleAuthResult: 正版校验通过，" + showMessage + "，服务端信息：" + message);
        } else {
            showMessage = "< 您使用的可能不是正版的 WinBoLL 应用。 >";
            tvAuthResult.setTextColor(Color.RED);
            LogUtils.e(TAG, "handleAuthResult: 正版校验失败，" + showMessage + "，失败原因：" + message);
        }
        // 更新UI并弹提示
        tvAuthResult.setText(showMessage);
        ToastUtils.show(showMessage);
    }

    // ===================================== 工具方法 =====================================
    /**
     * 签名字符串转0/1比特数组格式：每2个bit加空格，每16位换行，提升可读性
     * @param signStr 原始签名字符串
     * @return 格式化后的比特数字符串，签名字符为空返回空串
     */
    private String convertSignToBitArrayWithWrap(String signStr) {
        LogUtils.d(TAG, "convertSignToBitArrayWithWrap: 开始格式化签名字符串为比特数组");
        if (signStr == null || signStr.isEmpty()) {
            LogUtils.w(TAG, "convertSignToBitArrayWithWrap: 原始签名字符串为空，返回空串");
            return "";
        }
        // 字符转8位补零的二进制字符串
        StringBuilder bitBuilder = new StringBuilder();
        for (char c : signStr.toCharArray()) {
            String bit8 = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            bitBuilder.append(bit8);
        }
        String fullBitStr = bitBuilder.toString();
        LogUtils.d(TAG, "convertSignToBitArrayWithWrap: 签名转二进制完成，总长度=" + fullBitStr.length() + "bit");

        // 按16位分组，组内每2bit加空格，分组后换行
        StringBuilder finalBuilder = new StringBuilder();
        for (int i = 0; i < fullBitStr.length(); i += BIT_GROUP_SIZE) {
            int end = Math.min(i + BIT_GROUP_SIZE, fullBitStr.length());
            String group = fullBitStr.substring(i, end);
            // 组内加空格
            StringBuilder groupWithSpace = new StringBuilder();
            for (int j = 0; j < group.length(); j++) {
                groupWithSpace.append(group.charAt(j));
                if ((j + 1) % 2 == 0 && j != group.length() - 1) {
                    groupWithSpace.append(" ");
                }
            }
            finalBuilder.append(groupWithSpace);
            // 最后一组不换行
            if (end < fullBitStr.length()) {
                finalBuilder.append("\n");
            }
        }
        LogUtils.d(TAG, "convertSignToBitArrayWithWrap: 签名比特数组格式化完成");
        return finalBuilder.toString();
    }
}

