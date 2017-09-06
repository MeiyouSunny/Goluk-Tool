package com.rd.veuisdk.callback;

/**
 * 压缩视频回调接口
 */
public interface ICompressVideoCallback {
	
	/** 
	 *  压缩开始回调接口
	 */
	void onCompressStart();

	/**
	 * 
	 * @param progress
	 * 			当前进度
	 * @param max
	 * 			最大进度
	 */
	void onProgress(int progress, int max);

	/** 
	 *  压缩完成回调接口
	 */
	void onCompressComplete(String path);

	/** 
	 *  压缩失败回调接口
	 */
	void onCompressError(String errorLog);
}
