package com.mobnote.golukmain.wifimanage;

 
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * socket服务接收类
 * @author Harry
 *
 */
public class ReceiveSoket extends Thread  {
	private static final int TIMEOUTVALUE = 15; // 超过15秒钟 断开连接
	private static final int TIMEOUT = 3000;// 间隔3秒钟检查一次

	private final String TITLE = "SoketTransfers";
	private Socket socket = null;
	private Handler handler = null;
	private boolean fg_end = false; //是否读取的心跳检查


	private Date dateTime = new Date(); // 心跳时间戳
	boolean heartTg = true;// 心跳标志

	private BufferedOutputStream bos = null;
 
	private SocketClients clients;
	byte[] base = {};
	boolean readHead = true;
	int bodylen = 0;
	int headType = -1;
	public static int HEADLEN = 12;
	private Context mContext = null;
	
	public ReceiveSoket(Context context,Socket _socket, Handler _handler,SocketClients _clients) {
		mContext = context;
		clients=_clients;
		this.handler = _handler;
		socket = _socket;
		//启动心跳定时器
		doTimer();
		
		
	}

	/**
	 * 从客户端获取消息
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */

	public void doWork(Socket _socket) {
		InputStream bis = null;
		try {
			int len = 0;
			
			bis = socket.getInputStream();
			byte[] buf = new byte[1024];
			while ((len = bis.read(buf)) != -1) {

				// 将流转换为数组
				doByte(buf, len);
			}
			fg_end = true;
		} catch (Exception e) {
			// 重置流信息

			e.printStackTrace();
			closeSocket();
		}

	}

	/**
	 * 关闭socket服务
	 */
	public void closeSocket() {
		fg_end = true;
		Log.v(TITLE, "socket 接收通讯异常，服务关闭");
		Message msg = new Message();
		msg.what = 30;
		msg.obj = "socket 连接已经断开";
		clients.closeSocket(socket);

	}

	/**
	 * 心跳比较
	 * 
	 * @return
	 */
	private boolean compDate() {
		// 读流==-1 结束
		if (fg_end) {
			return false;
		}
		// 没有心跳前
		if (dateTime == null) {
			return true;
		}
		long time = getSubductionDay(new Date(), dateTime);
		if (time > TIMEOUTVALUE) {
			return false;
		}
		return true;

	}

	/**
	 * 定时器
	 */
	public void doTimer() {
		// 启动后15秒监听
		MyTimerCheck timerCheck = new MyTimerCheck() {

			@Override
			public void doTimerCheckWork() {
				// 如果在指定的时间内没有收到心跳请求 关闭soket
				if (!compDate()) {
					closeSocket();
					this.exit();
				}
			}

			@Override
			public void doTimeOutWork() {

			}
		};
		timerCheck.start(-1, TIMEOUT);

	}

	/**
	 * 得到两个日期的差
	 * 
	 * @param date1
	 * @param date2
	 * @return 如果 date1> date2 结果为“-”
	 */
	public long getSubductionDay(Date date1, Date date2) {
		long day = (date1.getTime() - date2.getTime()) / 1000;
		return day;
	}

	@Override
	public void run() {
		doWork(socket);
	}

	public void doByte(byte[] buf, int len) throws IOException, JSONException {
		String[] localfiles=null;
		// byte[] bb=bos2.write(buf, 0, len );
		byte[] temp = new byte[base.length + len];
		byte[] body = null;
		String[] fileRs = null;
		System.arraycopy(base, 0, temp, 0, base.length);
		System.arraycopy(buf, 0, temp, base.length, len);
		// 将总长度计入
		base = temp;

		while (true) {

			// 将新的字节追加到总字节后面

			// 如果要读取的是包头
			if (readHead) {
				if (base.length < HEADLEN || base.length == 0) {
					break;
				} else {
					headType = ByteUtil.bytes2Int(base, 0, 3);
					bodylen = ByteUtil.bytes2Int(base, 4, 7);
					// 将包头减去
					temp = new byte[base.length - HEADLEN];
					System.arraycopy(base, HEADLEN, temp, 0, base.length
							- HEADLEN);
					base = temp;
					readHead = !readHead;
				}

			} else {  //处理文件发送
				// 如果当前长度不足以读取body体返回
				if (base.length < bodylen) {
					break;
				} else {
				 //如果是心跳包协议
					if (headType == 0) {
						dateTime = new Date(); // 心跳时间戳
						
						//如果是false表示正在处理发送。不发送心跳包
						if (  clients.getSendState(socket)) {
							bos = new BufferedOutputStream(
									socket.getOutputStream());
							writeHead1(bos, 0, 0);
							bos.flush();
							Log.v(TITLE, "收到心跳包，并发送心跳");
						}else{
							Log.v(TITLE, "收到心跳包，发送文件中，不返回心跳");
						}
					} else { //发送文件协议
						Log.v(TITLE, "收到文件数据包");
						body = new byte[bodylen];
						// 将body减去
						temp = new byte[base.length - bodylen];
						System.arraycopy(base, bodylen, temp, 0, base.length
								- bodylen);
						System.arraycopy(base, 0, body, 0, bodylen);
						base = temp;
						String json = new String(body);
						Log.v(TITLE, "接收数据文件集合" + json);
						JSONObject jsonObject = new JSONObject(json);
						JSONArray filepath = jsonObject
								.getJSONArray("filepath");// 获取JSONArray
						int length = filepath.length();
						String oj = null;
						if (length > 0) {
							fileRs = new String[length];
							for (int i = 0; i < length; i++) {// 遍历JSONArray

								oj = (String) filepath.get(i);
								fileRs[i] = oj;

							}
						}
						localfiles = new FileManage(mContext,handler).getFiles(fileRs);
						if(localfiles!=null){
							// 将文件列表信息插入队列
							clients.addSocketFile(socket,localfiles);
						}
						
						
						
						/**
						// 回复文件及列表
						new Thread(
								new SendSocket(this, socket, handler, fileRs))
								.start();
								**/
					}
					readHead = !readHead;
				}

			}

		}
	}

	

	/**
	 * 封装大包头
	 * 
	 * @param bos
	 * @param type
	 * @param bufferSize
	 * @throws IOException
	 */
	private void writeHead1(BufferedOutputStream bos, int type, int bufferSize)
			throws IOException {
		// 写入 文件id
		bos.write(ByteUtil.int2Bytes(type));
		bos.write(ByteUtil.int2Bytes(bufferSize));
		bos.write(ByteUtil.int2Bytes(0));

	}

 
}
