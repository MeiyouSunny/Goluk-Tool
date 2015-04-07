package cn.com.mobnote.util;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.carrecorder.settings.VideoQualityActivity;

public class JsonUtil {

	/**
	 * 组织IPC连接方式的json串
	 * 
	 * @param mode
	 *            连接方式，取值为IPCManagerFn类中的IPCMgrMode_IPCDirect/IPCMgrMode_Mobnote
	 * @return json串
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	public static String getIPCConnModeJson(int mode) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("mode", mode);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 组织当前连接wifi的状态json串
	 * 
	 * @param state
	 *            连接wifi状态 0/1　未连接/已连接
	 * @param ip
	 *            连接的IP地址
	 * @return JSON串
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	public static String getWifiChangeJson(int state, String ip) {
		try {
			if (null == ip) {
				ip = "";
			}
			JSONObject obj = new JSONObject();
			obj.put("state", state);
			obj.put("domain", ip);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 组织下载文件的json串
	 * 
	 * @param filename
	 *            文件名称
	 * @param tag
	 *            此文件的唯一标识
	 * @param savepath
	 *            文件的保存路径
	 * @return JSON串
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	public static String getDownFileJson(String filename, String tag, String savepath) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("filename", filename);
			obj.put("tag", tag);
			obj.put("savepath", savepath);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 组织更新经纬度信息json串
	 * @param lon　经度
	 * @param lat	纬度
	 * @param speed 速度
	 * @param direction 方向
	 * @return json
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	public static String getGPSJson(long lon, long lat, int speed, int direction){
		try {
			JSONObject obj = new JSONObject();
			obj.put("lon", lon);
			obj.put("lat", lat);
			obj.put("speed", speed);
			obj.put("direction", direction);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 组织设置IPC系统时间json串
	 * @param time 时间
	 * @return
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public static String getTimeJson(long time){
		try {
			JSONObject obj = new JSONObject();
			obj.put("time", time);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 组织IPC的音视频设置，主码流与子码流同时设置
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static String getVideoConfig(VideoQualityActivity.SensitivityType type){
		try {
			JSONArray array = new JSONArray();
			for(int i=0; i<1; i++){
				JSONObject obj = new JSONObject();
				if(0 == i){
					obj.put("bitstreams", i);
					obj.put("frameRate", 30);
					obj.put("AudioEnabled", 1);
					
					if(VideoQualityActivity.SensitivityType._1080h == type){
						obj.put("resolution", "1080p");
						obj.put("bitrate", 8192);
					}else if(VideoQualityActivity.SensitivityType._1080l == type){
						obj.put("resolution", "1080p");
						obj.put("bitrate", 5120);
					}else if(VideoQualityActivity.SensitivityType._720h == type){
						obj.put("resolution", "720p");
						obj.put("bitrate", 6144);
					}else{
						obj.put("resolution", "720p");
						obj.put("bitrate", 4096);
					}
				}else{
					
					
				}
				array.put(obj);
			}
			
			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获取视频配置
	 * @param type　0:主码流，1:子码流
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static String getVideoCfgJson(int type){
		try {
			JSONObject obj = new JSONObject();
			obj.put("bitstreams", type);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
