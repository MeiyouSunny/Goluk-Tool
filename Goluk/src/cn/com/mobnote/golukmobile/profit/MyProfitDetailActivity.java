package cn.com.mobnote.golukmobile.profit;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;

public class MyProfitDetailActivity extends BaseActivity implements OnClickListener{

	private ImageButton mBtnBack;
	private RTPullListView mRTPullListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit_detail);
		
		initView();
	}
	
	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.profit_detail_back);
		mRTPullListView = (RTPullListView) findViewById(R.id.profit_detail_RTPullListView);
		
		mBtnBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.profit_detail_back:
			exit();
			break;
		default:
			break;
		}
	}
	
	private void exit() {
		this.finish();
	}
	
}
