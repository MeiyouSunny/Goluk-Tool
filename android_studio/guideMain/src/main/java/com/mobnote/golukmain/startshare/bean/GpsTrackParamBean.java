package com.mobnote.golukmain.startshare.bean;

import com.mobnote.golukmain.startshare.GpsInfo;

import java.util.List;

/**
 * 上传视频GPS轨迹请求参数
 */
public class GpsTrackParamBean {

    public String uid;
    public String videoid;
    public List<GpsInfo> gps;

}
