package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.VirtualVideo;

public class VideoOb implements Parcelable {

    public VideoOb(float tstart, float tend, float nStart, float nEnd, float rstart,
                   float rend, int isExtpic, ExtPicInfo info, int cropMode) {
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
        return new VideoOb(0, fdu, 0, fdu, 0, fdu, 0, null, 0);
    }

    public float nStart, nEnd; // 控制rangseekbar3 的min ,max 相对于当前视频段、速率的进度
    public float rStart, rEnd; // 控制rangseekbar3 的min ,max 原始视频且speed==1时的位置
    public float TStart, TEnd; // 相对于原始视频的位置(起始位置)

    private ExtPicInfo extpic;
    private int cropMode = 0; // 0自由裁切，1 正方形裁切，2 原始裁切

    private VideoObjectPack mVideoObjectPack; //倒序保存信息

    public VideoObjectPack getVideoObjectPack() {
        return mVideoObjectPack;
    }

    public void setVideoObjectPack(VideoObjectPack mVideoObjectPack) {
        this.mVideoObjectPack = mVideoObjectPack;
    }


    public ExtPicInfo getExtpic() {
        return extpic;
    }

    public int getCropMode() {
        return cropMode;
    }

    public void setCropMode(int cropMode) {
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
        }
    }

    @Override
    public String toString() {
        return "VideoOb [nStart=" + nStart + ", nEnd=" + nEnd + ", rStart="
                + rStart + ", rEnd=" + rEnd + ", TStart=" + TStart + ", TEnd="
                + TEnd + ", extpic=" + extpic + ", cropMode=" + cropMode +
                ", isExtPic=" + isExtPic + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
