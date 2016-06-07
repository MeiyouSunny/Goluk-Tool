# Android RtspPlayerView Integration Guide
 
####Functions:

`void setDataSource(String urlString);`  

		Discussion
		set player`s data source
		@param urlString The RTSP URL String , you can get the url by [GolukIPCUtils.getRtmpPreviewUrl()].
		  
`start();`

		Discussion
	 	start to play video
`pause();`

		Discussion
		pause
`canPause();` 

		Discussion
		canPause
`stopPlayback();`

		Discussion
		stop play
`setPlayerListener(new RtspPlayerLisener());`
		
		Discussion
		add player callback
`isPlaying();`

		Discussion
		isplaying
`cleanUp();`

		Discussion
		clean play sources，should call in method [onDestroy()] 
`setAudioMute(boolean isMute);`

		Discussion
		set audio mute

####RtspPlayerLisener: 
- onPlayerPrepared(RtspPlayerView var1);  
- onPlayerBegin(RtspPlayerView var1);
- onPlayerError(RtspPlayerView var1, int var2, int var3, String var4); 
- onPlayBuffering(RtspPlayerView var1, boolean var2);
- onPlayerCompletion(RtspPlayerView var1);
- onGetCurrentPosition(RtspPlayerView var1, int var2);

####Example:  

**1. Init the RTSP Player**

```
	mRtspplayer = (RtspPlyerView) findViewById(R.id.restplayer);
	mRtspPlayerView.setZOrderMediaOverlay(true);
	mRtspPlayerView.setBufferTime(1000);
	mRtspPlayerView.setConnectionTimeout(30000);
	mRtspplayer.setDataSource(GolukIPCUtils.getRtmpPreviewUrl());
	mRtspplayer.setPlayerListener(mPlayerListener);
```
**2. Play video**

```
	mRtspplayer.start();
```
**3. Set mute**

```
	mRtspPlayerView.setAudioMute(true);
```
**4. Stop play video**

```
	if (mRtspPlayerView.isPlaying()) {
		mRtspPlayerView.stopPlayback();	
	}
```
**5. Destroy**

```
	mRtspPlayerView.cleanUp();
```
