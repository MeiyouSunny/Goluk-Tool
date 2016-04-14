package com.mobnote.golukmain.livevideo;

//import android.widget.RelativeLayout;
//import cn.com.mobnote.module.location.BaiduPosition;
//import cn.com.tiros.debug.GolukDebugUtils;
//
//import com.baidu.mapapi.map.BaiduMapOptions;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
//import com.google.android.gms.maps.GoogleMapOptions;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.UiSettings;
//import com.mobnote.map.BaiduMapManage;
//import com.mobnote.map.GoogleMapTools;
//import com.mobnote.util.JsonUtil;
public class GooglemapLiveActivity extends AbstractLiveActivity{

	@Override
	public void LocationCallBack(String gpsJson) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPersonsHead() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawMyLocation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawMyPosition(double lon, double lat, double radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toMyLocation() {
		// TODO Auto-generated method stub
		
	}

//	private MapView mMapView;
//	private GoogleMap mGoogleMap;
//	
//	@Override
//	public void LocationCallBack(String gpsJson) {
//		// TODO Auto-generated method stub
//		if (isLiveUploadTimeOut) {
//			// 不更新数据
//			return;
//		}
//
//		GolukDebugUtils.e("",
//				"jyf----20150406----LiveActivity----LocationCallBack  : " + gpsJson);
//
//		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
//		if (null != location && null != mApp && null != mMapTools) {
//			if (mApp.isUserLoginSucess) {
//				if (null == myInfo) {
//					myInfo = mApp.getMyInfo();
//				}
//				if (null != myInfo) {
//					if (LOCATION_TYPE_UNKNOW == this.mCurrentLocationType) {
//						// 当前是未定位的,　直接画气泡
//					} else if (LOCATION_TYPE_POINT == mCurrentLocationType) {
//						// 当前画的是蓝点，需要清除掉蓝点，再画气泡
//					} else {
//						// 当前是画的气泡，直接更新气泡的位置即可
//						mMapTools.updatePosition(myInfo.aid,
//								location.rawLon, location.rawLat);
//					}
//					// 设置当前画的是头像
//					mCurrentLocationType = LOCATION_TYPE_HEAD;
//				}
//			} else {
//				drawMyPosition(location.rawLon, location.rawLat,
//						location.radius);
//			}
//		}
//	}
//
//	@Override
//	public void initMap() {
//		// TODO Auto-generated method stub
//		GoogleMapOptions options = new GoogleMapOptions();
//		options.rotateGesturesEnabled(false); // 不允许手势
//		//options.overlookingGesturesEnabled(false);
//		
//		mMapView = new MapView(this, options);
//		mMapView.setClickable(true);
//
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//				RelativeLayout.LayoutParams.MATCH_PARENT,
//				RelativeLayout.LayoutParams.MATCH_PARENT);
//		mMapRootLayout.addView(mMapView, 0, params);
//
////		mMapView.showScaleControl(false);
//		
//		mGoogleMap = mMapView.getMap();
//		mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
//		mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
//		// 找开定位图层，可以显示我的位置小蓝点
//		mGoogleMap.setMyLocationEnabled(true);
//		mMapTools = new GoogleMapTools(this, mApp, mGoogleMap, "LiveVideo");
//		mGoogleMap.setOnMapLoadedCallback(this);
//	}
//
//	@Override
//	public void drawPersonsHead() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void drawMyLocation() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void drawMyPosition(double lon, double lat, double radius) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void toMyLocation() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onMapReady(GoogleMap arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onMapLoaded() {
//		// TODO Auto-generated method stub
//		
//	}

}
