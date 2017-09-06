package com.rd.veuisdk.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Faceu 美颜参数
 * 
 * @author JIAN
 * @date 2017-5-18 下午6:22:33
 */
public class FaceuInfo implements Parcelable {
	/**
	 * 美白参数
	 * 
	 */
	public float getColor_level() {
		return color_level;
	}

	public void setColor_level(float color_level) {
		this.color_level = color_level;
	}

	/**
	 * 美颜时磨皮
	 * 
	 * @return
	 */
	public float getBlur_level() {
		return blur_level;
	}

	public void setBlur_level(float blur_level) {
		this.blur_level = blur_level;
	}

	/**
	 * 美颜时瘦脸
	 * 
	 * @return
	 */
	public float getCheek_thinning() {
		return cheek_thinning;
	}

	public void setCheek_thinning(float cheek_thinning) {
		this.cheek_thinning = cheek_thinning;
	}

	/**
	 * 大眼
	 * 
	 * @return
	 */
	public float getEye_enlarging() {
		return eye_enlarging;
	}

	public void setEye_enlarging(float eye_enlarging) {
		this.eye_enlarging = eye_enlarging;
	}

	private float color_level = 0f;
	private float blur_level = 0f;
	private float cheek_thinning = 0f;
	private float eye_enlarging = 0f;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "FaceuInfo [color_level=" + color_level + ", blur_level="
				+ blur_level + ", cheek_thinning=" + cheek_thinning
				+ ", eye_enlarging=" + eye_enlarging + "]";
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeFloat(this.color_level);
		dest.writeFloat(this.blur_level);
		dest.writeFloat(this.cheek_thinning);
		dest.writeFloat(this.eye_enlarging);

	}

	protected FaceuInfo(Parcel in) {

		this.color_level = in.readFloat();
		this.blur_level = in.readFloat();
		this.cheek_thinning = in.readFloat();
		this.eye_enlarging = in.readFloat();

	}

	public FaceuInfo() {
	}

	public static final Parcelable.Creator<FaceuInfo> CREATOR = new Parcelable.Creator<FaceuInfo>() {
		@Override
		public FaceuInfo createFromParcel(Parcel source) {
			return new FaceuInfo(source);
		}

		@Override
		public FaceuInfo[] newArray(int size) {
			return new FaceuInfo[size];
		}
	};

}
