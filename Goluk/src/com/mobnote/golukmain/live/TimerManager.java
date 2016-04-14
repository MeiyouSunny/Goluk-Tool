package com.mobnote.golukmain.live;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import cn.com.tiros.debug.GolukDebugUtils;

public class TimerManager {

	public static final int RESULT_FINISH = 1;

	private Timer mRecordTimer = null;
	private TimerTask task = null;
	private boolean isRunning = false;

	private ITimerManagerFn mListener = null;

	private int mCount = 0;
	private int mCurrent = 0;

	private int mFunction;

	private boolean isPause = false;
	private boolean isCountDown = false;

	public TimerManager(int function) {
		mFunction = function;
	}

	public interface ITimerManagerFn {
		public void CallBack_timer(int function, int result, int current);
	}

	public void setListener(ITimerManagerFn listener) {
		mListener = listener;
	}

	/** timer暂停 */
	public void timerPause() {
		isPause = true;
		cancelTimer();
	}

	public void timerResume() {
		if (isPause) {
			startTimer(mCurrent, isCountDown);
		}
		isPause = false;
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (100 == msg.what) {
				final int result = msg.arg1;
				final int curr = msg.arg2;
				if (null != mListener) {
					mListener.CallBack_timer(mFunction, result, curr);
				}
			}
			super.handleMessage(msg);
		}

	};

	// countdown 是否倒计时 true/false
	public void startTimer(final int time, final boolean countdown) {
		// 先取消
		cancelTimer();
		isCountDown = countdown;
		mCount = time;
		mCurrent = time;

		GolukDebugUtils.i("", "Timer-------time:" + time + "  countdown:" + countdown);

		isRunning = true;
		mRecordTimer = new Timer();
		task = new TimerTask() {
			public void run() {
				if (countdown) {
					// 倒计时
					mCurrent--;
				} else {
					mCurrent++;
				}

				Message msg = new Message();
				msg.what = 100;
				msg.arg1 = 0;

				if (countdown) {
					if (mCurrent <= 0) {
						cancelTimer();
						msg.arg1 = RESULT_FINISH;
					}
				} else {
					if (mCurrent >= mCount) {
						cancelTimer();
						msg.arg1 = RESULT_FINISH;
					}
				}

				msg.arg2 = mCurrent;

				mHandler.sendMessage(msg);
			}
		};
		mRecordTimer.schedule(task, 1000, 1000);
	}

	public void cancelTimer() {
		if (null != mRecordTimer) {
			mRecordTimer.cancel();
			mRecordTimer.purge();
			mRecordTimer = null;
		}
	}

}
