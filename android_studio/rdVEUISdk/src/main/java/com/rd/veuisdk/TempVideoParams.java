package com.rd.veuisdk;

import com.rd.vecore.Music;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.veuisdk.fragment.AudioInfo;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.MOInfo;
import com.rd.veuisdk.model.SoundInfo;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 临时保存的字幕，特效参数
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

    private int mHeadTime = 0;
    private int mTailTime = 0;
    private int mThemeId = 0;


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
        int len = mWordInfos.size();
        int poff = msec - mHeadTime;
        for (int i = 0; i < len; i++) {
            WordInfo info = mWordInfos.get(i);
            info.offTimeLine(poff);
        }
        len = mStickerInfos.size();
        for (int i = 0; i < len; i++) {
            StickerInfo info = mStickerInfos.get(i);
            info.offTimeLine(poff);
        }

        for (AudioInfo ai : mAudios) {
            ai.offset(Utils.ms2s(poff));
        }

        for (SoundInfo s : mSoundInfos) {
            s.offset(poff);
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
     * 当前编辑视频持续时间 单位：ms
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
     */
    public void setEditingVideoDuration(int duration) {
        this.mEditingVideoDuration = duration;
    }

    private ArrayList<WordInfo> mWordInfos = new ArrayList<>();


    private ArrayList<StickerInfo> mStickerInfos = new ArrayList<>();

    private ArrayList<MOInfo> mMOInfos = new ArrayList<>();
    private ArrayList<CollageInfo> mCollageInfos = new ArrayList<>();

    public void setCollageList(List<CollageInfo> list) {
        mCollageInfos.clear();
        mCollageInfos.addAll(list);

    }

    public void setSubs(ArrayList<WordInfo> msubs) {
        mWordInfos.clear();
        mWordInfos.addAll(msubs);

    }

    public void setMosaics(ArrayList<MOInfo> items) {
        mMOInfos.clear();
        mMOInfos.addAll(items);

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
        ArrayList<WordInfo> temp = new ArrayList<>();
        int len = mWordInfos.size();
        for (int i = 0; i < len; i++) {
            WordInfo info = mWordInfos.get(i);
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

    /**
     * 获取与编辑视频时间线对比后的马赛克信息
     *
     * @return
     */
    public ArrayList<MOInfo> getMosaicDuraionChecked() {
        int duration = getEditingVideoDuration();
        ArrayList<MOInfo> temp = new ArrayList<>();
        int len = mMOInfos.size();
        for (int i = 0; i < len; i++) {
            MOInfo info = mMOInfos.get(i);
            if (info.getStart() < duration) {
                if (info.getEnd() > duration) {
                    info = new MOInfo(info);
                    info.setEnd(duration);
                }
                temp.add(info);

            }
        }

        return temp;
    }

    /**
     * @return
     */
    public ArrayList<CollageInfo> getCollageDurationChecked() {
        return getCollageDurationChecked(Utils.ms2s(getEditingVideoDuration()));
    }

    public ArrayList<CollageInfo> getCollageDurationChecked(float duration) {
        ArrayList<CollageInfo> tmp = new ArrayList<>();
        int len = mCollageInfos.size();
        for (int i = 0; i < len; i++) {
            CollageInfo info = mCollageInfos.get(i);
            MediaObject mediaObject = info.getMediaObject();
            if (mediaObject.getTimelineFrom() < duration) {
                if (mediaObject.getTimelineTo() > duration) {
                    info.fixMediaLine(mediaObject.getTimelineFrom(), duration);
                }
                tmp.add(info);
            }
        }
        return tmp;
    }

    /**
     * 贴纸
     *
     * @param list
     */
    public void setSpecial(ArrayList<StickerInfo> list) {
        mStickerInfos.clear();
        mStickerInfos.addAll(list);

    }

    /**
     * 获取与编辑视频时间线对比后的特效信息
     *
     * @return
     */
    public ArrayList<StickerInfo> getSpecailsDurationChecked() {
        int duration = mEditingVideoDuration;
        ArrayList<StickerInfo> temp = new ArrayList<>();
        int len = mStickerInfos.size();
        for (int i = 0; i < len; i++) {
            StickerInfo info = mStickerInfos.get(i);
            if (info.getStart() < duration) {
                if (info.getEnd() > duration) {
                    info = new StickerInfo(info);
                    info.setEnd(duration);
                }
                temp.add(info);
            }
        }

        return temp;
    }

    public ArrayList<StickerInfo> getRSpEffects() {
        int duration = mEditingVideoDuration;
        ArrayList<StickerInfo> temp = new ArrayList<>();
        if (null != mStickerInfos && mStickerInfos.size() > 0) {
            for (StickerInfo spInfo : mStickerInfos) {
                if (null != spInfo && spInfo.getEnd() <= duration) {
                    temp.add(spInfo);
                }
            }
        }
        return temp;
    }


    /**
     * @return
     */
    public ArrayList<StickerInfo> getRSpecialInfos() {

        return mStickerInfos;
    }

    public void recycle() {

        int len = mWordInfos.size();
        for (int i = 0; i < len; i++) {
            mWordInfos.get(i).recycle();
        }
        mWordInfos.clear();


        len = mMOInfos.size();
        for (int i = 0; i < len; i++) {
            mMOInfos.get(i).recycle();
        }
        mMOInfos.clear();


        SubUtils.getInstance().recycle();
        StickerUtils.getInstance().recycle();
        TTFUtils.recycle();

        if (null != mMusicObject) {
            mMusicObject = null;
        }
        len = mAudios.size();
        for (int i = 0; i < len; i++) {
            mAudios.get(i).recycle();
        }
        mAudios.clear();
        //音效
        for (SoundInfo s : mSoundInfos) {
            s.recycle();
        }
        mSoundInfos.clear();
        //多段配乐
        for (SoundInfo s : mMusicinfo) {
            s.recycle();
        }
        mMusicinfo.clear();

        len = mStickerInfos.size();
        for (int i = 0; i < len; i++) {
            mStickerInfos.get(i).recycle();
        }
        mStickerInfos.clear();
        AEActivity.mCurrentFilterType = 0;

        mCollageInfos.clear();
    }

    /**
     * 配音
     */
    private ArrayList<AudioInfo> mAudios = new ArrayList<>();

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
        ArrayList<AudioInfo> temp = new ArrayList<>();
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

        /**
         * 判断音效
         */
        for (SoundInfo s : mSoundInfos) {
            if (s.getEnd() > duration) {
                s.recycle();
                mSoundInfos.remove(s);
            }
        }

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
        for (int i = 0; i < mWordInfos.size(); i++) {
            WordInfo info = mWordInfos.get(i);
            if (info.getEnd() > duration) {
                mWordInfos.remove(i);
            }
        }

        /**
         * 判断特效
         */
        for (int i = 0; i < mStickerInfos.size(); i++) {
            StickerInfo info = mStickerInfos.get(i);
            if (info.getEnd() > duration) {
                mStickerInfos.remove(i);
                i--;
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
            mMusicObject.setFadeInOut(1.5f, 1.5f);// 淡入淡出
            return mMusicObject;
        }
        return null;
    }

    /**
     * 字幕对象列表
     *
     * @return
     */
    public ArrayList<CaptionObject> getCaptionObjects() {
        ArrayList<CaptionObject> temp = new ArrayList<>();
        if (null != mWordInfos && mWordInfos.size() > 0) {
            for (WordInfo info : mWordInfos) {
                if (info.getEnd() <= mEditingVideoDuration) {
                    temp.add(info.getCaptionObject());
                } else if (info.getStart() < mEditingVideoDuration && mEditingVideoDuration <= info.getEnd()) {
                    //有交叉，保留有效部分的时间线
                    info.setEnd(mEditingVideoDuration);
                }
            }
        }
        return temp;
    }

    public ArrayList<WordInfo> getWordInfos() {
        return mWordInfos;
    }

    /**
     * 马赛克|去水印 对象列表
     *
     * @return
     */
    public ArrayList<DewatermarkObject> getMarkList() {
        ArrayList<DewatermarkObject> temp = new ArrayList<>();
        if (null != mMOInfos && mMOInfos.size() > 0) {
            for (MOInfo info : mMOInfos) {
                if (info.getEnd() <= mEditingVideoDuration) {
                    temp.add(info.getObject());
                } else if (info.getStart() < mEditingVideoDuration && mEditingVideoDuration <= info.getEnd()) {
                    //有交叉，保留有效部分的时间线
                    info.setEnd(mEditingVideoDuration);
                }
            }
        }
        return temp;
    }

    /**
     * 音效
     */
    private ArrayList<SoundInfo> mSoundInfos = new ArrayList<>();

    public void setSoundInfoList(ArrayList<SoundInfo> soundInfos) {
        mSoundInfos.clear();
        if (soundInfos != null && soundInfos.size() > 0) {
            mSoundInfos.addAll(soundInfos);
        }
    }

    public ArrayList<SoundInfo> getSoundInfoList() {
        int duration = getEditingVideoDuration();
        ArrayList<SoundInfo> temp = new ArrayList<>();
        int len = mSoundInfos.size();
        for (int i = 0; i < len; i++) {
            SoundInfo soundInfo = mSoundInfos.get(i);
            if (soundInfo.getStart() < duration) {
                if (soundInfo.getEnd() > duration) {
                    soundInfo = new SoundInfo(soundInfo);
                    soundInfo.setEnd(duration);
                }
                temp.add(soundInfo);
            }
        }
        return temp;
    }

    /**
     * 多段配乐
     */
    private ArrayList<SoundInfo> mMusicinfo = new ArrayList<>();

    public void setMusicInfoList(ArrayList<SoundInfo> soundInfos) {
        mMusicinfo.clear();
        if (soundInfos != null && soundInfos.size() > 0) {
            mMusicinfo.addAll(soundInfos);
        }
    }

    public ArrayList<SoundInfo> getMusicInfoList() {
        int duration = getEditingVideoDuration();
        ArrayList<SoundInfo> temp = new ArrayList<>();
        int len = mMusicinfo.size();
        for (int i = 0; i < len; i++) {
            SoundInfo soundInfo = mMusicinfo.get(i);
            if (soundInfo.getStart() < duration) {
                if (soundInfo.getEnd() > duration) {
                    soundInfo = new SoundInfo(soundInfo);
                    soundInfo.setEnd(duration);
                }
                temp.add(soundInfo);
            }
        }
        return temp;
    }

}
