package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukAnimal;

public class WifiLinkSetIpcLayout extends ViewFrame {
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private TextView mInfoTv = null;
	private ImageView mLoadingImg = null;
	/** 加载中动画对象 */
	private GolukAnimal mLoadingAnimal = null;

	int[] animalRes = { R.drawable.connect_gif_ipc_wifi1, R.drawable.connect_gif_ipc_wifi2,
			R.drawable.connect_gif_ipc_wifi3, R.drawable.connect_gif_ipc_wifi4 };

	public WifiLinkSetIpcLayout(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		mLoadingAnimal = new GolukAnimal(animalRes);
		init();
	}

	private void init() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.wifi_link_setipcmsg, null);
		mInfoTv = (TextView) mRootLayout.findViewById(R.id.wifi_link_setipc_txt1);
		String foryou = mContext.getResources().getString(R.string.wifi_link_complete_foryou);
		String config = mContext.getResources().getString(R.string.wifi_link_complete_config);
		String wifi = mContext.getResources().getString(R.string.wifi_link_complete_golukwifi);
		final String text = foryou + "<font color=\"#0587ff\">" + config + " </font>" + wifi;
		mInfoTv.setText(Html.fromHtml(text));
//		mInfoTv.getPaint().setFakeBoldText(true);

		mLoadingImg = (ImageView) mRootLayout.findViewById(R.id.wifi_link_setipc_img2);
		mLoadingImg.setImageDrawable(mLoadingAnimal.getAnimationDrawable());
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
