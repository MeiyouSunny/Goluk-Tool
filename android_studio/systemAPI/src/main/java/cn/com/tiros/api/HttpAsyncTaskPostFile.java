package cn.com.tiros.api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import cn.com.tiros.debug.GolukDebugUtils;
import android.os.AsyncTask;

public class HttpAsyncTaskPostFile extends AsyncTask<Void, Integer, Boolean> implements IHttpFn {

	private int httpErrorType = 0; // 错误码类型
	private int httpErrorCode = 0;// 错误码
	private int httpContentLength = 0;// 数据总长度
	private int mResponseCode;
	private int mContentLength;
	private String mHeaderLocation = null;// 头信息的Location
	private byte[] mData;
	/** 应答时间 */
	private int mResponseTime = 0;
	private int mAllCostTime = 0;
	private int mUploadProgress = 0;

	private Http mHttp;
	private long mHttpHandler;
	private boolean mIsCheckHeader = false; // 是否校验头信息

	public volatile boolean mIsCancel = false;

	private HttpURLConnection mHttpUrlConn = null;
	/** 文件上传的服务器地址 */
	private String mUploadurl = null;
	/** 要上传的文件路径 */
	private String mFilePath = null;
	private Map<String, String> mHeaders;

	public HttpAsyncTaskPostFile(Http http, long handle, String url, String filePath, boolean bcheck,
			Map<String, String> headers) {

		mHttp = http;
		mHttpHandler = handle;
		mUploadurl = url;
		mFilePath = filePath;
		mIsCheckHeader = bcheck;
		mHeaders = headers;
	}

	@SuppressWarnings("resource")
	@Override
	protected Boolean doInBackground(Void... voids) {
		if (mIsCancel) {
			return true;
		}
		 String CHARSET = "utf-8"; // 设置编码
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成  
        String PREFIX = "--", LINE_END = "\r\n";  
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型 
		mResponseTime = 0;
		mAllCostTime = 0;
		long startTime = System.currentTimeMillis();
		try {
			sendProgress(M_EVT_HTTP_REQUEST);
			if (mIsCancel) {
				return true;
			}
			File file = new File(mFilePath);
			if (!file.exists()) {
				mHttp.netstate = Http.NETSTATE_WAIT;
				httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
				httpErrorCode = SYS_HTTPERR_DISCONNECT;
				sendProgress(M_EVT_HTTP_ERROR);
				return false;
			}
			// 获取文件大小
			final long fileSize = file.length();
			int time = mHttp.getTrasferTime(fileSize);
			mHttp.netstate = Http.NETSTATE_RUNNING;
			URL url = new URL(mUploadurl);
			mHttpUrlConn = (HttpURLConnection) url.openConnection();
			mHttpUrlConn.setDoInput(true);
			mHttpUrlConn.setDoOutput(true);
			mHttpUrlConn.setUseCaches(false);
			mHttpUrlConn.setRequestMethod("POST");
			mHttpUrlConn.setRequestProperty("Connection", "Keep-Alive");
			mHttpUrlConn.setRequestProperty("Charset", CHARSET);
			mHttpUrlConn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="  
                    + BOUNDARY); 
//			mHttpUrlConn.setChunkedStreamingMode(0);

			// 设置头信息
			if (mHeaders != null) {
				Set<Entry<String, String>> set = mHeaders.entrySet();
				Iterator<Entry<String, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					mHttpUrlConn.addRequestProperty(key, value);
					entry = null;
				}
				set = null;
				iterator = null;
			}

			// 设置超时
			if (time > mHttp.getConnTimeOutTime()) {
				mHttpUrlConn.setConnectTimeout(time);
				mHttpUrlConn.setReadTimeout(time + mHttp.getSoTimeOutTime());
			} else {
				mHttpUrlConn.setConnectTimeout(mHttp.getConnTimeOutTime());
				mHttpUrlConn.setReadTimeout(mHttp.getSoTimeOutTime());
			}

			// 连接服务器
			mHttpUrlConn.connect();

			// 读取上传文件
			OutputStream os = mHttpUrlConn.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			FileInputStream fis = new FileInputStream(file);
			byte[] bytes = new byte[25 * 1024];
			int len = 0;

			int count = 0;

			StringBuffer sb = new StringBuffer();  
            sb.append(PREFIX);  
            sb.append(BOUNDARY);  
            sb.append(LINE_END);  
            /** 
             * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件 
             * filename是文件的名字，包含后缀名的 比如:abc.png 
             */  

