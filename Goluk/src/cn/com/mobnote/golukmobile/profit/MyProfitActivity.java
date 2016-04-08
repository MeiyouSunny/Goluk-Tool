package cn.com.mobnote.golukmobile.profit;

import cn.com.mobnote.application.GolukApplication;
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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我的收益
 * 
 * @author lily
 *
 */
public class MyProfitActivity extends BaseActivity implements OnClickListener, OnTouchListener, IRequestResultListener,
		ForbidBack {

	private ImageButton mBtnBack;
	private Button mBtnDetail, mBtnCash;
	private TextView mTextProblem;
	private TextView mTextTotalCount, mTextLeaveCount, mTextLastHint;
	private CustomTextView mTextLastCount;
	private RelativeLayout mProfitBgLayout;
	private ProfitJsonRequest profitJsonRequest = null;
	private ProfitInfo profitInfo = null;
	private AlertDialog mDialog = null;
	private LinearLayout mBottomLayout;
	private RelativeLayout mImageRefresh;
	/** 用户id **/
	private String uid, phone;
	/** 进入页面的loading **/
	private CustomLoadingDialog mLoadingDialog = null;
	/** 数据回调是否回来 **/
	private boolean mIsDataBack = false;
	/** 是否显示猛戳我刷新的图片 **/
	private boolean mIsVisibleImage = false;
	public static final String REQUEST_XIEYI = "100";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit);

		initView();
		GolukApplication app = GolukApplication.getInstance();
		if (null != app) {
			uid = GolukApplication.getInstance().mCurrentUId;
			phone = GolukApplication.getInstance().mCurrentPhoneNum;
		}
		showLoadingDialog();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mIsDataBack) {
			initData();
		}
	}

	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.profit_back);
		mBtnDetail = (Button) findViewById(R.id.my_profit_detail_btn);
		mBtnCash = (Button) findViewById(R.id.my_profit_leave_btn);
		mTextProblem = (TextView) findViewById(R.id.profit_problem);
		mTextLastCount = (CustomTextView) findViewById(R.id.last_profit);
		mTextTotalCount = (TextView) findViewById(R.id.my_profit_total_count);
		mTextLeaveCount = (TextView) findViewById(R.id.my_profit_leave_count);
		mTextLastHint = (TextView) findViewById(R.id.last_profit_no_hint);
		mProfitBgLayout = (RelativeLayout) findViewById(R.id.my_profit_bg_layout);
		mBottomLayout = (LinearLayout) findViewById(R.id.my_profit_bottom_layout);
		mImageRefresh = (RelativeLayout) findViewById(R.id.video_detail_click_refresh);
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
		if (null != uid || !"".equals(uid)) {
			profitJsonRequest = new ProfitJsonRequest(IPageNotifyFn.PageType_MyProfit, this);
			profitJsonRequest.get(uid, REQUEST_XIEYI);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 返回
		case R.id.profit_back:
			exit();
			break;
		// 明细
		case R.id.my_profit_detail_btn:
			Intent itDetail = new Intent(this, MyProfitDetailActivity.class);
			itDetail.putExtra("uid", uid);
			startActivity(itDetail);
			break;
		// 提现
		case R.id.my_profit_leave_btn:
			if (!UserUtils.isNetDeviceAvailable(this)) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
				return;
			}
			if (TextUtils.isEmpty(phone)) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_bind_phone_first));
				return;
			}
			Intent itCash = new Intent(this, UserOpenUrlActivity.class);
			itCash.putExtra(UserOpenUrlActivity.FROM_TAG, "cash");
			itCash.putExtra("isChangeUI", true);
			itCash.putExtra("uid", uid);
			itCash.putExtra("phone", phone);
			startActivity(itCash);
			break;
		// 常见问题
		case R.id.profit_problem:
			Intent itWeb = new Intent(MyProfitActivity.this, UserOpenUrlActivity.class);
			itWeb.putExtra(UserOpenUrlActivity.FROM_TAG, "profitProblem");
			startActivity(itWeb);
			break;
		// 收益为０时，点击跳转带有分享的相册页面
		case R.id.last_profit_no_hint:
			Intent photoalbum = new Intent(MyProfitActivity.this, PhotoAlbumActivity.class);
			photoalbum.putExtra("from", "cloud");
			startActivity(photoalbum);
			break;
		// 刷新
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
		mIsDataBack = false;
		mIsVisibleImage = false;
		this.finish();
	}

	/**
	 * 关闭提现按钮对话框
	 */
	private void closeAlertDialog() {
		if (null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		Drawable drawable = null;
		switch (view.getId()) {
		case R.id.my_profit_detail_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				drawable = getResources().getDrawable(R.drawable.profit_btn_detail_press);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				mBtnDetail.setCompoundDrawables(drawable, null, null, null);
				break;
			case MotionEvent.ACTION_UP:
				drawable = getResources().getDrawable(R.drawable.profit_btn_detail);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				mBtnDetail.setCompoundDrawables(drawable, null, null, null);
				break;
			default:
				break;
			}
			break;
		case R.id.my_profit_leave_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				drawable = getResources().getDrawable(R.drawable.profit_btn_cash_press);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				mBtnCash.setCompoundDrawables(drawable, null, null, null);
				break;
			case MotionEvent.ACTION_UP:
				drawable = getResources().getDrawable(R.drawable.profit_btn_cash);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				mBtnCash.setCompoundDrawables(drawable, null, null, null);
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
		if (requestType == IPageNotifyFn.PageType_MyProfit) {
			mIsDataBack = true;
			profitInfo = (ProfitInfo) result;
			if (null != profitInfo && profitInfo.success && null != profitInfo.data) {
				mIsVisibleImage = true;
				mProfitBgLayout.setVisibility(View.VISIBLE);
				mBottomLayout.setVisibility(View.VISIBLE);
				if (null == profitInfo.data.lgold || "".equals(profitInfo.data.lgold)
						|| "0".equals(profitInfo.data.lgold)) {
					profitInfo.data.lgold = "0";
					mProfitBgLayout.setBackgroundResource(R.drawable.profit_bg_orange);
					mTextLastHint.setVisibility(View.VISIBLE);
				} else {
					mProfitBgLayout.setBackgroundResource(R.drawable.profit_bg_blue);
					mTextLastHint.setVisibility(View.GONE);
				}
				if (null == profitInfo.data.hgold || "".equals(profitInfo.data.hgold)) {
					profitInfo.data.hgold = "0";
				}
				if (null == profitInfo.data.agold || "".equals(profitInfo.data.agold)) {
					profitInfo.data.agold = "0";
				}
				mTextLastCount.setText(UserUtils.formatNumber(profitInfo.data.lgold));
				mTextTotalCount.setText(UserUtils.formatNumber(profitInfo.data.hgold)
						+ this.getResources().getString(R.string.str_profit_unit));
				mTextLeaveCount.setText(UserUtils.formatNumber(profitInfo.data.agold)
						+ this.getResources().getString(R.string.str_profit_unit));
			} else {
				unusual();
			}
		}
	}

	// 显示loading
	private void showLoadingDialog() {
		if (null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(this, null);
			mLoadingDialog.show();
			mLoadingDialog.setListener(this);
		}
	}

	// 关闭loading
	private void closeLoadingDialog() {
		if (null != mLoadingDialog) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
	}

	/**
	 * 处理异常信息
	 */
	private void unusual() {
		if (!mIsVisibleImage) {
			mImageRefresh.setVisibility(View.VISIBLE);
			GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
			mIsDataBack = true;
		}
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeAlertDialog();
		closeLoadingDialog();
		mIsDataBack = false;
		mIsVisibleImage = false;
	}

}
