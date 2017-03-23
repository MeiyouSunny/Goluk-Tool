package com.mobnote.golukmain.livevideo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.mobnote.golukmain.R;
import com.mobnote.map.LngLat;
import com.mobnote.util.GlideCircleTransform;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import cn.com.mobnote.module.location.GolukPosition;

/**
 * Created by leege100 on 2016/7/19.
 */
public class GoogleMapLiveFragment extends AbstractLiveMapViewFragment implements OnMapReadyCallback {
    private MapView mMapView;
    private GoogleMap mGoogleMap;

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
                MarkerOptions markerOptions = new MarkerOptions().position(mPublisherLatLng).icon(bitmapDescriptor);
                mPublisherMarker = mGoogleMap.addMarker(markerOptions);
            } else {
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(mHeadImg[mHeadImg.length - 1]);
                MarkerOptions markerOptions = new MarkerOptions().position(mPublisherLatLng).icon(bitmapDescriptor);
                mPublisherMarker = mGoogleMap.addMarker(markerOptions);
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
                MarkerOptions markerOptions = new MarkerOptions().position(mCurrUserLatLng).icon(bitmapDescriptor);
                mCurrUserMarker = mGoogleMap.addMarker(markerOptions);
            } else {
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(mHeadImg[mHeadImg.length - 1]);
                MarkerOptions markerOptions = new MarkerOptions().position(mCurrUserLatLng).icon(bitmapDescriptor);
                mCurrUserMarker = mGoogleMap.addMarker(markerOptions);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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

        if (null == mLiveActivity.mUserInfo) {
            GolukUtils.showToast(mLiveActivity, this.getString(R.string.str_live_cannot_get_coordinates));
            return;
        }
        if (null == mGoogleMap) {
            return;
        }

        // 定义Maker坐标点
        mPublisherLatLng = new LatLng(Double.parseDouble(mLiveActivity.mUserInfo.lat), Double.parseDouble(mLiveActivity.mUserInfo.lon));

        if (mPublisherMarker == null) {

            if (TextUtils.isEmpty(mLiveActivity.mUserInfo.customavatar)) {
                int utype = 1;
                utype = Integer.valueOf(mLiveActivity.mUserInfo.head);
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
                MarkerOptions markerOptions = new MarkerOptions().position(mPublisherLatLng).icon(bitmap);
                // 在地图上添加Marker，并显示
                mPublisherMarker = mGoogleMap.addMarker(markerOptions);
            } else {
                Glide.with(this) // could be an issue!
                        .load(mLiveActivity.mUserInfo.customavatar)
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
            drawCurrUserMarker(lat, lon);
        } else {
            LatLng point = new LatLng(lat, lon);
            mCurrUserMarker.setPosition(point);
        }
    }

    /**
     * 绘制当前用户的标记
     */
    private void drawCurrUserMarker(double lat, double lon) {

        if (mGoogleMap == null) {
            return;
        }

        mCurrUserLatLng = new LatLng(lat, lon);

        if (GolukApplication.getInstance().isUserLoginSucess) {
            if (mCurrUserMarker == null) {
                if (TextUtils.isEmpty(myUserInfo.customavatar)) {
                    int utype = 1;
                    utype = Integer.valueOf(myUserInfo.head);
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
                    MarkerOptions markerOptions = new MarkerOptions().position(mCurrUserLatLng).icon(bitmap);
                    // 在地图上添加Marker，并显示
                    mCurrUserMarker = mGoogleMap.addMarker(markerOptions);
                } else {
                    Glide.with(this) // could be an issue!
                            .load(myUserInfo.customavatar)
                            .asBitmap()
                            .transform(new GlideCircleTransform(mLiveActivity))
                            .into(mCurrUserTarget);
                }

            } else {
                mCurrUserMarker.setPosition(mCurrUserLatLng);
            }
        } else {
            if (mCurrUserMarker == null) {
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location);
                mCurrUserMarker = mGoogleMap.addMarker(
                        new MarkerOptions()
                                .position(mCurrUserLatLng)
                                .icon(bitmap));
            } else {
                mCurrUserMarker.setPosition(mCurrUserLatLng);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if( mMapView == null) {
            return;
        }
        if (isResetedView && mTopMargin > 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0, mTopMargin, 0, 0);
            mMapView.setLayoutParams(params);
        }
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
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

        mMapView = new MapView(mLiveActivity, options);
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
        if (mGoogleMap != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LngLat.lat, LngLat.lng)));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // TODO Auto-generated method stub
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(false);
        if (!mLiveActivity.isMineLiveVideo && mLiveActivity.mUserInfo != null) {
            drawPublisherMarker();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(Double.valueOf(mLiveActivity.mUserInfo.lat), Double.valueOf(mLiveActivity.mUserInfo.lon)))      // Sets the center of the map to Mountain View
                    .zoom(9)                   // Sets the zoom
                    .build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            toMyLocation();
        }
    }

    @Override
    public void onFramgentTopMarginReceived(int topMargin) {
        if (!isResetedView && mMapView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0, topMargin, 0, 0);
            mMapView.setLayoutParams(params);
            isResetedView = true;
        }
    }
}
