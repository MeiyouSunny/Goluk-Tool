package com.rd.veuisdk.fragment;

import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.utils.Utils;

/**
 * 记录录音的时间片
 * 
 */
public class AudioInfo {

	/** 配乐ｉｄ */
	private int audioInfoId = 0;

	/** 开始录音时间 单位ms */
	private int startRecordTime;

	/** 结束录音时间  单位ms*/
	private int endRecordTime;

	private int seekBarValue = 50;

	private String audiopath;
	private Music audio;

	public AudioInfo(int id, String path) {

		this.audioInfoId = id;
		this.audiopath = path;
	}

	public String getPath() {
		return audiopath;
	}

	public int getStartRecordTime() {
		return startRecordTime;
	}

	public void setStartRecordTime(int startRecordTime) {
		this.startRecordTime = startRecordTime;
	}

	public int getEndRecordTime() {
		return endRecordTime;
	}

	public void setEndRecordTime(int endRecordTime) {
		this.endRecordTime = endRecordTime;
	}

	public int getAudioInfoId() {
		return audioInfoId;
	}

	public int getSeekBarValue() {
		return seekBarValue;
	}

	public void setSeekBarValue(int seekBarValue) {
		this.seekBarValue = seekBarValue;
		if (null != audio) {
			audio.setMixFactor(seekBarValue);
		}
	}

	@Override
	public String toString() {
		// if (null != audio) {
		// logInfo();
		// }
		return "AudioInfo [audioInfoId=" + audioInfoId + ", startRecordTime="
				+ startRecordTime + ", endRecordTime=" + endRecordTime + "]";
	}

	public Music getAudio() {
		if (null == audio)
			createAudioObject();
		// logInfo();
		return audio;
	}

	// private void logInfo() {
	// // Log.d(TAG,
	// // "getAudio ->" + audio.getTimelineFrom() + "...."
	// // + audio.getTimelineTo() + "....相对音频本身.."
	// // + audio.getTimeStart() + ".." + audio.getTimeEnd());
	// }

	private void createAudioObject() {
		audio = VirtualVideo.createMusic(audiopath);
		int duration = getEndRecordTime() - getStartRecordTime();
		if (Utils.s2ms(audio.getIntrinsicDuration()) < duration) {
			duration = Utils.s2ms(audio.getIntrinsicDuration());
		}
		audio.setTimeRange(0, Utils.ms2s(duration));
		audio.setTimelineRange(Utils.ms2s(getStartRecordTime()), Utils.ms2s(getEndRecordTime()));
		audio.setMixFactor(getSeekBarValue());
	}

	public void recycle() {
		if (null != audio) {
			audio = null;
		}

	}

	public AudioInfo(AudioInfo info) {
		this.audioInfoId = info.audioInfoId;
		this.startRecordTime = info.startRecordTime;
		this.endRecordTime = info.endRecordTime;
		this.seekBarValue = info.seekBarValue;
		this.audiopath = info.audiopath;
	}

	@Override
	public boolean equals(Object o) {
		if (null != o && o instanceof AudioInfo) {
			AudioInfo info = (AudioInfo) o;
			if (info.getPath().equals(getPath())
					&& info.getStartRecordTime() == getStartRecordTime()
					&& info.getEndRecordTime() == getEndRecordTime()
					&& getSeekBarValue() == info.getSeekBarValue()) {
				return true;
			}
		}
		return false;
	}

	public void offset(float offset) {
		startRecordTime += offset;
		endRecordTime += offset;
		audio = null;
	}

}
