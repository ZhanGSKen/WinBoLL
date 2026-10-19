package cc.winboll.studio.libappbase.common;

import android.graphics.Color;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author 豆包&Big Pickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @CreateDate 2026/07/21 11:55:00
 * @LastEditDate 2026/07/21 11:56:46
 * @Describe 全局静态控件颜色常量表
 * 命名规范：控件类型+状态/用途+Color
 * 全部常量基于android.graphics.Color原生色值定义，统一控件前缀命名规则，无实例化入口，纯静态常量工具类
 * Java7兼容标准，无高版本语法特性，所有函数入参、内部临时变量强制final修饰
 */
public final class ViewColorTable {

    // 私有构造，禁止外部实例化
    private ViewColorTable() {
        final String errMsg = "Cannot instantiate static class ViewColorTable";
        LogUtils.e("ViewColorTable", "constructor invoke failed, msg = " + errMsg);
        throw new AssertionError(errMsg);
    }

    // ====================== TextView 文本控件颜色常量 ======================
    // 主文本
    public static final int TextPrimaryColor = Color.BLACK;
    // 次要说明文本
    public static final int TextSecondaryColor = Color.GRAY;
    // 提示占位文字
    public static final int TextHintColor = Color.LTGRAY;
    // 错误警告文本
    public static final int TextErrorColor = Color.RED;
    // 链接可点击文本
    public static final int TextLinkColor = Color.parseColor("#007AFF");
    // 禁用置灰文本
    public static final int TextDisabledColor = Color.DKGRAY;
    // 标题大号文本
    public static final int TextTitleColor = Color.BLACK;
    // 白色背景下浅色标题
    public static final int TextTitleLightColor = Color.WHITE;

    // ====================== Button 普通按钮颜色常量 ======================
    // 按钮正常文字
    public static final int ButtonTextNormalColor = Color.WHITE;
    // 按钮按下文字
    public static final int ButtonTextPressedColor = Color.LTGRAY;
    // 按钮禁用文字
    public static final int ButtonTextDisabledColor = Color.GRAY;
    // 边框按钮文字（空心按钮）
    public static final int ButtonStrokeTextColor = Color.parseColor("#007AFF");

    // ====================== Switch 原生开关控件颜色常量 ======================
    // 开关滑块文字
    public static final int SwitchTextColor = Color.WHITE;
    // 开关轨道 关闭状态
    public static final int SwitchTrackNormalColor = Color.GRAY;
    // 开关轨道 开启状态
    public static final int SwitchTrackCheckedColor = Color.parseColor("#34C759");
    // 开关滑块
    public static final int SwitchThumbColor = Color.WHITE;

    // ====================== EditText 输入框颜色常量 ======================
    // 输入正文文字
    public static final int EditTextContentColor = Color.BLACK;
    // 输入框提示文字
    public static final int EditTextHintColor = Color.GRAY;
    // 输入框光标
    public static final int EditTextCursorColor = Color.parseColor("#007AFF");
    // 输入框底部线 正常
    public static final int EditTextLineNormalColor = Color.LTGRAY;
    // 输入框底部线 聚焦
    public static final int EditTextLineFocusedColor = Color.parseColor("#007AFF");
    // 输入框底部线 错误
    public static final int EditTextLineErrorColor = Color.RED;

    // ====================== CheckBox 多选框颜色常量 ======================
    // 多选框文字
    public static final int CheckBoxTextColor = Color.BLACK;
    // 多选框勾选状态
    public static final int CheckBoxCheckedColor = Color.parseColor("#007AFF");
    // 多选框未勾选边框
    public static final int CheckBoxUnCheckStrokeColor = Color.GRAY;

    // ====================== RadioButton 单选框颜色常量 ======================
    public static final int RadioTextColor = Color.BLACK;
    public static final int RadioCheckedCircleColor = Color.parseColor("#007AFF");
    public static final int RadioUnCheckedStrokeColor = Color.GRAY;

    // ====================== ProgressBar 进度条颜色常量 ======================
    // 进度底色
    public static final int ProgressTrackColor = Color.LTGRAY;
    // 已完成进度
    public static final int ProgressValueColor = Color.parseColor("#34C759");
    // 缓冲进度（视频/文件下载）
    public static final int ProgressBufferColor = Color.GRAY;

    // ====================== SeekBar 拖动条颜色常量 ======================
    public static final int SeekBarTrackNormalColor = Color.LTGRAY;
    public static final int SeekBarTrackProgressColor = Color.parseColor("#007AFF");
    public static final int SeekBarThumbColor = Color.parseColor("#007AFF");

