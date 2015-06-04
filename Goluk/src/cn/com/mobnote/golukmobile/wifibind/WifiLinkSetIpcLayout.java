package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;

public class WifiLinkSetIpcLayout extends ViewFrame {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private ImageView mLoadingImg = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;

	public WifiLinkSetIpcLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		init();
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_setipcmsg, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_setipc_txt1);
		final String text = "正在为你<font color=\"#0587ff\"> 配置 </font>极路客Wifi";
		mInfoTv.setText(Html.fromHtml(text));
		mInfoTv.getPaint().setFakeBoldText(true);

		mLoadingImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_setipc_img2);
		mAnimationDrawable = (AnimationDrawable) mLoadingImg.getBackground();
	}

	public void start() {
		mLoadingImg.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mAnimationDrawable != null) {
					if (!mAnimationDrawable.isRunning()) {
						mAnimationDrawable.start();
					}
				}
			}
		}, 100);
	}

	public void free() {
		if (null != mAnimationDrawable) {
			mAnimationDrawable.stop();
		}
	}

}
