package cn.com.tiros.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import android.os.Handler;
import android.os.Message;

public class TSocket {
	
	public static final int SYS_STREAM = 0;
    public static final int SYS_DGRAM = 1;
    public static final int SYS_HOST = 2;
	
    public static final int SYS_ERR_OBTAINED_IP = 0;
    public static final int SYS_ERR_CONNECTE = 1;
    public static final int SYS_ERR_NET = 2;
    
    public static final int SYS_EVT_OBTAINED_IP = 0;
    public static final int SYS_EVT_CONNECTED = 1;
    public static final int SYS_EVT_SENT = 2;
    public static final int SYS_EVT_RECEIVED = 3;
    public static final int SYS_EVT_ERROR = 4;
    
	private int mSocketType;
	
	private int mTSocket;
	
	private String mHostAddress = null;
	
	private boolean mIsShutDown = false;
	
	private DatagramPacket mPacket = null;
	/** UDP */
	private DatagramSocket mUdpSocket = null;
	/** TCP */
	private Socket mTcpSocket = null;
	
	private InetSocketAddress mTcpAddress;
	
	private boolean mSendingOrReceiving = true;
    
    private InputStream mInputStream;
    
    private OutputStream mOutputStream;

	
	private final int SendByteLength = 1024;
	
//	private final int RecvByteLength = 1024;
	private final int RecvByteLength = 8192;
	
	private int mSendSize = 0;
	private int mReceiveSize = 0;
	
	public byte[] recvbuf = new byte[RecvByteLength+1];
	
	private Object mRecvNotifyLock = new Object();
	
