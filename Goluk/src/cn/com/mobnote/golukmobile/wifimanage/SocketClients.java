package cn.com.mobnote.golukmobile.wifimanage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * 发送文件处理
 * 
 * @author hanzheng
 * 
 */
public class SocketClients {

	

	// 存储socket
	public List<Socket> lists = new ArrayList<Socket>();

	// 输出文件流的状态 true 没有输出 false 正在输出
	public ConcurrentMap<Socket, Boolean> socketSendState = new ConcurrentHashMap<Socket, Boolean>();
	// 存储 待输出文件列表
	public ConcurrentMap<Socket, ConcurrentLinkedQueue<String>> socketFiles = new ConcurrentHashMap<Socket, ConcurrentLinkedQueue<String>>();
	// 存储socket对应的输出线程
	public ConcurrentMap<Socket, SendSocket> mapThread = new ConcurrentHashMap<Socket, SendSocket>();

	/**
	 * 删除socket
	 * 
	 * @param socket
	 */
	private void removSocket(Socket socket) {

		lists.remove(socket);
		// 如果服务中存在此socket
		if (!socketFiles.containsKey(socket)) {
			socketFiles.remove(socket);
			mapThread.remove(socket);
		}
	}

	/**
	 * 添加socket
	 * 
	 * @param socket
	 */
	public synchronized void addSocket(Socket socket, SendSocket send) {

		lists.add(socket);
		// 如果服务中存在此socket
		if (!socketFiles.containsKey(socket)) {
			socketFiles.put(socket, new ConcurrentLinkedQueue<String>());
		}
		// 将线程加入
		if (!mapThread.containsKey(socket)) {
			mapThread.put(socket, send);

		}
		// 将线程加入
		if (!socketSendState.containsKey(socket)) {
			socketSendState.put(socket, true);
		}
	}

	/**
	 * 添加socket文件
	 * 
	 * @param socket
	 * @param filePath
	 */
	public synchronized boolean addSocketFile(Socket socket,
			String... filePaths) {

		ConcurrentLinkedQueue<String> list = socketFiles.get(socket);
		if (list != null) {
			if (filePaths != null) {
				for (String filePath : filePaths) {
					list.add(filePath);
				}

				SendSocket tt = mapThread.get(socket);
				// 有新的文件添加后 解锁
				synchronized (tt.mlock) {
					tt.mlock.notify();
				}
			}

			return true;
		}
		return false;

	}

	/**
	 * 添加socket文件
	 * 
	 * @param socket
	 * @param filePath
	 */ 
	public void addSocketFile(String... filePaths) {

		for (Socket temp : lists) {
			addSocketFile(temp, filePaths);
		}

	}

	/**
	 * 获取socket文件
	 * 
	 * @param socket
	 * @param filePath
	 */
	public ConcurrentLinkedQueue<String> getSocketFile(Socket socket) {
		ConcurrentLinkedQueue<String> list = socketFiles.get(socket);
		if (list == null) {

			return null;
		}
		return list;

	}

	/**
	 * 删除socket文件
	 * 
	 * @param socket
	 * @param filePath
	 */
	public boolean remSocketFile(Socket socket, String filePath) {
		ConcurrentLinkedQueue<String> list = socketFiles.get(socket);
		if (list != null) {
			list.remove(filePath);
			return true;
		}
		return false;

	}

	/**
	 * 关闭socket服务
	 */
	public void closeSocket(Socket socket) {

		try {
			if (!socket.isClosed()) {
				// 从列表中删除
				removSocket(socket);
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * private static SocketClients instance = null;
	 * 
	 * public static SocketClients getInstance() { if (instance == null) {
	 * synchronized (SocketClients.class) { if (instance == null) { instance =
	 * new SocketClients(); } } } return instance; }
	 **/

	/**
	 * 设置心跳的回复状态
	 * 
	 * @param socket
	 * @param flag
	 */
	public void setSendState(Socket socket, boolean flag) {
		socketSendState.put(socket, flag);
	}

	/**
	 * 设置心跳的回复状态
	 * 
	 * @param socket
	 * @param flag
	 */
	public boolean getSendState(Socket socket) {
		Boolean s = socketSendState.get(socket);
		if (s == null) {
			return false;
		} else {
			return s.booleanValue();
		}
	}
}
