package cc.winboll.studio.libwinboll;

import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.libappbase.ToastUtils;

public class WinBoLLLibraryActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winbolllibrary);
		
		ToastUtils.show("WinBoLLLibraryActivity onCreate");
    }
}
