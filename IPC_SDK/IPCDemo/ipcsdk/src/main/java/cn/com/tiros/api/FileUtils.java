package cn.com.tiros.api;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class FileUtils {
	public static final String PATH_FS1 = "/goluk";

	public static final String PATH_FS4 = "/Cloud";

	public static final double SYS_MEMORY_UNIT = 1048576.0;

	/**
	 * 文件是否存在
	 * 
	 * @param fileName
	 * @return
	 */
	public static int sys_fexist(String fileName) {
		String filePath = libToJavaPath(fileName);
		File file = new File(filePath);
		if (file.exists()) {
			return 1;
		}
		filePath = null;
		file = null;
		return 0;
	}

	/**
	 * 获取磁盘总空间
	 * 
	 * @param pszDisk
	 * @return MB
	 */
	public static double sys_fgetspace(String pszDisk) {
		StatFs sf = null;
		if (pszDisk.startsWith("fs1")) {
			if (sys_fexist("fs1:/") == 0) {
				String filePath = libToJavaPath(pszDisk);
				File f = new File(filePath);
				f.mkdirs();
				filePath = null;
				f = null;
			}
			try {
				sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
			} catch (Exception e) {
			}
		} else {
			String filePath = libToJavaPath(pszDisk);
			try {
				sf = new StatFs(filePath);
			} catch (Exception e) {
			}
		}
		return ((long) sf.getBlockCount() * (long) sf.getBlockSize()) / SYS_MEMORY_UNIT;
	}

	/**
	 * 获取磁盘剩余空间
	 * 
	 * @param pszDisk
	 * @return MB
	 */
	public static double sys_fgetfreespace(String pszDisk) {
		StatFs sf = null;
		if (pszDisk.startsWith("fs1")) {
			if (sys_fexist("fs1:/") == 0) {
				String filePath = libToJavaPath(pszDisk);
				File f = new File(filePath);
				f.mkdirs();
				filePath = null;
				f = null;
			}
			try {
				sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
			} catch (Exception e) {
			}
		} else {
			String filePath = libToJavaPath(pszDisk);
			try {
				sf = new StatFs(filePath);
			} catch (Exception e) {
			}
		}
		return ((long) sf.getAvailableBlocks() * (long) sf.getBlockSize()) / SYS_MEMORY_UNIT;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param fileName
	 * @return
	 */
	public static int sys_fgetsize(String fileName) {
		String filePath = libToJavaPath(fileName);
		File file = new File(filePath);
		filePath = null;
		if (file.exists()) {
			int length = (int) file.length();
			file = null;
			return length;
		}
		file = null;
		return 0;
	}

	/**
	 * 建立目录 Create all the directories needed for this File
	 * 
	 * @return
	 */
	public static int sys_fmkdir(String dirName) {
		String filePath = libToJavaPath(dirName);
		File file = new File(filePath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				file = null;
				return 1;
			} else {
				file = null;
				return 0;
			}
		} else {
			file = null;
			return 1;
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param file
	 * @return
	 */
	public static int sys_fremove(String fileName) {
		String filePath = libToJavaPath(fileName);
		File file = new File(filePath);
		if (file.exists()) {
			if (!file.isDirectory()) {
				if (file.delete()) {
					file = null;
					return 1;
				}
			}
		} else {
			return 1;
		}
		file = null;
		return 0;
	}

	/**
	 * 改名
	 * 
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public static int sys_frename(String oldName, String newName) {
		String oldPath = libToJavaPath(oldName);
		String newPath = libToJavaPath(newName);
		File oldFile = new File(oldPath);
		File newFile = new File(newPath);
		if (oldFile.renameTo(newFile)) {
			oldFile = null;
			newFile = null;
			return 1;
		} else {
			oldFile = null;
			newFile = null;
			return 0;
		}
	}

	/**
	 * 删除目录
	 * 
	 * @param dirName
	 * @return
	 */
	public static int sys_frmdir(String dirName) {
		String filePath = libToJavaPath(dirName);
		File file = new File(filePath);
		if (file.exists() && file.isDirectory()) {
			deleteFile(file);
		}
		file = null;
		return 1;
	}

	/**
	 * @brief 获取存储器是否存在
	 * @param[in] pszDisk - 准备判断是否存在的存储器
	 * @return - true：该存储器存在 false:该存储器不存在
	 * @par 接口使用约定: 1.参数pszDisk可以为 “fs0:/”、“fs1:/”或“fs2:/”<br>
	 */
	public static int sys_fdiskexist(String pszDisk) {
		if (pszDisk != null) {
			if (pszDisk.startsWith("fs4:/")) {
				sys_fmkdir("fs4:/");
				return 1;
			} else if (pszDisk.startsWith("fs0:/")) {
				return 1;
			} else if (pszDisk.startsWith("fs1:/")) {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					return 1;
				}
			} else if (pszDisk.startsWith("fs2:/")) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * @par ******** 注意 ******** <br>
	 *      文件系统目前共有四种类型: <br>
	 *      fs0:/代表应用程序内部存储器 <br>
	 *      fs1:/代表扩展存储器,建议在操作系统扩展存储器(T卡)里建立goluk目录作为腾瑞万里扩展存储器 <br>
	 *      fs2:/应用程序缓存存储目录，主要用于存储缓存数据，此目录内文件有可能被系统清除（如ios5系统）
	 *      fs3:/系统媒体文件存储目录，其下又分为： fs3:/picture/ 映射到系统图片存储目录 fs3:/audio/
	 *      映射到系统的音频文件存储目录 fs3:/video/ 映射到系统的视频文件存储目录
	 *      备注：系统媒体文件存储目录暂时固定只支持为以上3种媒体目录
	 *      如果路径没有fs0:/、fs1:/、fs2:/、fs3:\的前缀,则默认为fs0:/ <br>
	 * 
	 * @par 代码示例 fs0:/first/test1.dat-->应用程序内部存储根目录下first目录中的test1.dat文件 <br>
	 *      fs1:/second/test2.dat-->扩展存储器根目录下second目录中的test2.dat文件 <br>
	 *      fs2:/img/navidog.png-->应用程序缓存存储目录根目录下img目录中的navidog.png文件 <br>
	 *      fs3:/picture/p1.png-->系统媒体目录的图片存储目录下p1.png文件
	 *      fs3:/audio/a1.mp3-->系统媒体目录的音频存储目录下a1.mp3文件
	 *      fs3:/video/v1.avi-->系统媒体目录的视频存储目录下v1.avi文件
	 *      fourth/test4.dat-->没有固定前缀，则默认指向应用程序内部存储根目录下fourth目录中的test4.dat文件 <br>
	 */
	public static String libToJavaPath(String filename) {

		filename = winToLinuxPath(filename);

		String filepath = null;

		boolean is = filename.contains(":");
		if (is) {
			String substr = filename.substring(0, 3);
			String newfilename = filename.substring(4, filename.length());

			if (substr.equals("fs0")) {
				filepath = Const.getAppContext().getFilesDir().getPath() + newfilename;
			} else if (substr.equals("fs1")) {

				String exterStoragePath = Environment.getExternalStorageDirectory().getPath();
				// if (exterStoragePath.contains("sdcard0")) {
				// exterStoragePath = exterStoragePath.replace("sdcard0",
				// "sdcard1");
				// }
				//
				filepath = exterStoragePath + PATH_FS1 + newfilename;

			} else if (substr.equals("fs2")) {
				filepath = Const.getAppContext().getCacheDir().getPath() + newfilename;
			} else if (substr.equals("fs3")) {

			} else if (substr.equals("fs4")) {
				filepath = Const.getAppContext().getFilesDir().getPath() + PATH_FS4 + newfilename;
			} else if (substr.equals("fs5")) {
				filepath = "/data/data/" + Const.getAppContext().getPackageName() + newfilename;
			} else if (substr.equals("fs6")) {
				filepath = filename.substring(5, filename.length());
			}
			
			substr = null;
			newfilename = null;
		} else {
			if (filename.startsWith("./")) {
				filename = filename.substring(1);
			}
			filepath = Const.getAppContext().getFilesDir().getPath() + (filename.startsWith("/") ? "" : "/") + filename;
		}
		filename = null;

		// shizy 20120424 处理路径中包含../的情况
		while (filepath.contains("../")) {
			int start = filepath.indexOf("../");
			if (start <= 0) {
				break;
			}
			String str1 = filepath.substring(0, start - 1);
			String str2 = filepath.substring(start + 3);
			filepath = str1.substring(0, str1.lastIndexOf("/") + 1) + str2;
		}
		return filepath;
	}

	/**
	 * @param javaFilePath
	 * @return /data/data/..... /sdcard/.... /data/data/tiroscomcn/.... ....
	 */
	public static String javaToLibPath(String javaFilePath) {
		// GolukDebugUtils.i("TTS", "TTTTTTTTT javaToLibPath  0 javaFilePath =  " +
		// javaFilePath);
		String fs0Path = Const.getAppContext().getFilesDir().getPath();
		String fs1Path = Environment.getExternalStorageDirectory().getPath() + PATH_FS1;
		String fs2Path = Const.getAppContext().getCacheDir().getPath();
		String fs3Path = "fs3:/";
		String fs4Path = Const.getAppContext().getFilesDir().getPath() + PATH_FS4;

		String filePath = null;

		if (javaFilePath.startsWith(fs4Path)) {
			filePath = "fs4:" + javaFilePath.substring(fs4Path.length(), javaFilePath.length());
		} else if (javaFilePath.startsWith(fs1Path)) {
			filePath = "fs1:" + javaFilePath.substring(fs1Path.length(), javaFilePath.length());
		} else if (javaFilePath.startsWith(fs2Path)) {
			filePath = "fs2:" + javaFilePath.substring(fs2Path.length(), javaFilePath.length());
		} else if (javaFilePath.startsWith(fs3Path)) {
			filePath = "fs3:" + javaFilePath.substring(fs3Path.length(), javaFilePath.length());
		} else if (javaFilePath.startsWith(fs0Path)) {
			filePath = "fs0:" + javaFilePath.substring(fs0Path.length(), javaFilePath.length());
		}
		// GolukDebugUtils.i("TTS", "TTTTTTTTT javaToLibPath  1 filePath =  " + filePath);
		return filePath;
	}

	public static String getExternalStorageDirectory() {
		String exterStoragePath = Environment.getExternalStorageDirectory().getPath();
		if (exterStoragePath.contains("sdcard0")) {
			exterStoragePath = exterStoragePath.replace("sdcard0", "sdcard1");
		}
		return exterStoragePath;
	}

	/**
	 * 将window系统的路径转换为linux系统的路径
	 * 
	 * @param file
	 * @return
	 */
	public static String winToLinuxPath(String file) {
		return file.replace('\\', '/');
	}

	/**
	 * 将Linux路径转换为windows路径
	 * 
	 * @param file
	 * @return
	 */
	public static String linuxToWinPath(String file) {
		return file.replace('/', '\\');
	}

	public static void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] flist = file.listFiles();
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isDirectory()) {
					deleteFile(flist[i]);
				} else {
					flist[i].delete();
				}
			}
			file.delete();
		}
	}
}
