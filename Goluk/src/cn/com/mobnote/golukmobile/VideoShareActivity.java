package cn.com.mobnote.golukmobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import cn.com.mobnote.application.GlobalWindow;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.utils.LogUtil;

import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.upload.UploadListener;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.sdk.mobile.upload.VideoInfo;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:Goluk视频分享页面
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("HandlerLeak")
public class VideoShareActivity extends BaseActivity implements OnClickListener {

	private static final String DESCRIPTOR = "com.umeng.share";
	// 配置API KEY
	public final static String API_KEY = "O8g0bf8kqiWroHuJaRmihZfEmj7VWImF";
	// 配置帐户ID
	public final static String USERID = "77D36B9636FF19CF";
	// 配置下载文件路径
	public final static String DOWNLOAD_DIR = "CCDownload";
	// 配置视频回调地址
	public final static String NOTIFY_URL = "http://server.xiaocheben.com/cdcRegister/uMengCallBack.htm";
	/** 236服务器分享地址 */
	public static final String NOTIFY_URL2 = "http://svr.xiaocheben.com/navidog4MeetTrans/ccVideoApiCallback.htm";

	public static final String PATH_FS1 = "/tiros-com-cn-ext";

	final String fileFolder = Environment.getExternalStorageDirectory().getPath() + PATH_FS1 + "/Cache";
	private String thumbFile = fileFolder + "/thumb11.jpg";

	/** 上传视频更新进度 */
	private final int MSG_H_UPLOAD_PROGRESS = 2;
	/** 上传成功 */
	private final int MSG_H_UPLOAD_SUCESS = 3;
	/** 上传视频失败 */
	private final int MSG_H_UPLOAD_ERROR = 4;
	/** 取消上传 */
	private final int MSG_H_UPLOAD_CANCEL = 5;

	private final int MSG_H_START_UPLOAD = 6;

	private final int MSG_H_COUNT = 7;
	/** 重新上传 */
	private final int MSG_H_RETRY_UPLOAD = 8;

	private final UMSocialService mController = UMServiceFactory.getUMSocialService(DESCRIPTOR);
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 分享layout */
	private RelativeLayout mShareLayout = null;
	private ImageView mShortImg = null;
	/** 是否分享到视频广场 */
	private ImageView mIsShareToOther = null;
	private Spinner mSpinner = null;
	private EditText mDesEdit = null;

	/** 系统loading */
	private ProgressDialog mPdsave = null;
	/** 视频ID */
	private String mVideoVid = "";
	/** 视频路径 */
	private String mVideoPath = "";
	/** 上传视频时间记录 */
	private long uploadVideoTime = 0;

	/** cc视频上传对象 */
	private VideoInfo mVideoinfo = null;
	private Uploader mUploader = null;
	/** 上传视频是否完成 */
	private boolean mIsUploadSucess = false;

	/** 统计 */
	private int finishShowCount = 0;

	/** 是否正在上传视频 */
	private boolean isUploading = false;
	private boolean mIsCheck = true;

	private AlertDialog mErrorDialog = null;
	/** 退出提示框 */
	private AlertDialog mExitPromptDialog = null;
	private Bitmap mShortBitmap = null;

	SharePlatformUtil sharePlatform;
	/** 2/3 紧急/精彩 */
	private int mVideoType = 0;

	private String videoName;
	/** 上传失败次数后再提示 */
	private final int UPLOAD_FAILED_UP = 3;
	private int uploadCount = 0;

