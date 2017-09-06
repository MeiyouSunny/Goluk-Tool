package com.rd.veuisdk.manager;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

/**
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
     * 媒体对象开始时间
     */
    private int startTime;
    /**
     * 媒体对象结束时间
     */
    private int endTime;

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
     * 获取截取开始时间(ms)
     *
     * @return 开始时间(ms)
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间(ms)
     *
     * @param startTime 开始时间(ms)
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }


    /**
     * 获取截取结束时间(ms)
     *
     * @return 结束时间(ms)
     */
    public int getEndTime() {
        return endTime;
    }

    /**
     * 设置截取结束时间(ms)
     *
     * @param endTime 结束时间(ms)
     */
    public void setEndTime(int endTime) {
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
        dest.writeInt(this.startTime);
        dest.writeInt(this.endTime);
    }

    protected EditObject(Parcel in) {
        this.editObjectPath = in.readString();
        this.cropRect = in.readParcelable(Rect.class.getClassLoader());
        this.startTime = in.readInt();
        this.endTime = in.readInt();
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
