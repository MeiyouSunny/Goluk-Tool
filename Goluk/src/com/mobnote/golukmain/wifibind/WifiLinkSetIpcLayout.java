package com.mobnote.golukmain.wifibind;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.util.GolukAnimal;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiLinkSetIpcLayout extends ViewFrame {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private ImageView mLoadingImg = null;
	/** 加载中动画对象 */
	private GolukAnimal mLoadingAnimal = null;

	int[] animalRes_g = { R.drawable.ipcbind_g_direct_bg, R.drawable.ipcbind_g_direct_gif_1,
			R.drawable.ipcbind_g_direct_gif_2, R.drawable.ipcbind_g_direct_gif_3 };

	int[] animalRes_t = { R.drawable.ipcbind_t_direct_gif_0, R.drawable.ipcbind_t_direct_gif_1,
			R.drawable.ipcbind_t_direct_gif_2, R.drawable.ipcbind_t_direct_gif_3 };

	public WifiLinkSetIpcLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		mLoadingAnimal = new GolukAnimal(getAnimal());
		init();
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_setipcmsg, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_setipc_txt1);
		String foryou = mContext.getResources().getString(R.string.wifi_link_complete_foryou);
		String config = mContext.getResources().getString(R.string.wifi_link_complete_config);
		String wifi = mContext.getResources().getString(R.string.wifi_link_complete_golukwifi);
		final String text = foryou + "<font color=\"#0587ff\"> " + config + " </font>" + wifi;
		mInfoTv.setText(Html.fromHtml(text));
		mLoadingImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_setipc_img);
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
	public void stop() {
		if (null != mLoadingAnimal) {
			mLoadingAnimal.stopAnimal();
		}
	}

	@Override
	public void start() {
		mLoadingAnimal.start();
	}

	@Override
	public void free() {
		mLoadingAnimal.free();
	}

}
