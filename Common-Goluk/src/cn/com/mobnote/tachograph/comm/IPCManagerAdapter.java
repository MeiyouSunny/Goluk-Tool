package cn.com.mobnote.tachograph.comm;

//import cn.com.tiros.utils.LogUtil;

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
	public static void setIPcManageListener(IPCManagerFn _fn) {
		fn = _fn;
	}

	public static void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
//		LogUtil.e("jyf", "IPCManage_CallBack--------------IPCManagerAdapter-11---event:" + event + "	msg:");
		if (null == fn) {
			return;
		}

//		LogUtil.e("jyf", "IPCManage_CallBack--------------IPCManagerAdapter-22---event:" + event + "	msg:");
		fn.IPCManage_CallBack(event, msg, param1, param2);
	}

}
