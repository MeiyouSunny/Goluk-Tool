package com.rd.veuisdk.model;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.text.TextUtils;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

/**
 * 仅贴纸
 */
public class StickerInfo extends ISubStickerInfo {

    @Deprecated
    private RectF mPreviewRectF = new RectF(); //编辑时贴纸的显示位置
    @Deprecated
    private String mInputText;
    @Deprecated
    private String mText;
    @Deprecated
    private String mInputTTF = null;


    //单位：ms
    private long mStart, mEnd; //特效的起止时间，毫秒

    private static final String VER_TAG = "190726_StickerInfo";
    private static final int VER = 4;

    public Rect getRectOriginal() {
        return mRectOriginal;
    }

    /**
     * @param rectOriginal 单位：像素
     */
    public void setRectOriginal(Rect rectOriginal) {
        mRectOriginal = rectOriginal;
    }

    //没有角度时，图片的显示位置（插入liteObject时，显示区域必须是旋转角度为0时的位置）
    private Rect mRectOriginal = new Rect();

    protected StickerInfo(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();
            if (tVer >= 4) {
                mIcon = in.readString();
                mCategory = in.readString();
            }
            if (tVer >= 3) {
                parentWidth = in.readFloat();
                parentHeight = in.readFloat();
            }
            if (tVer >= 2) {
                mRectOriginal = in.readParcelable(Rect.class.getClassLoader());
            }
            if (tVer >= 1) {
                nPreviewAsp = in.readFloat();
                mPreviewRectF = in.readParcelable(RectF.class.getClassLoader());
            }
        } else {
            in.setDataPosition(oldPosition);
        }
        width = in.readInt();
        height = in.readInt();
        widthx = in.readDouble();
        heighty = in.readDouble();
        left = in.readDouble();
        top = in.readDouble();
        id = in.readInt();
        styleId = in.readInt();
        centerxy = in.createFloatArray();
        mInputText = in.readString();
        mInputTextColor = in.readInt();
        mTextColor = in.readInt();
        changed = in.readByte() != 0;


