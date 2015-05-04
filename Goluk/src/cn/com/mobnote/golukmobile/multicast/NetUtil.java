package cn.com.mobnote.golukmobile.multicast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.utils.LogUtil;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NetUtil {

	private static final int GRAM_PORT = 35293;
	public static final int SUCESS = 1;

	private DatagramPacket mPacket = null;

	private byte[] recvbuf = new byte[256];

	private boolean isCanScan = false;

	private DatagramSocket mUdpSocket = null;

	private int mType = 0;

	private static NetUtil mInstance = new NetUtil();

	private IMultiCastFn mFn = null;

	private static final int MSG_H_SUCESS = 1;
	private static final int MSG_H_ERROR = 2;

	public static NetUtil getInstance() {
		return mInstance;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 4:
				isCanScan = false;
				WifiRsBean bean = (WifiRsBean) msg.obj;
				sendData(SUCESS, bean);
				// 连接成功后，关闭连接
				cancel();
				break;
			case 5:
				break;
			case 6:
				Log.e("", "Error!!!!!!!");
				cancel();
				break;
			}
			super.handleMessage(msg);
		};
	};

	public void setMultiCastListener(IMultiCastFn fn) {
		mFn = fn;
	}

	private void sendData(int sucess, Object obj) {
		LogUtil.e("", "MultiCastUtil-----sendData------ip2222222:   " + sucess);
		if (null != mFn) {
			mFn.MultiCaskCallBack(mType, sucess, obj);
		}
	}

	public void cancel() {
		try {
			isCanScan = false;
			if (null != mUdpSocket) {
				mUdpSocket.close();
				mUdpSocket = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findServerIpAddress(int type, final String mac, final String ip, int seconds) {

		if (null != mUdpSocket) {
			return;
		}
		mType = type;
		Log.e("", "++++++++++++++++++++TestUDP--------findServerIpAddress-----1");
		try {
			mUdpSocket = new DatagramSocket(GRAM_PORT);
			Log.e("", "++++++++++++++++++++TestUDP--------findServerIpAddress-----2");
		} catch (SocketException e) {
			mHandler.sendEmptyMessage(6);
			Log.e("", "++++++++++++++++++++TestUDP--------findServerIpAddress-----3");
			return;
		}

		isCanScan = true;

		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.e("", "+TestUDP--------findServerIpAddress-----4-");
				while (isCanScan) {
					try {
						mPacket = new DatagramPacket(recvbuf, 256);
						Log.e("", "++TestUDP--------findServerIpAddress-----5");
						mUdpSocket.receive(mPacket);
						Log.e("", "++TestUDP--------findServerIpAddress----6");
						int length = mPacket.getLength();
						byte[] data = mPacket.getData();
						String s = new String(data, 0, length - 1, "GBK");
						if (s.equals("Goluk,good luck!")) {
							String address2 = mPacket.getAddress().toString();
							receiveSucess(address2);
							break;
						} else {
							Log.e("", "+++TestUDP--------findServerIpAddress-----77777 recvbuf1=" + s);
						}
					} catch (Exception e) {
						Log.e("", "++TestUDP--------findServerIpAddress-------8888888888-ip=");
						e.printStackTrace();
						mHandler.sendEmptyMessage(6);
						isCanScan = false;
						return;
					}
					Log.e("", "++TestUDP--------findServerIpAddress-------99999999999--ip=" + mPacket.getAddress());
				}
			}
		}).start();

	}

	private void receiveSucess(String ip) {
		Log.e("", "+++TestUDP--------receiveSucess-----1111111 recvbuf1=: " + ip);
		if (ip.contains("/")) {
			ip = ip.replace("/", "");
		}

		WifiRsBean bean = new WifiRsBean();
		bean.setIpc_ip(ip);

		Message msg = new Message();
		msg.what = 4;
		msg.obj = bean;
		mHandler.sendMessage(msg);

	}

}
