package cn.com.mobnote.golukmobile.usercenter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.golukmobile.newest.ClickCategoryListener;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickFunctionListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.ClickShareListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.newest.NewestAdapter.ViewHolder;
import cn.com.mobnote.golukmobile.special.ClusterCommentListener;
import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.ClusterPressListener;
import cn.com.mobnote.golukmobile.special.SpecialCommentListener;
import cn.com.mobnote.golukmobile.special.ClusterPressListener;
import cn.com.mobnote.golukmobile.special.SpecialCommentListener;
import cn.com.mobnote.golukmobile.special.SpecialInfo;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity.PraiseInfoGroup;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity.ShareVideoGroup;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressLint("InflateParams")
public class UserCenterAdapter extends BaseAdapter implements
		VideoSuqareManagerFn, OnTouchListener {
	private Context mContext = null;
	private ShareVideoGroup videogroupdata = null;		//分享视频数据
	private PraiseInfoGroup praisgroupData = null;		//被点赞信息数据
	private UCUserInfo userinfo;						//个人用户信息

	private SharePlatformUtil sharePlatform;

	private int width = 0;
	
	static final int ViewType_ShareVideoList = 0;	//分享视频列表
	static final int ViewType_PraiseUserList = 1;	//点赞用户列表
	
	private int currentViewType = 0;	//当前视图类型（分享视频列表，点赞列表）
	
	final int ItemType_UserInfo = 0;
	final int ItemType_VideoInfo = 1;
	final int ItemType_PraiseInfo = 2;
	final int ItemType_noVideoInfo = 3;
	final int ItemType_noPraiseInfo = 4;
	
	private Rect firstItemRect = null;
	
	/** 滚动中锁标识 */
	private boolean lock = false;
	public UserCenterAdapter(Context context, SharePlatformUtil spf) {
		mContext = context;
		videogroupdata = null;
		praisgroupData = null;
		
		sharePlatform = spf;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);
		
		//默认进入分享视频列表类别
		currentViewType = ViewType_ShareVideoList;
	}

	/**
	 * 更新数据链路
	 */
	public void setDataInfo(UCUserInfo user, ShareVideoGroup vdata, PraiseInfoGroup pdata)
	{
		this.userinfo = user;
		this.videogroupdata = vdata;
		this.praisgroupData = pdata;
	}
	
	/**
	 * 获取当前分类列表类型
	 */
	public int getCurrentViewType()
	{
		return currentViewType;
	}

	// 每个convert view都会调用此方法，获得当前所需要的view样式
	@Override
	public int getItemViewType(int position) {
		int p = position;
		if (p == 0)
			return ItemType_UserInfo;
		else
		{
			if (this.currentViewType == ViewType_ShareVideoList)
			{//视频分享列表类别
				if (videogroupdata.loadfailed == true)
				{//首次加载数据失败
					return ItemType_noVideoInfo;
				}
				else if (videogroupdata.videolist.size() <= 0)
				{//没有数据
					return ItemType_noVideoInfo;
				}
				else
					return ItemType_VideoInfo;
			}
			else
			{//点赞列表类别
				if (praisgroupData.loadfailed == true)
				{//首次加载数据失败
					return ItemType_noPraiseInfo;
				}
				else if (praisgroupData.praiselist.size() <= 0)
				{
					return ItemType_noPraiseInfo;
				}
				else
					return ItemType_PraiseInfo;
			}
		}
	}

	@Override
	public int getViewTypeCount() {
		return 5;
	}

	@Override
	public int getCount() {
		if (this.userinfo == null)
			return 0;
		int datacount = 0;
		if (this.currentViewType == ViewType_ShareVideoList)
		{
			datacount = this.videogroupdata.videolist.size() + 1;
		}
		else
		{
			datacount = this.praisgroupData.praiselist.size() + 1;
		}
		if (datacount <= 1)
		{//如果没有数据，则添加没有数据提示项
			datacount++;
		}
		return datacount;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);
		
		switch (type) {
		case ItemType_UserInfo:
			if (userinfo != null) {
				UserViewHolder holder = null;
				if (convertView == null)
				{
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.user_center_userinfo, null);
					holder = new UserViewHolder();
					
					holder.headImg = (ImageView) convertView
							.findViewById(R.id.user_head);
					holder.username = (TextView) convertView
							.findViewById(R.id.username);
					holder.description = (TextView) convertView
							.findViewById(R.id.description);
					holder.fxsp_num = (TextView) convertView
							.findViewById(R.id.fxsp_num);
					holder.fxsp_txt = (TextView) convertView
							.findViewById(R.id.fxsp_txt);
					holder.dz_num = (TextView) convertView
							.findViewById(R.id.dz_num);
					holder.dz_txt = (TextView) convertView
							.findViewById(R.id.dz_txt);
					holder.praise_select = (ImageView)convertView
							.findViewById(R.id.praise_select);
					holder.video_select = (ImageView)convertView
							.findViewById(R.id.video_select);
					holder.sharelayout = (LinearLayout)convertView
							.findViewById(R.id.sharelayout);
					holder.praiselayout = (LinearLayout)convertView
							.findViewById(R.id.praiselayout);
					
					convertView.setTag(holder);
				}
				else
				{
					holder = (UserViewHolder)convertView.getTag();
				}
				holder.headImg.setBackgroundResource(ILive.mHeadImg[Integer.valueOf(userinfo.headportrait)]); 
				holder.username.setText(userinfo.nickname);
				holder.description.setText(userinfo.introduce);
				holder.fxsp_num.setText(userinfo.sharevideonumber);
				holder.dz_num.setText(userinfo.praisemenumber);
				
				if (currentViewType == ViewType_ShareVideoList) {
					holder.praise_select.setVisibility(View.INVISIBLE);
					holder.video_select.setVisibility(View.VISIBLE);
					holder.fxsp_txt.setTextColor(Color.rgb(9, 132, 255));
					holder.fxsp_num.setTextColor(Color.rgb(9, 132, 255));

					holder.dz_txt.setTextColor(Color.rgb(255, 255, 255));
					holder.dz_num.setTextColor(Color.rgb(255, 255, 255));
				} else {
					holder.video_select.setVisibility(View.INVISIBLE);
					holder.praise_select.setVisibility(View.VISIBLE);

					holder.dz_txt.setTextColor(Color.rgb(9, 132, 255));
					holder.dz_num.setTextColor(Color.rgb(9, 132, 255));

					holder.fxsp_txt.setTextColor(Color.rgb(255, 255, 255));
					holder.fxsp_num.setTextColor(Color.rgb(255, 255, 255));
				}

				holder.sharelayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub

						if (ViewType_ShareVideoList != currentViewType)
						{
							currentViewType = ViewType_ShareVideoList;
							notifyDataSetChanged();
						}
					}
				});

				holder.praiselayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (ViewType_PraiseUserList != currentViewType)
						{
							currentViewType = ViewType_PraiseUserList;
							notifyDataSetChanged();
						}
					}
				});
				
			}
			break;
		case ItemType_VideoInfo:
			int index_v = position - 1;
			VideoSquareInfo clusterInfo = this.videogroupdata.videolist.get(index_v);
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.newest_list_item, null);
				holder.imageLayout = (RelativeLayout) convertView
						.findViewById(R.id.imageLayout);
				holder.headimg = (ImageView) convertView
						.findViewById(R.id.headimg);
				holder.nikename = (TextView) convertView
						.findViewById(R.id.nikename);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.function = (ImageView) convertView
						.findViewById(R.id.function);

				holder.praiseLayout = (LinearLayout) convertView
						.findViewById(R.id.praiseLayout);
				holder.zanIcon = (ImageView) convertView
						.findViewById(R.id.zanIcon);
				holder.zanText = (TextView) convertView
						.findViewById(R.id.zanText);

				holder.commentLayout = (LinearLayout) convertView
						.findViewById(R.id.commentLayout);
				holder.commentIcon = (ImageView) convertView
						.findViewById(R.id.commentIcon);
				holder.commentText = (TextView) convertView
						.findViewById(R.id.commentText);

				holder.shareLayout = (LinearLayout) convertView
						.findViewById(R.id.shareLayout);
				holder.shareIcon = (ImageView) convertView
						.findViewById(R.id.shareIcon);
				holder.shareText = (TextView) convertView
						.findViewById(R.id.shareText);

				holder.zText = (TextView) convertView.findViewById(R.id.zText);
				holder.weiguan = (TextView) convertView
						.findViewById(R.id.weiguan);
				holder.weiguan = (TextView) convertView
						.findViewById(R.id.weiguan);
				holder.totalcomments = (TextView) convertView
						.findViewById(R.id.totalcomments);

				holder.detail = (TextView) convertView
						.findViewById(R.id.detail);
				
				holder.totlaCommentLayout = (LinearLayout) convertView.findViewById(R.id.totlaCommentLayout);
				holder.comment1 = (TextView) convertView
						.findViewById(R.id.comment1);
				holder.comment2 = (TextView) convertView
						.findViewById(R.id.comment2);
				holder.comment3 = (TextView) convertView
						.findViewById(R.id.comment3);

				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(
						width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW,
						R.id.headlayout);
				holder.imageLayout.setLayoutParams(mPlayerLayoutParams);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.headimg.setBackgroundResource(ILive.mHeadImg[Integer.valueOf(clusterInfo.mUserEntity.headportrait)]);
			holder.nikename.setText(clusterInfo.mUserEntity.nickname);
			holder.time.setText(clusterInfo.mVideoEntity.sharingtime);
			holder.zText.setText(clusterInfo.mVideoEntity.praisenumber);
			holder.weiguan.setText(clusterInfo.mVideoEntity.clicknumber + " 围观");
			holder.detail.setText(clusterInfo.mUserEntity.nickname + "  "
					+ clusterInfo.mVideoEntity.describe);
			holder.totalcomments.setText("查看所有" + clusterInfo.mVideoEntity.comcount + "条评论");
			holder.zText.setText(clusterInfo.mVideoEntity.praisenumber + " 赞");
			loadImage(holder.imageLayout, clusterInfo.mVideoEntity.picture);
			initListener( holder, index_v);
			// 没点过
			if ("0".equals(clusterInfo.mVideoEntity.ispraise)) {
				holder.zanIcon
						.setBackgroundResource(R.drawable.videodetail_like);
			} else {// 点赞过
				holder.zanIcon
						.setBackgroundResource(R.drawable.videodetail_like_press);
			}
			if (clusterInfo.mVideoEntity.commentList.size() >= 1) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(0);
				holder.comment1.setText(comment.name + "  "
						+ comment.text);
			} else {
				holder.comment1.setVisibility(View.GONE);
			}

			if (clusterInfo.mVideoEntity.commentList.size() >= 2) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(1);
				holder.comment2.setText(comment.name + "  "
						+ comment.text);
			} else {
				holder.comment2.setVisibility(View.GONE);
			}

			if (clusterInfo.mVideoEntity.commentList.size() >= 3) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(2);
				holder.comment3.setText(comment.name + "  "
						+ comment.text);
			} else {
				holder.comment3.setVisibility(View.GONE);
			}
			break;
		case ItemType_PraiseInfo:
			int index_p = position - 1;
			final PraiseInfo prais = this.praisgroupData.praiselist.get(index_p);
			PraiseViewHolder praiseholder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.user_center_praise, null);
				praiseholder = new PraiseViewHolder();
				praiseholder.praiseLayout = (LinearLayout) convertView
						.findViewById(R.id.praiseLayout);
				praiseholder.headimg = (ImageView) convertView
						.findViewById(R.id.userhead);
				praiseholder.username = (TextView) convertView
						.findViewById(R.id.username);
				praiseholder.desc = (TextView) convertView
						.findViewById(R.id.desc);
				praiseholder.videoPic = (ImageView) convertView
						.findViewById(R.id.videopic);
				
				convertView.setTag(praiseholder);
			}
			else
				praiseholder = (PraiseViewHolder)convertView.getTag();
			
			praiseholder.headimg.setBackgroundResource(ILive.mHeadImg[Integer.valueOf(prais.headportrait)]);
			praiseholder.username.setText(prais.nickname);
