package com.rd.veuisdk.model;

/**
 * 定义显示扫描文件路径的接口
 * 
 * @author johnny
 * 
 */
public interface OnShowScanFileInterface {
    /**
     * 返回扫描文件路径
     * 
     * @param path
     */
    public void scanFilePath(String path);

    /**
     * 返回扫描文件的新增条数
     * 
     * @param newNum
     */
    public void scanNewFileNum(int newNum);
}
