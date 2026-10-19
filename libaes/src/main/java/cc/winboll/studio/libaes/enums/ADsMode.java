package cc.winboll.studio.libaes.enums;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/26 17:49
 * @Describe 广告控制模式枚举
 */
public enum ADsMode {
    STANDALONE("单机模式"),  // 单机模式（默认）
    MIMO_SDK("米盟广告SDK支持模式"),  // 米盟广告SDK模式
    STORE_QRCODE("云宝物语模式");  // 米盟广告SDK模式

    private final String modeName;

    ADsMode(String modeName) {
        this.modeName = modeName;
    }

    public String getModeName() {
        return modeName;
    }

    // 根据保存的字符串值解析枚举（SP读取时使用）
    public static ADsMode fromValue(String value) {
        if (value == null) return STANDALONE;
        try {
            return ADsMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return STANDALONE;
        }
    }
}
