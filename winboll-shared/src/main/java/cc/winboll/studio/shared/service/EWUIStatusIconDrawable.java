package cc.winboll.studio.shared.service;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/09 08:37:31
 * @Describe WinBoll UI 状态图标枚举
 */
import cc.winboll.studio.R;

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
