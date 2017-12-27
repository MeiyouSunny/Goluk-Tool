package cn.com.mobnote.module.ipcmanager;

import cn.com.mobnote.logic.IGolukCommFn;

public interface IPCManagerFn extends IGolukCommFn {


	public static final int WIFI_CONNECT_SUUCESS = 3;

	// VDCP 控制IPC
	// VDTP　文件下载

	public static final int RESULE_SUCESS = 0;

	/** 循环影像 */
	public static final int TYPE_CIRCULATE = 1;
	/** 紧急录像　 */
	public static final int TYPE_URGENT = 2;
	/** 精彩视频 */
	public static final int TYPE_SHORTCUT = 4;

	public static final String VIDEO_SAVEPATH = "fs1:/video/";
//
//	/**
//	 *
//	 * 网络传输事件声明
//	 * */
//
	/** IPC控制连接状态 */
	public static final int ENetTransEvent_IPC_VDCP_ConnectState = 0;
	/** IPC控制命令应答 */
	public static final int ENetTransEvent_IPC_VDCP_CommandResp = 1;
	/** IPC下载连接状态 */
	public static final int ENetTransEvent_IPC_VDTP_ConnectState = 2;
	/** IPC下载结果应答 */
	public static final int ENetTransEvent_IPC_VDTP_Resp = 3;

