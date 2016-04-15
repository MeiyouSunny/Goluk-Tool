package com.mobnote.golukmain.thirdshare;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import cn.com.tiros.debug.GolukDebugUtils;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.mobnote.golukmain.R;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

public class AbroadThirdShare extends AbsThirdShare implements OnClickListener {

	private PopupWindow mPopWindow = null;

	public AbroadThirdShare(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
			Bitmap bitmap, String realDesc, String videoId, String shareType) {
		super(activity, spf, surl, curl, db, tl, bitmap, realDesc, videoId, shareType);
		initView();
		modifyUMDialog();
		initFacebook();
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		mPopWindow = new PopupWindow();
		View rootView = LayoutInflater.from(mActivity).inflate(R.layout.custom_board_2, null);
		// 即刻分享
		if ("1".equals(mShareType)) {
			rootView.findViewById(R.id.instagram_layout).setVisibility(View.VISIBLE);
		} else {
			rootView.findViewById(R.id.instagram_layout).setVisibility(View.GONE);
		}
		rootView.findViewById(R.id.share_instagram).setOnClickListener(this);
		rootView.findViewById(R.id.share_facebook).setOnClickListener(this);
		rootView.findViewById(R.id.share_twitter).setOnClickListener(this);
		rootView.findViewById(R.id.share_whatsapp).setOnClickListener(this);
		rootView.findViewById(R.id.share_line).setOnClickListener(this);
		mPopWindow.setContentView(rootView);
		mPopWindow.setWidth(LayoutParams.MATCH_PARENT);
		mPopWindow.setHeight(LayoutParams.WRAP_CONTENT);
		mPopWindow.setFocusable(true);
		mPopWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopWindow.setTouchable(true);
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
	public void click(String type) {
		if (TextUtils.isEmpty(type)) {
			return;
		}
		if (TYPE_FACEBOOK.equals(type)) {
			click_facebook();
		} else if (TYPE_INSTAGRAM.equals(type)) {
			click_instagram("");
		} else if (TYPE_TWITTER.equals(type)) {
			click_twitter();
		} else if (TYPE_WHATSAPP.equals(type)) {
			click_whatsapp();
		} else if (TYPE_LINE.equals(type)) {
			click_line();
		} else {
		}
	}

	@Override
	public void CallBack_Share(int event) {

	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		if (null != mPopWindow) {
			mPopWindow.showAtLocation(parent, gravity, x, y);
		}
	}

	@Override
	public void close() {
		if (null != mPopWindow) {
			mPopWindow.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (!UserUtils.isNetDeviceAvailable(mActivity)) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.user_net_unavailable));
			return;
		}
		if (id == R.id.share_instagram) {
			click_instagram("");
		} else if (id == R.id.share_facebook) {
			click_facebook();
		} else if (id == R.id.share_twitter) {
			click_twitter();
		} else if (id == R.id.share_whatsapp) {
			click_whatsapp();
		} else if (id == R.id.share_line) {
			click_line();
		} else {
		}
	}

	private UMShareListener umShareListener = new UMShareListener() {
		@Override
		public void onResult(SHARE_MEDIA platform) {
			mHander.sendEmptyMessage(100);
			GolukDebugUtils.e("", "youmeng----goluk----AbroadThirdShare----umShareListener----onResult");
		}

		@Override
		public void onError(SHARE_MEDIA platform, Throwable t) {
			mHander.sendEmptyMessage(101);
			GolukDebugUtils.e("", "youmeng----goluk----AbroadThirdShare----umShareListener----onError" + t.toString());
		}

		@Override
		public void onCancel(SHARE_MEDIA platform) {
			mHander.sendEmptyMessage(102);

			GolukDebugUtils.e("", "youmeng----goluk----AbroadThirdShare----umShareListener----onCancel");
		}
	};

	private void click_line() {
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

	private void click_whatsapp() {
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
	private void click_twitter() {
		if (sharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER) == false) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_twitter_no_install));
			notifyShareState(false);
			return;
		}
		GolukDebugUtils.e("", "youmeng----goluk----AbroadThirdShare----click_twitter----0 ");
		if (!isCanClick()) {
			return;
		}
		GolukDebugUtils.e("", "youmeng----goluk----AbroadThirdShare----click_twitter----1 ");
		final ShareContent sc = getShareContent(TYPE_TWITTER);
		if (null == sc) {
			setCanJump();
			return;
		}
		final String shareTxt = sc.mText + "   " + sc.mTargetUrl;
		new ShareAction(mActivity).setPlatform(SHARE_MEDIA.TWITTER).setCallback(umShareListener).withText(shareTxt)
				.withTitle(sc.mTitle).share();
		GolukDebugUtils.e("", "youmeng----goluk----AbroadThirdShare----click_twitter----3 ");
		mCurrentShareType = TYPE_TWITTER;
		shareUp();// 上报分享统计
	}

	private void click_instagram(String videoPath) {
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
	private void click_facebook() {
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

}
