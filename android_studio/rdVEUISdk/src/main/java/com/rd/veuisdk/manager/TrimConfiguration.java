package com.rd.veuisdk.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * RdVEUISdk视频截取配置类
 */
public class TrimConfiguration implements Parcelable {

    /**
     * 自由截取
     */
    public static final int TRIM_TYPE_FREE = 0;
    /**
     * 单个定长截取
     */
    public static final int TRIM_TYPE_SINGLE_FIXED = 1;
    /**
     * 两个定长截取
     */
    public static final int TRIM_TYPE_DOUBLE_FIXED = 2;

    /**
     * 真实截取
     */
    public static final int TRIM_RETURN_MEDIA = 0;
    /**
     * 返回时间段
     */
    public static final int TRIM_RETURN_TIME = 1;
    /**
     * 动态截取
     */
    public static final int TRIM_DYNAMIC_RETURN = 2;


    /**
     * 默认裁切为1：1
     */
    public boolean default1x1CropMode = false;

    /**
     * 是否显示1:1裁切按钮
     */
    public boolean enable1x1 = true;

    /**
     * 截取返回方式
     */
    public int trimReturnMode = TRIM_DYNAMIC_RETURN;

    /**
     * 截取方式
     */
    public int trimType = TRIM_TYPE_FREE;

    /**
     * 保存路径
     */
    public String savePath = null;

    /**
     * 单个截取定长
     */
    public int trimSingleFixDuration = 0;

    /**
     * 两定长截取定长1
     */
    public int trimDuration1 = 0;

    /**
     * 两定长截取定长2
     */
    public int trimDuration2 = 0;

    /**
     * 标题栏标题文字
     */
    public String title = null;

    /**
     * 截取界面标题栏背景色
     */
    public int titleBarColor = 0;

    /**
     * 取消按钮文字
     */
    public String buttonCancelText = null;

    /**
     * 确认按钮文字
     */
    public String buttonConfirmText = null;

    /**
     * 按钮背景色
     */
    public int buttonColor = 0;


    public TrimConfiguration(Builder builder) {
        this.enable1x1 = builder.mEnable1x1;
        this.trimType = builder.mTrimType;
        this.trimReturnMode = builder.mTrimReturnMode;
        this.default1x1CropMode = builder.mDefault1x1CropMode;
        this.buttonCancelText = builder.mButtonCancelText;
        this.buttonConfirmText = builder.mButtonConfirmText;
        this.titleBarColor = builder.mTitleBarColor;
        this.buttonColor = builder.mButtonColor;
        this.title = builder.mTitle;
        if (builder.mTrimDuration1 < 1) {
            if (builder.mTrimDuration1 == 0) {
                builder.mTrimDuration1 = 8;
            } else {
                builder.mTrimDuration1 = 1;
            }
        }
        if (builder.mTrimDuration2 < 1) {
            if (builder.mTrimDuration2 == 0) {
                builder.mTrimDuration2 = 16;
            } else {
                builder.mTrimDuration2 = 1;
            }
        }
        if (builder.mTrimSingleFixedDuration < 1) {
            if (builder.mTrimSingleFixedDuration == 0) {
                builder.mTrimSingleFixedDuration = 15;
            } else {
                builder.mTrimSingleFixedDuration = 1;
            }
        }
        this.trimSingleFixDuration = builder.mTrimSingleFixedDuration;
        this.trimDuration1 = builder.mTrimDuration1;
        this.trimDuration2 = builder.mTrimDuration2;
        if (builder.mSavePath != null) {
            this.savePath = builder.mSavePath;
        }
    }

    /**
     * Builder class for {@link TrimConfiguration} objects.
     */
    public static class Builder {
        boolean mEnable1x1 = true;
        int mTrimType = TRIM_TYPE_FREE;
        int mTrimReturnMode = TRIM_DYNAMIC_RETURN;
        String mSavePath = null;
        int mTrimSingleFixedDuration = 0;
        int mTrimDuration1 = 0;
        int mTrimDuration2 = 0;
        String mTitle = null;
        boolean mDefault1x1CropMode = false;
        String mButtonCancelText = null;
        String mButtonConfirmText = null;
        int mTitleBarColor = 0;
        int mButtonColor = 0;

        /**
         * 设置标题栏文字
         *
         * @param text 标题栏文字
         */
        public Builder setTitle(String text) {
            this.mTitle = text;
            return this;
        }


        /**
         * 设置确定按钮文字
         *
         * @param text 取消按钮文字 设为null将显示"√"图标
         */
        public Builder setConfirmButtonText(String text) {
            this.mButtonConfirmText = text;
            return this;
        }

