package cn.com.tiros.api;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

/**
 * 2012-07-19 传感器实现
 * 
 * @author caoyingpeng
 * 
 */
public class SensorDetector implements SensorEventListener {

	/**
	 * @brief 传感器类型枚举，描述中若参数值为0,则表示无意义
	 */

	/** 方向感应，dwparam1:指北方位的角度，dwparam2：0 */
	public static final int ST_GYROSCOPE = 1;
	/** 光感，dwparam1：1--光线被遮住，2-光线被恢复 dwparam2：0 */
	public static final int ST_LIGHT = 2;
	/** 摇晃，dwparam1：0，dwparam2：0 */
	public static final int ST_SHAKE = 3;

	/** 如果不支持该传感器功能，则返回此值 */
	public static final int SENSOR_UNSUPPORT = -1;
	/** 光线被遮住 */
	private static final int LIGHT_BLOCKED = 1;
	/** 光线被恢复 */
	private static final int LIGHT_RESTORE = 2;

	/** 当前光线状态 */
	private static int curLightState = 0;
	/** 传感器管理 */
	private SensorManager mSensorManager;
	/** 方向传感器对象 */
	private Sensor mGyroscopeSensor;
	/** 感光检测对象 */
	private Sensor mLightSensor;
	/** 摇晃检测对象 */
	private Sensor mShakeSensor;

	private Map<Integer, SensorListener> mGyroscope_Listeners;
	private Map<Integer, SensorListener> mLight_Listeners;
	private Map<Integer, SensorListener> mShake_Listeners;

	private float[] Orientation = new float[3];

	private int listenerid = 0;

	private float srcX = 0;
	private float testX = 0;

	private float sum = 0;
	private int cont = 0;

	/** 检测的时间间隔 */
	private static final int UPDATE_INTERVAL = 100;
	/** 上一次检测的时间 */
	private long mLastUpdateTime;
	/** 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。 */
	private float mLastX, mLastY, mLastZ;
	/** 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感. */
	public int shakeThreshold = 2300;

	/**
	 * 传感器systemapi
	 * 
	 * @author caoyingpeng
	 * 
	 */
	class SensorListener {

		public int id = 0;// 当前监听id
		public int pfn_handler = 0;// 回调函数指针
		public int puser_handler = 0;// 回调对象指针
		public int sensortype = 0;

	}