            sb.append("Content-Disposition: form-data; name=\"upgrade\"; filename=\""  
                    + file.getName() + "\"" + LINE_END);  
            sb.append("Content-Type: application/octet-stream; charset="  
                    + CHARSET + LINE_END);  
            sb.append(LINE_END);  
            dos.write(sb.toString().getBytes());
			while (-1 != (len = fis.read(bytes))) {
				dos.write(bytes, 0, len);
				dos.flush();
				count += len;
				mUploadProgress = (int) (((float) count / (float) fileSize) * 100);
				if (mIsCancel) {
					try {
						fis.close();
						os.close();
					} catch (IOException e) {
					}
					return true;
				}
				// 通知文件上传进度
				sendProgress(M_EVT_HTTP_UPLOADFILE_PROGRESS);
			}
			dos.write(LINE_END.getBytes());  
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)  
                    .getBytes();  
            dos.write(end_data);  
            dos.flush(); 
			fis.close();

		} catch (ConnectTimeoutException e1) { // 链接超时
			GolukDebugUtils
					.e("",
							"SystemApi-----java-----Http-----HttpAsyncTaskPostFile:  doInBackground: --Exception 1111----6666666");
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_TIMEOUT;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_RESPONSE;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (SocketTimeoutException e1) {
			GolukDebugUtils
					.e("",
							"SystemApi-----java-----Http-----HttpAsyncTaskPostFile:  doInBackground: --Exception 2222----6666666");
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_TIMEOUT;
			httpErrorCode = SYS_HTTPERR_TIMEOUT_RESPONSE;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (ClientProtocolException e1) {
			GolukDebugUtils
					.e("",
							"SystemApi-----java-----Http-----HttpAsyncTaskPostFile:  doInBackground: --Exception 3333----6666666");
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_CLIENTERR;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (UnknownHostException e1) { // 无法链接网络
			GolukDebugUtils
					.e("",
							"SystemApi-----java-----Http-----HttpAsyncTaskPostFile:  doInBackground: --Exception 4444----6666666");
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_DISCONNECT;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (IOException e1) {
			GolukDebugUtils
					.e("",
							"SystemApi-----java-----Http-----HttpAsyncTaskPostFile:  doInBackground: --Exception 5555----6666666");
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_DISCONNECT;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		} catch (Exception e) {
			GolukDebugUtils
					.e("",
							"SystemApi-----java-----Http-----HttpAsyncTaskPostFile:  doInBackground: --Exception 6666----6666666");
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_DISCONNECT;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		}


		if (mIsCancel) {
			return true;
		}
		mResponseTime = (int) (System.currentTimeMillis() - startTime);

		/**
		 * 读取头信息
		 * */
		HeadBean bean = null;

		try {
			mResponseCode = mHttpUrlConn.getResponseCode();
			Map<String, List<String>> allHeader = mHttpUrlConn.getHeaderFields();
			if (null == allHeader || allHeader.size() <= 0) {
				httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
				httpErrorCode = SYS_HTTPERR_SERVERERR;
				sendProgress(M_EVT_HTTP_ERROR);
				return false;
			}
			if (mIsCancel) {
				return true;
			}
			bean = getHeaderSign(allHeader);
		} catch (Exception e) {
			e.printStackTrace();
			mHttp.netstate = Http.NETSTATE_WAIT;
			httpErrorType = SYS_HTTPERRTYPE_UNAVAILABLE;
			httpErrorCode = SYS_HTTPERR_DISCONNECT;
			sendProgress(M_EVT_HTTP_ERROR);
			return false;
		}

		if (mIsCheckHeader) {
			if (!bean.bPrivate) {
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
			if (bean.strLocation != null) {
				mHeaderLocation = bean.strLocation;
			}
			sendProgress(M_EVT_HTTP_RESPONSE_STRING);
		} else {
			sendProgress(M_EVT_HTTP_RESPONSE);
		}

		if (mIsCancel) {
			return true;
		}

		if (bean.bGzip) {
			sendProgress(M_EVT_HTTP_DATA_GZIP);
		}

		if (mIsCancel) {
			return true;
		}

		/**
		 * 读取数据
		 * */
		InputStream is = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			is = mHttpUrlConn.getInputStream();
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

	class HeadBean {
		boolean bPrivate = false;// 判断是否包含我们自己的私有头信息
		boolean bGzip = false;
		String strLocation = null;
		int contentLength = 0;
	}

	private HeadBean getHeaderSign(Map<String, List<String>> headers) {
		HeadBean bean = new HeadBean();

		String sKey = null;
		List<String> sValue = null;

		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			// 将名称转换为小写
			String kk = entry.getKey();
			if (null == kk) {
				continue;
			}
			sKey = entry.getKey().toLowerCase();
			sValue = entry.getValue();

			if (sKey.equals("service-provider")) {
				if (sValue.contains("tiros.com.cn")) {
					bean.bPrivate = true;
				}
			}
			if (sKey.equals("content-encoding")) {
				if (sValue.contains("gzip")) {
					bean.bGzip = true;
				}
			}

			if (sKey.equals("content-length")) {
				bean.contentLength = Integer.valueOf(sValue.get(0));
			}

			if (sKey.equals("location")) {
				bean.strLocation = sValue.toString();
			}
		}

		return bean;
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
		case M_EVT_HTTP_UPLOADFILE_PROGRESS:
			Http.sys_httpEvent(mHttpHandler, SYS_EVT_HTTP_POSTFILE_PROGRESS, mUploadProgress, 0);
			break;
		default:
			break;
		}
		synchronized (this) {
			notify();
		}
	}

}