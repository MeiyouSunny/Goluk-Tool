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

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				break;
			case 1:

				break;
			case 2:
				break;
			case 3:

				break;
			case 4:
				Log.e("", "SUccess!!!!!!!");
				isCanScan = false;
				WifiRsBean bean = (WifiRsBean) msg.obj;
				sendData(SUCESS, bean);
				break;
			case 5:
				break;
			case 6:
				Log.e("", "Error!!!!!!!");
				break;
			case 7:
				break;
			case 8:
				break;

			}
			super.handleMessage(msg);
		};
	};

	private IMultiCastFn mFn = null;

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

		mType = type;

		Log.e("", "++++++++++++++++++++TestUDP--------findServerIpAddress-----1");
		if (mUdpSocket == null) {
			try {
				mUdpSocket = new DatagramSocket(GRAM_PORT);
				Log.e("", "++++++++++++++++++++TestUDP--------findServerIpAddress-----2");
			} catch (SocketException e) {
				mHandler.sendEmptyMessage(6);
				Log.e("", "++++++++++++++++++++TestUDP--------findServerIpAddress-----3");
				return;
			}
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
						int length = mPacket.getLength();
						byte[] data = mPacket.getData();
						String s = new String(data, 0, length - 1, "GBK");
						if (s.equals("Goluk,good luck!")) {
							String address = mPacket.getSocketAddress().toString();
							String address2 = mPacket.getAddress().toString();

							if (address.contains("/")) {
								address = address.replace("/", "");
							}

							WifiRsBean bean = new WifiRsBean();
							bean.setIpc_ip(address);

							Message msg = new Message();
							msg.what = 4;
							msg.obj = bean;
							mHandler.sendMessage(msg);

							Log.e("", "++TestUDP--------findServerIpAddress-----6 recvbuf=Goluk,good luck:  " + s
									+ " address:" + address + "	address2:" + address2 + "	address3:");
							break;
						} else {
							Log.e("", "+++TestUDP--------findServerIpAddress-----77777 recvbuf1=" + s);
						}
					} catch (Exception e) {
						Log.e("", "++TestUDP--------findServerIpAddress-------8-898989-ip=");
						e.printStackTrace();
						mHandler.sendEmptyMessage(6);
						isCanScan = false;
						return;
					}

					Log.e("", "++TestUDP--------findServerIpAddress-------8--ip=" + mPacket.getAddress());
				}
			}
		}).start();

	}

}
