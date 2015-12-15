package cn.com.mobnote.golukmobile.carrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;

import com.rd.car.modal.CarRecoderConfig;
import com.rd.car.modal.VideoConfiguration;

/**
 * 读取本地配置
 */
public class PreferencesReader {
	public final static String PREFERENCES_NAME = "CarRecorderPreferaces";

	/**
	 * 本地分辨率
	 */
	public static final String DPI_LOCAL = "dpi_local";
	/**
	 * 本地帧率
	 */
	public static final String FRAME_LOCAL = "frame_local";
	/**
	 * 本地码率
	 */
	public static final String MA_LOCAL = "ma_local";
	/**
	 * 自动检测sd卡剩余空间可录制的视频段数
	 */
	public static final String MAXSIZE_LOCAL = "maxsize_local";
	/**
	 * 每段视频的录制时间 单位:分
	 */
	public static final String ITEM_TIME_LOCAL = "itemtime_local";
	/**
	 * 直播分辨率
	 */
	public static final String DPI_LIVE = "dpi_live";
	/**
	 * 直播帧率
	 */
	public static final String FRAME_LIVE = "frame_live";
	/**
	 * /直播码率
	 */
	public static final String MA_LIVE = "ma_live";
	/**
	 * 直播地址
	 */
	public static final String URL_LIVE = "url_live";
	/**
	 * 直播硬件编码
	 */
	public static final String HW_LIVE = "hw_live";
	private static final String TAG = "PreferencesReader";
	private CarRecoderConfig m_crcConfig;

	/**
	 * constructor
	 * 
	 * @param c
	 */
	public PreferencesReader(Context c, boolean isDefault) {
		SharedPreferences sp = c.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

		int mdpi_local = Integer.parseInt(sp.getString(DPI_LOCAL, "1"));
		int mframe_local = Integer.parseInt(sp.getString(FRAME_LOCAL, "0"));
		int mma_local = Integer.parseInt(sp.getString(MA_LOCAL, "1"));
		int mdpi_live = Integer.parseInt(sp.getString(DPI_LIVE, "2"));
		int mframe_live = Integer.parseInt(sp.getString(FRAME_LIVE, "0"));
		int mma_live = Integer.parseInt(sp.getString(MA_LIVE, "0"));

		m_crcConfig = new CarRecoderConfig();
		VideoConfiguration vcLive = getLive(c, mdpi_live, mframe_live, mma_live);
		m_crcConfig.setLiveVideoConfig(vcLive); // 直播视频配置
		Log.d(TAG, "live config:" + vcLive.toString());
		VideoConfiguration vcLocal = getLocal(c, mdpi_local, mframe_local, mma_local);
		m_crcConfig.setLocalVideoConfig(vcLocal); // 本地视频配置
		Log.d(TAG, "local config:" + vcLocal.toString());

		String strRtmpLive = sp.getString(PreferencesReader.URL_LIVE, PlayUrlManager.DEFAULT_LIVE_URL);
		m_crcConfig.setLiveUrl(strRtmpLive); // 直播路径

		int mIndex_MaxSize = Integer.parseInt(sp.getString(MAXSIZE_LOCAL, "0"));
		long mItmeTime = Long.parseLong(sp.getString(ITEM_TIME_LOCAL, "1")); // 分段时每段的时长，单位分钟
		int maxSize = Integer.parseInt(c.getResources().getStringArray(R.array.list_maxsize_key_local)[mIndex_MaxSize]);
		m_crcConfig.setMaxSize(maxSize); // 控制剩余空间可录制的视频段数
		m_crcConfig.setItemTime(mItmeTime * 60000); // 分段时每段的时长，实际配置单位为ms
		m_crcConfig.setLiveEnableHW(Integer.parseInt(sp.getString(PreferencesReader.HW_LIVE, "0")) == 1);
		// 直播缓冲时间
		m_crcConfig.setLiveBufferTime(0);
		// 指定RTSP源地址
		if (isDefault) {
			m_crcConfig.setSourceRTSPUrl(PlayUrlManager.DEFAULT_RTSP_URL);
		} else {
			m_crcConfig.setSourceRTSPUrl(PlayUrlManager.getRtspUrl());
		}

	}

	/**
	 * 获取配置值
	 * 
	 * @return
	 */
	public CarRecoderConfig getConfig() {
		return m_crcConfig;
	}

	private VideoConfiguration getLive(Context c, int dpi_live, int frame_live, int ma_live) {
		VideoConfiguration liveVideoConfig = new VideoConfiguration();
		// liveVideoConfig.setVideoFrameRate(c.getResources().getIntArray(
		// R.array.list_frame_source_live)[frame_live]);
		// // 1K = 1024 bytes
		// liveVideoConfig.setVideoEncodingBitRate(c.getResources().getIntArray(
		// R.array.list_ma_source_live)[ma_live] * 1024);
		//
		// int width = 640;
		// int height = 480;
		// if (dpi_live == 0) {
		// width = 360;
		// height = 240;
		// } else if (dpi_live == 1) {
		// width = 480;
		// height = 360;
		// } else if (dpi_live == 3) {
		// width = 1280;
		// height = 720;
		// }
		//
		// liveVideoConfig.setVideoSize(width, height);

		liveVideoConfig.setVideoFrameRate(24);
		liveVideoConfig.setVideoEncodingBitRate(300 * 1024);

		int width = 1280;
		int height = 720;

		liveVideoConfig.setVideoSize(width, height);
		return liveVideoConfig;
	}

	private VideoConfiguration getLocal(Context c, int dpi_local, int frame_local, int ma_local) {
		VideoConfiguration localVideoConfig = new VideoConfiguration();

		localVideoConfig.setVideoFrameRate(c.getResources().getIntArray(R.array.list_frame_source_local)[frame_local]);
		localVideoConfig.setVideoFrameRate(c.getResources().getIntArray(R.array.list_frame_source_local)[frame_local]);
		// 1M = 1024*1024 bytes
		localVideoConfig
				.setVideoEncodingBitRate(c.getResources().getIntArray(R.array.list_ma_source_local)[ma_local] * 1024 * 1024);
		int width = 1920;
		int height = 1080;
		if (dpi_local == 1) { // 720p
			width = 1280;
			height = 720;
		} else if (dpi_local == 2) { // 480p
			width = 640;
			height = 480;
		}

		localVideoConfig.setVideoSize(width, height);
		return localVideoConfig;
	}

}
