package com.mobnote.golukmain.carrecorder;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.adas.AdasConfigParamterBean;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.settings.VideoQualityActivity;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.reportlog.ReportLog;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * IPC控制管理
 * <p/>
 * 2015年3月21日
 *
 * @author xuhw
 */
public class IPCControlManager implements IPCManagerFn {

    public static final String G1_SIGN = "G1";
    public static final String G2_SIGN = "G2";
    //开发的适合叫G1S ，实际就是G2，解决BUG https://bugly.qq.com/v2/crash/apps/900012751/issues/3675?pid=1
    public static final String G1S_SIGN = "G1S";
    public static final String T1_SIGN = "T1";
    public static final String T1U_SIGN = "T1U";
    public static final String T1s_SIGN = "T1S";
    public static final String T2_SIGN = "T2";
    public static final String T2U_SIGN = "T2U";
    public static final String T3_SIGN = "T3";
    public static final String T3U_SIGN = "T3U";
    public static final String T2S_SIGN = "T2S";

    public static final String MODEL_T = "T";
    public static final String MODEL_G = "G";

    /**
     * IPC回调监听列表
     */
    private HashMap<String, IPCManagerFn> mIpcManagerListener = null;
    /**
     * Application实例,用于调用JNI的对象
     */
    private GolukApplication mApplication = null;
    /**
     * IPC设备型号
     **/
    public String mProduceName = "";
    /**
     * 当前设备的Sn号
     */
    public String mDeviceSn = null;
    /**
     * 是否需要上报绑定后的信息
     */
    public boolean isNeedReportSn = false;

    public IPCControlManager(GolukApplication application) {
        mApplication = application;
        mIpcManagerListener = new HashMap<>();
        mProduceName = SharedPrefUtil.getIpcModel();
        if ("".equals(mProduceName)) {
            setProduceName(G1_SIGN);
        }
        isNeedReportSn = false;
        // 注册IPC回调
        mApplication.mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_IPCManager, this);

