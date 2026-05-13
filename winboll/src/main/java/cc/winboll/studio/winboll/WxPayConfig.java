package cc.winboll.studio.winboll;

/**
 * 微信支付配置类
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/07
 */
public class WxPayConfig {
    // ========== 核心修改点：替换为你的服务端地址 ==========
    // 服务端IP/域名 + 端口（Docker部署的服务端，需确保安卓端可访问）
    public static final String BASE_URL = "https://wxpay.winboll.cc";

    // 统一下单接口路径（对应服务端的测试接口）
    public static final String CREATE_ORDER_URL = BASE_URL + "/pay/createOrder";

    // 订单查询接口路径
    public static final String QUERY_ORDER_URL = BASE_URL + "/pay/queryOrder";

    // ========== 固定支付配置 ==========
    public static final String ORDER_BODY = "定额测试支付"; // 商品描述
    public static final int TOTAL_FEE = 1; // 固定金额：1分（沙箱环境推荐）
    public static final String TRADE_TYPE = "NATIVE"; // 支付类型：二维码

    // ========== 轮询配置 ==========
    public static final long POLL_INTERVAL = 10000; // 轮询间隔：10秒
    public static final long POLL_TIMEOUT = 45000; // 轮询超时：45秒
}

