package cc.winboll.studio.winboll.models;

import android.content.Context;
import cc.winboll.studio.libappbase.BaseBean;
import java.util.ArrayList;

public class TermuxButtonManager {

    public static ArrayList<TermuxButtonModel> loadButtons(Context context) {
        ArrayList<TermuxButtonModel> list = new ArrayList<TermuxButtonModel>();
        BaseBean.loadBeanList(context, list, TermuxButtonModel.class);
        return list;
    }

    public static boolean saveButtons(Context context, ArrayList<TermuxButtonModel> list) {
        return BaseBean.saveBeanList(context, list, TermuxButtonModel.class);
    }

    public static void addButton(Context context, ArrayList<TermuxButtonModel> list, TermuxButtonModel button) {
        list.add(button);
        saveButtons(context, list);
    }

    public static void updateButton(Context context, ArrayList<TermuxButtonModel> list, int index, TermuxButtonModel button) {
        if (index >= 0 && index < list.size()) {
            list.set(index, button);
            saveButtons(context, list);
        }
    }

    public static void deleteButton(Context context, ArrayList<TermuxButtonModel> list, int index) {
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveButtons(context, list);
        }
    }
}
