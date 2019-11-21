package com.rd.veuisdk.model;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Parcel;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.rd.vecore.models.caption.CaptionAnimation;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 仅字幕
 * updatetime: 180829
 */
public class WordInfo extends ISubStickerInfo {
    private static final String TAG = "WordInfo";
    private CaptionObject mCaptionObject;


    protected WordInfo(Parcel in) {

        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();
            if (tVer >= 2) {
                mInputTextHor = in.readString();
                mInputTextVer = in.readString();
            }
            if (tVer >= 1) {
                this.mSpanList = in.createTypedArrayList(ISpan.CREATOR);
            }
        } else {
            this.mSpanList = new ArrayList<>();
            //恢复到读取之前的index
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

        mCaptionObject = in.readParcelable(CaptionObject.class.getClassLoader());
        mList = in.createTypedArrayList(PointF.CREATOR);
        mCaptionAnimation = in.readParcelable(CaptionAnimation.class.getClassLoader());
        checkId = in.readInt();
        mInputTextColor = in.readInt();
        mTextColor = in.readInt();
        mInputTextColorAlpha = in.readFloat();
        mInputTextStrokeAlpha = in.readFloat();
        mInputTextStrokeWidth = in.readFloat();
        mIsBold = in.readByte() != 0;
        mIsItalic = in.readByte() != 0;
        mIsShadow = in.readByte() != 0;
        mInputText = in.readString();
        mText = in.readString();
        mInputTTF = in.readString();

        testSpannable();
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "190401WordInfo";
    private static final int VER = 2;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeString(mInputTextHor);
        dest.writeString(mInputTextVer);
        dest.writeTypedList(mSpanList);

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

        dest.writeParcelable(mCaptionObject, flags);
        dest.writeTypedList(mList);
        dest.writeParcelable(mCaptionAnimation, flags);
        dest.writeInt(checkId);
        dest.writeInt(mInputTextColor);
        dest.writeInt(mTextColor);
        dest.writeFloat(mInputTextColorAlpha);
        dest.writeFloat(mInputTextStrokeAlpha);
        dest.writeFloat(mInputTextStrokeWidth);
        dest.writeByte((byte) (mIsBold ? 1 : 0));
        dest.writeByte((byte) (mIsItalic ? 1 : 0));
        dest.writeByte((byte) (mIsShadow ? 1 : 0));
        dest.writeString(mInputText);
        dest.writeString(mText);
        dest.writeString(mInputTTF);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WordInfo> CREATOR = new Creator<WordInfo>() {
        @Override
        public WordInfo createFromParcel(Parcel in) {
            return new WordInfo(in);
        }

        @Override
        public WordInfo[] newArray(int size) {
            return new WordInfo[size];
        }
    };

    public CaptionObject getCaptionObject() {
        return mCaptionObject;
    }

    @Override
    public String toString() {
        return "WordInfo{" +
                "TAG='" + TAG + '\'' +
//                ", width=" + width +
//                ", height=" + height +
//                ", widthx=" + widthx +
//                ", heighty=" + heighty +
//                ", left=" + left +
//                ", top=" + top +
                ", id=" + id +
                ", mCaptionObject=" + mCaptionObject +
                ", mList=" + mList +
                ", styleId=" + styleId +
//                ", centerxy=" + (null != centerxy ? Arrays.toString(centerxy) : "null") +
//                ", mCaptionAnimation=" + mCaptionAnimation +
                ", checkId=" + checkId +
//                ", mInputTextColor=" + mInputTextColor +
//                ", mTextColor=" + mTextColor +
//                ", mInputTextColorAlpha=" + mInputTextColorAlpha +
//                ", mInputTextStrokeAlpha=" + mInputTextStrokeAlpha +
//                ", mInputTextStrokeWidth=" + mInputTextStrokeWidth +
//                ", mIsBold=" + mIsBold +
//                ", mIsItalic=" + mIsItalic +
//                ", mIsShadow=" + mIsShadow +
                ", mInputText='" + mInputText + '\'' +
                ", mText='" + mText + '\'' +
                ", mInputTTF='" + mInputTTF + '\'' +
                ", changed=" + changed +
                '}';
    }

    /**
     * 控制器的四个顶点坐标
     *
     * @param list
     */
    public void setList(List<PointF> list) {
        if (null == mList) {
            mList = new ArrayList<>();
        } else {
            mList.clear();
        }
        if (null != list) {
            for (PointF pointF : list) {
                mList.add(pointF);
            }
        }
    }

    private List<PointF> mList;

    public List<PointF> getListPointF() {
        return mList;
    }


    public WordInfo clone() {
        return new WordInfo(this);
    }

    public void set(WordInfo info) {
        this.width = info.width;
        this.height = info.height;
        this.widthx = info.widthx;
        this.heighty = info.heighty;
        this.left = info.left;
        this.top = info.top;
        this.id = info.id;
        this.centerxy = info.centerxy;
        this.styleId = info.styleId;
        this.mCaptionObject = new CaptionObject(info.getCaptionObject());

        this.mInputTextVer = info.mInputTextVer;
        this.mInputTextHor = info.mInputTextHor;
        setDisf(info.getDisf());
        setInputText(info.getInputText());
        setText(info.getText());
        setCenterxy(info.getCenterxy());
        setInputTextColor(info.getInputTextColor());
        setTextColor(info.getTextColor());
        setInputTextColorAlpha(info.getInputTextColorAlpha());
        setInputTextStrokeAlpha(info.getInputTextStrokeAlpha());
        setInputTextStrokeWidth(info.getInputTextStrokeWidth());
        setList(info.mList);
        checkId = info.checkId;
    }

    public int getShadowColor() {
        return mCaptionObject.getShadowColor();
    }

    public void setShadowColor(int shadowColor) {
        mCaptionObject.setShadowColor(shadowColor);
        setChanged();
    }


    public CaptionAnimation getAnimType() {
        return mCaptionAnimation;
    }

    private CaptionAnimation mCaptionAnimation;

    /**
     * 获取动画类型
     *
     * @return
     */
    public int getCheckId() {
        return checkId;
    }

    private int checkId;

    public void setAnimType(CaptionAnimation anim, int checkId) {

        this.checkId = checkId;
        mCaptionAnimation = anim;
        getCaptionObject().setImageAnim(mCaptionAnimation);
        getCaptionObject().setTextAnim(mCaptionAnimation);
    }

    @Override
    public long getStart() {
        return Utils.s2ms(mCaptionObject.getTimelineStart());
    }

    @Override
    public void setStart(long start) {
        mCaptionObject.setTimelineRange(Utils.ms2s(start), mCaptionObject.getTimelineEnd());
        setChanged();
    }

    @Override
    public long getEnd() {
        return Utils.s2ms(mCaptionObject.getTimelineEnd());
    }

    @Override
    public void setEnd(long end) {
        mCaptionObject.setTimelineRange(mCaptionObject.getTimelineStart(), Utils.ms2s(end));
        setChanged();
    }

    /**
     * 设置时间线 (单位:毫秒)
     *
     * @param start 开始
     * @param end   结束
     */
    @Override
    public void setTimelineRange(long start, long end) {
        mCaptionObject.setTimelineRange(Utils.ms2s(start), Utils.ms2s(end));
        setChanged();
    }


    @Override
    public void setRotateAngle(float rotateAngle) {
        mCaptionObject.rotateCaption(rotateAngle);
        setChanged();
    }


    public String getText() {
        return mText;
    }


    private List<ISpan> mSpanList = new ArrayList<>();

    //测试多样式的开关
    private boolean enableTest = false;

    //初始化-子样式
    private void initSpanList() {
        mSpanList.clear();
        if (enableTest) {
            mSpanList.add(new ISpan(Color.argb(225, 225, 0, 0), 0, 2));
            mSpanList.add(new ISpan(Color.argb(225, 0, 255, 0), 2, 4));
            mSpanList.add(new ISpan(Color.argb(225, 0, 0, 225), 4, 6));

        }
    }

    /***
     * 测试  SpannableString支持自定义多种效果
     * @param spannableString
     */
    private void onSpanToSapannable(SpannableString spannableString) {
        if (spannableString.length() > 6) {
            int len = mSpanList.size();
            for (int i = 0; i < len; i++) {
                ISpan iSpan = mSpanList.get(i);
                spannableString.setSpan(new ForegroundColorSpan(iSpan.getTextColor()), iSpan.getStart(), iSpan.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void testSpannable() {
        //测试字幕多样式
        if (!TextUtils.isEmpty(mText)) {
            SpannableString spannableString = new SpannableString(mText);
            onSpanToSapannable(spannableString);
            mCaptionObject.setText(spannableString);
        } else {
            mCaptionObject.setText("");
        }
    }

    //字幕特效各个样式默认的文本
    @Override
    public void setText(String text) {
        mText = text;
        {
            //测试字幕多样式
            testSpannable();
        }
//        mCaptionObject.setInputText(text);
        setChanged();
    }

    public String getInputText() {
        return mInputText;
    }

    public void setInputText(String inputText) {
        mInputText = inputText;
        setText(inputText);
    }

    public int getInputTextColor() {
        return mInputTextColor;
    }

    /***
     * 用户手动触发的改变颜色字体颜色
     * @param inputTextColor
     */
    public void setInputTextColor(int inputTextColor) {
        mInputTextColor = inputTextColor;
        setTextColor(mInputTextColor);
    }

    public float getInputTextColorAlpha() {
        return mInputTextColorAlpha;
    }

    public float getInputTextStrokeAlpha() {
        return mInputTextStrokeAlpha;
    }

    public float getInputTextStrokeWidth() {
        return mInputTextStrokeWidth;
    }

    /***
     * 用户手动触发的改变颜色字体颜色
     * @param inputTextColorAlpha
     */
    public void setInputTextColorAlpha(float inputTextColorAlpha) {
        mInputTextColorAlpha = inputTextColorAlpha;
        setTextColorAlpha(mInputTextColorAlpha);
    }

    public void setInputTextStrokeAlpha(float inputTextStrokeAlpha) {
        mInputTextStrokeAlpha = inputTextStrokeAlpha;
        setTextStrokeAlpha(mInputTextStrokeAlpha);
    }

    public void setInputTextStrokeWidth(float inputTextStrokeWidth) {
        mInputTextStrokeWidth = inputTextStrokeWidth;
        setTextStrokeWidth(mInputTextStrokeWidth);
    }

    public boolean isBold() {
        return mIsBold;
    }

    public void setBold(boolean isBold) {
        this.mIsBold = isBold;
        mCaptionObject.setBold(mIsBold);
    }

    public boolean isItalic() {
        return mIsItalic;
    }

    public void setItalic(boolean isItalic) {
        this.mIsItalic = isItalic;
        mCaptionObject.setItalic(mIsItalic);
    }

    public boolean isShadow() {
        return mIsShadow;
    }

    public void setShadow(boolean isShadow) {
        this.mIsShadow = isShadow;
        mCaptionObject.setShadow(mIsShadow);
    }

    private int mInputTextColor = Color.WHITE;
    private int mTextColor;
    private float mInputTextColorAlpha = 1;
    private float mInputTextStrokeAlpha = 1;
    private float mInputTextStrokeWidth = 2;
    private boolean mIsBold = false;
    private boolean mIsItalic = false;
    private boolean mIsShadow = false;


    //输入框写入的文本
    private String mInputText;
    private String mText;

    public String getInputTextVer() {
        return mInputTextVer;
    }

    public String getInputTextHor() {
        return mInputTextHor;
    }


    /**
     * @param textHor 横向排版的文字
     * @param textVer 纵向排版的文字
     */
    public void setInputText(String textHor, String textVer) {
        mInputTextHor = textHor;
        mInputTextVer = textVer;
    }

    //单列排版文本
    private String mInputTextVer = "";
    //单行时排版
    private String mInputTextHor = "";


    public WordInfo() {
        mCaptionObject = new CaptionObject();
        initSpanList();
    }


    @Override
    public boolean equals(Object o) {
        if (null != o && (o instanceof WordInfo)) {
            WordInfo info = (WordInfo) o;
            return TextUtils.equals(getText(), info.getText())
                    && getStart() == info.getStart()
                    && getEnd() == info.getEnd()
                    && getId() == info.getId()
                    && TextUtils.equals(getTtfLocalPath(),
                    info.getTtfLocalPath())
                    && getRotateAngle() == info.getRotateAngle()
                    && getTextColor() == info.getTextColor()
                    && getDisf() == info.getDisf()
                    && centerxy == info.getCenterxy()
                    && getStyleId() == info.getStyleId();

        } else {
            return false;
        }
    }


    public WordInfo(WordInfo info) {
        this.width = info.width;
        this.height = info.height;
        this.widthx = info.widthx;
        this.heighty = info.heighty;
        this.left = info.left;
        this.top = info.top;
        this.id = info.id;
        this.centerxy = info.centerxy;
        this.mInputTextVer = info.mInputTextVer;
        this.mInputTextHor = info.mInputTextHor;
        setStyleId(info.getStyleId());

        mCaptionObject = new CaptionObject(info.getCaptionObject());

        setDisf(info.getDisf());
        mText = info.getText();
        mInputText = info.getInputText();
        mInputTTF = info.getInputTTF();
        mInputTextColor = info.getInputTextColor();
        mTextColor = info.getTextColor();
        mInputTextColorAlpha = info.getInputTextColorAlpha();
        mInputTextStrokeAlpha = info.getInputTextStrokeAlpha();
        mInputTextStrokeWidth = info.getInputTextStrokeWidth();
        mIsBold = info.isBold();
        mIsItalic = info.isItalic();
        mIsShadow = info.isShadow();
        this.changed = info.IsChanged();
        if (null != info.mCaptionAnimation) {
            this.mCaptionAnimation = new CaptionAnimation(info.mCaptionAnimation);
        }
        setList(info.mList);
        this.checkId = info.checkId;
        if (null != info.mSpanList) {
            this.mSpanList.addAll(info.mSpanList);
        } else {
            initSpanList();
        }
    }

    public float getRotateAngle() {
        return mCaptionObject.getRotateCaption();
    }


    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mCaptionObject.setTextColor(textColor);
        setChanged();
    }

    public int getTextColorAlpha() {
        return mCaptionObject.getTextColorAlpha();
    }

    /**
     * 0~1.0f
     *
     * @param textColorAlpha
     */
    public void setTextColorAlpha(float textColorAlpha) {
        mCaptionObject.setTextColorAlpha((int) (textColorAlpha * 255));
        setChanged();
    }

    public int getTextStrokeAlpha() {
        return mCaptionObject.getTextStrokeAlpha();
    }

    /**
     * 0~1.0f
     *
     * @param textStrokeAlpha
     */
    public void setTextStrokeAlpha(float textStrokeAlpha) {
        mCaptionObject.setTextStrokeAlpha((int) (textStrokeAlpha * 255));
        setChanged();
    }

    public float getTextStrokeWidth() {
        return mCaptionObject.getTextStrokeWidth();
    }

    public void setTextStrokeWidth(float textStrokeWidth) {
        mCaptionObject.setTextStrokeWidth(textStrokeWidth);
        setChanged();
    }

    public String getInputTTF() {
        return mInputTTF;
    }

    /**
     * 手动设置字体
     *
     * @param inputTTF
     */
    public void setInputTTF(String inputTTF) {
        mInputTTF = inputTTF;
        setTtfLocalPath(mInputTTF);
    }

    private String mInputTTF = null;


    public String getTtfLocalPath() {
        return mCaptionObject.getFontFilePath();
    }

    /**
     * 自定义字体
     *
     * @param ttfLocalPath
     */
    public void setTtfLocalPath(String ttfLocalPath) {
        mCaptionObject.setFontByFilePath(ttfLocalPath);
        setChanged();
    }

    public float getDisf() {
        return mCaptionObject.getScale();
    }

    public void setDisf(float disf) {
        mCaptionObject.setScale(disf);
        setChanged();
    }


    /**
     *
     */
    public void recycle() {
        if (null != mCaptionObject) {
            mCaptionObject.recycle();
        }
    }


}
