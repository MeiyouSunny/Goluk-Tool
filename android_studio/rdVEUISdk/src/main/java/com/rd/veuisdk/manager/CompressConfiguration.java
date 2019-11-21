package com.rd.veuisdk.manager;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.utils.ExportUtils;

/**
 * RdVEUISdk压缩配置类
 */
public class CompressConfiguration implements Parcelable {

    /**
     * 水印位置：<br>
     * 左下
     */
    public static final int WATERMARK_LEFT_BOTTOM = 0;
    /**
     * 水印位置：<br>
     * 左上
     */
    public static final int WATERMARK_LEFT_TOP = 1;
    /**
     * 水印位置：<br>
     * 右下
     */
    public static final int WATERMARK_RIGHT_BOTTOM = 2;
    /**
     * 水印位置：<br>
     * 右上
     */
    public static final int WATERMARK_RIGHT_TOP = 3;
    /**
     * 视频分辨率
     */
    private final double bitRate;
    /**
     * 帧率
     */
    private final int frameRate;
    /**
     * 是否显示水印
     */
    public final boolean enableWatermark;
    /**
     * 是否使用硬件加速
     */
    private final boolean enableHWCode;
    /**
     * 水印位置
     */
    private final int watermarkPosition;
    /**
     * 视频分辨率
     */
    private final int videoWidth;
    private final int videoHeight;
    /**
     * 保存视频地址
     */
    public final String savePath;
    /**
     * 压缩视频水印显示区域
     */
    private final RectF compressWatermarkRectF;

    public ExportUtils.CompressConfig toCompressConfig() {
        return new ExportUtils.CompressConfig(bitRate, frameRate, enableWatermark, enableHWCode, watermarkPosition, videoWidth, videoHeight, compressWatermarkRectF);
    }

    public CompressConfiguration(Builder builder) {
        bitRate = builder.mBitRate;
        enableWatermark = builder.mEnableWatermark;
        watermarkPosition = builder.mWatermarkPosition;
        videoWidth = builder.mVideoWidth;
        videoHeight = builder.mVideoHeight;
        enableHWCode = builder.mEnableHWCode;
        savePath = builder.mSavePath;
        compressWatermarkRectF = builder.mCompressWatermarkRectF;
        frameRate = builder.frameRate;
    }

    /**
     * Builder class for {@link CompressConfiguration} objects.
     */
    public static class Builder {

        double mBitRate = 4;
        int frameRate = 25;
        boolean mEnableWatermark = false;
        int mWatermarkPosition = WATERMARK_LEFT_BOTTOM;
        int mVideoWidth = 0;
        int mVideoHeight = 0;
        String mSavePath = null;
        boolean mEnableHWCode = true;
        RectF mCompressWatermarkRectF = null;

        /**
         * 设置视频分辨率
         *
         * @param bitRate 码流大小（单位：M）
         */
        public Builder setBitRate(double bitRate) {
            this.mBitRate = Math.max(0.01, bitRate);
            return this;
        }

        /**
         * 视频帧率
         *
         * @param frameRate
         * @return
         */
        public Builder setFrameRate(int frameRate) {
            this.frameRate = Math.min(60, Math.max(1, frameRate));
            return this;
        }

        /**
         * 设置是否使用硬件加速
         *
         * @param enable true为使用硬件加速
         */
        public Builder enableHWCode(boolean enable) {
            this.mEnableHWCode = enable;
            return this;
        }

        /**
         * 设置是否显示水印
         *
         * @param enable true代表显示水印
         */
        public Builder enableWatermark(boolean enable) {
            this.mEnableWatermark = enable;
            return this;
        }

        /**
         * 设置水印位置
         *
         * @param position 需指定的格式类型 可选值为:<br>
         *                 左下：{@link CompressConfiguration#WATERMARK_LEFT_BOTTOM}<br>
         *                 左上：{@link CompressConfiguration#WATERMARK_LEFT_TOP}<br>
         *                 右下：{@link CompressConfiguration#WATERMARK_RIGHT_BOTTOM}<br>
         *                 右上：{@link CompressConfiguration#WATERMARK_RIGHT_TOP}<br>
         */
        @Deprecated
        public Builder setWatermarkPosition(int position) {
            this.mWatermarkPosition = Math.max(0, Math.min(3, position));
            return this;
        }

        /**
         * 设置水印显示区域
         *
         * @param rectF rectF.left 代表在x轴的位置 <br>
         *              rectF.top 代表在y轴的位置 <br>
         *              rectF.right 代表x轴方向的缩放比例 <br>
         *              rectF.bottom 代表y轴方向的缩放比例 <br>
         */
        public Builder setWatermarkPosition(RectF rectF) {
            if (rectF != null) {
                rectF.left = Math.max(0, Math.min(1, rectF.left));
                rectF.top = Math.max(0, Math.min(1, rectF.top));
                if (rectF.right == 0) {
                    rectF.right = 1;
                }
                if (rectF.bottom == 0) {
                    rectF.bottom = 1;
                }
                this.mCompressWatermarkRectF = rectF;
            }
            return this;
        }

        /**
         * 设置分辨率
         *
         * @param width  （最大值1280，最小值240）
         * @param height （最大值1280，最小值240）
         */
        public Builder setVideoSize(int width, int height) {
            if (width != 0) {
                this.mVideoWidth = Math.max(240, Math.min(width, 1920));
            }
            if (height != 0) {
                this.mVideoHeight = Math.max(240, Math.min(height, 1920));
            }
            return this;
        }

        /**
         * 设置保存地址
         *
         * @param path
         */
        public Builder setSavePath(String path) {
            this.mSavePath = path;
            return this;
        }

        public CompressConfiguration get() {
            return new CompressConfiguration(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "190426CompressConfigBuilder";
    private static final int VER = 1;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeInt(this.frameRate);
        dest.writeDouble(this.bitRate);
        dest.writeByte(this.enableWatermark ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableHWCode ? (byte) 1 : (byte) 0);
        dest.writeInt(this.watermarkPosition);
        dest.writeInt(this.videoWidth);
        dest.writeInt(this.videoHeight);
        dest.writeString(this.savePath);
        dest.writeParcelable(this.compressWatermarkRectF, flags);
    }

    protected CompressConfiguration(Parcel in) {

        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();
            this.frameRate = in.readInt();
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
            this.frameRate = 25;
        }
        this.bitRate = in.readDouble();
        this.enableWatermark = in.readByte() != 0;
        this.enableHWCode = in.readByte() != 0;
        this.watermarkPosition = in.readInt();
        this.videoWidth = in.readInt();
        this.videoHeight = in.readInt();
        this.savePath = in.readString();
        this.compressWatermarkRectF = in.readParcelable(RectF.class.getClassLoader());
    }

    public static final Parcelable.Creator<CompressConfiguration> CREATOR = new Parcelable.Creator<CompressConfiguration>() {
        @Override
        public CompressConfiguration createFromParcel(Parcel source) {
            return new CompressConfiguration(source);
        }

        @Override
        public CompressConfiguration[] newArray(int size) {
            return new CompressConfiguration[size];
        }
    };
}
