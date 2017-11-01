package com.mobnote.golukmain.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by leege100 on 2017/3/12.
 */

public class SimpleSharePlatformAdapter extends RecyclerView.Adapter {

    private Activity mContext;
    private ThirdShareBean mThirdShareBean;
    private List<ThirdSharePlatformBean> mSharePlatformBeanList;
    private SharePlatformUtil mSharePlatform;

    public SimpleSharePlatformAdapter(Activity context, ThirdShareBean thirdShareBean) {
        this.mContext = context;
        this.mSharePlatform = new SharePlatformUtil(mContext);
        this.mThirdShareBean = thirdShareBean;
        fillList();
    }

    @Override
    public SharePlatformViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SharePlatformViewHolder holder = new SharePlatformViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.share_board_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SharePlatformViewHolder) {
            SharePlatformViewHolder sharePlatformViewHolder = (SharePlatformViewHolder) holder;
            sharePlatformViewHolder.render(position);
        }
    }

    @Override
    public int getItemCount() {
        return mSharePlatformBeanList != null ? mSharePlatformBeanList.size() : 0;
    }

    private void fillList() {
        mSharePlatformBeanList = new ArrayList<ThirdSharePlatformBean>();
        if (GolukApplication.getInstance().isMainland()) {
            addInternalSharePlatform();
        } else {
            addInternationalSharePlatform();
        }
    }

    private void addInternalSharePlatform() {
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WEXIN_CIRCLE));
        }
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WEXIN));
        }
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.SINA)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WEIBO_SINA));
        }
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_QQ));
        }
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_QQ_ZONE));
        }

    }

    private void addInternationalSharePlatform() {
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.FACEBOOK)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_FACEBOOK));
        }
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_TWITTER));
        }
        if (AppInstallationUtil.isAppInstalled(mContext, GolukConfig.INSTAGRAM_PACKAGE)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_INSTAGRAM));
        }
        if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.WHATSAPP)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_WHATSAPP));
        }
        if (GolukUtils.isAppInstalled(mContext, GolukConfig.LINE_PACKAGE)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_LINE));
        }
        if (GolukUtils.isAppInstalled(mContext, GolukConfig.VK_PACKAGE)) {
            mSharePlatformBeanList.add(ThirdSharePlatformFactory.createSharePlatformBean(mContext, SharePlatform.SHARE_PLATFORM_VK));
        }
    }

    private class SharePlatformOnClickListener implements View.OnClickListener {

        private int position;

        public SharePlatformOnClickListener(int p) {
            this.position = p;
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(mContext, "被点击了", Toast.LENGTH_SHORT).show();
            if (mSharePlatformBeanList != null && mSharePlatformBeanList.size() > position) {
                ThirdSharePlatformBean tempBean = mSharePlatformBeanList.get(position);
                if (tempBean != null) {
                    tempBean.startShare(mContext, mSharePlatform, mThirdShareBean);
                }
            }
        }
    }

    class SharePlatformViewHolder extends RecyclerView.ViewHolder {

        View rootView;
        TextView tvPlatformName;
        ImageView ivPlatformIcon;

        public SharePlatformViewHolder(View view) {
            super(view);
            rootView = view;
            tvPlatformName = (TextView) view.findViewById(R.id.rv_platform_name);
            ivPlatformIcon = (ImageView) view.findViewById(R.id.iv_platform_icon);
        }

        public void render(int position) {
            if (mSharePlatformBeanList != null && mSharePlatformBeanList.size() > position) {
                ThirdSharePlatformBean tempBean = mSharePlatformBeanList.get(position);
                if (tempBean != null) {
                    tvPlatformName.setVisibility(View.VISIBLE);
                    ivPlatformIcon.setVisibility(View.VISIBLE);
                    tvPlatformName.setText(tempBean.getName());
                    ivPlatformIcon.setImageResource(tempBean.getLargeIconRes());

                    rootView.setOnClickListener(new SimpleSharePlatformAdapter.SharePlatformOnClickListener(position));
                }
            }
        }
    }
}
