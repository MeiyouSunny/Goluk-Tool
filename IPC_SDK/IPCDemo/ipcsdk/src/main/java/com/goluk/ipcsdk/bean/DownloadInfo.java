package com.goluk.ipcsdk.bean;

/**
 * Created by hanzheng on 2016/5/27.
 */
public class DownloadInfo {
    /**
     * file name
     */
    public String filename;
    /**
     * file size
     */
    public int filesize;

    /**
     * the received file size
     */
    public int filerecvsize;

    /**
     * download status 0：download success  1：downloading
     */
    public int status;
}
