package com.mobnote.golukmain.livevideo;

import android.widget.RelativeLayout;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMapQuery;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.map.BaiduMapManage;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import de.greenrobot.event.EventBus;

public class BaidumapLiveActivity extends AbstractLiveActivity implements BaiduMap.OnMapStatusChangeListener,
		BaiduMap.OnMapLoadedCallback {

	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;

	@Override
	protected void onResume() {
		super.onResume();
//		if (null != mMapView) {
//			mMapView.onResume();
//			mMapView.invalidate();
//		}
	}

	@Override
	public void onMapStatusChange(MapStatus arg0) {

	}

	@Override
	public void onMapStatusChangeFinish(MapStatus arg0) {
		// mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_MYLOCATION, 10 * 1000);
	}

	@Override
	public void onMapStatusChangeStart(MapStatus arg0) {
		mBaseHandler.removeMessages(MSG_H_TO_MYLOCATION);
	}

	@Override
	public void onMapLoaded() {
		GolukDebugUtils.e("", "jyf-------live----LiveActivity--onMapLoaded:");
		mBaseHandler.sendEmptyMessage(MSG_H_MAPLOAD_FINISH);
		EventBus.getDefault().post(new EventMapQuery(EventConfig.LIVE_MAP_QUERY));
	}

	@Override
	public void LocationCallBack(String gpsJson) {
		if (isLiveUploadTimeOut) {
			// ???????????????
			return;
		}
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----LocationCallBack  : " + gpsJson);
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		if (null == location || null == mApp || null == mMapTools) {
			return;
		}

		if (mApp.isUserLoginSucess) {
			if (null == myInfo) {
				myInfo = mApp.getMyInfo();
			}
			if (null == myInfo) {
				return;
			}
			if (LOCATION_TYPE_UNKNOW == mCurrentLocationType) {
				// ?????????????????????,??????????????????
				this.setNewMyInfo(location);
				String drawTxt = JsonUtil.UserInfoToString(myInfo);
				if (this.isShareLive()) {
					mMapTools.addSinglePoint(drawTxt, true);
				} else {
					mMapTools.addSinglePoint(drawTxt, false);
				}
			} else if (LOCATION_TYPE_POINT == mCurrentLocationType) {
				// ????????????????????????????????????????????????????????????
				// TODO ?????????????????????
				this.setNewMyInfo(location);
				String drawTxt = JsonUtil.UserInfoToString(myInfo);
				if (this.isShareLive()) {
					mMapTools.addSinglePoint(drawTxt, true);
				} else {
					mMapTools.addSinglePoint(drawTxt, false);
				}
			} else {
				updateMyPosition(myInfo, location);
			}
			// ???????????????????????????
			mCurrentLocationType = LOCATION_TYPE_HEAD;
		} else {
			drawMyPosition(location.rawLon, location.rawLat, location.radius);
		}
	}
	
	boolean isFirstLocation = true;

	private void updateMyPosition(UserInfo info, BaiduPosition location) {
		if (this.isShareLive) {
			// ?????????????????????????????????????????????????????????
			mMapTools.updatePosition(info.aid, location.rawLon, location.rawLat, false);
		} else {
			// ?????????????????????????????????????????????????????????
			mMapTools.updatePosition(info.aid, location.rawLon, location.rawLat, false);
		}
	}

	@Override
	public void initMap() {
		BaiduMapOptions options = new BaiduMapOptions();
		options.rotateGesturesEnabled(false); // ???????????????
		options.overlookingGesturesEnabled(false);
		mMapView = new MapView(this, options);
		mMapView.setClickable(true);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mMapRootLayout.addView(mMapView, 0, params);

		mMapView.showZoomControls(false);
		mMapView.showScaleControl(false);
		mBaiduMap = mMapView.getMap();
		// ??????????????????????????????????????????????????????
		mBaiduMap.setMyLocationEnabled(true);
		mMapTools = new BaiduMapManage(this, mApp, mBaiduMap, "LiveVideo");
		mBaiduMap.setOnMapStatusChangeListener(this);
		mBaiduMap.setOnMapLoadedCallback(this);
	}

	@Override
	public void drawMyPosition(double lon, double lat, double radius) {
		MyLocationData locData = new MyLocationData.Builder().accuracy((float) radius).direction(100).latitude(lat)
				.longitude(lon).build();
		// ?????????????????????????????????????????????
		mBaiduMap.setMyLocationData(locData);
	}

	@Override
	public void toMyLocation() {
		LatLng ll = new LatLng(LngLat.lat, LngLat.lng);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	@Override
	public void drawPersonsHead() {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----1: ");
		try {
			drawMyLocation();
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----2: ");
			if (isShareLive) {
				// ???????????????????????????????????????
				return;
			}
			if (null == currentUserInfo) {
				GolukUtils.showToast(this, this.getString(R.string.str_live_cannot_get_coordinates));
				return;
			}
			GolukDebugUtils
					.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----3  : " + currentUserInfo.aid);
			mMapTools.addSinglePoint(JsonUtil.UserInfoToString(currentUserInfo), true);
		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead---4-Exception : ");
		}
	}

	@Override
	public void drawMyLocation() {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation----1: ");
		BaiduPosition myPosition = JsonUtil.parseLocatoinJson(mApp.mGoluk.GolukLogicCommGet(
				GolukModule.Goluk_Module_Location, ILocationFn.LOCATION_CMD_GET_POSITION, ""));
		if (null == myPosition) {
			GolukUtils.showToast(this, this.getString(R.string.str_live_cannot_get_location));
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---2: ");
			return;
		}

		// ????????????????????????
		if (mApp.isUserLoginSucess) {
			setNewMyInfo(myPosition);
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---3: " + myInfo.nickname);
			if (null != myInfo) {
				mCurrentLocationType = LOCATION_TYPE_HEAD;
				String drawTxt = JsonUtil.UserInfoToString(myInfo);
				if (this.isShareLive) {
					mMapTools.addSinglePoint(drawTxt, true);
				} else {
					mMapTools.addSinglePoint(drawTxt, false);
				}
				GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---5: " + drawTxt);
			}
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---6: ");
		} else {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawMyLocation---7: ");
			mCurrentLocationType = LOCATION_TYPE_POINT;
			// ????????????
			MyLocationData locData = new MyLocationData.Builder().accuracy((float) myPosition.radius).direction(100)
					.latitude(myPosition.rawLat).longitude(myPosition.rawLon).build();
			// ?????????????????????????????????????????????
			mBaiduMap.setMyLocationData(locData);
		}
	}

	private void setNewMyInfo(BaiduPosition newPosition) {
		if (null == myInfo) {
			myInfo = mApp.getMyInfo();
		}
		if (null != myInfo) {
			myInfo.lon = String.valueOf(newPosition.rawLon);
			myInfo.lat = String.valueOf(newPosition.rawLat);
		}
	}

}
