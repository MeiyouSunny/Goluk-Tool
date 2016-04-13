package cn.com.mobnote.golukmobile.startshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.util.GolukUtils;

public class ShareDeal implements OnClickListener {
	private RelativeLayout mYouMengRootLayout = null;
	private Activity mActivity = null;
	private SharePlatformUtil mSharePlatform;
	private CustomShareBoard mShareBoard = null;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";

	private int txtColor = 0;
	/** 标志是否退出 */
	private boolean mIsExit = false;

	public ShareDeal(Activity activity, RelativeLayout rootLayout) {
		mActivity = activity;
		mYouMengRootLayout = rootLayout;
		mSharePlatform = new SharePlatformUtil(mActivity);
		// mSharePlatform.configPlatforms();// 设置分享平台的参数

		txtColor = mActivity.getResources().getColor(R.color.youmeng_share_txt_color);

		initView();
	}

	private void initView() {
		mYouMengRootLayout.setBackgroundResource(R.color.youmeng_share_bg);
		TextView tv = (TextView) mYouMengRootLayout.findViewById(R.id.share_text);
		tv.setTextSize(13);
		tv.setTextColor(txtColor);
		mYouMengRootLayout.findViewById(R.id.wechat).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.wechat_circle).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.qq).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.qqZone).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.sina).setOnClickListener(this);
	}

	/**
	 * 请求分享数据成功后，调用此方法进行第三方分享
	 * 
	 * @param surl
	 *            分享地址
	 * @param curl
	 *            在线图片地址
	 * @param db
	 * @param tl
	 * @param bitmap
	 * @param inputDeafultStr
	 * @param videoId
	 *            视频 ID
	 * @author jyf
	 */
	public void toShare(String surl, String curl, String db, String tl, Bitmap bitmap, String inputDeafultStr,
			String videoId) {
		mShareBoard = new CustomShareBoard(mActivity, mSharePlatform, surl, curl, db, tl, bitmap, inputDeafultStr,
				videoId);
		mShareBoard.setShareType(mCurrentShareType);
		if (mCurrentShareType.equals(CustomShareBoard.TYPE_WEIXIN)) {
			mShareBoard.click_wechat();
		} else if (mCurrentShareType.equals(CustomShareBoard.TYPE_WEIXIN_CIRCLE)) {
			mShareBoard.click_wechat_circle();
		} else if (mCurrentShareType.equals(CustomShareBoard.TYPE_QQ)) {
			mShareBoard.click_QQ();
		} else if (mCurrentShareType.equals(CustomShareBoard.TYPE_QQ_ZONE)) {
			mShareBoard.click_qqZone();
		} else if (mCurrentShareType.equals(CustomShareBoard.TYPE_WEIBO_XINLANG)) {
			mShareBoard.click_sina();
		}
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
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.wechat:
			// 微信
			click_WeiXin();
			break;
		case R.id.wechat_circle:
			// 朋友圈
			click_WeiXin_Penyou();
			break;
		case R.id.qq:
			// QQ
			click_QQ();
			break;
		case R.id.qqZone:
			// QQ空间
			click_QQ_KongJian();
			break;
		case R.id.sina:
			// 新浪微博
			click_Sina();
			break;
		default:
			break;
		}
	}

	/**
	 * 判断点击第三方分享时，是否合法，如果合法再去执行,把不合法的因素全部写在这个方法里
	 * 
	 * @return false/true 不合法/合法
	 * @author jyf
	 */
	public boolean isValid() {
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
		if (mSharePlatform.isInstallWeiXin()) {
			mCurrentShareType = CustomShareBoard.TYPE_WEIXIN;
			click_deal(mCurrentShareType);
		} else {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_no_weixin));
		}
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
		if (mSharePlatform.isInstallWeiXin()) {
			mCurrentShareType = CustomShareBoard.TYPE_WEIXIN_CIRCLE;
			click_deal(mCurrentShareType);
		} else {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_no_weixin));
		}
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
		if (mSharePlatform.isInstallQQ()) {
			mCurrentShareType = CustomShareBoard.TYPE_QQ;
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
		if (mSharePlatform.isInstallQQ()) {
			mCurrentShareType = CustomShareBoard.TYPE_QQ_ZONE;
			click_deal(mCurrentShareType);
		} else {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_qq_low_version));
		}
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
		mCurrentShareType = CustomShareBoard.TYPE_WEIBO_XINLANG;
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
