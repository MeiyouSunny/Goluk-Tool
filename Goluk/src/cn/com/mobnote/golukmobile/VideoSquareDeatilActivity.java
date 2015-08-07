package cn.com.mobnote.golukmobile;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
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
public class VideoSquareDeatilActivity extends BaseActivity implements OnClickListener, VideoSuqareManagerFn {

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
	private VideoView mVideoView = null;
	private ImageView mImageLike;
	private TextView mTextLikeAll, mTextLookAll;
	private LinearLayout mLayoutShowAll = null;
	/** 评论内容 **/
	private LinearLayout hasCommentLayout = null;
	private RelativeLayout noCommentLayout = null;
	private TextView mTextAutor, mTextCommentCount, mTextCommentFirst, mTextCommentSecond, mTextCommenThird;
	private TextView mTextLink = null;
	private LinearLayout mLayoutPraise, mLayoutComment, mLayoutShare;

	/** 数据 **/
	public CustomLoadingDialog mCustomLoadingDialog, mCustomStartDialog;
	private SharePlatformUtil sharePlatform;
	private Context mContext;

	private VideoJson mVideoJson = null;
	private List<VideoJson> mVideoJsonList = null;
	private String ztId = null;

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

		// 注册视频详情监听
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("videodetail", this);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// 注册分享监听
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videodetailshare", this);
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
		mVideoView = (VideoView) findViewById(R.id.video_detail_videoview);
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

		mLayoutPraise = (LinearLayout) findViewById(R.id.praiseLayout);
		mLayoutComment = (LinearLayout) findViewById(R.id.commentLayout);
		mLayoutShare = (LinearLayout) findViewById(R.id.shareLayout);

		mTextTitle.setText("视频详情");

		// 点击事件
		mBackBtn.setOnClickListener(this);
		mLayoutShowAll.setOnClickListener(this);
		mTextLink.setOnClickListener(this);
		mLayoutPraise.setOnClickListener(this);
		mLayoutComment.setOnClickListener(this);
		mLayoutShare.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		// 评论
		case R.id.commentLayout:
			Intent toComment = new Intent(VideoSquareDeatilActivity.this,CommentActivity.class);
			toComment.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoJson.data.avideo.video.videoid);
			toComment.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
			toComment.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, true);
			toComment.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, true);
			startActivity(toComment);
			break;
		// 显示全部评论
		case R.id.video_square_show_all:
			Intent showComment = new Intent(VideoSquareDeatilActivity.this,CommentActivity.class);
			showComment.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoJson.data.avideo.video.videoid);
			showComment.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
			showComment.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, false);
			showComment.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, true);
			startActivity(showComment);
			break;
		// 外链接
		case R.id.video_square_link:

			break;
		// 点赞
		case R.id.praiseLayout:
			clickPraise();
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
		default:
			break;
		}
	}

	/**
	 * 获取网络视频详情数据
	 */
	public void getVideoDetailData() {
		boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailData("zt001");
		if (!b) {
			mCustomStartDialog.close();
		}
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
		Uri uri = null;
		if ("1".equals(mVideoJson.data.avideo.video.type)) {
			uri = Uri.parse(mVideoJson.data.avideo.video.livesdkaddress);
		} else if ("2".equals(mVideoJson.data.avideo.video.type)) {
			uri = Uri.parse(mVideoJson.data.avideo.video.ondemandwebaddress);
		}
		mImagePrepare.setVisibility(View.GONE);
		mVideoView.setVisibility(View.VISIBLE);
		mVideoView.setVideoURI(uri);
		MediaController controller = new MediaController(this);
		mVideoView.setMediaController(controller);
		mVideoView.requestFocus();
		mVideoView.start();
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
		// TODO 外链

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
	 * 分享
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
			if (RESULE_SUCESS == msg) {
				GolukDebugUtils.e("xuhw", "111VideoSuqare_CallBack=@@@@Get_VideoDetail==event=" + event + "=msg=" + msg
						+ "=param1=" + param1 + "=param2=" + param2);
				if (RESULE_SUCESS == msg) {
					mCustomStartDialog.close();
					String jsonStr = (String) param2;
					getData(jsonStr);
				} else {
					mCustomStartDialog.close();
					GolukUtils.showToast(this, "网络异常，请检查网络");
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
		String isPraise = "0";
		int likeNumber = 0;
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {// 没有点过赞
			likeNumber = Integer.parseInt(mTextLikeAll.getText().toString().replace(",", "")) + 1;
			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mTextLikeAll.setText(df.format(likeNumber));
			} else {
				mTextLikeAll.setText("100,000+");
			}

			mImageLike.setBackgroundResource(R.drawable.videodetail_like_press);
			isPraise = "1";
			GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
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

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		if(mVideoView != null){
			if(mVideoView.isPlaying()){
				
			}
		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
}
