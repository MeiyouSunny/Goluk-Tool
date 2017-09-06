package com.rd.veuisdk.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.rd.vecore.models.SubtitleObject;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.utils.AppConfiguration;

import java.util.ArrayList;

public class WordInfo implements Parcelable {

	/**
	 * start,end 字幕的起止时间，毫秒 x,y; 表示字幕相对于视频的位置 0<x<1,0<y<1;picpath
	 * 生成的字幕图片的位置sdcard/17rd/temp/word_***.png
	 */
	private int width = AppConfiguration.DEFAULT_WIDTH,
			height = AppConfiguration.DEFAULT_WIDTH;

	private double widthx = 0.5, heighty = 0.5; // 0 <widthx ,heighty <1
	// //宽高占当前预览区域宽高的比列
	private Double left = 0.2, top = 0.5;// (0<=left,top<1)控件距离字幕区域范围(left ,top
	// )
	private int isPreview = 1; // 0正式字幕。1，预览的字幕
	// 单位px TypedValue.COMPLEX_UNIT_PX
	private int textSize = 60;

	private float rotateAngle = 0.0f;// 旋转角度
	private int textColor = Color.WHITE;// 字体颜色
	private String ttfLocalPath = null;// 字体ttf from file
	private float disf = 1, mZoomFactor = 1;// 缩放比列
	private long start, end;// 起止位置单位ms
	private String picpath;
	/**
	 * 字幕背景图
	 */
	private String bgpicpath;
	private String text = "";
	private int id = -1;
	private int shadowColor = 0;
	private Double realx = 0.0, realy = 0.0;

	public WordInfo clone() {
		return new WordInfo(this);
	}

	public void set(WordInfo info) {
		this.width = info.width;
		this.height = info.height;
		this.widthx = info.widthx;
		this.heighty = info.heighty;
		this.left = info.left;
		this.top = info.top;
		this.isPreview = info.isPreview;
		this.textSize = info.textSize;
		this.rotateAngle = info.rotateAngle;
		this.textColor = info.textColor;
		this.ttfLocalPath = info.ttfLocalPath;
		this.disf = info.disf;
		this.start = info.start;
		this.end = info.end;
		this.picpath = info.picpath;
		this.bgpicpath = info.bgpicpath;
		this.text = info.text;
		this.id = info.id;
		this.centerxy = info.centerxy;
		this.realx = info.realx;
		this.realy = info.realy;
		this.shadowColor = info.shadowColor;
		this.styleId = info.styleId;
	}

	public int getShadowColor() {
		return shadowColor;
	}

	public void setShadowColor(int shadowColor) {
		this.shadowColor = shadowColor;
		setChanged();
	}

	private int styleId = SubUtils.DEFAULT_ID;

	public float[] getCenterxy() {
		return centerxy;
	}

	public void setCenterxy(float[] centerxy) {
		this.centerxy = centerxy;
		setChanged();
	}

	/**
	 * 主题片头
	 * 
	 * @param offTime
	 *            偏移时间量 可为正负 , +向后新增主题(主题时间变长),-向前清除主题(主题片头时间变短)
	 */
	public void offTimeLine(int offTime) {

		setStart(getStart() + offTime);
		setEnd(getEnd() + offTime);

	}

