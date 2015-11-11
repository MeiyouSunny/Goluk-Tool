package cn.com.mobnote.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import cn.com.mobnote.application.GolukApplication;
import android.content.Context;
import android.content.SharedPreferences;

public class FileUtils {
	public static String APP_PREF_KEY = "goluk_android";

	/** 是否显示活动提示 */
	public static final String SHOW_PROMOTION_POPUP_FLAG = "show_promotion_popup";
	
	/** 活动列表 */
	public static final String PROMOTION_LIST_STRING = "promotion_list_string";
	public static void remove(String name) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(name);
		editor.commit();
	}

	public static void saveString(String name, String value) {
		Context context = GolukApplication.getInstance();

		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public static void asyncSaveString(String name, String value) {
		Context context = GolukApplication.getInstance();

		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(name, value);
		editor.apply();
	}

	public static String loadString(String key, String defaultValue) {
		Context context = GolukApplication.getInstance();

		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		return sp.getString(key, defaultValue);
	}

	public static void saveInt(String name, int value) {
		Context context = GolukApplication.getInstance();

		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(name, value);
		editor.commit();
	}

	public static int loadInt(String name, int defaultValue) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		return sp.getInt(name, defaultValue);
	}


	public static void saveLong(String name, long value) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(name, value);
		editor.commit();
	}

	public static long loadLong(String name, long defaultValue) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		return sp.getLong(name, defaultValue);
	}

	public static void saveBoolean(String name, boolean value) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp =context.getSharedPreferences(APP_PREF_KEY,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public static boolean loadBoolean(String name, boolean defaultvalue) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY,
				Context.MODE_PRIVATE);
		return sp.getBoolean(name, defaultvalue);
	}

	public static void clearValue(String key) {
		Context context = GolukApplication.getInstance();
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(key);
		editor.commit();
	}

	/**
	 * Write a properties file
	 *
	 * @param path
	 *            String object handle for file path
	 * @param file
	 *            String object handle for file name
	 * @param properties
	 *            Properties object handle for file content
	 * @return operate status
	 */
	public static boolean writeFileProperties(String path, String file, Properties properties) {
		try {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			return writeFileProperties(path + file, properties);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Write a properties file
	 *
	 * @param path
	 *            String object handle for file path including file name
	 * @param properties
	 *            Properties object handle for file content
	 * @return operate status
	 */
	public static boolean writeFileProperties(String path, Properties properties) {
		FileOutputStream outputStream = null;
		try {
			File fileHandle = new File(path);
			if (fileHandle.exists()) {
				fileHandle.delete();
			}

			outputStream = new FileOutputStream(fileHandle);
			properties.store(outputStream, "");

			outputStream.flush();
			outputStream.close();
		} catch (Exception e1) {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (Exception e2) {
				return false;
			}
			return false;
		}

		return true;
	}

	/**
	 * Read a properties file
	 *
	 * @param path
	 *            the file path including the file name
	 * @return Properties object
	 */
	public static Properties readFileProperties(String path) {
		Properties properties = new Properties();

		FileInputStream inputStream = null;
		try {
			File file = new File(path);
			if (!file.exists()) {
				return null;
			}

			inputStream = new FileInputStream(file);
			properties.load(inputStream);

			inputStream.close();
		} catch (Exception e1) {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e2) {
				return null;
			}
			return null;
		}

		return properties;
	}

	/**
	 * Write a byte file
	 *
	 * @param path
	 *            the path for the file
	 * @param file
	 *            file name
	 * @param content
	 *            file content
	 * @param isAppend
	 *            true - can append into the same file false - can not append
	 * @return operate status
	 */
	public static boolean writeFileBytes(String path, String file, String content, boolean isAppend) {
		FileOutputStream outputStream = null;
		try {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File fileHandle = new File(path, file);
			if (!isAppend && fileHandle.exists()) {
				fileHandle.delete();
			}

			outputStream = new FileOutputStream(fileHandle);
			outputStream.write(content.getBytes());

			outputStream.flush();
			outputStream.close();
		} catch (Exception e1) {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (Exception e2) {
				return false;
			}
			return false;
		}

		return true;
	}

	/**
	 * Read a byte file
	 *
	 * @param path
	 *            the file path including the file name
	 * @return content string
	 */
	public static String readFileBytes(String path) {
		StringBuilder content = new StringBuilder("");
		FileInputStream inputStream = null;
		try {
			File file = new File(path);
			if (!file.exists()) {
				return null;
			}

			inputStream = new FileInputStream(file);

			byte[] buffer = new byte[256];
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
				content.append(new String(buffer, 0, len));
			}

			inputStream.close();
		} catch (Exception e1) {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e2) {
				return null;
			}
			return null;
		}

		return content.toString();
	}
}
