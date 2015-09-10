package cn.com.mobnote.golukmobile.helper;

import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;

public class VideoHelper extends GolukHttpClientHelper {
	private String service = "navidog4MeetTrans/video.htm";
	
	public VideoHelper(Context context, GolukApplication application) {
		super.setService(service);
	}
	
	/**
	 * 保存视频
	 * @param params 视频元数据
	 * @return
	 */
	public String save(LinkedList<BasicNameValuePair> params) {
		return super.post(params);
	}
}