package com.rd.veuisdk;

import com.rd.vecore.Music;
import com.rd.vecore.models.SubtitleObject;
import com.rd.veuisdk.fragment.AudioInfo;
import com.rd.veuisdk.model.SpecialInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SpecialUtils;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

/**
 * 临时保存的字幕，特效参数
 *
 * @author JIAN
 */
public class TempVideoParams {
    private final String TAG = "vparams";

    private static TempVideoParams mTempParamsInstance = new TempVideoParams();

    /**
     * 获取单例
     *
     * @return
     */
    public static TempVideoParams getInstance() {
        return mTempParamsInstance;
    }

    private int mHeadTime = 0, mTailTime = 0, mThemeId = 0;

    public int getThemeId() {
        return mThemeId;
    }

    public void setThemeId(int mThemeId) {
        this.mThemeId = mThemeId;
    }

    /**
     * 获取主题片头时间
     *
     * @return
     */
    public int getThemeHeader() {
        return mHeadTime;
    }

    /**
     * 设置主题片头时间
     *
     * @param msec 片头时间(ms)
     */
    public void setThemeHeader(int msec) {
        int len = mSubtitles.size();
        int poff = msec - mHeadTime;
        for (int i = 0; i < len; i++) {
            WordInfo info = mSubtitles.get(i);
            info.offTimeLine(poff);
        }
        len = mSpecails.size();
        for (int i = 0; i < len; i++) {
            WordInfo info = mSpecails.get(i);
            info.offTimeLine(poff);
        }

        len = mSubEffects.size();
        for (int i = 0; i < len; i++) {
            SubtitleObject info = mSubEffects.get(i);
            float nstart = info.getTimelineStart(), nend = info.getTimelineEnd();
            nstart = nstart + Utils.ms2s(poff);
            nend = nend + Utils.ms2s(poff);
            info.setTimelineRange(nstart, nend);
        }

        len = mSpEffects.size();
        for (int i = 0; i < len; i++) {
            SpecialInfo info = mSpEffects.get(i);
            info.offTimeLine(poff);
        }
        for (AudioInfo ai : mAudios) {
            ai.offset(Utils.ms2s(poff));
        }
        this.mHeadTime = msec;
    }

    /**
     * 获取主题片尾时间
     *
     * @return
     */
    public int getThemeLast() {
        return mTailTime;
    }

    /**
     * 设置主题片尾时间
     *
     * @param msec 片尾时间(ms)
     */
    public void setThemeLast(int msec) {
        this.mTailTime = msec;
    }

    /**
     * 当前编辑视频宽高比例
     */
    public static double mEditingVideoAspectRatio = -1;

    /**
     * 是否为标准横向视频
     *
     * @return
     */
    public static boolean isLandscapeVideo() {
        double fRoundAspectRatio = (double) (Math
                .floor(mEditingVideoAspectRatio * 10)) / 10;
        return fRoundAspectRatio == (double) (Math.floor((16.0 / 9) * 10)) / 10;
    }

    /**
     * 当前编辑视频持续时间
     */
    private int mEditingVideoDuration;

    /**
     * 当前编辑视频持续时间
     *
     * @return
     */
    public int getEditingVideoDuration() {
        return mEditingVideoDuration;
    }

    /**
     * 设置编辑视频持续时间
     *
     * @param mEditingVideoDuration
     */
    public void setEditingVideoDuration(int mEditingVideoDuration) {
        this.mEditingVideoDuration = mEditingVideoDuration;
    }

    private ArrayList<WordInfo> mSubtitles = new ArrayList<WordInfo>(),
            mSpecails = new ArrayList<WordInfo>();

    public void setSubs(ArrayList<WordInfo> msubs) {
        mSubtitles.clear();
        mSubtitles.addAll(msubs);

    }

    private double mAspectRatio = -1;

    /**
     * 获取编辑视频比例
     *
     * @return
     */
    public double getAspectRatio() {
        return mAspectRatio;
    }

    /**
     * 设置编辑视频比例
     *
     * @param _asp
     */
    public void setAspectRatio(double _asp) {
        mAspectRatio = _asp;
    }

