package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 多端配乐的信息
 * 
 * @author JIAN
 * 
 */
public class MultiAudioInfo implements Parcelable {

    @Override
    public String toString() {
	return "AudioInfo [  Start=" + Start + ", End=" + End + ", audioMusic="
		+ ((audioMusic == null) ? "null" : audioMusic.toString()) + "]";
    }

    public int getStart() {
	return Start;
    }

    public void setStart(int start) {
	Start = start;
    }

    public int getEnd() {
	return End;
    }

    public void setEnd(int end) {
	End = end;
    }

    public AudioMusicInfo getAudioMusic() {
	return audioMusic;
    }

    public void setAudioMusic(AudioMusicInfo audioMusic) {
	this.audioMusic = audioMusic;
    }

    private int Start, End, mixFactor;// 相对于整段视频的起始位置

    public int getMixFactor() {
	return mixFactor;
    }

    public void setMixFactor(int mixFactor) {
	this.mixFactor = mixFactor;
    }

    private AudioMusicInfo audioMusic;

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

	dest.writeInt(Start);
	dest.writeInt(End);
	dest.writeParcelable(audioMusic, 0);
	dest.writeInt(mixFactor);

    }

    public MultiAudioInfo(int start) {
	setStart(start);
    }

    public MultiAudioInfo(Parcel in) {
	setStart(in.readInt());
	setEnd(in.readInt());
	setAudioMusic((AudioMusicInfo) in.readParcelable(AudioMusicInfo.class
		.getClassLoader()));
	setMixFactor(in.readInt());

    }

    @Override
    public boolean equals(Object o) {

	if (null != o && o instanceof MultiAudioInfo) {
	    MultiAudioInfo info = (MultiAudioInfo) o;
	    return getStart() == info.getStart() && getEnd() == info.getEnd()
		    && audioMusic.equals(info.getAudioMusic())
		    && getMixFactor() == info.getMixFactor();

	}

	return false;
    }

    public static final Parcelable.Creator<MultiAudioInfo> CREATOR = new Parcelable.Creator<MultiAudioInfo>() {

	public MultiAudioInfo createFromParcel(Parcel in) {

	    return new MultiAudioInfo(in);
	}

	public MultiAudioInfo[] newArray(int size) {
	    return new MultiAudioInfo[size];
	}
    };

}
