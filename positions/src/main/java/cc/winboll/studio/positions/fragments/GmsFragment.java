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
import android.graphics.Color;
import android.widget.TextView;
import cc.winboll.studio.positions.views.GridMapView;

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

        // 初始化视图
        GridMapView mapView = (GridMapView) view.findViewById(R.id.map_view);

        // 设置网格参数
        mapView.setGridParameters(10000f, 10000f, 10f);

        // 初始化显示区域（中心点400,300，显示范围2000x200）
        mapView.initViewport(5000f, 5000f, 1000f, 1000f);

        // 绘制图形
        mapView.drawPoint(5000f, 5000f, Color.RED, 8f);
        mapView.drawCircle(5000f, 5000f, 50f, Color.BLUE, 8f);
        mapView.drawLine(4975f, 4975f, 5025f, 5025f, Color.GREEN, 2f);
        return view;
    }
}
