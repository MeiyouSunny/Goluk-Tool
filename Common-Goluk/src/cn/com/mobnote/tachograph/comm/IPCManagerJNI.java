package cn.com.mobnote.tachograph.comm;

public class IPCManagerJNI {

	public native static int IPCManager_Create();

	public native static void IPCManager_Destroy(int pIpcManager);

	public native static void IPCManager_RegisterNetTransNotify(int pIpcManager, IPCManagerFn notify);

	public native static void IPCManager_WifiStateChanged(int pIpcManager, int state);

	public native static void IPCManager_SetMode(int pIPcManager, int mode);

	public native static boolean IPCManager_VDCP_CommRequest(int pIpcManager, int cmd, String param);

	public native static void IPCManager_SetGpsInfo(int pIpcManager, String gpsinfo);

	public native static void IPCManager_AddDownloadFile(int pIpcManager, String filename, String tag, String savePath);

}
