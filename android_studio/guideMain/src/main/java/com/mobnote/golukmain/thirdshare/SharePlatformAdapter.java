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
import com.mobnote.eventbus.SharePlatformSelectedEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.thirdshare.bean.SharePlatformBean;
import com.mobnote.util.GolukConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by leege100 on 16/5/13.
 */
public class SharePlatformAdapter extends RecyclerView.Adapter{

    Context mContext;
    List<SharePlatformBean> mSharePlatformBeanList;
    /** 当前选中的平台 */
    public int mCurrSelectedPlatform;
    SharePlatformUtil mSharePlatform;

    public SharePlatformAdapter(Context context){

        this.mContext = context;
        mCurrSelectedPlatform = SharePlatformBean.SHARE_PLATFORM_NULL;
        mSharePlatform = new SharePlatformUtil(mContext);
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
        if(GolukApplication.getInstance().isMainland()){
            //国内版，国内平台显示在前面，国外平台在后面
            addInternalSharePlatform();
            addInternationalSharePlatform();
        }else{
            //国外版，则国外平台显示在前，国内的在后
            addInternationalSharePlatform();
            addInternalSharePlatform();
        }
        //添加拷贝链接
        mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_COPYLINK));
    }

    private void addInternalSharePlatform(){
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEXIN));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.SINA)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WEIBO_SINA));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_QQ));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_QQ_ZONE));
        }

    }

    private void addInternationalSharePlatform(){
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.FACEBOOK)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_FACEBOOK));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_TWITTER));
        }
        if(AppInstallationUtil.isAppInstalled(mContext, GolukConfig.INSTAGRAM_PACKAGE)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_INSTAGRAM));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.WHATSAPP)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_WHATSAPP));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.LINE)){
            mSharePlatformBeanList.add(new SharePlatformBean(SharePlatformBean.SHARE_PLATFORM_LINE));
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
                    EventBus.getDefault().post(new SharePlatformSelectedEvent(mCurrSelectedPlatform));
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
                        tvPlatformName.setTextColor(Color.parseColor("#7a7f85"));
                    }
                    if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_FACEBOOK){
                        tvPlatformName.setText(R.string.str_facebook);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_FACEBOOK){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_facebook_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_facebook);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_INSTAGRAM){
                        tvPlatformName.setText(R.string.str_instagram);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_INSTAGRAM){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_instagram_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_instagram);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_LINE){
                        tvPlatformName.setText(R.string.str_line);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_LINE){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_line_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_line);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                        tvPlatformName.setText(R.string.str_twitter);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_TWITTER){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_twitter_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_twitter);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WHATSAPP){
                        tvPlatformName.setText(R.string.str_whatsapp);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_WHATSAPP){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_whatsapp_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_whatsapp);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_QQ){
                        tvPlatformName.setText(R.string.str_qq_friends);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_QQ){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_qq_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_qq);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_QQ_ZONE){
                        tvPlatformName.setText(R.string.str_qzone);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_QQ_ZONE){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_qzone_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_qzone);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WEXIN){
                        tvPlatformName.setText(R.string.str_weixin_friends);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_WEXIN){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_wechat_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_wechat);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE){
                        tvPlatformName.setText(R.string.str_circle_of_friends);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_WEXIN_CIRCLE){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_moments_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_moments);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_WEIBO_SINA){
                        tvPlatformName.setText(R.string.str_weibo);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_WEIBO_SINA){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_weibo_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_weibo);
                        }
                    }else if(tempBean.getPlatformType() == SharePlatformBean.SHARE_PLATFORM_COPYLINK){
                        tvPlatformName.setText(R.string.str_copy_link);
                        if(mCurrSelectedPlatform == SharePlatformBean.SHARE_PLATFORM_COPYLINK){
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_copy_click);
                        }else{
                            ivPlatformIcon.setImageResource(R.drawable.icon_share_copy);
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
