package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.ae.model.AETextLayerInfo;

/**
 * AEPreviewActivity中 adapter 对应的item (记录显示比例，绑定的媒体资源...)
 */
public class AEMediaInfo implements Parcelable {

    /**
     * 媒体类型
     */
    public static enum MediaType {

        IMAGE,

        VIDEO,

        TEXT,
    }


    protected AEMediaInfo(Parcel in) {
        type = MediaType.values()[in.readInt()];
        mAETextLayerInfo = in.readParcelable(AETextLayerInfo.class.getClassLoader());
        mAsp = in.readFloat();
        duration = in.readFloat();
        mMediaObject = in.readParcelable(MediaObject.class.getClassLoader());
        text = in.readString();
        ttf = in.readString();
        ttfIndex = in.readInt();
        mThumbPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeParcelable(mAETextLayerInfo, flags);
        dest.writeFloat(mAsp);
        dest.writeFloat(duration);
        dest.writeParcelable(mMediaObject, flags);
        dest.writeString(text);
        dest.writeString(ttf);
        dest.writeInt(ttfIndex);
        dest.writeString(mThumbPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AEMediaInfo> CREATOR = new Creator<AEMediaInfo>() {
        @Override
        public AEMediaInfo createFromParcel(Parcel in) {
            return new AEMediaInfo(in);
        }

        @Override
        public AEMediaInfo[] newArray(int size) {
            return new AEMediaInfo[size];
        }
    };

    public void setMediaObject(MediaObject mediaObject) {
        mMediaObject = mediaObject;
    }

    public MediaObject getMediaObject() {
        return mMediaObject;
    }

    public MediaType getType() {
        return type;
    }


    public AEMediaInfo(MediaType type, float asp, float duration, MediaObject mediaObject) {
        this.type = type;
        this.mAsp = asp;
        this.duration = duration;
        this.mMediaObject = mediaObject;
    }


    public AEMediaInfo(AETextLayerInfo aeTextLayerInfo, MediaType type, float asp, float duration, MediaObject mediaObject) {
        this(type, asp, duration, mediaObject);
        mAETextLayerInfo = aeTextLayerInfo;
    }

    private MediaType type = MediaType.IMAGE;

    public AETextLayerInfo getAETextLayerInfo() {
        return mAETextLayerInfo;
    }

    private AETextLayerInfo mAETextLayerInfo;

    public float getAsp() {
        return mAsp;
    }

    private float mAsp = 1f;

    public float getDuration() {
        return duration;
    }

    //该媒体的显示时长 单位:秒
    private float duration = 1f;

    private MediaObject mMediaObject;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTtf() {
        return ttf;
    }


    private String text;

    public void setTtf(String ttf, int index) {
        this.ttf = ttf;
        ttfIndex = index;
    }

    private String ttf;

    public int getTtfIndex() {
        return ttfIndex;
    }

    private int ttfIndex;

    public String getThumbPath() {
        return mThumbPath;
    }

    public void setThumbPath(String thumbPath) {
        mThumbPath = thumbPath;
    }

    /**
     * 媒体加了滤镜之后，需要生成带滤镜的缩略图
     */
    private String mThumbPath;

}
