package cc.winboll.studio.winboll.termux;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MyTermuxActivity extends AppCompatActivity {

    public static final String TAG = "MyTermuxActivity";
    public static final String EXTRA_BUTTON_NAME = "extra_button_name";
    public static final String ACTION_EXECUTE_SHORTCUT =
        "cc.winboll.studio.winboll.action.EXECUTE_TERMUX_BUTTON";

    private Toolbar mToolbar;
    private ListView mListView;
    private Button mBtnAdd;
    private ButtonAdapter mAdapter;
    private ArrayList<TermuxButtonModel> mButtonList;

    private static final int REQUEST_PICK_ICON = 100;
    private String mDialogIconPath = "";
    private ImageView mDialogIconView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_termux);

        initToolbar();
        initListView();
        initAddButton();
        refreshList();
        handleShortcutIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShortcutIntent();
    }

    private void handleShortcutIntent() {
        Intent intent = getIntent();
        if (intent != null && ACTION_EXECUTE_SHORTCUT.equals(intent.getAction())) {
            String buttonName = intent.getStringExtra(EXTRA_BUTTON_NAME);
            if (buttonName != null && buttonName.length() > 0) {
                TermuxButtonModel model = findButtonByName(buttonName);
                if (model != null) {
                    TermuxCommandExecutor.openTermuxBash(this,
                        model.getButtonName(), model.getExeCommand(),
                        model.getWorkDir(), true);
                } else {
                    Toast.makeText(this, R.string.toast_shortcut_not_found,
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private TermuxButtonModel findButtonByName(String name) {
        for (int i = 0; i < mButtonList.size(); i++) {
            if (name.equals(mButtonList.get(i).getButtonName())) {
                return mButtonList.get(i);
            }
        }
        return null;
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
                    model.getButtonName(), model.getExeCommand(),
                    model.getWorkDir(), true);
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
        final String[] items = new String[]{
            getString(R.string.menu_execute),
            getString(R.string.menu_edit),
            getString(R.string.menu_delete),
            getString(R.string.menu_create_shortcut),
            getString(R.string.menu_cancel)
        };
        new AlertDialog.Builder(this)
            .setTitle(model.getButtonName())
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        TermuxCommandExecutor.openTermuxBash(MyTermuxActivity.this,
                            model.getButtonName(), model.getExeCommand(),
                            model.getWorkDir(), true);
                    } else if (which == 1) {
                        showButtonDialog(position, model);
                    } else if (which == 2) {
                        showDeleteConfirmDialog(position);
                    } else if (which == 3) {
                        createDesktopShortcut(model);
                    }
                }
            })
            .show();
    }

    private void createDesktopShortcut(TermuxButtonModel model) {
        Intent shortcutIntent = new Intent(this, MyTermuxActivity.class);
        shortcutIntent.setAction(ACTION_EXECUTE_SHORTCUT);
        shortcutIntent.putExtra(EXTRA_BUTTON_NAME, model.getButtonName());
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ShortcutManager manager = getSystemService(ShortcutManager.class);
                if (manager == null || !manager.isRequestPinShortcutSupported()) {
                    Toast.makeText(this, R.string.toast_shortcut_not_supported,
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                String shortcutId = "termux_" + model.getButtonName();
                android.content.pm.ShortcutInfo info =
                    new android.content.pm.ShortcutInfo.Builder(this, shortcutId)
                    .setShortLabel(model.getButtonName())
                    .setLongLabel(model.getButtonName())
                    .setIcon(Icon.createWithResource(this,
                        android.R.drawable.ic_menu_manage))
                    .setIntent(shortcutIntent)
                    .build();
                manager.requestPinShortcut(info, null);
            } catch (Exception e) {
                LogUtils.e(TAG, "createDesktopShortcut error: " + e.getMessage());
                Toast.makeText(this, R.string.toast_shortcut_failed,
                    Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                Intent installIntent =
                    new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                    model.getButtonName());
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this,
                        android.R.drawable.ic_menu_manage));
                installIntent.putExtra("duplicate", false);
                sendBroadcast(installIntent);
            } catch (Exception e) {
                LogUtils.e(TAG, "createDesktopShortcut error: " + e.getMessage());
                Toast.makeText(this, R.string.toast_shortcut_failed,
                    Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_ICON && resultCode == RESULT_OK
            && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String fileName = copyIconToAppStorage(uri);
            if (fileName != null) {
                mDialogIconPath = fileName;
                File iconFile = getIconFile(fileName);
                if (iconFile.exists() && mDialogIconView != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(
                        iconFile.getAbsolutePath());
                    if (bitmap != null) {
                        mDialogIconView.setImageBitmap(bitmap);
                    }
                }
            } else {
                Toast.makeText(this, R.string.toast_icon_copy_failed,
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String copyIconToAppStorage(Uri uri) {
        try {
            File iconDir = new File(getFilesDir(), "termux_icons");
            if (!iconDir.exists()) {
                iconDir.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".png";
            File destFile = new File(iconDir, fileName);
            InputStream in = null;
            FileOutputStream out = null;
            try {
                in = getContentResolver().openInputStream(uri);
                out = new FileOutputStream(destFile);
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
            } finally {
                if (in != null) {
                    try { in.close(); } catch (Exception ignored) {}
                }
                if (out != null) {
                    try { out.close(); } catch (Exception ignored) {}
                }
            }
            return fileName;
        } catch (Exception e) {
            LogUtils.e(TAG, "copyIconToAppStorage error: " + e.getMessage());
            return null;
        }
    }

    private File getIconFile(String iconPath) {
        return new File(getFilesDir(), "termux_icons/" + iconPath);
    }

    private void showButtonDialog(final int index, final TermuxButtonModel model) {
        final boolean isEdit = (model != null);
        mDialogIconPath = (model != null && model.getIconPath() != null)
            ? model.getIconPath() : "";

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

        // Icon section
        final TextView tvIconLabel = new TextView(this);
        tvIconLabel.setText(R.string.label_icon);
        tvIconLabel.setTextSize(14);
        tvIconLabel.setTextColor(Color.GRAY);
        layout.addView(tvIconLabel);

        LinearLayout iconRow = new LinearLayout(this);
        iconRow.setOrientation(LinearLayout.HORIZONTAL);

        final ImageView ivIcon = new ImageView(this);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(100, 100);
        iconLp.setMargins(0, 8, 16, 8);
        ivIcon.setLayoutParams(iconLp);
        ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (mDialogIconPath.length() > 0) {
            File iconFile = getIconFile(mDialogIconPath);
            if (iconFile.exists()) {
                ivIcon.setImageBitmap(BitmapFactory.decodeFile(
                    iconFile.getAbsolutePath()));
            }
        }
        iconRow.addView(ivIcon);
        mDialogIconView = ivIcon;

        LinearLayout iconBtnLayout = new LinearLayout(this);
        iconBtnLayout.setOrientation(LinearLayout.VERTICAL);

        Button btnSelectIcon = new Button(this);
        btnSelectIcon.setText(R.string.btn_select_icon);
        btnSelectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICK_ICON);
            }
        });
        iconBtnLayout.addView(btnSelectIcon);

        Button btnClearIcon = new Button(this);
        btnClearIcon.setText(R.string.btn_clear_icon);
        btnClearIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogIconPath = "";
                mDialogIconView.setImageDrawable(null);
            }
        });
        iconBtnLayout.addView(btnClearIcon);

        iconRow.addView(iconBtnLayout);
        layout.addView(iconRow);

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
                    newModel.setIconPath(mDialogIconPath);
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
            ImageView ivIcon;
            TextView tv;

            if (convertView instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) convertView;
                ivIcon = (ImageView) ll.getChildAt(0);
                tv = (TextView) ll.getChildAt(1);
            } else {
                LinearLayout ll = new LinearLayout(MyTermuxActivity.this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setGravity(Gravity.CENTER_VERTICAL);
                ll.setPadding(30, 16, 30, 16);

                ivIcon = new ImageView(MyTermuxActivity.this);
                ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivIcon.setVisibility(View.GONE);
                ll.addView(ivIcon);

                tv = new TextView(MyTermuxActivity.this);
                tv.setTextSize(16);
                tv.setMinHeight(80);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setPadding(16, 0, 0, 0);
                ll.addView(tv);

                convertView = ll;
            }

            TermuxButtonModel model = mButtonList.get(position);
            String name = model.getButtonName();
            String cmd = model.getExeCommand();
            String fullText = name + "\n" + cmd;
            SpannableString sp = new SpannableString(fullText);
            sp.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp.setSpan(new ForegroundColorSpan(Color.BLUE), 0, name.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(sp);

            String iconPath = model.getIconPath();
            if (iconPath != null && iconPath.length() > 0) {
                File iconFile = getIconFile(iconPath);
                if (iconFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(
                        iconFile.getAbsolutePath());
                    if (bitmap != null) {
                        float density = getResources()
                            .getDisplayMetrics().density;
                        int marginPx = (int) (5 * density + 0.5f);
                        int estHeight = tv.getMinHeight();
                        int iconWidth = estHeight * bitmap.getWidth()
                            / bitmap.getHeight();
                        iconWidth = Math.max(iconWidth, (int)(32*density));
                        iconWidth = Math.min(iconWidth, (int)(120*density));
                        ivIcon.setImageBitmap(bitmap);
                        ivIcon.setScaleType(
                            ImageView.ScaleType.FIT_CENTER);
                        LinearLayout.LayoutParams lp =
                            new LinearLayout.LayoutParams(iconWidth,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        lp.setMargins(marginPx, marginPx,
                            marginPx, marginPx);
                        ivIcon.setLayoutParams(lp);
                        ivIcon.setVisibility(View.VISIBLE);
                    } else {
                        ivIcon.setImageDrawable(null);
                        ivIcon.setVisibility(View.GONE);
                    }
                } else {
                    ivIcon.setImageDrawable(null);
                    ivIcon.setVisibility(View.GONE);
                }
            } else {
                ivIcon.setImageDrawable(null);
                ivIcon.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
