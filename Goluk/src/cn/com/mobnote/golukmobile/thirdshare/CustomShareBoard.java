package cn.com.mobnote.golukmobile.thirdshare;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.umeng.socialize.Config;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

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
	private Activity mActivity;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";

	private SharePlatformUtil sharePlatform;

	private String shareurl = "";
	private String mImageUrl = "";
	private String mDescribe = "";
	private String mTitle = "";
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
		mImageUrl = curl;
		mDescribe = db;
		mTitle = tl;
		mThumbBitmap = bitmap;
		mSinaTxt = realDesc;
		mVideoId = videoId;
		modifyUMDialog();
		initView(activity);
	}
	
	private void modifyUMDialog() {
		ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setMessage(mActivity.getString(R.string.str_um_share_dialog_txt));
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				setCanJump();
			}
		});
		Config.dialog = dialog;
	}

	private void printStr() {
		GolukDebugUtils.e("", "CustomShareBoard-----shareurl: " + shareurl + "   coverurl:" + mImageUrl
				+ "   describe:" + mDescribe + " ttl: " + mTitle);
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
		if (!UserUtils.isNetDeviceAvailable(mActivity)) {
			GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_network_unavailable));
			return;
		}
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
		performShare(SHARE_MEDIA.WEIXIN);
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
		performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
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
		performShare(SHARE_MEDIA.QQ);
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
		performShare(SHARE_MEDIA.QZONE);
	}

	public void click_sina() {
		GolukDebugUtils.e("", "sina-------click----11111");
		if (TextUtils.isEmpty(mImageUrl)) {
			Glide.with(mActivity).load(R.drawable.ic_launcher).asBitmap().into(new SimpleTarget<Bitmap>(50, 50) {
				@Override
				public void onLoadFailed(Exception e, Drawable errorDrawable) {
					dismiss();
				}

				@Override
				public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
					dismiss();
					doSinaShare(arg0, arg1);
				}
			});
		} else {
			Glide.with(mActivity).load(mImageUrl).asBitmap().into(new SimpleTarget<Bitmap>(50, 50) {
				@Override
				public void onLoadFailed(Exception e, Drawable errorDrawable) {
					dismiss();
				}

				@Override
				public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
					dismiss();
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

	// 分享成功后的回调
	private void callBack_ShareSuccess() {
		// GolukUtils.showToast(mActivity,
		// mActivity.getString(R.string.str_share_success));
		notifyShareState(true);
		setCanJump();
		if (GolukUtils.isActivityAlive(mActivity)) {
			dismiss();
		}
	}

	// 分享失败的回调
	private void callBack_ShareFailed() {
		GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----1");
		notifyShareState(false);
		GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_share_fail));
		// 分享失败的时候，保证下次还可以点击
		setCanJump();
		if (GolukUtils.isActivityAlive(mActivity)) {
			GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----3");
			dismiss();
		}
		GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----4");
	}

	Handler mHander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 100:
				callBack_ShareSuccess();
				break;
			case 101:
				callBack_ShareFailed();
				break;
			case 102:
				GolukUtils.showToast(mActivity, mActivity.getString(R.string.um_share_cancel));
				break;
			default:
				break;
			}
		}

	};

	private void setCanJump() {
		if (null != mActivity && mActivity instanceof BaseActivity) {
			GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----2");
			((BaseActivity) mActivity).setCanJump();
		}
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

	/**
	 * 上报分享服务器
	 * 
	 * @author jyf
	 * @date 2015年8月11日
	 */
	public void shareUp() {
		GolukDebugUtils.e("", "jyf----thirdshare--------share up: " + "   mVideoId:" + mVideoId);
		if (this.mVideoId != null && !"".equals(this.mVideoId)) {
			GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(mCurrentShareType, this.mVideoId);
		}
	}

	private void notifyShareState(boolean isSucess) {
		if (null == mActivity) {
			return;
		}

		if (mActivity instanceof VideoEditActivity) {
			((VideoEditActivity) mActivity).shareCallBack(isSucess);
		}
	}

	private ShareContent getShareContent(String shareType) {
		if (null == shareType) {
			return null;
		}
		final String videoUrl = shareurl + "&type=" + shareType;
		final UMImage image = new UMImage(mActivity, mImageUrl);
		ShareContent sc = new ShareContent();
		sc.mTitle = mTitle;
		sc.mText = mDescribe;
		sc.mTargetUrl = videoUrl;
		sc.mMedia = image;
		return sc;
	}

	private boolean isCanClick() {
		if (null != mActivity && mActivity instanceof BaseActivity) {
			if (!((BaseActivity) mActivity).isAllowedClicked()) {
				return false;
			}
			((BaseActivity) mActivity).setJumpToNext();
			return true;
		}
		return false;
	}

	private void performShare(SHARE_MEDIA platform) {
	}

}
