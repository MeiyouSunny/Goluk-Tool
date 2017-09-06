package com.rd.veuisdk.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;

/**
 * 存储工具
 * 
 * @author abreal
 */
public class StorageUtils {
	/**
	 * 当前存储路径
	 */
	private static String m_strCurrentStorageDir = null;
	/**
	 * 存放实的路径的集合
	 */
	private static ArrayList<String> mMounts;
	/**
	 * 判断路径是否已被切换
	 */
	private static boolean isSwitching;

	/**
	 * 当前是否使用内置存储
	 * 
	 * @return
	 */
	public static boolean useInternalStorage() {
		return getStorageDirectory().equals(m_strCurrentStorageDir);
	}

	/**
	 * @return True if the external storage is available. False otherwise.
	 */
	public static boolean isAvailable(boolean requireWriteAccess) {
		return isAvailable(getStorageDirectory(), requireWriteAccess);
	}

	private static boolean isAvailable(String strSdcardPath,
			boolean requireWriteAccess) {
		if (strSdcardPath.equals(m_strCurrentStorageDir)) {
			if (requireWriteAccess) {
				boolean writable = checkFsWritable(strSdcardPath);
				return writable;
			} else {
				File root = new File(strSdcardPath);
				return root.exists() && root.isDirectory() && root.canWrite();
			}
		} else {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				if (requireWriteAccess) {
					boolean writable = checkFsWritable(strSdcardPath);
					return writable;
				} else {
					return true;
				}
			} else if (!requireWriteAccess
					&& Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				return true;
			}
			return false;
		}
	}

	private static boolean checkFsWritable(String storageDirectory) {
		// Create a temporary file to see whether a volume is really writeable.
		// It's important not to put it in the root directory which may have a
		// limit on the number of files.
		String directoryName = storageDirectory.toString() + "/17rd";
		File directory = new File(directoryName);
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				return false;
			}
		}
		File f = new File(directoryName, ".probe");
		try {
			// Remove stale file if any
			if (f.exists()) {
				f.delete();
			}
			if (!f.createNewFile()) {
				return false;
			}
			f.delete();
			directory.delete();
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	/**
	 * 获取存储目录
	 * 
	 * @return
	 */
	public static String getStorageDirectory() {
		if (TextUtils.isEmpty(m_strCurrentStorageDir)) {
			return Environment.getExternalStorageDirectory().getPath() + "/";
		} else {
			return m_strCurrentStorageDir;
		}
	}

	/**
	 * 获取存储路径是否已被切换
	 * 
	 * @return
	 */
	public static boolean isSwitching() {
		return isSwitching;
	}

	/**
	 * 设置存储路径是否已被切换
	 * 
	 * @param isSwitching
	 */
	public static void setSwitching(boolean isSwitching) {
		StorageUtils.isSwitching = isSwitching;
	}

	/**
	 * 获取实在的存储路径集合
	 * 
	 * @return
	 */
	public static ArrayList<String> getmMounts() {
		return mMounts;
	}

	/**
	 * @return A map of all storage locations available
	 */
	public static void getAllStorageLocations(Context context) {
		boolean bRemoveFirstElement = false;
		mMounts = new ArrayList<String>(10);
		String strStoragePath = System.getenv("EXTERNAL_STORAGE");
		if (!TextUtils.isEmpty(strStoragePath)) {
			if (new File(strStoragePath).getParentFile().equals(
					Environment.getExternalStorageDirectory().getParentFile())) {
				mMounts.add(Environment.getExternalStorageDirectory()
						.toString());
			} else {
				mMounts.add(strStoragePath);
			}
		}
		strStoragePath = System.getenv("SECONDARY_STORAGE");
		if (!TextUtils.isEmpty(strStoragePath)) {
			// Kikat下副卡存储及插入的存储卡，只能写入到以下目录
			File fileSecondary = new File(strStoragePath, "/Android/data/"
					+ context.getPackageName() + "/files/");
			if (!fileSecondary.exists()) {
				fileSecondary.mkdirs();
			}
			strStoragePath = fileSecondary.getAbsolutePath();
			mMounts.add(strStoragePath);
		}
		if (mMounts.size() == 0) {
			List<String> mVold = new ArrayList<String>(10);
			mMounts.add("/mnt/sdcard");
			mVold.add("/mnt/sdcard");
			try {
				File mountFile = new File("/proc/mounts");
				if (mountFile.exists()) {
					Scanner scanner = new Scanner(mountFile);
					while (scanner.hasNext()) {
						String line = scanner.nextLine();
						if (line.startsWith("/dev/block/vold/")
								|| line.startsWith("/dev/fuse")) {
							String[] lineElements = line.split(" ");
							String element = lineElements[1];

							// don't add the default mount path
							// it's already in the list.
							if (!element.equals("/mnt/sdcard"))
								mMounts.add(element);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				File voldFile = new File("/system/etc/vold.fstab");
				if (voldFile.exists()) {
					Scanner scanner = new Scanner(voldFile);
					while (scanner.hasNext()) {
						String line = scanner.nextLine();
						if (line.startsWith("dev_mount")) {
							String[] lineElements = line.split(" ");
							String element = lineElements[2];

							if (element.contains(":"))
								element = element.substring(0,
										element.indexOf(":"));
							if (!element.equals("/mnt/sdcard"))
								mVold.add(element);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < mMounts.size(); i++) {
				String mount = mMounts.get(i);
				if (!mVold.contains(mount))
					mMounts.remove(i--);
			}
			mVold.clear();
			mVold = null;
			bRemoveFirstElement = true;
		}
		if (mMounts.size() > 1) {
			if (bRemoveFirstElement) {
				mMounts.remove(0);
			}
			for (int i = 0; i < mMounts.size(); i++) {
				// if (mMounts.size() > 1
				// && !CheckSDSize.getSDIsThanCurrentSize(mMounts.get(i))) {
				// continue;
				// }
				File root = new File(mMounts.get(i));
				if (checkPathCanWrite(root)) {
					String strExtrnalCanonicalPath = "/sdcard";
					try {
						strExtrnalCanonicalPath = new File("/sdcard")
								.getCanonicalPath();
					} catch (IOException e) {
					}
					setSwitching(!mMounts.get(i)
							.equals(strExtrnalCanonicalPath));// 设置是否已被切换
					m_strCurrentStorageDir = mMounts.get(i).endsWith("/") ? mMounts
							.get(i) : mMounts.get(i) + "/";
					break;
				}
			}
		}
	}

	private static boolean checkPathCanWrite(File file) {
		return file.exists() && file.isDirectory() && file.canWrite();
	}

	/**
	 * 获取外置存储root
	 * 
	 * @param type
	 * @return
	 */
	public static File getStoragePublicDirectory(String type) {
		return new File(getStorageDirectory(), type);
	}

	public static ArrayList<String> getSDPaths(Context context) {
		ArrayList<String> result = new ArrayList<String>();
		StorageManager sm = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);
		String[] paths = null;
		// 获取sdcard的路径：外置和内置
		try {
			paths = ((String[]) sm.getClass().getMethod("getVolumePaths")
					.invoke(sm));
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		} finally {
			if (paths == null) {
				paths = new String[] { Environment
						.getExternalStorageDirectory().getPath() };
			}
		}
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].equals(Environment.getExternalStorageDirectory()
					.getPath()) || "/storage/sdcard1".equals(paths[i])) {
				result.add(paths[i]);
			}
		}
		return result;
	}
}
