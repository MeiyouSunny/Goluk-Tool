package com.mobnote.golukmain;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.http.UrlHostManager;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.user.MyProgressWebView;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JavaScriptInterface;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
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
	/** 收益页面UI修改 **/
	private boolean mProfitChangeUI = false;

	private String mShareId;
	private String mTitle;
	private String mPicture;
	private String mIntroduction;
	private String mShareAddress;
	private boolean mNeedShare;
	private String mUrlOpenPath;
	private SharePlatformUtil mSharePlatform;

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
		itIndexMore = getIntent();
		String webType = itIndexMore.getStringExtra(GolukConfig.WEB_TYPE);
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mWebView = (MyProgressWebView) findViewById(R.id.my_webview);
        String currUa = mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setUserAgentString(currUa + "/ goluk /"+ "golukAndroid /");
        mWebView.getSettings().setJavaScriptEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "mobile");

		mTextRight = (TextView) findViewById(R.id.user_title_right);
		if (GolukConfig.NEED_SHARE.equals(webType)) {
			mNeedShare = true;
			mTextRight.setText(this.getString(R.string.share_text));
			mTitle = itIndexMore.getStringExtra(GolukConfig.NEED_H5_TITLE);
			mPicture = itIndexMore.getStringExtra(GolukConfig.NEED_SHARE_PICTURE);
			mIntroduction = itIndexMore.getStringExtra(GolukConfig.NEED_SHARE_INTRO);
			mShareId = itIndexMore.getStringExtra(GolukConfig.NEED_SHARE_ID);
			mShareAddress = itIndexMore.getStringExtra(GolukConfig.H5_URL);
			mUrlOpenPath = itIndexMore.getStringExtra(GolukConfig.URL_OPEN_PATH);
			mSharePlatform = new SharePlatformUtil(this);
		} else {
			mNeedShare = false;
			mTextRight.setBackgroundResource(R.drawable.btn_close_image);
		}
		mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);

		if (null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(this, this.getResources().getString(R.string.str_url_loading));
			mLoadingDialog.setListener(this);
		}

		mProfitChangeUI = itIndexMore.getBooleanExtra("isChangeUI", false);
		if (mProfitChangeUI) {
			mTextRight.setVisibility(View.GONE);
			mBackBtn.setBackgroundResource(R.drawable.browser_close_btn);
		} else {
			mTextRight.setVisibility(View.VISIBLE);
			mBackBtn.setBackgroundResource(R.drawable.cross_screen_arrow_icon);
		}
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 设置
																		// 缓存模式
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

				if (null != url && url.startsWith("http")) {
					view.loadUrl(url);
					return true;
				}
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				closeLoading();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				mErrorState = true;
				if (mWebView == null) {
					return;
				}
				mWebView.setVisibility(View.GONE);
				mErrorLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				// TODO Auto-generated method stub
