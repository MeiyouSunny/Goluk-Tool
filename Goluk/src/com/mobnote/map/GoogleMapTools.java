package com.mobnote.map;

//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map.Entry;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Point;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.DisplayMetrics;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//import cn.com.mobnote.logic.GolukModule;
//import cn.com.mobnote.module.page.IPageNotifyFn;
//import cn.com.tiros.api.FileUtils;
//import cn.com.tiros.debug.GolukDebugUtils;
//
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMapOptions;
//import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
//import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
//import com.google.android.gms.maps.model.BitmapDescriptor;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.mobnote.application.GolukApplication;
//import com.mobnote.golukmain.R;
//import com.mobnote.golukmain.live.UserInfo;
//import com.mobnote.map.BaiduMapManage.MyOnMarkerClickListener;
//import com.mobnote.util.JsonUtil;

public class GoogleMapTools implements IMapTools{

	@Override
	public void updatePosition(String aid, double lon, double lat,boolean isCenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSinglePoint(String pointStr,boolean isCenter) {
		// TODO Auto-generated method stub
		
	}

//	private Context mContext = null;
//	private LayoutInflater mLayoutInflater = null;
//	private String mPageSource = "";
//	private GoogleMap mMap = null;
//	/** ??????????????????????????? */
//	private HashMap<Marker, Object> mMarkerData = new HashMap<Marker, Object>();
//	/** ???????????????????????? */
//	private int[] mHeadImg = { 0, R.drawable.needle_boy_one_little, R.drawable.needle_boy_two_little,
//			R.drawable.needle_boy_three_little, R.drawable.needle_girl_one_little, R.drawable.needle_girl_two_little,
//			R.drawable.needle_girl_three_little, R.drawable.needle_index_little };
//
//	private int[] mBigHeadImg = { 0, R.drawable.needle_boy_one_big, R.drawable.needle_boy_two_big,
//			R.drawable.needle_boy_three_big, R.drawable.needle_girl_one_big, R.drawable.needle_girl_two_big,
//			R.drawable.needle_girl_three_big, R.drawable.needle_index_big };
//	/** ??????????????? */
//	private View mBubbleView = null;
//	/** ???????????????????????? */
//	private Marker mCurrentMarker = null;
//	/** ?????????????????????????????? */
//	private UserInfo mCurrentUserInfo = null;
//
//	private GolukApplication mApp = null;
//
//	@SuppressLint("InflateParams")
//	public GoogleMapTools(Context context, GolukApplication app, GoogleMap map, String source) {
//		mContext = context;
//		mApp = app;
//		mPageSource = source;
//		if (null == mLayoutInflater) {
//			mLayoutInflater = LayoutInflater.from(mContext);
//			mMap = map;
//		}
//	}
//
//	public UserInfo getCurrentUserInfo() {
//		return mCurrentUserInfo;
//	}
//
//	/** ??????handler??????????????????,??????UI */
//	public Handler manageHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			int what = msg.what;
//			switch (what) {
//			case 1:
//				mapStatusChange();
//			}
//		}
//	};
//
//	/**
//	 * ???gps???????????????baidu??????
//	 * 
//	 * @param lat
//	 * @param lon
//	 * @return
//	 */
//	public LatLng ConvertLonLat(double lat, double lon) {
//		LatLng sourceLatLng = new LatLng(lat, lon);
//		return sourceLatLng;
//	}
//
//	/**
//	 * ?????????????????????
//	 * 
//	 * @param lon
//	 * @param lat
//	 */
//	public void SetMapCenter(double lon, double lat) {
////		LatLng lg = ConvertLonLat(lat, lon);
////		MapStatus status = new MapStatus.Builder().target(lg).build();
////		MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
////		// ?????????????????????
////		mMap.setMapStatus(statusUpdate);
////		
////		GoogleMapOptions options;
////		options.
//
//	}
//
//	/**
//	 * ?????????????????????
//	 * 
//	 * @param map
//	 * @param json
//	 */
//	public void AddMapPoint(JSONArray json) {
//		if (null == json) {
//			return;
//		}
//		GolukDebugUtils.e("", "jyf------AddMapPoint----11111");
//		// ????????????marker
//		mMap.clear();
//		mMarkerData.clear();
//		mCurrentMarker = null;
//		JSONObject data;
//		for (int i = 0, len = json.length(); i < len; i++) {
//			try {
//				data = json.getJSONObject(i);
//				GolukDebugUtils.e("", "jyf------AddMapPoint----array[i]: " + data);
//				String lon = data.getString("lon");
//				String lat = data.getString("lat");
//				if (!"".equals(lon) && !"".equals(lat)) {
//					// ??????????????????
//					int utype = Integer.valueOf(data.getString("head"));
//					int head = mHeadImg[utype];
//					// ??????Maker?????????
//					LatLng point = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
//					// ??????Marker??????
//					BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
//					// ??????MarkerOption???????????????????????????Marker
//					MarkerOptions markerOptions = new MarkerOptions()
//                    .position(point)
//                    .icon(bitmap);
//					
//					// ??????????????????Marker????????????
//					Marker mk = (Marker) (mMap.addMarker(markerOptions));
//
//					mMarkerData.put(mk, data);
//					mMap.setOnMarkerClickListener(new MyOnMarkerClickListener());
//					GolukDebugUtils.e("", "jyf------AddMapPoint----array[2]: ");
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//				GolukDebugUtils.e("", "jyf------AddMapPoint----array Exception: ");
//			}
//		}
//		GolukDebugUtils.e("", "jyf------AddMapPoint----array : 333333");
//	}
//
//	@Override
//	public void addSinglePoint(String userinfo) {
//		try {
//			JSONObject data = new JSONObject(userinfo);
//			String lon = data.getString("lon");
//			String lat = data.getString("lat");
//			if (!"".equals(lon) && !"".equals(lat)) {
//				// ??????????????????
//				int utype = Integer.valueOf(data.getString("head"));
//				int head = mHeadImg[utype];
//
//				// ??????Maker?????????
//				LatLng point = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
//
//				// ??????Marker??????
//				BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
//				// ??????MarkerOption???????????????????????????Marker
//				MarkerOptions markerOptions = new MarkerOptions().position(point).icon(bitmap);
//				// ??????????????????Marker????????????
//				Marker mk = (Marker) (mMap.addMarker(markerOptions));
//		
//				mMarkerData.put(mk, data);
//
//				mMap.setOnMarkerClickListener(new MyOnMarkerClickListener());
//			}
//		} catch (Exception e) {
//		}
//	}
//
//	@Override
//	public void updatePosition(String aid, double lon, double lat) {
//		if (null == mMarkerData) {
//			return;
//		}
//
//		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----updatePosition --111 : ");
//
//		Iterator<Entry<Marker, Object>> it = mMarkerData.entrySet().iterator();
//		while (it.hasNext()) {
//			Entry<Marker, Object> obj = it.next();
//			try {
//				UserInfo temp = JsonUtil.parseSingleUserInfoJson((JSONObject) obj.getValue());
//				if (temp.aid.equals(aid)) {
//					// ????????????
//					Marker marker = obj.getKey();
//					LatLng point = ConvertLonLat(lat, lon);
//					marker.setPosition(point);
//
//					GolukDebugUtils.e("", "jyf----20150406----LiveActivity----updatePosition --222 : lat: " + lat
//							+ "  lon:" + lon);
//
//					break;
//				}
//			} catch (Exception e) {
//
//			}
//
//		}
//	}
//
//	/**
//	 * ??????????????????????????????
//	 * 
//	 * @param lon
//	 * @param lat
//	 */
//	public void AddMapPoint(String lon, String lat, String headId) {
//		if (!"".equals(lon) && !"".equals(lat)) {
//			// ????????????marker
//			mMap.clear();
//			// ??????Maker?????????
//			LatLng point = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
//
//			// ??????????????????
//			int utype = 7;
//			int head = mHeadImg[utype];
//			if (!"".equals(headId)) {
//				utype = Integer.valueOf(headId);
//				head = mHeadImg[utype];
//			}
//			// ??????Marker??????
//			BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
//			// ??????MarkerOption???????????????????????????Marker
//			MarkerOptions option = new MarkerOptions().position(point).icon(bitmap);
//
//			// ??????????????????Marker????????????
//			mMap.addMarker(option);
//		}
//	}
//
//	@SuppressLint("InflateParams")
//	public void createBubbleInfo(String nickName, String speed, String lon, String lat, String open) {
//		// ??????InfoWindow?????????view
//		// ?????????????????????InfoWindow????????????
//		LatLng pt = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
//
//		mBubbleView = mLayoutInflater.inflate(R.layout.bubble, null);
//		TextView nameView = (TextView) mBubbleView.findViewById(R.id.username);
//		TextView speedView = (TextView) mBubbleView.findViewById(R.id.speed);
//		ImageView playImage = (ImageView) mBubbleView.findViewById(R.id.play_img);
//
//		int isOpen = Integer.parseInt(open);
//		if (1 == isOpen) {
//			playImage.setVisibility(View.VISIBLE);
//		} else {
//			playImage.setVisibility(View.GONE);
//		}
//
//		nameView.setText(nickName);
//		speedView.setText(speed);
//
//		mBubbleView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				GolukDebugUtils.e("", "jyf-----click------1111");
//				if (mPageSource == "Main") {
//					GolukDebugUtils.e("", "jyf-----click------2222");
//					lookOtherLive();
//				}
//			}
//		});
//
//		// ????????????,????????????
//		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
//		float density = dm.density;
//		int offset = (int) (71 * density);
//		InfoWindowAdapter mInfoWindow = new InfoWindowAdapter() {
//			
//			@Override
//			public View getInfoWindow(Marker arg0) {
//				// TODO Auto-generated method stub
//				return mBubbleView;
//			}
//			
//			@Override
//			public View getInfoContents(Marker arg0) {
//				// TODO Auto-generated method stub
//				return mBubbleView;
//			}
//		};
//		mMap.setInfoWindowAdapter(mInfoWindow);
//	}
//
//	private void lookOtherLive() {
//		GolukDebugUtils.e("", "jyf-----click------3333");
//		// ???????????????????????????????????????
//		GolukDebugUtils.e("", "jyf-----click------4444");
//		mApp.startLiveLook(mCurrentUserInfo);
//		GolukDebugUtils.e("", "jyf-----click------55555");
//	}
//
//	/**
//	 * ??????????????????,????????????????????????
//	 */
//	public void mapStatusChange() {
//		if (null != mMap) {
//			// ???????????????
////			mMap.hideInfoWindow();
////			// ?????????????????????????????????,????????????
////			if (null != mCurrentMarker) {
////				int utype = (Integer) mCurrentMarker.getExtraInfo().get("utype");
////				int head = mHeadImg[utype];
////				BitmapDescriptor sbitmap = BitmapDescriptorFactory.fromResource(head);
////				mCurrentMarker.setIcon(sbitmap);
////				mCurrentMarker = null;
////			}
//		}
//	}
//
//	/**
//	 * ??????????????????????????????
//	 * 
//	 * @param json
//	 */
//	@SuppressWarnings("deprecation")
//	public void bubbleImageDownload(String json) {
//		try {
//			JSONObject obj = new JSONObject(json);
//			String path = obj.getString("path");
//			String localPath = FileUtils.libToJavaPath(path);
//			Drawable drawable = null;
//
//			Bitmap bitmap = BitmapFactory.decodeFile(localPath);
//			if (null != bitmap && null != mBubbleView) {
//				drawable = new BitmapDrawable(mContext.getResources(), bitmap);
//				ImageView view = (ImageView) mBubbleView.findViewById(R.id.bubble_img);
//				view.setBackgroundDrawable(drawable);
//			}
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * ??????????????????
//	 * 
//	 * @param url
//	 * @param aid
//	 */
//	@SuppressWarnings("static-access")
//	public void downloadBubbleImg(String url, String aid) {
//		GolukDebugUtils.e("", "??????????????????downloadBubbleImg:" + url + ",aid" + aid);
//		String json = "{\"purl\":\"" + url + "\",\"aid\":\"" + aid + "\",\"type\":\"1\"}";
//		GolukDebugUtils.e("", "downloadBubbleImg---json" + json);
//		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPictureByURL,
//				json);
//	}
//
//	class MyOnMarkerClickListener implements OnMarkerClickListener {
//
//		@Override
//		public boolean onMarkerClick(Marker marker) {
//
////			JSONObject data = (JSONObject) mMarkerData.get(marker);
////			try {
////				if (null != mCurrentMarker) {
////					// ?????????????????? ????????????
////					int utype = (Integer) mCurrentMarker.getExtraInfo().get("utype");
////					int head = mHeadImg[utype];
////					BitmapDescriptor sbitmap = BitmapDescriptorFactory.fromResource(head);
////					mCurrentMarker.setIcon(sbitmap);
////					mCurrentMarker.setZIndex(1);
////				}
////				marker.setZIndex(999);
////				mCurrentMarker = marker;
////				// ??????Marker?????????
////				int utype = (Integer) marker.getExtraInfo().get("utype");
////				int head = mBigHeadImg[utype];
////				BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(head);
////				marker.setIcon(bitmap);
////
////				String picUrl = data.getString("picurl");
////				String aid = data.getString("aid");
////				String nikeName = data.getString("nickname");
////				String speed = data.getString("speed");
////				String lon = data.getString("lon");
////				String lat = data.getString("lat");
////				String open = data.getString("open");
////				String zan = data.getString("zan");
////				String persons = data.getString("persons");
////
////				GolukDebugUtils.e("", "jyf-----click------AAAAA:" + data);
////
////				// ????????????????????????
////				mCurrentUserInfo = JsonUtil.parseSingleUserInfoJson(data);
////
////				// ?????????????????????,?????????????????????????????????
////				DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
////				float density = dm.density;
////				int offset = (int) (75 * density);
////				LatLng ll = ConvertLonLat(Double.parseDouble(lat), Double.parseDouble(lon));
////				Point pt = mMap.getProjection().toScreenLocation(ll);
////				pt.y = pt.y - offset;
////				LatLng lg = mMap.getProjection().fromScreenLocation(pt);
////				MapStatus status = new MapStatus.Builder().target(lg).build();
////				MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
////				// ?????????????????????
////				mMap.setMapStatus(statusUpdate);
////				GolukDebugUtils.e("", "??????????????????---onMarkerClick---" + picUrl);
////				downloadBubbleImg(picUrl, aid);
////				createBubbleInfo(nikeName, persons, lon, lat, open);
////			} catch (JSONException e) {
////				e.printStackTrace();
////			}
////
////			manageHandler.removeMessages(1);
////			manageHandler.sendEmptyMessageDelayed(1, 10000);
//			return false;
//		}
//	}
//
//	public void release(){
//		manageHandler.removeCallbacksAndMessages(null);
//		mContext = null;
//		mApp = null;
//	}
}