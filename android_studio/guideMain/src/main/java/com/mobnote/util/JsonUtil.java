package com.mobnote.util;

import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.settings.VideoQualityActivity;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.userinfohome.bean.UserLabelBean;
import com.mobnote.golukmain.userlogin.UserInfo;
import com.mobnote.golukmain.videosuqare.ShareDataBean;
import com.mobnote.golukmain.xdpush.SettingBean;
import com.mobnote.user.APPInfo;
import com.mobnote.user.IPCInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cn.com.mobnote.module.location.GolukPosition;
import cn.com.tiros.api.CSLog;
import cn.com.tiros.api.FileUtils;

//import com.mobnote.golukmain.xdpush.XingGeMsgBean;

//import com.mobnote.golukmain.xdpush.XingGeMsgBean;

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
     * ??????IPC???????????????json???
     *
     * @param mode ????????????????????????IPCManagerFn?????????IPCMgrMode_IPCDirect/IPCMgrMode_Mobnote
     * @return json???
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
     * ??????????????????wifi?????????json???
     *
     * @param state ??????wifi?????? 0/1????????????/?????????
     * @param ip    ?????????IP??????
     * @return JSON???
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
     * ?????????????????????json???
     *
     * @param filename ????????????
     * @param tag      ????????????????????????
     * @param savepath ?????????????????????
     * @return JSON???
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
     * ???????????????????????????json???
     *
     * @param lon       ?????????
     * @param lat       ??????
     * @param speed     ??????
     * @param direction ??????
     * @return json
     * @author xuhw
     * @date 2015???3???31???
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
     * ????????????IPC????????????json???
     *
     * @param time ??????
     * @return
     * @author xuhw
     * @date 2015???4???3???
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
     *  T3 ?????????????????????IPC????????????json???
     *
     * @param time ??????
     * @return
     * @author xuhw
     * @date 2015???4???3???
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

    public static UserInfo parseSingleUserInfoJson(JSONObject rootObj) {
        try {
            UserInfo userInfo = new UserInfo();
            // ?????? ????????????
            if (rootObj.has("label")) {
                JSONObject labelObj = rootObj.getJSONObject("label");
                UserLabelBean label = new UserLabelBean();
                label.approve = getJsonStringValue(labelObj, "approve", "");
                label.approvelabel = getJsonStringValue(labelObj, "approvelabel", "0");
                label.tarento = getJsonStringValue(labelObj, "tarento", "0");
                label.headplusv = getJsonStringValue(labelObj, "headplusv", "0");
                label.headplusvdes = getJsonStringValue(labelObj, "headplusvdes", "");
            }

            return userInfo;
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

    /**
     * ??????IPC??????????????????????????????????????????????????????
     *
     * @return
     * @author xuhw
     * @date 2015???4???7???
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
     * ??????IPC??????????????????????????????????????????????????????
     *
     * @return
     * @author xuhw
     * @date 2015???4???7???
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

    /**
     * ???????????????????????????????????????
     */
    public static String getEmgVideoSoundConfigJson_T1(int state) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("urgentSwitch", state);

            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * ????????????????????????????????????
     */
    public static String getTimelapseConfigJson_T1(int state) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("timelapse", state);

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
     * ??????????????????
     *
     * @param type ???0:????????????1:?????????
     * @return
     * @author xuhw
     * @date 2015???4???7???
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

    // ?????????????????????json???
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
     * ???????????????????????????
     *
     * @param appVersion appVersion????????????
     * @param ipcVersion
     * @param ipcModel   ipc????????????
     * @return
     */
    public static String putIPC(String appVersion, String ipcVersion, String ipcModel) {
        try {
            // {???AppVersionFilePath???:???fs6:/version???, ???IpcVersion???:???1.2.3.4???}
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
     * ipc??????????????????
     *
     * @param url
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
     * ??????ipc????????????
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
     * APP??????
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

    // videoId: ??????ID
    // type: ???????????? 1/2 ??????/??????
    // attribute: ???????????????????????????(????????????????????????,?????????json??????)
    // desc ???????????? (????????????????????? (???????????????????????????????????????))
    // issquare ??????????????????????????? 0/1 (???/???)
    // thumbImgJavaPath: ???????????????
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
            // ??????????????????????????? 0/1 ???/???
            obj.put("issquare", issquare);
            // ???????????????
            obj.put("imgpath", fsFile);
            // type: 1/2 ???????????? / ????????????
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
            // {PNumber??????13054875692??????Password??????xxx??????VCode??????1234???}
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
            // {PNumber??????13054875692??????Password??????xxx??????VCode??????1234???}
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
     * ???????????????????????????json???
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

    /**
     * ????????????????????????????????????
     *
     * @param json ??????json???
     * @return
     * @author jyf
     * @date 2015???8???9???
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
     * ????????????notify
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

//    public static XingGeMsgBean parseXingGePushMsg(String json) {
//        if (null == json) {
//            return null;
//        }
//        try {
//            JSONObject root = new JSONObject(json);
//            XingGeMsgBean bean = new XingGeMsgBean();
//            bean.notifyId = getValidNotifyId();
//            bean.title = getJsonStringValue(root, "t", "");
//            bean.msg = getJsonStringValue(root, "d", "");
//            bean.target = getJsonStringValue(root, "g", "0");
//            bean.tarkey = getJsonStringValue(root, "k", "1");
//            bean.weburl = getJsonStringValue(root, "w", "");
//            bean.params = root.getString("p");
//            bean.disturb = getJsonStringValue(root, "b", "1");
//
//            return bean;
//        } catch (Exception e) {
//            return null;
//        }
//    }

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
     * ??????0?????????????????????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param speakerSwitch   ??????????????????
     * @param wonderfulSwitch ???????????????????????????
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
     * ??????ipc????????????
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

    // ??????ipc????????????
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
     * ??????????????????
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
     * ????????????
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
