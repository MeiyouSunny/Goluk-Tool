package cn.com.mobnote.golukmobile.carrecorder;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import android.text.TextUtils;

public class IpcDataParser {

	public static class TriggerRecord {
		/** 1:循环影像  2:紧急录像  4:精彩视频 */
		public int type;
		/** 文件名字 */
		public String fileName;
	}

	/** 设备状态 */
	public static class DeviceState {
		/** SD卡总容量，单位为Byte。仅当SDActive为YES时，此字段有意义 */
		public int totalSizeOnSD;
		/** 当前镜头状态。 1 –正常 2 –异常 */
		public int cameraStatus;
		/** SD卡剩余容量，单位为Byte。仅当SDActive为YES时，此字段有意义。*/
		public int leftSizeOnSD;
		/** 当前登录用户数 */
		public int onlineUsers;
		/** SD卡在位YES(1) | SD卡不存在NO(0) */
		public int SDPresent;
		/** SD卡剩余容量太小，不能继续录制(1) | SD卡容量正常(0) */
		public int isSpaceTooSmall;
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
			for (int i = 0; i < length; i++) {
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

	public static DeviceState parseDeviceState(String json) {
		try {

			DeviceState deviceState = new DeviceState();

			JSONObject obj = new JSONObject(json);
			int totalSizeOnSD = obj.getInt("totalSizeOnSD");
			int cameraStatus = obj.getInt("cameraStatus");
			int leftSizeOnSD = obj.getInt("leftSizeOnSD");
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

}
