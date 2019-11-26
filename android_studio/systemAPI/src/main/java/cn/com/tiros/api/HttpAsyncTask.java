package cn.com.tiros.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;

import android.os.AsyncTask;
import android.text.TextUtils;

public class HttpAsyncTask extends AsyncTask<Void, Integer, Boolean> implements IHttpFn {

	private int httpErrorType = 0; // 错误码类型
	private int httpErrorCode = 0;// 错误码
	private int httpContentLength = 0;// 数据总长度
	private int mResponseCode;
	private int mContentLength;
	private String mHeaderLocation = null;// 头信息的Location
	private byte[] mData;
	private int mResponseTime = 0;
	private int mAllCostTime = 0;

	private HttpClient mHttpClient;
	private HttpResponse mResponse;
	private HttpRequestBase mRequest;

	private Http mHttp;
	private long mHttpHandler;
	private boolean mIsCheckHeader = false; // 是否校验头信息

	public volatile boolean mIsCancel = false;

	public HttpAsyncTask(Http http, long handle, HttpClient client, HttpRequestBase request, boolean bcheck) {
		mHttp = http;
		mHttpHandler = handle;
		mRequest = request;
		mHttpClient = client;
		mIsCheckHeader = bcheck;
	}

	@SuppressWarnings("resource")
	@Override
	protected Boolean doInBackground(Void... voids) {
		if (mIsCancel) {
			return true;
		}
		mResponseTime = 0;
		mAllCostTime = 0;
		long startTime = System.currentTimeMillis();
		try {
			sendProgress(M_EVT_HTTP_REQUEST);
			if (!mIsCancel) {
				mHttp.netstate = Http.NETSTATE_RUNNING;
				mResponse = mHttpClient.execute(mRequest);
			} else {
				return true;
			}
		} catch (ConnectTimeoutException e1) { // 链接超时
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_TIMEOUT;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_RESPONSE;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (SocketTimeoutException e1) {
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_TIMEOUT;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_RESPONSE;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (ClientProtocolException e1) {
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_CLIENTERR;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (UnknownHostException e1) { // 无法链接网络
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_DISCONNECT;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (IOException e1) {
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_DISCONNECT;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (Exception e) {
			return false;
		}

		if (mIsCancel) {
			return true;
		}
		mResponseTime = (int) (System.currentTimeMillis() - startTime);
		mResponseCode = mResponse.getStatusLine().getStatusCode();
		Header[] header = mResponse.getAllHeaders();

		if (header == null || header.length == 0) {
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_SERVERERR;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		}

		if (mIsCancel) {
			return true;
		}

		String sField = null;
		String sValue = null;
		boolean bGzip = false;
		boolean bPrivate = false;// 判断是否包含我们自己的私有头信息
		String strLocation = null;
		for (int i = 0; i < header.length; i++) {
			Header mheader = header[i];
			sField = mheader.getName();
			sValue = mheader.getValue();

			sField = sField.toLowerCase(); // 将名称转换为小写

			// GolukDebugUtils.i("HttpAsyncTask", "sField == " + sField);
			// GolukDebugUtils.i("HttpAsyncTask", "sValue == " + sValue);

			if (sField.equals("service-provider")) {
				if (sValue.equals("tiros.com.cn")) {
					bPrivate = true;
				}
			}
			if (sField.equals("content-length")) {
				if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
					httpContentLength = Integer.valueOf(sValue);
				} else {
					httpContentLength = 0;
				}
			}
			if (sField.equals("content-encoding")) {
				if (sValue.equals("gzip")) {
					bGzip = true;
				}
			}
			if (sField.equals("location")) {
				strLocation = sValue;
			}
		}

		if (mIsCheckHeader) {
			if (!bPrivate) {
				httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
				httpErrorCode = SYS_HTTPERR_SERVERERR;
				sendProgress(M_EVT_HTTP_ERROR);
				return false;
			}
		}

		if (mIsCancel) {
			return true;
		}

		if (mResponseCode == 302) {
			if (strLocation != null) {
				mHeaderLocation = strLocation;
			}
			sendProgress(M_EVT_HTTP_RESPONSE_STRING);
		} else {
			sendProgress(M_EVT_HTTP_RESPONSE);
		}

		if (mIsCancel) {
			return true;
		}

		if (bGzip) {
			sendProgress(M_EVT_HTTP_DATA_GZIP);
		}

		if (mIsCancel) {
			return true;
		}

		InputStream is = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			is = mResponse.getEntity().getContent();
		} catch (SocketTimeoutException e1) {
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_RESPONSE;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_DATA;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (IOException e) {
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_RESPONSE;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_DATA;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		}

		if (mIsCancel) {
			return true;
		}

		int len = 0;
		mContentLength = 0;
		byte[] buff = new byte[1024 * 10];

		try {
			while (len != -1) {
				boolean breadtimeout = false;
				try {
					len = is.read(buff);
				} catch (Exception e1) {
					breadtimeout = true;
				}
				if (breadtimeout) {
					mHttp.netstate = Http.NETSTATE_WAIT;
					httpErrorType = SYS_HTTPERRTYPE_TIMEOUT;
					httpErrorCode = SYS_HTTPERR_TIMEOUT_DATA;
					sendProgress(M_EVT_HTTP_ERROR);
					try {
						is.close();
						is = null;
					} catch (IOException e) {
					}
					return false;
				}

				mContentLength = len;
				mData = buff;

				if (len != -1) {
					sendProgress(M_EVT_HTTP_BODY);
					if (mIsCancel) {
						try {
							is.close();
							is = null;
						} catch (IOException e) {
						}
						return true;
					}
				}
			}
		} catch (Exception e) {
			// shizy 20120411 修改错误类型
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_TIMEOUT;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_DATA;
			sendProgress(M_EVT_HTTP_ERROR);
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (baos != null) {
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
			}
		}
		mAllCostTime = (int) (System.currentTimeMillis() - startTime);
		sendProgress(M_EVT_HTTP_COMPLETED);
		return true;
	}

	private void sendProgress(int progress) {
		synchronized (this) {
			publishProgress(progress);
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	protected void onProgressUpdate(Integer... progress) {
		if (mIsCancel) {
			synchronized (this) {
				notify();
			}
			return;
		}
		switch (progress[0]) {
		case M_EVT_HTTP_REQUEST:
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_REQUEST, 0, 0);
			break;
		case M_EVT_HTTP_RESPONSE:
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_RESPONSE, mResponseCode, httpContentLength);
			break;
		case M_EVT_HTTP_RESPONSE_STRING:
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_RESPONSE, mResponseCode, mHeaderLocation);
			break;
		case M_EVT_HTTP_BODY:
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_BODY, mContentLength, mData);
			break;
		case M_EVT_HTTP_COMPLETED:
		    mHttp.sys_httpcancel();
			mHttp.netstate = Http.NETSTATE_WAIT;
			mIsCancel = false;
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_COMPLETED, mResponseTime, mAllCostTime);
			break;
		case M_EVT_HTTP_ERROR:
			mHttp.sys_httpcancel();
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_ERROR, httpErrorType, httpErrorCode);
			break;
		case M_EVT_HTTP_DATA_GZIP:
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_DATA_GZIP, 0, 0);
			break;
		default:
			break;
		}
		synchronized (this) {
			notify();
		}
	}

}