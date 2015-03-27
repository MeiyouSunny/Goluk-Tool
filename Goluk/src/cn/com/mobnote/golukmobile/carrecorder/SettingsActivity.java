package cn.com.mobnote.golukmobile.carrecorder;

import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingsActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		initView();
		setListener();
		
	}
	
	private void initView(){
		boolean zdxhlx = SettingUtils.getInstance().getBoolean("zdxhlx", true);//自动循环录像初始化
		boolean hd = SettingUtils.getInstance().getBoolean("hd", true);//高清初始化
		boolean sylz = SettingUtils.getInstance().getBoolean("sylz", true);//声音录制初始化
		boolean ztts = SettingUtils.getInstance().getBoolean("ztts", true);//状态提示灯初始化
		
		if(zdxhlx){
			findViewById(R.id.zdxhlx).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
		}else{
			findViewById(R.id.zdxhlx).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
		}
		
		
		if(hd){
			findViewById(R.id.mHighDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_high_press);
			findViewById(R.id.mStandardDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_low);
		}else{
			findViewById(R.id.mHighDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_high);
			findViewById(R.id.mStandardDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_low_press);
		}
		
		if(sylz){
			findViewById(R.id.sylz).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
		}else{
			findViewById(R.id.sylz).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
		}
		
		if(ztts){
			findViewById(R.id.ztts).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
		}else{
			findViewById(R.id.ztts).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
		}
	}
	
	private void setListener(){
		findViewById(R.id.back_btn).setOnClickListener(this);//返回按钮
		findViewById(R.id.mHighDefinition).setOnClickListener(this);//高清按钮
		findViewById(R.id.mStandardDefinition).setOnClickListener(this);//标清按钮
		findViewById(R.id.zdxhlx).setOnClickListener(this);//自动循环录像按钮
		findViewById(R.id.sylz).setOnClickListener(this);//声音录制
		findViewById(R.id.ztts).setOnClickListener(this);//状态提示灯
		findViewById(R.id.pzgylmd_line).setOnClickListener(this);//碰撞感应灵敏度
		
		findViewById(R.id.rlcx_line).setOnClickListener(this);//存储容量查询
		findViewById(R.id.sysz_line).setOnClickListener(this);//水印设置
		findViewById(R.id.sjsz_line).setOnClickListener(this);//时间设置
		findViewById(R.id.gshsdk_line).setOnClickListener(this);//格式化SDK卡
		findViewById(R.id.hfccsz_line).setOnClickListener(this);//恢复出厂设置
		findViewById(R.id.bbxx_line).setOnClickListener(this);//版本信息
		findViewById(R.id.ipcbd_line).setOnClickListener(this);//摄像头绑定
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
				finish();
				break;
			case R.id.mHighDefinition://高清
				findViewById(R.id.mHighDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_high_press);
				findViewById(R.id.mStandardDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_low);
				SettingUtils.getInstance().putBoolean("hd", true);//高清
				break;
			case R.id.mStandardDefinition://标清
				findViewById(R.id.mHighDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_high);
				findViewById(R.id.mStandardDefinition).setBackgroundResource(R.drawable.carrecorder_setup_option_low_press);
				SettingUtils.getInstance().putBoolean("hd", false);//标清
				break;
			case R.id.zdxhlx://自动循环录像
				boolean zdxhlx=SettingUtils.getInstance().getBoolean("zdxhlx");
				this.setButtonsBk(zdxhlx, R.id.zdxhlx, "zdxhlx");
				break;
			case R.id.sylz://声音录制
				boolean sylz=SettingUtils.getInstance().getBoolean("sylz");
				this.setButtonsBk(sylz, R.id.sylz, "sylz");
				break;
			case R.id.ztts://状态提示灯
				boolean ztts = SettingUtils.getInstance().getBoolean("ztts");
				this.setButtonsBk(ztts, R.id.ztts, "ztts");
				break;
			case R.id.pzgylmd_line://碰撞感应灵敏度
				break;
			case R.id.rlcx_line://容量查询
				break;
			case R.id.sysz_line://水印设置
				break;
			case R.id.sjsz_line://时间设置
				break;
			case R.id.gshsdk_line://格式化SDK卡
				break;
			case R.id.hfccsz_line://恢复出厂设置
				break;
			case R.id.bbxx_line://版本信息
				break;
			case R.id.ipcbd_line://摄像头绑定
				break;
		default:
			break;
		}
	}
	
	/**
	 * 设置按钮的背景图片并保存属性值
	  * @Title: setButtonsBk 
	  * @Description: TODO
	  * @param flog
	  * @param id
	  * @param btn void 
	  * @author 曾浩 
	  * @throws
	 */
	private void setButtonsBk(boolean flog,int id,String btn){
		if(flog){
			findViewById(id).setBackgroundResource(R.drawable.carrecorder_setup_option_off);
			SettingUtils.getInstance().putBoolean(btn, false);
		}else{
			findViewById(id).setBackgroundResource(R.drawable.carrecorder_setup_option_on);
			SettingUtils.getInstance().putBoolean(btn, true);
		}
	}

}
