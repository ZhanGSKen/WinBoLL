package cc.winboll.studio.winboll.utils;

import cc.winboll.studio.winboll.WxPayConfig;
import com.alibaba.fastjson.JSONObject;

/**
 * 微信支付服务端接口封装
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/07
 */
public class WxPayApi {

    /**
     * 统一下单（生成二维码）
     * @param callback 回调
     */
    public static void createOrder(final OnCreateOrderCallback callback) {
        // 拼接请求参数（服务端测试接口需支持GET传参，若为POST需修改为表单/JSON）
        String url = WxPayConfig.CREATE_ORDER_URL +
			"?body=" + WxPayConfig.ORDER_BODY +
			"&totalFee=" + WxPayConfig.TOTAL_FEE +
			"&tradeType=" + WxPayConfig.TRADE_TYPE;

        OkHttpUtil.get(url, new OkHttpUtil.OnResultCallback() {
				@Override
				public void onSuccess(String result) {
					try {
						JSONObject jsonObject = JSONObject.parseObject(result);
						String outTradeNo = jsonObject.getString("out_trade_no");
						String codeUrl = jsonObject.getString("code_url");
						if (callback != null) {
							callback.onSuccess(outTradeNo, codeUrl);
						}
					} catch (Exception e) {
						if (callback != null) {
							callback.onFailure("解析统一下单结果失败：" + e.getMessage());
						}
					}
				}

				@Override
				public void onFailure(String errorMsg) {
					if (callback != null) {
						callback.onFailure("统一下单请求失败：" + errorMsg);
					}
				}
			});
    }

    /**
     * 订单查询
     * @param outTradeNo 商户订单号
     * @param callback 回调
     */
    public static void queryOrder(String outTradeNo, final OnQueryOrderCallback callback) {
        String url = WxPayConfig.QUERY_ORDER_URL + "?outTradeNo=" + outTradeNo;

        OkHttpUtil.get(url, new OkHttpUtil.OnResultCallback() {
				@Override
				public void onSuccess(String result) {
					try {
						JSONObject jsonObject = JSONObject.parseObject(result);
						String tradeState = jsonObject.getString("trade_state");
						boolean isSuccess = "SUCCESS".equals(tradeState);
						if (callback != null) {
							callback.onSuccess(isSuccess, tradeState);
						}
					} catch (Exception e) {
						if (callback != null) {
							callback.onFailure("解析订单查询结果失败：" + e.getMessage());
						}
					}
				}

				@Override
				public void onFailure(String errorMsg) {
					if (callback != null) {
						callback.onFailure("订单查询请求失败：" + errorMsg);
					}
				}
			});
    }

    /**
     * 统一下单回调接口
     */
    public interface OnCreateOrderCallback {
        void onSuccess(String outTradeNo, String codeUrl);
        void onFailure(String errorMsg);
    }

    /**
     * 订单查询回调接口
     */
    public interface OnQueryOrderCallback {
        void onSuccess(boolean isPaySuccess, String tradeState);
        void onFailure(String errorMsg);
    }
}

