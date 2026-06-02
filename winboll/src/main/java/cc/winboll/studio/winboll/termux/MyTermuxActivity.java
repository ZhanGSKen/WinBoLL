package cc.winboll.studio.winboll.termux;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.models.TermuxButtonManager;
import cc.winboll.studio.winboll.models.TermuxButtonModel;
import cc.winboll.studio.winboll.termux.TermuxCommandExecutor;
import java.util.ArrayList;

public class MyTermuxActivity extends AppCompatActivity {

    public static final String TAG = "MyTermuxActivity";

    private Toolbar mToolbar;
    private ListView mListView;
    private Button mBtnAdd;
    private ButtonAdapter mAdapter;
    private ArrayList<TermuxButtonModel> mButtonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_termux);

        initToolbar();
        initListView();
        initAddButton();
        refreshList();
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void initListView() {
        mListView = findViewById(R.id.list_termux_buttons);
        mButtonList = new ArrayList<TermuxButtonModel>();
        mAdapter = new ButtonAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TermuxButtonModel model = mButtonList.get(position);
                TermuxCommandExecutor.openTermuxBash(MyTermuxActivity.this,
                    model.getExeCommand(), model.getWorkDir());
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showContextMenu(position);
                return true;
            }
        });
    }

    private void initAddButton() {
        mBtnAdd = findViewById(R.id.btn_add_termux_button);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showButtonDialog(-1, null);
            }
        });
    }

    private void refreshList() {
        mButtonList.clear();
        ArrayList<TermuxButtonModel> loaded = TermuxButtonManager.loadButtons(this);
        if (loaded != null) {
            mButtonList.addAll(loaded);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showContextMenu(final int position) {
        final TermuxButtonModel model = mButtonList.get(position);
        String[] items = new String[]{
            getString(R.string.menu_execute),
            getString(R.string.menu_edit),
            getString(R.string.menu_delete),
            getString(R.string.menu_cancel)
        };
        new AlertDialog.Builder(this)
            .setTitle(model.getButtonName())
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        TermuxCommandExecutor.openTermuxBash(MyTermuxActivity.this,
                            model.getExeCommand(), model.getWorkDir());
                    } else if (which == 1) {
                        showButtonDialog(position, model);
                    } else if (which == 2) {
                        showDeleteConfirmDialog(position);
                    }
                }
            })
            .show();
    }

    private void showDeleteConfirmDialog(final int position) {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_message) + mButtonList.get(position).getButtonName())
            .setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TermuxButtonManager.deleteButton(MyTermuxActivity.this, mButtonList, position);
                    refreshList();
                    Toast.makeText(MyTermuxActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show();
    }

    private void showButtonDialog(final int index, final TermuxButtonModel model) {
        final boolean isEdit = (model != null);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this);
        etName.setHint(R.string.hint_button_name);
        if (model != null) {
            etName.setText(model.getButtonName());
        }
        layout.addView(etName);

        final EditText etCommand = new EditText(this);
        etCommand.setHint(R.string.hint_exe_command);
        if (model != null) {
            etCommand.setText(model.getExeCommand());
        }
        layout.addView(etCommand);

        final EditText etWorkDir = new EditText(this);
        etWorkDir.setHint(R.string.hint_work_dir);
        if (model != null) {
            etWorkDir.setText(model.getWorkDir());
        }
        layout.addView(etWorkDir);

        int titleResId = isEdit ? R.string.dialog_edit_title : R.string.dialog_add_title;
        new AlertDialog.Builder(this)
            .setTitle(titleResId)
            .setView(layout)
            .setPositiveButton(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = etName.getText().toString().trim();
                    String command = etCommand.getText().toString().trim();
                    String workDir = etWorkDir.getText().toString().trim();
                    if (name.isEmpty() || command.isEmpty()) {
                        Toast.makeText(MyTermuxActivity.this, R.string.toast_fields_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TermuxButtonModel newModel = new TermuxButtonModel();
                    newModel.setButtonName(name);
                    newModel.setExeCommand(command);
                    newModel.setWorkDir(workDir);
                    if (isEdit) {
                        TermuxButtonManager.updateButton(MyTermuxActivity.this, mButtonList, index, newModel);
                    } else {
                        TermuxButtonManager.addButton(MyTermuxActivity.this, mButtonList, newModel);
                    }
                    refreshList();
                }
            })
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show();
    }

    private class ButtonAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mButtonList.size();
        }

        @Override
        public Object getItem(int position) {
            return mButtonList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = new TextView(MyTermuxActivity.this);
                tv.setPadding(30, 20, 30, 20);
                tv.setTextSize(16);
                tv.setMinHeight(80);
            } else {
                tv = (TextView) convertView;
            }

            TermuxButtonModel model = mButtonList.get(position);
            tv.setText(model.getButtonName() + "\n" + model.getExeCommand());
            tv.setTextColor(getResources().getColor(android.R.color.black));
            return tv;
        }
    }
}
