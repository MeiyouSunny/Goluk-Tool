package com.mobnote.golukmain.thirdshare.china;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import cn.com.tiros.debug.GolukDebugUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class ChinaThirdShare extends AbsThirdShare implements OnClickListener {

	private PopupWindow mPopWindow = null;

	public ChinaThirdShare(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
			Bitmap bitmap, String realDesc, String videoId, String shareType) {
		super(activity, spf, surl, curl, db, tl, bitmap, realDesc, videoId, shareType);
		initView();
		modifyUMDialog();
	}

	private void initView() {
		mPopWindow = new PopupWindow();
		View rootView = LayoutInflater.from(mActivity).inflate(R.layout.custom_board, null);
		rootView.findViewById(R.id.wechat).setOnClickListener(this);
		rootView.findViewById(R.id.wechat_circle).setOnClickListener(this);
		rootView.findViewById(R.id.qq).setOnClickListener(this);
		rootView.findViewById(R.id.qqZone).setOnClickListener(this);
		rootView.findViewById(R.id.sina).setOnClickListener(this);
		mPopWindow.setContentView(rootView);
		mPopWindow.setWidth(LayoutParams.MATCH_PARENT);
		mPopWindow.setHeight(LayoutParams.WRAP_CONTENT);
		mPopWindow.setFocusable(true);
		mPopWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopWindow.setTouchable(true);
	}

	@Override
	public void click(String type) {
		if (TextUtils.isEmpty(type)) {
			return;
		}
		if (TYPE_WEIXIN.equals(type)) {
			this.click_wechat();
		} else if (TYPE_WEIXIN_CIRCLE.equals(type)) {
			this.click_wechat_circle();
		} else if (TYPE_QQ.equals(type)) {
			this.click_QQ();
		} else if (TYPE_QQ_ZONE.equals(type)) {
			this.click_qqZone();
		} else if (TYPE_WEIBO_XINLANG.equals(type)) {
			this.click_sina();
		} else {
		}
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		if (null != mPopWindow) {
			mPopWindow.showAtLocation(parent, gravity, x, y);
		}
	}

	@Override
	public void CallBack_Share(int event) {

	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (!UserUtils.isNetDeviceAvailable(mActivity)) {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_network_unavailable));
			return;
		}
		if (id == R.id.wechat) {
			click_wechat();
		} else if (id == R.id.wechat_circle) {
			click_wechat_circle();
		} else if (id == R.id.qq) {
			click_QQ();
		} else if (id == R.id.qqZone) {
			click_qqZone();
		} else if (id == R.id.sina) {
			click_sina();
		} else {
		}

	}

	// 点击　“微信”
	public void click_wechat() {
		if (!isCanClick()) {
			return;
		}

		final ShareContent sc = getShareContent(TYPE_WEIXIN);
		if (null == sc) {
			setCanJump();
			return;
		}

		new ShareAction(mActivity).setPlatform(SHARE_MEDIA.WEIXIN).setCallback(umShareListener).setShareContent(sc)
				.share();
		mCurrentShareType = TYPE_WEIXIN;
		this.shareUp();// 上报分享统计
	}

	// 点击　“朋友圈”
	public void click_wechat_circle() {
		if (!isCanClick()) {
			return;
		}
		final ShareContent sc = getShareContent(TYPE_WEIXIN_CIRCLE);
		if (null == sc) {
			setCanJump();
			return;
		}
		new ShareAction(mActivity).setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE).setCallback(umShareListener)
				.setShareContent(sc).share();
		mCurrentShareType = TYPE_WEIXIN_CIRCLE;
		this.shareUp();// 上报分享统计
	}

	// 点击　“ＱＱ”
	public void click_QQ() {
		if (!isCanClick()) {
			return;
		}
		final ShareContent sc = getShareContent(TYPE_QQ);
		if (null == sc) {
			setCanJump();
			return;
		}
		mCurrentShareType = TYPE_QQ;
		this.shareUp();// 上报分享统计
		new ShareAction(mActivity).setPlatform(SHARE_MEDIA.QQ).setCallback(umShareListener).setShareContent(sc).share();
	}

	// 点击　“ＱＱ空间”
	public void click_qqZone() {
		if (!isCanClick()) {
			return;
		}
		final ShareContent sc = getShareContent(TYPE_QQ_ZONE);
		if (null == sc) {
			setCanJump();
			return;
		}
		new ShareAction(mActivity).setPlatform(SHARE_MEDIA.QZONE).setCallback(umShareListener).setShareContent(sc)
				.share();
		mCurrentShareType = TYPE_QQ_ZONE;
		this.shareUp();// 上报分享统计
	}

	public void click_sina() {
		GolukDebugUtils.e("", "sina-------click----11111");
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

	private UMShareListener umShareListener = new UMShareListener() {
		@Override
		public void onResult(SHARE_MEDIA platform) {
			mHander.sendEmptyMessage(100);
			GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----umShareListener----onResult");
		}

		@Override
		public void onError(SHARE_MEDIA platform, Throwable t) {
			mHander.sendEmptyMessage(101);
			GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----umShareListener----onError" + t.toString());
		}

		@Override
		public void onCancel(SHARE_MEDIA platform) {
			mHander.sendEmptyMessage(102);

			GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----umShareListener----onCancel");
		}
	};

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

		GolukDebugUtils.e("", "sina-------click----2222");
		if (!sharePlatform.isSinaWBValid()) {
			GolukDebugUtils.e("", "sina-------click----3333");
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
		GolukDebugUtils.e("", "sina-------click----44444" + actionUrl);
		if (sharePlatform.mSinaWBUtils.isInstallClient()) {
			GolukDebugUtils.e("", "sina-------click----55555");
			final int supportApi = sharePlatform.mSinaWBUtils.getSupportAPI();
			GolukDebugUtils.e("", "sina-------click----6666:  " + supportApi);
			if (supportApi >= SUPPORT_MUTI_MSG) {
				GolukDebugUtils.e("", "sina-------click----77777:  ");
				sharePlatform.mSinaWBUtils.sendMessage(inputDefaultContent, title, t_des, actionUrl, dataUrl, t_bitmap,
						true);
			} else {
				GolukDebugUtils.e("", "sina-------click----88888:  ");
				sharePlatform.mSinaWBUtils.sendSingleMessage(inputDefaultContent, title, t_des, actionUrl, dataUrl,
						t_bitmap);
			}
		} else {
			sharePlatform.mSinaWBUtils.sendMessage(inputDefaultContent, title, t_des, actionUrl, dataUrl, t_bitmap,
					false);
			GolukDebugUtils.e("", "sina-------click----999999:  ");
		}
	}

	@Override
	public void close() {
		if (null != this.mPopWindow) {
			this.mPopWindow.dismiss();
		}
	}
}