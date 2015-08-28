package cn.com.mobnote.golukmobile;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.opinion.OpinionDialog;
import cn.com.mobnote.golukmobile.opinion.OpinionDialog.OpinionDialogFn;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 意见反馈
 * 
 * @author mobnote
 *
 */
public class UserOpinionActivity extends BaseActivity implements OnClickListener, OpinionDialogFn {

	/****/
	private GolukApplication mApp = null;
	private Context mContext = null;
	/** title **/
	private ImageButton mBtnBack = null;
	private TextView mTextTitle = null;
	private TextView mTextRight = null;
	/** 意见/建议字数限制 **/
	private TextView mTextSuggestCount = null;
	/** 意见/建议EditText **/
	private EditText mEditSuggest = null;
	/** 联系方式字数限制 **/
	private TextView mTextConnectionCount = null;
	/** 联系方式EditText **/
	private EditText mEditConnection = null;
	/** 选择意见类型 **/
	private Button mBtnSelect = null;
	private LinearLayout mSelectLayout = null;
	/** 意见/建议最大字数限制 **/
	private static final int MAX_SUGGEST_COUNT = 500;
	/** 联系方式最大字数限制 **/
	private static final int MAX_CONNECTION_COUNT = 70;
	/** 发送请求参数 **/
	private String sys_version = "";
	private String app_version = "";
	private String ipc_version = "";
	private String phone_models = "";
	private String userOpinion = "";
	private String userContact = "";
	/** loading **/
	private CustomLoadingDialog mLoadingDialog = null;
	/** 选择意见反馈类型的Dialog **/
	private OpinionDialog mOpinionDialog = null;

