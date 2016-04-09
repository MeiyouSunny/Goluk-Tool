package com.mobnote.golukmain.carrecorder.base;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CarRecordBaseActivity extends BaseActivity implements OnClickListener {
	private TextView title;
	private RelativeLayout main;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_base);

		findViewById(R.id.back_btn).setOnClickListener(this);
		title = (TextView) findViewById(R.id.title);
		main = (RelativeLayout) findViewById(R.id.main);
	}

	public void setTitle(String titleName) {
		this.title.setText(titleName);
	}

	public void addContentView(View view) {
		this.main.addView(view);
	}

	protected void subExit() {

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.back_btn) {
			subExit();
			finish();
		} else {
		}
	}
}
