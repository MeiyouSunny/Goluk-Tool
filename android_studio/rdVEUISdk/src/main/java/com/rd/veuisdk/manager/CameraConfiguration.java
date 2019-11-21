package com.rd.veuisdk.manager;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.manager.VEOSDBuilder.OSDState;
import com.rd.veuisdk.model.CloudAuthorizationInfo;

import java.util.ArrayList;

/**
 * RdVEUISdk录制摄像配置类
 */
public class CameraConfiguration implements Parcelable {

    /**
     * 录制界面方向定义：<br>
     * 自动旋转，根据系统设置自动选择方向
     */
    public static final int ORIENTATION_AUTO = 0;
    /**
     * 录制界面方向定义：<br>
     * 固定竖屏
     */
    public static final int ORIENTATION_PORTRAIT = 1;
    /**
     * 录制界面方向定义：<br>
     * 强制横屏
     */
    public static final int ORIENTATION_LANDSCAPE = 2;

    /**
     * 视频导出码率(Mbps)
     */
    private final double recordVideoBitRate;
    /**
     * 视频录制关键帧间隔时间(秒),设置为0代表全关键帧,默认1
     */
    public final int recordVideoKeyFrameTime;

    /**
     * 视频录制最大宽度
     */
    private int recordVideoMaxWH = 640;
    /**
     * 视频录制帧率
     */
    private int recordVideoFrameRate = 24;

    /**
     * 视频导出码率(bps)
     *
     * @return 码率(bps)
     */
    public int getRecordVideoBitRate() {
        return (int) (recordVideoBitRate * 1000 * 1000);
    }

