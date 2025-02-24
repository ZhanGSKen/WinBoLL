package cc.winboll.studio.positions.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/22 02:01:44
 */
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.views.GridMapView;
import android.view.View.OnClickListener;
import android.view.View;

public class GridMapActivity extends AppCompatActivity {

    GridMapView gridMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridmap);

        // 初始化视图
        GridMapView mapView = (GridMapView) findViewById(R.id.map_view);

        // 设置网格参数
        mapView.setGridParameters(10000f, 10000f, 10f);

        // 初始化显示区域（中心点400,300，显示范围2000x200）
        mapView.initViewport(5000f, 5000f, 1000f, 1000f);

        // 绘制图形
        mapView.drawPoint(5000f, 5000f, Color.RED, 8f);
        mapView.drawCircle(5000f, 5000f, 50f, Color.BLUE, 8f);
        mapView.drawLine(4975f, 4975f, 5025f, 5025f, Color.GREEN, 2f);
    }
}
