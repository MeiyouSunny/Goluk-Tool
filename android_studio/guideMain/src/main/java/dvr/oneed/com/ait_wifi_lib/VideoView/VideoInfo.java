package dvr.oneed.com.ait_wifi_lib.VideoView;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/10 0010.
 */

public class VideoInfo {
    /**
     * Default library loader
     * Load them by yourself, if your libraries are not installed at default place.
     */
    private static final IjkLibLoader sLocalLibLoader = new IjkLibLoader() {
        @Override
        public void loadLibrary(String libName) throws UnsatisfiedLinkError, SecurityException {
            System.loadLibrary(libName);
        }
    };
    private static volatile boolean mIsLibLoaded = false;

    public static void loadLibrariesOnce(IjkLibLoader libLoader) {
        synchronized (VideoInfo.class) {
            if (!mIsLibLoaded) {
                if (libLoader == null)
                    libLoader = sLocalLibLoader;
                libLoader.loadLibrary("videoGpsInfo");
                mIsLibLoaded = true;
            }
        }
    }

    /**
     *
     *
     */
    public VideoInfo() {
        this(sLocalLibLoader);
    }

    /**
     * do not loadLibaray
     *
     * @param libLoader custom library loader, can be null.
     */
    public VideoInfo(IjkLibLoader libLoader) {
        initPlayer(libLoader);
    }

    private void initPlayer(IjkLibLoader libLoader) {
        loadLibrariesOnce(libLoader);
    }

    public interface IjkLibLoader {
        void loadLibrary(String libName) throws UnsatisfiedLinkError,
                SecurityException;
    }

    /**
     * 获取一个gps信息
     * 调用这个函数就可以直接解析
     * 一个视频的jps信息;
     */
    public GpsInfo getOnePointGpsInfo(String path) {
        GpsInfo mInfo = getOnePointVideoInfo(path);
        mInfo.setDwLat(gpsChange(mInfo.dwLat));
        mInfo.setDwLon(gpsChange(mInfo.dwLon));
        return mInfo;
    }

    /**
     * 获取gps数组
     */
    public List<GpsInfo> getAllGpsInfo(String path) {
        List<GpsInfo> mList = new ArrayList<>();
        Integer length = new Integer(60);
        mList = getGpsVideoInfo(path, length);
        return mList;
    }

    public double gpsChange(double org) {
        return (int)(org/100) + (org/100.0 - (int)(org/100)) *100.0 / 60.0;

    }

    /**
     * 获取总的包括sensor+gps的信息
     */
    private native int getAllVideoInfo(String path,
                                       ArrayList<GpsInfo> gpsList,
                                       int gpsLent,
                                       ArrayList<SensorInfo> sensorList,
                                       int sensorLent);

    /**
     * 获取所有的gps信息
     */
    private native ArrayList<GpsInfo> getGpsVideoInfo(String path, int gpsLent);

    /**
     * 获取所有的gps信息
     */
    private native ArrayList<SensorInfo> getSensorVideoInfo(String path, int sensorLent);

    /**
     * 只获取一个视频的一个点
     */
    private native GpsInfo getOnePointVideoInfo(String path);
    /**
     * 只获取一个视频的一个点
     *
     * */
    //private  native int  getImei(String imei );
}
