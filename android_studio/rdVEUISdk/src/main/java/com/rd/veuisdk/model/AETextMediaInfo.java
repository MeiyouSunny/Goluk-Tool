package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rd.vecore.models.AEFragmentInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.ae.model.AETextLayerInfo;

/**
 * 仿quik 功能中 每个layer，对应一个AETextMediaInfo ，每个layer都要绑定一个mTextMediaObj
 */
public class AETextMediaInfo implements Parcelable {
    public AETextMediaInfo() {

    }

    protected AETextMediaInfo(Parcel in) {
        text = in.readString();
        mAETextLayerInfo = in.readParcelable(AETextLayerInfo.class.getClassLoader());
        ttf = in.readString();
        ttfIndex = in.readInt();
        mTextMediaObj = in.readParcelable(MediaObject.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeParcelable(mAETextLayerInfo, flags);
        dest.writeString(ttf);
        dest.writeInt(ttfIndex);
        dest.writeParcelable(mTextMediaObj, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AETextMediaInfo> CREATOR = new Creator<AETextMediaInfo>() {
        @Override
        public AETextMediaInfo createFromParcel(Parcel in) {
            return new AETextMediaInfo(in);
        }

        @Override
        public AETextMediaInfo[] newArray(int size) {
            return new AETextMediaInfo[size];
        }
    };

    public String getText() {
        return text;
    }

    /**
     * 文本（在仿Quik 功能中，text 对应AETextLayerInfo.getTextContent()  即为默认字符串)
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }


    private String text;


    public AETextLayerInfo getAETextLayerInfo() {
        return mAETextLayerInfo;
    }


    public AEFragmentInfo.LayerInfo getLayerInfo() {
        return mLayerInfo;
    }

    public void setAETextLayerInfo(AETextLayerInfo AETextLayerInfo, AEFragmentInfo.LayerInfo layerInfo) {
        mAETextLayerInfo = AETextLayerInfo;
        mLayerInfo = layerInfo;
    }


    private AETextLayerInfo mAETextLayerInfo;
    private AEFragmentInfo.LayerInfo mLayerInfo;


    private String ttf;
    private int ttfIndex;
    private MediaObject mTextMediaObj;

    public String getTtf() {
        return ttf;
    }

    public void setTtf(String ttf, int ttfIndex) {
        this.ttf = ttf;
        this.ttfIndex = ttfIndex;
    }

    public int getTtfIndex() {
        return ttfIndex;
    }


    public MediaObject getTextMediaObj() {
        return mTextMediaObj;
    }

    public void setTextMediaObj(MediaObject textMediaObj) {
        mTextMediaObj = textMediaObj;
    }


}
