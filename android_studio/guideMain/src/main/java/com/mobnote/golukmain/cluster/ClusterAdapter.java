package com.mobnote.golukmain.cluster;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.cluster.ClusterActivity.NoVideoDataViewHolder;
import com.mobnote.golukmain.cluster.bean.ClusterVoteShareBean;
import com.mobnote.golukmain.cluster.bean.ClusterVoteShareDataBean;
import com.mobnote.golukmain.cluster.bean.TagDataBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.golukmain.newest.ClickCommentListener;
import com.mobnote.golukmain.newest.ClickFunctionListener;
import com.mobnote.golukmain.newest.ClickNewestListener;
import com.mobnote.golukmain.newest.CommentDataInfo;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.golukmain.videosuqare.ZhugeParameterFn;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.view.ExpandableTextView;
import com.mobnote.view.FlowLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.TextUtils;
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
import android.widget.Toast;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;

@SuppressLint("InflateParams")
public class ClusterAdapter extends BaseAdapter implements OnTouchListener, IRequestResultListener, ZhugeParameterFn {
    private Context mContext = null;

    static final int VIEW_TYPE_HEAD = 0;
    static final int VIEW_TYPE_RECOMMEND = 1; // 推荐视频列表
    static final int VIEW_TYPE_NEWEST = 2; // 最新视频列表
    static final int VIEW_TYPE_NO_DATA = 3;

    private int mCurrentViewType = 1; // 当前视图类型（推荐列表，最新列表）

    public TagDataBean mHeadData = null;
    public List<VideoSquareInfo> mRecommendList = null;
    public List<VideoSquareInfo> mNewestList = null;

    private int mFirstItemHeight = 0;

    private int mWidth = 0;
    private String mTagId;
    Drawable mRecommendDrawable = null;

    public ClusterAdapter(Context context, int tabType, String tagId) {
        mContext = context;
        loadRes();
        // 默认进入分享视频列表类别
        mCurrentViewType = tabType;

        mWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
        mTagId = tagId;
    }

    public void setDataInfo(List<VideoSquareInfo> recommendList, List<VideoSquareInfo> newestList) {
        if(null != recommendList) {
            mRecommendList = recommendList;
        }

        if(null != newestList) {
            mNewestList = newestList;
        }
    }

    public void setDataInfo(TagDataBean head) {
        this.mHeadData = head;
        notifyDataSetChanged();
    }

    /**
     * 删除视频后页面更新
     *
     * @param vid
     */
    public void deleteVideo(String vid) {
        boolean isDelSuccess = false;
        if (this.getCurrentViewType() == VIEW_TYPE_RECOMMEND) {
            if (TextUtils.isEmpty(vid) || null == mRecommendList || mRecommendList.size() <= 0) {
                return;
            }
            for (int i = 0; i < mRecommendList.size(); i++) {
                if (mRecommendList.get(i).mVideoEntity.videoid.equals(vid)) {
                    mRecommendList.remove(i);
                    isDelSuccess = true;
                }
            }
        } else if (this.getCurrentViewType() == VIEW_TYPE_NEWEST) {
            if (TextUtils.isEmpty(vid) || null == mNewestList || mNewestList.size() <= 0) {
                return;
            }
            for (int i = 0; i < mNewestList.size(); i++) {
                if (mNewestList.get(i).mVideoEntity.videoid.equals(vid)) {
                    mNewestList.remove(i);
                    isDelSuccess = true;
                }
            }
        }
        if (isDelSuccess) {
            this.notifyDataSetChanged();
        }
    }

    public int getCurrentViewType() {
        return mCurrentViewType;
    }

    @Override
    public int getItemViewType(int position) {
        if(0 == position) {
            return VIEW_TYPE_HEAD;
        }

        if (mCurrentViewType == VIEW_TYPE_RECOMMEND) {
            if (mRecommendList == null || mRecommendList.size() == 0) {
                return VIEW_TYPE_NO_DATA;
            }
        }

        if(mCurrentViewType == VIEW_TYPE_NEWEST) {
            if (mNewestList == null || mNewestList.size() == 0) {
                return VIEW_TYPE_NO_DATA;
            }
        }
        return mCurrentViewType;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        if (this.mHeadData == null) {
            return 0;
        } else {
            int datacount = 0;

            if (this.mCurrentViewType == VIEW_TYPE_RECOMMEND) {
                if (mRecommendList != null && mRecommendList.size() > 0) {
                    datacount = this.mRecommendList.size() + 1;
                } else {
                    datacount++;
                }
            } else {
                if (mNewestList != null && mNewestList.size() > 0) {
                    datacount = this.mNewestList.size() + 1;
                } else {
                    datacount++;
                }

            }
            if (datacount <= 1) {// 如果没有数据，则添加没有数据提示项
                datacount++;
            }
            return datacount;
        }

    }

