package com.rd.veuisdk.mix;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;

/**
 * 每一个模块中的单个画框对应的数据
 * Created by JIAN on 2017/8/28.
 */

public class MixInfo implements Parcelable {
    public RectF getMixRect() {
        return mixRect;
    }

//    public String getMixPath() {
//        return mixPath;
//    }

    public MixInfo(RectF mixRect) {
        this.mixRect = mixRect;
    }

    public static final int RECORDED_VIDEO = 2;//已经录制成功的视频
    public static final int GALLERY_VIDEO = -1;//从图库选折的gallery视频
    public static final int RECORD_VIDEO = 1;//已打开摄像头，未录制
    public static final int OTHER_VIDEO = 0;//未选择模式

    public int getState() {
        return state;
    }

    /**
     * 是否是录制模式
     *
     * @return
     */
    public boolean isRecord() {
        return state == RECORD_VIDEO || state == RECORDED_VIDEO;
    }

    /**
     * 存在视频(不管是录制的视频还是gallery中的视频)
     *
     * @return
     */
    public boolean isExistsVideo() {
        return (null != mMediaObject);
    }

    /**
     * 设置绑定的视频状态
     *
     * @param state
     */
    public void setState(int state) {
        this.state = state;
    }

    public int state = OTHER_VIDEO;//未选择


    private RectF mixRect = new RectF();

    public AspectRatioFitMode getAspectRatioFitMode() {
        return aspectRatioFitMode;
    }


    /**
     * 设置模式
     *
     * @param aspectRatioFitMode
     */
    public void setAspectRatioFitMode(AspectRatioFitMode aspectRatioFitMode) {
        this.aspectRatioFitMode = aspectRatioFitMode;
    }

    private AspectRatioFitMode aspectRatioFitMode;


    /**
     * 删除后->恢复到默认状态
     */
    public void reset() {
        setVolumeFactor(50);
        setChannelFactor(50);
//        setVideoInfo(null, 0, 0);
        setState(OTHER_VIDEO);
        setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
        setClipRectF(null);
        setMediaObject(null);
        setThumbPath(null);
        setThumbObject(null);
    }

//    /***
//     * 获取指定的视频时长
//     * @return
//     */
//    public float getVideoDuration() {
//        float dura = getTrimEnd() - getTrimStart();
//        return dura > 0 ? dura : ModeUtils.getDuration(getMixPath());
//    }


    public RectF getClipRectF() {
        return clipRectF;
    }

    public void setClipRectF(RectF clipRectF) {
        this.clipRectF = clipRectF;
    }


//    public float getTrimStart() {
//        return nTrimStart;
//    }


//    public void setTimeRange(float nTrimStart, float nTrimEnd) {
//        this.nTrimStart = nTrimStart;
//        this.nTrimEnd = nTrimEnd;
//    }

//    public float getTrimEnd() {
//        return nTrimEnd;
//    }


//    private float nTrimStart = 0, nTrimEnd = 0;
//
//    public int getAngle() {
//        return angle;
//    }
//
//    public void setAngle(int angle) {
//        this.angle = angle;
//    }
//
//    private int angle = 0;

    /**
     * 要保留的部分视频
     */
    private RectF clipRectF;
    /**
     * 主视频时长 单位：秒
     */


    /**
     * 尾帧动画
     */
    private MediaObject mThumbObject;


    public MediaObject getMediaObject() {
        return mMediaObject;
    }

    public void setMediaObject(MediaObject mediaObject) {
        mMediaObject = mediaObject;
    }

    private MediaObject mMediaObject;


    public MediaObject getThumbObject() {
        return mThumbObject;
    }

    public void setThumbObject(MediaObject thumbObject) {
        mThumbObject = thumbObject;
    }


//    private String mixPath;


    public int getVolumeFactor() {
        return mVolumeFactor;
    }

    public void setVolumeFactor(int volumeFactor) {
        mVolumeFactor = volumeFactor;
    }

    public int getChannelFactor() {
        return mChannelFactor;
    }

