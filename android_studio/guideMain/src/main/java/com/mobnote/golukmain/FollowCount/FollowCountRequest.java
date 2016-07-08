package com.mobnote.golukmain.FollowCount;

import com.mobnote.golukmain.FollowCount.bean.FollowCountRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import java.util.HashMap;

/**
 * Created by leege100 on 16/7/7.
 */
public class FollowCountRequest extends GolukFastjsonRequest<FollowCountRetBean> {
    public FollowCountRequest(int requestType, IRequestResultListener listener) {
        super(requestType, FollowCountRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcGraph/link.htm";
    }

    @Override
    protected String getMethod() {
        return "followCount";
    }

    public void get(String commuid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("commuid", commuid);
        get();
    }
}
