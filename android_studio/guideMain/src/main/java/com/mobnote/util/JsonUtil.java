package com.mobnote.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.settings.VideoQualityActivity;
import com.mobnote.golukmain.cluster.bean.UserLabelBean;
import com.mobnote.golukmain.comment.CommentBean;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.live.LiveDataInfo;
import com.mobnote.golukmain.live.LiveSettingBean;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.videosuqare.ShareDataBean;
import com.mobnote.golukmain.xdpush.SettingBean;
import com.mobnote.golukmain.xdpush.XingGeMsgBean;
import com.mobnote.user.APPInfo;
import com.mobnote.user.IPCInfo;

import cn.com.mobnote.module.location.GolukPosition;
import cn.com.tiros.api.CSLog;
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

    public static int getJsonIntValue(JSONObject json_Channel, String key, int defaultValue) {
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
     * @param mode 连接方式，取值为IPCManagerFn类中的IPCMgrMode_IPCDirect/IPCMgrMode_Mobnote
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
     * @param state 连接wifi状态 0/1　未连接/已连接
     * @param ip    连接的IP地址
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
     * @param filename 文件名称
     * @param tag      此文件的唯一标识
     * @param savepath 文件的保存路径
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
     * @param lon       　经度
     * @param lat       纬度
     * @param speed     速度
     * @param direction 方向
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
     * @param time 时间
     * @return
     * @author xuhw
     * @date 2015年4月3日
     */
    public static String getTimeJson(long time, String zone) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("time", time);
            if (zone != null && !"".equals(zone)) {
                obj.put("zone", zone);
            }

            return obj.toString();
        } catch (Exception e) {
        }
        return null;
    }


    /**
     *  T3 国际版组织设置IPC系统时间json串
     *
     * @param time 时间
     * @return
     * @author xuhw
     * @date 2015年4月3日
     */
    public static String getTimeAndZoneJson(long time, String zone,int offsetHours,int offsetMins) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("time", time);
            if (zone != null && !"".equals(zone)) {
                obj.put("zone", zone);
            }
            obj.put("zone_offset_hours",offsetHours);
            obj.put("zone_offset_minutes",offsetMins);
            return obj.toString();
        } catch (Exception e) {
        }
        return null;
    }

    public static String getGpsTimeJson(int state) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("gpstimesync", state);

            return obj.toString();
        } catch (Exception e) {
        }
        return "";
    }

    public static int parseGpsTimeState(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.optInt("gpstimesync");
        } catch (Exception e) {
            return 0;
        }
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

            String voice = beanData.isEnableVoice ? "1" : "0";

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
            obj.put("nickname", userInfo.nickname);
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
            userInfo.nickname = getJsonStringValue(rootObj, "nickname", "");
            userInfo.picurl = getJsonStringValue(rootObj, "picurl", "");
            userInfo.sex = getJsonStringValue(rootObj, "sex", "");
            userInfo.lon = getJsonStringValue(rootObj, "lon", "");
            userInfo.lat = getJsonStringValue(rootObj, "lat", "");
            userInfo.speed = String.valueOf(getJsonIntValue(rootObj, "speed", 0));
            userInfo.active = getJsonStringValue(rootObj, "active", "");
            userInfo.phone = getJsonStringValue(rootObj, "phone", "");

            userInfo.tag = getJsonStringValue(rootObj, "tag", "");
            userInfo.groupId = getJsonStringValue(rootObj, "gid", "");
            userInfo.persons = String.valueOf(getJsonIntValue(rootObj, "persons", 0));
            userInfo.zanCount = getJsonStringValue(rootObj, "zan", "0");
            userInfo.liveDuration = Integer.valueOf(getJsonStringValue(rootObj, "restime", "60"));
            userInfo.desc = getJsonStringValue(rootObj, "desc", "");
            userInfo.head = getJsonStringValue(rootObj, "head", "7");
            userInfo.customavatar = getJsonStringValue(rootObj, "customavatar", "");
            userInfo.sharevideonumber = getJsonIntValue(rootObj, "sharevideonumber", 0);
            userInfo.praisemenumber = getJsonIntValue(rootObj, "praisemenumber", 0);
            userInfo.followingnumber = getJsonIntValue(rootObj, "followingnumber", 0);
            userInfo.fansnumber = getJsonIntValue(rootObj, "fansnumber", 0);
            userInfo.newfansnumber = getJsonIntValue(rootObj, "newfansnumber", 0);
            userInfo.link = getJsonIntValue(rootObj, "link", 0);
            // 解析 用户标签
            if (rootObj.has("label")) {
                JSONObject labelObj = rootObj.getJSONObject("label");
                UserLabelBean label = new UserLabelBean();
                label.approve = getJsonStringValue(labelObj, "approve", "");
                label.approvelabel = getJsonStringValue(labelObj, "approvelabel", "0");
                label.tarento = getJsonStringValue(labelObj, "tarento", "0");
                label.headplusv = getJsonStringValue(labelObj, "headplusv", "0");
                label.headplusvdes = getJsonStringValue(labelObj, "headplusvdes", "");
                userInfo.mUserLabel = label;
            }

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
            info.vurl = getJsonStringValue(obj, "vurl", "");

            info.membercount = getJsonIntValue(obj, "membercount", 0);
            info.title = getJsonStringValue(obj, "title", "");

            info.vid = getJsonStringValue(obj, "vid", "");
            String restime = getJsonStringValue(obj, "restime", "0");
            if (null == restime || "".equals(restime)) {
                restime = "0";
            }
            info.restime = Integer.valueOf(restime);
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

    public static String getVideoConfigJson_T1(int state) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("AudioEnable", state);

            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static GolukPosition parseLocatoinJson(String jsonData) {
        if (null == jsonData || "".equals(jsonData) || 0 >= jsonData.length()) {
            return null;
        }

        try {
            GolukPosition positon = new GolukPosition();
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
     * @param type 　0:主码流，1:子码流
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
     * @param appVersion appVersion存储路径
     * @param ipcVersion
     * @param ipcModel   ipc设备型号
     * @return
     */
    public static String putIPC(String appVersion, String ipcVersion, String ipcModel) {
        try {
            // {“AppVersionFilePath”:”fs6:/version”, “IpcVersion”:”1.2.3.4”}
            JSONObject obj = new JSONObject();
            obj.put("AppVersionFilePath", appVersion);
            obj.put("IpcVersion", ipcVersion);
            obj.put("IpcModel", ipcModel);
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
    public static String ipcDownLoad(String url, String ipcVersion, String ipcModel) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("URL", url);
            obj.put("IPCVersion", ipcVersion);
            obj.put("IPCModel", ipcModel);
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
                                         String thumbImgJavaPath, String createTime, String location, String channelid, String activityid,
                                         String activityname) {

        String json = null;
        try {
            final String fsFile = FileUtils.javaToLibPath(thumbImgJavaPath);
            String videoDes = "";
            String attriDefault = "";
            try {
                videoDes = URLEncoder.encode(desc, "UTF-8");
                attriDefault = URLEncoder.encode(attribute, "UTF-8");
                location = URLEncoder.encode(location, "UTF-8");
                activityname = URLEncoder.encode(activityname, "UTF-8");
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
            obj.put("creattime", createTime);
            obj.put("location", location);
            obj.put("channelid", channelid);
            obj.put("activityid", activityid);
            obj.put("activityname", activityname);
            json = obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    public static String registAndRepwdJson(String phoneNumber, String password, String vCode) {
        try {
            // {PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
            JSONObject obj = new JSONObject();
            obj.put("PNumber", phoneNumber);
            obj.put("Password", password);
            obj.put("VCode", vCode);
            obj.put("tag", "android");
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String registAndRepwdJson(String phoneNumber, String password, String vCode, String zone) {
        try {
            // {PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
            JSONObject obj = new JSONObject();
            obj.put("PNumber", phoneNumber);
            obj.put("Password", password);
            obj.put("VCode", vCode);
            obj.put("tag", "android");
            obj.put("dialingcode", zone);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 意见反馈请求服务器json串
     *
     * @param tag
     * @param sys_version
     * @param app_version
     * @param ipc_version
     * @param phone_models
     * @param opinion
     * @param contact
     * @return
     */
    public static String putOpinion(String tag, String sys_version, String app_version, String ipc_version,
                                    String phone_models, String opinion, String contact, String selectType) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("tag", tag);
            obj.put("system_version", sys_version);
            obj.put("app_version", app_version);
            obj.put("ipc_version", ipc_version);
            obj.put("phone_models", phone_models);
            obj.put("opinion", opinion);
            obj.put("contact", contact);
            obj.put("type", selectType);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCommentRequestStr(String id, String type, int operation, String timestamp, int pagesize,
                                              String ztid) {
        try {
            JSONObject json = new JSONObject();
            json.put("topicid", id);
            json.put("topictype", "" + type);
            json.put("operation", "" + operation);
            if (!timestamp.equals("")) {
                // timestamp = GolukUtils.formatTime(timestamp);
                timestamp = timestamp.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
            }
            json.put("timestamp", timestamp);
            json.put("pagesize", "" + pagesize);
            json.put("ztid", ztid);

            return json.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCommentRequestStr(String id, String type, int operation, String timestamp, int pagesize) {
        try {
            JSONObject json = new JSONObject();
            json.put("topicid", id);
            json.put("topictype", "" + type);
            json.put("operation", "" + operation);
            if (!timestamp.equals("")) {
                // timestamp = GolukUtils.formatTime(timestamp);
                timestamp = timestamp.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
            }
            json.put("timestamp", timestamp);
            json.put("pagesize", "" + pagesize);

            return json.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getAddCommentJson(String id, String type, String txt, String replyId, String replyName,
                                           String ztid) {
        try {
            JSONObject json = new JSONObject();
            json.put("topicid", id);
            json.put("topictype", type);
            txt = URLEncoder.encode(txt, "utf-8");
            json.put("text", txt);
            json.put("replyid", replyId);
            replyName = URLEncoder.encode(replyName, "utf-8");
            json.put("replyname", replyName);
            if (null != ztid) {
                json.put("ztid", ztid);
            }

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getDelCommentJson(String id) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);

            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static ArrayList<CommentBean> parseCommentData(JSONArray array) {
        if (null == array) {
            return null;
        }
        try {
            final int length = array.length();
            ArrayList<CommentBean> list = new ArrayList<CommentBean>();
            for (int i = 0; i < length; i++) {
                CommentBean temp = new CommentBean();
                final JSONObject obj = (JSONObject) array.get(i);
                temp.mCommentId = getJsonStringValue(obj, "commentId", "");
                temp.mCommentTime = getJsonStringValue(obj, "time", "");
                temp.mCommentTxt = getJsonStringValue(obj, "text", "");
                temp.mSeq = getJsonStringValue(obj, "seq", "");

                JSONObject replyObj = obj.getJSONObject("reply");
                temp.mReplyId = getJsonStringValue(replyObj, "id", "");
                temp.mReplyName = getJsonStringValue(replyObj, "name", "");

                JSONObject authorObj = obj.getJSONObject("author");
                temp.mUserId = getJsonStringValue(authorObj, "id", "");
                temp.mUserName = getJsonStringValue(authorObj, "name", "");
                temp.mUserHead = getJsonStringValue(authorObj, "avatar", "");
                temp.customavatar = getJsonStringValue(authorObj, "customavatar", "");

                if (authorObj.has("label")) {
                    JSONObject labelObj = authorObj.getJSONObject("label");
                    temp.mApprove = getJsonStringValue(labelObj, "approve", "");
                    temp.mApprovelabel = getJsonStringValue(labelObj, "approvelabel", "");
                    temp.mTarento = getJsonStringValue(labelObj, "tarento", "");
                    temp.mHeadplusv = getJsonStringValue(labelObj, "headplusv", "");
                    temp.mHeadplusvdes = getJsonStringValue(labelObj, "headplusvdes", "");
                }

                list.add(temp);
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CommentBean parseAddCommentData(JSONObject dataObj) {
        if (null == dataObj) {
            return null;
        }
        try {
            // JSONObject obj = new JSONObject(data);
            CommentBean bean = new CommentBean();

            bean.mCommentId = getJsonStringValue(dataObj, "commentid", "");
            bean.mCommentTxt = getJsonStringValue(dataObj, "text", "");
            bean.mUserHead = getJsonStringValue(dataObj, "authoravatar", "");
            bean.mUserId = getJsonStringValue(dataObj, "authorid", "");
            bean.mUserName = getJsonStringValue(dataObj, "authorname", "");
            bean.mReplyId = getJsonStringValue(dataObj, "replyid", "");
            bean.mReplyName = getJsonStringValue(dataObj, "replyname", "");
            bean.customavatar = getJsonStringValue(dataObj, "customavatar", "");
            bean.result = getJsonStringValue(dataObj, "result", "");
            bean.mSeq = getJsonStringValue(dataObj, "seq", "");
            if (dataObj.has("label")) {
                JSONObject labelObj = dataObj.getJSONObject("label");
                bean.mApprove = getJsonStringValue(labelObj, "approve", "");
                bean.mApprovelabel = getJsonStringValue(labelObj, "approvelabel", "");
                bean.mTarento = getJsonStringValue(labelObj, "tarento", "");
                bean.mHeadplusv = getJsonStringValue(labelObj, "headplusv", "");
                bean.mHeadplusvdes = getJsonStringValue(labelObj, "headplusvdes", "");
            }

            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 解析获取分享连接后的数据
     *
     * @param json 数据json串
     * @return
     * @author jyf
     * @date 2015年8月9日
     */
    public static ShareDataBean parseShareCallBackData(String json) {
        ShareDataBean bean = new ShareDataBean();
        bean.isSucess = false;
        try {
            JSONObject rootObj = new JSONObject(json);
            boolean isSucess = rootObj.getBoolean("success");

            JSONObject data = rootObj.getJSONObject("data");
            String shareurl = data.getString("shorturl");
            String coverurl = data.getString("coverurl");
            String describe = data.optString("describe");

            bean.isSucess = isSucess;
            bean.shareurl = shareurl;
            bean.coverurl = coverurl;
            bean.describe = describe;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    public static JSONObject getReportData(String tag, String function, String log) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("time", CSLog.getCurrentTime());
            obj.put("tag", tag);
            obj.put("function", function);
            obj.put("log", log);
            return obj;
        } catch (Exception e) {

        }
        return null;
    }

    public static JSONObject getActivationTimeJson(String sn) {
        try {
            JSONObject rootObj = new JSONObject();
            rootObj.put("sn", sn);
            rootObj.put("time", CSLog.getCurrentTime());

            return rootObj;
        } catch (Exception e) {

        }
        return null;
    }

    public static String getReportJson(String key, JSONObject dataObj, String hdtype) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("key", key);
            obj.put("data", dataObj);
            obj.put("hdtype", hdtype);
            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static String getCategoryLocalCacheJson(String mAttribute) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("attribute", mAttribute);

            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getUserNickNameJson(String nickName) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("nickname", nickName);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getUserSignJson(String sign) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("desc", sign);

            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getNetStateJson(boolean isConn) {
        try {
            JSONObject obj = new JSONObject();
            final String state = isConn ? "1" : "2";
            obj.put("NetStatus", state);

            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 获取显示notify
     *
     * @return
     * @author jyf
     */
    public static int getValidNotifyId() {
        int notifyId = 0;
        try {
            String current = String.valueOf(System.currentTimeMillis());
            if (current != null) {
                if (current.length() <= 9) {
                    notifyId = Integer.valueOf(current);
                } else {
                    int start = current.length() - 9;
                    notifyId = Integer.valueOf(current.substring(start));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notifyId;
    }

    public static XingGeMsgBean parseXingGePushMsg(String json) {
        if (null == json) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(json);
            XingGeMsgBean bean = new XingGeMsgBean();
            bean.notifyId = getValidNotifyId();
            bean.title = getJsonStringValue(root, "t", "");
            bean.msg = getJsonStringValue(root, "d", "");
            bean.target = getJsonStringValue(root, "g", "0");
            bean.tarkey = getJsonStringValue(root, "k", "1");
            bean.weburl = getJsonStringValue(root, "w", "");
            bean.params = root.getString("p");
            bean.disturb = getJsonStringValue(root, "b", "1");

            return bean;
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] parseVideoDetailId(String jsonArray) {
        try {
            JSONArray array = new JSONArray(jsonArray);
            int size = array.length();
            String[] strArray = new String[size];
            for (int i = 0; i < size; i++) {
                JSONObject obj = array.getJSONObject(i);
                strArray[i] = getJsonStringValue(obj, "vid", "");
            }

            return strArray;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPushRegisterJsonStr(String tid, String source, String ipcversion) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("tid", tid);
            obj.put("source", source);
            obj.put("ipcversion", ipcversion);
            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static SettingBean parsePushSettingJson(String json) {
        try {
            SettingBean bean = new SettingBean();

            JSONObject rootObj = new JSONObject(json);
            bean.isSucess = rootObj.getBoolean("success");
            JSONObject dataObj = rootObj.getJSONObject("data");
            bean.result = getJsonStringValue(dataObj, "result", "1");
            bean.isComment = getJsonStringValue(dataObj, "iscomment", "1");
            bean.isPraise = getJsonStringValue(dataObj, "ispraise", "1");
            bean.uid = getJsonStringValue(dataObj, "uid", "");
            bean.isFollow = getJsonStringValue(dataObj, "isfollow", "1");
            bean.isFriend = getJsonStringValue(dataObj, "isfriend", "1");
            return bean;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getPushSetJson(boolean iscomment, boolean ispraise, boolean isfollow) {
        try {
            String isC = iscomment ? "1" : "0";
            String isP = ispraise ? "1" : "0";
            String isF = isfollow ? "1" : "0";
            JSONObject obj = new JSONObject();
            obj.put("iscomment", isC);
            obj.put("ispraise", isP);
            obj.put("isfollow", isF);
            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 返回0是成功，其余的全是失败
     *
     * @param json
     * @return
     * @author jyf
     */
    public static String parseDelVideo(Object json) {
        try {
            JSONObject roobObj = new JSONObject((String) json);
            boolean sucess = roobObj.getBoolean("success");
            if (!sucess) {
                return "1";
            }
            JSONObject dataObj = roobObj.getJSONObject("data");
            String result = dataObj.getString("result");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "1";
    }

    public static String getDelRequestJson(String vid) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("videoid", vid);
            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 开关机提示音、精彩视频拍摄提示音
     *
     * @param speakerSwitch   开关机提示音
     * @param wonderfulSwitch 精彩视频拍摄提示音
     * @return
     */
    public static String getSpeakerSwitchJson(int speakerSwitch, int wonderfulSwitch) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("SpeakerSwitch", speakerSwitch);
            obj.put("WonderfulSwitch", wonderfulSwitch);
            return obj.toString();
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 查询ipc升级文件
     *
     * @param ipcVersion
     * @param ipcModel
     * @return
     */
    public static String selectIPCFile(String ipcVersion, String ipcModel) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("IPCVersion", ipcVersion);
            obj.put("IPCModel", ipcModel);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 保存ipc设备型号
    public static String getProductName(Object jsonStr) {
        try {
            JSONObject json = new JSONObject((String) jsonStr);
            if (json.isNull("productname")) {
                return "";
            } else {
                return json.optString("productname");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static VideoFileInfoBean jsonToVideoFileInfoBean(String data, String deviceType) {
        try {
            VideoFileInfoBean bean = new VideoFileInfoBean();
            JSONObject root = new JSONObject(data);
            bean.filename = root.optString("location");
            bean.type = String.valueOf(root.optInt("type"));
            Double size = root.optDouble("size");
            if (size != null) {
                bean.filesize = String.format("%.1fMB", size);
            }
            bean.resolution = root.optString("resolution");
            bean.period = String.valueOf(root.optInt("period"));
            if (!root.isNull("timestamp")) {
                bean.timestamp = root.optString("timestamp");
            } else {
                bean.timestamp = getTimeStamp(bean.filename);
            }
            bean.devicename = deviceType;
            bean.savetime = System.currentTimeMillis() + "";
            bean.picname = null;
            bean.gpsname = null;
            bean.reserve1 = null;
            bean.reserve2 = null;
            bean.reserve3 = null;
            bean.reserve4 = null;

            return bean;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getTimeStamp(String fileName) {
        try {
            int index = fileName.indexOf("_");
            int index2 = fileName.indexOf("_", index + 1);
            String timestamp = fileName.substring(index + 1, index2);
            if (timestamp.length() < 14) {
                timestamp = "20" + timestamp;
            }
            return timestamp;
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 精彩视频类型
     *
     * @param historyTime
     * @param futureTime
     * @return
     */
    public static String setWonderfulVideoTypeJson(int historyTime, int futureTime) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("wonder_history_time", historyTime);
            obj.put("wonder_future_time", futureTime);
//			obj.put("urgent_history_time", 0);
//			obj.put("urgent_future_time", 0);
            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 视频水印
     *
     * @param logoVisible
     * @return
     */
    public static String setVideoLogoJson(int logoVisible, int timeVisible) {
        try {
            JSONObject rootObj = new JSONObject();
            rootObj.put("logo_visible", logoVisible);
            rootObj.put("time_visible", timeVisible);

            return rootObj.toString();
        } catch (Exception e) {

        }
        return "";
    }

}
