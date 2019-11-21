package com.rd.veuisdk.model;

import com.rd.veuisdk.utils.FileUtils;

/**
 * 网络接口返回的字幕、贴纸、字体、音乐
 *
 * @author JIAN
 * @create 2019/1/14
 * @Describe
 */
public class IApiInfo {

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String file;

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUrl() {
        return file;
    }

    private String cover;

    public void setUpdatetime(long updatetime) {
        this.updatetime = updatetime;
    }

    private long updatetime;
    private String localPath;

    /**
     * @param name
     * @param file
     * @param cover
     * @param updatetime
     */
    public IApiInfo(String name, String file, String cover, long updatetime) {
        this.name = name;
        this.file = file;
        this.cover = cover;
        this.updatetime = updatetime;
    }

    /**
     * @param name
     * @param file
     * @param cover
     * @param localPath
     * @param updatetime
     */
    public IApiInfo(String name, String file, String cover, String localPath, long updatetime) {
        this.name = name;
        this.file = file;
        this.cover = cover;
        this.localPath = localPath;
        this.updatetime = updatetime;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getCover() {
        return cover;
    }

    public long getUpdatetime() {
        return updatetime;
    }


    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }


    @Override
    public String toString() {
        return "IApiInfo{" +
                "name='" + name + '\'' +
                ", file='" + file + '\'' +
                ", cover='" + cover + '\'' +
                ", updatetime=" + updatetime +
                ", localPath='" + localPath + '\'' +
                '}';
    }

    /**
     * 是否存在本地文件
     *
     * @return
     */
    public boolean isExistFile() {
        return FileUtils.isExist(localPath);
    }

    /**
     * 是rd Music
     *
     * @return
     */
    public boolean existsMusic() {
        return com.rd.lib.utils.FileUtils.isExist(localPath);
    }
}
