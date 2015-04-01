package cn.com.mobnote.module.talk;

public class TalkNotifyAdapter {

	/** 回调对象 */
	private static ITalkFn mFn = null;

	public static void setNotify(ITalkFn fn) {
		mFn = fn;
	}

	public static void TalkNotifyCallBack(int type, String data) {
		if (null != mFn) {
			mFn.TalkNotifyCallBack(type, data);
		}
	}

}
