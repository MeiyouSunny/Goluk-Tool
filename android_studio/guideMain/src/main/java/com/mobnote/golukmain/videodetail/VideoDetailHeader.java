package com.mobnote.golukmain.videodetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.player.FullScreenVideoView;
import com.mobnote.golukmain.player.factory.GolukPlayer;
import com.mobnote.golukmain.startshare.GpsInfo;
import com.mobnote.t1sp.map.MapTrackView;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.view.FlowLayout;

import net.sf.marineapi.bean.GPSData;

import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;

public class VideoDetailHeader implements OnClickListener, GolukPlayer.OnPreparedListener, GolukPlayer.OnCompletionListener, GolukPlayer.OnErrorListener, GolukPlayer.OnInfoListener, SeekBar.OnSeekBarChangeListener {
    private Context mContext;
    private RelativeLayout mUserCenterLayout = null;
    private ImageView mImageHead = null;
    private TextView mTextName = null;
    private TextView mTextTime = null;
    private TextView mTextLook = null;
    private FullScreenVideoView mVideoView = null;
    private RelativeLayout mImageLayout = null;
    private ImageView simpleDraweeView = null;
    private ImageView nHeadAuthentication = null;

    private ImageView mPlayBtn = null;
    private SeekBar mSeekBar = null;
    private LinearLayout mVideoLoading = null;
    private ProgressBar mLoading = null;
    private RelativeLayout mPlayerLayout = null;

    private TextView mTextDescribe = null;
    private TextView mTextAuthor, mTextLink;
    private RelativeLayout mLayoutAuthorAndLink;
    private LinearLayout mPraiseLayout, mShareLayout, mCommentLayout;
    private TextView mTextZan, mTextComment, mTextZanName, mTvVideoType;
    private ImageView mZanImage;
    private FlowLayout mTagsFL;

    // 奖励视频／推荐视频
    private ImageView mImageHeadAward, mActiveImage, mSysImage, mRecomImage;
    private TextView mTextLine1, mTextLine2, mActiveCount, mSysCount, mActiveReason, mSysReason, mRecomReason;
    //	private LinearLayout mReasonLayout;
    private RelativeLayout mActiveLayout, mSysLayout, mRecomLayout;
    public ConnectivityManager mConnectivityManager = null;
    public NetworkInfo mNetInfo = null;
    /**
     * 加载中动画对象
     */
    //private AnimationDrawable mAnimationDrawable = null;
    public boolean isShow = false;
    //	/** 缓冲标识 */
    public boolean isBuffering = false;
    /**
     * 播放器报错标识
     */
    public boolean error = false;
    /**
     * 判断是精选(0)还是最新(1)
     **/
    private int mType = 0;

    private VideoDetailRetBean mVideoDetailRetBean = null;

    private CustomDialog mCustomDialog;
    private int mVideoPosition = 0;

    // 速度
    private RelativeLayout mLayoutSpeed;
    private TextView mTvSpeed;
    // 地图轨迹
    private LinearLayout mLayoutMap;
    private MapTrackView mMapTrackView;
    private List<GPSData> mGpsList;

