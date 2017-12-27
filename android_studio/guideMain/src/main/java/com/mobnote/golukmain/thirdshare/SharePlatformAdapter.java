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
import com.mobnote.golukmain.thirdshare.bean.SharePlatform;
import com.mobnote.golukmain.thirdshare.bean.ThirdSharePlatformBean;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by leege100 on 16/5/13.
 */
public class SharePlatformAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private List<ThirdSharePlatformBean> mSharePlatformBeanList;

    /** 当前选中的平台 */
    private int mCurrSelectedPlatform;
    public SharePlatformUtil mSharePlatform;

    public SharePlatformAdapter(Context context){

        this.mContext = context;
        mCurrSelectedPlatform = SharePlatform.SHARE_PLATFORM_NULL;
        mSharePlatform = new SharePlatformUtil(mContext);
        fillList();
    }

    public int getCurrSelectedPlatformType () {
        return mCurrSelectedPlatform;
    }

    public ThirdSharePlatformBean getCurrSelectedPlatformzBean() {
        for (ThirdSharePlatformBean platformBean : mSharePlatformBeanList) {
            if (mCurrSelectedPlatform == platformBean.getPlatformType()) {
                return platformBean;
            }
        }
        return null;
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

        mSharePlatformBeanList = new ArrayList<ThirdSharePlatformBean>();
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
        mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_COPYLINK));
    }

    private void addInternalSharePlatform(){
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WEXIN_CIRCLE));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WEXIN));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.SINA)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WEIBO_SINA));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_QQ));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_QQ_ZONE));
        }

    }

    private void addInternationalSharePlatform(){
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.FACEBOOK)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_FACEBOOK));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_TWITTER));
        }
        if(AppInstallationUtil.isAppInstalled(mContext, GolukConfig.INSTAGRAM_PACKAGE)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_INSTAGRAM));
        }
        if(mSharePlatform.isInstallPlatform(SHARE_MEDIA.WHATSAPP)){
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WHATSAPP));
        }
//        if(GolukUtils.isAppInstalled(mContext, GolukConfig.LINE_PACKAGE)){
//            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_LINE));
//        }
        if(GolukUtils.isAppInstalled(mContext, GolukConfig.VK_PACKAGE)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_VK));
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
                ThirdSharePlatformBean tempBean = mSharePlatformBeanList.get(position);
                if(tempBean != null){

                    if(tempBean.getPlatformType() != mCurrSelectedPlatform){
                        mCurrSelectedPlatform = tempBean.getPlatformType();
                    }else{
                        mCurrSelectedPlatform = SharePlatform.SHARE_PLATFORM_NULL;
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
                ThirdSharePlatformBean tempBean = mSharePlatformBeanList.get(position);
                if(tempBean != null){
                    tvPlatformName.setVisibility(View.VISIBLE);
                    ivPlatformIcon.setVisibility(View.VISIBLE);
                    if(mCurrSelectedPlatform == tempBean.getPlatformType()){
                        tvPlatformName.setTextColor(Color.WHITE);
                    }else{
                        tvPlatformName.setTextColor(Color.parseColor("#7a7f85"));
                    }
                    tvPlatformName.setText(tempBean.getName());
                    if(mCurrSelectedPlatform == tempBean.getPlatformType()){
                        ivPlatformIcon.setImageResource(tempBean.getSmallIconSelectedRes());
                    }else{
                        ivPlatformIcon.setImageResource(tempBean.getSmallIconRes());
                    }

                    itemView.setOnClickListener(new SharePlatformOnClickListener(position));
                }
            }
        }
    }
}
