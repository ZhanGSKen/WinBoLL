package cc.winboll.studio.winboll.fragments;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/29 13:15
 * @Describe MainFragment
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.winboll.R;


public class MainFragment extends Fragment {
    
    public static final String TAG = "MainFragment";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Switch swEnablePosition = view.findViewById(R.id.fragmentmainSwitch1);
        swEnablePosition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToastUtils.show("Position");
                }
            });
        return view;
    }
    
}