    /**
     * 获取与编辑视频时间线对比后的字幕信息
     *
     * @return
     */
    public ArrayList<WordInfo> getSubsDuraionChecked() {
        int duration = getEditingVideoDuration();
        ArrayList<WordInfo> temp = new ArrayList<WordInfo>();
        int len = mSubtitles.size();
        WordInfo info;
        for (int i = 0; i < len; i++) {
            info = mSubtitles.get(i);
            if (info.getStart() < duration) {
                if (info.getEnd() > duration) {
                    info = new WordInfo(info);
                    info.setEnd(duration);
                }
                temp.add(info);

            }
        }

        return temp;
    }

    public ArrayList<WordInfo> getSubs() {
        return mSubtitles;
    }

    public void setSpecial(ArrayList<WordInfo> sps) {

        // Log.d(TAG, "setSpecial-->"+sps.size());
        mSpecails.clear();
        mSpecails.addAll(sps);

    }

    /**
     * 获取与编辑视频时间线对比后的特效信息
     *
     * @return
     */
    public ArrayList<WordInfo> getSpecailsDurationChecked() {
        int duration = mEditingVideoDuration;
        ArrayList<WordInfo> temp = new ArrayList<WordInfo>();
        int len = mSpecails.size();
        WordInfo info;
        // boolean mAspectRatio = getAsp() != EditorGlobal.editorAspectRatio;
        for (int i = 0; i < len; i++) {
            info = mSpecails.get(i);
            if (info.getStart() < duration) {
                if (info.getEnd() > duration) {
                    info = new WordInfo(info);
                    info.setEnd(duration);
                }
                temp.add(info);
            }
        }

        // Log.d(TAG, "getSpecails-->"+len+"......"+temp.size());
        return temp;
    }

    public ArrayList<WordInfo> getSpecails() {

        return mSpecails;
    }

    public void recycle() {
        mSubtitles.clear();
        mSpecails.clear();
        SubUtils.getInstance().recycle();
        SpecialUtils.getInstance().recycle();
        TTFUtils.recycle();

        if (null != mMusicObject) {
            //mMusicObject.recycle();
            mMusicObject = null;
        }
        int len = mAudios.size();
        for (int i = 0; i < len; i++) {
            mAudios.get(i).recycle();
        }
        mAudios.clear();

        len = mSubEffects.size();
//        for (int i = 0; i < len; i++) {
////            mSubEffects.get(i).recycle();
//        }
        mSubEffects.clear();
        len = mSpEffects.size();
        for (int i = 0; i < len; i++) {
            mSpEffects.get(i).recycle();
        }
        mSpEffects.clear();
        VideoEditActivity.mCurrentFilterType = 0;
    }

    /**
     * 配音
     */
    private ArrayList<AudioInfo> mAudios = new ArrayList<AudioInfo>();

    public void setAudioList(ArrayList<AudioInfo> audio) {
        mAudios.clear();
        mAudios.addAll(audio);
    }

    /**
     * 获取配音信息
     *
     * @return
     */
    public ArrayList<AudioInfo> getAudios() {
        int duration = getEditingVideoDuration();
        ArrayList<AudioInfo> temp = new ArrayList<AudioInfo>();
        int len = mAudios.size();
        for (int i = 0; i < len; i++) {
            AudioInfo info = mAudios.get(i);
            if (info.getStartRecordTime() < duration) {
                if (info.getEndRecordTime() > duration) {
                    info = new AudioInfo(info);
                    info.setEndRecordTime(duration);
                }
                temp.add(info);
            }
        }
        return temp;
    }

