package soda.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {

    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MLocationListener();

    private boolean isFirstLocation = true;
    private int mCurrentDirection = 0;
    private MyOrientationListener mMyOrientationListener;
    private float mCurrentAccracy;
    private double mCurrentLat;
    private double mCurrentLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        initLocation();
        initOrientationListener();
    }

    private void initOrientationListener() {
        mMyOrientationListener = new MyOrientationListener(this);
        mMyOrientationListener.setOnOrientationChangeListener(new MyOrientationListener.OnOrientationChangeListener() {
            @Override
            public void onOrientationChange(float x) {
                mCurrentDirection = (int) x;
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mCurrentDirection).latitude(mCurrentLat)
                        .longitude(mCurrentLong).build();
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);
                //创建定位光标
                BitmapDescriptor maker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
                MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, maker);
                mBaiduMap.setMyLocationConfigeration(myLocationConfiguration);
            }
        });
    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
    }


    /**
     * 节约资源，在onstart onstop方法中开启/关闭定位
     */

    @Override
    protected void onStart() {
        super.onStart();
        //打开图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted())
            mLocationClient.start();
        mMyOrientationListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭图层定位
        mBaiduMap.setMyLocationEnabled(false);
        if (mLocationClient.isStarted())
            mLocationClient.stop();
        mMyOrientationListener.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_normal:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.menu_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.menu_traffic:
                boolean open = mBaiduMap.isTrafficEnabled();
                mBaiduMap.setTrafficEnabled(!open);
                item.setTitle(open?"实时路况(off)":"实时路况(on)");
                break;
        }
        return true;
    }

    class MLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapview 销毁后不做操作
            if (location == null && mBaiduMap == null)
                return;
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mCurrentAccracy = location.getRadius();
            mCurrentLat = location.getLatitude();
            mCurrentLong = location.getLongitude();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            //创建定位光标
            BitmapDescriptor maker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, maker);
            mBaiduMap.setMyLocationConfigeration(myLocationConfiguration);
            if (isFirstLocation) {
                isFirstLocation = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }
    }

}
