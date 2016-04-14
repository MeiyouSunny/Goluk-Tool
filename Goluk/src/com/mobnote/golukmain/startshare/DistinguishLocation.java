package com.mobnote.golukmain.startshare;

import android.text.TextUtils;
import cn.com.tiros.baidu.LocationAddressDetailBean;

import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult.AddressComponent;

/**
 * 区分定位
 * 
 * @author lily
 *
 */
public class DistinguishLocation {

	private String mServer = "0";

	private Object mObject = null;
	private String mCity = "";
	private String mDistrict = "";
	private String mAdminArea = "";

	private LocalAddress mLocal = null;
	private InternationalAddress mInternational = null;

	public DistinguishLocation(Object obj) {
		this.mObject = obj;
		if (mServer.equals("1")) {
			mLocal = new LocalAddress(mObject);
			mCity = mLocal.getCity();
			mDistrict = mLocal.getDistrict();
		} else {
			mInternational = new InternationalAddress(mObject);
			mCity = mInternational.getCity();
			mDistrict = mInternational.getDistrict();
			mAdminArea = mInternational.getAdminArea();
		}
	}

	public String setAllAddress() {
		String currentAddress = "";
		boolean isValidCity = false;
		boolean isValidDistrict = false;
		if (!TextUtils.isEmpty(mCity)) {
			currentAddress = mCity;
			isValidCity = true;
		}
		if (!TextUtils.isEmpty(mDistrict)) {
			if (isValidCity) {
				currentAddress = currentAddress + "·" + mDistrict;
			} else {
				currentAddress = mDistrict;
			}
			isValidDistrict = true;
		}
		if (!TextUtils.isEmpty(mAdminArea)) {
			if (isValidCity && !isValidDistrict) {
				currentAddress = mAdminArea + "·" + mCity;
			}
		}
		return currentAddress;
	}

}

class LocalAddress {
	private Object object;
	private AddressComponent addressDetail;

	public LocalAddress(Object obj) {
		this.object = obj;
		ReverseGeoCodeResult result = (ReverseGeoCodeResult) obj;
		addressDetail = result.getAddressDetail();
	}

	// 获取当前城市
	public String getCity() {
		String city = "";
		if (null == addressDetail) {
			return city;
		}
		city = addressDetail.city;
		return city;
	}

	// 获取区县
	public String getDistrict() {
		String district = "";
		if (null == addressDetail) {
			return district;
		}
		district = addressDetail.district;
		return district;
	}

}

class InternationalAddress {
	private Object object;
	private LocationAddressDetailBean result;

	public InternationalAddress(Object obj) {
		this.object = obj;
		result = (LocationAddressDetailBean) obj;
	}

	// 获取当前城市
	public String getCity() {
		String city = "";
		if (null == result) {
			return city;
		}
		city = result.cityName;
		return city;
	}

	// 获取区县
	public String getDistrict() {
		String district = "";
		if (null == result) {
			return district;
		}
		district = result.subLocatity;
		return district;
	}

	// 获取地区
	public String getAdminArea() {
		String adminArea = "";
		if (null == result) {
			return adminArea;
		}
		adminArea = result.adminArea;
		return adminArea;
	}

}
