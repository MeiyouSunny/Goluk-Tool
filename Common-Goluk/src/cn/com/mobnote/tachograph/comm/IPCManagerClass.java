package cn.com.mobnote.tachograph.comm;

public class IPCManagerClass {

	/** 本模块的指针 */
	private int pManager = 0;

	/**
	 * 创建模块
	 * 
	 * @author jiayf
	 * @date Feb 11, 2015
	 */
	public void IPCManager_Create() {
		pManager = IPCManagerJNI.IPCManager_Create();
	}

	/**
	 * 销毁本模块
	 * 
	 * @author jiayf
	 * @date Feb 11, 2015
	 */
	public void IPCManager_Destroy() {
		IPCManagerJNI.IPCManager_Destroy(pManager);
	}

	/**
	 * 注册回调监听
	 * 
	 * @param notify
	 *            回调接口
	 * @author jiayf
	 * @date Feb 11, 2015
	 */
	public void IPCManager_RegisterNetTransNotify(IPCManagerFn notify) {
		IPCManagerAdapter.setIPcManageListener(notify);
		IPCManagerJNI.IPCManager_RegisterNetTransNotify(pManager, notify);
	}

	/**
	 * 通用VDCP控制函数
	 * 
	 * @param cmd
	 *            　参见IPCManagerFn接口中IPC_VDCPCmd_开头的命令
	 * @param param
	 *            扩展参数
	 * @author jiayf
	 * @date Mar 10, 2015
	 */
	public boolean IPCManager_VDCP_CommRequest(int cmd, String param) {
		return IPCManagerJNI.IPCManager_VDCP_CommRequest(pManager, cmd, param);
	}

	/**
	 * 添加要下载的文件
	 * 
	 * @param filename
	 *            文件名称
	 * @param tag
	 *            唯一标识，自定义
	 * @author jiayf
	 * @date Mar 10, 2015
	 */
	public void IPCManager_AddDownloadFile(String filename, String tag, String savePath) {
		IPCManagerJNI.IPCManager_AddDownloadFile(pManager, filename, tag, savePath);
	}

	/**
	 * wifi热点状态切换 (通知IPC网络状态)
	 * 
	 * @param state
	 * @author jiayf
	 * @date Feb 11, 2015
	 */
	public void IPCManager_WifiStateChanged(int state) {
		IPCManagerJNI.IPCManager_WifiStateChanged(pManager, state);
	}

	/**
	 * 传递GPS信息
	 * 
	 * @param gpsinfo
	 *            gps信息，JSON串
	 * @author jiayf
	 * @date Mar 10, 2015
	 */
	public void IPCManager_SetGpsInfo(String gpsinfo) {
		IPCManagerJNI.IPCManager_SetGpsInfo(pManager, gpsinfo);
	}

	/**
	 * 设置IPC连接模式
	 * 
	 * @param mode
	 *            　IPCManagerFn中 IPCMgrMode_IPCDirect/IPCMgrMode_Mobnote
	 * @author jiayf
	 * @date Mar 16, 2015
	 */
	public void IPCManager_SetMode(int mode) {
		IPCManagerJNI.IPCManager_SetMode(pManager, mode);
	}
}
