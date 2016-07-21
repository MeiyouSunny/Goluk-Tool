package com.mobnote.golukmain.watermark;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.watermark.bean.BandCarBrandResultBean;
import com.mobnote.util.SharedPrefUtil;

import java.util.HashMap;

/**
 * Created by pavkoo on 2016/7/21.
 */
public class BandCarBrandsRequest extends GolukFastjsonRequest<BandCarBrandResultBean> {
    public BandCarBrandsRequest(IRequestResultListener listener) {
        super(0, BandCarBrandResultBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcAdmin/bindAutoBrand.htm";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String protocolType, String brandId, String code, String storeName, String commUId) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.clear();
        headers.put("xieyi", protocolType);
        headers.put("commuid", commUId);
        headers.put("brandid", brandId);
        headers.put("code", code);
        headers.put("storename", storeName);
        get();
    }

    public void getCache() {
        get();
    }

    public void saveCacheRequest() {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        SharedPrefUtil.saveBandCarRequest(
                headers.get("xieyi"),
                headers.get("commuid"),
                headers.get("code"),
                headers.get("brandid"),
                headers.get("storename"));
    }

    /**
     * @return 是否有缓存纪录，需要连网重新提交到服务器
     */
    public boolean resotreCacheRequest() {
        HashMap<String, String> cacheHeader = SharedPrefUtil.getBandCarRequest();
        if (cacheHeader == null) {
            return false;
        }
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.clear();
        headers.putAll(cacheHeader);
        return true;
    }
}
