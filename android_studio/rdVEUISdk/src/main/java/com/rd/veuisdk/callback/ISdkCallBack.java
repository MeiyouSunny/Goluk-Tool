package com.rd.veuisdk.callback;

import android.content.Context;

import com.rd.veuisdk.SdkEntry;

/**
 * RdVEUISdk回调接口
 */
public interface ISdkCallBack {
	/**
	 * 目标视频的路径
	 * 
	 * @param context
	 *            应用上下文
	 * @param exportType
	 *            回调类型 来自简单录制 {@link SdkEntry#CAMERA_EXPORT}<br>
	 *            来自录制编辑{@link SdkEntry#CAMERA_EDIT_EXPORT}<br>
	 *            来自编辑导出{@link SdkEntry#EDIT_EXPORT}<br>
	 *            来自普通截取视频导出{@link SdkEntry#TRIMVIDEO_EXPORT}<br>
	 *            来自定长截取视频导出{@link SdkEntry#TRIMVIDEO_DURATION_EXPORT}<br>
	 * @param videoPath
	 */
	 void onGetVideoPath(Context context, int exportType, String videoPath);

	/**
	 * 响应截取视频时间
	 * 
	 * @param context
	 *            应用上下文
	 * @param exportType
	 *            回调类型 来自普通截取视频的时间导出{@link SdkEntry#TRIMVIDEO_EXPORT}<br>
	 *            来自定长截取视频的时间导出{@link SdkEntry#TRIMVIDEO_DURATION_EXPORT}<br>
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 */
	void onGetVideoTrimTime(Context context, int exportType,
								   int startTime, int endTime);

	/**
	 * 响应确认截取按钮
	 * 
	 * @param context
	 *            应用上下文
	 * @param exportType
	 *            来自普通截取的确认 {@link SdkEntry#TRIMVIDEO_EXPORT}<br>
	 *            来自定长截取的确认 {@link SdkEntry#TRIMVIDEO_DURATION_EXPORT}<br>
	 */
	void onGetVideoTrim(Context context, int exportType);

	/**
	 * 响应进入相册（只显示照片、图片）
	 * 
	 * @param context
	 *            应用上下文
	 */
	void onGetPhoto(Context context);

	/**
	 * 响应进入相册（只显示视频）
	 * 
	 * @param context
	 *            应用上下文
	 */
	void onGetVideo(Context context);
}
