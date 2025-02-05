package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/13 15:09:46
 */
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.app.IWinBollActivity;

public class TestWinBollActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "TestWinBollActivity";

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activitytestwinbollToolbar1);
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testwinboll);
    }
}