    // ====================== TabLayout 标签栏颜色常量 ======================
    // Tab未选中文字
    public static final int TabTextNormalColor = Color.GRAY;
    // Tab选中文字
    public static final int TabTextSelectedColor = Color.parseColor("#007AFF");
    // Tab底部指示器
    public static final int TabIndicatorColor = Color.parseColor("#007AFF");

    // ====================== Divider 分割线颜色常量 ======================
    public static final int DividerLineColor = Color.parseColor("#EEEEEE");
    public static final int DividerDarkLineColor = Color.parseColor("#DDDDDD");

    // ====================== CardView 卡片颜色常量 ======================
    // 卡片背景
    public static final int CardSurfaceColor = Color.WHITE;
    // 卡片边框
    public static final int CardStrokeColor = Color.parseColor("#EEEEEE");

    // ====================== Dialog / Popup 弹窗颜色常量 ======================
    // 弹窗标题文字
    public static final int DialogTitleTextColor = Color.BLACK;
    // 弹窗内容文字
    public static final int DialogContentTextColor = Color.GRAY;
    // 弹窗取消按钮文字
    public static final int DialogCancelTextColor = Color.GRAY;
    // 弹窗确认按钮文字
    public static final int DialogConfirmTextColor = Color.parseColor("#007AFF");
    // 弹窗遮罩背景
    public static final int DialogMaskBgColor = 0xCC000000;

    // ====================== Toolbar / TopBar 顶部栏颜色常量 ======================
    public static final int ToolbarTitleTextColor = Color.WHITE;
    public static final int ToolbarSubTitleTextColor = Color.LTGRAY;
    public static final int ToolbarIconTintColor = Color.WHITE;

    // ====================== StatusBar / NavigationBar 系统栏颜色常量 ======================
    // 浅色状态栏（黑文字）
    public static final int StatusBarLightBgColor = Color.WHITE;
    // 深色状态栏（白文字）
    public static final int StatusBarDarkBgColor = Color.parseColor("#212121");
    public static final int NavBarBgLightColor = Color.WHITE;
    public static final int NavBarBgDarkColor = Color.parseColor("#212121");

    // ====================== SwitchCompat AppCompat兼容开关颜色常量 ======================
    public static final int SwitchCompatTrackOffColor = Color.GRAY;
    public static final int SwitchCompatTrackOnColor = Color.parseColor("#34C759");
    public static final int SwitchCompatThumbNormalColor = Color.WHITE;

    // ====================== Chip 标签气泡控件颜色常量 ======================
    public static final int ChipTextNormalColor = Color.parseColor("#007AFF");
    public static final int ChipBgNormalColor = Color.parseColor("#E6F0FF");
    public static final int ChipCloseIconTintColor = Color.GRAY;

    // ====================== FloatingActionButton 悬浮按钮颜色常量 ======================
    public static final int FabIconTintColor = Color.WHITE;
    public static final int FabBgNormalColor = Color.parseColor("#007AFF");
    public static final int FabBgPressedColor = Color.parseColor("#005CC8");

    // ====================== RadioGroup/CheckBoxGroup 选择组颜色常量 ======================
    public static final int GroupLabelTextColor = Color.BLACK;

    // ====================== RecyclerView 列表颜色常量 ======================
    // 列表条目背景
    public static final int ListItemBgNormalColor = Color.WHITE;
    // 列表条目按压背景
    public static final int ListItemBgPressedColor = Color.parseColor("#F5F5F5");
    // 列表条目禁用背景
    public static final int ListItemBgDisabledColor = Color.parseColor("#FAFAFA");

    // ====================== Badge 角标红点颜色常量 ======================
    public static final int BadgeDotBgColor = Color.RED;
    public static final int BadgeTextColor = Color.WHITE;

    // ====================== 静态工具函数区域 ======================
    /**
     * 调试打印指定颜色常量十六进制字符串
     * @param color 目标颜色值，强制final
     * @param tag 颜色标识标签，强制final
     * @return 格式化十六进制颜色字符串
     */
    public static String getColorHex(final int color, final String tag) {
        LogUtils.d("ViewColorTable", "getColorHex invoke, tag = " + tag + ", rawColorInt = " + color);
        final String hexStr = String.format("#%06X", 0xFFFFFF & color);
        LogUtils.d("ViewColorTable", "getColorHex result hex = " + hexStr);
        return hexStr;
    }

    /**
     * 校验颜色值是否为透明色
     * @param color 待校验颜色值，强制final
     * @return true=完全透明，false=非透明
     */
    public static boolean isColorTransparent(final int color) {
        LogUtils.d("ViewColorTable", "isColorTransparent invoke, checkColor = " + color);
        final int alpha = Color.alpha(color);
        final boolean result = alpha == 0;
        LogUtils.d("ViewColorTable", "isColorTransparent alpha = " + alpha + ", result = " + result);
        return result;
    }
}

