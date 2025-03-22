package cc.winboll.studio.positions.fragments;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 12:57:50
 * @Describe 联系人
 */
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.models.PostionFixModel;
import cc.winboll.studio.positions.utils.LocationFusion;
import cc.winboll.studio.positions.utils.TimeUtils;
import android.widget.EditText;
import android.widget.Switch;

public class PositionsFragment extends Fragment {

    public static final String TAG = "PositionsFragment";

    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private LocationManager locationManager;

    static MyHandler _MyHandler;

    TextView mtvTXMyLocationInfo;
    TextView mtvPhoneMyLocationInfo;
    TextView mtvPostionFixModelInfo;
    TextView mtvLockPostionInfo;

    EditText metLockLatitude;
    EditText metLockLongitude;
    
    Switch mswTaskService;

    double latitudeWifiLock;
    double longitudeWifiLock;
    double latitudeGPSLock;
    double longitudeGPSLock;
    double latitudeFuseLock;
    double longitudeFuseLock;

    PostionFixModel mPostionFixModel;
    static Location _LocationTX;
    static Location _LocationPhoneGPS;
    static Location _LocationPhoneGPSLock;

    LocationManager locationManagerPhoneGPS;
    volatile static int nFixActivationCountValue = 70;
    volatile static int nCurrentFixActivationCountValue = 0;
    volatile static int nGPSUpdateCount = 0;

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

        nCurrentFixActivationCountValue = 0;
        nGPSUpdateCount = 0;

//        Toolbar toolbar = viewMain.findViewById(R.id.toolbar);
//        getActivity().getMenuInflater().inflate(R.menu.toolbar_positions,  toolbar.getMenu());
//        
        mtvTXMyLocationInfo = viewMain.findViewById(R.id.txmylocationinfo_tv);
        mtvPhoneMyLocationInfo = viewMain.findViewById(R.id.phonemylocationinfo_tv);
        mtvPostionFixModelInfo = viewMain.findViewById(R.id.postionfixmodelinfo_tv);
        mtvLockPostionInfo = viewMain.findViewById(R.id.lockpostioninfo_tv);

        metLockLatitude = viewMain.findViewById(R.id.locklatitude_et);
        metLockLongitude = viewMain.findViewById(R.id.locklongitude_et);
        metLockLatitude.setEnabled(false);
        metLockLongitude.setEnabled(false);

//        tvWifiLocation = viewMain.findViewById(R.id.wifi_position_tv);
//        tvGPSLocation = viewMain.findViewById(R.id.gps_position_tv);
//        tvFuseLocation = viewMain.findViewById(R.id.fuse_position_tv);
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        mswTaskService = viewMain.findViewById(R.id.taskservice_sw);
//        mswTaskService.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View p1) {
//                    
//                }
//            });

