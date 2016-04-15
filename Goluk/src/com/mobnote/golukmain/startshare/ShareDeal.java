package com.mobnote.golukmain.startshare;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.thirdshare.AppInstallationUtil;
import com.mobnote.golukmain.thirdshare.IThirdShareFn;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class ShareDeal implements OnClickListener {
	private RelativeLayout mYouMengRootLayout = null;
	private Activity mActivity = null;
	private SharePlatformUtil mSharePlatform;
	private ProxyThirdShare mShareBoard = null;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";
	private int txtColor = 0;
	/** 标志是否退出 */
	private boolean mIsExit = false;

	public ShareDeal(Activity activity, RelativeLayout rootLayout) {
		mActivity = activity;
		mYouMengRootLayout = rootLayout;
		mSharePlatform = new SharePlatformUtil(mActivity);
		txtColor = mActivity.getResources().getColor(R.color.youmeng_share_txt_color);
		if (1 == ProxyThirdShare.type) {
			initView();
		} else {
			initAbroadLayout();
		}
	}

	private void initAbroadLayout() {
		View rootView = LayoutInflater.from(mActivity).inflate(R.layout.custom_board_2, null);
		rootView.findViewById(R.id.instagram_layout).setVisibility(View.VISIBLE);
		rootView.findViewById(R.id.share_instagram).setOnClickListener(this);
		rootView.findViewById(R.id.share_facebook).setOnClickListener(this);
		rootView.findViewById(R.id.share_twitter).setOnClickListener(this);
		rootView.findViewById(R.id.share_whatsapp).setOnClickListener(this);
		rootView.findViewById(R.id.share_line).setOnClickListener(this);
		// 添加子布局
		addChildView(rootView);
	}

	private void initView() {
		LayoutInflater layoutFlater = LayoutInflater.from(mActivity);
		RelativeLayout aboardLayout = (RelativeLayout) layoutFlater.inflate(R.layout.custom_board, null);
		aboardLayout.setBackgroundResource(R.color.youmeng_share_bg);
		TextView tv = (TextView) aboardLayout.findViewById(R.id.share_text);
		tv.setTextSize(13);
		tv.setTextColor(txtColor);
		aboardLayout.findViewById(R.id.wechat).setOnClickListener(this);
		aboardLayout.findViewById(R.id.wechat_circle).setOnClickListener(this);
		aboardLayout.findViewById(R.id.qq).setOnClickListener(this);
		aboardLayout.findViewById(R.id.qqZone).setOnClickListener(this);
		aboardLayout.findViewById(R.id.sina).setOnClickListener(this);
		// 添加子布局
		addChildView(aboardLayout);
	}

	private void addChildView(View view) {
		mYouMengRootLayout.removeAllViews();
		mYouMengRootLayout.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
	}

	/**
	 * 请求分享数据成功后，调用此方法进行第三方分享
	 * 
	 * @author jyf
	 */
	public void toShare(ThirdShareBean bean) {
		mShareBoard = new ProxyThirdShare(mActivity, mSharePlatform, bean);
		mShareBoard.setShareType(mCurrentShareType);
		mShareBoard.click(mCurrentShareType);
	}

	/**
	 * 接受第三方界面分享的返回结果, (参数同Activity中的onActivityResult方法参数一样)
	 * 
	 * @author jyf
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mSharePlatform.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 标志本界面退出，不再响应事件
	 * 
	 * @author jyf
	 */
	public void setExit() {
		mIsExit = true;
		mYouMengRootLayout = null;
		if (null != mShareBoard) {
			mShareBoard.close();
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.wechat) {
			// 微信
			click_WeiXin();
		} else if (id == R.id.wechat_circle) {
			// 朋友圈
			click_WeiXin_Penyou();
		} else if (id == R.id.qq) {
			// QQ
			click_QQ();
		} else if (id == R.id.qqZone) {
			// QQ空间
			click_QQ_KongJian();
		} else if (id == R.id.sina) {
			// 新浪微博
			click_Sina();
		} else if (id == R.id.share_instagram) {
			click_instagram();
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

	private void click_instagram() {
		if (!isValid()) {
			return;
		}
		boolean flog = AppInstallationUtil.isAppInstalled(mActivity, GolukConfig.INSTAGRAM_PACKAGE);
		if (!flog) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_instagram_no_install));
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_INSTAGRAM;
		click_deal(mCurrentShareType);
	}

	private void click_facebook() {
		if (!isValid()) {
			return;
		}
		if (!mSharePlatform.isInstallPlatform(SHARE_MEDIA.FACEBOOK)) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_facebook_no_install));
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_FACEBOOK;
		click_deal(mCurrentShareType);
	}

	private void click_twitter() {
		if (!isValid()) {
			return;
		}
		if (!mSharePlatform.isInstallPlatform(SHARE_MEDIA.TWITTER)) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_twitter_no_install));
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_TWITTER;
		click_deal(mCurrentShareType);
	}

	private void click_whatsapp() {
		if (!isValid()) {
			return;
		}

		if (!AppInstallationUtil.isAppInstalled(mActivity, GolukConfig.WTATSAPP_PACKAGE)) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_whatsapp_no_install));
			return;
		}

		mCurrentShareType = IThirdShareFn.TYPE_WHATSAPP;
		click_deal(mCurrentShareType);

	}

	private void click_line() {
		if (!isValid()) {
			return;
		}
		if (!AppInstallationUtil.isAppInstalled(mActivity, GolukConfig.LINE_PACKAGE)) {
			GolukUtils.showToast(mActivity, mActivity.getResources().getString(R.string.str_line_no_install));
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_LINE;
		click_deal(mCurrentShareType);
	}

	/**
	 * 判断点击第三方分享时，是否合法，如果合法再去执行,把不合法的因素全部写在这个方法里
	 * 
	 * @return false/true 不合法/合法
	 * @author jyf
	 */
	private boolean isValid() {
		if (mIsExit) {
			return false;
		}
		if (!GolukApplication.getInstance().isUserLoginSucess) {
			if (null != mActivity && mActivity instanceof BaseActivity) {
				((BaseActivity) mActivity).toLoginBack();
				return false;
			}
		}
		return true;
	}

	/**
	 * 点击微信
	 * 
	 * @author jyf
	 */
	private void click_WeiXin() {
		if (!isValid()) {
			return;
		}
		if (!mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)) {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_no_weixin));
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_WEIXIN;
		click_deal(mCurrentShareType);
	}

	/**
	 * 点击微信朋友圈
	 * 
	 * @author jyf
	 */
	private void click_WeiXin_Penyou() {
		if (!isValid()) {
			return;
		}
		if (!mSharePlatform.isInstallPlatform(SHARE_MEDIA.WEIXIN)) {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_no_weixin));
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_WEIXIN_CIRCLE;
		click_deal(mCurrentShareType);
	}

	/**
	 * 点击QQ
	 * 
	 * @author jyf
	 */
	private void click_QQ() {
		if (!isValid()) {
			return;
		}
		if (mSharePlatform.isInstallPlatform(SHARE_MEDIA.QQ)) {
			mCurrentShareType = IThirdShareFn.TYPE_QQ;
			click_deal(mCurrentShareType);
		} else {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_qq_low_version));
		}
	}

	/**
	 * 点击QQ空间
	 * 
	 * @author jyf
	 */
	private void click_QQ_KongJian() {
		if (!isValid()) {
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_QQ_ZONE;
		click_deal(mCurrentShareType);
	}

	/**
	 * 点击新浪微博
	 * 
	 * @author jyf
	 */
	private void click_Sina() {
		if (!isValid()) {
			return;
		}
		mCurrentShareType = IThirdShareFn.TYPE_WEIBO_XINLANG;
		click_deal(mCurrentShareType);
	}

	/**
	 * 第三方分享，点击事件统一处理
	 * 
	 * @param type
	 *            CustomShareBoard类中的常量定义
	 * @author jyf
	 */
	private void click_deal(String type) {
		if (null != mActivity && mActivity instanceof VideoEditActivity) {
			((VideoEditActivity) mActivity).shareClick(type);
		}
	}
}
