package com.rd.veuisdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import com.rd.veuisdk.manager.CameraConfiguration;
import com.rd.veuisdk.manager.CompressConfiguration;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.FaceuConfig;
import com.rd.veuisdk.manager.TrimConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.FileLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * RdVEUISdk配置服务<br>
 */
public class SdkService {
	private ExportConfiguration mExportConfig;
	private UIConfiguration mUIConfig;
	private CameraConfiguration mCameraConfig;
	private CompressConfiguration mCompressConfig;
	private TrimConfiguration mTrimConfig;
	private FaceuConfig mFaceConfig;

	SdkService() {
		mExportConfig = (ExportConfiguration) restoreObject(
				AppConfiguration.getSharedPreferences(),
				AppConfiguration.EXPORT_CONFIGURATION_KEY,
				new ExportConfiguration.Builder().get());
		mUIConfig = (UIConfiguration) restoreObject(
				AppConfiguration.getSharedPreferences(),
				AppConfiguration.UI_CONFIGURATION_KEY,
				new UIConfiguration.Builder().get());
		mCameraConfig = (CameraConfiguration) restoreObject(
				AppConfiguration.getSharedPreferences(),
				AppConfiguration.CAMERA_CONFIGURATION_KEY,
				new CameraConfiguration.Builder().get());
		mCompressConfig = (CompressConfiguration) restoreObject(
				AppConfiguration.getSharedPreferences(),
				AppConfiguration.COMPRESS_CONFIGURATION_KEY,
				new CompressConfiguration.Builder().get());
		mFaceConfig = (FaceuConfig) restoreObject(
				AppConfiguration.getSharedPreferences(),
				AppConfiguration.FACEU_CONFIGURATION_KEY, new FaceuConfig());
		mTrimConfig = (TrimConfiguration) restoreObject(
				AppConfiguration.getSharedPreferences(),
				AppConfiguration.TRIM_CONFIGURATION_KEY,
				new TrimConfiguration.Builder().get());
	}

