package cn.com.mobnote.golukmobile.profit;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我的收益
 * @author lily
 *
 */
public class MyProfitActivity extends BaseActivity implements OnClickListener,OnTouchListener,IRequestResultListener{

	private ImageButton mBtnBack,mBtnDetail,mBtnCash;
	private TextView mTextProblem;
	private TextView mTextLastCount,mTextTotalCount,mTextLeaveCount,mTextLastHint;
	private RelativeLayout mProfitBgLayout ;
	private ProfitJsonRequest profitJsonRequest = null;
	private ProfitInfo profitInfo = null;
	private AlertDialog mDialog = null;
	
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
		mTextLastHint = (TextView) findViewById(R.id.last_profit_no_hint);
		mProfitBgLayout = (RelativeLayout) findViewById(R.id.my_profit_bg_layout);
		
		mBtnBack.setOnClickListener(this);
		mBtnDetail.setOnClickListener(this);
		mBtnCash.setOnClickListener(this);
		mTextProblem.setOnClickListener(this);
		
		mBtnDetail.setOnTouchListener(this);
		mBtnCash.setOnTouchListener(this);
		mTextLastHint.setOnClickListener(this);
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
			Intent itDetail = new Intent(this,MyProfitDetailActivity.class);
			startActivity(itDetail);
			break;
		//提现
		case R.id.my_profit_leave_btn:
			clickCashBtn();
			break;
		//常见问题
		case R.id.profit_problem:
			GolukUtils.showToast(this, "跳转web页面");
			break;
		//收益未０时，点击跳转带有分享的相册页面
		case R.id.last_profit_no_hint:
			GolukUtils.showToast(this, "跳转我的相册相册相册啊");
			break;
		default:
			break;
		}
	}
	
	/**
	 * 退出
	 */
	private void exit() {
		this.finish();
	}
	
	/**
	 * 关闭提现按钮对话框
	 */
	private void closeAlertDialog() {
		if(null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	/**
	 * 点击提现按钮
	 */
	private void clickCashBtn() {
		if (null != profitInfo && profitInfo.success && null != profitInfo.data) {
			int aGold = Integer.parseInt(profitInfo.data.agold);
			if(aGold <1000) {
				closeAlertDialog();
				mDialog = new AlertDialog.Builder(this).setTitle("提示")
						.setMessage(this.getResources().getString(R.string.my_profit_cash_less))
						.setNegativeButton("关闭", null)
						.setPositiveButton("赚Ｇ币", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent itPhoto = new Intent(MyProfitActivity.this,UserSetupActivity.class);
								startActivity(itPhoto);
							}
						}).create();
				mDialog.show();
			} else {
				closeAlertDialog();
				mDialog = new AlertDialog.Builder(this).setTitle("提示")
						.setMessage(this.getResources().getString(R.string.my_profit_cash_greater))
						.setPositiveButton("确定", null)
						.create();
				mDialog.show();
			}
		}
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
			profitInfo = (ProfitInfo)result;
			if (null != profitInfo && profitInfo.success && null != profitInfo.data) {
				if(null == profitInfo.data.lgold || "".equals(profitInfo.data.lgold) || "0".equals(profitInfo.data.lgold)) {
					mProfitBgLayout.setBackgroundResource(R.drawable.profit_bg_orange);
					mTextLastHint.setVisibility(View.VISIBLE);
				} else {
					mProfitBgLayout.setBackgroundResource(R.drawable.profit_bg_blue);
					mTextLastHint.setVisibility(View.GONE);
				}
				mTextLastCount.setText(profitInfo.data.lgold);
				mTextTotalCount.setText(GolukUtils.getFormatNumber(profitInfo.data.hgold)+"个Ｇ币");
				mTextLeaveCount.setText(GolukUtils.getFormatNumber(profitInfo.data.agold)+"个Ｇ币");
			} else {
				//TODO 异常处理
			}
			
		}
	}
}
