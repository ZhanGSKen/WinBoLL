package cc.winboll.studio.positions.fragments;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 12:57:50
 * @Describe 联系人
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.positions.R;
import androidx.appcompat.widget.Toolbar;

public class PositionsFragment extends Fragment {
    
    public static final String TAG = "ContactsFragment";
    
    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    public static PositionsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PositionsFragment fragment = new PositionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!= null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View viewMain = inflater.inflate(R.layout.fragment_positions, container, false);
        Toolbar toolbar = viewMain.findViewById(R.id.toolbar);
        getActivity().getMenuInflater().inflate(R.menu.toolbar_positions,  toolbar.getMenu());
        
        return viewMain;
    }
}
