package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/13 19:05:52
 * @Describe 应用主窗口
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import cc.winboll.studio.aes.R;
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;
import cc.winboll.studio.libaes.beans.DrawerMenuBean;
import cc.winboll.studio.libaes.dialogs.LocalFileSelectDialog;
import cc.winboll.studio.libaes.dialogs.StoragePathDialog;
import cc.winboll.studio.libaes.unittests.SecondaryLibraryActivity;
import cc.winboll.studio.libaes.unittests.TestAButtonFragment;
import cc.winboll.studio.libaes.unittests.TestASupportToolbarActivity;
import cc.winboll.studio.libaes.unittests.TestAToolbarActivity;
import cc.winboll.studio.libaes.unittests.TestDrawerFragmentActivity;
import cc.winboll.studio.libaes.unittests.TestViewPageFragment;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;
import com.a4455jkjh.colorpicker.ColorPickerDialog;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;

public class MainActivity extends DrawerFragmentActivity implements IWinBoLLActivity {


    public static final String TAG = "MainActivity";

    TestAButtonFragment mTestAButtonFragment;
    TestViewPageFragment mTestViewPageFragment;
    
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mTestAButtonFragment == null) {
            mTestAButtonFragment = new TestAButtonFragment();
            addFragment(mTestAButtonFragment);
        }
        showFragment(mTestAButtonFragment);
        //setSubtitle(TAG);
        //ToastUtils.show("onCreate");
    }

    @Override
    public void initDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {
        super.initDrawerMenuItemList(listDrawerMenu);
        LogUtils.d(TAG, "initDrawerMenuItemList");
        //listDrawerMenu.clear();
        // 添加抽屉菜单项
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestAButtonFragment.TAG));
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestViewPageFragment.TAG));
        notifyDrawerMenuDataChanged();
    }

    @Override
    public void reinitDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {
        super.reinitDrawerMenuItemList(listDrawerMenu);
        LogUtils.d(TAG, "reinitDrawerMenuItemList");
        //listDrawerMenu.clear();
        // 添加抽屉菜单项
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestAButtonFragment.TAG));
        listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, TestViewPageFragment.TAG));
        notifyDrawerMenuDataChanged();
    }

    @Override
    public DrawerFragmentActivity.ActivityType initActivityType() {
        return DrawerFragmentActivity.ActivityType.Main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_library, menu);
        if(App.isDebuging()) {
            getMenuInflater().inflate(cc.winboll.studio.libapputils.R.menu.toolbar_studio_debug, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        switch (position) {
            case 0 : {
                    if (mTestAButtonFragment == null) {
                        mTestAButtonFragment = new TestAButtonFragment();
                        addFragment(mTestAButtonFragment);
                    }
                    showFragment(mTestAButtonFragment);
                    break;
                }
            case 1 : {
                    if (mTestViewPageFragment == null) {
                        mTestViewPageFragment = new TestViewPageFragment();
                        addFragment(mTestViewPageFragment);
                    }
                    showFragment(mTestViewPageFragment);
                    break;
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int nItemId = item.getItemId();
//        if (item.getItemId() == R.id.item_log) {
//            WinBoLLActivityManager.getInstance(this).startWinBoLLActivity(getApplicationContext(), LogActivity.class);
//        } else 
        if (nItemId == R.id.item_atoast) {
            Toast.makeText(getApplication(), "item_testatoast", Toast.LENGTH_SHORT).show();
        } else if (nItemId == R.id.item_atoolbar) {
            Intent intent = new Intent(this, TestAToolbarActivity.class);
            startActivity(intent);

        } else if (nItemId == R.id.item_asupporttoolbar) {
            Intent intent = new Intent(this, TestASupportToolbarActivity.class);
            startActivity(intent);

        } else if (nItemId == R.id.item_colordialog) {
            ColorPickerDialog dlg = new ColorPickerDialog(this, getResources().getColor(R.color.colorPrimary));
            dlg.setOnColorChangedListener(new com.a4455jkjh.colorpicker.view.OnColorChangedListener() {

                    @Override
                    public void beforeColorChanged() {
                    }

                    @Override
                    public void onColorChanged(int color) {

                    }

                    @Override
                    public void afterColorChanged() {
                    }


                });
            dlg.show();

        } else if (nItemId ==  R.id.item_dialogstoragepath) {
            final StoragePathDialog dialog = new StoragePathDialog(this, 0);
            dialog.setOnOKClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            dialog.show();

        } else if (nItemId ==  R.id.item_localfileselectdialog) {
            final LocalFileSelectDialog dialog = new LocalFileSelectDialog(this);
            dialog.setOnOKClickListener(new LocalFileSelectDialog.OKClickListener() {
                    @Override
                    public void onOKClick(String sz) {
                        Toast.makeText(getApplication(), sz, Toast.LENGTH_SHORT).show();
                        //dialog.dismiss();
                    }
                });
            dialog.open();

        } else if (nItemId ==  R.id.item_secondarylibraryactivity) {
            Intent intent = new Intent(this, SecondaryLibraryActivity.class);
            startActivity(intent);
        } else if (nItemId ==  R.id.item_drawerfragmentactivity) {
            Intent intent = new Intent(this, TestDrawerFragmentActivity.class);
            startActivity(intent);
        } 
        else if (nItemId ==  R.id.item_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    
}
