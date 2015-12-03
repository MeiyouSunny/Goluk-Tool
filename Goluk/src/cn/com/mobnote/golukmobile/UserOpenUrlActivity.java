package cn.com.mobnote.golukmobile;

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
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog.ForbidBack;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
import cn.com.mobnote.user.MyProgressWebView;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 程序内打开浏览器
 * 
 * @author mobnote
 *
 */
public class UserOpenUrlActivity extends BaseActivity implements OnClickListener, ForbidBack, DownloadListener {

	private GolukApplication mApp = null;
	public static final String FROM_TAG = "from_tag";
	private MyProgressWebView mWebView = null;
	private ImageButton mBackBtn = null;
	private TextView mTextTitle = null;
	private CustomLoadingDialog mLoadingDialog = null;
	/****/
	private Intent itIndexMore = null;
	private TextView mTextRight = null;
	/** 加载webview发生错误状态 **/
	private boolean mErrorState = false;

	private RelativeLayout mErrorLayout = null;
	/**收益页面UI修改**/
	private boolean mProfitChangeUI = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_open_url_layout);

		mApp = (GolukApplication) getApplication();

		initView();
		GolukDebugUtils.e("", "--------UserOpenUrlActivity-------onCreate：");
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mWebView = (MyProgressWebView) findViewById(R.id.my_webview);
		mTextRight = (TextView) findViewById(R.id.user_title_right);
		mTextRight.setBackgroundResource(R.drawable.btn_close_image);
		mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);

		if (null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(this, "页面加载中");
			mLoadingDialog.setListener(this);
		}

		itIndexMore = getIntent();
		mProfitChangeUI = itIndexMore.getBooleanExtra("isChangeUI", false);
		if (mProfitChangeUI) {
			mTextRight.setVisibility(View.GONE);
			mBackBtn.setBackgroundResource(R.drawable.browser_close_btn);
		} else {
			mTextRight.setVisibility(View.VISIBLE);
			mBackBtn.setBackgroundResource(R.drawable.cross_screen_arrow_icon1);
		}
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);  //设置 缓存模式
		mWebView.setDownloadListener(this);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				GolukDebugUtils.e("", "Error--------------------url:" + url);
				String from_tag = itIndexMore.getStringExtra(FROM_TAG);
				if (!TextUtils.isEmpty(from_tag)) {
					if (url.contains("tel:")) {
						webviewCall(url);
						return true;
					}
				}
				// 如果是intent://开头的，不处理
				if (null != url && url.startsWith("intent://")) {
					getIntentPackageName(url);
					return true;
				}

				view.loadUrl(url);
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				closeLoading();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				mErrorState = true;
				mWebView.setVisibility(View.GONE);
				mErrorLayout.setVisibility(View.VISIBLE);
			}

		});

		if (!itIndexMore.getExtras().toString().equals("")) {
			String from_tag = itIndexMore.getStringExtra(FROM_TAG);
			mLoadingDialog.show();
			mBackBtn.setEnabled(false);
			if (!TextUtils.isEmpty(from_tag)) {
				if (from_tag.equals("skill")) {
					mTextTitle.setText("极路客小技巧");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=2");
				} else if (from_tag.equals("install")) {
					mTextTitle.setText("安装指导");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=3");
				} else if (from_tag.equals("shopping")) {
					mTextTitle.setText("购买极路客");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=4");
				} else if (from_tag.equals("buyline")) {
					mTextTitle.setText("购买极路客专用降压线");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=1");
				} else if (from_tag.equals("wifihelp")) {
					mTextTitle.setText("极路客视频安装帮助");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=3");
				} else if(from_tag.equals("profitProblem")) {
					mTextTitle.setText("常见问题");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=8");
				} else if (from_tag.equals("cash")) {
					String uid = itIndexMore.getStringExtra("uid");
					String phone = itIndexMore.getStringExtra("phone");
					mTextTitle.setText("申请提现");
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=7&phone=" + phone + "&uid=" + uid);
				}
			} else {
				String title = itIndexMore.getStringExtra("slide_h5_title");
				if(null != title && !title.equals("")) {
					mTextTitle.setText(title);
				} else {
					mTextTitle.setText("");
				}
				String url = itIndexMore.getStringExtra("url");

				if (mErrorState) {
					return;
				}
				mWebView.loadUrl(url);
			}

		}
		mBackBtn.setOnClickListener(this);
		mTextRight.setOnClickListener(this);
	}

	private String getIntentPackageName(String url) {
		try {
			int index = url.indexOf("package=");
			if (index > -1) {
				int start = index + 8;
				int end = url.indexOf(";", index);
				String packStr = url.substring(start, end);
				GolukDebugUtils.e("", "Error--------------------package:" + packStr);
				return packStr;
			}
		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils.e("", "Error--------------------message:" + e.toString());
		}
		return "";
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mErrorState) {
				finish();
				mErrorState = false;
				return true;
			}
			if(mProfitChangeUI) {
				finish();
				return true;
			}
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				finish();
			}
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			if (mErrorState) {
				finish();
				mErrorState = false;
				return;
			}
			if(mProfitChangeUI) {
				finish();
				return ;
			}
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				finish();
			}
			break;
		case R.id.user_title_right:
			this.finish();
			break;
		default:
			break;
		}
	}

	private void closeLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.close();
			mLoadingDialog = null;
			mBackBtn.setEnabled(true);
		}
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

	@Override
	protected void onDestroy() {
		closeLoading();
		super.onDestroy();
		GolukDebugUtils.e("", "--------UserOpenUrlActivity-------onDestory：");
		if (mWebView != null){
			mWebView.destroy();
			mWebView = null;
		}
	}

	@Override
	public void forbidBackKey(int backKey) {
		if (backKey == 1) {
			GolukDebugUtils.e("", "------------------customDialog------------back-----ok");
			finish();
		}
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
			long contentLength) {
		try {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
