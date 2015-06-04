package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;

public class WifiLinkSucessLayout extends ViewFrame {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;

	public WifiLinkSucessLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		init();
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_sucess, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_sucess_txt2);

		final String text = "你的极路客<font color=\"#0587ff\"> 成功连接 </font>到手机...";
		mInfoTv.setText(Html.fromHtml(text));
	}

}
