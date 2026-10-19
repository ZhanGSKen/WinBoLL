package cc.winboll.studio.contacts.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telecom.CallScreeningService;
import android.telephony.TelephonyManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import cc.winboll.studio.contacts.model.MainServiceBean;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe 通话筛选服务（无前台服务），负责识别通话类型、拦截指定号码、处理正常通话逻辑
 * 严格适配 Java7 语法 + Android API29-30 | 轻量稳定 | 强化空指针防护 | 无冗余代码
 */
@RequiresApi(api = 29) // 父类 CallScreeningService 最低要求 API29，明确标注
public class MyCallScreeningService extends CallScreeningService {

    // ====================== 常量定义区（全硬编码，无高版本API依赖） ======================
    public static final String TAG = "MyCallScreeningService";

    // 通话方向常量（硬编码替代 Call.Details 高版本字段，适配API29-30）
    private static final int CALL_DIRECTION_INCOMING = 1; // 来电
    private static final int CALL_DIRECTION_OUTGOING = 2; // 外拨

    // ====================== 成员属性区（精简必要属性，命名规范） ======================
    private Context mContext; // 上下文对象，避免重复调用 getApplicationContext()

    // ====================== Service生命周期方法区（按执行顺序排列） ======================
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        LogUtils.d(TAG, "===== onCreate: 通话筛选服务启动 =====");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand: 服务被启动，startId=" + startId);

        // 加载服务配置，决定重启策略（启用则自动重启，禁用则默认）
        MainServiceBean serviceConfig = MainServiceBean.loadBean(this, MainServiceBean.class);
        int startMode = (serviceConfig != null && serviceConfig.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
        LogUtils.d(TAG, "onStartCommand: 服务启动模式：" + (startMode == START_STICKY ? "START_STICKY（自动重启）" : "默认模式"));

        return startMode;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 置空上下文，释放引用，避免内存泄漏
        mContext = null;
        LogUtils.d(TAG, "===== onDestroy: 通话筛选服务销毁完成 =====");
    }

    // ====================== 核心通话筛选方法区（父类抽象方法实现） ======================
    /**
     * 核心：通话筛选入口（API29-30标准方法，100%兼容）
     * 功能：识别通话号码/类型、拦截指定号码、处理正常通话
     */
    @Override
    @RequiresApi(api = 29)
    public void onScreenCall(@NonNull android.telecom.Call.Details details) {
        LogUtils.d(TAG, "===== onScreenCall: 开始筛选通话 =====");

        // 1. 安全获取通话号码（多层空指针防护，Java7规范写法）
        String phoneNumber = getSafePhoneNumber(details);
        // 2. 识别通话方向（来电/外拨/未知）
        int callDirection = details.getCallDirection();
        String callTypeDesc = getCallDirectionDesc(callDirection);
        int callState = getCallStateByDirection(callDirection);

        LogUtils.d(TAG, "筛选结果：通话类型=" + callTypeDesc + "，号码=" + phoneNumber);

        // 3. 自定义拦截逻辑（示例：拦截指定号码10086，可按需扩展黑白名单）
        boolean isNeedBlock = isTargetBlockNumber(phoneNumber);

        // 4. 构建筛选响应（Java7分步调用，不使用链式写法，避免兼容问题）
        CallResponse callResponse = buildCallScreeningResponse(isNeedBlock);

        // 5. 提交筛选结果（必须调用父类方法，完成拦截/放行逻辑）
        respondToCall(details, callResponse);

        // 6. 分场景处理后续逻辑（拦截日志/正常通话业务）
        handleCallAfterScreening(phoneNumber, callTypeDesc, isNeedBlock);

        LogUtils.d(TAG, "===== onScreenCall: 通话筛选完成 =====");
    }

    // ====================== 业务逻辑方法区（按功能拆分，低耦合） ======================
    /**
     * 安全获取通话号码（多层空指针+空字符串防护，避免崩溃）
     */
    private String getSafePhoneNumber(android.telecom.Call.Details details) {
        String phoneNumber = "未知号码";
        if (details == null) {
            LogUtils.w(TAG, "getSafePhoneNumber: 通话详情为空，无法获取号码");
            return phoneNumber;
        }

        Uri handle = details.getHandle();
        if (handle != null) {
            String schemePart = handle.getSchemeSpecificPart();
            if (schemePart != null && !schemePart.trim().isEmpty()) {
                phoneNumber = schemePart.trim();
                LogUtils.d(TAG, "getSafePhoneNumber: 成功获取号码，原始值=" + schemePart + "，处理后=" + phoneNumber);
            } else {
                LogUtils.w(TAG, "getSafePhoneNumber: 号码格式异常，schemePart=" + schemePart);
            }
        } else {
            LogUtils.w(TAG, "getSafePhoneNumber: 通话 handle 为空，无法获取号码");
        }
        return phoneNumber;
    }