    private final Handler mHandler = new Handler();

    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                mPlayBtn.setVisibility(View.GONE);
                mImageLayout.setVisibility(View.GONE);
                hideLoading();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                int duration = mVideoView.getDuration();
                int progress = mVideoView.getCurrentPosition() * 100 / duration;
                mSeekBar.setProgress(progress);
            }
            mHandler.postDelayed(mProgressChecker, 500);
        }
    };

    public VideoDetailHeader(Context context, int type) {
        mContext = context;
        mType = type;
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetInfo = mConnectivityManager.getActiveNetworkInfo();
        error = false;
    }

    public View createHeadView() {
        View convertView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.video_detail_head, null);
        mUserCenterLayout = (RelativeLayout) convertView.findViewById(R.id.rl_video_usercenter);
        mImageHead = (ImageView) convertView.findViewById(R.id.user_head);
        nHeadAuthentication = (ImageView) convertView.findViewById(R.id.im_listview_item_head_authentication);
        mTextName = (TextView) convertView.findViewById(R.id.user_name);
        mTextTime = (TextView) convertView.findViewById(R.id.tv_user_time_location);
        mTextLook = (TextView) convertView.findViewById(R.id.video_detail_count_look);

        mVideoView = (FullScreenVideoView) convertView.findViewById(R.id.video_detail_videoview);
        mImageLayout = (RelativeLayout) convertView.findViewById(R.id.mImageLayout);
        mPlayBtn = (ImageView) convertView.findViewById(R.id.play_btn);
        mSeekBar = (SeekBar) convertView.findViewById(R.id.seekbar);
        mVideoLoading = (LinearLayout) convertView.findViewById(R.id.mLoadingLayout);
        mLoading = (ProgressBar) convertView.findViewById(R.id.mLoading);
        mPlayerLayout = (RelativeLayout) convertView.findViewById(R.id.mPlayerLayout);
        simpleDraweeView = (ImageView) convertView.findViewById(R.id.video_detail_first_pic);

        mTextDescribe = (TextView) convertView.findViewById(R.id.video_detail_describe);
        mTvVideoType = (TextView) convertView.findViewById(R.id.video_type);
        mTextAuthor = (TextView) convertView.findViewById(R.id.video_detail_author);
        mTextLink = (TextView) convertView.findViewById(R.id.video_detail_link);
        mLayoutAuthorAndLink = (RelativeLayout) convertView.findViewById(R.id.layout_author_link);
        mPraiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
        mShareLayout = (LinearLayout) convertView.findViewById(R.id.shareLayout);
        mCommentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);
        mTextZan = (TextView) convertView.findViewById(R.id.zanText);
        mTextComment = (TextView) convertView.findViewById(R.id.commentText);
        mZanImage = (ImageView) convertView.findViewById(R.id.video_square_detail_like_image);
        mTextZanName = (TextView) convertView.findViewById(R.id.zanName);

        mImageHeadAward = (ImageView) convertView.findViewById(R.id.video_detail_head_award_image);
        mActiveImage = (ImageView) convertView.findViewById(R.id.active_image);
        mSysImage = (ImageView) convertView.findViewById(R.id.sys_image);
        mRecomImage = (ImageView) convertView.findViewById(R.id.recom_image);
        mTextLine1 = (TextView) convertView.findViewById(R.id.video_detail_line1);
        mTextLine2 = (TextView) convertView.findViewById(R.id.video_detail_line2);
        mActiveCount = (TextView) convertView.findViewById(R.id.active_count);
        mSysCount = (TextView) convertView.findViewById(R.id.sys_count);
        mActiveReason = (TextView) convertView.findViewById(R.id.active_reason);
        mSysReason = (TextView) convertView.findViewById(R.id.sys_reason);
        mRecomReason = (TextView) convertView.findViewById(R.id.recom_reason);
