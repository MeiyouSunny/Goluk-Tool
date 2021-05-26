package cn.com.tiros.baidu;

import cn.com.tiros.debug.GolukDebugUtils;
import cn.com.tiros.location.PostMessageInterface;
import cn.com.tiros.location.UseGoogle;
import de.greenrobot.event.EventBus;

public class BaiduLocation implements PostMessageInterface {

    public static final double TIANANMEN_LAT = 39.914153;
    public static final double TIANANMEN_LON = 116.403918;

    /**
     * 定位时间间隔
     */
    public static final int LOCATION_TIMER = 3000;
    /**
     * 定位距离间隔
     */
    public static final int LOCATION_DISTANCE = 0;

    public static UseGoogle mGoogle;

    /**
     * 发送定位消息
     */
    public static final int MSG_H_SENDLOCATION = 1;

    /**
     * 是否正在定位中
     */
    private boolean mIsBusy;
    public static boolean mServerFlag;
    private boolean mIsRunning;

    private static BaiduLocation mInstance;

    public BaiduLocation() {
        mInstance = this;
        initBDLocation();
    }

    public static BaiduLocation getInstance() {
        if (mInstance == null)
            mInstance = new BaiduLocation();

        return mInstance;
    }

    public void sys_baiduLocationStart() {
        setLocationParam();
        startLocation();
        mIsBusy = true;
    }

    public boolean sys_baiduLocationIsbusy() {
        return mIsBusy;
    }

    public void sys_baidulocationStop() {
        mIsBusy = false;
        stopLocation();
    }

    private void initBDLocation() {
        GolukDebugUtils.e("", "-------initBDLocation-------------mServerFlag: " + mServerFlag);
        mGoogle = new UseGoogle();
    }

    private void setLocationParam() {
        if (mServerFlag) {
//            mBaidu.setBaiduLocationParam();
        } else {
            // mGoogle.getLocationParam();
        }
    }

    public void startLocation() {
        mIsRunning = true;
        mGoogle.startGoogleLocation();
        mGoogle.setPostGoogleMessage(this);
    }

    public void stopLocation() {
        if (!mIsRunning)
            return;
        mIsRunning = false;
        mGoogle.stopGoogleLocation();
    }

    public void getLastKnowLocation() {
    }

    public static double INVALID = 4.9E-324;

    private final static int LOCATION_FINISH = 1000;
    public final static int LOCATION_POST = 1001;

    public static void postLocationInfo(int locationType, double lat, double lon, float radius, float speed,
                                        float direction, String address, String cityCode) {
        EventBus.getDefault().post(
                new cn.com.mobnote.eventbus.EventLocationFinish(LOCATION_FINISH, locationType, lat, lon, radius, speed,
                        direction, address, cityCode));
    }

    public static void postLocationInfo(String cityCode) {
        EventBus.getDefault().post(new cn.com.mobnote.eventbus.EventLocationFinish(LOCATION_FINISH, cityCode));
    }

    public static void postShortLocationAddress(String shortAddress, double lat, double lon) {
        EventBus.getDefault().post(new cn.com.mobnote.eventbus.EventShortLocationFinish(shortAddress, lat, lon));
    }

    private int mTryCount = 0;
    public final static int LOCATION_TRY_LIMIT = 10;

    public static native void sys_baiduLocationChange(int locationType, double lon, double lat, double speed,
                                                      double direction, double radius);

    @Override
    public void getMessage(BaiduLocationInfo info) {
        if (null != info) {
            sys_baiduLocationChange(info.locationType, info.lon, info.lat, info.speed, info.direction, info.radius);
            info = null;
        }
    }

}
