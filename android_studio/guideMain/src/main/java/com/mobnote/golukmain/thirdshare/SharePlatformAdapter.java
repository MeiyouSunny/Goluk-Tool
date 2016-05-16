package com.mobnote.golukmain.thirdshare;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.thirdshare.bean.SharePlatformBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leege100 on 16/5/13.
 */
public class SharePlatformAdapter extends RecyclerView.Adapter{

    Context mContext;
    List<SharePlatformBean> mSharePlatformBeanList;
    /** 当前选中的平台 */
    int mCurrSelectedPlatform;

    public SharePlatformAdapter(Context context){

        this.mContext = context;
        mCurrSelectedPlatform = SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE;
        fillList();
    }

    @Override
    public SharePlatformViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SharePlatformViewHolder holder = new SharePlatformViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_share_platform, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof SharePlatformViewHolder){
            SharePlatformViewHolder sharePlatformViewHolder = (SharePlatformViewHolder) holder;
            sharePlatformViewHolder.render(position);
        }
    }

    @Override
    public int getItemCount() {
        return mSharePlatformBeanList != null ? mSharePlatformBeanList.size() : 0;
    }
    private void fillList(){
        mSharePlatformBeanList = new ArrayList<SharePlatformBean>();
        if(GolukApplication.getInstance().isInteral()){
            //国内版，国内平台显示在前面，国外平台在后面
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEXIN));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEIBO_SINA));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_QQ));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_QQ_ZONE));

            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_FACEBOOK));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_TWITTER));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_INSTAGRAM));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WHATSAPP));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_LINE));
        }else{
            //国外版，则国外平台显示在前，国内的在后
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_FACEBOOK));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_TWITTER));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_INSTAGRAM));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WHATSAPP));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_LINE));

            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEXIN));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEIBO_SINA));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_QQ));
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_QQ_ZONE));
        }
    }

    private class SharePlatformOnClickListener implements View.OnClickListener{

        private int position;
        public SharePlatformOnClickListener(int p){
            this.position = p;
        }

        @Override
        public void onClick(View view) {
            if(mSharePlatformBeanList != null && mSharePlatformBeanList.size() > position){
                SharePlatformBean tempBean = mSharePlatformBeanList.get(position);
                if(tempBean != null){

                    if(tempBean.getPlatformType() != mCurrSelectedPlatform){
                        mCurrSelectedPlatform = tempBean.getPlatformType();
                    }else{
                        mCurrSelectedPlatform = SharePlatformBean.SHARE_PLATFORM_NULL;
                    }
                    SharePlatformAdapter.this.notifyDataSetChanged();
                }
            }
        }
    }

    class SharePlatformViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlatformName;
        ImageView ivPlatformIcon;

        public SharePlatformViewHolder(View view) {
            super(view);
            tvPlatformName = (TextView) view.findViewById(R.id.tv_item_share_platform_name);
            ivPlatformIcon = (ImageView) view.findViewById(R.id.iv_item_share_platform_icon);
        }
        public void render(int position){
            if(mSharePlatformBeanList != null && mSharePlatformBeanList.size() > position){
                SharePlatformBean tempBean = mSharePlatformBeanList.get(position);
                if(tempBean != null){
                    tvPlatformName.setVisibility(View.VISIBLE);
                    ivPlatformIcon.setVisibility(View.VISIBLE);
                    if(mCurrSelectedPlatform == tempBean.getPlatformType()){
                        tvPlatformName.setTextColor(Color.WHITE);
                    }else{
                        tvPlatformName.setTextColor(Color.parseColor("#999999"));
                    }
                    if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_FACEBOOK){
                        tvPlatformName.setText(R.string.str_facebook);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_FACEBOOK){
                            ivPlatformIcon.setImageResource(R.drawable.share_facebook_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_facebook_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_INSTAGRAM){
                        tvPlatformName.setText(R.string.str_instagram);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_INSTAGRAM){
                            ivPlatformIcon.setImageResource(R.drawable.share_instagram_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_instagram_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_LINE){
                        tvPlatformName.setText(R.string.str_line);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_LINE){
                            ivPlatformIcon.setImageResource(R.drawable.share_line_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_line_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                        tvPlatformName.setText(R.string.str_twitter);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_twitter_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_twitter_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WHATSAPP){
                        tvPlatformName.setText(R.string.str_whatsapp);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_whatsapp_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_whatsapp_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_QQ){
                        tvPlatformName.setText(R.string.str_qq_friends);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_qq_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_qq_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_QQ_ZONE){
                        tvPlatformName.setText(R.string.str_qzone);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_qq_zone_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_qq_zone_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WEXIN){
                        tvPlatformName.setText(R.string.str_weixin_friends);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_wechat_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_wechat_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE){
                        tvPlatformName.setText(R.string.str_circle_of_friends);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_wechat_friend_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_wechat_friend_icon);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WEIBO_SINA){
                        tvPlatformName.setText(R.string.str_weibo);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.share_weibo_icon);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.share_weibo_icon);
                        }
                    }else {
                        tvPlatformName.setVisibility(View.VISIBLE);
                        ivPlatformIcon.setVisibility(View.VISIBLE);
                    }

                    itemView.setOnClickListener(new SharePlatformOnClickListener(position));
                }
            }
        }
    }
}
