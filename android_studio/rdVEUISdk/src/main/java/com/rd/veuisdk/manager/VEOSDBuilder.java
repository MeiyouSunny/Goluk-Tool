package com.rd.veuisdk.manager;

import android.content.Context;

import com.rd.recorder.OSDBuilder;

/**
 * 水印支持开始录制中、录制结束时更换水印内容
 * 
 */
public abstract class VEOSDBuilder extends OSDBuilder {
	/**
	 * OSD状态
	 */
	public enum OSDState {
		/**
		 * 录制片头
		 */
		header,
		/**
		 * 录制进行中
		 */
		recording,
		/**
		 * 录制结束
		 */
		end;
	}

	/**
	 * 长方形正方形切换,重新设置水印位置
	 * 
	 * @param context
	 * @param isSquare
	 *            当前UI是否是:(1:1录制)
	 */
	public VEOSDBuilder(Context context, Boolean isSquare) {
		super(context);
	}

	public OSDState mState = OSDState.header;

	/**
	 * 设置录制状态
	 * 
	 * @param state
	 *            OSD状态
	 */
	public void setOSDState(OSDState state) {
		mState = state;

	}

	/**
	 * 当前录制时间
	 */
	public int recorderTime = 0;

}
