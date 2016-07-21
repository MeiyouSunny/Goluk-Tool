package com.mobnote.golukmain.watermark;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.watermark.bean.CarBrandsResultBean;

import java.util.HashMap;

/**
 * Created by pavkoo on 2016/7/20.
 */
public class CarBrandsRequest extends GolukFastjsonRequest<CarBrandsResultBean> {
    public CarBrandsRequest( IRequestResultListener listener) {
        super(0, CarBrandsResultBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcAdmin/autoBrandList.htm";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String xieyi, String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", xieyi);
        headers.put("uid", uid);
        get();
    }
}
