package cn.com.mobnote.golukmobile.carrecorder.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	/**
	 * 文件大小的转换
	 * @param fileSize 文件大小
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static String getSizeShow(double fileSize) {
//		System.out.print("文件大小 : "+fileSize);
//		float sizeM = (float)fileSize / 1024 / 1024;
//		DecimalFormat df = new DecimalFormat("#.##");
//		String sizeResult ="";
//		if(fileSize > 1024){
//			double size = fileSize/1024;
//			sizeResult = size + "GB";
//		}else{
//			sizeResult = fileSize + "MB";
//		}
//		
//		return sizeResult;
		
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
	
	/**
	 * 冒泡排序
	 * @param list 需要排序的列表
	 * @param order true:正序　false:倒序
	 * @return
	 * @author xuhw
	 * @date 2015年5月16日
	 */
	public static List<String> bubbleSort(List<String> list, boolean order){
		List<String> arrlist = new ArrayList<String>();
		if (list == null)
			return arrlist;
		String arr[] = new String[list.size()];
		for(int i=0;i<list.size();i++){
			arr[i] = list.get(i);
		}
		
		int size=list.size();
		String t;
		for(int i=0;i<size-1;i++){
			for(int j=0;j<size-i-1;j++){
				if(order){
					if(arr[j+1].compareTo(arr[j]) < 0){
						t=arr[j+1];
						arr[j+1]=arr[j];
						arr[j]=t;
					}
				}else{
					if(arr[j+1].compareTo(arr[j]) > 0){
						t=arr[j+1];
						arr[j+1]=arr[j];
						arr[j]=t;
					}
				}
			}
		}
		
		for(int i=0;i<list.size();i++){
			arrlist.add(arr[i]);
		}
		
		return arrlist;
	}	

}
