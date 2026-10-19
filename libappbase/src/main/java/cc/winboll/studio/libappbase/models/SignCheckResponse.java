package cc.winboll.studio.libappbase.models;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/22 20:37
 */
// ==================== JSON响应模型（与后端返回字段完全匹配）====================
public class SignCheckResponse {
    private int code;                // 根节点code（后端返回）
    private String msg;              // 根节点提示信息（后端返回，替换原message）
    private DataBean data;           // 根节点data对象（后端返回）

    // 内部DataBean：对应后端返回的data字段内容
    public static class DataBean {
        private boolean valid;       // 实际是否合法的标识（后端data.valid）
        private String signature;    // 加密后的签名
        private String decryptedSign;// 解密后的原始签名
        private long validTime;      // 时间戳
    }

    // Getter/Setter（关键：获取data中的valid字段）
    public boolean isValid() {
        return data != null && data.valid; // 从data中获取valid值
    }

    public String getMessage() {
        return msg; // 对应后端根节点的msg字段
    }

    // 其他必要的Getter/Setter（用于后续扩展）
    public int getCode() {
        return code;
    }

    public DataBean getData() {
        return data;
    }
}

