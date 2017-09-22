package com.rd.veuisdk.manager;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 时间单位统一:秒
 * xpk媒体对象
 */
public class EditObject implements Parcelable {
    /**
     * 媒体对象地址
     */
    private String editObjectPath = null;
    /**
     * 媒体对象裁剪区域
     */
    private RectF cropRect;
    /**
     * 媒体对象开始时间 单位:秒
     */
    private float startTime;
    /**
     * 媒体对象结束时间 单位:秒
     */
    private float endTime;

    /**
     * 构造函数
     *
     * @param objectPath 编辑对象路径
     */
    public EditObject(String objectPath) {
        this.editObjectPath = objectPath;
    }

    /**
     * 返回编辑对象路径
     *
     * @return 对象路径
     */
    public String getObjectPath() {
        return editObjectPath;
    }

    /**
     * 设置编辑对象路径
     *
     * @param objectPath 对象路径
     */
    public void setObjectPath(String objectPath) {
        this.editObjectPath = objectPath;
    }

    /**
     * 获取截取开始时间(秒)
     *
     * @return 开始时间(秒)
     */
    public float getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间(秒)
     *
     * @param startTime 开始时间(秒)
     */
    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }


    /**
     * 获取截取结束时间(秒)
     *
     * @return 结束时间(秒)
     */
    public float getEndTime() {
        return endTime;
    }

    /**
     * 设置截取结束时间(秒)
     *
     * @param endTime 结束时间(秒)
     */
    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }

    /**
     * 获取裁剪区域
     *
     * @return 裁剪区域
     */
    public RectF getCropRect() {
        return cropRect;
    }

    /**
     * 设置裁剪区域
     *
     * @param cropRect 裁剪区域
     */
    public void setCropRect(RectF cropRect) {
        this.cropRect = cropRect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.editObjectPath);
        dest.writeParcelable(this.cropRect, flags);
        dest.writeFloat(this.startTime);
        dest.writeFloat(this.endTime);
    }

    protected EditObject(Parcel in) {
        this.editObjectPath = in.readString();
        this.cropRect = in.readParcelable(Rect.class.getClassLoader());
        this.startTime = in.readFloat();
        this.endTime = in.readFloat();
    }

    public static final Parcelable.Creator<EditObject> CREATOR = new Parcelable.Creator<EditObject>() {
        @Override
        public EditObject createFromParcel(Parcel source) {
            return new EditObject(source);
        }

        @Override
        public EditObject[] newArray(int size) {
            return new EditObject[size];
        }
    };
}
