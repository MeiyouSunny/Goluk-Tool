package com.rd.veuisdk.manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.rd.veuisdk.model.CloudAuthorizationInfo;

/**
 * RdVEUISdk界面配置类
 */
public class UIConfiguration implements Parcelable {


    public boolean isHideCover() {
        return hideCover;
    }

    public boolean isHideGraffiti() {
        return hideGraffiti;
    }

    public boolean isHideText() {
        return hideText;
    }


    public boolean isHideFilter() {
        return hideFilter;
    }

    public boolean isHideCollage() {
        return hideCollage;
    }

    public boolean isHideMusicEffect() {
        return hideSoundEffect;
    }


    public boolean isHideTitling() {
        return hideTitling;
    }


    public boolean isHideSpecialEffects() {
        return hideSpecialEffects;
    }

    public boolean isHideEffects() {
        return hideEffects || TextUtils.isEmpty(getEffectUrl());
    }

    /**
     * @return
     */
    public boolean isHideDewatermark() {
        return hideDewatermark;
    }


    public boolean isHideSoundTrack() {
        return hideSoundTrack;
    }

    public boolean isHideSound() {
        return hideSound;
    }

    public boolean isHideMusicMany() {
        return hideMusicMany;
    }

    public boolean isHideVolume() {
        return hideVolume;
    }

    public boolean isHideDubbing() {
        return hideDubbing;
    }


    public boolean isHideSort() {
        return hideSort;
    }


    public boolean isHideProportion() {
        return hideProportion;
    }


    public boolean isHidePartEdit() {
        return hidePartEdit;
    }


    public boolean isHideTrim() {
        return hideTrim;
    }


    public boolean isHideSplit() {
        return hideSplit;
    }


    public boolean isHideSpeed() {
        return hideSpeed;
    }


    public boolean isHideDuration() {
        return hideDuration;
    }


    public boolean isHideEdit() {
        return hideEdit;
    }


    public boolean isHideCopy() {
        return hideCopy;
    }


    public boolean isHideReverse() {
        return hideReverse;
    }


    public boolean isHideTransition() {
        return hideTransition;
    }

    public boolean isEnableWizard() {
        return enableWizard;
    }


    // 片段编辑
    private boolean hideSort = false;
    private boolean hideProportion = false;
    private boolean hideTrim = false;
    private boolean hideSplit = false;
    private boolean hideSpeed = false;
    private boolean hideDuration = false;
    private boolean hideEdit = false;
    private boolean hideCopy = false;
    private boolean hideText = false;
    private boolean hideReverse = false;//倒序
    private boolean hideTransition = false;

    // 编辑与导出
    private boolean hidePartEdit = false;
    private boolean hideDubbing = false; //隐藏配音
    private boolean hideSoundTrack = false;
    private boolean hideSound = false;//音效
    private boolean hideMusicMany = false;//多段配乐
    private boolean hideVolume = false;//音量
    private boolean hideSpecialEffects = false;
    private boolean hideEffects = false;
    private boolean hideTitling = false;
    private boolean hideFilter = false;
    private boolean hideSoundEffect = false;
    private boolean hideDewatermark = false;
    private boolean hideCollage = false;

    private boolean hideCover = false;
    private boolean hideGraffiti = false;

    @Deprecated
    private boolean hideEditAndExportSort = true; //编辑导出界面新增调序

    /**
     * 是否使用随机转场
     */
    private boolean enableRandTransition = false;

    public boolean isEnableRandTransition() {
        return enableRandTransition;
    }

    /**
     * 是否显示草稿箱
     *
     * @return true 显示;false 隐藏
     */
    public boolean isEnableDraft() {
        return enableDraft;
    }

    private boolean enableDraft = true;


    //向导化
    private boolean enableWizard = false;

    /**
     * 相册支持格式：<br>
     * 默认，同时支持图片和视频
     */
    public static final int ALBUM_SUPPORT_DEFAULT = 0;
    /**
     * 相册支持格式：<br>
     * 仅支持视频
     */
    public static final int ALBUM_SUPPORT_VIDEO_ONLY = 1;
    /**
     * 相册支持格式：<br>
     * 仅支持图片
     */
    public static final int ALBUM_SUPPORT_IMAGE_ONLY = 2;

    /**
     * 滤镜界面：<br>
     * 界面一 (分组滤镜 acv)
     */
    public static final int FILTER_LAYOUT_1 = 1;
    /**
     * 滤镜界面：<br>
     * 界面二  (jlk-滤镜 acv)
     */
    public static final int FILTER_LAYOUT_2 = 2;


    /**
     * 滤镜3 （本地lookup）
     */
    public static final int FILTER_LAYOUT_3 = 3;


