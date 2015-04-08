//package cn.com.mobnote.wifibind;
//
//import cn.com.mobnote.talk.wifimanage.R;
//import cn.com.mobnote.wifisupport.WifiConnectManager;
//import cn.com.mobnote.wifisupport.WifiConnectManagerSupport.WifiCipherType;
//import cn.com.mobnote.wifisupport.WifiEnum.WifTypePassEnum;
//import cn.com.mobnote.wifisupport.WifiRsBean;
//
//import cn.com.mobnote.wifisupport.WifiEnum.WifTypeEnum;
//
//import android.app.Activity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//
//import android.net.wifi.WifiManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//public class WifiActivity extends Activity implements WifiConnCallBack {
//	private static final String TAG = "testhan";
//
//	private Button mBtn1, mBtn2, mBtn3, mBtn4, mBtn5, mBtn6, mBtn7;
//	private TextView textView1;
//	private WifiConnectManager connectManage = null;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		// 加载视图配置
//		setContentView(R.layout.activity_wifi);
//		//
//		mBtn1 = (Button) findViewById(R.id.main_button1);
//		mBtn2 = (Button) findViewById(R.id.main_button2);
//		mBtn3 = (Button) findViewById(R.id.main_button3);
//		mBtn4 = (Button) findViewById(R.id.main_button4);
//		mBtn5 = (Button) findViewById(R.id.main_button5);
//		mBtn7 = (Button) findViewById(R.id.main_button7);
//		textView1= (TextView) findViewById(R.id.bindtextView1);
//		mBtn1.setOnClickListener(new Button.OnClickListener() {
//
//			// 通过SSID ----------扫描 wifi列表 -----------------------------
//			@Override
//			public void onClick(View v) {
//
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//
//				connectManage.scanWifiList("tiros");
//
//			}
//		});
//		// wifi链接
//		mBtn2.setOnClickListener(new Button.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//				String ssid = "Test123";
//				String password = "";
//				String mac = "74:51:ba:6e:3e:57";
//				// String mac="";
//				connectManage.connectWifi(ssid, password,
//						WifiCipherType.WIFICIPHER_NOPASS);
//
//			}
//
//		});
//		// 自动连接
//		mBtn3.setOnClickListener(new Button.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//		 
//				connectManage.autoWifiManage();
//
//			}
//
//		});
////		// 当前是否连接Ipc
////		mBtn4.setOnClickListener(new Button.OnClickListener() {
////
////			@Override
////			public void onClick(View v) {
////				WifiManager wm = (WifiManager) WifiActivity.this
////						.getSystemService(Context.WIFI_SERVICE);
////				connectManage = new WifiConnectManager(wm, WifiActivity.this);
////
////				connectManage.getClientList();
////			}
////
////		});
//		//保存wifi信息
//		mBtn5.setOnClickListener(new Button.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//				WifiRsBean beans = new WifiRsBean();
//				beans.setIpc_ip("A");
//				beans.setIpc_mac("B");
//				beans.setIpc_ssid("C");
//				beans.setPh_ip("D");
//				beans.setPh_mac("E");
//				beans.setPh_ssid("F");
//				connectManage.saveConfiguration(beans);
//			}
//
//		});
//
//
//	}
//
//	/*
//	 * 返回结果回调
//	 * 
//	 * @see android.app.Activity#onActivityResult(int, int,
//	 * android.content.Intent)
//	 */
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (resultCode) { // resultCode为回传的标记，我在B中回传的是RESULT_OK
//		case RESULT_OK:
//			Bundle b = data.getExtras(); // data为B中回传的Intent
//			String str = b.getString("str1");// str即为回传的值
//			break;
//		default:
//			break;
//		}
//	}
//
//	/*
//	 * 回调接口 用于
//	 * 
//	 * @see
//	 * com.example.demoandroid.wifipassword.WifiConnCallBack#wifiCallBack(int,
//	 * java.lang.String, java.lang.String[])
//	 */
//	@Override
//	public void wifiCallBack(int type, int state, int process, String message,
//			Object arrays) {
//		WifiRsBean[] beans = null;
//		WifiRsBean bean = null;
//		switch (type) {
//		case 1:
//			textView1.setText("Wifilist: \n\n");
//			Log.e(TAG, "get wifilist ------------- ok--------------");
//			beans = (WifiRsBean[]) arrays;
//			if (beans != null) {
//				for (WifiRsBean temp : beans) {
//					textView1.append("####################\n");
//					textView1.append("ssid: " + temp.getIpc_ssid() + ";");
//					textView1.append("singnal: " + temp.getWifiSignal() + ";");
//					textView1.append("mac: " + temp.getIpc_mac() + ";");
//					textView1.append("passnull: " + temp.isPassnull() + ";");
//					textView1.append("isconn: " + temp.isIsconn() + ";");
//					temp.getIpc_ip();
//					temp.getWifiSignal();
//					temp.getIpc_mac();
//					temp.isPassnull();
//					temp.isIsconn();
//					Log.e(TAG, temp.getPh_ssid() + "--------------"
//							+ "isconn-----" + temp.isIsconn());
//				}
//			} else {
//				textView1.append("nulllllllllllll\n");
//				Log.e(TAG, "get wifilist ------------- empty--------------");
//			}
//			break;
//		case 2:
//			Log.e(TAG, "wifi scan erro  -1-------------timeout--------------");
//			break;
//		case 3:
//			bean = (WifiRsBean) arrays;
//			bean.getIpc_ssid();
//			// unregisterReceiver(connectManage);
//			Log.e(TAG, "create wifi ------------- ok--------------");
//			break;
//
//		case 4:
//			beans = (WifiRsBean[]) arrays;
//			if (beans != null) {
//				for (WifiRsBean temp : beans) {
//					Log.e(TAG, temp.getIpc_ssid());
//				}
//			}
//			break;
//		default:
//			break;
//		}
//	}
//}