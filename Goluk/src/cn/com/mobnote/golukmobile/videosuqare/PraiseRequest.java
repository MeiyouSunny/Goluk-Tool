package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.videosuqare.bean.PraiseResultBean;
import cn.com.tiros.api.Tapi;

public class PraiseRequest extends GolukFastjsonRequest<PraiseResultBean> {

	public PraiseRequest(int requestType, IRequestResultListener listener) {
		super(requestType, PraiseResultBean.class, listener);
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
		return "shareVideoPraise";
	}

	public boolean get(String channel, String videoId, String type) {
		if (TextUtils.isEmpty("channel") || TextUtils.isEmpty(videoId) || TextUtils.isEmpty(type)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("channel", channel);
		paramters.put("videoid", videoId);
		paramters.put("type", type);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			paramters.put("uid", "");
		} else {
			paramters.put("uid", uid);
		}
		paramters.put("mobileid", "" + Tapi.getMobileId());
		super.get();
		return true;
	}
}