    /**
     * 配音方式1：<br>
     * 界面一
     */
    public static final int VOICE_LAYOUT_1 = 1;
    /**
     * 配音方式2：<br>
     * 界面二
     */
    public static final int VOICE_LAYOUT_2 = 2;

    /**
     * 视频比例定义：<br>
     * 自动，根据视频资源自动选择最佳
     */
    public static final int PROPORTION_AUTO = 0;
    /**
     * 视频比例定义：<br>
     * 1:1
     */
    public static final int PROPORTION_SQUARE = 1;
    /**
     * 视频比例定义：<br>
     * 16:9
     */
    public static final int PROPORTION_LANDSCAPE = 2;

    //视频比例、相册数量、支持格式类型、视频布局类型、
    //启用相册相机、隐藏音乐、自定义相册、打开编辑图片
    public final int videoProportion;
    public final int mediaCountLimit;
    public final int albumSupportFormatType;
    public final int voiceLayoutTpye;
    public final int filterLayoutTpye;
    public final boolean enableAlbumCamera;
    public final boolean hideMusic;
    public final boolean useCustomAlbum;
    public final boolean openEditbyPicture;

    // 是否打开mv功能，默认关闭
    public final boolean enableMV;
    public final String mvUrl;
    public final String newMvUrl;

    // AE相册、AE链接、自动重播
    public final boolean enableLottie;
    public final String lottieUrl;
    public final boolean enableAutoRepeat;

    //字幕、贴纸在mv之上
    public final boolean enableTitlingAndSpecialEffectOuter;

    public final String musicUrl;// 配乐2的背景音乐
    public final String newMusicUrl;// 配乐2的背景音乐

    public final String subUrl;//网络字幕
    public final String stickerUrl;//网络贴纸
    public final String fontUrl;//网络字体
    public String filterUrl;//网络滤镜
    public final String transitionUrl;//网络转场
    private String mEffectUrl;//网络特效

    public String getEffectUrl() {
        return mEffectUrl;
    }


    //资源分类
    public final String mResTypeUrl;
    //ae模板
    public final String mAEUrl;

    /**
     * 配乐2->云音乐
     */
    public final String cloudMusicUrl;
    public final String newCloudMusicUrl;
    public final String newCloudMusicTypeUrl;
    //云音乐授权
    public final CloudAuthorizationInfo mCloudAuthorizationInfo;

    /**
     * 音效
     */
    public final String soundTypeUrl;
    public final String soundUrl;

    //是否打开本地音乐
    public final boolean enableLocalMusic;

    /**
     * 片断编辑功能枚举
     */
    public enum ClipEditingModules {
        /**
         * 所有模块
         */
        ALL,

        /**
         * 视频调速
         */
        VIDEO_SPEED_CONTROL,

        /**
         * 设置图片时长
         */
        IMAGE_DURATION_CONTROL,

        /**
         * 复制
         */
        COPY,

        /**
         * 图片视频编辑
         */
        EDIT,

        /**
         * 视频比例
         */
        PROPORTION,

        /**
         * 调序
         */
        SORT,

        /**
         * 截取
         */
        TRIM,

        /**
         * 分割
         */
        SPLIT,

        /**
         * 文字板
         */
        TEXT,
        /**
         * 倒序
         */
        REVERSE,
        /**
         * 转场
         */
        TRANSITION;
    }

    /**
     * 编辑导出功能模块枚举
     */
    public enum EditAndExportModules {
        /**
         * 配乐
         */
        SOUNDTRACK,

        /**
         * 配音
         */
        DUBBING,

        /**
         * 音效
         */
        SOUND,

        /**
         * 多段配乐
         */
        MUSIC_MANY,

        /**
         * 音量
         */
        VOLUME,

        /**
         * 滤镜
         */
        FILTER,

        /**
         * 字幕
         */
        TITLING,

        /**
         * 贴纸
         */
        SPECIAL_EFFECTS,
        /**
         * 特效
         */
        EFFECTS,

        /**
         * 调序(编辑导出界面新增调序)
         */
        SORT,

        /**
         * 片段编辑
         */
        CLIP_EDITING,
    }

