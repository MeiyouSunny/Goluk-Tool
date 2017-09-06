package com.rd.veuisdk.manager;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 人脸贴纸
 */
public class FaceuConfig implements Parcelable {

    private String webUrl;

    private boolean enableNetFaceU = false;

    public float getColor_level() {
        return color_level;
    }

    /**
     * 设置开启美颜时美白的等级 默认0.48f(参数值0.0f-1.0f)
     *
     * @param color_level
     */
    public void setColor_level(float color_level) {
        this.color_level = Math.min(1.0f, Math.max(0.0f, color_level));
    }

    public float getBlur_level() {
        return blur_level;
    }

    /**
     * 设置开启美颜时磨皮的等级 默认4f(参数值0.0f-6.0f)
     *
     * @param level
     */
    public void setBlur_level(float level) {
        this.blur_level = Math.min(6.0f, Math.max(0.0f, level));
    }

    public float getCheek_thinning() {
        return cheek_thinning;
    }

    /**
     * 设置开启美颜时瘦脸的等级 默认0.68f(有效参数值0.0f-2.0f)
     *
     * @param level
     */
    public void setCheek_thinning(float level) {
        this.cheek_thinning = Math.min(2.0f, Math.max(0.0f, level));
    }

    public float getEye_enlarging() {
        return eye_enlarging;
    }

    /**
     * 设置开启美颜时大眼的等级 默认1.53f(有效参数值0.0f-4.0f)
     *
     * @param level
     */
    public void setEye_enlarging(float level) {
        this.eye_enlarging = Math.min(4.0f, Math.max(0.0f, level));
    }

    private float color_level = 0.48f;
    private float blur_level = 4f;
    private float cheek_thinning = 0.68f;
    private float eye_enlarging = 1.53f;

    public boolean isEnableNetFaceu() {
        return enableNetFaceU;
    }

    /**
     * 是否启用加载网络化的人脸贴纸资源
     *
     * @param enableNetFaceu true 将从传入的网络接口获取faceU资源(必须设置请求地址setUrl(webUrl))<br/>
     *                       false 加载本地的离线资源 ( 默认false)<br/>
     * @param webUrl         (为true 时,设置网络人脸贴纸的请求接口)
     */
    public void enableNetFaceu(boolean enableNetFaceu, String webUrl) {
        this.enableNetFaceU = enableNetFaceu;
        this.webUrl = webUrl;
    }

    public String getUrl() {
        return webUrl;
    }


    /**
     * 加载网络化的贴纸文件
     */
    private ArrayList<FaceInfo> webInfos = new ArrayList<FaceInfo>();

    public ArrayList<FaceInfo> getWebInfos() {
        return webInfos;
    }

    public void setWebInfos(ArrayList<FaceInfo> webInfos) {
        this.webInfos = webInfos;
    }


    public ArrayList<FaceInfo> getLists() {
        return lists;
    }

    /**
     * 设置人脸效果集合
     *
     * @param lists
     * @return
     */
    public FaceuConfig setLists(ArrayList<FaceInfo> lists) {
        this.lists = lists;
        return this;
    }

    private ArrayList<FaceInfo> lists = new ArrayList<FaceInfo>();

    public FaceuConfig(
            ArrayList<FaceInfo> lists) {
        super();

        this.lists = lists;
    }

    public FaceuConfig() {

    }

    /**
     * 新增单个人脸效果
     *
     * @param info
     * @return
     */
    public FaceuConfig addFaceu(FaceInfo info) {

        if (null == lists) {
            lists = new ArrayList<FaceInfo>();
        }
        if (null != info) {
            lists.add(info);
        }
        return this;
    }

    /**
     * 新增单个人脸贴纸
     *
     * @param path  .mp3 人脸贴纸本地路径
     * @param icon  人脸贴纸本地图标
     * @param title 说明
     * @return
     */
    public FaceuConfig addFaceu(String path, String icon, String title) {
        return addFaceu(new FaceInfo(path, icon, title));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.webUrl);
        dest.writeByte(this.enableNetFaceU ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.color_level);
        dest.writeFloat(this.blur_level);
        dest.writeFloat(this.cheek_thinning);
        dest.writeFloat(this.eye_enlarging);
        dest.writeList(this.webInfos);
        dest.writeList(this.lists);
    }

    protected FaceuConfig(Parcel in) {

        this.webUrl = in.readString();
        this.enableNetFaceU = in.readByte() != 0;
        this.color_level = in.readFloat();
        this.blur_level = in.readFloat();
        this.cheek_thinning = in.readFloat();
        this.eye_enlarging = in.readFloat();
        this.webInfos = new ArrayList<FaceInfo>();
        in.readList(this.webInfos, FaceInfo.class.getClassLoader());
        this.lists = new ArrayList<FaceInfo>();
        in.readList(this.lists, FaceInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<FaceuConfig> CREATOR = new Parcelable.Creator<FaceuConfig>() {
        @Override
        public FaceuConfig createFromParcel(Parcel source) {
            return new FaceuConfig(source);
        }

        @Override
        public FaceuConfig[] newArray(int size) {
            return new FaceuConfig[size];
        }
    };
}
