package com.mobnote.golukmain.livevideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import com.mobnote.golukmain.carrecorder.PreferencesReader;
import com.mobnote.golukmain.carrecorder.RecorderMsgReceiverBase;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.ResultConstants;

public class LiveOperateCarrecord implements ILiveOperateFn {

	private Context mContext = null;
	private String liveVid = null;
	/** 直播开启标志 */
	private boolean isStartLive = false;
	private boolean isSucessBind = false;
	private ILiveFnAdapter mListener = null;
	private boolean isStart = false;

	public LiveOperateCarrecord(Context context, ILiveFnAdapter listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	public boolean startLive(final StartLiveBean bean) {
		if (isStartLive) {
			return true;
		}
		if (CarRecorderManager.isRTSPLiving()) {
			// 正在直播，不可以开始
			// liveUploadVideoFailed();
			sendResult(ILiveFnAdapter.STATE_FAILED);
			return false;
		}
		try {
			SharedPreferences sp = mContext.getSharedPreferences("CarRecorderPreferaces", Context.MODE_PRIVATE);
			sp.edit().putString("url_live", bean.url).apply();
			sp.edit().commit();
			CarRecorderManager.updateLiveConfiguration(new PreferencesReader(mContext, false).getConfig());
			CarRecorderManager.setLiveMute(bean.isVoice);
			CarRecorderManager.startRTSPLive();
			isStartLive = true;
		} catch (RecorderStateException e) {
			e.printStackTrace();
			// liveUploadVideoFailed();
			sendResult(ILiveFnAdapter.STATE_FAILED);
		}
		return false;
	}

	@Override
	public void stopLive() {
		try {
			isStartLive = false;
			CarRecorderManager.stopRTSPLive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResult(int state) {
		if (null != mListener) {
			mListener.Live_CallBack(state);
		}
	}

	@Override
	public void onResume() {
		if (!isSucessBind) {
			mContext.registerReceiver(managerReceiver, new IntentFilter(CarRecorderManager.ACTION_RECORDER_MESSAGE));
		}
	}

	/**
	 * 响应视频Manager消息
	 */
	private BroadcastReceiver managerReceiver = new RecorderMsgReceiverBase() {
		@Override
		public void onManagerBind(Context context, int nResult, String strResultInfo) {
		}

		public void onLiveRecordBegin(Context context, int nResult, String strResultInfo) {
			if (nResult >= ResultConstants.SUCCESS) {
				sendResult(ILiveFnAdapter.STATE_SUCCESS);
			} else {
				// 视频录制上传失败
				// liveUploadVideoFailed();
				sendResult(ILiveFnAdapter.STATE_FAILED);
			}
		}

		@Override
		public void onLiveRecordFailed(Context context, int nResult, String strResultInfo) {
			sendResult(ILiveFnAdapter.STATE_FAILED);
			// liveUploadVideoFailed();
		}
	};

	@Override
	public void exit() {
		if (isSucessBind) {
			mContext.unregisterReceiver(managerReceiver);
			isSucessBind = false;
		}
	}

	@Override
	public void onStart() {
		try {
			if (!isStart) {
				isStart = true;
				CarRecorderManager.onStartRTSP(mContext);
			}
		} catch (RecorderStateException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean liveState() {
		// TODO Auto-generated method stub
		return false;
	}

}
