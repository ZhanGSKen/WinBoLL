package cc.winboll.studio.libappbase.models.libs1520000;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.UTF8FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * WinBoLL JSON 数据模型基类（抽象类）
 * 定义 Json Bean 的核心规范：序列化/反序列化、文件持久化、列表处理等通用逻辑，
 * 子类需实现抽象方法，完成自身字段JSON读写业务逻辑
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026/05/19 22:33:00
 * @EditTime 2026/05/19 23:15:48
 * @param <T> 泛型约束，限定子类必须继承自BaseBean
 */
public abstract class BaseBean<T extends BaseBean> {

    // ====================== 静态常量 ======================
    /** 日志输出标识TAG */
    public static final String TAG = "BaseBean";
    /** JSON存储Bean类名字段Key，用于类型校验 */
    static final String BEAN_NAME = "BeanName";

    // ====================== 构造方法 ======================
    /**
     * 无参空构造，满足反射实例化要求
     */
    public BaseBean() {

    }

    // ====================== 抽象方法 ======================
    /**
     * 获取当前实体类全类名
     * @return 全限定类名字符串
     */
    public abstract String getName();

    /**
     * 从JSON读取器解析构建实体对象
     * @param jsonReader JSON读取流
     * @return 解析完成实体对象
     * @throws IOException IO读写异常
     */
    public abstract T readBeanFromJsonReader(final JsonReader jsonReader) throws IOException;

    // ====================== 路径获取相关 ======================
    /**
     * 获取单个实体默认存储JSON文件路径
     * @param context 应用上下文
     * @return 文件绝对路径
     */
    public String getBeanJsonFilePath(final Context context) {
        return context.getExternalFilesDir(TAG) + "/" + getName() + ".json";
    }

    /**
     * 获取实体列表默认存储JSON文件路径
     * @param context 应用上下文
     * @return 文件绝对路径
     */
    public String getBeanListJsonFilePath(final Context context) {
        return context.getExternalFilesDir(TAG) + "/" + getName() + "_List.json";
    }

    // ====================== JSON序列化基础方法 ======================
    /**
     * 写入基础Bean标识字段至JSON
     * @param jsonWriter JSON写入流
     * @throws IOException 写入异常
     */
    public void writeThisToJsonWriter(final JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(BEAN_NAME).value(getName());
    }

    /**
     * 基类通用字段解析回调
     * @param jsonReader JSON读取流
     * @param name 字段名
     * @return 是否完成当前字段解析
     * @throws IOException 读取异常
     */
    public boolean initObjectsFromJsonReader(final JsonReader jsonReader, final String name) throws IOException {
        return false;
    }

    // ====================== 实体字符串序列化 ======================
    @Override
    public String toString() {
        LogUtils.d(TAG, "执行BaseBean实体转JSON字符串");
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("  ");
        try {
            jsonWriter.beginObject();
            writeThisToJsonWriter(jsonWriter);
            jsonWriter.endObject();
            return stringWriter.toString();
        } catch (final IOException e) {
            LogUtils.d(TAG, "实体转JSON字符串异常", Thread.currentThread().getStackTrace());
        }
        return "";
    }

