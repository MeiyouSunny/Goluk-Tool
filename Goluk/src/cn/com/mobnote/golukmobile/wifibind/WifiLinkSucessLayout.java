package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.mobnote.eventbus.EventFinishWifiActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UnbindActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WifiLinkSucessLayout extends ViewFrame implements OnClickListener {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private TextView mModifyPwdTv = null;
	private TextView mWifi_link_sucess_txt1 = null;

	private ImageView mMiddleImg = null;
	private Bitmap mMiddleBitmap = null;

	public WifiLinkSucessLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		initBitmap();
		init();
	}

	private void initBitmap() {
		mMiddleBitmap = ImageManager.getBitmapFromResource(R.drawable.connect_banner_4);
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_sucess, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_sucess_txt2);

		final String text = "你的极路客<font color=\"#0587ff\"> 成功连接 </font>到手机...";
		mInfoTv.setText(Html.fromHtml(text));
		mInfoTv.getPaint().setFakeBoldText(true);

		mWifi_link_sucess_txt1 = (TextView) mRootLayout.findViewById(R.id.wifi_link_sucess_txt1);
		mWifi_link_sucess_txt1.getPaint().setFakeBoldText(true);

		mModifyPwdTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_sucess_modify);
		// 设置下画线
		mModifyPwdTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mModifyPwdTv.setOnClickListener(this);

		mMiddleImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_sucess_img);
		mMiddleImg.setImageBitmap(mMiddleBitmap);
	}

	@Override
	public void free() {
		if (null != mMiddleBitmap) {
			GolukDebugUtils.e("", "jyf-----WifiBind-----layout3-----recyle----");
			GolukUtils.freeBitmap(mMiddleBitmap);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wifi_link_sucess_modify:
			EventBus.getDefault().post(new EventFinishWifiActivity());
			Intent i = new Intent(mContext, UnbindActivity.class);
			mContext.startActivity(i);
			break;
		}
	}

}
