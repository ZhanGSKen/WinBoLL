package cc.winboll.studio.aes;

import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/28 21:07
 * @Describe 窗口管理类测试窗口
 */
public class TestActivityManagerActivity extends WinBoLLActivity implements IWinBoLLActivity {
    
    public static final String TAG = "TestActivityManagerActivity";

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
        setContentView(R.layout.activity_testactivitymanager);
        
    }
    
}
