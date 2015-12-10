package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.videosuqare.bean.ShareVideoResultBean;

public class ShareVideoShortUrlRequest extends GolukFastjsonRequest<ShareVideoResultBean> {

	public ShareVideoShortUrlRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ShareVideoResultBean.class, listener);
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
		return "shareVideoShortUrl";
	}

	public boolean get(String videoId,String type) {
		if (TextUtils.isEmpty(videoId) || TextUtils.isEmpty(type)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("videoid", videoId);
		paramters.put("type", type);
		super.get();
		return true;
	}
}
