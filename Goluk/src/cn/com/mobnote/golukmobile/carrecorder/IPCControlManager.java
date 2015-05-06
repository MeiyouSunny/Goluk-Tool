package cn.com.mobnote.golukmobile.carrecorder;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.settings.VideoQualityActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.utils.LogUtil;


 /**
  * 1.编辑器必须显示空白处
  *
  * 2.所有代码必须使用TAB键缩进
  *
  * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
  *
  * 4.注释必须在行首写.(枚举除外)
  *
  * 5.函数使用块注释,代码逻辑使用行注释
  *
  * 6.文件头部必须写功能说明
  *
  * 7.所有代码文件头部必须包含规则说明
  *
  * IPC控制管理
  *
  * 2015年3月21日
  *
  * @author xuhw
  */
public class IPCControlManager implements IPCManagerFn{
	
	/** IPC回调监听列表 */
	private HashMap<String, IPCManagerFn> mIpcManagerListener = null;
	/** Application实例,用于调用JNI的对象 */
	private GolukApplication mApplication = null;
	
	public IPCControlManager(GolukApplication application) {
		mApplication = application;
		mIpcManagerListener = new HashMap<String, IPCManagerFn>();
		// 注册IPC回调
		int result = mApplication.mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_IPCManager, this);

		// 设置连接模式
		String json = JsonUtil.getIPCConnModeJson(IPCMgrMode_IPCDirect);
		boolean isSucess = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
				IPC_CommCmd_SetMode, json);

		// WIFI连接状态
		//setIPCWifiState(true);
	}
	
	/**
	 * 告知IPC wifi连接状态
	 * @param isConnect ture:连接　false:未连接
	 * @param ip ipc热点ip地址
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public boolean setIPCWifiState(boolean isConnect,String ip){
		int state = isConnect ? 1 : 0;
		String json = JsonUtil.getWifiChangeJson(state, ip);
		boolean isSucess = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_WifiChanged, json);
		return isSucess;
	}
	
	/**
	 * 通知ipc连接手机热点
	 * @param json
	 * @return
	 */
	public boolean setIpcLinkPhoneHot(String json){
		boolean isSucess = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetWifiCfg, json);
		return isSucess;
	}
	
	/**
	 * 视频截图
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
	 * @param filetype 1:循环影像 2:紧急视频 4:精彩视频
	 * @param limitCount 最多查询条数
	 * @param timestart 查询起始时间（0表示查询所有）
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public boolean queryFileListInfo(int filetype, int limitCount, long timestart, long timeend) {
		String queryParam = IpcDataParser.getQueryMoreFileJson(filetype, limitCount, timestart, timeend);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Query,
				queryParam);
	}
	
	/**
	 * 下载文件
	 * @param filename 文件名称
	 * @param tag 唯一标识
	 * @param savepath 文件保存路径
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public boolean downloadFile(String filename, String tag, String savepath, long filetime) {
		String json = JsonUtil.getDownFileJson(filename, tag, savepath, filetime);
		GFileUtils.writeIPCLog("==downloadFile==json="+json);
		LogUtil.e("xuhw", "YYYYYY====downloadFile=====json="+json);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDTPCmd_AddDownloadFile,
				json);
	}
	
	/**
	 * 删除文件
	 * @param filename 文件名称
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public boolean deleteFile(String filename){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Erase, filename);
	}
	
	/**
	 * 更新经纬度信息
	 * @param lon 经度
	 * @param lat 纬度
	 * @param speed 速度
	 * @param direction 方向
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	public boolean updateGPS(long lon, long lat, int speed, int direction){
		String json = JsonUtil.getGPSJson(lon, lat, speed, direction);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_SetGpsInfo, json);
	}
	
	/**
	 * 查询设备状态
	 * @author xuhw
	 * @date 2015年4月2日
	 */
	public boolean queryDeviceStatus(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_DeviceStatus, "");
	}
	
	/**
	 * 查询录制存储状态
	 * @author xuhw
	 * @date 2015年4月2日
	 */
	public boolean queryRecordStorageStatus(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecPicUsage, "");
	}
	
	/**
	 * 获取IPC系统标识
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public boolean getIPCIdentity(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetIdentity, "");
	}
	
	/**
	 * IPC重启
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public boolean rebootIPC(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Reboot, "");
	}
	
	/**
	 * IPC恢复出厂设置
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public boolean restoreIPC(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Restore, "");
	}
	
	/**
	 * IPC格式化SD卡
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public boolean formatDisk(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_FormatDisk, "");
	}
	
	/**
	 * 设置IPC系统时间
	 * @param time 距离1970年1月1日0时0分0秒所经过的秒数
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public boolean setIPCSystemTime(long time){
		String json = JsonUtil.getTimeJson(time);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetTime, json);
	}
	
	/**
	 * 获取IPC系统时间
	 * @return
	 * @author xuhw
	 * @date 2015年4月13日
	 */
	public boolean getIPCSystemTime(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetTime, "");
	}
	
	/**
	 * 设置IPC系统WIFI配置
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public boolean setIPCWifiCfg(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetWifiCfg, "");
	}
	
	/**
	 * 获取IPC系统音视频编码配置
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public boolean getVideoEncodeCfg(int type){
		String json = JsonUtil.getVideoCfgJson(type);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetVideoEncodeCfg, json);
	}
	
	/**
	 * 设置IPC系统音视频编码配置
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public boolean setVideoEncodeCfg(VideoQualityActivity.SensitivityType type){
		String json = JsonUtil.getVideoConfig(type);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetVideoEncodeCfg, json);
	}
	
	/**
	 * 设置IPC系统音视频编码配置
	 * @param mVideoConfigState
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean setVideoEncodeCfg(VideoConfigState mVideoConfigState){
		String json = JsonUtil.getVideoConfig(mVideoConfigState);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetVideoEncodeCfg, json);
	}
	
	/**
	 * 设置音频开关
	 * @param mVideoConfigState
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean setAudioCfg(VideoConfigState mVideoConfigState){
		String json = JsonUtil.getVideoConfig(mVideoConfigState);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetVideoEncodeCfg, json);
	}
	
	/**
	 * 设置IPC行车影像开始录制
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean startRecord(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_StartRecord, "");
	}
	
	/**
	 * 设置IPC行车影像停止录制
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean stopRecord(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_StopRecord, "");
	}
	
	/**
	 * 获取IPC行车影像录制状态
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean getRecordState(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetRecordState, "");
	}
	
	/**
	 * 获取IPC水印显示状态
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean getWatermarkShowState(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetImprintShow, "");
	}
	
	/**
	 * 设置IPC水印显示状态
	 * @return
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	public boolean setWatermarkShowState(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetImprintShow, "");
	}
	
	/**
	 * 获取IPC GSensor控制紧急录像策略
	 * @return
	 * @author xuhw
	 * @date 2015年4月9日
	 */
	public boolean getGSensorControlCfg(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetGSensorControlCfg, "");
	}
	/**
	 * 设置IPC GSensor控制紧急录像策略
	 * @param policy
	 * @return
	 * @author xuhw
	 * @date 2015年4月22日
	 */
	public boolean setGSensorControlCfg(int policy){
		JSONObject json = new JSONObject();
		try {
			json.put("policy", policy);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetGSensorControlCfg, json.toString());
	}
	
	/**
	 * IPC设备进行升级
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean ipcUpgrade(){
		Log.i("lily", "---------ipcUpgrade------"+IPC_VDCPCmd_IPCUpgrade);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_IPCUpgrade, "fs1:/update/ipc_upgrade_2015-04-30-15-58.bin");
	}
	
	/**
	 * 读取安防模式和移动侦测参数
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean getMotionCfg(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetMotionCfg, "");
	}
	
	/**
	 * 设置安防模式和移动侦测参数
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean setMotionCfg(int enableSecurity, int snapInterval){
		JSONObject json = new JSONObject();
		try {
			json.put("enableSecurity", enableSecurity);
			json.put("snapInterval", snapInterval);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SetMotionCfg, json.toString());
	}
	
	/**
	 * 读取IPC版本信息
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean getVersion(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_GetVersion, "");
	}
	
	/**
	 * SD卡图片查询
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean queryPic(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_QueryPic, "");
	}
	
	/**
	 * SD卡查询单张图片
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean singleQueryPic(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SingleQueryPic, "");
	}
	
	/**
	 * 删除多个SD卡录像、图片
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean recPicEraseMulti(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecPicEraseMulti, "");
	}
	
	/**
	 * 按条件删除录像
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean recErase(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecErase, "");
	}
	
	/**
	 * 按条件删除图片
	 * @return
	 * @author xuhw
	 * @date 2015年4月21日
	 */
	public boolean ricErase(){
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_PicErase, "");
	}
	
	/**
	 * 添加IPC管理监听
	 * @param from 来自哪里的
	 * @param fn 监听接口
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public void addIPCManagerListener(String from, IPCManagerFn fn) {
		this.mIpcManagerListener.put(from, fn);
	}
	
	/**
	 * 删除IPC监听
	 * @param from 来自那里的
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public void removeIPCManagerListener(String from){
		this.mIpcManagerListener.remove(from);
	}
	
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
//		LogUtil.e("jyf", "YYYYYYY----IPCManage_CallBack-----222222222---------IPCManagerAdapter-22---event:" + event + " msg:" + msg+"==param1="+param1+"==data:"+(String)param2);

		Iterator<String> iter = mIpcManagerListener.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			if (null != key) {
				IPCManagerFn fn = mIpcManagerListener.get(key);
				if (null != fn) {
					fn.IPCManage_CallBack(event, msg, param1, param2);
//					LogUtil.e("jyf", "YYYYYYY----IPCManage_CallBack-----3333---------key="+(String)key+"---event:" + event + " msg:" + msg+"==data:"+(String)param2);
				}
			}
		}
		
	}
	
}
