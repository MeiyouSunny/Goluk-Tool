package com.rd.veuisdk.ae.model;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;

/**
 * 背景媒体
 */
public class BackgroundMedia implements Parcelable{

    private String src;
    private float begintime, duration;
    private String music;  //另外的背景音乐，一般与image配合使用(可选项)
    private RectF cropRect=new RectF(0,0,1,1);  //裁剪区域（默认为{0,0,1,1}，可选项）,如果为左右结构视频时，需要使用左边，则设置为{"l": 0, "t": 0, "r": 0.5, "b": 1}
    private String type;  //背景资源,支持video,image两种类型,默认为视频(可选项)

    public BackgroundMedia(String src) {
        this.src = src;
    }

    protected BackgroundMedia(Parcel in) {
        src = in.readString();
        begintime = in.readFloat();
        duration = in.readFloat();
        music = in.readString();
        cropRect = in.readParcelable(RectF.class.getClassLoader());
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(src);
        dest.writeFloat(begintime);
        dest.writeFloat(duration);
        dest.writeString(music);
        dest.writeParcelable(cropRect, flags);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BackgroundMedia> CREATOR = new Creator<BackgroundMedia>() {
        @Override
        public BackgroundMedia createFromParcel(Parcel in) {
            return new BackgroundMedia(in);
        }

        @Override
        public BackgroundMedia[] newArray(int size) {
            return new BackgroundMedia[size];
        }
    };

    public String getSrc() {
        return src;
    }

    public float getBegintime() {
        return begintime;
    }

    public void setBegintime(float begintime) {
        this.begintime = begintime;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public RectF getCropRect() {
        return cropRect;
    }

    public void setCropRect(RectF cropRect) {
        this.cropRect = cropRect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "BackgroundMedia [src=" + src + ", begintime=" + begintime
                + ", duration=" + duration + ", music=" + music + ",cropRect=" + cropRect + "]";
    }

    /**
     * 转为媒体对象
     */
    public MediaObject toMediaObject() {
        try {
            MediaObject mediaObject = new MediaObject(src);
            mediaObject.setTimeRange(begintime, begintime + duration);
            RectF actualCropRect = new RectF(cropRect.left * mediaObject.getWidth(),
                    cropRect.top * mediaObject.getHeight(), cropRect.right * mediaObject.getWidth(),
                    cropRect.bottom * mediaObject.getHeight());
            mediaObject.setClipRectF(actualCropRect);
            return mediaObject;
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

}
