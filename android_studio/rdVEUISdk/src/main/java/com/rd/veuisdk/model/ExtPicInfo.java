package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ExtPicInfo implements Parcelable {

	public int getTxColor() {
		return txColor;
	}

	public int getBgColor() {
		return bgColor;
	}

	public int getTxSide() {
		return txSide;
	}

	public String getText() {
		return text;
	}

	private int txColor, bgColor, txSide;
	private int ttfposition, bgPosition, txColorPosition;

	public int getTtfposition() {
		return ttfposition;
	}

	public void setTtfposition(int ttfposition) {
		this.ttfposition = ttfposition;
	}

	public int getBgPosition() {
		return bgPosition;
	}

	public void setBgPosition(int bgPosition) {
		this.bgPosition = bgPosition;
	}

	public int getTxColorPosition() {
		return txColorPosition;
	}

	public void setTxColorPosition(int txColorPosition) {
		this.txColorPosition = txColorPosition;
	}

	public void setTxColor(int txColor) {
		this.txColor = txColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public void setTxSide(int txSide) {
		this.txSide = txSide;
	}

	public void setText(String text) {
		this.text = text;
	}

	private String text, ttf;

	public String getTtf() {
		return ttf;
	}

	public void setTtf(String ttf) {
		this.ttf = ttf;
	}

	public ExtPicInfo(int bg, int tx, String text, String ttf, int txSide,
			int ttfPosition, int bgPosition, int txColorPosition) {
		setBgColor(bg);
		setTxColor(tx);
		setText(text);
		setTtf(ttf);
		setTxSide(txSide);
		setTtfposition(ttfPosition);
		setBgPosition(bgPosition);
		setTxColorPosition(txColorPosition);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(bgColor);
		dest.writeInt(txColor);
		dest.writeString(text);
		dest.writeString(ttf);
		dest.writeInt(txSide);
		dest.writeInt(ttfposition);
		dest.writeInt(bgPosition);
		dest.writeInt(txColorPosition);
	}

	public static final Parcelable.Creator<ExtPicInfo> CREATOR = new Creator<ExtPicInfo>() {
		@Override
		public ExtPicInfo createFromParcel(Parcel source) {
			return new ExtPicInfo(source.readInt(), source.readInt(),
					source.readString(), source.readString(), source.readInt(),
					source.readInt(), source.readInt(), source.readInt());
		}

		@Override
		public ExtPicInfo[] newArray(int size) {
			return new ExtPicInfo[size];
		}
	};

}
