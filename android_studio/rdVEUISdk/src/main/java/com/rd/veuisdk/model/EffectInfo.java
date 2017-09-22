package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.EffectResourceStore;
import com.rd.vecore.models.EffectType;

/**
 * 特效信息
 */
public class EffectInfo implements Parcelable, EffectResourceStore {
    private EffectType mEffectType = EffectType.NONE;
    private float mStartTime, mEndTime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEffectType.ordinal());
        dest.writeFloat(this.mStartTime);
        dest.writeFloat(this.mEndTime);
    }

    public EffectInfo() {
    }

    protected EffectInfo(Parcel in) {
        this.mEffectType = EffectType.values()[in.readInt()];
        this.mStartTime = in.readFloat();
        this.mEndTime = in.readFloat();
    }

    public static final Creator<EffectInfo> CREATOR = new Creator<EffectInfo>() {
        @Override
        public EffectInfo createFromParcel(Parcel source) {
            return new EffectInfo(source);
        }

        @Override
        public EffectInfo[] newArray(int size) {
            return new EffectInfo[size];
        }
    };

    public EffectType getEffectType() {
        return mEffectType;
    }

    public void setEffectType(EffectType effectType) {
        mEffectType = effectType;
    }


    /**
     * 获取特效开始时间(秒为单位)
     *
     * @return
     */
    public float getStartTime() {
        return mStartTime;
    }

    /**
     * 获取特效结束时间(秒为单位)
     *
     * @return
     */
    public float getEndTime() {
        return mEndTime;
    }

    /**
     * 设置特效时间
     *
     * @param startTime 特效开始时间(秒为单位)
     * @param endTime   特效结束时间(秒为单位)
     */
    public void setTimeRange(float startTime, float endTime) {
        this.mStartTime = startTime;
        this.mEndTime = endTime;
    }

    private Object mObject;

    @Override
    public void setData(Object object) {
        mObject = object;
    }

    @Override
    public Object getData() {
        return mObject;
    }
}