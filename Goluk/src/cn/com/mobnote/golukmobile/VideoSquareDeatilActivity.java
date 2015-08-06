package cn.com.mobnote.golukmobile;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RingView;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.videodetail.VideoDetailParser;
import cn.com.mobnote.videodetail.VideoJson;
import cn.com.mobnote.videodetail.VideoListInfo;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	private RingView mRingView = null;
	private Button mBtnLike, mBtnComment, mBtnShare;
	private Button mBtnLikeAll, mBtnLookAll;
	private LinearLayout mLayoutShowAll = null;
	/** 评论内容 **/
	private LinearLayout hasCommentLayout = null;
	private RelativeLayout noCommentLayout = null;
	private TextView mTextAutor, mTextCommentCount, mTextCommentFirst, mTextCommentSecond, mTextCommenThird;
	private TextView mTextLink = null;

	/** 数据 **/
	public CustomLoadingDialog mCustomLoadingDialog = null;
	private SharePlatformUtil sharePlatform;
	private Context mContext;

	private VideoJson mVideoJson = null;
	private List<VideoJson> mVideoJsonList = null;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_detail);
		mContext = this;

		initView();
		String jsonStr = getFromAssets("json.json");
		GolukDebugUtils.e("detail", jsonStr);
		requestDetailCallback(jsonStr);

	}

	@Override
	protected void onResume() {
		super.onResume();
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
		mRingView = (RingView) findViewById(R.id.mRingView);
		mBtnLike = (Button) findViewById(R.id.video_square_detail_like);
		mBtnComment = (Button) findViewById(R.id.video_square_detail_comment);
		mBtnShare = (Button) findViewById(R.id.video_square_detail_share);
		mBtnLikeAll = (Button) findViewById(R.id.video_detail_like_all);
		mBtnLookAll = (Button) findViewById(R.id.video_detail_look_all);
		mLayoutShowAll = (LinearLayout) findViewById(R.id.video_square_show_all);
		// mVideoView = (VideoView) findViewById(R.id.videoview);
		// 评论内容
		hasCommentLayout = (LinearLayout) findViewById(R.id.video_square_has_comment_layout);
		noCommentLayout = (RelativeLayout) findViewById(R.id.video_square_no_comment_layout);
		mTextAutor = (TextView) findViewById(R.id.video_square_author);
		mTextCommentCount = (TextView) findViewById(R.id.video_square_comment_count);
		mTextCommentFirst = (TextView) findViewById(R.id.video_square_comment_first);
		mTextCommentSecond = (TextView) findViewById(R.id.video_square_comment_second);
		mTextCommenThird = (TextView) findViewById(R.id.video_square_comment_three);
		mTextLink = (TextView) findViewById(R.id.video_square_link);

		mTextTitle.setText("视频详情");

		// 点击事件
		mBackBtn.setOnClickListener(this);
		mRingView.setOnClickListener(this);
		mBtnComment.setOnClickListener(this);
		mLayoutShowAll.setOnClickListener(this);
		mTextLink.setOnClickListener(this);
		mBtnLike.setOnClickListener(this);
		mBtnShare.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.mRingView:

			break;
		case R.id.videoview:
			break;
		// 评论
		case R.id.video_square_detail_comment:
			GolukUtils.showToast(this, "跳到评论页");
			break;
		// 显示全部评论
		case R.id.video_square_show_all:
			GolukUtils.showToast(this, "跳到评论列表");
			break;
		// 外链接
		case R.id.video_square_link:

			break;
		// 点赞
		case R.id.video_square_detail_like:
			clickPraise();
			break;
		// 分享
		case R.id.video_square_detail_share:
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
	 * 视频详情
	 * 
	 * @param jsonStr
	 */
	public void requestDetailCallback(String jsonStr) {
		mVideoJson = VideoDetailParser.parseDataFromJson(jsonStr);
		mVideoJsonList = new ArrayList<VideoJson>();
		mVideoJsonList.add(mVideoJson);
		UserUtils.focusHead(mVideoJson.data.avideo.user.headportrait, mImageHead);
		mTextName.setText(mVideoJson.data.avideo.user.nickname);
		mTextTime.setText(this.formatTime(mVideoJson.data.avideo.video.sharingtime));
		mTextTime.setText(mVideoJson.data.avideo.video.sharingtime);
		// TODO 视频播放
		// 点赞数、评论数、观看数
		DecimalFormat df = new DecimalFormat("#,###");
		int wg_click = Integer.parseInt(mVideoJson.data.avideo.video.clicknumber);
		int wg_praise = Integer.parseInt(mVideoJson.data.avideo.video.praisenumber);
		int wg_comment = Integer.parseInt(mVideoJson.data.avideo.video.comment.comcount);
		if (wg_click < 100000 || wg_praise < 100000 || wg_comment < 100000) {
			mBtnLookAll.setText(df.format(wg_click));
			mBtnLikeAll.setText(df.format(wg_praise));
			mTextCommentCount.setText(df.format(wg_comment));
		} else {
			mBtnLookAll.setText("100,000+");
			mBtnLikeAll.setText("100,000+");
			mTextCommentCount.setText("100,000+");
		}
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.videodetail_like);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			mBtnLike.setCompoundDrawables(drawable, null, null, null);
		} else {
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.videodetail_like_press);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			mBtnLike.setCompoundDrawables(drawable, null, null, null);
		}

		Spanned text = Html.fromHtml("<font color='#1163a2'>" + mVideoJson.data.avideo.user.nickname + "</font>" + "&nbsp;&nbsp;"
				+ "<font color='#333333'>" + mVideoJson.data.avideo.video.describe + "</font>");
		mTextAutor.setText(text);
		// TODO 三条评论 外链
		if ("1".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			hasCommentLayout.setVisibility(View.VISIBLE);
			noCommentLayout.setVisibility(View.GONE);
			List<VideoListInfo> videoList = mVideoJson.data.avideo.video.comment.list;
			for (int i = 0; i < videoList.size(); i++) {
				if (i == 0) {
					Spanned text1 = Html.fromHtml("<font color='#1163a2'>" + videoList.get(i).nickname + "</font>"
							+ "&nbsp;&nbsp;" + "<font color='#333333'>" + videoList.get(i).content + "</font>");
					mTextCommentFirst.setText(text1);
				}
				if (i == 1) {
					Spanned text2 = Html.fromHtml("<font color='#1163a2'>" + videoList.get(i).nickname + "</font>"
							+ "&nbsp;&nbsp;" + "<font color='#333333'>" + videoList.get(i).content + "</font>");
					mTextCommentSecond.setText(text2);
				}
				if (i == 2) {
					Spanned text3 = Html.fromHtml("<font color='#1163a2'>" + videoList.get(i).nickname + "</font>"
							+ "&nbsp;&nbsp;" + "<font color='#333333'>" + videoList.get(i).content + "</font>");
					mTextCommenThird.setText(text3);
				}
			}
		} else {
			hasCommentLayout.setVisibility(View.GONE);
			noCommentLayout.setVisibility(View.VISIBLE);
		}

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
		if (event == SquareCmd_Req_GetShareUrl) {
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
			likeNumber = Integer.parseInt(mBtnLikeAll.getText().toString().replace(",", "")) + 1;
			
			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mBtnLikeAll.setText(df.format(likeNumber));
			} else {
				mBtnLikeAll.setText("100,000+");
			}
			
//			mBtnLikeAll.setText(likeNumber + "");
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.videodetail_like_press);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			mBtnLike.setCompoundDrawables(drawable, null, null, null);
			isPraise = "1";
			GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
		} else {
			likeNumber = Integer.parseInt(mBtnLikeAll.getText().toString().replace(",", "")) - 1;
			
			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				mBtnLikeAll.setText(df.format(likeNumber));
			} else {
				mBtnLikeAll.setText("100,000+");
			}
			
//			mBtnLikeAll.setText(likeNumber + "");
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.videodetail_like);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			mBtnLike.setCompoundDrawables(drawable, null, null, null);
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

	// 从assets 文件夹中获取文件并读取数据
	public String getFromAssets(String fileName) {
		String result = "";
		try {
			InputStream in = getResources().getAssets().open(fileName);
			// 获取文件的字节数
			int lenght = in.available();
			// 创建byte数组
			byte[] buffer = new byte[lenght];
			// 将文件中的数据读到byte数组中
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");// 你的文件的编码
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
