package com.rd.veuisdk.model;

import android.graphics.RectF;
import android.os.Parcel;

import com.rd.vecore.models.DewatermarkObject;
import com.rd.veuisdk.utils.Utils;

/**
 * 马赛克|去水印
 */
public class MOInfo extends ICommon {
    private DewatermarkObject mObject;

    public void setShowRectF(RectF showRectF) {
        if (null != showRectF) {
            mShowRectF = showRectF;
            mObject.setShowRectF(showRectF);
        }
    }

    public RectF getShowRectF() {
        return mShowRectF;
    }

    //相对于预览尺寸 0~1.0f
    private RectF mShowRectF = new RectF();

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (this.value != value) {
            this.value = value;
            mObject.setValue(this.value);
            setChanged();
        }
    }

    private float value = 0.5f;

    protected MOInfo(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tparcelVer = in.readInt();
            if (tparcelVer >= 1) {
                value = in.readFloat();
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }
        id = in.readInt();
        styleId = in.readInt();
        mObject = in.readParcelable(DewatermarkObject.class.getClassLoader());
        mShowRectF = in.readParcelable(RectF.class.getClassLoader());
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "191030MOInfo";
    private static final int VER = 1;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeFloat(value);

        dest.writeInt(id);
        dest.writeInt(styleId);
        dest.writeParcelable(mObject, flags);
        dest.writeParcelable(mShowRectF, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MOInfo> CREATOR = new Creator<MOInfo>() {
        @Override
        public MOInfo createFromParcel(Parcel in) {
            return new MOInfo(in);
        }

        @Override
        public MOInfo[] newArray(int size) {
            return new MOInfo[size];
        }
    };

    public DewatermarkObject getObject() {
        return mObject;
    }

    @Override
    public String toString() {
        return "MOInfo{" +
                " id=" + id +
                ", mObject=" + mObject +
                ", styleId=" + styleId +
                ", changed=" + changed +
                ", mShowRectF=" + mShowRectF +
                '}';
    }


    public MOInfo clone() {
        return new MOInfo(this);
    }

    public void set(MOInfo info) {
        this.id = info.id;
        this.styleId = info.styleId;
        this.mObject = new DewatermarkObject(info.getObject());
        this.mShowRectF = new RectF(info.mShowRectF);
        value = info.value;

    }


    @Override
    public long getStart() {
        return Utils.s2ms(mObject.getTimelineStart());
    }

    @Override
    public void setStart(long start) {
        mObject.setTimelineRange(Utils.ms2s(start), mObject.getTimelineEnd());
        setChanged();
    }

    @Override
    public long getEnd() {
        return Utils.s2ms(mObject.getTimelineEnd());
    }

    @Override
    public void setEnd(long end) {
        setEnd(end, true);
    }

    /**
     * @param end    更新结束点
     * @param update
     */
    public void setEnd(long end, boolean update) {
        mObject.setTimelineRange(mObject.getTimelineStart(), Utils.ms2s(end), update);
        setChanged();
    }

    /**
     * 设置时间线 (单位:毫秒)
     *
     * @param start 开始
     * @param end   结束
     */
    @Override
    public void setTimelineRange(long start, long end) {
        setTimelineRange(start, end, true);
    }

    /**
     * @param start
     * @param end
     * @param update 是否需要更新core中的UI
     */
    public void setTimelineRange(long start, long end, boolean update) {
        mObject.setTimelineRange(Utils.ms2s(start), Utils.ms2s(end), update);
        setChanged();
    }


    public MOInfo() {
        mObject = new DewatermarkObject();
    }


    @Override
    public boolean equals(Object o) {
        if (null != o && (o instanceof MOInfo)) {
            MOInfo info = (MOInfo) o;
            return
                    getStart() == info.getStart()
                            && getEnd() == info.getEnd()
                            && getId() == info.getId()
                            && getStyleId() == info.getStyleId()
                            && getValue() == info.getValue();

        } else {
            return false;
        }
    }


    public MOInfo(MOInfo info) {
        id = info.id;
        styleId = info.styleId;
        mObject = new DewatermarkObject(info.getObject());
        mShowRectF = new RectF(info.mShowRectF);
        changed = info.IsChanged();
        value = info.getValue();
    }

    public void recycle() {
        if (null != mObject) {
            mObject.recycle();
        }
    }


}
