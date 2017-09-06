package com.rd.veuisdk.manager;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.rd.veuisdk.SdkEntry;

/**
 * RdVEUISdk导出视频配置类
 */
public class ExportConfiguration implements Parcelable {
    /**
     * 视频保存路径
     */
    public String savePath = null;
    /**
     * 淡入淡出时长(单位秒)
     */
    public float trailerFadeDuration = 0.5f;
    /**
     * 片尾图片路径
     */
    public String trailerPath = null;
    /**
     * 片尾时长(单位秒)
     */
    public float trailerDuration = 2;
    /**
     * 视频分辨率
     */
    public int exportVideoWidth = 640;
    public int exportVideoHeight = 360;
    /**
     * 视频导出码率(M)
     */
    public double exportVideoBitRate = 4;
    /**
     * 视频导出时间(单位秒)
     */
    public float exportVideoDuration = 0;
    /**
     * 是否水印路径
     */
    public String watermarkPath = null;
    /**
     * 设置水印显示区域
     */
    public RectF watermarkShowRectF = null;

    private ExportConfiguration(Builder builder) {
        this.savePath = builder.b_savePath;
        this.trailerPath = builder.b_trailerPath;
        this.trailerDuration = builder.b_trailerDuration;
        this.exportVideoBitRate = builder.m_exportVideoBitRate;
        this.exportVideoDuration = builder.b_exportVideoDuration;
        this.exportVideoHeight = builder.b_exportVideoHeight;
        this.exportVideoWidth = builder.b_exportVideoWidth;
        this.watermarkPath = builder.b_watermarkPath;
        if (builder.b_watermarkShowRectF != null) {
            watermarkShowRectF = builder.b_watermarkShowRectF;
        }
    }

    /**
     * Builder class for {@link ExportConfiguration} objects.
     */
    public static class Builder {
        String b_savePath = null;
        String b_trailerPath = null;
        float b_trailerDuration = 2;
        int b_exportVideoWidth = 640;
        int b_exportVideoHeight = 360;
        double m_exportVideoBitRate = 4;
        float b_exportVideoDuration = 0;
        float b_trailerFadeDuration = 0.5f;
        String b_watermarkPath = null;
        RectF b_watermarkShowRectF = null;

        /**
         * 设置导出视频路径
         *
         * @param savePath 导出视频路径,传null将保存到默认路径
         */
        public Builder setSavePath(String savePath) {
            this.b_savePath = savePath;
            return this;
        }

        /**
         * 设置导出视频分辨率
         *
         * @param width
         * @param height
         */
        // public Builder setVideoSize(int width, int height) {
        // b_exportVideoWidth = width;
        // b_exportVideoHeight = height;
        // return this;
        // }

        /**
         * 设置导出视频码率
         *
         * @param bitRate 导出视频码率,单位M，传null默认4M
         */
        public Builder setVideoBitRate(double bitRate) {
            m_exportVideoBitRate = bitRate;
            SdkEntry.setVideoEncodingBitRate(m_exportVideoBitRate);
            return this;
        }

        /**
         * 设置导出视频时长,传0或者不设置将导出完整视频
         *
         * @param exportVideoDuration 导出视频时长，单位为秒(s)
         */
        public Builder setVideoDuration(float exportVideoDuration) {
            if (exportVideoDuration <= 0) {
                this.b_exportVideoDuration = 0;
            }
            this.b_exportVideoDuration = exportVideoDuration;
            return this;
        }

        /**
         * 设置导出视频片尾图片路径
         *
         * @param trailerPath 导出视频片尾图片路径
         */
        public Builder setTrailerPath(String trailerPath) {
            this.b_trailerPath = trailerPath;
            return this;
        }

        /**
         * 设置导出视频片尾时长
         *
         * @param trailerDuration 导出视频片尾时长，单位为秒(s)，不设置时默认为2s，最小0.5s
         */
        public Builder setTrailerDuration(float trailerDuration) {
            this.b_trailerDuration = Math.max(0.5f, trailerDuration);
            return this;
        }

        /**
         * 设置片尾淡入淡出时间
         *
         * @param fadeDuration 片尾淡入淡出时间，单位为秒(s)
         */
        public Builder setTrailerFadeDuration(float fadeDuration) {
            this.b_trailerFadeDuration = fadeDuration;
            return this;
        }


        /**
         * 设置水印路径
         *
         * @param path 水印路径
         */
        public Builder setWatermarkPath(String path) {
            this.b_watermarkPath = path;
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
                b_watermarkShowRectF = rectF;
            }
            return this;
        }

        public ExportConfiguration get() {
            return new ExportConfiguration(this);
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.savePath);
        dest.writeFloat(this.trailerFadeDuration);
        dest.writeString(this.trailerPath);
        dest.writeFloat(this.trailerDuration);
        dest.writeInt(this.exportVideoWidth);
        dest.writeInt(this.exportVideoHeight);
        dest.writeDouble(this.exportVideoBitRate);
        dest.writeFloat(this.exportVideoDuration);
        dest.writeString(this.watermarkPath);
        dest.writeParcelable(this.watermarkShowRectF, flags);
    }

    protected ExportConfiguration(Parcel in) {
        this.savePath = in.readString();
        this.trailerFadeDuration = in.readFloat();
        this.trailerPath = in.readString();
        this.trailerDuration = in.readFloat();
        this.exportVideoWidth = in.readInt();
        this.exportVideoHeight = in.readInt();
        this.exportVideoBitRate = in.readDouble();
        this.exportVideoDuration = in.readFloat();
        this.watermarkPath = in.readString();
        this.watermarkShowRectF = in.readParcelable(RectF.class.getClassLoader());
    }

    public static final Creator<ExportConfiguration> CREATOR = new Creator<ExportConfiguration>() {
        @Override
        public ExportConfiguration createFromParcel(Parcel source) {
            return new ExportConfiguration(source);
        }

        @Override
        public ExportConfiguration[] newArray(int size) {
            return new ExportConfiguration[size];
        }
    };
}
