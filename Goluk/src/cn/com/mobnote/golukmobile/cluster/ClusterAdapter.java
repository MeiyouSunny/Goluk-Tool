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
import android.net.Uri;
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
import cn.com.mobnote.golukmobile.cluster.bean.ClusterHeadBean;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickFunctionListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.ClickShareListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UserCenterAdapter.IUserCenterInterface;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

@SuppressLint("InflateParams")
public class ClusterAdapter extends BaseAdapter implements VideoSuqareManagerFn, OnTouchListener {

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
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
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
				datacount = this.recommendlist.size() + 1;
			} else {
				datacount = this.newslist.size() + 1;
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

				showUserInfoHead(holder.headImg, headData.picture);
				holder.describe.setText(headData.activitycontent);
				holder.partakes.setText(headData.participantcount);

				holder.partakeBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub

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
					holder.newsBtn.setTextColor(Color.rgb(255, 255, 255));
				} else {
					holder.newsBtn.setTextColor(Color.rgb(9, 132, 255));
					holder.recommendBtn.setTextColor(Color.rgb(255, 255, 255));
				}

				// 计算第一项的高度
				this.firstItemHeight = convertView.getBottom();
			}
			break;
		case ViewType_NewsVideoList:
		case ViewType_RecommendVideoList:
			int index_v = position - 1;
			VideoSquareInfo clusterInfo;
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
				holder.imageLayout.setImageURI(Uri.parse(clusterInfo.mVideoEntity.picture));
			}

			String headUrl = clusterInfo.mUserEntity.mCustomAvatar;
			if (null != headUrl && !"".equals(headUrl)) {
				// 使用服务器头像地址
				holder.headimg.setImageURI(Uri.parse(headUrl));
			} else {
				showHead(holder.headimg, clusterInfo.mUserEntity.headportrait);
			}
			holder.nikename.setText(clusterInfo.mUserEntity.nickname);
			holder.time.setText(GolukUtils.getCommentShowFormatTime(clusterInfo.mVideoEntity.sharingtime));
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
			noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_qitadifang);
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

	private void showUserInfoHead(ImageView view, String headportrait) {
		try {
			GlideUtils.loadLocalHead(mContext, view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
		} catch (Exception e) {
			GlideUtils.loadLocalHead(mContext, view, R.drawable.editor_head_feault7);
		}
	}

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

	public void onResume() {
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
	}

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

	// private void loadImage(SimpleDraweeView layout, String url, int nWidth) {
	// layout.setImageURI(Uri.parse(url));
	// }

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
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {

	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
