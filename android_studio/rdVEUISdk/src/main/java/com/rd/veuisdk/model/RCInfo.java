package com.rd.veuisdk.model;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.FlipType;

/**
 * 裁剪和旋转 应用到全部时，传递参数
 */
public class RCInfo implements Parcelable {

    private int angle;
    private RectF clipRectF;
    private FlipType flipType;

    public RCInfo(int angle, RectF clipRectF, FlipType flipType) {
        this.angle = angle;
        this.clipRectF = clipRectF;
        this.flipType = flipType;
    }

    protected RCInfo(Parcel in) {
        int type = in.readInt();
        flipType = type == -1 ? null : FlipType.values()[type];
        angle = in.readInt();
        clipRectF = in.readParcelable(RectF.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(flipType == null ? -1 : flipType.ordinal());
        dest.writeInt(angle);
        dest.writeParcelable(clipRectF, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RCInfo> CREATOR = new Creator<RCInfo>() {
        @Override
        public RCInfo createFromParcel(Parcel in) {
            return new RCInfo(in);
        }

        @Override
        public RCInfo[] newArray(int size) {
            return new RCInfo[size];
        }
    };

    public int getAngle() {
        return angle;
    }

    public RectF getClipRectF() {
        return clipRectF;
    }

    public FlipType getFlipType() {
        return flipType;
    }

    @Override
    public String toString() {
        return "RCInfo{" +
                "angle=" + angle +
                ", clipRectF=" + clipRectF +
                ", flipType=" + flipType +
                '}';
    }


}
