package com.mobnote.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.userlogin.UserInfo;
import com.mobnote.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class GoogleMapTools implements IMapTools{

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private String mPageSource = "";
	private GoogleMap mMap = null;
	/** 地图大头针数据集合 */
	private HashMap<Marker, Object> mMarkerData = new HashMap<Marker, Object>();
	/** 头像数据标识集合 */
	private int[] mHeadImg = { 0, R.drawable.needle_boy_one_little, R.drawable.needle_boy_two_little,
			R.drawable.needle_boy_three_little, R.drawable.needle_girl_one_little, R.drawable.needle_girl_two_little,
			R.drawable.needle_girl_three_little, R.drawable.needle_index_little };

	private int[] mBigHeadImg = { 0, R.drawable.needle_boy_one_big, R.drawable.needle_boy_two_big,
			R.drawable.needle_boy_three_big, R.drawable.needle_girl_one_big, R.drawable.needle_girl_two_big,
			R.drawable.needle_girl_three_big, R.drawable.needle_index_big };
	/** 显示的气泡 */
	private View mBubbleView = null;
	/** 当前高亮的大头针 */
	private Marker mCurrentMarker = null;
	/** 当前正操作的用户信息 */
	private UserInfo mCurrentUserInfo = null;

	private GolukApplication mApp = null;

	@SuppressLint("InflateParams")
	public GoogleMapTools(Context context, GolukApplication app, GoogleMap map, String source) {
		mContext = context;
		mApp = app;
		mPageSource = source;
		if (null == mLayoutInflater) {
			mLayoutInflater = LayoutInflater.from(mContext);
			mMap = map;
		}
	}

	public UserInfo getCurrentUserInfo() {
		return mCurrentUserInfo;
	}

	/** 首页handler用来接收消息,更新UI */
	public Handler manageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case 1:
				mapStatusChange();
			}
		}
	};

	/**
	 * 将gps坐标转换成baidu坐标
	 *
	 * @param lat
	 * @param lon
	 * @return
	 */
	public LatLng ConvertLonLat(double lat, double lon) {
		LatLng sourceLatLng = new LatLng(lat, lon);
		return sourceLatLng;
	}

	/**
	 * 设置地图中心点
	 *
	 * @param lon
	 * @param lat
	 */
	public void SetMapCenter(double lon, double lat) {
//		LatLng lg = ConvertLonLat(lat, lon);
//		MapStatus status = new MapStatus.Builder().target(lg).build();
//		MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
//		// 改变地图中心点
//		mMap.setMapStatus(statusUpdate);
//
//		GoogleMapOptions options;
//		options.

	}

	/**
	 * 添加地图大头针
	 *
	 * @param json
	 */
	public void AddMapPoint(JSONArray json) {
		if (null == json) {
			return;
		}
		GolukDebugUtils.e("", "jyf------AddMapPoint----11111");
		// 清楚历史marker
		mMap.clear();
		mMarkerData.clear();
		mCurrentMarker = null;
		JSONObject data;
		for (int i = 0, len = json.length(); i < len; i++) {
			try {
				data = json.getJSONObject(i);
				GolukDebugUtils.e("", "jyf------AddMapPoint----array[i]: " + data);
				String lon = data.getString("lon");
				String lat = data.getString("lat");
				if (!"".equals(lon) && !"".equals(lat)) {
					// 用户头像类型
					int utype = Integer.valueOf(data.getString("head"));
					int head = mHeadImg[utype];
					// 定义Maker坐标点
					LatLng point = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
					// 构建Marker图标
					BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
					// 构建MarkerOption，用于在地图上添加Marker
					MarkerOptions markerOptions = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);

					// 在地图上添加Marker，并显示
					Marker mk = (Marker) (mMap.addMarker(markerOptions));

					mMarkerData.put(mk, data);
					mMap.setOnMarkerClickListener(new MyOnMarkerClickListener(utype));
					GolukDebugUtils.e("", "jyf------AddMapPoint----array[2]: ");
				}
			} catch (JSONException e) {
				e.printStackTrace();
				GolukDebugUtils.e("", "jyf------AddMapPoint----array Exception: ");
			}
		}
		GolukDebugUtils.e("", "jyf------AddMapPoint----array : 333333");
	}

	@Override
	public void addSinglePoint(String userinfo) {
		try {
			GolukDebugUtils.e("", "leege----------userinfo: " + userinfo);
			JSONObject data = new JSONObject(userinfo);
			String lon = data.getString("lon");
			String lat = data.getString("lat");
			if (!"".equals(lon) && !"".equals(lat) ) {
				// 用户头像类型
				int utype = Integer.valueOf(data.getString("head"));
				int head = mHeadImg[utype];

				// 定义Maker坐标点
				LatLng point = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));

				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
				// 构建MarkerOption，用于在地图上添加Marker
				MarkerOptions markerOptions = new MarkerOptions().position(point).icon(bitmap);
				// 在地图上添加Marker，并显示
				Marker mk = (Marker) (mMap.addMarker(markerOptions));
                //mk.setExtraInfo(bundle);
				mMarkerData.put(mk, data);

				mMap.setOnMarkerClickListener(new MyOnMarkerClickListener(utype));
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void updatePosition(String aid, double lon, double lat) {
		if (null == mMarkerData) {
			return;
		}

		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----updatePosition --111 : ");

		Iterator<Entry<Marker, Object>> it = mMarkerData.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Marker, Object> obj = it.next();
			try {
				UserInfo temp = JsonUtil.parseSingleUserInfoJson((JSONObject) obj.getValue());
			} catch (Exception e) {

			}

		}
	}

	/**
	 * 根据经纬度添加大头针
	 *
	 * @param lon
	 * @param lat
	 */
	public void AddMapPoint(String lon, String lat, String headId) {
		if (!"".equals(lon) && !"".equals(lat)) {
			// 清楚历史marker
			mMap.clear();
			// 定义Maker坐标点
			LatLng point = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));

			// 用户头像类型
			int utype = 7;
			int head = mHeadImg[utype];
			if (!"".equals(headId)) {
				utype = Integer.valueOf(headId);
				head = mHeadImg[utype];
			}
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
			// 构建MarkerOption，用于在地图上添加Marker
			MarkerOptions option = new MarkerOptions().position(point).icon(bitmap);

			// 在地图上添加Marker，并显示
			mMap.addMarker(option);
		}
	}

	@SuppressLint("InflateParams")
	public void createBubbleInfo(String nickName, String speed, String lon, String lat, String open) {
		// 创建InfoWindow展示的view
		// 定义用于显示该InfoWindow的坐标点
		LatLng pt = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));

		mBubbleView = mLayoutInflater.inflate(R.layout.bubble, null);
		TextView nameView = (TextView) mBubbleView.findViewById(R.id.username);
		TextView speedView = (TextView) mBubbleView.findViewById(R.id.speed);
		ImageView playImage = (ImageView) mBubbleView.findViewById(R.id.play_img);

		int isOpen = Integer.parseInt(open);
		if (1 == isOpen) {
			playImage.setVisibility(View.VISIBLE);
		} else {
			playImage.setVisibility(View.GONE);
		}

		nameView.setText(nickName);
		speedView.setText(speed);

