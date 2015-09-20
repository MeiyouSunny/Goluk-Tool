package cn.com.mobnote.golukmobile.videodetail;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.player.FullScreenVideoView;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoDetailAdapter extends BaseAdapter{

	private Context mContext = null;
	private int count = 0;
	private VideoJson mVideoJson = null;
	private List<CommentBean> mDataList = null;
	/** head **/
	private final int FIRST_TYPE = 0;
	/** body **/
	private final int OTHERS_TYPE = 1;
	
	/**视频缓冲计时**/
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
	private ConnectivityManager connectivityManager = null;
	public NetworkInfo netInfo = null;
	private CustomDialog mCustomDialog;
	public Handler mHandler;
	
	public ViewHolder headHolder = null;
	
	public CustomLoadingDialog mCustomLoadingDialog;
	private String isPraise = "0";
	private int likeNumber = 0;

	public VideoDetailAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<CommentBean>();
		connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		netInfo = connectivityManager.getActiveNetworkInfo();
	}

	public void setData(VideoJson videoJsonData, List<CommentBean> commentData) {
		mVideoJson = videoJsonData;
		mDataList.clear();
//		List<VideoListInfo> list = videoJsonData.data.avideo.video.comment.comlist;
//		for (int i = 0; i < list.size(); i++) {
//			CommentBean bean = new CommentBean();
//			bean.mCommentId = list.get(i).commentid;
//			bean.mCommentTime = list.get(i).time;
//			bean.mCommentTxt = list.get(i).text;
//			bean.mUserHead = list.get(i).avatar;
//			bean.mUserId = list.get(i).authorid;
//			bean.mUserName = list.get(i).name;
//			mDataList.add(bean);
//		}
		mDataList.addAll(commentData);
		GolukDebugUtils.e("newadapter", "================VideoDetailAdapter：mDataList==" + mDataList.size());
		count = mDataList.size();
		GolukDebugUtils.e("newadapter", "================VideoDetailAdapter：commentData==" + commentData);
		GolukDebugUtils.e("newadapter", "================VideoDetailAdapter：count==" + count);
		if(0 == count){
			count += 2;
		}else{
			count++;
		}
		this.notifyDataSetChanged();
	}

	public void appendData(ArrayList<CommentBean> data) {
		mDataList.addAll(data);
		this.notifyDataSetChanged();
		GolukDebugUtils.e("", "========appendData====mDataList==="+mDataList.size());
	}
	
	public void addFirstData(CommentBean data) {
		mDataList.add(0, data);
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
				count --;
				break;
			}
		}
		if (isDelSuces) {
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
		return count;
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
		GolukDebugUtils.e("newadapter", "================VideoDetailActivity：count@@==" + count);
		if (FIRST_TYPE == type) {
			convertView = getHeadView(convertView);
		} else {
			GolukDebugUtils.e("newadapter", "================VideoDetailActivity：arg0==" + arg0);
			convertView = loadLayout(convertView, arg0 - 1);
		}
		return convertView;
	}

	/**
	 * 详情
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	private View getHeadView(View convertView) {
		if (null == convertView) {
			headHolder = new ViewHolder();
			convertView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.video_detail_head, null);
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

			convertView.setTag(headHolder);
		} else {
			headHolder = (ViewHolder) convertView.getTag();
		}
		getHeadData(headHolder, mVideoJson.data);
		
		headHolder.mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) headHolder.mLoading.getBackground();
		
		headHolder.mPlayBtn.setOnClickListener(new ClickVideoListener(mContext, this));
		headHolder.mPlayerLayout.setOnClickListener(new ClickVideoListener(mContext, this));
//		headHolder.mTextLink.setOnClickListener(new ClickListener());
		headHolder.mPraiseLayout.setOnClickListener(new ClickPraiseListener(mContext, this));
		headHolder.mShareLayout.setOnClickListener(new ClickShareListener(mContext, mVideoJson, this));
		
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
		
		return convertView;
	}

	// 设置详情数据
	@SuppressLint("HandlerLeak")
	private void getHeadData(final ViewHolder headHolder, VideoAllData mVideoAllData) {
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
			headHolder.mTextZan.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.praisenumber));
			headHolder.mTextComment.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.comment.comcount));
			headHolder.mTextDescribe.setText(mVideoAllData.avideo.video.describe);

			// 下载视频第一帧截图
			headHolder.mImageLayout.removeAllViews();
			RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);

			SimpleDraweeView view = new SimpleDraweeView(mContext);
			GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
			GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
					.setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
					.setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
					.setActualImageScaleType(ScaleType.FIT_XY).build();
			view.setHierarchy(hierarchy);
			view.setImageURI(Uri.parse(mVideoAllData.avideo.video.picture));
			headHolder.mImageLayout.addView(view, mPreLoadingParams);
			//TODO 外链接
//			if ("0".equals(mVideoAllData.link.showurl)) {
//				headHolder.mTextLink.setVisibility(View.GONE);
//			} else {
//				headHolder.mTextLink.setVisibility(View.VISIBLE);
//				headHolder.mTextLink.setText(mVideoAllData.link.outurlname);
//			}
			
			//视频
			if ((netInfo != null) && (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
				playVideo();
				headHolder.mVideoView.start();
				showLoading();
				GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------getHeadData  showLoading");
			} else {
				headHolder.mImageLayout.setVisibility(View.VISIBLE);
				headHolder.mPlayBtn.setVisibility(View.VISIBLE);
			}
			
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					switch (msg.what) {
					case 1:
						if (error) {
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
								GolukDebugUtils.e("videoview","VideoDetailActivity-------------------mHandler : hideLoading ");
							}
							playTime = 0;
							duration = headHolder.mVideoView.getDuration();
							int progress = headHolder.mVideoView.getCurrentPosition() * 100 / headHolder.mVideoView.getDuration();
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
		}
	}

	/**
	 * 评论
	 * 
	 * @return
	 */
	private View getCommentView(View convertView) {
		ViewHolder commentHolder = new ViewHolder();
		convertView = LayoutInflater.from(mContext).inflate(R.layout.comment_list_item, null);

		commentHolder.mCommentHead = (ImageView) convertView.findViewById(R.id.comment_item_head);
		commentHolder.mCommentTime = (TextView) convertView.findViewById(R.id.comment_item_time);
		commentHolder.mCommentName = (TextView) convertView.findViewById(R.id.comment_item_name);
		commentHolder.mCommentConennt = (TextView) convertView.findViewById(R.id.comment_item_content);
		
		commentHolder.mNoData = (ImageView) convertView.findViewById(R.id.comment_item_nodata);
		commentHolder.mListLayout = (RelativeLayout) convertView.findViewById(R.id.comment_list_layout);

		convertView.setTag(commentHolder);
		return convertView;
	}

	private View loadLayout(View convertView, int arg0) {
		ViewHolder holder = null;
		if (null == convertView) {
			convertView = getCommentView(convertView);
			holder = (ViewHolder) convertView.getTag();
		} else {
			holder = (ViewHolder) convertView.getTag();
			if (null == holder) {
				convertView = getCommentView(convertView);
			}
		}
		getCommentData(holder, arg0);
		return convertView;
	}

	// 设置评论数据
	private void getCommentData(ViewHolder holder, int index) {
		GolukDebugUtils.e("newadapter", "================VideoDetailActivity：mDataList.size()==" + mDataList.size());
		if(0 == mDataList.size()){
			holder.mListLayout.setVisibility(View.GONE);
			holder.mNoData.setVisibility(View.VISIBLE);
			return ;
		}
		holder.mListLayout.setVisibility(View.VISIBLE);
		holder.mNoData.setVisibility(View.GONE);
		CommentBean temp = mDataList.get(index);
		holder.mCommentHead.setBackgroundResource(UserUtils.getUserHeadImageResourceId(temp.mUserHead));
		holder.mCommentName.setText(temp.mUserName);
		holder.mCommentConennt.setText(temp.mCommentTxt);
		holder.mCommentTime.setText(GolukUtils.getCommentShowFormatTime(temp.mCommentTime));

	}
	
	/**
	 * 点赞
	 */
	public void clickPraise() {
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {// 没有点过赞
			likeNumber = Integer.parseInt(headHolder.mTextZan.getText().toString().replace(",", "")) + 1;
			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				headHolder.mTextZan.setText(df.format(likeNumber));
			} else {
				headHolder.mTextZan.setText("100,000+");
			}
			headHolder.mZanImage.setImageResource(R.drawable.videodetail_like_press);
			headHolder.mTextZan.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
			headHolder.mTextZanName.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
			isPraise = "1";
			boolean b = GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
			if (b) {
			}
		} else {
			likeNumber = Integer.parseInt(headHolder.mTextZan.getText().toString().replace(",", "")) - 1;

			DecimalFormat df = new DecimalFormat("#,###");
			if (likeNumber < 100000) {
				headHolder.mTextZan.setText(df.format(likeNumber));
			} else {
				headHolder.mTextZan.setText("100,000+");
			}

			headHolder.mZanImage.setImageResource(R.drawable.videodetail_like);
			headHolder.mTextZan.setTextColor(Color.rgb(136, 136, 136));
			headHolder.mTextZanName.setTextColor(Color.rgb(136, 136, 136));
			isPraise = "0";
		}
		mVideoJson.data.avideo.video.praisenumber = likeNumber + "";
		mVideoJson.data.avideo.video.ispraise = isPraise;
	}

	public static class ViewHolder {
		// 详情
		ImageView mImageHead = null;
		TextView mTextName = null;
		TextView mTextTime = null;
		TextView mTextLook = null;
		FullScreenVideoView mVideoView = null;
		RelativeLayout mImageLayout = null;
		ImageView mPlayBtn = null;
		SeekBar mSeekBar = null;
		LinearLayout mVideoLoading = null;
		ImageView mLoading = null;
		RelativeLayout mPlayerLayout = null;

		TextView mTextDescribe = null;
		TextView mTextAuthor, mTextLink;
		LinearLayout mPraiseLayout, mShareLayout, mCommentLayout;
		TextView mTextZan, mTextComment,mTextZanName;
		ImageView mZanImage;
		// 评论
		ImageView mCommentHead = null;
		TextView mCommentTime, mCommentName, mCommentConennt;
		ImageView mNoData = null;
		RelativeLayout mListLayout = null;
		
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
	public void dialog(String msg,final ViewHolder headHolder) {
		if (null == mCustomDialog) {
			mCustomDialog = new CustomDialog(mContext);
			mCustomDialog.setCancelable(false);
			mCustomDialog.setMessage(msg, Gravity.CENTER);
			mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
				@Override
				public void onClickListener() {
					cancleTimer();
					headHolder.mImageLayout.setVisibility(View.VISIBLE);
					headHolder.mPlayerLayout.setEnabled(false);
					headHolder.mSeekBar.setProgress(0);
				}
			});
			if (!((VideoDetailActivity)mContext).isFinishing()) {
				mCustomDialog.show();
			}
		}
	}
	/**
	 * 显示加载中布局
	 */
	public void showLoading() {
		GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------showLoading()  isShow==="+isShow);
		if (!isShow) {
			isShow = true;
			headHolder.mVideoLoading.setVisibility(View.VISIBLE);
			headHolder.mLoading.setVisibility(View.VISIBLE);
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
				GolukDebugUtils.e("videoview","VideoDetailActivity-------------------netWorkTimeoutCheck : hideLoading ");
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
	
	public void setOnResume(){
		GolukDebugUtils.e("videostate", "VideoDetailActivity--------------------setOnResume  "+isPause);
		if (isPause) {
			isPause = false;
			showLoading();
			GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------setOnResume  showLoading");
			headHolder.mPlayBtn.setVisibility(View.GONE);
			headHolder.mImageLayout.setVisibility(View.VISIBLE);
			headHolder.mVideoView.start();
		}
	}
	
	public void setOnPause(){
		GolukDebugUtils.e("videostate", "VideoDetailActivity--------------------setOnPause  "+isPause);
		if (null == headHolder.mVideoView) {
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
	
	public void showLoadingDialog(){
		if(mCustomLoadingDialog == null){
			mCustomLoadingDialog = new CustomLoadingDialog(mContext, null);
			mCustomLoadingDialog.show();
		}
	}
	
	public void closeLoadingDialog(){
		if(null != mCustomLoadingDialog){
			mCustomLoadingDialog.close();
		}
	}

}
