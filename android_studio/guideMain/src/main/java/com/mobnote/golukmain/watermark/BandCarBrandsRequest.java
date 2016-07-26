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

    public void post(String protocolType, String brandId, String code, String storeName, String commUId,String ssid) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.clear();
        headers.put("xieyi", protocolType);
        headers.put("commuid", commUId);
        headers.put("brandid", brandId);
        headers.put("code", code);
        headers.put("storename", storeName);
        headers.put("ssid",ssid);
        super.post();
    }

    public void postCache() {
        super.post();
    }

    public void saveCacheRequest() {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        SharedPrefUtil.saveBandCarRequest(
                headers.get("xieyi"),
                headers.get("commuid"),
                headers.get("code"),
                headers.get("brandid"),
                headers.get("storename"),
                headers.get("ssid"));
    }

    /**
     * @return 是否有缓存纪录，需要连网重新提交到服务器
     */
    public boolean resotreCacheRequest() {
        HashMap<String, String> cacheHeader = SharedPrefUtil.getBandCarRequest();
        if (cacheHeader == null) {
            return false;
        }
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.clear();
        headers.putAll(cacheHeader);
        return true;
    }
}
