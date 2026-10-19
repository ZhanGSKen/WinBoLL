package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import android.text.TextUtils;
import cc.winboll.studio.libappbase.LogUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图片下载工具类（单例模式）
 * 功能：下载网络图片到缓存目录、清理过期文件、获取最新下载文件
 * 适配：Java 7 + Android API 30
 * 核心策略：OkHttp 全局复用、7天文件过期清理、UUID 唯一文件名、内置缓存目录（无需权限）
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/19 20:52
 */
public class ImageDownloader {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "ImageDownloader";
    // 缓存目录子文件夹名称
    private static final String CACHE_DIR_NAME = "networkdownload";
    // 过期时间：7天（单位：毫秒）
    private static final long EXPIRE_TIME = 7 * 24 * 3600 * 1000;
    // OkHttp 超时配置
    private static final int CONNECT_TIMEOUT = 10;
    private static final int READ_WRITE_TIMEOUT = 15;
    // 文件后缀最大长度
    private static final int MAX_EXTENSION_LENGTH = 5;
    // 默认文件后缀
    private static final String DEFAULT_EXTENSION = ".jpg";
    // 缓冲区大小
    private static final int BUFFER_SIZE = 1024;

    // ================================== 成员变量（单例核心 + 全局资源）=================================
    // 单例实例
    private static ImageDownloader sInstance;
    // OkHttp 客户端（全局复用，提升性能）
    private OkHttpClient mOkHttpClient;
    // 缓存目录：/data/data/应用包名/cache/networkdownload
    private File mCacheDir;

    // ================================== 单例方法（线程安全 + 应用上下文）=================================
    /**
     * 单例获取方法（线程安全）
     * @param context 上下文（建议使用 Application 上下文避免内存泄漏）
     * @return 单例实例
     */
    public static synchronized ImageDownloader getInstance(Context context) {
        LogUtils.d(TAG, "【getInstance】单例获取方法调用");
        if (sInstance == null) {
            // 使用 Application 上下文，防止 Activity 销毁导致的内存泄漏
            sInstance = new ImageDownloader(context.getApplicationContext());
            LogUtils.d(TAG, "【getInstance】单例实例首次创建");
        }
        return sInstance;
    }

    // ================================== 构造方法（私有 + 初始化逻辑）=================================
    /**
     * 私有构造（单例模式禁止外部实例化）
     * @param context 应用上下文
     */
    private ImageDownloader(Context context) {
        LogUtils.d(TAG, "【ImageDownloader】构造方法调用，开始初始化");
        // 初始化 OkHttp 客户端（设置超时时间）
        initOkHttpClient();
        // 初始化缓存目录：networkdownload
        initCacheDir(context);
        // 初始化时清理过期文件
        clearExpiredFiles();
        LogUtils.d(TAG, "【ImageDownloader】初始化完成");
    }

    // ================================== 核心初始化方法（OkHttp + 缓存目录）=================================
    /**
     * 初始化 OkHttp 客户端（全局复用）
     */
    private void initOkHttpClient() {
        LogUtils.d(TAG, "【initOkHttpClient】开始初始化 OkHttp 客户端");
        mOkHttpClient = new OkHttpClient.Builder()
			.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
			.readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
			.writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
			.build();
        LogUtils.d(TAG, "【initOkHttpClient】OkHttp 客户端初始化完成");
    }

    /**
     * 初始化缓存目录：若不存在则创建
     * @param context 应用上下文
     */
    private void initCacheDir(Context context) {
        LogUtils.d(TAG, "【initCacheDir】开始初始化缓存目录");
        // 获取应用内置缓存目录（无需权限）
        File cacheRoot = context.getCacheDir();
        mCacheDir = new File(cacheRoot, CACHE_DIR_NAME);

        // 若目录不存在则创建（包括父目录）
        if (!mCacheDir.exists()) {
            boolean isCreated = mCacheDir.mkdirs();
            if (isCreated) {
                LogUtils.d(TAG, "【initCacheDir】缓存目录创建成功：" + mCacheDir.getAbsolutePath());
            } else {
                LogUtils.e(TAG, "【initCacheDir】缓存目录创建失败");
            }
        } else {
            LogUtils.d(TAG, "【initCacheDir】缓存目录已存在：" + mCacheDir.getAbsolutePath());
        }
    }

