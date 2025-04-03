package cc.winboll.studio.positions.fragments;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 12:44:39
 * @Describe 腾讯地图服务视图
 */
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.listeners.OnTXRTLocationListener;
import cc.winboll.studio.positions.models.PostionModel;
import cc.winboll.studio.positions.utils.LocationFileStorage;
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
import com.tencent.tencentmap.mapsdk.maps.UiSettings;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;
import java.util.ArrayList;

public class TXMSFragment extends Fragment implements /*EasyPermissions.PermissionCallbacks,*/LocationSource, TencentLocationListener,TencentMap.OnMapClickListener {

    public static final String TAG = "TXMSFragment";

    private static final int PERMISSION_REQUEST_CODE = 1;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private TextureMapView mapView;
    protected TencentMap tencentMap;

    private LocationSource.OnLocationChangedListener locationChangedListener;

    private TencentLocationManager mTencentLocationManager;
    private TencentLocationRequest mTencentLocationRequest;
    private MyLocationStyle mMyLocationStyle;
    ArrayList<PostionModel> locationPostionModelList;
    //Location lastLocation;
    static TXMSFragment _TXMSFragment;
    OnTXRTLocationListener mOnTXRTLocationListener;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _TXMSFragment = TXMSFragment.this;
        View viewRoot = inflater.inflate(R.layout.fragment_txms, container, false);
        locationPostionModelList = new ArrayList<PostionModel>();

        TencentMapInitializer.setAgreePrivacy(getActivity(), true);
        TencentMapInitializer.start(getActivity());
        TencentLocationManager.setUserAgreePrivacy(true);

        mapView = viewRoot.findViewById(R.id.mapview);
        mapView.setOpaque(false);
        //创建tencentMap地图对象，可以完成对地图的几乎所有操作
        tencentMap = mapView.getMap();


        checkLocationPermission();

        // 设置地图点击监听
        tencentMap.setOnMapClickListener(this);

        loadLocations();

        UiSettings uiSettings = tencentMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        mTencentLocationManager = TencentLocationManager.getInstance(getActivity());
        //创建定位请求
        mTencentLocationRequest = TencentLocationRequest.create();
        //mTencentLocationManager.requestLocationUpdates(mTencentLocationRequest, this);
        //地图上设置定位数据源
        tencentMap.setLocationSource(this);
        //设置当前位置可见
        tencentMap.setMyLocationEnabled(true);
        //设置定位图标样式
        setMyLocationMarkerStyle();
        startRTLocation();

        return viewRoot;
    }
    
    @Override
    public void onMapClick(LatLng latLng) {
        //创建Marker对象之前，设置属性
        //LatLng position = new LatLng(40.011313,116.391907);
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        Location location = createTXLocationFromLatLng(latLng);
        addLocationToMap(location);
        Marker mCustomMarker = tencentMap.addMarker(new MarkerOptions(latLng));

        //创建Marker对象之后，修改属性
//                    Animation animation = new AlphaAnimation(0.7f, 0f);
//                    animation.setDuration(2000);
//                    mCustomMarker.setAnimation(animation);
//                    mCustomMarker.startAnimation();
    }

    void genLocationFixModel() {

    }

    public void startRTLocation() {
        //ToastUtils.show("startRTLocation()");
        mTencentLocationManager.requestLocationUpdates(mTencentLocationRequest, this);
    }

    private void stopLocation() {
        if (mTencentLocationManager != null) {
            mTencentLocationManager.removeUpdates(this);
            //mTencentLocationManager.removeLocationListener(this);
        }
    }

    void loadLocations() {
        // 读取数据
        locationPostionModelList = LocationFileStorage.loadFromFile(getActivity());

        for (PostionModel lj : locationPostionModelList) {
            tencentMap.addMarker(new MarkerOptions(convertLocationToLatLng(lj.toLocation())));
        }
    }

    void addLocationToMap(Location location) {
        locationPostionModelList.add(new PostionModel(location));
        LocationFileStorage.saveToFile(getActivity(), locationPostionModelList);
    }

//    public void addCurrentLocationToMap() {
//        ToastUtils.show("addCurrentLocationToMap");
//        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        locationPostionModelList.add(new PostionModel(getCurrentGPSLocation()));
//        LocationFileStorage.saveToFile(getActivity(), locationPostionModelList);
//    }

    // 手机 GPS 定位信息
