package com.rd.veuisdk.manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rd.veuisdk.ui.SubFunctionUtils;

/**
 * RdVEUISdk界面配置类
 */
public class UIConfiguration implements Parcelable {

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
     * 界面一
     */
    public static final int FILTER_LAYOUT_1 = 1;
    /**
     * 滤镜界面：<br>
     * 界面二
     */
    public static final int FILTER_LAYOUT_2 = 2;


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
    public final boolean enableAutoRepeat;

    public final String mvUrl;
    //字幕、特效在mv之上
    public final boolean enableTitlingAndSpecialEffectOuter;

    public final String musicUrl;// 配乐2的网络音乐

    /**
     * 配乐2->云音乐
     */
    public final String cloudMusicUrl;

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
         * 滤镜
         */
        FILTER,

        /**
         * 字幕
         */
        TITLING,

        /**
         * 特效
         */
        SPECIAL_EFFECTS,

        /**
         * 片段编辑
         */
        CLIP_EDITING,
    }

    private UIConfiguration(Builder builder) {
        this.useCustomAlbum = builder.b_useCustomAlbum;
        this.albumSupportFormatType = builder.b_albumSupportFormatType;
        this.openEditbyPicture = builder.b_openEditbyPicture;
        this.mediaCountLimit = builder.b_mediaCountLimit;
        this.enableMV = builder.enableMV;
        this.mvUrl = builder.mvUrl;
        this.enableAutoRepeat = builder.enableAutoRepeat;
        this.hideMusic = builder.mHideMusic;
        this.filterLayoutTpye = builder.mFilterLayoutType;
        this.enableAlbumCamera = builder.mEnableAlbumCamera;

        if (enableMV) {
            this.videoProportion = PROPORTION_SQUARE;
            SubFunctionUtils.setHideProportion(true);
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
    }

    /**
     * Builder class for {@link UIConfiguration} objects.
     */
    public static class Builder {
        int b_videoProportion = PROPORTION_AUTO;
        int b_mediaCountLimit = 0;
        boolean b_useCustomAlbum = false;
        int b_voiceLayoutType = VOICE_LAYOUT_1;
        boolean b_enableWizard = false;
        int b_albumSupportFormatType = ALBUM_SUPPORT_DEFAULT;
        boolean b_openEditbyPicture = false;
        boolean enableMV = false;
        boolean enableAutoRepeat = false;
        int mFilterLayoutType = FILTER_LAYOUT_1;
        boolean mEnableAlbumCamera = true;
        boolean mHideMusic = false;
        private String mvUrl = "";
        boolean enableTitlingAllOuter = true;

        //不设置就默认关闭
        private String musicUrl = "";
        private String cloudMusicUrl = "";

        private boolean enableLocalMusic = true;

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
         * 设置简单后台网络音乐url
         *
         * @param musicUrl 网络音乐url
         */
        public Builder setMusicUrl(String musicUrl) {
            this.musicUrl = musicUrl;
            return this;
        }

        /**
         * 设置云音乐url
         *
         * @param cloudMusicUrl 云音乐url
         */
        public Builder setCloudMusicUrl(String cloudMusicUrl) {
            this.cloudMusicUrl = cloudMusicUrl;
            return this;
        }

        /**
         * 启用字幕特效在mv的外面(不受MV影响)
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
            if (SubFunctionUtils.isOnlyModule() && !enable) {
                Log.e(this.toString(), "只显示某一功能模块将强制向导化");
                return this;
            }
            this.b_enableWizard = enable;
            SubFunctionUtils.setEnableWizard(enable);
            return this;
        }


        /**
         * 配置mv 默认关闭
         *
         * @param enable true 打开;flase 关闭
         * @param url    网络MV地址
         */
        public Builder enableMV(boolean enable, String url) {
            this.enableMV = enable;
            this.mvUrl = url;
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
         */
        public Builder setFilterType(int type) {
            this.mFilterLayoutType = Math.min(FILTER_LAYOUT_2,
                    Math.max(FILTER_LAYOUT_1, type));
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
                SubFunctionUtils.setHidePartEdit(visibility);
            } else if (module.equals(ClipEditingModules.COPY)) {
                SubFunctionUtils.setHideCopy(visibility);
            } else if (module.equals(ClipEditingModules.EDIT)) {
                SubFunctionUtils.setHideEdit(visibility);
            } else if (module.equals(ClipEditingModules.IMAGE_DURATION_CONTROL)) {
                SubFunctionUtils.setHideDuration(visibility);
            } else if (module.equals(ClipEditingModules.VIDEO_SPEED_CONTROL)) {
                SubFunctionUtils.setHideSpeed(visibility);
            } else if (module.equals(ClipEditingModules.PROPORTION)) {
                SubFunctionUtils.setHideProportion(visibility);
            } else if (module.equals(ClipEditingModules.SORT)) {
                SubFunctionUtils.setHideSort(visibility);
            } else if (module.equals(ClipEditingModules.TRIM)) {
                SubFunctionUtils.setHideTrim(visibility);
            } else if (module.equals(ClipEditingModules.SPLIT)) {
                SubFunctionUtils.setHideSplit(visibility);
            } else if (module.equals(ClipEditingModules.TEXT)) {
                SubFunctionUtils.setHideText(visibility);
            } else if (module.equals(ClipEditingModules.REVERSE)) {
                SubFunctionUtils.setHideReverse(visibility);
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
                SubFunctionUtils.setHideDubbing(visibility);
            } else if (module.equals(EditAndExportModules.SOUNDTRACK)) {
                SubFunctionUtils.setHideSoundTrack(visibility);
            } else if (module.equals(EditAndExportModules.FILTER)) {
                SubFunctionUtils.setHideFilter(visibility);
            } else if (module.equals(EditAndExportModules.SPECIAL_EFFECTS)) {
                SubFunctionUtils.setHideSpecialEffects(visibility);
            } else if (module.equals(EditAndExportModules.TITLING)) {
                SubFunctionUtils.setHideTitling(visibility);
            } else if (module.equals(EditAndExportModules.CLIP_EDITING)) {
                SubFunctionUtils.setHidePartEdit(visibility);
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
    }

    protected UIConfiguration(Parcel in) {
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