    private UIConfiguration(Builder builder) {
        this.enableDraft = builder.enableDraft;
        this.useCustomAlbum = builder.b_useCustomAlbum;
        this.albumSupportFormatType = builder.b_albumSupportFormatType;
        this.openEditbyPicture = builder.b_openEditbyPicture;
        this.mediaCountLimit = builder.b_mediaCountLimit;
        this.enableMV = builder.enableMV;
        this.enableLottie = builder.enableLottie;
        this.mvUrl = builder.mvUrl;
        this.lottieUrl = builder.lottieUrl;
        this.enableAutoRepeat = builder.enableAutoRepeat;
        this.hideMusic = builder.mHideMusic;
        this.filterLayoutTpye = builder.mFilterLayoutType;
        this.enableAlbumCamera = builder.mEnableAlbumCamera;
        this.mAEUrl = builder.aeUrl;
        this.mResTypeUrl = builder.resTypeUrl;
        if (enableMV) {
            this.videoProportion = PROPORTION_SQUARE;
            this.hideProportion = true;
        } else {
            this.videoProportion = builder.b_videoProportion;
        }
        if (hideMusic) {
            this.voiceLayoutTpye = UIConfiguration.VOICE_LAYOUT_1;
        } else {
            this.voiceLayoutTpye = builder.b_voiceLayoutType;
        }
        this.musicUrl = builder.musicUrl;
        this.cloudMusicUrl = builder.cloudMusicUrl;
        this.enableTitlingAndSpecialEffectOuter = builder.enableTitlingAllOuter;
        this.enableLocalMusic = builder.enableLocalMusic;


        // 片段编辑
        this.hideSort = builder.hideSort;
        this.hideProportion = builder.hideProportion;
        this.hideTrim = builder.hideTrim;
        this.hideSplit = builder.hideSplit;
        this.hideSpeed = builder.hideSpeed;
        this.hideDuration = builder.hideDuration;
        this.hideEdit = builder.hideEdit;
        this.hideCopy = builder.hideCopy;
        this.hideText = builder.hideText;
        this.hideReverse = builder.hideReverse;//倒序
        this.hideTransition = builder.hideTransition;

        // 编辑与导出
        this.hidePartEdit = builder.hidePartEdit;
        this.hideDubbing = builder.hideDubbing; //隐藏配音
        this.hideSoundTrack = builder.hideSoundTrack;
        this.hideSound = builder.hideSound;
        this.hideMusicMany = builder.hideMusicMany;
        this.hideVolume = builder.hideVolume;
        this.hideSpecialEffects = builder.hideSpecialEffects;
        this.hideEffects = builder.hideEffects;
        this.hideTitling = builder.hideTitling;
        this.hideFilter = builder.hideFilter;
        this.hideEditAndExportSort = builder.hideEditAndExportSort;


        this.enableWizard = builder.b_enableWizard;

        this.subUrl = builder.subUrl;
        this.newMusicUrl = builder.newMusicUrl;
        this.newMvUrl = builder.newMvUrl;
        this.stickerUrl = builder.stickerUrl;
        this.fontUrl = builder.fontUrl;

        this.filterUrl = builder.filterUrl;
        this.transitionUrl = builder.transitionUrl;

        this.newCloudMusicUrl = builder.newCloudMusicUrl;
        this.newCloudMusicTypeUrl = builder.newCloudMusicTypeUrl;
        /**
         * 音效
         */
        this.soundTypeUrl = builder.soundTypeUrl;
        this.soundUrl = builder.soundUrl;
        //云音乐证书部分
        this.mCloudAuthorizationInfo = builder.mCloudAuthorizationInfo;

        this.hideSoundEffect = builder.hideSoundEffect;

        this.mEffectUrl = builder.effectUrl;
        this.hideDewatermark = builder.hideDewatermark;

        this.enableRandTransition = builder.enableRandTransition;
        this.hideCollage = builder.hideCollage;

        this.hideCover = builder.hideCover;
        this.hideGraffiti = builder.hideGraffiti;
    }

    /**
     * Builder class for {@link UIConfiguration} objects.
     */
    public static class Builder {
        private int b_videoProportion = PROPORTION_AUTO;
        private int b_mediaCountLimit = 0;
        private boolean b_useCustomAlbum = false;
        private int b_voiceLayoutType = VOICE_LAYOUT_1;
        private boolean b_enableWizard = false;
        private int b_albumSupportFormatType = ALBUM_SUPPORT_DEFAULT;
        private boolean b_openEditbyPicture = false;
        private boolean enableMV = false;
        private boolean enableLottie = false;
        private boolean enableAutoRepeat = false;
        private int mFilterLayoutType = FILTER_LAYOUT_1;
        private boolean mEnableAlbumCamera = true;
        private boolean mHideMusic = false;
        private String mvUrl = "";
        private String lottieUrl = "";
        private boolean enableTitlingAllOuter = true;
        private String subUrl = "";//字幕
        //不设置就默认关闭
        private String musicUrl = "";
        private String cloudMusicUrl = "";
        private String newCloudMusicTypeUrl = ""; //云音乐分类
        private String newCloudMusicUrl = ""; //云音乐url

        private String soundTypeUrl = "";
        private String soundUrl = "";
        private CloudAuthorizationInfo mCloudAuthorizationInfo;

        private String newMusicUrl = "";//配乐2 新背景音乐
        private String newMvUrl = ""; //新mv

