package com.mobnote.golukmain.promotion;

import java.util.ArrayList;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.util.GolukUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class PromotionActivity extends BaseActivity implements OnClickListener, IRequestResultListener, OnItemClickListener, ForbidBack{
	// title
	private ImageButton btnBack;
	private ListView mListView;
	private PromotionDataAdapter mPromotionDataAdapter;
	public static final String PROMOTION_SELECTED_ITEM = "selected_item";
	public static final String PROMOTION_DATA = "promotion_data";
	private String mSelectedId;
	private ArrayList<PromotionData> mPromotionList;
	private CustomLoadingDialog mCustomProgressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sharepromotion);
		if (savedInstanceState == null) {
			mSelectedId = getIntent().getStringExtra(PROMOTION_SELECTED_ITEM);
			mPromotionList = (ArrayList<PromotionData>) getIntent().getSerializableExtra(PROMOTION_DATA);
		} else {
			mSelectedId = savedInstanceState.getString(PROMOTION_SELECTED_ITEM);
			mPromotionList = (ArrayList<PromotionData>) savedInstanceState.getSerializable(PROMOTION_DATA);
		}
		initView();
		loadData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		if (mPromotionList != null) {
			outState.putSerializable(PROMOTION_DATA, mPromotionList);
			outState.putString(PROMOTION_SELECTED_ITEM, mSelectedId);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mBaseApp.setContext(this, "PromotionActivity");
	}

	public void loadData() {
		if (mPromotionList == null) {
			mCustomProgressDialog.show();
			PromotionListRequest request = new PromotionListRequest(IPageNotifyFn.PageType_GetPromotion, this);
			request.get();
		} else {
			mPromotionDataAdapter.setData(mPromotionList);
			mPromotionDataAdapter.notifyDataSetChanged();
		}
	}

	public void initView() {
		// title
		mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.goluk_pull_to_refresh_footer_refreshing_label));
		mCustomProgressDialog.setListener(this);
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mListView = (ListView) findViewById(R.id.promotion_list);
		mPromotionDataAdapter = new PromotionDataAdapter(this);
		mListView.setAdapter(mPromotionDataAdapter);
		mPromotionDataAdapter.setSelectId(mSelectedId);
		mListView.setOnItemClickListener(this);
		btnBack.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.back_btn) {
			finish();
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
		switch(requestType) {
		case IPageNotifyFn.PageType_GetPromotion:
			PromotionModel data = (PromotionModel) result;
			if (data != null && data.success) {
				mPromotionDataAdapter.setData(data.data.PromotionList);
				mPromotionDataAdapter.notifyDataSetChanged();
			} else {
				GolukUtils.showToast(this, getString(R.string.user_net_unavailable));
				finish();
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		if (mPromotionDataAdapter.isEnabled(position)) {
			PromotionSelectItem item = (PromotionSelectItem) mPromotionDataAdapter.getItem(position);
			Intent intent = new Intent();
			intent.putExtra(PROMOTION_SELECTED_ITEM, item);
			EventBus.getDefault().post(item);

			finish();
		}
	}

	@Override
	public void forbidBackKey(int backKey) {
		// TODO Auto-generated method stub
		if (backKey == 1) {
			finish();
		}
	}
}
