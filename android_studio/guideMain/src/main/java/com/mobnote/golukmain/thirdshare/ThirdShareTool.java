package com.mobnote.golukmain.thirdshare;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.mobnote.golukmain.R;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.umeng.facebook.FacebookCallback;
import com.umeng.facebook.FacebookException;
import com.umeng.facebook.share.Sharer;
import com.umeng.facebook.share.widget.ShareDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.media.UMWeb;

import java.io.File;

/**
 * Created by leege100 on 16/5/18.
 */
public class ThirdShareTool extends AbsThirdShare {
    public ThirdShareTool(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl, Bitmap bitmap, String realDesc, String videoId, String shareType, String filePath, String from) {
        super(activity, spf, surl, curl, db, tl, bitmap, realDesc, videoId, shareType, filePath, from);
        modifyUMDialog();
        initFacebook();
    }

    public ThirdShareTool(Activity activity, SharePlatformUtil spf, ThirdShareBean shareBean) {
        super(activity, spf, shareBean.surl, shareBean.curl, shareBean.db, shareBean.tl, shareBean.bitmap,
                shareBean.realDesc, shareBean.videoId, shareBean.mShareType, shareBean.filePath, shareBean.from);
    }

    private void initFacebook() {
        FacebookShareHelper.getInstance().mShareDialog = new ShareDialog(mActivity);
        FacebookShareHelper.getInstance().mShareDialog.registerCallback(
                FacebookShareHelper.getInstance().mCallbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onCancel() {
                        notifyShareState(false);
                        if (GolukUtils.isActivityAlive(mActivity)) {
                            close();
                        }
                        setCanJump();
                    }

                    @Override
                    public void onError(FacebookException exp) {
                        exp.printStackTrace();
                        notifyShareState(false);
                        if (GolukUtils.isActivityAlive(mActivity)) {
                            close();
                        }
                        setCanJump();
                    }

                    @Override
                    public void onSuccess(Sharer.Result ret) {
                        notifyShareState(true);
                        if (GolukUtils.isActivityAlive(mActivity)) {
                            close();
                        }
                        setCanJump();
                    }
                });
    }

    @Override
    public void CallBack_Share(int event) {

    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
    }

    @Override
    public void click(String type) {

    }

    @Override
    public void close() {
    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            mHander.sendEmptyMessage(100);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            mHander.sendEmptyMessage(101);
            String error = "";
            if (null != t) {
                error = t.toString();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            mHander.sendEmptyMessage(102);

        }
    };

    public void click_line() {
        if (!AppInstallationUtil.isAppInstalled(mActivity, GolukConfig.LINE_PACKAGE)) {
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_line_no_install));
            notifyShareState(false);
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_LINE);
        if (null == sc) {
            setCanJump();
            return;
        }
        shareUp();// 上报分享统计
    }

    public void click_whatsapp() {
        if (!AppInstallationUtil.isAppInstalled(mActivity, GolukConfig.WTATSAPP_PACKAGE)) {
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_whatsapp_no_install));
            notifyShareState(false);
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_WHATSAPP);
        if (null == sc) {
            setCanJump();
            return;
        }
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.WHATSAPP).setCallback(umShareListener)
                .setShareContent(sc).share();
        mCurrentShareType = TYPE_WHATSAPP;
        shareUp();// 上报分享统计
    }

    // 点击 "twitter"
    public void click_twitter() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER)) {
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_twitter_no_install));
            notifyShareState(false);
            return;
        }
        if (!isCanClick()) {
            return;
        }
//        final ShareContent sc = getShareContent(TYPE_TWITTER);
//        if (null == sc) {
//            setCanJump();
//            return;
//        }
        //new ShareAction(mActivity).setPlatform(SHARE_MEDIA.TWITTER).setCallback(umShareListener).setShareContent(sc).share();

        final String shareTxt = shareurl + "&type=" + TYPE_TWITTER;
        UMImage shareImage = new UMImage(mActivity, mImageUrl);
        new ShareAction(mActivity)
                .setPlatform(SHARE_MEDIA.TWITTER)
                .withText(shareTxt)
                .withMedia(shareImage)
                .setCallback(umShareListener)
                .share();
        mCurrentShareType = TYPE_TWITTER;
        shareUp();// 上报分享统计
    }

    public void click_instagram(String videoPath) {
        String type = "image/*";
        String mediaPath = "";

        if ("1".equals(mShareType)) {
            type = "video/*";
            mediaPath = videoPath;
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        boolean flog = AppInstallationUtil.isAppInstalled(mActivity, GolukConfig.INSTAGRAM_PACKAGE);

        if (!isCanClick()) {
            notifyShareState(false);
            return;
        }

        if (flog) {
            ComponentName cn = new ComponentName(GolukConfig.INSTAGRAM_PACKAGE, GolukConfig.INSTAGRAM_CLASS);
            share.setType(type);
            share.setComponent(cn);
            File media = new File(mediaPath);
            Uri uri = Uri.fromFile(media);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            mCurrentShareType = TYPE_INSTAGRAM;
            shareUp();// 上报分享统计
            mActivity.startActivity(Intent.createChooser(share, "Share to"));
        } else {
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_instagram_no_install));
            notifyShareState(false);
        }
        setCanJump();
    }

    // 点击 "facebook"
    public void click_facebook() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.FACEBOOK)) {
            notifyShareState(false);
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_facebook_no_install));
            return;
        }
        if (!isCanClick()) {
            return;
        }
