package com.rd.veuisdk.model;

import java.io.File;

/***
 * 加载网络INFO
 * mv、配乐共用此对象  (网络数据 url，name ，localPath)
 *
 * @author JIAN
 *
 */
public class WebInfo {


    private String url, img, name, localPath;


    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }




    /**
     * 是否是MV
     *
     * @return
     */
    public boolean exists() {
        File f = new File(localPath);
        String[] arr = null;
        // 目标目录 ：搞怪/gaoguai/
        // 异常情况 ：搞怪/
        return (null != f) && (f.isDirectory())
                && ((null != (arr = f.list())) && arr.length >= 2);// 该目录存在多个文件
    }

    /**
     * 是rd Music
     *
     * @return
     */
    public boolean existsMusic() {
        File f = new File(localPath);
        return (null != f) && f.isFile() && f.length() > 10;
    }


    public WebInfo(String url, String img, String name,
                   String _localPath) {
        super();
        this.url = url;
        this.img = img;
        this.name = name;
        this.localPath = _localPath;
    }




    private WebInfo() {

    }

}
