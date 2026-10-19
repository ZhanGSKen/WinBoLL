package cc.winboll.studio.libappbase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 应用崩溃保险丝内部类（单例）
 * 核心作用：限制短时间内重复崩溃，通过「熔断等级」控制崩溃页面启动策略
 * 等级范围：MINI（1）~ MAX（2），每次崩溃等级-1，熔断后启动基础版崩溃页面
 */
public final class AppCrashSafetyWire {
	
	public static final String TAG = "AppCrashSafetyWire";
	
	/** 单例实例（volatile 保证多线程可见性） */
	private static volatile AppCrashSafetyWire _AppCrashSafetyWire;

	/** 当前熔断等级（1：最低防护；2：最高防护；≤0：熔断） */
	private volatile Integer currentSafetyLevel;
	/** 最低熔断等级（1，再崩溃则熔断） */
	private static final int _MINI = 1;
	/** 最高熔断等级（2，初始状态） */
	private static final int _MAX = 2;

	/**
	 * 私有构造方法（单例模式，禁止外部实例化）
	 * 初始化时加载本地存储的熔断等级
	 */
	private AppCrashSafetyWire() {
		LogUtils.d(TAG, "AppCrashSafetyWire()");
		currentSafetyLevel = loadCurrentSafetyLevel();
	}

	/**
	 * 获取单例实例（双重检查锁定，线程安全）
	 * @return AppCrashSafetyWire 单例
	 */
	public static synchronized AppCrashSafetyWire getInstance() {
		if (_AppCrashSafetyWire == null) {
			_AppCrashSafetyWire = new AppCrashSafetyWire();
		}
		return _AppCrashSafetyWire;
	}

	/**
	 * 设置当前熔断等级（内存中）
	 * @param currentSafetyLevel 目标等级（1~2）
	 */
	public void setCurrentSafetyLevel(int currentSafetyLevel) {
		this.currentSafetyLevel = currentSafetyLevel;
	}

	/**
	 * 获取当前熔断等级（内存中）
	 * @return 当前等级（1~2 或 null）
	 */
	public int getCurrentSafetyLevel() {
		return currentSafetyLevel;
	}

	/**
	 * 保存熔断等级到本地文件（持久化，重启应用生效）
	 * @param currentSafetyLevel 待保存的等级
	 */
	public void saveCurrentSafetyLevel(int currentSafetyLevel) {
		LogUtils.d(TAG, "saveCurrentSafetyLevel()");
		this.currentSafetyLevel = currentSafetyLevel;
		try {
			// 序列化等级到文件（ObjectOutputStream 写入 int）
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CrashHandler._CrashCountFilePath));
			oos.writeInt(currentSafetyLevel);
			oos.flush();
			oos.close();
			LogUtils.d(TAG, String.format("saveCurrentSafetyLevel writeInt currentSafetyLevel %d", currentSafetyLevel));
		} catch (IOException e) {
			LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
		}
	}

	/**
	 * 从本地文件加载熔断等级（应用启动时初始化）
	 * @return 加载的等级（文件不存在则初始化为 MAX（2））
	 */
	public int loadCurrentSafetyLevel() {
		LogUtils.d(TAG, "loadCurrentSafetyLevel()");
		try {
			File f = new File(CrashHandler._CrashCountFilePath);
			if (f.exists()) {
				// 反序列化从文件读取等级
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CrashHandler._CrashCountFilePath));
				currentSafetyLevel = ois.readInt();
				LogUtils.d(TAG, String.format("loadCurrentSafetyLevel() readInt currentSafetyLevel %d", currentSafetyLevel));
			} else {
				// 文件不存在，初始化等级为最高（2）并保存
				currentSafetyLevel = _MAX;
				LogUtils.d(TAG, String.format("loadCurrentSafetyLevel() currentSafetyLevel init to _MAX->%d", _MAX));
				saveCurrentSafetyLevel(currentSafetyLevel);
			}
		} catch (IOException e) {
			LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
		}
		return currentSafetyLevel;
	}

	/**
	 * 熔断保险丝（每次崩溃调用，降低防护等级）
	 * @return 熔断后是否仍在防护范围内（true：是；false：已熔断）
	 */
	boolean burnSafetyWire() {
		LogUtils.d(TAG, "burnSafetyWire()");
		// 加载当前等级
		int safeLevel = loadCurrentSafetyLevel();
		// 若在防护范围内（1~2），等级-1 并保存
		if (isSafetyWireWorking(safeLevel)) {
			LogUtils.d(TAG, "burnSafetyWire() use");
			saveCurrentSafetyLevel(safeLevel - 1);
			// 返回熔断后的状态
			return isSafetyWireWorking(safeLevel - 1);
		}
		return false;
	}

	/**
	 * 检查熔断等级是否在有效范围内（1~2）
	 * @param safetyLevel 待检查的等级
	 * @return true：在范围内（防护有效）；false：超出范围（已熔断）
	 */
	boolean isSafetyWireWorking(int safetyLevel) {
		LogUtils.d(TAG, "isSafetyWireOK()");
		LogUtils.d(TAG, String.format("SafetyLevel %d", safetyLevel));

		if (safetyLevel >= _MINI && safetyLevel <= _MAX) {
			LogUtils.d(TAG, String.format("In Safety Level"));
			return true;
		}
		LogUtils.d(TAG, String.format("Out of Safety Level"));
		return false;
	}

	/**
	 * 立即恢复熔断等级到最高（2）
	 * 用于重启应用后重置防护状态
	 */
	void resumeToMaximumImmediately() {
		LogUtils.d(TAG, "resumeToMaximumImmediately() call saveCurrentSafetyLevel(_MAX)");
		AppCrashSafetyWire.getInstance().saveCurrentSafetyLevel(_MAX);
	}

	/**
	 * 关闭防护（设置等级为最低（1））
	 * 下次崩溃直接熔断
	 */
	void off() {
		LogUtils.d(TAG, "off()");
		saveCurrentSafetyLevel(_MINI);
	}

	/**
	 * 检查当前保险丝是否有效（防护未熔断）
	 * @return true：有效（等级 1~2）；false：已熔断
	 */
	public boolean isAppCrashSafetyWireOK() {
		LogUtils.d(TAG, "isAppCrashSafetyWireOK()");
		currentSafetyLevel = loadCurrentSafetyLevel();
		return isSafetyWireWorking(currentSafetyLevel);
	}

	/**
	 * 延迟恢复保险丝到最高等级（500ms 后）
	 * 核心作用：崩溃页面启动后，若下次即将熔断，提前恢复防护等级，避免持续崩溃
	 * @param context 上下文（用于获取主线程 Handler）
	 */
	void postResumeCrashSafetyWireHandler(final Context context) {
		// 主线程延迟 500ms 执行（避免页面启动时阻塞）
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "Handler run()");
					// 检查：若当前等级-1 后超出防护范围（即将熔断），则恢复到最高等级
					if (!AppCrashSafetyWire.getInstance().isSafetyWireWorking(currentSafetyLevel - 1)) {
						AppCrashSafetyWire.getInstance().resumeToMaximumImmediately();
						LogUtils.d(TAG, "postResumeCrashSafetyWireHandler: 恢复保险丝到最高等级");
					}
				}
			}, 500);
	}
}
