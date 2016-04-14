package com.mobnote.user;

import com.mobnote.golukmain.R;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Button;

public class CountDownButtonHelper {

	/**
	 * 获取验证码的帮助类 btn 获取验证码按钮 maxTime 获取验证码的最大时间 interval 最大时间间隔
	 */
	/** 倒计时的timer**/
	public CountDownTimer timer;
	/**倒计时回调时的接口**/
	private OnFinishListener listener;
	/**点击按钮倒计时**/
	private Button btn;

	public CountDownButtonHelper(final Context context, final Button btn, final String defaultString, int maxTime, int interval) {
		this.btn = btn;

		/**
		 * 由于CountDownTimer并不是准确计时，在onTick方法调用的时候，time会有1-10ms左右的误差，
		 * 这会导致最后一秒不会调用onTick() 因此，设置间隔的时候，默认减去了10ms，从而减去误差。
		 * 经过以上的微调，最后一秒的显示时间会由于10ms延迟的积累，导致显示时间比1s长max*10ms的时间，其他时间的显示正常,总时间正常
		 */
		timer = new CountDownTimer(maxTime * 1000, interval * 1000 - 10) {
			/**
			 * 倒计时开始后回调的函数
			 */
			@Override
			public void onTick(long time) {
				// TODO Auto-generated method stub
				// 第一次调用会有1-10ms的误差，因此需要+15ms，防止第一个数不显示，第二个数显示2s
				btn.setText(defaultString + context.getResources().getString(R.string.str_bracket_left)
						+ ((time + 15) / 1000) + context.getResources().getString(R.string.str_seconds)
						+ context.getResources().getString(R.string.str_bracket_rigth));
				btn.setEnabled(false);
			}
			/**
			 * onFinsh（）倒计时结束之后回调的函数
			 */
			@Override
			public void onFinish() {
				btn.setEnabled(true);
				if (listener != null) {
					listener.finish();
				}
			}
		};
	}

	/**
	 * 开始倒计时
	 */
	public void start() {
		btn.setEnabled(false);
		timer.start();
	}

	/**
	 * 设置倒计时结束的监听器
	 * 
	 * @param listener
	 */
	public void setOnFinishListener(OnFinishListener listener) {
		this.listener = listener;
	}

	/**
	 * 计时结束的回调接口
	 * 
	 */
	public interface OnFinishListener {
		public void finish();
	}

}
