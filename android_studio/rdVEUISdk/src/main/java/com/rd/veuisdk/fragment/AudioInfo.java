package com.rd.veuisdk.fragment;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.lib.utils.FileUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.utils.Utils;

import java.io.File;

/**
 * 记录录音的时间片
 */
public class AudioInfo implements Parcelable {

    /**
     * 配乐ｉｄ
     */
    private int audioInfoId = 0;

    /**
     * 开始录音时间 单位ms
     */
    private int startRecordTime;

    /**
     * 结束录音时间  单位ms
     */
    private int endRecordTime;

    private int seekBarValue = 50;

    private String audiopath;
    private Music audio;

    public AudioInfo(int id, String path) {

        this.audioInfoId = id;
        this.audiopath = path;
    }

    protected AudioInfo(Parcel in) {
        audioInfoId = in.readInt();
        startRecordTime = in.readInt();
        endRecordTime = in.readInt();
        seekBarValue = in.readInt();
        audiopath = in.readString();
        audio = in.readParcelable(Music.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(audioInfoId);
        dest.writeInt(startRecordTime);
        dest.writeInt(endRecordTime);
        dest.writeInt(seekBarValue);
        dest.writeString(audiopath);
        dest.writeParcelable(audio, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AudioInfo> CREATOR = new Creator<AudioInfo>() {
        @Override
        public AudioInfo createFromParcel(Parcel in) {
            return new AudioInfo(in);
        }

        @Override
        public AudioInfo[] newArray(int size) {
            return new AudioInfo[size];
        }
    };

    public String getPath() {
        return audiopath;
    }

    public int getStartRecordTime() {
        return startRecordTime;
    }

    public void setStartRecordTime(int startRecordTime) {
        this.startRecordTime = startRecordTime;
    }

    public int getEndRecordTime() {
        return endRecordTime;
    }

    public void setEndRecordTime(int endRecordTime) {
        this.endRecordTime = endRecordTime;
    }

    public int getAudioInfoId() {
        return audioInfoId;
    }

    public int getSeekBarValue() {
        return seekBarValue;
    }

    public void setSeekBarValue(int seekBarValue) {
        this.seekBarValue = seekBarValue;
        if (null != audio) {
            audio.setMixFactor(seekBarValue);
        }
    }

    @Override
    public String toString() {
        return "AudioInfo [audioInfoId=" + audioInfoId + ", startRecordTime="
                + startRecordTime + ", endRecordTime=" + endRecordTime + "]";
    }

    public Music getAudio() {
        if (null == audio)
            createAudioObject();
        return audio;
    }


    private void createAudioObject() {
        audio = VirtualVideo.createMusic(audiopath);
        int duration = getEndRecordTime() - getStartRecordTime();
        if (Utils.s2ms(audio.getIntrinsicDuration()) < duration) {
            duration = Utils.s2ms(audio.getIntrinsicDuration());
        }
        audio.setTimeRange(0, Utils.ms2s(duration));
        audio.setTimelineRange(Utils.ms2s(getStartRecordTime()), Utils.ms2s(getEndRecordTime()));
        audio.setMixFactor(getSeekBarValue());
    }

    public void recycle() {
        if (null != audio) {
            audio = null;
        }

    }

    public AudioInfo(AudioInfo info) {
        this.audioInfoId = info.audioInfoId;
        this.startRecordTime = info.startRecordTime;
        this.endRecordTime = info.endRecordTime;
        this.seekBarValue = info.seekBarValue;
        this.audiopath = info.audiopath;
    }

    @Override
    public boolean equals(Object o) {
        if (null != o && o instanceof AudioInfo) {
            AudioInfo info = (AudioInfo) o;
            if (info.getPath().equals(getPath())
                    && info.getStartRecordTime() == getStartRecordTime()
                    && info.getEndRecordTime() == getEndRecordTime()
                    && getSeekBarValue() == info.getSeekBarValue()) {
                return true;
            }
        }
        return false;
    }

    public void offset(float offset) {
        startRecordTime += offset;
        endRecordTime += offset;
        audio = null;
    }

    /**
     * 移动到草稿箱
     *
     * @param basePath
     */
    public void moveToDraft(String basePath) {
        //配音文件移动到草稿箱
        if (FileUtils.isExist(basePath)) {
            if (!audiopath.contains(basePath)) {
                //文件已经在草稿箱中，不需要再剪切文件
                File fileOld = new File(audiopath);
                File fileNew = new File(basePath, fileOld.getName());
                FileUtils.syncCopyFile(fileOld, fileNew, null);
                audio = null;
                audiopath = fileNew.getAbsolutePath();
            }
        }


    }

}
