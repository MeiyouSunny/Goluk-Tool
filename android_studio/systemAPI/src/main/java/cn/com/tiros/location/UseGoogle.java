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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class UseGoogle implements LocationListener {

    private LocationManager mLocationManager;
    private int mTryCount = 0;
    private String mProvider;
    private PostMessageInterface mMessageInterface;
    private GeocoderTask mGeocoderTask;

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

    // Gps 定位超时处理
    private boolean mHasLocated, mGpsTimeOut;
    private static int MSG_TIMEOUT = 100;
    private static final int GPS_TIME_OUT = 15 * 1000;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TIMEOUT) {
                if (!mHasLocated && TextUtils.equals(LocationManager.GPS_PROVIDER, mProvider)) {
                    GolukDebugUtils.e("Location", "===== Gps location timeout =====");
                    mGpsTimeOut = true;
                    mProvider = getProvider();
                    stopGoogleLocation();
                    requestLocation();
                }
            }
        }
    };

    public void startGoogleLocation() {
        if (null != mLocationManager) {
            mTryCount = 0;

            mProvider = getProvider();
            requestLocation();

            mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, GPS_TIME_OUT);
        }
    }

    public void requestLocation() {
        if (TextUtils.isEmpty(mProvider)) {
            GolukDebugUtils.e("Location", "===== No avalible location type! =====");
            return;
        }

        mLocationManager.requestLocationUpdates(mProvider, BaiduLocation.LOCATION_TIMER,
                BaiduLocation.LOCATION_DISTANCE, this);
        GolukDebugUtils.e("Location", "===== Start location =====");
    }

    public void stopGoogleLocation() {
        GolukDebugUtils.e("Location", "===== Stop location =====");
        if (null != mLocationManager) {

            mHandler.removeMessages(MSG_TIMEOUT);

            mHasLocated = false;
            mGpsTimeOut = false;
            mTryCount = 0;
            mLocationManager.removeUpdates(this);
        }
    }

    private String getProvider() {
        List<String> prodivers = mLocationManager.getProviders(true);
        if (prodivers.contains(LocationManager.GPS_PROVIDER) && !mGpsTimeOut) {
            GolukDebugUtils.e("Location", "===== Location type: GPS =====");
            return LocationManager.GPS_PROVIDER;
        } else if (prodivers.contains(LocationManager.NETWORK_PROVIDER)) {
            GolukDebugUtils.e("Location", "===== Location type: Network =====");
            return LocationManager.NETWORK_PROVIDER;
        }

        return "";
    }

    @Override
    public void onLocationChanged(Location location) {
        GolukDebugUtils.e("Location", "===== OnLocationChanged ===== [" + location.getLatitude() + "," + location.getLongitude() + "]");
        mHasLocated = true;
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
//            lat = BaiduLocation.TIANANMEN_LAT;
//            lng = BaiduLocation.TIANANMEN_LON;
            BaiduLocation.postLocationInfo("-1");
            return;
        }

        // 地址查询
        queryAddress(lat, lng, speed, direction, radius);

    }

    @Override
    public void onProviderDisabled(String arg0) {
        GolukDebugUtils.e("Location", "--------------onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String arg0) {
        GolukDebugUtils.e("Location", "--------------onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        GolukDebugUtils.e("Location", "--------------onStatusChanged");
    }

    public static LocationAddressDetailBean getAddress(double lat, double lng) {
        // 根据地理环境来确定编码
        Geocoder gc = new Geocoder(Const.getAppContext(), Locale.getDefault());
        LocationAddressDetailBean addressBean = new LocationAddressDetailBean();
        try {
            //GolukDebugUtils.e("Location", "location--------------getAddress--------lat: " + lat + "  lng: " + lng);
            // 取得地址相关的一些信息、经度、纬度
            List<Address> addressList = gc.getFromLocation(lat, lng, 1);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);

                String countryName = address.getCountryName();
                String subAdminArea = address.getSubAdminArea();
                String locality = address.getLocality();
                String adminArea = address.getAdminArea();
                String addressLine = address.getAddressLine(0);
                String throughFrare = address.getThoroughfare();

                addressBean.countryName = countryName;
                addressBean.cityName = adminArea;
                addressBean.subLocatity = !TextUtils.isEmpty(locality) ? locality : subAdminArea;
                addressBean.throughFrare = throughFrare;
                addressBean.state = 1;
                addressBean.adminArea = adminArea;

                if (null != addressLine && null != countryName && addressLine.equals(countryName)) {
                    addressBean.detail = adminArea + locality + throughFrare;
                } else {
                    addressBean.detail = addressLine;
                }

                GolukDebugUtils.e("Location", "===== Address geted =====");
                GolukDebugUtils.e("Location", "Address: " + addressBean.toString());
            }
            return addressBean;
        } catch (IOException e) {
            addressBean.detail = "";
            addressBean.state = 0;
            GolukDebugUtils.e("Location", "===== getAddress Exception =====");
            return addressBean;
        }
    }

    /**
     * 地址转换
     */
    public void queryAddress(final double lat, final double lng, final float speed, final float direction,
                             final float radius) {
        if (mGeocoderTask != null)
            mGeocoderTask.cancel(true);

        mGeocoderTask = new GeocoderTask(lat, lng, speed, direction, radius);
        mGeocoderTask.execute();
    }

    public void setPostGoogleMessage(PostMessageInterface messageInterface) {
        this.mMessageInterface = messageInterface;
    }

    private void getMessage(BaiduLocationInfo info) {
        if (null != mMessageInterface) {
            mMessageInterface.getMessage(info);
        }
    }

    /**
     * 地址转换Task
     */
    class GeocoderTask extends AsyncTask<Void, Integer, LocationAddressDetailBean> {
        private double lat, lng;
        private float speed, direction, radius;

        public GeocoderTask(double lat, double lng, float speed, float direction, float radius) {
            this.lat = lat;
            this.lng = lng;
            this.speed = speed;
            this.direction = direction;
            this.radius = radius;
            GolukDebugUtils.e("Location", "===== GeocoderTask =====");
        }

        @Override
        protected LocationAddressDetailBean doInBackground(Void... voids) {
            GolukDebugUtils.e("Location", "===== GeocoderTask: doInBackground =====");
            return getAddress(lat, lng);
        }

        @Override
        protected void onPostExecute(LocationAddressDetailBean result) {
            if (result == null || TextUtils.isEmpty(result.cityName)) {
                return;
            }

            GolukDebugUtils.e("Location", "===== Address geted [lat:" + lat + ",lon:" + lng + ",address:" + result + "] =====");

            BaiduLocationInfo locationInfo = new BaiduLocationInfo();
            locationInfo.lat = lat;
            locationInfo.lon = lng;
            locationInfo.speed = speed;
            locationInfo.direction = direction;
            locationInfo.radius = radius;

            BaiduLocation.postLocationInfo(0, lat, lng, radius, speed, direction, result.detail, "-1");
            BaiduLocation.postShortLocationAddress(result.subLocatity + "·" + result.cityName, lat, lng);

            getMessage(locationInfo);
        }
    }

}
