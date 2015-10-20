package cn.com.mobnote.golukmobile.videodetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.player.FullScreenVideoView;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

public class VideoDetailAdapter extends BaseAdapter {

	public Context mContext = null;
	private VideoJson mVideoJson = null;
	private List<CommentBean> mDataList = null;
	/** head **/
	private final int FIRST_TYPE = 0;
	/** body **/
	private final int OTHERS_TYPE = 1;

	/** 视频缓冲计时 **/
	public Timer timer = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	public boolean isShow = false;
	/** 缓冲标识 */
	public boolean isBuffering = false;
	/** 播放器报错标识 */
	public boolean error = false;
	/** 视频播放时间 */
	public int playTime = 0;
	/** 网络连接超时 */
	private int networkConnectTimeOut = 0;
	private int duration = 0;
	/** 暂停标识 */
	public boolean isPause = false;
	public ConnectivityManager connectivityManager = null;
	public NetworkInfo netInfo = null;
	private CustomDialog mCustomDialog;
	/** 头部视频详情holder **/
	public ViewHolder headHolder = null;
	/** 评论holder **/
	private ViewHolder commentHolder = null;

	public View mHeadView = null;

	public CustomLoadingDialog mCustomLoadingDialog;
	/**判断是精选(0)还是最新(1)**/
	private int mType = 0;
	