//				handler.proceed();
                final SslErrorHandler sslErrorHandler = handler;
				super.onReceivedSslError(view, handler, error);
                final AlertDialog.Builder builder = new AlertDialog.Builder(UserOpenUrlActivity.this);
                builder.setMessage(R.string.str_notification_error_ssl_cert_invalid);
                builder.setPositiveButton(UserOpenUrlActivity.this.getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sslErrorHandler.proceed();
                    }
                });
                builder.setNegativeButton(UserOpenUrlActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sslErrorHandler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
			}
		});

		if (!itIndexMore.getExtras().toString().equals("")) {
			String from_tag = itIndexMore.getStringExtra(FROM_TAG);
			mLoadingDialog.show();
			mBackBtn.setEnabled(false);
			if (!TextUtils.isEmpty(from_tag)) {
				if (from_tag.equals("skill")) {
					mTextTitle.setText(this.getResources().getString(R.string.my_skill_title_text));
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=2" + getCommParams());
				} else if (from_tag.equals("install")) {
					mTextTitle.setText(this.getResources().getString(R.string.my_install_title_text));
					if (mErrorState) {
						return;
					}
//					mWebView.loadUrl(getRtmpAddress() + "?type=3" + getCommParams());
					GolukDebugUtils.e("", "installationguide------"+HttpManager.getInstance().getWebDirectHost() + "/s/installationguide");
					mWebView.loadUrl(HttpManager.getInstance().getWebDirectHost() + "/s/installationguide");
				} else if (from_tag.equals("shopping")) {
					mTextTitle.setText(this.getResources().getString(R.string.my_shopping_title_text));
					if (mErrorState) {
						return;
					}
					final String shoppingUrl = getRtmpAddress() + "?type=4" + getCommParams();
					mWebView.loadUrl(shoppingUrl);
				} else if (from_tag.equals("buyline")) {
					mTextTitle.setText(this.getResources().getString(R.string.my_shopping_buck_line));
					if (mErrorState) {
						return;
					}
//					mWebView.loadUrl(getRtmpAddress() + "?type=1" + getCommParams());
					String url;
					if(getApp().isMainland()) {
						url = HttpManager.getInstance().getWebDirectHost() + "/s/buystepdownline";
					}else{
						//国际版固定链接
						url = "https://www.amazon.com/Spy-Tec-Dash-Camera-Vehicle/dp/B00MH4ZVHO/ref=sr_1_8?ie=UTF8&qid=1478595877";
					}
					GolukDebugUtils.e("", "buystepdownline------"+url);
					mWebView.loadUrl(url);
				} else if (from_tag.equals("wifihelp")) {
					mTextTitle.setText(this.getResources().getString(R.string.wifi_link_34_text));
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=3" + getCommParams());
				} else if (from_tag.equals("profitProblem")) {
					mTextTitle.setText(this.getResources().getString(R.string.my_profit_problem));
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(getRtmpAddress() + "?type=8" + getCommParams());
				} else if (from_tag.equals("cash")) {
					String uid = itIndexMore.getStringExtra("uid");
					String phone = itIndexMore.getStringExtra("phone");
					mTextTitle.setText(this.getResources().getString(R.string.str_apply_withdraw));
					if (mErrorState) {
						return;
					}
					mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
					mWebView.loadUrl(getRtmpAddress() + "?type=7&phone=" + phone + "&uid=" + uid + getCommParams());
				} else if (from_tag.equals("withdrawals")) {
					String url = itIndexMore.getStringExtra("withdraw_url");
					final String withDrawUrl = url + getCommParams2();
					mWebView.loadUrl(withDrawUrl);
				} else if(from_tag.equals("protocol")) {
					String url;
					if (!mBaseApp.isMainland()){
						url = "http://www.goluk.com/legal_cn.html";
					}else{
						url = "http://surl3.goluk.cn/golukwebsite_phone/legal_cn.html?commlocale=zh";
					}
					mTextTitle.setText(this.getString(R.string.str_user_protocol_and_privacy));
					if (mErrorState) {
						return;
					}
					mWebView.loadUrl(url);
				}
			} else {
				String title = itIndexMore.getStringExtra(GolukConfig.NEED_H5_TITLE);
				if (null != title && !title.equals("")) {
					mTextTitle.setText(title);
				} else {
					mTextTitle.setText("");
				}
				String url = itIndexMore.getStringExtra(GolukConfig.H5_URL);

				if (mErrorState) {
					return;
				}
				if (!TextUtils.isEmpty(url)) {
					if (url.contains("?")) {
						mWebView.loadUrl(url + getCommParams());
					} else {
						mWebView.loadUrl(url + getCommParams2());
					}
				}
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
			if (mProfitChangeUI) {
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
		int id = view.getId();
		if (id == R.id.back_btn) {
			if (mErrorState) {
				finish();
				mErrorState = false;
				return;
			}
			if (mProfitChangeUI) {
				finish();
				return;
			}
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				finish();
			}
		} else if (id == R.id.user_title_right) {
			if (mNeedShare && null != mSharePlatform) {
				String shareurl = mShareAddress;
				String coverurl = mPicture;
				String describe = mIntroduction;
				String urlOpenPath = mUrlOpenPath;

				if (TextUtils.isEmpty(coverurl)) {
					if (GolukApplication.getInstance().isMainland()) {
						coverurl = "http://pic.goluk.cn/ios_icon512.png";
					} else {
						coverurl = "http://i.pic.goluk.cn/ios_icon512.png";
					}
				}

				if (TextUtils.isEmpty(describe)) {
					describe = this.getResources().getString(R.string.app_name);
				}

				if ("cluster_adapter".equals(urlOpenPath)) {
					String realDesc = getString(R.string.str_vote_share_real_description);

					String ttl = mTitle;
					if (TextUtils.isEmpty(mTitle)) {
						ttl = getString(R.string.str_vote_share_title);
					}

					ThirdShareBean bean = new ThirdShareBean();
					bean.surl = shareurl;
					bean.curl = coverurl;
					bean.db = describe;
					bean.tl = ttl;
					bean.bitmap = null;
					bean.realDesc = realDesc;
					bean.videoId = "";
					bean.from = this.getString(R.string.str_zhuge_share_video_network_other);

					GolukDebugUtils.e("", "UserOpenUrlActivity------1: title: " + bean.tl + "  txt: " + bean.db);

					ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, bean);
					shareBoard.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
				} else {
					if (TextUtils.isEmpty(shareurl)) {
						return;
					}

					String ttl = mTitle;
					if (TextUtils.isEmpty(mTitle)) {
						ttl = getString(R.string.str_wonderful_share);
					}
					String realDesc = getString(R.string.str_wonderful_share);

					ThirdShareBean bean = new ThirdShareBean();
					bean.surl = shareurl;
					bean.curl = coverurl;
					bean.db = describe;
					bean.tl = ttl;
					bean.bitmap = null;
					bean.realDesc = realDesc;
					bean.videoId = "";
					bean.from = this.getString(R.string.str_zhuge_share_video_network_other);

					GolukDebugUtils.e("", "UserOpenUrlActivity------2: title: " + bean.tl + "  txt: " + bean.db);

					ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, bean);
					shareBoard.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
				}
			} else {
				this.finish();
			}
		} else {
		}}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mNeedShare) {
			if (null != mSharePlatform) {
				mSharePlatform.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	private void closeLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.close();
			mLoadingDialog = null;
			mBackBtn.setEnabled(true);
		}
	}

	private String getCommParams2() {
		return "?commversion=" + GolukUtils.GOLUK_APP_VERSION + "&commlocale=" + GolukUtils.getLanguageAndCountry();
	}

	private String getCommParams() {
		return "&commversion=" + GolukUtils.GOLUK_APP_VERSION + "&commlocale=" + GolukUtils.getLanguageAndCountry();
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
		new AlertDialog.Builder(this)
				.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
				.setMessage(this.getResources().getString(R.string.str_tell))
				.setPositiveButton(this.getResources().getString(R.string.str_button_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								try {
									Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
									startActivity(intent);
								}catch (Exception ex){}
							}
						}).setNegativeButton(this.getResources().getString(R.string.dialog_str_cancel), null).create()
				.show();
	}

	@Override
	protected void onDestroy() {
		closeLoading();
		super.onDestroy();
		GolukDebugUtils.e("", "--------UserOpenUrlActivity-------onDestory：");
		if (mWebView != null) {
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

    /**
     * 获取安装指导/购买链接的params
     *
     * @return
     */
    private String getUrlParams() {
        String url = "";
        if (GolukApplication.getInstance().isMainland()) {
            url = UrlHostManager.TEST_HOST;
        } else {
            url = UrlHostManager.INTERNATIIONAL_HOST;
        }
        return url;
    }

}
