package com.mobnote.golukmain.helper;

import java.util.HashMap;

public abstract class UpLoadRequest {
	protected IUploadRequestListener mListener;
	protected QCloudHelper mQCloudHelper;
	
	public UpLoadRequest(IUploadRequestListener listener) {
		mListener = listener;
		mQCloudHelper = QCloudHelper.getInstance();
	}
	public abstract boolean upLoad(HashMap<String, String> pathMap);
}