//    LocationListener phoneGPSLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            locationPhoneGPS = location;
//            // 位置变化时的处理逻辑
////            double latitude = location.getLatitude();
////            double longitude = location.getLongitude();
//            String szTemp = String.format("Phone GPS MyLocation Init Info\nLatitude %f, Longitude %f, Accuracy %f", locationPhoneGPS.getLatitude(), locationPhoneGPS.getLongitude(), locationPhoneGPS.getAccuracy());
//            mtvPhoneMyLocationInfo.setText(szTemp);
//            LogUtils.d(TAG, szTemp);
//            updatePostionFixModel();
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {}
//
//        @Override
//        public void onProviderEnabled(String provider) {}
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {}
//    };




    // 创建Location对象方法
    private Location createTXLocationFromLatLng(double latitudeLock, double longitudeLock) {
        Location location = new Location("Tencent_Map_Manual");

        // 设置基础坐标
        location.setLatitude(latitudeLock);
        location.setLongitude(longitudeLock);

        // 设置必要元数据
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setAccuracy(5.0f); // 手动点击精度设为5米

        return location;
    }

    // 创建Location对象方法
    private Location createTXLocationFromLatLng(LatLng latLng) {
        Location location = new Location("Tencent_Map_Manual");

        // 设置基础坐标
        location.setLatitude(latLng.getLatitude());
        location.setLongitude(latLng.getLongitude());

        // 设置必要元数据
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setAccuracy(5.0f); // 手动点击精度设为5米

        return location;
    }

    public static LatLng convertTencentLocationToLatLng(TencentLocation location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            return new LatLng(latitude, longitude);
        }
        return null;
    }

    public static LatLng convertLocationToLatLng(Location location) {
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

    public static final int REALTIME_POSITIONING = 1;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case REALTIME_POSITIONING:
                    // 在这里处理接收到消息后的逻辑，比如更新 UI

                    break;
                default:
                    break;
            }
        }
    };

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
    private void setMyLocationMarkerStyle() {
        mMyLocationStyle = new MyLocationStyle();
        //创建图标
        //BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getBitMap(R.drawable.marker));
        //mMyLocationStyle.icon(bitmapDescriptor);
        //设置定位圆形区域的边框宽度
        mMyLocationStyle.strokeWidth(0);
        //设置圆区域的颜色
        mMyLocationStyle.fillColor(R.color.style);

        tencentMap.setMyLocationStyle(mMyLocationStyle);
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

    public static void moveToLocation(double latitudeLock, double longitudeLock) {
        if (_TXMSFragment != null) {
            Location location = _TXMSFragment.createTXLocationFromLatLng(latitudeLock, longitudeLock);
            _TXMSFragment.moveToGPSLocation(location);
        }
    }



    private void moveToGPSLocation(Location location) {
        //对地图操作类进行操作
        CameraUpdate cameraSigma =
            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                                      convertLocationToLatLng(location),
                                                      19f,
                                                      0f,
                                                      0f));
        //移动地图
        tencentMap.moveCamera(cameraSigma);
        ToastUtils.show("Move To Location.");
        //addLocationToMap(location);
    }

    public void sendRealTimePositioningMessage() {
        Message message = Message.obtain();
        message.what = TXMSFragment.REALTIME_POSITIONING;
        handler.sendMessage(message);
    }
    
    public void setOnTXRTLocationListener(OnTXRTLocationListener listener) {
        mOnTXRTLocationListener = listener;
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
            final Location locationTX = new Location(tencentLocation.getProvider());
            //设置经纬度以及精度
            locationTX.setLatitude(tencentLocation.getLatitude());
            locationTX.setLongitude(tencentLocation.getLongitude());
            locationTX.setAccuracy(tencentLocation.getAccuracy());

            locationChangedListener.onLocationChanged(locationTX);
            moveToGPSLocation(locationTX);

            //显示回调的实时位置信息
//            getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        //对地图操作类进行操作
////                        CameraUpdate cameraSigma =
////                            CameraUpdateFactory.newCameraPosition(new CameraPosition(
////                                                                      convertToLatLng(location),
////                                                                      15,
////                                                                      0f,
////                                                                      0f));
////                        //移动地图
////                        tencentMap.moveCamera(cameraSigma);
////                        Rules.getEffectInfo(location);
////                        double distance = DistanceUtils.getDistance(
////                            locationA.getLatitude(), 
////                            locationA.getLongitude(),
////                            locationB.getLatitude(),
////                            locationB.getLongitude()
////                        );
//                        String szTemp = String.format("TX MyLocation Init Info\nLatitude %f, Longitude %f, Accuracy %f", locationTX.getLatitude(), locationTX.getLongitude(), locationTX.getAccuracy());
//                        mtvTXMyLocationInfo.setText(szTemp);
//                        LogUtils.d(TAG, szTemp);
//                        //打印tencentLocation的json字符串
////                    Toast.makeText(getApplicationContext(), new Gson().toJson(location), Toast.LENGTH_LONG).show();
//
//
//                        //
//                        // 本机 GPS 定位服务调用服务
//                        //
//                        locationManagerPhoneGPS = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
//                        String provider = LocationManager.GPS_PROVIDER;
//                        //Location location = locationManager.getLastKnownLocation(provider);
//                        locationManagerPhoneGPS.requestLocationUpdates(provider, 2000, 10, phoneGPSLocationListener);
//                    }
//                });

            // 保存最后定位信息
//            lastLocation = new Location(tencentLocation.getProvider());
//            lastLocation.setLatitude(tencentLocation.getLatitude());
//            lastLocation.setLongitude(tencentLocation.getLongitude());
//            lastLocation.setAccuracy(tencentLocation.getAccuracy());

            //PositionsFragment.sendInitPositioningMessage(locationTX);
            mOnTXRTLocationListener.onTXRTLocation(locationTX);

            // 当不再需要定位时
            // 取消定位监听
            if (mTencentLocationManager != null) {
                mTencentLocationManager.removeUpdates(this);
            }
            // 关闭当前位置显示
//            if (tencentMap != null) {
//                tencentMap.setMyLocationEnabled(false);
//            }


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

        int err = mTencentLocationManager.requestLocationUpdates(mTencentLocationRequest, this, Looper.myLooper());
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
        mTencentLocationManager.removeUpdates(this);
        mTencentLocationManager = null;
        mTencentLocationRequest = null;
        locationChangedListener = null;
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
