package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.rd.lib.utils.FileUtils;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.utils.Utils;

import java.io.File;

/**
 * 片段中的画中画小块信息 (包含时间线、旋转角度、显示位置)
 *
 * @author JIAN
 * @create 2019/1/5
 * @Describe
 */
public class CollageInfo implements IMoveToDraft, Parcelable {
    private static final String TAG = "CollageInfo";
    //缩放比
    private float mDisf = 1f;
    private MediaObject mMediaObject;
    private String thumbPath;
    private SubInfo mSubInfo;
    private int mId = 0;


    public CollageInfo(CollageInfo src) {
        this.mDisf = src.mDisf;
        this.thumbPath = src.thumbPath;
        this.mSubInfo = src.mSubInfo;


    }

    private CollageInfo() {

    }

    protected CollageInfo(Parcel in) {
        mDisf = in.readFloat();
        mMediaObject = in.readParcelable(MediaObject.class.getClassLoader());
        thumbPath = in.readString();
        mSubInfo = in.readParcelable(SubInfo.class.getClassLoader());
        mId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mDisf);
        dest.writeParcelable(mMediaObject, flags);
        dest.writeString(thumbPath);
        dest.writeParcelable(mSubInfo, flags);
        dest.writeInt(mId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollageInfo> CREATOR = new Creator<CollageInfo>() {
        @Override
        public CollageInfo createFromParcel(Parcel in) {
            return new CollageInfo(in);
        }

        @Override
        public CollageInfo[] newArray(int size) {
            return new CollageInfo[size];
        }
    };

    public int getId() {
        return mId;
    }

    @Override
    public String toString() {
        return "CollageInfo{" +
                "mDisf=" + mDisf +
                ", mMediaObject=" + mMediaObject +
                ", thumbPath='" + thumbPath + '\'' +
                ", mSubInfo=" + mSubInfo +
                ", mId=" + mId +
                '}';
    }

    /**
     * @param mediaObject
     * @param thumbPath
     * @param subInfo
     */
    public CollageInfo(MediaObject mediaObject, String thumbPath, SubInfo subInfo) {
        mMediaObject = mediaObject;
        this.thumbPath = thumbPath;
        mSubInfo = subInfo;
        mId = this.hashCode();
    }


    public MediaObject getMediaObject() {
        return mMediaObject;
    }

    public SubInfo getSubInfo() {
        return mSubInfo;
    }


    public String getThumbPath() {
        return thumbPath;
    }

    /**
     * @param subInfo
     */
    public void setSubInfo(SubInfo subInfo) {
        mSubInfo = subInfo;
    }


    public float getDisf() {
        return mDisf;
    }

    public void setDisf(float disf) {
        mDisf = disf;
    }


    /**
     * 更新媒体的时间线
     *
     * @param start 单位：秒
     * @param end
     */
    public void fixMediaLine(float start, float end) {
        if (null != mMediaObject) {
            if (mMediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                mMediaObject.setIntrinsicDuration(end - start);
            } else {
                mMediaObject.setTimeRange(0, Math.min(end - start, mMediaObject.getIntrinsicDuration()));
            }
            mMediaObject.setTimelineRange(start, end);
        }
    }


    /**
     * 更新单个画中画片段的时间线
     *
     * @param start
     * @param end
     */
    public void updateMixInfo(int start, int end) {
        if (getSubInfo() != null) {
            getSubInfo().setTimeLine(start, end);
        }
        fixMediaLine(Utils.ms2s(start), Utils.ms2s(end));
    }

    /**
     * 替换媒体
     *
     * @param mediaObject
     * @param thumb
     */
    public void setMedia(MediaObject mediaObject, String thumb) {
        mMediaObject = mediaObject;
        thumbPath = thumb;
    }

    /***
     * 判断画中画是否一致
     * @param info
     * @return
     */
    public boolean equals(CollageInfo info) {

        if (null != info) {
            return
                    TextUtils.equals(getMediaObject().getMediaPath(), info.getMediaObject().getMediaPath())
                            && getId() == info.getId() &&
                            getMediaObject().getTimelineFrom() == info.getMediaObject().getTimelineFrom()
                            && getMediaObject().getTimelineTo() == info.getMediaObject().getTimelineTo()
                            && getMediaObject().getShowRectF().equals(info.getMediaObject().getShowRectF())
                            && getMediaObject().getAngle() == info.getMediaObject().getAngle();

        } else {
            return false;
        }
    }


    /***
     * 移动到草稿箱
     * @param basePath
     */
    @Override
    public CollageInfo moveToDraft(String basePath) {

        if (FileUtils.isExist(basePath)) {
            if (!thumbPath.contains(basePath)) {
                //文件已经在草稿箱中，不需要再剪切文件
                File fileOld = new File(thumbPath);
                File fileNew = new File(basePath, fileOld.getName());
                FileUtils.syncCopyFile(fileOld, fileNew, null);
                thumbPath = fileNew.getAbsolutePath();
            }
        }
        mMediaObject = getMediaObject().moveToDraft(basePath);
        return this;
    }
}
