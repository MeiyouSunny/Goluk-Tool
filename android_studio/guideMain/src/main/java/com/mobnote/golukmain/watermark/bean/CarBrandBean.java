package com.mobnote.golukmain.watermark.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * this is the first time to user Parcelable
 * I Strongly recommend to use Parcelable to replace Serializable
 * http://www.developerphil.com/parcelable-vs-serializable/
 */
public class CarBrandBean implements Serializable {
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

}
