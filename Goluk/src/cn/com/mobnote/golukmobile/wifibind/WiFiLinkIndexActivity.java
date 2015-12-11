package cn.com.mobnote.golukmobile.wifibind;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventFinishWifiActivity;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * @ 功能描述:wifi链接首页
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkIndexActivity extends BaseActivity implements OnClickListener {

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	private TextView mDescTitleText2 = null;
	/** 继续按钮 */
	private Button mKeepBtn = null;
	private TextView mHelpTv = null;
	private ImageView mMiddleImg = null;
	private ImageView mProgressImg = null;

	private Bitmap mMiddleBitmap = null;
	private Bitmap mProgressBitmap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_index);
		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "WiFiLinkIndex");

		// 断开连接
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		// 改变Application-IPC退出登录
		mApp.setIpcLoginOut();
		// 页面初始化
		initBitmap();
		init();
		// Register EventBus
		EventBus.getDefault().register(this);
	}

	private void initBitmap() {
		mMiddleBitmap = ImageManager.getBitmapFromResource(R.drawable.connect_banner_1);
		mProgressBitmap = ImageManager.getBitmapFromResource(R.drawable.setp_1);
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mDescTitleText2 = (TextView) findViewById(R.id.textView2);
		mKeepBtn = (Button) findViewById(R.id.keep_btn);
		// 注册事件
		mBackBtn.setOnClickListener(this);
		mKeepBtn.setOnClickListener(this);
		// 修改title说明文字颜色
		String need = this.getResources().getString(R.string.wifi_link_index_need);
		String mobile = this.getResources().getString(R.string.wifi_link_index_mobile);
		String goluk = this.getResources().getString(R.string.app_name);
		String and = this.getResources().getString(R.string.wifi_link_index_and);
		String wificonn = this.getResources().getString(R.string.wifi_link_index_wificonn);
		final String showTxt = need + "<font color=\"#0587ff\"> " + mobile + " </font> " + and
				+ " <font color=\"#0587ff\"> " + goluk + "  </font>" + wificonn;
		mDescTitleText2.setText(Html.fromHtml(showTxt));

		mHelpTv = (TextView) findViewById(R.id.wifi_link_index_help);
		// 下划线
		mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mHelpTv.setOnClickListener(this);

		mMiddleImg = (ImageView) findViewById(R.id.imageView1);
		mProgressImg = (ImageView) findViewById(R.id.wifilink_progress);

		mMiddleImg.setImageBitmap(mMiddleBitmap);
		mProgressImg.setImageBitmap(mProgressBitmap);
	}

	@Override
	protected void onResume() {
		mApp.setContext(this, "WiFiLinkIndex");
		super.onResume();
	}

	private void free() {
		if (null != mMiddleImg) {
			GolukUtils.freeBitmap(mMiddleBitmap);
		}
		if (null != mProgressImg) {
			GolukUtils.freeBitmap(mProgressBitmap);
		}
	}

	public void onEventMainThread(EventFinishWifiActivity event) {
		finish();
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
		GolukDebugUtils.e("", "jyf-----WifiBind-----Index-----onDestroy----");
		free();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			// 返回
			finish();
			break;
		case R.id.keep_btn:
			Intent list = new Intent(WiFiLinkIndexActivity.this, WiFiLinkListActivity.class);
			startActivity(list);
			break;
		case R.id.wifi_link_index_help:
			Intent mHelpIntent = new Intent(this, UserOpenUrlActivity.class);
			mHelpIntent.putExtra(UserOpenUrlActivity.FROM_TAG, "wifihelp");
			startActivity(mHelpIntent);
			break;
		default:
			break;
		}
	}

}