	public Handler mmmHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:// 延迟让server返回的文字内容显示
				JSONObject json = new JSONObject();
				try {
					json.put("code", 200);
					json.put("videourl", "http://cdn2.xiaocheben.com/files/cdcvideo/test1111.mp4");
					json.put("imageurl", "http://cdn2.xiaocheben.com/files/cdcpic/test1111.png");
					json.put("text", "骚年赶紧戳进来吧");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				videoShareCallBack(1, json.toString());
			case MSG_H_UPLOAD_PROGRESS:
				if (!GolukApplication.getInstance().getIsBackgroundState()) {
					if (GlobalWindow.getInstance().isShow()) {
						// 更新进度条
						int percent = ((Integer) msg.obj).intValue();
						GlobalWindow.getInstance().refreshPercent(percent);
						console.log("upload service--VideoShareActivity-mmmHandler percent:" + percent);
					} else {
						if (null == GlobalWindow.getInstance().getApplication()) {
							GlobalWindow.getInstance().setApplication(mApp);
						}
						GlobalWindow.getInstance().createVideoUploadWindow("正在上传Goluk视频");
					}
				}

				break;
			case MSG_H_UPLOAD_SUCESS:
				showToast("上传完成");
				GlobalWindow.getInstance().topWindowSucess("视频上传成功");
				shareCanEnable();
				break;
			case MSG_H_UPLOAD_ERROR:
				if (isExit) {
					return;
				}
				// 上传失败
				uploadFailed();
				break;
			case MSG_H_UPLOAD_CANCEL:

				break;
			case MSG_H_START_UPLOAD:
				break;
			case MSG_H_COUNT:

				finishShowCount++;
				if (finishShowCount >= 3) {
					GlobalWindow.getInstance().dimissGlobalWindow();
					mmmHandler.removeMessages(MSG_H_COUNT);
					finishShowCount = 0;
				} else {
					mmmHandler.sendEmptyMessageDelayed(MSG_H_COUNT, 1000);
				}
				break;
			case MSG_H_RETRY_UPLOAD:
				if (isExit) {
					return;
				}
				showToastMsg("重新上传...");
				uploadShareVideo();
				if (null == GlobalWindow.getInstance().getApplication()) {
					GlobalWindow.getInstance().setApplication(mApp);
				}
				break;
			default:
				break;
			}
		};
	};

	/** cc视频上传回调事件 */
	private UploadListener uploadListenner = new UploadListener() {
		@Override
		public void handleStatus(VideoInfo v, int status) {
			// 处理上传回调的视频信息及上传状态
			mVideoinfo = v;
			switch (status) {
			case Uploader.PAUSE:
				// 暂停上传
				console.log("upload service--VideoShareActivity-handleStatus---暂停上传---pause...");
				break;
			case Uploader.UPLOAD:
				// 开始上传
				console.log("upload service--VideoShareActivity-handleStatus---开始上传---UPLOAD...");
				mmmHandler.sendEmptyMessage(MSG_H_START_UPLOAD);
				break;
			case Uploader.FINISH:
				// 上传完成
				mIsUploadSucess = true;
				isUploading = false;
				console.log("upload service--VideoShareActivity-handleStatus---上传完成---FINISH----");
				// 通知上传成功
				mmmHandler.sendEmptyMessage(MSG_H_UPLOAD_SUCESS);
				break;
			}
		}

		@Override
		public void handleProcess(long range, long size, String videoId) {
			if (isExit) {
				return;
			}
			// 保存上传视频ID
			mVideoVid = videoId;
			final int percent = (int) (range * 100 / size);
			Message msg = new Message();
			msg.what = MSG_H_UPLOAD_PROGRESS;
			msg.obj = percent;
			mmmHandler.sendMessage(msg);
			// 上传进度回调
			console.log("upload service--VideoShareActivity-handleProcess___range=" + range + ", size=" + size
					+ ", videoId = " + videoId + " percent:" + percent);
		}

		@Override
		public void handleException(DreamwinException exception, int status) {
			// 处理上传过程中出现的异常
			console.log("upload service--VideoShareActivity-handleException----上传失败，" + exception.getMessage());
			if (isExit) {
				return;
			}
			if (uploadCount >= UPLOAD_FAILED_UP) {
				// 报错
				mmmHandler.sendEmptyMessage(MSG_H_UPLOAD_ERROR);
				isUploading = false;
			} else {
				mmmHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 500);
			}

		}

		@Override
		public void handleCancel(String videoId) {
			// 处理取消上传的后续操作
			console.log("upload service--VideoShareActivity-handleCancel----取消上传---------videoId = " + videoId);
			isUploading = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_share);

		mContext = this;
		// 获取视频Id
		Intent intent = getIntent();
		mVideoPath = intent.getStringExtra("cn.com.mobnote.golukmobile.videopath");
		mVideoType = intent.getIntExtra("type", 0);
		videoName = intent.getStringExtra("videoName");
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "VideoShare");
		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数
		// 获取第一帧缩略图
		createThumb();
		// 初始化
		init();
		// 上传已倒出的本地视频
		uploadShareVideo();
		if (null == GlobalWindow.getInstance().getApplication()) {
			GlobalWindow.getInstance().setApplication(mApp);
		}
		GlobalWindow.getInstance().createVideoUploadWindow("正在上传Goluk视频");
	}

	private void createThumb() {
		long startTime = System.currentTimeMillis();
		// mShortBitmap = ThumbnailUtils.createVideoThumbnail(mVideoPath,
		// Thumbnails.MINI_KIND);
		String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		thumbFile = filePath + File.separator + videoName;
		mShortBitmap = ImageManager.getBitmapFromCache(thumbFile, 194, 109);
		LogUtil.e("xuhw", "BBBBBB======thumbFile=" + thumbFile);
		LogUtil.e("xuhw", "BBBBBB======mVideoPath=" + mVideoPath);
		File image = new File(thumbFile);
		if (image.exists()) {
			return;
		} else {
			thumbFile = fileFolder + "/thumb11.jpg";
			mShortBitmap = createVideoThumbnail(mVideoPath);
			// mShortBitmap = ThumbnailUtils.createVideoThumbnail(mVideoPath,
			// Thumbnails.MINI_KIND);
			if (mShortBitmap != null) {
				int width = mShortBitmap.getWidth();
				int height = mShortBitmap.getHeight();

				Log.e("", "VideoShareActivity createThumb: width:" + width + "	height:" + height);
			} else {
				Log.e("", "VideoShareActivity createThumb: NULL:");
				mShortBitmap = ThumbnailUtils.createVideoThumbnail(mVideoPath, Thumbnails.MINI_KIND);
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

				String fsFile = FileUtils.javaToLibPath(thumbFile);

				Log.e("", "VideoShareActivity createThumb: time: " + fsFile);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		long dur = System.currentTimeMillis() - startTime;

		Log.e("", "VideoShareActivity createThumb: time:" + dur);
	}

	@SuppressLint("NewApi")
	private Bitmap createVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
			}
		}
		return bitmap;
	}

	private void dimissErrorDialog() {
		if (null != mErrorDialog) {
			mErrorDialog.dismiss();
			mErrorDialog = null;
		}
	}

	private void dimissExitDialog() {
		if (null != mExitPromptDialog) {
			mExitPromptDialog.dismiss();
			mExitPromptDialog = null;
		}
	}

	// CC上传失败，提示用户重试或退出
	private void showExitDialog() {
		dimissErrorDialog();

		mExitPromptDialog = new AlertDialog.Builder(this).setTitle("提示").setMessage("正在上传视频，是否中断？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dimissExitDialog();
						GlobalWindow.getInstance().toFailed("视频上传取消");
						exit(false);

					}

				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dimissExitDialog();
					}

				}).create();
		mExitPromptDialog.show();
	}

	// CC上传失败，提示用户重试或退出
	private void uploadFailed() {

		dimissErrorDialog();
		if (isExit) {
			return;
		}
		GlobalWindow.getInstance().toFailed("视频上传失败");

		mErrorDialog = new AlertDialog.Builder(this).setTitle("提示").setMessage("上传失败")
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						uploadShareVideo();
						dimissErrorDialog();
						showToast("重新开始上传");
						if (null == GlobalWindow.getInstance().getApplication()) {
							GlobalWindow.getInstance().setApplication(mApp);
						}
						GlobalWindow.getInstance().createVideoUploadWindow("正在上传Goluk视频");

					}

				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dimissErrorDialog();
						GlobalWindow.getInstance().toFailed("视频上传取消");
						exit(false);

					}

				}).create();

		mErrorDialog.show();
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	/**
	 * 页面初始化
	 */
	private void init() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mIsShareToOther = (ImageView) findViewById(R.id.share_check);
		if (mIsCheck) {
			mIsShareToOther.setBackgroundResource(R.drawable.share_select_btn);
		} else {
			mIsShareToOther.setBackgroundResource(R.drawable.share_select_btn_down_down);
		}

		mIsShareToOther.setOnClickListener(this);

		mShortImg = (ImageView) findViewById(R.id.share_img);
		if (null != mShortBitmap) {
			Drawable drawable = new BitmapDrawable(mShortBitmap);
			mShortImg.setBackgroundDrawable(drawable);
		}

		mDesEdit = (EditText) findViewById(R.id.share_desc);

		this.mSpinner = (Spinner) findViewById(R.id.spinner1);

		mShareLayout = (RelativeLayout) findViewById(R.id.share_layout);
		// 注册事件
		mBackBtn.setOnClickListener(this);
		mShareLayout.setOnClickListener(this);
	}

	/**
	 * 上传要分享的视频 private void uploadShareVideo(){ //将本地视频地址,转成logic可读路径fs1://
	 * if(!"".equals(mVideoPath) && null != mVideoPath){ String localPath =
	 * FileUtils.javaToLibPath(mVideoPath); uploadVideoTime =
	 * SystemClock.uptimeMillis(); boolean b =
	 * mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
	 * IPageNotifyFn.PageType_UploadVideo,localPath); if(!b){
	 * Toast.makeText(mContext,"调用视频上传接口失败",Toast.LENGTH_SHORT).show(); } else{
	 * //显示全局上传进度条 //重置滤镜标识 //mMVListAdapter.setResChange(false); } } }
	 */

	/**
	 * 调用CC接口上传视频
	 */
	private void uploadShareVideo() {
		if (null == mVideoPath || "".equals(mVideoPath)) {
			return;
		}

		uploadCount++;

		isUploading = true;
		VideoInfo videoinfo = new VideoInfo();
		videoinfo.setTitle("标题");
		videoinfo.setTags("标签");
		// TODO 登录成功后的UID
		videoinfo.setDescription(mApp.mCurrentUId);
		videoinfo.setFilePath(mVideoPath);
		videoinfo.setUserId(USERID);
		// TODO 登录成功后的回调接口url
		String uploadURL = NOTIFY_URL2;
		if (null != mApp.mCCUrl && !"".equals(mApp.mCCUrl)) {
			uploadURL = mApp.mCCUrl;
		}
		// this.showToast(uploadURL);
		videoinfo.setNotifyUrl(uploadURL);
		if (mUploader != null) {
			mUploader = null;
		}
		mUploader = new Uploader(videoinfo, API_KEY);
		mUploader.setUploadListener(uploadListenner);
		mUploader.start();
		// 显示上传进度条

	}

	/**
	 * 本地视频上传回调
	 * 
	 * @param vid
	 *            ,视频ID
	 */
	public void videoUploadCallBack(int success, String vid) {
		// 视频上传成功,回调,跳转到视频分享页面
		// 隐藏loading
		// mLoadingAnimation.stop();
		// 显示播放图片
		// mPlayStatusImage.setVisibility(View.VISIBLE);
		// 隐藏loading布局
		// mVideoLoadingLayout.setVisibility(View.GONE);
		if (1 == success) {
			console.toast("视频上传使用时间：" + (SystemClock.uptimeMillis() - uploadVideoTime) + "ms", mContext);
			// 保存视频上传ID
			mVideoVid = vid;
			console.log("视频上传返回id--VideoShareActivity-videoUploadCallBack---vid---" + vid);
		} else {
			console.toast("视频上传失败", mContext);
		}
	}

	private void shareCanEnable() {
		if (null != mShareLayout) {
			mShareLayout.setClickable(true);
			mShareLayout.setBackgroundResource(R.drawable.video_ym_share_btn);
		}
	}

	/**
	 * 本地视频分享回调
	 * 
	 * @param json
	 *            ,分享数据
	 */
	public void videoShareCallBack(int success, String json) {
		mPdsave.dismiss();
		if (1 != success) {
			Toast.makeText(VideoShareActivity.this, "获取视频分享地址失败", Toast.LENGTH_SHORT).show();
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			System.out.println("分享地址回调:" + json.toString());
			boolean isSucess = obj.getBoolean("success");
			if (!isSucess) {
				Toast.makeText(VideoShareActivity.this, "获取视频分享地址失败", Toast.LENGTH_SHORT).show();
				return;
			}
			
			JSONObject dataObj = obj.getJSONObject("data");
			final String shortUrl = dataObj.getString("shorturl");
			final String coverUrl = dataObj.getString("coverurl");
			String title = "极路客精彩视频分享";
			String describe = mDesEdit.getText().toString();
			if(describe == null || "".equals(describe)){
				describe = "#极路客精彩视频#";
			}
			console.log("视频上传返回id--VideoShareActivity-videoUploadCallBack---调用第三方分享---: " + shortUrl);
			
			// 设置分享内容
			//sharePlatform.setShareContent(shortUrl, coverUrl, mDesEdit.getText().toString());
			CustomShareBoard shareBoard = new CustomShareBoard(this,sharePlatform,shortUrl,coverUrl,describe,title);
			shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
	}

	@Override
	protected void onResume() {
		mApp.setContext(this, "VideoShare");
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			// 返回
			preExit();
			break;
		case R.id.share_layout:
			click_share();
			break;
		case R.id.share_check:
			click_Check();
			break;
		}
	}

	private void click_Check() {
		if (mIsCheck) {
			mIsCheck = false;
			mIsShareToOther.setBackgroundResource(R.drawable.share_select_btn_down_down);
		} else {
			mIsCheck = true;
			mIsShareToOther.setBackgroundResource(R.drawable.share_select_btn);
		}
	}

	private void preExit() {
		if (this.isUploading) {
			// 正在上传视频，需要提示用户，
			showExitDialog();
		} else {
			exit(true);
		}
	}

	private void cancelLoad() {

		new Thread() {
			@Override
			public void run() {
				mUploader.cancel();
			}
		}.start();
	}

	private void exit(boolean isdestroyTopwindow) {
		isExit = true;
		this.dimissErrorDialog();
		this.dimissExitDialog();
		mmmHandler.removeMessages(MSG_H_RETRY_UPLOAD);
		long starTime = System.currentTimeMillis();
		// mUploader.cancel();
		cancelLoad();
		Log.e("", "uploader   cancal time:--------:" + (System.currentTimeMillis() - starTime));
		if (isdestroyTopwindow) {
			GlobalWindow.getInstance().dimissGlobalWindow();
		}
		VideoShareActivity.this.finish();
	}

	boolean isExit = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			preExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private String getSpinnerSelect() {
		int position = mSpinner.getSelectedItemPosition();
		// 为了保证与服务器协议兼容，需要加1
		position++;

		String type = createType("" + position);

		LogUtil.e("", "spinner select :" + position + " type:" + type);

		return type;
	}

	private void click_share() {
		if (!this.mIsUploadSucess) {
			// Toast.makeText(VideoShareActivity.this, "上传视频成功后才可以分享",
			// Toast.LENGTH_SHORT).show();
			Toast.makeText(VideoShareActivity.this, "正在上传视频,请稍等", Toast.LENGTH_SHORT).show();
			return;
		}

		final String selectJson = getSpinnerSelect();
		final String isSeque = mIsCheck ? "1" : "0";
		int type = mVideoType == 2 ? 2 : 1;
		final String json = createShareJson(selectJson, isSeque, "" + type);

		LogUtil.e("", "jyf-----VideoShareActivity -----click_shares json:" + json);
		if (null == mApp) {
			showToast("mApp==NULL");
		} else {
			if (mApp.mGoluk == null) {
				showToast("mApp.mGoluk == null");
			}
		}

		boolean b = false;
		if (mApp != null && mApp.mGoluk != null) {
			b = mApp.mGoluk
					.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Share, json);
		} else {
			LogUtil.e("", "jyf-----VideoShareActivity -----mAPP 为空 或者 mGoluk 为空");
		}

		if (b) {
			mPdsave = ProgressDialog.show(VideoShareActivity.this, "", "请求分享链接...");
			mPdsave.setCancelable(true);
			mPdsave.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					if (null != mPdsave) {
						mPdsave.dismiss();
						mPdsave = null;
					}
					mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Share,
							JsonUtil.getCancelJson());
				}
			});
		} else {
			showToast("分享失败");
		}

		Log.e("", "chxy__b__VideoShareActivity share11" + b);
		Log.e("", "chxy____VideoShareActivity share11" + json);
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			Toast.makeText(VideoShareActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
			return;
		}
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, mVideoVid);
	}

	public static String createShareSucesNotifyJson(String videoVid, String channel) {
		String json = null;
		try {
			JSONObject obj = new JSONObject();
			obj.put("videoid", videoVid);
			obj.put("channel", channel);

			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	private String createType(String type) {
		JSONArray array = new JSONArray();
		array.put(type);
		return array.toString();
	}

	// attribute: 用户选择的视频类型
	// issquare 是否分享到视频广场
	private String createShareJson(String attribute, String issquare, String type) {
		String json = null;
		try {
			String videoDes = "";
			String attriDefault = "";
			try {
				videoDes = URLEncoder.encode(mDesEdit.getText().toString(), "UTF-8");
				attriDefault = URLEncoder.encode(attribute, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			JSONObject obj = new JSONObject();
			obj.put("videoid", mVideoVid);
			obj.put("describe", videoDes);
			obj.put("attribute", attriDefault);
			// 是否分享到视频广场 0/1 否/是
			obj.put("issquare", issquare);
			// 缩略图路径
			String fsFile = FileUtils.javaToLibPath(thumbFile);
			obj.put("imgpath", fsFile);

			// type: 1/2 精彩视频 / 紧急视频
			obj.put("type", "1");

			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

}