        // 请求GPS定位
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, phoneGPSLocationListener);

        // 请求基站（网络）定位
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);

        _MyHandler = new MyHandler();

        //ToastUtils.show("PositionsFragment onCreateView");

        showLocationTX();
        showLocationPhoneGPS();
        showPostionFixModelInfo();

        return viewMain;
    }
    
    void moveToCurrentLocation() {
        if (!metLockLatitude.getText().toString().trim().equals("")
            && !metLockLongitude.getText().toString().trim().equals("")) {
            _LocationPhoneGPSLock = new Location("User_Defined_GPS");
            _LocationPhoneGPSLock.setLatitude(Double.parseDouble(metLockLatitude.getText().toString()));
            _LocationPhoneGPSLock.setLongitude(Double.parseDouble(metLockLongitude.getText().toString()));
            //ToastUtils.show("定位手动设定位置");
        } else {
            Location locationFix = fixGPSLocationFromPostionFixModel(_LocationPhoneGPS);
            //_LocationPhoneGPSLock = _LocationTX;
            _LocationPhoneGPSLock = locationFix;
           //ToastUtils.show("定位GPS设定位置");
        }
        showLockPostionInfo();
        //ToastUtils.show(String.format("%s", locationFix.toString()));

        TXMSFragment.moveToLocation(_LocationPhoneGPSLock.getLatitude(), _LocationPhoneGPSLock.getLongitude());
    }

    void showLocationPhoneGPS() {
        if (_LocationPhoneGPS != null) {
            String szTemp = String.format("\n(%d)PhoneGPS MyLocation Info\nLatitude %f\nLongitude %f\nAccuracy %f\n", nGPSUpdateCount, _LocationPhoneGPS.getLatitude(), _LocationPhoneGPS.getLongitude(), _LocationPhoneGPS.getAccuracy());
            mtvPhoneMyLocationInfo.append(szTemp);
            LogUtils.d(TAG, szTemp);
        }
    }

    void showPostionFixModelInfo() {
        if (mPostionFixModel != null && _LocationTX != null && _LocationPhoneGPS != null) {
            String szTemp = String.format("\n(%d)FixModel Info\nLatitude TX %f\nLatitude GPS %f\nLatitude Fix %f\nLongitude TX %f\nLongitude GPS %f\nLongitude Fix %f\n\n",
                                          nCurrentFixActivationCountValue,
                                          _LocationTX.getLatitude(),
                                          _LocationPhoneGPS.getLatitude(),
                                          mPostionFixModel.getLatitudeFixModel(),
                                          _LocationTX.getLongitude(),
                                          _LocationPhoneGPS.getLongitude(),
                                          mPostionFixModel.getLongitudeFixModel());
            mtvPostionFixModelInfo.append(szTemp);
            LogUtils.d(TAG, szTemp);
        }
    }

    void showLockPostionInfo() {
        if (mPostionFixModel != null && _LocationTX != null && _LocationPhoneGPSLock != null) {
            String szTemp = String.format("\n%s\nFixModel Info\nLatitude TX %f\nLatitude GPS %f\nLatitude Fix %f\nLatitude GPSLock %f\nLongitude TX %f\nLongitude GPS %f\nLongitude Fix %f\nLongitude GPSLock %f\n\n",
                                          TimeUtils.getCurrentTimeString(),
                                          _LocationTX.getLatitude(),
                                          _LocationPhoneGPS.getLatitude(),
                                          mPostionFixModel.getLatitudeFixModel(),
                                          _LocationPhoneGPSLock.getLatitude(),
                                          _LocationTX.getLongitude(),
                                          _LocationPhoneGPS.getLongitude(),
                                          mPostionFixModel.getLongitudeFixModel(),
                                          _LocationPhoneGPSLock.getLongitude());
            mtvLockPostionInfo.append(szTemp);
            LogUtils.d(TAG, szTemp);
        }
    }

    void showLocationTX() {
        if (_LocationTX != null) {
            String szTemp = String.format("TX MyLocation Init Info\nLatitude %f, Longitude %f, Accuracy %f", _LocationTX.getLatitude(), _LocationTX.getLongitude(), _LocationTX.getAccuracy());
            mtvTXMyLocationInfo.setText(szTemp);
            LogUtils.d(TAG, szTemp);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_positions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(phoneGPSLocationListener);
            locationManager.removeUpdates(networkLocationListener);
        }
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

    void updatePostionFixModel() {
        if (_LocationPhoneGPS == null
            || _LocationTX == null) {
            return;
        }

        nCurrentFixActivationCountValue++;
        if (nCurrentFixActivationCountValue < nFixActivationCountValue)  {
            mPostionFixModel = PostionFixModel.loadPostionFixModel();
            mPostionFixModel.setLatitudeFixModel(_LocationPhoneGPS.getLatitude() - _LocationTX.getLatitude());
            mPostionFixModel.setLongitudeFixModel(_LocationPhoneGPS.getLongitude() - _LocationTX.getLongitude());

//            String szTemp = String.format("PostionFixModel Info\nLatitude Fix %f, Longitude Fix %f", mPostionFixModel.getLatitudeFixModel(), mPostionFixModel.getLongitudeFixModel());
//            mtvPostionFixModelInfo.setText(szTemp);
//            LogUtils.d(TAG, szTemp);
            PostionFixModel.savePostionFixModel(mPostionFixModel);
            //ToastUtils.show(szTemp);
            LogUtils.d(TAG, String.format("updatePostionFixModel() run %d", nCurrentFixActivationCountValue));

            showPostionFixModelInfo();
        } else {
            // 定位修复模型数据定型, GPS定位监听停止
            // 在需要停止监听的地方（如onPause/onDestroy）添加：
            if (locationManagerPhoneGPS != null) {
                // 取消位置更新监听
                locationManagerPhoneGPS.removeUpdates(phoneGPSLocationListener);
                // 可选：停止后释放资源
                phoneGPSLocationListener = null;
            }
            LogUtils.d(TAG, String.format("updatePostionFixModel() stop %d", nCurrentFixActivationCountValue));
        } 
    }

    private Location fixGPSLocationFromPostionFixModel(Location location) {
        // 用腾讯定位数据与GPS定位数据的数据差修复模型，来修复一下GPS定位数据。
        mPostionFixModel = PostionFixModel.loadPostionFixModel();
        
        //Location location = locationTX;
        Location locationFix = new Location("GPS_Fix_Map_Manual");

        // 设置基础坐标
        locationFix.setLatitude(location.getLatitude() - mPostionFixModel.getLatitudeFixModel());
        locationFix.setLongitude(location.getLongitude() - mPostionFixModel.getLongitudeFixModel());

        // 设置必要元数据
        locationFix.setTime(System.currentTimeMillis());
        locationFix.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        locationFix.setAccuracy(5.0f); // 手动点击精度设为5米

        return locationFix;
    }

    private LocationListener phoneGPSLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 处理GPS定位结果
            nGPSUpdateCount++;
            updateGPSLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 处理基站（网络）定位结果
            updateWifiLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    void updateWifiLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            latitudeWifiLock = latitude;
            longitudeWifiLock = longitude;

            // 简单的融合示例：这里只是显示最后获取到的位置，实际应用中需要更复杂的融合算法
            //tvWifiLocation.setText(String.format("Wifi [ Latitude: %f \nLongitude: %f ]", latitudeWifiLock, longitudeWifiLock));
            fuseLocationData();
        }
    }

    void updateGPSLocation(Location location) {
        if (location != null) {
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//
//            latitudeGPSLock = latitude;
//            longitudeGPSLock = longitude;
//
//            // 简单的融合示例：这里只是显示最后获取到的位置，实际应用中需要更复杂的融合算法
//            tvGPSLocation.setText(String.format("GPS [ Latitude: %f \nLongitude: %f ]", latitudeGPSLock, longitudeGPSLock));
//            fuseLocationData();

            _LocationPhoneGPS = location;
            // 位置变化时的处理逻辑
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            String szTemp = String.format("Phone GPS MyLocation Init Info\nLatitude %f, Longitude %f, Accuracy %f", _LocationPhoneGPS.getLatitude(), _LocationPhoneGPS.getLongitude(), _LocationPhoneGPS.getAccuracy());
//            mtvPhoneMyLocationInfo.setText(szTemp);
//            LogUtils.d(TAG, szTemp);
            showLocationPhoneGPS();
            updatePostionFixModel();
            if(mswTaskService.isChecked()) {
                moveToCurrentLocation();
            }
        }
    }

    void fuseLocationData() {
        // 融合数据不充分退出
        if (latitudeWifiLock == 0 ||
            longitudeWifiLock == 0 ||
            latitudeGPSLock == 0 ||
            longitudeGPSLock == 0) {
            return;
        }

        double[] result = LocationFusion.fuseLocationData(latitudeGPSLock, longitudeGPSLock,
                                                          latitudeWifiLock, longitudeWifiLock,
                                                          0.6, 0.4);
        latitudeFuseLock = result[0];
        longitudeFuseLock = result[1];

        //tvFuseLocation.setText(String.format("Fuse [ Latitude: %f \nLongitude: %f ]", latitudeFuseLock, longitudeFuseLock));
    }

    public static final int INIT_POSITION = 1;
    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case INIT_POSITION:
                    // 在这里处理接收到消息后的逻辑，比如更新 UI
                    _LocationTX = (Location)msg.obj;
                    showLocationTX();

                    break;
                default:
                    break;
            }
        }
    };

    public static void sendInitPositioningMessage(Location location) {
        if (_MyHandler != null) {
            Message message = Message.obtain();
            message.what = INIT_POSITION;
            message.obj = location;
            _MyHandler.sendMessage(message);
        }
    }
}
