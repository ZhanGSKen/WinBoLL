package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/28 19:11:27
 * @Describe WinBoLL UI 状态图标枚举
 */
import cc.winboll.studio.libaes.R;

public enum EWUIStatusIconDrawable {
    NORMAL(0),
    NEWS(1)
    ;

    static final String TAG = "WUIStatusIconDrawable";

    static String[] _mlistCNName = { "正常", "新的消息" };

    private int value = 0;
    private EWUIStatusIconDrawable(int value) {    //必须是private的，否则编译错误
        this.value = value;
    }

    public static int getIconDrawableId(EWUIStatusIconDrawable drawableId) {
        int res;
        switch(drawableId){
            case NEWS :
                res = R.drawable.ic_winbollbeta;
                break;
            default :
                res = R.drawable.ic_winboll;
        }
        return res;
    }
}
