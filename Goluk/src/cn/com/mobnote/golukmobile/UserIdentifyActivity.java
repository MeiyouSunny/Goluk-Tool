package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 获取验证码
 * 
 * @author mobnote
 *
 */
public class UserIdentifyActivity extends BaseActivity implements OnClickListener, OnTouchListener {

	public static final String IDENTIFY_DIFFERENT = "identify_different";
	public static final String IDENTIFY_PHONE = "identify_phone";
	private static final String TAG = "lily";
	/** Application & Context **/
	private GolukApplication mApp = null;
	private Context mContext = null;
	/** title **/
	private ImageButton mBtnBack = null;
	private TextView mTextTitle = null;
	/** body **/
	private ImageView mImageAnim = null;
	private EditText mEditTextOne, mEditTextTwo, mEditTextThree, mEditTextFour, mEditTextFive, mEditTextSix;
	private LinearLayout mLayoutCount = null;
	private Button mBtnNext = null;
	/** 动画 **/
	private AnimationDrawable mAnimation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_identify_layout);

		mContext = this;
		mApp = (GolukApplication) getApplication();

		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserIdentify");
		getInfo();
	}

	/**
	 * 初始化view
	 */
	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mImageAnim = (ImageView) findViewById(R.id.user_identify_anim_image);
		mEditTextOne = (EditText) findViewById(R.id.user_identify_item_one);
		mEditTextTwo = (EditText) findViewById(R.id.user_identify_item_two);
		mEditTextThree = (EditText) findViewById(R.id.user_identify_item_three);
		mEditTextFour = (EditText) findViewById(R.id.user_identify_item_four);
		mEditTextFive = (EditText) findViewById(R.id.user_identify_item_five);
		mEditTextSix = (EditText) findViewById(R.id.user_identify_item_six);
		mLayoutCount = (LinearLayout) findViewById(R.id.user_identify_layout_count);
		mBtnNext = (Button) findViewById(R.id.user_identify_btn);

		mAnimation = (AnimationDrawable) mImageAnim.getBackground();

		if (null != mAnimation) {
			mAnimation.start();
		}

		// 监听
		mBtnBack.setOnClickListener(this);
		mLayoutCount.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);

		mEditTextOne.addTextChangedListener(mTextWatcher);
		mEditTextTwo.addTextChangedListener(mTextWatcher);
		mEditTextThree.addTextChangedListener(mTextWatcher);
		mEditTextFour.addTextChangedListener(mTextWatcher);
		mEditTextFive.addTextChangedListener(mTextWatcher);
		mEditTextSix.addTextChangedListener(mTextWatcher);
	}

	/**
	 * 获取信息
	 */
	public void getInfo() {
		Intent it = getIntent();
		if (null != it.getStringExtra(IDENTIFY_PHONE)) {
			String title_phone = it.getStringExtra(IDENTIFY_PHONE).toString();
			mTextTitle.setText("正在发送短信到" + title_phone);
		}

		if (null != it.getStringExtra(IDENTIFY_DIFFERENT)) {
			String identify_different = it.getStringExtra(IDENTIFY_DIFFERENT).toString();
			if ("user_regist".equals(identify_different)) {
				// TODO 注册逻辑
			} else if ("user_repwd".equals(identify_different)) {
				// TODO 重置密码逻辑
			}

		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.user_identify_layout_count:

			break;
		case R.id.user_identify_btn:

			break;
		default:
			break;
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			if (arg0.length() == 1) {
				if (mEditTextOne.isFocusable()) {
					mEditTextTwo.setFocusable(true);
					mEditTextTwo.setFocusableInTouchMode(true);
				} else if (mEditTextTwo.isFocusable()) {
					mEditTextThree.setFocusable(true);
					mEditTextThree.setFocusableInTouchMode(true);
				} else if (mEditTextThree.isFocusable()) {
					mEditTextFour.setFocusable(true);
					mEditTextFour.setFocusableInTouchMode(true);
				} else if (mEditTextFour.isFocusable()) {
					mEditTextFive.setFocusable(true);
					mEditTextFive.setFocusableInTouchMode(true);
				} else if (mEditTextFive.isFocusable()) {
					mEditTextSix.setFocusable(true);
					mEditTextSix.setFocusableInTouchMode(true);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable mEditable) {

			if (mEditable.toString().length() == 1) {
				if (mEditTextOne.isFocused()) {
					mEditTextOne.setFocusable(false);
					mEditTextTwo.requestFocus();
				} else if (mEditTextTwo.isFocused()) {
					mEditTextTwo.setFocusable(false);
					mEditTextThree.requestFocus();
				} else if (mEditTextThree.isFocused()) {
					mEditTextThree.setFocusable(false);
					mEditTextFour.requestFocus();
				} else if (mEditTextFour.isFocused()) {
					mEditTextFour.setFocusable(false);
					mEditTextFive.requestFocus();
				} else if (mEditTextFive.isFocused()) {
					mEditTextFive.setFocusable(false);
					mEditTextSix.requestFocus();
				} else if (mEditTextSix.isFocused()) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEditTextSix.getWindowToken(), 0);
					mBtnNext.setBackgroundResource(R.drawable.icon_login);
					mBtnNext.setEnabled(true);
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (mEditTextSix.isFocused()) {
				mEditTextFive.setFocusableInTouchMode(true);
				String str = mEditTextSix.getText().toString();
				int length = str.length();
				if (mEditTextSix.getText().toString().length() == 1) {
					mEditTextSix.getText().clear();
					mEditTextSix.requestFocus();
					return true;
				} else {
					mEditTextSix.clearFocus();
					mEditTextFive.getText().clear();
					mEditTextSix.setFocusable(false);
					mEditTextFive.requestFocus();
				}
			} else if (mEditTextFive.isFocused()) {
				mEditTextFive.clearFocus();
				mEditTextFive.setFocusable(false);
				mEditTextFour.setFocusableInTouchMode(true);
				mEditTextFour.getText().clear();
				mEditTextFour.requestFocus();
			} else if (mEditTextFour.isFocused()) {
				mEditTextFour.clearFocus();
				mEditTextFour.setFocusable(false);
				mEditTextThree.setFocusableInTouchMode(true);
				mEditTextThree.getText().clear();
				mEditTextThree.requestFocus();
			} else if (mEditTextThree.isFocused()) {
				mEditTextThree.clearFocus();
				mEditTextThree.setFocusable(false);
				mEditTextTwo.setFocusableInTouchMode(true);
				mEditTextTwo.getText().clear();
				mEditTextTwo.requestFocus();
			} else if (mEditTextTwo.isFocused()) {
				mEditTextTwo.clearFocus();
				mEditTextTwo.setFocusable(false);
				mEditTextOne.setFocusableInTouchMode(true);
				mEditTextOne.getText().clear();
				mEditTextOne.requestFocus();
			}
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return false;

	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_identify_btn:
			String identify_one = mEditTextOne.getText().toString();
			String identify_two = mEditTextTwo.getText().toString();
			String identify_three = mEditTextThree.getText().toString();
			String identify_four = mEditTextFour.getText().toString();
			String identify_five = mEditTextFive.getText().toString();
			String identify_six = mEditTextSix.getText().toString();
			if (!"".equals(identify_one) && !"".equals(identify_two) && !"".equals(identify_three)
					&& !"".equals(identify_four) && !"".equals(identify_five) && !"".equals(identify_six)){
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mBtnNext.setBackgroundResource(R.drawable.icon_login_click);
					break;
				case MotionEvent.ACTION_UP:
					mBtnNext.setBackgroundResource(R.drawable.icon_login);
					break;
				default:
					break;
				}
			}
			break;
		}
		return false;
	}

}
