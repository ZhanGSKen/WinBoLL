package cc.winboll.studio.libappbase;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 20:03
 * @Describe WinBoLL JSON 数据模型基类（抽象类）
 * 定义 Json Bean 的核心规范：序列化/反序列化、文件持久化、列表处理等通用逻辑，
 * 子类（如 APPModel）需实现抽象方法，实现自身字段的 JSON 读写
 * @param <T> 泛型约束，限定子类必须继承自 BaseBean
 */
public abstract class BaseBean<T extends BaseBean> {

	/** 日志标签，用于当前基类的日志输出标识 */
	public static final String TAG = "BaseBean";
	/** JSON 中存储 Bean 类名的字段键（用于校验 Bean 类型一致性） */
	static final String BEAN_NAME = "BeanName";

	/**
	 * 无参构造方法（子类需默认实现，支持反射实例化）
	 */
	public BaseBean() {}

	/**
	 * 抽象方法：获取当前 Bean 的全限定类名
	 * 子类需实现，用于标识 Bean 类型（序列化/校验时使用）
	 * @return 类的全限定名（如：cc.winboll.studio.libappbase.APPModel）
	 */
	public abstract String getName();

	/**
	 * 获取单个 Bean 的 JSON 持久化文件路径
	 * 路径：外部存储/应用私有目录/BaseBean/[类名].json
	 * @param context 上下文（用于获取应用存储目录）
	 * @return 单个 Bean 的文件绝对路径
	 */
	public String getBeanJsonFilePath(Context context) {
		return context.getExternalFilesDir(TAG) + "/" + getName() + ".json";
	}

	/**
	 * 获取 Bean 列表的 JSON 持久化文件路径
	 * 路径：外部存储/应用私有目录/BaseBean/[类名]_List.json
	 * @param context 上下文（用于获取应用存储目录）
	 * @return Bean 列表的文件绝对路径
	 */
	public String getBeanListJsonFilePath(Context context) {
		return context.getExternalFilesDir(TAG) + "/" + getName() + "_List.json";
	}

