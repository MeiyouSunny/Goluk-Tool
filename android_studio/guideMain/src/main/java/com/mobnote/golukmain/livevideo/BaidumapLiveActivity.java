package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.widget.RelativeLayout;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.location.GolukPosition;
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
import com.mobnote.map.BaiduMapTools;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import de.greenrobot.event.EventBus;

public class BaidumapLiveActivity extends AbstractLiveActivity implements
		BaiduMap.OnMapStatusChangeListener, BaiduMap.OnMapLoadedCallback {

	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;

	@Override
	public void onMapStatusChange(MapStatus arg0) {

	}

	@Override
	public void onMapStatusChangeFinish(MapStatus arg0) {
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_MYLOCATION, 10 * 1000);
	}

	@Override
	public void onMapStatusChangeStart(MapStatus arg0) {
		mBaseHandler.removeMessages(MSG_H_TO_MYLOCATION);
	}

	@Override
	public void onMapLoaded() {
		GolukDebugUtils.e("", "jyf-------live----LiveActivity--onMapLoaded:");
		// MainActivity.mMainHandler.sendEmptyMessage(99);
		EventBus.getDefault().post(
				new EventMapQuery(EventConfig.LIVE_MAP_QUERY));
	}

	@Override
	public void LocationCallBack(String gpsJson) {
		if (isLiveUploadTimeOut) {
			// 不更新数据
			return;
		}

		GolukDebugUtils.e("",
				"jyf----20150406----LiveActivity----LocationCallBack  : " + gpsJson);

		GolukPosition location = JsonUtil.parseLocatoinJson(gpsJson);
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
						mMapTools.updatePosition(myInfo.aid,
								location.rawLon, location.rawLat);
					}
					// 设置当前画的是头像
					mCurrentLocationType = LOCATION_TYPE_HEAD;
				}
			} else {
				drawMyPosition(location.rawLon, location.rawLat,
						location.radius);
			}
		}
	}

	@Override
	public void initMap(Bundle bundle) {
		// TODO Auto-generated method stub
		BaiduMapOptions options = new BaiduMapOptions();
		options.rotateGesturesEnabled(false); // 不允许手势
		options.overlookingGesturesEnabled(false);
		mMapView = new MapView(this, options);
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
		mMapTools = new BaiduMapTools(this, mApp, mBaiduMap, "LiveVideo");
		mBaiduMap.setOnMapStatusChangeListener(this);
		mBaiduMap.setOnMapLoadedCallback(this);
	}

	@Override
	public void drawMyPosition(double lon, double lat, double radius) {
		MyLocationData locData = new MyLocationData.Builder()
				.accuracy((float) radius).direction(100).latitude(lat)
				.longitude(lon).build();
		// 确认地图我的位置点是否更新位置
		mBaiduMap.setMyLocationData(locData);
	}

	@Override
	public void toMyLocation() {
		// TODO Auto-generated method stub
		LatLng ll = new LatLng(LngLat.lat, LngLat.lng);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	@Override
	public void drawPersonsHead() {
		// TODO Auto-generated method stub
		GolukDebugUtils.e(null,
				"jyf----20150406----LiveActivity----drawPersonsHead----1: ");
		try {
			drawMyLocation();
			GolukDebugUtils
					.e(null,"jyf----20150406----LiveActivity----drawPersonsHead----2: ");
			if (isShareLive) {
				// 自己直播不再绘制其它人的点
				return;
			}
			if (null == currentUserInfo) {
				GolukUtils.showToast(this, this.getString(R.string.str_live_cannot_get_coordinates));
				return;
			}
			GolukDebugUtils.e(null,
					"jyf----20150406----LiveActivity----drawPersonsHead----3  : " + currentUserInfo.aid);
			mMapTools.addSinglePoint(JsonUtil
					.UserInfoToString(currentUserInfo));
		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils
					.e(null,"jyf----20150406----LiveActivity----drawPersonsHead---4-Exception : ");
		}
	}

	@Override
	public void drawMyLocation() {
		// TODO Auto-generated method stub
		GolukDebugUtils.e(null,"jyf----20150406----LiveActivity----drawMyLocation----1: ");

		GolukPosition myPosition = JsonUtil.parseLocatoinJson(mApp.mGoluk
				.GolukLogicCommGet(GolukModule.Goluk_Module_Location,
						ILocationFn.LOCATION_CMD_GET_POSITION, ""));
		if (null == myPosition) {
			GolukUtils.showToast(this,
					this.getString(R.string.str_live_cannot_get_location));
			GolukDebugUtils.e(null,"jyf----20150406----LiveActivity----drawMyLocation---2: ");
			return;
		}

		// 开始绘制我的位置
		if (mApp.isUserLoginSucess) {
			if (null == myInfo) {
				myInfo = mApp.getMyInfo();
			}
			GolukDebugUtils.e(null,
					"jyf----20150406----LiveActivity----drawMyLocation---3: "
							+ myInfo.nickname);
			if (null != myInfo) {
				GolukDebugUtils
						.e(null,
								"jyf----20150406----LiveActivity----drawMyLocation---4: ");
				mCurrentLocationType = LOCATION_TYPE_HEAD;
				myInfo.lon = String.valueOf(myPosition.rawLon);
				myInfo.lat = String.valueOf(myPosition.rawLat);
				String drawTxt = JsonUtil.UserInfoToString(myInfo);
				mMapTools.addSinglePoint(drawTxt);
				GolukDebugUtils.e(null,
						"jyf----20150406----LiveActivity----drawMyLocation---5: "
								+ drawTxt);
			}

			GolukDebugUtils.e(null,
					"jyf----20150406----LiveActivity----drawMyLocation---6: ");

		} else {
			GolukDebugUtils.e(null,
					"jyf----20150406----LiveActivity----drawMyLocation---7: ");
			mCurrentLocationType = LOCATION_TYPE_POINT;
			// 画小蓝点
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy((float) myPosition.radius).direction(100)
					.latitude(myPosition.rawLat).longitude(myPosition.rawLon)
					.build();
			// 确认地图我的位置点是否更新位置
			mBaiduMap.setMyLocationData(locData);
		}

	}

}
