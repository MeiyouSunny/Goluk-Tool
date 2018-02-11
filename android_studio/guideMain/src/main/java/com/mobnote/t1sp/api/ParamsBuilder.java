package com.mobnote.t1sp.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 小白请求参数构建
 */
public class ParamsBuilder {

    //======= Key =======
    // 通用的请求通常只包括三个参数: action、property、value
    /* 操作类型 */
    public static final String KEY_ACTION = "action";
    /* 操作属性 */
    public static final String KEY_PROPERTY = "property";
    /* 具体值 */
    public static final String KEY_VALUE = "value";
    /* 格式 */
    public static final String KEY_FORMAT = "format";
    /* 开始索引 */
    public static final String KEY_FROM = "from";
    /* 数量 */
    public static final String KEY_COUNT = "count";
    /* 向后 */
    public static final String KEY_BACKWARD = "backward";

    //====== Action ======
    // 常用的Action值
    public static final String ACTION_GET = "get";
    public static final String ACTION_SET = "set";
    public static final String ACTION_SET_CAMERA_ID = "setcamid";
    public static final String ACTION_DEL = "del";
    public static final String ACTION_DIR = "dir";
    public static final String ACTION_FLASH = "flash";

    //====== Value ======
    //=== 通用 ===
    /* 开启 */
    public static final String VALUE_ON = "ON";
    /* 关闭 */
    public static final String VALUE_OFF = "OFF";
    /* 进入 */
    public static final String VALUE_ENTER = "enter";
    /* 退出 */
    public static final String VALUE_EXIT = "exit";
    //=== 文件类型 ===
    public static final String FILE_TYPE_ALL = "all";
    public static final String FILE_TYPE_MOV = "mov";
    public static final String FILE_TYPE_AVI = "avi";
    public static final String FILE_TYPE_JPEG = "jpeg";
    //=== 文件目录类型 ===
    public static final String FILE_DIR_TYPE_DCIM = "DCIM";
    public static final String FILE_DIR_TYPE_NORMAL = "Normal";
    public static final String FILE_DIR_TYPE_EVENT = "Event";
    public static final String FILE_DIR_TYPE_PARK = "Park";
    public static final String FILE_DIR_TYPE_SHARE = "Share";
    //=== 前后摄像头切换 ===
    /* 前置摄像头 */
    public static final String VALUE_CAMERA_FRONT = "front";
    /* 后置摄像头 */
    public static final String VALUE_CAMERA_REAR = "rear";
    //=== 抓拍照片|视频 ===
    /* 照片 */
    public static final String VALUE_CAPTURE_PIC = "capture";
    /* 短视频 */
    public static final String VALUE_CAPTURE_SHORT_VIDEO = "rec_short";
    // === 停车保护模式 ===
    /* 关闭 */
    public static final String VALUE_PARKING_GUARD_OFF = VALUE_OFF;
    /* 低 */
    public static final String VALUE_PARKING_GUARD_LOW = "LOW";
    /* 中 */
    public static final String VALUE_PARKING_GUARD_MIDDLE = "MIDDLE";
    /* 高 */
    public static final String VALUE_HIGH = "HIGH";
    // === 碰撞灵敏度 ===
    /* 关闭 */
    public static final String VALUE_GSENSOR_OFF = VALUE_OFF;
    /* 低 */
    public static final String VALUE_GSENSORLEVEL0 = "LEVEL0";
    /* 中 */
    public static final String VALUE_GSENSOR_LEVEL2 = "LEVEL2";
    /* 高 */
    public static final String VALUE_GSENSOR_LEVEL4 = "LEVEL4";
    // === 设置录制视频分辨率 ===
    /* 1080 */
    public static final String VALUE_VIDEO_CLARITY_1080 = "1080P30fps";
    /* 720 */
    public static final String VALUE_VIDEO_CLARITY_720 = "720P30fps";
    // === 设置关机时间(熄火录影)
    /* 10s */
    public static final String VALUE_POWER_OFF_DELAY_10_SEC = "10SEC";
    /* 60s */
    public static final String VALUE_POWER_OFF_DELAY_60_SEC = "60SEC";
    // === 设置默认抓拍时长
    /* 抓拍时长12s */
    public static final String VALUE_SNAP_TIME_10_SEC = "12S";
    /* 抓拍时长30s */
    public static final String VALUE_SNAP_TIME_60_SEC = "30S";

    /**
     * 构建通用的请求参数
     *
     * @param values 三个值: action, property, value
     * @return 通用请求参数
     */
    public static Map<String, String> commonParam(String... values) {
        if (values == null || values.length != 3)
            return null;
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, values[0]);
        params.put(KEY_PROPERTY, values[1]);
        params.put(KEY_VALUE, values[2]);

