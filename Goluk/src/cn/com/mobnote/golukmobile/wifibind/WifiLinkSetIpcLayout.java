package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.view.LayoutInflater;

public class WifiLinkSetIpcLayout extends ViewFrame {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;

	public WifiLinkSetIpcLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		init();
	}

	private void init() {

	}

}