    /**
     * 判断是否为目标拦截号码（可扩展黑白名单逻辑，当前示例拦截10086）
     */
    private boolean isTargetBlockNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            LogUtils.w(TAG, "isTargetBlockNumber: 号码为空，不拦截");
            return false;
        }

        // 示例拦截逻辑：拦截 10086（实际可替换为黑白名单查询）
        boolean isBlock = "10086".equals(phoneNumber.trim());
        if (isBlock) {
            LogUtils.d(TAG, "isTargetBlockNumber: 命中拦截规则，号码=" + phoneNumber);
        } else {
            LogUtils.d(TAG, "isTargetBlockNumber: 未命中拦截规则，号码=" + phoneNumber);
        }
        return isBlock;
    }

    /**
     * 构建通话筛选响应（按需配置拦截/放行参数，适配API29-30）
     */
    private CallResponse buildCallScreeningResponse(boolean isNeedBlock) {
        CallResponse.Builder responseBuilder = new CallResponse.Builder();
        // 拦截配置：是否禁止通话+是否拒绝通话（两者配合实现拦截）
        responseBuilder.setDisallowCall(isNeedBlock);
        responseBuilder.setRejectCall(isNeedBlock);
        // 日志/通知配置：拦截的通话跳过日志和通知，正常通话保留
        responseBuilder.setSkipCallLog(isNeedBlock);
        responseBuilder.setSkipNotification(isNeedBlock);

        CallResponse response = responseBuilder.build();
        LogUtils.d(TAG, "buildCallScreeningResponse: 响应构建完成，拦截状态=" + isNeedBlock);
        return response;
    }

    /**
     * 筛选后分场景处理（拦截日志/正常通话业务扩展）
     */
    private void handleCallAfterScreening(String phoneNumber, String callTypeDesc, boolean isNeedBlock) {
        if (isNeedBlock) {
            // 拦截场景：仅打日志（可扩展：添加拦截记录、本地存储等）
            LogUtils.d(TAG, "handleCallAfterScreening: 已拦截通话，类型=" + callTypeDesc + "，号码=" + phoneNumber);
        } else {
            // 正常通话场景：处理业务逻辑（可扩展：通话记录、广播通知、号码识别等）
            int callState = getCallStateByDirection(getCallDirectionFromDesc(callTypeDesc));
            handleNormalCallBusiness(phoneNumber, callState);
        }
    }

    /**
     * 正常通话业务处理（核心业务扩展入口，强化空指针防护）
     */
    private void handleNormalCallBusiness(String phoneNumber, int callState) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            LogUtils.w(TAG, "handleNormalCallBusiness: 号码为空，跳过业务处理");
            return;
        }

        String callStateDesc = getCallStateDesc(callState);
        LogUtils.d(TAG, "handleNormalCallBusiness: 处理正常通话业务，号码=" + phoneNumber + "，状态=" + callStateDesc);

        // 此处可扩展业务逻辑（示例）：
        // 1. 保存通话记录到本地
        // 2. 发送广播通知其他组件（如通话监听服务）
        // 3. 调用号码识别接口，匹配联系人信息
    }

    // ====================== 工具辅助方法区（统一归类，复用性强） ======================
    /**
     * 通话方向转文字描述（便于日志查看，快速定位场景）
     */
    private String getCallDirectionDesc(int callDirection) {
        switch (callDirection) {
            case CALL_DIRECTION_INCOMING:
                return "来电";
            case CALL_DIRECTION_OUTGOING:
                return "外拨";
            default:
                return "未知通话";
        }
    }

    /**
     * 文字描述转通话方向（配合业务逻辑反向匹配，避免重复判断）
     */
    private int getCallDirectionFromDesc(String callTypeDesc) {
        if ("来电".equals(callTypeDesc)) {
            return CALL_DIRECTION_INCOMING;
        } else if ("外拨".equals(callTypeDesc)) {
            return CALL_DIRECTION_OUTGOING;
        } else {
            return -1; // 未知方向
        }
    }

    /**
     * 通话方向转 TelephonyManager 状态（统一状态标准，便于业务复用）
     */
    private int getCallStateByDirection(int callDirection) {
        switch (callDirection) {
            case CALL_DIRECTION_INCOMING:
                return TelephonyManager.CALL_STATE_RINGING; // 来电=响铃中
            case CALL_DIRECTION_OUTGOING:
                return TelephonyManager.CALL_STATE_OFFHOOK; // 外拨=通话中
            default:
                return TelephonyManager.CALL_STATE_IDLE; // 未知=空闲
        }
    }

    /**
     * TelephonyManager 状态转文字描述（统一日志格式，提升可读性）
     */
    private String getCallStateDesc(int callState) {
        switch (callState) {
            case TelephonyManager.CALL_STATE_RINGING:
                return "响铃中";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "通话中";
            case TelephonyManager.CALL_STATE_IDLE:
                return "空闲";
            default:
                return "未知状态";
        }
    }
}

