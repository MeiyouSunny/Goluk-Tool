package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.RecordStorgeState;
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
  * 容量查询
  *
  * 2015年4月7日
  *
  * @author xuhw
  */
public class StorageCpacityQueryActivity extends BaseActivity implements IPCManagerFn{
	/**  SD卡总容量  */
	private TextView mTotalSize=null;
	/**  已用容量  */
	private TextView mUsedSize=null;
	/**  循环视频可用容量  */
	private TextView mCycleSize=null;
	/**  精彩视频可用容量  */
	private TextView mWonderfulSize=null;
	/**  紧急视频可用容量  */
	private TextView mEmergencySize=null;
	/**  其它可用容量  */
	private TextView mOtherSize=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("storage", this);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_storage_cpacity_query, null)); 
		setTitle("容量查询");
		
		initView();
		boolean flag = GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
		System.out.println("YYY===========flag="+flag);
		if(!flag){
			
		}
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	private void initView(){
		mTotalSize = (TextView)findViewById(R.id.mTotalSize);
		mUsedSize = (TextView)findViewById(R.id.mUsedSize);
		mCycleSize = (TextView)findViewById(R.id.mCycleSize);
		mWonderfulSize = (TextView)findViewById(R.id.mWonderfulSize);
		mEmergencySize = (TextView)findViewById(R.id.mEmergencySize);
		mOtherSize = (TextView)findViewById(R.id.mOtherSize);
		
		
		mTotalSize.setText("0GB");
		mUsedSize.setText("0MB");
		mCycleSize.setText("0GB");
		mWonderfulSize.setText("0MB");
		mEmergencySize.setText("0MB");
		mOtherSize.setText("0MB");
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("storage");
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if(msg == IPC_VDCP_Msg_RecPicUsage){
				System.out.println("YYY===========11111111=============param2=="+param2);
				if(param1 == RESULE_SUCESS){
					RecordStorgeState mRecordStorgeState = IpcDataParser.parseRecordStorageStatus((String)param2);
					
					double totalsize = mRecordStorgeState.totalSdSize/1024;
					double usedsize = mRecordStorgeState.totalSdSize - mRecordStorgeState.leftSize;
					double cyclesize = mRecordStorgeState.normalRecQuota - mRecordStorgeState.normalRecSize;
					double wonderfulsize = mRecordStorgeState.wonderfulRecQuota - mRecordStorgeState.wonderfulRecSize;
					double emergencysize = mRecordStorgeState.urgentRecQuota - mRecordStorgeState.urgentRecSize;
					double picsize = mRecordStorgeState.picQuota - mRecordStorgeState.picSize;
					
					java.text.DecimalFormat   df=new   java.text.DecimalFormat("#.##");  
					String totalsizestr = df.format(totalsize);
					
					mTotalSize.setText(totalsizestr + "GB");
					mUsedSize.setText(usedsize + "MB");
					mCycleSize.setText(cyclesize + "MB");
					mWonderfulSize.setText(wonderfulsize + "MB");
					mEmergencySize.setText(emergencysize + "MB");
					mOtherSize.setText(picsize + "MB");
					
					
					System.out.println("YYY===========２２２２２=========normalRecQuota="+mRecordStorgeState.normalRecQuota+"=====normalRecSize="+mRecordStorgeState.normalRecSize);
				}else{
					System.out.println("YYY===========３３３３３===============");
				}
			}
		}
	}

}
