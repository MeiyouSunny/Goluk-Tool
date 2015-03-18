package cn.com.mobonote.golukmobile.comm;

public class PageNotifyAdapter {

	private static IPageNotifyFn mFn = null;

	public static void setNotify(IPageNotifyFn fn) {
		mFn = fn;
	}

	public static void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
		if (null != mFn) {
			mFn.pageNotifyCallBack(type, success, param1, param2);
		}
	}

}
