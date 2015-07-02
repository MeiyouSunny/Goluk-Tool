package cn.com.mobnote.golukmobile.videosuqare;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.IndexMoreActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class BaiduMapView implements ILocationFn{
	private Context mContext = null;
	private RelativeLayout mRootLayout = null;
	private RelativeLayout indexMapLayout = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	/** 是否首次定位 */
	private boolean isFirstLoc = true;
	private BaiduMapManage mBaiduMapManage = null;

	/** 我的位置按钮 */
	private Button mMapLocationBtn = null;

	/** 定位相关 */
	private LocationClient mLocClient;
	
	private MainActivity ma;
	
	public BaiduMapView(Context context) {
		mContext = context;
		mRootLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.baidu_map, null);
		
		ma = (MainActivity) mContext;
		ma.mApp.addLocationListener("main", this);
		
		initMap();
	}

	public void onResume() {
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		if (null != mMapView) {
			mMapView.onResume();
			mMapView.invalidate();
		}
		
		// 回到页面启动定位
		if (null != mLocClient) {
			mLocClient.start();
		}
		
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {

		indexMapLayout = (RelativeLayout) mRootLayout.findViewById(R.id.index_map_layout);

		// 地图我的位置按钮
		mMapLocationBtn = (Button) mRootLayout.findViewById(R.id.map_location_btn);
		// 注册事件
		mMapLocationBtn.setOnClickListener(new click());

		BaiduMapOptions options = new BaiduMapOptions();
		options.rotateGesturesEnabled(false); // 不允许手势
		options.overlookingGesturesEnabled(false);
		mMapView = new MapView(mContext, options);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		indexMapLayout.addView(mMapView, 0, params);

		// 隐藏缩放按钮
		mMapView.showZoomControls(false);
		// 缩放标尺
		mMapView.showScaleControl(false);

		// 获取map对象
		mBaiduMap = mMapView.getMap();
		mBaiduMapManage = new BaiduMapManage(mContext, mBaiduMap, "Main");

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 地图加载完成事件
		mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				// 地图加载完成,请求大头针数据
				GolukDebugUtils.e("", "PageType_GetPinData:地图加载完成,请求大头针数据");
				
				ma.mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
						IPageNotifyFn.PageType_GetPinData, "");
			}
		});

		mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				// 隐藏气泡,大头针
				mBaiduMapManage.mapStatusChange();
				// 移动了地图,第一次不改变地图中心点位置
				isFirstLoc = false;
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {
			}
		});
	}
	
	
	@Override
	public void LocationCallBack(String gpsJson) {
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		if (location == null || mMapView == null) {
			return;
		}
		// 此处设置开发者获取到的方向信息，顺时针0-360
		MyLocationData locData = new MyLocationData.Builder().accuracy((float) location.radius).direction(100)
				.latitude(location.rawLat).longitude(location.rawLon).build();
		// 确认地图我的位置点是否更新位置
		mBaiduMap.setMyLocationData(locData);

		// 移动了地图,第一次不改变地图中心点位置
		if (isFirstLoc) {
			isFirstLoc = false;
			// 移动地图中心点
			LatLng ll = new LatLng(location.rawLat, location.rawLon);
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
		}

		// 保存经纬度
		LngLat.lng = location.rawLon;
		LngLat.lat = location.rawLat;

	}
	
	public View getView() {
		return mRootLayout;
	}

	private class click implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.map_location_btn:
				// 回到我的位置
				LatLng ll = new LatLng(LngLat.lat, LngLat.lng);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				break;
			}
		}
	}

}
