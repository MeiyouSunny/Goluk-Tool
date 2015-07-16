package cn.com.mobnote.golukmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.IpcUpdateManage;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 版本信息
 * 
 * @author mobnote
 *
 */
public class UserVersionActivity extends BaseActivity implements OnClickListener {

	/****/
	private GolukApplication mApp = null;
	private Context mContext = null;
	/** 返回按钮 **/
	private ImageButton mBtnBack = null;
	/** title文字 **/
	private TextView mTextTitle = null;
	/** APP版本号 **/
	private TextView mTextVersion = null;
	/** 版本检测 **/
	private RelativeLayout mAppUpdateLayout = null;
	/** 欢迎页 **/
	private RelativeLayout mWelcomeLayout = null;
	/** 意见反馈 **/
	private RelativeLayout mOpinionLayout = null;
	/**固件版本**/
	private String vIpc = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_version_layout);

		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();

		initView();
		mApp.initSharedPreUtil(this);
		vIpc = mApp.mSharedPreUtil.getIPCVersion();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserVersion");
		// 调用同步接口，在设置页显示APP版本号
		String verName = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVersion, "fs6:/version");
		GolukDebugUtils.i("lily", "=======+version+=====" + verName);
		mTextVersion.setText(verName);

	}

	// 初始化
	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mTextVersion = (TextView) findViewById(R.id.user_version_text);
		mAppUpdateLayout = (RelativeLayout) findViewById(R.id.app_update_item);
		mWelcomeLayout = (RelativeLayout) findViewById(R.id.welcome_item);
		mOpinionLayout = (RelativeLayout) findViewById(R.id.opinion_item);

		mTextTitle.setText("版本信息");
		// 监听
		mBtnBack.setOnClickListener(this);
		mAppUpdateLayout.setOnClickListener(this);
		mWelcomeLayout.setOnClickListener(this);
		mOpinionLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		//返回
		case R.id.back_btn:
			this.finish();
			break;
		//版本检测
		case R.id.app_update_item:
			mApp.mUser.setUserInterface(null);
			// 点击设置页中版本检测无最新版本提示标识
			GolukDebugUtils.i("lily", vIpc + "========UserSetupActivity==点击版本检测===中ipcVersion=====");
			boolean appB = mApp.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_APP, vIpc);
			break;
		//欢迎页
		case R.id.welcome_item:
			Intent itWelcome = new Intent(UserVersionActivity.this,UserWelcomeActivity.class);
			startActivity(itWelcome);
			break;
		//意见反馈
		case R.id.opinion_item:
			Intent itOpinion = new Intent(UserVersionActivity.this,UserOpinionActivity.class);
			startActivity(itOpinion);
			break;

		default:
			break;
		}
	}

}