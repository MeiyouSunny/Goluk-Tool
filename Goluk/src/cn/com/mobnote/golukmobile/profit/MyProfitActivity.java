package cn.com.mobnote.golukmobile.profit;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 我的收益
 * @author lily
 *
 */
public class MyProfitActivity extends BaseActivity implements OnClickListener,OnTouchListener,IRequestResultListener{

	private ImageButton mBtnBack,mBtnDetail,mBtnCash;
	private TextView mTextProblem;
	private TextView mTextLastCount,mTextTotalCount,mTextLeaveCount;
	private ProfitJsonRequest profitJsonRequest = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit);
		
		initView();
		
		profitJsonRequest = new ProfitJsonRequest(IPageNotifyFn.PageType_MyProfit, this);
		profitJsonRequest.get("9f1ae807-8466-4f3d-bd3f-c7f77292b60b", "100");
		
	}
	
	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.profit_back);
		mBtnDetail = (ImageButton) findViewById(R.id.my_profit_detail_btn);
		mBtnCash = (ImageButton) findViewById(R.id.my_profit_leave_btn);
		mTextProblem = (TextView) findViewById(R.id.profit_problem);
		mTextLastCount = (TextView) findViewById(R.id.last_profit);
		mTextTotalCount = (TextView) findViewById(R.id.my_profit_total_count);
		mTextLeaveCount = (TextView) findViewById(R.id.my_profit_leave_count);
		
		mBtnBack.setOnClickListener(this);
		mBtnDetail.setOnClickListener(this);
		mBtnCash.setOnClickListener(this);
		mTextProblem.setOnClickListener(this);
		
		mBtnDetail.setOnTouchListener(this);
		mBtnCash.setOnTouchListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		//返回
		case R.id.profit_back:
			exit();
			break;
		//明细
		case R.id.my_profit_detail_btn:
			GolukUtils.showToast(this, "明细明细明细");
			Intent itDetail = new Intent(this,MyProfitDetailActivity.class);
			startActivity(itDetail);
			break;
		//提现
		case R.id.my_profit_leave_btn:
			GolukUtils.showToast(this, "提现提想提现");
			break;
		//常见问题
		case R.id.profit_problem:
			GolukUtils.showToast(this, "跳转web页面");
			break;
		default:
			break;
		}
	}
	
	private void exit() {
		this.finish();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.my_profit_detail_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnDetail.setBackgroundResource(R.drawable.profit_btn_detail_press);
				break;
			case MotionEvent.ACTION_UP:
				mBtnDetail.setBackgroundResource(R.drawable.profit_btn_detail);
				break;
			default:
				break;
			}
			break;
		case R.id.my_profit_leave_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnCash.setBackgroundResource(R.drawable.profit_btn_cash_press);
				break;
			case MotionEvent.ACTION_UP:
				mBtnCash.setBackgroundResource(R.drawable.profit_btn_cash);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if(requestType == IPageNotifyFn.PageType_MyProfit) {
			ProfitInfo profitInfo = (ProfitInfo)result;
			if (null != profitInfo && profitInfo.success && null != profitInfo.data) {
				mTextLastCount.setText(profitInfo.data.lgold+"个Ｇ币");
				mTextTotalCount.setText(profitInfo.data.hgold+"个Ｇ币");
				mTextLeaveCount.setText(profitInfo.data.agold+"个Ｇ币");
			} else {
				//TODO 异常处理
			}
			
		}
	}
}
