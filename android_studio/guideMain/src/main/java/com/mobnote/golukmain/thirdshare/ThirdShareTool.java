package com.mobnote.golukmain.thirdshare;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.dialogs.VKShareDialogBuilder;

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
    public ThirdShareTool(Activity activity,SharePlatformUtil spf, ThirdShareBean shareBean) {
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
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.LINE).setCallback(umShareListener)
                .withText(sc.mTitle + "\n" + sc.mText + "\n" + sc.mTargetUrl).withMedia((UMImage) sc.mMedia).share();
        mCurrentShareType = TYPE_LINE;
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
                .withText(sc.mTitle + "\n" + sc.mText + "\n" + sc.mTargetUrl).share();
        mCurrentShareType = TYPE_WHATSAPP;
        shareUp();// 上报分享统计
    }

    // 点击 "twitter"
    public void click_twitter() {
        if (sharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER) == false) {
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_twitter_no_install));
            notifyShareState(false);
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_TWITTER);
        if (null == sc) {
            setCanJump();
            return;
        }
        final String shareTxt = sc.mText + "   " + sc.mTargetUrl;
        new ShareAction(mActivity).setPlatform(SHARE_MEDIA.TWITTER).setCallback(umShareListener).withText(shareTxt)
                .withTitle(sc.mTitle).share();
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
        if (sharePlatform.isInstallPlatform(SHARE_MEDIA.FACEBOOK) == false) {
            notifyShareState(false);
            GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_facebook_no_install));
            return;
        }
        if (!isCanClick()) {
            return;
        }
        final ShareContent sc = getShareContent(TYPE_FACEBOOK);
        if (null == sc) {
            setCanJump();
            return;
        }
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent.Builder linkBuilder = new ShareLinkContent.Builder().setContentTitle(sc.mTitle)
                    .setContentDescription(sc.mText);
            if (!TextUtils.isEmpty(sc.mTargetUrl)
                    && (sc.mTargetUrl.startsWith("http://") || sc.mTargetUrl.startsWith("https://"))) {
                linkBuilder.setContentUrl(Uri.parse(sc.mTargetUrl));
            }
            if (!TextUtils.isEmpty(mImageUrl) && (mImageUrl.startsWith("http://") || mImageUrl.startsWith("https://"))) {
                linkBuilder.setImageUrl(Uri.parse(mImageUrl));
            }
            ShareLinkContent linkContent = linkBuilder.build();
            FacebookShareHelper.getInstance().mShareDialog.show(linkContent);
        }

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
        if (TextUtils.isEmpty(mImageUrl)) {
            Glide.with(mActivity).load(R.drawable.ic_launcher).asBitmap().into(new SimpleTarget<Bitmap>(50, 50) {
                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    close();
                }

                @Override
                public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
                    close();
                    doSinaShare(arg0, arg1);
                }
            });
        } else {
            Glide.with(mActivity).load(mImageUrl).asBitmap().into(new SimpleTarget<Bitmap>(50, 50) {
                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    close();
                }

                @Override
                public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
                    close();
                    doSinaShare(arg0, arg1);
                }
            });
        }
    }

    /**
     * 接受第三方界面分享的返回结果, (参数同Activity中的onActivityResult方法参数一样)
     *
     * @author jyf
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCurrentShareType == TYPE_VK) {
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    // User passed Authorization
                    shareVK();
                }

                @Override
                public void onError(VKError error) {
                    // User didn't pass Authorization
                }
            }))
                return;
        }
        sharePlatform.onActivityResult(requestCode, resultCode, data);
    }

    private void doSinaShare(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
        if (null == sharePlatform) {
            return;
        }

        if (null != mActivity && mActivity instanceof BaseActivity) {
            if (!((BaseActivity) mActivity).isAllowedClicked()) {
                return;
            }
            ((BaseActivity) mActivity).setJumpToNext();
        }

        if (!sharePlatform.isSinaWBValid()) {
            // 去授权
            sharePlatform.mSinaWBUtils.authorize();
            return;
        }
        mCurrentShareType = TYPE_WEIBO_XINLANG;
        shareUp();// 上报分享统计
        printStr();
        final String t_des = mDescribe;
        final String inputDefaultContent = mSinaTxt;
        final String title = mTitle;
        final String dataUrl = shareurl;
        final String actionUrl = shareurl + "&type=" + TYPE_WEIBO_XINLANG;
        final Bitmap t_bitmap = arg0;
        if (sharePlatform.mSinaWBUtils.isInstallClient()) {
            final int supportApi = sharePlatform.mSinaWBUtils.getSupportAPI();
            if (supportApi >= SUPPORT_MUTI_MSG) {
                sharePlatform.mSinaWBUtils.sendMessage(inputDefaultContent, title, t_des, actionUrl, dataUrl, t_bitmap, true);
            } else {
                sharePlatform.mSinaWBUtils.sendSingleMessage(inputDefaultContent, title, t_des, actionUrl, dataUrl, t_bitmap);
            }
        } else {
            sharePlatform.mSinaWBUtils.sendMessage(inputDefaultContent, title, t_des, actionUrl, dataUrl, t_bitmap, false);
        }
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
        if (VKSdk.isLoggedIn()) {
            shareVK();
        } else {
            VKSdk.login(mActivity, VKScope.WALL, VKScope.PHOTOS);
        }

        mCurrentShareType = TYPE_VK;
        this.shareUp();// 上报分享统计
    }

    private void shareVK() {
        VKShareDialogBuilder vkShareDialogBuilder = new VKShareDialogBuilder();
        if (mThumbBitmap != null) {
            vkShareDialogBuilder.setAttachmentImages(new VKUploadImage[]{
                    new VKUploadImage(mThumbBitmap, VKImageParameters.pngImage())
            });
        }
        vkShareDialogBuilder.setText(mDescribe)
                .setAttachmentLink(mTitle, shareurl)
                .setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                    public void onVkShareComplete(int postId) {
                        mHander.sendEmptyMessage(100);
                    }

                    public void onVkShareCancel() {
                        mHander.sendEmptyMessage(101);
                    }

                    @Override
                    public void onVkShareError(VKError error) {
                        mHander.sendEmptyMessage(102);
                    }
                })
                .show(mActivity.getFragmentManager(), "VK_SHARE_DIALOG");
    }
}
