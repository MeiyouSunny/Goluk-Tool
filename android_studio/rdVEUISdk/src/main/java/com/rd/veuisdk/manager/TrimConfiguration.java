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
    public final boolean default1x1CropMode;

    /**
     * 是否显示1:1裁切按钮
     */
    public final boolean enable1x1;

    /**
     * 截取返回方式
     */
    public final int trimReturnMode;

    /**
     * 截取方式
     */
    public final int trimType;

    /**
     * 保存路径
     */
    public final String savePath;

    /**
     * 单个截取定长
     */
    public final int trimSingleFixDuration;

    /**
     * 两定长截取定长1
     */
    public final int trimDuration1;

    /**
     * 两定长截取定长2
     */
    public int trimDuration2;

    /**
     * 标题栏标题文字
     */
    public final String title;

    /**
     * 截取界面标题栏背景色
     */
    public final int titleBarColor;

    /**
     * 取消按钮文字
     */
    public final String buttonCancelText;

    /**
     * 确认按钮文字
     */
    public final String buttonConfirmText;

    /**
     * 按钮背景色
     */
    public final int buttonColor;
    private int exportVideoMaxWH = 640;

    /**
     * 获取实际截取导出视频时的最大边
     *
     * @return 最大边
     */
    public int getVideoMaxWH() {
        return Math.min(3480, exportVideoMaxWH);
    }

    /**
     * 视频导出码率(M)
     */
    public final double exportVideoBitRate;

    /**
     * 获取导出视频码率(bps)
     *
     * @return 码率(bps)
     */
    public int getVideoBitratebps() {
        return (int) (exportVideoBitRate * 1000 * 1000);
    }

    public TrimConfiguration(Builder builder) {
        this.exportVideoMaxWH = builder.mExportVideoMaxWH;
        this.exportVideoBitRate = builder.mExportVideoBitRate;
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
        this.savePath = builder.mSavePath;
    }

    /**
     * Builder class for {@link TrimConfiguration} objects.
     */
    public static class Builder {
        private boolean mEnable1x1 = true;
        private         int mTrimType = TRIM_TYPE_FREE;
        private int mTrimReturnMode = TRIM_DYNAMIC_RETURN;
        private String mSavePath = null;
        private int mTrimSingleFixedDuration = 0;
        private int mTrimDuration1 = 0;
        private int mTrimDuration2 = 0;
        private String mTitle = null;
        private boolean mDefault1x1CropMode = false;
        private String mButtonCancelText = null;
        private String mButtonConfirmText = null;
        private int mTitleBarColor = 0;
        private int mButtonColor = 0;
        private int mExportVideoMaxWH = 640;
        private double mExportVideoBitRate = 4;

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

        /**
         * 实际截取时，设置导出视频最大边
         *
         * @param maxWH 导出视频最大边
         */
        public Builder setVideoMaxWH(int maxWH) {
            mExportVideoMaxWH = Math.max(176, Math.min(maxWH, 3840));
            return this;
        }

        /**
         * 实际截取时，设置导出视频码率
         *
         * @param bitRate 导出视频码率,单位M，传null默认4M
         */
        public Builder setVideoBitRate(double bitRate) {
            mExportVideoBitRate = bitRate;
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
        dest.writeInt(this.exportVideoMaxWH);
        dest.writeDouble(this.exportVideoBitRate);
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
        this.exportVideoMaxWH = in.readInt();
        this.exportVideoBitRate = in.readDouble();
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
