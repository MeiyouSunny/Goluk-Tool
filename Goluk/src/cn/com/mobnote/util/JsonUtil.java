package cn.com.mobnote.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.settings.VideoQualityActivity;
import cn.com.mobnote.golukmobile.live.LiveDataInfo;
import cn.com.mobnote.golukmobile.live.LiveSettingBean;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.user.APPInfo;
import cn.com.mobnote.user.IPCInfo;
import cn.com.tiros.api.FileUtils;

public class JsonUtil {

	public static boolean getJsonBooleanValue(String jsonData, String key, boolean defaultValue) {
		try {
			return getJsonBooleanValue(new JSONObject(jsonData), key, defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static boolean getJsonBooleanValue(JSONObject json_Channel, String key, boolean defaultValue) {
		try {
			if (!json_Channel.has(key)) {
				return defaultValue;
			}
			if (json_Channel.isNull(key)) {
				return defaultValue;
			}
			return json_Channel.getBoolean(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static String getJsonStringValue(String jsonData, String key, String defaultValue) {
		try {
			return getJsonStringValue(new JSONObject(jsonData), key, defaultValue);
		} catch (Exception e) {

		}
		return defaultValue;
	}

	private static String getJsonStringValue(JSONObject json_Channel, String key, String defaultValue) {
		try {
			if (!json_Channel.has(key)) {
				return defaultValue;
			}
			if (json_Channel.isNull(key)) {
				return defaultValue;
			}
			return json_Channel.getString(key);
		} catch (Exception e) {

		}
		return defaultValue;
	}

	private static double getJsonDoubleValue(JSONObject json_Channel, String key, double defaultValue) {
		try {
			if (!json_Channel.has(key)) {
				return defaultValue;
			}
			if (json_Channel.isNull(key)) {
				return defaultValue;
			}
			return json_Channel.getDouble(key);
		} catch (Exception e) {

		}
		return defaultValue;
	}

	public static int getJsonIntValue(String message, String key, int defaultValue) {
		try {
			JSONObject obj = new JSONObject(message);
			return getJsonIntValue(obj, key, defaultValue);
		} catch (Exception e) {

		}

		return defaultValue;
	}

	private static int getJsonIntValue(JSONObject json_Channel, String key, int defaultValue) {
		try {
			if (!json_Channel.has(key)) {
				return defaultValue;
			}
			if (json_Channel.isNull(key)) {
				return defaultValue;
			}
			return json_Channel.getInt(key);
		} catch (Exception e) {

		}
		return defaultValue;
	}

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
	public static String getDownFileJson(String filename, String tag, String savepath, long filetime) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("filename", filename);
			obj.put("tag", tag);
			obj.put("savepath", savepath);
			obj.put("filetime", "" + filetime);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 组织更新经纬度信息json串
	 * 
	 * @param lon
	 *            　经度
	 * @param lat
	 *            纬度
	 * @param speed
	 *            速度
	 * @param direction
	 *            方向
	 * @return json
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	public static String getGPSJson(long lon, long lat, int speed, int direction) {
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
	 * 
	 * @param time
	 *            时间
	 * @return
	 * @author xuhw
	 * @date 2015年4月3日
	 */
	public static String getTimeJson(long time) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("time", time);

			return obj.toString();
		} catch (Exception e) {
		}
		return null;
	}

	// vid 为视频id
	public static String getStartLiveJson(String vid, LiveSettingBean beanData) {
		try {

			String desc = "";

			if (null != beanData && null != beanData.desc) {
				desc = URLEncoder.encode(beanData.desc, "utf-8");
			}

			String duration = null != beanData ? "" + beanData.duration : "3600";
			String netCountStr = null != beanData ? "" + beanData.netCountStr : "";
			String vtypStr = null != beanData ? "" + beanData.vtype : "";
			String talk = "0";
			if (beanData != null) {
				talk = beanData.isCanTalk ? "1" : "0";
			}

			String voice = beanData.isCanVoice ? "1" : "0";

			JSONObject obj = new JSONObject();
			obj.put("active", "1");
			obj.put("talk", talk);
			obj.put("tag", "android");
			obj.put("vid", vid);
			obj.put("desc", desc);
			obj.put("restime", duration);
			obj.put("flux", netCountStr);
			obj.put("vtype", "" + vtypStr);
			obj.put("voice", voice);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getStopLiveJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("tag", "android");
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String getStartLookLiveJson(String uid, String aid) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("uid", uid);
			obj.put("aid", aid);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getJoinGroup(String grouptype, int membercount, String title, String groupid,
			String groupnumber) {
		try {
			JSONObject json = new JSONObject();
			json.put("grouptype", grouptype);
			json.put("membercount", membercount);
			json.put("title", title);
			json.put("groupid", groupid);
			json.put("groupnumber", groupnumber);
			json.put("tag", 0);

			return json.toString();
		} catch (Exception e) {

		}

		return null;
	}

	public static String UserInfoToString(UserInfo userInfo) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("uid", userInfo.uid);
			obj.put("aid", userInfo.aid);
			obj.put("nickname", userInfo.nickName);
			obj.put("active", "" + userInfo.active);
			obj.put("tag", userInfo.tag);
			obj.put("persons", "" + userInfo.persons);
			obj.put("lon", userInfo.lon);
			obj.put("lat", userInfo.lat);
			obj.put("open", "1");
			obj.put("speed", userInfo.speed);
			obj.put("desc", "");
			obj.put("talk", "1");
			obj.put("zan", userInfo.zanCount);
			obj.put("sex", userInfo.sex);
			obj.put("head", userInfo.head);

			return obj.toString();
		} catch (Exception e) {

		}

		return null;
	}

	public static UserInfo parseSingleUserInfoJson(JSONObject rootObj) {
		try {
			UserInfo userInfo = new UserInfo();
			userInfo.uid = getJsonStringValue(rootObj, "uid", "");
			userInfo.aid = getJsonStringValue(rootObj, "aid", "");
			userInfo.nickName = getJsonStringValue(rootObj, "nickname", "");
			userInfo.picurl = getJsonStringValue(rootObj, "picurl", "");
			userInfo.sex = getJsonStringValue(rootObj, "sex", "");
			userInfo.lon = getJsonStringValue(rootObj, "lon", "");
			userInfo.lat = getJsonStringValue(rootObj, "lat", "");
			userInfo.speed = String.valueOf(getJsonIntValue(rootObj, "speed", 0));
			userInfo.active = getJsonStringValue(rootObj, "active", "");

			userInfo.tag = getJsonStringValue(rootObj, "tag", "");
			userInfo.groupId = getJsonStringValue(rootObj, "gid", "");
			userInfo.persons = String.valueOf(getJsonIntValue(rootObj, "persons", 0));
			userInfo.zanCount = getJsonStringValue(rootObj, "zan", "0");
			userInfo.liveDuration = Integer.valueOf(getJsonStringValue(rootObj, "restime", "60"));
			userInfo.desc = getJsonStringValue(rootObj, "desc", "");
			userInfo.head = getJsonStringValue(rootObj, "head", "7");

			return userInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static LiveDataInfo parseLiveDataJson2(String data) {
		try {
			int code = 0;
			String groupId = null;
			String grouptype = null;
			int membercount = 0;
			String title = null;
			String groupnumber = null;

			JSONObject obj = new JSONObject(data);
			code = Integer.valueOf(obj.getString("code"));

			groupId = obj.getString("groupid");
			groupnumber = obj.getString("groupnumber");
			grouptype = obj.getString("grouptype");
			membercount = obj.getInt("membercount");
			title = obj.getString("title");

			LiveDataInfo info = new LiveDataInfo();
			info.code = code;
			info.groupId = groupId;
			info.groupnumber = groupnumber;
			info.groupType = grouptype;
			info.membercount = membercount;
			info.title = title;
			return info;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getLiveOKJson(String zid) {
		try {
			JSONObject json = new JSONObject();
			json.put("zid", zid);
			json.put("tag", 0);
			return json.toString();
		} catch (Exception e) {

		}

		return "";
	}

	public static LiveDataInfo parseLiveDataJson(String data) {
		try {
			JSONObject obj = new JSONObject(data);

			LiveDataInfo info = new LiveDataInfo();
			info.code = Integer.valueOf(getJsonStringValue(obj, "code", "0"));
			info.active = Integer.valueOf(getJsonStringValue(obj, "active", "1"));
			info.groupId = getJsonStringValue(obj, "groupid", "");
			info.groupnumber = getJsonStringValue(obj, "groupnumber", "");
			info.groupType = getJsonStringValue(obj, "grouptype", "");
			info.playUrl = getJsonStringValue(obj, "vurl", "");

			info.membercount = getJsonIntValue(obj, "membercount", 0);
			info.title = getJsonStringValue(obj, "title", "");

			info.vid = getJsonStringValue(obj, "vid", "");
			String restime = getJsonStringValue(obj, "restime", "0");
			if (null == restime || "".equals(restime)) {
				restime = "0";
			}
			info.restTime = Integer.valueOf(restime);
			info.desc = getJsonStringValue(obj, "desc", "");
			info.voice = getJsonStringValue(obj, "voice", "1");

			return info;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 组织IPC的音视频设置，主码流与子码流同时设置
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static String getVideoConfig(VideoQualityActivity.SensitivityType type) {
		try {
			JSONArray array = new JSONArray();
			for (int i = 0; i < 1; i++) {
				JSONObject obj = new JSONObject();
				if (0 == i) {
					obj.put("bitstreams", i);
					obj.put("frameRate", 30);
					obj.put("AudioEnabled", 1);

					if (VideoQualityActivity.SensitivityType._1080h == type) {
						obj.put("resolution", "1080P");
						obj.put("bitrate", 8192);
					} else if (VideoQualityActivity.SensitivityType._1080l == type) {
						obj.put("resolution", "1080P");
						obj.put("bitrate", 5120);
					} else if (VideoQualityActivity.SensitivityType._720h == type) {
						obj.put("resolution", "720P");
						obj.put("bitrate", 6144);
					} else {
						obj.put("resolution", "720P");
						obj.put("bitrate", 4096);
					}
				} else {

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
	 * 组织IPC的音视频设置，主码流与子码流同时设置
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static String getVideoConfig(VideoConfigState mVideoConfigState) {
		try {
			JSONArray array = new JSONArray();
			JSONObject obj = new JSONObject();
			obj.put("bitstreams", mVideoConfigState.bitstreams);
			obj.put("frameRate", mVideoConfigState.frameRate);
			obj.put("audioEnabled", mVideoConfigState.AudioEnabled);
			obj.put("resolution", mVideoConfigState.resolution);
			obj.put("bitrate", mVideoConfigState.bitrate);

			array.put(obj);

			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static BaiduPosition parseLocatoinJson(String jsonData) {
		if (null == jsonData || "".equals(jsonData) || 0 >= jsonData.length()) {
			return null;
		}

		try {
			BaiduPosition positon = new BaiduPosition();
			JSONObject rootObj = new JSONObject(jsonData);
			positon.elon = getJsonDoubleValue(rootObj, "elon", 0.0);
			positon.elat = getJsonDoubleValue(rootObj, "elat", 0.0);
			positon.rawLon = getJsonDoubleValue(rootObj, "rawLon", 0.0);
			positon.rawLat = getJsonDoubleValue(rootObj, "rawLat", 0.0);

			positon.speed = getJsonDoubleValue(rootObj, "speed", 0.0);
			positon.course = getJsonDoubleValue(rootObj, "course", 0.0);
			positon.altitude = getJsonDoubleValue(rootObj, "altitude", 0.0);
			positon.radius = getJsonDoubleValue(rootObj, "radius", 0.0);
			positon.accuracy = getJsonDoubleValue(rootObj, "accuracy", 0.0);
			// positon.locationType =
			// Integer.parseInt(getJsonStringValue(rootObj, "locationType",
			// "0"));

			return positon;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 获取视频配置
	 * 
	 * @param type
	 *            　0:主码流，1:子码流
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static String getVideoCfgJson(int type) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("bitstreams", type);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// 获取图片上传的json串
	public static String getUploadSnapJson(String vid, String imgPath) {
		try {
			JSONObject rootObj = new JSONObject();
			rootObj.put("vid", vid);
			rootObj.put("imgpath", imgPath);

			return rootObj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getCancelJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("cancel", 1);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 升级传服务器的参数
	 * 
	 * @param ipcVersion
	 * @return
	 */
	public static String putIPC(String appVersion, String ipcVersion) {
		try {
			// {“AppVersionFilePath”:”fs6:/version”, “IpcVersion”:”1.2.3.4”}
			JSONObject obj = new JSONObject();
			obj.put("AppVersionFilePath", appVersion);
			obj.put("IpcVersion", ipcVersion);
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ipc升级下载文件
	 * 
	 * @param url
	 * @param savePath
	 * @return
	 */
	public static String ipcDownLoad(String url, String savePath) {
		// {“url”:”http://www.baidu.com”,
		// “savePath”:”fs1:/update/ipc_upgrade_2015-04-30-15-58.bin”}
		try {
			JSONObject obj = new JSONObject();
			obj.put("url", url);
			obj.put("savePath", savePath);
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSingleIPCInfoJson(IPCInfo ipcInfo) {
		if (null == ipcInfo) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", ipcInfo.version);
			obj.put("path", ipcInfo.path);
			obj.put("url", ipcInfo.url);
			obj.put("md5", ipcInfo.md5);
			obj.put("filesize", ipcInfo.filesize);
			obj.put("releasetime", ipcInfo.releasetime);
			obj.put("appcontent", ipcInfo.appcontent);
			obj.put("isnew", ipcInfo.isnew);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static IPCInfo getSingleIPCInfo(String infoStr) {
		try {
			IPCInfo upgradeInfo = new IPCInfo();
			JSONObject json = new JSONObject(infoStr);
			upgradeInfo.version = getJsonStringValue(json, "version", "");
			upgradeInfo.path = getJsonStringValue(json, "path", "");
			upgradeInfo.url = getJsonStringValue(json, "url", "");
			upgradeInfo.md5 = getJsonStringValue(json, "md5", "");
			upgradeInfo.filesize = getJsonStringValue(json, "filesize", "");
			upgradeInfo.releasetime = getJsonStringValue(json, "releasetime", "");
			upgradeInfo.appcontent = getJsonStringValue(json, "appcontent", "");
			upgradeInfo.isnew = getJsonStringValue(json, "isnew", "");

			return upgradeInfo;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 解析ipc返回数据
	 * 
	 * @param jsonData
	 * @return
	 */
	public static IPCInfo[] upgradeJson(JSONArray jsonData) {
		if (null == jsonData || "".equals(jsonData) || 0 >= jsonData.length()) {
			return null;
		}
		try {
			final int length = jsonData.length();
			IPCInfo[] infoArray = new IPCInfo[length];
			for (int i = 0; i < length; i++) {
				IPCInfo upgradeInfo = new IPCInfo();
				JSONObject json = jsonData.getJSONObject(i);
				upgradeInfo.version = getJsonStringValue(json, "version", "");
				upgradeInfo.path = getJsonStringValue(json, "path", "");
				upgradeInfo.url = getJsonStringValue(json, "url", "");
				upgradeInfo.md5 = getJsonStringValue(json, "md5", "");
				upgradeInfo.filesize = getJsonStringValue(json, "filesize", "");
				upgradeInfo.releasetime = getJsonStringValue(json, "releasetime", "");
				upgradeInfo.appcontent = getJsonStringValue(json, "appcontent", "");
				upgradeInfo.isnew = getJsonStringValue(json, "isnew", "");
				infoArray[i] = upgradeInfo;
			}
			return infoArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * APP升级
	 * 
	 * @param rootObj
	 * @return
	 */
	public static APPInfo appUpgradeJson(JSONObject jsonObject) {
		try {
			APPInfo appinfo = new APPInfo();
			appinfo.appcontent = getJsonStringValue(jsonObject, "appcontent", "");
			appinfo.filesize = getJsonStringValue(jsonObject, "filesize", "");
			appinfo.isupdate = getJsonStringValue(jsonObject, "isupdate", "");
			appinfo.md5 = getJsonStringValue(jsonObject, "md5", "");
			appinfo.path = getJsonStringValue(jsonObject, "path", "");
			appinfo.releasetime = getJsonStringValue(jsonObject, "releasetime", "");
			appinfo.url = getJsonStringValue(jsonObject, "url", "");
			appinfo.version = getJsonStringValue(jsonObject, "version", "");
			appinfo.ipcVersion = getJsonStringValue(jsonObject, "ipcversion", "");

			return appinfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getIPcJson(String ipc_ssid, String ipc_pwd, String mobile_ssid, String mobile_pwd, String ip,
			String way, boolean isHasPassword) {
		try {
			JSONObject obj = new JSONObject();
			if (isHasPassword) {
				obj.put("AP_SSID", ipc_ssid);
				obj.put("AP_PWD", ipc_pwd);
			}
			obj.put("GolukSSID", mobile_ssid);
			obj.put("GolukPWD", mobile_pwd);

			obj.put("GolukIP", ip);
			obj.put("GolukGateway", way);

			return obj.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static String createShareType(String type) {
		JSONArray array = new JSONArray();
		array.put(type);
		return array.toString();
	}

	// videoId: 视频ID
	// type: 视频类型 1/2 精彩/紧急
	// attribute: 用户选择的视频类型(包括曝光，看天下,是一个json数組)
	// desc 视频描述 (用户输入或选择 (比如：有人扔东西，注意素质))
	// issquare 是否分享到视频广场 0/1 (否/是)
	// thumbImgJavaPath: 缩略图路径
	public static String createShareJson(String videoId, String type, String attribute, String desc, String issquare,
			String thumbImgJavaPath) {

		String json = null;
		try {
			final String fsFile = FileUtils.javaToLibPath(thumbImgJavaPath);
			String videoDes = "";
			String attriDefault = "";
			try {
				videoDes = URLEncoder.encode(desc, "UTF-8");
				attriDefault = URLEncoder.encode(attribute, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			JSONObject obj = new JSONObject();
			obj.put("videoid", videoId);
			obj.put("describe", videoDes);
			obj.put("attribute", attriDefault);
			// 是否分享到视频广场 0/1 否/是
			obj.put("issquare", issquare);
			// 缩略图路径
			obj.put("imgpath", fsFile);
			// type: 1/2 精彩视频 / 紧急视频
			obj.put("type", "1");
			json = obj.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

}
