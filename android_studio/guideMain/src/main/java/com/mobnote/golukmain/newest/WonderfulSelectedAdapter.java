package com.mobnote.golukmain.newest;

import java.util.ArrayList;
import java.util.List;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.util.Utils;
import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.view.SlideShowView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.tiros.debug.GolukDebugUtils;


public class WonderfulSelectedAdapter extends BaseAdapter {
    private Context mContext = null;
    //	private List<JXListItemDataInfo> mDataList = null;
    private List<Object> mDataList = null;
    private int count = 0;
    private int width = 0;
 //   private Typeface mTypeface = null;
    private final static int BANNER_ITEM = 0;
    private final static int VIDEO_ITEM = 1;
    private BannerDataModel mBannerData = null;
    private final static String FAKE_CONTENT = "fake";

    private final static String PURE_PIC = "0";
    private final static String VIDEO_DETAIL = "1";
    private final static String SPECIAL_LIST = "2";
    private final static String LIVE_VIDEO = "3";
    private final static String ACTIVITY_TOGETHER = "4";
    private final static String H5_PAGE = "5";
    private final static String SPECIAL_SOLO = "6";
    private final static String TAG_PAGE = "10";
    private final static String TAG = "WonderfulSelectedAdapter";

    public WonderfulSelectedAdapter(Context context) {
        mContext = context;
        mDataList = new ArrayList<Object>();
        width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
 //       mTypeface = Typeface//Typeface.createFromAsset(context.getAssets(), "AdobeHebrew-Bold.otf");
    }

    public void setBannerData(BannerDataModel model) {
        mBannerData = model;
        if (mDataList.size() == 0) {
            mDataList.add(model);
        } else {
            mDataList.set(0, model);
        }
        notifyDataSetChanged();
    }

