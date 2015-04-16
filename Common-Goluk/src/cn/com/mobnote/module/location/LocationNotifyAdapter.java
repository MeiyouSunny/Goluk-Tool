package cn.com.mobnote.module.location;

public class LocationNotifyAdapter {

	private static ILocationFn mFn = null;

	public static void setLocationNotifyListener(ILocationFn fn) {
		mFn = fn;
	}

	public static void LocationCallBack(String gpsJson) {
		if (null != mFn) {
			mFn.LocationCallBack(gpsJson);
		}
	}
}
