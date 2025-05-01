package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/30 15:00:51
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;
import cc.winboll.studio.libaes.beans.DrawerMenuBean;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;
import java.util.ArrayList;

public class TestDrawerFragmentActivity extends DrawerFragmentActivity implements IWinBoLLActivity {

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return null;
    }

    public static final String TAG = "TestDrawerFragmentActivity";

    TestFragment1 mTestFragment1;
    TestFragment2 mTestFragment2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTestFragment1 = new TestFragment1();
        addFragment(mTestFragment1);
        mTestFragment2 = new TestFragment2();
        addFragment(mTestFragment2);
        showFragment(0);
    }

    @Override
    protected DrawerFragmentActivity.ActivityType initActivityType() {
        return DrawerFragmentActivity.ActivityType.Main;
    }

    @Override
    public void initDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {
        super.initDrawerMenuItemList(listDrawerMenu);
        LogUtils.d(TAG, "initDrawerMenuItemList");
        //listDrawerMenu.clear();
        // 添加抽屉菜单项
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestFragment1.TAG));
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestFragment2.TAG));
        notifyDrawerMenuDataChanged();
    }

    @Override
    public void reinitDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {
        super.reinitDrawerMenuItemList(listDrawerMenu);
        LogUtils.d(TAG, "reinitDrawerMenuItemList");
        //listDrawerMenu.clear();
        // 添加抽屉菜单项
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestFragment1.TAG));
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestFragment2.TAG));
        notifyDrawerMenuDataChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        switch (position) {
            case 0 : {
                    Toast.makeText(getApplicationContext(), "0", Toast.LENGTH_SHORT).show();
                    //LogUtils.d(TAG, "MenuItem 1");
                    showFragment(mTestFragment1);
                    break;
                }
            case 1 : {
                    //LogUtils.d(TAG, "MenuItem 2");
                    showFragment(mTestFragment2);
                    break;
                }

        }
    }

    public static class TestFragment1 extends Fragment {

        public static final String TAG = "TestFragment1";

        View mView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.fragment_test1, container, false);

            return mView;
        }

    }

    public static class TestFragment2 extends Fragment {

        public static final String TAG = "TestFragment2";

        View mView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.fragment_test2, container, false);

            return mView;
        }
    }
}
