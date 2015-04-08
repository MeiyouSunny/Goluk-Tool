package cn.com.mobnote.module.ipcmanager;

import cn.com.mobnote.logic.IGolukCommFn;

public interface IPCManagerFn extends IGolukCommFn {

	// VDCP 控制IPC
	// VDTP　文件下载

	public static final int RESULE_SUCESS = 0;

	/** 循环影像 */
	public static final int TYPE_CIRCULATE = 1;
	/** 紧急录像　 */
	public static final int TYPE_URGENT = 2;
	/** 精彩视频 */
	public static final int TYPE_SHORTCUT = 4;

	/**
	 * 
	 * 网络传输事件声明
	 * */

	/** IPC控制连接状态 */
	public static final int ENetTransEvent_IPC_VDCP_ConnectState = 0;
	/** IPC控制命令应答 */
	public static final int ENetTransEvent_IPC_VDCP_CommandResp = 1;
	/** IPC下载连接状态 */
	public static final int ENetTransEvent_IPC_VDTP_ConnectState = 2;
	/** IPC下载结果应答 */
	public static final int ENetTransEvent_IPC_VDTP_Resp = 3;
	/** Mobnote连接状态 */
	public static final int ENetTransEvent_Mobnote_ConnectState = 4;
	/** Mobnote结构应答 */
	public static final int ENetTransEvent_Mobnote_Resp = 5;

	/**
	 * 
	 * 服务器连接状态事件下消息ID
	 * */

	/** 空闲 */
	public static final int ConnectionStateMsg_Idle = 0;
	/** 连接中 */
	public static final int ConnectionStateMsg_Connecting = 1;
	/** 连接成功 */
	public static final int ConnectionStateMsg_Connected = 2;
	/** 连接断开 */
	public static final int ConnectionStateMsg_DisConnected = 3;

	/**
	 * 
	 * 服务器IPC_VDTP消息ID
	 * */

	/** 文件传输消息 */
	public static final int IPC_VDTP_Msg_File = 0;

	/**
	 * 
	 * 服务器IPC_VDCP消息ID
	 * */

	/** 初始化消息 */
	public static final int IPC_VDCP_Msg_Init = 0;
	/** 多文件目录查询 */
	public static final int IPC_VDCP_Msg_Query = 1000;
	/** 单文件查询 */
	public static final int IPC_VDCP_Msg_SingleQuery = 1001;
	/** 删除文件 */
	public static final int IPC_VDCP_Msg_Erase = 1002;
	/** 请求紧急、精彩视频录制 */
	public static final int IPC_VDCP_Msg_TriggerRecord = 1003;
	/** 实时抓图 */
	public static final int IPC_VDCP_Msg_SnapPic = 1004;
	/** 查询录制存储状态 */
	public static final int IPC_VDCP_Msg_RecPicUsage = 1005;
	/** 查询设备状态 */
	public static final int IPC_VDCP_Msg_DeviceStatus = 1006;
	/** 获取IPC系统标识 */
	public static final int IPC_VDCP_Msg_GetIdentity = 1007;
	/** IPC重启 */
	public static final int IPC_VDCP_Msg_Reboot = 1008;
	/** IPC恢复出厂设置 */
	public static final int IPC_VDCP_Msg_Restore = 1009;
	/** IPC格式化SD卡 */
	public static final int IPC_VDCP_Msg_FormatDisk = 1010;
	/** 设置IPC系统时间 */
	public static final int IPC_VDCP_Msg_SetTime = 1011;
	/** 设置IPC系统WIFI配置 */
	public static final int IPC_VDCP_Msg_SetWifiCfg = 1012;
	/** 获取IPC系统音视频编码配置 */
	public static final int IPC_VDCP_Msg_GetVedioEncodeCfg = 1013;
	/** 设置IPC系统音视频编码配置 */
	public static final int IPC_VDCP_Msg_SetVedioEncodeCfg = 1014;
	/** 设置IPC行车影像开始录制 */
	public static final int IPC_VDCP_Msg_StartRecord = 1015;
	/** 设置IPC行车影像停止录制 */
	public static final int IPC_VDCP_Msg_StopRecord = 1016;
	/** 获取IPC行车影像录制状态 */
	public static final int IPC_VDCP_Msg_GetRecordState = 1017;
	/** 获取IPC水印显示状态 */
	public static final int IPC_VDCP_Msg_GetImprintShow = 1018;
	/** 设置IPC水印显示状态 */
	public static final int IPC_VDCP_Msg_SetImprintShow = 1019;

	
	