    /**
     * 使用场景(保存配音，配乐，字幕，特效等 ->选择视频，调速裁剪 ->编辑配音配乐等)
     *
     * @param duration
     */
    public void checkParams(int duration) {
        // Log.d(TAG,
        // "checkParams..." + duration + "....mSubtitles.size(): " + mSubtitles.size()
        // + "....mSpecails.size() ->" + mSpecails.size());
        /**
         * 判断配音
         */
        for (int i = 0; i < mAudios.size(); i++) {
            AudioInfo info = mAudios.get(i);
            if (info.getEndRecordTime() > duration) {
                info.recycle();
                mAudios.remove(i);
                i--;
            }
        }
        /**
         * 判断字幕
         */
        // SubExportUtils sexpU=new SubExportUtils(_context, _list, lwidth,
        // lheight)
        WordInfo info;
        for (int i = 0; i < mSubtitles.size(); i++) {
            info = mSubtitles.get(i);
            if (info.getEnd() > duration) {
                mSubtitles.remove(i);
            }
        }

        SubtitleObject mitem;
        for (int m = 0; m < mSubEffects.size(); m++) {
            mitem = mSubEffects.get(m);
            if (Utils.s2ms(mitem.getTimelineEnd()) > duration) {
                mSubEffects.remove(mitem);
                m--;
            }
        }

        /**
         * 判断特效
         */
        for (int i = 0; i < mSpecails.size(); i++) {
            info = mSpecails.get(i);
            if (info.getEnd() > duration) {
                mSpecails.remove(i);
                i--;
            }
        }
        SpecialInfo spInfo;
        for (int m = 0; m < mSpEffects.size(); m++) {
            spInfo = mSpEffects.get(m);
            if (spInfo.getTimelineTo() > duration) {
                spInfo.recycle();
                mSpEffects.remove(m);
                m--;
            }
        }

        if (null != mMusicObject) {
            if (Utils.s2ms(mMusicObject.getTimelineStart()) > duration) {
                mMusicObject = null;
            } else if (Utils.s2ms(mMusicObject.getTimelineEnd()) > duration) {
                mMusicObject.setTimelineRange(mMusicObject.getTimelineEnd(),
                        Utils.ms2s(duration));
            }
        }
    }


    private Music mMusicObject;

    public void setMusicObject(Music music) {
        mMusicObject = music;
    }

    /**
     * 选择无配乐。重置数据
     */
    public void recycleMusicObject() {
        if (null != mMusicObject) {
            mMusicObject = null;
        }
    }

    /**
     * 获取配乐
     *
     * @return
     */
    public Music getMusic() {
        if (null != mMusicObject) {
            mMusicObject.setTimelineRange(Utils.ms2s(this.mHeadTime), -Utils.ms2s(this.mTailTime));
            mMusicObject.setFadeInOut(Utils.ms2s(3000), Utils.ms2s(1500));// 淡入淡出
            return mMusicObject;
        }
        return null;
    }

    private ArrayList<SubtitleObject> mSubEffects = new ArrayList<SubtitleObject>();
    private ArrayList<SpecialInfo> mSpEffects = new ArrayList<SpecialInfo>();

    public void setSubEffects(ArrayList<SubtitleObject> sublist) {
        mSubEffects.clear();
        mSubEffects.addAll(sublist);
    }

    public ArrayList<SubtitleObject> getSubEffects() {
        int duration = mEditingVideoDuration;
        ArrayList<SubtitleObject> temp = new ArrayList<SubtitleObject>();
        SubtitleObject mitem;
        int len = mSubEffects.size();
        for (int m = 0; m < len; m++) {
            mitem = mSubEffects.get(m);
            if (Utils.s2ms(mitem.getTimelineEnd()) <= duration) {
                temp.add(mitem);
            }
        }
        return temp;
    }

    public void setSpEffects(ArrayList<SpecialInfo> splist) {
        mSpEffects.clear();
        mSpEffects.addAll(splist);
    }

    public ArrayList<SpecialInfo> getSpEffects() {
        int duration = mEditingVideoDuration;
        ArrayList<SpecialInfo> temp = new ArrayList<SpecialInfo>();

        SpecialInfo spInfo;
        for (int m = 0; m < mSpEffects.size(); m++) {
            spInfo = mSpEffects.get(m);
            if (spInfo.getTimelineTo() <= duration) {
                temp.add(spInfo);
            }
        }
        return temp;
    }

    public boolean checkAspectRatioChanged() {
        return getAspectRatio() == mEditingVideoAspectRatio;
    }

}
