package cn.com.mobnote.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 单个配音信息
 * 
 * @author abreal
 * 
 */
public class MixAudioInfo implements Parcelable {
    private int m_nTimelineStart, m_nTimelineEnd, m_nRecordStart, m_nRecordEnd,
	    m_nDuration;
    private String m_strRecordFilePath;
    private float m_fFactor;

    /**
     * 获取主时间线开始位置
     * 
     * @return
     */
    public int getTimelineStart() {
	return m_nTimelineStart;
    }

    /**
     * 设置主时间线开始位置
     * 
     * @param i
     */
    public void setTimelineStart(int nStart) {
	m_nTimelineStart = nStart;
    }

    /**
     * 获取主时间线结束位置
     * 
     * @return
     */
    public int getTimelineEnd() {
	return m_nTimelineEnd;
    }

    /**
     * 设置主时间线结束位置
     * 
     * @param nEnd
     */
    public void setTimelineEnd(int nEnd) {
	m_nTimelineEnd = nEnd;
    }

    /**
     * 获取配音内开始位置
     * 
     * @return
     */
    public int getRecordStart() {
	return m_nRecordStart;
    }

    /**
     * 设置配音内开始位置
     * 
     * @param nOffset
     */
    public void setRecordStart(int nStart) {
	m_nRecordStart = nStart;
    }

    /**
     * 获取配音内结束位置
     * 
     * @return
     */
    public int getRecordEnd() {
	return m_nRecordEnd;
    }

    /**
     * 设置配音内结束位置
     * 
     * @param nEnd
     */
    public void setRecordEnd(int nEnd) {
	m_nRecordEnd = nEnd;
    }

    /**
     * 获取配音持续时间
     * 
     * @return
     */
    public int getDuration() {
	return m_nDuration;
    }

    /**
     * 设置配音持续时间
     * 
     * @param nDuration
     * @return
     */
    public int setDuration(int nDuration) {
	this.m_nDuration = nDuration;
	return nDuration;
    }

    /**
     * 获取配音文件
     * 
     * @return
     */
    public String getRecordFiePath() {
	return m_strRecordFilePath;
    }

    public void setRecordFiePath(String path) {
	m_strRecordFilePath = path;
    }

    /**
     * 在主音频中所占比例
     * 
     * @return
     */
    public float getFactor() {
	return m_fFactor;
    }

    /**
     * constructor
     * 
     * @param nTimelineStart
     * @param nDuration
     * @param nDuration2
     * @param nRecordStart
     * @param strMixAudioFilePath
     * @param fFactor
     */
    public MixAudioInfo(int nTimelineStart, int nTimelineEnd, int nRecordStart,
	    int nRecordEnd, int nDuration, String strMixAudioFilePath,
	    float fFactor) {
	m_nTimelineStart = nTimelineStart;
	m_nTimelineEnd = nTimelineEnd;
	m_nRecordStart = nRecordStart;
	m_nRecordEnd = nRecordEnd;
	m_nDuration = nDuration;
	m_fFactor = fFactor;
	m_strRecordFilePath = strMixAudioFilePath;
    }

    public MixAudioInfo() {
	// TODO Auto-generated constructor stub
    }

    /**
     * copy constructor
     * 
     * @param src
     */
    public MixAudioInfo(MixAudioInfo src) {
	m_nTimelineStart = src.m_nTimelineStart;
	m_nTimelineEnd = src.m_nTimelineEnd;
	m_nRecordStart = src.m_nRecordStart;
	m_nRecordEnd = src.m_nRecordEnd;
	m_nDuration = src.m_nDuration;
	m_fFactor = src.m_fFactor;
	m_strRecordFilePath = src.m_strRecordFilePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public int describeContents() {

	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeInt(m_nTimelineStart);
	dest.writeInt(m_nTimelineEnd);
	dest.writeInt(m_nRecordStart);
	dest.writeInt(m_nRecordEnd);
	dest.writeInt(m_nDuration);
	dest.writeFloat(m_fFactor);
	dest.writeString(m_strRecordFilePath);
    }

    public static final Parcelable.Creator<MixAudioInfo> CREATOR = new Parcelable.Creator<MixAudioInfo>() {
	public MixAudioInfo createFromParcel(Parcel in) {
	    return new MixAudioInfo(in);
	}

	public MixAudioInfo[] newArray(int size) {
	    return new MixAudioInfo[size];
	}
    };

    private MixAudioInfo(Parcel in) {
	m_nTimelineStart = in.readInt();
	m_nTimelineEnd = in.readInt();
	m_nRecordStart = in.readInt();
	m_nRecordEnd = in.readInt();
	m_nDuration = in.readInt();
	m_fFactor = in.readFloat();
	m_strRecordFilePath = in.readString();
    }

}