	private Object mLock = new Object();
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mIsShutDown){
				return;
			}
			switch (msg.what) {
			case 0:
				break;
			case 1:
				sys_socketEvent(mTSocket, SYS_EVT_CONNECTED, 0, 0);
				break;
			case 2:
				break;
			case 3:
				sys_socketEvent(mTSocket, SYS_EVT_SENT, mSendSize, 0);
				break;
			case 4:
				if (mReceiveSize > 0 )
				{
					sys_socketEvent(mTSocket, SYS_EVT_RECEIVED, mReceiveSize, recvbuf);
				}
				synchronized (mRecvNotifyLock) {
					mRecvNotifyLock.notify();
				}
				break;
			case 5:
				mIsShutDown = true;
				sys_socketEvent(mTSocket, SYS_EVT_ERROR, SYS_ERR_CONNECTE, 0);
				break;
			case 6:
				mIsShutDown = true;
				sys_socketEvent(mTSocket, SYS_EVT_ERROR, SYS_ERR_NET, 0);
				break;
			case 7:
				sys_socketEvent(mTSocket, SYS_EVT_OBTAINED_IP, mHostAddress, 0);
				break;
			case 8:
				sys_socketEvent(mTSocket, SYS_EVT_ERROR, SYS_ERR_OBTAINED_IP, 0);
				break;
			
			}
			super.handleMessage(msg);
		};
	};
	
	/**
	 * @brief ����Socket
	 * @param[in] pSocket - TSocket����ָ��
	 * @param[in] type - Socket������(STREAM,DGRAM,HOST)
	 * @return - �ɹ�����Socket�ṹ��ָ��,ʧ�ܷ���NULL
	 */
    public void sys_sktopen(int pSocket, int type) {
		mTSocket = pSocket;
		mSocketType = type;
	}
    
    /**
     * @brief �ͷ�Socket
     * @return - ��
     */
	public void sys_sktclose() {
		mIsShutDown = true;
		mSendingOrReceiving = false;
		try {
			switch (mSocketType) {
			case SYS_STREAM:
				if (mTcpSocket != null) {
					mTcpSocket.close();
					mTcpSocket = null;
				}
				break;
			case SYS_DGRAM:
				if (mUdpSocket != null) {
					mUdpSocket.disconnect();
					mUdpSocket.close();
					mUdpSocket = null;
				}
				break;
			}
		} catch (Exception e) {}
		finally {
			synchronized (mLock) {
				try {
					if (mOutputStream != null) {
						mOutputStream.close();
						mOutputStream = null;
					}
				} catch (IOException e) {
				}
			}
				try{
					if (mInputStream != null) {
						mInputStream.close();
						mInputStream = null;
					}
				} catch (IOException e) {
				}
		}
		if (mHandler != null) {
			mHandler.removeMessages(4);
		}
	}
    
    /**
     * @brief ��ȡ�����Ӧ��IP��ַ
     * @param[in] addr - �����ַ�
     * @return - ��
     * @par �ӿ�ʹ��Լ��:
     *      1.HOST������Ч <br>
     */
    public void sys_sktobtainip(final String addr) {
    	if(mSocketType != SYS_HOST){
    		return;
    	}
		mIsShutDown = false;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mHostAddress = InetAddress.getByName(addr).getHostAddress();
					mHandler.sendEmptyMessage(7);
				} catch (Exception e1) {						
					mHandler.sendEmptyMessage(8);
			   }
		}
	  }).start();
	}
	
	/**
	 * @brief ����TCP����
	 * @param[in] ip - IP��ַ
	 * @param[in] port - �˿ں�
	 * @return - ��
	 * @par �ӿ�ʹ��Լ��:
	 *      1.STREAM������Ч <br>
	 */
	public void sys_sktconnect(final String addr, final int port) {
		if(mSocketType != SYS_STREAM){
			return;
		}
		mSendingOrReceiving = true;
		mIsShutDown = false;
		
		mTcpSocket = new Socket();
		mTcpAddress = new InetSocketAddress(addr, port);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (mTcpSocket != null && !mTcpSocket.isConnected()) {
						mTcpSocket.connect(mTcpAddress, 10000);
					}
				} catch (Exception e) {
					mSendingOrReceiving = false;
					mHandler.sendEmptyMessage(5);
					return;
				}
				mHandler.sendEmptyMessage(1);

				try {
					if (mTcpSocket != null){
						mInputStream = mTcpSocket.getInputStream();
					}
				} catch (IOException e) {
					mHandler.sendEmptyMessage(6);
					return;
				}
				while (mSendingOrReceiving) {
					try {
						synchronized (mRecvNotifyLock) {
							if (mInputStream != null && null != recvbuf) {
								mReceiveSize = mInputStream.read(recvbuf, 0, RecvByteLength);
								if (mReceiveSize >= 0) {
									mHandler.sendEmptyMessage(4);
									try {
										mRecvNotifyLock.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								else
								{
									mHandler.sendEmptyMessage(6);
									mSendingOrReceiving = false;
								}
							}
						}
					} catch (IOException e) {
						mHandler.sendEmptyMessage(6);
						mSendingOrReceiving = false;
						return;
					}
				}
			}
		}).start();
	}
    
	/**
	 * @brief �󶨱���UDP�˿�
	 * @param[in] port - ���ض˿ں�
	 * @return - ��
	 * @par �ӿ�ʹ��Լ��:
	 *      1.DGRAM������Ч <br>
	 *      2.δ�󶨹�˿� <br>
	 */
	public boolean sys_sktbind(int port) {
		if(mSocketType != SYS_DGRAM){
			return false;
		}
		
		if(mUdpSocket == null){
			try {
				mUdpSocket = new DatagramSocket(port);
			} catch (SocketException e) {
				return false;
			};
		}
		
		mIsShutDown = false;
		mSendingOrReceiving = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (mSendingOrReceiving) {
					synchronized (mRecvNotifyLock) {
						try {
							mPacket = new DatagramPacket(recvbuf, RecvByteLength);
							mUdpSocket.receive(mPacket);
							mReceiveSize = mPacket.getData().length;
	
						} 
						catch (Exception e) {
							mHandler.sendEmptyMessage(6);
							mSendingOrReceiving = false;
							return;
						}
						mHandler.sendEmptyMessage(4);
						try {
							mRecvNotifyLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
		return true;
	}
	
	/**
	 * @brief ��һ�������ӵ�Socket�������(TCP)
	 * @param[in] buf - ������ݵĻ�����
	 * @param[in] bufsize - ��������ݵĳ���
	 * @return - ��
	 * @par �ӿ�ʹ��Լ��:
	 *      1.STREAM������Ч <br>
	 *      2.�����ӳɹ� <br>
	 */
	public void sys_sktsend(final byte[] buf, final int bufsize) {
		if(mSocketType != SYS_STREAM){
			return;
		}
		
		new Thread(){
			@Override
			public void run() {
				if (mIsShutDown == false)
				{
					try {
						if (null != mTcpSocket) {
							synchronized (mLock) {
								if (mTcpSocket != null) {
									mOutputStream = mTcpSocket.getOutputStream();
									mOutputStream.write(buf, 0, bufsize);
									mSendSize = bufsize;
									mOutputStream.flush();
								}
							}
						}

						if(null != mHandler) {
							mHandler.sendEmptyMessage(3);
						}
					} 
					catch (IOException e)
					{
						if(null != mHandler) {
							mHandler.sendEmptyMessage(6);
						}
					}
				}
			};
		}.start();
		
	}

	/**
	 * @brief ��һָ��Ŀ�ĵط������(UDP)
	 * @param[in] socket - Socket�ṹ��ָ��
	 * @param[in] buf - ������ݵĻ�����
	 * @param[in] bufsize - ��������ݵĳ���
	 * @param[in] ip - Ŀ�ĵ�IP��ַ
	 * @param[in] port - Ŀ�ĵض˿ں�
	 * @return - ��
	 * @par �ӿ�ʹ��Լ��:
	 *      1.DGRAM������Ч <br>
	 */
	public void sys_sktsendto(final String ip, final int port, final byte[] buf, final int bufsize) {
		if(mSocketType != SYS_DGRAM){
			return;
		}
		
		new Thread(){
			@Override
			public void run() {
				try {
					DatagramSocket udp = new DatagramSocket(port);
					InetAddress address = InetAddress.getByName(ip);		
					DatagramPacket packet = new DatagramPacket(buf, bufsize, address, port);
					udp.send(packet);
					udp.disconnect();
					udp.close();
					udp = null;
					packet = null;
					address = null;
				} catch (Exception e) {}
			};
		}.start();
	}
	
	/**
	 * @brief �ر�Socket
	 * @return - ��
	 * @par �ӿ�ʹ��Լ��:
	 *      1.����TCP��ʽ,�ϵ�����,ȡ�����иö�����ص��첽���� <br>
	 *      2.����UDP��ʽ,ȡ��˿ڰ�,ȡ�����иö�����ص��첽���� <br>
	 *      3.����HOST��ʽ,ȡ�����иö�����ص��첽���� <br>
	 */
	public void sys_sktshutdown() {
		sys_sktclose();
	}
	
	/**
	 * @brief �ж��Ƿ��Ѿ����ӻ�󶨶˿ں�
	 * @return - (1)�Ѿ�����(2)�Ѱ󶨶˿ں�(3)���ڽ����첽����,�򷵻�ture,���򷵻�false
	 */
	public boolean sys_sktisbusy(){
		return mSendingOrReceiving;
	}
	
	public static native void sys_socketEvent(int socket, int dwEvent, int dwParam1, int dwParam2);

	public static native void sys_socketEvent(int socket, int dwEvent, String dwParam1, int dwParam2);

	public static native void sys_socketEvent(int socket, int dwEvent, int dwParam1, byte[] dwParam2);
}

