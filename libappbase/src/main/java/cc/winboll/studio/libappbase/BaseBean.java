package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/15 11:11:52
 * @Describe Json Bean 基础类。
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public abstract class BaseBean<T extends BaseBean> {

    public static final String TAG = "BaseBean";
    static final String BEAN_NAME = "BeanName";

    public BaseBean() {}

    public abstract String getName();

    public String getBeanJsonFilePath(Context context) {

        return context.getExternalFilesDir(TAG) + "/" + getName() + ".json";
    }

    public String getBeanListJsonFilePath(Context context) {

        return context.getExternalFilesDir(TAG) + "/" + getName() + "_List.json";
    }

    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(BEAN_NAME).value(getName());
    }

    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        return false;
    }

    abstract public T readBeanFromJsonReader(JsonReader jsonReader) throws IOException;

    public static <T extends BaseBean> String checkIsTheSameBeanListAndFile(String szFilePath, Class<T> clazz) {
        StringBuilder sbResult = new StringBuilder();
        String szErrorInfo = "Check Is The Same Bean List And File Error : ";

        try {
            int nSameCount = 0;
            int nBeanListCout = 0;

            T beanTemp = clazz.newInstance();
            String szBeanSimpleName = beanTemp.getName();
            String szListJson = FileUtils.readStringFromFile(szFilePath);
            StringReader stringReader = new StringReader(szListJson);
            JsonReader jsonReader = new JsonReader(stringReader);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                nBeanListCout++;
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();
                    if (name.equals(BEAN_NAME)) {
                        if (szBeanSimpleName.equals(jsonReader.nextString())) {
                            nSameCount++;
                        }
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
            }
            jsonReader.endArray();

            // 返回检查结果
            if (nSameCount == nBeanListCout) {
                // 检查一致直接返回空串
                return "";
            } else {
                // 检查不一致返回对比信息
                sbResult.append("Total : ");
                sbResult.append(nBeanListCout);
                sbResult.append(" Diff : ");
                sbResult.append(nBeanListCout - nSameCount);
            }
        } catch (InstantiationException e) {
            sbResult.append(szErrorInfo);
            sbResult.append(e);
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IllegalAccessException e) {
            sbResult.append(szErrorInfo);
            sbResult.append(e);
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IOException e) {
            sbResult.append(szErrorInfo);
            sbResult.append(e);
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return sbResult.toString();
    }

    public static <T extends BaseBean> T parseStringToBean(String szBean, Class<T> clazz) throws IOException {
        // 创建 JsonWriter 对象
        StringReader stringReader = new StringReader(szBean);
        JsonReader jsonReader = new JsonReader(stringReader);
        try {
            T beanTemp = clazz.newInstance();
            return (T)beanTemp.readBeanFromJsonReader(jsonReader);
        } catch (InstantiationException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IllegalAccessException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return null;
    }

    public static <T extends BaseBean> boolean parseStringToBeanList(String szBeanList, ArrayList<T> beanList, Class<T> clazz) {
        try {
            beanList.clear();
            StringReader stringReader = new StringReader(szBeanList);
            JsonReader jsonReader = new JsonReader(stringReader);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                T beanTemp = clazz.newInstance();
                T bean = (T)beanTemp.readBeanFromJsonReader(jsonReader);
                if (bean != null) {
                    beanList.add(bean);
                    //LogUtils.d(TAG, "beanList.add(bean)");
                }
            }
            jsonReader.endArray();
            return true;
            //LogUtils.d(TAG, "beanList.size() is " + Integer.toString(beanList.size()));
        } catch (InstantiationException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IllegalAccessException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }

    @Override
    public String toString() {
        // 创建 JsonWriter 对象
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("  ");
        try {// 开始 JSON 对象
            jsonWriter.beginObject();
            // 写入键值对
            writeThisToJsonWriter(jsonWriter);
            // 结束 JSON 对象
            jsonWriter.endObject();
            return stringWriter.toString();
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        // 获取 JSON 字符串
        return "";
    }

    public static <T extends BaseBean> String toStringByBeanList(ArrayList<T> beanList) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setIndent("  ");
            jsonWriter.beginArray();
            for (int i = 0; i < beanList.size(); i++) {
                // 开始 JSON 对象
                jsonWriter.beginObject();
                // 写入键值对
                beanList.get(i).writeThisToJsonWriter(jsonWriter);
                // 结束 JSON 对象
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();
            return stringWriter.toString();
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return "";
    }


    public static <T extends BaseBean> T loadBean(Context context, Class<T> clazz) {
        try {
            T beanTemp = clazz.newInstance();
            return loadBeanFromFile(beanTemp.getBeanJsonFilePath(context), clazz);
        } catch (InstantiationException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IllegalAccessException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return null;
    }

    public static <T extends BaseBean> T loadBeanFromFile(String szFilePath, Class<T> clazz) {
        try {
            try {
                File fTemp = new File(szFilePath);
                if (fTemp.exists()) {
                    T beanTemp = clazz.newInstance();String szJson = FileUtils.readStringFromFile(szFilePath);
                    return beanTemp.parseStringToBean(szJson, clazz);
                }
            } catch (InstantiationException e) {
                LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
            } catch (IllegalAccessException e) {
                LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
            }

        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return null;
    }

    public static <T extends BaseBean> boolean saveBean(Context context, T bean) {
        return saveBeanToFile(bean.getBeanJsonFilePath(context), bean);
    }

    public static <T extends BaseBean> boolean saveBeanToFile(String szFilePath, T bean) {
        try {
            String szJson = bean.toString();
            FileUtils.writeStringToFile(szFilePath, szJson);
            return true;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }

    public static <T extends BaseBean> boolean loadBeanList(Context context, ArrayList<T> beanListDst, Class<T> clazz) {
        try {
            T beanTemp = clazz.newInstance();
            return loadBeanListFromFile(beanTemp.getBeanListJsonFilePath(context), beanListDst, clazz);
        } catch (InstantiationException e) {} catch (IllegalAccessException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }

    public static <T extends BaseBean> boolean loadBeanListFromFile(String szFilePath, ArrayList<T> beanList, Class<T> clazz) {
        try {
            File fTemp = new File(szFilePath);
            if (fTemp.exists()) {
                String szListJson = FileUtils.readStringFromFile(szFilePath);
                return parseStringToBeanList(szListJson, beanList, clazz);
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }

    public static <T extends BaseBean> boolean saveBeanList(Context context, ArrayList<T> beanList, Class<T> clazz) {
        try {
            T beanTemp = clazz.newInstance();
            return saveBeanListToFile(beanTemp.getBeanListJsonFilePath(context), beanList);
        } catch (InstantiationException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } catch (IllegalAccessException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }

    public static <T extends BaseBean> boolean saveBeanListToFile(String szFilePath, ArrayList<T> beanList) {
        try {
            String szJson = toStringByBeanList(beanList);
            FileUtils.writeStringToFile(szFilePath, szJson);
            //LogUtils.d(TAG, "FileUtil.writeFile beanList.size() is " + Integer.toString(beanList.size()));
            return true;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }
}
