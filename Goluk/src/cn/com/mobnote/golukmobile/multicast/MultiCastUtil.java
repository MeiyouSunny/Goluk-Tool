package cn.com.mobnote.golukmobile.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.utils.LogUtil;

public class MultiCastUtil {

	private static final String TAG = "Net.Utils";

	private static final String GROUP_IP = "224.0.0.224";
	// 绑定本地的端口
	private static final int BIND_PORT = 2241;
	// 发送数据到2240端口
	private static final int SEND_TO_PORT = 2240;

	private static String findIP = null;
	/** 即将查询的MAC地址 */
	private static String findMAC = null;

	private static int DURATION = 10;

	private static boolean isStarting = false;

	private IMultiCastFn mFn = null;

	private static Timer mTimer = null;

	boolean isSearchSucess = false;

	private int mType = 0;

	private static MultiCastUtil mInstance = new MultiCastUtil();

	public static MultiCastUtil getInstance() {
		return mInstance;
	}

	public void setMultiCastListener(IMultiCastFn fn) {
		mFn = fn;
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (100 == msg.what) {
				isSearchSucess = true;
				sendData(1, msg.obj);
				isStarting = false;
				mHandler.removeMessages(102);
			} else if (101 == msg.what) {
				sendData(0, null);
				isStarting = false;
			} else if (102 == msg.what) {
				cancelTimer();
				// 定时结束，没查到信息
				sendData(0, null);
				isStarting = false;
			}
		}
	};

	private static byte[] getData() {
		RequestVdcpBean bean = new RequestVdcpBean();
		bean.setPmask1(305837688);
		bean.setSername("from xiaocheben");
		bean.setNtypemain((short) 1);
		bean.setNtypesub((short) 2);
		bean.setVersion((byte) 1);
		bean.setRes("res");
		bean.setNchannel(5);
		bean.setDwdatasize(0);
		bean.setPmask2(0X876CD321);

		return bean.makeSendByte();
	}

	private void cancelTimer() {
		if (null != mTimer) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	public void findServerIP(int type, final String mac, final String ip, int seconds) {
		LogUtil.e("", "MultiCastUtil-----findServerIP------ip1111:   " + ip);
		if (isStarting) {
			return;
		}
		if (null == mac || "".equals(mac)) {
			return;
		}
		LogUtil.e("", "MultiCastUtil-----findServerIP------ip2222222:   ");
		mType = type;
		isSearchSucess = false;
		DURATION = seconds;
		findIP = ip;
		findMAC = mac;

		try {
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						findServerIpAddress();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 定时结束请求
		mHandler.sendEmptyMessageDelayed(102, DURATION);

	}

	private String findServerIpAddress() throws IOException {
		String ip = null;

		LogUtil.e("", "MultiCastUtil-----findServerIpAddress------111111111111:   ");

		MulticastSocket multicastSocket = new MulticastSocket(BIND_PORT);
		multicastSocket.setLoopbackMode(true);
		InetAddress group = InetAddress.getByName(GROUP_IP);
		LogUtil.e("", "MultiCastUtil-----findServerIpAddress------232323:   ");
		multicastSocket.joinGroup(group);
		LogUtil.e("", "MultiCastUtil-----findServerIpAddress------2424242424:   ");
		// 获取与IPC的交互信息
		final byte[] sentDataByte = getData();

		LogUtil.e("", "MultiCastUtil-----findServerIpAddress------22222222:   ");

		DatagramPacket packet = new DatagramPacket(sentDataByte, sentDataByte.length, group, SEND_TO_PORT);
		LogUtil.e("", "MultiCastUtil-----findServerIpAddress------start muticastSocket  Data:   ");
		// 向外广播发送数据
		multicastSocket.send(packet);
		Log.d(TAG, "WifiActivity----------->>>send packet ok");

		LogUtil.e("", "MultiCastUtil-----findServerIpAddress------send muticastSocket  OK!!:   ");

		// 接受数据
		for (;;) {

			LogUtil.e("", "MultiCastUtil-----findServerIpAddress------for-----------11111:   ");

			// 接受数据的字节数组
			byte[] receiveData = new byte[256];
			packet = new DatagramPacket(receiveData, receiveData.length);

			LogUtil.e("", "MultiCastUtil-----findServerIpAddress------for-----------start Accept Data:   ");

			// 阻塞接收数据
			multicastSocket.receive(packet);

			LogUtil.e("", "MultiCastUtil-----findServerIpAddress------for----------- Accept Data:   OKOKOK!!!!");

			// 读取接收到的数据
			byte[] result_mac = new byte[6];
			byte[] result_ip = new byte[4];
			System.arraycopy(receiveData, 72, result_mac, 0, 6);
			System.arraycopy(receiveData, 80, result_ip, 0, 4);

			final String ipStr = getIP(result_ip);
			final String macStr = getMacAddress(result_mac);

			LogUtil.e("", "MultiCastUtil-----findServerIpAddress------for----------- 33333:   received IP:" + ipStr
					+ "  macStr:" + macStr);

			if (macStr.substring(3).equals(findMAC.substring(3))) {
				// 找到了
				WifiRsBean bean = new WifiRsBean();
				bean.setIpc_ip(ipStr);
				bean.setIpc_mac(macStr);

				Message msg = new Message();
				msg.what = 100;
				msg.obj = bean;
				mHandler.sendMessage(msg);

				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

		}
		return ip;
	}

	private static String getIP(byte[] result_ip) {
		String ipStr = "";

		int firstIP = parseData(result_ip[3]);
		int secondIP = parseData(result_ip[2]);
		int threeIP = parseData(result_ip[1]);
		int fourIP = parseData(result_ip[0]);

		ipStr += "" + firstIP + "." + secondIP + "." + threeIP + "." + fourIP;

		return ipStr;
	}

	private static String getMacAddress(byte[] result_mac) {
		String mac0 = SwitchUtil.S10To116(result_mac[0]);
		String mac1 = SwitchUtil.S10To116(result_mac[1]);
		String mac2 = SwitchUtil.S10To116(result_mac[2]);
		String mac3 = SwitchUtil.S10To116(result_mac[3]);
		String mac4 = SwitchUtil.S10To116(result_mac[4]);
		String mac5 = SwitchUtil.S10To116(result_mac[5]);

		StringBuffer sb = new StringBuffer();
		sb.append(mac0);
		sb.append(":");
		sb.append(mac1);
		sb.append(":");
		sb.append(mac2);
		sb.append(":");
		sb.append(mac3);
		sb.append(":");
		sb.append(mac4);
		sb.append(":");
		sb.append(mac5);

		return sb.toString();
	}

	private void sendData(int sucess, Object obj) {
		LogUtil.e("", "MultiCastUtil-----sendData------ip2222222:   " + sucess);
		if (null != mFn) {
			mFn.MultiCaskCallBack(mType, sucess, obj);
		}
	}

	private static int parseData(int value) {
		return value > 0 ? value : 256 - value;
	}
}