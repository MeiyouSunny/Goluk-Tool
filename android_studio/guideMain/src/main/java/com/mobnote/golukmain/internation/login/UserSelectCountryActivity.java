package com.mobnote.golukmain.internation.login;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;

public class UserSelectCountryActivity extends BaseActivity implements OnItemClickListener, OnClickListener {

	public static final String TAG = "UserSelectCountryActivity";

	private ListView mListView = null;
	private CountryListAdapter mCountryAdapter = null;
	private ArrayList<CountryBean> mDataList = null;
	private TextView mCancleTextView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setContentView(R.layout.activity_user_selectcountry);
		loadData();
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBaseApp.setContext(this, TAG);
	}

	private void initView() {
		mCancleTextView = (TextView) findViewById(R.id.tv_select_country_title_cancle);
		mListView = (ListView) findViewById(R.id.select_count_listview);
		mCountryAdapter = new CountryListAdapter(this);

		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mCountryAdapter);
		mCountryAdapter.notifyDataSetChanged();
		mCancleTextView.setOnClickListener(this);
	}

	private void loadData() {
		mDataList = GolukMobUtils.getCountryList();

		GolukDebugUtils.e("", "select country ---loadData:  " + mDataList.size());
	}

	public class CountryListAdapter extends BaseAdapter {
		private LayoutInflater mLayoutFlater = null;

		public CountryListAdapter(Context context) {
			mLayoutFlater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			final int count = (null == mDataList) ? 0 : mDataList.size();
			GolukDebugUtils.e("", "select country ---getCount:  " + count);
			return count;
		}

		@Override
		public Object getItem(int pos) {
			return mDataList.get(pos);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View contentView, ViewGroup parent) {
			GolukDebugUtils.e("", "select country ---getView:  ");
			ViewHolder viewHolder = null;
			if (null == contentView) {
				viewHolder = new ViewHolder();
				contentView = mLayoutFlater.inflate(R.layout.item_user_select_country, null);
				viewHolder.mCountryNameTv = (TextView) contentView.findViewById(R.id.select_country_name);
				viewHolder.mCodeTv = (TextView) contentView.findViewById(R.id.select_country_code);
			} else {
				viewHolder = (ViewHolder) contentView.getTag();
			}

			final CountryBean dataBean = mDataList.get(position);
			viewHolder.mCountryNameTv.setText(dataBean.name);
			viewHolder.mCodeTv.setText("+" + dataBean.code);

			contentView.setTag(viewHolder);

			return contentView;
		}

	}

	private static class ViewHolder {
		TextView mCountryNameTv;
		TextView mCodeTv;
	}

	@Override
	public void onItemClick(AdapterView<?> view, View arg1, int position, long arg3) {
		CountryBean bean = (CountryBean) view.getItemAtPosition(position);
		if (null != bean) {
			mBaseApp.mLocationCityCode = bean;
			GolukDebugUtils.e("", "onclick:  " + bean.toString());
			Intent it = new Intent();
			it.putExtra(InternationUserLoginActivity.COUNTRY_BEAN, bean);
			setResult(RESULT_OK, it);
		}
		finish();
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.tv_select_country_title_cancle){
			finish();
		}
	}
}
