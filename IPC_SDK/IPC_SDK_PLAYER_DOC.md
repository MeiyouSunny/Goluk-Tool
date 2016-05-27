##Goluk Doc##

###video preview###

实时预览视频采用播放器:RtspPlayerView  
####RtspPlyerView:

- **setDataSource(String url);** *//设置播放源,获取播放源参见PICUtils.getRtmpPreviewUrl()方法*
- **start();** *//开始播放*
- **stopPlayback();** *//停止播放* 
- **setPlayerListener(new RtspPlayerLisener());** *//添加播放监听*
- **isPlaying();** *//判断当前是否正在播放*
- **cleanUp();** *清空播放器，在onDestroy()里调用*  

####RtspPlayerLisener: 
- **onPlayerPrepared(RtspPlayerView var1);**  
- **onPlayerBegin(RtspPlayerView var1);**
- **onPlayerError(RtspPlayerView var1, int var2, int var3, String var4);**  
- **onPlayBuffering(RtspPlayerView var1, boolean var2);**
- **onPlayerCompletion(RtspPlayerView var1);**  
- **onGetCurrentPosition(RtspPlayerView var1, int var2);**  

####example:  
<pre>
RtspPlyerView mRtspplayer = findViewById(R.id.restplayer);
mRtspplayer.setDataSource(PICUtils.getRtmpPreviewUrl());
mRtspplayer.setPlayerListener(mPlayerListener);
mRtspplayer.start();
</pre>