	private String selectType = "5";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_opinion_layout);

		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();

		initView();
		// 初始化字数限制
		int count_suggest = mEditSuggest.getText().toString().length();
		mTextSuggestCount.setText("（" + (MAX_SUGGEST_COUNT - count_suggest) + "/" + MAX_SUGGEST_COUNT + "）");
		int count_connection = mEditConnection.getText().toString().length();
		mTextConnectionCount
				.setText("（" + (MAX_CONNECTION_COUNT - count_connection) + "/" + MAX_CONNECTION_COUNT + "）");

		// 获取请求参数
		sys_version = GolukUtils.getSystem_version();
		app_version = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVersion, "fs6:/version");
		ipc_version = mApp.mSharedPreUtil.getIPCVersion();
		phone_models = GolukUtils.getPhone_models();

		mOpinionDialog = new OpinionDialog(mContext, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserOpinion");
	}

	// 初始化
	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mTextRight = (TextView) findViewById(R.id.user_title_right);
		mTextSuggestCount = (TextView) findViewById(R.id.opinion_layout_suggest_count);
		mEditSuggest = (EditText) findViewById(R.id.opinion_layout_suggest_edit);
		mTextConnectionCount = (TextView) findViewById(R.id.opinion_layout_connection_count);
		mEditConnection = (EditText) findViewById(R.id.opinion_layout_connection_edit);
		mBtnSelect = (Button) findViewById(R.id.user_opinion_select_btn);
		mSelectLayout = (LinearLayout) findViewById(R.id.user_opinion_select_layout);

		mTextTitle.setText("意见反馈");
		mTextRight.setText("发送");
		// 初始化对话框
		if (null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(mContext, "发送中");
		}
		// 监听
		mBtnSelect.setOnClickListener(this);
		mSelectLayout.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);
		mTextRight.setOnClickListener(this);
		mEditSuggest.addTextChangedListener(mTextWatcher1);
		mEditConnection.addTextChangedListener(mTextWatcher2);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 返回
		case R.id.back_btn:
			this.finish();
			break;
		// 发送
		case R.id.user_title_right:
			request();
			break;
		// 选择意见类型
		case R.id.user_opinion_select_layout:
		case R.id.user_opinion_select_btn:
			mOpinionDialog.show();
			break;
		default:
			break;
		}
	}

	public void request() {
		userOpinion = mEditSuggest.getText().toString();
		userContact = mEditConnection.getText().toString();
		UserUtils.hideSoftMethod(this);
		if ("".equals(userOpinion)) {
			UserUtils.showDialog(mContext, this.getResources().getString(R.string.opinion_content_null));
		} else {
			boolean b = requestOpinion("android", sys_version, app_version, ipc_version, phone_models, userOpinion,
					userContact, selectType);
			if (b) {
				mLoadingDialog.show();
				mBtnBack.setEnabled(false);
				mTextRight.setEnabled(false);
				mEditSuggest.setEnabled(false);
				mEditConnection.setEnabled(false);
			} else {
				GolukUtils.showToast(mContext, "反馈失败，请稍候重试");
			}
		}
	}

	/**
	 * 发送请求
	 * 
	 * @param tag
	 * @param sys_version
	 * @param app_version
	 * @param ipc_version
	 * @param phone_models
	 * @param opinion
	 * @param contact
	 * @return
	 */
	public boolean requestOpinion(String tag, String sys_version, String app_version, String ipc_version,
			String phone_models, String opinion, String contact, String type) {
		if (!UserUtils.isNetDeviceAvailable(mApp.getContext())) {
			UserUtils.showDialog(mContext, this.getResources().getString(R.string.opinion_fail));
			return false;
		} else {
			String newOpinion = "";
			String newContact = "";
			try {
				newOpinion = URLEncoder.encode(opinion, "utf-8");
				newContact = URLEncoder.encode(contact, "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			String opinionJson = JsonUtil.putOpinion(tag, sys_version, app_version, ipc_version, phone_models,
					newOpinion, newContact, type);
			GolukDebugUtils.i("lily", "-----------opinionJson------" + opinionJson);
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_FeedBack, opinionJson);
			GolukDebugUtils.e("", "-----------UserOpinionActivity-----------b：" + b);
			return b;
		}
	}

	public void requestOpinionCallback(int success, Object outTime, Object obj) {
		GolukDebugUtils.i("lily", "------------success=====-" + success);
		closeDialog();
		int codeOut = (Integer) outTime;
		if (1 == success) {
			try {
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i("lily", "------requestOpinionCallback--------------json-------" + json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);
				String result = jsonData.getString("result");
				if ("0".equals(result)) {
					new AlertDialog.Builder(mContext)
							.setTitle("感谢")
							.setMessage(this.getResources().getString(R.string.opinion_success))
							.setOnKeyListener(new DialogInterface.OnKeyListener() {
								
								@Override
								public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
									return true;
								}
							})
							.setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											finish();
										}
									}).create().show();
				} else {
					UserUtils.showDialog(mContext, this.getResources().getString(R.string.opinion_fail));
				}

			} catch (JSONException e) {
				e.printStackTrace();
				UserUtils.showDialog(mContext, this.getResources().getString(R.string.opinion_fail));
			}
		} else {
			switch (codeOut) {
			case 1:// 没有网络
			case 2:// 服务端错误
			case 3:// 网络链接超时
			default:
				UserUtils.showDialog(mContext, this.getResources().getString(R.string.opinion_fail));
				break;
			}
		}
	}

	/**
	 * 关闭对话框
	 */
	private void closeDialog() {
		if (null != mLoadingDialog) {
			mLoadingDialog.close();
			mBtnBack.setEnabled(true);
			mTextRight.setEnabled(true);
			mEditSuggest.setEnabled(true);
			mEditConnection.setEnabled(true);
		}
	}

	TextWatcher mTextWatcher1 = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			int num = arg0.length();
			int number = MAX_SUGGEST_COUNT - num;
			if (number < 0) {
				number = 0;
			}
			mTextSuggestCount.setText("（" + number + "/" + MAX_SUGGEST_COUNT + "）");
		}
	};

	TextWatcher mTextWatcher2 = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			int num = arg0.length();
			int number = MAX_CONNECTION_COUNT - num;
			if (number < 0) {
				number = 0;
			}
			mTextConnectionCount.setText("（" + number + "/" + MAX_CONNECTION_COUNT + "）");
		}
	};

	@Override
	public void showOpinionDialog(int type) {
		if (type == OpinionDialogFn.TYPE_FIRST) {
			mBtnSelect.setText(mContext.getResources().getString(R.string.user_opinion_type_yingjiang));
			selectType = OpinionDialogFn.TYPE_FIRST + "";
			GolukDebugUtils.e("", "----------------UserOpinionActivity----------selectType：" + selectType);
		} else if (type == OpinionDialogFn.TYPE_SECOND) {
			mBtnSelect.setText(mContext.getResources().getString(R.string.user_opinion_type_anzhuang));
			selectType = OpinionDialogFn.TYPE_SECOND + "";
		} else if (type == OpinionDialogFn.TYPE_THIRD) {
			mBtnSelect.setText(mContext.getResources().getString(R.string.user_opinion_type_tuxiang));
			selectType = OpinionDialogFn.TYPE_THIRD + "";
		} else if (type == OpinionDialogFn.TYPE_FOUR) {
			mBtnSelect.setText(mContext.getResources().getString(R.string.user_opinion_type_shouji));
			selectType = OpinionDialogFn.TYPE_FOUR + "";
		} else if (type == OpinionDialogFn.TYPE_FIVE) {
			mBtnSelect.setText(mContext.getResources().getString(R.string.user_opinion_type_qita));
			selectType = OpinionDialogFn.TYPE_FIVE + "";
		} else {
			selectType = OpinionDialogFn.TYPE_FIVE + "";
		}
		GolukDebugUtils.e("", "----------------UserOpinionActivity----------selectType222：" + selectType);
	}

}
