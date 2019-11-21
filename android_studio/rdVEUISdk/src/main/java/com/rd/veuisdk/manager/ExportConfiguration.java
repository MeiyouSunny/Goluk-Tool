package com.rd.veuisdk.manager;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.Watermark;
import com.rd.veuisdk.SdkEntry;

/**
 * RdVEUISdk导出视频配置类
 */
public class ExportConfiguration implements Parcelable {

    private static final int EXPORT_SIZE_MAX_SIDE = 3840;

    /**
     * 视频保存路径（文件夹）
     */
    public final String saveDir;
    /**
     * 淡入淡出时长(单位秒)
     */
    public final float trailerFadeDuration;
    /**
     * 片尾图片路径
     */
    public final String trailerPath;
    /**
     * 片尾时长(单位秒)
     */
    public final float trailerDuration;

    /**
     * 视频导出时间(单位秒)
     */
    public final float exportVideoDuration;
    /**
     * 是否水印路径
     */
    public final String watermarkPath;
    /**
     * 是否为文字水印
     */
    public boolean enableTextWatermark = false;
    /**
     * 文字水印内容
     */
    public String textWatermarkContent = null;
    /**
     * 文字水印大小
     */
    public int textWatermarkSize;
    /**
     * 文字水印颜色
     */
    public int textWatermarkColor;
    /**
     * 文字阴影颜色
     */
    public int textWatermarkShadowColor;
    /**
     * 水印显示区域
     */
    public final RectF watermarkShowRectF;
    /**
     * 导出视频帧率
     */
    public final int exportVideoFrameRate;
    /**
     * 视频导出码率(M)
     */
    private double exportVideoBitRate;
    /**
     * 视频分辨率
     */
    private int exportVideoMaxWH = 960;
    /**
     * 添加媒体时长限制
     */
    public float importVideoDuration;

    /**
     * 水印显示模式
     */
    public int watermarkShowMode = Watermark.MODE_DEFAULT;

    /**
     * 使用自定义导出引导界面
     */
    public boolean useCustomExportGuide = false;

    /**
     * 是否弹出导出分辨率界面
     */
    public boolean useExportVideoSizeDialog = false;
    /**
     * 水印显示区域(竖屏)
     */
    public RectF watermarkPortLayoutRectF;

    /**
     * 水印显示区域(横屏)
     */
    public RectF watermarkLandLayoutRectF;

    /**
     * 获取导出视频的最大边
     *
     * @return 最大边
     */
    public int getVideoMaxWH() {
        return Math.min(EXPORT_SIZE_MAX_SIDE, exportVideoMaxWH);
    }

    /**
     * 获取导出视频码率(bps)
     *
     * @return 码率(bps)
     */
    public int getVideoBitratebps() {
        return (int) (exportVideoBitRate * 1000 * 1000);
    }

    private ExportConfiguration(Builder builder) {
        this.saveDir = builder.mSavePath;
        this.trailerPath = builder.mTrailerPath;
        this.trailerDuration = builder.trailerDuration;
        this.trailerFadeDuration = builder.mTrailerFadeDuration;
        this.exportVideoMaxWH = builder.mExportVideoMaxWH;
        this.watermarkPath = builder.mWatermarkPath;
        this.enableTextWatermark = builder.mEnableTextWatermark;
        this.textWatermarkContent = builder.mTextWatermarkContent;
        this.textWatermarkSize = builder.mTextWatermarkSize;
        this.textWatermarkColor = builder.mTextWatermarkColor;
        this.textWatermarkShadowColor = builder.mTextWatermarkShadowColor;
        this.watermarkShowRectF = builder.mWatermarkShowRectF;
        this.watermarkLandLayoutRectF = builder.mWatermarkLandLayout;
        this.watermarkPortLayoutRectF = builder.mWatermarkPortLayout;
        this.watermarkShowMode = builder.mWatermarkShowMode;

        this.exportVideoBitRate = builder.mExportVideoBitRate;
        this.exportVideoDuration = builder.mExportVideoDuration;
        this.exportVideoFrameRate = builder.mExportVideoFrameRate;
        this.importVideoDuration = builder.mImportVideoDuration;

        this.useCustomExportGuide = builder.mUseCustomExportGuide;
        this.useExportVideoSizeDialog = builder.mUseExportVideoSizeDialog;
    }

    /**
     * Builder class for {@link ExportConfiguration} objects.
     */
    public static class Builder {
        private int mExportVideoMaxWH = 960; //默认544*960
        private double mExportVideoBitRate = 4;
        private int mExportVideoFrameRate = 30;

