package cc.winboll.studio.libaes.enums;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/27 12:35
 * @Describe 隐私协议签约状态枚举
 * 对应值：0-拒绝，1-赞同，2-未签约（默认）
 */
public enum PrivacyAgreeStatus {
	REJECTED(0, "拒绝"),      // 0: 拒绝隐私协议
	AGREED(1, "赞同"),        // 1: 赞同隐私协议
	UN_SIGNED(2, "未签约");   // 2: 未签约（初始默认状态）

	private final int statusCode;  // 对应存储的int值
	private final String statusDesc; // 状态描述（可选，便于日志/UI显示）

	// Java 7 枚举构造方法（必须private）
	private PrivacyAgreeStatus(int statusCode, String statusDesc) {
		this.statusCode = statusCode;
		this.statusDesc = statusDesc;
	}

	/**
	 * 根据int值获取枚举（SP读取时使用，兼容Java 7）
	 * @param code 存储的int值（0/1/2）
	 * @return 对应枚举，默认返回UN_SIGNED（未签约）
	 */
	public static PrivacyAgreeStatus fromCode(int code) {
		// Java 7 不支持switch(String)，用if-else兼容
		if (code == REJECTED.statusCode) {
			return REJECTED;
		} else if (code == AGREED.statusCode) {
			return AGREED;
		} else {
			return UN_SIGNED; // 默认未签约
		}
	}

	/**
	 * 根据SP存储的字符串值获取枚举（兼容原逻辑中String类型存储）
	 * @param codeStr 存储的字符串值（"0"/"1"/"2"）
	 * @return 对应枚举，默认返回UN_SIGNED（未签约）
	 */
	public static PrivacyAgreeStatus fromString(String codeStr) {
		if (codeStr == null) {
			return UN_SIGNED;
		}
		try {
			int code = Integer.parseInt(codeStr);
			return fromCode(code);
		} catch (NumberFormatException e) {
			// 字符串格式异常时，默认返回未签约
			return UN_SIGNED;
		}
	}

	// 获取状态码（用于存储到SP）
	public int getStatusCode() {
		return statusCode;
	}

	// 获取状态描述（用于日志/UI显示，可选）
	public String getStatusDesc() {
		return statusDesc;
	}
}

