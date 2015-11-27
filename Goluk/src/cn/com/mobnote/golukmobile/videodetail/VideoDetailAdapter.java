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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.comment.CommentTimerManager;
import cn.com.mobnote.golukmobile.player.FullScreenVideoView;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

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
	/** 判断是精选(0)还是最新(1) **/
	private int mType = 0;

	public VideoDetailAdapter(Context context, int type) {
		mContext = context;
		this.mType = type;
		mDataList = new ArrayList<CommentBean>();
		connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		netInfo = connectivityManager.getActiveNetworkInfo();
		error = false;
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
						GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------mHandler : hideLoading ");
					}
					playTime = 0;
					duration = headHolder.mVideoView.getDuration();
					int progress = headHolder.mVideoView.getCurrentPosition() * 100 / duration;
					GolukDebugUtils.e("videoloop", "VideoDetailActivity-----------mHandler :  progress" + progress);
					if (progress >= 94) {
						connectivityManager = (ConnectivityManager) mContext
								.getSystemService(Context.CONNECTIVITY_SERVICE);
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

		if (null != commentData) {
			mDataList.addAll(commentData);
		}
		this.notifyDataSetChanged();
	}

	public void appendData(ArrayList<CommentBean> data) {
		mDataList.addAll(data);
		this.notifyDataSetChanged();
	}

	public void addFirstData(CommentBean data) {
		mDataList.add(0, data);
		mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
				.parseInt(mVideoJson.data.avideo.video.comment.comcount) + 1);
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
			mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
					.parseInt(mVideoJson.data.avideo.video.comment.comcount) - 1);
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
		if (null == mVideoJson) {
			return 1;
		}
		if ("0".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			return 2;
		}
		return mDataList.size() + 1;
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
		String s = (null == convertView) ? "convertView == NULL" : "converView Not null";
		GolukDebugUtils.e("newadapter", "VideoDetailActivity===getView=  positon:" + arg0 + "  " + s);
		if (FIRST_TYPE == type) {
			convertView = getHeadView(convertView);
		} else {
			convertView = loadLayout(convertView, arg0 - 1);
		}
		if (null == mVideoJson) {
			convertView.setVisibility(View.GONE);
		} else {
			convertView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	public View createHeadView() {
		View convertView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.video_detail_head, null);
		headHolder.mUserCenterLayout = (RelativeLayout) convertView.findViewById(R.id.rl_video_usercenter);
		headHolder.mImageHead = (ImageView) convertView.findViewById(R.id.user_head);
		headHolder.nHeadAuthentication = (ImageView) convertView.findViewById(R.id.im_listview_item_head_authentication);
		headHolder.mTextName = (TextView) convertView.findViewById(R.id.user_name);
		headHolder.mTextTime = (TextView) convertView.findViewById(R.id.tv_user_time_location);
		headHolder.mTextLook = (TextView) convertView.findViewById(R.id.video_detail_count_look);

		headHolder.mVideoView = (FullScreenVideoView) convertView.findViewById(R.id.video_detail_videoview);
		headHolder.mImageLayout = (RelativeLayout) convertView.findViewById(R.id.mImageLayout);
		headHolder.mPlayBtn = (ImageView) convertView.findViewById(R.id.play_btn);
		headHolder.mSeekBar = (SeekBar) convertView.findViewById(R.id.seekbar);
		headHolder.mVideoLoading = (LinearLayout) convertView.findViewById(R.id.mLoadingLayout);
		headHolder.mLoading = (ImageView) convertView.findViewById(R.id.mLoading);
		headHolder.mPlayerLayout = (RelativeLayout) convertView.findViewById(R.id.mPlayerLayout);
		headHolder.simpleDraweeView = (ImageView) convertView.findViewById(R.id.video_detail_first_pic);

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
		
		headHolder.mImageHeadAward = (ImageView) convertView.findViewById(R.id.video_detail_head_award_image);
		headHolder.mActiveImage = (ImageView) convertView.findViewById(R.id.active_image);
		headHolder.mSysImage = (ImageView) convertView.findViewById(R.id.sys_image);
		headHolder.mRecomImage = (ImageView) convertView.findViewById(R.id.recom_image);
		headHolder.mTextLine1 = (TextView) convertView.findViewById(R.id.video_detail_line1);
		headHolder.mTextLine2 = (TextView) convertView.findViewById(R.id.video_detail_line2);
		headHolder.mActiveCount = (TextView) convertView.findViewById(R.id.active_count);
		headHolder.mSysCount = (TextView) convertView.findViewById(R.id.sys_count);
		headHolder.mActiveReason = (TextView) convertView.findViewById(R.id.active_reason);
		headHolder.mSysReason = (TextView) convertView.findViewById(R.id.sys_reason);
		headHolder.mRecomReason = (TextView) convertView.findViewById(R.id.recom_reason);
		headHolder.mReasonLayout = (LinearLayout) convertView.findViewById(R.id.video_detail_reason_layout);
		headHolder.mActiveLayout = (RelativeLayout) convertView.findViewById(R.id.video_detail_activie_layout);
		headHolder.mSysLayout = (RelativeLayout) convertView.findViewById(R.id.video_detail_sys_layout);
		headHolder.mRecomLayout = (RelativeLayout) convertView.findViewById(R.id.video_detail_recom_layout);
		
		return convertView;
	}

	/**
	 * 详情
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	private View getHeadView(View convertView) {
		if (null == mHeadView) {
			headHolder = new ViewHolder();
			mHeadView = createHeadView();
			mHeadView.setTag(headHolder);
		} else {
			headHolder = (ViewHolder) mHeadView.getTag();
		}

		if (null == headHolder.mVideoView) {
			return mHeadView;
		}
		if (null != mVideoJson) {
			getHeadData(mVideoJson.data, true);
			headHolder.mShareLayout.setOnClickListener(new ClickShareListener(mContext, mVideoJson, this));
			if (null != mVideoJson.data.link) {
				headHolder.mTextLink.setOnClickListener(new ClickLinkListener(mContext, mVideoJson, this));
			}
			headHolder.mPraiseLayout.setOnClickListener(new ClickPraiseListener(mContext, this, mVideoJson));
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
		
		if(mVideoJson != null && mVideoJson.data.avideo.video.comment !=null && "1".equals(mVideoJson.data.avideo.video.comment.iscomment)){
			headHolder.mCommentLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (mContext instanceof VideoDetailActivity) {
						((VideoDetailActivity) mContext).showSoft();
					} else {
						((WonderfulActivity) mContext).showSoft();
					}
				}
			});
		}

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
			String netUrlHead = mVideoAllData.avideo.user.customavatar;
			if (null != netUrlHead && !"".equals(netUrlHead)) {
				// 使用网络地址
				GlideUtils.loadNetHead(mContext, headHolder.mImageHead, netUrlHead, R.drawable.my_head_moren7);
			} else {
				UserUtils.focusHead(mContext, mVideoAllData.avideo.user.headportrait, headHolder.mImageHead);
			}
			if (null != mVideoAllData && null != mVideoAllData.avideo && null != mVideoAllData.avideo.user
					&& null != mVideoAllData.avideo.user.mUserLabel) {
				String approvelabel = mVideoAllData.avideo.user.mUserLabel.approvelabel;
				String headplusv = mVideoAllData.avideo.user.mUserLabel.headplusv;
				String tarento = mVideoAllData.avideo.user.mUserLabel.tarento;
				headHolder.nHeadAuthentication.setVisibility(View.VISIBLE);
				if ("1".equals(approvelabel)) {
					headHolder.nHeadAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
				} else if ("1".equals(headplusv)) {
					headHolder.nHeadAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
				} else if ("1".equals(tarento)) {
					headHolder.nHeadAuthentication.setImageResource(R.drawable.authentication_star_icon);
				} else {
					headHolder.nHeadAuthentication.setVisibility(View.GONE);
				}
			} else {
				headHolder.nHeadAuthentication.setVisibility(View.GONE);
			}

			headHolder.mTextName.setText(mVideoAllData.avideo.user.nickname);
			headHolder.mTextTime.setText(GolukUtils.getCommentShowFormatTime(mVideoAllData.avideo.video.sharingtime));
			// 点赞数、评论数、观看数
			headHolder.mTextLook.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.clicknumber)+"围观");
			if (!"0".equals(mVideoAllData.avideo.video.praisenumber)) {
				headHolder.mTextZan.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.praisenumber));
				headHolder.mTextZan.setTextColor(Color.rgb(136, 136, 136));
			}
			headHolder.mZanImage.setImageResource(R.drawable.videodetail_like);
			headHolder.mTextZanName.setTextColor(Color.rgb(136, 136, 136));
			headHolder.mTextComment.setText(GolukUtils.getFormatNumber(mVideoAllData.avideo.video.comment.comcount));
			//TODO 在视频描述之后添加活动标签
			if(null == mVideoAllData.avideo.video.recom || "".equals(mVideoAllData.avideo.video.recom)
					|| null == mVideoAllData.avideo.video.recom.topicname || "".equals(mVideoAllData.avideo.video.recom.topicname)) {
				showTopicText(headHolder.mTextDescribe, mVideoAllData.avideo.video.describe, "");
			} else {
				showTopicText(headHolder.mTextDescribe, mVideoAllData.avideo.video.describe+"    ", "#"+mVideoAllData.avideo.video.recom.topicname+"#");
			}
			
			final String location = mVideoAllData.avideo.video.mLocation;
			if (null != location && !"".equals(location)) {
				headHolder.mTextTime.append("  "+location);
			} else {
				headHolder.mTextTime.append("  "+location);
			}

			if (0 == mType) {
				headHolder.mTextAuthor.setVisibility(View.VISIBLE);
				headHolder.mTextAuthor.setText("感谢作者  " + mVideoAllData.avideo.user.nickname);
			} else {
				headHolder.mTextAuthor.setVisibility(View.GONE);
			}
			GlideUtils.loadImage(mContext, headHolder.simpleDraweeView, mVideoAllData.avideo.video.picture,
					R.drawable.tacitly_pic);

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
				if (!headHolder.mVideoView.isPlaying() && !isShow) {
					headHolder.mImageLayout.setVisibility(View.VISIBLE);
					headHolder.mPlayBtn.setVisibility(View.VISIBLE);
				}
			}
			
			//TODO　没有活动奖励视频没有奖励信息这个模块
			//头部获奖视频icon显示
			//获奖／推荐
			if(null != mVideoAllData.avideo.video.recom) {
				if("1".equals(mVideoAllData.avideo.video.recom.isreward)) {
					headHolder.mImageHeadAward.setVisibility(View.VISIBLE);
				} else {
					headHolder.mImageHeadAward.setVisibility(View.GONE);
				}
				
				if(!"1".equals(mVideoAllData.avideo.video.recom.atflag) && !"1".equals(mVideoAllData.avideo.video.recom.sysflag)
						&&!"1".equals(mVideoAllData.avideo.video.recom.isrecommend)) {
					headHolder.mTextLine1.setVisibility(View.GONE);
					headHolder.mTextLine2.setVisibility(View.GONE);
				} else {
					headHolder.mTextLine1.setVisibility(View.VISIBLE);
					headHolder.mTextLine2.setVisibility(View.VISIBLE);
				}
				
				if("1".equals(mVideoAllData.avideo.video.recom.atflag)) {
					headHolder.mActiveLayout.setVisibility(View.VISIBLE);
					if("".equals(mVideoAllData.avideo.video.recom.atreason)) {
						headHolder.mActiveReason.setText("理由：活动参与积极奖～");
					} else {
						headHolder.mActiveReason.setText("理由："+mVideoAllData.avideo.video.recom.atreason);
					}
					headHolder.mActiveCount.setText("+"+UserUtils.formatNumber(mVideoAllData.avideo.video.recom.atgold)+"Ｇ币");
				} else {
					headHolder.mActiveLayout.setVisibility(View.GONE);
				}
				
				if("1".equals(mVideoAllData.avideo.video.recom.sysflag)) {
					headHolder.mSysLayout.setVisibility(View.VISIBLE);
					if("".equals(mVideoAllData.avideo.video.recom.sysreason)) {
						headHolder.mSysReason.setText("理由：活动参与积极奖～");
					} else {
						headHolder.mSysReason.setText("理由："+mVideoAllData.avideo.video.recom.sysreason);
					}
					headHolder.mSysCount.setText("+"+UserUtils.formatNumber(mVideoAllData.avideo.video.recom.sysgold)+"Ｇ币");
				} else {
					headHolder.mSysLayout.setVisibility(View.GONE);
				}
				
				if("1".equals(mVideoAllData.avideo.video.recom.isrecommend)) {
					headHolder.mRecomLayout.setVisibility(View.VISIBLE);
					if("".equals(mVideoAllData.avideo.video.recom.reason)) {
						headHolder.mRecomReason.setText("理由：活动参与积极奖～");
					} else {
						headHolder.mRecomReason.setText("理由："+mVideoAllData.avideo.video.recom.reason);
					}
				} else {
					headHolder.mRecomLayout.setVisibility(View.GONE);
				}
			} else {
				headHolder.mImageHeadAward.setVisibility(View.GONE);
				headHolder.mTextLine1.setVisibility(View.GONE);
				headHolder.mTextLine2.setVisibility(View.GONE);
				headHolder.mActiveLayout.setVisibility(View.GONE);
				headHolder.mSysLayout.setVisibility(View.GONE);
				headHolder.mRecomLayout.setVisibility(View.GONE);
			}
			
			headHolder.mUserCenterLayout.setOnClickListener(new OnClickListener() {

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
					user.customavatar = videoUser.customavatar;
					user.praisemenumber = "0";
					user.sharevideonumber = "0";

					GolukDebugUtils.e("", "-------user.nickname-----" + videoUser.nickname);

					it.putExtra("userinfo", user);
					it.putExtra("type", 0);
					mContext.startActivity(it);
					CommentTimerManager.getInstance().cancelTimer();
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
		commentHolder.nCommentAuthentication = (ImageView) convertView.findViewById(R.id.im_listview_item_comment_authentication);

		commentHolder.mListLayout = (RelativeLayout) convertView.findViewById(R.id.comment_list_layout);
		commentHolder.mForbidComment = (TextView) convertView.findViewById(R.id.comment_forbid);
		commentHolder.mNoDataLayout = (RelativeLayout) convertView.findViewById(R.id.show_nodata_layout);
		commentHolder.nTextCommentFloor = (TextView) convertView.findViewById(R.id.tv_listview_item_floor);

		convertView.setTag(commentHolder);
		return convertView;
	}

	private View loadLayout(View convertView, int arg0) {
		if (null == convertView) {
			convertView = getCommentView(convertView);
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
		commentHolder.mListLayout.setVisibility(View.VISIBLE);
		commentHolder.mNoDataLayout.setVisibility(View.GONE);
		if ("0".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			closeComment();
		} else {
			if (null != mDataList && 0 == mDataList.size()) {
				return ;
			}
			CommentBean temp = mDataList.get(index);
			String netHeadUrl = temp.customavatar;
			if (null != netHeadUrl && !"".equals(netHeadUrl)) {
				// 使用网络地址
				GlideUtils.loadNetHead(mContext, commentHolder.mCommentHead, netHeadUrl, -1);
			} else {
				// 使用本地头像
				GlideUtils.loadLocalHead(mContext, commentHolder.mCommentHead,
						UserUtils.getUserHeadImageResourceId(temp.mUserHead));
			}
			commentHolder.nCommentAuthentication.setVisibility(View.VISIBLE);
			if (null != temp) {
				if ("1".equals(temp.mApprovelabel)) {
					commentHolder.nCommentAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
				} else if ("1".equals(temp.mHeadplusv)) {
					commentHolder.nCommentAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
				} else if ("1".equals(temp.mTarento)) {
					commentHolder.nCommentAuthentication.setImageResource(R.drawable.authentication_star_icon);
				} else {
					commentHolder.nCommentAuthentication.setVisibility(View.GONE);
				}
			}
			commentHolder.mCommentName.setText(temp.mUserName);
			if (!"".equals(temp.mReplyId) && null != temp.mReplyId && !"".equals(temp.mReplyName)
					&& null != temp.mReplyName) {
				// 评论回复
				UserUtils.showText(commentHolder.mCommentConennt, temp.mReplyName, temp.mCommentTxt);
			} else {
				// 普通评论
				commentHolder.mCommentConennt.setText(temp.mCommentTxt);
			}
		
			commentHolder.mCommentTime.setText(GolukUtils.getCommentShowFormatTime(temp.mCommentTime));
			if(null != temp.mSeq && !"".equals(temp.mSeq)) {
				commentHolder.nTextCommentFloor.setVisibility(View.VISIBLE);
				commentHolder.nTextCommentFloor.setText(temp.mSeq+"楼");
			} else {
				commentHolder.nTextCommentFloor.setVisibility(View.GONE);
			}

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
					user.customavatar = bean.customavatar;
					user.praisemenumber = "0";
					user.sharevideonumber = "0";

					GolukDebugUtils.e("", "-------user.nickname-----" + bean.mUserName);

					it.putExtra("userinfo", user);
					it.putExtra("type", 0);
					mContext.startActivity(it);
					CommentTimerManager.getInstance().cancelTimer();
				}
			});

		}
	}

	// 没有评论
	public void commentNoData() {
		if (null != mDataList && 0 == mDataList.size()) {
			commentHolder.mListLayout.setVisibility(View.GONE);
			return;
		}
	}

	// 评论被关闭
	public void closeComment() {
		commentHolder.mListLayout.setVisibility(View.GONE);
		commentHolder.mNoDataLayout.setVisibility(View.VISIBLE);
		commentHolder.mForbidComment.setVisibility(View.VISIBLE);
	}

	public String setClickPraise() {
		int likeNumber = 0;
		if ("0".equals(mVideoJson.data.avideo.video.ispraise)) {
			if (mVideoJson.data.avideo.video.praisenumber.replace(",", "").equals("")) {
				likeNumber = 1;
			} else {
				try {
					likeNumber = Integer.parseInt(mVideoJson.data.avideo.video.praisenumber.replace(",", "")) + 1;
				} catch (Exception e) {
					likeNumber = 1;
					e.printStackTrace();
				}
			}
			mVideoJson.data.avideo.video.ispraise = "1";
			boolean b = GolukApplication.getInstance().getVideoSquareManager()
					.clickPraise("1", mVideoJson.data.avideo.video.videoid, "1");
		} else {
			try {
				likeNumber = Integer.parseInt(mVideoJson.data.avideo.video.praisenumber.replace(",", "")) - 1;
			} catch (Exception e) {
				likeNumber = 0;
				e.printStackTrace();
			}
			mVideoJson.data.avideo.video.ispraise = "0";
		}
		mVideoJson.data.avideo.video.praisenumber = likeNumber + "";
		return GolukUtils.getFormatNumber(likeNumber + "");
	}

	public static class ViewHolder {
		// 详情
		RelativeLayout mUserCenterLayout = null;
		ImageView mImageHead = null;
		TextView mTextName = null;
		TextView mTextTime = null;
		TextView mTextLook = null;
		FullScreenVideoView mVideoView = null;
		RelativeLayout mImageLayout = null;
		ImageView simpleDraweeView = null;
		ImageView nHeadAuthentication = null;

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
		RelativeLayout mListLayout, mNoDataLayout;
		TextView mForbidComment = null;
		Uri url = null;
		TextView nTextCommentFloor;
		ImageView nCommentAuthentication;
		//奖励视频／推荐视频
		ImageView mImageHeadAward,mActiveImage,mSysImage,mRecomImage;
		TextView mTextLine1,mTextLine2,mActiveCount,mSysCount,mActiveReason,mSysReason,mRecomReason;
		LinearLayout mReasonLayout;
		RelativeLayout mActiveLayout,mSysLayout,mRecomLayout;
		
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
		if (!UserUtils.isNetDeviceAvailable(mContext) && !headHolder.mVideoView.isPlaying()) {
			return;
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
					if(error) {
						headHolder.mVideoView.suspend();
					} else {
						headHolder.mVideoView.stopPlayback();
					}
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

	/**
	 * 显示视频描述和活动名称
	 * @param view
	 * @param describe
	 * @param text
	 */
	private void showTopicText(TextView view, String describe, String text) {
		String reply_str = describe + text;
		SpannableString style = new SpannableString(reply_str);
		ClickableSpan clickttt = new TopicClickableSpan(mContext, text, mVideoJson);
		style.setSpan(clickttt, describe.length(), reply_str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		view.setText(style);
		view.setMovementMethod(LinkMovementMethod.getInstance());
		
	}
	
}
