package cn.com.mobnote.golukmobile.cluster;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.cluster.ClusterActivity.NoVideoDataViewHolder;
import cn.com.mobnote.golukmobile.cluster.bean.ActivityBean;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickFunctionListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.ClickShareListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

@SuppressLint("InflateParams")
public class ClusterAdapter extends BaseAdapter implements OnTouchListener {

	public interface IClusterInterface {
		// 刷新页面数据
		public void OnRefrushMainPageData();

		public int OnGetListViewWidth();

		public int OnGetListViewHeight();
	}

	private Context mContext = null;

	private IClusterInterface mIClusterInterface = null;

	static final int ViewType_Head = 0;
	static final int ViewType_RecommendVideoList = 1; // 推荐视频列表
	static final int ViewType_NewsVideoList = 2; // 最新视频列表
	static final int ViewType_NoData = 3;

	private int currentViewType = 1; // 当前视图类型（推荐列表，最新列表）

	ClusterActivity clusterActivity = null;

	public ActivityBean headData = null;
	public List<VideoSquareInfo> recommendlist = null;
	public List<VideoSquareInfo> newslist = null;

	private int firstItemHeight = 0;

	private int width = 0;

	public ClusterAdapter(Context context, SharePlatformUtil spf, int tabtype, IClusterInterface ici) {
		mContext = context;
		clusterActivity = (ClusterActivity) mContext;

		mIClusterInterface = ici;
		// 默认进入分享视频列表类别
		currentViewType = tabtype;

		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	}

	/**
	 * 填充数据
	 */
	public void setDataInfo(ActivityBean head, List<VideoSquareInfo> recommend, List<VideoSquareInfo> news) {
		this.headData = head;
		this.recommendlist = recommend;
		this.newslist = news;
	}

	/**
	 * 获取当前分类列表类型
	 */
	public int getCurrentViewType() {
		return currentViewType;
	}

	// 每个convert view都会调用此方法，获得当前所需要的view样式
	@Override
	public int getItemViewType(int position) {
		int p = position;
		if (p == 0) {
			return ViewType_Head;
		} else {
			if (currentViewType == ViewType_RecommendVideoList) {
				if (recommendlist == null || recommendlist.size() == 0) {
					return ViewType_NoData;
				}
			} else {
				if (newslist == null || newslist.size() == 0) {
					return ViewType_NoData;
				}
			}
			return currentViewType;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getCount() {
		if (this.headData == null) {
			return 0;
		} else {
			int datacount = 0;

			if (this.currentViewType == ViewType_RecommendVideoList) {
				if(recommendlist != null && recommendlist.size() > 0){
					datacount = this.recommendlist.size() + 1;
				}else{
					datacount++;
				}
				
			} else {
				
				if(newslist != null && newslist.size() > 0){
					datacount = this.newslist.size() + 1;
				}else{
					datacount++;
				}
				
			}
			if (datacount <= 1) {// 如果没有数据，则添加没有数据提示项
				datacount++;
			}
			return datacount;
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);

		switch (type) {
		case ViewType_Head:
			if (headData != null) {
				HeadViewHolder holder = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.cluster_head, null);
					holder = new HeadViewHolder();

					holder.headImg = (ImageView) convertView.findViewById(R.id.mPreLoading);
					holder.describe = (TextView) convertView.findViewById(R.id.video_title);
					holder.partakes = (TextView) convertView.findViewById(R.id.partake_num);
					holder.recommendBtn = (Button) convertView.findViewById(R.id.recommend_btn);
					holder.newsBtn = (Button) convertView.findViewById(R.id.news_btn);
					holder.partakeBtn = (Button) convertView.findViewById(R.id.partake_btn);
					convertView.setTag(holder);
				} else {
					holder = (HeadViewHolder) convertView.getTag();
				}
				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
				holder.headImg.setLayoutParams(mPlayerLayoutParams);
				
				GlideUtils.loadImage(mContext, holder.headImg, headData.picture,
						R.drawable.tacitly_pic);
				
				holder.describe.setText(headData.activitycontent);
				holder.partakes.setText(headData.participantcount);

				holder.partakeBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent photoalbum = new Intent(mContext,PhotoAlbumActivity.class);
						photoalbum.putExtra("from", "cloud");
						
						PromotionSelectItem item = new PromotionSelectItem();
						item.activityid = headData.activityid;
						item.activitytitle = headData.activityname;
						item.channelid = headData.channelid;
						photoalbum.putExtra(PhotoAlbumActivity.ACTIVITY_INFO, item);
						mContext.startActivity(photoalbum);
					}
				});

