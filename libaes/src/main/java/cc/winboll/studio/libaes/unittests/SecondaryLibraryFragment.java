package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/16 02:36:34
 * @Describe SecondaryLibraryFragment
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libaes.R;

public class SecondaryLibraryFragment extends Fragment {

    public static final String TAG = "SecondaryLibraryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secondarylibrary, container, false);

        return view;
    }
}
