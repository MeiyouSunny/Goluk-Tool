package cn.com.mobnote.user;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoSquareDeatilActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoSquareDetailManage {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;

	public VideoSquareDetailManage(GolukApplication mApp) {
		this.mApp = mApp;
	}

	/**
	 * 视频详情
	 * @param success
	 * @param outTime
	 * @param obj
	 */
	public void requestDetailCallback(int success, Object outTime, Object obj) {
		GolukDebugUtils.i(TAG, "----------requestDetailCallback--------success----" + success + "-----outTime----"
				+ outTime + "----obj--" + obj);
		int codeOut = (Integer) outTime;
		if (1 == success) {
			try{
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i(TAG, "---requestDetailCallback---"+json);
				String data = json.optString("data");
				JSONObject dataJson = new JSONObject(data);
				String aVideo = dataJson.optString("avideo");
				JSONObject avideoJson = new JSONObject(aVideo);
				String video = avideoJson.optString("video");
				String user = avideoJson.optString("user");
				
			}catch(Exception e){
				e.printStackTrace();
			}
		} else {
			switch (codeOut) {
			case 1:// 没有网络
			case 2:// 服务端错误
			case 3:// 网络链接超时
			default:
				GolukUtils.showToast(mApp.getContext(), mApp.getContext().getResources().getString(R.string.user_netword_outtime));
				break;
			}
		}
	}
	
}
