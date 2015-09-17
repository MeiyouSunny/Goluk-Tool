package cn.com.mobnote.golukmobile;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PushSettingActivity extends BaseActivity implements OnClickListener {

	private Button mCanCommentBtn = null;
	private Button mCanPariseBtn = null;

	/** 是否允许评论 */
	private boolean mIsCanComment = true;
	/** 是否允许点赞 */
	private boolean isCanParise = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setContentView(R.layout.pushsetting);
		initView();
	}

	private void initView() {
		mCanCommentBtn = (Button) findViewById(R.id.notify_setting_comment_btn);
		mCanPariseBtn = (Button) findViewById(R.id.notify_setting_prise_btn);
		mCanCommentBtn.setOnClickListener(this);
		mCanPariseBtn.setOnClickListener(this);
		// 赋初始值
		setCommentState(mIsCanComment);
		setPariseState(isCanParise);
	}

	private void setCommentState(boolean isOpen) {
		mIsCanComment = isOpen;
		if (isOpen) {
			mCanCommentBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mCanCommentBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	private void setPariseState(boolean isOpen) {
		isCanParise = isOpen;
		if (isOpen) {
			mCanPariseBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mCanPariseBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.notify_setting_comment_btn:
			setCommentState(!mIsCanComment);
			break;
		case R.id.notify_setting_prise_btn:
			setPariseState(!isCanParise);
			break;
		}
	}

	private void exit() {
		// 把当前的设置通知上报服务器
		this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
