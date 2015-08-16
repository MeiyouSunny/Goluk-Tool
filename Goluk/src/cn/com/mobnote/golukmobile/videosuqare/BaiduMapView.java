package cn.com.mobnote.golukmobile.videosuqare;

import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
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
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class BaiduMapView implements ILocationFn {
	private Context mContext = null;
	private RelativeLayout mRootLayout = null;
	private RelativeLayout indexMapLayout = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;

	/** 定时请求直播点时间 */
	private int mTiming = 1 * 60 * 1000;
	/** 是否首次定位 */
	private boolean isFirstLoc = true;
	private BaiduMapManage mBaiduMapManage = null;

	/** 我的位置按钮 */
	private Button mMapLocationBtn = null;

	private VideoCategoryActivity ma;

	/** 控制离开页面不自动请求大头针数据 */
	private boolean isCurrent = true;

	/** 首页handler用来接收消息,更新UI */
	public static Handler mBaiduHandler = null;

	private GolukApplication mApp = null;

	public BaiduMapView(Context context, GolukApplication app) {
		mContext = context;
		mApp = app;
		mRootLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.baidu_map, null);

		ma = (VideoCategoryActivity) mContext;
		ma.mApp.addLocationListener("main", this);

		initMap();
	}

	public void onResume() {
		isCurrent = true;

		boolean b = mBaiduHandler.hasMessages(2);
		if (!b) {
			Message msg = new Message();
			msg.what = 2;
			mBaiduHandler.sendMessageDelayed(msg, mTiming);
		}
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		if (null != mMapView) {
			mMapView.onResume();
			mMapView.invalidate();
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
		mBaiduMapManage = new BaiduMapManage(mContext, mApp, mBaiduMap, "Main");

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 地图加载完成事件
		mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				// 地图加载完成,请求大头针数据
				GolukDebugUtils.e("", "jyf----VideoCategoryActivity----BaiduMapView--onMapLoaded ----11111");

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

				GolukDebugUtils.e("", "jyf----VideoCategoryActivity----BaiduMapView--onMapStatusChangeStart ----: ");
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				GolukDebugUtils.e("", "jyf----VideoCategoryActivity----BaiduMapView--onMapStatusChangeFinish ----: ");
			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {
				GolukDebugUtils.e("", "jyf----VideoCategoryActivity----BaiduMapView--onMapStatusChange ----: ");
			}
		});

		// 更新UI handler
		mBaiduHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case 2:
					// 5分钟更新一次大头针数据
					ma.mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
							IPageNotifyFn.PageType_GetPinData, "");
					break;
				case 99:
					// 隐藏气泡,大头针
					mBaiduMapManage.mapStatusChange();
				}
			}
		};
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

		if (ma.mApp.getContext() instanceof CarRecorderActivity) {
			GetBaiduAddress.getInstance().searchAddress(location.rawLat, location.rawLon);
		}

	}

	/**
	 * 首页大头针数据返回
	 */
	public void pointDataCallback(int success, Object obj) {
		if (1 == success) {
			String str = (String) obj;
			GolukDebugUtils.e("", "jyf----VideoCategoryActivity----BaiduMapView--pointDataCallback ----obj: "
					+ (String) obj);
			try {
				JSONObject json = new JSONObject(str);
				// 请求成功
				JSONArray list = json.getJSONArray("info");
				mBaiduMapManage.AddMapPoint(list);
			} catch (Exception e) {

			}
		} else {
			GolukDebugUtils.e("", "请求大头针数据错误");
		}

		if (isCurrent) {
			// 不管大头针数据请求成功/失败,都需要定时5分钟请求下一次数据
			boolean b = mBaiduHandler.hasMessages(2);
			if (!b) {
				Message msg = new Message();
				msg.what = 2;
				MainActivity.mMainHandler.sendMessageDelayed(msg, mTiming);
			}
		}
	}

	public View getView() {
		return mRootLayout;
	}

	public void onDestroy() {
		ma.mApp.removeLocationListener("main");
		// TODO 先不释放，释放会引起界面退出卡死的问题
		// if (null != mMapView) {
		// mMapView.onDestroy();
		// }
	}

	protected void onPause() {
		if (null != mMapView) {
			mMapView.onPause();
		}
	}

	/**
	 * 下载气泡图片完成
	 * 
	 * @param obj
	 */
	public void downloadBubbleImageCallBack(int success, Object obj) {
		if (1 == success) {
			// 更新在线视频图片
			String imgJson = (String) obj;
			// String imgJson = "{\"path\":\"fs1:/Cache/test11.png\"}";
			GolukDebugUtils.e("", "下载气泡图片完成downloadBubbleImageCallBack:" + imgJson);
			mBaiduMapManage.bubbleImageDownload(imgJson);
		} else {
			GolukUtils.showToast(mContext, "气泡图片下载失败");
		}
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
