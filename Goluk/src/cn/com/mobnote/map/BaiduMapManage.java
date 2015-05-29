package cn.com.mobnote.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:百度地图管理类
 * 
 * @author 陈宣宇
 * 
 */
public class BaiduMapManage {

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private String mPageSource = "";
	private BaiduMap mBaiduMap = null;
	/** 地图大头针数据集合 */
	private HashMap<Marker, Object> mMarkerData = new HashMap<Marker, Object>();
	/** 当前点击的大头针数据 */
	private String mCurrentAid = null;
	/** 头像数据标识集合 */
	private int[] mHeadImg = { 0, R.drawable.needle_boy_one_little, R.drawable.needle_boy_two_little,
			R.drawable.needle_boy_three_little, R.drawable.needle_girl_one_little, R.drawable.needle_girl_two_little,
			R.drawable.needle_girl_three_little, R.drawable.needle_index_little };
	private int[] mBigHeadImg = { 0, R.drawable.needle_boy_one_big, R.drawable.needle_boy_two_big,
			R.drawable.needle_boy_three_big, R.drawable.needle_girl_one_big, R.drawable.needle_girl_two_big,
			R.drawable.needle_girl_three_big, R.drawable.needle_index_big };
	/** 显示的气泡 */
	private View mBubbleView = null;
	/** 气泡用户名称 */
	// private TextView mNameView = null;
	/** 气泡用户速度 */
	// private TextView mSpeedView = null;
	/** 气泡显示图片 */
	// private ImageView mBubbleImage = null;

	/** 气泡显示图片 */
	private String mBubbleImageUrl = "";
	/** 当前高亮的大头针 */
	private Marker mCurrentMarker = null;
	/** 当前正操作的用户信息 */
	private UserInfo mCurrentUserInfo = null;

	@SuppressLint("InflateParams")
	public BaiduMapManage(Context context, BaiduMap map, String source) {
		mContext = context;
		mPageSource = source;
		if (null == mLayoutInflater) {
			mLayoutInflater = LayoutInflater.from(mContext);
			mBaiduMap = map;

			// mBubbleView = mLayoutInflater.inflate(R.layout.bubble,null);
			// mNameView = (TextView) mBubbleView.findViewById(R.id.username);
			// mSpeedView = (TextView) mBubbleView.findViewById(R.id.speed);
			// mBubbleImage = (ImageView)
			// mBubbleView.findViewById(R.id.bubble_img);
		}
	}

	public UserInfo getCurrentUserInfo() {
		return mCurrentUserInfo;
	}

	/**
	 * 将gps坐标转换成baidu坐标
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public LatLng ConvertLonLat(double lat, double lon) {
		// 将google地图、soso地图、aliyun地图、mapabc地图和amap地图
		// 所用坐标转换成百度坐标
		// CoordinateConverter converter = new CoordinateConverter();
		// converter.from(CoordType.COMMON);
		// sourceLatLng待转换坐标
		// converter.coord(sourceLatLng);
		// LatLng desLatLng = converter.convert();

		// 原始坐标点
		LatLng sourceLatLng = new LatLng(lat, lon);
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		// CoordinateConverter converter = new CoordinateConverter();
		// converter.from(CoordType.GPS);
		// // sourceLatLng待转换坐标
		// converter.coord(sourceLatLng);
		// LatLng point = converter.convert();
		return sourceLatLng;
	}

	/**
	 * 设置地图中心点
	 * 
	 * @param lon
	 * @param lat
	 */
	public void SetMapCenter(double lon, double lat) {
		LatLng lg = ConvertLonLat(lat, lon);
		MapStatus status = new MapStatus.Builder().target(lg).build();
		MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
		// 改变地图中心点
		mBaiduMap.setMapStatus(statusUpdate);
	}