    // add fake banner data
    public void setData(List<JXListItemDataInfo> data) {
        mDataList.clear();

        if (null != mBannerData) {
            mDataList.add(mBannerData);
        } else {
            BannerDataModel model = new BannerDataModel();
            model.setResult(FAKE_CONTENT);
            mDataList.add(new BannerDataModel());
        }
        mDataList.addAll(data);
        count = mDataList.size();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override

    public int getItemViewType(int position) {
        if (position == 0) {
            return BANNER_ITEM;
        } else {
            return VIDEO_ITEM;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        ViewHolderBanner bannerHolder = null;
        int height = (int) ((float) width / 1.78f);

        if (convertView == null) {
            if (getItemViewType(position) == VIDEO_ITEM) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.wonderful_selected_item, null);
                holder = new ViewHolder();
                holder.main = (RelativeLayout) convertView
                        .findViewById(R.id.main);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
                holder.videoImg = (ImageView) convertView
                        .findViewById(R.id.simpledrawee);
                holder.icon = (ImageView) convertView
                        .findViewById(R.id.wonderful_icon);
                holder.mTitleName = (TextView) convertView
                        .findViewById(R.id.mTitleName);
//				holder.mTagName = (TextView) convertView
//						.findViewById(R.id.mTagName);
//				holder.mVideoLayout = (LinearLayout) convertView
//						.findViewById(R.id.mVideoLayout);
//				holder.mLookLayout = (LinearLayout) convertView
//						.findViewById(R.id.mLookLayout);
                holder.mVideoNum = (TextView) convertView
                        .findViewById(R.id.mVideoNum);
//                holder.mLookNum = (TextView) convertView
//                        .findViewById(R.id.mLookNum);

                RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height);
                holder.videoImg.setLayoutParams(mPreLoadingParams);
                convertView.setTag(holder);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.wonderful_banner_item, null);
                bannerHolder = new ViewHolderBanner();
                bannerHolder.mBannerSlide = (SlideShowView) convertView.findViewById(R.id.ssv_wonderful_banner);
                LinearLayout.LayoutParams mPreLoadingParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, /*height*/(int) ((float) width / 2.67f));
                bannerHolder.mBannerSlide.setLayoutParams(mPreLoadingParams);
                bannerHolder.mTextBanner1 = (TextView) convertView.findViewById(R.id.tv_text_banner1);
                bannerHolder.mTextBanner2 = (TextView) convertView.findViewById(R.id.tv_text_banner2);
                bannerHolder.mTextBannerLL = (LinearLayout) convertView.findViewById(R.id.ll_text_banner);
//	            bannerHolder.mReFreshText = (TextView) convertView.findViewById(R.id.refresh_msg);
                convertView.setTag(bannerHolder);
            }
        } else {
            if (getItemViewType(position) == VIDEO_ITEM) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                bannerHolder = (ViewHolderBanner) convertView.getTag();
            }
        }

        if (getItemViewType(position) == VIDEO_ITEM) {
            JXListItemDataInfo info = (JXListItemDataInfo) mDataList.get(position);
            holder.mTitleName.setText(getTitleString(info.ztitle));
//			holder.mTagName.setVisibility(View.GONE);
//			if ("-1".equals(info.clicknumber)) {
//				holder.mVideoLayout.setVisibility(View.GONE);
//			} else {
//				holder.mVideoNum.setText(GolukUtils.getFormatNumber(info.clicknumber));
//				holder.mVideoLayout.setVisibility(View.VISIBLE);
//			}
            if ("-1".equals(info.clicknumber)) {
                holder.mVideoNum.setVisibility(View.GONE);
            } else {
                holder.mVideoNum.setVisibility(View.VISIBLE);
                holder.mVideoNum.setText(GolukUtils.getFormatNumber(info.clicknumber));
            }

//			if ("-1".equals(info.videonumber)) {
//				holder.mLookLayout.setVisibility(View.GONE);
//			} else {
//				holder.mLookNum.setText(GolukUtils.getFormatNumber(info.videonumber));
//				holder.mLookLayout.setVisibility(View.VISIBLE);
//			}

            if (!TextUtils.isEmpty(info.jxdate)) {
                if (0 == position) {
                    holder.mDate.setVisibility(View.GONE);
                } else {
                    String phoneDate = Utils.getDateStr(System.currentTimeMillis());
                    if (phoneDate.trim().equals(info.jxdate.trim())) {
 //                       holder.mDate.setTypeface(mTypeface);
                        holder.mDate.setText(mContext.getString(R.string.str_jx_today));
                        holder.mDate.setVisibility(View.VISIBLE);
                    } else {
                        if (position != 1) {
 //                           holder.mDate.setTypeface(mTypeface);
                            holder.mDate.setText(GolukUtils.getTime(info.jxdate));
                            holder.mDate.setVisibility(View.VISIBLE);
                        } else {
 //                           holder.mDate.setTypeface(mTypeface);
                            holder.mDate.setText(mContext.getString(R.string.str_jx_other_day));
                            holder.mDate.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } else {
                holder.mDate.setVisibility(View.GONE);
            }
            holder.main.setOnTouchListener(new ClickWonderfulSelectedListener(mContext, info, this));
            loadImage(holder.videoImg, holder.icon, info.jximg, info.jtypeimg);
        } else {
            BannerDataModel model = (BannerDataModel) mDataList.get(position);
            if (null == model || null == model.getSlides()) {
                showDefaultImage(bannerHolder.mBannerSlide);
                bannerHolder.mTextBannerLL.setVisibility(View.GONE);
            } else {
                if (null != model && "0".equals(model.getResult())) {
                    List<BannerSlideBody> slidesList = model.getSlides();
                    if (slidesList != null && slidesList.size() > 0) {
                        // exceed 10 images
                        if (slidesList.size() > 10) {
                            while (slidesList.size() > 10) {
                                slidesList.remove(slidesList.size() - 1);
                            }
                        }
                        for (int i = 1; i <= slidesList.size(); i++) {
                            slidesList.get(i-1).setIndex(i);
                        }
                        bannerHolder.mBannerSlide.clearImages();
                        bannerHolder.mBannerSlide.setImageDataList(slidesList);
                    } else {
                        showDefaultImage(bannerHolder.mBannerSlide);
                    }
                } else {
                    //default pic
                    showDefaultImage(bannerHolder.mBannerSlide);
                }
            }

            if (null != model && null != model.getTexts()) {
                List<BannerTextBody> bannerTextList = model.getTexts();
                if (null != bannerTextList && bannerTextList.size() >= 2) {
                    bannerHolder.mTextBannerLL.setVisibility(View.VISIBLE);
                    final BannerTextBody body0 = bannerTextList.get(0);
                    if (null != body0) {
                        bannerHolder.mTextBanner1.setText(body0.getTitle());
                        if (null != body0.getColor() && body0.getColor().startsWith("#")) {
                            bannerHolder.mTextBanner1.setTextColor(Color.parseColor(body0.getColor()));
                        }
                        bannerHolder.mTextBanner1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startTextBannerDetail(body0);
                            }
                        });
                    }

                    final BannerTextBody body1 = bannerTextList.get(1);
                    if (null != body1) {
                        bannerHolder.mTextBanner2.setText(body1.getTitle());
                        if (null != body1.getColor() && body1.getColor().startsWith("#")) {
                            bannerHolder.mTextBanner2.setTextColor(Color.parseColor(body1.getColor()));
                        }
                        bannerHolder.mTextBanner2.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startTextBannerDetail(body1);
                            }
                        });
                    }

                    final Animation slideDown = AnimationUtils.loadAnimation(
                            mContext, R.anim.anim_slide_down);
                    bannerHolder.mTextBannerLL.startAnimation(slideDown);
                } else {
                    final Animation slideUp = AnimationUtils.loadAnimation(
                            mContext, R.anim.anim_slide_up);
                    bannerHolder.mTextBannerLL.startAnimation(slideUp);
                    final LinearLayout tempLL = bannerHolder.mTextBannerLL;
                    slideUp.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tempLL.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            }
        }
        return convertView;
    }

    private void showDefaultImage(SlideShowView slideView) {
        slideView.clearImages();
        BannerSlideBody body = new BannerSlideBody();
        body.setPicture(FAKE_CONTENT);
        body.setIndex(1);
        List<BannerSlideBody> bodyList = new ArrayList<BannerSlideBody>();
        bodyList.add(body);
        slideView.setImageDataList(bodyList);
    }

    private String getTitleString(String title) {
        String name = "";
        int len = title.length();
        if (len > 15) {
            int size = len / 15 + 1;
            for (int i = 0; i < size; i++) {
                int index = 15 * (i + 1);
                if (index < len) {
                    name += title.substring(15 * i, index) + "\n";
                } else {
                    name += title.substring(15 * i);
                }
            }
        } else {
            name = title;
        }

        return name;
    }

    public void startTextBannerDetail(BannerTextBody body) {
        if (null == body) {
            return;
        }

        String type = body.getType();
        if (null == type || type.trim().equals("")) {
            return;
        }

        Intent intent = null;
        ZhugeUtils.eventBannerText(mContext, body.getTitle());

        if (PURE_PIC.equals(type)) {
            // do nothing
            GolukDebugUtils.d(TAG, "pure picture clicked");
        } else if (VIDEO_DETAIL.equals(type)) {
            // launch video detail
            String accessId = body.getAccess();
            if (null == accessId || accessId.trim().equals("")) {
                return;
            } else {
                //视频详情页访问
                ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_share_video_network_other));
                intent = new Intent(mContext, VideoDetailActivity.class);
                intent.putExtra(VideoDetailActivity.VIDEO_ID, body.getAccess());
                intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
                mContext.startActivity(intent);
            }
        } else if (SPECIAL_LIST.equals(type)) {
            // launch special list
            String accessId = body.getAccess();
            if (null == accessId || accessId.trim().equals("")) {
                return;
            } else {
                intent = new Intent(mContext, SpecialListActivity.class);
                intent.putExtra("ztid", body.getAccess());
                intent.putExtra("title", body.getTitle());
                mContext.startActivity(intent);
            }
        } else if (LIVE_VIDEO.equals(type)) {
            // TODO: This should proceed in future
            // intent = new Intent(mContext, LiveActivity.class);
            // intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
            // intent.putExtra(LiveActivity.KEY_GROUPID, "");
            // intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
            // intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
            // intent.putExtra(LiveActivity.KEY_USERINFO, user);
            // mContext.startActivity(intent);
        } else if (ACTIVITY_TOGETHER.equals(type)) {
            // launch topic
            String accessId = body.getAccess();
            if (null == accessId || accessId.trim().equals("")) {
                return;
            } else {
                intent = new Intent(mContext, ClusterActivity.class);
                intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID,
                        body.getAccess());
                // intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
                String topName = "#" + body.getTitle();
                intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, topName);
                mContext.startActivity(intent);
            }
        } else if (H5_PAGE.equals(type)) {
            // launch h5 page
            String accessId = body.getAccess();
            if (null == accessId || accessId.trim().equals("")) {
                return;
            } else {
                String url = body.getAccess();
                intent = new Intent(mContext, UserOpenUrlActivity.class);
                intent.putExtra(GolukConfig.WEB_TYPE, GolukConfig.NEED_SHARE);
                intent.putExtra(GolukConfig.H5_URL, url);
                intent.putExtra(GolukConfig.URL_OPEN_PATH, "text_banner");
                if (null != body && !body.getTitle().equals("")) {
                    intent.putExtra(GolukConfig.NEED_H5_TITLE, body.getTitle());
                }
                mContext.startActivity(intent);
            }
        } else if (SPECIAL_SOLO.equals(type)) {
            String accessId = body.getAccess();
            if (null == accessId || accessId.trim().equals("")) {
                return;
            } else {
                //视频详情页访问
                ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_share_video_network_other));

                intent = new Intent(mContext, VideoDetailActivity.class);
                // intent.putExtra("imageurl", body.getPicture());
                intent.putExtra("ztid", body.getAccess());
                intent.putExtra("title", body.getTitle());
                mContext.startActivity(intent);
            }
        } else if (TAG_PAGE.equals(type)) {
            // launch topic
            String accessId = body.getAccess();
            if (null == accessId || accessId.trim().equals("")) {
                return;
            } else {
                intent = new Intent(mContext, ClusterActivity.class);
                intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID,
                        body.getAccess());
                // intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
                String topName = "#" + body.getTitle();
                intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, topName);
                mContext.startActivity(intent);
            }
        }
    }

    private void loadImage(ImageView mPlayerLayout, ImageView iconView, String url, String iconUrl) {
        GlideUtils.loadImage(mContext, mPlayerLayout, url, R.drawable.tacitly_pic);
        if (TextUtils.isEmpty(iconUrl)) {
            iconView.setVisibility(View.GONE);
        } else {
            iconView.setVisibility(View.VISIBLE);
            GlideUtils.loadImage(mContext, iconView, iconUrl, -1);
        }
    }

    public static class ViewHolder {
        RelativeLayout main;
        ImageView videoImg;
        ImageView icon;
        TextView mTitleName;
        //		TextView mTagName;
//		LinearLayout mVideoLayout;
//		LinearLayout mLookLayout;
        TextView mVideoNum;
//        TextView mLookNum;
        TextView mDate;
    }

    static class ViewHolderBanner {
        SlideShowView mBannerSlide;
        TextView mTextBanner1;
        TextView mTextBanner2;
        LinearLayout mTextBannerLL;
//		TextView mReFreshText;
    }
}