    // ====================== 列表序列化工具 ======================
    /**
     * Bean列表转为格式化JSON数组字符串
     * @param beanList 实体集合
     * @param <T> 实体泛型
     * @return JSON字符串
     */
    public static <T extends BaseBean> String toStringByBeanList(final ArrayList<T> beanList) {
        LogUtils.d(TAG, "执行Bean列表序列化JSON操作");
        try {
            final StringWriter stringWriter = new StringWriter();
            final JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setIndent("  ");
            jsonWriter.beginArray();
            for (int i = 0; i < beanList.size(); i++) {
                jsonWriter.beginObject();
                beanList.get(i).writeThisToJsonWriter(jsonWriter);
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();
            return stringWriter.toString();
        } catch (final IOException e) {
            LogUtils.d(TAG, "列表序列化JSON异常", Thread.currentThread().getStackTrace());
        }
        return "";
    }

    // ====================== JSON字符串解析实体 ======================
    /**
     * JSON文本解析为单个实体对象
     * @param szBean JSON文本
     * @param clazz 实体Class字节码
     * @param <T> 实体泛型
     * @return 解析完成实体
     * @throws IOException 解析IO异常
     */
    public static <T extends BaseBean> T parseStringToBean(final String szBean, final Class<T> clazz) throws IOException {
        LogUtils.d(TAG, "进入字符串解析实体方法，目标Class：" + clazz.getSimpleName());
        final StringReader stringReader = new StringReader(szBean);
        final JsonReader jsonReader = new JsonReader(stringReader);
        try {
            final T beanTemp = clazz.newInstance();
            return (T) beanTemp.readBeanFromJsonReader(jsonReader);
        } catch (final InstantiationException e) {
            LogUtils.d(TAG, "实体反射实例化失败(InstantiationException)", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            LogUtils.d(TAG, "实体反射权限访问失败(IllegalAccessException)", Thread.currentThread().getStackTrace());
        }
        return null;
    }

    /**
     * JSON数组文本解析填充实体列表
     * @param szBeanList JSON数组文本
     * @param beanList 目标存储集合
     * @param clazz 实体字节码
     * @param <T> 实体泛型
     * @return 解析结果
     */
    public static <T extends BaseBean> boolean parseStringToBeanList(final String szBeanList, ArrayList<T> beanList, final Class<T> clazz) {
        LogUtils.d(TAG, "进入列表字符串解析方法");
        try {
            if (beanList == null) {
                beanList = new ArrayList<T>();
            } else {
                beanList.clear();
            }
            final StringReader stringReader = new StringReader(szBeanList);
            final JsonReader jsonReader = new JsonReader(stringReader);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                final T beanTemp = clazz.newInstance();
                final T bean = (T) beanTemp.readBeanFromJsonReader(jsonReader);
                if (bean != null) {
                    beanList.add(bean);
                }
            }
            jsonReader.endArray();
            return true;
        } catch (final InstantiationException e) {
            LogUtils.d(TAG, "列表解析反射实例化异常", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            LogUtils.d(TAG, "列表解析反射权限异常", Thread.currentThread().getStackTrace());
        } catch (final IOException e) {
            LogUtils.d(TAG, "列表解析JSON读写异常", Thread.currentThread().getStackTrace());
        }
        return false;
    }

    // ====================== 文件加载实体 ======================
    /**
     * 从默认路径加载单个实体
     * @param context 上下文
     * @param clazz 实体字节码
     * @param <T> 实体泛型
     * @return 加载实体
     */
    public static <T extends BaseBean> T loadBean(final Context context, final Class<T> clazz) {
        LogUtils.d(TAG, "执行默认路径加载实体数据");
        try {
            final T beanTemp = clazz.newInstance();
            return loadBeanFromFile(beanTemp.getBeanJsonFilePath(context), clazz);
        } catch (final InstantiationException e) {
            LogUtils.d(TAG, "加载实体反射实例化失败", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            LogUtils.d(TAG, "加载实体反射权限失败", Thread.currentThread().getStackTrace());
        }
        return null;
    }

    /**
     * 自定义文件路径加载单个实体
     * @param szFilePath 文件路径
     * @param clazz 实体字节码
     * @param <T> 实体泛型
     * @return 实体对象
     */
    public static <T extends BaseBean> T loadBeanFromFile(final String szFilePath, final Class<T> clazz) {
        LogUtils.d(TAG, "指定路径加载实体，路径：" + szFilePath);
        try {
            final File file = new File(szFilePath);
            if (file.exists()) {
                final T beanTemp = clazz.newInstance();
                final String json = UTF8FileUtils.readStringFromFile(szFilePath);
                return beanTemp.parseStringToBean(json, clazz);
            }
        } catch (final InstantiationException e) {
            LogUtils.d(TAG, "文件加载实体反射实例化异常", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            LogUtils.d(TAG, "文件加载实体反射权限异常", Thread.currentThread().getStackTrace());
        } catch (final IOException e) {
            LogUtils.d(TAG, "文件读取JSON数据异常", Thread.currentThread().getStackTrace());
        }
        return null;
    }

    /**
     * 默认路径加载实体列表
     * @param context 上下文
     * @param beanListDst 目标集合
     * @param clazz 实体字节码
     * @param <T> 实体泛型
     * @return 加载结果
     */
    public static <T extends BaseBean> boolean loadBeanList(final Context context, final ArrayList<T> beanListDst, final Class<T> clazz) {
        LogUtils.d(TAG, "默认路径加载实体列表数据");
        try {
            final T beanTemp = clazz.newInstance();
            return loadBeanListFromFile(beanTemp.getBeanListJsonFilePath(context), beanListDst, clazz);
        } catch (final InstantiationException e) {
            LogUtils.d(TAG, "列表加载反射实例化异常", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            LogUtils.d(TAG, "列表加载反射权限异常", Thread.currentThread().getStackTrace());
        }
        return false;
    }

    /**
     * 自定义路径加载实体列表
     * @param szFilePath 文件路径
     * @param beanList 目标集合
     * @param clazz 实体字节码
     * @param <T> 实体泛型
     * @return 加载结果
     */
    public static <T extends BaseBean> boolean loadBeanListFromFile(final String szFilePath, final ArrayList<T> beanList, final Class<T> clazz) {
        LogUtils.d(TAG, "指定路径加载实体列表，路径：" + szFilePath);
        try {
            final File file = new File(szFilePath);
            if (file.exists()) {
                final String listJson = UTF8FileUtils.readStringFromFile(szFilePath);
                return parseStringToBeanList(listJson, beanList, clazz);
            }
        } catch (final IOException e) {
            LogUtils.d(TAG, "列表文件读取异常", Thread.currentThread().getStackTrace());
        }
        return false;
    }

    // ====================== 实体数据保存文件 ======================
    /**
     * 默认路径保存单个实体
     * @param context 上下文
     * @param bean 待保存实体
     * @param <T> 实体泛型
     * @return 保存结果
     */
    public static <T extends BaseBean> boolean saveBean(final Context context, final T bean) {
        return saveBeanToFile(bean.getBeanJsonFilePath(context), bean);
    }

    /**
     * 自定义路径保存单个实体
     * @param szFilePath 保存路径
     * @param bean 待保存实体
     * @param <T> 实体泛型
     * @return 保存结果
     */
    public static <T extends BaseBean> boolean saveBeanToFile(final String szFilePath, final T bean) {
        LogUtils.d(TAG, "执行实体数据写入文件操作");
        try {
            final String json = bean.toString();
            UTF8FileUtils.writeStringToFile(szFilePath, json);
            return true;
        } catch (final IOException e) {
            LogUtils.d(TAG, "实体写入文件异常", Thread.currentThread().getStackTrace());
        }
        return false;
    }

    /**
     * 默认路径保存实体列表
     * @param context 上下文
     * @param beanList 实体集合
     * @param clazz 实体字节码
     * @param <T> 实体泛型
     * @return 保存结果
     */
    public static <T extends BaseBean> boolean saveBeanList(final Context context, final ArrayList<T> beanList, final Class<T> clazz) {
        LogUtils.d(TAG, "默认路径保存实体列表数据");
        try {
            final T beanTemp = clazz.newInstance();
            return saveBeanListToFile(beanTemp.getBeanListJsonFilePath(context), beanList);
        } catch (final InstantiationException e) {
            LogUtils.d(TAG, "列表保存反射实例化异常", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            LogUtils.d(TAG, "列表保存反射权限异常", Thread.currentThread().getStackTrace());
        }
        return false;
    }

    /**
     * 自定义路径保存实体列表
     * @param szFilePath 保存路径
     * @param beanList 实体集合
     * @param <T> 实体泛型
     * @return 保存结果
     */
    public static <T extends BaseBean> boolean saveBeanListToFile(final String szFilePath, final ArrayList<T> beanList) {
        LogUtils.d(TAG, "指定路径保存实体列表数据");
        try {
            final String json = toStringByBeanList(beanList);
            UTF8FileUtils.writeStringToFile(szFilePath, json);
            return true;
        } catch (final IOException e) {
            LogUtils.d(TAG, "列表数据写入文件异常", Thread.currentThread().getStackTrace());
        }
        return false;
    }

    // ====================== Bean类型一致性校验 ======================
    /**
     * 校验本地JSON列表内实体类型是否统一
     * @param szFilePath 列表文件路径
     * @param clazz 目标校验实体Class
     * @param <T> 实体泛型
     * @return 校验结果信息
     */
    public static <T extends BaseBean> String checkIsTheSameBeanListAndFile(final String szFilePath, final Class<T> clazz) {
        final StringBuilder sbResult = new StringBuilder();
        final String szErrorInfo = "Check Is The Same Bean List And File Error : ";
        try {
            int sameCount = 0;
            int totalCount = 0;
            final T beanTemp = clazz.newInstance();
            final String targetBeanName = beanTemp.getName();
            final String listJson = UTF8FileUtils.readStringFromFile(szFilePath);
            final StringReader stringReader = new StringReader(listJson);
            final JsonReader jsonReader = new JsonReader(stringReader);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                totalCount++;
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    final String name = jsonReader.nextName();
                    if (BEAN_NAME.equals(name)) {
                        if (targetBeanName.equals(jsonReader.nextString())) {
                            sameCount++;
                        }
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
            }
            jsonReader.endArray();
            if (sameCount != totalCount) {
                sbResult.append("Total : ").append(totalCount).append(" Diff : ").append(totalCount - sameCount);
            }
        } catch (final InstantiationException e) {
            sbResult.append(szErrorInfo).append(e);
            LogUtils.d(TAG, "类型校验反射实例化异常", Thread.currentThread().getStackTrace());
        } catch (final IllegalAccessException e) {
            sbResult.append(szErrorInfo).append(e);
            LogUtils.d(TAG, "类型校验反射权限异常", Thread.currentThread().getStackTrace());
        } catch (final IOException e) {
            sbResult.append(szErrorInfo).append(e);
            LogUtils.d(TAG, "类型校验文件读取解析异常", Thread.currentThread().getStackTrace());
        }
        return sbResult.toString();
    }
}

