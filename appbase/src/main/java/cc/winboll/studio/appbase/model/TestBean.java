package cc.winboll.studio.appbase.model;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;

/**
 * 测试实体类
 * 继承BaseBean实现JSON序列化/反序列化能力，提供基础int类型属性的封装与数据持久化支持
 * 适配Java7语法，遵循BaseBean统一的反射识别、JSON读写规范
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/31 19:16:00
 * @LastEditTime 2026/02/01 10:46:00
 */
public class TestBean extends BaseBean {

    // ====================================== 常量定义 ======================================
    /** 当前类的日志 TAG（用于调试输出） */
    public static final String TAG = "TestBean";

    // ====================================== 成员属性 ======================================
    /**
     * 测试数字属性（默认值：123）
     * 基础int类型属性，用于测试BaseBean的JSON序列化/反序列化能力
     */
    private int testNum1;

    // ====================================== 构造方法 ======================================
    /**
     * 无参构造器（默认初始化）
     * 给testNum1赋值默认值123，满足反射实例化、JSON解析的无参构造要求
     */
    public TestBean() {
        this.testNum1 = 123;
        LogUtils.d(TAG, "TestBean无参构造器调用，testNum1默认初始化值：" + this.testNum1);
    }

    /**
     * 有参构造器（自定义初始化）
     * @param testNum1 测试数字初始值
     */
    public TestBean(int testNum1) {
        this.testNum1 = testNum1;
        LogUtils.d(TAG, "TestBean有参构造器调用，传入testNum1：" + testNum1);
    }

    // ====================================== Get/Set 方法 ======================================
    /**
     * 设置测试数字属性值
     * @param testNum1 待设置的int类型值
     */
    public void setTestNum1(int testNum1) {
        LogUtils.d(TAG, "setTestNum1调用，传入参数：" + testNum1);
        this.testNum1 = testNum1;
    }

    /**
     * 获取测试数字属性值
     * @return 当前testNum1的int类型值
     */
    public int getTestNum1() {
        LogUtils.d(TAG, "getTestNum1调用，返回值：" + this.testNum1);
        return testNum1;
    }

    // ====================================== 重写父类BaseBean方法 ======================================
    /**
     * 重写父类方法：获取当前类的全限定名
     * 用于BaseBean反射识别、类名匹配等统一逻辑
     * @return 类全限定名（cc.winboll.studio.appbase.model.TestBean）
     */
    @Override
    public String getName() {
        LogUtils.d(TAG, "getName方法调用，返回类全限定名：" + TestBean.class.getName());
        return TestBean.class.getName();
    }

    /**
     * 重写父类方法：将当前对象序列化为JSON（持久化存储专用）
     * 遵循BaseBean规范，先执行父类序列化逻辑，再处理子类专属字段
     * @param jsonWriter JSON写入器（外部传入的JSON流操作实例）
     * @throws IOException JSON写入异常（流关闭、格式错误等）
     */
    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        LogUtils.d(TAG, "writeThisToJsonWriter调用，传入参数JsonWriter：" + jsonWriter);
        // 执行父类公共字段的序列化逻辑
        super.writeThisToJsonWriter(jsonWriter);
        // 序列化子类专属字段testNum1
        jsonWriter.name("testNum1").value(this.getTestNum1());
        LogUtils.d(TAG, "writeThisToJsonWriter执行完成，已序列化testNum1：" + this.getTestNum1());
    }

    /**
     * 重写父类方法：从JSON字段初始化当前对象属性（解析JSON专用）
     * 先让父类处理公共字段，再匹配子类专属字段，不匹配则返回false跳过
     * @param jsonReader JSON读取器（外部传入的JSON流操作实例）
     * @param name 当前解析的JSON字段名
     * @return true-字段解析成功；false-字段不匹配，需跳过/父类处理
     * @throws IOException JSON读取异常（字段类型不匹配、流中断等）
     */
    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        LogUtils.d(TAG, "initObjectsFromJsonReader调用，传入参数：name=" + name + "，JsonReader=" + jsonReader);
        // 父类优先处理公共字段，处理成功则直接返回
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            LogUtils.d(TAG, "initObjectsFromJsonReader：字段" + name + "由父类BaseBean处理成功");
            return true;
        }
        // 解析子类专属字段
        if ("testNum1".equals(name)) {
            this.setTestNum1(jsonReader.nextInt());
            LogUtils.d(TAG, "initObjectsFromJsonReader：解析testNum1成功，值为：" + this.getTestNum1());
        } else {
            LogUtils.w(TAG, "initObjectsFromJsonReader：字段" + name + "不匹配，返回false跳过解析");
            // 字段不匹配，返回false表示跳过
            return false;
        }
        return true;
    }

    /**
     * 重写父类方法：从JSON读取器完整解析并初始化当前对象（JSON解析入口）
     * 负责JSON对象的开始/结束标识处理，遍历所有字段并调用字段解析方法
     * 严格遵循writeThisToJsonWriter的序列化结构，保证解析一致性
     * @param jsonReader JSON读取器（外部传入的JSON流操作实例）
     * @return 解析后的当前TestBean实例（支持链式调用）
     * @throws IOException JSON解析异常（格式错误、字段缺失、流异常等）
     */
    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        LogUtils.d(TAG, "readBeanFromJsonReader调用，传入参数JsonReader：" + jsonReader);
        // 开始解析JSON对象，与序列化结构保持一致
        jsonReader.beginObject();
        // 遍历所有JSON字段
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            LogUtils.d(TAG, "readBeanFromJsonReader：开始解析字段，fieldName=" + fieldName);
            // 解析字段，不匹配则跳过该值
            if (!this.initObjectsFromJsonReader(jsonReader, fieldName)) {
                jsonReader.skipValue();
                LogUtils.w(TAG, "readBeanFromJsonReader：字段" + fieldName + "解析失败，已跳过该值");
            }
        }
        // 结束JSON对象解析，必须调用避免流异常
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader执行完成，JSON解析结束，当前TestBean实例testNum1：" + this.getTestNum1());
        // 返回当前实例，支持链式调用
        return this;
    }
}

