package cn.com.mobnote.golukmobile.startshare;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
// Tecent QCloud
import com.tencent.upload.task.ITask.TaskState;
import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.VideoAttr;
import com.tencent.upload.task.data.FileInfo;
import com.tencent.upload.task.impl.VideoUploadTask;
import com.tencent.upload.task.impl.PhotoUploadTask;
import cn.com.mobnote.golukmobile.helper.QCloudHelper;

public class UploadVideo {
	/** 上传视频更新进度 */
	private final int MSG_H_UPLOAD_PROGRESS = 2;
	/** 上传成功 */
	private final int MSG_H_UPLOAD_SUCESS = 3;
	/** 上传视频失败 */
	private final int MSG_H_UPLOAD_ERROR = 4;
	private final int MSG_H_COUNT = 7;
	/** 重新上传 */
	private final int MSG_H_RETRY_UPLOAD = 8;
	
	/* 云服务相关 Micle */
	private final int MSG_CLOUD_UPLOAD_SUCCESS = 10;

	private GolukApplication mApp = null;
	private Context mContext = null;
	private final int UPLOAD_FAILED_UP = 3;
	private int uploadCount = 0;
	private boolean isUploading = false;
	/** 统计 */
	private int finishShowCount = 0;
	/** 要上传的文件路径 */
	private String mVideoPath = null;
	/** 2/3 紧急/精彩 */
	private int mVideoType = 0;
	/** 文件名称 */
	private String mVideoName;
	private boolean mIsExit = false;
	private String mVideoVid = "";
	private String mSignTime = "";
	/** 上传视频是否完成 */
	private boolean mIsUploadSucess = false;
	private AlertDialog mErrorDialog = null;

	private IUploadVideoFn mFn = null;

	/** 退出提示框 */
	private AlertDialog mExitPromptDialog = null;

	public void setListener(IUploadVideoFn fn) {
		mFn = fn;
	}

	public void setExit() {
		mIsExit = true;
		exit(true);
	}

	public Handler mBaseHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_H_UPLOAD_PROGRESS:
				int percent = ((Integer) msg.obj).intValue();
				sendData(IUploadVideoFn.EVENT_PROCESS, percent);
				break;
			case MSG_H_UPLOAD_SUCESS:
				// 回调文件上传成功
				sendData(IUploadVideoFn.EVENT_UPLOAD_SUCESS, null);
				break;
			case MSG_H_UPLOAD_ERROR:
				if (mIsExit) {
					return;
				}
				// 上传失败
				uploadFailed();
				break;
			case MSG_H_COUNT:
				finishShowCount++;
				if (finishShowCount >= 3) {
					mBaseHandler.removeMessages(MSG_H_COUNT);
					finishShowCount = 0;
				} else {
					mBaseHandler.sendEmptyMessageDelayed(MSG_H_COUNT, 1000);
				}
				break;
			case MSG_H_RETRY_UPLOAD:
				if (mIsExit) {
					return;
				}
				uploadVideoFile(mVideoPath);
				break;
			// Upload to cloud, Micle
			case MSG_CLOUD_UPLOAD_SUCCESS:
				uploadToCloudCallBack(msg.obj);
				break;				
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	public UploadVideo(Context context, GolukApplication application, final String videoName) {
		mContext = context;
		mApp = application;
		mVideoName = videoName;
		createThumb();
	}

	public void setUploadInfo(final String videoPath, final int videoType, final String videoName) {
		mVideoPath = videoPath;
		mVideoType = videoType;
		mVideoName = videoName;

		uploadCount = 0;

		createThumb();

		uploadVideoFile(mVideoPath);

		initState();
	}

	private void initState() {

	}

	public static final String PATH_FS1 = "/goluk";
	final String fileFolder = Environment.getExternalStorageDirectory().getPath() + PATH_FS1 + "/Cache";
	private String thumbFile = fileFolder + "/thumb11.jpg";
	private Bitmap mShortBitmap = null;

	public Bitmap getThumbBitmap() {
		return mShortBitmap;
	}

	private final int THUMB_WIDTH = 854;
	private final int THUMB_HEIGHT = 480;

	//

