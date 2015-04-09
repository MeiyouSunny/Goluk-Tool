package cn.com.mobnote.golukmobile.carrecorder.settings;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;

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
public class TimeSettingActivity extends BaseActivity implements OnClickListener{
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
	/** 保存自动同步时间开关状态 */
	private boolean systemtime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_time_setting, null)); 
		setTitle("时间设置");
		
		initView();
		getSystemTime();
		
		systemtime = SettingUtils.getInstance().getBoolean("systemtime", true);
		if(systemtime){
			mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
		}else{
			mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
		}
	}
	
	/**
	 * 获取当前系统时间
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void getSystemTime(){
	    Time t=new Time();
	    t.setToNow();
	    year = t.year;  
	    month = t.month + 1;  
	    day = t.monthDay;  
	    hour = t.hour;
	    minute = t.minute;  
	    
	    mDateText.setText(year + "-" + month + "-" + day);
	    mTimeText.setText(hour+":"+minute);
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView(){
		findViewById(R.id.mDateLayout).setOnClickListener(this);
		findViewById(R.id.mTimeLayout).setOnClickListener(this);
		mAutoBtn = (Button)findViewById(R.id.mAutoBtn);
		mDateText = (TextView)findViewById(R.id.mDateText);
		mTimeText = (TextView)findViewById(R.id.mTimeText);
		mAutoBtn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.mAutoBtn:
				if(systemtime){
					systemtime=false;
					mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
				}else{
					systemtime=true;
					mAutoBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
				}
				SettingUtils.getInstance().putBoolean("systemtime", systemtime);
				break;
			case R.id.mDateLayout:
				if(!systemtime){
					DatePickerDialog datePicker=new DatePickerDialog(TimeSettingActivity.this, new OnDateSetListener() {
						public void onDateSet(DatePicker view, int _year, int monthOfYear, int dayOfMonth) {
							year = _year;
							month = monthOfYear + 1;
							day = dayOfMonth;
							mDateText.setText(year + "-" + month + "-" + day);
						  }
					}, year, month - 1, day);
					datePicker.show();
				}
				
				break;
			case R.id.mTimeLayout:
				if(!systemtime){
					TimePickerDialog time=new TimePickerDialog(TimeSettingActivity.this, new OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int _minute) {
							hour = hourOfDay;
							minute = _minute;
							mTimeText.setText(hourOfDay+":"+minute);
						}
					}, hour, minute, true);
					time.show();
				}
				
				break;
	
			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
					System.out.println("YYY====str to time fail======22222222222222==");  
				}
			}
			
			
			System.out.println("YYY=============time=="+time);
			if(0 != time){
				boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(time);
				System.out.println("YYY============setIPCSystemTime===============a="+a);
			}
		}
		
	}
	
}
