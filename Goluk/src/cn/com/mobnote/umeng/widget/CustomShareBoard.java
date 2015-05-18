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
import cn.com.mobnote.golukmobile.VideoShareActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquarePlayActivity;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;

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
	/** 微信朋友圈 */
	public final String TYPE_WEIXIN_CIRCLE = "5";
	/** 短信 */
	public final String TYPE_SMS = "6";

	private UMSocialService mController = UMServiceFactory.getUMSocialService(Constants.DESCRIPTOR);
	private Activity mActivity;
	/** 保存当前的分享方式 */
	private String mCurrentShareType = "2";

	public CustomShareBoard(Activity activity) {
		super(activity);
		this.mActivity = activity;
		initView(activity);
	}

	@SuppressWarnings("deprecation")
	private void initView(Context context) {
		View rootView = LayoutInflater.from(context).inflate(R.layout.custom_board, null);
		rootView.findViewById(R.id.wechat).setOnClickListener(this);
		rootView.findViewById(R.id.wechat_circle).setOnClickListener(this);
		rootView.findViewById(R.id.qq).setOnClickListener(this);
		rootView.findViewById(R.id.sms).setOnClickListener(this);
		//rootView.findViewById(R.id.sina).setOnClickListener(this);
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
			
			mCurrentShareType = TYPE_WEIXIN;
			performShare(SHARE_MEDIA.WEIXIN);
			break;
		case R.id.wechat_circle:
			mCurrentShareType = TYPE_WEIXIN_CIRCLE;
			performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
			break;
		case R.id.qq:
			@SuppressWarnings("static-access")
			Boolean isQQ=mController.getConfig().isSupportQQZoneSSO(mActivity);
			mCurrentShareType = TYPE_QQ;
			if(isQQ){
				performShare(SHARE_MEDIA.QQ);
			}else{
				Toast.makeText(mActivity, "你还没有安装QQ或版本太低",Toast.LENGTH_SHORT).show();
			}
			
			break;
		case R.id.sms:
			mCurrentShareType = TYPE_SMS;
			performShare(SHARE_MEDIA.SMS);
			break;
//		case R.id.sina:
//			mCurrentShareType = TYPE_WEIBO_XINLANG;
//			performShare(SHARE_MEDIA.SINA);
//			break;
		case R.id.share_cancel:
			dismiss();
			break;
		default:
			break;
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
				System.out.println("mCurrentShareType------"+mCurrentShareType);
				//String showText = platform.toString();
				if (eCode == StatusCode.ST_CODE_SUCCESSED) {
					if (mActivity instanceof VideoShareActivity) {
						((VideoShareActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
					}else if (mActivity instanceof MainActivity){
						((MainActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
					} else if(mActivity instanceof VideoSquarePlayActivity){
						((VideoSquarePlayActivity) mActivity).shareSucessDeal(true, mCurrentShareType);
					}
					//showText += "平台分享成功";
				} else {
					//showText += "平台分享失败";
					if (mActivity instanceof VideoShareActivity) {
						((VideoShareActivity) mActivity).shareSucessDeal(false, null);
					}
				}
				//mCurrentShareType = null;
				//Toast.makeText(mActivity, showText,Toast.LENGTH_SHORT).show();
				dismiss();
			}
		});
	}

}