    private String getRtmpAddress() {
        String rtmpUrl = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_GetServerAddress,
                IGetServerAddressType.GetServerAddress_HttpServer, "UrlRedirect");
        return rtmpUrl;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);

        switch (type) {
        case VIEW_TYPE_HEAD: {
            if (null == mHeadData) {
                return convertView;
            }

            HeadViewHolder headViewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.cluster_head, null);
                headViewHolder = new HeadViewHolder();

                headViewHolder.headImg = (ImageView) convertView.findViewById(R.id.mPreLoading);
                headViewHolder.partakes = (TextView) convertView.findViewById(R.id.partake_num);
                headViewHolder.recommendBtn = (Button) convertView.findViewById(R.id.recommend_btn);
                headViewHolder.newsBtn = (Button) convertView.findViewById(R.id.news_btn);
                headViewHolder.partakeBtn = (Button) convertView.findViewById(R.id.partake_btn);
                headViewHolder.voteBtn = (Button) convertView.findViewById(R.id.btn_cluster_head_vote);
                headViewHolder.nTagDescriptionETV = (ExpandableTextView) convertView.findViewById(R.id.tag_description_expandable_textview);
                convertView.setTag(headViewHolder);
            } else {
                headViewHolder = (HeadViewHolder) convertView.getTag();
            }

            int height = (int) ((float) mWidth / 1.77f);
            RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(mWidth, height);
            mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
            headViewHolder.headImg.setLayoutParams(mPlayerLayoutParams);

            if(mHeadData.type == 1 && null != mHeadData.activity) {
                if(TextUtils.isEmpty(mHeadData.activity.picture)) {
                    headViewHolder.headImg.setVisibility(View.GONE);
                } else {
                    headViewHolder.headImg.setVisibility(View.VISIBLE);
                    GlideUtils.loadImage(mContext, headViewHolder.headImg, mHeadData.activity.picture,
                            R.drawable.tacitly_pic);
                }

//                headViewHolder.headImg.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String url = getRtmpAddress() + "?type=9&activityid=" + mTagId;
//                        Intent intent = new Intent(mContext, UserOpenUrlActivity.class);
//                        intent.putExtra("url", url);
//                        intent.putExtra("need_h5_title", mContext.getString(R.string.str_activity_rule));
//                        mContext.startActivity(intent);
//                    }
//                });

                if (!TextUtils.isEmpty(mHeadData.activity.voteaddress)) {
                    headViewHolder.voteBtn.setVisibility(View.VISIBLE);
                    headViewHolder.voteBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!TextUtils.isEmpty(mHeadData.activity.voteid)) {
                                ClusterVoteShareRequest request =
                                        new ClusterVoteShareRequest(IPageNotifyFn.PageType_VoteShare, ClusterAdapter.this);
                                request.get(mHeadData.activity.voteid);
                            }
                        }
                    });
                } else {
                    headViewHolder.voteBtn.setVisibility(View.GONE);
                }

                if(TextUtils.isEmpty(mHeadData.activity.activitycontent)) {
                    headViewHolder.nTagDescriptionETV.setVisibility(View.GONE);
                } else {
                    headViewHolder.nTagDescriptionETV.setText(mHeadData.activity.activitycontent);
                }
                String joinNumStr = mContext.getString(R.string.str_participation_prompt)
                        + "<font color='#216da8'>"
                        + mContext.getString(R.string.str_participation_num_unit, mHeadData.activity.participantcount)
                        + "</font>";
                headViewHolder.partakes.setText(Html.fromHtml(joinNumStr));

                if ("1".equals(mHeadData.activity.expiration)) {
                    headViewHolder.partakeBtn.setText(mContext.getResources().getString(R.string.activity_time_out));
                    headViewHolder.partakeBtn.setTextColor(mContext.getResources().getColor(R.color.white));
                    headViewHolder.partakeBtn.setBackgroundResource(R.drawable.btn_gray_stroke_gray_body);
                } else {
                    headViewHolder.partakeBtn.setText(mContext.getResources().getString(R.string.attend_activity));
                    headViewHolder.partakeBtn.setBackgroundResource(R.drawable.btn_style_white_blue_selector);
                    headViewHolder.partakeBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            //聚合页面相册页面访问统计
                            ZhugeUtils.eventCallAlbum(mContext, mContext.getString(R.string.str_zhuge_call_album_source_cluster));

                            Intent photoalbum = new Intent(mContext, PhotoAlbumActivity.class);
                            photoalbum.putExtra("from", "cloud");

                            PromotionSelectItem item = new PromotionSelectItem();
                            item.activityid = mHeadData.activity.activityid;
                            item.activitytitle = mHeadData.activity.activityname;
                            item.channelid = mHeadData.activity.channelid;
                            item.type = 1;
                            photoalbum.putExtra(FragmentAlbum.ACTIVITY_INFO, item);
                            mContext.startActivity(photoalbum);
                        }
                    });
                }
            } else if(mHeadData.type == 0 && null != mHeadData.tag) {
                if(TextUtils.isEmpty(mHeadData.tag.picture)) {
                    headViewHolder.headImg.setVisibility(View.GONE);
                } else {
                    headViewHolder.headImg.setVisibility(View.VISIBLE);
                    GlideUtils.loadImage(mContext, headViewHolder.headImg, mHeadData.tag.picture,
                            R.drawable.tacitly_pic);
                }

                if(TextUtils.isEmpty(mHeadData.tag.description)) {
                    headViewHolder.nTagDescriptionETV.setVisibility(View.GONE);
                } else {
                    headViewHolder.nTagDescriptionETV.setText(mHeadData.tag.description);
                }

                String joinNumStr = mContext.getString(R.string.str_participation_prompt)
                        + "<font color='#216da8'>"
                        + mContext.getString(R.string.str_participation_num_unit, mHeadData.tag.participantcount)
                        + "</font>";
                headViewHolder.partakes.setText(Html.fromHtml(joinNumStr));
                headViewHolder.voteBtn.setVisibility(View.GONE);

                headViewHolder.partakeBtn.setText(mContext.getResources().getString(R.string.attend_activity));
                headViewHolder.partakeBtn.setBackgroundResource(R.drawable.btn_style_white_blue_selector);
                headViewHolder.partakeBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //聚合页面相册页面访问统计
                        ZhugeUtils.eventCallAlbum(mContext, mContext.getString(R.string.str_zhuge_call_album_source_cluster));

                        Intent photoalbum = new Intent(mContext, PhotoAlbumActivity.class);
                        photoalbum.putExtra("from", "cloud");

                        PromotionSelectItem item = new PromotionSelectItem();
                        item.activityid = mHeadData.tag.tagid;
                        item.activitytitle = mHeadData.tag.name;
                        item.channelid = "";
                        item.type = 0;
                        photoalbum.putExtra(FragmentAlbum.ACTIVITY_INFO, item);
                        mContext.startActivity(photoalbum);
                    }
                });
            }

            headViewHolder.recommendBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (mCurrentViewType == VIEW_TYPE_NEWEST) {
                        mCurrentViewType = VIEW_TYPE_RECOMMEND;
                        notifyDataSetChanged();
                    }
                }
            });

            headViewHolder.newsBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (mCurrentViewType == VIEW_TYPE_RECOMMEND) {
                        mCurrentViewType = VIEW_TYPE_NEWEST;
                        notifyDataSetChanged();
                    }
                }
            });

            if (mCurrentViewType == VIEW_TYPE_RECOMMEND) {
                headViewHolder.recommendBtn.setTextColor(Color.rgb(9, 132, 255));
                headViewHolder.newsBtn.setTextColor(Color.rgb(51, 51, 51));
            } else {
                headViewHolder.newsBtn.setTextColor(Color.rgb(9, 132, 255));
                headViewHolder.recommendBtn.setTextColor(Color.rgb(51, 51, 51));
            }

            // 计算第一项的高度
            this.mFirstItemHeight = convertView.getBottom();
            ClusterActivity ca = (ClusterActivity) mContext;
            ca.updateListViewBottom(mCurrentViewType);
        }
            break;

        case VIEW_TYPE_NEWEST:
        case VIEW_TYPE_RECOMMEND:
            int index_v = position - 1;
            final VideoSquareInfo clusterInfo;
            if (mCurrentViewType == VIEW_TYPE_RECOMMEND) {
                clusterInfo = this.mRecommendList.get(index_v);
            } else {
                clusterInfo = this.mNewestList.get(index_v);
            }

            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_sharevideo, null);
                holder.imageLayout = (ImageView) convertView.findViewById(R.id.imageLayout);
                holder.headimg = (ImageView) convertView.findViewById(R.id.headimg);
                holder.nikename = (TextView) convertView.findViewById(R.id.nikename);
                holder.time_location = (TextView) convertView.findViewById(R.id.time_location);
                holder.videoGoldImg = (ImageView) convertView.findViewById(R.id.user_center_gold);
                holder.userInfoLayout = (RelativeLayout) convertView.findViewById(R.id.user_info_layout);
                holder.function = (ImageView) convertView.findViewById(R.id.function);
                holder.v = (ImageView) convertView.findViewById(R.id.v);
                holder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
                holder.zanIcon = (ImageView) convertView.findViewById(R.id.zanIcon);

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
                int height = (int) ((float) mWidth / 1.77f);
                RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(mWidth, height);
                mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
                holder.imageLayout.setLayoutParams(mPlayerLayoutParams);
                holder.tvPraiseCount = (TextView) convertView.findViewById(R.id.tv_share_video_list_item_praise_count);
                holder.nTagsFL = (FlowLayout) convertView.findViewById(R.id.flowlayout_tag_page_video_item_tags);
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
            if (clusterInfo.mUserEntity != null && clusterInfo.mUserEntity.label != null) {
                if ("1".equals(clusterInfo.mUserEntity.label.approvelabel)) {//企业认证
                    holder.v.setImageResource(R.drawable.authentication_bluev_icon);
                    holder.v.setVisibility(View.VISIBLE);
                } else {
                    if ("1".equals(clusterInfo.mUserEntity.label.headplusv)) {//个人加V
                        holder.v.setImageResource(R.drawable.authentication_yellowv_icon);
                        holder.v.setVisibility(View.VISIBLE);
                    } else {
                        if ("1".equals(clusterInfo.mUserEntity.label.tarento)) {//达人
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
            if (null != headUrl && !"".equals(headUrl)) {
                // 使用服务器头像地址
                GlideUtils.loadNetHead(mContext, holder.headimg, headUrl, R.drawable.editor_head_feault7);
            } else {
                showHead(holder.headimg, clusterInfo.mUserEntity.headportrait);
            }

            holder.userInfoLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUserCenter(clusterInfo);
                }
            });
            holder.headimg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUserCenter(clusterInfo);
                }
            });
            holder.nikename.setText(clusterInfo.mUserEntity.nickname);
            final String sharingTime = GolukUtils.getCommentShowFormatTime(mContext,
                    clusterInfo.mVideoEntity.sharingtime);
            final String location = clusterInfo.mVideoEntity.location;
            String showTimeLocation = sharingTime;
            if (null != location) {
                showTimeLocation = showTimeLocation + " " + location;
            }
            holder.time_location.setText(showTimeLocation);

            setVideoExtra(holder, clusterInfo);
            holder.weiguan.setText(clusterInfo.mVideoEntity.clicknumber + " " + mContext.getResources().getString(R.string.cluster_weiguan));
            int count = Integer.parseInt(clusterInfo.mVideoEntity.comcount);
            holder.totalcomments.setText(mContext.getString(R.string.str_see_comments, clusterInfo.mVideoEntity.comcount));
            if (count > 3) {
                holder.totalcomments.setVisibility(View.VISIBLE);
            } else {
                holder.totalcomments.setVisibility(View.GONE);
            }

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
                holder.tvPraiseCount.setText(
                        GolukUtils.getFormatNumber(clusterInfo.mVideoEntity.praisenumber) +
                                mContext.getString(R.string.str_usercenter_praise));
            }

            if(null != clusterInfo.mVideoEntity && null != clusterInfo.mVideoEntity.tags) {
                GolukUtils.addTagsViews(mContext, clusterInfo.mVideoEntity.tags, holder.nTagsFL);
            } else {
                holder.nTagsFL.setVisibility(View.GONE);
            }

            if (clusterInfo.mVideoEntity.commentList.size() >= 1) {
                CommentDataInfo comment = clusterInfo.mVideoEntity.commentList.get(0);
                if (null != comment.replyid && !"".equals(comment.replyid) && null != comment.replyname
                        && !"".equals(comment.replyname)) {
                    UserUtils.showReplyText(mContext, holder.comment1, comment.name, comment.replyname, comment.text);
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
                    UserUtils.showReplyText(mContext, holder.comment2, comment.name, comment.replyname, comment.text);
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
                    UserUtils.showReplyText(mContext, holder.comment3, comment.name, comment.replyname, comment.text);
                } else {
                    UserUtils.showCommentText(holder.comment3, comment.name, comment.text);
                }
                holder.comment3.setVisibility(View.VISIBLE);
            } else {
                holder.comment3.setVisibility(View.GONE);
            }
            ClusterActivity ca = (ClusterActivity) mContext;
            ca.updateListViewBottom(mCurrentViewType);
            break;

        case VIEW_TYPE_NO_DATA:
            NoVideoDataViewHolder noVideoDataViewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.user_center_novideodata, null);
                noVideoDataViewHolder = new NoVideoDataViewHolder();
                noVideoDataViewHolder.tipsText = (TextView) convertView.findViewById(R.id.tv_tipstext);
                noVideoDataViewHolder.emptyImage = (TextView) convertView.findViewById(R.id.tipsimage);
                noVideoDataViewHolder.bMeasureHeight = false;
                convertView.setTag(noVideoDataViewHolder);
            } else {
                noVideoDataViewHolder = (NoVideoDataViewHolder) convertView.getTag();
            }

            if (noVideoDataViewHolder.bMeasureHeight == false) {
                if (this.mFirstItemHeight > 0) {
                    noVideoDataViewHolder.bMeasureHeight = true;
                    RelativeLayout rl = (RelativeLayout) convertView.findViewById(R.id.subject_ll);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rl.getLayoutParams();
                    lp.height = ((ClusterActivity)mContext).getListViewHeight() - this.mFirstItemHeight;
                    rl.setLayoutParams(lp);
                }
            }

            boolean bNeedRefrush = false;

            noVideoDataViewHolder.emptyImage.setVisibility(View.GONE);
            if (mCurrentViewType == VIEW_TYPE_NEWEST) {
                noVideoDataViewHolder.tipsText.setText(mContext.getString(R.string.str_cluster_newest));
            } else {
                noVideoDataViewHolder.tipsText.setText(mContext.getString(R.string.str_cluster_wonderful));
            }

            bNeedRefrush = true;
            if (bNeedRefrush == true) {
                noVideoDataViewHolder.tipsText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: re-get list here
                    }
                 });
            }
            break;

            default:
            break;
        }
        return convertView;
    }

    private void loadRes() {
        mRecommendDrawable = mContext.getResources().getDrawable(R.drawable.together_recommend_icon);
        mRecommendDrawable.setBounds(0, 0, mRecommendDrawable.getMinimumWidth(), mRecommendDrawable.getMinimumHeight());
    }

    private void showHead(ImageView view, String headportrait) {
        try {
            GlideUtils.loadLocalHead(mContext, view, ILive.mHeadImg[Integer.parseInt(headportrait)]);
        } catch (Exception e) {
            GlideUtils.loadLocalHead(mContext, view, R.drawable.editor_head_feault7);
        }
    }

    private void initListener(ViewHolder holder, final int index) {
        VideoSquareInfo videoSquareInfo = null;

        if (mCurrentViewType == VIEW_TYPE_RECOMMEND) {
            videoSquareInfo = this.mRecommendList.get(index);
        } else {
            videoSquareInfo = this.mNewestList.get(index);
        }

        final String videoId = videoSquareInfo.mVideoEntity.videoid;
        final String type = videoSquareInfo.mVideoEntity.type;
        holder.shareLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ClusterActivity)mContext).sendGetShareVideoUrlRequest(videoId, type);
                ((ClusterActivity)mContext).storeCurrentIndex(index + 1);
            }
        });

        holder.function.setOnClickListener(new ClickFunctionListener(mContext, videoSquareInfo,
                false, null)
                .setConfirm(true));

        holder.commentLayout.setOnClickListener(new ClickCommentListener(mContext, videoSquareInfo, true, mContext.getString(R.string.str_zhuge_play_video_page_cluster)));
        // 播放区域监听
        holder.imageLayout.setOnClickListener(new ClickNewestListener(mContext, videoSquareInfo, null, ZHUGE_PLAY_VIDEO_PAGE_CLUSTER));
        // 点赞
        holder.praiseLayout.setOnClickListener(new ClusterPraiseListener(mContext, videoSquareInfo));
        // 评论总数监听
        List<CommentDataInfo> comments = videoSquareInfo.mVideoEntity.commentList;
        if (comments.size() > 0) {
            holder.totalcomments.setOnClickListener(new ClickCommentListener(mContext, videoSquareInfo, false, mContext.getString(R.string.str_zhuge_play_video_page_cluster)));
            holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, videoSquareInfo, false, mContext.getString(R.string.str_zhuge_play_video_page_cluster)));
        }
    }

    /**
     * 设置，视频的，是否推荐，是否获奖，是否有参加活动
     *
     * @param holder      UI控件
     * @param clusterInfo 数据载体
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
            String reward = clusterInfo.mVideoEntity.videoExtra.isreward;
            String sysflag = clusterInfo.mVideoEntity.videoExtra.sysflag;
            if (null != reward && "1".equals(reward) && null != sysflag && "1".equals(sysflag)) {
                holder.videoGoldImg.setVisibility(View.VISIBLE);
            } else {
                holder.videoGoldImg.setVisibility(View.GONE);
            }
            // 显示是否推荐
            if (clusterInfo.mVideoEntity.videoExtra.isrecommend.equals("1")) {
                holder.time_location.setCompoundDrawables(null, null, this.mRecommendDrawable, null);
            } else {
                holder.time_location.setCompoundDrawables(null, null, null, null);
            }
        } else {
            holder.videoGoldImg.setVisibility(View.GONE);
            holder.time_location.setCompoundDrawables(null, null, null, null);
        }

        UserUtils.showCommentText(mContext, false, clusterInfo, holder.detail, clusterInfo.mUserEntity.nickname,
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

    public static class HeadViewHolder {
        TextView title;
        ImageView headImg;
        TextView partakes;
        Button recommendBtn;
        Button newsBtn;
        Button partakeBtn;
        Button voteBtn;
        ExpandableTextView nTagDescriptionETV;
    }

    public static class ViewHolder {
        String VideoID;
        ImageView imageLayout;
        ImageView headimg;
        ImageView v;
        TextView nikename;
        TextView time_location;
        ImageView function;
        ImageView videoGoldImg;
        RelativeLayout userInfoLayout;

        LinearLayout praiseLayout;
        ImageView zanIcon;

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
        TextView tvPraiseCount;

        LinearLayout totlaCommentLayout;
        TextView comment1;
        TextView comment2;
        TextView comment3;

        ImageView isopen;
        FlowLayout nTagsFL;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int id = v.getId();

        if (id == R.id.share_btn) {
            Button sharebtn = (Button)v;
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
        }
        return false;
    }

    public void startUserCenter(VideoSquareInfo clusterInfo) {
        GolukUtils.startUserCenterActivity(mContext, clusterInfo.mUserEntity.uid);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (!GolukUtils.isActivityAlive(activity)) {
                return;
            }
        }
        if (requestType != IPageNotifyFn.PageType_VoteShare) {
            return;
        }

        if (null == result) {
            Toast.makeText(mContext, mContext.getString(R.string.network_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ClusterVoteShareBean bean = (ClusterVoteShareBean) result;
        if (!bean.success || null == bean.data) {
            if (!TextUtils.isEmpty(bean.msg)) {
                Toast.makeText(mContext, bean.msg,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.network_error),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ClusterVoteShareDataBean data = bean.data;
        String address = mHeadData.activity.voteaddress;
        if (null == address || address.trim().equals("")) {
            return;
        } else {
            Intent intent = new Intent(mContext, UserOpenUrlActivity.class);
            intent.putExtra(GolukConfig.H5_URL, address);
            intent.putExtra(GolukConfig.WEB_TYPE, GolukConfig.NEED_SHARE);
            intent.putExtra(GolukConfig.NEED_H5_TITLE, data.title);
            intent.putExtra(GolukConfig.NEED_SHARE_ID, data.voteid);
            intent.putExtra(GolukConfig.NEED_SHARE_PICTURE, data.picture);
            intent.putExtra(GolukConfig.NEED_SHARE_INTRO, data.introduction);
            intent.putExtra(GolukConfig.URL_OPEN_PATH, "cluster_adapter");
            mContext.startActivity(intent);
        }
    }
}