        return params;
    }

    /**
     * 获取记录仪文件列表
     *
     * @param values property, format, from, count
     * @return
     */
    public static Map<String, String> getFileListParam(String... values) {
        if (values == null || values.length != 3)
            return null;
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_DIR);
        params.put(KEY_PROPERTY, values[0]);
        params.put(KEY_FORMAT, "MP4");
        params.put(KEY_FROM, values[1]);
        params.put(KEY_COUNT, values[2]);
        params.put(KEY_BACKWARD, "");

        return params;
    }

    /**
     * 前后摄像头切换
     *
     * @param frontOrBack 前/后
     * @return
     */
    public static Map<String, String> switchCameraParam(boolean frontOrBack) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET_CAMERA_ID);
        params.put(KEY_PROPERTY, "Camera.Preview.Source.1.Camid");
        if (frontOrBack)
            params.put(KEY_VALUE, VALUE_CAMERA_FRONT);
        else
            params.put(KEY_VALUE, VALUE_CAMERA_REAR);

        return params;
    }

    /**
     * 抓拍照片|视频
     *
     * @param picOrVideo 照片/视频
     * @return
     */
    public static Map<String, String> captureParam(boolean picOrVideo) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Video");
        if (picOrVideo)
            params.put(KEY_VALUE, VALUE_CAPTURE_PIC);
        else
            params.put(KEY_VALUE, VALUE_CAPTURE_SHORT_VIDEO);

        return params;
    }

    /**
     * 获取当前视频码流类型
     *
     * @return
     */
    public static Map<String, String> getVideoCodeTypeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "Camera.Preview.RTSP.av");

        return params;
    }

    /**
     * 设置记录仪时间
     *
     * @param value 时间,格式: yyyy$MM$dd$HH$mm$ss
     * @return
     */
    public static Map<String, String> setTimeParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "TimeSettings");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 格式化SDCard
     *
     * @return
     */
    public static Map<String, String> formatSdCardParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "SD0");
        params.put(KEY_VALUE, "format");

        return params;
    }

    /**
     * 进入|退出回放模式
     *
     * @param enterOrExit 进入|退出
     * @return
     */
    public static Map<String, String> enterPlaybackModeParam(boolean enterOrExit) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Playback");
        if (enterOrExit)
            params.put(KEY_VALUE, VALUE_ENTER);
        else
            params.put(KEY_VALUE, VALUE_EXIT);

        return params;
    }

    /**
     * 进入回放模式发送心跳
     *
     * @return
     */
    public static Map<String, String> sendHeartbeatPlaybackModeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Playback");
        params.put(KEY_VALUE, "heartbeat");

        return params;
    }

    /**
     * 切换到录像模式
     *
     * @return
     */
    public static Map<String, String> enterVideoModeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "UIMode");
        params.put(KEY_VALUE, "VIDEO");

        return params;
    }

    /**
     * 获取记录仪版本信息
     *
     * @return
     */
    public static Map<String, String> getDeviceInfoParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "Camera.Menu.DeviceID");

        return params;
    }

    /**
     * 设置WIFI名称
     *
     * @param value WIFI名称
     * @return
     */
    public static Map<String, String> setWifiNameParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Net.WIFI_AP.SSID");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 设置WIFI密码
     *
     * @param value WIFI密码
     * @return
     */
    public static Map<String, String> setWifiPwdParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Net.WIFI_AP.CryptoKey");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 设置停车保护
     *
     * @param value [OFF|LOW|MIDDLE|HIGH]
     * @return
     */
    public static Map<String, String> setParkingGuardParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "ParkingGuard");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 设置停车保护
     *
     * @param value [OFF|LEVEL0|LEVEL2|LEVEL4]
     * @return
     */
    public static Map<String, String> setGSensorParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "GSensor");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 设置录制视频分辨率
     *
     * @param value [1080P30fps|720P30fps]
     * @return
     */
    public static Map<String, String> setVideoClarityParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Videores");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 设置录制音频开关
     *
     * @param onOrOff [OFF|ON]
     * @return
     */
    public static Map<String, String> setRecordSoundParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "SoundRecord");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 获取录制音频开关信息
     *
     * @return
     */
    public static Map<String, String> getRecordSoundParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "SoundRecord");

        return params;
    }

    /**
     * 进入|退出设置模式
     *
     * @param enterOrExit [enter|exit]
     * @return
     */
    public static Map<String, String> enterOrExitSettingModeParam(boolean enterOrExit) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Setting");
        if (enterOrExit)
            params.put(KEY_VALUE, VALUE_ENTER);
        else
            params.put(KEY_VALUE, VALUE_EXIT);

        return params;
    }

    /**
     * 进入设置模式心跳
     *
     * @return
     */
    public static Map<String, String> sendHeartbeatSettingModeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Setting");
        params.put(KEY_VALUE, "heartbeat");

        return params;
    }

    /**
     * 获取默认设置参数
     *
     * @return
     */
    public static Map<String, String> getSettingInfoParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "Camera.Menu.*");

        return params;
    }

    /**
     * 进入升级模式
     *
     * @return
     */
    public static Map<String, String> enterUpdaetModeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "UIMode");
        params.put(KEY_VALUE, "UPDATEFW");

        return params;
    }

    /**
     * 开始设计固件
     *
     * @return
     */
    public static Map<String, String> updateFWParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_FLASH);

        return params;
    }

    /**
     * 删除记录仪文件
     *
     * @param value filename
     * @return
     */
    public static Map<String, String> deleteFileParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_DEL);
        params.put(KEY_PROPERTY, value);

        return params;
    }

    /**
     * 获取当前记录仪模式
     *
     * @return
     */
    public static Map<String, String> getCameraModeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "Camera.Preview.MJPEG.status.*");

        return params;
    }

    /**
     * 获取当前SDCard容量
     *
     * @return
     */
    public static Map<String, String> getSdCardInfoParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "Camera.Menu.SDInfo");

        return params;
    }

    /**
     * 设置抓拍视频声音开关
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setCaptureSoundParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "SnapSound");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 设置紧急视频声音开关
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setEmgVideoSoundParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "LockSound");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 设置休眠模式开关
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setSleepModeParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "SleepMode");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 设置安防模式开关
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setPKModeParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "PKMode");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 获取抓拍视频声音开关信息
     *
     * @return
     */
    public static Map<String, String> getCaptureSoundParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "SnapSound");

        return params;
    }

    /**
     * 设置视频水印开关
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setRecStampParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "RecStamp");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 获取视频水印开关
     *
     * @return
     */
    public static Map<String, String> getRecStampParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "RecStamp");

        return params;
    }

    /**
     * 设置移动侦测属性
     *
     * @param value [OFF|LOW|MIDDLE|HIGH]
     * @return
     */
    public static Map<String, String> setMTDParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "MTD");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 获取移动侦测属性
     *
     * @return
     */
    public static Map<String, String> getMTDParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "MTD");

        return params;
    }

    /**
     * 设置关机时间(熄火录影)
     *
     * @param value [10SEC|60SEC]
     * @return
     */
    public static Map<String, String> setPowerOffDelayParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "PowerOffDelay");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 恢复出厂设置
     *
     * @return
     */
    public static Map<String, String> resetFactoryParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "FactoryReset");
        params.put(KEY_VALUE, "FactoryReset");

        return params;
    }

    /**
     * 设置镜头自动翻转
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setAutoRotateParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "AutoRotate");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 获取镜头自动翻转信息
     *
     * @return
     */
    public static Map<String, String> getAutoRotateParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "AutoRotate");

        return params;
    }

    /**
     * 设置默认抓拍时长
     *
     * @param value [12S|30S]
     * @return
     */
    public static Map<String, String> setCaptureTimeParam(String value) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "SnapTime");
        params.put(KEY_VALUE, value);

        return params;
    }

    /**
     * 获取默认抓拍时长
     *
     * @return
     */
    public static Map<String, String> getCaptureTimeParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "SnapTime");

        return params;
    }

    /**
     * 设置默认开机声音
     *
     * @param onOrOff [ON|OFF]
     * @return
     */
    public static Map<String, String> setPowerSoundParam(boolean onOrOff) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "PWRSound");
        if (onOrOff)
            params.put(KEY_VALUE, VALUE_ON);
        else
            params.put(KEY_VALUE, VALUE_OFF);

        return params;
    }

    /**
     * 获取默认开机声音
     *
     * @return
     */
    public static Map<String, String> getPowerSoundParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_GET);
        params.put(KEY_PROPERTY, "PWRSound");

        return params;
    }

    /**
     * 打开循环录像
     */
    public static Map<String, String> openLoopRecordParam() {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_ACTION, ACTION_SET);
        params.put(KEY_PROPERTY, "Video");
        params.put(KEY_VALUE, "record");

        return params;
    }

}
