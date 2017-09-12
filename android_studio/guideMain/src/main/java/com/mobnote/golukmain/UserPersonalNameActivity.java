package com.mobnote.golukmain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.userlogin.UpNameResult;
import com.mobnote.golukmain.userlogin.UpdUserNameBeanRequest;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 编辑昵称
 * 
 * @author mobnote
 * 
 */
public class UserPersonalNameActivity extends BaseActivity implements OnClickListener,IRequestResultListener {

	/** application **/
	private GolukApplication mApplication = null;
	/** title **/
	private ImageButton btnBack;
	private TextView mTextTitle, mTextOk;
	/** body **/
	private EditText mEditName;
	private ImageView mImageNameRight;
	private String mNameText;
	private String mNameNewText;
	/** 文字字数提示 **/
	private TextView mTextCount = null;
	private TextView mTextCountAll = null;
	/** 最大输入字数 **/
	private static final int MAX_COUNT = 10;
	/** 输入超出的字数 **/
	private int number = 0;

	// 保存数据的loading
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	private UpdUserNameBeanRequest mUpUserNameBeanRequest = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_personal_edit_name);
		// 获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		/**
		 * 获取从编辑界面传来的姓名
		 */

		if (savedInstanceState == null) {
			Intent it = getIntent();
			mNameText = it.getStringExtra("intentNameText");
			GolukDebugUtils.i("lily", "----------UserPersonalNameActivity---------name=====" + mNameText);
		} else {
			mNameText = savedInstanceState.getString("intentNameText");
		}
		initView();
		// title
		mTextTitle.setText(R.string.user_personal_name_edit);
		mTextOk.setText(R.string.user_personal_title_right);
		//
		int count = mEditName.getText().toString().length();
		mTextCount.setText("" + (MAX_COUNT - count));
		mTextCountAll.setText(this.getResources().getString(R.string.str_slash) + MAX_COUNT
				+ this.getResources().getString(R.string.str_bracket_rigth));
		

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mApplication.setContext(this, "UserPersonalName");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putString("intentNameText", mNameText);
		super.onSaveInstanceState(outState);
	}

	// 初始化控件
	public void initView() {
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextOk = (TextView) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mEditName = (EditText) findViewById(R.id.user_personal_name_edit);
		mImageNameRight = (ImageView) findViewById(R.id.user_personal_name_image);
		mTextCount = (TextView) findViewById(R.id.number_count);
		mTextCountAll = (TextView) findViewById(R.id.number_count_all);
		mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.user_personal_saving));

		mEditName.setText(mNameText);
		mEditName.setSelection(mNameText.length());

		mEditName.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					GolukDebugUtils.e("lily", "----------------KEYCODE_ENTER-------");
					mImageNameRight.setVisibility(View.GONE);
				} else {
					mImageNameRight.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		mTextOk.setOnClickListener(this);
		mImageNameRight.setOnClickListener(this);
		mEditName.addTextChangedListener(mTextWatcher);
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.back_btn) {
			UserUtils.hideSoftMethod(this);
			finish();
		} else if (id == R.id.user_title_right) {
			if (number < 0) {
				UserUtils.showDialog(this, this.getResources().getString(R.string.str_user_name_limit));
			} else {
				String name = mEditName.getText().toString().trim();
				GolukDebugUtils.i("lily", "------UserPersonalNameActivity--修改昵称------" + name);
				if (name.isEmpty()) {
					UserUtils.showDialog(this, this.getResources().getString(R.string.str_user_name_warn));
				} else {
					UserUtils.hideSoftMethod(this);
					if (!name.equalsIgnoreCase(mNameText)) {
						saveName(name);
					} else {

						Intent it = new Intent(UserPersonalNameActivity.this, UserPersonalInfoActivity.class);
						it.putExtra("itName", name);
						this.setResult(1, it);
						this.finish();
					}
				}
			}
		} else if (id == R.id.user_personal_name_image) {
			// 点击清空
			mEditName.setText("");
		} else {
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			String name = mEditName.getText().toString();
			if ("".equals(name) || null == name) {
				mImageNameRight.setVisibility(View.GONE);
			} else {
				mImageNameRight.setVisibility(View.VISIBLE);
			}
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
				mTextCount.setTextColor(getResources().getColor(R.color.setting_right_text_color));
			}
			mTextCount.setText(number + "");
		}
	};

	/**
	 * 修改用户名称
	 */
	private void saveName(String name) {

		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			// {NickName：“昵称”}
			mNameNewText = name;
			
			try {
				mUpUserNameBeanRequest = new UpdUserNameBeanRequest(IPageNotifyFn.PageType_ModifyNickName, this);
				mUpUserNameBeanRequest.get(mApplication.getMyInfo().uid, mApplication.getMyInfo().phone, URLEncoder.encode(name, "UTF-8"));
				// 保存中
				mCustomProgressDialog.show();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void startUserLogin(){
		Intent loginIntent = null;
		if(GolukApplication.getInstance().isMainland() == false){
			loginIntent = new Intent(this, InternationUserLoginActivity.class);
		}else{
			loginIntent = new Intent(this, UserLoginActivity.class);
		}
		startActivity(loginIntent);
	}

	/**
	 * 修改用户名回调
	 */

	public void saveNameCallBack(int success, Object obj) {
		GolukDebugUtils.e("", "修改用户名称回调---saveNameCallBack---" + success + "---" + obj);
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
		if (1 == success) {
			mApplication.setMyinfo(mNameNewText, "", "",null);
			Intent it = new Intent(UserPersonalNameActivity.this, UserPersonalInfoActivity.class);
			it.putExtra("itName", mNameNewText);
			this.setResult(RESULT_OK, it);
			this.finish();
		} else {
			GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {

		if(requestType == IPageNotifyFn.PageType_ModifyNickName){
			UpNameResult upnameresult = (UpNameResult) result;
			
			if (mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.close();
			}
			if(upnameresult ==null || !upnameresult.success){
				GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
				return;
			}
			if(upnameresult.data != null){
				if (!GolukUtils.isTokenValid(upnameresult.data.result)){
					startUserLogin();
					return;
				}
			}
			if (upnameresult.success) {
				mApplication.setMyinfo(mNameNewText, "", "",null);
				Intent it = new Intent(UserPersonalNameActivity.this, UserPersonalInfoActivity.class);
				it.putExtra("itName", mNameNewText);
				this.setResult(RESULT_OK, it);
				this.finish();
			}
		}
	}
}
