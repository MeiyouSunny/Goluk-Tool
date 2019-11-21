package com.rd.veuisdk.model.bean;

import android.support.annotation.Keep;

/**
 * 单个list->item 数据
 *
 * @create 2019/7/4
 */
@Keep
public class DataBean {


    /**
     * id : 576472
     * cover : https://rdfile.oss-cn-hangzhou.aliyuncs.com/filemanage/6ecb39f1c12f1a35/videoae/1561704867750/cover.jpg
     * width : 579
     * height : 1030
     * file : https://rdfile.oss-cn-hangzhou.aliyuncs.com/filemanage/6ecb39f1c12f1a35/videoae/1561704867750/file.zip
     * name : 烟雾蹦迪卡点
     * category : 0
     * categoryname : 未分类
     * video : https://rdfile.oss-cn-hangzhou.aliyuncs.com/filemanage/6ecb39f1c12f1a35/videoae/1561704867750/video.mp4
     * text_need : 0
     * video_need : 0
     * picture_need : 5
     * updatetime : 1561704886
     */

    private String id;
    private String cover;
    private int width;
    private int height;
    private String file;
    private String name;
    private String category;
    private String categoryname;
    private String video;
    private int text_need;
    private int video_need;
    private int picture_need;
    private long updatetime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public int getText_need() {
        return text_need;
    }

    public void setText_need(int text_need) {
        this.text_need = text_need;
    }

    public int getVideo_need() {
        return video_need;
    }

    public void setVideo_need(int video_need) {
        this.video_need = video_need;
    }

    public int getPicture_need() {
        return picture_need;
    }

    public void setPicture_need(int picture_need) {
        this.picture_need = picture_need;
    }

    public long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(long updatetime) {
        this.updatetime = updatetime;
    }
}