    /**
     * 获取录制尺寸
     *
     * @param aspectRatio 输出比例
     * @param size        输出的录制尺寸
     * @param calcSquare  是否根据16:9计算1:1方形尺寸
     * @return 最大宽度
     */
    public boolean getRecordVideoSize(float aspectRatio, VirtualVideo.Size size,
                                      boolean align, boolean calcSquare) {
        if (aspectRatio > 0.0f) {
            size.set(0, recordVideoMaxWH);
            VirtualVideo.getMediaObjectOutSize(new ArrayList<Scene>(), aspectRatio, size, align, calcSquare);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取视频录制帧率
     *
     * @return 视频录制帧率
     */
    public int getRecordVideoFrameRate() {
        return recordVideoFrameRate;
    }

    /**
     * 是否支持拍照模式下点击拍摄按钮立即返回
     */
    // public boolean takePhotoReturn = false;
    /**
     * 设置限制录制的视频最大时长 单位为秒 0代表没有最大时间限制
     */
    public final int videoMaxTime;
    /**
     * 设置限制录制的视频最小时长 单位为秒 0代表没有最小时间限制
     */
    public final int videoMinTime;
    /**
     * 录制界面启动页面 CODE 值 WIDE_SCREEN_CAN_CHANGE 代表默认启动16：9宽屏录制界面并可切换到1：1界面
     */
    public static final int WIDE_SCREEN_CAN_CHANGE = 0;

    /**
     * 录制界面启动页面 CODE 值 SQUARE_SCREEN_CAN_CHANGE 代表默认启动1：1界面并可切换到16：9宽屏录制界面
     */
    public static final int SQUARE_SCREEN_CAN_CHANGE = 1;

    /**
     * 录制界面启动页面 CODE 值 ONLY_SQUARE_SCREEN 代表默认启动1：1界面并不可切换到16：9宽屏录制界面
     */
    public static final int ONLY_SQUARE_SCREEN = 2;

    /**
     * 录制界面启动页面 CODE 值 ONLY_WIDE_SCREEN 代表仅启动16：9宽屏录制界面并不可切换到1：1界面
     */
    public static final int ONLY_WIDE_SCREEN = 3;
    /**
     * 录制界面启动默认页面
     */
    public final int cameraUIType;

    /**
     * 录制时静音
     */
    public final boolean audioMute;
    /**
     * 默认后置摄像头
     */
    public final boolean dafaultRearCamera;
    /**
     * 是否允许多次拍摄
     */
    public final boolean useMultiShoot;
    /**
     * 单次拍摄是否将媒体保存至相册
     */
    public final boolean isSaveToAlbum;
    /**
     * 是否使用水印
     */
    public final boolean enableWatermark;
    /**
     * 片尾水印时长 (0-1.0f) 单位秒
     */
    public final float cameraTrailerTime;
    // 相机水印片头、片尾 水印时长 (0-1.0f) 单位秒
    public final float cameraOsdHeader;
    public final float cameraOsdEnd;

    /**
     * 录制时支持播放音乐
     */
    public final boolean enablePlayMusic;

    /**
     * 是否显示相册
     */
    public final boolean enableAlbum;
    /**
     * 是否使用自定义相册
     */
    public final boolean useCustomAlbum;

    /**
     * 是否开启人脸贴纸功能
     */
    public final boolean enableFaceU;

    /**
     * 是否加密录制文件
     */
    public final boolean enableAntiChange;

    /**
     * 设置水印显示区域
     */
    public final RectF cameraWatermarkRectF;

    /**
     * 是否隐藏mv
     */
    public final boolean hideMV;

    /**
     * 云音乐
     */
    public final String cloudMusicUrl;


    /**
     * 云音乐-分类
     */
    public final String cloudMusicTypeUrl;
    //云音乐授权
    public final CloudAuthorizationInfo mCloudAuthorizationInfo;

    public final String fitlerUrl;

    /**
     * 是否隐藏视频录制
     */
    public final boolean hideRec;

    /**
     * 是否隐藏相机
     */
    public final boolean hidePhoto;
    /**
     * 设置MV最小时长 单位为秒 设置0将无限制
     */
    public final int cameraMVMinTime;
    /**
     * 设置MV最大时长 单位为秒 设置0将默认为15秒
     */
    public final int cameraMVMaxTime;

    public final byte[] pack;
    /**
     * 录制方向默认可自定旋转
     */
    public final int orientation;

    public final boolean enableBeauty;

    /**
     * 前置输出镜像
     */
    public final boolean enableFrontMirror;

    public CameraConfiguration(Builder builder) {
        this.recordVideoBitRate = builder.mRecordVideoBitRate;
        this.recordVideoKeyFrameTime = builder.mRecordVideoKeyFrameTime;
        this.recordVideoFrameRate = builder.mRecordVideoFrameRate;
        this.recordVideoMaxWH = builder.mRecordVideoMaxWH;

        this.cameraUIType = builder.mCameraUIType;
        this.audioMute = builder.mAudioMute;
        this.dafaultRearCamera = builder.mDefaultRearCamera;
        this.useMultiShoot = builder.mUseMultiShoot;
        this.isSaveToAlbum = builder.mIsSaveToAlbum;
        this.enableWatermark = builder.mEnableWatermark;

        this.enableFaceU = builder.mEnableFaceu;
        this.enableAntiChange = builder.mEnableAntiChange;
        this.pack = builder.pack;
        this.enableAlbum = builder.mEnableAlbum;
        this.enablePlayMusic = builder.mEnablePlayMusic;

        this.useCustomAlbum = builder.mUseCustomAlbum;

        this.hideMV = builder.mHideMV;
        this.hidePhoto = builder.mHidePhoto;

        int tmin = 0, tmax = 0;


        tmin = Math.max(0, builder.mCameraMVMinTime);
        if (builder.mCameraMVMaxTime <= builder.mCameraMVMinTime && builder.mCameraMVMaxTime != 0) {
            //最大值无效
            tmax = 0;
        } else {
            tmax = Math.max(0, builder.mCameraMVMaxTime);
        }

        this.cameraMVMinTime = tmin;
        this.cameraMVMaxTime = tmax;
        if (hideMV && hidePhoto && builder.mHideRec) {
            Log.e(this.toString(), "不能同时隐藏所有功能，现已显示视频拍摄功能");
            this.hideRec = false;
        } else {
            this.hideRec = builder.mHideRec;
        }

        this.cameraWatermarkRectF = builder.mCameraWatermarkRectF;


        tmin = 0;
        tmax = 0;
        tmin = Math.max(0, builder.mVideoMinTime);
        if (builder.mVideoMaxTime <= builder.mVideoMinTime && builder.mVideoMaxTime != 0) {
            //最大值无效
            tmax = 0;
        } else {
            tmax = Math.max(0, builder.mVideoMaxTime);
        }

        this.videoMinTime = tmin;
        this.videoMaxTime = tmax;

        this.cameraOsdHeader = builder.mCameraOsdHeader;
        this.cameraOsdEnd = builder.mCameraOsdEnd;
        this.cameraTrailerTime = builder.mCameraOsdEnd;
        this.enableFrontMirror = builder.enableFrontMirror;
        this.orientation = builder.mOrientation;
        this.enableBeauty = builder.enableBeauty;
        this.cloudMusicUrl = builder.mCloudMusicUrl;
        this.cloudMusicTypeUrl = builder.mCloudMusicTypeUrl;
        this.fitlerUrl = builder.fitlerUrl;
        this.mCloudAuthorizationInfo = builder.mCloudAuthorizationInfo;
    }

    /**
     * Builder class for {@link CameraConfiguration} objects.
     */
    public static class Builder {
        private boolean mAudioMute;
        private int mVideoMaxTime = 0;
        private int mVideoMinTime = 0;
        private int mCameraUIType = 0;
        private boolean mDefaultRearCamera = false;
        private boolean mUseMultiShoot = false;
        private boolean mIsSaveToAlbum = false;
        private boolean mEnableWatermark = false;
        private float mCameraOsdHeader = 0f;
        private float mCameraOsdEnd = 0f;
        private boolean mEnableAlbum = true;
        private boolean mUseCustomAlbum = false;
        private boolean mEnablePlayMusic = false;

        private boolean mEnableFaceu = false;
        private boolean mEnableAntiChange = false;
        private byte[] pack = null;
        private RectF mCameraWatermarkRectF = null;

        private boolean mHideMV = false;
        private boolean mHidePhoto = false;
        private boolean mHideRec = false;

        private int mCameraMVMinTime = 0;
        private int mCameraMVMaxTime = 0;
        private boolean enableFrontMirror = false;
        private int mOrientation = ORIENTATION_AUTO;
        private boolean enableBeauty = true;

        private double mRecordVideoBitRate = 4;
        private int mRecordVideoKeyFrameTime = 1;
        private int mRecordVideoMaxWH = 640;
        private int mRecordVideoFrameRate = 24;

        /**
         * 滤镜
         *
         * @param url
         * @return
         */
        public Builder setFilterUrl(String url) {
            this.fitlerUrl = url;
            return this;
        }

        private String fitlerUrl;

        /**
         * 录制界面的云音乐
         *
         * @param url
         * @return
         */
        public Builder setCloudMusicUrl(String url) {
            mCloudMusicUrl = url;
            return this;
        }

        /**
         * * 录制界面的云音乐
         *
         * @param musicTypeUrl 分类
         * @param url          单个类型的云音乐请求接口 （支持分页）
         * @return
         */
        public Builder setCloudMusicUrl(String musicTypeUrl, String url) {
            mCloudMusicTypeUrl = musicTypeUrl;
            mCloudMusicUrl = url;
            return this;
        }

        private String mCloudMusicUrl = "";
        private String mCloudMusicTypeUrl = "";


        private CloudAuthorizationInfo mCloudAuthorizationInfo = null;

        /**
         * 云音乐
         *
         * @param url                云音乐地址
         * @param artist             艺术家
         * @param homepageTitle      个人中心标题
         * @param homepageUrl        个人中心Url
         * @param authorizationTitle 证书标题
         * @param authorizationUrl   证书Url
         * @return
         */
        public Builder setCloudMusicUrl(String url, String artist, String homepageTitle, String homepageUrl, String authorizationTitle, String authorizationUrl) {
            setCloudMusicUrl(url);
            this.mCloudAuthorizationInfo = new CloudAuthorizationInfo(artist, homepageTitle, homepageUrl, authorizationTitle, authorizationUrl);
            return this;
        }

        /**
         * 云音乐
         *
         * @param musicTypeUrl       云音乐分类地址
         * @param url                云音乐地址（支持分页）
         * @param artist             艺术家
         * @param homepageTitle      个人中心标题
         * @param homepageUrl        个人中心Url
         * @param authorizationTitle 证书标题
         * @param authorizationUrl   证书Url
         * @return
         */
        public Builder setCloudMusicUrl(String musicTypeUrl, String url, String artist, String homepageTitle, String homepageUrl, String authorizationTitle, String authorizationUrl) {
            setCloudMusicUrl(musicTypeUrl, url);
            this.mCloudAuthorizationInfo = new CloudAuthorizationInfo(artist, homepageTitle, homepageUrl, authorizationTitle, authorizationUrl);
            return this;
        }

        /**
         * 设置录制码率
         *
         * @param recordVideoBitRate 码率(Mbps)
         * @return
         */
        public Builder setRecordVideoBitRate(double recordVideoBitRate) {
            mRecordVideoBitRate = Math.max(1, recordVideoBitRate);
            return this;
        }

        /**
         * 设置录制时关键帧间隔时间（秒为单位）
         *
         * @param recordVideoKeyFrameTime 关键帧间隔时间（秒为单位）
         * @return
         */
        public Builder setRecordVideoKeyFrameTime(int recordVideoKeyFrameTime) {
            mRecordVideoKeyFrameTime = Math.max(0, recordVideoKeyFrameTime);
            return this;
        }

        /**
         * 设置录制时尺寸最大边
         *
         * @param recordVideoMaxWH 录制时尺寸最大边（默认为640)
         * @return
         */
        public Builder setRecordVideoMaxWH(int recordVideoMaxWH) {
            mRecordVideoMaxWH = Math.max(176, Math.min(recordVideoMaxWH, 3840));
            return this;
        }

        /**
         * 设置录制帧率
         *
         * @param recordVideoFrameRate 录制帧率
         * @return
         */
        public Builder setRecordVideoFrameRate(int recordVideoFrameRate) {
            mRecordVideoFrameRate = Math.max(10, Math.min(30, recordVideoFrameRate));
            return this;
        }

        /**
         * 是否开启美颜
         *
         * @param enable 开启美颜
         * @return Builder
         */
        public Builder enableBeauty(boolean enable) {
            this.enableBeauty = enable;
            return this;
        }

        /**
         * 设置录制界面的默认方向(仅全屏录制模式时有效 CameraConfiguration#ONLY_WIDE_SCREEN)
         *
         * @param orientation 需指定的横竖屏方向 可选值为:<br>
         *                    自动旋转：{@link CameraConfiguration#ORIENTATION_AUTO}<br>
         *                    固定竖屏：{@link CameraConfiguration#ORIENTATION_PORTRAIT}<br>
         *                    固定横屏：{@link CameraConfiguration#ORIENTATION_LANDSCAPE}<br>
         */
        public Builder setOrientation(int orientation) {
            this.mOrientation = Math.max(ORIENTATION_AUTO,
                    Math.min(orientation, ORIENTATION_LANDSCAPE));
            return this;
        }

        /***
         * 是否启用前置输出镜像
         *
         * @param enable
         * @return
         */
        public Builder enableFrontMirror(boolean enable) {
            enableFrontMirror = enable;
            return this;
        }

        /**
         * 是否开启人脸贴纸道具功能
         *
         * @param enable true 开启;flase 关闭
         * @return
         */
        public Builder enableFaceu(boolean enable) {
            mEnableFaceu = enable;
            return this;
        }

        /**
         * 是否支持防纂改录制
         *
         * @param enable true为支持
         * @return
         */
        public Builder enableAntiChange(boolean enable) {
            mEnableAntiChange = enable;
            return this;
        }

        /**
         * 设置人脸贴纸鉴权证书
         *
         * @param packBytes 证书
         * @return
         */
        public Builder setPack(byte[] packBytes) {
            pack = packBytes;
            return this;
        }

        /**
         * 确定是否支持拍照模式下点击拍照按钮立即返回
         *
         * @param taskPhotoReturn 是否立即返回
         */
        @Deprecated
        public Builder setTakePhotoReturn(boolean taskPhotoReturn) {
            // this.mTakePhotoReturn = taskPhotoReturn;
            this.mUseMultiShoot = !taskPhotoReturn;
            return this;
        }

        /**
         * 设置限制录制的视频最大时长 单位为秒 0代表没有最大时间限制
         *
         * @param maxTime 视频时长 单位为秒 0代表没有时间限制
         */
        public Builder setVideoMaxTime(int maxTime) {
            if (maxTime < 0) {
                maxTime = 0;
            }
            this.mVideoMaxTime = maxTime;
            return this;
        }

        /**
         * 设置限制录制的视频最小时长 单位为秒 0代表没有最小时间限制
         *
         * @param minTime 视频时长 单位为秒 0代表没有时间限制
         */
        public Builder setVideoMinTime(int minTime) {
            if (minTime < 0) {
                minTime = 0;
            }
            this.mVideoMinTime = minTime;
            return this;
        }

        /**
         * 设置MV最小时长 单位为秒 设置0将无限制
         *
         * @param MVMinTime MV时长 单位为秒 设置0将无限制
         */
        public Builder setCameraMVMinTime(int MVMinTime) {
            if (MVMinTime <= 0) {
                MVMinTime = 0;
            }
            this.mCameraMVMinTime = MVMinTime;
            return this;
        }

        /**
         * 设置MV最大时长 单位为秒 设置0将设为默认15秒
         *
         * @param MVMaxTime MV时长 单位为秒 设置0将设为默认15秒
         */
        public Builder setCameraMVMaxTime(int MVMaxTime) {
            if (MVMaxTime <= 0) {
                MVMaxTime = 15;
            }
            this.mCameraMVMaxTime = MVMaxTime;
            return this;
        }

        /**
         * 设置录制时启动默认页面方式
         *
         * @param type 录制时启动默认页面方式 可选值为:<br>
         *             {@link CameraConfiguration#WIDE_SCREEN_CAN_CHANGE}<br>
         *             {@link CameraConfiguration#SQUARE_SCREEN_CAN_CHANGE}<br>
         *             {@link CameraConfiguration#ONLY_SQUARE_SCREEN}<br>
         *             {@link CameraConfiguration#ONLY_WIDE_SCREEN}<br>
         */
        public Builder setCameraUIType(int type) {
            this.mCameraUIType = Math.max(WIDE_SCREEN_CAN_CHANGE,
                    Math.min(type, ONLY_WIDE_SCREEN));
            return this;
        }

        /**
         * 设置录制时静音，默认不静音
         *
         * @param mute 是否静音,true代表静音录制
         */
        public Builder setAudioMute(boolean mute) {
            this.mAudioMute = mute;
            return this;
        }

        /**
         * 设置进入录制界面，默认使用前置或后置摄像头
         *
         * @param rearCamera true代表默认为后置摄像头
         */
        public Builder setDefaultRearCamera(boolean rearCamera) {
            this.mDefaultRearCamera = rearCamera;
            return this;
        }

        /**
         * 设置是否使用多次拍摄
         *
         * @param useMultiShoot true代表使用
         */
        public Builder useMultiShoot(boolean useMultiShoot) {
            this.mUseMultiShoot = useMultiShoot;
            return this;
        }

        /**
         * 单次拍摄是否保存至相册
         *
         * @param save true代表保存
         */
        public Builder setSingleCameraSaveToAlbum(boolean save) {
            this.mIsSaveToAlbum = save;
            return this;
        }

        /**
         * 设置是否显示相册按钮
         *
         * @param enable true 代表显示相册按钮
         */
        public Builder enableAlbum(boolean enable) {
            this.mEnableAlbum = enable;
            return this;
        }

        /**
         * 设置是否可以录制的时候播放音乐
         *
         * @param enable 为true代表录制时播放音乐
         */
        public Builder enablePlayMusic(boolean enable) {
            this.mEnablePlayMusic = enable;
            return this;
        }

        /**
         * 设置是否使用自定义相册
         *
         * @param useCustomAlbum 设置true为使用，设置false将调用秀拍客SDK内置相册
         */
        public Builder useCustomAlbum(boolean useCustomAlbum) {
            this.mUseCustomAlbum = useCustomAlbum;
            return this;
        }

        /**
         * 设置是否使用录制水印
         *
         * @param enable 设置true为使用
         */
        public Builder enableWatermark(boolean enable) {
            this.mEnableWatermark = enable;
            return this;
        }

        /**
         * 设置片尾时长
         *
         * @param trailerTime 片尾时长，单位(秒)
         */
        @Deprecated
        public Builder setCameraTrailerTime(float trailerTime) {
            this.mCameraOsdEnd = Math.min(2, Math.max(0, trailerTime));
            return this;
        }

        /**
         * 设置相机片头、片尾水印显示时长
         *
         * @param state 片头或片尾
         * @param time  水印显示时长 (0-2秒)单位:秒
         * @return
         */
        public Builder setCameraTrailerTime(OSDState state, float time) {
            if (state == OSDState.header) {
                this.mCameraOsdHeader = Math.min(2, Math.max(0, time));
            } else if (state == OSDState.end) {
                this.mCameraOsdEnd = Math.min(2, Math.max(0, time));
            } else {
                Log.e("CameraConfiguration", "无效的设置");
            }
            return this;
        }

        /**
         * 设置是否使用隐藏视频拍摄
         *
         * @param hide 设置true为隐藏
         */
        public Builder hideRec(boolean hide) {
            this.mHideRec = hide;
            return this;
        }

        /**
         * 设置是否使用隐藏MV
         *
         * @param hide 设置true为隐藏
         */
        public Builder hideMV(boolean hide) {
            this.mHideMV = hide;
            return this;
        }

        /**
         * 设置是否使用隐藏相机
         *
         * @param hide 设置true为隐藏
         */
        public Builder hidePhoto(boolean hide) {
            this.mHidePhoto = hide;
            return this;
        }

        public CameraConfiguration get() {
            return new CameraConfiguration(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "181127cameraconfig";
    private final int ver = 2;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(ver);
        }

        dest.writeString(cloudMusicTypeUrl);

        //新增部分字段
        dest.writeParcelable(this.mCloudAuthorizationInfo, flags);

        //*********************************顺序不能改变。支持草稿箱
        dest.writeDouble(this.recordVideoBitRate);
        dest.writeInt(this.recordVideoKeyFrameTime);
        dest.writeInt(this.recordVideoMaxWH);
        dest.writeInt(this.recordVideoFrameRate);
        dest.writeInt(this.videoMaxTime);
        dest.writeInt(this.videoMinTime);
        dest.writeInt(this.cameraUIType);
        dest.writeByte(this.audioMute ? (byte) 1 : (byte) 0);
        dest.writeByte(this.dafaultRearCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.useMultiShoot ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSaveToAlbum ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableWatermark ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.cameraTrailerTime);
        dest.writeFloat(this.cameraOsdHeader);
        dest.writeFloat(this.cameraOsdEnd);
        dest.writeByte(this.enablePlayMusic ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableAlbum ? (byte) 1 : (byte) 0);
        dest.writeByte(this.useCustomAlbum ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableFaceU ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableAntiChange ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.cameraWatermarkRectF, flags);
        dest.writeByte(this.hideMV ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hideRec ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hidePhoto ? (byte) 1 : (byte) 0);
        dest.writeInt(this.cameraMVMinTime);
        dest.writeInt(this.cameraMVMaxTime);
        dest.writeByteArray(this.pack);
        dest.writeInt(this.orientation);
        dest.writeByte(this.enableBeauty ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableFrontMirror ? (byte) 1 : (byte) 0);
        dest.writeString(this.cloudMusicUrl);
        dest.writeString(this.fitlerUrl);
    }

    protected CameraConfiguration(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();
            if (tVer >= 2) {
                cloudMusicTypeUrl = in.readString();
            } else {
                cloudMusicTypeUrl = null;
            }
            this.mCloudAuthorizationInfo = in.readParcelable(CloudAuthorizationInfo.class.getClassLoader());
        } else {
            cloudMusicTypeUrl = null;
            this.mCloudAuthorizationInfo = null;
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        this.recordVideoBitRate = in.readDouble();
        this.recordVideoKeyFrameTime = in.readInt();
        this.recordVideoMaxWH = in.readInt();
        this.recordVideoFrameRate = in.readInt();
        this.videoMaxTime = in.readInt();
        this.videoMinTime = in.readInt();
        this.cameraUIType = in.readInt();
        this.audioMute = in.readByte() != 0;
        this.dafaultRearCamera = in.readByte() != 0;
        this.useMultiShoot = in.readByte() != 0;
        this.isSaveToAlbum = in.readByte() != 0;
        this.enableWatermark = in.readByte() != 0;
        this.cameraTrailerTime = in.readFloat();
        this.cameraOsdHeader = in.readFloat();
        this.cameraOsdEnd = in.readFloat();
        this.enablePlayMusic = in.readByte() != 0;
        this.enableAlbum = in.readByte() != 0;
        this.useCustomAlbum = in.readByte() != 0;
        this.enableFaceU = in.readByte() != 0;
        this.enableAntiChange = in.readByte() != 0;
        this.cameraWatermarkRectF = in.readParcelable(RectF.class.getClassLoader());
        this.hideMV = in.readByte() != 0;
        this.hideRec = in.readByte() != 0;
        this.hidePhoto = in.readByte() != 0;
        this.cameraMVMinTime = in.readInt();
        this.cameraMVMaxTime = in.readInt();
        this.pack = in.createByteArray();
        this.orientation = in.readInt();
        this.enableBeauty = in.readByte() != 0;
        this.enableFrontMirror = in.readByte() != 0;
        this.cloudMusicUrl = in.readString();
        this.fitlerUrl = in.readString();
    }

    public static final Parcelable.Creator<CameraConfiguration> CREATOR = new Parcelable.Creator<CameraConfiguration>() {
        @Override
        public CameraConfiguration createFromParcel(Parcel source) {
            return new CameraConfiguration(source);
        }

        @Override
        public CameraConfiguration[] newArray(int size) {
            return new CameraConfiguration[size];
        }
    };
}
