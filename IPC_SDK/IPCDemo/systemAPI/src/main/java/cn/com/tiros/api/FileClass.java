package cn.com.tiros.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.os.StatFs;
import cn.com.tiros.debug.GolukDebugUtils;

public class FileClass {

	public static final int SYS_OFM_READ = 0;
	public static final int SYS_OFM_READWRITE = 1;
	public static final int SYS_OFM_APPEND = 2;
	public static final int SYS_OFM_CREATE = 3;

	public static final int SYS_FST_START = 0;
	public static final int SYS_FST_END = 1;
	public static final int SYS_FST_CURRENT_DOWN = 2;
	public static final int SYS_FST_CURRENT_UP = 3;

	public static final String PATH_FS1 = "/goluk";
	public static final String PATH_FS4 = "/Cloud";

	private RandomAccessFile mRandomFile = null;
	private InputStream mAssertInputStream = null;

	public int sys_fopen(String fileName, int mode) {
		String filePath = FileUtils.libToJavaPath(fileName);
		Boolean bAssert = false;
		File file = null;
		if (fileName.substring(0, 3).equals("fs6")) {// 读取Assert文件夹
			bAssert = true;
		} else {
			filePath = FileUtils.libToJavaPath(fileName);
			file = new File(filePath);
		}
		switch (mode) {
		case SYS_OFM_READ:// 只读
			if (bAssert) {
				try {
					mAssertInputStream = Const.getAppContext().getAssets().open(filePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				}
			} else {
				if (file.exists()) {
					try {
						mRandomFile = new RandomAccessFile(file, "r");
					} catch (FileNotFoundException e) {
						return 0;
					}
				} else {
					return 0;
				}
			}
			break;
		case SYS_OFM_READWRITE:// 读写
			if (file.exists()) {
				try {
					mRandomFile = new RandomAccessFile(file, "rw");
				} catch (Exception e) {
					return 0;
				}
			} else {
				return 0;
			}
			break;
		case SYS_OFM_APPEND:// 追加
			if (file.exists()) {
				try {
					mRandomFile = new RandomAccessFile(file, "rw");
					mRandomFile.seek(file.length());
				} catch (Exception e) {
					return 0;
				}
			} else {
				return 0;
			}
			break;
		case SYS_OFM_CREATE:// 创建
			if (file.exists()) {
				return 0;
			}

			String fileDir = null;

			for (int i = filePath.length() - 1; i >= 0; i--) {
				if (filePath.charAt(i) == '/') {
					fileDir = filePath.substring(0, i);
					break;
				}
			}

			File f = new File(fileDir);
			if (!f.exists()) {
				f.mkdirs();
			}
			fileDir = null;
			f = null;
			try {
				mRandomFile = new RandomAccessFile(file, "rw");
			} catch (FileNotFoundException e) {
				return 0;
			}
			break;
		}
		file = null;
		filePath = null;
		return 1;
	}

	public void sys_fclose() {
		try {
			if (mRandomFile != null) {
				mRandomFile.close();
			}
			if (mAssertInputStream != null) {
				mAssertInputStream.close();
			}
		} catch (IOException e) {
		}
		mRandomFile = null;
		mAssertInputStream = null;
	}

	public int sys_fchsize(int size) {
		if (mRandomFile == null) {
			return 0;
		}
		try {
			mRandomFile.setLength(size);
		} catch (IOException e) {
		}
		return size;
	}

	public int sys_fread(byte[] b, int bufSize) {
		if (mRandomFile == null && mAssertInputStream == null) {
			return 0;
		}
		int size = 0;
		if (mRandomFile != null) {
			try {
				size = mRandomFile.read(b, 0, bufSize);
			} catch (IndexOutOfBoundsException e) {
			} catch (IOException e) {
			}
		} else {
			try {
				size = mAssertInputStream.read(b);
				// String s = new String(b);
				// mAssertInputStream.skip(size);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				GolukDebugUtils.e("", "wwwwww sys_fread---------mAssertInputStream  -error--1");
			}
		}
		if (size < 0) {
			size = 0;
		}
		return size;
	}

	/**
	 * @brief 定位文件读写位置
	 * @param[in] seekType - 文件定位类型
	 * @param[in] moveDistance - 从定位类型约定位置的位移
	 * @return - 当前文件读写位置,FST_START的偏移量
	 */
	public synchronized int sys_fseek(int type, int moveDis) {
		if (mRandomFile == null) {
			return 0;
		}
		int oldIndex = 0;
		int seekIndex = 0;
		try {
			oldIndex = (int) mRandomFile.getFilePointer();
		} catch (IOException e) {
		}
		seekIndex = oldIndex;
		if (type == SYS_FST_CURRENT_DOWN) {
			try {
				seekIndex += moveDis;
				if (seekIndex > (int) mRandomFile.length()) {
					seekIndex = (int) mRandomFile.length();
				}
			} catch (IOException e) {
			}
		} else if (type == SYS_FST_CURRENT_UP) {
			seekIndex -= moveDis;
			if (seekIndex < 0) {
				seekIndex = 0;
			}
		} else if (type == SYS_FST_START) {
			seekIndex = moveDis;
		} else if (type == SYS_FST_END) {
			try {
				seekIndex = (int) mRandomFile.length();

			} catch (IOException e) {
			}
		}
		try {
			mRandomFile.seek(seekIndex);
		} catch (IOException e) {
		}
		return seekIndex;
	}

	/**
	 * 写文件
	 **/
	public int sys_fwrite(byte[] pvBuf, int bufSize) {
		if (mRandomFile == null) {
			return 0;
		}
		try {
			mRandomFile.write(pvBuf, 0, bufSize);
		} catch (Exception e) {
			return 0;
		}
		pvBuf = null;
		return bufSize;
	}

	/**
	 * 文件是否存在
	 * 
	 * @param fileName
	 * @return
	 */
	public static int sys_fexist(String fileName) {
		String filePath = FileUtils.libToJavaPath(fileName);
		if (fileName.substring(0, 3).equals("fs6")) {// assertFile
			try {
				int rev = 0;
				InputStream is = Const.getAppContext().getAssets().open(filePath);
				if (is != null) {
					rev = 1;
				}
				is.close();
				return rev;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				filePath = null;
				return 0;
			}
		}

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
				String filePath = FileUtils.libToJavaPath(pszDisk);
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
			String filePath = FileUtils.libToJavaPath(pszDisk);
			try {
				sf = new StatFs(filePath);
			} catch (Exception e) {
			}
		}
		return ((long) sf.getBlockCount() * (long) sf.getBlockSize()) / FileUtils.SYS_MEMORY_UNIT;
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
				String filePath = FileUtils.libToJavaPath(pszDisk);
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
			String filePath = FileUtils.libToJavaPath(pszDisk);
			try {
				sf = new StatFs(filePath);
			} catch (Exception e) {
			}
		}
		return ((long) sf.getAvailableBlocks() * (long) sf.getBlockSize()) / FileUtils.SYS_MEMORY_UNIT;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param fileName
	 * @return
	 */
	public static int sys_fgetsize(String fileName) {
		String filePath = FileUtils.libToJavaPath(fileName);
		if (fileName.substring(0, 3).equals("fs6")) {// assertFile
			try {
				InputStream is = Const.getAppContext().getAssets().open(filePath);
				int length = is.available();
				is.close();
				return length;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
		}
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
		String filePath = FileUtils.libToJavaPath(dirName);
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
		String filePath = FileUtils.libToJavaPath(fileName);
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
		String oldPath = FileUtils.libToJavaPath(oldName);
		String newPath = FileUtils.libToJavaPath(newName);
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
		String filePath = FileUtils.libToJavaPath(dirName);
		File file = new File(filePath);
		if (file.exists() && file.isDirectory()) {
			FileUtils.deleteFile(file);
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
			if (pszDisk.startsWith("fs6:/")) {
				return 1;
			} else if (pszDisk.startsWith("fs4:/")) {
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
	 * 相对路径转绝对路径
	 * 
	 * @param javaFilePath
	 *            相对路径
	 * @return /data/data/..... /sdcard/.... /data/data/tiroscomcn/.... ....
	 * @author qianwei
	 * @date 2014-8-7
	 */
	public static String sys_flibToJavaPath(String filename) {
		String path = FileUtils.libToJavaPath(filename);
		return path;
	}
	
	
	/**
	 * 修改文件时间属性
	 * 
	 * @param filename 相对路径
	 * @return 成功1 失败0
	 */
	public static int sys_fchangemodificationdate(String filename, int time) {
		String filePath = FileUtils.libToJavaPath(filename);
		File file = new File(filePath);
		if (file.exists())
		{
			long rtime = Long.parseLong(time +"000");
			if (file.setLastModified(rtime) == true)
				return 1;
		}
		file = null;
		return 0;
	}
}