        private String stickerUrl;//网络贴纸
        private String fontUrl;//网络字体

        private String filterUrl;//网络滤镜
        private String transitionUrl;//网络转场
        private String effectUrl;//网络特效


        private String resTypeUrl;//资源分类的url
        private String aeUrl;

        /***
         *  由于部分功能下的资源太多(做分类加载)
         * 设置分类的地址  （已支持AE模板、特效 ）
         */
        public Builder setResouceTypeUrl(String url) {
            resTypeUrl = url;
            return this;
        }

        /**
         * AE模板 url
         */
        public Builder setAEUrl(String url) {
            aeUrl = url;
            return this;
        }


        private boolean enableLocalMusic = true;
        private boolean hideSoundEffect = false;


        // 片段编辑
        private boolean hideSort = false;
        private boolean hideProportion = false;
        private boolean hideTrim = false;
        private boolean hideSplit = false;
        private boolean hideSpeed = false;
        private boolean hideDuration = false;
        private boolean hideEdit = false;
        private boolean hideCopy = false;
        private boolean hideText = false;
        private boolean hideReverse = false;//倒序
        private boolean hideTransition = false;
        private boolean enableDraft = true;

        // 编辑与导出
        private boolean hidePartEdit = false;
        private boolean hideDubbing = false; //隐藏配音
        private boolean hideSoundTrack = false;
        private boolean hideSound = false;
        private boolean hideMusicMany = false;
        private boolean hideVolume = false;
        private boolean hideSpecialEffects = false;
        private boolean hideEffects = false;
        private boolean hideTitling = false;
        private boolean hideFilter = false;
        private boolean hideDewatermark = false;
        @Deprecated
        private boolean hideEditAndExportSort = false; //编辑排序
        private boolean hideCollage = false;

        private boolean hideCover = false;
        private boolean hideGraffiti = false;

        /**
         * 是否启用设置封面
         *
         * @param enable true 启用(默认)；false  关闭
         */
        public Builder enableCover(boolean enable) {
            hideCover = !enable;
            return this;
        }

        /**
         * 是否启用涂鸦
         *
         * @param enable true 启用(默认)；false  关闭
         */
        public Builder enableGraffiti(boolean enable) {
            hideGraffiti = !enable;
            return this;
        }


        /**
         * 是否启用编辑排序
         *
         * @param enable true 启用编辑排序 (默认)； false 关闭
         */
        private Builder enableEditSort(boolean enable) {
            hideEditAndExportSort = !enable;
            return this;
        }


        /**
         * 是否启用画中画
         *
         * @param enable true 启用画中画 (默认)； false 关闭画中画
         */
        public Builder enableCollage(boolean enable) {
            hideCollage = !enable;
            return this;
        }

        // only功能
        private boolean onlyModule = false;
        // 使用随机转场
        private boolean enableRandTransition = false;

        public Builder enableRandTransition(boolean enable) {
            enableRandTransition = enable;
            return this;
        }

        /**
         * 是否打开本地音乐
         *
         * @param enable 打开本地音乐
         */
        public Builder enableLocalMusic(boolean enable) {
            this.enableLocalMusic = enable;
            return this;
        }

        /**
         * 是否因此声音特效
         *
         * @param enable true 启用 (默认)；false
         */
        public Builder enableSoundEffect(boolean enable) {
            this.hideSoundEffect = !enable;
            return this;
        }

        /**
         * 是否启用去水印功能
         *
         * @param enable true 启用 (默认)；false
         * @return
         */
        public Builder enableDewatermark(boolean enable) {
            this.hideDewatermark = !enable;
            return this;
        }

        /**
         * 设置简单后台网络音乐url
         *
         * @param musicUrl 网络音乐url
         */
        @Deprecated
        public Builder setMusicUrl(String musicUrl) {
            this.musicUrl = musicUrl;
            return this;
        }

        /**
         * 配乐2 中的自定义音乐
         *
         * @param musicUrl
         * @return
         */
        public Builder setNewMusicUrl(String musicUrl) {
            this.newMusicUrl = musicUrl;
            return this;
        }

        /**
         * 设置网络滤镜
         *
         * @param url
         * @return
         */
        public Builder setFilterUrl(String url) {
            this.filterUrl = url;
            return this;
        }

        /**
         * 网络转场
         *
         * @param url
         * @return
         */
        public Builder setTransitionUrl(String url) {
            this.transitionUrl = url;
            return this;
        }

        /**
         * 网络特效
         *
         * @param url
         * @return
         */
        public Builder setEffectUrl(String url) {
            this.effectUrl = url;
            return this;
        }


