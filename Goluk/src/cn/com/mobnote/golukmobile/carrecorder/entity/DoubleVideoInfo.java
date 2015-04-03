package cn.com.mobnote.golukmobile.carrecorder.entity;


public class DoubleVideoInfo {
	private VideoInfo videoInfo1;
	private VideoInfo videoInfo2;
	
	public DoubleVideoInfo(VideoInfo _videoInfo1, VideoInfo _videoInfo2){
		videoInfo1 = _videoInfo1;
		videoInfo2 = _videoInfo2;
	}
	
	public VideoInfo getVideoInfo1(){
		return videoInfo1;
	}
	
	public VideoInfo getVideoInfo2(){
		return videoInfo2;
	}

}
