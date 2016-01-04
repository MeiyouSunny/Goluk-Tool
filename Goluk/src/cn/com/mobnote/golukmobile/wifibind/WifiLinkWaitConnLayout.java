package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.util.GolukAnimal;

public class WifiLinkWaitConnLayout extends ViewFrame implements OnClickListener {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private ImageView mLoadingImg = null;
	private TextView mHelpTv = null;

	private GolukAnimal mLoadingAnimal = null;

	int[] animalRes_g = { R.drawable.finish_pic_14, R.drawable.finish_pic_13, R.drawable.finish_pic_12,
			R.drawable.finish_pic_11, R.drawable.finish_pic_10, R.drawable.finish_pic_9, R.drawable.finish_pic_8,
			R.drawable.finish_pic_7, R.drawable.finish_pic_6, R.drawable.finish_pic_5, R.drawable.finish_pic_4,
			R.drawable.finish_pic_3, R.drawable.finish_pic_2, R.drawable.finish_pic_1 };

	int[] animalRes_t = { R.drawable.finish_t1_pic_14, R.drawable.finish_t1_pic_13, R.drawable.finish_t1_pic_12,
			R.drawable.finish_t1_pic_11, R.drawable.finish_t1_pic_10, R.drawable.finish_t1_pic_9,
			R.drawable.finish_t1_pic_8, R.drawable.finish_t1_pic_7, R.drawable.finish_t1_pic_6,
			R.drawable.finish_t1_pic_5, R.drawable.finish_t1_pic_4, R.drawable.finish_t1_pic_3,
			R.drawable.finish_t1_pic_2, R.drawable.finish_t1_pic_1 };

	public WifiLinkWaitConnLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		mLoadingAnimal = new GolukAnimal(getAnimal());
		init();
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_waitconn, null);
		String wait = mContext.getResources().getString(R.string.wifi_link_complete_wait);
		String conn = mContext.getResources().getString(R.string.wifi_link_complete_conn);
		String mobile = mContext.getResources().getString(R.string.wifi_link_complete_mobile);
		final String text = wait + "<font color=\"#0587ff\"> " + conn + " </font>" + mobile;
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_waitconn_txt);
		mInfoTv.setText(Html.fromHtml(text));
		mHelpTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_waitconn_help);
		mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mHelpTv.setOnClickListener(this);

		mLoadingImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_waitconn_img);
		mLoadingImg.setImageDrawable(mLoadingAnimal.getAnimationDrawable());
	}

	private int[] getAnimal() {
		if (null != mContext && mContext instanceof WiFiLinkCompleteActivity) {
			String type = ((WiFiLinkCompleteActivity) mContext).getCurrentIpcType();
			if (IPCControlManager.MODEL_T.equals(type)) {
				return animalRes_t;
			} else {
				return animalRes_g;
			}
		}
		return animalRes_t;
	}

	@Override
	public void start() {
		mLoadingAnimal.start();
	}

	@Override
	public void free() {
		mLoadingAnimal.free();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wifi_link_waitconn_help:
			Intent itSkill = new Intent(mContext, UserOpenUrlActivity.class);
			itSkill.putExtra(UserOpenUrlActivity.FROM_TAG, "skill");
			mContext.startActivity(itSkill);
			break;
		}
	}

}
