package cn.com.mobnote.module.serveraddress;

public interface IGetServerAddressType {

	public static final int GetServerAddress_NoServer = 0;
	public static final int GetServerAddress_HttpServer = 1;
	public static final int GetServerAddress_TcpServer = 2;
	public static final int GetServerAddress_TcpServerPort = 3;
	public static final int GetServerAddress_UdpServer = 4;
	public static final int GetServerAddress_AirTalkeeServer = 5;
	public static final int GetServerAddress_RtmpServer = 6;

}
