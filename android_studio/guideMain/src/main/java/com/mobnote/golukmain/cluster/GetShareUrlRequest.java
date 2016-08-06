package com.mobnote.golukmain.cluster;

import java.util.HashMap;

import com.mobnote.golukmain.cluster.bean.GetClusterShareUrlData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class GetShareUrlRequest extends GolukFastjsonRequest<GetClusterShareUrlData> {

    public GetShareUrlRequest(int requestType, IRequestResultListener listener) {
        super(requestType, GetClusterShareUrlData.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/tag.htm";
    }

    @Override
    protected String getMethod() {
        return "share";
    }

    public void get(String protocol, String tagId) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", protocol);
        headers.put("tagid", tagId);
        get();
    }
}
