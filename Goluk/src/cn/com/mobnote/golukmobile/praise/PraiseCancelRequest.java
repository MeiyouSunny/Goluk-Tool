package cn.com.mobnote.golukmobile.praise;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.praise.bean.PraiseCancelResultBean;
import cn.com.tiros.api.Tapi;

public class PraiseCancelRequest extends GolukFastjsonRequest<PraiseCancelResultBean> {

	public PraiseCancelRequest(int requestType, IRequestResultListener listener) {
		super(requestType, PraiseCancelResultBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/shareVideo.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "cancelpraise";
	}

	public boolean get(String channel, String videoId) {
		if (TextUtils.isEmpty("channel")) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("channel", channel);
		paramters.put("videoid", videoId);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			paramters.put("commuid", "");
		} else {
			paramters.put("commuid", uid);
		}
		paramters.put("commmid", "" + Tapi.getMobileId());
		super.get();
		return true;
	}
}
