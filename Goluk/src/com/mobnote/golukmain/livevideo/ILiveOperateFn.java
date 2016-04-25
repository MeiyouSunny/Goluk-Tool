package com.mobnote.golukmain.livevideo;

public interface ILiveOperateFn {
	
	public void onStart();

	public void onResume();

	public boolean startLive(StartLiveBean bean);

	public void stopLive();

	public void exit();
	
	public boolean liveState();

}