//        final ShareContent sc = getShareContent(TYPE_FACEBOOK);
//        if (null == sc) {
//            setCanJump();
//            return;
//        }

        final String url = shareurl + "&type=" + SHARE_MEDIA.FACEBOOK;
        UMWeb webMedia = new UMWeb(url);
        webMedia.setTitle(mTitle);
        webMedia.setThumb(new UMImage(mActivity, mImageUrl));
        webMedia.setDescription(mDescribe);
        new ShareAction(mActivity)
                .setPlatform(SHARE_MEDIA.FACEBOOK)
                .setCallback(umShareListener)
                .withMedia(webMedia)
                .share();
        mCurrentShareType = TYPE_FACEBOOK;
        shareUp();// 上报分享统计
    }

    // 点击　“微信”
    public void click_wechat() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)) {
            GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_no_weixin));
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_WEIXIN);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (TextUtils.isEmpty(sc.mText)) {
            sc.mText = mActivity.getResources().getString(R.string.app_name);
        }
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.WEIXIN).setCallback(umShareListener).setShareContent(sc)
                .share();
        mCurrentShareType = TYPE_WEIXIN;
        this.shareUp();// 上报分享统计
    }

    // 点击“朋友圈”
    public void click_wechat_circle() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)) {
            GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_no_weixin));
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_WEIXIN_CIRCLE);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (TextUtils.isEmpty(sc.mText)) {
            sc.mText = mActivity.getResources().getString(R.string.app_name);
        }

        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE).setCallback(umShareListener)
                .setShareContent(sc).share();

        mCurrentShareType = TYPE_WEIXIN_CIRCLE;
        this.shareUp();// 上报分享统计
    }

    // 点击“ＱＱ”
    public void click_QQ() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)) {
            GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_qq_low_version));
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_QQ);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (TextUtils.isEmpty(sc.mText)) {
            sc.mText = mActivity.getResources().getString(R.string.app_name);
        }
        mCurrentShareType = TYPE_QQ;
        this.shareUp();// 上报分享统计
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.QQ).setCallback(umShareListener).setShareContent(sc).share();
    }

    // 点击“ＱＱ空间”
    public void click_qqZone() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)) {
            GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_qq_low_version));
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_QQ_ZONE);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (TextUtils.isEmpty(sc.mText)) {
            sc.mText = mActivity.getResources().getString(R.string.app_name);
        }
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.QZONE).setCallback(umShareListener).setShareContent(sc).share();
        mCurrentShareType = TYPE_QQ_ZONE;
        this.shareUp();// 上报分享统计
    }

    public void click_sina() {
        if (!sharePlatform.isInstallPlatform(SHARE_MEDIA.SINA)) {
            GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_qq_low_version));
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_WEIBO_XINLANG);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (TextUtils.isEmpty(sc.mText)) {
            sc.mText = mActivity.getResources().getString(R.string.app_name);
        }
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.SINA).setCallback(umShareListener).setShareContent(sc)
                .share();
        mCurrentShareType = TYPE_WEIBO_XINLANG;
        this.shareUp();// 上报分享统计
    }

    public void click_VK() {
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_VK);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (TextUtils.isEmpty(sc.mText)) {
            sc.mText = mActivity.getResources().getString(R.string.app_name);
        }

        if (sc.getShareType() == ShareContent.VIDEO_STYLE) {
            final String url = sc.mMedia.toUrl();
            new GetRealUrlTask(new GetRealUrlTask.OnRealUrlListener() {
                @Override
                public void onGetRealUrl(String realUrl) {
                    UMVideo umVideo = new UMVideo(realUrl);
                    umVideo.setTitle(mTitle);
                    umVideo.setDescription(mDescribe);
                    if (!TextUtils.isEmpty(mImageUrl)) {
                        final UMImage image = new UMImage(mActivity, mImageUrl);
                        umVideo.setThumb(image);
                    }

                    sc.mMedia = umVideo;

                    new ShareAction(mActivity)
                            .setPlatform(SHARE_MEDIA.VKONTAKTE)
                            .setCallback(umShareListener)
                            .setShareContent(sc)
                            .share();
                    mCurrentShareType = TYPE_VK;
                }
            }).execute(url);
        } else {
            new ShareAction(mActivity).setPlatform(SHARE_MEDIA.VKONTAKTE).setCallback(umShareListener).setShareContent(sc).share();
            mCurrentShareType = TYPE_VK;
        }

        this.shareUp();// 上报分享统计
    }
}
