package cn.com.mobnote.golukmobile.carrecorder.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Utils {

	/**
	 * 文件大小的转换
	 * @param fileSize 文件大小
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static String getSizeShow(long fileSize) {
		System.out.print("文件大小 : "+fileSize);
		float sizeM = (float)fileSize / 1024 / 1024;
		DecimalFormat df = new DecimalFormat("#.0");
		String sizeResult = df.format(sizeM) + "MB";
		return sizeResult;
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

		if (time / 60 < 1) {
			if (time < 10) {
				result = "00:0" + time;
			} else {
				result = "00:" + time;
			}

		} else {
			int minue = time / 60;
			int nSecond = time % 60;
			String minueStr = minue > 10 ? "" + minue : "0" + minue;
			String secondStr = nSecond > 10 ? "" + nSecond : "0" + nSecond;
			result = minueStr + ":" + secondStr;
		}

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String result = sdf.format(new Long(time));
		return result;
	}
	
	

}
