package com.rd.veuisdk.net;

public class URLConstants {

	public static final int REQUESTCODE = 30;

	/**
	 * 接口的根目录
	 */
	private static final String ROOT_URL = "http://kx.56show.com/kuaixiu/";
	// private static final String ROOT_URL = "http://videoapi.rdsdk.com/";
	private static final String OPENAPI = ROOT_URL + "index.php/openapi/";

	public static final String SIGNATURE = "http://dianbook.17rd.com/api/appverify/signature";

	/**
	 * 百度获取地理位置
	 */
	public static final String BAIDU_GEO_SERVER_URL = "http://api.map.baidu.com/geocoder/v2/?ak=B6a52eecc9bba676e4784f04c8c02084&location=%s&output=json&pois=0";

	/**
	 * 新接口<br/>
	 * 接口根路径
	 */
	public static final String APP = OPENAPI + "app/";

	/**
	 * 获取扩展音乐信息
	 */
	// public static final String GETVIDEOMUSIC = VIDEO + "getvideomusic2";
	public static final String GETFONT = ROOT_URL + "openapi/video/getfont3";

	/**
	 * 特效
	 */
	public static final String STYLEURL = ROOT_URL + "openapi/video/getcaption2";
	/**
	 * 字幕
	 */
	public static final String GETZIMU = ROOT_URL + "openapi/video/getzimu3";
	private static final String MUSIC = OPENAPI + "music/";
	public static final String SINGERLIST = MUSIC + "singerlist";
	public static final String TRANSITIONLIST = MUSIC + "transitlist";
	public static final String THEMELIST = MUSIC + "themelist";
	public static final String MUSICDOWNCOUNT = MUSIC + "musicdowncount";
	public static final String GETMV = MUSIC + "getmv";
}
