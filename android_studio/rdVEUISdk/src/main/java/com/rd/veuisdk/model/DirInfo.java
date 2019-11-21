package com.rd.veuisdk.model;

import java.util.List;

/**
 *
 * 单个目录
 */
public class DirInfo implements IDirInfo {
    @Override
    public List<ImageItem> getList() {
        return mList;
    }

    public DirInfo(String bucketName, String bucketId, List<ImageItem> list) {
        this.bucketName = bucketName;
        this.bucketId = bucketId;
        mList = list;
    }

    public DirInfo(String bucketName, String bucketId) {
        this.bucketName = bucketName;
        this.bucketId = bucketId;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }


    /**
     * 文件夹名称
     */
    private String bucketName;
    private String bucketId;

    public void setList(List<ImageItem> list) {
        mList = list;
    }

    /***
     * 单个文件夹的数据
     */
    private List<ImageItem> mList;


}
