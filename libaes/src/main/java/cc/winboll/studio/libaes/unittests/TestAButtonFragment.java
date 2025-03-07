package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:27:50
 * @Describe TestAButtonFragment
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.views.AButton;
import cc.winboll.studio.libappbase.LogUtils;

public class TestAButtonFragment extends Fragment {

    public static final String TAG = "TestAButtonFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_abutton, container, false);
        AButton aButton = view.findViewById(R.id.fragmentabuttonAButton1);
        aButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    LogUtils.d(TAG, "onClick");
                    Toast.makeText(getActivity(), "AButton", Toast.LENGTH_SHORT).show();
                }

            });
        return view;
    }
}