//		mBubbleView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				GolukDebugUtils.e("", "jyf-----mBubbleViewclick------1111");
//				if (mPageSource == "Main") {
//					GolukDebugUtils.e("", "jyf-----mBubbleViewclick------2222");
//					lookOtherLive();
//				}
//			}
//		});

		// 当前全屏,改成半屏
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		float density = dm.density;
		int offset = (int) (71 * density);
		InfoWindowAdapter mInfoWindow = new InfoWindowAdapter() {

			@Override
			public View getInfoWindow(Marker arg0) {
				// TODO Auto-generated method stub
				return mBubbleView;
			}

			@Override
			public View getInfoContents(Marker arg0) {
				// TODO Auto-generated method stub
				return mBubbleView;
			}
		};

		mMap.setInfoWindowAdapter(mInfoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mPageSource == "Main") {
                    GolukDebugUtils.e("", "jyf-----mBubbleViewclick------2222");
                    lookOtherLive();
                }
            }
        });
	}

	private void lookOtherLive() {
		GolukDebugUtils.e("", "jyf-----click------3333");
		// 通知主界面要观看别人的视频
		GolukDebugUtils.e("", "jyf-----click------4444");
		mApp.startLiveLook(mCurrentUserInfo);
		GolukDebugUtils.e("", "jyf-----click------55555");
	}

	/**
	 * 地图状态改变,隐藏气泡和大头针
	 */
	public void mapStatusChange() {
		if (null != mMap) {
			// 隐藏气泡框
//			mMap.hideInfoWindow();
//			// 如果有当前选择的大头针,需要复原
//			if (null != mCurrentMarker) {
//				int utype = (Integer) mCurrentMarker.getExtraInfo().get("utype");
//				int head = mHeadImg[utype];
//				BitmapDescriptor sbitmap = BitmapDescriptorFactory.fromResource(head);
//				mCurrentMarker.setIcon(sbitmap);
//				mCurrentMarker = null;
//			}
		}
	}

	/**
	 * 气泡图片下载完成回调
	 *
	 * @param json
	 */
	@SuppressWarnings("deprecation")
	public void bubbleImageDownload(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			String path = obj.getString("path");
			String localPath = FileUtils.libToJavaPath(path);
			Drawable drawable = null;

			Bitmap bitmap = BitmapFactory.decodeFile(localPath);
			if (null != bitmap && null != mBubbleView) {
				drawable = new BitmapDrawable(mContext.getResources(), bitmap);
				ImageView view = (ImageView) mBubbleView.findViewById(R.id.bubble_img);
				view.setBackgroundDrawable(drawable);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载气泡图片
	 *
	 * @param url
	 * @param aid
	 */
	@SuppressWarnings("static-access")
	public void downloadBubbleImg(String url, String aid) {
		GolukDebugUtils.e("", "下载气泡图片downloadBubbleImg:" + url + ",aid" + aid);
		String json = "{\"purl\":\"" + url + "\",\"aid\":\"" + aid + "\",\"type\":\"1\"}";
		GolukDebugUtils.e("", "downloadBubbleImg---json" + json);
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPictureByURL,
				json);
	}

	class MyOnMarkerClickListener implements OnMarkerClickListener {

        private int utype;
        public MyOnMarkerClickListener(int type){
            this.utype = type;
        }
		@Override
		public boolean onMarkerClick(Marker marker) {

			JSONObject data = (JSONObject) mMarkerData.get(marker);
			try {
				if (null != mCurrentMarker) {
					// 原来的大头针 换小图标
					int head = mHeadImg[utype];
					BitmapDescriptor sbitmap = BitmapDescriptorFactory.fromResource(head);
					mCurrentMarker.setIcon(sbitmap);
				}
				mCurrentMarker = marker;
				// 构建Marker大图标
				int head = mBigHeadImg[utype];
				BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
				marker.setIcon(bitmap);

				String picUrl = data.getString("picurl");
				String aid = data.getString("aid");
				String nikeName = data.getString("nickname");
				String speed = data.getString("speed");
				String lon = data.getString("lon");
				String lat = data.getString("lat");
				String open = data.getString("open");
				String zan = data.getString("zan");
				String persons = data.getString("persons");

				GolukDebugUtils.e("", "jyf-----click------AAAAA:" + data);

				// 解析获取用户信息
				mCurrentUserInfo = JsonUtil.parseSingleUserInfoJson(data);

				// 改变地图中心点,让气泡框显示到屏幕中间
				DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
				float density = dm.density;
				int offset = (int) (75 * density);
				LatLng ll = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
				Point pt = mMap.getProjection().toScreenLocation(ll);
				pt.y = pt.y - offset;
				LatLng lg = mMap.getProjection().fromScreenLocation(pt);
                if(mMap != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LngLat.lat, LngLat.lng)));
                }
//				MapStatus status = new MapStatus.Builder().target(lg).build();
//				MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
//				// 改变地图中心点
//				mMap.setMapStatus(statusUpdate);
				GolukDebugUtils.e("", "下载气泡图片---onMarkerClick---" + picUrl);
				downloadBubbleImg(picUrl, aid);
				createBubbleInfo(nikeName, persons, lon, lat, open);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			manageHandler.removeMessages(1);
			manageHandler.sendEmptyMessageDelayed(1, 10000);
			return false;
		}
	}

	public void release(){
		manageHandler.removeCallbacksAndMessages(null);
		mContext = null;
		mApp = null;
	}
}