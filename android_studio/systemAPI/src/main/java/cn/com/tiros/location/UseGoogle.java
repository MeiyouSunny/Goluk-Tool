package cn.com.tiros.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import cn.com.tiros.api.Const;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.baidu.BaiduLocationInfo;
import cn.com.tiros.baidu.LocationAddressDetailBean;
import cn.com.tiros.debug.GolukDebugUtils;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

public class UseGoogle implements LocationListener {

	private LocationManager mLocationManager = null;
	private int mTryCount = 0;
	private PostMessageInterface mMessageInterface = null;

	@SuppressWarnings("static-access")
	public UseGoogle() {
		super();
		mLocationManager = (LocationManager) Const.getAppContext().getSystemService(
				Const.getAppContext().LOCATION_SERVICE);
	}

	public Criteria getLocationParam() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);// 设置定位经度（精确）
		criteria.setAltitudeRequired(true); // 是否需要海拔信息
		criteria.setBearingRequired(true); // 是否需要方位信息
		criteria.setCostAllowed(true); // 是否允许运营商收费
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 设置队电源的需求
		criteria.setSpeedRequired(true); // 速度

		return criteria;
	}

	public void startGoogleLocation() {
		if (null != mLocationManager) {
			mTryCount = 0;
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, BaiduLocation.LOCATION_TIMER,
					BaiduLocation.LOCATION_DISTANCE, this);
		}
	}

	public void stopGoogleLocation() {
		if (null != mLocationManager) {
			mTryCount = 0;
			mLocationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		GolukDebugUtils.e("xxx", "location--------------onLocationChanged---0");
		mTryCount++;
		if (null == location) {
			if (mTryCount > BaiduLocation.LOCATION_TRY_LIMIT) {
				BaiduLocation.postLocationInfo("-1");
			}
			return;
		}
		double lat = location.getLatitude(); // 获取维度
		double lng = location.getLongitude(); // 获取经度
		final float speed = location.getSpeed(); // 获取速度
		final float direction = location.getBearing();// 获取方向
		float radius = location.getAccuracy();// 获取半径

		if (lat == BaiduLocation.INVALID || lng == BaiduLocation.INVALID) {
			lat = BaiduLocation.TIANANMEN_LAT;
			lng = BaiduLocation.TIANANMEN_LON;
		}

		postAddress(lat, lng, speed, direction, radius);

	}

	@Override
	public void onProviderDisabled(String arg0) {
		GolukDebugUtils.e("", "--------------onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(String arg0) {
		GolukDebugUtils.e("", "--------------onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		GolukDebugUtils.e("", "--------------onStatusChanged");
	}

	public static LocationAddressDetailBean getAddress(double lat, double lng) {
		// 根据地理环境来确定编码
		Geocoder gc = new Geocoder(Const.getAppContext(), Locale.getDefault());
		LocationAddressDetailBean addressBean = new LocationAddressDetailBean();
		try {
			GolukDebugUtils.e("xxx", "location--------------getAddress--------lat: " + lat + "  lng: " + lng);
			// 取得地址相关的一些信息、经度、纬度
			List<Address> addressList = gc.getFromLocation(lat, lng, 1);
			if (addressList.size() > 0) {
				Address address = addressList.get(0);

				String countryName = address.getCountryName();
				String subAdminArea = address.getSubAdminArea();
				String locality = address.getLocality();
				String adminArea = address.getAdminArea();
				String featureName = address.getFeatureName();
				String postalCode = address.getPostalCode();
				String subLocatity = address.getSubLocality();
				String permises = address.getPremises();
				String addressLine = address.getAddressLine(0);
				String throughFrare = address.getThoroughfare();
				String subFrae = address.getSubThoroughfare();

				addressBean.countryName = countryName;
				addressBean.cityName = locality;
				addressBean.subLocatity = subLocatity;
				addressBean.throughFrare = throughFrare;
				addressBean.state = 1;
				addressBean.adminArea = adminArea;

				if (null != addressLine && null != countryName && addressLine.equals(countryName)) {
					addressBean.detail = adminArea + locality + throughFrare;
				} else {
					addressBean.detail = addressLine;
				}

				GolukDebugUtils.e("xxx", "location--------------getAddress--------2: " + "     countryName:"
						+ countryName + " locality:" + locality + "  adminArea:" + adminArea + "  subAdminArea:"
						+ subAdminArea + "  subLocatity:" + subLocatity + "  featureName:" + featureName
						+ " postalCode:" + postalCode + "permises: " + permises + "  addressLine:" + addressLine
						+ " throughFrare:" + throughFrare + "  subFrae:" + subFrae);
			}
			return addressBean;
		} catch (IOException e) {
			addressBean.detail = "";
			addressBean.state = 0;
			return addressBean;
		}
	}

	public void postAddress(final double lat, final double lng, final float speed, final float direction,
			final float radius) {
		// 根据地理环境来确定编码
		new AsyncTask<Void, Integer, LocationAddressDetailBean>() {

			@Override
			protected LocationAddressDetailBean doInBackground(Void... arg0) {
				return getAddress(lat, lng);
			}

			@Override
			protected void onPostExecute(LocationAddressDetailBean result) {
				if (result == null || TextUtils.isEmpty(result.cityName)
						|| TextUtils.isEmpty(result.subLocatity) || TextUtils.isEmpty(result.detail)) {
					return;
				}
				GolukDebugUtils.e("xxx", "location--------------onLocationChanged---1: lat: " + lat + "  lon:" + lng
						+ "  address:" + result);

				BaiduLocationInfo locationInfo = new BaiduLocationInfo();
				locationInfo.lat = lat;
				locationInfo.lon = lng;
				locationInfo.speed = speed;
				locationInfo.direction = direction;
				locationInfo.radius = radius;

				BaiduLocation.postLocationInfo(0, lat, lng, radius, speed, direction, result.detail, "-1");
				BaiduLocation.postShortLocationAddress(result.cityName  + "·" +  result.subLocatity,lat,lng);
				getMessage(locationInfo);
			}
		}.execute();

	}
	
	public void setPostGoogleMessage(PostMessageInterface messageInterface) {
		this.mMessageInterface = messageInterface;
	}
	
	private void getMessage(BaiduLocationInfo info) {
		if (null != mMessageInterface) {
			mMessageInterface.getMessage(info);
		}
	}

}
