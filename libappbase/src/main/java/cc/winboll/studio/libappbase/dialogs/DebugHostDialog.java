package cc.winboll.studio.libappbase.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;
import cc.winboll.studio.libappbase.ToastUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/22 20:59
 * @Describe WinBoLL服务器地址设置对话框（调试模式专用）
 */
public class DebugHostDialog extends Dialog implements View.OnClickListener {
    public static final String TAG = "DebugHostDialog";

    private Context mContext;
    private EditText etHostInput;
    private Button btnConfirm;
    private Button btnCancel;

    // 构造方法（适配默认样式）
    public DebugHostDialog(Context context) {
        super(context, R.style.DialogStyle);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_winboll_host); // 绑定XML布局
        setCancelable(true); // 点击外部可关闭
        initView();
        initData();
        LogUtils.d(TAG, "DebugHostDialog 初始化完成");
    }

    // 初始化视图
    private void initView() {
        etHostInput = findViewById(R.id.et_host_input);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);

        // 绑定点击事件
        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    // 初始化数据（显示当前已保存的地址）
    private void initData() {
        String currentHost = GlobalApplication.getWinbollHost();
        if (!TextUtils.isEmpty(currentHost)) {
            etHostInput.setText(currentHost);
            etHostInput.setSelection(currentHost.length()); // 光标定位到末尾
            LogUtils.d(TAG, "当前已保存的服务器地址：" + currentHost);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_confirm) {
            handleConfirm(); // 确认设置
        } else if (id == R.id.btn_cancel) {
            dismiss(); // 取消对话框
        }
    }

    // 处理确认设置逻辑
    private void handleConfirm() {
        String inputHost = etHostInput.getText().toString().trim();
        if (TextUtils.isEmpty(inputHost)) {
            ToastUtils.show("服务器地址不能为空");
            LogUtils.w(TAG, "设置失败：地址为空");
            return;
        }

        // 简单校验URL格式（避免明显错误）
        if (!inputHost.startsWith("http://") && !inputHost.startsWith("https://")) {
            ToastUtils.show("地址需以http://或https://开头");
            LogUtils.w(TAG, "设置失败：地址格式错误，input=" + inputHost);
            return;
        }

        // 保存地址到SP+内存
        GlobalApplication.setWinbollHost(inputHost);
        ToastUtils.show("服务器地址设置成功");
        LogUtils.d(TAG, "服务器地址设置成功：" + inputHost);
        dismiss(); // 关闭对话框
    }
}

