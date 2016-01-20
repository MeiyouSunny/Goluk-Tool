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
import cn.com.mobnote.golukmobile.UserPersonalInfoActivity;
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
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity.PraiseInfoGroup;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity.ShareVideoGroup;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

@SuppressLint("InflateParams")
public class UserCenterAdapter extends BaseAdapter implements VideoSuqareManagerFn, OnTouchListener {

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

	private int width = 0;

	static final int ViewType_ShareVideoList = 0; // 分享视频列表
	static final int ViewType_PraiseUserList = 1; // 点赞用户列表

	private int currentViewType = 0; // 当前视图类型（分享视频列表，点赞列表）

	final int ItemType_UserInfo = 0;
	final int ItemType_VideoInfo = 1;
	final int ItemType_PraiseInfo = 2;
	final int ItemType_noDataInfo = 3;
	private int firstItemHeight = 0;
	UserCenterActivity uca = null;

	private boolean mBneedRefrush = false;

	public UserCenterAdapter(Context context, SharePlatformUtil spf, IUserCenterInterface iUser, int tabtype) {
		mContext = context;
		videogroupdata = null;
		praisgroupData = null;
		mUserCenterInterface = iUser;
		uca = (UserCenterActivity) mContext;
		loadRes();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);

		// 默认进入分享视频列表类别
		currentViewType = tabtype;
	}

	/**
	 * 更新数据链路
	 */
	public void setDataInfo(UCUserInfo user, ShareVideoGroup vdata, PraiseInfoGroup pdata) {
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
					convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_userinfo, null);
					holder = new UserViewHolder();

					holder.headImg = (ImageView) convertView.findViewById(R.id.user_head);
					holder.username = (TextView) convertView.findViewById(R.id.username);
					holder.description = (TextView) convertView.findViewById(R.id.description);
					holder.fxsp_num = (TextView) convertView.findViewById(R.id.fxsp_num);
					holder.fxsp_txt = (TextView) convertView.findViewById(R.id.fxsp_txt);
					holder.dz_num = (TextView) convertView.findViewById(R.id.dz_num);
					holder.dz_txt = (TextView) convertView.findViewById(R.id.dz_txt);
					holder.praise_select = (ImageView) convertView.findViewById(R.id.praise_select);
					holder.video_select = (ImageView) convertView.findViewById(R.id.video_select);
					holder.sharelayout = (LinearLayout) convertView.findViewById(R.id.sharelayout);
					holder.praiselayout = (LinearLayout) convertView.findViewById(R.id.praiselayout);
					holder.usercenterlyout = (LinearLayout) convertView.findViewById(R.id.user_center_lyout);
					holder.sharebtn = (ImageView) convertView.findViewById(R.id.title_share);
					holder.v = (ImageView) convertView.findViewById(R.id.v);
					holder.vText = (TextView) convertView.findViewById(R.id.vText);
					convertView.setTag(holder);
				} else {
					holder = (UserViewHolder) convertView.getTag();
				}

				if (uca.testUser()) {
					holder.dz_txt.setText("赞我的人");
					holder.headImg.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// 跳到个人中心编辑页面
							Intent it = new Intent(mContext, UserPersonalInfoActivity.class);
							mContext.startActivity(it);
						}
					});
					holder.usercenterlyout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent it = new Intent(mContext, UserPersonalInfoActivity.class);
							mContext.startActivity(it);
						}
					});
				} else {
					holder.dz_txt.setText("赞Ta的人");
				}

				String headUrl = userinfo.customavatar;
				if (null != headUrl && !"".equals(headUrl)) {
					GlideUtils.loadNetHead(mContext, holder.headImg, headUrl, R.drawable.editor_head_feault7);
				} else {
					showUserInfoHead(holder.headImg, userinfo.headportrait);
				}
				holder.username.setText(userinfo.nickname);
				holder.description.setText(userinfo.introduce);
				if (userinfo.sharevideonumber != null && !"".equals(userinfo.sharevideonumber)) {
					holder.fxsp_num.setText(GolukUtils.getFormatNumber(userinfo.sharevideonumber));
				} else {
					holder.fxsp_num.setText("0");
				}

				if (userinfo.praisemenumber != null && !"".equals(userinfo.praisemenumber)) {
					holder.dz_num.setText(GolukUtils.getFormatNumber(userinfo.praisemenumber));
				} else {
					holder.dz_num.setText("0");
				}

				if (userinfo.label != null) {
					if ("1".equals(userinfo.label.approvelabel)) {// 企业认证
						if (userinfo.label.approve != null && !"".equals(userinfo.label.approve)) {
							holder.vText.setVisibility(View.VISIBLE);
							holder.vText.setText(mContext.getResources().getString(R.string.str_add_v_txt) + "  "
									+ userinfo.label.approve);
						} else {
							holder.vText.setVisibility(View.GONE);
						}
						holder.v.setBackgroundResource(R.drawable.authentication_bluev_icon);
						holder.v.setVisibility(View.VISIBLE);
					} else {
						if ("1".equals(userinfo.label.headplusv)) {// 个人加V
							if (userinfo.label.headplusvdes != null && !"".equals(userinfo.label.headplusvdes)) {
								holder.vText.setVisibility(View.VISIBLE);
								holder.vText.setText(mContext.getResources().getString(R.string.str_add_v_txt) + "  "
										+ userinfo.label.headplusvdes);
							} else {
								holder.vText.setVisibility(View.GONE);
							}
							holder.v.setBackgroundResource(R.drawable.authentication_yellowv_icon);
							holder.v.setVisibility(View.VISIBLE);
						} else {
							holder.vText.setVisibility(View.GONE);
							if ("1".equals(userinfo.label.tarento)) {// 达人
								holder.v.setBackgroundResource(R.drawable.authentication_star_icon);
								holder.v.setVisibility(View.VISIBLE);
							} else {
								holder.v.setVisibility(View.GONE);
							}
						}
					}
				} else {
					holder.v.setVisibility(View.GONE);
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
						uca.showProgressDialog();
						boolean flog = GolukApplication.getInstance().getVideoSquareManager()
								.getUserCenterShareUrl(userinfo.uid);
						if (flog == false) {
							GolukUtils.showToast(mContext, "请求异常，请检查网络是否正常");
						}
					}
				});

				uca.updateTheEnd(false);
				if (currentViewType == ViewType_ShareVideoList) {
					if (videogroupdata.videolist != null && videogroupdata.videolist.size() > 0) {
						if (videogroupdata.isHaveData == false) {
							uca.updateTheEnd(true);
						}
					}

				} else if (currentViewType == ViewType_PraiseUserList) {
					if (praisgroupData.praiselist != null && praisgroupData.praiselist.size() > 0) {
						if (praisgroupData.isHaveData == false) {
							uca.updateTheEnd(true);
						}
					}
				}

				holder.sharelayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (ViewType_ShareVideoList != currentViewType) {
							currentViewType = ViewType_ShareVideoList;
							notifyDataSetChanged();
						}
					}
				});

				holder.praiselayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
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
			VideoSquareInfo clusterInfo = this.videogroupdata.videolist.get(index_v);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_sharevideo, null);
				holder.imageLayout = (ImageView) convertView.findViewById(R.id.imageLayout);
				holder.headimg = (ImageView) convertView.findViewById(R.id.headimg);
				holder.nikename = (TextView) convertView.findViewById(R.id.nikename);
				holder.time_location = (TextView) convertView.findViewById(R.id.time_location);

				// holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.function = (ImageView) convertView.findViewById(R.id.function);
				holder.videoGoldImg = (ImageView) convertView.findViewById(R.id.user_center_gold);
				// holder.recommentImg = (ImageView)
				// convertView.findViewById(R.id.uc_recommend);

				holder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
				holder.zanIcon = (ImageView) convertView.findViewById(R.id.zanIcon);
				// holder.zanText = (TextView)
				// convertView.findViewById(R.id.zanText);

				holder.commentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);
				holder.commentIcon = (ImageView) convertView.findViewById(R.id.commentIcon);
				holder.commentText = (TextView) convertView.findViewById(R.id.commentText);

				holder.shareLayout = (LinearLayout) convertView.findViewById(R.id.shareLayout);
				holder.shareIcon = (ImageView) convertView.findViewById(R.id.shareIcon);
				holder.shareText = (TextView) convertView.findViewById(R.id.shareText);

				holder.zText = (TextView) convertView.findViewById(R.id.zText);
				holder.v = (ImageView) convertView.findViewById(R.id.v);
				holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
				holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
				holder.totalcomments = (TextView) convertView.findViewById(R.id.totalcomments);

				holder.detail = (TextView) convertView.findViewById(R.id.detail);

				holder.totlaCommentLayout = (LinearLayout) convertView.findViewById(R.id.totlaCommentLayout);
				holder.comment1 = (TextView) convertView.findViewById(R.id.comment1);
				holder.comment2 = (TextView) convertView.findViewById(R.id.comment2);
				holder.comment3 = (TextView) convertView.findViewById(R.id.comment3);
				holder.isopen = (ImageView) convertView.findViewById(R.id.isopen);
				holder.tvPraiseCount = (TextView) convertView.findViewById(R.id.tv_share_video_list_item_praise_count);
				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
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
			holder.nikename.setText(clusterInfo.mUserEntity.nickname);
			final String sharingTime = GolukUtils.getCommentShowFormatTime(clusterInfo.mVideoEntity.sharingtime);
			final String location = clusterInfo.mVideoEntity.location;
			String showTimeLocation = sharingTime;
			if (null != location) {
				showTimeLocation = showTimeLocation + " " + location;
			}
			holder.time_location.setText(showTimeLocation);

			setVideoExtra(holder, clusterInfo);

			// 设置显示 视频位置信息
			// final String location = clusterInfo.mVideoEntity.location;
			// if (null == location || "".equals(location)) {
			// holder.location.setVisibility(View.GONE);
			// } else {
			// holder.location.setVisibility(View.VISIBLE);
			// holder.location.setText(location);
			// }

			// holder.zText.setText(clusterInfo.mVideoEntity.praisenumber);
			holder.weiguan.setText(clusterInfo.mVideoEntity.clicknumber);

			int count = Integer.parseInt(clusterInfo.mVideoEntity.comcount);
			holder.totalcomments.setText("查看所有" + clusterInfo.mVideoEntity.comcount + "条评论");
			if (count > 3) {
				holder.totalcomments.setVisibility(View.VISIBLE);
			} else {
				holder.totalcomments.setVisibility(View.GONE);
			}

			if (clusterInfo.mUserEntity != null && clusterInfo.mUserEntity.label != null) {
				if ("1".equals(clusterInfo.mUserEntity.label.approvelabel)) {// 企业认证
					holder.v.setImageResource(R.drawable.authentication_bluev_icon);
					holder.v.setVisibility(View.VISIBLE);
				} else {
					if ("1".equals(clusterInfo.mUserEntity.label.headplusv)) {// 个人加V
						holder.v.setImageResource(R.drawable.authentication_yellowv_icon);
						holder.v.setVisibility(View.VISIBLE);
					} else {
						if ("1".equals(clusterInfo.mUserEntity.label.tarento)) {// 达人
							holder.v.setImageResource(R.drawable.authentication_star_icon);
							holder.v.setVisibility(View.VISIBLE);
						} else {
							holder.v.setVisibility(View.GONE);
						}
					}
				}
			} else {
				holder.v.setVisibility(View.GONE);
			}

			// holder.zText.setText(clusterInfo.mVideoEntity.praisenumber +
			// " 赞");
			initListener(holder, index_v);
			// 没点过
			if ("0".equals(clusterInfo.mVideoEntity.ispraise)) {
				holder.zText.setTextColor(Color.rgb(136, 136, 136));
				holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like);
			} else {// 点赞过
				holder.zText.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
				holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like_press);
			}

			if ("-1".equals(clusterInfo.mVideoEntity.praisenumber)) {
				holder.tvPraiseCount.setText(mContext.getString(R.string.str_usercenter_praise));
			} else {
				holder.tvPraiseCount.setText(GolukUtils.getFormatNumber(clusterInfo.mVideoEntity.praisenumber)
						+ mContext.getString(R.string.str_usercenter_praise));
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
		case ItemType_PraiseInfo:
			int index_p = position - 1;
			final PraiseInfo prais = this.praisgroupData.praiselist.get(index_p);
			PraiseViewHolder praiseholder = null;
			int nwidth = (int) (GolukUtils.mDensity * 95);
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_praise, null);
				praiseholder = new PraiseViewHolder();

				praiseholder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
				praiseholder.headimg = (ImageView) convertView.findViewById(R.id.userhead);
				praiseholder.username = (TextView) convertView.findViewById(R.id.username);
				praiseholder.desc = (TextView) convertView.findViewById(R.id.desc);
				praiseholder.videoPicLayout = (ImageView) convertView.findViewById(R.id.videopic);
				praiseholder.userinfo = (LinearLayout) convertView.findViewById(R.id.userinfo);
				praiseholder.v = (ImageView) convertView.findViewById(R.id.v);

				int nheight = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(nwidth, nheight);
				mPlayerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				mPlayerLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				mPlayerLayoutParams.rightMargin = (int) (GolukUtils.mDensity * 5);
				mPlayerLayoutParams.topMargin = (int) (GolukUtils.mDensity * 5);
				mPlayerLayoutParams.bottomMargin = (int) (GolukUtils.mDensity * 5);
				praiseholder.videoPicLayout.setLayoutParams(mPlayerLayoutParams);

				convertView.setTag(praiseholder);
			} else {
				praiseholder = (PraiseViewHolder) convertView.getTag();
			}

			loadImage(praiseholder.videoPicLayout, prais.picture, nwidth);

			String netHeadUrl = prais.customavatar;
			if (null != netHeadUrl && !"".equals(netHeadUrl)) {
				// 使用服务器头像地址
				GlideUtils.loadNetHead(mContext, praiseholder.headimg, netHeadUrl, R.drawable.editor_head_feault7);
			} else {
				showHead(praiseholder.headimg, prais.headportrait);
			}

			if (prais.label != null) {
				if ("1".equals(prais.label.approvelabel)) {// 企业认证
					praiseholder.v.setBackgroundResource(R.drawable.authentication_bluev_icon);
					praiseholder.v.setVisibility(View.VISIBLE);
				} else {
					if ("1".equals(prais.label.headplusv)) {// 个人加V
						praiseholder.v.setBackgroundResource(R.drawable.authentication_yellowv_icon);
						praiseholder.v.setVisibility(View.VISIBLE);
					} else {
						if ("1".equals(prais.label.tarento)) {// 达人
							praiseholder.v.setBackgroundResource(R.drawable.authentication_star_icon);
							praiseholder.v.setVisibility(View.VISIBLE);
						} else {
							praiseholder.v.setVisibility(View.GONE);
						}
					}
				}
			} else {
				praiseholder.v.setVisibility(View.GONE);
			}

			praiseholder.username.setText(prais.nickname);
			praiseholder.desc.setText(prais.introduce);
			praiseholder.userinfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(mContext, VideoDetailActivity.class);
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
			praiseholder.videoPicLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(mContext, VideoDetailActivity.class);
					i.putExtra("videoid", prais.videoid);
					mContext.startActivity(i);
				}

			});
			break;
		case ItemType_noDataInfo:
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
					lp.height = mUserCenterInterface.OnGetListViewHeight() - this.firstItemHeight;
					rl.setLayoutParams(lp);
				}
			}

			if (this.currentViewType == ViewType_ShareVideoList) {
				// 分享视频列表
				if (this.videogroupdata.loadfailed == true) {
					noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_qitadifang);
					mBneedRefrush = true;
				} else {
					if (uca.testUser()) {
						noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_novideo);
						mBneedRefrush = false;
					} else {
						noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_tavideo);
						mBneedRefrush = false;
					}

				}
			} else {
				// 被点赞人信息列表
				if (this.praisgroupData.loadfailed == true) {
					noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_qitadifang);
					mBneedRefrush = true;
				} else {

					if (uca.testUser()) {
						noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_nolike);
						mBneedRefrush = false;
					} else {
						noVideoDataViewHolder.tipsimage.setBackgroundResource(R.drawable.mine_talike);
						mBneedRefrush = false;
					}

				}
			}

			noVideoDataViewHolder.tipsimage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mBneedRefrush == true) {
						if (mUserCenterInterface != null) {
							uca.httpPost(userinfo.uid);
							uca.showProgressDialog();
						}
					}
				}

			});
			break;

		default:
			break;
		}
		return convertView;
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
		if (null == clusterInfo.mVideoEntity) {
			return;
		}
		String got = "";
		if (null != clusterInfo.mVideoEntity.videoExtra) {
			// 显示是否获奖
			if ("1".equals(clusterInfo.mVideoEntity.videoExtra.isreward)
					&& "1".equals(clusterInfo.mVideoEntity.videoExtra.sysflag)) {
				holder.videoGoldImg.setVisibility(View.VISIBLE);
			} else {
				holder.videoGoldImg.setVisibility(View.GONE);
			}
			// 显示是否推荐
			if (clusterInfo.mVideoEntity.videoExtra.isrecommend.equals("1")) {
				// holder.recommentImg.setVisibility(View.VISIBLE);
				holder.time_location.setCompoundDrawables(null, null, this.mRecommentDrawable, null);
			} else {
				holder.time_location.setCompoundDrawables(null, null, null, null);
			}

			// 获得聚合字符串
			if (clusterInfo.mVideoEntity.videoExtra.topicname == null
					|| "".equals(clusterInfo.mVideoEntity.videoExtra.topicname)) {
				got = "";
			} else {
				got = "#" + clusterInfo.mVideoEntity.videoExtra.topicname + "#";
			}

		} else {
			holder.videoGoldImg.setVisibility(View.GONE);
			// holder.recommentImg.setVisibility(View.GONE);
			holder.time_location.setCompoundDrawables(null, null, null, null);
		}

		UserUtils.showCommentText(mContext, true, clusterInfo, holder.detail, clusterInfo.mUserEntity.nickname,
				clusterInfo.mVideoEntity.describe, got);
	}

	Drawable mRecommentDrawable = null;

	private void loadRes() {
		mRecommentDrawable = mContext.getResources().getDrawable(R.drawable.together_recommend_icon);
		mRecommentDrawable.setBounds(0, 0, mRecommentDrawable.getMinimumWidth(), mRecommentDrawable.getMinimumHeight());
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
		VideoSquareInfo mVideoSquareInfo = this.videogroupdata.videolist.get(index);

		// 分享监听
		ClickShareListener tempShareListener = new ClickShareListener(mContext, mVideoSquareInfo,
				(UserCenterActivity) mContext);
		holder.shareLayout.setOnClickListener(tempShareListener);
		// 举报监听

		holder.function.setOnClickListener(new ClickFunctionListener(mContext, mVideoSquareInfo,
				isMy(mVideoSquareInfo.mUserEntity.uid), (UserCenterActivity) mContext)
				.setConfirm(!isMy(mVideoSquareInfo.mUserEntity.uid)));
		// 评论监听
		holder.commentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, true));
		// 播放区域监听
		holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext, mVideoSquareInfo, null));
		// 点赞
		holder.praiseLayout.setOnClickListener(new ClickPraiseListener(mContext, mVideoSquareInfo,
				(UserCenterActivity) mContext));
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

	private void loadImage(ImageView layout, String url, int nWidth) {
		GlideUtils.loadImage(mContext, layout, url, R.drawable.tacitly_pic);
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
		ImageView v;

		TextView vText;

		ImageView sharebtn;
	}

	public static class PraiseViewHolder {
		LinearLayout praiseLayout;
		ImageView headimg;
		TextView username;
		TextView desc;
		LinearLayout userinfo;
		ImageView videoPicLayout;
		ImageView v;
	}

	public static class NoVideoDataViewHolder {
		TextView tips;
		ImageView tipsimage;
		boolean bMeasureHeight;
	}

	public static class ViewHolder {
		String VideoID;
		ImageView imageLayout;
		ImageView headimg;
		TextView nikename;
		TextView time_location;
		ImageView function;
		ImageView videoGoldImg;
		// ImageView recommentImg;

		LinearLayout praiseLayout;
		ImageView zanIcon;
		// TextView zanText;

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
		ImageView v;
		TextView tvPraiseCount;

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

	public void dealData(String vid) {
		if (null == vid || null == videogroupdata) {
			return;
		}
		int size = videogroupdata.videolist.size();
		for (int i = 0; i < size; i++) {
			if (videogroupdata.videolist.get(i).mVideoEntity.videoid.equals(vid)) {
				videogroupdata.videolist.remove(i);
				this.notifyDataSetChanged();
				break;
			}
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {

	}
}
