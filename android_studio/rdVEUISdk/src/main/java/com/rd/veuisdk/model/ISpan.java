package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 字幕-子样式
 *
 * @create 2019/4/1
 */
public class ISpan implements Parcelable {


    /**
     * @param textColor
     * @param start
     * @param end
     */
    public ISpan(int textColor, int start, int end) {
        this.textColor = textColor;
        this.start = start;
        this.end = end;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    private int textColor;
    private int start;
    private int end;

    protected ISpan(Parcel in) {
        textColor = in.readInt();
        start = in.readInt();
        end = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(textColor);
        dest.writeInt(start);
        dest.writeInt(end);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ISpan> CREATOR = new Creator<ISpan>() {
        @Override
        public ISpan createFromParcel(Parcel in) {
            return new ISpan(in);
        }

        @Override
        public ISpan[] newArray(int size) {
            return new ISpan[size];
        }
    };
}
