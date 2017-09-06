package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.MediaObject;

public class VideoObjectPack implements Parcelable {

    public MediaObject mediaObject;
    public boolean isReverse;
    public float originReverseStartTime;
    public float originReverseEndTime;

    public VideoObjectPack(MediaObject mediaObject, boolean isReverse
            , float reverseStartTime, float reverseEndTime) {
        this.mediaObject = mediaObject;
        this.isReverse = isReverse;
        originReverseStartTime = reverseStartTime;
        originReverseEndTime = reverseEndTime;
    }

    public VideoObjectPack clone() {
        return new VideoObjectPack(mediaObject, isReverse,
                originReverseStartTime, originReverseEndTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mediaObject, flags);
        dest.writeByte(this.isReverse ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.originReverseStartTime);
        dest.writeFloat(this.originReverseEndTime);
    }

    protected VideoObjectPack(Parcel in) {
        this.mediaObject = in.readParcelable(MediaObject.class.getClassLoader());
        this.isReverse = in.readByte() != 0;
        this.originReverseStartTime = in.readFloat();
        this.originReverseEndTime = in.readFloat();
    }

    public static final Creator<VideoObjectPack> CREATOR = new Creator<VideoObjectPack>() {
        @Override
        public VideoObjectPack createFromParcel(Parcel source) {
            return new VideoObjectPack(source);
        }

        @Override
        public VideoObjectPack[] newArray(int size) {
            return new VideoObjectPack[size];
        }
    };
}