				holder.recommendBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (currentViewType == ViewType_NewsVideoList) {
							currentViewType = ViewType_RecommendVideoList;
							notifyDataSetChanged();
						}
					}
				});

				holder.newsBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (currentViewType == ViewType_RecommendVideoList) {
							currentViewType = ViewType_NewsVideoList;
							notifyDataSetChanged();
						}
					}
				});

				if (currentViewType == ViewType_RecommendVideoList) {
					holder.recommendBtn.setTextColor(Color.rgb(9, 132, 255));
					holder.newsBtn.setTextColor(Color.rgb(51, 51, 51));
				} else {
					holder.newsBtn.setTextColor(Color.rgb(9, 132, 255));
					holder.recommendBtn.setTextColor(Color.rgb(51, 51, 51));
				}

				// 计算第一项的高度
				this.firstItemHeight = convertView.getBottom();
			}
			break;
		case ViewType_NewsVideoList:
		case ViewType_RecommendVideoList:
			int index_v = position - 1;
			final VideoSquareInfo clusterInfo;
			if (currentViewType == ViewType_RecommendVideoList) {
				clusterInfo = this.recommendlist.get(index_v);
			} else {
				clusterInfo = this.newslist.get(index_v);
			}

			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_sharevideo, null);
				holder.imageLayout = (ImageView) convertView.findViewById(R.id.imageLayout);
				holder.headimg = (ImageView) convertView.findViewById(R.id.headimg);
				holder.nikename = (TextView) convertView.findViewById(R.id.nikename);
				holder.location = (TextView) convertView.findViewById(R.id.uc_location);
				holder.videoGoldImg = (ImageView) convertView.findViewById(R.id.user_center_gold);
				holder.recommentImg = (ImageView) convertView.findViewById(R.id.uc_recommend);

				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.function = (ImageView) convertView.findViewById(R.id.function);

				holder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
				holder.zanIcon = (ImageView) convertView.findViewById(R.id.zanIcon);
				holder.zanText = (TextView) convertView.findViewById(R.id.zanText);

				holder.commentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);
				holder.commentIcon = (ImageView) convertView.findViewById(R.id.commentIcon);
				holder.commentText = (TextView) convertView.findViewById(R.id.commentText);

				holder.shareLayout = (LinearLayout) convertView.findViewById(R.id.shareLayout);
				holder.shareIcon = (ImageView) convertView.findViewById(R.id.shareIcon);
				holder.shareText = (TextView) convertView.findViewById(R.id.shareText);

				holder.zText = (TextView) convertView.findViewById(R.id.zText);

				holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
				holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
				holder.totalcomments = (TextView) convertView.findViewById(R.id.totalcomments);

				holder.detail = (TextView) convertView.findViewById(R.id.detail);

				holder.totlaCommentLayout = (LinearLayout) convertView.findViewById(R.id.totlaCommentLayout);
				holder.comment1 = (TextView) convertView.findViewById(R.id.comment1);
				holder.comment2 = (TextView) convertView.findViewById(R.id.comment2);
				holder.comment3 = (TextView) convertView.findViewById(R.id.comment3);
				holder.isopen = (ImageView) convertView.findViewById(R.id.isopen);
				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
				holder.imageLayout.setLayoutParams(mPlayerLayoutParams);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.isopen.setVisibility(View.GONE);

			if (holder.VideoID == null || !holder.VideoID.equals(clusterInfo.mVideoEntity.videoid)) {
				holder.VideoID = new String(clusterInfo.mVideoEntity.videoid);
				
				GlideUtils.loadImage(mContext, holder.imageLayout, clusterInfo.mVideoEntity.picture,
						R.drawable.tacitly_pic);
			}

			String headUrl = clusterInfo.mUserEntity.mCustomAvatar;
			if (null != headUrl && !"".equals(headUrl)) {
				// 使用服务器头像地址
				GlideUtils.loadNetHead(mContext, holder.headimg, headUrl, R.drawable.editor_head_feault7);
			} else {
				showHead(holder.headimg, clusterInfo.mUserEntity.headportrait);
			}
			
			holder.headimg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 跳转当前点赞人的个人中心
					UCUserInfo user = new UCUserInfo();
					user.uid = clusterInfo.mUserEntity.uid;
					user.nickname = clusterInfo.mUserEntity.nickname;
					user.headportrait = clusterInfo.mUserEntity.headportrait;
					user.introduce = "";
					user.sex = clusterInfo.mUserEntity.sex;
					user.customavatar = clusterInfo.mUserEntity.mCustomAvatar;
					user.praisemenumber = "0";
					user.sharevideonumber = "0";
					Intent i = new Intent(mContext, UserCenterActivity.class);
					i.putExtra("userinfo", user);
					i.putExtra("type", 0);
					mContext.startActivity(i);
				}
			});
			holder.nikename.setText(clusterInfo.mUserEntity.nickname);
			holder.time.setText(GolukUtils.getCommentShowFormatTime(clusterInfo.mVideoEntity.sharingtime));
			
			setVideoExtra(holder, clusterInfo);
			// 设置显示 视频位置信息
			final String location = clusterInfo.mVideoEntity.location;
			if (null == location || "".equals(location)) {
				holder.location.setVisibility(View.GONE);
			} else {
				holder.location.setText(location);
			}

			holder.zText.setText(clusterInfo.mVideoEntity.praisenumber);
			holder.weiguan.setText(clusterInfo.mVideoEntity.clicknumber + " 围观");
			UserUtils.showCommentText(holder.detail, clusterInfo.mUserEntity.nickname,
					clusterInfo.mVideoEntity.describe);
			int count = Integer.parseInt(clusterInfo.mVideoEntity.comcount);
			holder.totalcomments.setText("查看所有" + clusterInfo.mVideoEntity.comcount + "条评论");
			if (count > 3) {
				holder.totalcomments.setVisibility(View.VISIBLE);
			} else {
				holder.totalcomments.setVisibility(View.GONE);
			}

			holder.zText.setText(clusterInfo.mVideoEntity.praisenumber + " 赞");
			initListener(holder, index_v);
			// 没点过
			if ("0".equals(clusterInfo.mVideoEntity.ispraise)) {
				holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like);
			} else {// 点赞过
				holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like_press);
			}
			if (clusterInfo.mVideoEntity.commentList.size() >= 1) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(0);
				if (null != comment.replyid && !"".equals(comment.replyid) && null != comment.replyname
						&& !"".equals(comment.replyname)) {
					UserUtils.showReplyText(holder.comment1, comment.name, comment.replyname, comment.text);
				} else {
					UserUtils.showCommentText(holder.comment1, comment.name, comment.text);
				}
				holder.comment1.setVisibility(View.VISIBLE);
			} else {
				holder.comment1.setVisibility(View.GONE);
			}

			if (clusterInfo.mVideoEntity.commentList.size() >= 2) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(1);
				if (null != comment.replyid && !"".equals(comment.replyid) && null != comment.replyname
						&& !"".equals(comment.replyname)) {
					UserUtils.showReplyText(holder.comment2, comment.name, comment.replyname, comment.text);
				} else {
					UserUtils.showCommentText(holder.comment2, comment.name, comment.text);
				}
				holder.comment2.setVisibility(View.VISIBLE);
			} else {
				holder.comment2.setVisibility(View.GONE);
			}

			if (clusterInfo.mVideoEntity.commentList.size() >= 3) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(2);
				if (null != comment.replyid && !"".equals(comment.replyid) && null != comment.replyname
						&& !"".equals(comment.replyname)) {
					UserUtils.showReplyText(holder.comment3, comment.name, comment.replyname, comment.text);
				} else {
					UserUtils.showCommentText(holder.comment3, comment.name, comment.text);
				}
				holder.comment3.setVisibility(View.VISIBLE);
			} else {
				holder.comment3.setVisibility(View.GONE);
			}
			break;

		case ViewType_NoData:
			NoVideoDataViewHolder noVideoDataViewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_novideodata, null);
				noVideoDataViewHolder = new NoVideoDataViewHolder();
				noVideoDataViewHolder.tipsimage = (ImageView) convertView.findViewById(R.id.tipsimage);
				noVideoDataViewHolder.bMeasureHeight = false;
				convertView.setTag(noVideoDataViewHolder);
			} else {
				noVideoDataViewHolder = (NoVideoDataViewHolder) convertView.getTag();
			}

			if (noVideoDataViewHolder.bMeasureHeight == false) {
				if (this.firstItemHeight > 0) {
					noVideoDataViewHolder.bMeasureHeight = true;
					RelativeLayout rl = (RelativeLayout) convertView.findViewById(R.id.subject_ll);
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rl.getLayoutParams();
					lp.height = mIClusterInterface.OnGetListViewHeight() - this.firstItemHeight;
					rl.setLayoutParams(lp);
				}
			}

			boolean bNeedRefrush = false;
			
			if(currentViewType == ViewType_NewsVideoList){
				noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.together_noactivity_text);
			}else{
				noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.together_norecommend_text);
			}
			
			bNeedRefrush = true;
			if (bNeedRefrush == true) {
				noVideoDataViewHolder.tipsimage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mIClusterInterface != null) {
							mIClusterInterface.OnRefrushMainPageData();
						}
					}

				});
			}
			break;
		default:
			break;
		}
		return convertView;
	}

	private void showHead(ImageView view, String headportrait) {
		try {
			GlideUtils.loadLocalHead(mContext, view, ILive.mHeadImg[Integer.parseInt(headportrait)]);
		} catch (Exception e) {
			GlideUtils.loadLocalHead(mContext, view, R.drawable.editor_head_feault7);
		}
	}