        /**
         * 设置云音乐url
         *
         * @param url 云音乐url
         */
        @Deprecated
        public Builder setCloudMusicUrl(String url) {
            this.cloudMusicUrl = url;
            return this;
        }

        /**
         * 设置云音乐url
         *
         * @param url 云音乐url
         */
        @Deprecated
        public Builder setNewCloudMusicUrl(String url) {
            this.newCloudMusicUrl = url;
            return this;
        }

        /**
         * 设置音效
         *
         * @param typeUrl 音效分类
         * @param url     单个音效数据url
         */
        public Builder setSoundUrl(String typeUrl, String url) {
            this.soundTypeUrl = typeUrl;
            this.soundUrl = url;
            return this;
        }

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
        @Deprecated
        public Builder setNewCloudMusicUrl(String url, String artist, String homepageTitle, String homepageUrl, String authorizationTitle, String authorizationUrl) {
            return setNewCloudMusicUrl("", url, artist, homepageTitle, homepageUrl, authorizationTitle, authorizationUrl);
        }

        /**
         * 云音乐
         *
         * @param musicTypeUrl       云音乐分类支持分页
         * @param url                云音乐地址
         * @param artist             艺术家
         * @param homepageTitle      个人中心标题
         * @param homepageUrl        个人中心Url
         * @param authorizationTitle 证书标题
         * @param authorizationUrl   证书Url
         */
        public Builder setNewCloudMusicUrl(String musicTypeUrl, String url, String artist, String homepageTitle, String homepageUrl, String authorizationTitle, String authorizationUrl) {
            this.newCloudMusicTypeUrl = musicTypeUrl;
            this.newCloudMusicUrl = url;
            this.mCloudAuthorizationInfo = new CloudAuthorizationInfo(artist, homepageTitle, homepageUrl, authorizationTitle, authorizationUrl);
            return this;
        }

        /**
         * 启用字幕、贴纸在mv的外面(不受MV影响)
         *
         * @param enable 为true代表字幕在mv的外面
         */
        public Builder enableTitlingAndSpecialEffectOuter(boolean enable) {
            this.enableTitlingAllOuter = enable;
            return this;
        }


        /**
         * 设置相册界面是否显示跳转拍摄按钮(仅相册api生效)
         *
         * @param enable 是否显示跳转拍摄按钮(
         */
        public Builder enableAlbumCamera(boolean enable) {
            this.mEnableAlbumCamera = enable;
            return this;
        }


        /**
         * 启用自动重播
         *
         * @param enable true代表启用
         */
        public Builder enableAutoRepeat(boolean enable) {
            this.enableAutoRepeat = enable;
            return this;
        }

        /**
         * 配置配音模式
         *
         * @param voiceType UIConfiguration#VOICE_LAYOUT_1 <br/>
         *                  UIConfiguration#VOICE_LAYOUT_2
         */
        public Builder setVoiceLayoutType(int voiceType) {
            this.b_voiceLayoutType = Math.max(VOICE_LAYOUT_1,
                    Math.min(voiceType, VOICE_LAYOUT_2));
            return this;
        }


        /**
         * 设置视频比例
         *
         * @param proportion 需指定的视频比例 可选值为:<br>
         *                   自动：{@link UIConfiguration#PROPORTION_AUTO}<br>
         *                   1:1：{@link UIConfiguration#PROPORTION_SQUARE}<br>
         *                   16:9：{@link UIConfiguration#PROPORTION_LANDSCAPE}<br>
         */
        public Builder setVideoProportion(int proportion) {
            this.b_videoProportion = Math.max(PROPORTION_AUTO,
                    Math.min(proportion, PROPORTION_LANDSCAPE));
            return this;
        }

        /**
         * 设置相册支持格式
         *
         * @param format 需指定的格式类型 可选值为:<br>
         *               视频和图片：{@link UIConfiguration#ALBUM_SUPPORT_DEFAULT}<br>
         *               仅图片：{@link UIConfiguration#ALBUM_SUPPORT_IMAGE_ONLY}<br>
         *               仅视频：{@link UIConfiguration#ALBUM_SUPPORT_VIDEO_ONLY}<br>
         */
        public Builder setAlbumSupportFormat(int format) {
            this.b_albumSupportFormatType = Math.max(ALBUM_SUPPORT_DEFAULT,
                    Math.min(format, ALBUM_SUPPORT_IMAGE_ONLY));
            return this;
        }

        /**
         * 设置是否使用自定义相册
         *
         * @param useCustomAlbum 设置true为使用，设置false将调用秀拍客SDK内置相册
         */
        public Builder useCustomAlbum(boolean useCustomAlbum) {
            this.b_useCustomAlbum = useCustomAlbum;
            return this;
        }

