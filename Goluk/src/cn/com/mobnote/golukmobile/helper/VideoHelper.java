package cn.com.mobnote.golukmobile.helper;

import com.loopj.android.http.*;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;

public class VideoHelper extends GolukHttpClientHelper {
	private String uri = "navidog4MeetTrans/video.htm";
	
	public VideoHelper(Context context, GolukApplication application) {
		super(context, application);
		super.setUri(uri);
	}
	
	/**
	 * 保存视频
	 * @param params 视频元数据
	 * @param responseHandler
	 */
	public void save(RequestParams params, AsyncHttpResponseHandler responseHandler) {
		super.post(params, responseHandler);
	}
}