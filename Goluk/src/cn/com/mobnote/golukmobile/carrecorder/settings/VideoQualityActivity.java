package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

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
  * 视频质量设置页面
  *
  * 2015年4月7日
  *
  * @author xuhw
  */
@SuppressLint("InflateParams")
public class VideoQualityActivity extends BaseActivity implements OnClickListener, IPCManagerFn{
	/** 视频类型文字显示 */
	private TextView mCloseText=null;
	private TextView mLowText=null;
	private TextView mMiddleText=null;
	private TextView mHighText=null;
	/** 视频类型选中高亮 */
	private ImageButton mCloseIcon=null;
	private ImageButton mLowIcon=null;
	private ImageButton mMiddleIcon=null;
	private ImageButton mHighIcon=null;
	/** 视频质量类型　1080高 1080低 720高 720低 */
	public  static enum SensitivityType{_1080h, _1080l, _720h, _720l};
	/** 保存选中视频类型 */
	private SensitivityType curType=SensitivityType._1080h;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_video_quality, null)); 
		setTitle("视频质量");
		
		initView();
		setListener();
		
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("videoquality", this);
		if(GolukApplication.getInstance().getIpcIsLogin()){
			boolean flag = GolukApplication.getInstance().getIPCControlManager().getVideoEncodeCfg(0);
			System.out.println("YYY============getVideoEncodeCfg=========flag="+flag);
			if(!flag){
				//获取失败默认显示1080P
				updateSensitivity(SensitivityType._1080h);
			}
		}
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView(){
		mCloseText = (TextView)findViewById(R.id.closeText);
		mLowText = (TextView)findViewById(R.id.lowText);
		mMiddleText = (TextView)findViewById(R.id.middleText);
		mHighText = (TextView)findViewById(R.id.highText);
		mCloseIcon = (ImageButton)findViewById(R.id.cRight);
		mLowIcon = (ImageButton)findViewById(R.id.dRight);
		mMiddleIcon = (ImageButton)findViewById(R.id.zRight);
		mHighIcon = (ImageButton)findViewById(R.id.gRight);
	}
	
	/**
	 * 设置控件监听事件
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	private void setListener(){
		findViewById(R.id.close).setOnClickListener(this);
		findViewById(R.id.low).setOnClickListener(this);
		findViewById(R.id.middle).setOnClickListener(this);
		findViewById(R.id.high).setOnClickListener(this);
	}
	
	/**
	 * 切换视频质量
	 * @param type 视频类型
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void updateSensitivity(SensitivityType type){
		curType = type;
		mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mCloseIcon.setVisibility(View.GONE);
		mLowIcon.setVisibility(View.GONE);
		mMiddleIcon.setVisibility(View.GONE);
		mHighIcon.setVisibility(View.GONE);
		
		if(SensitivityType._1080h == curType){
			mCloseIcon.setVisibility(View.VISIBLE);
			mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}else if(SensitivityType._1080l == curType){
			mLowIcon.setVisibility(View.VISIBLE);
			mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}else if(SensitivityType._720h == curType){
			mMiddleIcon.setVisibility(View.VISIBLE);
			mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}else{
			mHighIcon.setVisibility(View.VISIBLE);
			mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}
		
		if(GolukApplication.getInstance().getIpcIsLogin()){
			boolean flag = GolukApplication.getInstance().getIPCControlManager().setVideoEncodeCfg(curType);
			System.out.println("YYY==========curType=========flag="+flag);
		}
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.close:
				updateSensitivity(SensitivityType._1080h);
				break;
			case R.id.low:
				updateSensitivity(SensitivityType._1080l);
				break;
			case R.id.middle:
				updateSensitivity(SensitivityType._720h);
				break;
			case R.id.high:
				updateSensitivity(SensitivityType._720l);
				break;
	
			default:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("videoquality");
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			//获取IPC系统音视频编码配置
			if(msg == IPC_VDCP_Msg_GetVedioEncodeCfg){
				if(param1 == RESULE_SUCESS){
					System.out.println("YYY================1111==================param2="+param2);
					VideoConfigState mVideoConfigState = IpcDataParser.parseVideoConfigState((String)param2);
					if(null != mVideoConfigState){
						System.out.println("YYY================22222==============resolution====="+mVideoConfigState.resolution);
						if("1080P".equals(mVideoConfigState.resolution)){
							//bitrate数据返回的不对
							if(8192 == mVideoConfigState.bitrate){
								updateSensitivity(SensitivityType._1080h);
							}else{
								updateSensitivity(SensitivityType._1080l);
							}
						}else{
							if(6144 == mVideoConfigState.bitrate){
								updateSensitivity(SensitivityType._720h);
							}else{
								updateSensitivity(SensitivityType._720l);
							}
						}
					}else{
						System.out.println("YYY================33333==============");
					}
					
				}else{
					//获取失败默认显示1080P
					updateSensitivity(SensitivityType._1080h);
				}
			//设置IPC系统音视频编码配置
			}else if(msg == IPC_VDCP_Msg_SetVedioEncodeCfg){
				if(param1 == RESULE_SUCESS){
					if(null != CarRecorderActivity.mHandler){
						Message mm = CarRecorderActivity.mHandler.obtainMessage(CarRecorderActivity.UPDATEVIDEORESOLUTIONS);
						if(SensitivityType._1080h == curType || SensitivityType._1080l == curType){
							mm.arg1=1080;
						}else{
							mm.arg1=720;
						}
						CarRecorderActivity.mHandler.sendMessage(mm);
					}
					
					System.out.println("YYY==========IPC_VDCP_Msg_SetVedioEncodeCfg=====22222222222222222==RESULE_SUCESS===");
				}else{
					System.out.println("YYY==========IPC_VDCP_Msg_SetVedioEncodeCfg=====3333===fail==");
				}
			}
		}
	}
}
