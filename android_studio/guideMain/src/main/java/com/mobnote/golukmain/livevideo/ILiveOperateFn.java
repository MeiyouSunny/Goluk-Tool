package com.mobnote.golukmain.livevideo;

import com.mobnote.golukmain.livevideo.bean.StartLiveBean;

public interface ILiveOperateFn {
	
	public void onStart();

	public void onResume();

	public boolean startLive(StartLiveBean bean);

	public void stopLive();

	public void exit();
	
	public boolean liveState();

	public int getZhugeErrorCode();

}
