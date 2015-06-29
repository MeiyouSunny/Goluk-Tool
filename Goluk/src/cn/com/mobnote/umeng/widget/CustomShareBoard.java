/**
 * 
 */

package cn.com.mobnote.umeng.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.VideoShareActivity;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquarePlayActivity;
import cn.com.mobnote.util.GolukUtils;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SnsPlatform;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

/**
 * 
 */
public class CustomShareBoard extends PopupWindow implements OnClickListener {

	/** 微信 */
	private final String TYPE_WEIXIN = "2";
	/** 微博 */
	public final String TYPE_WEIBO_XINLANG = "3";
	/** QQ */
	public final String TYPE_QQ = "4";
	/** QQ空间 **/
	public final String TYPE_QQ_ZONE = "7";
	/** 微信朋友圈 */
	public final String TYPE_WEIXIN_CIRCLE = "5";
	/** 短信 */
	public final String TYPE_SMS = "6";

	private UMSocialService mController = UMServiceFactory.getUMSocialService(Constants.DESCRIPTOR);
	private Activity mActivity;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";

	SharePlatformUtil sharePlatform;

	String shareurl = "";
	String coverurl = "";
	String describe = "";
	String ttl = "";

	public CustomShareBoard(Activity activity, SharePlatformUtil spf, String surl, String curl, String db,String tl) {
		super(activity);
		this.mActivity = activity;
		sharePlatform = spf;
		shareurl = surl;
		coverurl = curl;
		describe = db;
		ttl = tl;
		initView(activity);
	}

	@SuppressWarnings("deprecation")
	private void initView(Context context) {
		View rootView = LayoutInflater.from(context).inflate(R.layout.custom_board, null);
		rootView.findViewById(R.id.wechat).setOnClickListener(this);
		rootView.findViewById(R.id.wechat_circle).setOnClickListener(this);
		rootView.findViewById(R.id.qq).setOnClickListener(this);
		rootView.findViewById(R.id.qqZone).setOnClickListener(this);
		rootView.findViewById(R.id.sina).setOnClickListener(this);
		rootView.findViewById(R.id.share_cancel).setOnClickListener(this);
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
			System.out.println("zh======wx"+shareurl + coverurl + describe + ttl);
			sharePlatform.setShareContent(shareurl+"&type=2", coverurl, describe,ttl);
			mCurrentShareType = TYPE_WEIXIN;
			this.shareUp();//上报分享统计
			performShare(SHARE_MEDIA.WEIXIN);
			break;
		case R.id.wechat_circle:
			sharePlatform.setShareContent(shareurl+"&type=5", coverurl, describe,ttl);
			mCurrentShareType = TYPE_WEIXIN_CIRCLE;
			this.shareUp();//上报分享统计
			performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
			break;
		case R.id.qq:
			@SuppressWarnings("static-access")
			Boolean isQQ = mController.getConfig().isSupportQQZoneSSO(mActivity);
			mCurrentShareType = TYPE_QQ;
			this.shareUp();//上报分享统计
			if (isQQ) {
				sharePlatform.setShareContent(shareurl+"&type=4", coverurl, describe,ttl);
				performShare(SHARE_MEDIA.QQ);
			} else {
				GolukUtils.showToast(mActivity, "你还没有安装QQ或版本太低"); 
			}

			break;
		case R.id.qqZone:
			sharePlatform.setShareContent(shareurl+"&type=7", coverurl, describe,ttl);
			mCurrentShareType = TYPE_QQ_ZONE;
			this.shareUp();//上报分享统计
			performShare(SHARE_MEDIA.QZONE);
			break;
		 case R.id.sina:
			 sharePlatform.setShareContent(shareurl+"&type=3", coverurl, describe,ttl);
			 mCurrentShareType = TYPE_WEIBO_XINLANG;
			 this.shareUp();//上报分享统计
			 performShare(SHARE_MEDIA.SINA);
			 break;
		case R.id.share_cancel:
			dismiss();
			break;
		default:
			break;
		}
	}
	
	public void shareUp(){
		if (mActivity instanceof VideoShareActivity) {
			((VideoShareActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof MainActivity) {
			((MainActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof VideoSquarePlayActivity) {
			((VideoSquarePlayActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
		} else if (mActivity instanceof LiveActivity) {
			((LiveActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
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
