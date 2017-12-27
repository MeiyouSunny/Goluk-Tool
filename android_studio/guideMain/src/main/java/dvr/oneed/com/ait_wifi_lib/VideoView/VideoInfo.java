package dvr.oneed.com.ait_wifi_lib.VideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/10 0010.
 */

public class VideoInfo {

    static {
        System.loadLibrary("videoGpsInfo");
    }

    public VideoInfo() {
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
        mList = getGpsVideoInfo(path, 60);
        return mList;
    }

    public List<GpsInfo> getAllInfo(String path) {
        ArrayList<GpsInfo> gpsList = new ArrayList<>();
        ArrayList<SensorInfo> sensorList = new ArrayList<>();

        getAllVideoInfo(path, gpsList, 60, sensorList, 60);

        return gpsList;
    }

    public double gpsChange(double org) {
        double newOrg = org;
        newOrg = org / 100;
        return newOrg + (org - newOrg * 100) / 60;
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
