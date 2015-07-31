package cn.com.mobnote.golukmobile;

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
import cn.com.mobnote.user.UserUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 编辑昵称
 * 
 * @author mobnote
 *
 */
public class UserPersonalNameActivity extends BaseActivity implements OnClickListener {

	/** title **/
	private ImageButton btnBack;
	private TextView mTextTitle, mTextOk;
	/** body **/
	private EditText mEditName;
	private ImageView mImageNameRight;
	private String nameText;
	/** 文字字数提示 **/
	private TextView mTextCount = null;
	private TextView mTextCountAll = null;
	/** 最大输入字数 **/
	private static final int MAX_COUNT = 10;
	/** 输入超出的字数 **/
	private int number = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_personal_edit_name);

		initView();
		// title
		mTextTitle.setText("编辑昵称");
		mTextOk.setText("确认");
		//
		int count = mEditName.getText().toString().length();
		mTextCount.setText("" + (MAX_COUNT - count));
		mTextCountAll.setText("/" + MAX_COUNT + "）");

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

		/**
		 * 获取从编辑界面传来的姓名
		 */
		Intent it = getIntent();
		if (null != it.getStringExtra("intentNameText")) {
			Bundle bundle = it.getExtras();
			nameText = bundle.getString("intentNameText");
			GolukDebugUtils.i("lily", "----------UserPersonalNameActivity---------name=====" + nameText);
		}
		mEditName.setText(nameText);
		mEditName.setSelection(nameText.length());

		mEditName.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				 if(keyCode == KeyEvent.KEYCODE_ENTER){
					GolukDebugUtils.e("lily", "----------回车------KEYCODE_ENTER-------");
					mImageNameRight.setVisibility(View.GONE);
				}else{
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
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			UserUtils.hideSoftMethod(this);
			finish();
			break;
		// que认
		case R.id.user_title_right:
			if (number < 0) {
				UserUtils.showDialog(this, "请输入10个字符以内的有效昵称");
			} else {
				String name = mEditName.getText().toString();
				GolukDebugUtils.i("lily", "------UserPersonalNameActivity--修改昵称------" + name);
				if (name.trim().isEmpty()) {
					UserUtils.showDialog(this, "数据修改失败，昵称不能为空");
				} else {
					UserPersonalInfoActivity.clickBtn = true;
					UserUtils.hideSoftMethod(this);
					Intent it = new Intent(UserPersonalNameActivity.this, UserPersonalInfoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("itName", name);
					it.putExtras(bundle);
					this.setResult(1, it);
					this.finish();
				}
			}
			break;
		//
		case R.id.user_personal_name_image:
			// 点击清空
			mEditName.setText("");
			break;
		default:
			break;
		}
	}
	
	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			String name = mEditName.getText().toString();
			if("".equals(name) || null == name){
				mImageNameRight.setVisibility(View.GONE);
			}else{
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
			}else{
				mTextCount.setTextColor(getResources().getInteger(R.color.setting_right_text_color));
			}
			mTextCount.setText(number+"");
		}
	};

}
