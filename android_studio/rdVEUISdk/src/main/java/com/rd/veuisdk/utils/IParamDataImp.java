package com.rd.veuisdk.utils;

import android.graphics.Color;

import com.rd.vecore.RdVECore;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.fragment.MusicFragmentEx;
import com.rd.veuisdk.model.AppConfigInfo;
import com.rd.veuisdk.model.GraffitiInfo;
import com.rd.veuisdk.model.ShortVideoInfoImp;

import java.util.ArrayList;

/**
 * 存储短视频部分菜单相关的参数  （配乐、mv 、滤镜）
 *
 * @author JIAN
 * @create 2018/11/12
 * @Describe
 */
public class IParamDataImp implements IParamData {
    private String TAG = "IParamDataImp";
    //配乐菜单选中项
    private int nMusicIndex = MusicFragmentEx.MENU_NONE;
    private String mMusicName;

    //调节音量把手的最大值
    public static final int MAX_FACTOR = 500;

    private int mFactor = 100; //可调节区间0~500 ，推荐0~100 太大会爆音 ，默认100，与mediaObject统一
    //配乐音量占比
    private int mMusicFactor = 50;
    private int nMVId = RdVECore.DEFAULT_MV_ID;
    /*
     * 原音开关
     */
    private boolean mMediaMute = false;


    /**
     * 记录当前的滤镜效果  (滤镜分组时，index 表示 group的下标   ；  否则   只需关注 mCurrentFilterType 即可 )
     */
    private int nFilterMenuIndex = 0;
    /**
     * 滤镜id
     */
    private int mCurrentFilterType = 0;

    /**
     * lookup滤镜  (选中项由  nFilterMenuIndex 决定)
     */
    private VisualFilterConfig lookupConfig;


    private boolean mIsRemoveMVMusic = false;

    private boolean mIsZoomOut = true;

    /**
     * 参考：AppConfigInfo.isEnableBGMode的定义
     */
    private boolean mIsEnableBackground = false;

    private ArrayList<GraffitiInfo> mGraffitiInfos;
    private int bgColor = Color.BLACK;
    private float mProportionAsp = 0;

    @Override
    public int getBgColor() {
        return bgColor;
    }

    @Override
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public IParamDataImp() {
        //新的短短视频 重置为全局设置
        AppConfigInfo appConfigInfo = AppConfiguration.getAppConfig();
        if (null != appConfigInfo) {
            mIsZoomOut = appConfigInfo.isZoomOut();
            mIsEnableBackground = appConfigInfo.isEnableBGMode();
            bgColor = appConfigInfo.getBgColor();
            mProportionAsp = appConfigInfo.getProportionAsp();
        }
    }

    /**
     * 还原草稿箱保存的数据
     *
     * @param shortVideoInfoImp
     */
    public void restore(ShortVideoInfoImp shortVideoInfoImp) {

        if (null != shortVideoInfoImp) {
            nFilterMenuIndex = shortVideoInfoImp.getFilterMenuIndex();
            nMVId = shortVideoInfoImp.getMVId();
            mIsRemoveMVMusic = shortVideoInfoImp.isRemoveMVMusic();
            mCurrentFilterType = shortVideoInfoImp.getFilterId();
            lookupConfig = shortVideoInfoImp.getLookupConfig();
            nMusicIndex = shortVideoInfoImp.getMusicIndex();
            mFactor = shortVideoInfoImp.getFactor();
            mMusicFactor = shortVideoInfoImp.getMusicFactor();
            mMediaMute = shortVideoInfoImp.isMediaMute();
            mMusicName = shortVideoInfoImp.getMusicName();
            mSoundEffectId = shortVideoInfoImp.getSoundEffectId();
            mIsZoomOut = shortVideoInfoImp.isZoomOut();
            mIsEnableBackground = shortVideoInfoImp.isEnableBackground();
            mGraffitiInfos = shortVideoInfoImp.getGraffitiList();
            mCover = shortVideoInfoImp.getCoverCaption();
            mMusicPitch = shortVideoInfoImp.getMusicPitch();
            bgColor = shortVideoInfoImp.getBgColor();
            mProportionAsp = shortVideoInfoImp.getProportionAsp();
        }
    }


    /**
     * 是否媒体静音(原音关)
     *
     * @return
     */
    public boolean isMediaMute() {
        return mMediaMute;
    }

    @Override
    public void setZoomOut(boolean isZoomOut) {
        mIsZoomOut = isZoomOut;
    }

    @Override
    public boolean isZoomOut() {
        return mIsZoomOut;
    }

    @Override
    public void enableBackground(boolean enable) {
        mIsEnableBackground = enable;
    }

    @Override
    public boolean isEnableBackground() {
        return mIsEnableBackground;
    }

    @Override
    public void setGraffitiList(ArrayList<GraffitiInfo> list) {
        mGraffitiInfos = list;
    }

    @Override
    public ArrayList<GraffitiInfo> getGraffitiList() {
        return mGraffitiInfos;
    }


    private CaptionLiteObject mCover;

    @Override
    public void setCoverCaption(CaptionLiteObject captionLiteObject) {
        mCover = captionLiteObject;
    }

    @Override
    public CaptionLiteObject getCoverCaption() {
        return mCover;
    }

    private float mMusicPitch = 0.5f;

    @Override
    public void setMusicPitch(float musicPitch) {
        mMusicPitch = musicPitch;
    }

    @Override
    public float getMusicPitch() {
        return mMusicPitch;
    }

    public void setMediaMute(boolean mediaMute) {
        mMediaMute = mediaMute;
    }

    @Override
    public VisualFilterConfig getLookupConfig() {
        return lookupConfig;
    }

    @Override
    public void setLookupConfig(VisualFilterConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }

    @Override
    public int getCurrentFilterType() {
        return mCurrentFilterType;
    }

    @Override
    public void setCurrentFilterType(int currentFilterType) {
        mCurrentFilterType = currentFilterType;
    }


    public boolean isRemoveMVMusic() {
        return mIsRemoveMVMusic;
    }

    public void setRemoveMVMusic(boolean removeMVMusic) {
        mIsRemoveMVMusic = removeMVMusic;
    }

    private int mSoundEffectId = 0;

    /**
     * 声音特效
     *
     * @param index
     */
    @Override
    public void setSoundEffectId(int index) {
        mSoundEffectId = index;
    }

    @Override
    public int getSoundEffectId() {
        return mSoundEffectId;
    }

    @Override
    public void setMVId(int mvId) {
        nMVId = mvId;

    }

    @Override
    public int getMVId() {
        return nMVId;
    }

    @Override
    public void setFilterIndex(int index) {
        nFilterMenuIndex = index;
    }

    @Override
    public int getFilterIndex() {
        return nFilterMenuIndex;
    }

    @Override
    public void setMusicIndex(int index, String musicName) {
        nMusicIndex = index;
        mMusicName = musicName;
    }

    @Override
    public int getMusicIndex() {
        return nMusicIndex;
    }

    @Override
    public String getMusicName() {
        return mMusicName;
    }

    @Override
    public int getFactor() {
        return mFactor;
    }

    @Override
    public int getMusicFactor() {
        return mMusicFactor;
    }

    @Override
    public void setFactor(int factor) {
        mFactor = factor;
    }

    @Override
    public void setMusicFactor(int factor) {
        mMusicFactor = factor;
    }

    /**
     * 预览比例
     */
    @Override
    public void setProportionAsp(float asp) {
        mProportionAsp = asp;
    }

    @Override
    public float getProportionAsp() {
        return mProportionAsp;
    }
}
