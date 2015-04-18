package cn.com.mobnote.wifibind;


import java.lang.reflect.Field;  
import java.lang.reflect.Method;  
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;  
import java.util.Map;  
  
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;  
import android.net.wifi.WifiManager;  
import android.os.Build; 
import android.text.TextUtils;
import android.util.Log;

 
 

public class WifiApManagerSupport {
	private static final String tag = "WifiApManager";

	private static final String METHOD_GET_WIFI_AP_STATE = "getWifiApState";
	private static final String METHOD_SET_WIFI_AP_ENABLED = "setWifiApEnabled";
	private static final String METHOD_GET_WIFI_AP_CONFIG = "getWifiApConfiguration";
	private static final String METHOD_IS_WIFI_AP_ENABLED = "isWifiApEnabled";

	private static final Map<String, Method> methodMap = new HashMap<String, Method>();
	private static Boolean mIsSupport;
	private static boolean mIsHtc;

	public synchronized static final boolean isSupport() {
		if (mIsSupport != null) {
			return mIsSupport;
		}

		boolean result = Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO;
		if (result) {
			try {
				Field field = WifiConfiguration.class
						.getDeclaredField("mWifiApProfile");
				mIsHtc = field != null;
			} catch (Exception e) {
			}
		}

		if (result) {
			try {
				String name = METHOD_GET_WIFI_AP_STATE;
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = METHOD_SET_WIFI_AP_ENABLED;
				Method method = WifiManager.class.getMethod(name,
						WifiConfiguration.class, boolean.class);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = METHOD_GET_WIFI_AP_CONFIG;
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = getSetWifiApConfigName();
				Method method = WifiManager.class.getMethod(name,
						WifiConfiguration.class);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = METHOD_IS_WIFI_AP_ENABLED;
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		mIsSupport = result;
		return isSupport();
	}

	private final WifiManager mWifiManager;

	WifiApManagerSupport(WifiManager manager) {
		if (!isSupport()) {
			throw new RuntimeException("Unsupport Ap!");
		}
		Log.i(tag, "Build.BRAND -----------> " + Build.BRAND);

		mWifiManager = manager;
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public int getWifiApState() {
		try {
			Method method = methodMap.get(METHOD_GET_WIFI_AP_STATE);
			return (Integer) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return 0;
	}

	public WifiConfiguration getWifiApConfiguration() {
		WifiConfiguration configuration = null;
		try {
			Method method = methodMap.get(METHOD_GET_WIFI_AP_CONFIG);
			configuration = (WifiConfiguration) method.invoke(mWifiManager);

		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return configuration;
	}

	public boolean setWifiApConfiguration(WifiConfiguration netConfig) {
		boolean result = false;
		try {
			/**
			 * if (isHtc()) { setupHtcWifiConfiguration(netConfig); }
			 **/
			Method method = methodMap.get(getSetWifiApConfigName());
			Class<?>[] params = method.getParameterTypes();
			for (Class<?> clazz : params) {
				Log.i(tag, "param -> " + clazz.getSimpleName());
			}

			result = (Boolean) method.invoke(mWifiManager, netConfig);

		} catch (Exception e) {
			Log.e(tag, "", e);
		}
		return result;
	}

	public boolean setWifiApEnabled(WifiConfiguration configuration,
			boolean enabled) {
		boolean result = false;
		try {
			Method method = methodMap.get(METHOD_SET_WIFI_AP_ENABLED);
			result = (Boolean) method.invoke(mWifiManager, configuration,
					enabled);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	public boolean isWifiApEnabled() {
		boolean result = false;
		try {
			Method method = methodMap.get(METHOD_IS_WIFI_AP_ENABLED);
			result = (Boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	private static String getSetWifiApConfigName() {
		return mIsHtc ? "setWifiApConfig" : "setWifiApConfiguration";
	}
	
	public  String getNetworkIpAddress(String name) {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces
						.nextElement();
				Enumeration<InetAddress> enumeration = networkInterface
						.getInetAddresses();
				while (enumeration.hasMoreElements()) {
					InetAddress inetAddress = (InetAddress) enumeration
							.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address
							&& TextUtils.equals(name,
									networkInterface.getDisplayName())) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public  String getApName(Context context) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			Method method = connectivityManager.getClass().getMethod(
					"getTetheredIfaces");
			String[] names = (String[]) method.invoke(connectivityManager);
			return names[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	     /**
	       * Endable/disable wifi
	       * @param enabled
	       * @return WifiAP state
	       */
	      public void closeWifiAP( ) {
	          Log.d("WifiAP", "*** setWifiApEnabled CALLED **** " );
	          try {
	          if (mWifiManager.getConnectionInfo() !=null) {
	        	  mWifiManager.setWifiEnabled(false);
	              try {Thread.sleep(1500);} catch (Exception e) {}
	          
	   
	        	  mWifiManager.setWifiEnabled(false);
	              Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
	                  WifiConfiguration.class, boolean.class);
	              method1.invoke(mWifiManager, null, false); // true
	              
	          }
	          } catch (Exception e) {
	            
	             // toastText += "ERROR " + e.getMessage();
	          }
	          
	      }
}  
