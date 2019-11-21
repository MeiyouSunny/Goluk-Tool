package com.rd.veuisdk.model;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.rd.lib.utils.FileUtils;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.utils.Utils;

import java.io.File;

/**
 * 涂鸦对象
 */
public class GraffitiInfo implements IMoveToDraft, Parcelable {

    //单位：毫秒
    private int mTimelineFrom;

    protected GraffitiInfo(Parcel in) {
        mTimelineFrom = in.readInt();
        mTimelineTo = in.readInt();
        mPath = in.readString();
        mLiteObject = in.readParcelable(CaptionLiteObject.class.getClassLoader());
        id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTimelineFrom);
        dest.writeInt(mTimelineTo);
        dest.writeString(mPath);
        dest.writeParcelable(mLiteObject, flags);
        dest.writeInt(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GraffitiInfo> CREATOR = new Creator<GraffitiInfo>() {
        @Override
        public GraffitiInfo createFromParcel(Parcel in) {
            return new GraffitiInfo(in);
        }

        @Override
        public GraffitiInfo[] newArray(int size) {
            return new GraffitiInfo[size];
        }
    };

    public int getTimelineTo() {
        return mTimelineTo;
    }

    private int mTimelineTo;
    private String mPath;
    private CaptionLiteObject mLiteObject;

    private GraffitiInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id = 0;

    public GraffitiInfo(int timelineFrom, int timelineTo) {
        mTimelineFrom = timelineFrom;
        mTimelineTo = timelineTo;
        id = hashCode();
    }


    public void updateTimeline(int timelineFrom, int timelineTo) {
        mTimelineFrom = timelineFrom;
        mTimelineTo = timelineTo;
        updateObject();
    }

    public void setTimelineFrom(int timelineFrom) {
        mTimelineFrom = timelineFrom;
        updateObject();
    }

    public void setTimelineTo(int timelineTo) {
        mTimelineTo = timelineTo;
        updateObject();
    }

    public int getTimelineFrom() {
        return mTimelineFrom;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public CaptionLiteObject getLiteObject() {
        return mLiteObject;
    }

    private void setLiteObject(CaptionLiteObject liteObject) {
        mLiteObject = liteObject;
    }

    public void createObject() {
        try {
            CaptionLiteObject captionLiteObject = new CaptionLiteObject(null, mPath);
            captionLiteObject.setShowRectF(new RectF(0, 0, 1, 1));
            captionLiteObject.setTimelineRange(Utils.ms2s(mTimelineFrom), Utils.ms2s(mTimelineTo));
            setLiteObject(captionLiteObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void updateObject() {
        if (null != mLiteObject) {
            mLiteObject.setTimelineRange(Utils.ms2s(mTimelineFrom), Utils.ms2s(mTimelineTo));
        }
    }

    @Override
    public GraffitiInfo moveToDraft(String basePath) {
        if (FileUtils.isExist(basePath)) {
            if (!mPath.contains(basePath)) {
                //文件已经在草稿箱中，不需要再剪切文件
                File fileOld = new File(mPath);
                File fileNew = new File(basePath, fileOld.getName());
                FileUtils.syncCopyFile(fileOld, fileNew, null);
                mPath = fileNew.getAbsolutePath();
                createObject();
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "GraffitiInfo{" +
                "mTimelineFrom=" + mTimelineFrom +
                ", mTimelineTo=" + mTimelineTo +
                ", mPath='" + mPath + '\'' +
                ", mLiteObject=" + mLiteObject +
                '}';
    }

    public boolean equals(GraffitiInfo graffitiInfo) {
        if (null == graffitiInfo) {
            return false;
        }
        return mTimelineFrom == graffitiInfo.mTimelineFrom && mTimelineTo == graffitiInfo.mTimelineTo && TextUtils.equals(mPath, graffitiInfo.mPath);

    }
}