	/**
	 * 将 Bean 类名写入 JSON（序列化基础字段）
	 * 子类可重写扩展，添加自身字段的 JSON 写入逻辑
	 * @param jsonWriter JSON 写入器（用于输出 JSON 数据）
	 * @throws IOException JSON 写入失败时抛出（如流异常）
	 */
	public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
		// 写入 Bean 类名字段（用于反序列化时校验类型）
		jsonWriter.name(BEAN_NAME).value(getName());
	}

	/**
	 * 从 JSON 读取字段并初始化 Bean（反序列化基础逻辑）
	 * 子类需重写，实现自身字段的解析逻辑
	 * @param jsonReader JSON 读取器（用于读取 JSON 数据）
	 * @param name 当前解析的 JSON 字段名
	 * @return true：字段解析成功（当前类处理）；false：字段未处理（需跳过）
	 * @throws IOException JSON 读取失败时抛出（如流异常）
	 */
	public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
		return false; // 基类未处理任何字段，返回 false
	}

	/**
	 * 抽象方法：从 JSON 读取器解析并返回 Bean 实例
	 * 子类需实现，处理自身字段的完整解析逻辑
	 * @param jsonReader JSON 读取器（用于读取 JSON 数据）
	 * @return 解析完成的 Bean 实例
	 * @throws IOException JSON 读取失败时抛出（如流异常）
	 */
	abstract public T readBeanFromJsonReader(JsonReader jsonReader) throws IOException;

	/**
	 * 校验 JSON 文件中的 Bean 列表与目标类是否一致
	 * 对比文件中每个 Bean 的类名与目标类名，返回不一致信息
	 * @param szFilePath JSON 文件路径（存储 Bean 列表的文件）
	 * @param clazz 目标 Bean 类（用于校验类型）
	 * @return 空串：校验一致；非空串：不一致信息（总数/差异数）或异常信息
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> String checkIsTheSameBeanListAndFile(String szFilePath, Class<T> clazz) {
		StringBuilder sbResult = new StringBuilder();
		String szErrorInfo = "Check Is The Same Bean List And File Error : ";

		try {
			int sameCount = 0; // 类名匹配的 Bean 数量
			int totalCount = 0; // 文件中 Bean 总数量

			// 反射创建目标 Bean 实例（用于获取类名）
			T beanTemp = clazz.newInstance();
			String targetBeanName = beanTemp.getName();
			// 读取文件中的 JSON 字符串
			String listJson = UTF8FileUtils.readStringFromFile(szFilePath);
			StringReader stringReader = new StringReader(listJson);
			JsonReader jsonReader = new JsonReader(stringReader);

			jsonReader.beginArray(); // 开始解析 JSON 数组（Bean 列表）
			while (jsonReader.hasNext()) {
				totalCount++;
				jsonReader.beginObject(); // 开始解析单个 Bean 对象
				while (jsonReader.hasNext()) {
					String name = jsonReader.nextName();
					// 只校验 BEAN_NAME 字段，其他字段跳过
					if (name.equals(BEAN_NAME)) {
						// 对比当前 Bean 类名与目标类名
						if (targetBeanName.equals(jsonReader.nextString())) {
							sameCount++;
						}
					} else {
						jsonReader.skipValue(); // 跳过非目标字段
					}
				}
				jsonReader.endObject(); // 结束单个 Bean 对象解析
			}
			jsonReader.endArray(); // 结束 JSON 数组解析

			// 生成校验结果
			if (sameCount == totalCount) {
				return ""; // 全部匹配，返回空串
			} else {
				// 部分不匹配，返回统计信息
				sbResult.append("Total : ").append(totalCount)
					.append(" Diff : ").append(totalCount - sameCount);
			}
		} catch (InstantiationException e) {
			// 反射实例化失败（如无无参构造）
			sbResult.append(szErrorInfo).append(e);
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		} catch (IllegalAccessException e) {
			// 反射访问权限异常
			sbResult.append(szErrorInfo).append(e);
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		} catch (IOException e) {
			// 文件读取或 JSON 解析异常
			sbResult.append(szErrorInfo).append(e);
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return sbResult.toString();
	}

	/**
	 * 将 JSON 字符串解析为目标 Bean 实例
	 * 通过反射创建 Bean 实例，调用子类解析逻辑完成初始化
	 * @param szBean JSON 字符串（单个 Bean 的 JSON 数据）
	 * @param clazz 目标 Bean 类（用于反射实例化）
	 * @return 解析成功的 Bean 实例；失败返回 null
	 * @throws IOException JSON 解析失败时抛出
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> T parseStringToBean(String szBean, Class<T> clazz) throws IOException {
		StringReader stringReader = new StringReader(szBean);
		JsonReader jsonReader = new JsonReader(stringReader);

		try {
			// 反射创建 Bean 实例
			T beanTemp = clazz.newInstance();
			// 调用子类解析方法，返回解析后的实例
			return (T) beanTemp.readBeanFromJsonReader(jsonReader);
		} catch (InstantiationException | IllegalAccessException e) {
			// 反射异常日志记录
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return null;
	}

	/**
	 * 将 JSON 字符串解析为 Bean 列表
	 * 清空目标列表，将解析后的 Bean 逐个添加到列表中
	 * @param szBeanList JSON 字符串（Bean 列表的 JSON 数组）
	 * @param beanList 目标列表（存储解析后的 Bean）
	 * @param clazz 目标 Bean 类（用于反射实例化）
	 * @return true：解析成功；false：解析失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean parseStringToBeanList(String szBeanList, ArrayList<T> beanList, Class<T> clazz) {
		try {
			// 初始化目标列表（为空则创建，非空则清空）
			if (beanList == null) {
				beanList = new ArrayList<T>();
			} else {
				beanList.clear();
			}

			StringReader stringReader = new StringReader(szBeanList);
			JsonReader jsonReader = new JsonReader(stringReader);

			jsonReader.beginArray(); // 开始解析 JSON 数组
			while (jsonReader.hasNext()) {
				// 反射创建 Bean 实例，解析并添加到列表
				T beanTemp = clazz.newInstance();
				T bean = (T) beanTemp.readBeanFromJsonReader(jsonReader);
				if (bean != null) {
					beanList.add(bean);
				}
			}
			jsonReader.endArray(); // 结束 JSON 数组解析
			return true;
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			// 异常日志记录
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return false;
	}

	/**
	 * 重写 toString()，将 Bean 序列化为格式化的 JSON 字符串
	 * 调用自身序列化逻辑，生成带缩进的 JSON（便于调试）
	 * @return Bean 的 JSON 字符串；失败返回空串
	 */
	@Override
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);
		jsonWriter.setIndent("  "); // 设置 JSON 缩进（格式化输出）

		try {
			jsonWriter.beginObject(); // 开始 JSON 对象
			writeThisToJsonWriter(jsonWriter); // 写入 Bean 字段（子类扩展）
			jsonWriter.endObject(); // 结束 JSON 对象
			return stringWriter.toString();
		} catch (IOException e) {
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return "";
	}

	/**
	 * 将 Bean 列表序列化为格式化的 JSON 字符串
	 * 遍历列表，逐个序列化每个 Bean，生成 JSON 数组
	 * @param beanList 待序列化的 Bean 列表
	 * @return 列表的 JSON 字符串；失败返回空串
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> String toStringByBeanList(ArrayList<T> beanList) {
		try {
			StringWriter stringWriter = new StringWriter();
			JsonWriter jsonWriter = new JsonWriter(stringWriter);
			jsonWriter.setIndent("  "); // 格式化缩进

			jsonWriter.beginArray(); // 开始 JSON 数组
			for (int i = 0; i < beanList.size(); i++) {
				jsonWriter.beginObject(); // 单个 Bean 开始
				beanList.get(i).writeThisToJsonWriter(jsonWriter); // 调用 Bean 自身序列化
				jsonWriter.endObject(); // 单个 Bean 结束
			}
			jsonWriter.endArray(); // 结束 JSON 数组
			jsonWriter.close();
			return stringWriter.toString();
		} catch (IOException e) {
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return "";
	}

	/**
	 * 从默认路径（getBeanJsonFilePath）加载 Bean 实例
	 * 读取应用私有目录下的 JSON 文件，解析为目标 Bean
	 * @param context 上下文（用于获取文件路径）
	 * @param clazz 目标 Bean 类（用于反射实例化）
	 * @return 加载成功的 Bean 实例；失败返回 null
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> T loadBean(Context context, Class<T> clazz) {
		try {
			// 反射创建 Bean 实例，获取默认文件路径
			T beanTemp = clazz.newInstance();
			return loadBeanFromFile(beanTemp.getBeanJsonFilePath(context), clazz);
		} catch (InstantiationException | IllegalAccessException e) {
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return null;
	}

	/**
	 * 从指定文件路径加载 Bean 实例
	 * 检查文件是否存在，存在则读取 JSON 并解析为目标 Bean
	 * @param szFilePath 目标文件路径（存储 Bean 的 JSON 文件）
	 * @param clazz 目标 Bean 类（用于反射实例化）
	 * @return 加载成功的 Bean 实例；失败返回 null
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> T loadBeanFromFile(String szFilePath, Class<T> clazz) {
		try {
			File file = new File(szFilePath);
			if (file.exists()) { // 检查文件是否存在
				T beanTemp = clazz.newInstance();
				// 读取文件 JSON 字符串，解析为 Bean
				String json = UTF8FileUtils.readStringFromFile(szFilePath);
				return beanTemp.parseStringToBean(json, clazz);
			}
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return null;
	}

	/**
	 * 将 Bean 保存到默认路径（getBeanJsonFilePath）的文件中
	 * 序列化 Bean 为 JSON，写入应用私有目录下的文件
	 * @param context 上下文（用于获取文件路径）
	 * @param bean 待保存的 Bean 实例
	 * @return true：保存成功；false：保存失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean saveBean(Context context, T bean) {
		return saveBeanToFile(bean.getBeanJsonFilePath(context), bean);
	}

	/**
	 * 将 Bean 保存到指定文件路径
	 * 序列化 Bean 为 JSON 字符串，写入目标文件（覆盖原有内容）
	 * @param szFilePath 目标文件路径（保存 JSON 的文件）
	 * @param bean 待保存的 Bean 实例
	 * @return true：保存成功；false：保存失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean saveBeanToFile(String szFilePath, T bean) {
		try {
			// 序列化 Bean 为 JSON 字符串
			String json = bean.toString();
			// 写入文件（UTF-8 编码）
			UTF8FileUtils.writeStringToFile(szFilePath, json);
			return true;
		} catch (IOException e) {
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return false;
	}

	/**
	 * 从默认路径（getBeanListJsonFilePath）加载 Bean 列表
	 * 读取应用私有目录下的列表 JSON 文件，解析并填充到目标列表
	 * @param context 上下文（用于获取文件路径）
	 * @param beanListDst 目标列表（存储加载后的 Bean）
	 * @param clazz 目标 Bean 类（用于反射实例化）
	 * @return true：加载成功；false：加载失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean loadBeanList(Context context, ArrayList<T> beanListDst, Class<T> clazz) {
		try {
			// 反射创建 Bean 实例，获取默认列表文件路径
			T beanTemp = clazz.newInstance();
			return loadBeanListFromFile(beanTemp.getBeanListJsonFilePath(context), beanListDst, clazz);
		} catch (InstantiationException | IllegalAccessException e) {
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return false;
	}

	/**
	 * 从指定文件路径加载 Bean 列表
	 * 检查文件是否存在，存在则读取 JSON 数组，解析并填充到目标列表
	 * @param szFilePath 目标文件路径（存储列表 JSON 的文件）
	 * @param beanList 目标列表（存储加载后的 Bean）
	 * @param clazz 目标 Bean 类（用于反射实例化）
	 * @return true：加载成功；false：加载失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean loadBeanListFromFile(String szFilePath, ArrayList<T> beanList, Class<T> clazz) {
		try {
			File file = new File(szFilePath);
			if (file.exists()) { // 检查文件是否存在
				// 读取文件中的 JSON 字符串（Bean 列表数组）
				String listJson = UTF8FileUtils.readStringFromFile(szFilePath);
				// 解析 JSON 字符串为 Bean 列表，填充到目标列表
				return parseStringToBeanList(listJson, beanList, clazz);
			}
		} catch (IOException e) {
			// 日志记录文件读取或解析异常
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return false;
	}

	/**
	 * 将 Bean 列表保存到默认路径（getBeanListJsonFilePath）的文件中
	 * 序列化列表为 JSON 数组，写入应用私有目录下的文件
	 * @param context 上下文（用于获取文件路径）
	 * @param beanList 待保存的 Bean 列表
	 * @param clazz 目标 Bean 类（用于反射获取保存路径）
	 * @return true：保存成功；false：保存失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean saveBeanList(Context context, ArrayList<T> beanList, Class<T> clazz) {
		try {
			// 反射创建 Bean 实例，获取默认列表保存路径
			T beanTemp = clazz.newInstance();
			return saveBeanListToFile(beanTemp.getBeanListJsonFilePath(context), beanList);
		} catch (InstantiationException | IllegalAccessException e) {
			// 日志记录反射实例化异常
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return false;
	}

	/**
	 * 将 Bean 列表保存到指定文件路径
	 * 序列化列表为 JSON 数组字符串，写入目标文件（覆盖原有内容）
	 * @param szFilePath 目标文件路径（保存列表 JSON 的文件）
	 * @param beanList 待保存的 Bean 列表
	 * @return true：保存成功；false：保存失败
	 * @param <T> 泛型约束，限定为 BaseBean 子类
	 */
	public static <T extends BaseBean> boolean saveBeanListToFile(String szFilePath, ArrayList<T> beanList) {
		try {
			// 序列化 Bean 列表为 JSON 字符串（数组格式）
			String json = toStringByBeanList(beanList);
			// 将 JSON 字符串写入文件（UTF-8 编码）
			UTF8FileUtils.writeStringToFile(szFilePath, json);
			return true;
		} catch (IOException e) {
			// 日志记录文件写入或序列化异常
			LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
		}
		return false;
	}
}