    public void setChannelFactor(int channelFactor) {
        mChannelFactor = channelFactor;
    }

    //音量比
    private int mVolumeFactor = 50;
    //声道比
    private int mChannelFactor = 50;

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    /**
     * 视频尾帧图片
     */
    private String thumbPath;

    public int getId() {
        return ModeUtils.getRect2Id(mixRect);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeParcelable(mixRect, flag);
//        parcel.writeString(mixPath);
        parcel.writeInt(state);
        parcel.writeParcelable(mThumbObject, flag);
        parcel.writeString(thumbPath);
        parcel.writeInt(mVolumeFactor);
        parcel.writeInt(mChannelFactor);
        parcel.writeParcelable(clipRectF, flag);
        parcel.writeInt(this.aspectRatioFitMode == null ? -1 : this.aspectRatioFitMode.ordinal());
        parcel.writeParcelable(mMediaObject, flag);
//        parcel.writeFloat(nTrimStart);
//        parcel.writeFloat(nTrimEnd);
//        parcel.writeInt(angle);
    }

    public static final Creator<MixInfo> CREATOR = new Creator<MixInfo>() {
        @Override
        public MixInfo createFromParcel(Parcel source) {

            RectF tempRect = source.readParcelable(RectF.class.getClassLoader());
            MixInfo temp = new MixInfo(tempRect);
//            temp.setVideoInfo(source.readString(), source.readFloat(), source.readFloat());
            temp.setState(source.readInt());
            MediaObject thumb = source.readParcelable(MediaObject.class.getClassLoader());
            temp.setThumbObject(thumb);
            temp.setThumbPath(source.readString());
            temp.setVolumeFactor(source.readInt());
            temp.setChannelFactor(source.readInt());

            int tmpAspectRatioFitMode = source.readInt();
            temp.setAspectRatioFitMode(tmpAspectRatioFitMode == -1 ? null : AspectRatioFitMode.values()[tmpAspectRatioFitMode]);

            RectF clip = source.readParcelable(RectF.class.getClassLoader());
            temp.setClipRectF(clip);
            MediaObject tmp = source.readParcelable(MediaObject.class.getClassLoader());
            temp.setMediaObject(tmp);
//            temp.setAngle(source.readInt());
            return temp;


        }

        @Override
        public MixInfo[] newArray(int size) {
            return new MixInfo[size];
        }
    };

    /**
     * 克隆
     *
     * @return
     */
    public MixInfo clone() {
        MixInfo info = new MixInfo(this.getMixRect());
//        info.setVideoInfo(this.getMixPath(), this.getTrimStart(), this.getTrimEnd());
        info.setState(this.getState());
//        info.setAngle(this.getAngle());
        MediaObject mediaObject = this.getMediaObject();
        if (null != mediaObject) {
            info.setMediaObject(mediaObject.clone());
        }
        info.setClipRectF(this.getClipRectF());
        info.setVolumeFactor(this.getVolumeFactor());
        info.setThumbPath(this.getThumbPath());
        info.setAspectRatioFitMode(this.getAspectRatioFitMode());
        info.setChannelFactor(this.getChannelFactor());
        if (!TextUtils.isEmpty(info.getThumbPath())) {
            if (null != this.getThumbObject()) {
                info.setThumbObject(this.getThumbObject().clone());
            }
        }
        return info;

    }

    @Override
    public String toString() {
        return "MixInfo{" +
                "state=" + state +
                ", mixRect=" + mixRect +
                ", aspectRatioFitMode=" + aspectRatioFitMode +
                ", clipRectF=" + clipRectF +
                ", mThumbObject=" + mThumbObject +
                ", mMediaObject=" + ((null != mMediaObject) ? mMediaObject.getMediaPath() : "null") +
                ", mVolumeFactor=" + mVolumeFactor +
                ", mChannelFactor=" + mChannelFactor +
                ", thumbPath='" + thumbPath + '\'' +
                '}';
    }
}
