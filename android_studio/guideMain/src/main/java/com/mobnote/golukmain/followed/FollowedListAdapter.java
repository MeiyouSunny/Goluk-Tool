package com.mobnote.golukmain.followed;

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
import com.mobnote.golukmain.followed.bean.FollowedComListBean;
import com.mobnote.golukmain.followed.bean.FollowedRecomUserBean;
import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.view.FlowLayout;

import java.util.List;

public class FollowedListAdapter extends BaseAdapter {
    private FragmentFollowed mFragment;
    private List<Object> mList;
    private final float widthHeight = 1.78f;
    private int width = 0;

    private final static String TAG = "FollowedListAdapter";

    public FollowedListAdapter(FragmentFollowed fragment) {
        super();
        this.mFragment = fragment;
        width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
    }

    public void setData(List<Object> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void appendData(List<Object> list) {
        mList.addAll(list);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return null == mList ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (null == mList || position < 0 || position > mList.size() - 1) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_FOLLOWED = 1;
    private final static int TYPE_RECOMMEND = 2;

    @Override
    public int getItemViewType(int position) {
        Object obj = mList.get(position);

        if (obj instanceof FollowedVideoObjectBean) {
            return TYPE_FOLLOWED;
        } else if (obj instanceof FollowedRecomUserBean) {
            return TYPE_RECOMMEND;
        } else if (obj instanceof String) {
            if (FragmentFollowed.FOLLOWED_EMPTY.equals((String) obj)) {
                return TYPE_EMPTY;
            }
        }
        return TYPE_EMPTY;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int type = getItemViewType(position);
        if (TYPE_FOLLOWED == type) {
            convertView = getFollowedView(position, convertView, viewGroup);
        } else if (TYPE_RECOMMEND == type) {
            convertView = getRecommendView(position, convertView, viewGroup);
        } else {
            convertView = getEmptyView(position, convertView, viewGroup);
        }

        return convertView;
    }

    private View getEmptyView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolderEmpty holderEmpty = null;
        if (null == convertView) {
            holderEmpty = new ViewHolderEmpty();
            convertView = LayoutInflater.from(mFragment.getActivity()).inflate(
                    R.layout.rl_list_view_follow_type_recommend_item_empty, null);
            holderEmpty.nEmptyTV = (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_item_empty);
        }
        return convertView;
    }

    private View getRecommendView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolderRecommend holderRec = null;
        if (null == convertView) {
            holderRec = new ViewHolderRecommend();
            convertView = LayoutInflater.from(mFragment.getActivity()).inflate(R.layout.rl_list_view_follow_type_recommend_item, null);
            holderRec.nDividerV = convertView.findViewById(R.id.v_list_view_follow_type_recommend_item_label_title_div);
            holderRec.nItemContentTV = (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_content);
            holderRec.nItemFirstVideoComTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_first_video_comment);
            holderRec.nItemFirstVideoDesTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_first_video_description);
            holderRec.nItemFirstVideoIV =
                    (ImageView) convertView.findViewById(R.id.iv_list_view_follow_type_recommend_item_first_video);
            holderRec.nItemFirstVideoViewTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_first_video_view);
            holderRec.nItemFollowTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_follow);
            holderRec.nItemNameTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_name);
            holderRec.nItemSecondVideoComTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_second_video_comment);
            holderRec.nItemSecondVideoDesTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_second_video_description);
            holderRec.nItemSecondVideoIV =
                    (ImageView) convertView.findViewById(R.id.iv_list_view_follow_type_recommend_item_second_video);
            holderRec.nItemSecondVideoViewTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_second_video_view);
            holderRec.nItemUserAuthIV =
                    (ImageView) convertView.findViewById(R.id.iv_list_view_follow_type_recommend_item_auth_tag);
            holderRec.nItemUserAvatarIV =
                    (ImageView) convertView.findViewById(R.id.iv_list_view_follow_type_recommend_item_avatar);
            holderRec.nItemUserRL =
                    convertView.findViewById(R.id.rl_list_view_follow_type_recommend_item_user);
            holderRec.nLabelTitleTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_label_title);
            holderRec.nLabelRL =
                    convertView.findViewById(R.id.rl_list_view_follow_type_recommend_item_label);
            holderRec.nLableFollowTV =
                    (TextView) convertView.findViewById(R.id.tv_list_view_follow_type_recommend_item_follow_all);
            holderRec.nItemFollowRL =
                    convertView.findViewById(R.id.rl_list_view_follow_type_recommend_item_follow);
            holderRec.nLabelFollowAllRL =
                    convertView.findViewById(R.id.rl_list_view_follow_type_recommend_item_follow_all);
            holderRec.nItemFirstVideoRL =
                    convertView.findViewById(R.id.rl_list_view_follow_type_recommend_item_first_video);
            holderRec.nItemSecondVideoRL =
                    convertView.findViewById(R.id.rl_list_view_follow_type_recommend_item_second_video);
            convertView.setTag(holderRec);
        } else {
            holderRec = (ViewHolderRecommend) convertView.getTag();
        }

        if (position < 0 || position >= mList.size()) {
            return convertView;
        }

        FollowedRecomUserBean recomUserBean = (FollowedRecomUserBean) mList.get(position);
        if (null == recomUserBean) {
            return convertView;
        }

        if (recomUserBean.position != 0) {
            holderRec.nDividerV.setVisibility(View.GONE);
            holderRec.nLabelRL.setVisibility(View.GONE);
        } else {
            holderRec.nDividerV.setVisibility(View.VISIBLE);
            holderRec.nLabelRL.setVisibility(View.VISIBLE);
        }

        String headUrl = recomUserBean.customavatar;
        if (null != headUrl && !"".equals(headUrl)) {
            // 使用服务器头像地址
            GlideUtils.loadNetHead(mFragment.getActivity(), holderRec.nItemUserAvatarIV, headUrl, R.drawable.editor_head_feault7);
        } else {
            showHead(holderRec.nItemUserAvatarIV, recomUserBean.avatar);
        }

        holderRec.nItemNameTV.setText(recomUserBean.nickname);
        holderRec.nItemContentTV.setText(recomUserBean.introduction);

        if (null != recomUserBean.hotvideo && recomUserBean.hotvideo.size() > 0) {
            int videoNum = recomUserBean.hotvideo.size();
            GlideUtils.loadImage(mFragment.getActivity(), holderRec.nItemFirstVideoIV,
                    recomUserBean.hotvideo.get(0).pictureurl, R.drawable.tacitly_pic);
            holderRec.nItemFirstVideoDesTV.setText(recomUserBean.hotvideo.get(0).description);
            holderRec.nItemFirstVideoViewTV.setText(GolukUtils.getFormatedNumber(recomUserBean.hotvideo.get(0).clickcount + ""));
            holderRec.nItemFirstVideoComTV.setText(GolukUtils.getFormatedNumber(recomUserBean.hotvideo.get(0).commentcount + ""));
            final String firstVideoId = recomUserBean.hotvideo.get(0).videoid;
            holderRec.nItemFirstVideoIV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //视频详情页访问
                    ZhugeUtils.eventVideoDetail(mFragment.getActivity(), mFragment.getActivity().getString(R.string.str_zhuge_follow));

                    GolukUtils.startVideoDetailActivity(mFragment.getActivity(), firstVideoId);
                }
            });

            holderRec.nItemFirstVideoDesTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //视频详情页访问
                    ZhugeUtils.eventVideoDetail(mFragment.getActivity(), mFragment.getActivity().getString(R.string.str_zhuge_follow));

                    GolukUtils.startVideoDetailActivity(mFragment.getActivity(), firstVideoId);
                }
            });

            if (videoNum == 2) {
                GlideUtils.loadImage(mFragment.getActivity(), holderRec.nItemSecondVideoIV,
                        recomUserBean.hotvideo.get(1).pictureurl, R.drawable.tacitly_pic);
                holderRec.nItemSecondVideoDesTV.setText(recomUserBean.hotvideo.get(1).description);
                holderRec.nItemSecondVideoViewTV.setText(GolukUtils.getFormatedNumber(recomUserBean.hotvideo.get(1).clickcount + ""));
                holderRec.nItemSecondVideoComTV.setText(GolukUtils.getFormatedNumber(recomUserBean.hotvideo.get(1).commentcount + ""));
                holderRec.nItemSecondVideoRL.setVisibility(View.VISIBLE);

                final String secondVideoId = recomUserBean.hotvideo.get(1).videoid;

                holderRec.nItemSecondVideoIV.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //视频详情页访问
                        ZhugeUtils.eventVideoDetail(mFragment.getActivity(), mFragment.getActivity().getString(R.string.str_zhuge_follow));

                        GolukUtils.startVideoDetailActivity(mFragment.getActivity(), secondVideoId);
                    }
                });

                holderRec.nItemSecondVideoDesTV.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //视频详情页访问
                        ZhugeUtils.eventVideoDetail(mFragment.getActivity(), mFragment.getActivity().getString(R.string.str_zhuge_follow));

                        GolukUtils.startVideoDetailActivity(mFragment.getActivity(), secondVideoId);
                    }
                });
            } else if (videoNum == 1) {
                holderRec.nItemSecondVideoRL.setVisibility(View.INVISIBLE);
            }
        }

        if (recomUserBean.showAllFollow) {
            holderRec.nLabelFollowAllRL.setVisibility(View.VISIBLE);
        } else {
            holderRec.nLabelFollowAllRL.setVisibility(View.GONE);
        }

        if (null != recomUserBean && null != recomUserBean.certification) {
            String approveLabel = recomUserBean.certification.isorgcertificated;
            String approve = recomUserBean.certification.orgcertification;
            String tarento = recomUserBean.certification.isstar;
            String headplusv = recomUserBean.certification.isusercertificated;
            String headplusvdes = recomUserBean.certification.usercertification;

            if (null == approveLabel && null == approve &&
                    null == tarento && null == headplusv && null == headplusvdes) {
                holderRec.nItemUserAuthIV.setVisibility(View.GONE);
            } else {
                if ("1".equals(approveLabel)) {
                    holderRec.nItemUserAuthIV.setImageResource(R.drawable.authentication_bluev_icon);
                    holderRec.nItemUserAuthIV.setVisibility(View.VISIBLE);
                } else if ("1".equals(headplusv)) {
                    holderRec.nItemUserAuthIV.setImageResource(R.drawable.authentication_yellowv_icon);
                    holderRec.nItemUserAuthIV.setVisibility(View.VISIBLE);
                } else if ("1".equals(tarento)) {
                    holderRec.nItemUserAuthIV.setImageResource(R.drawable.authentication_star_icon);
                    holderRec.nItemUserAuthIV.setVisibility(View.VISIBLE);
                } else {
                    holderRec.nItemUserAuthIV.setVisibility(View.GONE);
                }
            }
        }

        switch (recomUserBean.link) {
            case 0: // no relations
            {
                holderRec.nItemFollowRL.setBackgroundResource(R.drawable.follow_button_border_normal);
                holderRec.nItemFollowTV.setText(mFragment.getString(R.string.str_follow));
                holderRec.nItemFollowTV.setTextColor(Color.parseColor("#0080ff"));
                Drawable drawable = mFragment.getActivity().getResources().getDrawable(R.drawable.icon_follow_normal);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holderRec.nItemFollowTV.setCompoundDrawables(drawable, null, null, null);
            }
            break;
            case 1: // followed
            {
                holderRec.nItemFollowRL.setBackgroundResource(R.drawable.follow_button_border_followed);
                holderRec.nItemFollowTV.setText(mFragment.getString(R.string.str_usercenter_header_attention_already_text));
                holderRec.nItemFollowTV.setTextColor(Color.parseColor("#ffffff"));
                Drawable drawable = mFragment.getActivity().getResources().getDrawable(R.drawable.icon_followed);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holderRec.nItemFollowTV.setCompoundDrawables(drawable, null, null, null);
            }
            break;
            case 2: // followed each other
            {
                holderRec.nItemFollowRL.setBackgroundResource(R.drawable.follow_button_border_mutual);
                holderRec.nItemFollowTV.setText(mFragment.getString(R.string.str_usercenter_header_attention_each_other_text));
                holderRec.nItemFollowTV.setTextColor(Color.parseColor("#ffffff"));
                Drawable drawable = mFragment.getActivity().getResources().getDrawable(R.drawable.icon_follow_mutual);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holderRec.nItemFollowTV.setCompoundDrawables(drawable, null, null, null);
            }
            break;
            case 3: // fans
                break;
            default:
                break;
        }

        final String linkuid = recomUserBean.uid;
        int relation = recomUserBean.link;

        final String type = relation == 0 ? "1" : "0";
        final FollowedRecomUserBean finalUserBean = recomUserBean;

        holderRec.nItemFollowTV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.sendFollowRequest(linkuid, type);
            }
        });

        holderRec.nItemUserAvatarIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GolukUtils.startUserCenterActivity(mFragment.getActivity(), finalUserBean.uid);
            }
        });

        holderRec.nItemNameTV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GolukUtils.startUserCenterActivity(mFragment.getActivity(), finalUserBean.uid);
            }
        });

        holderRec.nLableFollowTV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.sendFollowAllRequest(get2FollowLinkList(position));
            }
        });

        return convertView;
    }

    public String get2FollowLinkList(int index) {
        StringBuffer linkListBuffer = new StringBuffer();
        // to count the recommend number
        int count = 0;
        if (mList == null || mList.size() == 0) {
            return null;
        }

        for (; index < mList.size() && count < 5; index++) {
            Object obj = mList.get(index);
            if (obj instanceof FollowedRecomUserBean) {
                FollowedRecomUserBean bean = (FollowedRecomUserBean) obj;
                linkListBuffer.append(bean.uid + ",");
                count++;
            }
        }

        if (linkListBuffer.length() > 1) {
            linkListBuffer.deleteCharAt(linkListBuffer.length() - 1);
        }

        return linkListBuffer.toString();
    }

    private void showHead(ImageView view, String headportrait) {
        try {
            GlideUtils.loadLocalHead(mFragment.getActivity(), view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
        } catch (Exception e) {
            GlideUtils.loadLocalHead(mFragment.getActivity(), view, R.drawable.editor_head_feault7);
        }
    }

    private View getFollowedView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolderFollow holderFollow = null;
        if (null == convertView) {
            holderFollow = new ViewHolderFollow();
            convertView = LayoutInflater.from(mFragment.getActivity()).inflate(R.layout.newest_list_item, null);
            holderFollow.vDivider = convertView.findViewById(R.id.v_item_divider_line);
            holderFollow.videoImg = (ImageView) convertView.findViewById(R.id.imageLayout);
            holderFollow.liveImg = (ImageView) convertView.findViewById(R.id.newlist_item_liveicon);
            holderFollow.headimg = (ImageView) convertView.findViewById(R.id.headimg);
            holderFollow.nikename = (TextView) convertView.findViewById(R.id.nikename);
            holderFollow.timeLocation = (TextView) convertView.findViewById(R.id.time_location);
            holderFollow.function = (ImageView) convertView.findViewById(R.id.function);

            holderFollow.praiseText = (TextView) convertView.findViewById(R.id.tv_newest_list_item_praise);
            holderFollow.commentText = (TextView) convertView.findViewById(R.id.tv_newest_list_item_comment);
            holderFollow.shareText = (TextView) convertView.findViewById(R.id.tv_newest_list_item_share);

            holderFollow.surroundWatch = (TextView) convertView.findViewById(R.id.tv_newest_list_item_surround);
            holderFollow.totalcomments = (TextView) convertView.findViewById(R.id.totalcomments);
            holderFollow.detail = (TextView) convertView.findViewById(R.id.detail);
            holderFollow.ivReward = (ImageView) convertView.findViewById(R.id.iv_reward_tag);
            holderFollow.totlaCommentLayout = (LinearLayout) convertView.findViewById(R.id.totlaCommentLayout);
            holderFollow.comment1 = (TextView) convertView.findViewById(R.id.comment1);
            holderFollow.comment2 = (TextView) convertView.findViewById(R.id.comment2);
            holderFollow.comment3 = (TextView) convertView.findViewById(R.id.comment3);
            holderFollow.ivLogoVIP = (ImageView) convertView.findViewById(R.id.iv_vip_logo);
            holderFollow.rlUserInfo = (RelativeLayout) convertView.findViewById(R.id.rl_user_info);
            holderFollow.tvPraiseCount = (TextView) convertView.findViewById(R.id.tv_newest_list_item_praise_count);

            holderFollow.liveBackLayout = (LinearLayout) convertView.findViewById(R.id.ly_followed_live_back);
            holderFollow.liveBackTime = (TextView) convertView.findViewById(R.id.tv_followed_live_back_time);
            holderFollow.playBtn = (ImageView) convertView.findViewById(R.id.mPlayBigBtn);
            holderFollow.nTagsFL = (FlowLayout) convertView.findViewById(R.id.flowlayout_video_item_tags);

            int height = (int) ((float) width / widthHeight);
            RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
            mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
            holderFollow.videoImg.setLayoutParams(mPlayerLayoutParams);
            convertView.setTag(holderFollow);
        } else {
            holderFollow = (ViewHolderFollow) convertView.getTag();
        }

        if (position < 0 || position >= mList.size()) {
            return convertView;
        }

        FollowedVideoObjectBean videoObjectBean = (FollowedVideoObjectBean) mList.get(position);
        if (null == videoObjectBean) {
            return convertView;
        }

        if (0 == position) {
            holderFollow.vDivider.setVisibility(View.GONE);
        } else {
            holderFollow.vDivider.setVisibility(View.VISIBLE);
        }

        if (null != videoObjectBean.video && !TextUtils.isEmpty(videoObjectBean.video.picture)) {
            GlideUtils.loadImage(mFragment.getActivity(), mFragment,
                    holderFollow.videoImg, videoObjectBean.video.picture, R.drawable.tacitly_pic);
        }

        if (null != videoObjectBean.user && null != videoObjectBean.user.label) {
            String approveLabel = videoObjectBean.user.label.approvelabel;
            String approve = videoObjectBean.user.label.approve;
            String tarento = videoObjectBean.user.label.tarento;
            String headplusv = videoObjectBean.user.label.headplusv;
            String headplusvdes = videoObjectBean.user.label.headplusvdes;
            if (null == approveLabel && null == approve &&
                    null == tarento && null == headplusv && null == headplusvdes) {
                holderFollow.ivLogoVIP.setVisibility(View.GONE);
            } else {
                if ("1".equals(approveLabel)) {
                    holderFollow.ivLogoVIP.setImageResource(R.drawable.authentication_bluev_icon);
                    holderFollow.ivLogoVIP.setVisibility(View.VISIBLE);
                } else if ("1".equals(headplusv)) {
                    holderFollow.ivLogoVIP.setImageResource(R.drawable.authentication_yellowv_icon);
                    holderFollow.ivLogoVIP.setVisibility(View.VISIBLE);
                } else if ("1".equals(tarento)) {
                    holderFollow.ivLogoVIP.setImageResource(R.drawable.authentication_star_icon);
                    holderFollow.ivLogoVIP.setVisibility(View.VISIBLE);
                } else {
                    holderFollow.ivLogoVIP.setVisibility(View.GONE);
                }
            }
        }

        if (null != videoObjectBean.user) {
            String headUrl = videoObjectBean.user.customavatar;
            if (null != headUrl && !"".equals(headUrl)) {
                // 使用服务器头像地址
                GlideUtils.loadNetHead(mFragment.getActivity(), holderFollow.headimg, headUrl, R.drawable.editor_head_feault7);
            } else {
                showHead(holderFollow.headimg, videoObjectBean.user.headportrait);
            }
            holderFollow.nikename.setText(videoObjectBean.user.nickname);
        }

        if (videoObjectBean.video != null) {
            holderFollow.timeLocation.setText(
                    GolukUtils.getCommentShowFormatTime(mFragment.getActivity(),
                            videoObjectBean.video.sharingts) + " " + videoObjectBean.video.location);

            if (null != videoObjectBean.video.gen) {
                String recommend = videoObjectBean.video.gen.isrecommend;
                if (null != recommend && "1".equals(recommend)) {
                    Drawable drawable = mFragment.getActivity().getResources().getDrawable(R.drawable.together_recommend_icon);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    holderFollow.timeLocation.setCompoundDrawables(null, null, drawable, null);
                } else {
                    holderFollow.timeLocation.setCompoundDrawables(null, null, null, null);
                }

                String reward = videoObjectBean.video.gen.isreward;
                String sysflag = videoObjectBean.video.gen.sysflag;
                if (null != reward && "1".equals(reward) && null != sysflag && "1".equals(sysflag)) {
                    holderFollow.ivReward.setVisibility(View.VISIBLE);
                } else {
                    holderFollow.ivReward.setVisibility(View.GONE);
                }
            } else {
                holderFollow.timeLocation.setCompoundDrawables(null, null, null, null);
                holderFollow.ivReward.setVisibility(View.GONE);
            }

            if ("0".equals(videoObjectBean.video.ispraise)) {
                holderFollow.praiseText.setTextColor(Color.rgb(0x88, 0x88, 0x88));
                Drawable drawable = mFragment.getActivity().getResources().getDrawable(R.drawable.videodetail_like);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holderFollow.praiseText.setCompoundDrawables(drawable, null, null, null);
            } else {
                holderFollow.praiseText.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
                Drawable drawable = mFragment.getActivity().getResources().getDrawable(R.drawable.videodetail_like_press);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holderFollow.praiseText.setCompoundDrawables(drawable, null, null, null);
            }

            if ("-1".equals(videoObjectBean.video.praisenumber)) {
                holderFollow.tvPraiseCount.setText(mFragment.getActivity().getString(R.string.str_usercenter_praise));
            } else {
                holderFollow.tvPraiseCount.setText(
                        GolukUtils.getFormatNumber(videoObjectBean.video.praisenumber) +
                                mFragment.getActivity().getString(R.string.str_usercenter_praise));
            }
            if ("-1".equals(videoObjectBean.video.clicknumber)) {
                holderFollow.surroundWatch.setText("");
                holderFollow.surroundWatch.setVisibility(View.GONE);
            } else {
                holderFollow.surroundWatch.setVisibility(View.VISIBLE);
                holderFollow.surroundWatch.setText(GolukUtils.getFormatNumber(
                        videoObjectBean.video.clicknumber));
            }

            if (TextUtils.isEmpty(videoObjectBean.video.describe)) {
                holderFollow.detail.setVisibility(View.GONE);
            } else {
                holderFollow.detail.setVisibility(View.VISIBLE);
                UserUtils.showCommentText(holderFollow.detail, videoObjectBean.user.nickname,
                        videoObjectBean.video.describe);
            }

            if(null != videoObjectBean.video.tags) {
                GolukUtils.addTagsViews(mFragment.getActivity(), videoObjectBean.video.tags, holderFollow.nTagsFL);
            } else {
                holderFollow.nTagsFL.setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(videoObjectBean.video.type) || !"1".equals(videoObjectBean.video.type)) {
                holderFollow.liveBackLayout.setVisibility(View.GONE);
                holderFollow.playBtn.setVisibility(View.VISIBLE);
            } else {
                if (TextUtils.isEmpty(videoObjectBean.video.livetime) || "".equals(videoObjectBean.video.livetime)) {
                    holderFollow.liveBackTime.setText(mFragment.getActivity().getString(R.string.str_live_time, "00:00:00"));
                } else {
                    holderFollow.liveBackTime.setText(mFragment.getActivity().getString(R.string.str_live_time,
                            GolukUtils.secondToString(Integer.valueOf(videoObjectBean.video.livetime))));
                }
                holderFollow.liveBackLayout.setVisibility(View.VISIBLE);
                holderFollow.playBtn.setVisibility(View.GONE);
            }
        }

        holderFollow.liveImg.setVisibility(View.GONE);
        holderFollow.commentText.setVisibility(View.VISIBLE);
        holderFollow.surroundWatch.setVisibility(View.VISIBLE);

        if (null != videoObjectBean.video && null != videoObjectBean.video.comment &&
                "1".equals(videoObjectBean.video.comment.iscomment)) {
            List<FollowedComListBean> comments = videoObjectBean.video.comment.comlist;
            if (null != comments && comments.size() > 0) {
                int comcount = Integer.parseInt(videoObjectBean.video.comment.comcount);
                if (comcount <= 3) {
                    holderFollow.totalcomments.setVisibility(View.GONE);
                } else {
                    holderFollow.totalcomments.setVisibility(View.VISIBLE);
                    holderFollow.totalcomments.setText(mFragment.getActivity().getString(R.string.str_see_comments, GolukUtils
                            .getFormatNumber(videoObjectBean.video.comment.comcount)));
                }

                holderFollow.totlaCommentLayout.setVisibility(View.VISIBLE);
                holderFollow.totalcomments
                        .setOnClickListener(new VideoItemCommentClickListener(mFragment.getActivity(), videoObjectBean, false));
                holderFollow.totlaCommentLayout
                        .setOnClickListener(new VideoItemCommentClickListener(mFragment.getActivity(), videoObjectBean, false));
                holderFollow.comment1.setVisibility(View.VISIBLE);
                holderFollow.comment2.setVisibility(View.VISIBLE);
                holderFollow.comment3.setVisibility(View.VISIBLE);

                if (1 == comments.size()) {
                    if (null != comments.get(0).replyid && !"".equals(comments.get(0).replyid)
                            && null != comments.get(0).replyname && !"".equals(comments.get(0).replyname)) {
                        showReplyText(holderFollow.comment1, comments.get(0).name,
                                comments.get(0).replyname, comments.get(0).text);
                    } else {
                        UserUtils.showCommentText(holderFollow.comment1,
                                comments.get(0).name, comments.get(0).text);
                    }

                    holderFollow.comment2.setVisibility(View.GONE);
                    holderFollow.comment3.setVisibility(View.GONE);
                } else if (2 == comments.size()) {
                    if (null != comments.get(0).replyid && !"".equals(comments.get(0).replyid)
                            && null != comments.get(0).replyname && !"".equals(comments.get(0).replyname)) {
                        showReplyText(holderFollow.comment1, comments.get(0).name,
                                comments.get(0).replyname, comments.get(0).text);
                    } else {
                        UserUtils.showCommentText(holderFollow.comment1,
                                comments.get(0).name, comments.get(0).text);
                    }
                    if (null != comments.get(1).replyid && !"".equals(comments.get(1).replyid)
                            && null != comments.get(1).replyname && !"".equals(comments.get(1).replyname)) {
                        showReplyText(holderFollow.comment2, comments.get(1).name,
                                comments.get(1).replyname, comments.get(1).text);
                    } else {
                        UserUtils.showCommentText(holderFollow.comment2,
                                comments.get(1).name, comments.get(1).text);
                    }
                    holderFollow.comment3.setVisibility(View.GONE);
                } else if (3 == comments.size()) {
                    if (null != comments.get(0).replyid && !"".equals(comments.get(0).replyid)
                            && null != comments.get(0).replyname && !"".equals(comments.get(0).replyname)) {
                        showReplyText(holderFollow.comment1, comments.get(0).name,
                                comments.get(0).replyname, comments.get(0).text);
                    } else {
                        UserUtils.showCommentText(holderFollow.comment1,
                                comments.get(0).name, comments.get(0).text);
                    }

                    if (null != comments.get(1).replyid && !"".equals(comments.get(1).replyid)
                            && null != comments.get(1).replyname && !"".equals(comments.get(1).replyname)) {
                        showReplyText(holderFollow.comment2, comments.get(1).name,
                                comments.get(1).replyname, comments.get(1).text);
                    } else {
                        UserUtils.showCommentText(holderFollow.comment2,
                                comments.get(1).name, comments.get(1).text);
                    }
                    if (null != comments.get(2).replyid && !"".equals(comments.get(2).replyid)
                            && null != comments.get(2).replyname && !"".equals(comments.get(2).replyname)) {
                        showReplyText(holderFollow.comment3, comments.get(2).name,
                                comments.get(2).replyname, comments.get(2).text);
                    } else {
                        UserUtils.showCommentText(holderFollow.comment3,
                                comments.get(2).name, comments.get(2).text);
                    }
                }
            } else {
                holderFollow.totalcomments.setVisibility(View.GONE);
                holderFollow.totlaCommentLayout.setVisibility(View.GONE);
            }
        } else {
            holderFollow.totalcomments.setVisibility(View.GONE);
            holderFollow.totlaCommentLayout.setVisibility(View.GONE);
        }
        initFollowedListener(position, holderFollow);
        return convertView;
    }

    private void initFollowedListener(final int index, ViewHolderFollow viewHolder) {
        if (index < 0 || index >= mList.size()) {
            return;
        }

        final FollowedVideoObjectBean videoObjectBean = (FollowedVideoObjectBean) mList.get(index);

        if (null == videoObjectBean || videoObjectBean.video == null) {
            return;
        }
        // 分享监听
        final String videoId = videoObjectBean.video.videoid;
        final String type = videoObjectBean.video.type;
        viewHolder.shareText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.sendGetShareVideoUrlRequest(videoId, type);
                mFragment.storeCurrentIndex(index);
            }
        });

        // 举报监听
        viewHolder.function.setOnClickListener(
                new VideoItemFunctionClickListener(mFragment.getActivity(), videoObjectBean, false, null));
        // 评论监听
        viewHolder.commentText.setOnClickListener(
                new VideoItemCommentClickListener(mFragment.getActivity(), videoObjectBean, true));
        // 播放区域监听
        viewHolder.videoImg.setOnClickListener(new VideoItemClickPlayListener(mFragment.getActivity(), videoObjectBean));
        viewHolder.headimg.setOnClickListener(new VideoItemHeadClickListener(mFragment.getActivity(), videoObjectBean));
        // 点赞
        viewHolder.praiseText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GolukUtils.isFastDoubleClick()) {
                    return;
                }
                if ("1".equals(videoObjectBean.video.ispraise)) {
                    mFragment.sendCancelPraiseRequest(videoId);
                } else {
                    mFragment.sendPraiseRequest(videoId, "1");
                }
                mFragment.storeCurrentIndex(index);
            }
        });
        // 评论总数监听
        List<FollowedComListBean> comments = videoObjectBean.video.comment.comlist;
        if (comments.size() > 0) {
            viewHolder.totalcomments.setOnClickListener(
                    new VideoItemCommentClickListener(mFragment.getActivity(), videoObjectBean, false));
            viewHolder.totlaCommentLayout.setOnClickListener(
                    new VideoItemCommentClickListener(mFragment.getActivity(), videoObjectBean, false));
        }

        final FollowedVideoObjectBean vsInfo = videoObjectBean;

        viewHolder.rlUserInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != vsInfo && null != vsInfo.user) {
                    GolukUtils.startUserCenterActivity(mFragment.getActivity(), vsInfo.user.uid);
                }
            }
        });
    }

    private void showReplyText(TextView view, String nikename, String replyName, String text) {
        String replyLabel = mFragment.getString(R.string.str_reply);
        String replyText = "@" + replyName + mFragment.getString(R.string.str_colon);
        String str = nikename + " " + replyLabel + replyText + text;
        SpannableStringBuilder style = new SpannableStringBuilder(str);

        style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)),
                nikename.length() + 1 + replyLabel.length(),
                nikename.length() + replyLabel.length() + replyText.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(style);
    }

    static class ViewHolderRecommend {
        View nDividerV;
        View nLabelRL;
        TextView nLabelTitleTV;
        TextView nLableFollowTV;
        View nItemUserRL;
        ImageView nItemUserAvatarIV;
        ImageView nItemUserAuthIV;
        TextView nItemNameTV;
        TextView nItemContentTV;
        TextView nItemFollowTV;
        ImageView nItemFirstVideoIV;
        TextView nItemFirstVideoDesTV;
        TextView nItemFirstVideoComTV;
        TextView nItemFirstVideoViewTV;
        ImageView nItemSecondVideoIV;
        TextView nItemSecondVideoDesTV;
        TextView nItemSecondVideoComTV;
        TextView nItemSecondVideoViewTV;
        View nLabelFollowAllRL;
        View nItemFollowRL;
        View nItemFirstVideoRL;
        View nItemSecondVideoRL;
    }

    static class ViewHolderFollow {
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
        TextView totalcomments;

        LinearLayout totlaCommentLayout;
        TextView comment1;
        TextView comment2;
        TextView comment3;
        ImageView ivReward;
        View vDivider;
        TextView tvPraiseCount;

        RelativeLayout rlUserInfo;

        LinearLayout liveBackLayout;
        TextView liveBackTime;
        ImageView playBtn;
        FlowLayout nTagsFL;
    }

    static class ViewHolderEmpty {
        TextView nEmptyTV;
    }
}

