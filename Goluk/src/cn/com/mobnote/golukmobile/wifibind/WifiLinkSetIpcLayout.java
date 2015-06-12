package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.util.GolukAnimal;
import cn.com.tiros.debug.GolukDebugUtils;

public class WifiLinkSetIpcLayout extends ViewFrame {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private ImageView mLoadingImg = null;
	/** 加载中动画对象 */
	// private AnimationDrawable mAnimationDrawable = null;
	private Bitmap mLoadBitmap = null;

	private GolukAnimal mLoadingAnimal = null;

	int[] animalRes = { R.drawable.connect_gif_ipc_wifi1, R.drawable.connect_gif_ipc_wifi2,
			R.drawable.connect_gif_ipc_wifi3, R.drawable.connect_gif_ipc_wifi4 };

	public WifiLinkSetIpcLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		// initBitmap();
		mLoadingAnimal = new GolukAnimal(animalRes);
		init();
	}

	private void initBitmap() {
		// mLoadBitmap = ImageManager.getBitmapFromResource(R.anim.goluk_wifi);
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_setipcmsg, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_setipc_txt1);
		final String text = "正在为你<font color=\"#0587ff\"> 配置 </font>极路客Wifi";
		mInfoTv.setText(Html.fromHtml(text));
		mInfoTv.getPaint().setFakeBoldText(true);

		mLoadingImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_setipc_img2);
		mLoadingImg.setImageDrawable(mLoadingAnimal.getAnimationDrawable());
		// mAnimationDrawable = (AnimationDrawable) mLoadingImg.getBackground();
	}

	@Override
	public void start() {
		mLoadingAnimal.start();
		// mLoadingImg.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// if (mAnimationDrawable != null) {
		// if (!mAnimationDrawable.isRunning()) {
		// mAnimationDrawable.start();
		// }
		// }
		// }
		// }, 100);
	}

	@Override
	public void free() {
		mLoadingAnimal.free();
		// if (null != mAnimationDrawable) {
		// GolukDebugUtils.e("", "jyf-----WifiBind-----layout1-----recyle----");
		// mAnimationDrawable.stop();
		// mAnimationDrawable = null;
		//
		// // GolukUtils.freeBitmap(mLoadBitmap);
		// }
	}

}
