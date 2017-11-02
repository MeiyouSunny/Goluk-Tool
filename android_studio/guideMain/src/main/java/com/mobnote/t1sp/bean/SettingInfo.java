package com.mobnote.t1sp.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 设置信息
 */
public class SettingInfo implements Parcelable {

    // 循环视频质量
    public String videoRes;
    // 声音控制
    public boolean soundRecord;
    // 紧急碰撞感应
    public String GSensor;
    // 停车安防模式
    public boolean parkingGuard;
    // 移动侦测
    public boolean MTD;
    // 关机时间
    public String powerOffDelay;
    // 开机声音
    public boolean powerSound;
    // 拍照提示音
    public boolean snapSound;
    // 自动旋转
    public boolean autoRotate;
    // 视频水印
    public boolean recStamp;
    // 精彩视频时间
    public String captureTime;
    // 存储卡容量
    public String SDCardInfo;
    // 设备型号
    public String deviceModel;
    // 设备ID
    public String deviceId;
    // 设备版本
    public String deviceVersion;

    public SettingInfo() {
    }

    protected SettingInfo(Parcel in) {
        videoRes = in.readString();
        soundRecord = in.readByte() != 0;
        GSensor = in.readString();
        parkingGuard = in.readByte() != 0;
        MTD = in.readByte() != 0;
        powerOffDelay = in.readString();
        powerSound = in.readByte() != 0;
        snapSound = in.readByte() != 0;
        autoRotate = in.readByte() != 0;
        recStamp = in.readByte() != 0;
        captureTime = in.readString();
        SDCardInfo = in.readString();
        deviceModel = in.readString();
        deviceId = in.readString();
        deviceVersion = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoRes);
        dest.writeByte((byte) (soundRecord ? 1 : 0));
        dest.writeString(GSensor);
        dest.writeByte((byte) (parkingGuard ? 1 : 0));
        dest.writeByte((byte) (MTD ? 1 : 0));
        dest.writeString(powerOffDelay);
        dest.writeByte((byte) (powerSound ? 1 : 0));
        dest.writeByte((byte) (snapSound ? 1 : 0));
        dest.writeByte((byte) (autoRotate ? 1 : 0));
        dest.writeByte((byte) (recStamp ? 1 : 0));
        dest.writeString(captureTime);
        dest.writeString(SDCardInfo);
        dest.writeString(deviceModel);
        dest.writeString(deviceId);
        dest.writeString(deviceVersion);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SettingInfo> CREATOR = new Creator<SettingInfo>() {
        @Override
        public SettingInfo createFromParcel(Parcel in) {
            return new SettingInfo(in);
        }

        @Override
        public SettingInfo[] newArray(int size) {
            return new SettingInfo[size];
        }
    };

}
