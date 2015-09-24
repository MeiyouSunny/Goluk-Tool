package cn.com.mobnote.golukmobile.usercenter;

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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserPersonalInfoActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickFunctionListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.ClickShareListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.newest.IDialogDealFn;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity.PraiseInfoGroup;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity.ShareVideoGroup;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;

import cn.com.mobnote.util.GolukUtils;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lidroid.xutils.util.LogUtils;

@SuppressLint("InflateParams")
public class UserCenterAdapter extends BaseAdapter implements
		VideoSuqareManagerFn, OnTouchListener {

	public interface IUserCenterInterface {
		// 刷新页面数据
		public void OnRefrushMainPageData();

		public int OnGetListViewWidth();

		public int OnGetListViewHeight();
	}

	private IUserCenterInterface mUserCenterInterface = null;

	private Context mContext = null;
	private ShareVideoGroup videogroupdata = null; // 分享视频数据
	private PraiseInfoGroup praisgroupData = null; // 被点赞信息数据
	private UCUserInfo userinfo; // 个人用户信息

	private SharePlatformUtil sharePlatform;

	private int width = 0;

	static final int ViewType_ShareVideoList = 0; // 分享视频列表
	static final int ViewType_PraiseUserList = 1; // 点赞用户列表

	private int currentViewType = 0; // 当前视图类型（分享视频列表，点赞列表）

	final int ItemType_UserInfo = 0;
	final int ItemType_VideoInfo = 1;
	final int ItemType_PraiseInfo = 2;
	final int ItemType_noDataInfo = 3;
	private int firstItemHeight = 0;
	/** 滚动中锁标识 */
	private boolean lock = false;

	UserCenterActivity uca = null;
	

	public UserCenterAdapter(Context context, SharePlatformUtil spf,
			IUserCenterInterface iUser, int tabtype) {
		mContext = context;
		videogroupdata = null;
		praisgroupData = null;
		mUserCenterInterface = iUser;
		uca = (UserCenterActivity) mContext;
		sharePlatform = spf;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);

		// 默认进入分享视频列表类别
		currentViewType = tabtype;
	}

	/**
	 * 更新数据链路
	 */
	public void setDataInfo(UCUserInfo user, ShareVideoGroup vdata,
			PraiseInfoGroup pdata) {
		this.userinfo = user;
		this.videogroupdata = vdata;
		this.praisgroupData = pdata;
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
		if (p == 0)
			return ItemType_UserInfo;
		else {
			if (this.currentViewType == ViewType_ShareVideoList) {// 视频分享列表类别
				if (videogroupdata.loadfailed == true) {// 首次加载数据失败
					return ItemType_noDataInfo;
				} else if (videogroupdata.videolist.size() <= 0) {// 没有数据
					return ItemType_noDataInfo;
				} else
					return ItemType_VideoInfo;
			} else {// 点赞列表类别
				if (praisgroupData.loadfailed == true) {// 首次加载数据失败
					return ItemType_noDataInfo;
				} else if (praisgroupData.praiselist.size() <= 0) {
					return ItemType_noDataInfo;
				} else
					return ItemType_PraiseInfo;
			}
		}
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getCount() {
		if (this.userinfo == null)
			return 0;
		int datacount = 0;
		if (this.currentViewType == ViewType_ShareVideoList) {
			datacount = this.videogroupdata.videolist.size() + 1;
		} else {
			datacount = this.praisgroupData.praiselist.size() + 1;
		}
		if (datacount <= 1) {// 如果没有数据，则添加没有数据提示项
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
				if (convertView == null) {
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
					holder.praise_select = (ImageView) convertView
							.findViewById(R.id.praise_select);
					holder.video_select = (ImageView) convertView
							.findViewById(R.id.video_select);
					holder.sharelayout = (LinearLayout) convertView
							.findViewById(R.id.sharelayout);
					holder.praiselayout = (LinearLayout) convertView
							.findViewById(R.id.praiselayout);
					holder.usercenterlyout = (LinearLayout) convertView
							.findViewById(R.id.user_center_lyout);
					
					holder.sharebtn = (ImageView) convertView.findViewById(R.id.title_share);
					

//					holder.userinfoarrow = (ImageView) convertView
//							.findViewById(R.id.userinfo_arrow);
					
					convertView.setTag(holder);
				} else {
					holder = (UserViewHolder) convertView.getTag();
				}
				
				if (uca.testUser()) {
					holder.dz_txt.setText("赞我的人");
					//holder.userinfoarrow.setVisibility(View.VISIBLE);
					holder.headImg.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									// 跳到个人中心编辑页面
									Intent it = new Intent(mContext,
											UserPersonalInfoActivity.class);
									mContext.startActivity(it);
								}
							});
					holder.usercenterlyout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							Intent it = new Intent(mContext,
									UserPersonalInfoActivity.class);
							mContext.startActivity(it);
						}
					});
				} else {
					holder.dz_txt.setText("赞Ta的人");
					//holder.userinfoarrow.setVisibility(View.INVISIBLE);
				}
				holder.headImg.setBackgroundResource(ILive.mBigHeadImg[Integer
						.valueOf(userinfo.headportrait)]);
				holder.username.setText(userinfo.nickname);
				holder.description.setText(userinfo.introduce);
				if (userinfo.sharevideonumber != null
						&& !"".equals(userinfo.sharevideonumber)) {
					holder.fxsp_num.setText(GolukUtils
							.getFormatNumber(userinfo.sharevideonumber));
				} else {
					holder.fxsp_num.setText("0");
				}

				if (userinfo.praisemenumber != null
						&& !"".equals(userinfo.praisemenumber)) {
					holder.dz_num.setText(GolukUtils
							.getFormatNumber(userinfo.praisemenumber));
				} else {
					holder.dz_num.setText("0");
				}

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
				
				holder.sharebtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						uca.showProgressDialog();
						boolean flog = GolukApplication.getInstance()
								.getVideoSquareManager().getUserCenterShareUrl(userinfo.uid);
						if (flog == false) {
							GolukUtils.showToast(mContext, "请求异常，请检查网络是否正常");
						}
					}
				});
				
				
				if(currentViewType == ViewType_ShareVideoList){
					if(videogroupdata.isHaveData){
						uca.updateTheEnd(false);
					}
						
				}else if(currentViewType == ViewType_PraiseUserList){
					uca.updateTheEnd(false);
					if(praisgroupData.praiselist != null && praisgroupData.praiselist.size()>0){
						uca.updateTheEnd(true);
					}else{
						uca.updateTheEnd(false);
					}
				}
				
				
				
				
				holder.sharelayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub

						if (ViewType_ShareVideoList != currentViewType) {
							currentViewType = ViewType_ShareVideoList;
							notifyDataSetChanged();
						}
					}
				});

				holder.praiselayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (ViewType_PraiseUserList != currentViewType) {
							currentViewType = ViewType_PraiseUserList;
							notifyDataSetChanged();
						}
					}
				});
				// 计算第一项的高度
				this.firstItemHeight = convertView.getBottom();
			}
			break;
		case ItemType_VideoInfo:
			int index_v = position - 1;
			VideoSquareInfo clusterInfo = this.videogroupdata.videolist
					.get(index_v);
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.user_center_sharevideo, null);
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

				holder.totlaCommentLayout = (LinearLayout) convertView
						.findViewById(R.id.totlaCommentLayout);
				holder.comment1 = (TextView) convertView
						.findViewById(R.id.comment1);
				holder.comment2 = (TextView) convertView
						.findViewById(R.id.comment2);
				holder.comment3 = (TextView) convertView
						.findViewById(R.id.comment3);
				holder.isopen = (ImageView) convertView
						.findViewById(R.id.isopen);

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

			if (uca.testUser()) {
				if ("0".equals(clusterInfo.mVideoEntity.isopen)) {
					holder.isopen.setVisibility(View.VISIBLE);
				} else {
					holder.isopen.setVisibility(View.GONE);
				}
			}
			holder.headimg.setBackgroundResource(ILive.mHeadImg[Integer
					.valueOf(clusterInfo.mUserEntity.headportrait)]);

			holder.nikename.setText(clusterInfo.mUserEntity.nickname);
			holder.time
					.setText(GolukUtils
							.getCommentShowFormatTime(clusterInfo.mVideoEntity.sharingtime));
			holder.zText.setText(clusterInfo.mVideoEntity.praisenumber);
			holder.weiguan
					.setText(clusterInfo.mVideoEntity.clicknumber + " 围观");
			holder.detail.setText(clusterInfo.mUserEntity.nickname + "  "
					+ clusterInfo.mVideoEntity.describe);
			int count = Integer.parseInt(clusterInfo.mVideoEntity.comcount);
			holder.totalcomments.setText("查看所有"
					+ clusterInfo.mVideoEntity.comcount + "条评论");
			if (count > 3) {
				holder.totalcomments.setVisibility(View.VISIBLE);
			} else {
				holder.totalcomments.setVisibility(View.GONE);
			}

			holder.zText.setText(clusterInfo.mVideoEntity.praisenumber + " 赞");
			loadImage(holder.imageLayout, clusterInfo.mVideoEntity.picture,
					width);
			initListener(holder, index_v);
			// 没点过
			if ("0".equals(clusterInfo.mVideoEntity.ispraise)) {
				holder.zanIcon
						.setBackgroundResource(R.drawable.videodetail_like);
			} else {// 点赞过
				holder.zanIcon
						.setBackgroundResource(R.drawable.videodetail_like_press);
			}
			if (clusterInfo.mVideoEntity.commentList.size() >= 1) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList
						.get(0);
				holder.comment1.setText(comment.name + "  " + comment.text);
				holder.comment1.setVisibility(View.VISIBLE);
			} else {
				holder.comment1.setVisibility(View.GONE);
			}

			if (clusterInfo.mVideoEntity.commentList.size() >= 2) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList
						.get(1);
				holder.comment2.setText(comment.name + "  " + comment.text);
				holder.comment2.setVisibility(View.VISIBLE);
			} else {
				holder.comment2.setVisibility(View.GONE);
			}

			if (clusterInfo.mVideoEntity.commentList.size() >= 3) {
				CommentDataInfo comment = clusterInfo.mVideoEntity.commentList
						.get(2);
				holder.comment3.setText(comment.name + "  " + comment.text);
				holder.comment3.setVisibility(View.VISIBLE);
			} else {
				holder.comment3.setVisibility(View.GONE);
			}
			break;
		case ItemType_PraiseInfo:
			int index_p = position - 1;
			final PraiseInfo prais = this.praisgroupData.praiselist
					.get(index_p);
			PraiseViewHolder praiseholder = null;
			int nwidth = (int) (GolukUtils.mDensity * 95);
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
				praiseholder.videoPicLayout = (RelativeLayout) convertView
						.findViewById(R.id.videopic);
				praiseholder.userinfo = (LinearLayout) convertView
						.findViewById(R.id.userinfo);

				int nheight = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(
						nwidth, nheight);
				mPlayerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
						RelativeLayout.TRUE);
				mPlayerLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
						RelativeLayout.TRUE);
				mPlayerLayoutParams.rightMargin = (int) (GolukUtils.mDensity * 5);
				praiseholder.videoPicLayout
						.setLayoutParams(mPlayerLayoutParams);

				convertView.setTag(praiseholder);
			} else {
				praiseholder = (PraiseViewHolder) convertView.getTag();
			}

			loadImage(praiseholder.videoPicLayout, prais.picture, nwidth);
			praiseholder.headimg.setBackgroundResource(ILive.mHeadImg[Integer
					.valueOf(prais.headportrait)]);
			praiseholder.username.setText(prais.nickname);
			// praiseholder.desc.setText(prais.introduce);
			praiseholder.desc.setText(prais.introduce);
			praiseholder.userinfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(mContext, VideoDetailActivity.class);
					LogUtils.d("fucking = " + prais.videoid);
					i.putExtra("videoid", prais.videoid);
					mContext.startActivity(i);
				}
			});
			praiseholder.headimg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 跳转当前点赞人的个人中心
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
					i.putExtra("userinfo", user);
					i.putExtra("type", 0);
					mContext.startActivity(i);

				}

			});
			praiseholder.videoPicLayout
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							LogUtils.d("fucking = " + prais.videoid);
							Intent i = new Intent(mContext,
									VideoDetailActivity.class);
							i.putExtra("videoid", prais.videoid);
							mContext.startActivity(i);
						}

					});
			break;
		case ItemType_noDataInfo:
			NoVideoDataViewHolder noVideoDataViewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.user_center_novideodata, null);
				noVideoDataViewHolder = new NoVideoDataViewHolder();
				noVideoDataViewHolder.tipsimage = (ImageView) convertView
						.findViewById(R.id.tipsimage);
				noVideoDataViewHolder.bMeasureHeight = false;
				convertView.setTag(noVideoDataViewHolder);
			} else {
				noVideoDataViewHolder = (NoVideoDataViewHolder) convertView
						.getTag();
			}

			if (noVideoDataViewHolder.bMeasureHeight == false) {
				if (this.firstItemHeight > 0) {
					noVideoDataViewHolder.bMeasureHeight = true;
					RelativeLayout rl = (RelativeLayout) convertView
							.findViewById(R.id.subject_ll);
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rl
							.getLayoutParams();
					lp.height = mUserCenterInterface.OnGetListViewHeight()
							- this.firstItemHeight;
					rl.setLayoutParams(lp);
				}
			}
					
			boolean bNeedRefrush = false;
			if (this.currentViewType == ViewType_ShareVideoList) {
				// 分享视频列表
				if (this.videogroupdata.loadfailed == true) {
					noVideoDataViewHolder.tipsimage
						.setBackgroundResource(R.drawable.mine_qitadifang);
					bNeedRefrush = true;
				} else {
					if(uca.testUser()){
						noVideoDataViewHolder.tipsimage
						.setBackgroundResource(R.drawable.mine_novideo);
					}else{
						noVideoDataViewHolder.tipsimage
						.setBackgroundResource(R.drawable.mine_tavideo);
					}
					
				}
			} else {
				// 被点赞人信息列表
				if (this.praisgroupData.loadfailed == true) {
					noVideoDataViewHolder.tipsimage
						.setBackgroundResource(R.drawable.mine_qitadifang);
					bNeedRefrush = true;
				} else {
					
					if(uca.testUser()){
						noVideoDataViewHolder.tipsimage
						.setBackgroundResource(R.drawable.mine_nolike);
					}else{
						noVideoDataViewHolder.tipsimage
						.setBackgroundResource(R.drawable.mine_talike);
					}
					
				}
			}
			if (bNeedRefrush == true) {
				noVideoDataViewHolder.tipsimage
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								if (mUserCenterInterface != null) {
									mUserCenterInterface
											.OnRefrushMainPageData();
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

	private String mVideoId = null;

	public void setWillShareVideoId(String vid) {
		mVideoId = vid;
	}

	public String getWillShareVideoId() {
		return mVideoId;
	}

	private void initListener(ViewHolder holder, int index) {
		VideoSquareInfo mVideoSquareInfo = this.videogroupdata.videolist
				.get(index);

		// 分享监听
		ClickShareListener tempShareListener = new ClickShareListener(mContext,
				mVideoSquareInfo, (UserCenterActivity) mContext);
		holder.shareLayout.setOnClickListener(tempShareListener);
		// 举报监听

		holder.function.setOnClickListener(new ClickFunctionListener(mContext,
				mVideoSquareInfo, isMy(mVideoSquareInfo.mUserEntity.uid),
				(UserCenterActivity) mContext)
				.setConfirm(!isMy(mVideoSquareInfo.mUserEntity.uid)));
		// 评论监听
		holder.commentLayout.setOnClickListener(new ClickCommentListener(
				mContext, mVideoSquareInfo, true));
		// 播放区域监听
		holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext,
				mVideoSquareInfo, null));
		// 点赞
		ClickPraiseListener tempPraiseListener = new ClickPraiseListener(
				mContext, mVideoSquareInfo, (UserCenterActivity) mContext);
		holder.praiseLayout.setOnClickListener(tempPraiseListener);
		// 评论总数监听
		List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
		if (comments.size() > 0) {
			holder.totalcomments.setOnClickListener(new ClickCommentListener(
					mContext, mVideoSquareInfo, false));
			holder.totlaCommentLayout
					.setOnClickListener(new ClickCommentListener(mContext,
							mVideoSquareInfo, false));
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
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);
	}

	// /**
	// * 点赞
	// *
	// * @Title: setLikePress
	// * @Description: TODO
	// * @param clusterInfo
	// * void
	// * @author 曾浩
	// * @throws
	// */
	// public void setLikePress(ClusterInfo clusterInfo) {
	// for (int i = 0; i < this.videogroupdata.videolist.size(); i++) {
	// ClusterInfo cl = this.videogroupdata.videolist.get(i);
	// if (cl.videoid.equals(clusterInfo.videoid)) {
	// this.videogroupdata.videolist.set(i, clusterInfo);
	// break;
	// }
	// }
	//
	// this.notifyDataSetChanged();
	//
	// }

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

	private void loadImage(RelativeLayout layout, String url, int nWidth) {
		layout.removeAllViews();
		SimpleDraweeView view = new SimpleDraweeView(mContext);
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(
				mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder
				.setFadeDuration(300)
				.setPlaceholderImage(
						mContext.getResources().getDrawable(
								R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setFailureImage(
						mContext.getResources().getDrawable(
								R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(hierarchy);

		if (!lock) {
			view.setImageURI(Uri.parse(url));
		}

		int height = (int) ((float) nWidth / 1.77f);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
				nWidth, height);
		mPreLoadingParams.addRule(RelativeLayout.CENTER_VERTICAL,
				RelativeLayout.TRUE);
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

		LinearLayout usercenterlyout;
		
		ImageView sharebtn;
		//ImageView userinfoarrow;
	}

	public static class PraiseViewHolder {
		LinearLayout praiseLayout;
		ImageView headimg;
		TextView username;
		TextView desc;
		// ImageView videoPic;
		LinearLayout userinfo;
		RelativeLayout videoPicLayout;
	}

	public static class NoVideoDataViewHolder {
		TextView tips;
		ImageView tipsimage;
		boolean bMeasureHeight;
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

		ImageView isopen;
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

	public void dealData(String vid) {
		if (null == vid || null == videogroupdata) {
			return;
		}
		int size = videogroupdata.videolist.size();
		for (int i = 0; i < size; i++) {
			if (videogroupdata.videolist.get(i).mVideoEntity.videoid
					.equals(vid)) {
				videogroupdata.videolist.remove(i);
				this.notifyDataSetChanged();
				break;
			}
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {

	}
}
