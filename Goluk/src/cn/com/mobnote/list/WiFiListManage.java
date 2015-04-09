package cn.com.mobnote.list;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;

import cn.com.mobnote.util.console;
import cn.com.mobnote.wifibind.WifiRsBean;


/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:视频编辑页面选择音乐管理类
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("SimpleDateFormat")
public class WiFiListManage {
	@SuppressWarnings("unused")
	private Context mContext = null;
	private ArrayList<WiFiListData> mWiFiListData = new ArrayList<WiFiListData>();
	public WiFiListManage(Context context){
		mContext = context;
	}
	
	/**
	 * 获取本地滤镜主题列表
	 * @return
	 */
	public ArrayList<WiFiListData> getWiFiList(){
		return mWiFiListData;
	}
	
	/**
	 * 解析wifi列表数据
	 */
	public void analyzeWiFiData(WifiRsBean[] arrays){
		mWiFiListData.clear();
		for (WifiRsBean wifi : arrays) {
			String wifiName = wifi.getIpc_ssid();
			boolean wifiStatus = wifi.isIsconn();
			WiFiListData data = new WiFiListData();
			data.wifiName = wifiName;
			data.wifiStatus = wifiStatus;
			data.hasPwd = wifi.isPassnull();
			data.mac = wifi.getIpc_bssid();

			console.log("获取小车本wifi---getwifiList---" + wifiName + "---" + wifiStatus + "---mac---" + data.mac + "---pwd---" + data.hasPwd);
			mWiFiListData.add(data);
		}
	}
	
	public class WiFiListData{
		//wifi名称
		public String wifiName;
		//wifi状态标识
		public boolean wifiStatus = false;
		//wifi信号
		public int signal = 9;
		//是否有密码
		public boolean hasPwd = true;
		//mac地址
		public String mac = "";
	}
}










