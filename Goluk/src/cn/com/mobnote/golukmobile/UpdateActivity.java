package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.user.DataCleanManage;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 升级下载安装
 * @author mobnote
 *
 */
public class UpdateActivity extends BaseActivity implements OnClickListener{

	/**返回按钮**/
	private ImageButton mBtnBack = null;
	/**下载 / 安装按钮**/
	private Button mBtnDownload = null;
	/**极路客固件版本号**/
	private TextView mTextIpcVersion = null;
	/**极路客固件大小**/
	private TextView mTextIpcSize = null;
	/**更新说明**/
	private TextView mTextUpdateContent = null;
	/**未下载  /  下载中  /  已下载**/
	private TextView mTextDowload = null;
	/**GolukApplication**/
	private GolukApplication mApp = null;
	
	/**0 下载  1安装**/
	public final static String UPDATE_SIGN ="update_sign" ;
	/**数据展示**/
	public final static String UPDATE_DATA ="update_data" ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upgrade_layout);
		
		mApp = (GolukApplication)getApplication();
		
		initView();
		
		//ipc版本号
		String vIpc = mApp.mSharedPreUtil.getIPCVersion();
		mTextIpcVersion.setText(vIpc);
		//ipc文件大小
		String ipcSize = mApp.mSharedPreUtil.getIPCFileSize();
		String size = DataCleanManage.getFormatSize(Double.parseDouble(ipcSize));
		mTextIpcSize.setText(size);
		//ipc更新信息
		String ipcContent = mApp.mSharedPreUtil.getIPCContent();
		mTextUpdateContent.setText(ipcContent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	//初始化view
	public void initView(){
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mBtnDownload = (Button) findViewById(R.id.update_btn);
		mTextIpcVersion = (TextView) findViewById(R.id.upgrade_ipc_name);
		mTextIpcSize = (TextView) findViewById(R.id.upgrade_ipc_size_text);
		mTextUpdateContent = (TextView) findViewById(R.id.update_info_content);
		mTextDowload = (TextView) findViewById(R.id.upgrade_ipc_size_download);
		
		//监听
		mBtnBack.setOnClickListener(this);
		mBtnDownload.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.update_btn:
			//下载  /  升级
			break;
		default:
			break;
		}
	}
	
}