        /**
         * 设置向导化
         *
         * @param enable true为片断编辑在前，编辑导出在后
         */
        public Builder enableWizard(boolean enable) {
            if (onlyModule && !enable) {
                Log.e(this.toString(), "只显示某一功能模块将强制向导化");
                return this;
            }
            this.b_enableWizard = enable;
            return this;
        }


        /**
         * 配置mv 默认关闭
         *
         * @param enable true 打开;flase 关闭
         * @param url    网络MV地址
         */
        public Builder enableNewMV(boolean enable, String url) {
            this.enableMV = enable;
            this.newMvUrl = url;
            return this;
        }

        /**
         * @param enable
         * @param url
         * @return
         */
        public Builder enableLottie(boolean enable, String url) {
            this.enableLottie = enable;
            this.lottieUrl = url;
            return this;
        }

        @Deprecated
        public Builder enableMV(boolean enable, String url) {
            this.enableMV = enable;
            this.mvUrl = url;
            return this;
        }

        /**
         * 设置自定义的字幕 (网络路径)
         *
         * @param url
         * @return
         */
        public Builder setTitlingUrl(String url) {
            this.subUrl = url;
            return this;
        }

        /**
         * 设置自定义的贴纸(网络路径)
         *
         * @param url
         * @return
         */
        public Builder setSpecialEffectsUrl(String url) {
            this.stickerUrl = url;
            return this;
        }

        /**
         * 设置自定义的字体 (网络路径)
         *
         * @param url
         * @return
         */
        public Builder setFontUrl(String url) {
            this.fontUrl = url;
            return this;
        }

        /**
         * 设置是否从相册打开编辑界面
         *
         * @param hide 为true代表隐藏片段编辑
         */
        public Builder openEditbyPicture(boolean hide) {
            this.b_openEditbyPicture = hide;
            return this;
        }

        /**
         * 设置滤镜界面风格
         *
         * @param type 需指定的格式类型 可选值为:<br>
         *             风格一：{@link UIConfiguration#FILTER_LAYOUT_1}<br>
         *             风格二：{@link UIConfiguration#FILTER_LAYOUT_2}<br>
         *             风格三：{@link UIConfiguration#FILTER_LAYOUT_3}<br>
         */
        public Builder setFilterType(int type) {
            this.mFilterLayoutType = Math.min(FILTER_LAYOUT_3,
                    Math.max(FILTER_LAYOUT_1, type));
            return this;
        }

        /**
         * 是否启用草稿箱功能
         *
         * @param enable true 启用(默认)；false 禁用
         * @return
         */
        public Builder enableDraft(boolean enable) {
            this.enableDraft = enable;
            return this;
        }


        /**
         * 限制相册选择媒体最大数量
         *
         * @param count 默认为0为不限制
         */
        public Builder setMediaCountLimit(int count) {
            if (count < 0) {
                count = 0;
            }
            this.b_mediaCountLimit = count;
            return this;
        }

        /**
         * 设置片断编辑功能模块显示与隐藏
         */
        public Builder setClipEditingModuleVisibility(
                ClipEditingModules module, boolean visibility) {
            visibility = !visibility;
            if (module.equals(ClipEditingModules.ALL)) {
                this.hidePartEdit = visibility;
            } else if (module.equals(ClipEditingModules.COPY)) {
                this.hideCopy = visibility;
            } else if (module.equals(ClipEditingModules.EDIT)) {
                this.hideEdit = visibility;
            } else if (module.equals(ClipEditingModules.IMAGE_DURATION_CONTROL)) {
                this.hideDuration = visibility;
            } else if (module.equals(ClipEditingModules.VIDEO_SPEED_CONTROL)) {
                this.hideSpeed = visibility;
            } else if (module.equals(ClipEditingModules.PROPORTION)) {
                this.hideProportion = visibility;
            } else if (module.equals(ClipEditingModules.SORT)) {
                this.hideSort = visibility;
            } else if (module.equals(ClipEditingModules.TRIM)) {
                this.hideTrim = visibility;
            } else if (module.equals(ClipEditingModules.SPLIT)) {
                this.hideSplit = visibility;
            } else if (module.equals(ClipEditingModules.TEXT)) {
                this.hideText = visibility;
            } else if (module.equals(ClipEditingModules.REVERSE)) {
                this.hideReverse = visibility;
            } else if (module.equals(ClipEditingModules.TRANSITION)) {
                this.hideTransition = visibility;
            }
            return this;
        }


