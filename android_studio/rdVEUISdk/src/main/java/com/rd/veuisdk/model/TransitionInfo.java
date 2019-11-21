package com.rd.veuisdk.model;

import com.rd.vecore.models.Transition;

/**
 * 转场
 */
public class TransitionInfo extends IApiInfo {
    private int coreFilterId = Transition.Unknown;

    public int getCoreFilterId() {
        return coreFilterId;
    }

    public void setCoreFilterId(int coreFilterId) {
        this.coreFilterId = coreFilterId;
    }
    /**
     * 网络转场
     *
     * @param url
     * @param img
     * @param name
     * @param _localPath
     * @param updatetime
     */
    public TransitionInfo(String url, String img, String name, String _localPath, long updatetime) {
        super(name, url, img, _localPath, updatetime);
    }

    /**
     * 本地转场
     *
     * @param id
     * @param text
     * @param strGrayAlphaPath
     */
    public TransitionInfo(int id, String text, String strGrayAlphaPath) {
        super(text, strGrayAlphaPath, strGrayAlphaPath, strGrayAlphaPath, 0);

    }



    @Override
    public String toString() {
        return "TransitionInfo{" +
                "coreFilterId=" + coreFilterId + "  >>" + super.toString() +
                '}';
    }
}
