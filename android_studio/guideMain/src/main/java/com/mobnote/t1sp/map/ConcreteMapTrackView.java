package com.mobnote.t1sp.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult.AddressComponent;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;

import net.sf.marineapi.bean.GPSData;

import java.util.ArrayList;
import java.util.List;

public class ConcreteMapTrackView extends MapTrackView implements OnGetGeoCoderResultListener {

    private static final String TAG = "CarSvc_BaiduTrackView";

    private static final String LAST_LOCATION_LONGITUDE = "last_location_longitude";
    private static final String LAST_LOCATION_LATITUDE = "last_location_latitude";
    public static final String LAST_LOCATION_CITY = "last_location_city";

    //分割时间，两个gps数据间隔大于此时间表示两段路程
    private static final int DIVISION_TIME = 30 * 60; //30分钟
    private static final int COLORS[] = {
            0xFF1E90FF,
            0xFFFFFF00,
            0xFFFF4500
    };
    private static final int MAX_POINT = 36000;
    //两个点的经度或者纬度大于此值时表示第二个点无效
    private static final double THRESHOLD_VALUE = 1;

    private List<PolylineOptions> mTrackLineOverlayData = new ArrayList<PolylineOptions>();
    private TextView mTimeView;
    private TextureMapView mMapView;
    private BaiduMap mBaiduMap;
    private MyLocationListenner mMyLocationListenner = new MyLocationListenner();
    boolean mSaveLocation = false;// 是否保存了当前位置
    boolean isFirstLoc = false;// 是否应用首次定位
    boolean isFirstCarLoc = true;//是否首次获取车的位置
    // 定位相关
    private LocationClient mLocClient;
    private int gpsType = -1;
    private Marker mTrackCarMar;
    private Marker mTrackCarStart;
    private Marker mTrackCarEnd;
    private BitmapDescriptor mCarBitmapDescriptor;
    private BitmapDescriptor mOnlyOnePointBitmapDescriptor;
    private BitmapDescriptor mCarBitmapStart;
    private BitmapDescriptor mCarBitmapEnd;
    private Handler mHandler = new Handler();
    private GeoCoder mSearch;
    private boolean mShowCarInfo = true;
    private boolean mShowCarInfoTime = true;

    public ConcreteMapTrackView(Context context) {
        super(context);
        initView();
    }

    public ConcreteMapTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ConcreteMapTrackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    @Override
    public boolean isMapAvailable() {
        return true;
    }

    @Override
    public void setGPSDataFromType(int value) {
        gpsType = value;
    }

    @Override
    public void onPause() {
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onResume() {
        if (mMapView != null)
            mMapView.onResume();
        if (mBaiduMap != null)
            mBaiduMap.setTrafficEnabled(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
        if (mMapView != null)
            mMapView.onDestroy();
        mSearch.destroy();
        mBaiduMap.setMyLocationEnabled(false);
        mLocClient.stop();
        mMapView = null;
        mBaiduMap = null;
        mContext = null;
    }

    @Override
    public void clear() {
        if (mMapView != null)
            mMapView.getMap().clear();
        mTrackCarMar = null;
        //setTimeText(Long.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        findViewById(R.id.baidumap_tarck_follow_button).performClick();
    }

    //GPSData 为标准GPS数据
    @SuppressWarnings("unchecked")
    @Override
    public void drawTrackLine(List<GPSData> list) {
        if (list == null) {
            if (mMapView != null)
                mMapView.getMap().clear();
            //setTimeText(Long.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            mTrackCarMar = null;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    findViewById(R.id.baidumap_tarck_follow_button).performClick();
                }
            });
            return;
        }

