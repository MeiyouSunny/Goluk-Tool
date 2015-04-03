package cn.com.mobnote.util;

import org.json.JSONObject;

import cn.com.mobnote.golukmobile.live.LiveDataInfo;
import cn.com.mobnote.golukmobile.live.UserInfo;

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

	public static String getStartLiveJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("active", "1");
			obj.put("talk", "1");
			obj.put("tag", "android");

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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

	public static UserInfo parseSingleUserInfoJson(JSONObject rootObj) {
		try {
			String uid = rootObj.getString("uid");
			String aid = rootObj.getString("aid");
			String nikeName = rootObj.getString("nickname");
			String picUrl = rootObj.getString("picurl");
			String sex = rootObj.getString("sex");

			String lon = rootObj.getString("lon");
			String lat = rootObj.getString("lat");
			String speed = rootObj.getString("speed");

			String open = rootObj.getString("open");
			String active = rootObj.getString("active");
			String tag = rootObj.getString("tag");
			String groupid = rootObj.getString("gid");
			String person = rootObj.getString("persons");
			String zan = rootObj.getString("zan");

			UserInfo userInfo = new UserInfo();
			userInfo.aid = aid;
			userInfo.nickName = nikeName;
			userInfo.picurl = picUrl;
			userInfo.sex = sex;
			userInfo.lon = lon;
			userInfo.lat = lat;
			userInfo.speed = speed;
			userInfo.active = active;
			userInfo.tag = tag;
			userInfo.groupId = groupid;
			userInfo.persons = person;
			userInfo.zanCount = zan;

			return userInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public LiveDataInfo parseLiveDataJson(String data) {
		try {
			int code = 0;
			String groupId = null;
			String playUrl = null; // 直播地址
			String grouptype = "";
			int membercount = 0;
			String title = "";
			String groupnumber = "";
			int tag = 0;
			String joniGroup = null;

			JSONObject obj = new JSONObject(data);
			code = Integer.valueOf(obj.getString("code"));
			groupId = obj.getString("groupid");
			if (!obj.isNull("vurl")) {
				playUrl = obj.getString("vurl");
			}
			grouptype = obj.getString("grouptype");
			membercount = obj.getInt("membercount");
			title = obj.getString("title");
			groupnumber = obj.getString("groupnumber");

			LiveDataInfo info = new LiveDataInfo();
			info.code = code;
			info.groupId = groupId;
			info.groupnumber = groupnumber;
			info.groupType = grouptype;
			info.playUrl = playUrl;
			info.membercount = membercount;
			info.title = title;
			return info;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
