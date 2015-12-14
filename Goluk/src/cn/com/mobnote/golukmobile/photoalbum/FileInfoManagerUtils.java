package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import android.text.TextUtils;

public class FileInfoManagerUtils {

	/**
	 * 根据文件名计算日期
	 * 
	 * @param name
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

	// /**
	// * 根据文件的最后修改时间进行排序
	// *
	// * @param files
	// * @return
	// */
	// public static List<String> sortFile(List<String> files) {
	// String file = "";
	// if (files != null && files.size() > 0) {
	// for (int i = 0; i < files.size(); i++) {
	// for (int j = i + 1; j <= files.size() - 1; j++) {
	// String fileI = files.get(i);
	// String fileJ = files.get(j);
	// if (fileI.length() >= 17) {
	// continue;
	// }
	//
	// if (fileJ.length() >= 17) {
	// continue;
	// }
	//
	// long timeI = Long.valueOf(fileI.substring(5, 17));
	// long timeJ = Long.valueOf(fileJ.substring(5, 17));
	// if (timeI > timeJ) {
	// file = files.get(i);
	// files.set(i, files.get(j));
	// files.set(j, file);
	// }
	// }
	// }
	// }
	// return files;
	// }

	// public static List<String> bubbleSort(List<String> list, boolean order) {
	// List<String> arrlist = new ArrayList<String>();
	// if (list == null)
	// return arrlist;
	// String arr[] = new String[list.size()];
	// for (int i = 0; i < list.size(); i++) {
	// arr[i] = list.get(i);
	// }
	//
	// int size = list.size();
	// String t;
	// for (int i = 0; i < size - 1; i++) {
	// for (int j = 0; j < size - i - 1; j++) {
	// if (order) {
	// if (arr[j + 1].length() < 4 || arr[j].length() < 4) {
	// continue;
	// }
	// String str1 = arr[j + 1].substring(4);
	// String str2 = arr[j].substring(4);
	// if (str1.compareTo(str2) < 0) {
	// // if(arr[j+1].compareTo(arr[j]) < 0){
	// t = arr[j + 1];
	// arr[j + 1] = arr[j];
	// arr[j] = t;
	// }
	// } else {
	// if (arr[j + 1].length() < 4 || arr[j].length() < 4) {
	// continue;
	// }
	// String str1 = arr[j + 1].substring(4);
	// String str2 = arr[j].substring(4);
	// if (str1.compareTo(str2) > 0) {
	// // if(arr[j+1].compareTo(arr[j]) > 0){
	// t = arr[j + 1];
	// arr[j + 1] = arr[j];
	// arr[j] = t;
	// }
	// }
	// }
	// }
	//
	// for (int i = 0; i < list.size(); i++) {
	// arrlist.add(arr[i]);
	// }
	//
	// return arrlist;
	// }

	/**
	 * 读取本地视频配置文件
	 * 
	 * @return
	 */
	public static List<String> getVideoConfigFile(String path) {
		List<String> data = new ArrayList<String>();

		File file = new File(path);
		if (file.exists()) {
			String str = "";
			try {
				FileInputStream fin = new FileInputStream(path);
				int length = fin.available();
				if (length <= 0) {
					fin.close();
					return data;
				}
				byte[] buffer = new byte[length];
				fin.read(buffer);
				str = EncodingUtils.getString(buffer, "UTF-8");
				fin.close();

				if (TextUtils.isEmpty(str)) {
					return data;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			String[] files = str.split(",");

			// 去重
			for (String f : files) {
				if (!data.contains(f)) {
					data.add(f);
				}
			}
			return data;
		} else {
			return null;
		}

	}

	/**
	 * 获取文件大小
	 * 
	 * @param f
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String getFileSize(File f) {
		// 获取文件大小
		FileInputStream fis = null;
		String size = "";
		try {
			fis = new FileInputStream(f);
			int fileLen = fis.available();
			size = String.format("%.1f", fileLen / 1024.f / 1024.f) + "MB";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

}
