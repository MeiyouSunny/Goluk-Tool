package cn.com.mobnote.golukmobile.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoSquareDeatilActivity;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.startshare.VideoShareActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquarePlayActivity;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;

public class CustomShareBoard extends PopupWindow implements OnClickListener {

	/** 新浪微博支持多条消息的值，大于等于字个值，就支持多条消息，否则只支持单条消息 */
	private final int SUPPORT_MUTI_MSG = 10351;

	/** 微信 */
	public static final String TYPE_WEIXIN = "2";
	/** 微博 */
	public static final String TYPE_WEIBO_XINLANG = "3";
	/** QQ */
	public static final String TYPE_QQ = "4";
	/** 微信朋友圈 */
	public static final String TYPE_WEIXIN_CIRCLE = "5";
	/** 短信 */
	public static final String TYPE_SMS = "6";
	/** QQ空间 **/
	public static final String TYPE_QQ_ZONE = "7";

	private UMSocialService mController = UMServiceFactory.getUMSocialService(Constants.DESCRIPTOR);
	private Activity mActivity;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";

	SharePlatformUtil sharePlatform;

	String shareurl = "";
	String coverurl = "";
	String describe = "";
	String ttl = "";
	Bitmap mThumbBitmap = null;
	private String mRealDesc = null;

	public CustomShareBoard(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
			Bitmap bitmap, String realDesc) {

		super(activity);
		this.mActivity = activity;
		sharePlatform = spf;
		shareurl = surl;
		coverurl = curl;
		describe = db;
		ttl = tl;
		mThumbBitmap = bitmap;
		mRealDesc = realDesc;

		initView(activity);
	}

	private void printStr() {
		GolukDebugUtils.e("", "CustomShareBoard-----shareurl: " + shareurl + "   coverurl:" + coverurl + "   describe:"
				+ describe + " ttl: " + ttl);
	}

	@SuppressWarnings("deprecation")
	private void initView(Context context) {
		View rootView = LayoutInflater.from(context).inflate(R.layout.custom_board, null);
		rootView.findViewById(R.id.wechat).setOnClickListener(this);
		rootView.findViewById(R.id.wechat_circle).setOnClickListener(this);
		rootView.findViewById(R.id.qq).setOnClickListener(this);
		rootView.findViewById(R.id.qqZone).setOnClickListener(this);
		rootView.findViewById(R.id.sina).setOnClickListener(this);
		setContentView(rootView);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());
		setTouchable(true);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.wechat:
			click_wechat();
			break;
		case R.id.wechat_circle:
			click_wechat_circle();
			break;
		case R.id.qq:
			click_QQ();
			break;
		case R.id.qqZone:
			click_qqZone();
			break;
		case R.id.sina:
			click_sina();
			break;
		default:
			break;
		}
	}

	public void setShareType(String type) {
		mCurrentShareType = type;
	}

	// 点击　“微信”
	public void click_wechat() {
		System.out.println("zh======wx" + shareurl + coverurl + describe + ttl);
		sharePlatform.setShareContent(shareurl + "&type=2", coverurl, describe, ttl);
		mCurrentShareType = TYPE_WEIXIN;
		this.shareUp();// 上报分享统计
		performShare(SHARE_MEDIA.WEIXIN);
	}

	// 点击　“朋友圈”
	public void click_wechat_circle() {
		sharePlatform.setShareContent(shareurl + "&type=5", coverurl, describe, ttl);
		mCurrentShareType = TYPE_WEIXIN_CIRCLE;
		this.shareUp();// 上报分享统计
		performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
	}

	// 点击　“ＱＱ”
	public void click_QQ() {
		@SuppressWarnings("static-access")
		Boolean isQQ = mController.getConfig().isSupportQQZoneSSO(mActivity);
		mCurrentShareType = TYPE_QQ;
		this.shareUp();// 上报分享统计
		if (isQQ) {
			sharePlatform.setShareContent(shareurl + "&type=4", coverurl, describe, ttl);
			performShare(SHARE_MEDIA.QQ);
		} else {
			GolukUtils.showToast(mActivity, "你还没有安装QQ或版本太低");
		}
	}

	// 点击　“ＱＱ空间”
	public void click_qqZone() {
		sharePlatform.setShareContent(shareurl + "&type=7", coverurl, describe, ttl);
		mCurrentShareType = TYPE_QQ_ZONE;
		this.shareUp();// 上报分享统计
		performShare(SHARE_MEDIA.QZONE);
	}

	public void click_sina() {
		GolukDebugUtils.e("", "sina-------click----11111");
		this.dismiss();
		if (null == sharePlatform) {
			return;
		}

		GolukDebugUtils.e("", "sina-------click----2222");
		if (!sharePlatform.mSinaWBUtils.isAccessValid()) {
			GolukDebugUtils.e("", "sina-------click----3333");
			// 去授权
			sharePlatform.mSinaWBUtils.authorize();
			return;
		}
		mCurrentShareType = TYPE_WEIBO_XINLANG;
		this.shareUp();// 上报分享统计
		printStr();
		final String t_des = describe;
		final String inputDefaultContent = mRealDesc;
		final String title = ttl;
		final String dataUrl = shareurl;
		final String actionUrl = shareurl + "&type=" + TYPE_WEIBO_XINLANG;
		final Bitmap t_bitmap = mThumbBitmap;
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
			// GolukUtils.showToast(mActivity, PROMPT_UNINSTALL);
		}

	}

	public void shareUp() {
		if (mActivity instanceof VideoShareActivity) {
			((VideoShareActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof MainActivity) {
			((MainActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof VideoSquarePlayActivity) {
			((VideoSquarePlayActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof LiveActivity) {
			((LiveActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if(mActivity instanceof VideoSquareDeatilActivity){
			((VideoSquareDeatilActivity)mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof VideoCategoryActivity) {
			((VideoCategoryActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		}
	}

	private void performShare(SHARE_MEDIA platform) {
		mController.getConfig().cleanListeners();

		mController.postShare(mActivity, platform, new SnsPostListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
				System.out.println("mCurrentShareType------" + mCurrentShareType);
				dismiss();
			}
		});
	}

}
