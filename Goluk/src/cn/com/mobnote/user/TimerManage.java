package cn.com.mobnote.user;

import java.util.Timer;
import java.util.TimerTask;

import cn.com.mobnote.application.GolukApplication;

public class TimerManage {

	private GolukApplication mApp = null;
	private Timer mTimer = null;
	private int count = 0;
	public boolean flag = true;

	public TimerManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}

	public void timerCount() {
		timerCancel();
		flag = false;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				count++;
				if (count >= 60) {
					timerCancel();
				}
			}
		}, 0, 1000);
	}
	
	public void timerCancel() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			flag = true;
			count = 0;
		}
	}

}
