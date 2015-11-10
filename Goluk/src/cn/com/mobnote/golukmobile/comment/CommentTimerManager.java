package cn.com.mobnote.golukmobile.comment;

import java.util.Timer;
import java.util.TimerTask;

public class CommentTimerManager {

	private Timer mTimer = null;
	private TimerTask task = null;
	private boolean isStarting = false;
	private ICommentTimerFn mFn = null;

	private static CommentTimerManager mInstance = new CommentTimerManager();

	public static CommentTimerManager getInstance() {
		return mInstance;
	}

	public void setListener(ICommentTimerFn fn) {
		mFn = fn;
	}

	private void sendCallBackData(int event) {
		if (null != mFn) {
			mFn.CallBack_timer(event);
		}
	}

	public interface ICommentTimerFn {
		public void CallBack_timer(int event);
	}

	public boolean getIsStarting() {
		return isStarting;
	}

	public void cancelTimer() {
		if (null != mTimer) {
			mTimer.cancel();
			mTimer = null;
			isStarting = false;
		}
	}

	public void start(int seconds) {
		cancelTimer();
		isStarting = true;
		mTimer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				sendCallBackData(0);
				cancelTimer();
			}

		};
		mTimer.schedule(task, seconds * 1000 * 30);
	}

}