        new DrawTrackTask().execute(list);
    }

    @Override
    public void drawOnlyOnePoint(GPSData data) {
        LatLng point = new LatLng(data.latitude, data.longitude);
        if (data.coordType == GPSData.COORD_TYPE_GPS) {
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            // sourceLatLng待转换坐标
            converter.coord(point);
            point = converter.convert();
        } else if (data.coordType == GPSData.COORD_TYPE_AMAP) {
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.COMMON);
            // sourceLatLng待转换坐标
            converter.coord(point);
            point = converter.convert();
        }

        OverlayOptions oo = new MarkerOptions().position(point).icon(mOnlyOnePointBitmapDescriptor).zIndex(9).draggable(true).anchor(0.5f, 0.5f);
        if (mBaiduMap != null) {
            mTrackCarMar = (Marker) (mBaiduMap.addOverlay(oo));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(point);
        MapStatusUpdate statusZoom = MapStatusUpdateFactory.newLatLngZoom(point, 16);
        if (mBaiduMap != null)
            mBaiduMap.animateMapStatus(statusZoom);
    }

    //画远程视频的GPS轨迹，数据从记录仪获取
    @Override
    public void drawRemoteTrackLine(String url) {
        //new DrawRemoteTrackTask().execute(url);
    }

    @Override
    public void drawTrackCar(GPSData data, boolean center, boolean connectLine) {
        if (connectLine) {
            if (mBaiduMap != null)
                mBaiduMap.setTrafficEnabled(true);
        }
        LatLng ll = new LatLng(data.latitude, data.longitude);
        if (data.coordType == GPSData.COORD_TYPE_GPS) {
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            // sourceLatLng待转换坐标
            converter.coord(ll);
            ll = converter.convert();
        } else if (data.coordType == GPSData.COORD_TYPE_AMAP) {
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.COMMON);
            // sourceLatLng待转换坐标
            converter.coord(ll);
            ll = converter.convert();
        }

        if (ll == null)
            return;

        LatLng prell = null;
        if (mTrackCarMar == null) {
            OverlayOptions oo = new MarkerOptions().position(ll).icon(mCarBitmapDescriptor).zIndex(9).draggable(true).anchor(0.5f, 0.5f);
            if (mBaiduMap != null) {
                mTrackCarMar = (Marker) (mBaiduMap.addOverlay(oo));
            } else {
                return;
            }
        } else {
            prell = mTrackCarMar.getPosition();
        }
        mTrackCarMar.setPosition(ll);
        //gps角度是顺时针方向，setRotate设置角度是逆时针方向,地图也有一个旋转角度
        if (mBaiduMap != null)
            mTrackCarMar.setRotate((360 - data.angle + mBaiduMap.getMapStatus().rotate) % 360 - 90);

        //setTimeText(((long) data.time) * 1000, data.altitude, data.speed);
        /*
        LatLngBounds bound = mBaiduMap.getMapStatus().bound;
		if(!bound.contains(ll)){
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
		}*/
        if (center) {
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        }

