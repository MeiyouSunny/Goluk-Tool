package cn.com.mobnote.golukmobile;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
import cn.com.mobnote.golukmobile.player.FullScreenVideoView;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.videodetail.VideoDetailParser;
import cn.com.mobnote.videodetail.VideoJson;
import cn.com.mobnote.videodetail.VideoListInfo;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 视频详情
 * 
 * @author mobnote
 *
 */
@SuppressLint("NewApi")
public class VideoSquareDeatilActivity extends BaseActivity implements OnClickListener, VideoSuqareManagerFn,
		OnInfoListener, OnErrorListener, OnCompletionListener, OnPreparedListener {

	/** title **/
	private ImageButton mBackBtn = null;
	private TextView mTextTitle = null;
	/** 用户信息 **/
	private ImageView mImageHead = null;
	private TextView mTextName = null;
	private TextView mTextTime = null;
	/** 视频内容 **/
	private RelativeLayout mPrepareLayout = null;
	private ImageView mImagePrepare = null;
	private FullScreenVideoView mFullVideoView = null;
	private ImageView mImageLike;
	private TextView mTextLikeAll, mTextLookAll;
	private LinearLayout mLayoutShowAll = null;
	/** 评论内容 **/
	private LinearLayout hasCommentLayout = null;
	private RelativeLayout noCommentLayout = null;
	private TextView mTextAutor, mTextCommentCount, mTextCommentFirst, mTextCommentSecond, mTextCommenThird;
	private TextView mTextLink = null;
	private LinearLayout mLayoutPraise, mLayoutComment, mLayoutShare;
	/** 所有数据 **/
	private LinearLayout mLayoutAllInfo = null;

	/** 数据 **/
	public CustomLoadingDialog mCustomLoadingDialog, mCustomStartDialog;
	private SharePlatformUtil sharePlatform;
	private Context mContext;

	private VideoJson mVideoJson = null;
	private List<VideoJson> mVideoJsonList = null;
	private String ztId = null;

	/** 视频播放/暂停 **/
	private ImageView mPlayBtn = null;
	private SeekBar mSeekBar = null;
	private LinearLayout mLoadingLayout = null;
	private ImageView mLoading = null;

	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	private boolean isShow = false;
	/** 缓冲标识 */
	private boolean isBuffering = false;
	/** 播放器报错标识 */
	private boolean error = false;
	/** 视频播放时间 */
	private int playTime = 0;
	/** 播放重置标识 */
	private boolean reset = false;
	/** 网络连接超时 */
	private int networkConnectTimeOut = 0;
	private int duration = 0;
	private boolean isStop = false;
	/** 暂停标识 */
	private boolean isPause = false;
	/** 点赞标识 **/
	private boolean isZanOk = false;
	private String isPraise = "0";
	private int likeNumber = 0;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_detail);
		mContext = this;

		initView();
		// ---------------------
		Intent it = getIntent();
		if (null != it.getStringExtra("ztid")) {
			ztId = it.getStringExtra("ztid").toString();
		}

		mCustomStartDialog = new CustomLoadingDialog(mContext, null);
		mCustomStartDialog.show();
		// 获取视频详情数据
		getVideoDetailData();

		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数

		// 注册监听
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("videodetailshare", this);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isStop) {
			isStop = false;
			showLoading();
			mImagePrepare.setVisibility(View.VISIBLE);
		}
		if (isPause) {
			isPause = false;
			if (playTime != 0) {
				if (0 != duration) {
					mSeekBar.setProgress(playTime * 100 / duration);
				}
				mFullVideoView.seekTo(playTime);
			}
			mFullVideoView.start();
		}
	}

	// 初始化
	public void initView() {
		// title
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		// 用户信息
		mImageHead = (ImageView) findViewById(R.id.user_head);
		mTextName = (TextView) findViewById(R.id.user_name);
		mTextTime = (TextView) findViewById(R.id.user_time);
		// 视频内容
		mPrepareLayout = (RelativeLayout) findViewById(R.id.mPlayerLayout);
		mImagePrepare = (ImageView) findViewById(R.id.mPreLoading);
		mFullVideoView = (FullScreenVideoView) findViewById(R.id.video_detail_videoview);
		mImageLike = (ImageView) findViewById(R.id.video_square_detail_like);
		mTextLikeAll = (TextView) findViewById(R.id.video_detail_like_all);
		mTextLookAll = (TextView) findViewById(R.id.video_detail_look_all);
		mLayoutShowAll = (LinearLayout) findViewById(R.id.video_square_show_all);
		// 评论内容
		hasCommentLayout = (LinearLayout) findViewById(R.id.video_square_has_comment_layout);
		noCommentLayout = (RelativeLayout) findViewById(R.id.video_square_no_comment_layout);
		mTextAutor = (TextView) findViewById(R.id.video_square_author);
		mTextCommentCount = (TextView) findViewById(R.id.video_square_comment_count);
		mTextCommentFirst = (TextView) findViewById(R.id.video_square_comment_first);
		mTextCommentSecond = (TextView) findViewById(R.id.video_square_comment_second);
		mTextCommenThird = (TextView) findViewById(R.id.video_square_comment_three);
		mTextLink = (TextView) findViewById(R.id.video_square_link);
		// 点赞、评论、分享
		mLayoutPraise = (LinearLayout) findViewById(R.id.praiseLayout);
		mLayoutComment = (LinearLayout) findViewById(R.id.commentLayout);
		mLayoutShare = (LinearLayout) findViewById(R.id.shareLayout);
		// 视频播放/暂停
		mPlayBtn = (ImageView) findViewById(R.id.play_btn);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLayoutAllInfo = (LinearLayout) findViewById(R.id.video_square_detail_show_allinfo);

		mTextTitle.setText("视频详情");

		mImagePrepare.setVisibility(View.VISIBLE);
		mImagePrepare.setBackgroundResource(R.drawable.tacitly_pic);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();

		// 点击事件
		mBackBtn.setOnClickListener(this);
		mLayoutShowAll.setOnClickListener(this);
		mTextLink.setOnClickListener(this);
		mLayoutPraise.setOnClickListener(this);
		mLayoutComment.setOnClickListener(this);
		mLayoutShare.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
		mPrepareLayout.setOnClickListener(this);

		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mFullVideoView.setOnPreparedListener(this);
		mFullVideoView.setOnErrorListener(this);
		if (GolukUtils.getSystemSDK() >= 17) {
			try {
				mFullVideoView.setOnInfoListener(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mFullVideoView.setOnCompletionListener(this);
		showLoading();

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (error) {
					return;
				}
				if (null == mFullVideoView) {
					return;
				}
				netWorkTimeoutCheck();
				if (mFullVideoView.getCurrentPosition() > 0) {
					if (!mFullVideoView.isPlaying()) {
						return;
					}
					if (!isBuffering) {
						hideLoading();
					}
					playTime = 0;
					duration = mFullVideoView.getDuration();
					int progress = mFullVideoView.getCurrentPosition() * 100 / mFullVideoView.getDuration();
					mSeekBar.setProgress(progress);
					if (mFullVideoView.getCurrentPosition() > mFullVideoView.getDuration() - 100) {
						mSeekBar.setProgress(0);
					}
					mPlayBtn.setVisibility(View.GONE);
				} else {
					mPlayBtn.setVisibility(View.VISIBLE);
					mPlayBtn.setImageResource(R.drawable.player_play_btn);
					if (0 != duration) {
						mSeekBar.setProgress(playTime * 100 / duration);
					} else {
						mSeekBar.setProgress(0);
					}
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		// 评论
		case R.id.commentLayout:
			Intent toComment = new Intent(VideoSquareDeatilActivity.this, CommentActivity.class);
			toComment.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoJson.data.avideo.video.videoid);
			toComment.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
			toComment.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, true);
			toComment.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, true);
			startActivity(toComment);
			break;
		// 显示全部评论
		case R.id.video_square_show_all:
			Intent showComment = new Intent(VideoSquareDeatilActivity.this, CommentActivity.class);
			showComment.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoJson.data.avideo.video.videoid);
			showComment.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
			showComment.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, false);
			showComment.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, true);
			startActivity(showComment);
			break;
		// 外链接
		case R.id.video_square_link:
			Intent mBugLayout = new Intent(this, UserOpenUrlActivity.class);
			mBugLayout.putExtra("url", "http://www.goluk.com");
			startActivity(mBugLayout);
			break;
		// 点赞
		case R.id.praiseLayout:
			GolukDebugUtils.e("lily", "----------点赞状态判断-------"+isZanOk);
			if(isZanOk){
				clickPraise();
			}else{
				GolukUtils.showToast(mContext, "系统繁忙，请稍候再试");
			}
			break;
		// 分享
		case R.id.shareLayout:
			mCustomLoadingDialog = new CustomLoadingDialog(mContext, null);
			mCustomLoadingDialog.show();
			boolean result = GolukApplication.getInstance().getVideoSquareManager()
					.getShareUrl(mVideoJson.data.avideo.video.videoid, mVideoJson.data.avideo.video.type);
			GolukDebugUtils.i("detail", "--------result-----Onclick------" + result);
			if (!result) {
				mCustomLoadingDialog.close();
			}
			break;
		case R.id.play_btn:
			if (isBuffering) {
				return;
			}
			if (mFullVideoView.isPlaying()) {
				mFullVideoView.pause();
				mPlayBtn.setVisibility(View.VISIBLE);
				mPlayBtn.setImageResource(R.drawable.btn_video_detail_play);
			} else {
				mFullVideoView.start();
				mPlayBtn.setVisibility(View.GONE);
			}
			break;
		case R.id.mPlayerLayout:
			if (isBuffering) {
				return;
			}
			if(!isShow){
				if(null != mFullVideoView){
					if(mFullVideoView.isPlaying()){
						mFullVideoView.pause();
						mPlayBtn.setVisibility(View.VISIBLE);
						mPlayBtn.setImageResource(R.drawable.btn_video_detail_play);
					}else{
						mPlayBtn.setVisibility(View.GONE);
						mFullVideoView.start();
					}
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 获取网络视频详情数据
	 */
	public void getVideoDetailData() {
		boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailData(ztId);
		if (!b) {
			mCustomStartDialog.close();
		}
		mLayoutAllInfo.setVisibility(View.GONE);
	}

	/**
	 * 视频详情数据
	 * 
	 * @param jsonStr
	 */
	public void getData(String jsonStr) {
		mVideoJson = VideoDetailParser.parseDataFromJson(jsonStr);
		mVideoJsonList = new ArrayList<VideoJson>();
		mVideoJsonList.add(mVideoJson);
		UserUtils.focusHead(mVideoJson.data.avideo.user.headportrait, mImageHead);
		mTextName.setText(mVideoJson.data.avideo.user.nickname);
		mTextTime.setText(formatTime(mVideoJson.data.avideo.video.sharingtime));
		// TODO 视频播放
		if (!TextUtils.isEmpty(mVideoJson.data.avideo.video.picture)) {
			BitmapManager.getInstance().mBitmapUtils.display(mImagePrepare, mVideoJson.data.avideo.video.picture);
		}
		// 点赞数、评论数、观看数
		DecimalFormat df = new DecimalFormat("#,###");
		int wg_click = Integer.parseInt(mVideoJson.data.avideo.video.clicknumber);
		int wg_praise = Integer.parseInt(mVideoJson.data.avideo.video.praisenumber);
		int wg_comment = Integer.parseInt(mVideoJson.data.avideo.video.comment.comcount);
		if (wg_click < 100000 || wg_praise < 100000 || wg_comment < 100000) {
			mTextLookAll.setText(df.format(wg_click));
			mTextLikeAll.setText(df.format(wg_praise));
			mTextCommentCount.setText(df.format(wg_comment));
		} else {
			mTextLookAll.setText("100,000+");
			mTextLikeAll.setText("100,000+");
			mTextCommentCount.setText("100,000+");
		}
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {
			mImageLike.setBackgroundResource(R.drawable.videodetail_like);
		} else {
			mImageLike.setBackgroundResource(R.drawable.videodetail_like_press);
		}

		showText(mTextAutor, mVideoJson.data.avideo.user.nickname, mVideoJson.data.avideo.video.describe);
		// 三条评论
		if ("1".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			hasCommentLayout.setVisibility(View.VISIBLE);
			noCommentLayout.setVisibility(View.GONE);
			List<VideoListInfo> videoList = mVideoJson.data.avideo.video.comment.comlist;
			for (int i = 0; i < videoList.size(); i++) {
				if (i == 0) {
					showText(mTextCommentFirst, videoList.get(i).name, videoList.get(i).text);
				}
				if (i == 1) {
					showText(mTextCommentSecond, videoList.get(i).name, videoList.get(i).text);
				}
				if (i == 2) {
					showText(mTextCommenThird, videoList.get(i).name, videoList.get(i).text);
				}
			}
		} else {
			hasCommentLayout.setVisibility(View.GONE);
			noCommentLayout.setVisibility(View.VISIBLE);
		}
		playVideo();
	}

	/**
	 * 显示评论信息
	 * 
	 * @param view
	 * @param nikename
	 * @param text
	 */
	private void showText(TextView textView, String nikeName, String text) {
		String t_str = nikeName + "  " + text;
		SpannableStringBuilder style = new SpannableStringBuilder(t_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikeName.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		textView.setText(style);
	}

	/**
	 * 格式化时间
	 * 
	 * @param date
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			try {
				Date strtodate = formatter.parse(date);
				if (null != strtodate) {
					formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
					if (null != formatter) {
						time = formatter.format(strtodate);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return time;
	}

	/**
	 * 分享、获取视频详情、点赞
	 * 
	 */
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.i("detail", "-------VideoSuqare_CallBack------event----" + event + "------msg----" + msg
				+ "------param1----" + param1);
		if (event == VSquare_Req_VOP_GetShareURL_Video) {
			if (RESULE_SUCESS == msg) {
				try {
					JSONObject result = new JSONObject((String) param2);
					if (result.getBoolean("success")) {
						JSONObject data = result.getJSONObject("data");
						GolukDebugUtils.i("detail", "------VideoSuqare_CallBack--------data-----" + data);
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						String describe = data.optString("describe");
						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";

						if (TextUtils.isEmpty(describe)) {
							if ("1".equals(mVideoJson.data.avideo.video.type)) {
								describe = "#极路客直播#";
							} else {
								describe = "#极路客精彩视频#";
							}
						}
						String ttl = "极路客精彩视频分享";
						if ("1".equals(mVideoJson.data.avideo.video.type)) {// 直播
							ttl = mVideoJson.data.avideo.user.nickname + "的直播视频分享";
							realDesc = ttl + "(使用#极路客Goluk#拍摄)";
						}
						// 缩略图
						Bitmap bitmap = getThumbBitmap(mVideoJson.data.avideo.video.picture);
						if (this != null && !this.isFinishing()) {
							this.mCustomLoadingDialog.close();
							CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shareurl, coverurl,
									describe, ttl, bitmap, realDesc);
							shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
						}
					} else {
						GolukUtils.showToast(this, "网络异常，请检查网络");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				mCustomLoadingDialog.close();
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}
		} else if (event == VSquare_Req_Get_VideoDetail) {
			GolukDebugUtils.e("lily", "111VideoSuqare_CallBack=@@@@Get_VideoDetail==event=" + event + "=msg=" + msg
					+ "=param1=" + param1 + "=param2=" + param2);
			if (RESULE_SUCESS == msg) {
				mCustomStartDialog.close();
				mLayoutAllInfo.setVisibility(View.VISIBLE);
				String jsonStr = (String) param2;
				getData(jsonStr);
			} else {
				mCustomStartDialog.close();
				mLayoutAllInfo.setVisibility(View.GONE);
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}
		} else if (event == VSquare_Req_VOP_Praise) {
			GolukDebugUtils.e("lily", "222VideoSuqare_CallBack=@@@@Get_VideoDetail==event=" + event + "=msg=" + msg
					+ "=param1=" + param1 + "=param2=" + param2);
			if (msg == RESULE_SUCESS) {
				//{"data":{"result":"3"},"msg":"视频不存在","success":false}
				isZanOk = true;
				try{
					String jsonStr = (String) param2;
					JSONObject jsonObject = new JSONObject(jsonStr);
					JSONObject dataObject = jsonObject.optJSONObject("data");
					String result = dataObject.optString("result");
					if("0".equals(result)){
						//成功
						likeNumber = Integer.parseInt(mTextLikeAll.getText().toString().replace(",", "")) + 1;
						DecimalFormat df = new DecimalFormat("#,###");
						if (likeNumber < 100000) {
							mTextLikeAll.setText(df.format(likeNumber));
						} else {
							mTextLikeAll.setText("100,000+");
						}
						mImageLike.setBackgroundResource(R.drawable.videodetail_like_press);
						isPraise = "1";
					}else{
						//错误
						GolukUtils.showToast(mContext, "视频点赞异常，请稍后再试");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (null == file) {
			return null;
		}
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 100, 100);
		}
		return t_bitmap;
	}

	/**
	 * 点赞
	 */
	public void clickPraise() {
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {// 没有点过赞
			/*likeNumber = Integer.parseInt(mTextLikeAll.getText().toString().replace(",", "")) + 1;
			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mTextLikeAll.setText(df.format(likeNumber));
			} else {
				mTextLikeAll.setText("100,000+");
			}
			mImageLike.setBackgroundResource(R.drawable.videodetail_like_press);
			isPraise = "1";*/
			boolean b = GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
			if(b){
				isZanOk = true;
			}else{
				isZanOk = false;
			}
		} else {
			likeNumber = Integer.parseInt(mTextLikeAll.getText().toString().replace(",", "")) - 1;

			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mTextLikeAll.setText(df.format(likeNumber));
			} else {
				mTextLikeAll.setText("100,000+");
			}

			mImageLike.setBackgroundResource(R.drawable.videodetail_like);
			isPraise = "0";
		}
		// 为点赞数重新赋值
		String videoId = mVideoJson.data.avideo.video.videoid;
		for (int i = 0; i < mVideoJsonList.size(); i++) {
			VideoJson videoJson = mVideoJsonList.get(i);
			if (videoJson.data.avideo.video.videoid.equals(videoId)) {
				mVideoJson.data.avideo.video.praisenumber = likeNumber + "";
				mVideoJsonList.get(i).data.avideo.video.praisenumber = likeNumber + "";
				mVideoJsonList.get(i).data.avideo.video.ispraise = isPraise;
				break;
			}
		}
	}

	/**
	 * 显示加载中布局
	 */
	private void showLoading() {
		if (!isShow) {
			isShow = true;
			mLoadingLayout.setVisibility(View.VISIBLE);
			mLoading.setVisibility(View.VISIBLE);
			mLoading.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mAnimationDrawable != null) {
						if (!mAnimationDrawable.isRunning()) {
							mAnimationDrawable.start();
						}
					}
				}
			}, 100);
		}
	}

	private void playVideo() {
		Uri uri = null;
		if ("1".equals(mVideoJson.data.avideo.video.type)) {
			uri = Uri.parse(mVideoJson.data.avideo.video.livesdkaddress);
		} else if ("2".equals(mVideoJson.data.avideo.video.type)) {
			uri = Uri.parse(mVideoJson.data.avideo.video.ondemandwebaddress);
		}
		mFullVideoView.setVideoURI(uri);
		mFullVideoView.requestFocus();
	}

	/**
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void hideLoading() {
		if (isShow) {
			isShow = false;
			mImagePrepare.setVisibility(View.GONE);
			if (mAnimationDrawable != null) {
				if (mAnimationDrawable.isRunning()) {
					mAnimationDrawable.stop();
				}
			}
			mLoadingLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 提示对话框
	 * @param msg   提示信息
	 */
	private void dialog(String msg) {
		CustomDialog mCustomDialog = new CustomDialog(this);
		mCustomDialog.setCancelable(false);
		mCustomDialog.setMessage(msg, Gravity.CENTER);
		mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				finish();
			}
		});
		mCustomDialog.show();
	}

	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				int time = progress * mFullVideoView.getDuration() / 100;
				mFullVideoView.seekTo(time);
			}
		}
	};

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onInfoListener有警告或者错误信息时调用（开始缓冲、缓冲结束）
		switch (arg1) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			isBuffering = true;
			if (0 == mFullVideoView.getCurrentPosition()) {
				mImagePrepare.setVisibility(View.VISIBLE);
			}
			showLoading();
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			isBuffering = false;
			hideLoading();
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onErrorListener
		if (error) {
			return false;
		}
		String msg = "播放错误";
		switch (arg1) {
		case 1:
		case -1010:
			msg = "视频出错，请重试！";
			break;
		case -110:
			msg = "网络访问异常，请重试！";
			break;

		default:
			break;
		}
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			msg = "网络访问异常，请重试！";
		}
		error = true;
		hideLoading();
		mImagePrepare.setVisibility(View.VISIBLE);
		dialog(msg);
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO OnCompletionListener视频播放完后进度条回到初始位置
		if (error) {
			return;
		}
		mFullVideoView.seekTo(0);
		mPlayBtn.setVisibility(View.VISIBLE);
		mPlayBtn.setImageResource(R.drawable.player_play_btn);
		mSeekBar.setProgress(0);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO OnPreparedListener  视频播放之前的一个视频准备工作，准备完成后调用此方法
		mFullVideoView.setVideoWidth(mp.getVideoWidth());
		mFullVideoView.setVideoHeight(mp.getVideoHeight());

		mFullVideoView.start();
		if (playTime != 0) {
			mFullVideoView.seekTo(playTime);
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(1);
			}
		}, 0, 1000);
	}

	/**
	 * 无网络超时检查
	 */
	private void netWorkTimeoutCheck() {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			networkConnectTimeOut++;
			if (networkConnectTimeOut > 15) {
				if (!reset) {
					hideLoading();
					mImagePrepare.setVisibility(View.VISIBLE);
					dialog("网络访问异常，请重试！");
					if (null != mFullVideoView) {
						mFullVideoView.stopPlayback();
						mFullVideoView = null;
					}
					return;
				}
			}
		} else {
			networkConnectTimeOut = 0;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mFullVideoView.isPlaying()) {
			isPause = true;
			playTime = mFullVideoView.getCurrentPosition();
			mFullVideoView.pause();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isBackground(this)) {
			isStop = true;
		}
	}

	public boolean isBackground(final Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

}
