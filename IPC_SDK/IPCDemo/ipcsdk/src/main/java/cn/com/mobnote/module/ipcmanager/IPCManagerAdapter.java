package cn.com.mobnote.module.ipcmanager;

public class IPCManagerAdapter {

	/** 回调接口实例 */
	private static IPCManagerFn fn = null;

	/**
	 * 设置接口回调
	 * 
	 * @param _fn
	 *            回调接口
	 * @author jiayf
	 * @date Feb 11, 2015
	 */
	public static void setIPCManageListener(IPCManagerFn _fn) {
		fn = _fn;
	}

	public static void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if(param2 != null){
			//Log.i("ipc_callback", "event: " + event + " msg: " + msg + " param1: " + param1 + " param2: " + param2.toString());
		}else{
			//Log.i("ipc_callback", "event: " + event + " msg: " + msg + " param1: " + param1 + " param2: null");
		}
		if (null == fn) {
			return;
		}
		String data = null;
		if (param2 instanceof String) {
			data = (String)param2;
		}
		fn.IPCManage_CallBack(event, msg, param1, param2);
	}

}
