package com.rd.veuisdk.ae.model;

import android.os.Parcel;
import android.os.Parcelable;

//模板默认资源信息
public class DefaultMedia implements Parcelable{
    private String refId;
    private String path;
    private int width;
    private int height;

    public DefaultMedia(String refId, String path) {
        this.refId = refId;
        this.path = path;
    }

    protected DefaultMedia(Parcel in) {
        refId = in.readString();
        path = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(refId);
        dest.writeString(path);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DefaultMedia> CREATOR = new Creator<DefaultMedia>() {
        @Override
        public DefaultMedia createFromParcel(Parcel in) {
            return new DefaultMedia(in);
        }

        @Override
        public DefaultMedia[] newArray(int size) {
            return new DefaultMedia[size];
        }
    };

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public void setHeight(int height) {
        this.height = height;
    }


}
