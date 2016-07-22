package com.mobnote.golukmain.livevideo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.map.LngLat;
import com.mobnote.util.GlideCircleTransform;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import cn.com.mobnote.module.location.GolukPosition;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Created by leege100 on 2016/7/19.
 */
public class BaiduMapLiveFragment extends AbstractLiveMapViewFragment implements BaiduMap.OnMapStatusChangeListener, BaiduMap.OnMapLoadedCallback {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;

    private Marker mPublisherMarker;
    private Marker mCurrUserMarker;

    private LatLng mPublisherLatLng;
    private LatLng mCurrUserLatLng;

    private SimpleTarget mPublisherTarget = new SimpleTarget<Bitmap>(48, 48) {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {

            if (mLiveActivity.isLiveUploadTimeOut) {
                return;
            }
            if (bitmap != null) {

                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                OverlayOptions markerOptions = new MarkerOptions().position(mPublisherLatLng).icon(bitmapDescriptor).zIndex(1);
                mPublisherMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
            } else {
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(mHeadImg[mHeadImg.length - 1]);
                OverlayOptions markerOptions = new MarkerOptions().position(mPublisherLatLng).icon(bitmapDescriptor).zIndex(1);
                mPublisherMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
            }
        }
    };

    private SimpleTarget mCurrUserTarget = new SimpleTarget<Bitmap>(48, 48) {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {

            if (mLiveActivity.isLiveUploadTimeOut) {
                return;
            }
            if (bitmap != null) {

                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                OverlayOptions markerOptions = new MarkerOptions().position(mCurrUserLatLng).icon(bitmapDescriptor).zIndex(1);
                mCurrUserMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
            } else {
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(mHeadImg[mHeadImg.length - 1]);
                OverlayOptions markerOptions = new MarkerOptions().position(mCurrUserLatLng).icon(bitmapDescriptor).zIndex(1);
                mCurrUserMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
            }
        }
    };

    @Override
    public void LocationCallBack(String gpsJson) {
        if (mLiveActivity.isLiveUploadTimeOut) {
            // 不更新数据
            return;
        }
        if (TextUtils.isEmpty(gpsJson)) {
            return;
        }
        GolukPosition location = JsonUtil.parseLocatoinJson(gpsJson);
        if (location != null) {
            updateCurrUserMarker(location.rawLat, location.rawLon);
        }

    }

    @Override
    public void updatePublisherMarker(double lat, double lon) {
        if (mPublisherMarker == null) {
            drawPublisherMarker();
        } else {
            LatLng point = new LatLng(lat, lon);
            mPublisherMarker.setPosition(point);
        }
    }

    /**
     * 绘制发布者的标记
     */
    private void drawPublisherMarker() {

        if (null == mLiveActivity.mPublisher) {
            GolukUtils.showToast(mLiveActivity, this.getString(R.string.str_live_cannot_get_coordinates));
            return;
        }
        if (null == mBaiduMap) {
            return;
        }

        // 定义Maker坐标点
        mPublisherLatLng = new LatLng(Double.parseDouble(mLiveActivity.mPublisher.lat), Double.parseDouble(mLiveActivity.mPublisher.lon));

        if (mPublisherMarker == null) {

            if (TextUtils.isEmpty(mLiveActivity.mPublisher.customavatar)) {
                int utype = 1;
                utype = Integer.valueOf(mLiveActivity.mPublisher.head);
                if (utype <= 0) {// 防止数组越界，且不能为第0个
                    utype = 1;
                }
                if (utype >= mHeadImg.length) {
                    utype = mHeadImg.length - 1;
                }
                int head = mHeadImg[utype];

                // 构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
                // 构建MarkerOption，用于在地图上添加Marker
                OverlayOptions markerOptions = new MarkerOptions().position(mPublisherLatLng).icon(bitmap).zIndex(1);
                // 在地图上添加Marker，并显示
                mPublisherMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
            } else {
                Glide.with(this) // could be an issue!
                        .load(mLiveActivity.mPublisher.customavatar)
                        .asBitmap()
                        .transform(new GlideCircleTransform(mLiveActivity))
                        .into(mPublisherTarget);
            }

        } else {
            mPublisherMarker.setPosition(mPublisherLatLng);
        }

    }

