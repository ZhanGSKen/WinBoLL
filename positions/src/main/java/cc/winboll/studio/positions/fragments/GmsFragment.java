package cc.winboll.studio.positions.fragments;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 12:57:00
 * @Describe 拨号
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.libappbase.LogView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import android.widget.TextView;

public class GmsFragment extends Fragment {

    public static final String TAG = "GmsFragment";

    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    public static GmsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        GmsFragment fragment = new GmsFragment();
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
        View view = inflater.inflate(R.layout.fragment_gms, container, false);
        TextView textView = view.findViewById(R.id.page_text);
        textView.setText("这是第 " + mPage + " 页");
        return view;
    }
}
