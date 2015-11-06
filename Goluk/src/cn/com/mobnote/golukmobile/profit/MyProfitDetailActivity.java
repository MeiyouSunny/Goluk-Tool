package cn.com.mobnote.golukmobile.profit;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.module.page.IPageNotifyFn;

public class MyProfitDetailActivity extends BaseActivity implements OnClickListener,IRequestResultListener{

	private ImageButton mBtnBack;
	private RTPullListView mRTPullListView;
	private ProfitDetailRequest profitDetailRequest = null;
	private MyProfitDetailAdapter mAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit_detail);
		
		initView();
		
		profitDetailRequest = new ProfitDetailRequest(IPageNotifyFn.PageType_ProfitDetail, this);
		profitDetailRequest.get("9f1ae807-8466-4f3d-bd3f-c7f77292b60b", "0", "", "20", "100");
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

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if (requestType == IPageNotifyFn.PageType_ProfitDetail) {
			ProfitDetailInfo detailInfo = (ProfitDetailInfo) result;
			if (null != detailInfo && detailInfo.success && null != detailInfo.data) {
				mAdapter = new MyProfitDetailAdapter(this, detailInfo.data.incomelist);
				mRTPullListView.setAdapter(mAdapter);
			}
		}
	}
	
}
