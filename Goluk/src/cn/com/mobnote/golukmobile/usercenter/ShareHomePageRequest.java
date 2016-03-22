package cn.com.mobnote.golukmobile.usercenter;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.usercenter.bean.ShareJson;

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
