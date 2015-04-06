package cn.com.mobnote.golukmobile.carrecorder.settings;


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
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;

public class TimeSettingActivity extends BaseActivity implements OnClickListener{
	private TextView mDateText=null;
	private TextView mTimeText=null;
	
	private Button mAutoBtn=null;
	
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_time_setting, null)); 
		setTitle("时间设置");
		
		initView();
		getSystemTime();
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
				
				break;
			case R.id.mDateLayout:
				DatePickerDialog datePicker=new DatePickerDialog(TimeSettingActivity.this, new OnDateSetListener() {
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						month = monthOfYear + 1;
						day = dayOfMonth;
						mDateText.setText(year + "-" + month + "-" + day);
					  }
				}, year, month - 1, day);
				datePicker.show();
				break;
			case R.id.mTimeLayout:
				TimePickerDialog time=new TimePickerDialog(TimeSettingActivity.this, new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int _minute) {
						hour = hourOfDay;
						minute = _minute;
						mTimeText.setText(hourOfDay+":"+minute);
					}
				}, hour, minute, true);
				time.show();
				break;
	
			default:
				break;
		}
	}

}