//			praiseholder.desc.setText(prais.introduce);
			praiseholder.desc.setText("赞了您的视频");
			praiseholder.praiseLayout.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					//跳转当前点赞人的个人中心
					UCUserInfo user = new UCUserInfo();
			        user.uid = prais.uid;
		            user.nickname = prais.nickname;
		            user.headportrait = prais.headportrait;
		            user.introduce = prais.introduce;
		            user.sex = prais.sex;
		            user.customavatar = prais.customavatar;
		            user.praisemenumber = "0";
		            user.sharevideonumber = "0";
			        Intent i = new Intent(mContext, UserCenterActivity.class);
					i.putExtra("userinfo",user);
					mContext.startActivity(i);
					
				}
				
			});
			break;
		case ItemType_noVideoInfo:
			{
				NoVideoDataViewHolder noVideoDataViewHolder = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.user_center_novideodata, null);
					noVideoDataViewHolder = new NoVideoDataViewHolder();
//					noVideoDataViewHolder.tips = (TextView) convertView
//							.findViewById(R.id.novideoinfo);
					noVideoDataViewHolder.tipsimage = (ImageView) convertView
							.findViewById(R.id.tipsimage);
					
					convertView.setTag(noVideoDataViewHolder);
				}
				else
					noVideoDataViewHolder = (NoVideoDataViewHolder)convertView.getTag();
				if (this.videogroupdata.loadfailed == true) {
					noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.qitadifang);
				}
				else {
					noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.videodetail_sofaicon);
				}
				
			}
			break;
		case ItemType_noPraiseInfo:
			{
				NoVideoDataViewHolder noVideoDataViewHolder = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.user_center_novideodata, null);
					noVideoDataViewHolder = new NoVideoDataViewHolder();
