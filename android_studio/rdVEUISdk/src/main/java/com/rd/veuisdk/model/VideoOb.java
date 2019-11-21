package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.utils.IMediaParamImp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VideoOb implements Parcelable {

    public VideoOb(float tstart, float tend, float nStart, float nEnd, float rstart,
                   float rend, int isExtpic, ExtPicInfo info, @CropMode int cropMode) {
        this.TStart = tstart;
        this.TEnd = tend;
        this.nStart = nStart;
        this.nEnd = nEnd;
        this.rStart = rstart;
        this.rEnd = rend;
        this.isExtPic = isExtpic;
        this.extpic = info;
        this.cropMode = cropMode;
    }

    public VideoOb(MediaObject mediaObject) {
        this.TStart = 0;
        this.TEnd = mediaObject.getDuration();
        this.nStart = 0;
        this.nEnd = mediaObject.getDuration();
        this.rStart = 0;
        this.rEnd = mediaObject.getDuration();
        this.isExtPic = 0;
        this.extpic = null;
        this.cropMode = DEFAULT_CROP;
    }

    /**
     * 创建一个视频的正常Vob ( 1X )
     *
     * @param path 可以是图库图片、图库视频
     * @return
     */
    public static VideoOb createVideoOb(String path) {
        float fdu = VirtualVideo.getMediaInfo(path, null);
        if (fdu < 0) {
            fdu = 0;
        }
        return new VideoOb(0, fdu, 0, fdu, 0, fdu, 0, null, DEFAULT_CROP);
    }

    public float nStart, nEnd; // 控制rangseekbar3 的min ,max 相对于当前视频段、速率的进度
    public float rStart, rEnd; // 控制rangseekbar3 的min ,max 原始视频且speed==1时的位置
    public float TStart, TEnd; // 相对于原始视频的位置(起始位置)

    private ExtPicInfo extpic;
    @CropMode
    private int cropMode = CROP_ORIGINAL; // 0自由裁切，1 正方形裁切，2 原始裁切,


    public static final int CROP_ORIGINAL = 0;//原始
    public static final int CROP_FREE = 1;//自由
    public static final int CROP_1 = 2;//1:1
    public static final int CROP_169 = -1;//16:9
    public static final int CROP_916 = -2;//9:16


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CROP_ORIGINAL, CROP_FREE, CROP_1, CROP_169, CROP_916})
    public @interface CropMode {

    }

    @CropMode
    public static final int DEFAULT_CROP = CROP_1;

    private VideoObjectPack mVideoObjectPack; //倒序保存信息
    private IMediaParamImp mMediaParamImp; //单个媒体保存滤镜
    private String mEffectReversePath;//特效倒序文件

    @Override
    public String toString() {
        return "VideoOb{" +
                "nStart=" + nStart +
                ", nEnd=" + nEnd +
                ", rStart=" + rStart +
                ", rEnd=" + rEnd +
                ", TStart=" + TStart +
                ", TEnd=" + TEnd +
                ", extpic=" + extpic +
                ", cropMode=" + cropMode +
                ", mVideoObjectPack=" + mVideoObjectPack +
                ", mMediaParamImp=" + mMediaParamImp +
                ", isExtPic=" + isExtPic +
                ", ver=" + ver +
                '}';
    }

    public IMediaParamImp getMediaParamImp() {
        return mMediaParamImp;
    }

    /**
     * 单个媒体的滤镜，透明度
     *
     * @param mediaParamImp
     */
    public void setMediaParamImp(IMediaParamImp mediaParamImp) {
        mMediaParamImp = mediaParamImp;
    }


    public VideoObjectPack getVideoObjectPack() {
        return mVideoObjectPack;
    }

    public void setVideoObjectPack(VideoObjectPack mVideoObjectPack) {
        this.mVideoObjectPack = mVideoObjectPack;
    }

    private String TAG = "VideoOb";

    /**
     * 倒序文件，存入草稿
     *
     * @param basePath
     */
    public void moveToDraft(String basePath) {
        //Log.e(TAG, "moveToDraft: " + basePath + ">>" + mEffectReversePath + "mVideoObjectPack" + mVideoObjectPack);
        if (null != mVideoObjectPack) {
            //倒序视频
            mVideoObjectPack.moveToDraft(basePath);
        }
    }


    public ExtPicInfo getExtpic() {
        return extpic;
    }

    @CropMode
    public int getCropMode() {
        return cropMode;
    }

    public void setCropMode(@CropMode int cropMode) {
        this.cropMode = cropMode;
    }


    public int isExtPic = 0;// 0 普通mediaobject ,1 可编辑的文字图片


    public VideoOb(VideoOb copy) {
        if (null != copy) {
            this.nStart = copy.nStart;
            this.nEnd = copy.nEnd;
            this.TStart = copy.TStart;
            this.TEnd = copy.TEnd;
            this.rStart = copy.rStart;
            this.rEnd = copy.rEnd;
            this.isExtPic = copy.isExtPic;
            this.extpic = copy.extpic;
            this.cropMode = copy.cropMode;
            this.mVideoObjectPack = copy.mVideoObjectPack;
            this.mMediaParamImp = copy.mMediaParamImp;
            this.mEffectReversePath = copy.mEffectReversePath;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "181204VideoOb";
    private final int ver = 2;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(ver);
        }

        dest.writeString(mEffectReversePath);

        //新增部分字段
        dest.writeParcelable(this.mMediaParamImp, flags);


        dest.writeFloat(this.nStart);
        dest.writeFloat(this.nEnd);
        dest.writeFloat(this.rStart);
        dest.writeFloat(this.rEnd);
        dest.writeFloat(this.TStart);
        dest.writeFloat(this.TEnd);
        dest.writeParcelable(this.extpic, flags);
        dest.writeInt(this.cropMode);
        dest.writeParcelable(this.mVideoObjectPack, flags);
        dest.writeInt(this.isExtPic);
    }

    protected VideoOb(Parcel in) {

        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();
            if (tVer > 1) {
                this.mEffectReversePath = in.readString();
            }
            this.mMediaParamImp = in.readParcelable(IMediaParamImp.class.getClassLoader());
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }
        this.nStart = in.readFloat();
        this.nEnd = in.readFloat();
        this.rStart = in.readFloat();
        this.rEnd = in.readFloat();
        this.TStart = in.readFloat();
        this.TEnd = in.readFloat();
        this.extpic = in.readParcelable(ExtPicInfo.class.getClassLoader());
        this.cropMode = in.readInt();
        this.mVideoObjectPack = in.readParcelable(VideoObjectPack.class.getClassLoader());
        this.isExtPic = in.readInt();
    }

    public static final Creator<VideoOb> CREATOR = new Creator<VideoOb>() {
        @Override
        public VideoOb createFromParcel(Parcel source) {
            return new VideoOb(source);
        }

        @Override
        public VideoOb[] newArray(int size) {
            return new VideoOb[size];
        }
    };
}