//		mReasonLayout = (LinearLayout) convertView.findViewById(R.id.video_detail_reason_layout);
        mActiveLayout = (RelativeLayout) convertView.findViewById(R.id.video_detail_activie_layout);
        mSysLayout = (RelativeLayout) convertView.findViewById(R.id.video_detail_sys_layout);
        mRecomLayout = (RelativeLayout) convertView.findViewById(R.id.video_detail_recom_layout);
        mTagsFL = (FlowLayout) convertView.findViewById(R.id.flowlayout_video_detail_header_tags);
        //mLoading.setBackgroundResource(R.anim.video_loading);
        //mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();

        // 速度
        mLayoutSpeed = (RelativeLayout) convertView.findViewById(R.id.layout_speed);
        mTvSpeed = (TextView) convertView.findViewById(R.id.tv_speed);
        // 轨迹
        mLayoutMap = (LinearLayout) convertView.findViewById(R.id.layout_map);

        mPlayBtn.setOnClickListener(this);
        mPlayerLayout.setOnClickListener(this);

        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnInfoListener(this);

        mShareLayout.setOnClickListener(this);
        mTextLink.setOnClickListener(this);
        mPraiseLayout.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        return convertView;
    }

    public void setData(VideoDetailRetBean videoDetailRetBeanData) {
        mVideoDetailRetBean = videoDetailRetBeanData;

        // 视频轨迹
        boolean hasGpsTrack = mVideoDetailRetBean != null && !CollectionUtils.isEmpty(mVideoDetailRetBean.data.avideo.locations);
        mLayoutMap.setVisibility(hasGpsTrack ? View.VISIBLE : View.GONE);
        mMapTrackView = MapTrackView.create(mContext);
        mMapTrackView.setDrawStartAndEndIcon(false);
        mLayoutMap.addView(mMapTrackView);
        mGpsList = parseGpsTrackData(mVideoDetailRetBean.data.avideo.locations);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapTrackView.drawTrackLine(mGpsList);
            }
        }, 1500);
    }

    private List<GPSData> parseGpsTrackData(List<GpsInfo> locations) {
        if (CollectionUtils.isEmpty(locations))
            return null;

        List<GPSData> datas = new ArrayList<>();
        GPSData gpsData = null;
        for (GpsInfo gpsInfo : locations) {
            gpsData = new GPSData();
            gpsData.coordType = GPSData.COORD_TYPE_GPS;
            gpsData.latitude = gpsInfo.lat;
            gpsData.longitude = gpsInfo.lon;
            gpsData.angle = (int) gpsInfo.direction;
            gpsData.speed = gpsInfo.speed;
            gpsData.altitude = gpsInfo.altitude;

            datas.add(gpsData);
        }

        return datas;
    }

    public void getHeadData(boolean isStartPlay) {
        final VideoDetailDataBean videoDetailDataBean = mVideoDetailRetBean.data;
        String netUrlHead = videoDetailDataBean.avideo.user.customavatar;
        if (null != netUrlHead && !"".equals(netUrlHead)) {
            // 使用网络地址
            GlideUtils.loadNetHead(mContext, mImageHead, netUrlHead, R.drawable.my_head_moren7);
        } else {
            UserUtils.focusHead(mContext, videoDetailDataBean.avideo.user.headportrait, mImageHead);
        }
        if (null != videoDetailDataBean && null != videoDetailDataBean.avideo && null != videoDetailDataBean.avideo.user
                && null != videoDetailDataBean.avideo.user.label) {
            String approvelabel = videoDetailDataBean.avideo.user.label.approvelabel;
            String headplusv = videoDetailDataBean.avideo.user.label.headplusv;
            String tarento = videoDetailDataBean.avideo.user.label.tarento;
            nHeadAuthentication.setVisibility(View.VISIBLE);
            if ("1".equals(approvelabel)) {
                nHeadAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
            } else if ("1".equals(headplusv)) {
                nHeadAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
            } else if ("1".equals(tarento)) {
                nHeadAuthentication.setImageResource(R.drawable.authentication_star_icon);
            } else {
                nHeadAuthentication.setVisibility(View.GONE);
            }
        } else {
            nHeadAuthentication.setVisibility(View.GONE);
        }

        mTextName.setText(videoDetailDataBean.avideo.user.nickname);
        mTextTime.setText(GolukUtils.getCommentShowFormatTime(mContext, videoDetailDataBean.avideo.video.sharingtime));
        // 点赞数、评论数、观看数
        mTextLook.setText(GolukUtils.getFormatNumber(videoDetailDataBean.avideo.video.clicknumber)
                + mContext.getString(R.string.cluster_weiguan));
        if (!"0".equals(videoDetailDataBean.avideo.video.praisenumber)) {
            mTextZan.setText(GolukUtils.getFormatNumber(videoDetailDataBean.avideo.video.praisenumber));
            mTextZan.setTextColor(Color.rgb(136, 136, 136));
        }

        if ("0".equals(videoDetailDataBean.avideo.video.ispraise)) {
            mZanImage.setImageResource(R.drawable.videodetail_like);
            mTextZanName.setTextColor(Color.rgb(136, 136, 136));
            mTextZan.setTextColor(Color.rgb(136, 136, 136));
        } else {
            mZanImage.setImageResource(R.drawable.videodetail_like_press);
            mTextZanName.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
            mTextZan.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
        }
        mTextComment.setText(GolukUtils.getFormatNumber(videoDetailDataBean.avideo.video.comment.comcount));
        if (VideoInfo.VIDEO_TYPE_LIVE.equals(videoDetailDataBean.avideo.video.type)) {
            mTvVideoType.setVisibility(View.VISIBLE);
        }

        // TODO 在视频描述之后添加活动标签
        if (null != videoDetailDataBean.avideo.video) {
            mTextDescribe.setText(videoDetailDataBean.avideo.video.describe);
        }

        if (null != videoDetailDataBean.avideo.video && null != videoDetailDataBean.avideo.video.tags) {
            GolukUtils.addTagsViews(mContext, videoDetailDataBean.avideo.video.tags, mTagsFL);
        } else {
            mTagsFL.setVisibility(View.GONE);
        }
        final String location = videoDetailDataBean.avideo.video.location;
        if (null != location && !"".equals(location)) {
            mTextTime.append("  " + location);
        } else {
            mTextTime.append("  " + location);
        }

        if (0 == mType) {
            mTextAuthor.setVisibility(View.VISIBLE);
            mTextAuthor.setText(mContext.getString(R.string.str_thank_to_author) + "  "
                    + videoDetailDataBean.avideo.user.nickname);
        } else {
            mTextAuthor.setVisibility(View.GONE);
        }
        GlideUtils.loadImage(mContext, simpleDraweeView, videoDetailDataBean.avideo.video.picture, R.drawable.tacitly_pic);

        // 外链接
        if (null != videoDetailDataBean.link) {
            if ("0".equals(videoDetailDataBean.link.showurl)) {
                mTextLink.setVisibility(View.GONE);
            } else {
                mTextLink.setVisibility(View.VISIBLE);
                mTextLink.setText(videoDetailDataBean.link.outurlname);
            }
        }
        boolean authorAndLinkVisible = (mTextAuthor.getVisibility() == View.VISIBLE || mTextLink.getVisibility() == View.VISIBLE);
        if (!authorAndLinkVisible)
            mLayoutAuthorAndLink.setVisibility(View.GONE);

        // 视频
        if ((mNetInfo != null) && (mNetInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
            if (!mVideoView.isPlaying() && isStartPlay) {
                GolukDebugUtils.e("videoview", "VideoDetailActivity===getHeadData=  stat Play:");
                playVideo();
                GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------getHeadData:  showLoading");
            }

        } else {
            if (!mVideoView.isPlaying() && !isShow && isStartPlay) {
                mImageLayout.setVisibility(View.VISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);
            }
        }

        // TODO　没有活动奖励视频没有奖励信息这个模块
        // 头部获奖视频icon显示
        // 获奖／推荐
        if (null != videoDetailDataBean.avideo.video.recom) {
            if ("1".equals(videoDetailDataBean.avideo.video.recom.isreward) && "1".equals(videoDetailDataBean.avideo.video.recom.sysflag)) {
                mImageHeadAward.setVisibility(View.VISIBLE);
            } else {
                mImageHeadAward.setVisibility(View.GONE);
            }

            if (!"1".equals(videoDetailDataBean.avideo.video.recom.atflag)
                    && !"1".equals(videoDetailDataBean.avideo.video.recom.sysflag)
                    && !"1".equals(videoDetailDataBean.avideo.video.recom.isrecommend)) {
                mTextLine1.setVisibility(View.GONE);
                mTextLine2.setVisibility(View.GONE);
            } else {
                mTextLine1.setVisibility(View.VISIBLE);
                mTextLine2.setVisibility(View.VISIBLE);
            }

            if ("1".equals(videoDetailDataBean.avideo.video.recom.atflag)) {
                mActiveLayout.setVisibility(View.VISIBLE);
                if ("".equals(videoDetailDataBean.avideo.video.recom.atreason)) {
                    mActiveReason.setText(mContext.getString(R.string.str_atreason_default));
                } else {
                    mActiveReason.setText(mContext.getString(R.string.msg_system_reason_began)
                            + videoDetailDataBean.avideo.video.recom.atreason);
                }
                mActiveCount.setText("+" + UserUtils.formatNumber(videoDetailDataBean.avideo.video.recom.atgold)
                        + mContext.getString(R.string.str_profit_detail_unit));
            } else {
                mActiveLayout.setVisibility(View.GONE);
            }

            if ("1".equals(videoDetailDataBean.avideo.video.recom.sysflag)) {
                mSysLayout.setVisibility(View.VISIBLE);
                if ("".equals(videoDetailDataBean.avideo.video.recom.sysreason)) {
                    mSysReason.setText(mContext.getString(R.string.str_atreason_default));
                } else {
                    mSysReason.setText(mContext.getString(R.string.msg_system_reason_began)
                            + videoDetailDataBean.avideo.video.recom.sysreason);
                }
                mSysCount.setText("+" + UserUtils.formatNumber(videoDetailDataBean.avideo.video.recom.sysgold)
                        + mContext.getString(R.string.str_profit_detail_unit));
            } else {
                mSysLayout.setVisibility(View.GONE);
            }

            if ("1".equals(videoDetailDataBean.avideo.video.recom.isrecommend)) {
                mRecomLayout.setVisibility(View.VISIBLE);
                if ("".equals(videoDetailDataBean.avideo.video.recom.reason)) {
                    mRecomReason.setText(mContext.getString(R.string.str_atreason_default));
                } else {
                    mRecomReason.setText(mContext.getString(R.string.msg_system_reason_began)
                            + videoDetailDataBean.avideo.video.recom.reason);
                }
            } else {
                mRecomLayout.setVisibility(View.GONE);
            }
        } else {
            mImageHeadAward.setVisibility(View.GONE);
            mTextLine1.setVisibility(View.GONE);
            mTextLine2.setVisibility(View.GONE);
            mActiveLayout.setVisibility(View.GONE);
            mSysLayout.setVisibility(View.GONE);
            mRecomLayout.setVisibility(View.GONE);
        }

        mUserCenterLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                VideoUserInfo videoUser = videoDetailDataBean.avideo.user;
                GolukUtils.startUserCenterActivity(mContext, videoUser.uid);
            }
        });
    }

    public void setCommentCount(String count) {
        mTextComment.setText(GolukUtils.getFormatNumber(count));
    }

    private boolean isCallVideo = false;

    public void playVideo() {
        if (isCallVideo) {
            return;
        }
        isCallVideo = true;
        Uri uri = null;
        uri = Uri.parse(mVideoDetailRetBean.data.avideo.video.ondemandwebaddress);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
        showLoading();
        mHandler.post(mProgressChecker);
        mHandler.post(mPlayingChecker);
    }

    /**
     * 显示视频描述和活动名称
     *
     * @param view
     * @param describe
     * @param text
     */
    private void showTopicText(TextView view, String describe, String text) {
        String reply_str = describe + text;
        SpannableString style = new SpannableString(reply_str);
        ClickableSpan clickttt = new TopicClickableSpan(mContext, text, mVideoDetailRetBean);
        style.setSpan(clickttt, describe.length(), reply_str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(style);
        view.setMovementMethod(LinkMovementMethod.getInstance());

    }

    /**
     * 显示加载中布局
     */
    public void showLoading() {
        GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------showLoading()  isShow===" + isShow);
        if (!UserUtils.isNetDeviceAvailable(mContext) && !mVideoView.isPlaying()) {
            return;
        }
        if (!isShow) {
            isShow = true;
            mVideoLoading.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.GONE);
//			mLoading.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					if (mAnimationDrawable != null) {
//						if (!mAnimationDrawable.isRunning()) {
//							mAnimationDrawable.start();
//						}
//					}
//				}
//			}, 100);
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
            mImageLayout.setVisibility(View.GONE);
//			if (mAnimationDrawable != null) {
//				if (mAnimationDrawable.isRunning()) {
//					mAnimationDrawable.stop();
//				}
//			}
            mVideoLoading.setVisibility(View.GONE);
        }
    }

    /**
     * 提示对话框
     *
     * @param msg 提示信息
     */
    public void dialog(String msg) {
        if (null == mCustomDialog) {
            mCustomDialog = new CustomDialog(mContext);
            mCustomDialog.setCancelable(false);
            mCustomDialog.setMessage(msg, Gravity.CENTER);
            mCustomDialog.setLeftButton(mContext.getString(R.string.user_repwd_ok), new OnLeftClickListener() {
                @Override
                public void onClickListener() {
                    mImageLayout.setVisibility(View.VISIBLE);
                    mPlayBtn.setVisibility(View.VISIBLE);
                    mSeekBar.setProgress(0);
                }
            });

            if (!((VideoDetailActivity) mContext).isFinishing()) {
                mCustomDialog.show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        int id = v.getId();
        if (id == R.id.play_btn
                || id == R.id.mPlayerLayout) {
            if (!UserUtils.isNetDeviceAvailable(mContext)) {
                GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
                return;
            }
            if (isBuffering) {
                return;
            }
            if (mIsRePlay) {
                mVideoView.seekTo(0);
                mVideoView.start();
                mHandler.post(mProgressChecker);
                mHandler.post(mPlayingChecker);
                mIsRePlay = false;
                return;
            }
            if (mVideoView.isPlaying() && mVideoView.canPause()) {
                mVideoView.pause();
                mPlayBtn.setVisibility(View.VISIBLE);
            } else {
                if (isCallVideo) {
                    mVideoView.start();
                    mImageLayout.setVisibility(View.GONE);
                } else {
                    playVideo();
                }
                GolukDebugUtils.e("", "VideoDetailActivity-------------------------onClick  showLoading");
                mPlayBtn.setVisibility(View.GONE);
            }
        } else if (id == R.id.shareLayout) {
            ((VideoDetailActivity) mContext).getShare();
        } else if (id == R.id.video_detail_link) {
            if (!UserUtils.isNetDeviceAvailable(mContext)) {
                GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
            } else {
                if ((mVideoDetailRetBean.data != null) && (mVideoDetailRetBean.data.link != null) && "1".equals(mVideoDetailRetBean.data.link.showurl)) {
                    Intent mLinkIntent = new Intent(mContext, UserOpenUrlActivity.class);
                    mLinkIntent.putExtra("url", mVideoDetailRetBean.data.link.outurl);
                    mContext.startActivity(mLinkIntent);
                }
            }
        } else if (id == R.id.praiseLayout) {
            if (!UserUtils.isNetDeviceAvailable(mContext)) {
                GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
            } else {
                String praise = setClickPraise();
                if ("0".equals(praise)) {
                    mTextZan.setText(praise);
                    mTextZan.setVisibility(View.GONE);
                    mZanImage.setImageResource(R.drawable.videodetail_like);
                    mTextZan.setTextColor(Color.rgb(136, 136, 136));
                    mTextZanName.setTextColor(Color.rgb(136, 136, 136));
                } else {
                    mTextZan.setVisibility(View.VISIBLE);
                    mTextZan.setText(praise);
                    if (mVideoDetailRetBean.data.avideo.video.ispraise.equals("1")) {
                        mZanImage.setImageResource(R.drawable.videodetail_like_press);
                        mTextZan.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
                        mTextZanName.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
                    } else {
                        mZanImage.setImageResource(R.drawable.videodetail_like);
                        mTextZan.setTextColor(Color.rgb(136, 136, 136));
                        mTextZanName.setTextColor(Color.rgb(136, 136, 136));
                    }

                }

            }
        } else {
            Log.e("", "VideoDetailHeader click id = " + v.getId());
        }

    }

    public String setClickPraise() {
        int likeNumber = 0;
        if ("0".equals(mVideoDetailRetBean.data.avideo.video.ispraise)) {
            if (mVideoDetailRetBean.data.avideo.video.praisenumber.replace(",", "").equals("")) {
                likeNumber = 1;
            } else {
                try {
                    likeNumber = Integer.parseInt(mVideoDetailRetBean.data.avideo.video.praisenumber.replace(",", "")) + 1;
                } catch (Exception e) {
                    likeNumber = 1;
                    e.printStackTrace();
                }
            }
            mVideoDetailRetBean.data.avideo.video.ispraise = "1";
            boolean b = ((VideoDetailActivity) mContext).sendPraiseRequest();
        } else {
            try {
                likeNumber = Integer.parseInt(mVideoDetailRetBean.data.avideo.video.praisenumber.replace(",", "")) - 1;
            } catch (Exception e) {
                likeNumber = 0;
                e.printStackTrace();
            }
            mVideoDetailRetBean.data.avideo.video.ispraise = "0";
            boolean b = ((VideoDetailActivity) mContext).sendCancelPraiseRequest();
        }
        mVideoDetailRetBean.data.avideo.video.praisenumber = likeNumber + "";
        return GolukUtils.getFormatNumber(likeNumber + "");
    }

    private boolean mResume = false;

    public void pausePlayer() {
        if (!isCallVideo) {
            return;
        }
        mResume = true;
        mHandler.removeCallbacksAndMessages(null);
        mVideoPosition = mVideoView.getCurrentPosition();
        mImageLayout.setVisibility(View.VISIBLE);
        mVideoView.suspend();
    }

    public void startPlayer() {
        if (!isCallVideo) {
            return;
        }
        if (mResume) {
            mVideoView.seekTo(mVideoPosition);
            mVideoView.resume();
            mHandler.post(mProgressChecker);
            mHandler.post(mPlayingChecker);
//			showLoading();
        }
    }

    /**
     * DP
     */
    public final int mPlayerHeight = 205;
    /**
     * 布局title 的高度 dp
     */
    public final int mTitleHeight = 46;
    /**
     * 是否是用户拖出屏幕暂停的，拖回来根据此变变量恢复
     */
    private boolean isOuterPause = false;

    public void scrollDealPlayer() {
        if (null == mVideoView) {
            return;
        }
        final int[] locations = new int[2];
        mVideoView.getLocationOnScreen(locations);
        // 计算播放布局的所占的像素高度
        final int playHeightPx = (int) (mPlayerHeight * GolukUtils.mDensity);
        // 计算布局title所占的高度
        final int titleHeightPx = (int) (mTitleHeight * GolukUtils.mDensity);
        final int duration = -(playHeightPx - titleHeightPx - VideoDetailActivity.stateBraHeight);

        if (locations[1] < duration) {
            GolukDebugUtils.e("", "onScreen--------------pause");
            // 滑出屏幕外了
            if (mVideoView.isPlaying() && mVideoView.canPause()) {
                isOuterPause = true;
                mVideoView.pause();
                mPlayBtn.setVisibility(View.VISIBLE);
            }
        } else {
            int dd = (titleHeightPx + VideoDetailActivity.stateBraHeight);
            if (locations[1] > dd) {
                // 开始播放
                if (!mVideoView.isPlaying() && isOuterPause) {
                    GolukDebugUtils.e("", "onScreen--------------start");
                    mVideoView.start();
                    mPlayBtn.setVisibility(View.GONE);
                    GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------startPlayer:  showLoading");
                }
                isOuterPause = false;
            }
        }
    }

    @Override
    public void onPrepared(GolukPlayer mp) {
        // TODO Auto-generated method stub
        mVideoView.setVideoWidth(mp.getVideoWidth());
        mVideoView.setVideoHeight(mp.getVideoHeight());
//		if ((null != mNetInfo) && (mNetInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
//			mp.setLooping(true);
//		}
    }

    private boolean mIsRePlay = false;

    @Override
    public void onCompletion(GolukPlayer mp) {
        // TODO Auto-generated method stub
        // TODO OnCompletionListener视频播放完后进度条回到初始位置
        GolukDebugUtils.e("videostate", "VideoDetailActivity-------------------------onCompletion :  ");
        if (error || null == mVideoView) {
            return;
        }

        mSeekBar.setProgress(0);
        if ((null != mNetInfo) && (mNetInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
//			mp.setLooping(false);
            mHandler.removeCallbacksAndMessages(null);
            mIsRePlay = true;
            mPlayBtn.setVisibility(View.VISIBLE);
//			mImageLayout.setVisibility(View.VISIBLE);
        } else {
            try {
                mVideoView.seekTo(0);
                mVideoView.start();
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
            }
        }
    }

    @Override
    public boolean onError(GolukPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        // TODO onErrorListener
        GolukDebugUtils.e("videostate", "VideoDetailActivity-------------------------onError :  ");
        if (error) {
            return true;
        }
        String msg = mContext.getString(R.string.str_play_error);
        switch (what) {
            case 1:
            case -1010:
                msg = mContext.getString(R.string.str_play_video_error);
                break;
            case -110:
                msg = mContext.getString(R.string.str_play_video_network_error);
                break;

            default:
                break;
        }
        if (!UserUtils.isNetDeviceAvailable(mContext)) {
            msg = mContext.getString(R.string.str_play_video_network_error);
        }
        error = true;

        hideLoading();
        GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------onError : hideLoading ");
        dialog(msg);
        return true;
    }

    @Override
    public boolean onInfo(GolukPlayer arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        GolukDebugUtils.e("videostate", "VideoDetailActivity-----------FullVideoView--------------onInfo : arg1 " + arg1);
        switch (arg1) {
            case 3:
//			error = false;
//			mPlayBtn.setVisibility(View.GONE);
//			mImageLayout.setVisibility(View.GONE);
//			hideLoading();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                isBuffering = true;
                if (0 == mVideoView.getCurrentPosition()) {
                    mImageLayout.setVisibility(View.VISIBLE);
                }
                showLoading();
                GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------onInfo  showLoading");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                isBuffering = false;
                hideLoading();
                GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------onInfo : hideLoading ");
                break;
            default:
                break;
        }
        return true;
    }

    public void exit() {
        if (mCustomDialog != null && mCustomDialog.isShowing()) {
            mCustomDialog.dismiss();
        }
        mCustomDialog = null;
        mVideoView.stopPlayback();
        mHandler.removeCallbacksAndMessages(null);
    }

    public String getVideoThumbnailURL() {
        if (mVideoDetailRetBean == null || mVideoDetailRetBean.data == null || mVideoDetailRetBean.data.avideo == null || mVideoDetailRetBean.data.avideo.video == null) {
            return "";
        }
        return mVideoDetailRetBean.data.avideo.video.picture;
    }

    /**
     * 更新当前速度
     */
    private void updateCurrentSpeed(GPSData carGps) {
        if (carGps == null)
            return;
        if (mLayoutSpeed.getVisibility() == View.GONE)
            mLayoutSpeed.setVisibility(View.VISIBLE);
        mTvSpeed.setText(String.valueOf(carGps.speed));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // 画当前车辆位置
        if (mMapTrackView == null || CollectionUtils.isEmpty(mGpsList))
            return;
        final int position = progress * mGpsList.size() / 100;
        GPSData carGps = mGpsList.get(position);
        // 更新当前轨迹
        mMapTrackView.drawTrackCar(carGps);
        // 更新当前速度
        updateCurrentSpeed(carGps);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
