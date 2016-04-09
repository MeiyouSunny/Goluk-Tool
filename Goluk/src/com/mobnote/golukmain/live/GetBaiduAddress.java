package com.mobnote.golukmain.live;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import cn.com.tiros.debug.GolukDebugUtils;

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
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);

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
		msg.obj = new LatLng(lat, lon);
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
					LatLng ptCenter = (LatLng) msg.obj;
					// 反Geo搜索
					mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
					GolukDebugUtils.e("", "jyf----20150406----LiveActivity----searchAddress--reverseGeoCode--4444  : ");
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

}