        private String mSavePath = null;
        private String mTrailerPath = null;
        private float trailerDuration = 2;
        private float mExportVideoDuration = 0;
        private float mTrailerFadeDuration = 0.5f;
        private String mWatermarkPath = null;
        private boolean mEnableTextWatermark = false;
        private String mTextWatermarkContent = "";
        private int mTextWatermarkSize = 10;
        private int mTextWatermarkColor = 0;
        private int mTextWatermarkShadowColor = 0;
        private RectF mWatermarkShowRectF = null;
        private RectF mWatermarkPortLayout = null;
        private RectF mWatermarkLandLayout = null;
        private float mImportVideoDuration = 0;
        private int mWatermarkShowMode;

        private boolean mUseCustomExportGuide = false;
        private boolean mUseExportVideoSizeDialog = false;

        /**
         * 设置导出视频路径
         *
         * @param savePath 导出视频路径,传null将保存到默认路径
         */
        public Builder setSavePath(String savePath) {
            this.mSavePath = savePath;
            return this;
        }

        /**
         * 设置导出视频最大边
         *
         * @param maxWH 导出视频最大边
         */
        public Builder setVideoMaxWH(int maxWH) {
            mExportVideoMaxWH = Math.max(176, Math.min(maxWH, EXPORT_SIZE_MAX_SIDE));
            return this;
        }

        /**
         * 设置导出视频码率
         *
         * @param bitRate 导出视频码率,单位M，传null默认4M
         */
        public Builder setVideoBitRate(double bitRate) {
            mExportVideoBitRate = bitRate;
            SdkEntry.setVideoEncodingBitRate(mExportVideoBitRate);
            return this;
        }

        public Builder setVideoFrameRate(int frameRate) {
            mExportVideoFrameRate = Math.max(10, Math.min(30, frameRate));
            return this;
        }

        /**
         * 设置导出视频时长,传0或者不设置将导出完整视频
         *
         * @param exportVideoDuration 导出视频时长，单位为秒(s)
         */
        public Builder setVideoDuration(float exportVideoDuration) {
            this.mExportVideoDuration = exportVideoDuration;
            if (exportVideoDuration <= 0) {
                this.mExportVideoDuration = 0;
            }
            return this;
        }

        /**
         * 设置导出视频片尾图片路径
         *
         * @param trailerPath 导出视频片尾图片路径
         */
        public Builder setTrailerPath(String trailerPath) {
            this.mTrailerPath = trailerPath;
            return this;
        }

        /**
         * 设置导出视频片尾时长
         *
         * @param trailerDuration 导出视频片尾时长，单位为秒(s)，不设置时默认为2s，最小0.5s
         */
        public Builder setTrailerDuration(float trailerDuration) {
            this.trailerDuration = Math.max(0.5f, trailerDuration);
            return this;
        }

        /**
         * 设置片尾淡入淡出时间
         *
         * @param fadeDuration 片尾淡入淡出时间，单位为秒(s)
         */
        public Builder setTrailerFadeDuration(float fadeDuration) {
            this.mTrailerFadeDuration = fadeDuration;
            return this;
        }


        /**
         * 设置图片水印路径
         *
         * @param path 水印路径
         */
        public Builder setWatermarkPath(String path) {
            this.mWatermarkPath = path;
            return this;
        }

        /**
         * 设置是否使用文字水印（启用文字水印，图片水印将失效）
         *
         * @param enable
         */
        public Builder enableTextWatermark(boolean enable) {
            this.mEnableTextWatermark = enable;
            return this;
        }

        /**
         * 设置文字水印内容
         *
         * @param content
         */
        public Builder setTextWatermarkContent(String content) {
            this.mTextWatermarkContent = content;
            return this;
        }

        /**
         * 设置文字水印大小
         *
         * @param size
         */
        public Builder setTextWatermarkSize(int size) {
            this.mTextWatermarkSize = size;
            return this;
        }

        /**
         * 设置文字水印颜色
         *
         * @param color 文字颜色（默认白色）
         */
        public Builder setTextWatermarkColor(int color) {
            this.mTextWatermarkColor = color;
            return this;
        }

        /**
         * 设置文字水印阴影颜色（不设置将没有阴影）
         *
         * @param color 文字阴影颜色（默认无阴影）
         */
        public Builder setTextWatermarkShadowColor(int color) {
            this.mTextWatermarkShadowColor = color;
            return this;
        }

