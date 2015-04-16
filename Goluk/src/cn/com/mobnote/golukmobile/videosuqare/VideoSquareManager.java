package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;

public class VideoSquareManager implements VideoSuqareManagerFn{
	/** Application实例,用于调用JNI的对象 */
	private GolukApplication mApplication = null;
	/** IPC回调监听列表 */
	private HashMap<String, VideoSuqareManagerFn> mVideoSquareManagerListener = null;
	
	public VideoSquareManager(GolukApplication application){
		mApplication = application;
		mVideoSquareManagerListener = new HashMap<String, VideoSuqareManagerFn>();
		mApplication.mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Square, this);
	}
	
	/**
	 * 获取广场列表数据
	 * @return
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public boolean getSquareList(){
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", "1");
			obj.put("type", "0");
			obj.put("attribute", "1");
			obj.put("operation", "0");
			obj.put("timestamp", "");
			
			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		System.out.println("YYY=======11111=====json="+json);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_SquareList, json);
	}
	
	/**
	 * 获取热门列表
	 * @return
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public boolean getHotList(){
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", "1");
			obj.put("operation", "0");
			
			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		System.out.println("YYY=======11111=====json="+json);
		
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_HotList, json);
	}
	
	
	
	
	
	
	/**
	 * 添加视频广场监听
	 * @param from
	 * @param fn
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public void addVideoSquareManagerListener(String from, VideoSuqareManagerFn fn) {
		this.mVideoSquareManagerListener.put(from, fn);
	}
	
	/**
	 * 删除视频广场监听
	 * @param from
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public void removeVideoSquareManagerListener(String from){
		this.mVideoSquareManagerListener.remove(from);
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
//		System.out.println("YYY====getSquareList===33333=======msg="+msg+"===param2="+param2);
		Iterator<String> iter = mVideoSquareManagerListener.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			if (null != key) {
				VideoSuqareManagerFn fn = mVideoSquareManagerListener.get(key);
				if (null != fn) {
//					System.out.println("YYY====ddddddddddddddddd======");
					fn.VideoSuqare_CallBack(event, msg, param1, param2);
				}
			}
		}
		
	}

}
