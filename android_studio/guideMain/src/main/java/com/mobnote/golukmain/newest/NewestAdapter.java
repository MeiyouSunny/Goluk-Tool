package com.mobnote.golukmain.newest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.golukmain.videosuqare.CategoryListView;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class NewestAdapter extends BaseAdapter {
    private Context mContext = null;
    private NewestListHeadDataInfo mHeadDataInfo = null;
    private List<VideoSquareInfo> mDataList = null;
    private int count = 0;
    private int width = 0;
    private float density = 0;
    private NewestListView mNewestListView = null;
    private CategoryListView mCategoryListView = null;
    private final int FIRST_TYPE = 0;
    private final int OTHERS_TYPE = 1;
    private RelativeLayout mHeadView;
    private ViewHolder holder;
    private final float widthHeight = 1.78f;
    private int mPlayPage = 0;

    public NewestAdapter(Context context, int playPage) {
        mContext = context;
        this.mPlayPage = playPage;
        mDataList = new ArrayList<VideoSquareInfo>();
        width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
        density = SoundUtils.getInstance().getDisplayMetrics().density;
    }

    public void setData(NewestListHeadDataInfo headata, List<VideoSquareInfo> data) {
        mHeadView = null;
        mHeadDataInfo = headata;
        mDataList.clear();
        mDataList.addAll(data);
        if (null == mHeadDataInfo) {
            count = mDataList.size();
        } else {
            count = mDataList.size() + 1;
        }
        this.notifyDataSetChanged();
    }

    public void loadData(List<VideoSquareInfo> data) {
        mDataList.clear();
        mDataList.addAll(data);
        if (null == mHeadDataInfo) {
            count = mDataList.size();
        } else {
            count = mDataList.size() + 1;
        }
        this.notifyDataSetChanged();
    }

    // 删除视频
    public void deleteVideo(String vid) {
        if (TextUtils.isEmpty(vid) || null == mDataList || mDataList.size() <= 0) {
            return;
        }
        boolean isDel = false;
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).mVideoEntity.videoid.equals(vid)) {
                mDataList.remove(i);
                isDel = true;
            }
        }
        if (isDel) {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return count;
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (null == mHeadDataInfo) {
            return OTHERS_TYPE;
        } else {
            if (position == 0) {
                return FIRST_TYPE;
            } else {
                return OTHERS_TYPE;
            }
        }
    }

    @Override
    public View getView(int arg0, View convertView, ViewGroup parent) {
        int type = getItemViewType(arg0);
        if (FIRST_TYPE == type) {
            convertView = getHeadView();
        } else {
            convertView = loadLayout(convertView, arg0);
        }

        return convertView;
    }

    private View loadLayout(View convertView, int arg0) {
        if (null == convertView) {
            convertView = initLayout();
        } else {
            holder = (ViewHolder) convertView.getTag();
            if (null == holder) {
                convertView = initLayout();
            }
        }

        int index = arg0;
        if (null != mHeadDataInfo) {
            index = arg0 - 1;
        }
        initView(index);
        initListener(index);

        return convertView;
    }

    private View initLayout() {
        holder = new ViewHolder();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.newest_list_item, null);
        holder.vDivider = convertView.findViewById(R.id.v_item_divider_line);
        holder.videoImg = (ImageView) convertView.findViewById(R.id.imageLayout);
        holder.liveImg = (ImageView) convertView.findViewById(R.id.newlist_item_liveicon);
        holder.headimg = (ImageView) convertView.findViewById(R.id.headimg);
        holder.nikename = (TextView) convertView.findViewById(R.id.nikename);
        holder.timeLocation = (TextView) convertView.findViewById(R.id.time_location);
        holder.function = (ImageView) convertView.findViewById(R.id.function);
        // holder.rlHead = convertView.findViewById(R.id.rl_head_img);

        holder.praiseText = (TextView) convertView.findViewById(R.id.tv_newest_list_item_praise);
        holder.commentText = (TextView) convertView.findViewById(R.id.tv_newest_list_item_comment);
        holder.shareText = (TextView) convertView.findViewById(R.id.tv_newest_list_item_share);

        holder.surroundWatch = (TextView) convertView.findViewById(R.id.tv_newest_list_item_surround);
        holder.totalcomments = (TextView) convertView.findViewById(R.id.totalcomments);
        holder.detail = (TextView) convertView.findViewById(R.id.detail);
        holder.nTagsFL = (FlowLayout) convertView.findViewById(R.id.flowlayout_video_item_tags);
        holder.ivReward = (ImageView) convertView.findViewById(R.id.iv_reward_tag);
        holder.totlaCommentLayout = (LinearLayout) convertView.findViewById(R.id.totlaCommentLayout);
        holder.comment1 = (TextView) convertView.findViewById(R.id.comment1);
        holder.comment2 = (TextView) convertView.findViewById(R.id.comment2);
        holder.comment3 = (TextView) convertView.findViewById(R.id.comment3);
        holder.ivLogoVIP = (ImageView) convertView.findViewById(R.id.iv_vip_logo);
        holder.rlUserInfo = (RelativeLayout) convertView.findViewById(R.id.rl_user_info);
        holder.tvPraiseCount = (TextView) convertView.findViewById(R.id.tv_newest_list_item_praise_count);

        int height = (int) ((float) width / widthHeight);
        RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
        mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
        holder.videoImg.setLayoutParams(mPlayerLayoutParams);
        convertView.setTag(holder);

        return convertView;
    }

    private void initListener(int index) {
        if (index < 0 || index >= mDataList.size()) {
            return;
        }

        VideoSquareInfo mVideoSquareInfo = mDataList.get(index);
        // 分享监听
        ClickShareListener tempShareListener = new ClickShareListener(mContext, mVideoSquareInfo, mNewestListView);
        tempShareListener.setCategoryListView(mCategoryListView);
        holder.shareText.setOnClickListener(tempShareListener);
        // 举报监听
        holder.function.setOnClickListener(new ClickFunctionListener(mContext, mVideoSquareInfo, false, null));
        // 评论监听
        holder.commentText.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, true, mContext.getString(R.string.str_zhuge_newest_event)));
        // 播放区域监听
        holder.videoImg.setOnClickListener(new ClickNewestListener(mContext, mVideoSquareInfo, mNewestListView, mPlayPage));
        holder.headimg.setOnClickListener(new ClickHeadListener(mContext, mVideoSquareInfo));
        // 点赞
        ClickPraiseListener tempPraiseListener = new ClickPraiseListener(mContext, mVideoSquareInfo, mNewestListView);
        tempPraiseListener.setCategoryListView(mCategoryListView);
        holder.praiseText.setOnClickListener(tempPraiseListener);
        // 评论总数监听
        List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
        if (comments.size() > 0) {
            holder.totalcomments.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false, mContext.getString(R.string.str_zhuge_newest_event)));
            holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false, mContext.getString(R.string.str_zhuge_newest_event)));
        }

        final VideoSquareInfo vsInfo = mVideoSquareInfo;

        holder.rlUserInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startUserCenter(vsInfo);
            }
        });
    }

    private void initView(int index) {
        if (index < 0 || index >= mDataList.size()) {
            return;
        }
        VideoSquareInfo mVideoSquareInfo = mDataList.get(index);
        if (0 == index) {
            holder.vDivider.setVisibility(View.GONE);
        } else {
            holder.vDivider.setVisibility(View.VISIBLE);
        }

        GlideUtils.loadImage(mContext, holder.videoImg, mVideoSquareInfo.mVideoEntity.picture, R.drawable.tacitly_pic);
        if (null != mVideoSquareInfo.mUserEntity && null != mVideoSquareInfo.mUserEntity.label) {
            String approveLabel = mVideoSquareInfo.mUserEntity.label.approvelabel;
            String approve = mVideoSquareInfo.mUserEntity.label.approve;
            String tarento = mVideoSquareInfo.mUserEntity.label.tarento;
            String headplusv = mVideoSquareInfo.mUserEntity.label.headplusv;
            String headplusvdes = mVideoSquareInfo.mUserEntity.label.headplusvdes;
            if (null == approveLabel && null == approve && null == tarento && null == headplusv && null == headplusvdes) {
                holder.ivLogoVIP.setVisibility(View.GONE);
            } else {
                if ("1".equals(approveLabel)) {
                    holder.ivLogoVIP.setImageResource(R.drawable.authentication_bluev_icon);
                    holder.ivLogoVIP.setVisibility(View.VISIBLE);
                } else if ("1".equals(headplusv)) {
                    holder.ivLogoVIP.setImageResource(R.drawable.authentication_yellowv_icon);
                    holder.ivLogoVIP.setVisibility(View.VISIBLE);
                } else if ("1".equals(tarento)) {
                    holder.ivLogoVIP.setImageResource(R.drawable.authentication_star_icon);
                    holder.ivLogoVIP.setVisibility(View.VISIBLE);
                } else {
                    holder.ivLogoVIP.setVisibility(View.GONE);
                }
            }
        }

        String headUrl = mVideoSquareInfo.mUserEntity.mCustomAvatar;
        if (null != headUrl && !"".equals(headUrl)) {
            // 使用服务器头像地址
            GlideUtils.loadNetHead(mContext, holder.headimg, headUrl, R.drawable.editor_head_feault7);
        } else {
            showHead(holder.headimg, mVideoSquareInfo.mUserEntity.headportrait);
        }

        holder.nikename.setText(mVideoSquareInfo.mUserEntity.nickname);

        holder.timeLocation.setText(GolukUtils.getCommentShowFormatTime(mContext,
                mVideoSquareInfo.mVideoEntity.sharingts) + " " + mVideoSquareInfo.mVideoEntity.location);

        if (null != mVideoSquareInfo.mVideoEntity.videoExtra) {
            String recommend = mVideoSquareInfo.mVideoEntity.videoExtra.isrecommend;
            if (null != recommend && "1".equals(recommend)) {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.together_recommend_icon);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.timeLocation.setCompoundDrawables(null, null, drawable, null);
            } else {
                holder.timeLocation.setCompoundDrawables(null, null, null, null);
            }

            String reward = mVideoSquareInfo.mVideoEntity.videoExtra.isreward;
            String sysflag = mVideoSquareInfo.mVideoEntity.videoExtra.sysflag;
            if (null != reward && "1".equals(reward) && null != sysflag && "1".equals(sysflag)) {
                holder.ivReward.setVisibility(View.VISIBLE);
            } else {
                holder.ivReward.setVisibility(View.GONE);
            }
        } else {
            holder.timeLocation.setCompoundDrawables(null, null, null, null);
            holder.ivReward.setVisibility(View.GONE);
        }

        if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
            holder.praiseText.setTextColor(Color.rgb(0x88, 0x88, 0x88));
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.videodetail_like);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            holder.praiseText.setCompoundDrawables(drawable, null, null, null);
        } else {
            holder.praiseText.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.videodetail_like_press);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            holder.praiseText.setCompoundDrawables(drawable, null, null, null);
        }

        if ("-1".equals(mVideoSquareInfo.mVideoEntity.praisenumber)) {
            holder.tvPraiseCount.setText(mContext.getString(R.string.str_usercenter_praise));
        } else {
            holder.tvPraiseCount.setText(GolukUtils.getFormatNumber(mVideoSquareInfo.mVideoEntity.praisenumber)
                    + mContext.getString(R.string.str_usercenter_praise));
        }

        if ("-1".equals(mVideoSquareInfo.mVideoEntity.clicknumber)) {
            holder.surroundWatch.setText("");
            holder.surroundWatch.setVisibility(View.GONE);
        } else {
            holder.surroundWatch.setVisibility(View.VISIBLE);
            holder.surroundWatch.setText(GolukUtils.getFormatNumber(mVideoSquareInfo.mVideoEntity.clicknumber));
        }

        if (TextUtils.isEmpty(mVideoSquareInfo.mVideoEntity.describe)) {
            holder.detail.setVisibility(View.GONE);
        } else {
            holder.detail.setVisibility(View.VISIBLE);
            UserUtils.showCommentText(holder.detail, mVideoSquareInfo.mUserEntity.nickname,
                    mVideoSquareInfo.mVideoEntity.describe);
        }

        if(null != mVideoSquareInfo.mVideoEntity && null != mVideoSquareInfo.mVideoEntity.tags) {
            GolukUtils.addTagsViews(mContext, mVideoSquareInfo.mVideoEntity.tags, holder.nTagsFL);
        } else {
            holder.nTagsFL.setVisibility(View.GONE);
        }

        if (isLive(mVideoSquareInfo)) {
            // 直播
            holder.liveImg.setVisibility(View.VISIBLE);
            holder.commentText.setVisibility(View.GONE);
            holder.surroundWatch.setVisibility(View.GONE);
        } else {
            // 点播
            holder.liveImg.setVisibility(View.GONE);
            holder.commentText.setVisibility(View.VISIBLE);
            holder.surroundWatch.setVisibility(View.VISIBLE);
        }

        if ("1".equals(mVideoSquareInfo.mVideoEntity.iscomment)) {
            List<CommentDataInfo> comments = mVideoSquareInfo.mVideoEntity.commentList;
            if (null != comments && comments.size() > 0) {
                if (isLive(mVideoSquareInfo)) {
                    // 直播不显示评论
                    holder.totalcomments.setVisibility(View.GONE);
                    holder.totlaCommentLayout.setVisibility(View.GONE);
                } else {
                    int comcount = Integer.parseInt(mVideoSquareInfo.mVideoEntity.comcount);
                    if (comcount <= 3) {
                        holder.totalcomments.setVisibility(View.GONE);
                    } else {
                        holder.totalcomments.setVisibility(View.VISIBLE);
                        holder.totalcomments.setText(mContext.getString(R.string.str_see_comments,
                                GolukUtils.getFormatNumber(mVideoSquareInfo.mVideoEntity.comcount)));
                    }

                    holder.totlaCommentLayout.setVisibility(View.VISIBLE);
                    holder.totalcomments
                            .setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo, false, mContext.getString(R.string.str_zhuge_newest_event)));
                    holder.totlaCommentLayout.setOnClickListener(new ClickCommentListener(mContext, mVideoSquareInfo,
                            false, mContext.getString(R.string.str_zhuge_newest_event)));
                    holder.comment1.setVisibility(View.VISIBLE);
                    holder.comment2.setVisibility(View.VISIBLE);
                    holder.comment3.setVisibility(View.VISIBLE);
                    if (1 == comments.size()) {
                        if (null != comments.get(0).replyid && !"".equals(comments.get(0).replyid)
                                && null != comments.get(0).replyname && !"".equals(comments.get(0).replyname)) {
                            showReplyText(holder.comment1, comments.get(0).name, comments.get(0).replyname,
                                    comments.get(0).text);
                        } else {
                            UserUtils.showCommentText(holder.comment1, comments.get(0).name, comments.get(0).text);
                        }
                        holder.comment2.setVisibility(View.GONE);
                        holder.comment3.setVisibility(View.GONE);
                    } else if (2 == comments.size()) {
                        if (null != comments.get(0).replyid && !"".equals(comments.get(0).replyid)
                                && null != comments.get(0).replyname && !"".equals(comments.get(0).replyname)) {
                            showReplyText(holder.comment1, comments.get(0).name, comments.get(0).replyname,
                                    comments.get(0).text);
                        } else {
                            UserUtils.showCommentText(holder.comment1, comments.get(0).name, comments.get(0).text);
                        }
                        if (null != comments.get(1).replyid && !"".equals(comments.get(1).replyid)
                                && null != comments.get(1).replyname && !"".equals(comments.get(1).replyname)) {
                            showReplyText(holder.comment2, comments.get(1).name, comments.get(1).replyname,
                                    comments.get(1).text);
                        } else {
                            UserUtils.showCommentText(holder.comment2, comments.get(1).name, comments.get(1).text);
                        }
                        holder.comment3.setVisibility(View.GONE);
                    } else if (3 == comments.size()) {
                        if (null != comments.get(0).replyid && !"".equals(comments.get(0).replyid)
                                && null != comments.get(0).replyname && !"".equals(comments.get(0).replyname)) {
                            showReplyText(holder.comment1, comments.get(0).name, comments.get(0).replyname,
                                    comments.get(0).text);
                        } else {
                            UserUtils.showCommentText(holder.comment1, comments.get(0).name, comments.get(0).text);
                        }
                        if (null != comments.get(1).replyid && !"".equals(comments.get(1).replyid)
                                && null != comments.get(1).replyname && !"".equals(comments.get(1).replyname)) {
                            showReplyText(holder.comment2, comments.get(1).name, comments.get(1).replyname,
                                    comments.get(1).text);
                        } else {
                            UserUtils.showCommentText(holder.comment2, comments.get(1).name, comments.get(1).text);
                        }
                        if (null != comments.get(2).replyid && !"".equals(comments.get(2).replyid)
                                && null != comments.get(2).replyname && !"".equals(comments.get(2).replyname)) {
                            showReplyText(holder.comment3, comments.get(2).name, comments.get(2).replyname,
                                    comments.get(2).text);
                        } else {
                            UserUtils.showCommentText(holder.comment3, comments.get(2).name, comments.get(2).text);
                        }
                    }
                }

            } else {
                holder.totalcomments.setVisibility(View.GONE);
                holder.totlaCommentLayout.setVisibility(View.GONE);
            }
        } else {
            holder.totalcomments.setVisibility(View.GONE);
            holder.totlaCommentLayout.setVisibility(View.GONE);
        }
    }

    private void showHead(ImageView view, String headportrait) {
        try {
            GlideUtils.loadLocalHead(mContext, view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
        } catch (Exception e) {
            GlideUtils.loadLocalHead(mContext, view, R.drawable.editor_head_feault7);
        }
    }

    private boolean isLive(VideoSquareInfo mVideoSquareInfo) {
        return "1".equals(mVideoSquareInfo.mVideoEntity.type);
    }

    private void showReplyText(TextView view, String nikename, String replyName, String text) {
        String replyLabel = mContext.getString(R.string.str_reply);
        String replyText = "@" + replyName + mContext.getString(R.string.str_colon);
        String str = nikename + " " + replyLabel + replyText + text;
        SpannableStringBuilder style = new SpannableStringBuilder(str);

        style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)),
                      nikename.length() + 1 + replyLabel.length(),
                      nikename.length() + replyLabel.length() + replyText.length(),
                      Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(style);
    }

    private View getHeadView() {
        if (null == mHeadView) {
            int imagewidth = (int) ((width - 10 * density) / 2);
            int imageheight = (int) (imagewidth * 0.56);
            mHeadView = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.category_layout, null);

            addLiveLayout();

            RelativeLayout main = (RelativeLayout) mHeadView.findViewById(R.id.main);

            final int size = mHeadDataInfo.categoryList.size();
            final int commMargin = (int) (10 * density);
            for (int i = 0; i < size; i++) {
                main.setPadding(0, commMargin, 0, 0);
                CategoryDataInfo mCategoryDataInfo = mHeadDataInfo.categoryList.get(i);

                RelativeLayout item = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.category_item,
                        null);
                item.setId(i + 1111);
                item.setOnTouchListener(new ClickCategoryListener(mContext, mCategoryDataInfo, this));

                TextView mTitleName = (TextView) item.findViewById(R.id.mTitleName);
                TextView mUpdateTime = (TextView) item.findViewById(R.id.mUpdateTime);
                ImageView mImageView = (ImageView) item.findViewById(R.id.mImageView);
                mTitleName.setText(mCategoryDataInfo.name);
                mUpdateTime.setText(GolukUtils.getNewCategoryShowTime(mContext, mCategoryDataInfo.ts));
                RelativeLayout.LayoutParams dvParams = new RelativeLayout.LayoutParams(imagewidth, imageheight);
                mImageView.setLayoutParams(dvParams);
                loadHeadImage(mImageView, mCategoryDataInfo.coverurl, imagewidth, imageheight);

                RelativeLayout.LayoutParams itemparams = new RelativeLayout.LayoutParams(imagewidth, imageheight);
                int id = i + 1111 - 2;
                if (i % 2 == 0) {
                    itemparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    itemparams.setMargins(0, 0, commMargin, commMargin);
                } else {
                    itemparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    itemparams.setMargins(0, 0, 0, commMargin);
                }
                itemparams.addRule(RelativeLayout.BELOW, id);

                main.addView(item, itemparams);
            }
        }

        return mHeadView;
    }

    private void addLiveLayout() {
        RelativeLayout liveLayout = null;
        try {
            liveLayout = (RelativeLayout) mHeadView.findViewById(R.id.liveLayout);
            LiveInfo mLiveInfo = mHeadDataInfo.mLiveDataInfo;
            if (null == mLiveInfo || Integer.parseInt(mLiveInfo.number) <= 0) {
                liveLayout.setVisibility(View.GONE);
                return;
            }
            liveLayout.setVisibility(View.VISIBLE);
            liveLayout.setOnClickListener(new ClickLiveListener(mContext));

            int height = (int) ((float) width / widthHeight);
            RelativeLayout.LayoutParams liveLayoutParams = new RelativeLayout.LayoutParams(width, height);
            liveLayoutParams.addRule(RelativeLayout.BELOW, R.id.main);
            liveLayoutParams.topMargin = (int) (10 * density);
            liveLayout.setLayoutParams(liveLayoutParams);

            ImageView mImageView = (ImageView) mHeadView.findViewById(R.id.mImageView);
            RelativeLayout.LayoutParams dvParams = new RelativeLayout.LayoutParams(width, height);
            mImageView.setLayoutParams(dvParams);
            loadHeadImage(mImageView, mLiveInfo.pic, width, height);

            LinearLayout mLookLayout = (LinearLayout) mHeadView.findViewById(R.id.mLookLayout);
            TextView mLookNum = (TextView) mHeadView.findViewById(R.id.mLookNum);

            if ("-1".equals(mLiveInfo.number)) {
                mLookLayout.setVisibility(View.GONE);
            } else {
                mLookLayout.setVisibility(View.VISIBLE);
                mLookNum.setText(mLiveInfo.number);
            }
        } catch (Exception e) {
            if (null != liveLayout) {
                liveLayout.setVisibility(View.GONE);
            }
        }
    }

    private void loadHeadImage(final ImageView image, String url, int width, int height) {
        GlideUtils.loadImage(mContext, image, url, R.drawable.tacitly_pic);
    }

    static class ViewHolder {
        ImageView videoImg;
        ImageView liveImg;
        ImageView headimg;
        TextView nikename;
        TextView timeLocation;
        ImageView function;
        ImageView ivLogoVIP;

        TextView praiseText;
        TextView commentText;
        TextView shareText;
        TextView surroundWatch;
        TextView detail;
        FlowLayout nTagsFL;
        TextView totalcomments;

        LinearLayout totlaCommentLayout;
        TextView comment1;
        TextView comment2;
        TextView comment3;
        ImageView ivReward;
        View vDivider;
        TextView tvPraiseCount;

        RelativeLayout rlUserInfo;
        // View rlHead;
    }

    public void setNewestLiseView(NewestListView view) {
        this.mNewestListView = view;
    }

    public void startUserCenter(VideoSquareInfo videoSquareInfo) {
        GolukUtils.startUserCenterActivity(mContext, videoSquareInfo.mUserEntity.uid);
    }

    public void setCategoryListView(CategoryListView view) {
        mCategoryListView = view;
    }

    public void updateClickPraiseNumber(VideoSquareInfo info) {
        final int size = mDataList.size();
        for (int i = 0; i < size; i++) {
            VideoSquareInfo vs = mDataList.get(i);
            if (vs.id.equals(info.id)) {
                mDataList.get(i).mVideoEntity.praisenumber = info.mVideoEntity.praisenumber;
                mDataList.get(i).mVideoEntity.ispraise = info.mVideoEntity.ispraise;
                this.notifyDataSetChanged();
                break;
            }
        }
    }
}
