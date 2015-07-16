package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 意见反馈
 * @author mobnote
 *
 */
public class UserOpinionActivity extends BaseActivity implements OnClickListener{

	/****/
	private GolukApplication mApp = null;
	private Context mContext = null;
	/**title**/
	private ImageButton mBtnBack = null;
	private TextView mTextTitle = null;
	private TextView mTextRight = null;
	/**意见/建议字数限制**/
	private TextView mTextSuggestCount = null;
	/**意见/建议EditText**/
	private EditText mEditSuggest = null;
	/**联系方式字数限制**/
	private TextView mTextConnectionCount = null;
	/**联系方式EditText**/
	private EditText mEditConnection = null;
	/**意见/建议最大字数限制**/
	private static final int MAX_SUGGEST_COUNT = 500;
	/**联系方式最大字数限制**/
	private static final int MAX_CONNECTION_COUNT = 70;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_opinion_layout);
		
		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		
		initView();
		//初始化字数限制
		int count_suggest = mEditSuggest.getText().toString().length();
		mTextSuggestCount.setText("（"+(MAX_SUGGEST_COUNT-count_suggest)+"/"+MAX_SUGGEST_COUNT+"）");
		int count_connection = mEditConnection.getText().toString().length();
		mTextConnectionCount.setText("（"+(MAX_CONNECTION_COUNT-count_connection)+"/"+MAX_CONNECTION_COUNT+"）");
	}
	
	//初始化
	public void initView(){
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mTextRight = (TextView) findViewById(R.id.user_title_right);
		mTextSuggestCount = (TextView) findViewById(R.id.opinion_layout_suggest_count);
		mEditSuggest = (EditText) findViewById(R.id.opinion_layout_suggest_edit);
		mTextConnectionCount = (TextView) findViewById(R.id.opinion_layout_connection_count);
		mEditConnection = (EditText) findViewById(R.id.opinion_layout_connection_edit);
		
		mTextTitle.setText("意见反馈");
		mTextRight.setText("发送");
		//监听
		mBtnBack.setOnClickListener(this);
		mTextRight.setOnClickListener(this);
		mEditSuggest.addTextChangedListener(mTextWatcher1);
		mEditConnection.addTextChangedListener(mTextWatcher2);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		//返回
		case R.id.back_btn:
			this.finish();
			break;
		//发送
		case R.id.user_title_right:
			GolukUtils.showToast(mContext, "发送。。。。");
			break;
		default:
			break;
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
			if(number < 0){
				number = 0;
			}
			mTextSuggestCount.setText("（"+number + "/"+MAX_SUGGEST_COUNT+"）");
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
			if(number < 0){
				number = 0;
			}
			mTextConnectionCount.setText("（"+number + "/"+MAX_CONNECTION_COUNT+"）");
		}
	};
}
