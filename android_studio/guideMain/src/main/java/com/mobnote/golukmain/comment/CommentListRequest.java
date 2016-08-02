package com.mobnote.golukmain.comment;

import java.util.HashMap;

import com.mobnote.golukmain.comment.bean.CommentResultBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;

public class CommentListRequest extends GolukFastjsonRequest<CommentResultBean> {
    public static final int PAGE_SIZE = 20;

    public CommentListRequest(int requestType, IRequestResultListener listener) {
        super(requestType, CommentResultBean.class, listener);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getPath() {
        // TODO Auto-generated method stub
        return "/cdcComment/comment.htm";
    }

    @Override
    protected String getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getBySort(String topicid, String topictype, int operation, String timestamp,String sort){
        if (TextUtils.isEmpty(topicid)) {
            return false;
        }
        HashMap<String, String> paramters = (HashMap<String, String>) getParam();
        paramters.put("xieyi", "100");
        paramters.put("topicid", topicid);
        paramters.put("topictype", topictype);
        paramters.put("operation", "" + operation);
        paramters.put("timestamp", timestamp);
        paramters.put("pagesize", "" + PAGE_SIZE);
        paramters.put("sort",sort);//0或其它倒序，1顺序
        super.get();
        return true;
    }
    public boolean get(String topicid, String topictype, int operation, String timestamp) {
        getBySort(topicid,topictype,operation,timestamp,"0");
        return true;
    }
}
