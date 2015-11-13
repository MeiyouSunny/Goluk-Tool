package cn.com.mobnote.golukmobile.profit;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog.ForbidBack;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我的收益
 * @author lily
 *
 */
public class MyProfitActivity extends BaseActivity implements OnClickListener,OnTouchListener,IRequestResultListener, 
		ForbidBack{

	private ImageButton mBtnBack,mBtnDetail,mBtnCash;
	private TextView mTextProblem;
	private TextView mTextTotalCount,mTextLeaveCount,mTextLastHint;
	private CustomTextView mTextLastCount;
	private RelativeLayout mProfitBgLayout ;
	private ProfitJsonRequest profitJsonRequest = null;
	private ProfitInfo profitInfo = null;
	private AlertDialog mDialog = null;
	private LinearLayout mBottomLayout;
	private ImageView mImageRefresh;
	/**用户id**/
	private String uid;
	/**进入页面的loading**/
	private CustomLoadingDialog mLoadingDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit);
		
		initView();
		
		initData();
		
	}
	
	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.profit_back);
		mBtnDetail = (ImageButton) findViewById(R.id.my_profit_detail_btn);
		mBtnCash = (ImageButton) findViewById(R.id.my_profit_leave_btn);
		mTextProblem = (TextView) findViewById(R.id.profit_problem);
		mTextLastCount = (CustomTextView) findViewById(R.id.last_profit);
		mTextTotalCount = (TextView) findViewById(R.id.my_profit_total_count);
		mTextLeaveCount = (TextView) findViewById(R.id.my_profit_leave_count);
		mTextLastHint = (TextView) findViewById(R.id.last_profit_no_hint);
		mProfitBgLayout = (RelativeLayout) findViewById(R.id.my_profit_bg_layout);
		mBottomLayout = (LinearLayout) findViewById(R.id.my_profit_bottom_layout);
		mImageRefresh = (ImageView) findViewById(R.id.video_detail_click_refresh);
		mProfitBgLayout.setVisibility(View.GONE);
		mBottomLayout.setVisibility(View.GONE);
		
		mBtnBack.setOnClickListener(this);
		mBtnDetail.setOnClickListener(this);
		mBtnCash.setOnClickListener(this);
		mTextProblem.setOnClickListener(this);
		mImageRefresh.setOnClickListener(this);
		
		mBtnDetail.setOnTouchListener(this);
		mBtnCash.setOnTouchListener(this);
		mTextLastHint.setOnClickListener(this);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		Intent itUser = getIntent();
		uid = itUser.getStringExtra("uid").toString();
		if(null != uid || !"".equals(uid)) {
			showLoadingDialog();
			profitJsonRequest = new ProfitJsonRequest(IPageNotifyFn.PageType_MyProfit, this);
			profitJsonRequest.get(uid, "100");
		}
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
			Intent itDetail = new Intent(this, MyProfitDetailActivity.class);
			itDetail.putExtra("uid", uid);
			startActivity(itDetail);
			break;
		//提现
		case R.id.my_profit_leave_btn:
			clickCashBtn();
			break;
		//常见问题
		case R.id.profit_problem:
			Intent itWeb = new Intent(MyProfitActivity.this,UserOpenUrlActivity.class);
			itWeb.putExtra(UserOpenUrlActivity.FROM_TAG, "profitProblem");
			startActivity(itWeb);
			break;
		//收益为０时，点击跳转带有分享的相册页面
		case R.id.last_profit_no_hint:
			Intent photoalbum = new Intent(MyProfitActivity.this, PhotoAlbumActivity.class);
			photoalbum.putExtra("from", "cloud");
			startActivity(photoalbum);
			break;
		//刷新
		case R.id.video_detail_click_refresh:
			initData();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 退出
	 */
	private void exit() {
		closeAlertDialog();
		closeLoadingDialog();
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
			if("".equals(profitInfo.data.agold) || null == profitInfo.data.agold) {
				profitInfo.data.agold = "0";
			}
			int aGold = Integer.parseInt(profitInfo.data.agold);
			if(aGold <1000) {
				closeAlertDialog();
				mDialog = new AlertDialog.Builder(this).setTitle("提示")
						.setMessage(this.getResources().getString(R.string.my_profit_cash_less))
						.setNegativeButton("关闭", null)
						.setPositiveButton("赚Ｇ币", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent photoalbum = new Intent(MyProfitActivity.this, PhotoAlbumActivity.class);
								photoalbum.putExtra("from", "cloud");
								startActivity(photoalbum);
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
		closeLoadingDialog();
		mImageRefresh.setVisibility(View.GONE);
		if(requestType == IPageNotifyFn.PageType_MyProfit) {
			profitInfo = (ProfitInfo)result;
			if (null != profitInfo && profitInfo.success && null != profitInfo.data) {
				mProfitBgLayout.setVisibility(View.VISIBLE);
				mBottomLayout.setVisibility(View.VISIBLE);
				if(null == profitInfo.data.lgold || "".equals(profitInfo.data.lgold) || "0".equals(profitInfo.data.lgold)) {
					profitInfo.data.lgold = "0";
					mProfitBgLayout.setBackgroundResource(R.drawable.profit_bg_orange);
					mTextLastHint.setVisibility(View.VISIBLE);
				} else {
					mProfitBgLayout.setBackgroundResource(R.drawable.profit_bg_blue);
					mTextLastHint.setVisibility(View.GONE);
				}
				if(null == profitInfo.data.hgold || "".equals(profitInfo.data.hgold)) {
					profitInfo.data.hgold = "0";
				}
				if(null == profitInfo.data.agold || "".equals(profitInfo.data.agold)) {
					profitInfo.data.agold = "0";
				}
				mTextLastCount.setText(UserUtils.formatNumber(profitInfo.data.lgold));
				mTextTotalCount.setText(UserUtils.formatNumber(profitInfo.data.hgold)+"个Ｇ币");
				mTextLeaveCount.setText(UserUtils.formatNumber(profitInfo.data.agold)+"个Ｇ币");
			} else {
				//TODO 异常处理
				unusual();
			}
			
		}
	}
	
	//显示loading
	private void showLoadingDialog() {
		if(null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(this, null);
			mLoadingDialog.show();
			mLoadingDialog.setListener(this);
		}
	}
	//关闭loading
	private void closeLoadingDialog() {
		if(null != mLoadingDialog) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
	}
	
	/**
	 * 处理异常信息
	 */
	private void unusual() {
		mImageRefresh.setVisibility(View.VISIBLE);
		GolukUtils.showToast(this, "网络数据异常");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			exit();
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public void forbidBackKey(int backKey) {
		if (backKey == 1) {
			exit();
		}
	}
	
}
