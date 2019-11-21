package com.rd.veuisdk.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rd.vecore.Music;
import com.rd.vecore.RdVECore;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.IShortVideoInfo;
import com.rd.veuisdk.fragment.AudioInfo;
import com.rd.veuisdk.fragment.MusicFragmentEx;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IShortParamData;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 记录草稿箱视频
 *
 * @author JIAN
 * @create 2018/11/7
 * @Describe
 */
public class ShortVideoInfoImp implements IShortVideoInfo, IShortParamData, Parcelable {
    private static final String TAG = "ShortVideoInfoImp";
    /**
     * 视频信息类型：普通编辑
     */
    public static final byte VIDEO_INFO_TYPE_NORMAL = 0;
    /**
     * 视频信息类型： 草稿箱视频
     */
    public static final byte VIDEO_INFO_TYPE_DRAFT = 1;

    /**
     * @param nCreateTime
     * @param nDuration
     */
    public ShortVideoInfoImp(long nCreateTime, float nDuration) {
        this.nCreateTime = nCreateTime;
        this.nDuration = nDuration;
    }


    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "181218shortInfoImp";
    private static final int PARCEL_VER = 11;
    @Deprecated
    private MediaObject mCoverMedia;

    protected ShortVideoInfoImp(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        int tparcelVer = -100;
        if (VER_TAG.equals(tmp)) {
            tparcelVer = in.readInt();

            if (tparcelVer >= 11) {
                bgColor = in.readInt();
            }
            if (tparcelVer >= 10) {
                mSoundInfos = in.createTypedArrayList(SoundInfo.CREATOR);
                mMusicInfos = in.createTypedArrayList(SoundInfo.CREATOR);
            }

            if (tparcelVer >= 9) {
                mCoverCapation = in.readParcelable(CaptionLiteObject.class.getClassLoader());
                mMusicPitch = in.readFloat();
            }

            if (tparcelVer >= 8) {
                mCoverMedia = in.readParcelable(MediaObject.class.getClassLoader());
                mGraffitiList = in.createTypedArrayList(GraffitiInfo.CREATOR);
            }

            if (tparcelVer >= 7) {
                mCollageInfos = in.createTypedArrayList(CollageInfo.CREATOR);
            }
            if (tparcelVer >= 6) {
                mIsZoomOut = in.readByte() != 0;
                mIsEnableBackground = in.readByte() != 0;
            }
            if (tparcelVer >= 5) {
                mMOInfos = in.createTypedArrayList(MOInfo.CREATOR);
            }
            if (tparcelVer >= 3) {
                mSoundEffectIndex = in.readInt();
            }
            if (tparcelVer >= 2) {
                mMusicName = in.readString();
                if (tparcelVer >= 4) {
                    //保留网络特效
                    mEffectInfos = in.createTypedArrayList(EffectInfo.CREATOR);
                } else {
                    //放弃之前的本地特效
                    Object obj = in.createTypedArrayList(EffectInfo.CREATOR);
                    mEffectInfos = null;
                }

            } else {
                Object obj = in.createTypedArrayList(EffectInfo.CREATOR);
                mEffectInfos = null;//抛弃之前dyUIApi中的特效 bin.181228.zip中有暴露接口
            }
        } else {
            this.mEffectInfos = null;
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        mProportionStatus = in.readInt();
        mCurProportion = in.readFloat();
        mExtData = in.readString();
        basePath = in.readString();
        nCreateTime = in.readLong();
        nDuration = in.readFloat();
        mCover = in.readString();
        nVideoType = in.readInt();
        mMediaMute = in.readByte() != 0;
        mSceneList = in.createTypedArrayList(Scene.CREATOR);

        if (tparcelVer < 4) {
            //清理旧版本上，单个媒体绑定的特效
            int len = mSceneList.size();
            for (int i = 0; i < len; i++) {
                List<MediaObject> list = mSceneList.get(i).getAllMedia();
                int count = list.size();
                for (int n = 0; n < count; n++) {
                    list.get(n).setEffectInfos(null);
                }
            }
        }
        mWordInfoList = in.createTypedArrayList(WordInfo.CREATOR);
        mStickerInfos = in.createTypedArrayList(StickerInfo.CREATOR);
        mMusic = in.readParcelable(Music.class.getClassLoader());
        mMusicFactor = in.readInt();
        factor = in.readInt();
        mMusicIndex = in.readInt();
        mUIConfiguration = in.readParcelable(UIConfiguration.class.getClassLoader());
        mExportConfiguration = in.readParcelable(ExportConfiguration.class.getClassLoader());
        nFilterMenuIndex = in.readInt();
        nFilterId = in.readInt();
        lookupConfig = in.readParcelable(VisualFilterConfig.class.getClassLoader());
        nMVId = in.readInt();
        mIsRemoveMVMusic = in.readByte() != 0;
        mAudioInfos = in.createTypedArrayList(AudioInfo.CREATOR);
        if (tparcelVer == -100) {
            //解析之前的错误变量
            int tmpVer = in.readInt();
        }
        nId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(PARCEL_VER);
        }

        dest.writeInt(bgColor);
        dest.writeTypedList(mSoundInfos);
        dest.writeTypedList(mMusicInfos);

        dest.writeParcelable(mCoverCapation, 0);
        dest.writeFloat(mMusicPitch);

        dest.writeParcelable(mCoverMedia, 0);
        dest.writeTypedList(mGraffitiList);

        dest.writeTypedList(mCollageInfos);

        dest.writeByte((byte) (mIsZoomOut ? 1 : 0));
        dest.writeByte((byte) (mIsEnableBackground ? 1 : 0));

        dest.writeTypedList(mMOInfos);

        dest.writeInt(mSoundEffectIndex);

        //新增部分字段
        dest.writeString(mMusicName);
        dest.writeTypedList(mEffectInfos);


        dest.writeInt(mProportionStatus);
        dest.writeFloat(mCurProportion);
        dest.writeString(mExtData);
        dest.writeString(basePath);
        dest.writeLong(nCreateTime);
        dest.writeFloat(nDuration);
        dest.writeString(mCover);
        dest.writeInt(nVideoType);
        dest.writeByte((byte) (mMediaMute ? 1 : 0));
        dest.writeTypedList(mSceneList);
        dest.writeTypedList(mWordInfoList);
        dest.writeTypedList(mStickerInfos);
        dest.writeParcelable(mMusic, flags);
        dest.writeInt(mMusicFactor);
        dest.writeInt(factor);
        dest.writeInt(mMusicIndex);
        dest.writeParcelable(mUIConfiguration, flags);
        dest.writeParcelable(mExportConfiguration, flags);
        dest.writeInt(nFilterMenuIndex);
        dest.writeInt(nFilterId);
        dest.writeParcelable(lookupConfig, flags);
        dest.writeInt(nMVId);
        dest.writeByte((byte) (mIsRemoveMVMusic ? 1 : 0));
        dest.writeTypedList(mAudioInfos);
        dest.writeInt(nId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShortVideoInfoImp> CREATOR = new Creator<ShortVideoInfoImp>() {
        @Override
        public ShortVideoInfoImp createFromParcel(Parcel in) {
            return new ShortVideoInfoImp(in);
        }

        @Override
        public ShortVideoInfoImp[] newArray(int size) {
            return new ShortVideoInfoImp[size];
        }
    };

    public float getCurProportion() {
        return mCurProportion;
    }

    public int getProportionStatus() {
        return mProportionStatus;
    }

    private int mProportionStatus = 0;
    private float mCurProportion = 0;
    //扩展参数，防止今后维护升级时，此对象需要新增多个字段 (多个字段记录成json 方便解析)
    private String mExtData = null;

    private boolean mIsZoomOut = true;
    private boolean mIsEnableBackground = true;


    public boolean isEnableBackground() {
        return mIsEnableBackground;
    }

    public void enableBackground(boolean enable) {
        this.mIsEnableBackground = enable;
    }

    public void setZoomOut(boolean isZoomOut) {
        mIsZoomOut = isZoomOut;
    }

    public boolean isZoomOut() {
        return mIsZoomOut;
    }

    public int getFilterMenuIndex() {
        return nFilterMenuIndex;
    }


    public VisualFilterConfig getLookupConfig() {
        return lookupConfig;
    }


    public boolean isRemoveMVMusic() {
        return mIsRemoveMVMusic;
    }

    /**
     * 比例
     *
     * @param status
     * @param mCurProportion
     */
    public void setProportion(int status, float mCurProportion) {
        this.mProportionStatus = status;
        this.mCurProportion = mCurProportion;
    }

    @Override
    public void setProportionAsp(float asp) {
        mCurProportion = asp;
    }

    @Override
    public float getProportionAsp() {
        return mCurProportion;
    }

    /**
     * 存滤镜相关
     *
     * @param nFilterMenuIndex
     * @param nFilterId
     * @param lookupConfig
     */
    public void setFilter(int nFilterMenuIndex, int nFilterId, VisualFilterConfig lookupConfig) {
        this.nFilterMenuIndex = nFilterMenuIndex;
        this.nFilterId = nFilterId;
        this.lookupConfig = lookupConfig;
    }

    /**
     * 存mv相关
     *
     * @param nMVId
     * @param mIsRemoveMVMusic
     */
    public void setMV(int nMVId, boolean mIsRemoveMVMusic) {
        this.nMVId = nMVId;
        this.mIsRemoveMVMusic = mIsRemoveMVMusic;
    }


    public String getBasePath() {
        return basePath;
    }

    private String basePath = null;


    public void setCreateTime(long nCreateTime) {
        this.nCreateTime = nCreateTime;
    }

    private long nCreateTime;

    public void setDuration(float nDuration) {
        this.nDuration = nDuration;
    }

    private float nDuration;

    /**
     * 封面
     *
     * @param cover
     */
    public void setCover(String cover) {
        mCover = cover;
    }

    private String mCover;


    private int nVideoType = VIDEO_INFO_TYPE_NORMAL;

    public int getVideoType() {
        return nVideoType;
    }


    public ArrayList<Scene> getSceneList() {
        return mSceneList;
    }

    public void setSceneList(ArrayList<Scene> sceneList) {
        mSceneList = sceneList;
    }

    public ArrayList<WordInfo> getWordInfoList() {
        return mWordInfoList;
    }

    public void setWordInfoList(ArrayList<WordInfo> wordInfoList) {
        mWordInfoList = wordInfoList;
    }

    public ArrayList<StickerInfo> getRSpecialInfos() {
        return mStickerInfos;
    }

    public void setRSpecialInfos(ArrayList<StickerInfo> RSpecialInfos) {
        mStickerInfos = RSpecialInfos;
    }

    public Music getMusic() {
        return mMusic;
    }

    /**
     * 配乐
     *
     * @param factor
     * @param musicFactor 配乐音量占比
     * @param mediaMute
     * @param index
     * @param music
     * @param musicName
     */
    public void setMusic(int factor, int musicFactor, boolean mediaMute, int index, Music music, String musicName) {
        this.factor = factor;
        this.mMusicFactor = musicFactor;
        mMediaMute = mediaMute;
        mMusicIndex = index;
        mMusic = music;
        mMusicName = musicName;
    }

    public int getMusicFactor() {
        return mMusicFactor;
    }

    /**
     * @return
     */
    public boolean isMediaMute() {
        return mMediaMute;
    }


    //媒体是否静音
    private boolean mMediaMute = false;


    public int getMusicIndex() {
        return mMusicIndex;
    }


    public UIConfiguration getUIConfiguration() {
        return mUIConfiguration;
    }

    public void setUIConfiguration(UIConfiguration UIConfiguration) {
        mUIConfiguration = UIConfiguration;
    }

    public ExportConfiguration getExportConfiguration() {
        return mExportConfiguration;
    }

    public void setExportConfiguration(ExportConfiguration exportConfiguration) {
        mExportConfiguration = exportConfiguration;
    }

    public int getFilterId() {
        return nFilterId;
    }


    public int getMVId() {
        return nMVId;
    }


    public ArrayList<AudioInfo> getAudioInfos() {
        return mAudioInfos;
    }

    public void setAudioInfos(ArrayList<AudioInfo> audioInfos) {
        mAudioInfos = audioInfos;
    }

    public ArrayList<SoundInfo> getSoundInfos() {
        return mSoundInfos;
    }

    public void setSoundInfos(ArrayList<SoundInfo> mSoundInfos) {
        this.mSoundInfos = mSoundInfos;
    }

    public ArrayList<SoundInfo> getMusicInfos() {
        return mMusicInfos;
    }

    public void setMusicInfos(ArrayList<SoundInfo> mMusicInfos) {
        this.mMusicInfos = mMusicInfos;
    }

    //媒体
    private ArrayList<Scene> mSceneList;
    //字幕
    private ArrayList<WordInfo> mWordInfoList;

    //贴纸
    private ArrayList<StickerInfo> mStickerInfos;
    //特效
    private ArrayList<EffectInfo> mEffectInfos;


    //马赛克|水印
    private ArrayList<MOInfo> mMOInfos;


    //画中画列表
    private ArrayList<CollageInfo> mCollageInfos;
    //涂鸦
    private ArrayList<GraffitiInfo> mGraffitiList;

    @Override
    public ArrayList<GraffitiInfo> getGraffitiList() {
        return mGraffitiList;
    }


    @Override
    public void setCoverCaption(CaptionLiteObject captionLiteObject) {
        mCoverCapation = captionLiteObject;
    }

    private CaptionLiteObject mCoverCapation = null;

    @Override
    public CaptionLiteObject getCoverCaption() {
        return mCoverCapation;
    }

    public float getMusicPitch() {
        return mMusicPitch;
    }

    @Override
    public int getBgColor() {
        return bgColor;
    }

    @Override
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }


    private int bgColor = Color.BLACK;


    public void setMusicPitch(float musicPitch) {
        mMusicPitch = musicPitch;
    }

    //音调
    private float mMusicPitch = 0.5f;


    @Override
    public void setSoundEffectId(int effectId) {
        mSoundEffectIndex = effectId;
    }

    @Override
    public int getSoundEffectId() {
        return mSoundEffectIndex;
    }

    public void setGraffitiList(ArrayList<GraffitiInfo> graffitiList) {
        mGraffitiList = graffitiList;
    }


    public ArrayList<CollageInfo> getCollageInfos() {
        if (null == mCollageInfos) {
            mCollageInfos = new ArrayList<>();
        }
        return mCollageInfos;
    }

    public void setCollageInfos(ArrayList<CollageInfo> collageInfos) {
        mCollageInfos = collageInfos;
    }


    public void setMOInfos(ArrayList<MOInfo> MOInfos) {
        mMOInfos = MOInfos;
    }

    public ArrayList<EffectInfo> getEffectInfos() {
        if (null == mEffectInfos) {
            mEffectInfos = new ArrayList<>();
        }
        return mEffectInfos;
    }

    public ArrayList<MOInfo> getMOInfos() {
        if (null == mMOInfos) {
            mMOInfos = new ArrayList<>();
        }
        return mMOInfos;
    }

    public void setEffectInfos(ArrayList<EffectInfo> effectInfos) {
        mEffectInfos = effectInfos;
    }

    //配乐
    private Music mMusic;
    //配乐音量
    private int mMusicFactor;

    public int getFactor() {
        return factor;
    }

    private int factor;

    //声音特效
    private int mSoundEffectIndex = MusicFilterType.MUSIC_FILTER_NORMAL.ordinal();

    //配乐菜单索引 ( 1 无配乐)
    private int mMusicIndex = MusicFragmentEx.MENU_NONE;

    public String getMusicName() {
        return mMusicName;
    }

    private String mMusicName;

    //预览界面的功能
    private UIConfiguration mUIConfiguration;

    //导出参数
    private ExportConfiguration mExportConfiguration;

    //记录滤镜相关索引和效果
    private int nFilterMenuIndex = 0;
    //滤镜
    private int nFilterId = 0;
    //lookup滤镜
    private VisualFilterConfig lookupConfig = null;


    //mvId
    private int nMVId = RdVECore.DEFAULT_MV_ID;
    //是否需要移除MV中的配乐
    private boolean mIsRemoveMVMusic = false;
    //配音
    private ArrayList<AudioInfo> mAudioInfos;
    //音效
    private ArrayList<SoundInfo> mSoundInfos;
    //多段配乐
    private ArrayList<SoundInfo> mMusicInfos;

    public int getVer() {
        return 0;
    }


    public int getId() {
        return nId;
    }

    public void setId(int nId) {
        this.nId = nId;
    }


    //数据库中的唯一Id
    private int nId = -1;


    @Override
    public long getCreateTime() {
        return nCreateTime;
    }

    @Override
    public float getDuration() {
        return nDuration;
    }

    @Override
    public String getCover() {
        return mCover;
    }


//**************************************************

    /**
     * 移到草稿箱
     */
    public void moveToDraft() {
        //每次保存到草稿，均创建新的草稿文件夹，复制封面等
        if (nVideoType != VIDEO_INFO_TYPE_DRAFT) {
            //防止重复加入草稿箱，不断复制文件
            nVideoType = VIDEO_INFO_TYPE_DRAFT;
            basePath = PathUtils.getDraftPath(UUID.randomUUID().toString());
        }
        //防止新增的字幕、特效、配音，有部分额数据没有放到此草稿箱文件夹里
        moveToDraftImp();

    }

    /**
     * 保存草稿时，需要记录当前媒体绑定的滤镜文件
     *
     * @param src
     */
    private void onSaveFilterFile(MediaObject src) {
        ArrayList<EffectInfo> effectInfos = src.getEffectInfos();
        //当前媒体绑定的自定义特效滤镜（记录滤镜文件）
        if (null != effectInfos && effectInfos.size() > 0) {
            for (int m = 0; m < effectInfos.size(); m++) {
                EffectInfo effectInfo = effectInfos.get(m);
                if (effectInfo.getFilterId() != EffectInfo.Unknown) {
                    effectInfo.setTag(EffectManager.getInstance().getCustomFilterPath(effectInfo.getFilterId()));
                } else {
                    effectInfo.setTag(null);
                }
            }
        }
    }


    /**
     * 移动全部资源到指定的文件夹
     */
    private void moveToDraftImp() {
        File fileNew;
        fileNew = new File(basePath);
        if (!fileNew.exists()) {
            fileNew.mkdirs();
        }

        //媒体
        int len = mSceneList.size();
        for (int i = 0; i < len; i++) {
            Scene scene = mSceneList.get(i);
            //媒体绑定的倒序视频
            List<MediaObject> tmp = scene.getAllMedia();
            if (null != tmp && tmp.size() > 0) {
                int count = tmp.size();
                for (int j = 0; j < count; j++) {
                    MediaObject src = tmp.get(j);
                    //当前媒体绑定的自定义特效滤镜（记录滤镜文件）
                    onSaveFilterFile(src);
                    //绑定的参数
                    Object obj = src.getTag();
//                    Log.e(TAG, "moveToDraftImp: " + j + "/" + count + ">>" + obj + " src:" + src);
                    if (obj instanceof VideoOb) {
                        VideoOb videoOb = (VideoOb) obj;
                        if (videoOb.getVideoObjectPack() != null) {
                            if (videoOb.getVideoObjectPack().isReverse) {
                                //倒序视频需要放到草稿箱（第一次倒序 ，此时src 为倒序视频,TAG 绑定的为原始媒体）
                                MediaObject dst = src.moveToDraft(basePath);
//                                Log.e(TAG, "moveToDraftImp: dst:" + dst);
                                tmp.set(j, dst);
                            } else {
                                //倒序再倒序 (恢复到原始视频状态，但TAG中绑定的倒序文件依然需要放入草稿)
                                ((VideoOb) obj).moveToDraft(basePath);
                            }
                        }
                    }
                }
            }
        }

        //字幕
        if (null != mWordInfoList && mWordInfoList.size() > 0) {
            len = mWordInfoList.size();
            for (int i = 0; i < len; i++) {
                WordInfo wordInfo = mWordInfoList.get(i);
                wordInfo.getCaptionObject().moveToDraft(basePath);
            }
        }

        //贴纸
        if (null != mStickerInfos && mStickerInfos.size() > 0) {
            len = mStickerInfos.size();
            StickerInfo info;
            for (int i = 0; i < len; i++) {
                info = mStickerInfos.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }
        }
        //配音
        if (null != mAudioInfos && mAudioInfos.size() > 0) {
            len = mAudioInfos.size();
            AudioInfo info;
            for (int i = 0; i < len; i++) {
                info = mAudioInfos.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }

        }
        //音效
        if (null != mSoundInfos && mSoundInfos.size() > 0) {
            for (SoundInfo s : mSoundInfos) {
                if (s != null) {
                    s.moveToDraft(basePath);
                }
            }
        }
        //多段配乐
        if (null != mMusicInfos && mMusicInfos.size() > 0) {
            for (SoundInfo s : mMusicInfos) {
                if (s != null) {
                    s.moveToDraft(basePath);
                }
            }
        }
        //马赛克|水印
        if (null != mMOInfos && mMOInfos.size() > 0) {
            len = mMOInfos.size();
            MOInfo info;
            for (int i = 0; i < len; i++) {
                info = mMOInfos.get(i);
                if (null != info) {
                    info.getObject().moveToDraft(basePath);
                }
            }
        }


        //画中画
        if (null != mCollageInfos && mCollageInfos.size() > 0) {
            len = mCollageInfos.size();
            CollageInfo info;
            for (int i = 0; i < len; i++) {
                info = mCollageInfos.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }
        }

        //封面
        if (null != mCoverCapation) {
            CaptionLiteObject tmp = mCoverCapation.moveToDraft(basePath);
            if (null != tmp) {
                mCoverCapation = tmp;
            }
        }
        //涂鸦
        if (null != mGraffitiList && mGraffitiList.size() > 0) {
            len = mGraffitiList.size();
            GraffitiInfo info;
            for (int i = 0; i < len; i++) {
                info = mGraffitiList.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }
        }


    }

    @Override
    public String toString() {
        return "ShortVideoInfoImp{" +
                "basePath='" + basePath + '\'' +
                ", nCreateTime=" + nCreateTime +
                ", nDuration=" + nDuration +
                ", mCover='" + mCover + '\'' +
                ", nVideoType=" + nVideoType +
                ", mSceneList=" + mSceneList +
                ", mWordInfoList=" + mWordInfoList +
                ", mStickerInfos=" + mStickerInfos +
                ", mMusic=" + mMusic +
                ", mMusicFactor=" + mMusicFactor +
                ", mMusicIndex=" + mMusicIndex +
                ", mUIConfiguration=" + mUIConfiguration +
                ", mExportConfiguration=" + mExportConfiguration +
                ", filterId=" + nFilterId +
                ", nMVId=" + nMVId +
                ", mAudioInfos=" + mAudioInfos +
                ", nId=" + nId +
                '}';
    }


    /**
     * 删除全部数据
     */
    public void deleteData() {
        FileUtils.deleteAll(basePath);
    }

    /**
     * 判断草稿箱视频的主要媒体是否全部存在
     *
     * @return true 全部存在；false 文件不存在
     */
    public boolean isExit() {
        boolean allIsExits = true;
        if (null != mSceneList) {
            //判断主媒体是否被删除
            int len = mSceneList.size();
            for (int i = 0; i < len; i++) {
                Scene scene = mSceneList.get(i);
                if (null != scene) {
                    int count = scene.getAllMedia().size();
                    for (int j = 0; j < count; j++) {
                        MediaObject tmp = scene.getAllMedia().get(j);
                        if (tmp != null) {
                            if (!FileUtils.isExist(tmp.getMediaPath())) {
                                allIsExits = false;
                                Log.e(TAG, "isExit: " + tmp.getMediaPath());
                                break;
                            }

                            Object obj = tmp.getTag();
                            if (null != obj && obj instanceof VideoOb) {
                                //检测倒序文件是否存在
                                VideoOb videoOb = (VideoOb) obj;
                                VideoObjectPack videoObjectPack = videoOb.getVideoObjectPack();
                                if (videoObjectPack != null) {
                                    MediaObject revsereMedia = videoObjectPack.mediaObject;
                                    if (null != revsereMedia && !FileUtils.isExist(revsereMedia.getMediaPath())) {
                                        Log.e(TAG, "isExit:revsereMedia: " + revsereMedia.getMediaPath());
                                        allIsExits = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return allIsExits;
    }


}
