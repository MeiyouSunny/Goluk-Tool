package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 多断配乐的单首音乐段
 * 
 * @author JIAN
 * 
 */
public class AudioMusicInfo implements Parcelable {

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	private String path, name;

	private int start, end;// 单位ms

	private int duration;

	public void setAll(AudioMusicInfo newsrc) {
		if (null != newsrc) {
			setPath(newsrc.getPath());
			setName(newsrc.getName());
			setStart(newsrc.getStart());
			setEnd(newsrc.getEnd());
			setDuration(newsrc.getDuration());
		} else {
			setPath("");
			setName("");
			setStart(0);
			setEnd(0);
			setDuration(0);
		}
	}

	public AudioMusicInfo(String path, String name, int start, int end,
			int duration) {
		setPath(path);
		setName(name);
		setStart(start);
		setEnd(end);
		setDuration(duration);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (null != o && o instanceof AudioMusicInfo) {
			AudioMusicInfo info = (AudioMusicInfo) o;
			return TextUtils.equals(info.getPath(), path)
					&& info.getStart() == getStart()
					&& info.getEnd() == getEnd()
					&& TextUtils.equals(info.getName(), getName());

		}
		return false;
	}

	@Override
	public String toString() {
		return "AudioMusicInfo [path=" + path + ", name=" + name + ", start="
				+ start + ", end=" + end + ",duration" + duration + "]";
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(start);
		dest.writeInt(end);
		dest.writeInt(duration);
		dest.writeString(path);
		dest.writeString(name);
	}

	public static final Parcelable.Creator<AudioMusicInfo> CREATOR = new Creator<AudioMusicInfo>() {

		@Override
		public AudioMusicInfo[] newArray(int size) {

			return new AudioMusicInfo[size];

		}

		@Override
		public AudioMusicInfo createFromParcel(Parcel source) {
			return new AudioMusicInfo(source);
		}
	};

	private AudioMusicInfo(Parcel source) {
		setStart(source.readInt());
		setEnd(source.readInt());
		setDuration(source.readInt());
		setPath(source.readString());
		setName(source.readString());
	}

}