	/**
	 * 初始化编辑导出、编辑界面、拍摄录制配置项
	 * 
	 * @param exportConfig
	 *            编辑导出配置项
	 * @param uiConfig
	 *            编辑界面配置项
	 * @param cameraConfig
	 *            拍摄录制配置项
	 */
	public void initConfiguration(ExportConfiguration exportConfig,
			UIConfiguration uiConfig, CameraConfiguration cameraConfig) {

		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.EXPORT_CONFIGURATION_KEY, exportConfig);
		if (null != exportConfig) {
			mExportConfig = exportConfig;
		}
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.UI_CONFIGURATION_KEY, uiConfig);
		if (null != uiConfig) {
			mUIConfig = uiConfig;
		}
		initConfiguration(cameraConfig);
	}

	/**
	 * 初始化录制配置项
	 * 
	 * @param cameraConfig
	 *            拍摄录制配置项
	 */
	public void initConfiguration(CameraConfiguration cameraConfig) {
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.CAMERA_CONFIGURATION_KEY, cameraConfig);
		if (null != cameraConfig) {
			mCameraConfig = cameraConfig;
		}
	}

	/**
	 * "人脸贴纸"配置
	 * 
	 * @param config
	 */
	public void initFaceuConfig(FaceuConfig config) {
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.FACEU_CONFIGURATION_KEY, config);
		if (null != config) {
			mFaceConfig = config;
		}
	}

	/**
	 * 初始化视频编辑导出、视频编辑界面配置项
	 * 
	 * @param exportConfig
	 *            视频编辑导出配置项
	 * @param uiConfig
	 *            视频编辑界面配置项
	 */
	public void initConfiguration(ExportConfiguration exportConfig,
			UIConfiguration uiConfig) {
		initConfiguration(exportConfig, uiConfig, null);
	}

	/**
	 * 初始化压缩视频配置
	 * 
	 * @param compressConfig
	 *            压缩视频配置项
	 */
	public void initCompressConfiguration(CompressConfiguration compressConfig) {
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.COMPRESS_CONFIGURATION_KEY, compressConfig);
		if (null != compressConfig) {
			mCompressConfig = compressConfig;
		}
	}

	/**
	 * 视频截取配置
	 * 
	 * @param config
	 */
	public void initTrimConfiguration(TrimConfiguration config) {
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.TRIM_CONFIGURATION_KEY, config);
		if (null != config) {
			mTrimConfig = config;
		}
	}

	CameraConfiguration getCameraConfig() {
		return mCameraConfig;
	}

	ExportConfiguration getExportConfig() {
		return mExportConfig;
	}

	UIConfiguration getUIConfig() {
		return mUIConfig;
	}

	CompressConfiguration getCompressConfig() {
		return mCompressConfig;
	}

	FaceuConfig getFaceUnityConfig() {
		return mFaceConfig;
	}

	TrimConfiguration getTrimConfig() {
		return mTrimConfig;
	}

	void saveConfigStatus(String key, Context context) {
		Parcelable objConfiguration;
		if (key.equals(AppConfiguration.CAMERA_CONFIGURATION_KEY)) {
			objConfiguration = mCameraConfig;
		} else if (key.equals(AppConfiguration.UI_CONFIGURATION_KEY)) {
			objConfiguration = mUIConfig;
		} else if (key.equals(AppConfiguration.EXPORT_CONFIGURATION_KEY)) {
			objConfiguration = mExportConfig;
		} else if (key.equals(AppConfiguration.COMPRESS_CONFIGURATION_KEY)) {
			objConfiguration = mCompressConfig;
		} else if (key.equals(AppConfiguration.FACEU_CONFIGURATION_KEY)) {
			objConfiguration = mFaceConfig;
		} else if (key.equals(AppConfiguration.TRIM_CONFIGURATION_KEY)) {
			objConfiguration = mTrimConfig;
		} else {
			return;
		}
		saveObject(AppConfiguration.getSharedPreferences(), key,
				objConfiguration);
	}

	/**
	 * 持久化保存对象
	 * 
	 * @param sp
	 *            保存对象关联的SharedPreferences
	 * @param key
	 *            保存对象关联的key
	 * @param object
	 *            支持Parcelable的对象
	 * @return true代表保存成功
	 */
	public static <T extends Parcelable> boolean saveObject(
			SharedPreferences sp, String key, T object) {
		try {
			if (sp == null) {
				return false;
			}
			if (object == null) {
				sp.edit().remove(key).commit();
				return false;
			}
			Editor ed = sp.edit();
			ed.putString(key, objectToString(object));
			ed.commit();
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	private static <T extends Parcelable> String objectToString(T object)
			throws IOException {
		Parcel parcel = Parcel.obtain();
		parcel.writeParcelable(object, 0);
		byte[] data = parcel.marshall();
		parcel.recycle();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
		b64.write(data);
		try {
			return out.toString();
		} finally {
			b64.close();
			out.close();
		}
	}

	/**
	 * 还原持久化保存的对象
	 * 
	 * @param sp
	 *            保存对象关联的SharedPreferences
	 * @param key
	 *            保存对象关联的key
	 * @param defaultObject
	 *            如果key不存在时，返回默认的Parcelable的对象
	 * @return 返回还原的持久化保存的对象
	 */
	public static <T extends Parcelable> T restoreObject(SharedPreferences sp,
			String key, T defaultObject) {
		if (sp == null) {
			return defaultObject;
		}
		byte[] bytes = sp.getString(key, "").getBytes();
		if (bytes.length == 0) {
			return defaultObject;
		}
		Parcel parcel = null;
		ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
		Base64InputStream base64InputStream = new Base64InputStream(byteArray,
				Base64.DEFAULT);

		try {
			base64InputStream.skip(0);
			if (base64InputStream.available() > 0) {
				byte[] unBase64Bytes = new byte[base64InputStream.available()];
				base64InputStream.read(unBase64Bytes, 0, unBase64Bytes.length);
				parcel = Parcel.obtain();
				parcel.unmarshall(unBase64Bytes, 0, unBase64Bytes.length);
				parcel.setDataPosition(0);
				T result = parcel.readParcelable(SdkService.class
						.getClassLoader());
				return result != null ? result : defaultObject;
			} else {
				return defaultObject;
			}
		} catch (Exception ex) {
			return defaultObject;
		} finally {
			if (byteArray != null) {
				try {
					byteArray.close();
				} catch (IOException e) {
				}
			}
			if (base64InputStream != null) {
				try {
					base64InputStream.close();
				} catch (IOException e) {
				}
			}
			if (null != parcel) {
				parcel.recycle();
			}
		}
	}

	void reset() {
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.EXPORT_CONFIGURATION_KEY, null);
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.UI_CONFIGURATION_KEY, null);
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.CAMERA_CONFIGURATION_KEY, null);
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.COMPRESS_CONFIGURATION_KEY, null);
		saveObject(AppConfiguration.getSharedPreferences(),
				AppConfiguration.TRIM_CONFIGURATION_KEY, null);
	}

	static <T extends Parcelable> void writeFileLog(String prefix, T obj) {
		try {
			FileLog.writeLog(prefix + objectToString(obj));
		} catch (IOException e) {
		}
	}
}
