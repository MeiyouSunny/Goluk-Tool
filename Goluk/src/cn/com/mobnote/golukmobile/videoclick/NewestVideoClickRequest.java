package cn.com.mobnote.golukmobile.videoclick;

import java.util.HashMap;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.tiros.api.Tapi;

public class NewestVideoClickRequest extends GolukFastjsonRequest<VideoClickInfo> {

	public NewestVideoClickRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoClickInfo.class, listener);
	}

	@Override
	protected String getPath() {

		return "/navidog4MeetTrans/shareVideo.htm";
	}

	@Override
	protected String getMethod() {

		return "shareVideoClick";
	}

	public void get(String xieyi, String channel, String videolist) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("channel", channel);
		headers.put("uid", GolukApplication.getInstance().mCurrentUId);
		headers.put("mobileid",  Tapi.getMobileId());
		headers.put("videolist", videolist);
		get();
	}

}
