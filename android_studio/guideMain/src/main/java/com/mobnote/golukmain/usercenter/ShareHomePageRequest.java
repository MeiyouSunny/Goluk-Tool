package com.mobnote.golukmain.usercenter;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.usercenter.bean.ShareJson;

public class ShareHomePageRequest extends GolukFastjsonRequest<ShareJson> {

	public ShareHomePageRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ShareJson.class, listener);
	}

	@Override
	protected String getPath() {

		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {

		return "shareMyHomePage";
	}

	public void get(String uid, String otheruid) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("uid", uid);
		headers.put("otheruid", otheruid);
		get();
	}

}