	public VideoDetailAdapter(Context context,int type) {
		mContext = context;
		this.mType = type;
		mDataList = new ArrayList<CommentBean>();
		connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		netInfo = connectivityManager.getActiveNetworkInfo();
	}
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (error) {
					cancleTimer();
					return;
				}
				netWorkTimeoutCheck();
				if (null == headHolder.mVideoView) {
					return;
				}
				if (headHolder.mVideoView.getCurrentPosition() > 0) {
					if (!headHolder.mVideoView.isPlaying()) {
						return;
					}
					if (!isBuffering) {
						hideLoading();
						GolukDebugUtils.e("videoview",
								"VideoDetailActivity-------------------mHandler : hideLoading ");
					}
					playTime = 0;
					duration = headHolder.mVideoView.getDuration();
					int progress = headHolder.mVideoView.getCurrentPosition() * 100 / duration;
					GolukDebugUtils.e("videoloop","VideoDetailActivity-----------mHandler :  progress"+progress);
					if(progress >= 94){
						connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
						netInfo = connectivityManager.getActiveNetworkInfo();
						if ((null != netInfo) && (netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
							headHolder.mVideoView.pause();
							headHolder.mPlayBtn.setVisibility(View.VISIBLE);
							headHolder.mImageLayout.setVisibility(View.VISIBLE);
							headHolder.mVideoView.seekTo(0);
							headHolder.mSeekBar.setProgress(0);
						}
					}
					
					headHolder.mSeekBar.setProgress(progress);
					if (headHolder.mVideoView.getCurrentPosition() > headHolder.mVideoView.getDuration() - 100) {
						headHolder.mSeekBar.setProgress(0);
					}
				} else {
					if (0 != duration) {
						headHolder.mSeekBar.setProgress(playTime * 100 / duration);
					} else {
						headHolder.mSeekBar.setProgress(0);
					}
				}
				break;
			default:
				break;
			}
		}
	};
	
	public void setData(VideoJson videoJsonData, List<CommentBean> commentData) {
		mVideoJson = videoJsonData;
		mDataList.clear();
		
		GolukDebugUtils.e("newadapter", "================VideoDetailAdapter：commentData==" + commentData);
		if (null != commentData) {
			mDataList.addAll(commentData);
		}
		this.notifyDataSetChanged();
	}

	public void appendData(ArrayList<CommentBean> data) {
		mDataList.addAll(data);
		this.notifyDataSetChanged();
		GolukDebugUtils.e("", "========appendData====mDataList===" + mDataList.size());
	}

	public void addFirstData(CommentBean data) {
		mDataList.add(0, data);
		mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer.parseInt(mVideoJson.data.avideo.video.comment.comcount)+1);
		this.notifyDataSetChanged();
	}

	public void deleteData(CommentBean delBean) {
		if (null == delBean) {
			return;
		}
		boolean isDelSuces = false;
		int size = mDataList.size();
		for (int i = 0; i < size; i++) {
			if (mDataList.get(i).mCommentId.equals(delBean.mCommentId)) {
				mDataList.remove(i);
				isDelSuces = true;
				break;
			}
		}
		if (isDelSuces) {
			mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer.parseInt(mVideoJson.data.avideo.video.comment.comcount)-1);
			this.notifyDataSetChanged();
		}
	}

	// 获取最后一条数据的时间戳
	public String getLastDataTime() {
		if (null == mDataList || mDataList.size() <= 0) {
			return "";
		}
		return mDataList.get(mDataList.size() - 1).mCommentTime;
	}

	@Override
	public int getCount() {
		if(null == mVideoJson){
			return 1;
		}
		if ("0".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			return 2;
		}
		if(0 == mDataList.size()){
			return 2;
		}
		return mDataList.size()+1;
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mDataList || arg0 < 0 || arg0 > mDataList.size() - 1) {
			return null;
		}

		return mDataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * 返回你有多少个不同的布局
	 */
	@Override
	public int getViewTypeCount() {
		if (null == mDataList) {
			return 1;
		} else {
			return 2;
		}
	}

	/**
	 * 由position返回view type id
	 */
	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return FIRST_TYPE;
		} else {
			return OTHERS_TYPE;
		}
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		int type = getItemViewType(arg0);
		boolean isNULL = null == convertView ? true : false;
		String s = (null == convertView) ? "convertView == NULL" : "converView Not null";
		GolukDebugUtils.e("newadapter", "VideoDetailActivity===getView=  positon:" + arg0 + "  " + s);
		if (FIRST_TYPE == type) {
			convertView = getHeadView(convertView);
		} else {
			GolukDebugUtils.e("newadapter", "================VideoDetailActivity：arg0==" + arg0);
			convertView = loadLayout(convertView, arg0 - 1);
		}
		if(null == mVideoJson){
			convertView.setVisibility(View.GONE);
		}else{
			convertView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	public View createHeadView() {
		View convertView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.video_detail_head, null);
		headHolder.mImageHead = (ImageView) convertView.findViewById(R.id.user_head);
		headHolder.mTextName = (TextView) convertView.findViewById(R.id.user_name);
		headHolder.mTextTime = (TextView) convertView.findViewById(R.id.user_time);
		headHolder.mTextLook = (TextView) convertView.findViewById(R.id.video_detail_count_look);

		headHolder.mVideoView = (FullScreenVideoView) convertView.findViewById(R.id.video_detail_videoview);
		headHolder.mImageLayout = (RelativeLayout) convertView.findViewById(R.id.mImageLayout);
		headHolder.mPlayBtn = (ImageView) convertView.findViewById(R.id.play_btn);
		headHolder.mSeekBar = (SeekBar) convertView.findViewById(R.id.seekbar);
		headHolder.mVideoLoading = (LinearLayout) convertView.findViewById(R.id.mLoadingLayout);
		headHolder.mLoading = (ImageView) convertView.findViewById(R.id.mLoading);
		headHolder.mPlayerLayout = (RelativeLayout) convertView.findViewById(R.id.mPlayerLayout);

		headHolder.mTextDescribe = (TextView) convertView.findViewById(R.id.video_detail_describe);
		headHolder.mTextAuthor = (TextView) convertView.findViewById(R.id.video_detail_author);
		headHolder.mTextLink = (TextView) convertView.findViewById(R.id.video_detail_link);
		headHolder.mPraiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
		headHolder.mShareLayout = (LinearLayout) convertView.findViewById(R.id.shareLayout);
		headHolder.mCommentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);
		headHolder.mTextZan = (TextView) convertView.findViewById(R.id.zanText);
		headHolder.mTextComment = (TextView) convertView.findViewById(R.id.commentText);
		headHolder.mZanImage = (ImageView) convertView.findViewById(R.id.video_square_detail_like_image);
		headHolder.mTextZanName = (TextView) convertView.findViewById(R.id.zanName);
		
		loadFirstPic();
		
		return convertView;
	}

	/**
	 * 详情
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	private View getHeadView(View convertView) {
		boolean isStartPlay = true;
		if (null == mHeadView) {
			isStartPlay = true;
			headHolder = new ViewHolder();
			mHeadView = createHeadView();
			mHeadView.setTag(headHolder);
		} else {
			headHolder = (ViewHolder) mHeadView.getTag();
			isStartPlay = false;
		}
		
		if(null == headHolder.mVideoView){
			return mHeadView;
		}
		if(null != mVideoJson){
			getHeadData(mVideoJson.data, true);
			headHolder.mShareLayout.setOnClickListener(new ClickShareListener(mContext, mVideoJson, this));
			if (null != mVideoJson.data.link) {
				headHolder.mTextLink.setOnClickListener(new ClickLinkListener(mContext, mVideoJson, this));
			}
			headHolder.mPraiseLayout.setOnClickListener(new ClickPraiseListener(mContext, this,mVideoJson));
		}

		headHolder.mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) headHolder.mLoading.getBackground();

		headHolder.mPlayBtn.setOnClickListener(new ClickVideoListener(mContext, this));
		headHolder.mPlayerLayout.setOnClickListener(new ClickVideoListener(mContext, this));

		headHolder.mVideoView.setOnPreparedListener(new PlayPreparedListener(headHolder, this));
		headHolder.mVideoView.setOnCompletionListener(new PlayCompletionListener(this, headHolder));
		headHolder.mVideoView.setOnErrorListener(new PlayErrorListener(mContext, headHolder, this));
		if (GolukUtils.getSystemSDK() >= 17) {
			try {
				headHolder.mVideoView.setOnInfoListener(new PlayInfoListener(this, headHolder));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		headHolder.mCommentLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mContext instanceof VideoDetailActivity){
					((VideoDetailActivity)mContext).showSoft();
				}else{
					((WonderfulActivity)mContext).showSoft();
				}
			}
		});

		return mHeadView;
	}

	// 设置详情数据
	@SuppressLint("HandlerLeak")
	private void getHeadData(final VideoAllData mVideoAllData, boolean isStartPlay) {
		if (!mVideoJson.success) {
			// TODO 后台数据异常
			GolukDebugUtils.e("lily", "---------后台服务器数据异常-------" + mVideoAllData);
			GolukUtils.showToast(mContext, "数据异常，请重试");
		} else {
			UserUtils.focusHead(mVideoAllData.avideo.user.headportrait, headHolder.mImageHead);
			headHolder.mTextName.setText(mVideoAllData.avideo.user.nickname);
			headHolder.mTextTime.setText(GolukUtils.getCommentShowFormatTime(mVideoAllData.avideo.video.sharingtime));
			// 点赞数、评论数、观看数
			headHolder.mTextLook.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.clicknumber));
			if(!"0".equals(mVideoAllData.avideo.video.praisenumber)){
				headHolder.mTextZan.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.praisenumber));
			}
			headHolder.mTextComment.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.comment.comcount));
			headHolder.mTextDescribe.setText(mVideoAllData.avideo.video.describe);
			if(0 == mType){
				headHolder.mTextAuthor.setVisibility(View.VISIBLE);
				headHolder.mTextAuthor.setText("感谢作者  "+mVideoAllData.avideo.user.nickname);
			}else{
				headHolder.mTextAuthor.setVisibility(View.GONE);
			}
			headHolder.simpleDraweeView.setImageURI(Uri.parse(mVideoAllData.avideo.video.picture));
			
			// 外链接
			if (null != mVideoAllData.link) {
				if ("0".equals(mVideoAllData.link.showurl)) {
					headHolder.mTextLink.setVisibility(View.GONE);
				} else {
					headHolder.mTextLink.setVisibility(View.VISIBLE);
					headHolder.mTextLink.setText(mVideoAllData.link.outurlname);
				}
			}

			// 视频
			if ((netInfo != null) && (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
				if (!headHolder.mVideoView.isPlaying() && isStartPlay) {
					GolukDebugUtils.e("newadapter", "VideoDetailActivity===getHeadData=  stat Play:");
					playVideo();
					headHolder.mVideoView.start();
					showLoading();
					GolukDebugUtils.e("videoview",
							"VideoDetailActivity-------------------------getHeadData:  showLoading");
				}

			} else {
				if(!headHolder.mVideoView.isPlaying() && !isShow){
					headHolder.mImageLayout.setVisibility(View.VISIBLE);
					headHolder.mPlayBtn.setVisibility(View.VISIBLE);
				}
			}
			
			headHolder.mImageHead.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent it = new Intent(mContext, UserCenterActivity.class);

					VideoUserInfo videoUser = mVideoAllData.avideo.user;
					UCUserInfo user = new UCUserInfo();
					user.uid = videoUser.uid;
					user.nickname = videoUser.nickname;
					user.headportrait = videoUser.headportrait;
					user.introduce = "";
					user.sex = "";
					user.customavatar = "";
					user.praisemenumber = "0";
					user.sharevideonumber = "0";

					GolukDebugUtils.e("", "-------user.nickname-----" + videoUser.nickname);

					it.putExtra("userinfo", user);
					it.putExtra("type", 0);
					mContext.startActivity(it);
				}
			});

		}
	}

	/**
	 * 评论
	 * 
	 * @return
	 */
	private View getCommentView(View convertView) {
		commentHolder = new ViewHolder();
		convertView = LayoutInflater.from(mContext).inflate(R.layout.comment_list_item, null);

		commentHolder.mCommentHead = (ImageView) convertView.findViewById(R.id.comment_item_head);
		commentHolder.mCommentTime = (TextView) convertView.findViewById(R.id.comment_item_time);
		commentHolder.mCommentName = (TextView) convertView.findViewById(R.id.comment_item_name);
		commentHolder.mCommentConennt = (TextView) convertView.findViewById(R.id.comment_item_content);

		commentHolder.mNoData = (ImageView) convertView.findViewById(R.id.comment_item_nodata);
		commentHolder.mListLayout = (RelativeLayout) convertView.findViewById(R.id.comment_list_layout);
		commentHolder.mForbidComment = (TextView) convertView.findViewById(R.id.comment_forbid);

		convertView.setTag(commentHolder);
		return convertView;
	}

	private View loadLayout(View convertView, int arg0) {
		// ViewHolder holder = null;
		if (null == convertView) {
			convertView = getCommentView(convertView);
			commentHolder = (ViewHolder) convertView.getTag();
		} else {
			commentHolder = (ViewHolder) convertView.getTag();
			if (null == commentHolder) {
				convertView = getCommentView(convertView);
			}
		}
		getCommentData(arg0);
		return convertView;
	}

	// 设置评论数据
	private void getCommentData(final int index) {
		GolukDebugUtils.e("newadapter", "================VideoDetailActivity：mDataList.size()==" + mDataList.size());
		if (0 == mDataList.size()) {
			commentHolder.mListLayout.setVisibility(View.GONE);
			commentHolder.mNoData.setVisibility(View.VISIBLE);
			return;
		}
		commentHolder.mListLayout.setVisibility(View.VISIBLE);
		commentHolder.mNoData.setVisibility(View.GONE);
		if ("0".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			closeComment();
			if (mContext instanceof WonderfulActivity) {
				((WonderfulActivity) mContext).mCommentLayout.setVisibility(View.GONE);
			}
		} else {
			CommentBean temp = mDataList.get(index);
			commentHolder.mCommentHead.setBackgroundResource(UserUtils.getUserHeadImageResourceId(temp.mUserHead));
			commentHolder.mCommentName.setText(temp.mUserName);
			if(!"".equals(temp.mReplyId) && null != temp.mReplyId && !"".equals(temp.mReplyName) && null != temp.mReplyName){
				//评论回复
				showText(commentHolder.mCommentConennt, temp.mReplyName, temp.mCommentTxt);
			}else{
				//普通评论
				commentHolder.mCommentConennt.setText(temp.mCommentTxt);
			}
			commentHolder.mCommentTime.setText(GolukUtils.getCommentShowFormatTime(temp.mCommentTime));

			commentHolder.mCommentHead.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent it = new Intent(mContext, UserCenterActivity.class);

					CommentBean bean = mDataList.get(index);
					UCUserInfo user = new UCUserInfo();
					user.uid = bean.mUserId;
					user.nickname = bean.mUserName;
					user.headportrait = bean.mUserHead;
					user.introduce = "";
					user.sex = "";
					user.customavatar = "";
					user.praisemenumber = "0";
					user.sharevideonumber = "0";

					GolukDebugUtils.e("", "-------user.nickname-----" + bean.mUserName);

					it.putExtra("userinfo", user);
					it.putExtra("type", 0);
					mContext.startActivity(it);
				}
			});

		}
	}

	// 没有评论
	public void commentNoData() {
		if (0 == mDataList.size()) {
			commentHolder.mListLayout.setVisibility(View.GONE);
			commentHolder.mNoData.setVisibility(View.VISIBLE);
			return;
		}
	}

	// 评论被关闭
	public void closeComment() {
		commentHolder.mListLayout.setVisibility(View.GONE);
		commentHolder.mNoData.setVisibility(View.GONE);
		commentHolder.mForbidComment.setVisibility(View.VISIBLE);
	}
	
	public String setClickPraise(){
		int likeNumber = 0;
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {
			if (mVideoJson.data.avideo.video.praisenumber.replace(",", "").equals("")) {
				likeNumber = 1;
			}else{
				try{
					likeNumber = Integer.parseInt(mVideoJson.data.avideo.video.praisenumber.replace(",", "")) + 1;
				}catch(Exception e){
					likeNumber = 1;
					e.printStackTrace();
				}
			}
			mVideoJson.data.avideo.video.ispraise = "1";
			boolean b = GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
		}else{
			try {
				likeNumber = Integer.parseInt(mVideoJson.data.avideo.video.praisenumber.replace(",", "")) - 1;
			} catch (Exception e) {
				likeNumber = 0;
				e.printStackTrace();
			}
			mVideoJson.data.avideo.video.ispraise = "0";
		}
		mVideoJson.data.avideo.video.praisenumber = likeNumber + "";
		return GolukUtils.getFormatNumber(likeNumber+"");
	}

	public static class ViewHolder {
		// 详情
		ImageView mImageHead = null;
		TextView mTextName = null;
		TextView mTextTime = null;
		TextView mTextLook = null;
		FullScreenVideoView mVideoView = null;
		RelativeLayout mImageLayout = null;
		SimpleDraweeView simpleDraweeView = null;
		
		ImageView mPlayBtn = null;
		SeekBar mSeekBar = null;
		LinearLayout mVideoLoading = null;
		ImageView mLoading = null;
		RelativeLayout mPlayerLayout = null;

		TextView mTextDescribe = null;
		TextView mTextAuthor, mTextLink;
		LinearLayout mPraiseLayout, mShareLayout, mCommentLayout;
		TextView mTextZan, mTextComment, mTextZanName;
		ImageView mZanImage;
		// 评论
		ImageView mCommentHead = null;
		TextView mCommentTime, mCommentName, mCommentConennt;
		ImageView mNoData = null;
		RelativeLayout mListLayout = null;
		TextView mForbidComment = null;
		Uri url = null;
	}

	private boolean isCallVideo = false;
	
	
	
	public void playVideo() {
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
		headHolder.mVideoView.setVideoURI(uri);
		headHolder.mVideoView.requestFocus();
		headHolder.url = uri;
	}

	/** DP */
	public final int mPlayerHeight = 205;
	/** 布局title 的高度 dp */
	public final int mTitleHeight = 46;
	/** 是否是用户拖出屏幕暂停的，拖回来根据此变变量恢复 */
	private boolean isOuterPause = false;

	public void scrollDealPlayer() {
		if (null == headHolder || null == headHolder.mVideoView) {
			return;
		}
		final int[] locations = new int[2];
		headHolder.mVideoView.getLocationOnScreen(locations);
		// 计算播放布局的所占的像素高度
		final int playHeightPx = (int) (mPlayerHeight * GolukUtils.mDensity);
		// 计算布局title所占的高度
		final int titleHeightPx = (int) (mTitleHeight * GolukUtils.mDensity);
		final int duration = -(playHeightPx - titleHeightPx - VideoDetailActivity.stateBraHeight);

		if (locations[1] < duration) {
			GolukDebugUtils.e("", "onScreen--------------pause");
			// 滑出屏幕外了
			pausePlayer();
		} else {
			int dd = (titleHeightPx + VideoDetailActivity.stateBraHeight);
			if (locations[1] > dd) {
				// 开始播放
				startPlayer();
			}
		}
	}

	private void pausePlayer() {
		if (headHolder.mVideoView.isPlaying()) {
			isOuterPause = true;
			headHolder.mVideoView.pause();
			headHolder.mImageLayout.setVisibility(View.VISIBLE);
			headHolder.mVideoView.setVisibility(View.GONE);
		}
	}

	private void startPlayer() {
		if (!headHolder.mVideoView.isPlaying() && isOuterPause) {
			GolukDebugUtils.e("", "onScreen--------------start");
			headHolder.mVideoView.start();
			headHolder.mVideoView.setVisibility(View.VISIBLE);
			showLoading();
			GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------startPlayer:  showLoading");
		}
		isOuterPause = false;
	}

	/**
	 * 取消计时
	 */
	public void cancleTimer() {
		if (null != timer) {
			timer.cancel();
		}
	}

	/**
	 * 提示对话框
	 * 
	 * @param msg
	 *            提示信息
	 */
	public void dialog(String msg, final ViewHolder headHolder) {
		if (null == mCustomDialog) {
			mCustomDialog = new CustomDialog(mContext);
			mCustomDialog.setCancelable(false);
			mCustomDialog.setMessage(msg, Gravity.CENTER);
			mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
				@Override
				public void onClickListener() {
					headHolder.mImageLayout.setVisibility(View.VISIBLE);
					headHolder.mPlayBtn.setVisibility(View.VISIBLE);
					headHolder.mSeekBar.setProgress(0);
				}
			});
			if (mContext instanceof VideoDetailActivity) {
				if (!((VideoDetailActivity) mContext).isFinishing()) {
					mCustomDialog.show();
				}
			} else {
				if (!((WonderfulActivity) mContext).isFinishing()) {
					mCustomDialog.show();
				}
			}
		}
	}

	/**
	 * 显示加载中布局
	 */
	public void showLoading() {
		GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------showLoading()  isShow===" + isShow);
		if(!UserUtils.isNetDeviceAvailable(mContext) && !headHolder.mVideoView.isPlaying()){
			return ;
		}
		if (!isShow) {
			isShow = true;
			headHolder.mVideoLoading.setVisibility(View.VISIBLE);
			headHolder.mLoading.setVisibility(View.VISIBLE);
			headHolder.mPlayBtn.setVisibility(View.GONE);
			headHolder.mLoading.postDelayed(new Runnable() {
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

	/**
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	public void hideLoading() {
		if (isShow) {
			isShow = false;
			headHolder.mImageLayout.setVisibility(View.GONE);
			if (mAnimationDrawable != null) {
				if (mAnimationDrawable.isRunning()) {
					mAnimationDrawable.stop();
				}
			}
			headHolder.mVideoLoading.setVisibility(View.GONE);
		}
	}

	/**
	 * 无网络超时检查
	 */
	private void netWorkTimeoutCheck() {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			networkConnectTimeOut++;
			if (networkConnectTimeOut > 15) {
				hideLoading();
				GolukDebugUtils.e("videoview",
						"VideoDetailActivity-------------------netWorkTimeoutCheck : hideLoading ");
				headHolder.mImageLayout.setVisibility(View.VISIBLE);
				dialog("网络访问异常，请重试！", headHolder);
				if (null != headHolder.mVideoView) {
					headHolder.mVideoView.stopPlayback();
				}
				return;
			}
		} else {
			networkConnectTimeOut = 0;
		}
	}

	public void setOnResume() {
		GolukDebugUtils.e("videostate", "VideoDetailActivity--------------------setOnResume  " + isPause);
		if (isPause) {
			isPause = false;
			showLoading();
			GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------setOnResume  showLoading");
			headHolder.mPlayBtn.setVisibility(View.GONE);
			headHolder.mImageLayout.setVisibility(View.VISIBLE);
			headHolder.mVideoView.start();
		}
	}

	public void setOnPause() {
		GolukDebugUtils.e("videostate", "VideoDetailActivity--------------------setOnPause  " + isPause);
		if (null == headHolder || null == headHolder.mVideoView) {
			return;
		}
		boolean isPlaying = headHolder.mVideoView.isPlaying();
		if (isPlaying) {
			isPause = true;
			playTime = headHolder.mVideoView.getCurrentPosition();
			headHolder.mVideoView.pause();
			headHolder.mImageLayout.setVisibility(View.VISIBLE);
		}
	}

	public void showLoadingDialog() {
		if (mCustomLoadingDialog == null) {
			mCustomLoadingDialog = new CustomLoadingDialog(mContext, null);
			mCustomLoadingDialog.show();
		}
	}

	public void closeLoadingDialog() {
		if (null != mCustomLoadingDialog) {
			mCustomLoadingDialog.close();
			mCustomLoadingDialog = null;
		}
	}
	
	private void loadFirstPic() {
		// 下载视频第一帧截图
		headHolder.mImageLayout.removeAllViews();
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		SimpleDraweeView simpleDraweeView = new SimpleDraweeView(mContext);
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
				.setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		simpleDraweeView.setHierarchy(hierarchy);
		headHolder.mImageLayout.addView(simpleDraweeView, mPreLoadingParams);
		headHolder.simpleDraweeView = simpleDraweeView;
	}

	/**
	 * 回复评论颜色设置
	 * @param view
	 * @param nikename
	 * @param text
	 */
	private void showText(TextView view, String nikename, String text) {
		String replyName = "@" + nikename + "：";
		String reply_str = "回复" + replyName + text;
		SpannableStringBuilder style = new SpannableStringBuilder(reply_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 2,
				replyName.length() + 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}
	
}
