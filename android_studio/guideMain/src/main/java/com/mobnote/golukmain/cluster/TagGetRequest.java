package com.mobnote.golukmain.cluster;

import android.text.TextUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.cluster.bean.TagRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import java.util.HashMap;

import cn.com.tiros.api.Tapi;

/**
 * Created by DELL-PC on 2016/7/29.
 */
public class TagGetRequest extends GolukFastjsonRequest<TagRetBean> {

    public TagGetRequest(int requestType, IRequestResultListener listener) {
        super(requestType, TagRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/tag.htm";
    }

    @Override
    protected String getMethod() {
        return "tagInfo";
    }

    public void get(String protocol, String tagId) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("tagid", tagId);
        headers.put("xieyi", protocol);
        get();
    }
}
