package com.mobnote.golukmain.cluster.bean;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.golukmain.msg.SystemMsgAdapter;
import com.mobnote.golukmain.msg.bean.MessageMsgsBean;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.usercenter.NewUserCenterActivity;
import com.mobnote.golukmain.usercenter.UCUserInfo;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;

import java.util.List;

/**
 * Created by hanzheng on 2016/5/13.
 */
public class RankingAdapter extends BaseAdapter{
    private Context mContext;

    public List<RankingListVideo> mRandkingList;

    public RankingAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<RankingListVideo> list) {
        mRandkingList = list;
    }

    @Override
    public int getCount() {
        if (mRandkingList == null || mRandkingList.size() == 0){
            return 0;
        }
        return mRandkingList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RankingListVideo video = mRandkingList.get(position);

        HolderView holderView = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ranking_list_item, null);
            holderView = new HolderView();
            holderView.videoImg = (ImageView) convertView.findViewById(R.id.video_img);
            holderView.numberImg = (ImageView) convertView.findViewById(R.id.number_img);
            holderView.numberIndex = (TextView) convertView.findViewById(R.id.number_index);
            holderView.popularity = (TextView) convertView.findViewById(R.id.popularity);
            holderView.userAuthentication = (ImageView) convertView.findViewById(R.id.im_user_head_authentication);
            holderView.userHead = (ImageView) convertView.findViewById(R.id.user_head);
            holderView.userIntroductionTxt = (TextView) convertView.findViewById(R.id.user_introduction_text);
            holderView.userNameText = (TextView) convertView.findViewById(R.id.user_name_text);
            holderView.imgLayout = (RelativeLayout) convertView.findViewById(R.id.img_layout);
            holderView.userinfoLayout = (RelativeLayout) convertView.findViewById(R.id.userinfo_layout);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }

        holderView.imgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,VideoDetailActivity.class);
                intent.putExtra("videoid",video.videoid);
                mContext.startActivity(intent);
            }
        });
        holderView.userinfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,VideoDetailActivity.class);
                intent.putExtra("videoid",video.videoid);
                mContext.startActivity(intent);
            }
        });
        GlideUtils.loadImage(mContext,holderView.videoImg,video.pictureurl,-1);
        holderView.numberIndex.setText(position + 1 + "");
        holderView.popularity.setText("人气值:" + video.rank);
        holderView.userNameText.setText(video.user.nickname);
        if(position == 0){
            holderView.numberImg.setImageResource(R.drawable.icon__one);
            holderView.numberIndex.setTextSize(20);
        }else if(position == 1){
            holderView.numberImg.setImageResource(R.drawable.icon__three);
            holderView.numberIndex.setTextSize(20);
        }else if (position == 2){
            holderView.numberImg.setImageResource(R.drawable.icon__two);
            holderView.numberIndex.setTextSize(20);
        }else{
            holderView.numberImg.setImageResource(R.drawable.icon__four);
            holderView.numberIndex.setTextSize(10);
        }

        if (video.user.customavatar != null && !"".equals(video.user.customavatar)) {
            holderView.userHead.setImageURI(Uri.parse(video.user.customavatar));
            GlideUtils.loadNetHead(mContext, holderView.userHead, video.user.customavatar,
                    R.drawable.editor_head_feault7);
        } else {
            showHead(holderView.userHead, video.user.avatar);
        }

        if (null != video.user.certification) {
            holderView.userAuthentication.setVisibility(View.VISIBLE);
            if ("1".equals(video.user.certification.isorgcertificated)) {
                holderView.userAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
            } else if ("1".equals(video.user.certification.isusercertificated)) {
                holderView.userAuthentication
                        .setImageResource(R.drawable.authentication_yellowv_icon);
            } else if ("1".equals(video.user.certification.isstar)) {
                holderView.userAuthentication.setImageResource(R.drawable.authentication_star_icon);
            } else {
                holderView.userAuthentication.setVisibility(View.GONE);
            }
        } else {
            holderView.userAuthentication.setVisibility(View.GONE);
        }

        holderView.userIntroductionTxt.setText(video.user.introduction);
        return convertView;
    }

    private void showHead(ImageView view, String headportrait) {
        try {
            GlideUtils.loadLocalHead(mContext, view,
                    ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
        } catch (Exception e) {
            GlideUtils.loadLocalHead(mContext, view,
                    R.drawable.usercenter_head_default);
        }
    }

    public class HolderView{
        private ImageView videoImg;
        private ImageView numberImg;
        private TextView numberIndex;
        private TextView popularity;
        private ImageView userHead;
        private ImageView userAuthentication;
        private TextView userNameText;
        private TextView userIntroductionTxt;
        private RelativeLayout imgLayout;
        private RelativeLayout userinfoLayout;
    }
}
