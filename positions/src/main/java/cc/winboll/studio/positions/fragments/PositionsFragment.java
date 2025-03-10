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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.positions.R;
import android.widget.Button;

public class PositionsFragment extends Fragment {
    
    public static final String TAG = "ContactsFragment";
    
    private static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private LocationManager locationManager;
    private TextView locationTextView;
    double latitudeLock;
    double longitudeLock;
    
    
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
        locationTextView = viewMain.findViewById(R.id.current_position_tv);
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        
        Button btnLockingPosition = viewMain.findViewById(R.id.locking_position_btn);
        btnLockingPosition.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View p1) {
                    TXMSFragment.moveToLocation(latitudeLock, longitudeLock);
                }
            });

        // 请求GPS定位
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);

        // 请求基站（网络）定位
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
        
        return viewMain;
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
            locationManager.removeUpdates(gpsLocationListener);
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
    
    private LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 处理GPS定位结果
            updateLocation(location);
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
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void updateLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            
            latitudeLock = latitude;
            longitudeLock = longitude;

            // 简单的融合示例：这里只是显示最后获取到的位置，实际应用中需要更复杂的融合算法
            locationTextView.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
        }
    }
}
