package com.rd.veuisdk.model;

import java.util.List;

/**
 * 字说
 */
public class ZiShuoInfo {
    private String TAG = "ZiShuoInfo";

    public List<AETextMediaInfo> getAETextMediaList() {
        return mAETextMediaList;
    }

    public void setAETextMediaList(List<AETextMediaInfo> AETextMediaList) {
        mAETextMediaList = AETextMediaList;

//        int len = mAETextMediaList.size();
//        for (int i = 0; i < len; i++) {
//            Log.e(TAG, "setAETextMediaList: " + i + "/" + len + "  " + mAETextMediaList.get(i));
//        }
    }

    private List<AETextMediaInfo> mAETextMediaList;

    /**
     * @param name 名称
     * @param path 目标sd文件夹路径
     */
    public ZiShuoInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }


    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    private String name, path;

}