	private float[] centerxy = new float[] { 0.5f, 0.5f }; // 图片旋转中心点坐标在x，y的比例

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
		setChanged();
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
		setChanged();
	}

	public String getPicpath() {
		return picpath;
	}

	public void setPicpath(String picpath) {
		this.picpath = picpath;
	}

	public String getBgPicpath() {
		return bgpicpath;
	}

	public void setBgPicpath(String bgpicpath) {
		this.bgpicpath = bgpicpath;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(Double left) {
		this.left = left;
	}

	public double getTop() {
		return top;
	}

	public void setTop(Double top) {
		this.top = top;
	}

	public Double getRealx() {
		return realx;
	}

	public void setRealx(Double realx) {
		this.realx = realx;
	}

	public Double getRealy() {
		return realy;
	}

	public void setRealy(Double realy) {
		this.realy = realy;
		setChanged();
	}

	public int getIsPreview() {
		return isPreview;
	}

	public void setRotateAngle(float rotateAngle) {
		this.rotateAngle = rotateAngle;
		setChanged();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public double getWidthx() {
		return widthx;
	}

	public void setWidthx(double widthx) {
		this.widthx = widthx;

	}

	public double getHeighty() {
		return heighty;
	}

	public void setHeighty(double heighty) {
		this.heighty = heighty;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setStyleId(int _styleId) {
		styleId = _styleId;
		setChanged();
	}

	public int getStyleId() {
		return styleId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		setChanged();
	}

	public int isPreview() {
		return isPreview;
	}

	public void setIsPreview(int ispreview) {
		this.isPreview = ispreview;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
		setChanged();
	}

	public static final Parcelable.Creator<WordInfo> CREATOR = new Parcelable.Creator<WordInfo>() {

		public WordInfo createFromParcel(Parcel in) {

			return new WordInfo(in);
		}

		public WordInfo[] newArray(int size) {
			return new WordInfo[size];
		}
	};

	public WordInfo() {
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(text);
		arg0.writeString(picpath);
		arg0.writeString(bgpicpath);
		arg0.writeLong(start);
		arg0.writeLong(end);
		arg0.writeInt(width);
		arg0.writeInt(height);
		arg0.writeDouble(left);
		arg0.writeDouble(top);
		arg0.writeDouble(realx);
		arg0.writeDouble(realy);
		arg0.writeInt(id);
		arg0.writeInt(isPreview);
		arg0.writeInt(textSize);
		arg0.writeDouble(widthx);
		arg0.writeDouble(heighty);
		arg0.writeFloat(rotateAngle);
		arg0.writeInt(textColor);
		arg0.writeString(ttfLocalPath);
		arg0.writeFloat(disf);
		arg0.writeFloatArray(centerxy);
		arg0.writeInt(shadowColor);
		arg0.writeInt(styleId);
		// arg0.writeParcelableArray(subeffs.toArray(SubtitleEffectsObject[]),
		// 0);
	}

	@Override
	public boolean equals(Object o) {
		if (null != o && (o instanceof WordInfo)) {
			WordInfo info = (WordInfo) o;
			return TextUtils.equals(getText(), info.getText())

					&& getStart() == info.getStart()
					&& getEnd() == info.getEnd()
					&& getId() == info.getId()
					&& TextUtils.equals(getTtfLocalPath(),
							info.getTtfLocalPath())
					&& getTextSize() == info.getTextSize()
					&& getRotateAngle() == info.getRotateAngle()
					&& getTextColor() == info.getTextColor()
					&& getDisf() == info.getDisf()
					&& centerxy == info.getCenterxy()
					&& shadowColor == info.getShadowColor()
					&& styleId == info.getStyleId();

		} else {
			return false;
		}
	}

	private WordInfo(Parcel in) {
		text = in.readString();
		picpath = in.readString();
		bgpicpath = in.readString();
		start = in.readLong();
		end = in.readLong();
		width = in.readInt();
		height = in.readInt();
		left = in.readDouble();
		top = in.readDouble();
		realx = in.readDouble();
		realy = in.readDouble();
		id = in.readInt();
		isPreview = in.readInt();
		textSize = in.readInt();
		widthx = in.readDouble();
		heighty = in.readDouble();
		rotateAngle = in.readFloat();
		textColor = in.readInt();
		ttfLocalPath = in.readString();
		disf = in.readFloat();
		in.readFloatArray(centerxy);
		shadowColor = in.readInt();
		styleId = in.readInt();

	}

	public WordInfo(WordInfo info) {
		this.width = info.width;
		this.height = info.height;
		this.widthx = info.widthx;
		this.heighty = info.heighty;
		this.left = info.left;
		this.top = info.top;
		this.isPreview = info.isPreview;
		this.textSize = info.textSize;
		this.rotateAngle = info.rotateAngle;
		this.textColor = info.textColor;
		this.ttfLocalPath = info.ttfLocalPath;
		this.disf = info.disf;
		this.start = info.start;
		this.end = info.end;
		this.picpath = info.picpath;
		this.bgpicpath = info.bgpicpath;
		this.text = info.text;
		this.id = info.id;
		this.centerxy = info.centerxy;
		this.realx = info.realx;
		this.realy = info.realy;
		this.shadowColor = info.shadowColor;
		this.styleId = info.styleId;

		ArrayList<SubtitleObject> temps = info.getList();

		int len = temps.size();
		for (int i = 0; i < len; i++) {
			this.subeffs.add(new SubtitleObject(temps.get(i)));
		}

		// Log.d(TAG, "new copy..."+temps.size());

		this.changed = info.IsChanged();
	}

	public float getRotateAngle() {
		return rotateAngle;
	}

	public void setRotateAngel(float rotateAngle) {
		this.rotateAngle = rotateAngle;
		setChanged();
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
		setChanged();
	}

	public String getTtfLocalPath() {
		return ttfLocalPath;
	}

	public void setTtfLocalPath(String ttfLocalPath) {
		this.ttfLocalPath = ttfLocalPath;
		setChanged();
	}

	public float getDisf() {
		return disf;
	}

	public void setDisf(float disf) {
		this.disf = disf;
		setChanged();
	}

	public float getZoomFactor() {
		return mZoomFactor;
	}

	public void setZoomFactor(float disf) {
		this.mZoomFactor = disf;
		setChanged();
	}

	private ArrayList<SubtitleObject> subeffs = new ArrayList<SubtitleObject>();

	public ArrayList<SubtitleObject> getList() {
		return subeffs;
	}

	public void recycle() {
		subeffs.clear();
	}

	private final String TAG = "wordInfo";

	public void addSubObject(SubtitleObject subobj) {

		subeffs.add(subobj);

	}

	private boolean changed = false;

	public void setChanged() {
		changed = true;

	}

	public boolean IsChanged() {
		return changed;
	}

	public void resetChanged() {
		changed = false;
	}
}
