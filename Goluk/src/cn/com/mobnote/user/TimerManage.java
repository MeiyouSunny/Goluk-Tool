package cn.com.mobnote.user;

import java.util.Timer;
import java.util.TimerTask;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.UserRegistActivity;
import cn.com.mobnote.golukmobile.UserRepwdActivity;

public class TimerManage {

	private GolukApplication mApp = null;
	private Timer mTimer = null;
	private int count = 0;
	public boolean flag = false;

	public TimerManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}

	public void timerCount() {
		flag = false;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				count++;
				if (count >= 60) {
					flag = true;
					UserRegistActivity.btnClick = false;
					UserRepwdActivity.repwdClick = false;
					mTimer.cancel();
				}
			}
		}, 0, 1000);
	}

}
