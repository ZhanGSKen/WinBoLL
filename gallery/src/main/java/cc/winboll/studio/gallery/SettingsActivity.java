package cc.winboll.studio.gallery;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libappbase.LogUtils;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";
    private Preferences prefs;
    private EditText editFolderPath;
    private TextView textCurrentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LogUtils.d(TAG, "onCreate");
        
        prefs = new Preferences(this);
        
        editFolderPath = findViewById(R.id.edit_folder_path);
        textCurrentPath = findViewById(R.id.text_current_path);
        Button btnSave = findViewById(R.id.btn_save);
        
        String currentPath = prefs.getFolderPath();
        editFolderPath.setText(currentPath);
        textCurrentPath.setText("Current: " + currentPath);
        
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPath = editFolderPath.getText().toString().trim();
                if (!newPath.isEmpty()) {
                    prefs.setFolderPath(newPath);
                    textCurrentPath.setText("Current: " + newPath);
                }
                finish();
            }
        });
    }
}