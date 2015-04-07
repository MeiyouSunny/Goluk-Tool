package cn.com.mobnote.golukmobile.carrecorder;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.carrecorder.entity.DeviceState;
import cn.com.mobnote.golukmobile.carrecorder.entity.RecordStorgeState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import android.text.TextUtils;

public class IpcDataParser {

	public static class TriggerRecord {
		/** 1:循环影像  2:紧急录像  4:精彩视频 */
		public int type;
		/** 文件名字 */
		public String fileName;
	}
	
	/**
	 * 多个文件查询
	 * 
	 * @param type
	 * @param limitCount
	 * @param timestart
	 * @param timeend
	 * @return
	 * @author jiayf
	 * @date Mar 10, 2015
	 */
	public static String getQueryMoreFileJson(int type, int limitCount, int timestart, int timeend) {
		JSONObject json = new JSONObject();
		try {
			json.put("type", type);
			json.put("limitCount", limitCount);
			json.put("timestart", timestart);
			json.put("timeend", timeend);
		} catch (Exception e) {
			return null;
		}

		return json.toString();
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
		if(!TextUtils.isEmpty(json)){
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
	 */
	public static ArrayList<VideoFileInfo> parseMoreFile(String json) {

		ArrayList<VideoFileInfo> list = new ArrayList<VideoFileInfo>();
		try {
			JSONObject obj = new JSONObject(json);
			int total = obj.getInt("total");
			
			JSONArray array = obj.getJSONArray("items");
			int length = array.length();
			for (int i = length-1; i >= 0; i--) {
				JSONObject itemObj = array.getJSONObject(i);
				VideoFileInfo info = parseSingleFileResult(itemObj.toString());
				if(null != info){
					list.add(info);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return list;
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
		int total=0;
		try {
			JSONObject obj = new JSONObject(json);
			total = obj.getInt("total");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total;
	}

	public static VideoFileInfo parseSingleFileResult(String json) {

		try {
			JSONObject obj = new JSONObject(json);
			int id = obj.getInt("id");
			long time = obj.getLong("time");

			int period = obj.getInt("period");
			int type = obj.getInt("type");
			int size = obj.getInt("size");
			String location = obj.getString("location");
			int resolution = obj.getInt("resolution");
			int withSnapshot = obj.getInt("withSnapshot");
			int withGps = obj.getInt("withGps");

			VideoFileInfo fileInfo = new VideoFileInfo();
			fileInfo.id = id;
			fileInfo.period = period;
			fileInfo.time = time;
			fileInfo.type = type;
			fileInfo.size = size;
			fileInfo.location = location;
			fileInfo.resolution = resolution;
			fileInfo.withSnapshot = withSnapshot;
			fileInfo.withGps = withGps;

			return fileInfo;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 解析IPC设备存储状态json
	 * @param json
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
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
	}
	
	/**
	 * 解析录制存储状态json
	 * @param json
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static RecordStorgeState parseRecordStorageStatus(String json) {
		try {
			RecordStorgeState mRecordStorgeState = new RecordStorgeState();
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
			
			mRecordStorgeState.SDCardActive=SDCardActive;
			mRecordStorgeState.isSpaceTooSmall=isSpaceTooSmall;
			mRecordStorgeState.totalSdSize=totalSdSize;
			mRecordStorgeState.userFilesSize=userFilesSize;
			mRecordStorgeState.leftSize=leftSize;
			mRecordStorgeState.normalRecQuota=normalRecQuota;
			mRecordStorgeState.normalRecSize=normalRecSize;
			mRecordStorgeState.urgentRecQuota=urgentRecQuota;
			mRecordStorgeState.urgentRecSize=urgentRecSize;
			mRecordStorgeState.wonderfulRecQuota=wonderfulRecQuota;
			mRecordStorgeState.wonderfulRecSize=wonderfulRecSize;
			mRecordStorgeState.picQuota=picQuota;
			mRecordStorgeState.picSize=picSize;
			
			return mRecordStorgeState;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 解析视频配置信息json
	 * @param json
	 * @return
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	public static VideoConfigState parseVideoConfigState(String json) {
		try {
			VideoConfigState mVideoConfigState = new VideoConfigState();
			JSONObject obj = new JSONObject(json);
			int bitstreams = obj.getInt("bitstreams");
			String resolution = obj.getString("resolution");
			int frameRate = obj.getInt("frameRate");
			int bitrate = obj.getInt("bitrate");
			int AudioEnabled = obj.getInt("audioEnabled");
			
			mVideoConfigState.bitstreams=bitstreams;
			mVideoConfigState.resolution=resolution;
			mVideoConfigState.frameRate=frameRate;
			mVideoConfigState.bitrate=bitrate;
			mVideoConfigState.AudioEnabled=AudioEnabled;
			
			return mVideoConfigState;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