        // 设置连接模式
        setIpcMode();
    }

    public void setProduceName(String name) {
        if (G1S_SIGN.equals(name)) {
            mProduceName = G2_SIGN;
            return;
        }
        mProduceName = name;
    }

    public void setIpcMode() {
        if (G1_SIGN.equals(mProduceName) || G2_SIGN.equals(mProduceName) || T3_SIGN.equals(mProduceName)|| T3U_SIGN.equals(mProduceName)) {
            setIpcMode(IPCMgrMode_IPCDirect);
        } else if (T1_SIGN.equals(mProduceName) || T2_SIGN.equals(mProduceName)) {
            setIpcMode(IPCMgrMode_T1);
        } else {
            // 不处理
        }
    }

    // 判断是否是T1设备
    public boolean isT1Relative() {
        return T1_SIGN.equals(mProduceName) || T2_SIGN.equals(mProduceName);
    }


    // 判断是否是T1设备
    public boolean isT1RelativeWifiName() {
        return T1_SIGN.equals(mProduceName) || T2_SIGN.equals(mProduceName) || T3_SIGN.equals(mProduceName) ||
                T1U_SIGN.equals(mProduceName) || T2U_SIGN.equals(mProduceName) || T3U_SIGN.equals(mProduceName) ||
                T1s_SIGN.equals(mProduceName);
    }

    /**
     * 是否T1/T2
     */
    public boolean isT1OrT2() {
        return T1_SIGN.equals(mProduceName) || T1U_SIGN.equals(mProduceName)
                || T2_SIGN.equals(mProduceName) || T2U_SIGN.equals(mProduceName);
    }

    /**
     * 是否是T2S设备
     */
    public boolean isT2S() {
        return !TextUtils.isEmpty(mProduceName) &&
                (TextUtils.equals(mProduceName, "T2S")
                        || TextUtils.equals(mProduceName, "T2SU")
                        || TextUtils.equals(mProduceName, "F30")
                        || TextUtils.equals(mProduceName, "F20")
                        || TextUtils.equals(mProduceName, "T4")
                        || TextUtils.equals(mProduceName, "T4U")
                        || TextUtils.equals(mProduceName, "T3Pro")
                        || TextUtils.equals(mProduceName, "F5"));
    }

    // 是否是新的T1S设备
    public boolean isT1S() {
        return !TextUtils.isEmpty(mProduceName) &&
                (TextUtils.equals(mProduceName, "T3Pro") || TextUtils.equals(mProduceName, "F20"));
    }

    /**
     * 判断是否是G1与T1S，两个处理流程是一样的
     *
     * @return
     * @author jyf
     */
    public boolean isG1Relative() {
        boolean isG1 = IPCControlManager.G1_SIGN.equals(mProduceName);
        boolean isT1S = IPCControlManager.T1s_SIGN.equals(mProduceName);
        return isG1 || isT1S;
    }

    public boolean isT3Relative() {
        return IPCControlManager.T3_SIGN.equals(mProduceName)
                || IPCControlManager.T3U_SIGN.equals(mProduceName);
    }

    /**
     * 直接设置模式
     *
     * @param mode MODEL_T / MODEL_G
     * @author jyf
     */
    public void setIpcMode(String mode) {

        if (MODEL_T.equals(mode)) {
            setIpcMode(IPCMgrMode_T1);
        } else {
            setIpcMode(IPCMgrMode_IPCDirect);
        }
    }

    /**
     * 设置IPC 连接模式
     *
     * @param mode 0/1/2
     * @author jyf
     */
    private void setIpcMode(int mode) {
        if (mode < 0
                || null == mApplication
                || null == mApplication.mGoluk) {
            return;
        }
        String json = JsonUtil.getIPCConnModeJson(mode);
        mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_SetMode, json);
    }

    /**
     * 绑定成功后需要把设备号上报给服务端，统计设备首次激活的时间
     *
     * @author jyf
     */
    public void reportBindMsg() {
        if (null == mDeviceSn || !isNeedReportSn) {
            return;
        }
        final ReportLogManager loadManager = ReportLogManager.getInstance();

        loadManager.getReport(IMessageReportFn.KEY_ACTIVATION_TIME).addLogData(
                JsonUtil.getActivationTimeJson(mDeviceSn));
        // 设置绑定成功
        loadManager.getReport(IMessageReportFn.KEY_ACTIVATION_TIME).setType(ReportLog.TYPE_SUCESS);
        if (null != mProduceName) {
            loadManager.getReport(IMessageReportFn.KEY_ACTIVATION_TIME).setHdType(mProduceName);
        }
        // 开始上报
        final String jsonData = loadManager.getReport(IMessageReportFn.KEY_ACTIVATION_TIME).getReportData();
        mApplication.uploadMsg(jsonData, false);
        // 还原上报状态
        loadManager.removeKey(IMessageReportFn.KEY_ACTIVATION_TIME);
        isNeedReportSn = false;
    }

    /**
     * 告知IPC wifi连接状态
     *
     * @param isConnect ture:连接　false:未连接
     * @param ip        ipc热点ip地址
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean setIPCWifiState(boolean isConnect, String ip) {
        GolukDebugUtils.e("xuhw", "YYYYYYY==============ip=" + ip);
        SettingUtils.getInstance().putString("IPC_IP", ip);
        int state = isConnect ? 1 : 0;
        String json = JsonUtil.getWifiChangeJson(state, ip);
        if (null == mApplication || null == mApplication.mGoluk) {
            return false;
        }
        boolean isSucess = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_CommCmd_WifiChanged, json);
        return isSucess;
    }

    /**
     * 设置VDCP断开连接
     *
     * @author jyf
     */
    public void setVdcpDisconnect() {
        setIPCWifiState(false, "");
    }

    /**
     * 通知ipc连接手机热点
     *
     * @param json
     * @return
     */
    public boolean setIpcLinkPhoneHot(String json) {
        boolean isSucess = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetWifiCfg, json);
        return isSucess;
    }

    /**
     * 视频截图
     *
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean screenShot() {
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                    IPCManagerFn.IPC_VDCPCmd_SnapPic, "");
        } else {
            GFileUtils.writeShootLog("========ipc截图命令失败　未登录========");
        }

        return false;
    }

    /**
     * 发起精彩视频截取功能
     *
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean startWonderfulVideo() {
        String queryParam = IpcDataParser.getTriggerRecordJson(TYPE_SHORTCUT, 6, 6);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_TriggerRecord, queryParam);
    }

    /**
     * 发起紧急视频截取功能
     *
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean startEmergencyVideo() {
        String queryParam = IpcDataParser.getTriggerRecordJson(TYPE_URGENT, 8, 8);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_TriggerRecord, queryParam);
    }

    /**
     * 文件查询
     *
     * @param filename 要查询的文件名
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean querySingleFile(String filename) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SingleQuery,
                filename);
    }

    /**
     * 查询文件列表信息
     *
     * @param filetype   1:循环影像 2:紧急视频 4:精彩视频
     * @param limitCount 最多查询条数
     * @param timestart  查询起始时间（0表示查询所有）
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean queryFileListInfo(int filetype, int limitCount, long timestart, long timeend, String resform) {
        String queryParam = IpcDataParser.getQueryMoreFileJson(filetype, limitCount, timestart, timeend, resform);
        GolukDebugUtils.e("queryFileList", "queryParam= " + queryParam);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Query,
                queryParam);
    }

    /**
     * 设置IPC系统时间
     *
     * @param time 距离1970年1月1日0时0分0秒所经过的秒数
     * @author xuhw
     * @date 2015年4月3日
     */
    public boolean setIPCSystemTime(long time) {
        String zone = "";
        String json = "";
        int hourOffset = 0, minOffset = 0;
        if (isT1Relative() || T3_SIGN.equals(mProduceName) || T3U_SIGN.equals(mProduceName)) {
            TimeZone tz = TimeZone.getDefault();
            zone = tz.getID();
            Calendar cal = GregorianCalendar.getInstance(tz);
            int seconds = tz.getOffset(cal.getTimeInMillis()) / 1000;
            hourOffset = seconds / 60 / 60;
            minOffset = Math.abs(seconds / 60 % 60);
        } else {
            zone = "";
        }
        if (T3_SIGN.equals(mProduceName) || T3U_SIGN.equals(mProduceName)) {
            json = JsonUtil.getTimeAndZoneJson(time, zone, hourOffset, minOffset);
        } else {
            json = JsonUtil.getTimeJson(time, zone);
        }
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetTime, json);
    }

    /**
     * 下载文件
     *
     * @param filename 文件名称
     * @param tag      唯一标识
     * @param savepath 文件保存路径
     * @author xuhw
     * @date 2015年3月25日
     */
    public boolean downloadFile(String filename, String tag, String savepath, long filetime) {
        String json = JsonUtil.getDownFileJson(filename, tag, savepath, filetime);
        if (filename.contains(".mp4")) {
            GFileUtils.writeIPCLog("==downloadFile==json=" + json);
            GolukDebugUtils.e("xuhw", "YYYYYY====downloadFile=====json=" + json);
        }
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDTPCmd_AddDownloadFile, json);
    }

    /**
     * 停止IPC下载所有任务
     *
     * @return
     * @author xuhw
     * @date 2015年5月19日
     */
    public boolean stopDownloadFile() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDTPCmd_StopDownloadFile, "");
    }

    /**
     * 删除文件
     *
     * @param filename 文件名称
     * @author xuhw
     * @date 2015年3月25日
     */
    public boolean deleteFile(String filename) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Erase,
                filename);
    }

    /**
     * 更新经纬度信息
     *
     * @param lon       经度
     * @param lat       纬度
     * @param speed     速度
     * @param direction 方向
     * @author xuhw
     * @date 2015年3月31日
     */
    public boolean updateGPS(long lon, long lat, int speed, int direction) {
        String json = JsonUtil.getGPSJson(lon, lat, speed, direction);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_SetGpsInfo,
                json);
    }

    /**
     * 查询设备状态
     *
     * @author xuhw
     * @date 2015年4月2日
     */
    public boolean queryDeviceStatus() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_DeviceStatus,
                "");
    }

    /**
     * 查询录制存储状态
     *
     * @author xuhw
     * @date 2015年4月2日
     */
    public boolean queryRecordStorageStatus() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecPicUsage,
                "");
    }

    /**
     * 获取IPC系统标识
     *
     * @author xuhw
     * @date 2015年4月3日
     */
    public boolean getIPCIdentity() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetIdentity,
                "");
    }

    /**
     * IPC重启
     *
     * @author xuhw
     * @date 2015年4月3日
     */
    public boolean rebootIPC() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Reboot, "");
    }

    /**
     * IPC恢复出厂设置
     *
     * @author xuhw
     * @date 2015年4月3日
     */
    public boolean restoreIPC() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Restore, "");
    }

    /**
     * IPC格式化SD卡
     *
     * @author xuhw
     * @date 2015年4月3日
     */
    public boolean formatDisk() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_FormatDisk,
                "");
    }

    /**
     * 获取IPC系统时间
     *
     * @return
     * @author xuhw
     * @date 2015年4月13日
     */
    public boolean getIPCSystemTime() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetTime, "");
    }

    public boolean getTimeSyncCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCP_Msg_GetTimeSyncCfg, "");
    }

    public boolean setTimeSyncCfg(int state) {
        String json = JsonUtil.getGpsTimeJson(state);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCP_Msg_SetTimeSyncCfg, json);
    }

    /**
     * 设置IPC系统WIFI配置
     *
     * @author xuhw
     * @date 2015年4月3日
     */
    public boolean setIPCWifiCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetWifiCfg,
                "");
    }

    /**
     * 获取IPC系统音视频编码配置
     *
     * @return
     * @author xuhw
     * @date 2015年4月7日
     */
    public boolean getVideoEncodeCfg(int type) {
        String json = JsonUtil.getVideoCfgJson(type);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetVideoEncodeCfg, json);
    }

    /**
     * 设置IPC系统音视频编码配置
     *
     * @return
     * @author xuhw
     * @date 2015年4月7日
     */
    public boolean setVideoEncodeCfg(VideoQualityActivity.SensitivityType type) {
        String json = JsonUtil.getVideoConfig(type);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetVideoEncodeCfg, json);
    }

    /**
     * 设置IPC系统音视频编码配置
     *
     * @param mVideoConfigState
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean setVideoEncodeCfg(VideoConfigState mVideoConfigState) {
        String json = JsonUtil.getVideoConfig(mVideoConfigState);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetVideoEncodeCfg, json);
    }

    /**
     * 设置音频开关
     *
     * @param mVideoConfigState
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean setAudioCfg(VideoConfigState mVideoConfigState) {
        String json = JsonUtil.getVideoConfig(mVideoConfigState);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetVideoEncodeCfg, json);
    }

    public boolean setAudioCfg_T1(int state) {
        String json = JsonUtil.getVideoConfigJson_T1(state);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetRecAudioCfg, json);
    }

    public boolean getAudioCfg_T1() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetRecAudioCfg, "");
    }

    /**
     * 设置IPC行车影像开始录制
     *
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean startRecord() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_StartRecord,
                "");
    }

    /**
     * 设置IPC行车影像停止录制
     *
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean stopRecord() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_StopRecord,
                "");
    }

    /**
     * 获取IPC行车影像录制状态
     *
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean getRecordState() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetRecordState, "");
    }

    /**
     * 获取IPC水印显示状态
     *
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean getWatermarkShowState() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetImprintShow, "");
    }

    /**
     * 设置IPC水印显示状态
     *
     * @return
     * @author xuhw
     * @date 2015年4月8日
     */
    public boolean setWatermarkShowState() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetImprintShow, "");
    }

    /**
     * 获取IPC GSensor控制紧急录像策略
     *
     * @return
     * @author xuhw
     * @date 2015年4月9日
     */
    public boolean getGSensorControlCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetGSensorControlCfg, "");
    }

    /**
     * 设置IPC GSensor控制紧急录像策略
     *
     * @param policy
     * @return
     * @author xuhw
     * @date 2015年4月22日
     */
    public boolean setGSensorControlCfg(int policy) {
        JSONObject json = new JSONObject();
        try {
            json.put("policy", policy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetGSensorControlCfg, json.toString());
    }

    /**
     * IPC设备进行升级
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean ipcUpgrade(String binPath) {
        GolukDebugUtils.i("lily", "---------ipcUpgrade------" + IPC_VDCPCmd_IPCUpgrade);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_IPCUpgrade,
                binPath);
    }

    /**
     * 读取安防模式和移动侦测参数
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean getMotionCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetMotionCfg,
                "");
    }

    /**
     * 获取紧急视频提示音
     */
    public boolean getEmgVideoSoundCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCP_Msg_GetUrgentVoiceConf,
                "");
    }

    /**
     * 设置紧急视频提示音
     */
    public boolean setEmgVideoSoundCfg(int state) {
        String json = JsonUtil.getEmgVideoSoundConfigJson_T1(state);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetUrgentVoiceConf,
                json);
    }

    /**
     * 获取缩时视频录制开关状态
     */
    public boolean getTimelapseCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetTimelapseConf,
                "");
    }

    /**
     * 设置缩时视频录制开关
     */
    public boolean setTimelapseCfg(int state) {
        String json = JsonUtil.getTimelapseConfigJson_T1(state);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetTimelapseConf,
                json);
    }

    /**
     * 获取紧急碰撞灵敏配置
     */
    public boolean getGSensorMoreValueCfg() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetCollisionValueConf, "");
    }

    /**
     * 设置紧急碰撞灵敏度配置
     */
    public boolean setGSensorMoreValueCfg(int collisionValue) {
        JSONObject json = new JSONObject();
        try {
            json.put("collisionValue", collisionValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetCollisionValueConf, json.toString());
    }

    /**
     * 获取设备异常日志
     */
    public boolean getExceptionList(int fromId, int count) {
        JSONObject json = new JSONObject();
        try {
            json.put("id", fromId);
            json.put("count", count);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mApplication.mGoluk == null)
            return false;

        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetExceptionList,
                json.toString());
    }

    /**
     * 设置安防模式和移动侦测参数
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean setMotionCfg(int enableSecurity, int snapInterval) {
        JSONObject json = new JSONObject();
        try {
            json.put("enableSecurity", enableSecurity);
            json.put("snapInterval", snapInterval);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetMotionCfg,
                json.toString());
    }

    /**
     * 读取IPC版本信息
     */
    public boolean getVersion() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetVersion,
                "");
    }

    /**
     * wifi直连的之后，需要调用这个函数，设备就不会发出“下载app的语音提示”了
     */
    public boolean setBindStatus() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetBindStatus, "");
    }

    /**
     * SD卡图片查询
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean queryPic() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_QueryPic, "");
    }

    /**
     * SD卡查询单张图片
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean singleQueryPic() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SingleQueryPic, "");
    }

    /**
     * 删除多个SD卡录像、图片
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean recPicEraseMulti() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_RecPicEraseMulti, "");
    }

    /**
     * 按条件删除录像
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean recErase() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecErase, "");
    }

    /**
     * 按条件删除图片
     *
     * @return
     * @author xuhw
     * @date 2015年4月21日
     */
    public boolean ricErase() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_PicErase, "");
    }

    public boolean getIpcWifiConfig() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetWifiCfg,
                "");
    }

    /**
     * 添加IPC管理监听
     *
     * @param from 来自哪里的
     * @param fn   监听接口
     * @author xuhw
     * @date 2015年3月21日
     */
    public void addIPCManagerListener(String from, IPCManagerFn fn) {
        if (mIpcManagerListener.containsKey(from))
            this.mIpcManagerListener.remove(from);
        this.mIpcManagerListener.put(from, fn);
    }

    /**
     * 删除IPC监听
     *
     * @param from 来自那里的
     * @author xuhw
     * @date 2015年3月21日
     */
    public void removeIPCManagerListener(String from) {
        if (mIpcManagerListener.containsKey(from))
            this.mIpcManagerListener.remove(from);
    }

    /**
     * 获取ipc开关机声音状态
     *
     * @return
     */
    public boolean getIPCSwitchState() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetSpeakerSwitch, "");
    }

    /**
     * 设置ipc开关机声音状态
     *
     * @return
     */
    public boolean setIPCSwitchState(String status) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetSpeakerSwitch, status);
    }

    /**
     * 获取isp模式
     *
     * @return
     */
    public boolean getISPMode() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetISPMode, "");
    }

    /**
     * 设置isp模式
     *
     * @return
     */
    public boolean setISPMode(String status) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetISPMode, status);
    }

    /**
     * 获取照片质量
     *
     * @return
     */
    public boolean getPhotoQualityMode() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetPicCfg, "");
    }

    /**
     * 设置照片质量
     *
     * @return
     */
    public boolean setPhotoQualityMode(String status) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetPicCfg, status);
    }

    /**
     * 获取疲劳驾驶、图像自动翻转、停车休眠
     *
     * @return
     */
    public boolean getFunctionMode() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetFunctionSwitch, "");
    }

    /**
     * 设置疲劳驾驶、图像自动翻转、停车休眠
     *
     * @return
     */
    public boolean setFunctionMode(String status) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetFunctionSwitch, status);
    }

    /**
     * 获取遥控器功能
     *
     * @return
     */
    public boolean getKitMode() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetKitCfg, "");
    }

    /**
     * 设置遥控器功能
     *
     * @return
     */
    public boolean setKitMode(String status) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetKitCfg, status);
    }

    /**
     * 获取T1图像自动翻转
     *
     * @return
     */
    public boolean getT1AutoRotaing() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetAutoRotationCfg, "");
    }

    /**
     * 设置T1图像自动翻转
     *
     * @return
     */
    public boolean setT1AutoRotaing(String status) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetAutoRotationCfg, status);
    }


    public boolean setT1SW(String status) {
        String s = "{\"MoveMonitor\":" + status + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetMotionSW, s);
    }

    public boolean getT1SW() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetMotionSW, "");
    }

    public boolean getTxLanguage() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetVoiceTypeList, "");
    }

    /**
     * 获取T1 ADAS配置
     *
     * @return
     */
    public boolean getT1AdasConfig() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetADASConfig, "");
    }

    /**
     * 设置T1 ADAS配置开关
     *
     * @return
     */
    public boolean setT1AdasConfigEnable(int enabled) {
        String s = "{\"enable\":" + enabled + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetADASConfig, s);
    }

    /**
     * 设置T1 ADAS OSD配置开关
     *
     * @return
     */
    public boolean setT1AdasConfigOSD(int osd) {
        String s = "{\"osd\":" + osd + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetADASConfig, s);
    }

    /**
     * 设置T1 ADAS OSD配置开关
     *
     * @return
     */
    public boolean setT1AdasConfigFcs(int fcs_enable) {
        String s = "{\"fcs_enable\":" + fcs_enable + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetADASConfig, s);
    }

    /**
     * 设置T1 ADAS OSD配置开关
     *
     * @return
     */
    public boolean setT1AdasConfigFcw(int fcw_enable) {
        String s = "{\"fcw_enable\":" + fcw_enable + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetADASConfig, s);
    }

    /**
     * 设置T1 ADAS配置所有参数
     *
     * @return
     */
    public boolean setT1AdasConfigAll(AdasConfigParamterBean data) {
        String s = JSON.toJSONString(data);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetADASConfig, s);
    }

    /**
     * 获取精彩视频质量
     *
     * @return
     */
    public boolean getVideoResolution() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetVideoResolution, "");
    }

    public boolean getAntiFlicker() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetDeflickerMode, "");
    }

    public boolean setAntiFlicker(String value) {
        String s = "{\"mode\":" + value + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetDeflickerMode, s);
    }

    /**
     * 设置精彩视频质量
     *
     * @return
     */
    public boolean setVideoResolution(String wonderful_resolution) {
        String s = "{\"wonderful_resolution\":" + "\"" + wonderful_resolution + "\"}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetVideoResolution, s);
    }

    /**
     * 获取提示音音量大小
     *
     * @return
     */
    public boolean getVolume() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetVolume, "");
    }

    /**
     * 设置提示音音量大小
     *
     * @return
     */
    public boolean setVolume(int value) {
        String s = "{\"value\":" + value + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetVolume, s);
    }

    /**
     * 获取关机时间
     *
     * @return
     */
    public boolean getPowerOffTime() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetPowerOffTime, "");
    }

    /**
     * 设置关机时间
     *
     * @return
     */
    public boolean setPowerOffTime(int time_second) {
        String s = "{\"time_second\":" + time_second + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetPowerOffTime, s);
    }

    /**
     * 获取语言类型
     *
     * @return
     */
    public boolean getVoiceType() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetVoiceType, "");
    }

    /**
     * 设置语言类型
     *
     * @return
     */
    public boolean setVoiceType(int type) {
        String s = "{\"type\":" + type + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetVoiceType, s);
    }


    public boolean startLive(String jsonData) {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_LiveStart, jsonData);
    }

    public boolean stopLive() {
        if (null == mApplication || null == mApplication.mGoluk) {
            return false;
        }

        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_LiveStop, "");
    }

    /**
     * 获取全局设置列表
     *
     * @return
     */
    public boolean getCapacityList() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetCapacityList, "");
    }

    /**
     * 获取精彩视频类型（时长）
     *
     * @return
     */
    public boolean getWonderfulVideoType() {
        return mApplication.mGoluk != null && mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetVideoTimeConf, "");
    }

    /**
     * 设置精彩视频类型（时长）
     *
     * @return
     */
    public boolean setWonderfulVideoType(int historyTime, int futureTime) {
        String str = JsonUtil.setWonderfulVideoTypeJson(historyTime, futureTime);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetVideoTimeConf, str);
    }

    /**
     * 获取视频水印
     *
     * @return
     */
    public boolean getVideoLogo() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_GetOSDConf, "");
    }


    /**
     * 设置视频水印
     *
     * @return
     */
    public boolean setVideoLogo(int logoVisible, int timeVisible) {
        String str = JsonUtil.setVideoLogoJson(logoVisible, timeVisible);
        GolukDebugUtils.e("", "----------------------setVideoLogo-------str:" + str);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetOSDConf, str);
    }

    /**
     * 重启IPC
     *
     * @return
     */
    public boolean setIPCReboot() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_Reboot, "");
    }

    public boolean setIPCWatermark(String logoCode, String name) {
        JSONObject json = new JSONObject();
        try {
            json.put("logo", logoCode);
            json.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPCManagerFn.IPC_VDCPCmd_SetIPCLogo, json.toString());
    }

    public boolean getIPCWatermark() {
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPCManagerFn.IPC_VDCPCmd_GetIPCLogo, "");
    }

    @Override
    public synchronized void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        // LogUtil.e("jyf",
        // "YYYYYYY----IPCManage_CallBack-----222222222---------IPCManagerAdapter-22---event:"
        // + event + " msg:" + msg+"==param1="+param1+"==data:"+(String)param2);

        if (null != mApplication && mApplication.isExit()) {
            return;
        }

        Iterator<String> iter = mIpcManagerListener.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (null != key) {
                IPCManagerFn fn = mIpcManagerListener.get(key);
                if (null != fn) {
                    fn.IPCManage_CallBack(event, msg, param1, param2);
                    // LogUtil.e("jyf",
                    // "YYYYYYY----IPCManage_CallBack-----3333---------key="+(String)key+"---event:"
                    // + event + " msg:" + msg+"==data:"+(String)param2);
                }
            }
        }

    }

    public boolean isSupportMoveDection() {
        if (!GolukApplication.getInstance().isMainland()) {
            return true;
        }
        //t1u and t2u hardware version can not be figured by productName. only by property version name
        if (T1_SIGN.equals(mProduceName) || T2_SIGN.equals(mProduceName)) {
            if (!TextUtils.isEmpty(mApplication.mIpcVersion)) {
                if (mApplication.mIpcVersion.startsWith(T1U_SIGN) || mApplication.mIpcVersion.startsWith(T2U_SIGN)) {
                    return true;
                }
                // T1/T2 1.6
                if (getVersionCode().compareTo("1.6") >= 0) {
                    return true;
                }
            }
        }
        //t3u从 1.2版本开始支持移动探测
        if (T3_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.3") >= 0) {
            return true;
        } else if (T3U_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.2") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否支持: 缩时视频/紧急视频提示语开关/碰撞灵敏度6个等级设置
     * 型号及对应最低版本: T1U_1.7 T1_1.7 T2U_1.7 T2_1.7 T3U_1.4 T3_1.4
     */
    public boolean isSupportTimeslapse() {
        final String ipcType = mProduceName;
        final String ipcVersion = getVersionCode();
        if (T1_SIGN.equals(ipcType) || T1U_SIGN.equals(ipcType) || T2_SIGN.equals(ipcType) || T2U_SIGN.equals(ipcType))
            return bigThan(ipcVersion, "1.7");
        if (T3_SIGN.equals(ipcType) || T3U_SIGN.equals(ipcType))
            return bigThan(ipcVersion, "1.5");

        return false;
    }

    /**
     * 版本比较
     */
    private boolean bigThan(String versionOne, String versionTwo) {
        return !TextUtils.isEmpty(versionOne) && !TextUtils.isEmpty(versionTwo) && versionOne.compareTo(versionTwo) >= 0;
    }

    public boolean needShowT3Hint() {
        if (T3_SIGN.equals(mProduceName)
                ||
                (T3U_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.3") >= 0)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSupportVideoLogo() {
        if ((T3_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.3") >= 0)
                ||
                (T3U_SIGN.equals(mProduceName))) {
            return true;
        } else {
            return false;
        }
    }

    public String getVersionCode() {
        if (TextUtils.isEmpty(mApplication.mIpcVersion)) {
            return "";
        }
        String version = "";
        try {
            version = mApplication.mIpcVersion.split("_")[1].substring(1, 4);
        } catch (Exception ex) {
            return "";
        }
        return version;
    }

    public boolean getT3WifiMode() {
        if (T3_SIGN.equals(mProduceName) || T3U_SIGN.equals(mProduceName)) {
            return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                    IPCManagerFn.IPC_VDCPCmd_GetWirelessMode, "");
        }
        return false;
    }


    /**
     * @param mode 0 //0:单Ap模式，1：单Station模式，2：Ap+Station混合模式
     */
    public boolean setT3WifiMode(int mode) {
        String s = "{\"mode\":" + mode + "}";
        return mApplication.mGoluk.GolukLogicCommRequest(
                GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetWirelessMode,
                s);
    }

    public boolean getSupportT3DualMode() {
        if ((T3_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.3") >= 0)
                ||
                (T3U_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.3") >= 0)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getSupportT3Logo() {
        if ((T3_SIGN.equals(mProduceName) && getVersionCode().compareTo("1.3") >= 0)
                ||
                T3U_SIGN.equals(mProduceName)) {
            return true;
        } else {
            return false;
        }
    }

}