	public SensorDetector() {
		mSensorManager = (SensorManager) Const.getAppContext().getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * 判断该设备是否支持陀螺仪功能
	 * 
	 * @return true 支持 ，false 不支持
	 */
	public static boolean isSupportOrientationSensor() {
		SensorManager mSensorManager = (SensorManager) Const.getAppContext().getSystemService(Context.SENSOR_SERVICE);
		Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (mSensor == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @brief 启动指定类型传感器
	 * @param[in] st - 传感器类型
	 * @param[in] pfn - 回调函数地址
	 * @param[in] pvuser - 请求
	 * @return - sensor：传感器句柄，若不支持该功能,则返回SENSOR_UNSUPPORT
	 */
	public int sys_sensorstart(int st, int pfn, int pvuser) {
		listenerid++;
		SensorListener listener = new SensorListener();
		listener.id = listenerid;
		listener.sensortype = st;
		listener.pfn_handler = pfn;
		listener.puser_handler = pvuser;

		// 添加对象监听
		if (start(listener, listenerid)) {
			return listenerid;
		} else {
			return SENSOR_UNSUPPORT;
		}
	}

	/**
	 * @brief 停止指定的传感器
	 * @param[in] s - 传感器类型
	 * @return - 无
	 */
	public void sys_sensorstop(int id) {
		stop(id);
	}

	/**
	 * 启动传感器检测
	 * 
	 * @param listener
	 * @param id
	 * @return 功能开启成功 true, 否则返回false
	 */
	@SuppressLint("UseSparseArrays")
	private boolean start(SensorListener listener, int id) {
		// 启动遥感检测
		if (ST_GYROSCOPE == listener.sensortype) {// 方向感应
			if (mGyroscope_Listeners == null) {
				if (!registerGyroscope()) {
					return false;
				}
				mGyroscope_Listeners = new HashMap<Integer, SensorListener>();
			}
		} else if (ST_LIGHT == listener.sensortype) {// 光线遮罩
			if (mLight_Listeners == null) {
				if (!registerLight()) {
					return false;
				}
				mLight_Listeners = new HashMap<Integer, SensorListener>();
			}
		} else if (ST_SHAKE == listener.sensortype) {// 摇晃
			if (mShake_Listeners == null) {
				if (!registerShake()) {
					return false;
				}
				mShake_Listeners = new HashMap<Integer, SensorListener>();
			}
		}
		registerOnSensorListener(listener.sensortype, listener, id);
		return true;
	}

	/**
	 * 开启检测方向感应
	 * 
	 * @return 成功返回true，否则返回false
	 */
	private boolean registerGyroscope() {
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (mGyroscopeSensor == null) {
			return false;
		}
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
		return true;
	}

	/**
	 * 开启检测光线遮罩
	 * 
	 * @return 成功返回true，否则返回false
	 */
	private boolean registerLight() {
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (mLightSensor == null) {
			return false;
		}
		mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_GAME);
		return true;
	}

	/**
	 * 开启检测摇晃
	 * 
	 * @return 成功返回true，否则返回false
	 */
	private boolean registerShake() {
		mShakeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (mShakeSensor == null) {
			return false;
		}
		mSensorManager.registerListener(this, mShakeSensor, SensorManager.SENSOR_DELAY_GAME);
		return true;
	}

	/**
	 * 停止检测
	 */
	private void stop(int id) {
		unregisterOnSensorListener(id);
		if (mSensorManager != null) {
			if (mGyroscope_Listeners == null) {
				mSensorManager.unregisterListener(this, mGyroscopeSensor);
			}
			if (mLight_Listeners == null) {
				mSensorManager.unregisterListener(this, mLightSensor);
			}
			if (mShake_Listeners == null) {
				mSensorManager.unregisterListener(this, mShakeSensor);
			}
		}
	}

	/**
	 * 注册SensorListener，当传感器有改变时接收通知
	 * 
	 * @param listener
	 */
	private void registerOnSensorListener(int sensortype, SensorListener listener, int id) {
		if (sensortype == ST_GYROSCOPE) { // 方向感应
			if (mGyroscope_Listeners.containsKey(id))
				return;
			mGyroscope_Listeners.put(id, listener);
		} else if (sensortype == ST_LIGHT) {
			if (mLight_Listeners.containsKey(id))
				return;
			mLight_Listeners.put(id, listener);
		} else if (sensortype == ST_SHAKE) {
			if (mShake_Listeners.containsKey(id))
				return;
			mShake_Listeners.put(id, listener);
		}
	}

	/**
	 * 移除已经注册的SensorListener 监听
	 * 
	 * @param listener
	 */
	private void unregisterOnSensorListener(int id) {
		if (mGyroscope_Listeners != null && mGyroscope_Listeners.containsKey(id)) {
			mGyroscope_Listeners.remove(id);
			if (mGyroscope_Listeners.isEmpty()) {
				mGyroscope_Listeners.clear();
				mGyroscope_Listeners = null;
			}
			return;
		}
		if (mLight_Listeners != null && mLight_Listeners.containsKey(id)) {
			mLight_Listeners.remove(id);
			if (mLight_Listeners.isEmpty()) {
				mLight_Listeners.clear();
				mLight_Listeners = null;
			}
		}
		if (mShake_Listeners != null && mShake_Listeners.containsKey(id)) {
			mShake_Listeners.remove(id);
			if (mShake_Listeners.isEmpty()) {
				mShake_Listeners.clear();
				mShake_Listeners = null;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();
		if (type == Sensor.TYPE_ORIENTATION && mGyroscope_Listeners != null) {
			// 方向感应
			// 计算陀螺仪方向
			callBack_Orientation(event);
		} else if (type == Sensor.TYPE_ACCELEROMETER && mShake_Listeners != null) {
			// 重力加速度
			callBack_Accelerometer(event);

		} else if (type == Sensor.TYPE_LIGHT && mLight_Listeners != null) {
			// 光线感应
			callBack_Light(event);
		}
	}

	private void callBack_Light(SensorEvent event) {
		int value = (int) event.values[SensorManager.DATA_X];

		if (value <= 10) { // 光线被遮住
			if (curLightState != LIGHT_BLOCKED) {

				// for (SensorListener listener : mLight_Listeners.values())
				// {
				// SensorChanged(listener.puser_handler,
				// listener.pfn_handler, ST_LIGHT, LIGHT_BLOCKED, 0);
				// }

				curLightState = LIGHT_BLOCKED;
			}

		} else if (value >= 50) {

			if (curLightState != LIGHT_RESTORE) {

				for (SensorListener listener : mLight_Listeners.values()) {

					// SensorChanged(listener.puser_handler,
					// listener.pfn_handler, ST_LIGHT, LIGHT_RESTORE, 0);

				}

				curLightState = LIGHT_RESTORE;

			}
		}
	}

	// 重力加速度回调
	private void callBack_Accelerometer(SensorEvent event) {
		long currentTime = System.currentTimeMillis();
		long diffTime = currentTime - mLastUpdateTime;
		if (diffTime < UPDATE_INTERVAL)
			return;
		mLastUpdateTime = currentTime;
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		float deltaX = x - mLastX;
		float deltaY = y - mLastY;
		float deltaZ = z - mLastZ;
		mLastX = x;
		mLastY = y;
		mLastZ = z;
		float delta = (float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / diffTime * 10000);
		if (delta > shakeThreshold) { // 当加速度的差值大于指定的阈值，认为这是一个摇晃
			for (SensorListener listener : mShake_Listeners.values()) {
				SensorChanged(listener.puser_handler, listener.pfn_handler, ST_SHAKE, x, y, z);
			}
		}
	}

	// 方向感应回调
	private void callBack_Orientation(SensorEvent event) {
		Orientation[0] = event.values[SensorManager.DATA_X];
		int valueX = Math.round(Orientation[0]);
		if (valueX % 6 == 0) {
			if (testX == valueX) {
				return;
			} else {
				for (SensorListener listener : mGyroscope_Listeners.values()) {
					SensorChanged(listener.puser_handler, listener.pfn_handler, ST_GYROSCOPE, valueX, 0, 0);
				}
				testX = valueX;
			}
		} else {
			float X = (float) (Math.floor(valueX / 6) * 6 + 3);

			if (X == srcX) {
				return;
			} else {
				srcX = X;
				for (SensorListener listener : mGyroscope_Listeners.values()) {
					SensorChanged(listener.puser_handler, listener.pfn_handler, ST_GYROSCOPE, X, 0, 0);
				}
			}
		}

	}

	/**
	 * 
	 * @param puser
	 *            回调对象指针
	 * @param pfn
	 *            回调函数指针
	 * @param st
	 *            回调类型
	 * @param x
	 *            回调参数1
	 * @param dwparam2
	 *            回调参数2 陀螺仪，dwparam1:指北方位的角度，dwparam2：0
	 *            光感，dwparam1：1--光线被遮住，2-光线被恢复 dwparam2：0
	 *            摇晃，dwparam1：0，dwparam2：0
	 */
	public static native void SensorChanged(int puser, int pfn, int st, float x, float y, float z);

}