//					noVideoDataViewHolder.tips = (TextView) convertView
//							.findViewById(R.id.novideoinfo);
					noVideoDataViewHolder.tipsimage = (ImageView) convertView
							.findViewById(R.id.tipsimage);
					
					convertView.setTag(noVideoDataViewHolder);
				}
				else
					noVideoDataViewHolder = (NoVideoDataViewHolder)convertView.getTag();
				if (this.praisgroupData.loadfailed == true) {
					noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.qitadifang);
				}
				else {
					noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.videodetail_sofaicon);
				}
			}
			break;
		default:
			break;
		}

		if (position == 0)
		{
//			Rect rc = new Rect();
//			rc.left = convertView.getLeft();
//			rc.top = convertView.
//			rc.right = convertView.getWidth();
//			rc.bottom = convertView.getHeight();
//			this.firstItemRect = rc;
//			Log.e("", "=================RECT========" + rc.left + ","+ rc.top + ","+ rc.right + ","+ rc.bottom);
			
		}
		return convertView;
	}

	private String mVideoId = null;

	public void setWillShareVideoId(String vid) {
		mVideoId = vid;
	}

	public String getWillShareVideoId() {
		return mVideoId;
	}

	private void initListener(ViewHolder holder, int index) {
		VideoSquareInfo mVideoSquareInfo = this.videogroupdata.videolist.get(index);

		// 分享监听
		ClickShareListener tempShareListener = new ClickShareListener(mContext, mVideoSquareInfo, (UserCenterActivity)mContext);
		holder.shareLayout.setOnClickListener(tempShareListener);
		// 举报监听
		holder.function.setOnClickListener(new ClickFunctionListener(mContext, mVideoSquareInfo));
		// 评论监听
		holder.commentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, true));
		// 播放区域监听
		holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext,  mVideoSquareInfo, null));
		// 点赞
		ClickPraiseListener tempPraiseListener = new ClickPraiseListener(mContext, mVideoSquareInfo, (UserCenterActivity)mContext);
		holder.praiseLayout.setOnClickListener(tempPraiseListener);
		// 评论总数监听
		List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
		if (comments.size() > 0) {
			holder.totalcomments.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
			holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false));
		}
	}
	
	/**
	 * 检查是否有可用网络
	 * @return
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);
	}

//	/**
//	 * 点赞
//	 * 
//	 * @Title: setLikePress
//	 * @Description: TODO
//	 * @param clusterInfo
//	 *            void
//	 * @author 曾浩
//	 * @throws
//	 */
//	public void setLikePress(ClusterInfo clusterInfo) {
//		for (int i = 0; i < this.videogroupdata.videolist.size(); i++) {
//			ClusterInfo cl = this.videogroupdata.videolist.get(i);
//			if (cl.videoid.equals(clusterInfo.videoid)) {
//				this.videogroupdata.videolist.set(i, clusterInfo);
//				break;
//			}
//		}
//		
//		this.notifyDataSetChanged();
//
//	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");

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

	private void loadImage(RelativeLayout layout, String url) {
		layout.removeAllViews();
		SimpleDraweeView view = new SimpleDraweeView(mContext);
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(
				mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
		// .setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic),
		// ScaleType.FIT_XY)
		// .setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic),
		// ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(hierarchy);

		if (!lock) {
			view.setImageURI(Uri.parse(url));
		}

		int height = (int) ((float) width / 1.77f);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
				width, height);
		layout.addView(view, mPreLoadingParams);
		//
	}

	/**
	 * 锁住后滚动时禁止下载图片
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void lock() {
		lock = true;
	}

	/**
	 * 解锁后恢复下载图片功能
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void unlock() {
		lock = false;
		this.notifyDataSetChanged();
	}

	public static class UserViewHolder {
		ImageView headImg;
		TextView username;
		TextView description;
		
		TextView fxsp_num;
		TextView fxsp_txt;
		TextView dz_num;
		TextView dz_txt;
		
		ImageView praise_select;
		ImageView video_select;
		
		LinearLayout sharelayout;
		LinearLayout praiselayout;
	}
	
	public static class PraiseViewHolder {
		LinearLayout praiseLayout;
		ImageView headimg;
		TextView username;
		TextView desc;
		ImageView videoPic;
	}
	
	public static class NoVideoDataViewHolder {
		TextView tips;
		ImageView tipsimage;
	}
	
	public static class ViewHolder {
		RelativeLayout imageLayout;
		ImageView headimg;
		TextView nikename;
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
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory()
				+ File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(),
					100, 100);
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
				Drawable more_down = mContext.getResources().getDrawable(
						R.drawable.share_btn_press);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_down,
						null, null, null);
				sharebtn.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable more_up = mContext.getResources().getDrawable(
						R.drawable.share_btn);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_up, null,
						null, null);
				sharebtn.setTextColor(Color.rgb(136, 136, 136));
				break;
			}
			break;
		}
		return false;
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		// TODO Auto-generated method stub

	}

}
