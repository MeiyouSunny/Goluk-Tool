package com.mobnote.golukmain.wifimanage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 发送socket服务
 * 
 * @author hanz
 * 
 */
public class SendSocket extends Thread {

	private final String TITLE = "SendSocket";
	private final  int BUFFER= 4*1024;
	
	private BufferedOutputStream bos = null;
	private SocketClients clients = null;
	/**
	 * 需要构造的参数
	 **/
	private Socket socket = null;
	private Handler handler = null;

	public Object mlock = new Object();
	BufferedInputStream fbis = null;

	public SendSocket( Socket _socket,
			Handler _handler, SocketClients _clients) {
		clients = _clients;
		socket = _socket;
		handler = _handler;


	}

	public void run() {

		try {
			 
			String[] filePaths = null;

			ConcurrentLinkedQueue<String> list = null;

			while (true) {
				synchronized (mlock) {

					// 如果socket 断开了
					if (socket == null || socket.isInputShutdown()) {
						break;
					}
					list = clients.getSocketFile(socket);
					if (list != null && list.size() > 0) {
						filePaths = (String[]) list.toArray(new String[0]);
						responseFlie(filePaths);

						for (String temp : filePaths) {
							Log.v("删除文件！！！", temp);
							clients.remSocketFile(socket, temp);
						}

						// clients.getSocketFile(socket).remove(filePath);
						// list.remove(filePath);
						// 移除已经处理过的文件

					} else {
						try {
							mlock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			closeSocket();
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			
			clients.closeSocket(socket);
			e.printStackTrace();
		}
	}

	/**
	 * 发送文件名
	 * 
	 * @param filePaths
	 * @throws JSONException
	 * @throws IOException
	 */
	public void sendFilePath(Socket socket, String[] filePathArray)
			throws JSONException, IOException {

		JSONObject file = new JSONObject();
		JSONArray paths = new JSONArray();
		if (filePathArray != null) {
			for (String filePath : filePathArray) {
			//	paths.put(filterPath(filePath));  这里不需要过滤文件夹
				paths.put(filePath);
			}
		}

		file.put("filepath", paths);
		byte[] bb = file.toString().getBytes();
		bos = new BufferedOutputStream(socket.getOutputStream());
		writeHead1(bos, 2, bb.length);
		bos.write(bb);
		bos.flush();

	}

	/**
	 * 发送文件
	 * 
	 * @param filePaths
	 * @throws IOException
	 */
	public void sendFile(Socket socket, String[] filePathArray)
			throws IOException {

		if (filePathArray != null) {
			for (int i = 0; i < filePathArray.length; i++) {
				byte[] buf = new byte[BUFFER];
				bos = new BufferedOutputStream(socket.getOutputStream());

				File sendFile = new File(doPath(filePathArray[i])); // 要发送的文件
				fbis = new BufferedInputStream(new FileInputStream(sendFile));
				int fileSize = (int) sendFile.length();
				Log.v("socket", "发送文件长度" + fileSize);
				Log.v("socket", "发送文件下标" + i);

				int len = 0;

				// writeHead1(bos,2,readBuffer.length);

				// bos.write(new String("yesyesyesyes").getBytes());
				// bos.flush();
				writeHead1(bos, 3, fileSize + 16);
				writeHead2(bos, i, 0, fileSize);
				while ((len = fbis.read(buf)) != -1) {
					// 如果是最后一次 修改start状态

					bos.write(buf, 0, len);
					bos.flush();

				}
				// 31 代表发送文件名称
				Message msg = new Message();
				msg.what = 31;
				msg.obj = filePathArray[i];
				Log.v("socket", "已发送文件名称" + filePathArray[i]);
				// 发送消息
				handler.sendMessage(msg);
				fbis.close();

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

	/**
	 * 封装小包头
	 * 
	 * @param bos
	 * @param fileid
	 * @param start
	 * @param fileSize
	 * @throws IOException
	 */
	private void writeHead2(BufferedOutputStream bos, int fileid, int start,
			int fileSize) throws IOException {
		// 写入 文件id
		bos.write(ByteUtil.int2Bytes(fileid));
		bos.write(ByteUtil.int2Bytes(start));
		bos.write(ByteUtil.int2Bytes(fileSize));
		bos.write(ByteUtil.int2Bytes(0));

	}

	/**
	 * 发送文件列表及文件 先发文件列表 后发文件
	 * 
	 * @param localfiles
	 * @throws IOException
	 * @throws JSONException
	 */
	public void responseFlie(String[] localfiles) throws IOException,
			JSONException {
		 //设置心跳包回复状态
		clients.setSendState(socket, false);
		// 比较本地文件后进行文件传输。;

		// 回写流文件名
		sendFilePath(socket, localfiles);
		// 如果有未同步数据进行同步
		/**
		 * 不写流
		if (localfiles != null) {
			// 回写文件
			sendFile(socket, localfiles);
		}
		 //恢复心跳包回复状态
	
		 **/
		clients.setSendState(socket, true);
	}

	/**
	 * 获取文件名
	 * 
	 * @param path
	 * @return
	 */
	private String filterPath(String path) {
		int count = path.lastIndexOf("/");
		return path.substring(count + 1);
	}
	
	/**
	 * 获取文件名
	 * 
	 * @param path
	 * @return
	 */
	private String doPath(String path) {
		 String rs="";
		String[] sz=new String[2];
		int count = path.lastIndexOf("/");
		sz[0]=path.substring(0,count);
		sz[1]=path.substring(count + 3);
		rs=sz[0]+"/"+sz[1];
		System.out.println(rs);
		return rs;
	}
	
	/**
	 * 关闭socket服务
	 */
	public void closeSocket() {
		 
		Log.v(TITLE, "socket 发送通讯异常，服务关闭");

		Message msg = new Message();
		msg.what = 30;
		msg.obj = "socket 连接已经断开";
		clients.closeSocket(socket);

	}
}
