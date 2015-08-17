package cn.com.mobnote.golukmobile;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
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

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

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
	private FullScreenVideoView mFullVideoView = null;
	private ImageView mImageLike = null;
	private TextView mTextLike = null;
	private TextView mTextLikeAll, mTextLookAll;
	private LinearLayout mLayoutShowAll = null;
	/** 评论内容 **/
	private LinearLayout hasCommentLayout = null;
	private ImageView mImageNoInput = null;
	private TextView mTextNoComment = null;
	private TextView mTextAutor, mTextCommentCount, mTextCommentFirst, mTextCommentSecond, mTextCommenThird;
	private TextView mTextLink = null;
	private LinearLayout mLayoutPraise, mLayoutComment, mLayoutShare;
	/** 所有数据 **/
	private LinearLayout mLayoutAllInfo = null;
	private LinearLayout mLayoutShowComment = null;
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
	/** 暂停标识 */
	private boolean isPause = false;
	/** 点赞标识 **/
	// private boolean isZanOk = false;
	private String isPraise = "0";
	private int likeNumber = 0;
	/** 猛戳刷新 **/
	private ImageView mImageToRefresh = null;

	private ConnectivityManager connectivityManager = null;
	private NetworkInfo netInfo = null;
	private RelativeLayout mImageLayout;
	private CustomDialog mCustomDialog;
	private Timer timer = null;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_detail);
		mContext = this;
		connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		netInfo = connectivityManager.getActiveNetworkInfo();
		initView();
		mCustomStartDialog = new CustomLoadingDialog(mContext, null);
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
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onResume  " + isPause);
		if (isPause) {
			isPause = false;
			// if (playTime != 0) {
			// if (0 != duration) {
			// mSeekBar.setProgress(playTime * 100 / duration);
			// }
			// mFullVideoView.seekTo(playTime);
			// }
			showLoading();
			mPlayBtn.setVisibility(View.GONE);
			mImageLayout.setVisibility(View.VISIBLE);
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
		mFullVideoView = (FullScreenVideoView) findViewById(R.id.video_detail_videoview);
		mImageLike = (ImageView) findViewById(R.id.video_square_detail_like);
		mTextLike = (TextView) findViewById(R.id.zanText);
		mTextLikeAll = (TextView) findViewById(R.id.video_detail_like_all);
		mTextLookAll = (TextView) findViewById(R.id.video_detail_look_all);
		mLayoutShowAll = (LinearLayout) findViewById(R.id.video_detail_comment_content);
		mLayoutShowComment = (LinearLayout) findViewById(R.id.video_square_show_all);
		// 评论内容
		hasCommentLayout = (LinearLayout) findViewById(R.id.video_square_has_comment_layout);
		mImageNoInput = (ImageView) findViewById(R.id.video_square_no_comment_img);
		mTextNoComment = (TextView) findViewById(R.id.comment_noinput);
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
		mImageToRefresh = (ImageView) findViewById(R.id.video_square_detail_click_refresh);
		mImageLayout = (RelativeLayout) findViewById(R.id.mImageLayout);

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
		mImageToRefresh.setOnClickListener(this);

		String title = getIntent().getStringExtra("title");
		if (null == title || "".equals(title)) {
			mTextTitle.setText("视频详情");
		} else {
			mTextTitle.setText(title);
		}
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
				netWorkTimeoutCheck();
				if (null == mFullVideoView) {
					return;
				}
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
				} else {
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

	/**
	 * 退出
	 * 
	 * @author jyf
	 * @date 2015年8月16日
	 */
	private void exit() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.removeVideoSquareManagerListener("videodetailshare");
		}
		this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void toComment(boolean isShowSoft) {
		Intent toComment = new Intent(VideoSquareDeatilActivity.this, CommentActivity.class);
		toComment.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoJson.data.avideo.video.videoid);
		toComment.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
		toComment.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, isShowSoft);
		toComment.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, noInput());
		toComment.putExtra(CommentActivity.COMMENT_KEY_USERID, mVideoJson.data.avideo.user.uid);
		startActivity(toComment);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			exit();
			break;
		// 评论
		case R.id.commentLayout:
			toComment(true);
			break;
		// 显示全部评论
		case R.id.video_detail_comment_content:
			toComment(false);
			break;
		// 外链接
		case R.id.video_square_link:
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
			} else {
				if ("1".equals(mVideoJson.data.link.showurl)) {
					Intent mBugLayout = new Intent(this, UserOpenUrlActivity.class);
					mBugLayout.putExtra("url", mVideoJson.data.link.outurl);
					startActivity(mBugLayout);
				}
			}
			break;
		// 点赞
		case R.id.praiseLayout:
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
			} else {
				clickPraise();
			}
			break;
		// 分享
		case R.id.shareLayout:
			boolean result = GolukApplication.getInstance().getVideoSquareManager()
					.getShareUrl(mVideoJson.data.avideo.video.videoid, mVideoJson.data.avideo.video.type);
			GolukDebugUtils.i("detail", "--------result-----Onclick------" + result);
			if (!result) {
				GolukUtils.showToast(this, "网络异常，请检查网络");
			} else {
				mCustomLoadingDialog = new CustomLoadingDialog(mContext, null);
				mCustomLoadingDialog.show();
			}
			break;
		case R.id.play_btn:
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
				return;
			}
			if (isBuffering) {
				return;
			}
			if (mFullVideoView.isPlaying()) {
				mFullVideoView.pause();
				isPause = true;
				mPlayBtn.setVisibility(View.VISIBLE);
				mPlayBtn.setImageResource(R.drawable.btn_player_play);
			} else {
				playVideo();
				mFullVideoView.start();
				showLoading();
				mPlayBtn.setVisibility(View.GONE);
			}
			break;
		case R.id.mPlayerLayout:
			if (isBuffering) {
				return;
			}
			if (!isShow) {
				if (null != mFullVideoView) {
					if (mFullVideoView.isPlaying()) {
						mFullVideoView.pause();
						isPause = true;
						mPlayBtn.setVisibility(View.VISIBLE);
						mPlayBtn.setImageResource(R.drawable.btn_player_play);
					} else {
						mPlayBtn.setVisibility(View.GONE);
						mFullVideoView.start();
						showLoading();
					}
				}
			}
			break;
		case R.id.video_square_detail_click_refresh:
			getVideoDetailData();
			break;
		default:
			break;
		}
	}

	/**
	 * 获取网络视频详情数据
	 */
	public void getVideoDetailData() {
		Intent it = getIntent();
		if (null != it.getStringExtra("ztid")) {
			ztId = it.getStringExtra("ztid").toString();
			boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailData(ztId);
			if (!b) {
				mCustomStartDialog.close();
			} else {
				mCustomStartDialog.show();
			}
			mImageToRefresh.setVisibility(View.GONE);
			mLayoutAllInfo.setVisibility(View.GONE);
		}
	}

	/**
	 * 视频详情数据
	 * 
	 * @param jsonStr
	 */
	public void getData(String jsonStr) {
		mVideoJson = VideoDetailParser.parseDataFromJson(jsonStr);
		GolukDebugUtils.e("testtest", "------jsonStr--------" + jsonStr);
		if (!mVideoJson.success) {
			// TODO 后台数据异常
			mLayoutAllInfo.setVisibility(View.GONE);
			mImageToRefresh.setVisibility(View.VISIBLE);
			GolukDebugUtils.e("lily", "---------后台服务器数据异常-------" + mVideoJson);
			GolukUtils.showToast(mContext, "数据异常，请重试");
		} else {
			mVideoJsonList = new ArrayList<VideoJson>();
			mVideoJsonList.add(mVideoJson);
			UserUtils.focusHead(mVideoJson.data.avideo.user.headportrait, mImageHead);
			mTextName.setText(mVideoJson.data.avideo.user.nickname);
			mTextTime.setText(GolukUtils.getCommentShowFormatTime(mVideoJson.data.avideo.video.sharingtime));
			// 点赞数、评论数、观看数
			mTextLookAll.setText(GolukUtils.getFormatNumber(mVideoJson.data.avideo.video.clicknumber));
			mTextLikeAll.setText(GolukUtils.getFormatNumber(mVideoJson.data.avideo.video.praisenumber));
			mTextCommentCount.setText(GolukUtils.getFormatNumber(mVideoJson.data.avideo.video.comment.comcount));
			if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {
				mImageLike.setImageResource(R.drawable.videodetail_like);
				mTextLike.setTextColor(Color.rgb(136, 136, 136));
			} else {
				mImageLike.setImageResource(R.drawable.videodetail_like_press);
				mTextLike.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
			}

			showText(mTextAutor, mVideoJson.data.avideo.user.nickname, mVideoJson.data.avideo.video.describe);
			// 三条评论
			if ("1".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
				hasCommentLayout.setVisibility(View.VISIBLE);
				mImageNoInput.setVisibility(View.GONE);
				mTextNoComment.setVisibility(View.GONE);
				List<VideoListInfo> videoList = mVideoJson.data.avideo.video.comment.comlist;
				if (null != videoList) {
					if (null != mVideoJson.data.avideo.video.comment.comcount
							&& !"".equals(mVideoJson.data.avideo.video.comment.comcount)) {
						int commentCount = Integer.parseInt(mVideoJson.data.avideo.video.comment.comcount);
						if (commentCount <= 3) {
							mLayoutShowComment.setVisibility(View.GONE);
						} else {
							mLayoutShowComment.setVisibility(View.VISIBLE);
						}
					} else {
						mLayoutShowComment.setVisibility(View.GONE);
					}
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
					mTextNoComment.setVisibility(View.GONE);
					mImageNoInput.setVisibility(View.VISIBLE);
				}
			} else {
				hasCommentLayout.setVisibility(View.GONE);
				mTextNoComment.setVisibility(View.VISIBLE);
				mImageNoInput.setVisibility(View.GONE);
			}
			// 下载视频第一帧截图

			mImageLayout.removeAllViews();
			RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);

			SimpleDraweeView view = new SimpleDraweeView(this);
			GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
			GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
					.setPlaceholderImage(getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
					.setFailureImage(getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
					.setActualImageScaleType(ScaleType.FIT_XY).build();
			view.setHierarchy(hierarchy);
			view.setImageURI(Uri.parse(mVideoJson.data.avideo.video.picture));
			mImageLayout.addView(view, mPreLoadingParams);
			// 外链接
			if ("0".equals(mVideoJson.data.link.showurl)) {
				mTextLink.setVisibility(View.GONE);
			} else {
				mTextLink.setVisibility(View.VISIBLE);
				mTextLink.setText(mVideoJson.data.link.outurlname);
			}

			if ((netInfo != null) && (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
				playVideo();
				mFullVideoView.start();
				showLoading();
			} else {
				mImageLayout.setVisibility(View.VISIBLE);
				mPlayBtn.setVisibility(View.VISIBLE);
				mPlayBtn.setImageResource(R.drawable.btn_player_play);
			}
		}

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
								describe = "#极路客精彩视频#";
							} else {
								describe = "#极路客精彩视频分享#";
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
									describe, ttl, bitmap, realDesc, mVideoJson.data.avideo.video.videoid);
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
				mImageToRefresh.setVisibility(View.GONE);
				String jsonStr = (String) param2;
				getData(jsonStr);
			} else {
				mCustomStartDialog.close();
				mLayoutAllInfo.setVisibility(View.GONE);
				mImageToRefresh.setVisibility(View.VISIBLE);
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}
		} else if (event == VSquare_Req_VOP_Praise) {
			GolukDebugUtils.e("lily", "222VideoSuqare_CallBack=@@@@Get_VideoDetail==event=" + event + "=msg=" + msg
					+ "=param1=" + param1 + "=param2=" + param2);
			if (msg == RESULE_SUCESS) {
				// {"data":{"result":"3"},"msg":"视频不存在","success":false}
				try {
					String jsonStr = (String) param2;
					JSONObject jsonObject = new JSONObject(jsonStr);
					JSONObject dataObject = jsonObject.optJSONObject("data");
					String result = dataObject.optString("result");
					if ("0".equals(result)) {
						// 成功
					} else {
						// 错误
						GolukUtils.showToast(mContext, "视频点赞异常，请稍后再试");
					}
				} catch (Exception e) {
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
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
		}
		return t_bitmap;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != sharePlatform) {
			sharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * 点赞
	 */
	public void clickPraise() {
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {// 没有点过赞
			likeNumber = Integer.parseInt(mTextLikeAll.getText().toString().replace(",", "")) + 1;
			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mTextLikeAll.setText(df.format(likeNumber));
			} else {
				mTextLikeAll.setText("100,000+");
			}
			mImageLike.setImageResource(R.drawable.videodetail_like_press);
			mTextLike.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
			isPraise = "1";
			boolean b = GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
			if (b) {
			}
		} else {
			likeNumber = Integer.parseInt(mTextLikeAll.getText().toString().replace(",", "")) - 1;

			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mTextLikeAll.setText(df.format(likeNumber));
			} else {
				mTextLikeAll.setText("100,000+");
			}

			mImageLike.setImageResource(R.drawable.videodetail_like);
			mTextLike.setTextColor(Color.rgb(136, 136, 136));
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
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------showLoading : isShow " + isShow);
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

	private boolean isCallVideo = false;

	private void playVideo() {
		if (isCallVideo) {
			return;
		}
		isCallVideo = true;
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
			mImageLayout.setVisibility(View.GONE);
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
	 * 
	 * @param msg
	 *            提示信息
	 */
	private void dialog(String msg) {
		if (null == mCustomDialog) {
			mCustomDialog = new CustomDialog(this);
			mCustomDialog.setCancelable(false);
			mCustomDialog.setMessage(msg, Gravity.CENTER);
			mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
				@Override
				public void onClickListener() {
					if (timer != null)
						timer.cancel();
					finish();
				}
			});
			if (!this.isFinishing()) {
				mCustomDialog.show();
			}
		}
	}

	int originProgress = 0;
	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			originProgress = mSeekBar.getProgress();
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				// int time = progress * mFullVideoView.getDuration() / 100;
				// mFullVideoView.seekTo(time);
				mSeekBar.setProgress(originProgress);
			}
		}
	};

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onInfoListener有警告或者错误信息时调用（开始缓冲、缓冲结束）
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onInfo : arg1 " + arg1);
		switch (arg1) {
		case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
			callBack_realStart();
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			isBuffering = true;
			if (0 == mFullVideoView.getCurrentPosition()) {
				mImageLayout.setVisibility(View.VISIBLE);
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
		mImageLayout.setVisibility(View.VISIBLE);
		dialog(msg);
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO OnCompletionListener视频播放完后进度条回到初始位置
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onCompletion :  ");
		if (error) {
			return;
		}
		if (netInfo.getType() != ConnectivityManager.TYPE_WIFI) {
			mPlayBtn.setVisibility(View.VISIBLE);
			mPlayBtn.setImageResource(R.drawable.btn_player_play);
			mImageLayout.setVisibility(View.VISIBLE);
		}
		mFullVideoView.seekTo(0);
		mSeekBar.setProgress(0);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO OnPreparedListener 视频播放之前的一个视频准备工作，准备完成后调用此方法
		if (null == mFullVideoView) {
			return;
		}
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onPrepared :  ");
		mFullVideoView.setVideoWidth(mp.getVideoWidth());
		mFullVideoView.setVideoHeight(mp.getVideoHeight());
		if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			mp.setLooping(true);
		}
		if (playTime != 0) {
			mFullVideoView.seekTo(playTime);
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(1);
			}
		}, 0, 1000);
	}

	private void callBack_realStart() {
		mPlayBtn.setVisibility(View.GONE);
		// mFullVideoView.start();
		mImageLayout.setVisibility(View.GONE);
		hideLoading();

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
					mImageLayout.setVisibility(View.VISIBLE);
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
		if (null == mFullVideoView) {
			return;
		}
		boolean isPlaying = mFullVideoView.isPlaying();
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onPause : isPause  " + isPause);
		if (isPlaying) {
			isPause = true;
			playTime = mFullVideoView.getCurrentPosition();
			mFullVideoView.pause();
			mImageLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 根据iscomment字段，判断是否允许评论
	 * 
	 * @return
	 */
	private boolean noInput() {
		if ("1".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			return true;
		} else {
			return false;
		}
	}

}
