package cn.com.tiros.api;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public class Timerr {

	private long pTimer;
	private Handler mHandler = null;

	public Timerr(){
		pTimer = 0;
		mHandler = null;
	}
	public void sys_tmrcreate(long t) {
		pTimer = t;
	}

	public void sys_tmrdestroy() {
		sys_tmrcancel();
		mHandler = null;
		pTimer = 0;
	}

	public void sys_tmrstart(final int dwMSecs) {
		if(mHandler == null){
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch(msg.what){
					case 0:
						sys_tmrcancel();
						sys_timerEvent(pTimer);
						break;
					}
				};
			};
		}
		mHandler.sendEmptyMessageAtTime(0, SystemClock.uptimeMillis() + dwMSecs);
	}

	public boolean sys_tmrisbusy() {
		if (mHandler == null || !mHandler.hasMessages(0)) {
			return false;
		}
		return true;
	}

	public void sys_tmrcancel() {

		if(mHandler != null){
			mHandler.removeMessages(0);
		}
	}

	public static native void sys_timerEvent(long timer);

}
