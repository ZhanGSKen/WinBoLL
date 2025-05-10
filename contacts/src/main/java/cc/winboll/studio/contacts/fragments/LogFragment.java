package cc.winboll.studio.contacts.fragments;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/20 12:58:15
 * @Describe 应用日志
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogView;
import com.hjq.toast.ToastUtils;

public class LogFragment extends Fragment {

    public static final String TAG = "LogFragment";

    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    
    LogView mLogView;

    public static LogFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        LogFragment fragment = new LogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        mLogView = view.findViewById(R.id.logview);
        mLogView.start();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //ToastUtils.show("onResume");
        mLogView.start();
    }
    
    
}
