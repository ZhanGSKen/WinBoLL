package cc.winboll.studio.appbase;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libappbase.GlobalApplication;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(toolbar);
        
        CheckBox cbIsDebugMode = findViewById(R.id.activitymainCheckBox1);
        cbIsDebugMode.setChecked(GlobalApplication.isDebuging());
    }

	public void onSwitchDebugMode(View view) {
        GlobalApplication.setIsDebuging(this, ((CheckBox)view).isChecked());
    }
}