	public static final int ENetTransEvent_IPC_UpGrade_Resp = 4;
	/** Mobnote连接状态 */
//	public static final int ENetTransEvent_Mobnote_ConnectState = 5;
//	/** Mobnote结构应答 */
//	public static final int ENetTransEvent_Mobnote_Resp = 6;
//
//	/**
//	 *
//	 * 服务器连接状态事件下消息ID
//	 * */
//
	/** 空闲 */
	public static final int ConnectionStateMsg_Idle = 0;
	/** 连接中 */
	public static final int ConnectionStateMsg_Connecting = 1;
	/** 连接成功 */
	public static final int ConnectionStateMsg_Connected = 2;
	/** 连接断开 */
	public static final int ConnectionStateMsg_DisConnected = 3;
//
//	/**
//	 *
//	 * 服务器IPC_VDTP消息ID
//	 * */
//
//	/** 文件传输消息 */
	public static final int IPC_VDTP_Msg_File = 0;
//
//	/**
//	 *
//	 * 服务器IPC_VDCP消息ID
//	 * */
//
	/** 初始化消息 */
	public static final int IPC_VDCP_Msg_Init = 0;
//	/** 多文件目录查询 */
//	public static final int IPC_VDCP_Msg_Query = 1000;
//	/** 单文件查询 */
//	public static final int IPC_VDCP_Msg_SingleQuery = 1001;
//	/** 删除文件 */
//	public static final int IPC_VDCP_Msg_Erase = 1002;
//	/** 请求紧急、精彩视频录制 */
//	public static final int IPC_VDCP_Msg_TriggerRecord = 1003;
//	/** 实时抓图 */
//	public static final int IPC_VDCP_Msg_SnapPic = 1004;
//	/** 查询录制存储状态 */
//	public static final int IPC_VDCP_Msg_RecPicUsage = 1005;
//	/** 查询设备状态 */
//	public static final int IPC_VDCP_Msg_DeviceStatus = 1006;
//	/** 获取IPC系统标识 */
//	public static final int IPC_VDCP_Msg_GetIdentity = 1007;
//	/** IPC重启 */
//	public static final int IPC_VDCP_Msg_Reboot = 1008;
//	/** IPC恢复出厂设置 */
//	public static final int IPC_VDCP_Msg_Restore = 1009;
//	/** IPC格式化SD卡 */
//	public static final int IPC_VDCP_Msg_FormatDisk = 1010;
	/** 设置IPC系统时间 */
	public static final int IPC_VDCP_Msg_SetTime = 1011;
	/** 获取IPC系统时间 */
	public static final int IPC_VDCP_Msg_GetTime = 1012;
//	/** 设置IPC系统WIFI配置 */
//	public static final int IPC_VDCP_Msg_SetWifiCfg = 1013;
	/** 获取IPC系统音视频编码配置 */
	public static final int IPC_VDCP_Msg_GetVedioEncodeCfg = 1014;
//	/** 设置IPC系统音视频编码配置 */
//	public static final int IPC_VDCP_Msg_SetVedioEncodeCfg = 1015;
//	/** 设置IPC行车影像开始录制 */
//	public static final int IPC_VDCP_Msg_StartRecord = 1016;
//	/** 设置IPC行车影像停止录制 */
//	public static final int IPC_VDCP_Msg_StopRecord = 1017;
//	/** 获取IPC行车影像录制状态 */
//	public static final int IPC_VDCP_Msg_GetRecordState = 1018;
//	/** 获取IPC水印显示状态 */
//	public static final int IPC_VDCP_Msg_GetImprintShow = 1019;
//	/** 设置IPC水印显示状态 */
//	public static final int IPC_VDCP_Msg_SetImprintShow = 1020;
//	/** 获取IPC GSensor控制紧急录像策略 */
//	public static final int IPC_VDCP_Msg_GetGSensorControlCfg = 1021;
//	/** 设置IPC GSensor控制紧急录像策略 */
//	public static final int IPC_VDCP_Msg_SetGSensorControlCfg = 1022;
//	/** IPC升级状态 */
//	public static final int IPC_VDCP_Msg_IPCUpgrade = 1023;
//	/** IPC 自动录制紧急、精彩视频 */
//	public static final int IPC_VDCP_Msg_IPCKit = 1024;
//	/** 读取安防模式和移动侦测参数 */
//	public static final int IPC_VDCP_Msg_GetMotionCfg = 1025;
//	/** 设置安防模式和移动侦测参数 */
//	public static final int IPC_VDCP_Msg_SetMotionCfg = 1026;
//	/** 读取IPC版本信息 */
//	public static final int IPC_VDCP_Msg_GetVersion = 1027;
//	/** SD卡图片查询 */
//	public static final int IPC_VDCP_Msg_QueryPic = 1028;
//	/** SD卡查询单张图片 */
//	public static final int IPC_VDCP_Msg_SingleQueryPic = 1029;
//	/** 删除多个SD卡录像、图片 */
//	public static final int IPC_VDCP_Msg_RecPicEraseMulti = 1030;
//	/** 按条件删除图片 */
//	public static final int IPC_VDCP_Msg_PicErase = 1031;
//	/** 按条件删除录像 */
//	public static final int IPC_VDCP_Msg_RecErase = 1032;
//	/** 获取IPC系统WIFI配置 */
//	public static final int IPC_VDCP_Msg_GetWifiCfg = 1033;
//	/** 获取IPC系统语音播报开关状态 **/
//	public static final int IPC_VDCP_Msg_GetSpeakerSwitch = 1034;
//	/** 设置IPC系统语音播报开关状态 **/
//	public static final int IPC_VDCP_Msg_SetSpeakerSwitch = 1035;
//	/** 获取停车休眠参数 **/
//	public static final int IPC_VDCP_Msg_GetHiberatePara = 1036;
//	/** 设置停车休眠参数 **/
//	public static final int IPC_VDCP_Msg_SetHiberatePara = 1037;
//	/** 获取功能开关状态 **/
//	public static final int IPC_VDCP_Msg_GetFunctionSwitch = 1038;
//	/** 设置功能开关状态 **/
//	public static final int IPC_VDCP_Msg_SetFunctionSwitch = 1039;
//	/** 获取isp模式 **/
//	public static final int IPC_VDCP_Msg_GetISPMode = 1041;
//	/** 设置isp模式 **/
//	public static final int IPC_VDCP_Msg_SetISPMode = 1042;
	/** 获取IPC录制声音配置 **/
	public static final int IPC_VDCP_Msg_GetRecAudioCfg = 1043;
	/** 设置IPC录制声音 **/
	public static final int IPC_VDCP_Msg_SetRecAudioCfg = 1044;
//	/** 获取抓图质量配置 **/
//	public static final int IPC_VDCP_Msg_GetPicCfg = 1045;
//	/** 设置抓图质量配置 **/
//	public static final int IPC_VDCP_Msg_SetPicCfg = 1046;
//	/** 获取kit功能配置 **/
//	public static final int IPC_VDCP_Msg_GetKitCfg = 1047;
//	/** 设置kit功能配置 **/
//	public static final int IPC_VDCP_Msg_SetKitCfg = 1048;
//	/** 获取ipc时间同步配置 **/
//	public static final int IPC_VDCP_Msg_GetTimeSyncCfg = 1049;
//	/** 设置ipc时间同步配置 **/
//	public static final int IPC_VDCP_Msg_SetTimeSyncCfg = 1050;
//	/** 获取ipc时间同步配置 **/
//	public static final int IPC_VDCP_Msg_GetAutoRotationCfg = 1051;
//	/** 设置ipc时间同步配置 **/
//	public static final int IPC_VDCP_Msg_SetAutoRotationCfg = 1052;
//	/** adas 推送的应答消息 **/
//	public static final int IPC_VDCP_Msg_PushEvent_ADAS = 3000;
//	/** 通用类推送消息 */
//	public static final int IPC_VDCP_Msg_PushEvent_Comm = 3001;
//
//	/** 获取ipc ADAS功能 **/
//	public static final int IPC_VDCP_Msg_GetADASConfig = 2000;
//	/** 设置ipc ADAS功能 **/
//	public static final int IPC_VDCP_Msg_SetADASConfig = 2001;
//	/** 获取精彩视频分辨率 **/
//	public static final int IPC_VDCP_Msg_GetVideoResolution = 2200;
//	/** 设置精彩视频分辨率 **/
//	public static final int IPC_VDCP_Msg_SetVideoResolution = 2201;
//	/** 获取音量大小 **/
//	public static final int IPC_VDCP_Msg_GetVolume = 2202;
//	/** 设置音量大小 **/
//	public static final int IPC_VDCP_Msg_SetVolume = 2203;
//	/** 获取关机时间 **/
//	public static final int IPC_VDCP_Msg_GetPowerOffTime = 2204;
//	/** 设置关机时间 **/
//	public static final int IPC_VDCP_Msg_SetPowerOffTime = 2205;
//	/** 获取语音类型 **/
//	public static final int IPC_VDCP_Msg_GetVoiceType = 2206;
//	/** 设置语音类型 **/
//	public static final int IPC_VDCP_Msg_SetVoiceType = 2207;
//
//	/** 开始IPC直播推流 */
//	public static final int IPC_VDCP_Msg_LiveStart = 2213;
//	/** 停止IPC直播推流 */
//	public static final int IPC_VDCP_Msg_LiveStop = 2214;
//
//	/** 获取精彩等视频获取时长配置 **/
//	public static final int IPC_VDCP_Msg_GetVideoTimeConf = 2208;
//	/** 设置精彩等视频获取时长配置 **/
//	public static final int IPC_VDCP_Msg_SetVideoTimeConf = 2209;
//	/** 获取OSD水印显示配置 **/
//	public static final int IPC_VDCP_Msg_GetOSDConf = 2210;
//	/** 设置OSD水印显示配置 **/
//	public static final int IPC_VDCP_Msg_SetOSDConf = 2211;
//	/** 获取全局设置项列表 **/
//	public static final int IPC_VDCP_Msg_GetCapacityList = 2212;
//
//
//
//	/**
//	 *
//	 * 文件数据传输事件下消息ID (用于手机与平板之间发送消息回调)
//	 * */
//
//	/** 校验文件列表消息 */
//	public static final int MobRespMsg_FileList = 0;
//
//	public static final int MobRespMsg_File = 1;
//
//	/**
//	 *
//	 * IPCMgr模式设置
//	 * */
//
	/** IPC直连，默认为直连 */
	public static final int IPCMgrMode_IPCDirect = 0;
//	/** 与Mobnote中转连接 */
//	public static final int IPCMgrMode_Mobnote = 1;
	/** T1模式 */
	public static final int IPCMgrMode_T1 = 2;
//
//	/**
//	 *
//	 * IPC中VDCP控制命令 主要用于控制IPC相关的各种命令
//	 * */
//
	/** wifi网络环境变更 */
	public static final int IPC_CommCmd_WifiChanged = 0;
	/** 设置IPC连接模式 */
	public static final int IPC_CommCmd_SetMode = 1;
//	/** 设置GPS信息 */
//	public static final int IPC_CommCmd_SetGpsInfo = 2;
//	/** 添加IPC下载任务 */
	public static final int IPC_VDTPCmd_AddDownloadFile = 3;
	/** 停止IPC下载所有任务 */
	public static final int IPC_VDTPCmd_StopDownloadFile = 4;
//
	/** 多文件目录查询 */
	public static final int IPC_VDCPCmd_Query = 1000;
	/** 单文件查询 */
	public static final int IPC_VDCPCmd_SingleQuery = 1001;
//	/** 删除文件 */
//	public static final int IPC_VDCPCmd_Erase = 1002;
//	/** 请求紧急、精彩视频录制 */
//	public static final int IPC_VDCPCmd_TriggerRecord = 1003;
//	/** 实时抓图 */
//	public static final int IPC_VDCPCmd_SnapPic = 1004;
	/** 查询录制存储状态 */
	public static final int IPC_VDCPCmd_RecPicUsage = 1005;
//	/** 查询设备状态 */
//	public static final int IPC_VDCPCmd_DeviceStatus = 1006;
//	/** 获取IPC系统标识 */
//	public static final int IPC_VDCPCmd_GetIdentity = 1007;
//	/** IPC重启 */
//	public static final int IPC_VDCPCmd_Reboot = 1008;
//	/** IPC恢复出厂设置 */
//	public static final int IPC_VDCPCmd_Restore = 1009;
//	/** IPC格式化SD卡 */
//	public static final int IPC_VDCPCmd_FormatDisk = 1010;
	/** 设置IPC系统时间 */
	public static final int IPC_VDCPCmd_SetTime = 1011;
	/** 获取IPC系统时间 */
	public static final int IPC_VDCPCmd_GetTime = 1012;
//	/** 设置IPC系统WIFI配置 */
//	public static final int IPC_VDCPCmd_SetWifiCfg = 1013;
	/** 获取IPC系统音视频编码配置 */
	public static final int IPC_VDCPCmd_GetVideoEncodeCfg = 1014;
	/** 设置IPC系统音视频编码配置 */
	public static final int IPC_VDCPCmd_SetVideoEncodeCfg = 1015;
//	/** 设置IPC行车影像开始录制 */
//	public static final int IPC_VDCPCmd_StartRecord = 1016;
//	/** 设置IPC行车影像停止录制 */
//	public static final int IPC_VDCPCmd_StopRecord = 1017;
//	/** 获取IPC行车影像录制状态 */
//	public static final int IPC_VDCPCmd_GetRecordState = 1018;
//	/** 获取IPC水印显示状态 */
//	public static final int IPC_VDCPCmd_GetImprintShow = 1019;
//	/** 设置IPC水印显示状态 */
//	public static final int IPC_VDCPCmd_SetImprintShow = 1020;
//	/** 获取IPC GSensor控制紧急录像策略 */
//	public static final int IPC_VDCPCmd_GetGSensorControlCfg = 1021;
//	/** 设置IPC GSensor控制紧急录像策略 */
//	public static final int IPC_VDCPCmd_SetGSensorControlCfg = 1022;
//	/** /IPC设备进行升级 */
//	public static final int IPC_VDCPCmd_IPCUpgrade = 1023;
//	/** 读取安防模式和移动侦测参数 */
//	public static final int IPC_VDCPCmd_GetMotionCfg = 1024;
//	/** 设置安防模式和移动侦测参数 */
//	public static final int IPC_VDCPCmd_SetMotionCfg = 1025;
//	/** 读取IPC版本信息 */
//	public static final int IPC_VDCPCmd_GetVersion = 1026;
//	/** SD卡图片查询 */
//	public static final int IPC_VDCPCmd_QueryPic = 1027;
//	/** SD卡查询单张图片 */
//	public static final int IPC_VDCPCmd_SingleQueryPic = 1028;
//	/** 删除多个SD卡录像、图片 */
//	public static final int IPC_VDCPCmd_RecPicEraseMulti = 1029;
//	/** 按条件删除录像 */
//	public static final int IPC_VDCPCmd_RecErase = 1030;
//	/** 按条件删除图片 */
//	public static final int IPC_VDCPCmd_PicErase = 1031;
//	/** 获取IPC系统WIFI配置 */
//	public static final int IPC_VDCPCmd_GetWifiCfg = 1032;
//	/** IPC设备升级停止 **/
//	public static final int IPC_VDCPCmd_StopIPCUpgrade = 1033;
//	/** 获取IPC系统语音播报开关状态 **/
//	public static final int IPC_VDCPCmd_GetSpeakerSwitch = 1034;
//	/** 设置IPC系统语音播报开关状态 **/
//	public static final int IPC_VDCPCmd_SetSpeakerSwitch = 1035;
//	/** 获取停车休眠参数 **/
//	public static final int IPC_VDCPCmd_GetHiberatePara = 1036;
//	/** 设置停车休眠参数 **/
//	public static final int IPC_VDCPCmd_SetHiberatePara = 1037;
//	/** 获取功能开关状态 **/
//	public static final int IPC_VDCPCmd_GetFunctionSwitch = 1038;
//	/** 设置功能开关状态 **/
//	public static final int IPC_VDCPCmd_SetFunctionSwitch = 1039;
//	/** 获取isp模式 **/
//	public static final int IPC_VDCPCmd_GetISPMode = 1041;
//	/** 设置isp模式 **/
//	public static final int IPC_VDCPCmd_SetISPMode = 1042;
	/** 获取IPC录制声音配置 **/
	public static final int IPC_VDCPCmd_GetRecAudioCfg = 1043;
	/** 设置IPC录制声音 **/
	public static final int IPC_VDCPCmd_SetRecAudioCfg = 1044;
//	/** 获取抓图质量配置 **/
//	public static final int IPC_VDCPCmd_GetPicCfg = 1045;
//	/** 设置抓图质量配置 **/
//	public static final int IPC_VDCPCmd_SetPicCfg = 1046;
//	/** 获取kit功能配置 **/
//	public static final int IPC_VDCPCmd_GetKitCfg = 1047;
//	/** 设置kit功能配置 **/
//	public static final int IPC_VDCPCmd_SetKitCfg = 1048;
//	/** 获取ipc时间同步配置 **/
//	public static final int IPC_VDCPCmd_GetTimeSyncCfg = 1049;
//	/** 设置ipc时间同步配置 **/
//	public static final int IPC_VDCPCmd_SetTimeSyncCfg = 1050;
//	/** 获取ipc时间同步配置 **/
//	public static final int IPC_VDCPCmd_GetAutoRotationCfg = 1051;
//	/** 设置ipc时间同步配置 **/
//	public static final int IPC_VDCPCmd_SetAutoRotationCfg = 1052;
//	/** 获取ipc ADAS功能 **/
//	public static final int IPC_VDCPCmd_GetADASConfig = 2000;
//	/** 设置ipc ADAS功能 **/
//	public static final int IPC_VDCPCmd_SetADASConfig = 2001;
//	/** 获取精彩视频分辨率 **/
//	public static final int IPC_VDCPCmd_GetVideoResolution = 2200;
//	/** 设置精彩视频分辨率 **/
//	public static final int IPC_VDCPCmd_SetVideoResolution = 2201;
//	/** 获取音量大小 **/
//	public static final int IPC_VDCPCmd_GetVolume = 2202;
//	/** 设置音量大小 **/
//	public static final int IPC_VDCPCmd_SetVolume = 2203;
//	/** 获取关机时间 **/
//	public static final int IPC_VDCPCmd_GetPowerOffTime = 2204;
//	/** 设置关机时间 **/
//	public static final int IPC_VDCPCmd_SetPowerOffTime = 2205;
//	/** 获取语音类型 **/
//	public static final int IPC_VDCPCmd_GetVoiceType = 2206;
//	/** 设置语音类型 **/
//	public static final int IPC_VDCPCmd_SetVoiceType = 2207;
//
//	/** 开始IPC直播推流 */
//	public static final int IPC_VDCPCmd_LiveStart = 2213;
//	/** 停止IPC直播推流 */
//	public static final int IPC_VDCPCmd_LiveStop = 2214;
//	/** 获取直播推流状态 */
//	public static final int IPC_VDCPCmd_GetLiveStatus = 2215;
//	/** 命令数量 */
//	public static final int IPC_VDCPCmd_Msg_COUNT = 2216;
//
//	/** 获取精彩等视频时长配置 **/
//	public static final int IPC_VDCPCmd_GetVideoTimeConf = 2208;
//	/** 设置精彩等视频时长配置 **/
//	public static final int IPC_VDCPCmd_SetVideoTimeConf = 2209;
//	/** 获取OSD水印显示配置 **/
//	public static final int IPC_VDCPCmd_GetOSDConf = 2210;
//	/** 设置OSD水印显示配置 **/
//	public static final int IPC_VDCPCmd_SetOSDConf = 2211;
//	/** 获取全局设置项列表 **/
//	public static final int IPC_VDCPCmd_GetCapacityList = 2212;


	public void IPCManage_CallBack(int event, int msg, int param1, Object param2);
}
