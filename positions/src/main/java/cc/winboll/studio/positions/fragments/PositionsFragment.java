package cc.winboll.studio.positions.fragments;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 12:57:50
 * @Describe 联系人
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.positions.R;
import com.hjq.toast.ToastUtils;

public class PositionsFragment extends Fragment {
    
    public static final String TAG = "ContactsFragment";
    
    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

//    public static PositionsFragment newInstance(int page) {
//        Bundle args = new Bundle();
//        args.putInt(ARG_PAGE, page);
//        PositionsFragment fragment = new PositionsFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments()!= null) {
//            mPage = getArguments().getInt(ARG_PAGE);
//        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View viewMain = inflater.inflate(R.layout.fragment_positions, container, false);
//        Toolbar toolbar = viewMain.findViewById(R.id.toolbar);
//        getActivity().getMenuInflater().inflate(R.menu.toolbar_positions,  toolbar.getMenu());
//        
        return viewMain;
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_positions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.item_addposition) {
//            ToastUtils.show("item_addposition");
//        }
//        } else 
//        if (item.getItemId() == R.id.item_exit) {
//            exit();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
