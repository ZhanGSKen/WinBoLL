package cc.winboll.studio.winboll.activities;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/04 11:06
 * @Describe 云宝云
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.BuildConfig;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.winboll.R;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YunActivity extends Activity implements IWinBoLLActivity {

    public static final String TAG = "YunActivity";

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
        setContentView(R.layout.activity_yun);
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

    public void onTestYun(View view) {
        LogUtils.d(TAG, "onTestYun");
        (new Thread(new Runnable(){
                @Override
                public void run() {
                    testYun();
                }
            })).start();
    }

    void testYun() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(mHost + "/backups/")
            .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = "";
                if (response.body() != null) {
                    responseBody = response.body().string();
                }

                // 正则匹配：任意主机名 -> Test OK（主机名部分匹配非空字符）
                boolean isMatch = responseBody.matches(".+? -> Test OK");

                if (isMatch) {
                    LogUtils.d(TAG, responseBody);
                } else {
                    LogUtils.d(TAG, "响应内容不匹配，内容：" + responseBody);
                }
            } else {
                LogUtils.d(TAG, "请求失败，状态码：" + response.code());
            }
        } catch (IOException e) {
            LogUtils.d(TAG, "读取响应体失败：" + e.getMessage());
        } catch (Exception e) {
            LogUtils.d(TAG, "异常：" + e.getMessage());
            e.printStackTrace(); // Java 7 需显式打印堆栈
        } finally {
            // 手动关闭 Response（Java 7 不支持 try-with-resources）
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }
}
