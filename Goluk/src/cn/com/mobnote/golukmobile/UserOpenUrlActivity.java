package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
import cn.com.mobnote.user.MyProgressWebView;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 程序内打开浏览器
 * 
 * @author mobnote
 *
 */
public class UserOpenUrlActivity extends BaseActivity implements OnClickListener {

	private GolukApplication mApp = null;
	public static final String FROM_TAG = "from_tag";
	private MyProgressWebView mWebView = null;
	private ImageButton mBackBtn = null;
	private TextView mTextTitle = null;
	private CustomLoadingDialog mLoadingDialog = null;
	/****/
	private Intent itIndexMore = null;
	private TextView mTextRight = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_open_url_layout);

		mApp = (GolukApplication) getApplication();

		initView();
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mWebView = (MyProgressWebView) findViewById(R.id.my_webview);
		mTextRight = (TextView) findViewById(R.id.user_title_right);
		mTextRight.setBackgroundResource(R.drawable.btn_close_image);

		if (null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(this, "页面加载中");
		}

		itIndexMore = getIntent();
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setUseWideViewPort(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);  
		webSettings.setDomStorageEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				String from_tag = itIndexMore.getStringExtra(FROM_TAG);
				if(!TextUtils.isEmpty(from_tag)) {
					if (from_tag.equals("skill")) {
						if (url.contains("tel:")) {
							webviewCall(url);
							return true;
						}
					}
				}
				view.loadUrl(url);
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				GolukDebugUtils.e("webview", "--------onPageFinished--------");
				closeLoading();
			}
		});

		if (!itIndexMore.getExtras().toString().equals("")) {
			String from_tag = itIndexMore.getStringExtra(FROM_TAG);
			mLoadingDialog.show();
			mBackBtn.setEnabled(false);
			if (!TextUtils.isEmpty(from_tag)) {
				if (from_tag.equals("skill")) {
					mTextTitle.setText("极路客小技巧");
					mWebView.loadUrl(getRtmpAddress() + "?type=2");
				} else if (from_tag.equals("install")) {
					mTextTitle.setText("安装指导");
					mWebView.loadUrl(getRtmpAddress() + "?type=3");
				} else if (from_tag.equals("shopping")) {
					mTextTitle.setText("购买极路客");
					mWebView.loadUrl(getRtmpAddress() + "?type=4");
				} else if (from_tag.equals("buyline")) {
					mTextTitle.setText("购买极路客专用降压线");
					mWebView.loadUrl(getRtmpAddress() + "?type=1");
				}
			}else {
				mTextTitle.setText("");
				String url = itIndexMore.getStringExtra("url");
				mWebView.loadUrl(url);
			}
			
		}
		mBackBtn.setOnClickListener(this);
		mTextRight.setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				mWebView.destroy();
				finish();
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				mWebView.destroy();
				finish();
			}
			break;
		case R.id.user_title_right:
			mWebView.destroy();
			this.finish();
			break;
		default:
			break;
		}
	}

	private void closeLoading() {
		mLoadingDialog.close();
		mBackBtn.setEnabled(true);
	}

	/**
	 * 读取UrlConfig
	 */
	private String getRtmpAddress() {
		String rtmpUrl = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_GetServerAddress,
				IGetServerAddressType.GetServerAddress_HttpServer, "UrlRedirect");
		GolukDebugUtils.e("", "jyf-----MainActivity-----test:" + rtmpUrl);
		return rtmpUrl;
	}

	/**
	 * 拨打电话
	 * 
	 * @param phoneNumber
	 */
	public void webviewCall(final String phoneNumber) {
		new AlertDialog.Builder(this).setTitle("提示").setMessage("确定拨打该电话号码？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber));
						startActivity(intent);
					}
				}).setNegativeButton("取消", null).create().show();
	}
}
