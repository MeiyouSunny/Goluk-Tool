//package cn.com.mobnote.wifibind;
// 
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
//	private TextView view1;
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
//
//		mBtn1.setOnClickListener(new Button.OnClickListener() {
//
//// 通过SSID   ----------扫描  wifi列表  -----------------------------
//			@Override
//			public void onClick(View v) {
//
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//
//		 
//				connectManage.scanWifiList("Test123", 4000);
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
//				connectManage.connectWifi(ssid, password,mac,
//						WifiCipherType.WIFICIPHER_NOPASS, 5000);
//				 
//			}
//
//		});
//		// 创建热点
//		mBtn3.setOnClickListener(new Button.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//				// registerReceiver(connectManage, new
//				// IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
//				String ssid = "mwifi";
//				String password = "123456789";
//				// String mac="";
//				connectManage.createWifiAP(ssid, password, 0);
//
//			}
//
//		});
// 
//
//		mBtn4.setOnClickListener(new Button.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				WifiManager wm = (WifiManager) WifiActivity.this
//						.getSystemService(Context.WIFI_SERVICE);
//				connectManage = new WifiConnectManager(wm, WifiActivity.this);
//	 
//				connectManage.getClientList();
//			}
//
//		});
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
//	public void wifiCallBack(int state, String message, Object arrays) {
//		WifiRsBean[] beans=null;
//		WifiRsBean bean=null;
//		switch (state) {
//		case 1:
//			Log.e(TAG, "get wifilist ------------- ok--------------");
//			beans = (WifiRsBean[]) arrays;
//			if (beans != null) {
//				for (WifiRsBean temp : beans) {
//					
//					temp.getSsid();
//					temp.getWifiSignal();
//					temp.getMacaddress();
//					temp.isPassnull();
//					temp.isIsconn();
//					Log.e(TAG, temp.getSsid() +"--------------"+"isconn-----"+temp.isIsconn());
//				}
//			}else{
//				Log.e(TAG, "get wifilist ------------- empty--------------");
//			}
//			break;
//		case -1:
//			Log.e(TAG, "wifi scan erro  -1-------------timeout--------------");
//			break;
//		case 11:
//
//			  bean = (WifiRsBean) arrays;
//			bean.getSsid();
//			// unregisterReceiver(connectManage);
//			Log.e(TAG, "create wifi ------------- ok--------------");
//			break;
//
//		case 12:
//
//			  beans = (WifiRsBean[]) arrays;
//			if (beans != null) {
//				for (WifiRsBean temp : beans) {
//					Log.e(TAG, temp.getSsid());
//				}
//			}
//			break;
//		default:
//			break;
//		}
//	}
//}