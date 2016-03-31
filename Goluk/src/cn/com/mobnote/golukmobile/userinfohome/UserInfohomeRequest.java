package cn.com.mobnote.golukmobile.userinfohome;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.userinfohome.bean.UserinfohomeRetBean;

public class UserInfohomeRequest extends GolukFastjsonRequest<UserinfohomeRetBean> {
	public UserInfohomeRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UserinfohomeRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {
		return "userInfoHome";
	}
	
	public void get (String xieyi,String uid,String otheruid){
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("uid", uid);
		headers.put("otheruid", otheruid);
		get();
	}
}

