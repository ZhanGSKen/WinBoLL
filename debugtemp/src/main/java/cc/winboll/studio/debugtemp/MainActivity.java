package cc.winboll.studio.debugtemp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.LibraryActivity;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.ToastUtils;

public class MainActivity extends AppCompatActivity {

	LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        mLogView = findViewById(R.id.logview);

        ToastUtils.show("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }

    public void onLibraryActivity(View view) {
		startActivity(new Intent(this, LibraryActivity.class));
    }
}
