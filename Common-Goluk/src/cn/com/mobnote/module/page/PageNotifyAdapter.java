package cn.com.mobnote.module.page;


public class PageNotifyAdapter {

	private static IPageNotifyFn mFn = null;

	public static void setNotify(IPageNotifyFn fn) {
		mFn = fn;
	}

	public static void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
		String data1 = "";
		String data2 = "";
		if (param1 instanceof String) {
			data1 = (String) param1;
		}

		if (param2 instanceof String) {
			data2 = (String) param2;
		}
//		LogUtil.e("jyf", "jyf-----pageNotifyCallBack------22---type:" + type + " success:" + success + " data1:"
//				+ data1 + " data2:" + data2);
		if (null != mFn) {
			mFn.pageNotifyCallBack(type, success, param1, param2);
		}
	}

}
