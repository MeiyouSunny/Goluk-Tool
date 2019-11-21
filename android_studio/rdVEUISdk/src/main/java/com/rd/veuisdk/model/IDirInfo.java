package com.rd.veuisdk.model;

import java.util.List;


/**
 * 单个目录
 */
public interface IDirInfo {

    List<ImageItem> getList();

    String getBucketName();
}
