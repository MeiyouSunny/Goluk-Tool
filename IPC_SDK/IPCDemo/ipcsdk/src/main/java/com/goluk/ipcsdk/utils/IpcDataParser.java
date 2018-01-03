package com.goluk.ipcsdk.utils;

import android.text.TextUtils;

import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorageState;
import com.goluk.ipcsdk.bean.VideoConfigState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cn.com.tiros.debug.GolukDebugUtils;

public class IPCDataParser {

    public static class TriggerRecord {
        /**
         * 1:循环影像  2:紧急录像
         */

        public int type;
        /**
         * 文件名字
         */

        public String fileName;
    }

    /**
     * 多个文件查询
     *
     * @param type
     * @param limitCount
     * @param timestart
     * @param timeend
     * @param resForm    0:自动查询  1：相册查询
     * @return
     * @author jiayf
     * @date Mar 10, 2015
     */

    public static String getQueryMoreFileJson(int type, int limitCount, long timestart, long timeend, String resForm) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("limitCount", limitCount);
            json.put("timestart", timestart);
            json.put("timeend", timeend);
            json.put("tag", resForm);
        } catch (Exception e) {
            return null;
        }

        return json.toString();
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
     * 查询8秒视频与精彩视频 JSON串
     *
     * @param type
     * @param preSeconds
     * @param postSeconds
     * @return
     * @author jiayf
     * @date Mar 10, 2015
     */

    public static final String getTriggerRecordJson(int type, int preSeconds, int postSeconds) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("preSeconds", preSeconds);
            json.put("postSeconds", postSeconds);
        } catch (Exception e) {
            return null;
        }

        return json.toString();
    }

    public static final TriggerRecord parseTriggerRecordResult(String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                TriggerRecord record = new TriggerRecord();
                JSONObject obj = new JSONObject(json);
                int type = obj.getInt("type");
                String fileName = obj.optString("filename");

                record.type = type;
                record.fileName = fileName;

                return record;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 解析多个文件
     *
     * @param json
     * @return
     * @author jiayf
     * @date Mar 10, 2015
     *//*

	public static ArrayList<FileInfo> parseMoreFile(String json) {

		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		try {
			JSONObject obj = new JSONObject(json);
			
			JSONArray array = obj.getJSONArray("items");
			int length = array.length();
			for (int i = length-1; i >= 0; i--) {
				JSONObject itemObj = array.getJSONObject(i);
				FileInfo info = parseSingleFileResult(itemObj.toString());
				if(null != info){
					list.add(info);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return list;
	}
	
/*
/**
	 * 解析多个文件
	 * 
	 * @param json
	 * @return
	 * @author jiayf
	 * @date Mar 10, 2015
	 */
    public static ArrayList<VideoInfo> parseVideoListData(String json) {
//		String filePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/";
//		String[] videoPaths = { "", "loop/", "urgent/", "", "wonderful/" };
        ArrayList<VideoInfo> list = new ArrayList<VideoInfo>();
//		List<String> files = null;

        try {
            JSONObject obj = new JSONObject(json);

            JSONArray array = obj.getJSONArray("items");
            int length = array.length();

            for (int i = length - 1; i >= 0; i--) {
                JSONObject itemObj = array.getJSONObject(i);
                FileInfo info = parseSingleFileResult(itemObj.toString());
                if (null != info) {
                    VideoInfo mVideoInfo = getVideoInfo(info);
//					if (files == null) {
//						int type = IPCDataParser.parseVideoFileType(mVideoInfo.filename);
//						files = FileInfoManagerUtils.getFileNames(filePath + videoPaths[type], "(.+?mp4)");
//					}
//
//					if (files != null && files.contains(mVideoInfo.filename)) {
//						mVideoInfo.isAsync = true;
//					}
                    list.add(mVideoInfo);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return list;
    }

    /**
     * IPC视频文件信息转列表显示视频信息
     *
     * @param mVideoFileInfo IPC视频文件信息
     * @return 列表显示视频信息
     * @author xuhw
     * @date 2015年3月25日
     */
    public static VideoInfo getVideoInfo(FileInfo mVideoFileInfo) {
        VideoInfo info = new VideoInfo();
        // 文件选择状态
        info.isSelect = false;
        info.id = mVideoFileInfo.id;
        info.videoSize = GolukUtils.getSizeShow(mVideoFileInfo.size);
        info.countTime = GolukUtils.minutesTimeToString(mVideoFileInfo.period);
        info.videoHP = mVideoFileInfo.resolution;
        if (TextUtils.isEmpty(mVideoFileInfo.timestamp)) {
            info.videoCreateDate = GolukUtils.getTimeStr(mVideoFileInfo.time * 1000);
        } else {
            info.videoCreateDate = countFileDateToString(mVideoFileInfo.timestamp);
        }

        info.videoPath = mVideoFileInfo.location;
        info.filename = mVideoFileInfo.location;
        info.time = mVideoFileInfo.time;
//        info.isNew = SettingUtils.getInstance().getBoolean("Cloud_" + mVideoFileInfo.location, true);

        String fileName = mVideoFileInfo.location;
        fileName = fileName.replace(".mp4", ".jpg");
        String filePath = GolukIPCSdk.getInstance().getCarrecorderCachePath() + File.separator + "image";
        GolukUtils.makedir(filePath);
        File file = new File(filePath + File.separator + fileName);
        if (file.exists()) {
            // info.videoBitmap = ImageManager.getBitmapFromCache(filePath +
            // File.separator + fileName, 194, 109);
        } else {
            if (1 == mVideoFileInfo.withSnapshot) {
//                GolukIPCSdk.getInstance() .getInstance() .getIPCControlManager().downloadFile(fileName, "IPC_IMAGE" + mVideoFileInfo.id, FileUtils.javaToLibPath(filePath),mVideoFileInfo.time);
                GolukDebugUtils.e("xuhw", "TTT====111111=====filename=" + fileName + "===tag=" + mVideoFileInfo.id);
            }
        }

        return info;
    }

    /**
     * 根据文件名计算日期
     *
     * @param date
     * @return
     */
    public static String countFileDateToString(String date) {

        String dateString = "";
        try {
            String year = date.substring(0, 4);
            String mouth = date.substring(4, 6);
            String day = date.substring(6, 8);
            String hour = date.substring(8, 10);
            String minute = date.substring(10, 12);
            String second = date.substring(12, 14);
            dateString = year + "-" + mouth + "-" + day + " " + hour + ":" + minute + ":" + second;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    public static String getIpcQueryListReqTag(String data) {
        String tag = "";
        try {
            JSONObject obj = new JSONObject(data);
            tag = obj.optString("tag");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return tag;

    }

    /**
     * 获取文件列表总个数
     *
     * @param json
     * @return
     * @author jiayf
     * @date Mar 10, 2015
     */

    public static int getFileListCount(String json) {
        int total = 0;
        try {
            JSONObject obj = new JSONObject(json);
            total = obj.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public static FileInfo parseSingleFileResult(String json) {

        try {
            JSONObject obj = new JSONObject(json);
            int id = obj.getInt("id");
            long time = obj.getLong("time");

            int period = obj.getInt("period");
            int type = obj.getInt("type");
            double size = obj.getDouble("size");
            String location = obj.getString("location");
            String resolution = obj.getString("resolution");
            int withSnapshot = obj.getInt("withSnapshot");
            int withGps = obj.getInt("withGps");
            String timeStamp = obj.optString("timestamp");
            FileInfo fileInfo = new FileInfo();
            fileInfo.id = id;
            fileInfo.period = period;
            fileInfo.time = time;
            fileInfo.type = type;
            fileInfo.size = size;
            fileInfo.location = location;
            fileInfo.resolution = resolution;
            fileInfo.withSnapshot = withSnapshot;
            fileInfo.withGps = withGps;
            fileInfo.timestamp = timeStamp;
            return fileInfo;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


   /* *//**
     * 解析IPC设备存储状态json
     *
     * @param json
     * @return
     * @author xuhw
     * @date 2015年4月7日
     *//*

    public static DeviceState parseDeviceState(String json) {
        try {

            DeviceState deviceState = new DeviceState();

            JSONObject obj = new JSONObject(json);
            double totalSizeOnSD = obj.getDouble("totalSizeOnSD");
            int cameraStatus = obj.getInt("cameraStatus");
            double leftSizeOnSD = obj.getDouble("leftSizeOnSD");
            int onlineUsers = obj.getInt("onlineUsers");
            int SDPresent = obj.getInt("SDPresent");
            int isSpaceTooSmall = obj.getInt("isSpaceTooSmall");

            deviceState.totalSizeOnSD = totalSizeOnSD;
            deviceState.cameraStatus = cameraStatus;
            deviceState.leftSizeOnSD = leftSizeOnSD;
            deviceState.onlineUsers = onlineUsers;
            deviceState.SDPresent = SDPresent;
            deviceState.isSpaceTooSmall = isSpaceTooSmall;

            return deviceState;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }*/

    /**
     * 解析录制存储状态json
     *
     * @param json
     * @return
     * @author xuhw
     * @date 2015年4月7日
     */

    public static RecordStorageState parseRecordStorageStatus(String json) {
        try {
            RecordStorageState mRecordStorgeState = new RecordStorageState();
            JSONObject obj = new JSONObject(json);
            int SDCardActive = obj.getInt("SDCardActive");
            int isSpaceTooSmall = obj.getInt("isSpaceTooSmall");
            double totalSdSize = obj.getDouble("totalSdSize");
            double userFilesSize = obj.getDouble("userFilesSize");
            double leftSize = obj.getDouble("leftSize");
            double normalRecQuota = obj.getDouble("normalRecQuota");
            double normalRecSize = obj.getDouble("normalRecSize");
            double urgentRecQuota = obj.getDouble("urgentRecQuota");
            double urgentRecSize = obj.getDouble("urgentRecSize");
            double wonderfulRecQuota = obj.getDouble("wonderfulRecQuota");
            double wonderfulRecSize = obj.getDouble("wonderfulRecSize");
            double picQuota = obj.getDouble("picQuota");
            double picSize = obj.getDouble("picSize");

            mRecordStorgeState.SDCardActive = SDCardActive;
            mRecordStorgeState.isSpaceTooSmall = isSpaceTooSmall;
            mRecordStorgeState.totalSdSize = totalSdSize;
            mRecordStorgeState.userFilesSize = userFilesSize;
            mRecordStorgeState.leftSize = leftSize;
            mRecordStorgeState.normalRecQuota = normalRecQuota;
            mRecordStorgeState.normalRecSize = normalRecSize;
            mRecordStorgeState.urgentRecQuota = urgentRecQuota;
            mRecordStorgeState.urgentRecSize = urgentRecSize;
            mRecordStorgeState.wonderfulRecQuota = wonderfulRecQuota;
            mRecordStorgeState.wonderfulRecSize = wonderfulRecSize;
            mRecordStorgeState.picQuota = picQuota;
            mRecordStorgeState.picSize = picSize;

            return mRecordStorgeState;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

     /*
      * Parse video config info
      */
    public static VideoConfigState parseVideoConfigState(String json) {
        try {
            VideoConfigState mVideoConfigState = new VideoConfigState();
            JSONArray array = new JSONArray(json);
            if (null != array) {
                if (array.length() > 0) {
                    JSONObject obj = array.getJSONObject(0);
                    int bitstreams = obj.getInt("bitstreams");
                    String resolution = obj.getString("resolution");
                    int frameRate = obj.getInt("frameRate");
                    int bitrate = obj.getInt("bitrate");
                    int AudioEnabled = obj.getInt("audioEnabled");

                    mVideoConfigState.bitstreams = bitstreams;
                    mVideoConfigState.resolution = resolution;
                    mVideoConfigState.frameRate = frameRate;
                    mVideoConfigState.bitrate = bitrate;
                    mVideoConfigState.AudioEnabled = AudioEnabled;

                    return mVideoConfigState;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

 /*   *//**
     * 获取自动循环录制状态
     *
     * @param json
     * @return
     * @author xuhw
     * @date 2015年4月8日
     *//*

    public static boolean getAutoRecordState(String json) {
        try {
            boolean state = false;
            JSONObject obj = new JSONObject(json);
            if (null != obj) {
                int status = obj.optInt("status");
                if (1 == status) {
                    state = true;
                } else {
                    state = false;
                }
            }
            return state;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
*/
 /*   *//**
     * 解析版本信息json
     *
     * @param json
     * @return
     * @author xuhw
     * @date 2015年4月9日
     *//*

    public static IPCIdentityState parseVersionState(String json) {
        try {
            IPCIdentityState mVersionState = new IPCIdentityState();
            JSONObject obj = new JSONObject(json);
            if (null != obj) {
                int code = obj.optInt("code");
                String name = obj.optString("name");
                String contact = obj.optString("contact");
                String location = obj.optString("location");
                String memo = obj.optString("memo");

                mVersionState.code = code;
                mVersionState.name = name;
                mVersionState.contact = contact;
                mVersionState.location = location;
                mVersionState.memo = memo;
            }
            return mVersionState;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
*/

    /**
     * @param json
     * @return
     */
    public static long parseIPCTime(String json) {
        long time = 0;
        try {
            JSONObject obj = new JSONObject(json);
            if (null != obj) {
                time = obj.optLong("IPCTime");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return time;
    }

  /*  *//**
     * 解析kit和紧急视频返回的数据
     *
     * @param json
     * @return
     * @author xuhw
     * @date 2015年4月23日
     *//*

    public static List<ExternalEventsDataInfo> parseKitData(String json) {
        List<ExternalEventsDataInfo> dataList = new ArrayList<ExternalEventsDataInfo>();
        try {
            JSONArray array = new JSONArray(json);
            if (null != array) {
                for (int i = 0; i < array.length(); i++) {
                    ExternalEventsDataInfo info = new ExternalEventsDataInfo();
                    JSONObject obj = array.getJSONObject(i);
                    if (null != obj) {
                        info.id = obj.optDouble("id");
                        info.type = obj.optInt("type");
                        info.resolution = obj.optString("resolution");
                        info.period = obj.optInt("period");
                        info.time = obj.optInt("time");
                        info.size = obj.optDouble("size");
                        info.location = obj.optString("location");
                        info.withSnapshot = obj.optInt("withSnapshot");
                        info.withGps = obj.optInt("withGps");
                        dataList.add(info);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    public static int parseVideoFileType(String filename) {
        int type = -1;

        if (filename.contains("WND")) {
            type = IPCManagerFn.TYPE_SHORTCUT;
        } else if (filename.contains("URG")) {
            type = IPCManagerFn.TYPE_URGENT;
        } else if (filename.contains("NRM")) {
            type = IPCManagerFn.TYPE_CIRCULATE;
        }

        return type;
    }
*/
}
