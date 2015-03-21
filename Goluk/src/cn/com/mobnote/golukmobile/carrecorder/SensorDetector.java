package cn.com.mobnote.golukmobile.carrecorder;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;

/**
  * 1.编辑器必须显示空白处
  *
  * 2.所有代码必须使用TAB键缩进
  *
  * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
  *
  * 4.注释必须在行首写.(枚举除外)
  *
  * 5.函数使用块注释,代码逻辑使用行注释
  *
  * 6.文件头部必须写功能说明
  *
  * 7.所有代码文件头部必须包含规则说明
  *
  * 传感器控制管理类
  *
  * 2014-11-5
  */
public class SensorDetector {
	private Activity mActivity;
	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;
	private SensorEventListener  mAccelerometerSensorEventListener;
	private AccelerometerListener mAccelerometerListener;
	private float ax_old=0;
	private float ay_old=0;
	private float az_old=0;
	private Timer mTimer=null;
	private int recordTime=0;
	
	public SensorDetector(Activity activity){
		mActivity = activity;
		mSensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
	}
	
	/**
	 * 判断是否支持加速度计传感器
	 * @return
	 */
	public static boolean isSupportAccelerometerSensor(Activity activity){
		SensorManager manager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
		if(sensor != null){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 注册加速度计监听
	 * @param listener
	 * @author xuhw
	 * @date 2014-11-5
	 */
	public void registerAccelerometerListener(AccelerometerListener listener){
		mAccelerometerListener=listener;
		
		mAccelerometerSensorEventListener = new SensorEventListener() {
	         public void onSensorChanged(SensorEvent event) {
	             if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
	                 return;
	             }
	             
	             float[] values = event.values;
	             float ax = values[0];
	             float ay = values[1];
	             float az = values[2];
	             
	             int medumValue = 19;
	             if (Math.abs(ax) > medumValue || Math.abs(ay) > medumValue || Math.abs(az) > medumValue) {
//	             if((ax_old != 0 && (Math.abs(ax - ax_old) > 25)) || (ay_old != 0 && (Math.abs(ay - ay_old) > 25)) || (az_old != 0 && (Math.abs(az - az_old) > 25))){
	            	 if(null == mTimer){
	            		 mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if(null != mAccelerometerListener){
					            		 mAccelerometerListener.onChanged();
					            	 }
								}
	            		 });
	            		 
	            		 mTimer = new Timer();
	            		 TimerTask task = new TimerTask() {
							@Override
							public void run() {
								recordTime++;
								if(recordTime > 8){
									recordTime = 0;
									
									if (null != mTimer) {
										mTimer.cancel();
										mTimer.purge();
										mTimer = null;
									}
									
								}
							}
	            		 };
	            		 mTimer.schedule(task, 1000, 1000);
	            		 
	            	 }
	            	 
	             }
	             
	             ax_old = ax;
	             ay_old = ay;
	             az_old = az;
	         }

	         public void onAccuracyChanged(Sensor sensor, int accuracy) {
	        	 
	         }
	     };
		 mSensorManager.registerListener(mAccelerometerSensorEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * 取消注册加速度计监听
	 * @author xuhw
	 * @date 2014-11-5
	 */
	public void unregisterAccelerometerListener(){
		if(null != mSensorManager){
			if(null != mAccelerometerSensorEventListener){
				mSensorManager.unregisterListener(mAccelerometerSensorEventListener);
			}
		}
	}
	
	public interface AccelerometerListener{
		public void onChanged();
	}

}