//	private void showUserInfoHead(ImageView view, String headportrait) {
//		try {
//			GlideUtils.loadLocalHead(mContext, view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
//		} catch (Exception e) {
//			GlideUtils.loadLocalHead(mContext, view, R.drawable.editor_head_feault7);
//		}
//	}

	private String mVideoId = null;

	public void setWillShareVideoId(String vid) {
		mVideoId = vid;
	}

	public String getWillShareVideoId() {
		return mVideoId;
	}

	private void initListener(ViewHolder holder, int index) {

		VideoSquareInfo mVideoSquareInfo = null;

		if (currentViewType == ViewType_RecommendVideoList) {
			mVideoSquareInfo = this.recommendlist.get(index);
		} else {
			mVideoSquareInfo = this.newslist.get(index);
		}

		// 分享监听
		ClickShareListener tempShareListener = new ClickShareListener(mContext, mVideoSquareInfo,
				(ClusterActivity) mContext);
		holder.shareLayout.setOnClickListener(tempShareListener);
		// 举报监听

		holder.function.setOnClickListener(new ClickFunctionListener(mContext, mVideoSquareInfo,
				isMy(mVideoSquareInfo.mUserEntity.uid), (ClusterActivity) mContext)
				.setConfirm(!isMy(mVideoSquareInfo.mUserEntity.uid)));
		// 评论监听
		holder.commentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, true));
		// 播放区域监听
		holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext, mVideoSquareInfo, null));
		// 点赞
		holder.praiseLayout.setOnClickListener(new ClickPraiseListener(mContext, mVideoSquareInfo,
				(ClusterActivity) mContext));
		// 评论总数监听
		List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
		if (comments.size() > 0) {
			holder.totalcomments.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
			holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
		}
	}
	
	/**
	 * 设置，视频的，是否推荐，是否获奖，是否有参加活动
	 * 
	 * @param holder
	 *            UI控件
	 * @param clusterInfo
	 *            数据载体
	 * @author jyf
	 */
	private void setVideoExtra(ViewHolder holder, VideoSquareInfo clusterInfo) {
		if (null == clusterInfo || null == holder) {
			return;
		}
		String got = "";
		if (null != clusterInfo.mVideoEntity.videoExtra) {
			// 显示是否获奖
			if (clusterInfo.mVideoEntity.videoExtra.isreward.equals("1")) {
				holder.videoGoldImg.setVisibility(View.VISIBLE);
			} else {
				holder.videoGoldImg.setVisibility(View.GONE);
			}
			// 显示是否推荐
			if (clusterInfo.mVideoEntity.videoExtra.isrecommend.equals("1")) {
				holder.recommentImg.setVisibility(View.VISIBLE);
			} else {
				holder.recommentImg.setVisibility(View.GONE);
			}
			// 获得聚合字符串
			got = clusterInfo.mVideoEntity.videoExtra.topicname;
		} else {
			holder.videoGoldImg.setVisibility(View.GONE);
			holder.recommentImg.setVisibility(View.GONE);
		}

		UserUtils.showCommentText(mContext, false,clusterInfo, holder.detail, clusterInfo.mUserEntity.nickname,
				clusterInfo.mVideoEntity.describe, got);
	}

	/**
	 * 检查是否有可用网络
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {

	}

//	public void onResume() {
//		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
//	}

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
	 * 锁住后滚动时禁止下载图片
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void lock() {
	}

	/**
	 * 解锁后恢复下载图片功能
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void unlock() {
		this.notifyDataSetChanged();
	}

	public static class HeadViewHolder {
		TextView title;
		ImageView headImg;
		TextView describe;
		TextView partakes;
		Button recommendBtn;
		Button newsBtn;
		Button partakeBtn;

	}

	public static class ViewHolder {
		String VideoID;
		ImageView imageLayout;
		ImageView headimg;
		TextView nikename;
		TextView location;
		TextView time;
		ImageView function;
		ImageView videoGoldImg;
		ImageView recommentImg;

		LinearLayout praiseLayout;
		ImageView zanIcon;
		TextView zanText;

		LinearLayout commentLayout;
		ImageView commentIcon;
		TextView commentText;

		LinearLayout shareLayout;
		ImageView shareIcon;
		TextView shareText;

		TextView zText;
		TextView weiguan;
		TextView detail;
		TextView totalcomments;

		LinearLayout totlaCommentLayout;
		TextView comment1;
		TextView comment2;
		TextView comment3;

		ImageView isopen;
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 100, 100);
		}
		return t_bitmap;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (v.getId()) {

		case R.id.share_btn:
			Button sharebtn = (Button) v;
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable more_down = mContext.getResources().getDrawable(R.drawable.share_btn_press);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_down, null, null, null);
				sharebtn.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable more_up = mContext.getResources().getDrawable(R.drawable.share_btn);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_up, null, null, null);
				sharebtn.setTextColor(Color.rgb(136, 136, 136));
				break;
			}
			break;
		}
		return false;
	}

	private boolean isMy(String uid) {
		if (!GolukApplication.getInstance().isUserLoginSucess) {
			return false;
		}
		UserInfo userInfo = GolukApplication.getInstance().getMyInfo();
		if (null == userInfo || !userInfo.uid.equals(uid)) {
			return false;
		}
		return true;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
}