        /**
         * 设置水印显示区域 与 setWatermarkLayout互斥
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
                mWatermarkShowRectF = rectF;
                mWatermarkPortLayout = null;
                mWatermarkLandLayout = null;
            }
            return this;
        }

        /**
         * 设置水印显示区域 与 setWatermarkPosition互斥
         *
         * @param landRectF 横屏水印<br>
         *                  rectF.left 代表水印在x轴left位置(范围0-1 下同) <br>
         *                  rectF.top 代表水印在y轴top位置 <br>
         *                  rectF.right 代表水印在x轴right位置 <br>
         *                  rectF.bottom 代表水印在y轴bottom位置<br>
         * @param portRectF 竖屏水印<br>
         *                  同上
         */
        public Builder setWatermarkLayout(RectF landRectF, RectF portRectF) {
            if (portRectF != null) {
                portRectF.left = Math.max(0, Math.min(1, portRectF.left));
                portRectF.right = Math.max(0, Math.min(1, portRectF.right));
                portRectF.top = Math.max(0, Math.min(1, portRectF.top));
                portRectF.bottom = Math.max(0, Math.min(1, portRectF.bottom));
            }
            if (landRectF != null) {
                landRectF.left = Math.max(0, Math.min(1, landRectF.left));
                landRectF.right = Math.max(0, Math.min(1, landRectF.right));
                landRectF.top = Math.max(0, Math.min(1, landRectF.top));
                landRectF.bottom = Math.max(0, Math.min(1, landRectF.bottom));
            }
            mWatermarkPortLayout = portRectF;
            mWatermarkLandLayout = landRectF;
            mWatermarkShowRectF = null;
            return this;
        }

        /**
         * 设置水印显示模式
         *
         * @param showMode 显示模式
         * @return
         */
        public Builder setWatermarkShowMode(int showMode) {
            mWatermarkShowMode = showMode;
            return this;
        }

        /**
         * 设置添加媒体时长限制
         *
         * @param importDuration 媒体时长（单位秒）
         * @return
         */
        public Builder setImportVideoDuration(float importDuration) {
            mImportVideoDuration = importDuration;
            if (importDuration <= 0) {
                mImportVideoDuration = 0;
            }
            return this;
        }

        /**
         * 设置是否使用自定义导出引导界面
         *
         * @param useCustomExportGuide
         * @return
         */
        public Builder useCustomExportGuide(boolean useCustomExportGuide) {
            mUseCustomExportGuide = useCustomExportGuide;
            return this;
        }

        /**
         * 设置是否弹出导出分辨率界面
         *
         * @param useExportVideoSizeDialog
         * @return
         */
        public Builder useExportVideoSizeDialog(boolean useExportVideoSizeDialog) {
            mUseExportVideoSizeDialog = useExportVideoSizeDialog;
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

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "190108ExportConfig";
    private static final int VER = 2;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }

        dest.writeInt(this.watermarkShowMode);
        dest.writeByte(this.useCustomExportGuide ? (byte) 1 : (byte) 0);
        dest.writeByte(this.useExportVideoSizeDialog ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.watermarkPortLayoutRectF, flags);
        dest.writeParcelable(this.watermarkLandLayoutRectF, flags);

        dest.writeString(this.saveDir);
        dest.writeFloat(this.trailerFadeDuration);
        dest.writeString(this.trailerPath);
        dest.writeFloat(this.trailerDuration);
        dest.writeFloat(this.exportVideoDuration);
        dest.writeString(this.watermarkPath);
        dest.writeByte(this.enableTextWatermark ? (byte) 1 : (byte) 0);
        dest.writeString(this.textWatermarkContent);
        dest.writeInt(this.textWatermarkSize);
        dest.writeInt(this.textWatermarkColor);
        dest.writeInt(this.textWatermarkShadowColor);
        dest.writeParcelable(this.watermarkShowRectF, flags);
        dest.writeInt(this.exportVideoFrameRate);
        dest.writeDouble(this.exportVideoBitRate);
        dest.writeInt(this.exportVideoMaxWH);
        dest.writeFloat(this.importVideoDuration);

    }

    protected ExportConfiguration(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();

            this.watermarkShowMode = in.readInt();
            this.useCustomExportGuide = in.readByte() != 0;
            this.useExportVideoSizeDialog = in.readByte() != 0;
            if (tVer >= 2) {
                this.watermarkPortLayoutRectF = in.readParcelable(RectF.class.getClassLoader());
                this.watermarkLandLayoutRectF = in.readParcelable(RectF.class.getClassLoader());
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);

        }

        this.saveDir = in.readString();
        this.trailerFadeDuration = in.readFloat();
        this.trailerPath = in.readString();
        this.trailerDuration = in.readFloat();
        this.exportVideoDuration = in.readFloat();
        this.watermarkPath = in.readString();
        this.enableTextWatermark = in.readByte() != 0;
        this.textWatermarkContent = in.readString();
        this.textWatermarkSize = in.readInt();
        this.textWatermarkColor = in.readInt();
        this.textWatermarkShadowColor = in.readInt();
        this.watermarkShowRectF = in.readParcelable(RectF.class.getClassLoader());
        this.exportVideoFrameRate = in.readInt();
        this.exportVideoBitRate = in.readDouble();
        this.exportVideoMaxWH = in.readInt();
        this.importVideoDuration = in.readFloat();

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
