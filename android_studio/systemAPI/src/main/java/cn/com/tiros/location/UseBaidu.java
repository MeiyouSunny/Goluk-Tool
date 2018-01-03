package cn.com.tiros.location;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import cn.com.tiros.api.Const;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.baidu.BaiduLocationInfo;

public class UseBaidu implements BDLocationListener {

    private LocationClient mLocClient = null;
    private int mTryCount = 0;
    private PostMessageInterface mMessageInterface = null;

    public UseBaidu() {
        super();
        mLocClient = new LocationClient(Const.getAppContext());
        mLocClient.registerLocationListener(this);
    }

    public void setBaiduLocationParam() {
        // 支持热配置　
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationMode.Hight_Accuracy); // 设置定位模式
        option.setOpenGps(true); // 打开GPS
        option.setCoorType("bd09ll");// 返回百度经纬度
        option.setScanSpan(1500); // 反回定位间隔
        option.setIsNeedAddress(true);
        if (null != mLocClient) {
            mLocClient.setLocOption(option);
        }
    }

    public void startBaiduLocation() {
        if (null != mLocClient) {
            mTryCount = 0;
            mLocClient.start();
            mLocClient.requestLocation();
        }
    }

    public void stopBaiduLocation() {
        if (null != mLocClient) {
            mTryCount = 0;
            mLocClient.stop();
        }
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        onLocated(location);
    }

    public void getLastKnownLocation() {
        mLocClient.start();
        BDLocation location = mLocClient.getLastKnownLocation();
        onLocated(location);
        mLocClient.stop();
    }

    private void onLocated(BDLocation location) {
        mTryCount++;
        if (null == location) {
            if (mTryCount > BaiduLocation.LOCATION_TRY_LIMIT) {
                BaiduLocation.postLocationInfo("-1");
            }
            return;
        }

        final int locationType = location.getLocType();
        double lat = location.getLatitude(); // 获取维度
        double lon = location.getLongitude(); // 获取经度
        final float radius = location.getRadius();// 获取定位精度半径，单位是米
        final float speed = location.getSpeed(); // 获取速度
        final float direction = location.getDirection();
        final String cityCode = location.getCityCode();
        final String city = location.getCity();
        final String province = location.getProvince();
        final String street = location.getStreet();
        final String district = location.getDistrict();

        if (lat == BaiduLocation.INVALID || lon == BaiduLocation.INVALID) {
            lat = BaiduLocation.TIANANMEN_LAT;
            lon = BaiduLocation.TIANANMEN_LON;
        }

        BaiduLocationInfo locationInfo = new BaiduLocationInfo();
        locationInfo.locationType = locationType;
        locationInfo.lat = lat;
        locationInfo.lon = lon;
        locationInfo.radius = radius;
        locationInfo.speed = speed;
        locationInfo.direction = direction;

        if (null != cityCode) {
            BaiduLocation.postLocationInfo(locationType, lat, lon, radius, speed, direction, province + city + district
                    + street, cityCode);
            BaiduLocation.postShortLocationAddress(city + "·" + district, lat, lon);
        } else {
            if (mTryCount > BaiduLocation.LOCATION_TRY_LIMIT) {
                BaiduLocation.postLocationInfo("-1");
            }
        }

        getMessage(locationInfo);
    }

    public void setPostBaiduMessage(PostMessageInterface messageInterface) {
        this.mMessageInterface = messageInterface;
    }

    private void getMessage(BaiduLocationInfo info) {
        if (null != mMessageInterface) {
            mMessageInterface.getMessage(info);
        }
    }

}
