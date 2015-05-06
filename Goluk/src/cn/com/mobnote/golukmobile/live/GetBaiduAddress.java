package cn.com.mobnote.golukmobile.live;

import cn.com.tiros.utils.LogUtil;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class GetBaiduAddress implements OnGetGeoCoderResultListener {

	private static GetBaiduAddress mInstance = new GetBaiduAddress();
	private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	private IBaiduGeoCoderFn mListener = null;

	public static final int FUN_GET_ADDRESS = 0;

	private boolean isRequesting = false;

	private GetBaiduAddress() {
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
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
		if (isRequesting) {
			return;
		}
		isRequesting = true;
		LatLng ptCenter = new LatLng(lat, lon);
		// 反Geo搜索
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
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
		isRequesting = false;
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			sendCallBackData(FUN_GET_ADDRESS, null);
			LogUtil.e(null, "jyf----20150406----GetBaiduAddress----onGetReverseGeoCodeResult----NULL  : ");
			// 抱歉，未能找到结果
			return;
		}
		final String address = result.getAddress();
		sendCallBackData(FUN_GET_ADDRESS, address);

		LogUtil.e(null, "jyf----20150406----GetBaiduAddress----onGetReverseGeoCodeResult----  : " + address);
	}

}
