package cn.com.mobnote.golukmobile.startshare;

import android.content.Context;

import com.rd.car.editor.EditorParam;
import com.rd.car.editor.FilterPlaybackView;
import com.rd.car.editor.FilterVideoEditorException;

public class CreateNewVideo implements FilterPlaybackView.FilterVideoEditorListener {

	private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();
	private String mNewVideoFilePath = APP_FOLDER + "/" + "goluk/";
	/** 滤镜保存视频路径 */
	private String mVideoSavePath = null;

	public FilterPlaybackView mVVPlayVideo = null;

	private Context mContext = null;
	private ICreateNewVideoFn mFn = null;

	public CreateNewVideo(Context context, FilterPlaybackView mFilterPlay, ICreateNewVideoFn fn) {
		mContext = null;
		mVVPlayVideo = mFilterPlay;
		mFn = fn;
	}

	public void setFn(ICreateNewVideoFn fn) {
		mFn = fn;
	}

	private void sendData(int event, Object obj1, Object obj2, Object obj3) {
		if (null == mFn) {
			return;
		}
		mFn.CallBack_CreateNewVideoFn(event, obj1, obj2, obj3);
	}

	private EditorParam getNewVideoParams() {
		// 创建保存视频参数，默认参数为 输出size为480*480,码率为512k，帧率为21的视频
		EditorParam editorParam = new EditorParam();
		// 高清
		editorParam.nVideoWidth = 854;
		editorParam.nVideoHeight = 480;
		// //分辨率 帧率 码率 480*270 30fps 1400kbps
		editorParam.nVideoBitrate = 1500 * 1024;
		editorParam.nFps = 15;
		return editorParam;
	}

	/**
	 * 保存视频
	 */
	public void onSaveVideo() {
		try {
			mVideoSavePath = mNewVideoFilePath + "newvideo.mp4";
			mVVPlayVideo.saveVideo(mVideoSavePath, getNewVideoParams(), this);
		} catch (FilterVideoEditorException e) {
			sendData(ICreateNewVideoFn.EVENT_ERROR, null, null, null);
		}

	}

	@Override
	public void onFilterVideoSaveStart() {
		sendData(ICreateNewVideoFn.EVENT_START, null, null, null);
	}

	@Override
	public boolean onFilterVideoSaving(int nProgress, int nMax) {
		sendData(ICreateNewVideoFn.EVENT_SAVING, nProgress, null, null);
		return true;
	}

	@Override
	public void onFilterVideoEnd(boolean bSuccess, boolean bCancel) {
		sendData(ICreateNewVideoFn.EVENT_END, bSuccess, bCancel, mVideoSavePath);
	}

	@Override
	public void onFilterVideoSaveError(int nErrorType, int nErrorNo, String strErrorInfo) {
		sendData(ICreateNewVideoFn.EVENT_ERROR, nErrorType, nErrorNo, strErrorInfo);
	}

}