	/**
	 * 
	 * 文件数据传输事件下消息ID (用于手机与平板之间发送消息回调)
	 * */

	/** 校验文件列表消息 */
	public static final int MobRespMsg_FileList = 0;

	public static final int MobRespMsg_File = 1;

	/**
	 * 
	 * IPCMgr模式设置
	 * */

	/** IPC直连，默认为直连 */
	public static final int IPCMgrMode_IPCDirect = 0;
	/** 与Mobnote中转连接 */
	public static final int IPCMgrMode_Mobnote = 1;

	/**
	 * 
	 * IPC中VDCP控制命令 主要用于控制IPC相关的各种命令
	 * */

	/** wifi网络环境变更 */
	public static final int IPC_CommCmd_WifiChanged = 0;
	/** 设置IPC连接模式 */
	public static final int IPC_CommCmd_SetMode = 1;
	/** 设置GPS信息 */
	public static final int IPC_CommCmd_SetGpsInfo = 2;
	/** 添加IPC下载任务 */
	public static final int IPC_VDTPCmd_AddDownloadFile = 3;
	/** 多文件目录查询 */
	public static final int IPC_VDCPCmd_Query = 1000;
	/** 单文件查询 */
	public static final int IPC_VDCPCmd_SingleQuery = 1001;
	/** 删除文件 */
	public static final int IPC_VDCPCmd_Erase = 1002;
	/** 请求紧急、精彩视频录制 */
	public static final int IPC_VDCPCmd_TriggerRecord = 1003;
	/** 实时抓图 */
	public static final int IPC_VDCPCmd_SnapPic = 1004;
	/** 查询录制存储状态 */
	public static final int IPC_VDCPCmd_RecPicUsage = 1005;
	/** 查询设备状态 */
	public static final int IPC_VDCPCmd_DeviceStatus = 1006;
	/** 获取IPC系统标识 */
	public static final int IPC_VDCPCmd_GetIdentity = 1007;
	/** IPC重启 */
	public static final int IPC_VDCPCmd_Reboot = 1008;
	/** IPC恢复出厂设置 */
	public static final int IPC_VDCPCmd_Restore = 1009;
	/** IPC格式化SD卡 */
	public static final int IPC_VDCPCmd_FormatDisk = 1010;
	/** 设置IPC系统时间 */
	public static final int IPC_VDCPCmd_SetTime = 1011;
	/** 设置IPC系统WIFI配置 */
	public static final int IPC_VDCPCmd_SetWifiCfg = 1012;
	/** 获取IPC系统音视频编码配置 */
	public static final int IPC_VDCPCmd_GetVideoEncodeCfg = 1013;
	/** 设置IPC系统音视频编码配置 */
	public static final int IPC_VDCPCmd_SetVideoEncodeCfg = 1014;
	/** 设置IPC行车影像开始录制 */
	public static final int IPC_VDCPCmd_StartRecord = 1015;
	/** 设置IPC行车影像停止录制 */
	public static final int IPC_VDCPCmd_StopRecord = 1016;
	/** 获取IPC行车影像录制状态 */
	public static final int IPC_VDCPCmd_GetRecordState = 1017;
	/** 获取IPC水印显示状态 */
	public static final int IPC_VDCPCmd_GetImprintShow = 1018;
	/** 设置IPC水印显示状态 */
	public static final int IPC_VDCPCmd_SetImprintShow = 1019;
	
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2);
}