    @Override
    public void updateCurrUserMarker(double lat, double lon) {
        if (mCurrUserMarker == null) {
            drawAudienceMarker(lat, lon);
        } else {
            LatLng point = new LatLng(lat, lon);
            mCurrUserMarker.setPosition(point);
        }
    }

    /**
     * 绘制观看用户的标记
     */
    private void drawAudienceMarker(double lat, double lon) {

        if (mLiveActivity.isShareLive) {
            return;
        }
        if (mBaiduMap == null) {
            return;
        }

        mCurrUserLatLng = new LatLng(lat, lon);

        if (GolukApplication.getInstance().isUserLoginSucess) {
            if (null ==mLiveActivity. myInfo) {
                mLiveActivity.myInfo = GolukApplication.getInstance().getMyInfo();
            }

            if (mCurrUserMarker == null) {

                if (TextUtils.isEmpty(mLiveActivity.myInfo.customavatar)) {
                    int utype = 1;
                    utype = Integer.valueOf(mLiveActivity.myInfo.head);
                    if (utype <= 0) {// 防止数组越界，且不能为第0个
                        utype = 1;
                    }
                    if (utype >= mHeadImg.length) {
                        utype = mHeadImg.length - 1;
                    }
                    int head = mHeadImg[utype];

                    // 构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
                    // 构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions markerOptions = new MarkerOptions().position(mCurrUserLatLng).icon(bitmap).zIndex(1);
                    // 在地图上添加Marker，并显示
                    mCurrUserMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
                } else {
                    Glide.with(this) // could be an issue!
                            .load(mLiveActivity.myInfo.customavatar)
                            .asBitmap()
                            .transform(new GlideCircleTransform(mLiveActivity))
                            .into(mCurrUserTarget);
                }


            } else {
                mCurrUserMarker.setPosition(mCurrUserLatLng);
            }
        } else {
//            MyLocationData locData = new MyLocationData.Builder()
//                    .direction(100).latitude(lat)
//                    .longitude(lon).build();
//            // 确认地图我的位置点是否更新位置
//            mBaiduMap.setMyLocationData(locData);
            if(mCurrUserMarker == null){
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location);
                mCurrUserMarker = (Marker) mBaiduMap.addOverlay(
                        new MarkerOptions()
                                .position(mCurrUserLatLng)
                                .icon(bitmap));
            }else{
                mCurrUserMarker.setPosition(mCurrUserLatLng);
            }
        }
    }

    @Override
    public void onMapStatusChange(MapStatus arg0) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus arg0) {
        //mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_MYLOCATION, 10 * 1000);
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
       // mBaseHandler.removeMessages(MSG_H_TO_MYLOCATION);
    }

    @Override
    public void onMapLoaded() {
        GolukDebugUtils.e("", "jyf-------live----LiveActivity--onMapLoaded:");
    }

    @Override
    public void initMap(Bundle bundle) {
        // TODO Auto-generated method stub
        SDKInitializer.initialize(mLiveActivity.getApplicationContext());
        BaiduMapOptions options = new BaiduMapOptions();
        options.rotateGesturesEnabled(false); // 不允许手势
        options.overlookingGesturesEnabled(false);
        mMapView = new MapView(mLiveActivity, options);
        mMapView.setClickable(true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mMapRootLayout.addView(mMapView, 0, params);

        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        mBaiduMap = mMapView.getMap();
        // 找开定位图层，可以显示我的位置小蓝点
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnMapLoadedCallback(this);

        if(!mLiveActivity.isShareLive && mLiveActivity.mPublisher != null){
            updatePublisherMarker(Double.valueOf(mLiveActivity.mPublisher.lat),Double.valueOf(mLiveActivity.mPublisher.lon));
            LatLng ll = new LatLng(Double.valueOf(mLiveActivity.mPublisher.lat),Double.valueOf(mLiveActivity.mPublisher.lon));
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        }else{
            toMyLocation();
        }
    }

    @Override
    public void toMyLocation() {
        // TODO Auto-generated method stub
        LatLng ll = new LatLng(LngLat.lat, LngLat.lng);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(u);
    }
}
