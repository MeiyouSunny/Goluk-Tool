package cn.com.mobnote.golukmobile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 编辑签名
 * 
 * @author mobnote
 * 
 */
public class UserPersonalSignActivity extends BaseActivity implements OnClickListener {

	/** application **/
	private GolukApplication mApplication = null;
	/** itle **/
	private ImageButton btnBack;
	private TextView mTextTitle, mTextOk;
	/** body **/
	private EditText mEditBody;
	private String mSignText;
	private String mSignNewText;
	/** 最大字数限制 **/
	private TextView mTextCount = null;
	private TextView mTextCountAll = null;
	private static final int MAX_COUNT = 50;
	/** 输入的个性签名剩余字数 **/
	private int number = 0;

	// 保存数据的loading
	private CustomLoadingDialog mCustomProgressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_personal_edit_sign);

		// 获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		/**
		 * 获取从编辑界面传来的信息
		 * 
		 */

		if (savedInstanceState == null) {
			Intent it = getIntent();
			mSignText = it.getStringExtra("intentSignText");
		} else {
			mSignText = savedInstanceState.getString("intentSignText");
		}

		initView();
		// title
		mTextTitle.setText(R.string.user_personal_sign_edit);
		mTextOk.setText(R.string.user_personal_title_right);
		//
		int count = mEditBody.getText().toString().length();
		mTextCount.setText("" + (MAX_COUNT - count));
		mTextCountAll.setText(this.getResources().getString(R.string.str_slash) + MAX_COUNT
				+ this.getResources().getString(R.string.str_bracket_rigth));
	}

	// 初始化控件
	public void initView() {
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextOk = (TextView) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mEditBody = (EditText) findViewById(R.id.user_personal_edit_sign_body);
		mTextCount = (TextView) findViewById(R.id.number_count);
		mTextCountAll = (TextView) findViewById(R.id.number_count_all);
		mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.user_personal_saving));

		mEditBody.setText(mSignText);
		if (null != mSignText || "".equals(mSignText))
			mEditBody.setSelection(mSignText.length());

		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		mTextOk.setOnClickListener(this);
		mEditBody.addTextChangedListener(mTextWatcher);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mApplication.setContext(this, "UserPersonalSign");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putString("intentSignText", mSignText);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			finish();
			break;
		// que认
		case R.id.user_title_right:
			if (number < 0) {
				UserUtils.showDialog(this, this.getResources().getString(R.string.str_sign_limit));
			} else {
				String body = mEditBody.getText().toString();
				if (body.equalsIgnoreCase(mSignText)) {

					Intent it = new Intent(UserPersonalSignActivity.this, UserPersonalInfoActivity.class);
					it.putExtra("itSign", mSignNewText);
					this.setResult(2, it);
					this.finish();
				} else {
					saveSign(body);
				}
			}
			break;

		default:
			break;
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			int num = arg0.length();
			number = MAX_COUNT - num;
			if (number < 0) {
				mTextCount.setTextColor(Color.RED);
			} else {
				mTextCount.setTextColor(getResources().getInteger(R.color.setting_right_text_color));
			}
			mTextCount.setText("" + number);
		}
	};

	/**
	 * 修改用户个性签名
	 */
	private void saveSign(String sign) {

		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			// {desc：“个性签名”}
			mSignNewText = sign;
			boolean b;
			try {
				b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
						IPageNotifyFn.PageType_ModifySignature,
						JsonUtil.getUserSignJson(URLEncoder.encode(sign, "UTF-8")));
				if (b) {
					// 保存中
					mCustomProgressDialog.show();
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * 修改用户名回调
	 */

	public void saveSignCallBack(int success, Object obj) {
		GolukDebugUtils.e("", "---saveSignCallBack---" + success + "---" + obj);
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
		if (1 == success) {

			Intent it = new Intent(UserPersonalSignActivity.this, UserPersonalInfoActivity.class);
			it.putExtra("itSign", mSignNewText);
			this.setResult(RESULT_OK, it);
			this.finish();
		} else {
			GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
		}
	}
}
