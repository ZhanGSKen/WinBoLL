package cc.winboll.studio.libaes.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/06/14 01:53:34
 * @Describe 抽屉菜单项目类
 */
public class DrawerMenuBean {

    public static final String TAG = "DrawerMenuBean";

    private int iconId;
    private String iconName;

    public DrawerMenuBean(int iconId, String iconName) {
        this.iconId = iconId;
        this.iconName = iconName;
    }

    public int getIconId() {
        return iconId;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}
