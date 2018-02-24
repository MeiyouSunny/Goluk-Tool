package com.mobnote.t1sp.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;

import net.sf.marineapi.bean.GPSData;

import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.api.Gps;

/**
 * Google地图轨迹视图
 */
public class GoogleMapTrackView extends MapTrackView implements OnMapReadyCallback {

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
    private int mLineColor;
    private static final int MAX_POINT = 36000;
    //两个点的经度或者纬度大于此值时表示第二个点无效
    private static final double THRESHOLD_VALUE = 1;

    private PolylineOptions mTrackLineOverlayData;
    private TextView mTimeView;
    private GoogleMap mMap;
    boolean mSaveLocation = false;// 是否保存了当前位置
    boolean isFirstLoc = false;// 是否应用首次定位
    boolean isFirstCarLoc = true;//是否首次获取车的位置
    private int gpsType = -1;
    private Marker mTrackCarMar;
    private BitmapDescriptor mCarBitmapDescriptor;
    private BitmapDescriptor mCarBitmapStart;
    private BitmapDescriptor mCarBitmapEnd;
    private Handler mHandler = new Handler();
    private boolean mShowCarInfo = true;
    private boolean mShowCarInfoTime = true;
    boolean mTrackDraw = false;
    private boolean mapReady = false;
    private boolean googleAvailable = false;

    public GoogleMapTrackView(Context context) {
        super(context);
        initView();
    }

    public GoogleMapTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public GoogleMapTrackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    @Override
    public boolean isMapAvailable() {
        return googleAvailable;
    }

    @Override
    public void setGPSDataFromType(int value) {
        gpsType = value;
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
        if (!mapReady) {
            return;
        }
        mMap.setMyLocationEnabled(false);
        mMap = null;
        mContext = null;
    }

    @Override
    public void clear() {
        mTrackCarMar = null;
    }

    //GPSData 为标准GPS数据
    @SuppressWarnings("unchecked")
    @Override
    public void drawTrackLine(List<GPSData> list) {
        doDrawTrackLine(list);
        if (mMapListener != null)
            mMapListener.onAfterDrawLineTrack(list);
    }

    @Override
    public void drawOnlyOnePoint(GPSData gpsData) {

    }

    @Override
    public void drawTrackCar(GPSData data, boolean center, boolean connectLine) {
        LatLng ll = new LatLng(data.latitude, data.longitude);
        if (mTrackCarMar != null) {
            LatLng old = mTrackCarMar.getPosition();
            mTrackCarMar.setPosition(ll);
            mTrackCarMar.setRotation(data.angle + 90);
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
        googleAvailable = MapsInitializer.initialize(GolukApplication.getInstance().getApplicationContext()) == 0;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.google_track_view, this);
        SupportMapFragment mapFragment = (SupportMapFragment) ((FragmentActivity) getContext()).getSupportFragmentManager()
                .findFragmentById(R.id.map);
        try {
            mCarBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.image_mycar);
            mCarBitmapStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_starting);
            mCarBitmapEnd = BitmapDescriptorFactory.fromResource(R.drawable.pos_end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapFragment.getMapAsync(this);

        mLineColor = Color.parseColor("#FF1E90FF");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
    }

    //远程视频，需要播放的时候解析gps数据，然后重新画轨迹
    private List<GPSData> doDrawTrackLine(List<GPSData> list) {
        if (list == null) {
            return list;
        }
        if (mTrackDraw) {
            return list;
        }
        if (!mapReady || mMap == null) {
            return list;
        }
        mMap.clear();
        List<GPSData> gpsDataList = new ArrayList<GPSData>();
        mTrackCarMar = null;
        mTrackLineOverlayData = new PolylineOptions();
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSData> gpsDatas = new ArrayList<GPSData>();
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        GPSData preData = null;
        //int time = list.size() > 0? list.get(0).time : 0;
        int increase = 1;
        int interval = 0;
        if (list.size() > MAX_POINT) {
            increase = list.size() / MAX_POINT;
            int total = list.size() / increase;
            interval = total / (total % MAX_POINT);
        }
        Log.i(TAG, "increase = " + increase);
        Log.i(TAG, "interval = " + interval);
        for (int i = 0, count = 1; i < list.size(); i += increase, count++) {
            if (interval != 0 && count % interval == 0)
                continue;
            GPSData data = list.get(i);
            if ((data.latitude == 0 && data.longitude == 0))
                continue;
            LatLng latLng = GpsConvert.convertGps(data.latitude, data.longitude);
//            if (data.coordType == GPSData.COORD_TYPE_GPS) {
            data.latitude = latLng.latitude;
            data.longitude = latLng.longitude;
            data.coordType = GPSData.COORD_TYPE_GPS;
//            } else {
            //因为百度定位坐标不准，轨迹不画出来
//                continue;
//            }
            //当时间间隔大于DIVISION_TIME时认为是新的一段路，用不同的颜色画轨迹
            if (preData != null && ((data.time - preData.time) > DIVISION_TIME)) {
                //一段轨迹的点必须大于等于两个
                if (points.size() >= 2) {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(10)
                            .color(mLineColor);
                    mTrackLineOverlayData = polylineOptions;
                    gpsDataList.addAll(gpsDatas);
                    for (LatLng l : points)
                        builder.include(l);
                    mTrackLineOverlayData.addAll(points);
                }
                points = new ArrayList<LatLng>();
                gpsDatas = new ArrayList<GPSData>();
            }
            if (preData == null || (Math.abs(latLng.latitude - preData.latitude) < THRESHOLD_VALUE
                    && Math.abs(latLng.longitude - preData.longitude) < THRESHOLD_VALUE)) {
                points.add(latLng);
                gpsDatas.add(data);
            }
            preData = data;
        }
        if (points.size() >= 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .width(10)
                    .color(mLineColor);
            mTrackLineOverlayData = polylineOptions;
            gpsDataList.addAll(gpsDatas);
            for (LatLng l : points)
                builder.include(l);
            mTrackLineOverlayData.addAll(points);
            mMap.addPolyline(mTrackLineOverlayData);

            mTrackCarMar = mMap.addMarker(
                    new MarkerOptions().position(points.get(0)).icon(mCarBitmapDescriptor)
                            .anchor(0.5f, 0.5f).rotation(list.get(0).angle + 90).flat(true));
            // 是否显示起点终点Icon
            if (mDrawStartAndEndIcon) {
                mMap.addMarker(new MarkerOptions().icon(mCarBitmapStart).position(points.get(0)));
                mMap.addMarker(new MarkerOptions().icon(mCarBitmapEnd).position(points.get(points.size() - 1)));
            }

            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 16 * 2));
            } catch (IllegalStateException e) {
                //https://stackoverflow.com/questions/13692579/movecamera-with-cameraupdatefactory-newlatlngbounds-crashes
                final View mapView = ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.map).getView();
                if (mapView.getViewTreeObserver().isAlive()) {
                    mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener() {
                                @SuppressWarnings("deprecation")
                                @SuppressLint("NewApi")
                                // We check which build version we are using.
                                @Override
                                public void onGlobalLayout() {
                                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 16 * 2));
                                }
                            });
                }
            }
            mTrackDraw = true;
        }
        return gpsDataList;
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

}
