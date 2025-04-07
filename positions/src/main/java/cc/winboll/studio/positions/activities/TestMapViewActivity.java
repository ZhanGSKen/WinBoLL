package cc.winboll.studio.positions.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/24 12:14:04
 */
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.positions.R;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;

public class TestMapViewActivity extends AppCompatActivity {
    
    public static final String TAG = "TestMapViewActivity";

    /**
     * 由于SDK并没有提供用于MapView管理地图生命周期的Activity
     * 因此需要用户继承Activity后管理地图的生命周期，防止内存泄露
     */

    private TextureMapView mapView;
    protected TencentMap tencentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testmapview);

        mapView = findViewById(R.id.mapview);
        mapView.setOpaque(false);
        //创建tencentMap地图对象，可以完成对地图的几乎所有操作
        tencentMap = mapView.getMap();

    }


    /**
     * mapview的生命周期管理
     */
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onRestart();
    }
}
