package cn.com.tiros.api;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LLocation implements LocationListener {
	
	private boolean mIsBusy;
	private LocationManager locationManager = null;
	private LLocationInfo mLocationInfo = null;
	
	private final long UPDATE_INTERVAL = 1000;
	private final long UPDATE_DISTANCE = 0;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if(mLocationInfo != null){
					sys_llocationNotify(mLocationInfo.lon * Gps.GPS_FACTOR, mLocationInfo.lat * Gps.GPS_FACTOR, mLocationInfo.accuracy);
				}
				break;
			}
			super.handleMessage(msg);
		}

	};
	
	public void sys_llocationcreate(){
		mLocationInfo = new LLocationInfo();
		locationManager = (LocationManager)Const.getAppContext().getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void sys_llocationdestory(){
		mLocationInfo = null;
		locationManager = null;
	}
	
	public boolean sys_llocationisbusy(){
		return mIsBusy;
	}
	
	public void sys_llocationstart(){
		mIsBusy = true;
		try{
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
		}catch(Exception e){}
	}
	
	public void sys_llocationstop(){
		mIsBusy = false;
		if(locationManager!=null){
			locationManager.removeUpdates(this);
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		
		if (location != null && mLocationInfo != null) {
			mLocationInfo.lon = location.getLongitude();
			mLocationInfo.lat = location.getLatitude();
			mLocationInfo.accuracy = location.getAccuracy();			
			handler.sendEmptyMessage(1);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	public class LLocationInfo {
		public double  lon;         ///< 经度
	    public double  lat;         ///< 纬度
	    public double  accuracy;    ///< 精确度，若获取不到则返回0
	}

	public static native void sys_llocationNotify(double lon, double lat, double accuracy);
}