        /**
         * 设置编辑导出功能模块显示与隐藏
         */
        public Builder setEditAndExportModuleVisibility(
                EditAndExportModules module, boolean visibility) {
            visibility = !visibility;
            if (module.equals(EditAndExportModules.DUBBING)) {
                hideDubbing = visibility;
            } else if (module.equals(EditAndExportModules.SOUNDTRACK)) {
                hideSoundTrack = visibility;
            } else if (module.equals(EditAndExportModules.SOUND)) {
                hideSound = visibility;
            } else if (module.equals(EditAndExportModules.MUSIC_MANY)) {
                hideMusicMany = visibility;
            } else if (module.equals(EditAndExportModules.VOLUME)) {
                hideVolume = visibility;
            } else if (module.equals(EditAndExportModules.FILTER)) {
                hideFilter = visibility;
            } else if (module.equals(EditAndExportModules.SPECIAL_EFFECTS)) {
                hideSpecialEffects = visibility;
            } else if (module.equals(EditAndExportModules.TITLING)) {
                hideTitling = visibility;
            } else if (module.equals(EditAndExportModules.CLIP_EDITING)) {
                hidePartEdit = visibility;
            } else if (module.equals(EditAndExportModules.EFFECTS)) {
                hideEffects = visibility;
            }
            return this;
        }

