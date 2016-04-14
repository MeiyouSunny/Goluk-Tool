package com.mobnote.golukmain.helper;

import java.util.HashMap;

public interface IUploadRequestListener {
	/**QCloudHelper.VIDEO_BUCKET ，QCloudHelper.PHOTO_BUCKET, QCloudHelper.FILE_BUCKET分别作为key*/
	public void onUploadSucceed(HashMap<String, String> urlMap);
	/**上传百分比*/
	public void onUploadProgress(int percent);
	public void onUploadFailed(int errorCode, String errorMsg);
}