	/**
	 * 创建视频缩略图
	 * 
	 * @author jyf
	 * @date 2015年8月14日
	 */
	private void createThumb() {
		if (null != mShortBitmap) {
			return;
		}
		String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		thumbFile = filePath + File.separator + mVideoName;
		mShortBitmap = ImageManager.getBitmapFromCache(thumbFile, THUMB_WIDTH, THUMB_HEIGHT);
		File image = new File(thumbFile);
		if (image.exists() && null != image) {
			return;
		}
		thumbFile = fileFolder + "/thumb11.jpg";
		mShortBitmap = getSelfBitmap();
		if (null == mShortBitmap) {
			return;
		}
		try {
			File file = new File(fileFolder);
			file.mkdirs();
			file = new File(thumbFile);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			mShortBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			mShortBitmap = ImageManager.getBitmapFromCache(thumbFile, THUMB_WIDTH, THUMB_HEIGHT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自己抓取视频第一幀图片
	 * 
	 * @return
	 * @author jyf
	 * @date 2015年8月14日
	 */
	private Bitmap getSelfBitmap() {
		Bitmap temp = ThumbnailUtils.createVideoThumbnail(mVideoPath, Thumbnails.MINI_KIND);
		if (temp == null) {
			temp = GolukUtils.createVideoThumbnail(mVideoPath);
		}

		return temp;
	}

	public String getVideoId() {
		return this.mVideoVid;
	}
	public String getSignTime() {
		return this.mSignTime;
	}

	public String getThumbPath() {
		return thumbFile;
	}

	/**
	 * 上传视频文件，支持两种上传途径
	 * 		1. 上传至自建文件服务器
	 * 		2. 上传至云服务器（目前是腾讯云的微视频）
	 * @param videoPath
	 */
	public void uploadVideoFile(String videoPath) {
		if (null == videoPath || "".equals(videoPath)) {
			return;
		}
		mVideoPath = videoPath;
		uploadCount++;
		GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------uploadVideoFile :" + uploadCount);
		isUploading = true;
		final String filePath = FileUtils.javaToLibPath(videoPath);
		boolean isSuccess = false;
		
		// 根据存储服务选择不同的上传途径， Micle
		String storage = mApp.mSharedPreUtil.getConfigStorage();				
		if (storage.equals("cloud")) {
			// 云服务器
			isSuccess = true;
			uploadToCloud(videoPath);
		} else {
			// 自建文件服务器
			isSuccess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_UploadVideo, filePath);
		}

		GolukDebugUtils.e("", "Request---------:" + isSuccess);
	}

	public boolean isUploading() {
		return isUploading;
	}

	// CC上传失败，提示用户重试或退出
	public void showExitDialog() {
		dimissErrorDialog();

		mExitPromptDialog = new AlertDialog.Builder(mContext).setTitle("提示").setMessage("正在上传视频，是否中断？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						click_Exit();
					}

				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dimissExitDialog();
					}

				}).create();
		mExitPromptDialog.show();
	}

	private void click_Exit() {
		dimissExitDialog();
		dimissErrorDialog();
		// GlobalWindow.getInstance().toFailed("视频上传取消");
		exit(false);
		sendData(IUploadVideoFn.EVENT_EXIT, false);
	}

	// CC上传失败，提示用户重试或退出
	private void uploadFailed() {
		dimissErrorDialog();
		if (mIsExit) {
			return;
		}
		// GlobalWindow.getInstance().toFailed("视频上传失败");

		mErrorDialog = new AlertDialog.Builder(mContext).setTitle("提示").setMessage("上传失败")
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						uploadVideoFile(mVideoPath);
						dimissErrorDialog();
						GolukUtils.showToast(mContext, "重新开始上传");
					}

				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						click_Exit();
					}

				}).create();

		mErrorDialog.show();
	}

	private void sendData(int event, Object data) {
		if (null != mFn) {
			mFn.CallBack_UploadVideo(event, data);
		}
	}

	private void dimissErrorDialog() {
		if (null != mErrorDialog) {
			mErrorDialog.dismiss();
			mErrorDialog = null;
		}
	}

	public void setExit(boolean isExit) {
		mIsExit = isExit;
	}

	// 上传视频成功
	private void uploadSucess() {
		mIsUploadSucess = true;
		isUploading = false;
		GolukDebugUtils.e("", "upload service--VideoShareActivity-handleStatus---上传完成---FINISH----");
		// 通知上传成功
		mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_SUCESS);
	}

	// 上传视频失败
	private void uploadError() {
		if (mIsExit) {
			return;
		}
		GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------uploadError :uploadCount:  " + uploadCount);
		if (uploadCount >= UPLOAD_FAILED_UP) {
			// 报错
			mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_ERROR);
			isUploading = false;
		} else {
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 500);
		}
	}

	private void updateFreshProgress(int per) {
		if (mIsExit) {
			return;
		}
		Message msg = new Message();
		msg.what = MSG_H_UPLOAD_PROGRESS;
		msg.obj = per;
		mBaseHandler.sendMessage(msg);
	}

	private void dimissExitDialog() {
		if (null != mExitPromptDialog) {
			mExitPromptDialog.dismiss();
			mExitPromptDialog = null;
		}
	}

	private void exit(boolean isdestroyTopwindow) {
		mIsExit = true;
		// 取消上传
		if (!mIsUploadSucess) {
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_UploadVideo,
					JsonUtil.getCancelJson());
		}
		this.dimissErrorDialog();
		this.dimissExitDialog();
		mBaseHandler.removeMessages(MSG_H_RETRY_UPLOAD);
	}

	/** 文件上传成功 */
	private static final int UPLOAD_STATE_SUCESS = 1;
	/** 文件上传进度 */
	private static final int UPLOAD_STATE_PROGRESS = 2;

	public void videoUploadCallBack(int success, Object param1, Object param2) {
		GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------videoUploadCallBack :success:  " + success);
		if (UPLOAD_STATE_SUCESS == success) {
			// 保存视频上传ID
			mVideoVid = (String) param2;
			uploadSucess();
			GolukDebugUtils.e("", "视频上传返回id--VideoShareActivity-videoUploadCallBack---vid---" + mVideoVid);
		} else if (UPLOAD_STATE_PROGRESS == success) {
			// 上传进度
			final int per = (Integer) param1;
			updateFreshProgress(per);
		} else {
			// 文件上传失败
			uploadError();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 云存储服务
	private void uploadToCloud(String filePath) {
		// 请求签名
		QCloudHelper helper = QCloudHelper.getInstance(mContext, mApp);
		RequestParams params = new RequestParams("uid", mApp.mCurrentUId);
		helper.videoSign(params, new AsyncHttpResponseHandler() {
		    @Override
		    public void onStart() {
		        // called before request is started
		    	Log.d("goluk", "请求签名开始！");
		    }

		    @Override
		    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
		        // called when "200 OK"
		    	Log.d("goluk", "请求签名成功！");

	    		String content = new String(response);	    		
	    		signAndUploadVideoCloud(mVideoPath, content);
		    }

		    @Override
		    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
		        // called when "4XX" (eg. 401, 403, 404)
		    	Log.e("goluk", "请求签名失败！");
		    	click_Exit();
		    	GolukUtils.showToast(mContext, "网络错误，分享失败！");
		    }

		    @Override
		    public void onRetry(int retryNo) {
		        // called when request is retried
		    	Log.e("goluk", "请求签名重试！");
			}
		});
	}
	
	private boolean signAndUploadVideoCloud(String filePath, String content) {
		boolean result = false;
		 try {
		    	if (null == content) {
		    		return result;
		    	}
				JSONObject rootObj = new JSONObject(content);
				boolean isSuccess = rootObj.getBoolean("success");

				if (isSuccess) {
					JSONObject data = rootObj.getJSONObject("data");
					String videoid = data.optString("videoid");
					String videosign = data.optString("videosign");
					String videopath = data.optString("videopath");
					String coversign = data.optString("coversign");
					String coverpath = data.optString("coverpath");
					String signtime = data.optString("signtime");
					String env = data.optString("envsync");
					
					// 根据不同环境使用不同存储空间					
					result = uploadVideoToCloud(env, videoid, videosign, filePath, videopath, signtime);
					uploadPhotoToCloud(env, videoid, coversign, thumbFile, coverpath, signtime);
				} else {
					GolukDebugUtils.e("goluk", "请求视频签名失败！");
			    	click_Exit();
			    	GolukUtils.showToast(mContext, "网络错误，分享失败！");
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		 return result;
	}

	/**
	 * 上传视频至云服务
	 * @param id
	 * @param sign
	 * @param localPath
	 * @param remotePath
	 * @param signTime
	 * @return
	 */
	private boolean uploadVideoToCloud(String env, String id, String sign, String localPath, String remotePath, String signTime) {		
		mVideoVid = id;
		mSignTime = signTime;
		
		String space = env + QCloudHelper.VIDEO_BUCKET;
		VideoAttr videoAttr = new VideoAttr();
		videoAttr.isCheck = false;
		videoAttr.title = id;
		remotePath = String.format("%s%s.mp4", remotePath, id);
		
		VideoUploadTask task = new VideoUploadTask(space , localPath, remotePath, "wonderful", videoAttr, 
				new IUploadTaskListener() {
			@Override
			public void onUploadSucceed(FileInfo fileInfo) {
				Log.e("goluk", "上传成功! ret:" + fileInfo);
				Map<String, String> map = new HashMap<String, String>();
				map.put("url", fileInfo.url);
				map.put("type", "video");
				
				Message msg = new Message();
	    		msg.what = MSG_CLOUD_UPLOAD_SUCCESS;
	    		msg.obj = map;
	    		mBaseHandler.sendMessage(msg);
			}
			
			@Override
	  		public void onUploadProgress(long totalSize, long sendSize) {
	  			int percent = (int) ((sendSize * 100) / (totalSize * 1.0f));
				Log.d("goluk", "上传中! ret:" + percent);
				
	    		Message msg = new Message();
	    		msg.what = MSG_H_UPLOAD_PROGRESS;
	    		msg.obj = percent;
	    		mBaseHandler.sendMessage(msg);
	  		}

			@Override
			public void onUploadFailed(int errorCode, String errorMsg) {
				Log.e("goluk", "上传结果:失败! ret:" + errorCode + " msg:" + errorMsg);
				
				mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_ERROR);
			}

			@Override
			public void onUploadStateChange(TaskState taskState) {
				Log.d("goluk", "上传状态变化! ret:" + taskState);				
			}
		});
		task.setBucket(space);
		task.setAppid(QCloudHelper.APPID);
		task.setAuth(sign);
		
		// 上传
		QCloudHelper helper = QCloudHelper.getInstance(mContext, mApp);
		boolean result =  helper.upload(task);
		return result;
	}

	/**
	 * 上传视频封面至云服务
	 * @param id
	 * @param sign
	 * @param localPath
	 * @param remotePath
	 * @param signTime
	 * @return
	 */
	private boolean uploadPhotoToCloud(String env, String id, String sign, String localPath, String remotePath, String signTime) {
		String space = env + QCloudHelper.PHOTO_BUCKET;
		PhotoUploadTask task = new PhotoUploadTask(localPath,
				new IUploadTaskListener() {
			@Override
			public void onUploadSucceed(FileInfo fileInfo) {
				Log.e("goluk", "上传成功! ret:" + fileInfo);
				Map<String, String> map = new HashMap<String, String>();
				map.put("url", fileInfo.url);
				map.put("type", "photo");
				
				Message msg = new Message();
	    		msg.what = MSG_CLOUD_UPLOAD_SUCCESS;
	    		msg.obj = map;
	    		mBaseHandler.sendMessage(msg);  		
			}
			
			@Override
	  		public void onUploadProgress(long totalSize, long sendSize) {
	  			int percent = (int) ((sendSize * 100) / (totalSize * 1.0f));
				Log.d("goluk", "上传中! ret:" + percent);
	  		}

			@Override
			public void onUploadFailed(int errorCode, String errorMsg) {
				Log.e("goluk", "上传结果:失败! ret:" + errorCode + " msg:" + errorMsg);
			}

			@Override
			public void onUploadStateChange(TaskState taskState) {
				Log.d("goluk", "上传状态变化! ret:" + taskState);				
			}
		});
		task.setBucket(space);
		task.setAppid(QCloudHelper.APPID);
		task.setFileId(String.format("%s/%s.png", remotePath, id));
		task.setAuth(sign);
		
		// 上传
		QCloudHelper helper = QCloudHelper.getInstance(mContext, mApp);
		return helper.upload(task);
	}
	
	private String mVideoUrl = "";
	private String mCoverUrl = "";
	/**
	 * 上传回调
	 * @param obj
	 */
	private void uploadToCloudCallBack(Object obj) {
		Map<String, String> map = (HashMap<String, String>) obj;
		String url = map.get("url");
		String type = map.get("type");
		if (type.equals("video")) { 
			mVideoUrl = url;
			
			Log.e("goluk", "Video url: " + url);
		}
		
		if (type.equals("photo")) { 
			mCoverUrl = url;
			
			Log.e("goluk", "Cover url: " + url);
		}
		
		if (!mVideoUrl.equals("") &&
				!mCoverUrl.equals("")) {
			mVideoUrl = "";
			mCoverUrl = "";
			videoUploadCallBack(UPLOAD_STATE_SUCESS, "qcloud", mVideoVid);
		}
	}
}
