package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMapQuery;
import com.mobnote.golukmain.R;
import com.mobnote.map.GoogleMapTools;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class GooglemapLiveActivity extends AbstractLiveActivity implements OnMapReadyCallback,OnMapLoadedCallback{

	private MapView mMapView;
	private GoogleMap mGoogleMap;

	@Override
	public void LocationCallBack(String gpsJson) {
		// TODO Auto-generated method stub
		if (isLiveUploadTimeOut) {
			// 不更新数据
			return;
		}

        GolukDebugUtils.e("", "leege----------gpsJson: " + gpsJson);

		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		if (null != location && null != mApp && null != mMapTools) {
			if (mApp.isUserLoginSucess) {
				if (null == myInfo) {
					myInfo = mApp.getMyInfo();
				}
				if (null != myInfo) {
					if (LOCATION_TYPE_UNKNOW == this.mCurrentLocationType) {
						// 当前是未定位的,　直接画气泡
					} else if (LOCATION_TYPE_POINT == mCurrentLocationType) {
						// 当前画的是蓝点，需要清除掉蓝点，再画气泡
					} else {
						// 当前是画的气泡，直接更新气泡的位置即可
						mMapTools.updatePosition(myInfo.aid, location.rawLon, location.rawLat);
					}
					// 设置当前画的是头像
					mCurrentLocationType = LOCATION_TYPE_HEAD;
				}
			} else {
				drawMyPosition(location.rawLon, location.rawLat, location.radius);
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

        mMapView.setClickable(true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mMapRootLayout.addView(mMapView, 0, params);
        mMapView.onCreate(saveInstance);
        mMapView.getMapAsync(this);
	}

    @Override
	public void drawPersonsHead() {
		// TODO Auto-generated method stub

        try {
            drawMyLocation();

            if (isShareLive) {
                // 自己直播不再绘制其它人的点
                return;
            }
            if (null == currentUserInfo) {
                GolukUtils.showToast(this, this.getString(R.string.str_live_cannot_get_coordinates));
                return;
            }

            mMapTools.addSinglePoint(JsonUtil.UserInfoToString(currentUserInfo));
        } catch (Exception e) {
            e.printStackTrace();
            GolukDebugUtils.e(null,"jyf----20150406----LiveActivity----drawPersonsHead---4-Exception : ");
        }
	}

	@Override
	public void drawMyLocation() {
		// TODO Auto-generated method stub

        BaiduPosition myPosition = JsonUtil.parseLocatoinJson(mApp.mGoluk
                .GolukLogicCommGet(GolukModule.Goluk_Module_Location, ILocationFn.LOCATION_CMD_GET_POSITION, ""));
        if (null == myPosition) {
            GolukUtils.showToast(this, this.getString(R.string.str_live_cannot_get_location));
            return;
        }

        // 开始绘制我的位置
        if (mApp.isUserLoginSucess) {
            if (null == myInfo) {
                myInfo = mApp.getMyInfo();
            }
            GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---3: " + myInfo.nickname);
            if (null != myInfo) {
                GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---4: ");
                mCurrentLocationType = LOCATION_TYPE_HEAD;
                myInfo.lon = String.valueOf(myPosition.rawLon);
                myInfo.lat = String.valueOf(myPosition.rawLat);
                String drawTxt = JsonUtil.UserInfoToString(myInfo);
                mMapTools.addSinglePoint(drawTxt);
                GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---5: " + drawTxt);
            }

            GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---6: ");

        } else {
            GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---7: ");
            mCurrentLocationType = LOCATION_TYPE_POINT;

        }
	}

	@Override
	public void drawMyPosition(double lon, double lat, double radius) {
		// TODO Auto-generated method stub
	}

	@Override
	public void toMyLocation() {
		// TODO Auto-generated method stub
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LngLat.lat, LngLat.lng)));
	}

	@Override
	public void onMapReady(GoogleMap map) {
		// TODO Auto-generated method stub
        mGoogleMap = map;
        if(currentUserInfo != null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(Double.valueOf(currentUserInfo.lat), Double.valueOf(currentUserInfo.lon)))      // Sets the center of the map to Mountain View
                    .zoom(10)                   // Sets the zoom
                    .build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        mMapTools = new GoogleMapTools(this, mApp, mGoogleMap, "LiveVideo");
        mGoogleMap.setOnMapLoadedCallback(this);
        mGoogleMap.setMyLocationEnabled(false);
        drawPersonsHead();
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
        EventBus.getDefault().post(
                new EventMapQuery(EventConfig.LIVE_MAP_QUERY));
	}

}
