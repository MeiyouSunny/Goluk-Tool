package cn.com.mobnote.golukmobile.carrecorder.settings;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

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
  * IPC设置界面
  *
  * 2015年4月6日
  *
  * @author xuhw
  */
public class SettingsActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		initView();
		setListener();
		
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView(){
		boolean zdxhlx = SettingUtils.getInstance().getBoolean("zdxhlx", true);//自动循环录像初始化
		boolean sylz = SettingUtils.getInstance().getBoolean("sylz", true);//声音录制初始化
		boolean ztts = SettingUtils.getInstance().getBoolean("ztts", true);//状态提示灯初始化
		
		if(zdxhlx){
			findViewById(R.id.zdxhlx).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
		}else{
			findViewById(R.id.zdxhlx).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
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
	
	/**
	 * 设置监听
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void setListener(){
		findViewById(R.id.back_btn).setOnClickListener(this);//返回按钮
		findViewById(R.id.mVideoDefinition).setOnClickListener(this);//视频质量
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
			case R.id.mVideoDefinition://视频质量
				Intent mVideoDefinition = new Intent(SettingsActivity.this, VideoQualityActivity.class);
				startActivity(mVideoDefinition);
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
				Intent pzgylmd_line = new Intent(SettingsActivity.this, ImpactSensitivityActivity.class);
				startActivity(pzgylmd_line);
				break;
			case R.id.rlcx_line://容量查询
				Intent rlcx_line = new Intent(SettingsActivity.this, StorageCpacityQueryActivity.class);
				startActivity(rlcx_line);
				break;
			case R.id.sysz_line://水印设置
				Intent sysz_line = new Intent(SettingsActivity.this, WatermarkSettingActivity.class);
				startActivity(sysz_line);
				break;
			case R.id.sjsz_line://时间设置
				Intent sjsz_line = new Intent(SettingsActivity.this, TimeSettingActivity.class);
				startActivity(sjsz_line);
				break;
			case R.id.gshsdk_line://格式化SDK卡
				Intent gshsdk_line = new Intent(SettingsActivity.this, FormatSDCardActivity.class);
				startActivity(gshsdk_line);
				break;
			case R.id.hfccsz_line://恢复出厂设置
				Intent hfccsz_line = new Intent(SettingsActivity.this, RestoreFactorySettingsActivity.class);
				startActivity(hfccsz_line);
				break;
			case R.id.bbxx_line://版本信息
				Intent bbxx = new Intent(SettingsActivity.this, VersionActivity.class);
				startActivity(bbxx);
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