//        if (isFirstCarLoc) {
//            isFirstCarLoc = false;
//            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
//            mBaiduMap.animateMapStatus(u);
//        }

        if (connectLine && prell != null) {
            if ((Math.abs(ll.latitude - prell.latitude) < THRESHOLD_VALUE
                    && Math.abs(ll.longitude - prell.longitude) < THRESHOLD_VALUE)) {
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                points.add(prell);
                points.add(ll);
                PolylineOptions polylineOptions = new PolylineOptions()
                        .width(10)
                        .color(COLORS[0])
                        .points(points);
                if (mBaiduMap != null)
                    mBaiduMap.addOverlay(polylineOptions);
            }
        }
    }

    //GPSData 为标准GPS数据
    @Override
    public void drawTrackCar(GPSData data, boolean center) {
        drawTrackCar(data, center, false);
    }

    @Override
    public void resetFirstCarLoc() {
        isFirstCarLoc = true;
    }

    @Override
    public void setLocationEnabled(boolean enable) {
        if (mBaiduMap != null) {
            if (enable) {
                mBaiduMap.setMyLocationEnabled(true);
                mLocClient.start();
            } else {
                mBaiduMap.setMyLocationEnabled(false);
                mLocClient.stop();
            }
        }
    }

    @Override
    public void setShowCarInfo(boolean show) {
        mShowCarInfo = show;
    }

    @Override
    public void setShowCarInfoTime(boolean show) {
        mShowCarInfoTime = show;
    }

    private void initView() {
        SDKInitializer.initialize(GolukApplication.getInstance().getApplicationContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCarBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.image_mycar);
        mOnlyOnePointBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_one_location);
        mCarBitmapStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_starting);
        mCarBitmapEnd = BitmapDescriptorFactory.fromResource(R.drawable.pos_end);
        inflater.inflate(R.layout.baidumap_track_view, this);

        mMapView = (TextureMapView) findViewById(R.id.baidumap_tarck_bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(false);
        //mBaiduMap.setOnMapClickListener(this);
        // 不显示百度地图的缩放按钮
        mMapView.showZoomControls(false);

        findViewById(R.id.baidumap_tarck_follow_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                BDLocation location = mLocClient.getLastKnownLocation();
//                if (location == null)
//                    return;
//                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
//                if (mBaiduMap != null)
//                    mBaiduMap.animateMapStatus(u);
            }

        });

        findViewById(R.id.baidumap_tarck_car_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                if (mTrackCarMar == null)
//                    return;
//                LatLng ll = mTrackCarMar.getPosition();
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
//                if (mBaiduMap != null)
//                    mBaiduMap.animateMapStatus(u);
            }

        });

        findViewById(R.id.baidumap_tarck_zoom_in).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MapStatusUpdate u = MapStatusUpdateFactory.zoomIn();
                if (mBaiduMap != null)
                    mBaiduMap.animateMapStatus(u);
            }

        });

        findViewById(R.id.baidumap_tarck_zoom_out).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MapStatusUpdate u = MapStatusUpdateFactory.zoomOut();
                if (mBaiduMap != null)
                    mBaiduMap.animateMapStatus(u);
            }

        });

        mTimeView = (TextView) findViewById(R.id.baidumap_track_time);

        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        mLocClient = new LocationClient(getContext());
        mLocClient.registerLocationListener(mMyLocationListenner);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String latitude = sp.getString(LAST_LOCATION_LATITUDE, "");
        String longitude = sp.getString(LAST_LOCATION_LONGITUDE, "");
        if (!latitude.isEmpty() && !longitude.isEmpty()) {
            LatLng ll = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        } else {
            isFirstLoc = true;
        }
    }

    public BaiduMap getBaiduMap() {
        return mBaiduMap;
    }

    private List<GPSData> doDrawTrackLine(List<GPSData> list) {
        List<GPSData> gpsDataList = new ArrayList<GPSData>();
        if (mMapView != null)
            mMapView.getMap().clear();
        mTrackCarMar = null;
        mTrackLineOverlayData.clear();
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSData> gpsDatas = new ArrayList<GPSData>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        GPSData preData = null;
        //int time = list.size() > 0? list.get(0).time : 0;
        int increase = 1;
        int interval = 0;
        if (list.size() > MAX_POINT) {
            increase = list.size() / MAX_POINT;
            int total = list.size() / increase;
            interval = total / (total % MAX_POINT);
        }
        for (int i = 0, count = 1; i < list.size(); i += increase, count++) {
            if (interval != 0 && count % interval == 0)
                continue;
            GPSData data = list.get(i);
            if ((data.latitude == 0 && data.longitude == 0) || data.time == 0)
                continue;
            LatLng ll = new LatLng(data.latitude, data.longitude);
            if (data.coordType == GPSData.COORD_TYPE_GPS) {
                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(ll);
                ll = converter.convert();
                data.latitude = ll.latitude;
                data.longitude = ll.longitude;
                data.coordType = GPSData.COORD_TYPE_BD0911;
            } else {
                //因为百度定位坐标不准，轨迹不画出来
                continue;
            }
            //当时间间隔大于DIVISION_TIME时认为是新的一段路，用不同的颜色画轨迹
            if (preData != null && ((data.time - preData.time) > DIVISION_TIME)) {
                //一段轨迹的点必须大于等于两个
                if (points.size() >= 2) {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(10)
                            .color(COLORS[mTrackLineOverlayData.size() % COLORS.length])
                            .points(points);
                    mTrackLineOverlayData.add(polylineOptions);
                    gpsDataList.addAll(gpsDatas);
                    for (LatLng l : points)
                        builder.include(l);
                }
                points = new ArrayList<LatLng>();
                gpsDatas = new ArrayList<GPSData>();
            }
            if (preData == null || (Math.abs(ll.latitude - preData.latitude) < THRESHOLD_VALUE
                    && Math.abs(ll.longitude - preData.longitude) < THRESHOLD_VALUE)) {
                points.add(ll);
                gpsDatas.add(data);
            }
            preData = data;
        }

        if (points.size() >= 2) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .width(10)
                    .color(COLORS[mTrackLineOverlayData.size() % COLORS.length])
                    .points(points);
            mTrackLineOverlayData.add(polylineOptions);
            gpsDataList.addAll(gpsDatas);
            for (LatLng l : points)
                builder.include(l);
        }

        //特殊处理，只有一个点的时候或者只有百度坐标的时候
        if (mTrackLineOverlayData.size() == 0) {
            if (list.size() == 1) {
                GPSData data = new GPSData();
                data.altitude = list.get(0).altitude;
                data.angle = list.get(0).angle;
                data.coordType = list.get(0).coordType;
                data.latitude = list.get(0).latitude;
                data.longitude = list.get(0).longitude;
                data.speed = list.get(0).speed;
                data.time = list.get(0).time;
                list.add(data);
            }

            points = new ArrayList<LatLng>();
            preData = null;
            for (int i = 0, count = 1; i < list.size(); i += increase, count++) {
                if (interval != 0 && count % interval == 0)
                    continue;
                GPSData data = list.get(i);
                LatLng ll = new LatLng(data.latitude, data.longitude);
                if (data.coordType == GPSData.COORD_TYPE_GPS) {
                    CoordinateConverter converter = new CoordinateConverter();
                    converter.from(CoordinateConverter.CoordType.GPS);
                    converter.coord(ll);
                    ll = converter.convert();
                    data.latitude = ll.latitude;
                    data.longitude = ll.longitude;
                    data.coordType = GPSData.COORD_TYPE_BD0911;
                } else if (data.coordType == GPSData.COORD_TYPE_AMAP) {
                    CoordinateConverter converter = new CoordinateConverter();
                    converter.from(CoordinateConverter.CoordType.COMMON);
                    converter.coord(ll);
                    ll = converter.convert();
                    data.latitude = ll.latitude;
                    data.longitude = ll.longitude;
                    data.coordType = GPSData.COORD_TYPE_BD0911;
                }
                if (preData == null || (Math.abs(ll.latitude - preData.latitude) < THRESHOLD_VALUE
                        && Math.abs(ll.longitude - preData.longitude) < THRESHOLD_VALUE)) {
                    points.add(ll);
                    gpsDataList.add(data);
                    preData = data;
                }
            }
            if (points.size() >= 2) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .width(10)
                        .color(COLORS[mTrackLineOverlayData.size() % COLORS.length])
                        .points(points);
                mTrackLineOverlayData.add(polylineOptions);
                for (LatLng l : points)
                    builder.include(l);
            } else {
                gpsDataList.clear();
            }
        }

        try {
            // 轨迹线
            for (PolylineOptions polylineOptions : mTrackLineOverlayData) {
                if (mBaiduMap != null)
                    mBaiduMap.addOverlay(polylineOptions);
            }

            // 起点终点Icon
            if (mDrawStartAndEndIcon) {
                if (points.size() >= 1) {
                    if (mTrackCarStart == null) {
                        OverlayOptions oo = new MarkerOptions().position(points.get(0)).icon(mCarBitmapStart);
                        if (mBaiduMap != null) {
                            mTrackCarStart = (Marker) (mBaiduMap.addOverlay(oo));
                        }
                    }
                    mTrackCarStart.setPosition(points.get(0));
                    if (mTrackCarEnd == null) {
                        OverlayOptions oo = new MarkerOptions().position(points.get(points.size() - 1)).icon(mCarBitmapEnd);
                        if (mBaiduMap != null) {
                            mTrackCarEnd = (Marker) (mBaiduMap.addOverlay(oo));
                        }
                    }
                    mTrackCarEnd.setPosition(points.get(points.size() - 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        try {
            if (mBaiduMap != null)
                mBaiduMap.animateMapStatus(u);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "gpsDataList.size() = " + gpsDataList.size());

        return gpsDataList;
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || mBaiduMap == null)
                return;
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            if (Math.abs(location.getLatitude()) <= 0.1 && Math.abs(location.getLongitude()) <= 0.1)
                return;
            mBaiduMap.setMyLocationData(locData);
            if (!mSaveLocation) {
                mSaveLocation = true;
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor ed = sp.edit();
                ed.putString(LAST_LOCATION_LATITUDE, "" + location.getLatitude());
                ed.putString(LAST_LOCATION_LONGITUDE, "" + location.getLongitude());
                ed.commit();
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(
                        new LatLng(location.getLatitude(), location.getLongitude())));
            }

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {

        }
    }

    class DrawTrackTask extends AsyncTask<List<GPSData>, Void, List<GPSData>> {

        @Override
        protected List<GPSData> doInBackground(List<GPSData>... params) {
            List<GPSData> list = params[0];
            return doDrawTrackLine(list);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mMapListener != null)
                mMapListener.onPreDrawLineTrack();
        }

        @Override
        protected void onPostExecute(List<GPSData> result) {
            super.onPostExecute(result);
            if (mMapListener != null)
                mMapListener.onAfterDrawLineTrack(result);
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.d(TAG, "Can not get the detail of this address from video");
            return;
        }

        AddressComponent info = result.getAddressDetail();
        if (info != null && info.city != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor ed = sp.edit();
            ed.putString(LAST_LOCATION_CITY, info.city);
            ed.commit();
        }

    }

}