        mStart = in.readLong();
        mEnd = in.readLong();
        mShadowColor = in.readInt();
        mAngle = in.readFloat();
        mInputText = in.readString();
        mText = in.readString();
        mInputTTF = in.readString();
        mDisf = in.readFloat();
        mArrayList = in.createTypedArrayList(CaptionLiteObject.CREATOR);
    }

    public float getPreviewAsp() {
        return nPreviewAsp;
    }

    public void setPreviewAsp(float nPreviewAsp) {
        this.nPreviewAsp = nPreviewAsp;
    }


    private float nPreviewAsp = 1f; //编辑时的视频比例

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }

        dest.writeString(mIcon);
        dest.writeString(mCategory);

        dest.writeFloat(parentWidth);
        dest.writeFloat(parentHeight);

        dest.writeParcelable(mRectOriginal, flags);
        dest.writeFloat(nPreviewAsp);
        dest.writeParcelable(mPreviewRectF, flags);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeDouble(widthx);
        dest.writeDouble(heighty);
        dest.writeDouble(left);
        dest.writeDouble(top);
        dest.writeInt(id);
        dest.writeInt(styleId);
        dest.writeFloatArray(centerxy);
        dest.writeString(mInputText);
        dest.writeInt(mInputTextColor);
        dest.writeInt(mTextColor);
        dest.writeByte((byte) (changed ? 1 : 0));

        dest.writeLong(mStart);
        dest.writeLong(mEnd);
        dest.writeInt(mShadowColor);
        dest.writeFloat(mAngle);
        dest.writeString(mInputText);
        dest.writeString(mText);
        dest.writeString(mInputTTF);
        dest.writeFloat(mDisf);
        dest.writeTypedList(mArrayList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StickerInfo> CREATOR = new Creator<StickerInfo>() {
        @Override
        public StickerInfo createFromParcel(Parcel in) {
            return new StickerInfo(in);
        }

        @Override
        public StickerInfo[] newArray(int size) {
            return new StickerInfo[size];
        }
    };

    @Override
    public String toString() {
        return "StickerInfo{" +
                "mStart=" + mStart +
                ", mEnd=" + mEnd +
                ", mRectOriginal=" + mRectOriginal +
                ", nPreviewAsp=" + nPreviewAsp +
                ", mShadowColor=" + mShadowColor +
                ", mAngle=" + mAngle +
                ", mInputText='" + mInputText + '\'' +
                ", mText='" + mText + '\'' +
                ", mDisf=" + mDisf +
//                ", mArrayList=" + mArrayList +
                '}';
    }

    public StickerInfo clone() {
        return new StickerInfo(this);
    }

    public void set(StickerInfo info) {
        this.mRectOriginal = new Rect(mRectOriginal);
        this.nPreviewAsp = info.nPreviewAsp;
        this.width = info.width;
        this.height = info.height;
        this.left = info.left;
        this.top = info.top;
        this.id = info.id;
        this.styleId = info.styleId;
        this.mCategory = info.mCategory;
        this.mIcon = info.mIcon;
        setDisf(info.getDisf());
        setCenterxy(info.getCenterxy());
    }

    private int mShadowColor;

    @Override
    public int getShadowColor() {
        return mShadowColor;
    }

    @Override
    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
        setChanged();
    }


    @Override
    public long getStart() {
        return mStart;
    }

    @Override
    public void setStart(long start) {
        mStart = start;
        setChanged();
    }

    @Override
    public long getEnd() {
        return mEnd;
    }

    @Override
    public void setEnd(long end) {
        mEnd = end;
        setChanged();
    }

    @Override
    public void offTimeLine(int offTime) {
        super.offTimeLine(offTime);
        if (null != mArrayList) {
            //处理特效list
            int len = mArrayList.size();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    CaptionLiteObject info = mArrayList.get(i);
                    float fpoff = Utils.ms2s(offTime);
                    float nstart = info.getTimelineStart() + fpoff, nend = info
                            .getTimelineEnd() + fpoff;
                    info.setTimelineRange(nstart, nend);
                }
            }
        }
    }


    /**
     * 设置时间线 (单位:毫秒)
     *
     * @param start 开始
     * @param end   结束
     */
    @Override
    public void setTimelineRange(long start, long end) {
        mStart = start;
        mEnd = end;
        setChanged();

    }


    private float mAngle;

    @Override
    public void setRotateAngle(float rotateAngle) {
        mAngle = rotateAngle;
        setChanged();
    }

    @Override
    public String getText() {
        return "";
    }

    public String getTtfLocalPath() {
        return "";
    }

    @Deprecated
    @Override
    void setText(String text) {

    }


    public int getTextSize() {
        return 0;
    }


    public StickerInfo() {
        super();
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof StickerInfo) {
            StickerInfo info = (StickerInfo) o;
            return TextUtils.equals(getText(), info.getText())
                    && getStart() == info.getStart()
                    && getEnd() == info.getEnd()
                    && getId() == info.getId()
                    && getRotateAngle() == info.getRotateAngle()
                    && getTextColor() == info.getTextColor()
                    && getDisf() == info.getDisf()
                    && getCenterxy() == info.getCenterxy()
                    && getStyleId() == info.getStyleId();

        } else {
            return false;
        }
    }


    public StickerInfo(StickerInfo info) {
        this.mRectOriginal = new Rect(mRectOriginal);
        this.mStart = info.mStart;
        this.mEnd = info.mEnd;
        this.mAngle = info.mAngle;
        this.nPreviewAsp = info.nPreviewAsp;
        this.width = info.width;
        this.height = info.height;
        this.left = info.left;
        this.top = info.top;
        this.id = info.id;
        this.mCategory = info.mCategory;
        this.mIcon = info.mIcon;
        setStyleId(info.getStyleId());
        ArrayList<CaptionLiteObject> temps = info.getList();
        int len = temps.size();
        for (int i = 0; i < len; i++) {
            this.mArrayList.add(new CaptionLiteObject(temps.get(i)));
        }
        setDisf(info.getDisf());
    }

    public float getRotateAngle() {
        return mAngle;
    }


    public float getDisf() {
        return mDisf;
    }

    private float mDisf = 1f;

    @Override
    public void setDisf(float disf) {
        mDisf = disf;
        setChanged();
    }

    //贴纸分类代码、贴纸图标
    private String mCategory;
    private String mIcon;

    public String getIcon() {
        return mIcon;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String mCategory, String icon) {
        this.mCategory = mCategory;
        this.mIcon = icon;
    }

    /**
     * 每个特效的jni对象列表
     */
    private ArrayList<CaptionLiteObject> mArrayList = new ArrayList<CaptionLiteObject>();

    public ArrayList<CaptionLiteObject> getList() {
        return mArrayList;
    }

    /**
     *
     */
    public void recycle() {
        mArrayList.clear();
    }


    public void addSubObject(CaptionLiteObject subobj) {
        mArrayList.add(subobj);
    }

    /**
     * 移动到草稿箱
     *
     * @param basePath
     */
    public void moveToDraft(String basePath) {
        int len = mArrayList.size();
        for (int i = 0; i < len; i++) {
            CaptionLiteObject tmp = mArrayList.get(i).moveToDraft(basePath);
            if (null != tmp) {
                mArrayList.set(i, tmp);
            }
        }
    }

    /**
     * 实时清理播放器中的对象
     */
    public void removeListLiteObject(VirtualVideo virtualVideo) {
        if (null != mArrayList) {
            int count = mArrayList.size();
            for (int j = 0; j < count; j++) { //删除旧的对象
                virtualVideo.deleteSubtitleObject(mArrayList.get(j));
            }
        }
    }


}
