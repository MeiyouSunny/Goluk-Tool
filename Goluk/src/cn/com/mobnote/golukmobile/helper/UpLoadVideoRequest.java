package cn.com.mobnote.golukmobile.helper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.os.Message;

import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.VideoAttr;
import com.tencent.upload.task.ITask.TaskState;
import com.tencent.upload.task.data.FileInfo;
import com.tencent.upload.task.impl.PhotoUploadTask;
import com.tencent.upload.task.impl.VideoUploadTask;

import cn.com.mobnote.golukmobile.helper.bean.SignBean;
import cn.com.mobnote.golukmobile.helper.bean.SignDataBean;
import cn.com.mobnote.golukmobile.http.HttpManager;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class UpLoadVideoRequest extends UpLoadRequest implements IRequestResultListener {

	private String mVideoPath;
	private String mPhotoPath;
	private PhotoUploadTask mCoverUploadtask;
	private PhotoUploadTask mPhotoUploadtask;
	private VideoUploadTask mVideoUploadTask;
	private SignDataBean mSignDataBean;
	private long mTotalSize = 0;
	private long mCoverFileSize = 0;
	private long mPhotoFileSize = 0;
	 /**上传视频生成的封面地址和视频地址，QCloudHelper.VIDEO_BUCKET ，QCloudHelper.PHOTO_BUCKET分别作为key*/
	private HashMap<String, String> mUrl;
	public UpLoadVideoRequest(IUploadRequestListener listener) {
		super(listener);
		mUrl = new HashMap<String, String>();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean upLoad(HashMap<String, String> pathMap) {
		// TODO Auto-generated method stub
		if (pathMap == null || pathMap.size() != 2 || !pathMap.containsKey(QCloudHelper.PHOTO_BUCKET)
				|| !pathMap.containsKey(QCloudHelper.VIDEO_BUCKET)) {
			return false;
		}
		mVideoPath = pathMap.get(QCloudHelper.VIDEO_BUCKET);
		mPhotoPath = pathMap.get(QCloudHelper.PHOTO_BUCKET);
		File coverFile = new File(mPhotoPath);
		mCoverFileSize = coverFile.length();
		mTotalSize += mCoverFileSize;
		File photoFile = new File(mPhotoPath);
		mPhotoFileSize = photoFile.length();
		mTotalSize += mPhotoFileSize;
		File videoFile = new File(mVideoPath);
		mTotalSize += videoFile.length();
		UpLoadVideoSignRequest request = new UpLoadVideoSignRequest(IPageNotifyFn.PageType_UploadVideo, this);
		request.setTag(this);
		request.get();
		return true;
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if (requestType == IPageNotifyFn.PageType_UploadVideo) {
			SignBean signBean = (SignBean) result;
			if (signBean != null && signBean.success && signBean.data != null) {
				mSignDataBean = signBean.data;

				if (!uploadCoverToCloud()) {
					mListener.onUploadFailed(-1, "upload photo failed");
				}
			} else {
				mListener.onUploadFailed(-1, "get sign failed");
			}
		}
	}

	private boolean uploadCoverToCloud() {
		String space = mSignDataBean.envsync + QCloudHelper.PHOTO_BUCKET;
		mCoverUploadtask = new PhotoUploadTask(mPhotoPath,
				new IUploadTaskListener() {
			@Override
			public void onUploadSucceed(FileInfo fileInfo) {
				GolukDebugUtils.e("goluk", "上传成功! ret:" + fileInfo);
				mUrl.put(QCloudHelper.PHOTO_BUCKET, fileInfo.url);
				mCoverUploadtask = null;
				if (mPhotoFileSize != 0) {
					if (!uploadPhotoToCloud()) {
						mListener.onUploadFailed(-1, "upload photo failed");
					}
				} else {
					if (!uploadVideoToCloud()) {
						mListener.onUploadFailed(-1, "upload video failed");
					}
				}
			}

			@Override
	  		public void onUploadProgress(long totalSize, long sendSize) {
	  			int percent = (int) ((sendSize * 100) / (mTotalSize * 1.0f));
	  			mListener.onUploadProgress(percent);
	  		}

			@Override
			public void onUploadFailed(int errorCode, String errorMsg) {
				mListener.onUploadFailed(-1, "upload cover failed");
				mCoverUploadtask = null;
			}

			@Override
			public void onUploadStateChange(TaskState taskState) {
				GolukDebugUtils.d("goluk", "上传状态变化! ret:" + taskState);				
			}
		});
		mCoverUploadtask.setBucket(space);
		mCoverUploadtask.setAppid(QCloudHelper.APPID);
		mCoverUploadtask.setFileId(String.format("%s/%s.png", mSignDataBean.coverpath, mSignDataBean.videoid));
		mCoverUploadtask.setAuth(mSignDataBean.coversign);
		
		// 上传
		QCloudHelper helper = QCloudHelper.getInstance();
		return helper.upload(mCoverUploadtask);
	}

	private boolean uploadPhotoToCloud() {
		String space = mSignDataBean.envsync + QCloudHelper.PHOTO_BUCKET;
		mPhotoUploadtask = new PhotoUploadTask(mPhotoPath,
				new IUploadTaskListener() {
			@Override
			public void onUploadSucceed(FileInfo fileInfo) {
				GolukDebugUtils.e("goluk", "上传成功! ret:" + fileInfo);
				mUrl.put(QCloudHelper.PHOTO_BUCKET, fileInfo.url);
				mPhotoUploadtask = null;
				if (!uploadVideoToCloud()) {
					mListener.onUploadFailed(-1, "upload video failed");
				}
			}

			@Override
	  		public void onUploadProgress(long totalSize, long sendSize) {
	  			int percent = (int) (((sendSize + mCoverFileSize) * 100) / (mTotalSize * 1.0f));
	  			mListener.onUploadProgress(percent);
	  		}

			@Override
			public void onUploadFailed(int errorCode, String errorMsg) {
				mListener.onUploadFailed(-1, "upload photo failed");
				mPhotoUploadtask = null;
			}

			@Override
			public void onUploadStateChange(TaskState taskState) {
				GolukDebugUtils.d("goluk", "上传状态变化! ret:" + taskState);				
			}
		});
		mPhotoUploadtask.setBucket(space);
		mPhotoUploadtask.setAppid(QCloudHelper.APPID);
		mPhotoUploadtask.setFileId(String.format("%s/%s.png", mSignDataBean.wonderfulpath, mSignDataBean.videoid));
		mPhotoUploadtask.setAuth(mSignDataBean.coversign);
		
		// 上传
		QCloudHelper helper = QCloudHelper.getInstance();
		return helper.upload(mPhotoUploadtask);
	}

	private boolean uploadVideoToCloud() {
		
		String space = mSignDataBean.envsync + QCloudHelper.VIDEO_BUCKET;
		VideoAttr videoAttr = new VideoAttr();
		videoAttr.isCheck = false;
		videoAttr.title = mSignDataBean.videoid;
		String remotePath = String.format("%s%s.mp4", mSignDataBean.videopath, mSignDataBean.videoid);
		
		mVideoUploadTask = new VideoUploadTask(space , mVideoPath, remotePath, "wonderful", videoAttr, 
				new IUploadTaskListener() {
			@Override
			public void onUploadSucceed(FileInfo fileInfo) {
				GolukDebugUtils.e("goluk", "上传成功! ret:" + fileInfo);
				mUrl.put(QCloudHelper.VIDEO_BUCKET, fileInfo.url);
				mListener.onUploadSucceed(mUrl);
				mVideoUploadTask = null;
			}
			
			@Override
	  		public void onUploadProgress(long totalSize, long sendSize) {
	  			int percent = (int) (((sendSize + mCoverFileSize + mPhotoFileSize) * 100) / (mTotalSize * 1.0f));
	  			mListener.onUploadProgress(percent);
	  		}

			@Override
			public void onUploadFailed(int errorCode, String errorMsg) {
				mListener.onUploadFailed(errorCode, errorMsg);
				mVideoUploadTask = null;
			}

			@Override
			public void onUploadStateChange(TaskState taskState) {
				GolukDebugUtils.d("goluk", "上传状态变化! ret:" + taskState);				
			}
		});

		mVideoUploadTask.setBucket(space);
		mVideoUploadTask.setAppid(QCloudHelper.APPID);
		mVideoUploadTask.setAuth(mSignDataBean.videosign);
		
		// 上传
		QCloudHelper helper = QCloudHelper.getInstance();
		boolean result =  helper.upload(mVideoUploadTask);
		return result;
	}

	public String getVideoId() {
		return mSignDataBean.videoid;
	}

	public String getSignTime() {
		return mSignDataBean.signtime;
	}
	
	public void Cancel() {
		mQCloudHelper.cancel(mCoverUploadtask);
		mQCloudHelper.cancel(mPhotoUploadtask);
		mQCloudHelper.cancel(mVideoUploadTask);
		HttpManager.getInstance().cancelAll(this);
	}
}