        /**
         * 设置取消按钮文字
         *
         * @param text 取消按钮文字 设为null将显示"x"图标
         */
        public Builder setCancelButtonText(String text) {
            this.mButtonCancelText = text;
            return this;
        }


        /**
         * 设置标题栏颜色
         *
         * @param color 颜色
         */
        public Builder setTitleBarColor(int color) {
            this.mTitleBarColor = color;
            return this;
        }

        /**
         * 设置按钮颜色
         *
         * @param color 颜色
         */
        public Builder setButtonColor(int color) {
            this.mButtonColor = color;
            return this;
        }

        /**
         * 设置是否显示1:1截取按钮
         *
         * @param enable true为显示
         */
        public Builder enable1x1(boolean enable) {
            this.mEnable1x1 = enable;
            return this;
        }

        /**
         * 设置保存路径
         *
         * @param savePath true为显示
         */
        public Builder setSavePath(String savePath) {
            this.mSavePath = savePath;
            return this;
        }

        /**
         * 设置默认裁切区域是否为1：1
         *
         * @param default1x1 true为默认1:1
         */
        public Builder setDefault1x1CropMode(boolean default1x1) {
            this.mDefault1x1CropMode = default1x1;
            return this;
        }

        /**
         * 设置两定长截取时间
         *
         * @param trimDuration1 第一个定长
         * @param trimDuration2 第二个定长
         */
        public Builder setTrimDuration(int trimDuration1, int trimDuration2) {
            mTrimDuration1 = trimDuration1;
            mTrimDuration2 = trimDuration2;
            return this;
        }

        /**
         * 设置单定长截取时间
         *
         * @param trimSingleFixedDuration 单定长截取时间
         */
        public Builder setTrimDuration(int trimSingleFixedDuration) {
            mTrimSingleFixedDuration = trimSingleFixedDuration;
            return this;
        }

        /**
         * 设置截取类型
         *
         * @param trimType 需指定的截取类型 可选值为:<br>
         *                 自由截取：{@link TrimConfiguration#TRIM_TYPE_FREE}<br>
         *                 单个定长截取：{@link TrimConfiguration#TRIM_TYPE_SINGLE_FIXED}<br>
         *                 两个定长截取：{@link TrimConfiguration#TRIM_TYPE_DOUBLE_FIXED}<br>
         */
        public Builder setTrimType(int trimType) {
            this.mTrimType = Math.min(Math.max(0, trimType), 2);
            return this;
        }

        /**
         * 设置截取返回类型
         *
         * @param trimReturnMode 需指定的截取类型 可选值为:<br>
         *                       返回媒体：{@link TrimConfiguration#TRIM_RETURN_MEDIA}<br>
         *                       返回时间段：{@link TrimConfiguration#TRIM_RETURN_TIME}<br>
         *                       动态选择：{@link TrimConfiguration#TRIM_DYNAMIC_RETURN}<br>
         */
        public Builder setTrimReturnMode(int trimReturnMode) {
            this.mTrimReturnMode = Math.min(Math.max(0, trimReturnMode), 2);
            return this;
        }


        public TrimConfiguration get() {
            return new TrimConfiguration(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.default1x1CropMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enable1x1 ? (byte) 1 : (byte) 0);
        dest.writeInt(this.trimReturnMode);
        dest.writeInt(this.trimType);
        dest.writeString(this.savePath);
        dest.writeInt(this.trimSingleFixDuration);
        dest.writeInt(this.trimDuration1);
        dest.writeInt(this.trimDuration2);
        dest.writeString(this.title);
        dest.writeInt(this.titleBarColor);
        dest.writeString(this.buttonCancelText);
        dest.writeString(this.buttonConfirmText);
        dest.writeInt(this.buttonColor);
    }

    protected TrimConfiguration(Parcel in) {
        this.default1x1CropMode = in.readByte() != 0;
        this.enable1x1 = in.readByte() != 0;
        this.trimReturnMode = in.readInt();
        this.trimType = in.readInt();
        this.savePath = in.readString();
        this.trimSingleFixDuration = in.readInt();
        this.trimDuration1 = in.readInt();
        this.trimDuration2 = in.readInt();
        this.title = in.readString();
        this.titleBarColor = in.readInt();
        this.buttonCancelText = in.readString();
        this.buttonConfirmText = in.readString();
        this.buttonColor = in.readInt();
    }

    public static final Parcelable.Creator<TrimConfiguration> CREATOR = new Parcelable.Creator<TrimConfiguration>() {
        @Override
        public TrimConfiguration createFromParcel(Parcel source) {
            return new TrimConfiguration(source);
        }

        @Override
        public TrimConfiguration[] newArray(int size) {
            return new TrimConfiguration[size];
        }
    };
}
