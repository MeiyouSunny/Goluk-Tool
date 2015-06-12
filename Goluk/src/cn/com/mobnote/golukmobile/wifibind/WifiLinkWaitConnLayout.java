package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.util.GolukAnimal;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class WifiLinkWaitConnLayout extends ViewFrame implements OnClickListener {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private ImageView mLoadingImg = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	private TextView mHelpTv = null;

	private Bitmap mLoadBitmap = null;

	private GolukAnimal mLoadingAnimal = null;

	int[] animalRes = { R.drawable.finish_pic_14, R.drawable.finish_pic_13, R.drawable.finish_pic_12,
			R.drawable.finish_pic_11, R.drawable.finish_pic_10, R.drawable.finish_pic_9, R.drawable.finish_pic_8,
			R.drawable.finish_pic_7, R.drawable.finish_pic_6, R.drawable.finish_pic_5, R.drawable.finish_pic_4,
			R.drawable.finish_pic_3, R.drawable.finish_pic_2, R.drawable.finish_pic_1 };

	public WifiLinkWaitConnLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		// initBitmap();
		mLoadingAnimal = new GolukAnimal(animalRes);
		init();
	}

	private void initBitmap() {
		// mLoadBitmap =
		// ImageManager.getBitmapFromResource(R.anim.wifi_linking2);
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_waitconn, null);

		final String text = "等待极路客<font color=\"#0587ff\"> 连接 </font>到手机...";
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_waitconn_txt);
		mInfoTv.setText(Html.fromHtml(text));
		mInfoTv.getPaint().setFakeBoldText(true);

		mHelpTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_waitconn_help);
		mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mHelpTv.setOnClickListener(this);

		mLoadingImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_waitconn_img);
		mLoadingImg.setImageDrawable(mLoadingAnimal.getAnimationDrawable());
		// mLoadingImg.setBackgroundResource(R.anim.wifi_linking2);
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
		// GolukDebugUtils.e("", "jyf-----WifiBind-----layout2-----recyle----");
		// mAnimationDrawable.stop();
		// mAnimationDrawable = null;
		// }
		//
		// if (null != mLoadBitmap) {
		// // GolukUtils.freeBitmap(mLoadBitmap);
		// }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wifi_link_waitconn_help:
			GolukUtils.openUrl(GolukUtils.URL_BIND_CONN_PROBLEM, mContext);
			break;
		}

	}

}
