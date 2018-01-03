package com.goluk.ipcsdk.utils;

import com.goluk.ipcsdk.bean.VideoConfigState;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class GolukUtils {

	/**
	 * 文件大小的转换
	 * @param fileSize 文件大小
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static String getSizeShow(double fileSize) {
		
		String result="";
		double totalsize=0;
		
		java.text.DecimalFormat   df=new   java.text.DecimalFormat("#.##");
		if(fileSize >= 1024){
			totalsize = fileSize/1024;
			result = df.format(totalsize) + "GB";
		}else{
			totalsize = fileSize;
			result = df.format(totalsize) + "MB";
		}

		return result;
	}
	
	
	
	/**
	 * 时间转换成字符串
	 * @param time 秒
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static String minutesTimeToString(int time) {
		String result = null;

//		if (time / 60 < 1) {
//			if (time < 10) {
//				result = "00:0" + time;
//			} else {
//				result = "00:" + time;
//			}
//
//		} else {
//			int minue = time / 60;
//			int nSecond = time % 60;
//			String minueStr = minue > 10 ? "" + minue : "0" + minue;
//			String secondStr = nSecond > 10 ? "" + nSecond : "0" + nSecond;
//			result = minueStr + ":" + secondStr;
//		}
		
		result = time + "s";

		return result;
	}

	/**
	 * 格式化时间格式
	 * @param time 时间
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static String getTimeStr(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String result = sdf.format(new Long(time));
		return result;
	}

	public static String getDateStr(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String result = sdf.format(new Long(time));
		return result;
	}

	public static String getSavePath(int type) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			return IPCManagerFn.VIDEO_SAVEPATH + "wonderful/";
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			return IPCManagerFn.VIDEO_SAVEPATH + "urgent/";
		} else {
			return IPCManagerFn.VIDEO_SAVEPATH + "loop/";
		}
	}

	/**
	 * 创建文件夹
	 *
	 * @param dirname
	 *            文件夹路径
	 * @author xuhw
	 * @date 2015年2月12日
	 */
	public static boolean makedir(String dirname) {
		File dir = new File(dirname);
		if (!dir.exists()) {
			return dir.mkdirs();
		}

		return false;
	}

    public static int parseVideoFileType(String filename) {
        int type = -1;

        if (filename.contains("WND")) {
            type = IPCManagerFn.TYPE_SHORTCUT;
        } else if(filename.contains("URG")) {
            type = IPCManagerFn.TYPE_URGENT;
        }else if(filename.contains("NRM")) {
            type = IPCManagerFn.TYPE_CIRCULATE;
        }

        return type;
    }

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
}
