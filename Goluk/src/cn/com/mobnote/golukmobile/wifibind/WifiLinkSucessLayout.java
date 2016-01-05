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
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WifiLinkSucessLayout extends ViewFrame implements OnClickListener {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private TextView mModifyPwdTv = null;

	private ImageView mMiddleImg = null;
	private Bitmap mMiddleBitmap = null;

	public WifiLinkSucessLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		initBitmap();
		init();
	}

	private void initBitmap() {
		mMiddleBitmap = ImageManager.getBitmapFromResource(getAnimal());
	}

	private int getAnimal() {
		if (null != mContext && mContext instanceof WiFiLinkCompleteActivity) {
			String type = ((WiFiLinkCompleteActivity) mContext).getCurrentIpcType();
			if (IPCControlManager.MODEL_T.equals(type)) {
				return R.drawable.ipcbind_finish_t_success;
			} else {
				return R.drawable.ipcbind_finish_g_success;
			}
		}
		return R.drawable.ipcbind_finish_g_success;
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_sucess, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_sucess_txt2);
		String yourgoluk = mContext.getResources().getString(R.string.wifi_link_complete_your_goluk);
		String success = mContext.getResources().getString(R.string.wifi_link_complete_sucess_conn);
		String mobile = mContext.getResources().getString(R.string.wifi_link_complete_mobile);
		final String text = yourgoluk + "<font color=\"#0587ff\"> " + success + " </font>" + mobile;
		mInfoTv.setText(Html.fromHtml(text));
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
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mContext.startActivity(i);
			break;
		}
	}

}
