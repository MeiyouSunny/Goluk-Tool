package com.mobnote.golukmain.specialsetting.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * this is the first time to user Parcelable
 * I Strongly recommend to use Parcelable to replace Serializable
 * http://www.developerphil.com/parcelable-vs-serializable/
 */
public class CarBrandBean implements Parcelable {
    /**
     * 品牌id
     */
    @JSONField(name = "brandid")
    public String brandId;
    /**
     * 品牌名称
     */
    @JSONField(name = "name")
    public String name;
    /**
     * 品牌名称（字母顺序）
     */
    @JSONField(name = "alphaname")
    public String alphaName;
    /**
     * 品牌编码
     */
    @JSONField(name = "code")
    public String code;
    /**
     * LOGO文件Url
     */
    @JSONField(name = "logo")
    public String logoUrl;
    /**
     * 描述
     */
    @JSONField(name = "description")
    public String description;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.brandId);
        dest.writeString(this.name);
        dest.writeString(this.alphaName);
        dest.writeString(this.code);
        dest.writeString(this.logoUrl);
        dest.writeString(this.description);
    }

    public CarBrandBean() {
    }

    protected CarBrandBean(Parcel in) {
        this.brandId = in.readString();
        this.name = in.readString();
        this.alphaName = in.readString();
        this.code = in.readString();
        this.logoUrl = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<CarBrandBean> CREATOR = new Parcelable.Creator<CarBrandBean>() {
        @Override
        public CarBrandBean createFromParcel(Parcel source) {
            return new CarBrandBean(source);
        }

        @Override
        public CarBrandBean[] newArray(int size) {
            return new CarBrandBean[size];
        }
    };


}
