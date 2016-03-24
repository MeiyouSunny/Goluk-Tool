package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

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
	 * Get a list of filenames in this folder.
	 * 
	 * @param folder
	 *            Full path of directory
	 * @param fileNameFilterPattern
	 *            Regular expression suitable for {@link String#matches(String)}
	 *            , or null. See {@link java.util.regex.Pattern} for more
	 *            details.
	 * @return list of filenames (the names only, not the full path), or
	 *         <tt>null</tt> if <tt>folder</tt> doesn't exist or isn't a
	 *         directory, or if nothing matches <tt>fileNameFilterPattern</tt>
	 * @throws PatternSyntaxException
	 *             if <tt>fileNameFilterPattern</tt> is non-null and isn't a
	 *             valid Java regular expression
	 */
	public static ArrayList<String> getFileNames(final String folder, final String fileNameFilterPattern) {
		ArrayList<String> myData = new ArrayList<String>();
		File fileDir = new File(folder);
		if (!fileDir.exists() || !fileDir.isDirectory()) {
			return myData;
		}

		String[] files = fileDir.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (fileNameFilterPattern == null || files[i].matches(fileNameFilterPattern))
					myData.add(files[i]);
			}
		}

		return myData;
	}
	/**
	 * 读取本地视频配置文件
	 * 
	 * @return
	 */
	public static List<String> getVideoConfigFile(String path) {
		List<String> data = new ArrayList<String>();
		FileInputStream fin = null;
		File file = new File(path);
		if (file.exists()) {
			String str = "";
			try {
				fin = new FileInputStream(path);
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
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
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
			fis.close();
		} catch (Exception e) {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return size;
	}

}
