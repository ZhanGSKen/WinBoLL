package cc.winboll.studio.appbase;

import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.appbase.R;

public class Main2Activity extends MainActivity {

    public static final String TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setActionBar(toolbar);
        }
    }
}