        public UIConfiguration get() {
            return new UIConfiguration(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "181127uiconfig";
    private static final int VER = 13;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeString(this.newCloudMusicTypeUrl);

        dest.writeString(this.mAEUrl);
        dest.writeString(this.mResTypeUrl);
        //音量
        dest.writeByte(this.hideVolume ? (byte) 0 : (byte) 1);
        //音效 多段配乐
        dest.writeByte(this.hideSound ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideMusicMany ? (byte) 0 : (byte) 1);
        dest.writeString(this.soundTypeUrl);
        dest.writeString(this.soundUrl);

        dest.writeByte(this.hideCover ? (byte) 1 : (byte) (0));
        dest.writeByte(this.hideGraffiti ? (byte) 1 : (byte) (0));
        dest.writeByte(this.hideCollage ? (byte) 1 : (byte) (0));
        dest.writeByte(this.enableRandTransition ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hideEditAndExportSort ? (byte) 1 : (byte) 0);

        dest.writeByte(this.hideDewatermark ? (byte) 1 : (byte) 0);


        dest.writeString(mEffectUrl);
        //新增部分字段

        dest.writeByte(this.hideSoundEffect ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableDraft ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hideEffects ? (byte) 0 : (byte) 1);
        dest.writeParcelable(this.mCloudAuthorizationInfo, flags);


        //*********************************顺序不能改变。支持草稿箱

        dest.writeInt(this.videoProportion);
        dest.writeInt(this.mediaCountLimit);
        dest.writeInt(this.albumSupportFormatType);
        dest.writeInt(this.voiceLayoutTpye);
        dest.writeInt(this.filterLayoutTpye);
        dest.writeByte(this.enableAlbumCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hideMusic ? (byte) 1 : (byte) 0);
        dest.writeByte(this.useCustomAlbum ? (byte) 1 : (byte) 0);
        dest.writeByte(this.openEditbyPicture ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableMV ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableAutoRepeat ? (byte) 1 : (byte) 0);
        dest.writeString(this.mvUrl);
        dest.writeByte(this.enableTitlingAndSpecialEffectOuter ? (byte) 1 : (byte) 0);
        dest.writeString(this.musicUrl);
        dest.writeString(this.cloudMusicUrl);
        dest.writeByte(this.enableLocalMusic ? (byte) 1 : (byte) 0);


        // 片段编辑
        dest.writeByte(this.hideSort ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideProportion ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideTrim ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideSplit ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideSpeed ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideDuration ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideEdit ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideCopy ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideText ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideReverse ? (byte) 0 : (byte) 1);//倒序
        dest.writeByte(this.hideTransition ? (byte) 0 : (byte) 1);

        // 编辑与导出
        dest.writeByte(this.hidePartEdit ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideDubbing ? (byte) 0 : (byte) 1); //隐藏配音
        dest.writeByte(this.hideSoundTrack ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideSpecialEffects ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideTitling ? (byte) 0 : (byte) 1);
        dest.writeByte(this.hideFilter ? (byte) 0 : (byte) 1);


        //向导
        dest.writeByte(this.enableWizard ? (byte) 1 : (byte) 0);
        //新的网络接口 （字幕、音乐、mv）
        dest.writeString(this.subUrl);
        dest.writeString(this.newMusicUrl);
        dest.writeString(this.newMvUrl);

        dest.writeString(this.stickerUrl);
        dest.writeString(this.fontUrl);


        dest.writeString(this.filterUrl);
        dest.writeString(this.transitionUrl);

        dest.writeByte(this.enableLottie ? (byte) 1 : (byte) 0);
        dest.writeString(this.lottieUrl);

        dest.writeString(this.newCloudMusicUrl);


    }


    protected UIConfiguration(Parcel in) {

        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();

            if (tVer >= 13) {
                this.newCloudMusicTypeUrl = in.readString();
            } else {
                this.newCloudMusicTypeUrl = "";
            }

            if (tVer >= 12) {
                this.mAEUrl = in.readString();
                this.mResTypeUrl = in.readString();
            } else {
                this.mAEUrl = "";
                this.mResTypeUrl = "";
            }

            //音量
            if (tVer >= 11) {
                this.hideVolume = in.readByte() == 0;
            }
            //音效和多段配乐
            if (tVer >= 10) {
                this.hideSound = in.readByte() == 0;
                this.hideMusicMany = in.readByte() == 0;
                this.soundTypeUrl = in.readString();
                this.soundUrl = in.readString();
            } else {
                this.soundTypeUrl = "";
                this.soundUrl = "";
            }

            if (tVer >= 9) {
                this.hideCover = in.readByte() == 1;
                this.hideGraffiti = in.readByte() == 1;
            }
            if (tVer >= 8) {
                this.hideCollage = in.readByte() == 1;
            }
            if (tVer >= 7) {
                this.enableRandTransition = in.readByte() == 1;
                this.hideEditAndExportSort = in.readByte() == 1;
            }
            if (tVer >= 6) {
                this.hideDewatermark = in.readByte() == 1;
            }

            if (tVer >= 5) {
                this.mEffectUrl = in.readString();
            }
            if (tVer >= 4) {
                this.hideSoundEffect = in.readByte() == 1;
            }
            if (tVer >= 2) {
                this.enableDraft = in.readByte() == 1;
            }
            if (tVer >= 3) {
                this.hideEffects = in.readByte() == 0;

            }
            this.mCloudAuthorizationInfo = in.readParcelable(CloudAuthorizationInfo.class.getClassLoader());
        } else {
            this.newCloudMusicTypeUrl = "";
            this.mAEUrl = "";
            this.mResTypeUrl = "";
            this.soundTypeUrl = "";
            this.soundUrl = "";
            this.mCloudAuthorizationInfo = null;
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        this.videoProportion = in.readInt();
        this.mediaCountLimit = in.readInt();
        this.albumSupportFormatType = in.readInt();
        this.voiceLayoutTpye = in.readInt();
        this.filterLayoutTpye = in.readInt();
        this.enableAlbumCamera = in.readByte() != 0;
        this.hideMusic = in.readByte() != 0;
        this.useCustomAlbum = in.readByte() != 0;
        this.openEditbyPicture = in.readByte() != 0;
        this.enableMV = in.readByte() != 0;
        this.enableAutoRepeat = in.readByte() != 0;
        this.mvUrl = in.readString();
        this.enableTitlingAndSpecialEffectOuter = in.readByte() != 0;
        this.musicUrl = in.readString();
        this.cloudMusicUrl = in.readString();
        this.enableLocalMusic = in.readByte() != 0;


        // 片段编辑
        this.hideSort = in.readByte() == 0;
        this.hideProportion = in.readByte() == 0;
        this.hideTrim = in.readByte() == 0;
        this.hideSplit = in.readByte() == 0;
        this.hideSpeed = in.readByte() == 0;
        this.hideDuration = in.readByte() == 0;
        this.hideEdit = in.readByte() == 0;
        this.hideCopy = in.readByte() == 0;
        this.hideText = in.readByte() == 0;
        this.hideReverse = in.readByte() == 0;//倒序
        this.hideTransition = in.readByte() == 0;

        // 编辑与导出
        this.hidePartEdit = in.readByte() == 0;
        this.hideDubbing = in.readByte() == 0; //隐藏配音
        this.hideSoundTrack = in.readByte() == 0;
        this.hideSpecialEffects = in.readByte() == 0;
        this.hideTitling = in.readByte() == 0;
        this.hideFilter = in.readByte() == 0;


        //向导
        this.enableWizard = in.readByte() != 0;
        //新的网络接口 （字幕、音乐、mv）
        this.subUrl = in.readString();
        this.newMusicUrl = in.readString();
        this.newMvUrl = in.readString();
        this.stickerUrl = in.readString();
        this.fontUrl = in.readString();
        this.filterUrl = in.readString();
        this.transitionUrl = in.readString();


        this.enableLottie = in.readByte() != 0;
        this.lottieUrl = in.readString();
        this.newCloudMusicUrl = in.readString();


    }

    public static final Parcelable.Creator<UIConfiguration> CREATOR = new Parcelable.Creator<UIConfiguration>() {
        @Override
        public UIConfiguration createFromParcel(Parcel source) {
            return new UIConfiguration(source);
        }

        @Override
        public UIConfiguration[] newArray(int size) {
            return new UIConfiguration[size];
        }
    };


}
