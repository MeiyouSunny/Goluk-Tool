package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.lib.utils.FileUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.utils.Utils;

import java.io.File;

public class SoundInfo implements Parcelable {

    /**
     * id
     */
    private int id;
    /**
     * 时间片上开始时间和结束时间  毫秒
     */
    private int mStart;
    private int mEnd;

    private Music mMusic;
    /**
     * 截取时间  毫秒
     */
    private int mTrmeStart;
    private int mTrmeEnd;
    private String mName;
    /**
     * 存放地址
     */
    private String mPath;

    /**
     * 音量
     */
    private int mMixFactor = 50;

    public int getMixFactor() {
        return mMixFactor;
    }


    public void setMixFactor(int mMixFactor) {
        this.mMixFactor = mMixFactor;
        if (mMusic != null) {
            mMusic.setMixFactor(mMixFactor);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStart() {
        return mStart;
    }

    public void setStart(int mStart) {
        this.mStart = mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public void setEnd(int mEnd) {
        this.mEnd = mEnd;
    }

    public Music getmMusic() {
        if (null == mMusic) {
            mMusic = VirtualVideo.createMusic(getPath());
            mMusic.setMixFactor(getMixFactor());
            mMusic.setTimeRange(Utils.ms2s(getTrmeStart()), Utils.ms2s(getTrmeEnd()));
        }
        mMusic.setTimelineRange(Utils.ms2s(getStart()), Utils.ms2s(getEnd()));

        return mMusic;
    }

    public SoundInfo(SoundInfo info) {
        this.mName = info.mName;
        this.id = info.id;
        this.mStart = info.mStart;
        this.mEnd = info.mEnd;
        this.mPath = info.mPath;
        this.mTrmeStart = info.mTrmeStart;
        this.mTrmeEnd = info.mTrmeEnd;
        this.mMixFactor = info.mMixFactor;
    }


    public void recycle() {
        if (null != mMusic) {
            mMusic = null;
        }

    }

    public void offset(float offset) {
        mStart += offset;
        mEnd += offset;
        mMusic = null;
    }

    @Override
    public boolean equals(Object o) {
        if (null != o && (o instanceof SoundInfo)) {
            SoundInfo info = (SoundInfo) o;
            return
                    getStart() == info.getStart()
                            && getEnd() == info.getEnd()
                            && getId() == info.getId()
                            && getTrmeEnd() == info.getTrmeEnd()
                            && getMixFactor() == info.getMixFactor()
                            && getTrmeStart() == getTrmeStart();

        } else {
            return false;
        }
    }

    /**
     * 移动到草稿箱
     *
     * @param basePath
     */
    public void moveToDraft(String basePath) {
        //配音文件移动到草稿箱
        if (FileUtils.isExist(basePath)) {
            if (!mPath.contains(basePath)) {
                //文件已经在草稿箱中，不需要再剪切文件
                File fileOld = new File(mPath);
                File fileNew = new File(basePath, fileOld.getName());
                FileUtils.syncCopyFile(fileOld, fileNew, null);
                mMusic = null;
                mPath = fileNew.getAbsolutePath();
            }
        }


    }


    public int getTrmeStart() {
        return mTrmeStart;
    }

    public void setTrmeStart(int mTrmeStart) {
        this.mTrmeStart = mTrmeStart;
    }

    public int getTrmeEnd() {
        return mTrmeEnd;
    }

    public void setTrmeEnd(int mTrmeEnd) {
        this.mTrmeEnd = mTrmeEnd;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.mStart);
        dest.writeInt(this.mEnd);
        dest.writeParcelable(this.mMusic, flags);
        dest.writeInt(this.mTrmeStart);
        dest.writeInt(this.mTrmeEnd);
        dest.writeString(this.mName);
        dest.writeString(this.mPath);
        dest.writeInt(this.mMixFactor);
    }

    public SoundInfo() {
    }

    protected SoundInfo(Parcel in) {
        this.id = in.readInt();
        this.mStart = in.readInt();
        this.mEnd = in.readInt();
        this.mMusic = in.readParcelable(Music.class.getClassLoader());
        this.mTrmeStart = in.readInt();
        this.mTrmeEnd = in.readInt();
        this.mName = in.readString();
        this.mPath = in.readString();
        this.mMixFactor = in.readInt();
    }

    public static final Creator<SoundInfo> CREATOR = new Creator<SoundInfo>() {
        @Override
        public SoundInfo createFromParcel(Parcel source) {
            return new SoundInfo(source);
        }

        @Override
        public SoundInfo[] newArray(int size) {
            return new SoundInfo[size];
        }
    };

    @Override
    public String toString() {
        return "SoundInfo{" +
                "hash=" + hashCode() +
                ",id=" + id +
//                ", mStart=" + mStart +
//                ", mEnd=" + mEnd +
                ", mMusic=" + mMusic +
                ", mTrmeStart=" + mTrmeStart +
                ", mTrmeEnd=" + mTrmeEnd +
//                ", mName='" + mName + '\'' +
//                ", mPath='" + mPath + '\'' +
                ", mMixFactor=" + mMixFactor +
                '}';
    }
}
