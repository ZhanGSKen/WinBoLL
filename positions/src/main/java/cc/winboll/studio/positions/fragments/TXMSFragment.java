package cc.winboll.studio.positions.fragments;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 12:44:39
 * @Describe 腾讯地图服务视图
 */
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.activities.SettingsActivity;
import cc.winboll.studio.positions.activities.TestMapViewActivity;
import cc.winboll.studio.positions.utils.LocationFileStorage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;
import java.util.ArrayList;
import java.util.List;
import pub.devrel.easypermissions.EasyPermissions;
import cc.winboll.studio.positions.beans.LocationJson;
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
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;

public class TXMSFragment extends Fragment implements EasyPermissions.PermissionCallbacks,LocationSource, TencentLocationListener {

    public static final String TAG = "TXMSFragment";

    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private TextureMapView mapView;
    protected TencentMap tencentMap;
    TextView mtvInfo;
    private LocationSource.OnLocationChangedListener locationChangedListener;

    private TencentLocationManager locationManager;
    private TencentLocationRequest locationRequest;
    private MyLocationStyle locationStyle;
    ArrayList<LocationJson> locationJsonList;

    public static TXMSFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        TXMSFragment fragment = new TXMSFragment();
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
        View viewRoot = inflater.inflate(R.layout.fragment_txms, container, false);
        locationJsonList = new ArrayList<LocationJson>();

        TencentMapInitializer.setAgreePrivacy(getActivity(), true);
        TencentMapInitializer.start(getActivity());
        TencentLocationManager.setUserAgreePrivacy(true);


        mapView = viewRoot.findViewById(R.id.mapview);
        mapView.setOpaque(false);
        //创建tencentMap地图对象，可以完成对地图的几乎所有操作
        tencentMap = mapView.getMap();

//        FloatingActionButton fab = viewRoot.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Snackbar.make(view, "点击了悬浮按钮", Snackbar.LENGTH_LONG).show();
//                }
//            });

        mtvInfo = viewRoot.findViewById(R.id.tv_info);

        checkLocationPermission();

        //设置显示定位的图标
        TencentLocationManager.setUserAgreePrivacy(true);
        //建立定位
        //initLocation();
        //对地图操作类进行操作
        CameraUpdate cameraSigma =
            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                                      new LatLng(22.984066, 116.307548),
                                                      15,
                                                      0f,
                                                      0f));
        //移动地图
        tencentMap.moveCamera(cameraSigma);

        // 设置地图点击监听
        tencentMap.setOnMapClickListener(new TencentMap.OnMapClickListener(){

                @Override
                public void onMapClick(com.tencent.tencentmap.mapsdk.maps.model.LatLng latLng) {
                    //创建Marker对象之前，设置属性
                    //LatLng position = new LatLng(40.011313,116.391907);
                    BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.drawable.marker);
                    Location location = createLocationFromLatLng(latLng);
                    addLocationJson(location);
                    Marker mCustomMarker = tencentMap.addMarker(new MarkerOptions(latLng));

                    //创建Marker对象之后，修改属性
//                    Animation animation = new AlphaAnimation(0.7f, 0f);
//                    animation.setDuration(2000);
//                    mCustomMarker.setAnimation(animation);
//                    mCustomMarker.startAnimation();
                }
            });

        loadLocations();
        
        return viewRoot;
    }

    void loadLocations() {
        // 存储位置数据
//        Location location = new Location("gps");
//        location.setLatitude(22.984066);
//        location.setLongitude(116.307548);
//        location.setTime(System.currentTimeMillis());
//
//        // 方式1：保存到文件
//        List<Location> locations = new ArrayList<>();
//        locations.add(location);
//        LocationFileStorage.saveToFile(this, locations);

        // 读取数据
        locationJsonList = LocationFileStorage.loadFromFile(getActivity());

        for (LocationJson lj : locationJsonList) {
            tencentMap.addMarker(new MarkerOptions(toTencentLatLng(lj.toLocation())));
            //LogUtils.d("Location", "Lat: " + loc.getLatitude() + ", Lng: " + loc.getLongitude());
        }
    }

    void addLocationJson(Location location) {
        // 存储位置数据
//        Location location = new Location("gps");
//        location.setLatitude(22.984066);
//        location.setLongitude(116.307548);
//        location.setTime(System.currentTimeMillis());

        // 方式1：保存到文件
        //List<Location> locations = new ArrayList<>();
        locationJsonList.add(new LocationJson(location));
        LocationFileStorage.saveToFile(getActivity(), locationJsonList);

        // 读取数据
//        List<Location> loaded = LocationFileStorage.loadFromFile(this);
//        for (Location loc : loaded) {
//            tencentMap.addMarker(new MarkerOptions(toTencentLatLng(loc)));
//            //LogUtils.d("Location", "Lat: " + loc.getLatitude() + ", Lng: " + loc.getLongitude());
//        }
    }



