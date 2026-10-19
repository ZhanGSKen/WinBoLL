package cc.winboll.studio.autonfc.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cc.winboll.studio.autonfc.R;
import cc.winboll.studio.autonfc.models.NfcTermuxCmd;
import cc.winboll.studio.libappbase.LogUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NFCInterfaceActivity extends Activity {

    public static final String TAG = "NFCInterfaceActivity";

    private EditText et_script;
    private EditText et_args;
    private EditText et_workDir;
    private EditText et_background;
    private EditText et_resultDir;

    private TextView tvResult;
    private TextView tvStatus;

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private Tag mCurrentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_interface);
        initView();
        initNfc();
    }

    private void initView() {
        et_script = findViewById(R.id.et_script);
        et_args = findViewById(R.id.et_args);
        et_workDir = findViewById(R.id.et_workDir);
        et_background = findViewById(R.id.et_background);
        et_resultDir = findViewById(R.id.et_resultDir);

        tvResult = findViewById(R.id.tv_result);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void initNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            tvStatus.setText("设备不支持NFC");
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            tvStatus.setText("请开启NFC");
            return;
        }

        Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        tvStatus.setText("NFC已启动，等待卡片靠近");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
        mCurrentTag = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCurrentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mCurrentTag == null) return;

        tvStatus.setText("卡片已连接，解析中...");
        readNfc();
    }

    // -------------------------------------------------------------------------
    // 读取 NFC（完全委托给工具类）
    // -------------------------------------------------------------------------
    private void readNfc() {
        try {
            NfcTermuxCmd cmd = NfcUtils.readTag(mCurrentTag);
            if (cmd == null) {
                tvStatus.setText("读取成功：标签为空");
                tvResult.setText("");
                // 清空窗体
                clearUiFields();
                return;
            }

            // 核心改动：读取成功后，同时更新详情显示 和 窗体输入框
            updateUiWithCmd(cmd);

        } catch (Exception e) {
            LogUtils.e(TAG, "readNfc 失败", e);
            tvStatus.setText("读取失败：" + e.getMessage());
            // 出错时清空窗体
            clearUiFields();
        }
    }

    // -------------------------------------------------------------------------
    // 新增：根据读取到的 Cmd 填充 UI（详情 + 窗体）
    // -------------------------------------------------------------------------
    private void updateUiWithCmd(NfcTermuxCmd cmd) {
        if (cmd == null) return;

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        String show = "【读取时间】 " + time + "\n\n"
			+ "【解析结果】\n"
			+ "script:    " + cmd.getScript() + "\n"
			+ "args:      " + (cmd.getArgs() != null ? String.join(", ", cmd.getArgs()) : "[]") + "\n"
			+ "workDir:   " + cmd.getWorkDir() + "\n"
			+ "background: " + cmd.isBackground() + "\n"
			+ "resultDir: " + cmd.getResultDir();

        tvResult.setText(show);
        tvStatus.setText("读取成功！");

        // 👇 关键逻辑：自动填入窗体（每次读取后都会覆盖输入框）
        et_script.setText(cmd.getScript() != null ? cmd.getScript() : "");
        et_args.setText(cmd.getArgs() != null ? String.join(",", cmd.getArgs()) : "");
        et_workDir.setText(cmd.getWorkDir() != null ? cmd.getWorkDir() : "");
        et_background.setText(String.valueOf(cmd.isBackground()));
        et_resultDir.setText(cmd.getResultDir() != null ? cmd.getResultDir() : "");
    }

    // -------------------------------------------------------------------------
    // 辅助：清空所有输入框
    // -------------------------------------------------------------------------
    private void clearUiFields() {
        et_script.setText("");
        et_args.setText("");
        et_workDir.setText("");
        et_background.setText("");
        et_resultDir.setText("");
    }

    // -------------------------------------------------------------------------
    // 写入按钮（委托给工具类）
    // -------------------------------------------------------------------------
    public void onWriteClick(View view) {
        if (mCurrentTag == null) {
            showToast("请先靠近卡片");
            return;
        }

        try {
            NfcTermuxCmd cmd = buildCmdFromUI();
            NfcUtils.writeTag(mCurrentTag, cmd);

            tvStatus.setText("写入成功！");
            showToast("写入成功");
            readNfc(); // 写入后重读，此时会自动填入窗体

        } catch (Exception e) {
            LogUtils.e(TAG, "写入失败", e);
            tvStatus.setText("写入失败：" + e.getMessage());
            showToast("写入失败");
        }
    }

    // -------------------------------------------------------------------------
    // 填充调试数据
    // -------------------------------------------------------------------------
    public void onFillTestDataClick(View view) {
        String testJson = "{\"script\":\"BuildWinBoLLProject.sh\",\"args\":[\"DebugTemp\"],\"workDir\":null,\"background\":true,\"resultDir\":null}";
        try {
            NfcTermuxCmd cmd = NfcUtils.jsonToCmd(testJson);
            et_script.setText(cmd.getScript());
            et_args.setText(cmd.getArgs() != null ? String.join(",", cmd.getArgs()) : "");
            et_workDir.setText(cmd.getWorkDir() != null ? cmd.getWorkDir() : "");
            et_background.setText(String.valueOf(cmd.isBackground()));
            et_resultDir.setText(cmd.getResultDir() != null ? cmd.getResultDir() : "");

            showToast("调试数据已填入");
        } catch (Exception e) {
            showToast("解析失败");
        }
    }

    // -------------------------------------------------------------------------
    // 从 UI 构建 NfcTermuxCmd
    // -------------------------------------------------------------------------
    private NfcTermuxCmd buildCmdFromUI() {
        String script = et_script.getText().toString().trim();
        String argsStr = et_args.getText().toString().trim();
        String workDir = et_workDir.getText().toString().trim();
        String bgStr = et_background.getText().toString().trim();
        String resultDir = et_resultDir.getText().toString().trim();

        NfcTermuxCmd cmd = new NfcTermuxCmd();
        cmd.setScript(script);
        cmd.setArgs(argsStr.isEmpty() ? new String[0] : argsStr.split(","));
        cmd.setWorkDir(workDir.isEmpty() ? null : workDir);
        cmd.setBackground("true".equalsIgnoreCase(bgStr));
        cmd.setResultDir(resultDir.isEmpty() ? null : resultDir);

        return cmd;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

