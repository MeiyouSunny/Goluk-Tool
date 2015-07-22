package cn.com.mobnote.golukmobile.carrecorder.settings;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

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
  * 时间设置页面
  *
  * 2015年4月8日
  *
  * @author xuhw
  */
@SuppressLint("InflateParams")
public class TimeSettingActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn{
	/** 显示年月日 */
	private TextView mDateText=null;
	/** 显示当前时间 */
	private TextView mTimeText=null;
	/** 自动同步开关按钮 */
	private Button mAutoBtn=null;
	/** 年 */
	private int year;
	/** 月 */
	private int month;
	/** 日 */
	private int day;
	/** 时 */
	private int hour;
	/** 分 */
	private int minute;
	/** 秒 */
	private int seconds;
	/** 保存自动同步时间开关状态 */
	private boolean systemtime;
	/** 获取IPC系统时间标识 */
	private boolean getTimeing=false;
	private RelativeLayout mDateLayout = null;
	private TextView line = null;
	private float density = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_time_setting, null)); 
		setTitle("时间设置");
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("timesetting", this);
		density = SoundUtils.getInstance().getDisplayMetrics().density;
		
		initView();
		getSystemTime();
		
		systemtime = SettingUtils.getInstance().getBoolean("systemtime", true);
		if(systemtime){
			hideTimeLayout();
			mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
		}else{
			showTimeLayout();
			mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
		}
	}
	
	private void showTimeLayout() {
		mDateLayout.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
		lineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lineParams.setMargins((int)(15*density), 0, 0, 0);
		line.setLayoutParams(lineParams);
	}
	
	private void hideTimeLayout() {
		mDateLayout.setVisibility(View.GONE);
		RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
		lineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		line.setLayoutParams(lineParams);
	}
	
	/**
	 * 获取当前系统时间
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void getSystemTime(){
		Calendar calendar = Calendar.getInstance();  
		calendar.setTimeInMillis(System.currentTimeMillis());
		
	    year = calendar.get(Calendar.YEAR);  
	    month = calendar.get(Calendar.MONTH) + 1;
	    day = calendar.get(Calendar.DAY_OF_MONTH);
	    hour = calendar.get(Calendar.HOUR_OF_DAY);
	    minute = calendar.get(Calendar.MINUTE);
	    seconds = calendar.get(Calendar.SECOND);
	    
	    mDateText.setText(year + "年" + month + "月" + day + "日");
	    if(minute < 10){
	    	mTimeText.setText(hour+":0"+minute);
		}else{
			mTimeText.setText(hour+":"+minute);
		}
	    
	    
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
				GolukDebugUtils.e("xuhw", "YYY========getIPCSystemTime=======a="+a);
			}
		}).start();
		
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView(){
		findViewById(R.id.mDateText).setOnClickListener(this);
		findViewById(R.id.mTimeText).setOnClickListener(this);
		mAutoBtn = (Button)findViewById(R.id.mAutoBtn);
		mDateText = (TextView)findViewById(R.id.mDateText);
		mTimeText = (TextView)findViewById(R.id.mTimeText);
		mAutoBtn.setOnClickListener(this);
		mDateLayout = (RelativeLayout)findViewById(R.id.mDateLayout);
		line = (TextView)findViewById(R.id.line);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.back_btn:
				exit(); 
				break;
			case R.id.mAutoBtn:
				if(systemtime){
					systemtime = false;
					showTimeLayout();
					mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
				}else{
					systemtime = true;
					mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
					hideTimeLayout();
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							if(GolukApplication.getInstance().getIpcIsLogin()){
								long time  = System.currentTimeMillis()/1000;
								boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(time);
								GolukDebugUtils.e("xuhw", "YYY============setIPCSystemTime===============a="+a);
							}
						}
					}).start();
				}
				SettingUtils.getInstance().putBoolean("systemtime", systemtime);
				break;
			case R.id.mDateText:
				DatePickerDialog datePicker=new DatePickerDialog(TimeSettingActivity.this, new OnDateSetListener() {
					public void onDateSet(DatePicker view, int _year, int monthOfYear, int dayOfMonth) {
						year = _year;
						month = monthOfYear + 1;
						day = dayOfMonth;
						mDateText.setText(year + "年" + month + "月" + day + "日");
					  }
				}, year, month - 1, day);
				datePicker.show();
				
				break;
			case R.id.mTimeText:
				TimePickerDialog time=new TimePickerDialog(TimeSettingActivity.this, new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int _minute) {
						hour = hourOfDay;
						minute = _minute;
						if(minute < 10){
							mTimeText.setText(hourOfDay+":0"+minute);
						}else{
							mTimeText.setText(hourOfDay+":"+minute);
						}
					}
				}, hour, minute, true);
				time.show();
				
				break;
	
			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			//获取IPC系统时间
			if(msg == IPC_VDCP_Msg_GetTime){
				if(param1 == RESULE_SUCESS){
					GolukDebugUtils.e("xuhw", "YYY=====IPC_VDCP_Msg_GetTime=====param2="+param2);
					getTimeing=true;
					long time = IpcDataParser.parseIPCTime((String)param2)*1000;
					Calendar calendar = Calendar.getInstance();  
					calendar.setTimeInMillis(time);
					
				    year = calendar.get(Calendar.YEAR);  
				    month = calendar.get(Calendar.MONTH) + 1;
				    day = calendar.get(Calendar.DAY_OF_MONTH);
				    hour = calendar.get(Calendar.HOUR_OF_DAY);
				    minute = calendar.get(Calendar.MINUTE);
				    seconds = calendar.get(Calendar.SECOND);
				    
				    mDateText.setText(year + "年" + month + "月" + day + "日");
				    if(minute < 10){
				    	mTimeText.setText(hour+":0"+minute);
					}else{
						mTimeText.setText(hour+":"+minute);
					}
				}
			}else if(msg == IPC_VDCP_Msg_SetTime){
				GolukDebugUtils.e("xuhw", "YYY========IPC_VDCP_Msg_SetTime=======param1="+param1+"==param2="+param2);
				if(param1 == RESULE_SUCESS){
					new Thread(new Runnable() {
						@Override
						public void run() {
							boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
							GolukDebugUtils.e("xuhw", "YYY========getIPCSystemTime=======a="+a);
						}
					}).start();
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "timesetting");
	}
	
	public void exit(){
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("timesetting");
		}
		
		if(GolukApplication.getInstance().getIpcIsLogin()){
			long time = 0;
			if(systemtime){
				time = System.currentTimeMillis()/1000;
			}else{
				SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
				 Date date;
				try {
					 Time t=new Time();
					 t.setToNow();
					 int sec = t.second;
					String timestr = year + "-"+ month + "-" + day + " " + hour + ":" + minute + ":"+sec;
					date = sdf.parse(timestr);
					time = date.getTime()/1000;
				} catch (ParseException e) {
					GolukDebugUtils.e("xuhw", "YYY====str to time fail======22222222222222==");  
				}
			}
			
			
			GolukDebugUtils.e("xuhw", "YYY=============time=="+time);
			if(0 != time){
			
				final long times = time;
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(GolukApplication.getInstance().getIpcIsLogin()){
							boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(times);
							GolukDebugUtils.e("xuhw", "YYY============setIPCSystemTime===============a="+a);
						}
					}
				}).start();
				
			}
		}
		
		finish();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		exit(); 
        	return true;
        }else
        	return super.onKeyDown(keyCode, event); 
	}
	
}
