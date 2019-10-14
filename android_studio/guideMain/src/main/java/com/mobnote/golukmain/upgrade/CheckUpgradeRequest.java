package com.mobnote.golukmain.upgrade;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.upgrade.bean.UpgradeResultbean;

import java.util.Map;

/**
 * Created by pavkoo on 2016/7/18.
 */
public class CheckUpgradeRequest extends GolukFastjsonRequest<UpgradeResultbean> {
    public CheckUpgradeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UpgradeResultbean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcAdmin/upgradeGoluk.htm";
    }

    @Override
    protected String getMethod() {
        return "upgradeIPC";
    }

    public void get(String vapp, String vipc) {
        Map<String, String> headerParams = getHeader();
        headerParams.put("vapp", vapp);
        headerParams.put("vipc", vipc);
        headerParams.put("commhdtype", "T2S");
        get();
    }
}
