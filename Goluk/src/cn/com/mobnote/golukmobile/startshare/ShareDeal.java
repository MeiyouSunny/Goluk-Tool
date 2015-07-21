package cn.com.mobnote.golukmobile.startshare;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.umeng.widget.CustomShareBoard;

public class ShareDeal implements OnClickListener {
	private RelativeLayout mYouMengRootLayout = null;
	private Activity mActivity = null;
	private SharePlatformUtil mSharePlatform;
	private CustomShareBoard mShareBoard = null;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";
	
	private int txtColor = 0;

	public ShareDeal(Activity activity, RelativeLayout rootLayout) {
		mActivity = activity;
		mYouMengRootLayout = rootLayout;
		mSharePlatform = new SharePlatformUtil(mActivity);
		mSharePlatform.configPlatforms();// 设置分享平台的参数
		
		txtColor = mActivity.getResources().getColor(R.color.youmeng_share_txt_color);

		initView();
	}

	private void initView() {
		mYouMengRootLayout.setBackgroundResource(R.color.youmeng_share_bg);
		TextView tv =(TextView) mYouMengRootLayout.findViewById(R.id.share_text);
		tv.setTextSize(13);
		tv.setTextColor(txtColor);
		mYouMengRootLayout.findViewById(R.id.wechat).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.wechat_circle).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.qq).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.qqZone).setOnClickListener(this);
		mYouMengRootLayout.findViewById(R.id.sina).setOnClickListener(this);
	}

	public void toShare(String surl, String curl, String db, String tl) {
		mShareBoard = new CustomShareBoard(mActivity, mSharePlatform, surl, curl, db, tl);
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

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.wechat:
			// 微信
			mCurrentShareType = CustomShareBoard.TYPE_WEIXIN;
			click_deal(mCurrentShareType);
			break;
		case R.id.wechat_circle:
			// 朋友圈
			mCurrentShareType = CustomShareBoard.TYPE_WEIXIN_CIRCLE;
			click_deal(mCurrentShareType);
			break;
		case R.id.qq:
			// QQ
			mCurrentShareType = CustomShareBoard.TYPE_QQ;
			click_deal(mCurrentShareType);
			break;
		case R.id.qqZone:
			// QQ空间
			mCurrentShareType = CustomShareBoard.TYPE_QQ_ZONE;
			click_deal(mCurrentShareType);
			break;
		case R.id.sina:
			// 新浪微博
			mCurrentShareType = CustomShareBoard.TYPE_WEIBO_XINLANG;
			click_deal(mCurrentShareType);
			break;
		default:
			break;
		}

	}

	private void click_deal(String type) {
		if (null != mActivity && mActivity instanceof VideoEditActivity) {
			((VideoEditActivity) mActivity).shareClick(type);
		}
	}

}
