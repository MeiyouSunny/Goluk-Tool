package cn.com.tiros.api;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Gps {
	
	public static final int GPS_FACTOR = 3600000;

	private LocationManager mLocationManager = null;
	
	private boolean mIsBusy;
	private static boolean mIsBest = false;
	
	private MyThread mThread = null;
	
	private final long UPDATE_INTERVAL = 1000;
	private final long UPDATE_DISTANCE = 0;
	
	public void sys_gpsstart() {
		if (mThread == null) {
			mThread = new MyThread();
			mThread.run();
		}
		mIsBusy = true;
	}
	
	public boolean sys_gpsisbusy() {
		return mIsBusy;
	}
	
	public void sys_gpsstop() {
		mIsBusy = false;
		if (mThread != null) {
			mThread.removeListener();
			mThread = null;
		}
	}
	
	public static Criteria getCriteria(boolean isHighAccuracy) {
		Criteria criteria = new Criteria();
		if (isHighAccuracy) {
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
		} else {
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		}
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(true);
		return criteria;
	}

	/**
	 * 
	 * */
	class MyThread implements LocationListener {

		private GPSInfo mGpsInfo = null;
		private Message mMsg = null;

		public void run() {
			if (mIsBest) {
				getBestProvider();
			} else {
				getCommonProvider();
			}
		}

		@Override
		public void onLocationChanged(Location _location) {
			
			if (_location != null) {
				mGpsInfo = new GPSInfo();
				mGpsInfo.lat = _location.getLatitude();
				mGpsInfo.lon = _location.getLongitude();
				mGpsInfo.speed = _location.getSpeed();
				mGpsInfo.direction = _location.getBearing();
				
				if ((mGpsInfo.lat > 1.0) && (mGpsInfo.lon > 1.0) && (mGpsInfo.speed < 100) && (mGpsInfo.direction > -0.5)) {
					
					boolean has = handler.hasMessages(1);
					if (has) {
						handler.removeMessages(1);
					}
					mMsg = handler.obtainMessage(1, mGpsInfo);
					handler.sendMessage(mMsg);
				}
			}
		}

		public void getCommonProvider() {
			if (mLocationManager == null) {
				mLocationManager = (LocationManager) Const.getAppContext().getSystemService(Context.LOCATION_SERVICE);
			}
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
		}

		public void getBestProvider() {
			if (mLocationManager == null) {
				mLocationManager = (LocationManager) Const.getAppContext().getSystemService(Context.LOCATION_SERVICE);
			}
			String t_provider = mLocationManager.getBestProvider(getCriteria(true), true);
			mLocationManager.requestLocationUpdates(t_provider, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
		}

		public void removeListener() {
			try {
				mLocationManager.removeUpdates(this);
			} catch (Exception e) {}
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

	}
	
	class GPSInfo {
		public double lon;
		public double lat;
		public double speed;
		public double direction;
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				GPSInfo info = (GPSInfo) msg.obj;
				sys_gpsChange(info.lon * GPS_FACTOR, info.lat * GPS_FACTOR, info.speed, info.direction);
				info = null;
				break;
			}
			super.handleMessage(msg);
		}

	};
	
	public static native void sys_gpsChange(double lon, double lat, double speed, double course);
}
