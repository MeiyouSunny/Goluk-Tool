package com.mobnote.golukmain.videodetail;

import android.text.TextUtils;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import java.util.HashMap;

/**
 * Created by lily on 16-5-26.
 */
public class DeleteVideoRequest extends GolukFastjsonRequest<DeleteJson> {

    public DeleteVideoRequest(int requestType, IRequestResultListener listener) {
        super(requestType, DeleteJson.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/myHomePage.htm";
    }

    @Override
    protected String getMethod() {
        return "deleteMyVideo";
    }

    public boolean get(String videoid, String commuid) {
        if (TextUtils.isEmpty(videoid) || TextUtils.isEmpty(commuid)) {
            return false;
        }
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("videoid", videoid);
        headers.put("commuid", commuid);
        super.get();
        return true;
    }

}
