package cn.com.mobnote.golukmobile.promotion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;

public class PromotionActivity extends BaseActivity implements OnClickListener, IRequestResultListener, OnItemClickListener{
	// title
	private ImageButton btnBack;
	private ListView mListView;
	private PromotionDataAdapter mPromotionDataAdapter;
	public static final String PROMOTION_SELECTED_ITEM = "selected_item";
	private int selectId = -1;
	private CustomLoadingDialog mCustomProgressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sharepromotion);
		if (savedInstanceState == null) {
			selectId = getIntent().getIntExtra(PROMOTION_SELECTED_ITEM, -1);
		}
		initView();
		loadData();
	}

	public void loadData() {
		mCustomProgressDialog.show();
		PromotionListRequest request = new PromotionListRequest(IPageNotifyFn.PageType_GetPromotion, this);
		request.get(null);
	}

	public void initView() {
		// title
		mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.goluk_pull_to_refresh_footer_refreshing_label));
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mListView = (ListView) findViewById(R.id.promotion_list);
		mPromotionDataAdapter = new PromotionDataAdapter(this);
		mListView.setAdapter(mPromotionDataAdapter);
		mPromotionDataAdapter.setSelectId(selectId);
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
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			finish();
			break;
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
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		if (mPromotionDataAdapter.isEnabled(position)) {
			Intent intent = new Intent();
			PromotionSelectItem item = (PromotionSelectItem) mPromotionDataAdapter.getItem(position);
			intent.putExtra(PROMOTION_SELECTED_ITEM, item);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
