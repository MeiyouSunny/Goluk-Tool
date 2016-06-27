package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMapQuery;
import com.mobnote.golukmain.R;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import cn.com.mobnote.module.location.GolukPosition;
import de.greenrobot.event.EventBus;

public class GooglemapLiveActivity extends AbstractLiveActivity01 implements OnMapReadyCallback,OnMapLoadedCallback{

	private MapView mMapView;
	private GoogleMap mGoogleMap;

    private Marker mPublisherMarker;
    private Marker mAudienceMarker;

	@Override
	public void LocationCallBack(String gpsJson) {
		// TODO Auto-generated method stub
        if(TextUtils.isEmpty(gpsJson)){
            return;
        }
        GolukPosition location = JsonUtil.parseLocatoinJson(gpsJson);
        if(location != null){
            if(isShareLive){
                updatePublisherMarker(location.rawLat,location.rawLon);
            }else{
                updateAudienceMarker(location.rawLat,location.rawLon);
            }
        }
	}

    @Override
    public void updatePublisherMarker(double lat , double lon){
        if(mPublisherMarker == null){
            drawPublisherMarker();
        }else{
            LatLng point = new LatLng(lat, lon);
            mPublisherMarker.setPosition(point);
        }
    }

    /**
     * 绘制发布者的标记
     */
    private void drawPublisherMarker(){

        if (null == currentUserInfo) {
            GolukUtils.showToast(this, this.getString(R.string.str_live_cannot_get_coordinates));
            return;
        }
        if(null == mGoogleMap){
            return;
        }

        // 定义Maker坐标点
        LatLng point = new LatLng(Double.parseDouble(currentUserInfo.lat), Double.parseDouble(currentUserInfo.lon));

        if(mPublisherMarker == null){

            int utype = 1;
            utype = Integer.valueOf(currentUserInfo.head);
            if(utype <= 0){// 防止数组越界，且不能为第0个
                utype = 1;
            }
            if(utype >= mHeadImg.length){
                utype = mHeadImg.length -1;
            }
            int head = mHeadImg[utype];

            // 构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
            // 构建MarkerOption，用于在地图上添加Marker
            MarkerOptions markerOptions = new MarkerOptions().position(point).icon(bitmap);
            // 在地图上添加Marker，并显示
            mPublisherMarker = mGoogleMap.addMarker(markerOptions);
        }else{
            mPublisherMarker.setPosition(point);
        }

    }

    @Override
    public void updateAudienceMarker(double lat , double lon){
        if(mAudienceMarker == null){
            drawAudienceMarker(lat,lon);
        }else{
            LatLng point = new LatLng(lat,lon);
            mAudienceMarker.setPosition(point);
        }
    }

    /**
     * 绘制观看用户的标记
     *
     */
    private void drawAudienceMarker(double lat , double lon){

        if(isShareLive){
            return;
        }
        if(mGoogleMap == null){
            return;
        }

        LatLng point = new LatLng(lat, lon);

        if (GolukApplication.getInstance().isUserLoginSucess) {
            if (null == myInfo) {
                myInfo = mApp.getMyInfo();
            }

            if(mAudienceMarker == null){
                int utype = 1;
                utype = Integer.valueOf(myInfo.head);
                if(utype <= 0){// 防止数组越界，且不能为第0个
                    utype = 1;
                }
                if(utype >= mHeadImg.length){
                    utype = mHeadImg.length -1;
                }
                int head = mHeadImg[utype];

                // 构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
                // 构建MarkerOption，用于在地图上添加Marker
                MarkerOptions markerOptions = new MarkerOptions().position(point).icon(bitmap);
                // 在地图上添加Marker，并显示
                mAudienceMarker = mGoogleMap.addMarker(markerOptions);
            }else{
                mAudienceMarker.setPosition(point);
            }
        } else {
            if(mAudienceMarker == null){
                mAudienceMarker = mGoogleMap.addMarker(
                        new MarkerOptions()
                                .position(point)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }else{
                mAudienceMarker.setPosition(point);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

	@Override
	public void initMap(Bundle saveInstance) {
        GoogleMapOptions options = new GoogleMapOptions();
        options.rotateGesturesEnabled(false); // 不允许手势

        mMapView = new MapView(this, options);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mMapRootLayout.addView(mMapView, 0, params);

        mMapView.setClickable(true);
        mMapView.onCreate(saveInstance);
        mMapView.getMapAsync(this);
	}

	@Override
	public void toMyLocation() {
		// TODO Auto-generated method stub
        if(mGoogleMap != null){
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LngLat.lat, LngLat.lng)));
        }
	}

	@Override
	public void onMapReady(GoogleMap map) {
		// TODO Auto-generated method stub
        mGoogleMap = map;
        if(currentUserInfo != null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(Double.valueOf(currentUserInfo.lat), Double.valueOf(currentUserInfo.lon)))      // Sets the center of the map to Mountain View
                    .zoom(9)                   // Sets the zoom
                    .build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        mGoogleMap.setOnMapLoadedCallback(this);
        mGoogleMap.setMyLocationEnabled(false);
        drawPublisherMarker();
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
        EventBus.getDefault().post(new EventMapQuery(EventConfig.LIVE_MAP_QUERY));
	}

}
