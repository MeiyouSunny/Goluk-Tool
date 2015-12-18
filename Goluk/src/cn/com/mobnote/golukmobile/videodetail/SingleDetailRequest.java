package cn.com.mobnote.golukmobile.videodetail;

import java.util.HashMap;



import android.text.TextUtils;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class SingleDetailRequest extends GolukFastjsonRequest<VideoJson> {

	public SingleDetailRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoJson.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "singleDetails";
	}

	public boolean get(String videoid) {
		if (TextUtils.isEmpty(videoid)) {
			return false;
		}
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", ""+100);
		headers.put("videoid", videoid);
		super.get();
		return true;
	}
}
