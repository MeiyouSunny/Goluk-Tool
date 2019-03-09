package com.mobnote.golukmain.live;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import cn.com.tiros.baidu.LocationAddressDetailBean;
import cn.com.tiros.debug.GolukDebugUtils;
import cn.com.tiros.location.UseGoogle;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.mobnote.application.GolukApplication;

public class GetBaiduAddress implements OnGetGeoCoderResultListener {

	private static GetBaiduAddress mInstance = new GetBaiduAddress();
	private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	private IBaiduGeoCoderFn mListener = null;

	public static final int FUN_GET_ADDRESS = 0;
	private static final int UPDATE = 1;
	
	private GetBaiduAddress() {
		if (GolukApplication.getInstance().isMainland()) {
			// 初始化搜索模块，注册事件监听
			try {
				mSearch = GeoCoder.newInstance();
				mSearch.setOnGetGeoCodeResultListener(this);
			} catch (Exception e) {
				GolukDebugUtils.e("baidu SDK",e.getLocalizedMessage());
			}
		}

		GolukDebugUtils.e("", "GetBaiduAddress------------------init");
	}

	public static GetBaiduAddress getInstance() {
		return mInstance;
	}

	public interface IBaiduGeoCoderFn {
		public void CallBack_BaiduGeoCoder(int function, Object obj);
	}

	public void setCallBackListener(IBaiduGeoCoderFn fn) {
		mListener = fn;
	}

	public void searchAddress(double lat, double lon) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----searchAddress----1111111  : ");
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----searchAddress----22222  : ");
		mHandler.removeMessages(UPDATE);
		Message msg = mHandler.obtainMessage(UPDATE);
		if (GolukApplication.getInstance().isMainland()) {
			msg.obj = new LatLng(lat, lon);
		} else {
			msg.obj = new Position(lat, lon);
		}
		mHandler.sendMessageDelayed(msg, 1000);
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----searchAddress----33333  : ");
	}

	public void exit() {
		mHandler.removeMessages(UPDATE);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE:
				removeMessages(UPDATE);
				if (isNetworkConnected(GolukApplication.getInstance())) {
					if (GolukApplication.getInstance().isMainland()) {
						LatLng p = (LatLng) msg.obj;
						// 反Geo搜索
						mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(p));
						GolukDebugUtils.e("", "jyf----20150406----LiveActivity----searchAddress--reverseGeoCode--4444  : ");
					} else {
						final Position ptCenter = (Position) msg.obj;
						new AsyncTask<Void, Integer, LocationAddressDetailBean>() {
							@Override
							protected LocationAddressDetailBean doInBackground(Void... params) {
								// TODO Auto-generated method stub
								return UseGoogle.getAddress(ptCenter.lat, ptCenter.lon);
							}

							@Override
							protected void onPostExecute(LocationAddressDetailBean result) {
								GolukDebugUtils.e("",
										"jyf----20150406----LiveActivity----searchAddress--LocationAddressDetailBean--4444  : ");
								if (null == result) {
									return;
								}
								sendCallBackData(FUN_GET_ADDRESS, result);
							}
						}.execute();
					}
				}
				break;
			default:
				break;
			}
		};
	};

	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (null == mConnectivityManager) {
				return false;
			}
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {

	}

	private void sendCallBackData(int event, Object obj) {
		if (null == mListener) {
			return;
		}
		mListener.CallBack_BaiduGeoCoder(event, obj);
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			sendCallBackData(FUN_GET_ADDRESS, null);
			GolukDebugUtils.e("", "jyf----20150406----GetBaiduAddress----onGetReverseGeoCodeResult----NULL  : ");
			// 抱歉，未能找到结果
			return;
		}
		sendCallBackData(FUN_GET_ADDRESS, result);
	}
	
	class Position {
		Position(double la, double ln) {
			lat = la;
			lon = ln;
		}

		double lat;
		double lon;
	}

}