// 创建Location对象方法
    private Location createLocationFromLatLng(LatLng latLng) {
        Location location = new Location("tencent_map_manual");

        // 设置基础坐标
        location.setLatitude(latLng.getLatitude());
        location.setLongitude(latLng.getLongitude());

        // 设置必要元数据
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setAccuracy(5.0f); // 手动点击精度设为5米

        return location;
    }


    public LatLng toTencentLatLng(Location location) {
        return new LatLng(
            location.getLatitude(),
            location.getLongitude()
        );
    }


    // 添加标记方法
    private void addMarker(LatLng latLng) {
        tencentMap.clearAllOverlays();
        MarkerOptions options = new MarkerOptions(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker())
            .title("点击保存");
        tencentMap.addMarker(options);
    }

    /**
     * mapview的生命周期管理
     */
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 设置定位图标样式
     */
    private void setLocMarkerStyle() {
        locationStyle = new MyLocationStyle();
        //创建图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getBitMap(R.drawable.marker));
        locationStyle.icon(bitmapDescriptor);
        //设置定位圆形区域的边框宽度
        locationStyle.strokeWidth(3);
        //设置圆区域的颜色
        locationStyle.fillColor(R.color.style);

        tencentMap.setMyLocationStyle(locationStyle);
    }



    private Bitmap getBitMap(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = 55;
        int newHeight = 55;
        float widthScale = ((float)newWidth) / width;
        float heightScale = ((float)newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, heightScale);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }



    /**
     * 定位的一些初始化设置
     */
    private void initLocation() {
        //用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        locationManager = TencentLocationManager.getInstance(getActivity());
        //设置坐标系
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        //创建定位请求
        locationRequest = TencentLocationRequest.create();
        //设置定位周期（位置监听器回调周期）为3s
        locationRequest.setInterval(3000);

        //地图上设置定位数据源
        tencentMap.setLocationSource(this);
        //设置当前位置可见
        tencentMap.setMyLocationEnabled(true);
        //设置定位图标样式
        setLocMarkerStyle();
//        locationStyle = locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        tencentMap.setMyLocationStyle(locationStyle);
    }
    /**
     * 实现位置监听
     * @param tencentLocation
     * @param i
     * @param s
     */
    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {

        if (i == TencentLocation.ERROR_OK && locationChangedListener != null) {
            final Location location = new Location(tencentLocation.getProvider());
            //设置经纬度以及精度
            location.setLatitude(tencentLocation.getLatitude());
            location.setLongitude(tencentLocation.getLongitude());
            location.setAccuracy(tencentLocation.getAccuracy());
            locationChangedListener.onLocationChanged(location);

            //显示回调的实时位置信息
            getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Rules.getEffectInfo(location);
//                        double distance = DistanceUtils.getDistance(
//                            locationA.getLatitude(), 
//                            locationA.getLongitude(),
//                            locationB.getLatitude(),
//                            locationB.getLongitude()
//                        );
                        mtvInfo.setText(String.format("\n%f %f", location.getLatitude(), location.getLongitude()));
                        //打印tencentLocation的json字符串
//                    Toast.makeText(getApplicationContext(), new Gson().toJson(location), Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //GPS, WiFi, Radio 等状态发生变化
        Log.v("State changed", s + "===" +  s1);
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;

        int err = locationManager.requestLocationUpdates(locationRequest, this, Looper.myLooper());
        switch (err) {
            case 1:
                Toast.makeText(getActivity(), "设备缺少使用腾讯定位服务需要的基本条件", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(getActivity(), "manifest 中配置的 key 不正确", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(getActivity(), "自动加载libtencentloc.so失败", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    @Override
    public void deactivate() {
        locationManager.removeUpdates(this);
        locationManager = null;
        locationRequest = null;
        locationChangedListener = null;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.e("location quest: ", "success");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.e("location quest: ", "failed");
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                                              new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                              PERMISSION_REQUEST_CODE);
        } else {
            // 权限已授予，可进行定位操作
            //startLocationUpdates();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startLocationUpdates();
            } else {
                // 用户拒绝了权限请求
                Toast.makeText(getActivity(), "请授予定位权限", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
