package com.rd.veuisdk.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.VisualFilterConfig;

/**
 * 媒体滤镜
 *
 * @author JIAN
 * @create 2018/12/4
 * @Describe
 */
public class IMediaParamImp implements IMediaParam, Parcelable {
    public IMediaParamImp() {

    }


    protected IMediaParamImp(Parcel in) {
        mBrightness = in.readFloat();
        mContrast = in.readFloat();
        mSaturation = in.readFloat();
        mSharpen = in.readFloat();
        mWhite = in.readFloat();
        mVignette = in.readFloat();
        mVignetteId = in.readInt();
        nFilterMenuIndex = in.readInt();
        mCurrentFilterType = in.readInt();
        lookupConfig = in.readParcelable(VisualFilterConfig.class.getClassLoader());
    }

    @Override
    public String toString() {
        return "IMediaParamImp{" +
                "mBrightness=" + mBrightness +
                ", mContrast=" + mContrast +
                ", mSaturation=" + mSaturation +
                ", mSharpen=" + mSharpen +
                ", mWhite=" + mWhite +
                ", mVignette=" + mVignette +
                ", mVignetteId=" + mVignetteId +
                ", nFilterMenuIndex=" + nFilterMenuIndex +
                ", mCurrentFilterType=" + mCurrentFilterType +
                ", lookupConfig=" + lookupConfig +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mBrightness);
        dest.writeFloat(mContrast);
        dest.writeFloat(mSaturation);
        dest.writeFloat(mSharpen);
        dest.writeFloat(mWhite);
        dest.writeFloat(mVignette);
        dest.writeInt(mVignetteId);
        dest.writeInt(nFilterMenuIndex);
        dest.writeInt(mCurrentFilterType);
        dest.writeParcelable(lookupConfig, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IMediaParamImp> CREATOR = new Creator<IMediaParamImp>() {
        @Override
        public IMediaParamImp createFromParcel(Parcel in) {
            return new IMediaParamImp(in);
        }

        @Override
        public IMediaParamImp[] newArray(int size) {
            return new IMediaParamImp[size];
        }
    };

    public float getBrightness() {
        return mBrightness;
    }

    public void setBrightness(float brightness) {
        mBrightness = brightness;
    }

    public float getContrast() {
        return mContrast;
    }

    public void setContrast(float contrast) {
        mContrast = contrast;
    }

    public float getSaturation() {
        return mSaturation;
    }

    public void setSaturation(float saturation) {
        mSaturation = saturation;
    }

    public float getSharpen() {
        return mSharpen;
    }

    public void setSharpen(float sharpen) {
        mSharpen = sharpen;
    }

    public float getWhite() {
        return mWhite;
    }

    public void setWhite(float white) {
        mWhite = white;
    }

    public float getVignette() {
        return mVignette;
    }

    public void setVignette(float vignette) {
        mVignette = vignette;
    }

    public int getVignetteId() {
        return mVignetteId;
    }

    public void setVignetteId(int vignetteId) {
        mVignetteId = vignetteId;
    }

    //调色
    //记录对比度、亮度、白平衡、锐度、饱和度
    private float mBrightness = Float.NaN, mContrast = Float.NaN, mSaturation = Float.NaN, mSharpen = Float.NaN, mWhite = Float.NaN, mVignette = Float.NaN;
    //记录暗角滤镜 （mVignetteId ==VisualFilterConfig.FILTER_ID_VIGNETTE时，暗角滤镜启用）
    public static final int NO_VIGNETTEDID = -1;
    private int mVignetteId = NO_VIGNETTEDID;

    /**
     * 参数值是否有意义
     * @return true 有效，false 默认效果
     */
    public boolean isValid() {
        if (Float.isNaN(mBrightness)             //亮度
//                && Float.isNaN(mExposure)                  //曝光
                && Float.isNaN(mContrast)            //对比度
                && Float.isNaN(mSaturation)          //饱和度
                && Float.isNaN(mWhite)   //白平衡 (色温)
                && Float.isNaN(mSharpen)) {           //锐度
//                && Float.isNaN(mFeatherX)) {              //左右羽化
            return false;
        }
        return true;
    }

    public IMediaParamImp(int nFilterMenuIndex, int currentFilterType, VisualFilterConfig lookupConfig) {
        this.nFilterMenuIndex = nFilterMenuIndex;
        mCurrentFilterType = currentFilterType;
        this.lookupConfig = lookupConfig;
    }

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


    public IMediaParamImp clone() {
        IMediaParamImp tmp = new IMediaParamImp(nFilterMenuIndex, mCurrentFilterType, lookupConfig);
        tmp.mBrightness = mBrightness;
        tmp.mContrast = mContrast;
        tmp.mSaturation = mSaturation;
        tmp.mSharpen = mSharpen;
        tmp.mWhite = mWhite;
        tmp.mVignette = mVignette;
        tmp.mVignetteId = mVignetteId;

        return tmp;
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

}