    // ================================== 核心业务方法（下载 + 清理 + 获取最新文件）=================================
    /**
     * 下载网络图片到缓存目录
     * @param imageUrl 图片网络链接
     * @param callback 下载结果回调（成功/失败）
     */
    public void downloadImage(final String imageUrl, final DownloadCallback callback) {
        LogUtils.d(TAG, "【downloadImage】下载方法调用 | 图片链接=" + imageUrl);
        // 1. 校验参数
        if (TextUtils.isEmpty(imageUrl)) {
            String errorMsg = "图片链接为空";
            LogUtils.e(TAG, "【downloadImage】参数校验失败：" + errorMsg);
            if (callback != null) {
                callback.onFailure(errorMsg);
            }
            return;
        }

        if (mCacheDir == null || !mCacheDir.exists()) {
            String errorMsg = "缓存目录不存在";
            LogUtils.e(TAG, "【downloadImage】参数校验失败：" + errorMsg);
            if (callback != null) {
                callback.onFailure(errorMsg);
            }
            return;
        }

        // 2. 构建 OkHttp 请求
        Request request = new Request.Builder()
			.url(imageUrl)
			.build();

        // 3. 异步下载（避免阻塞主线程）
        mOkHttpClient.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					String errorMsg = "下载失败：" + e.getMessage();
					LogUtils.e(TAG, "【downloadImage】OkHttp 下载失败", e);
					if (callback != null) {
						callback.onFailure(errorMsg);
					}
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					// 3.1 响应状态校验
					if (!response.isSuccessful()) {
						String errorMsg = "响应失败：" + response.code();
						LogUtils.e(TAG, "【downloadImage】响应失败，状态码：" + response.code());
						if (callback != null) {
							callback.onFailure(errorMsg);
						}
						// 关闭响应体
						if (response.body() != null) {
							response.body().close();
						}
						return;
					}

					// 3.2 响应成功，写入文件
					InputStream inputStream = null;
					FileOutputStream outputStream = null;
					try {
						inputStream = response.body().byteStream();
						// 生成 UUID 唯一文件名（保留原文件后缀）
						String fileExtension = getFileExtension(imageUrl);
						String fileName = UUID.randomUUID().toString() + fileExtension;
						File imageFile = new File(mCacheDir, fileName);

						// 写入文件
						outputStream = new FileOutputStream(imageFile);
						byte[] buffer = new byte[BUFFER_SIZE];
						int len;
						while ((len = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, len);
						}
						outputStream.flush();

						// 下载成功，回调主线程并返回文件路径
						String filePath = imageFile.getAbsolutePath();
						LogUtils.d(TAG, "【downloadImage】图片下载成功：" + filePath);
						if (callback != null) {
							callback.onSuccess(filePath);
						}

					} catch (IOException e) {
						String errorMsg = "文件写入失败：" + e.getMessage();
						LogUtils.e(TAG, "【downloadImage】文件写入失败", e);
						if (callback != null) {
							callback.onFailure(errorMsg);
						}
					} finally {
						// 关闭流（Java7 手动关闭，避免资源泄漏）
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException e) {
								LogUtils.e(TAG, "【downloadImage】输入流关闭失败", e);
							}
						}
						if (outputStream != null) {
							try {
								outputStream.close();
							} catch (IOException e) {
								LogUtils.e(TAG, "【downloadImage】输出流关闭失败", e);
							}
						}
						// 关闭响应体
						if (response.body() != null) {
							response.body().close();
						}
					}
				}
			});
    }

    /**
     * 清理过期文件（最后修改时间超过 EXPIRE_TIME 的文件）
     */
    private void clearExpiredFiles() {
        LogUtils.d(TAG, "【clearExpiredFiles】开始清理过期文件");
        if (mCacheDir == null || !mCacheDir.exists()) {
            LogUtils.d(TAG, "【clearExpiredFiles】缓存目录不存在，无需清理");
            return;
        }

        File[] files = mCacheDir.listFiles();
        if (files == null || files.length == 0) {
            LogUtils.d(TAG, "【clearExpiredFiles】缓存目录无文件，无需清理");
            return;
        }

        long currentTime = System.currentTimeMillis();
        int deleteCount = 0;

        // 遍历所有文件，删除过期文件
        for (File file : files) {
            long lastModifyTime = file.lastModified();
            if (currentTime - lastModifyTime > EXPIRE_TIME) {
                if (file.delete()) {
                    deleteCount++;
                    LogUtils.d(TAG, "【clearExpiredFiles】删除过期文件：" + file.getName());
                } else {
                    LogUtils.e(TAG, "【clearExpiredFiles】删除过期文件失败：" + file.getName());
                }
            }
        }

        LogUtils.d(TAG, "【clearExpiredFiles】过期文件清理完成，共删除 " + deleteCount + " 个文件");
    }

    /**
     * 获取 networkdownload 目录中最后下载的文件（按修改时间排序）
     * @return 最后下载的文件路径（null 表示无文件）
     */
    public String getLastDownloadedFile() {
        LogUtils.d(TAG, "【getLastDownloadedFile】获取最新下载文件");
        if (mCacheDir == null || !mCacheDir.exists()) {
            LogUtils.e(TAG, "【getLastDownloadedFile】缓存目录不存在");
            return null;
        }

        File[] files = mCacheDir.listFiles();
        if (files == null || files.length == 0) {
            LogUtils.d(TAG, "【getLastDownloadedFile】缓存目录无文件");
            return null;
        }

        // 按最后修改时间降序排序，取第一个即为最新文件
        File lastFile = files[0];
        for (File file : files) {
            if (file.lastModified() > lastFile.lastModified()) {
                lastFile = file;
            }
        }

        String filePath = lastFile.getAbsolutePath();
        LogUtils.d(TAG, "【getLastDownloadedFile】最后下载的文件：" + filePath);
        return filePath;
    }

    // ================================== 辅助工具方法（文件后缀提取）=================================
    /**
     * 工具方法：从图片链接中提取文件后缀（如 .png、.jpg）
     * @param imageUrl 图片链接
     * @return 文件后缀（含点号，若无法提取则返回 .jpg）
     */
    private String getFileExtension(String imageUrl) {
        LogUtils.d(TAG, "【getFileExtension】提取文件后缀 | 图片链接=" + imageUrl);
        if (TextUtils.isEmpty(imageUrl)) {
            LogUtils.d(TAG, "【getFileExtension】图片链接为空，返回默认后缀：" + DEFAULT_EXTENSION);
            return DEFAULT_EXTENSION;
        }

        int lastDotIndex = imageUrl.lastIndexOf(".");
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        // 确保后缀在最后一个斜杠之后，且长度合理（1-5 个字符）
        if (lastDotIndex > lastSlashIndex && lastDotIndex < imageUrl.length() - 1) {
            String extension = imageUrl.substring(lastDotIndex);
            if (extension.length() <= MAX_EXTENSION_LENGTH) {
                extension = extension.toLowerCase(); // 统一转为小写
                LogUtils.d(TAG, "【getFileExtension】提取后缀成功：" + extension);
                return extension;
            }
        }

        // 无法提取后缀时，默认使用 .jpg
        LogUtils.d(TAG, "【getFileExtension】无法提取有效后缀，返回默认后缀：" + DEFAULT_EXTENSION);
        return DEFAULT_EXTENSION;
    }

    // ================================== 下载结果回调接口（Java7 接口实现）=================================
    /**
     * 下载结果回调接口
     */
    public interface DownloadCallback {
        /**
         * 下载成功
         * @param filePath 图片保存路径
         */
        void onSuccess(String filePath);

        /**
         * 下载失败
         * @param errorMsg 失败原因
         */
        void onFailure(String errorMsg);
    }
}