	/**
	 * 添加地图大头针
	 * 
	 * @param map
	 * @param json
	 */
	public void AddMapPoint(JSONArray json) {
		if (null == json) {
			return;
		}
		GolukDebugUtils.e("", "jyf------AddMapPoint----11111");
		// 清楚历史marker
		mBaiduMap.clear();
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
					OverlayOptions option = new MarkerOptions().position(point).icon(bitmap).zIndex(1);
					// 在地图上添加Marker，并显示
					Marker mk = (Marker) (mBaiduMap.addOverlay(option));
					Bundle bundle = new Bundle();
					bundle.putSerializable("utype", utype);
					mk.setExtraInfo(bundle);
					mMarkerData.put(mk, data);

					mBaiduMap.setOnMarkerClickListener(new MyOnMarkerClickListener());

					GolukDebugUtils.e("", "jyf------AddMapPoint----array[2]: ");
				}
			} catch (JSONException e) {
				e.printStackTrace();
				GolukDebugUtils.e("", "jyf------AddMapPoint----array Exception: ");
			}
		}

		GolukDebugUtils.e("", "jyf------AddMapPoint----array : 333333");
	}

	// 添加单个点,不清除数据
	public void addSinglePoint(String userinfo) {
		try {
			JSONObject data = new JSONObject(userinfo);
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
				OverlayOptions option = new MarkerOptions().position(point).icon(bitmap).zIndex(1);
				// 在地图上添加Marker，并显示
				Marker mk = (Marker) (mBaiduMap.addOverlay(option));
				Bundle bundle = new Bundle();
				bundle.putSerializable("utype", utype);
				mk.setExtraInfo(bundle);
				mMarkerData.put(mk, data);

				mBaiduMap.setOnMarkerClickListener(new MyOnMarkerClickListener());
			}
		} catch (Exception e) {

		}

	}

	// 更新点的位置
	public void updatePosition(String aid, double lon, double lat) {
		if (null == mMarkerData) {
			return;
		}

		Iterator<Entry<Marker, Object>> it = mMarkerData.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Marker, Object> obj = it.next();
			try {
				UserInfo temp = JsonUtil.parseSingleUserInfoJson((JSONObject) obj.getValue());
				if (temp.aid.equals(aid)) {
					// 更新位置
					Marker marker = obj.getKey();
					LatLng point = ConvertLonLat(lat, lon);
					marker.setPosition(point);
					break;
				}
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
			mBaiduMap.clear();

			// 用户头像类型
			// String utype = data.getString("utype");
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
			OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);

			// 在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
			// Marker mk = (Marker) (mBaiduMap.addOverlay(option));
			// mMarkerData.put(mk,data);

			// mBaiduMap.setOnMarkerClickListener(new
			// MyOnMarkerClickListener(i));
		}
	}

	@SuppressLint("InflateParams")
	public void createBubbleInfo(String nickName, String speed, String lon, String lat, String open) {
		// OnInfoWindowClickListener listener = new OnInfoWindowClickListener()
		// {
		// public void onInfoWindowClick() {
		//
		// }
		// };

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

		mBubbleView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GolukDebugUtils.e("", "jyf-----click------1111");
				if (mPageSource == "Main") {
					GolukDebugUtils.e("", "jyf-----click------2222");
					lookOtherLive();
				}
			}
		});

		// 当前全屏,改成半屏
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		float density = dm.density;
		int offset = (int) (71 * density);

		// InfoWindow mInfoWindow = new
		// InfoWindow(BitmapDescriptorFactory.fromView(mBubbleView),pt,
		// -90,listener);
		InfoWindow mInfoWindow = new InfoWindow(mBubbleView, pt, -offset);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}

	private void lookOtherLive() {
		// 首页地图气泡跳转到直播详情页
		// Intent bubble = new Intent(mContext, LiveVideoPlayActivity.class);
		// bubble.putExtra("cn.com.mobnote.map.aid", mCurrentAid);
		// bubble.putExtra("cn.com.mobnote.map.uid", "1");
		// bubble.putExtra("cn.com.mobnote.map.imageurl", mBubbleImageUrl);
		// mContext.startActivity(bubble);

		GolukDebugUtils.e("", "jyf-----click------3333");

		// 通知主界面要观看别人的视频
		if (mContext instanceof MainActivity) {
			GolukDebugUtils.e("", "jyf-----click------4444");
			((MainActivity) mContext).startLiveLook(mCurrentUserInfo);

			GolukDebugUtils.e("", "jyf-----click------55555");
		}
	}

	/**
	 * 地图状态改变,隐藏气泡和大头针
	 */
	public void mapStatusChange() {
		if (null != mBaiduMap) {
			// 隐藏气泡框
			mBaiduMap.hideInfoWindow();
			// 如果有当前选择的大头针,需要复原
			if (null != mCurrentMarker) {
				int utype = (Integer) mCurrentMarker.getExtraInfo().get("utype");
				int head = mHeadImg[utype];
				BitmapDescriptor sbitmap = BitmapDescriptorFactory.fromResource(head);
				mCurrentMarker.setIcon(sbitmap);
				mCurrentMarker = null;
			}
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
				// 保存图片给视频详情用
				mBubbleImageUrl = localPath;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	class MyOnMarkerClickListener implements OnMarkerClickListener {

		@Override
		public boolean onMarkerClick(Marker marker) {

			JSONObject data = (JSONObject) mMarkerData.get(marker);
			try {
				if (null != mCurrentMarker) {
					// 原来的大头针 换小图标
					int utype = (Integer) mCurrentMarker.getExtraInfo().get("utype");
					int head = mHeadImg[utype];
					BitmapDescriptor sbitmap = BitmapDescriptorFactory.fromResource(head);
					mCurrentMarker.setIcon(sbitmap);
					mCurrentMarker.setZIndex(1);
				}
				marker.setZIndex(999);
				mCurrentMarker = marker;
				// 构建Marker大图标
				int utype = (Integer) marker.getExtraInfo().get("utype");
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
				Point pt = mBaiduMap.getProjection().toScreenLocation(ll);
				pt.y = pt.y - offset;
				LatLng lg = mBaiduMap.getProjection().fromScreenLocation(pt);

				MapStatus status = new MapStatus.Builder().target(lg).build();
				MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
				// 改变地图中心点
				mBaiduMap.setMapStatus(statusUpdate);

				// 保存aid,跳转到
				mCurrentAid = aid;

				GolukDebugUtils.e("","下载气泡图片---onMarkerClick---" + picUrl);

				if (mPageSource == "Main") {
					((MainActivity) mContext).downloadBubbleImg(picUrl, aid);
				}
				createBubbleInfo(nikeName, persons, lon, lat, open);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
