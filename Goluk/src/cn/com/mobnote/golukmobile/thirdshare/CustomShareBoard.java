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
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
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

	private SharePlatformUtil sharePlatform;

	private String shareurl = "";
	private String coverurl = "";
	private String describe = "";
	private String ttl = "";
	private Bitmap mThumbBitmap = null;
	/** 新浪微博Txt */
	private String mSinaTxt = null;
	/** 视频Id ,用户服务器上报　 */
	private String mVideoId = null;

	public CustomShareBoard(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
			Bitmap bitmap, String realDesc, String videoId) {

		super(activity);
		this.mActivity = activity;
		sharePlatform = spf;
		shareurl = surl;
		coverurl = curl;
		describe = db;
		ttl = tl;
		mThumbBitmap = bitmap;
		mSinaTxt = realDesc;
		mVideoId = videoId;

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
		if (sharePlatform.isInstallWeiXin()) {
			if (null != mActivity && mActivity instanceof BaseActivity) {
				if (!((BaseActivity) mActivity).isAllowedClicked()) {
					return;
				}
				((BaseActivity) mActivity).setJumpToNext();
			}
		}
		System.out.println("zh======wx" + shareurl + coverurl + describe + ttl);
		sharePlatform.setShareContent(shareurl + "&type=2", coverurl, describe, ttl);
		mCurrentShareType = TYPE_WEIXIN;
		this.shareUp();// 上报分享统计
		performShare(SHARE_MEDIA.WEIXIN);
	}

	// 点击　“朋友圈”
	public void click_wechat_circle() {
		if (sharePlatform.isInstallWeiXin()) {
			if (null != mActivity && mActivity instanceof BaseActivity) {
				if (!((BaseActivity) mActivity).isAllowedClicked()) {
					return;
				}
				((BaseActivity) mActivity).setJumpToNext();
			}
		}
		sharePlatform.setShareContent(shareurl + "&type=5", coverurl, describe, describe);
		mCurrentShareType = TYPE_WEIXIN_CIRCLE;
		this.shareUp();// 上报分享统计
		performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
	}

	// 点击　“ＱＱ”
	public void click_QQ() {
		if (null != mActivity && mActivity instanceof BaseActivity) {
			if (!((BaseActivity) mActivity).isAllowedClicked()) {
				return;
			}
			((BaseActivity) mActivity).setJumpToNext();
		}
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
		if (null != mActivity && mActivity instanceof BaseActivity) {
			if (!((BaseActivity) mActivity).isAllowedClicked()) {
				return;
			}
			((BaseActivity) mActivity).setJumpToNext();
		}
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
		this.shareUp();// 上报分享统计
		printStr();
		final String t_des = describe;
		final String inputDefaultContent = mSinaTxt;
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

	/**
	 * 上报分享服务器
	 * 
	 * @author jyf
	 * @date 2015年8月11日
	 */
	public void shareUp() {

		GolukDebugUtils.e("", "jyf----thirdshare--------share up: " + "   mVideoId:" + mVideoId);

		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(mCurrentShareType, this.mVideoId);

		// if (mActivity instanceof VideoEditActivity) {
		// ((VideoEditActivity) mActivity).shareSucessDeal(true,
		// mCurrentShareType);
		// } else if (mActivity instanceof MainActivity) {
		// ((MainActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		// } else if (mActivity instanceof LiveActivity) {
		// ((LiveActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		// } else if (mActivity instanceof VideoSquareDeatilActivity) {
		// ((VideoSquareDeatilActivity) mActivity).shareSucessDeal(true,
		// mCurrentShareType);
		// } else if (mActivity instanceof VideoCategoryActivity) {
		// ((VideoCategoryActivity) mActivity).shareSucessDeal(true,
		// mCurrentShareType);
		// }
	}

	private void notifyShareState(boolean isSucess) {
		if (null == mActivity) {
			return;
		}

		if (mActivity instanceof VideoEditActivity) {
			((VideoEditActivity) mActivity).shareCallBack(isSucess);
		}
	}

	private void performShare(SHARE_MEDIA platform) {
		mController.getConfig().cleanListeners();

		mController.postShare(mActivity, platform, new SnsPostListener() {

			@Override
			public void onStart() {
				GolukDebugUtils.e("", "jyf----thirdshare--------onStart: " + "   mCurrentShareType:"
						+ mCurrentShareType);
			}

			@Override
			public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
				// 第一个参数platform是分享的平台
				// 第二个参数是状态码（200代码分享成功，非200表示失败）
				// 第三个参数entity是保存本次分享相关的信息

				if (eCode == 200) {
					GolukUtils.showToast(mActivity, "分享成功");
					notifyShareState(true);
				} else {
					notifyShareState(false);
					GolukUtils.showToast(mActivity, "分享失败");
				}

				GolukDebugUtils.e("", "jyf----thirdshare--------onComplete eCode: " + eCode + "   mCurrentShareType:"
						+ mCurrentShareType);
				dismiss();
			}
		});
	}

}
