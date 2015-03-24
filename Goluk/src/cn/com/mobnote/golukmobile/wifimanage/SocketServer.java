package cn.com.mobnote.golukmobile.wifimanage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketServer implements Runnable {

	private final String TITLE = "SoketTransfers";
	/**
	 * 服务器监听
	 */
	public ServerSocket serverSocket;
	private static int PORT = 12345;
	public int start = 1;
	private Handler handler = null;
	public SocketClients clients = null;
	private Context mContext = null;

	private SocketServer(Context context ,Handler _handler) {
		clients = new SocketClients();
		this.mContext = context;
		this.handler = _handler;
		try {
			// 如果服务端口被占用就创建监听
			if (serverSocket == null || !serverSocket.isClosed()) {
				serverSocket = new ServerSocket(PORT);

			}

			// new Thread(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	int getState() {
		return start;
	}

	public void run() {
		Socket msocket = null;
		// 等待客户端连接
		try {

			while (true) {
				if (serverSocket != null) {
					msocket = serverSocket.accept();
					// 将新接入soket 加入列表中

					Log.v(TITLE, "获取链接服务器的地址" + msocket.getInetAddress().getHostAddress());

					ReceiveSoket thread = new ReceiveSoket(mContext,msocket, handler, clients);
					thread.start();
					SendSocket send = new SendSocket(msocket, handler, clients);
					send.start();
					clients.addSocket(msocket, send);
					// 21 连接ip
					Message msg = new Message();
					msg.what = 21;
					msg.obj = msocket.getInetAddress().getHostAddress();
					// 发送消息
					handler.sendMessage(msg);

				}
			}

		} catch (IOException e) {
			e.getCause();
			Message msg = new Message();
			msg.what = 30;
			msg.obj = "socket 连接已经断开";
			// 发送消息
			handler.sendMessage(msg);
			try {
				if (!msocket.isClosed()) {
					clients.closeSocket(msocket);
					msocket.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	/**
	 * 关闭 ServerSocket
	 */
	public void closeServerSocket() {
		try {

			this.serverSocket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static SocketServer instance = null;

	public static SocketServer getInstance(Context context, Handler _handler) {
		if (instance == null) {
			
			synchronized (SocketServer.class) {
				if (instance == null) {
					instance = new SocketServer(context,_handler);
				}
			}
		}
		return instance;
	}

}
