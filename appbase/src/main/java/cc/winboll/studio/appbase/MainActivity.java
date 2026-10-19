package cc.winboll.studio.appbase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.appbase.model.TestBean;
import cc.winboll.studio.libappbase.LogActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 未标注（建议补充创建日期）
 * @Describe 应用主界面 Activity（入口界面）
 * 包含功能测试按钮（崩溃测试、日志查看、Toast测试）、顶部工具栏（菜单功能），是应用交互的核心入口
 */
public class MainActivity extends Activity {

    /** 当前 Activity 的日志 TAG（用于调试输出，标识日志来源） */
    public static final String TAG = "MainActivity";

    /** 顶部工具栏（用于展示标题、菜单，绑定布局中的 Toolbar 控件） */
    private Toolbar mToolbar;

    /**
     * Activity 创建时回调（初始化界面）
     * 在 Activity 首次创建时执行，用于加载布局、初始化控件、设置事件监听
     * @param savedInstanceState 保存 Activity 状态的 Bundle（如屏幕旋转时的数据恢复）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ToastUtils.show("onCreate"); // 显示 Activity 创建提示（调试用）
        setContentView(R.layout.activity_main); // 加载主界面布局

        // 初始化 Toolbar 并设置为 ActionBar
        mToolbar = findViewById(R.id.toolbar);
        setActionBar(mToolbar); // 将 Toolbar 替代系统默认 ActionBar
		
		initTestData();
    }
	
	void initTestData() {
		TestBean bean1 = new TestBean();
		bean1.setTestNum1(456);
		TestBean.saveBeanToFile(getFilesDir().getAbsolutePath() + getTestBeanRelativePath(), bean1);
		TestBean bean2 = new TestBean();
		bean2.setTestNum1(789);
		TestBean.saveBeanToFile(getExternalFilesDir(null).getAbsolutePath() + getTestBeanRelativePath(), bean2);
	}
	
	String getTestBeanRelativePath() {
		return "/BaseBaen/"+TestBean.class.getName()+".json";
	}

    /**
     * 创建菜单时回调（加载工具栏菜单）
     * 初始化 ActionBar 菜单，加载自定义菜单布局
     * @param menu 菜单对象（用于承载菜单项）
     * @return true：显示菜单；false：不显示菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单布局（R.menu.toolbar_main 为自定义菜单文件）
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单 item 点击时回调（处理菜单事件）
     * 响应 Toolbar 菜单项的点击事件，执行对应业务逻辑
     * @param item 被点击的菜单项
     * @return true：消费点击事件；false：不消费（传递给父类）
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_home:
                // 点击 "首页/官网" 菜单项，唤起浏览器打开指定网站
                openWebsiteInBrowser(this);
                break;
				// 可扩展其他菜单项（如设置、关于等）的处理逻辑
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 崩溃测试按钮点击事件（触发应用崩溃，用于调试异常捕获）
     * 故意执行非法操作（循环获取不存在的字符串资源），强制应用崩溃
     * @param view 触发事件的 View（对应布局中的崩溃测试按钮）
     */
    public void onCrashTest(View view) {
        // 循环从 Integer.MIN_VALUE 到 Integer.MAX_VALUE，获取不存在的字符串资源 ID，触发崩溃
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            getString(i); // i 超出资源 ID 范围，抛出 Resources.NotFoundException 导致崩溃
        }
    }

    public void onLogTestNewTask(View view) {
        LogActivity.startLogActivity(this, true);
    }

    /**
     * 日志测试按钮点击事件（打开日志查看界面）
     * 启动 LogActivity，用于查看应用运行日志
     * @param view 触发事件的 View（对应布局中的日志测试按钮）
     */
    public void onLogTest(View view) {
        LogActivity.startLogActivity(this, false);
    }

    /**
     * Toast 工具测试按钮点击事件（测试全局 Toast 功能）
     * 测试主线程、子线程中 Toast 的显示效果，验证 ToastUtils 的可用性
     * @param view 触发事件的 View（对应布局中的 Toast 测试按钮）
     */
    public void onToastUtilsTest(View view) {
        LogUtils.d(TAG, "onToastUtilsTest"); // 打印调试日志，标识进入 Toast 测试
        ToastUtils.show("Hello, WinBoLL!"); // 主线程显示 Toast

        // 开启子线程，延迟 2 秒后显示 Toast（测试子线程 Toast 兼容性）
        new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000); // 线程休眠 2 秒
						// 若 ToastUtils 已处理主线程切换，此处可直接调用；否则需通过 Handler 切换到主线程
						ToastUtils.show("Thread.sleep(2000);ToastUtils.show...");
					} catch (InterruptedException e) {
						// 捕获线程中断异常（如线程被销毁时），不做处理（测试场景）
						e.printStackTrace();
					}
				}
			}).start();
    }

    /**
     * 唤起系统默认浏览器打开指定网站（跳转至应用官网）
     * 通过 Intent.ACTION_VIEW 隐式意图，触发浏览器打开目标 URL
     * @param context 上下文对象（如 Activity、Application，此处为 MainActivity）
     */
    public void openWebsiteInBrowser(Context context) {
        String url = "https://www.winboll.cc"; // 目标网站 URL（应用官网）
        // 构建隐式意图：ACTION_VIEW 表示查看指定数据（Uri 为网站地址）
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        // 设置标志：在新的任务栈中启动 Activity（避免与当前应用任务栈混淆）
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动意图（唤起浏览器）
        context.startActivity(intent);
    }

	public void onAboutActivity(View view) {
        LogUtils.d(TAG, "onAboutActivity() 调用");
        Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
        startActivity(aboutIntent);
    }



	public void onMultiInstance(View view) {
        LogUtils.d(TAG, "onMultiInstance() 多开窗口按钮已点击");
        ToastUtils.show("多开窗口：已启动新窗口");
        android.content.Intent intent = new android.content.Intent(this, Main2Activity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        LogUtils.d(TAG, "onMultiInstance() 准备启动Main2Activity");
        startActivity(intent);
        LogUtils.d(TAG, "onMultiInstance() Main2Activity已启动");
    }
}

