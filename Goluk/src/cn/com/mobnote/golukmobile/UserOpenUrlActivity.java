package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
import cn.com.mobnote.user.MyProgressWebView;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_open_url_layout);

		mApp = (GolukApplication) getApplication();

		initView();
	}

	public void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mWebView = (MyProgressWebView) findViewById(R.id.my_webview);
		
		if(null == mLoadingDialog){
			mLoadingDialog = new CustomLoadingDialog(this, "页面加载中");
		}
		
		Intent itIndexMore = getIntent();
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				closeLoading();
			}
		});

		if (!itIndexMore.getExtras().toString().equals("")) {
			String from_tag = itIndexMore.getStringExtra(FROM_TAG).toString();
			mLoadingDialog.show();
			mBackBtn.setEnabled(false);
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
		}
		mBackBtn.setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
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
			finish();
			break;

		default:
			break;
		}
	}
	
	private void closeLoading(){
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
}