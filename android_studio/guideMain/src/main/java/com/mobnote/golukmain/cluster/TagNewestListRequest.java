package com.mobnote.golukmain.cluster;

import java.util.HashMap;

import com.mobnote.golukmain.cluster.bean.TagGeneralRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class TagNewestListRequest extends GolukFastjsonRequest<TagGeneralRetBean> {

    public TagNewestListRequest(int requestType, IRequestResultListener listener) {
        super(requestType, TagGeneralRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/tag.htm";
    }

    @Override
    protected String getMethod() {
        return "latestVideo";
    }

    public void get(String tagId, String operation, String timestamp, String pagesize) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("tagid", tagId);
        headers.put("operation", operation);
        headers.put("timestamp", timestamp);
        headers.put("pagesize", pagesize);
        get();
    }
}
