package com.mobnote.t1sp.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 设置项实体
 */
public class SettingValue implements Parcelable {

    // 值
    public String value;
    // 描述
    public String description;
    // 是否选中
    public boolean isSelected;

    public SettingValue() {
    }

    public SettingValue(String value, String description) {
        this.value = value;
        this.description = description;
    }

    protected SettingValue(Parcel in) {
        value = in.readString();
        description = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
        dest.writeString(description);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SettingValue> CREATOR = new Creator<SettingValue>() {
        @Override
        public SettingValue createFromParcel(Parcel in) {
            return new SettingValue(in);
        }

        @Override
        public SettingValue[] newArray(int size) {
            return new SettingValue[size];
        }
    };
}
