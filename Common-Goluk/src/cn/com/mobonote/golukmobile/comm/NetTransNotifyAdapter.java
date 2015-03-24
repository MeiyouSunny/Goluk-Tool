package cn.com.mobonote.golukmobile.comm;

public class NetTransNotifyAdapter {
	private static INetTransNotifyFn mFn = null;

	public static void setNetTransNotify(INetTransNotifyFn fn) {
		mFn = fn;
	}

	public static void netTransNotifyCallBack(int event, int msg, Object param1, Object param2) {
		if (null != mFn) {
//			mFn.netTransNotifyCallBack(event, msg, param1, param2);
		}
	}

}
